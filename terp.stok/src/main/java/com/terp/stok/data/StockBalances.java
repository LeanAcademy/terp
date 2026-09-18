/*
 * Copyright (C) 2026 LeanAcademy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.terp.stok.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StockBalances {

    private StockBalances() {
    }

    public static double onHand(List<StockMovement> movements, String warehouseCode,
            String itemCode, Long excludeRowId) {
        double total = 0d;
        if (movements == null) {
            return total;
        }
        for (StockMovement row : movements) {
            if (row == null) {
                continue;
            }
            if (excludeRowId != null && excludeRowId.equals(row.getRowId())) {
                continue;
            }
            total += row.signedQuantityFor(warehouseCode, itemCode);
        }
        return total;
    }

    public static double onHandLot(List<StockMovement> movements, String warehouseCode,
            String itemCode, String lotNo) {
        double total = 0d;
        if (movements == null || lotNo == null || lotNo.isBlank()) {
            return total;
        }
        for (StockMovement row : movements) {
            if (row == null || lotNo.equals(row.getLotNo()) == false) {
                continue;
            }
            total += row.signedQuantityFor(warehouseCode, itemCode);
        }
        return total;
    }

    public static Double averageCost(List<StockMovement> movements, String warehouseCode, String itemCode) {
        if (movements == null || warehouseCode == null || itemCode == null) {
            return null;
        }
        List<StockMovement> ordered = new ArrayList<>();
        for (StockMovement row : movements) {
            if (row != null && itemCode.equals(row.getItemCode())) {
                ordered.add(row);
            }
        }
        ordered.sort(Comparator
                .comparing(StockMovement::getMovementDate, Comparator.nullsLast(Date::compareTo))
                .thenComparing(StockMovement::getRowId, Comparator.nullsLast(Long::compareTo)));
        double qty = 0d;
        double avg = 0d;
        for (StockMovement row : ordered) {
            double signed = row.signedQuantityFor(warehouseCode, itemCode);
            if (Math.abs(signed) < 0.0000001d) {
                continue;
            }
            if (signed > 0d) {
                double price = row.getUnitPrice() == null ? 0d : row.getUnitPrice();
                double next = qty + signed;
                if (next > 0.0000001d) {
                    avg = (qty * avg + signed * price) / next;
                }
                qty = next;
            } else {
                qty += signed;
                if (qty < 0d) {
                    qty = 0d;
                }
            }
        }
        return avg;
    }

    public static List<LotBalance> fefoLots(List<StockMovement> movements, String warehouseCode,
            String itemCode) {
        Map<String, LotBalance> lots = new LinkedHashMap<>();
        if (movements == null) {
            return List.of();
        }
        for (StockMovement row : movements) {
            if (row == null || row.getLotNo() == null || row.getLotNo().isBlank()) {
                continue;
            }
            double signed = row.signedQuantityFor(warehouseCode, itemCode);
            if (Math.abs(signed) < 0.0000001d) {
                continue;
            }
            LotBalance lot = lots.computeIfAbsent(row.getLotNo(), code -> {
                LotBalance created = new LotBalance();
                created.lotNo = code;
                created.quantity = 0d;
                return created;
            });
            lot.quantity += signed;
            if (row.getExpiryDate() != null
                    && (lot.expiryDate == null || row.getExpiryDate().before(lot.expiryDate))) {
                lot.expiryDate = row.getExpiryDate();
            }
        }
        List<LotBalance> result = new ArrayList<>();
        for (LotBalance lot : lots.values()) {
            if (lot.quantity > 0.0000001d) {
                result.add(lot);
            }
        }
        result.sort(Comparator.comparing((LotBalance lot) -> lot.expiryDate,
                Comparator.nullsLast(Date::compareTo)).thenComparing(lot -> lot.lotNo));
        return result;
    }

    public static class LotBalance {
        public String lotNo;
        public Date expiryDate;
        public double quantity;
    }

    public static List<StockBalanceRow> report(List<Warehouse> warehouses, List<Item> items,
            List<StockMovement> movements, boolean onlyNonZero, boolean onlyBelowMin) {
        return report(warehouses, items, movements, null, onlyNonZero, onlyBelowMin);
    }

    public static List<StockBalanceRow> report(List<Warehouse> warehouses, List<Item> items,
            List<StockMovement> movements, List<ItemWarehouseLevel> levels,
            boolean onlyNonZero, boolean onlyBelowMin) {
        List<StockBalanceRow> rows = new ArrayList<>();
        if (warehouses == null || items == null) {
            return rows;
        }
        Map<String, Double> totals = new LinkedHashMap<>();
        if (movements != null) {
            for (StockMovement movement : movements) {
                if (movement == null || movement.getItemCode() == null) {
                    continue;
                }
                add(totals, key(movement.getWarehouseCode(), movement.getItemCode()),
                        movement.signedQuantityFor(movement.getWarehouseCode(), movement.getItemCode()));
                if (movement.getMovementType() == StockMovement.TYPE_TRANSFER) {
                    add(totals, key(movement.getTargetWarehouseCode(), movement.getItemCode()),
                            movement.signedQuantityFor(movement.getTargetWarehouseCode(),
                                    movement.getItemCode()));
                }
            }
        }
        for (Warehouse warehouse : warehouses) {
            if (warehouse == null || warehouse.getWarehouseCode() == null) {
                continue;
            }
            for (Item item : items) {
                if (item == null || item.getItemId() == null) {
                    continue;
                }
                if (item.getItemType() == Item.TYPE_SERVICE) {
                    continue;
                }
                double qty = totals.getOrDefault(key(warehouse.getWarehouseCode(), item.getItemId()), 0d);
                Double min = warehouseMin(levels, warehouse.getWarehouseCode(), item.getItemId());
                if (min == null) {
                    min = item.getMinStock();
                }
                boolean belowMin = min != null && qty < min;
                if (onlyNonZero && qty == 0d && !belowMin) {
                    continue;
                }
                if (onlyBelowMin && !belowMin) {
                    continue;
                }
                Double unitCost = averageCost(movements, warehouse.getWarehouseCode(), item.getItemId());
                StockBalanceRow row = new StockBalanceRow();
                row.setWarehouseCode(warehouse.getWarehouseCode());
                row.setWarehouseName(warehouse.getWarehouseName());
                row.setItemCode(item.getItemId());
                row.setItemDesc(item.getItemDesc());
                row.setItemUnit(item.getItemUnit());
                row.setQuantity(qty);
                row.setMinStock(min);
                row.setMaxStock(item.getMaxStock());
                row.setUnitCost(unitCost);
                row.setStockValue(unitCost == null ? null : qty * unitCost);
                if (belowMin) {
                    row.setStatusLabel("Min. altı");
                } else if (item.getMaxStock() != null && qty > item.getMaxStock()) {
                    row.setStatusLabel("Max. üstü");
                } else if (qty == 0d) {
                    row.setStatusLabel("Sıfır");
                } else {
                    row.setStatusLabel("Normal");
                }
                rows.add(row);
            }
        }
        return rows;
    }

    private static Double warehouseMin(List<ItemWarehouseLevel> levels, String warehouseCode,
            String itemCode) {
        if (levels == null) {
            return null;
        }
        for (ItemWarehouseLevel level : levels) {
            if (level != null && warehouseCode.equals(level.getWarehouseCode())
                    && itemCode.equals(level.getItemCode()) && level.getMinStock() != null) {
                return level.getMinStock();
            }
        }
        return null;
    }

    private static void add(Map<String, Double> totals, String key, double delta) {
        if (key == null) {
            return;
        }
        totals.merge(key, delta, Double::sum);
    }

    private static String key(String warehouse, String item) {
        if (warehouse == null || item == null) {
            return null;
        }
        return warehouse + '\u0001' + item;
    }
}

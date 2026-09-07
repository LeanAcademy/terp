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

    public static List<StockBalanceRow> report(List<Warehouse> warehouses, List<Item> items,
            List<StockMovement> movements, boolean onlyNonZero, boolean onlyBelowMin) {
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
                Double min = item.getMinStock();
                boolean belowMin = min != null && qty < min;
                if (onlyNonZero && qty == 0d && !belowMin) {
                    continue;
                }
                if (onlyBelowMin && !belowMin) {
                    continue;
                }
                StockBalanceRow row = new StockBalanceRow();
                row.setWarehouseCode(warehouse.getWarehouseCode());
                row.setWarehouseName(warehouse.getWarehouseName());
                row.setItemCode(item.getItemId());
                row.setItemDesc(item.getItemDesc());
                row.setItemUnit(item.getItemUnit());
                row.setQuantity(qty);
                row.setMinStock(min);
                row.setMaxStock(item.getMaxStock());
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

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
package com.terp.plugin.data;

import com.terp.plugin.data.model.IStockItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Applies material-card rules on documents: tracking, unit conversion, list prices.
 */
public final class StockCard {

    private StockCard() {
    }

    public static String trackingError(IStockItem item, String lotNo, String serialNo) {
        if (item == null) {
            return null;
        }
        if (item.isLotTracked() && blank(lotNo)) {
            return item.getItemId() + " lot takibi gerektirir.";
        }
        if (item.isSerialTracked() && blank(serialNo)) {
            return item.getItemId() + " seri takibi gerektirir.";
        }
        return null;
    }

    public static double baseQuantity(IStockItem item, String unit, Double quantity) {
        double qty = quantity == null ? 0d : quantity;
        if (item == null || unit == null || unit.isBlank() || item.getItemUnit() == null) {
            return qty;
        }
        if (unit.equalsIgnoreCase(item.getItemUnit())) {
            return qty;
        }
        String second = item.getSecondUnit();
        Double factor = item.getUnitFactor();
        if (second != null && unit.equalsIgnoreCase(second) && factor != null && factor > 0d) {
            return qty * factor;
        }
        return qty;
    }

    public static Double fillPrice(IStockItem item, Double current, boolean purchase) {
        if (current != null) {
            return current;
        }
        if (item == null) {
            return null;
        }
        return purchase ? item.getPurchasePrice() : item.getSalesPrice();
    }

    public static List<String> units(IStockItem item) {
        List<String> units = new ArrayList<>();
        if (item == null) {
            return units;
        }
        if (!blank(item.getItemUnit())) {
            units.add(item.getItemUnit());
        }
        if (!blank(item.getSecondUnit()) && (units.isEmpty()
                || !item.getSecondUnit().equalsIgnoreCase(units.get(0)))) {
            units.add(item.getSecondUnit());
        }
        return units;
    }

    public static String minStockWarning(IStockItem item, double onHandAfter) {
        if (item == null || item.getMinStock() == null) {
            return null;
        }
        if (onHandAfter + 0.000001d < item.getMinStock()) {
            return item.getItemId() + " min. stok " + item.getMinStock()
                    + " altına düşecek (kalan " + onHandAfter + ").";
        }
        return null;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}

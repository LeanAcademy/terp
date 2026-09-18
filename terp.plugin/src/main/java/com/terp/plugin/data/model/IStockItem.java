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
package com.terp.plugin.data.model;

/**
 * Cross-plugin view of a material card. Concrete {@code Item} stays in terp.stok.
 */
public interface IStockItem {

    int TYPE_TRADE = 0;
    int TYPE_RAW = 1;
    int TYPE_SEMI = 2;
    int TYPE_FINISHED = 3;
    int TYPE_SERVICE = 4;
    int TYPE_CONSUMABLE = 5;

    String getItemId();

    String getItemDesc();

    String getItemUnit();

    default String getSecondUnit() {
        return null;
    }

    default Double getUnitFactor() {
        return null;
    }

    default boolean isLotTracked() {
        return false;
    }

    default boolean isSerialTracked() {
        return false;
    }

    default Double getPurchasePrice() {
        return null;
    }

    default Double getSalesPrice() {
        return null;
    }

    default Double getMinStock() {
        return null;
    }

    default Integer getShelfLifeDays() {
        return null;
    }

    int getItemType();

    int getStatus();

    Double getVatRate();

    /**
     * Percent used on sales lines when the card has no rate. Zero is a real rate.
     */
    default double vatPercent() {
        Double rate = getVatRate();
        return rate == null ? 20d : rate;
    }

    /**
     * Sales may ship trade goods and finished products only.
     */
    default boolean isSellable() {
        int type = getItemType();
        return type == TYPE_TRADE || type == TYPE_FINISHED;
    }

    default String getDisplayLabel() {
        String code = getItemId() == null ? "" : getItemId();
        String name = getItemDesc() == null ? "" : getItemDesc();
        if (code.isBlank()) {
            return name;
        }
        if (name.isBlank()) {
            return code;
        }
        return code + " — " + name;
    }
}

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

/**
 * Warehouse quantity direction. Business reasons (purchase, sales delivery)
 * are separate codes; balances still use these values.
 */
public final class StockDirection {

    public static final int IN = 0;
    public static final int OUT = 1;
    public static final int TRANSFER = 2;
    public static final int COUNT_IN = 3;
    public static final int COUNT_OUT = 4;
    public static final int SCRAP = 5;

    public static final String[] LABELS = {
        "Giriş", "Çıkış", "Transfer", "Sayım artış", "Sayım azalış", "Fire"
    };

    private StockDirection() {
    }

    public static String labelOf(int direction) {
        if (direction < 0 || direction >= LABELS.length) {
            return Integer.toString(direction);
        }
        return LABELS[direction];
    }
}

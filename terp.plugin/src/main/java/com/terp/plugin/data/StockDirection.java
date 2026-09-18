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
 * Warehouse quantity sign. Business meaning lives on the movement reason.
 * Stored values other than {@link #IN}/{@link #OUT} are folded by {@link #of(int)}.
 */
public final class StockDirection {

    public static final int IN = 0;
    public static final int OUT = 1;

    public static final String[] LABELS = {
        "Giriş", "Çıkış"
    };

    private StockDirection() {
    }

    public static int of(int direction) {
        if (direction == IN) {
            return IN;
        }
        if (direction == 3) {
            return IN;
        }
        if (direction == OUT || direction == 2 || direction == 4 || direction == 5) {
            return OUT;
        }
        return IN;
    }

    public static boolean isOut(int direction) {
        return of(direction) == OUT;
    }

    public static String labelOf(int direction) {
        int value = of(direction);
        return LABELS[value];
    }
}

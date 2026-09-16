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
 * Current-account debit/credit. Balances use these values.
 */
public final class AccountDirection {

    public static final int DEBIT = 0;
    public static final int CREDIT = 1;

    public static final String[] LABELS = {
        "Borç", "Alacak"
    };

    private AccountDirection() {
    }

    public static String labelOf(int direction) {
        if (direction < 0 || direction >= LABELS.length) {
            return Integer.toString(direction);
        }
        return LABELS[direction];
    }
}

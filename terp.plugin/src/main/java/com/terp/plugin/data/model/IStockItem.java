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

    int TYPE_SERVICE = 4;

    String getItemId();

    String getItemDesc();

    String getItemUnit();

    int getItemType();

    int getStatus();

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

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
package com.terp.plugin;

/**
 * CRUD flags for a menu program, taken from the current user's group.
 */
public final class FormRights {

    public final boolean add;
    public final boolean edit;
    public final boolean delete;

    private FormRights(boolean add, boolean edit, boolean delete) {
        this.add = add;
        this.edit = edit;
        this.delete = delete;
    }

    public static FormRights forMenu(String menuId) {
        IUser user = TerpApplication.getInstance().getUser();
        if (user == null) {
            return new FormRights(true, true, true);
        }
        return new FormRights(user.canAdd(menuId), user.canEdit(menuId), user.canDelete(menuId));
    }
}

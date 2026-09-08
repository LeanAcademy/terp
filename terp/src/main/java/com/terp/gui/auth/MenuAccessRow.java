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
package com.terp.gui.auth;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class MenuAccessRow {

    private final String menuId;
    private final String title;
    private final BooleanProperty view = new SimpleBooleanProperty();
    private final BooleanProperty add = new SimpleBooleanProperty();
    private final BooleanProperty edit = new SimpleBooleanProperty();
    private final BooleanProperty delete = new SimpleBooleanProperty();

    public MenuAccessRow(String menuId, String title, boolean view, boolean add,
            boolean edit, boolean delete) {
        this.menuId = menuId;
        this.title = title;
        this.view.set(view);
        this.add.set(add);
        this.edit.set(edit);
        this.delete.set(delete);
        this.add.addListener((obs, old, value) -> {
            if (Boolean.TRUE.equals(value)) {
                this.view.set(true);
            }
        });
        this.edit.addListener((obs, old, value) -> {
            if (Boolean.TRUE.equals(value)) {
                this.view.set(true);
            }
        });
        this.delete.addListener((obs, old, value) -> {
            if (Boolean.TRUE.equals(value)) {
                this.view.set(true);
            }
        });
        this.view.addListener((obs, old, value) -> {
            if (Boolean.FALSE.equals(value)) {
                this.add.set(false);
                this.edit.set(false);
                this.delete.set(false);
            }
        });
    }

    public String getMenuId() {
        return menuId;
    }

    public String getTitle() {
        return title;
    }

    public BooleanProperty viewProperty() {
        return view;
    }

    public BooleanProperty addProperty() {
        return add;
    }

    public BooleanProperty editProperty() {
        return edit;
    }

    public BooleanProperty deleteProperty() {
        return delete;
    }

    public boolean isView() {
        return view.get();
    }

    public boolean isAdd() {
        return add.get();
    }

    public boolean isEdit() {
        return edit.get();
    }

    public boolean isDelete() {
        return delete.get();
    }
}

/*
 * Copyright (C) 2026 LeanAcademy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.terp.plugin;

/**
 * Menu row declared by a plugin. The host writes {@code menu} if
 * {@code menuId} is missing. {@code programName} null means a folder.
 */
public final class PluginMenu {

    private final String menuId;
    private final String title;
    private final String parentMenuId;
    private final String programName;

    private PluginMenu(String menuId, String title, String parentMenuId, String programName) {
        this.menuId = menuId;
        this.title = title;
        this.parentMenuId = parentMenuId;
        this.programName = programName;
    }

    public static PluginMenu folder(String menuId, String title) {
        return new PluginMenu(menuId, title, null, null);
    }

    public static PluginMenu program(String menuId, String title, String parentMenuId,
            String programName) {
        return new PluginMenu(menuId, title, parentMenuId, programName);
    }

    public String getMenuId() {
        return menuId;
    }

    public String getTitle() {
        return title;
    }

    public String getParentMenuId() {
        return parentMenuId;
    }

    public String getProgramName() {
        return programName;
    }

    public boolean isFolder() {
        return programName == null || programName.isBlank();
    }
}

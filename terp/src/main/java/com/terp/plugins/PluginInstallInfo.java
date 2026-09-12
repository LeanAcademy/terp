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
package com.terp.plugins;

/**
 * One row in Sistem yönetimi → Eklenti yönetimi: disk JAR merged with {@code eklenti}.
 */
public class PluginInstallInfo {

    public static final String STATUS_LOADED = "Yüklü";
    public static final String STATUS_MISSING_JAR = "JAR yok";
    public static final String STATUS_MISSING_ROW = "kayıt yok";

    private String pluginName;
    private String pluginVersion;
    private int type;
    private String jarFileName;
    private boolean hasJar;
    private boolean hasRegistry;

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTypeLabel() {
        return type == 1 ? "Çekirdek" : "Eklenti";
    }

    public String getJarFileName() {
        return jarFileName;
    }

    public void setJarFileName(String jarFileName) {
        this.jarFileName = jarFileName;
    }

    public boolean isHasJar() {
        return hasJar;
    }

    public void setHasJar(boolean hasJar) {
        this.hasJar = hasJar;
    }

    public boolean isHasRegistry() {
        return hasRegistry;
    }

    public void setHasRegistry(boolean hasRegistry) {
        this.hasRegistry = hasRegistry;
    }

    public String getStatus() {
        if (hasJar && hasRegistry) {
            return STATUS_LOADED;
        }
        if (hasJar) {
            return STATUS_MISSING_ROW;
        }
        return STATUS_MISSING_JAR;
    }

    public boolean isRemovable() {
        return type != 1 && pluginName != null && !pluginName.isBlank();
    }
}

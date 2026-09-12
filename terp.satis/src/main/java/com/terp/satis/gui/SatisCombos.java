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
package com.terp.satis.gui;

import java.util.List;
import javafx.scene.control.ComboBox;

final class SatisCombos {

    private SatisCombos() {
    }

    static String codeOf(ComboBox<String> combo) {
        String value = combo.getValue();
        if (value == null || value.isBlank()) {
            value = combo.getEditor() == null ? null : combo.getEditor().getText();
        }
        if (value == null || value.isBlank()) {
            return null;
        }
        int sep = value.indexOf(" — ");
        String code = sep > 0 ? value.substring(0, sep) : value;
        String trimmed = code.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    static String nameOf(ComboBox<String> combo) {
        String value = combo.getValue();
        if (value == null || value.isBlank()) {
            value = combo.getEditor() == null ? null : combo.getEditor().getText();
        }
        if (value == null) {
            return null;
        }
        int sep = value.indexOf(" — ");
        if (sep < 0) {
            return null;
        }
        String name = value.substring(sep + 3).trim();
        return name.isEmpty() ? null : name;
    }

    static void select(ComboBox<String> combo, String code) {
        if (code == null || code.isBlank()) {
            combo.getSelectionModel().clearSelection();
            if (combo.getEditor() != null) {
                combo.getEditor().clear();
            }
            return;
        }
        for (String item : combo.getItems()) {
            if (item != null && (item.equals(code) || item.startsWith(code + " — "))) {
                combo.getSelectionModel().select(item);
                return;
            }
        }
        combo.setValue(code);
    }

    static void fill(ComboBox<String> combo, List<String> labels) {
        combo.getItems().clear();
        if (labels == null) {
            return;
        }
        combo.getItems().addAll(labels);
    }
}

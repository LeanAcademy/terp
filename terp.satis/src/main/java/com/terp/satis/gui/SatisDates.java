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

import com.terp.plugin.data.DocumentNumbers;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;

final class SatisDates {

    private SatisDates() {
    }

    static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return LocalDate.now();
        }
        if (date instanceof java.sql.Date sql) {
            return sql.toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    static Date toDate(LocalDate local) {
        if (local == null) {
            return new Date();
        }
        return Date.from(local.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    static Double parseDouble(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(text.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    static String empty(String value) {
        return value == null ? "" : value;
    }

    static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    static String assignDocumentNo(String documentType, TextField field) {
        String assigned = DocumentNumbers.assign(documentType,
                field == null ? null : trimToNull(field.getText()));
        if (field != null && assigned != null && !assigned.isBlank()) {
            field.setText(assigned);
        }
        return assigned;
    }

    static void showError(String title, String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();
    }

    static boolean appendLike(StringBuilder sql, boolean whereAdded, String field, String value) {
        if (value == null || value.isBlank()) {
            return whereAdded;
        }
        if (!whereAdded) {
            sql.append(" where");
        } else {
            sql.append(" and");
        }
        sql.append(' ').append(field).append(" like '")
                .append(value.replace("'", "''")).append('\'');
        return true;
    }

    static FXMLLoader pluginLoader(Class<?> owner, String fxml) {
        FXMLLoader loader = new FXMLLoader(owner.getResource(fxml));
        loader.setClassLoader(owner.getClassLoader());
        return loader;
    }
}

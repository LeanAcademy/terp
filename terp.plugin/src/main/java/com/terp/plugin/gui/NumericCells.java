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
package com.terp.plugin.gui;

import java.util.Locale;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

/**
 * Two-decimal, right-aligned numeric display for tables and document fields.
 */
public final class NumericCells {

    private static final Locale TR = Locale.forLanguageTag("tr-TR");
    private static final String ALIGN = "-fx-alignment: CENTER-RIGHT;";

    private NumericCells() {
    }

    public static String format(Number value) {
        if (value == null) {
            return "";
        }
        return String.format(TR, "%,.2f", value.doubleValue());
    }

    public static String text(Number value) {
        return format(value);
    }

    public static Double parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.contains(",") && trimmed.contains(".")) {
            trimmed = trimmed.replace(".", "").replace(',', '.');
        } else {
            trimmed = trimmed.replace(',', '.');
        }
        try {
            return Double.valueOf(trimmed);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static StringConverter<Double> converter() {
        return new StringConverter<>() {
            @Override
            public String toString(Double value) {
                return format(value);
            }

            @Override
            public Double fromString(String text) {
                return parse(text);
            }
        };
    }

    @SafeVarargs
    public static <S> void apply(TableColumn<S, Double>... columns) {
        if (columns == null) {
            return;
        }
        for (TableColumn<S, Double> column : columns) {
            applyOne(column, false);
        }
    }

    public static <S> void applyEditable(TableColumn<S, Double> column) {
        applyOne(column, true);
    }

    public static void align(TextInputControl... fields) {
        if (fields == null) {
            return;
        }
        for (TextInputControl field : fields) {
            if (field instanceof TextField textField) {
                textField.setAlignment(Pos.CENTER_RIGHT);
            }
        }
    }

    private static <S> void applyOne(TableColumn<S, Double> column, boolean editable) {
        if (column == null) {
            return;
        }
        column.setStyle(ALIGN);
        if (editable) {
            StringConverter<Double> converter = converter();
            column.setCellFactory(col -> {
                TextFieldTableCell<S, Double> cell = new TextFieldTableCell<>(converter);
                cell.setAlignment(Pos.CENTER_RIGHT);
                cell.setStyle(ALIGN);
                return cell;
            });
            return;
        }
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : format(item));
                setAlignment(Pos.CENTER_RIGHT);
                setStyle(ALIGN);
            }
        });
    }
}

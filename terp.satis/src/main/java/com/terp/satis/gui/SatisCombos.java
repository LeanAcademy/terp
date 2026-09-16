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

import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.IVatRateLookup;
import com.terp.plugin.data.model.IStockItem;
import com.terp.plugin.data.model.IVatRate;
import java.util.ArrayList;
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

    static void fillSellableItems(ComboBox<String> combo, List<IStockItem> rows) {
        List<String> labels = new ArrayList<>();
        if (rows != null) {
            for (IStockItem item : rows) {
                if (item != null && item.isSellable()) {
                    labels.add(item.getDisplayLabel());
                }
            }
        }
        fill(combo, labels);
    }

    static void fillVatRates(ComboBox<String> combo) {
        List<String> labels = new ArrayList<>();
        IVatRateLookup lookup = TerpApplication.getInstance().getVatRateLookup();
        if (lookup != null) {
            List<IVatRate> rows = lookup.findActive();
            if (rows != null) {
                for (IVatRate rate : rows) {
                    if (rate != null) {
                        labels.add(rate.getDisplayLabel());
                    }
                }
            }
        }
        fill(combo, labels);
    }

    static void selectVat(ComboBox<String> combo, Double rate) {
        IVatRateLookup lookup = TerpApplication.getInstance().getVatRateLookup();
        IVatRate match = lookup == null ? null : lookup.findByRate(rate == null ? 20d : rate);
        if (match != null) {
            select(combo, match.getRateCode());
            return;
        }
        if (rate == null) {
            combo.getSelectionModel().clearSelection();
            return;
        }
        String label = "%" + rate;
        if (label.endsWith(".0")) {
            label = label.substring(0, label.length() - 2);
        }
        if (!combo.getItems().contains(label)) {
            combo.getItems().add(label);
        }
        combo.setValue(label);
    }

    static Double vatRateOf(ComboBox<String> combo) {
        String code = codeOf(combo);
        if (code == null) {
            return null;
        }
        IVatRateLookup lookup = TerpApplication.getInstance().getVatRateLookup();
        IVatRate byCode = lookup == null ? null : lookup.findByCode(code);
        if (byCode != null && byCode.getRate() != null) {
            return byCode.getRate();
        }
        String numeric = code.startsWith("%") ? code.substring(1) : code;
        try {
            return Double.valueOf(numeric.replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

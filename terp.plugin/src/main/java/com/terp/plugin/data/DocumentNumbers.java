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

import com.terp.plugin.TerpApplication;
import java.util.List;

/**
 * Document type codes and a facade over {@link IDocumentNumbers}.
 */
public final class DocumentNumbers {

    public static final String PURCHASE_REQUEST = "SATIN_TALEP";
    public static final String PURCHASE_ORDER = "SATIN_SIPARIS";
    public static final String PURCHASE_RECEIPT = "SATIN_KABUL";
    public static final String STOCK_COUNT = "STOK_SAYIM";
    public static final String SALES_DELIVERY = "SATIS_IRS";

    public static final int DEFAULT_PADDING = 6;

    private DocumentNumbers() {
    }

    public static List<Spec> catalog() {
        return List.of(
                new Spec(PURCHASE_REQUEST, "Satınalma talebi", "TAL-"),
                new Spec(PURCHASE_ORDER, "Satınalma siparişi", "SIP-"),
                new Spec(PURCHASE_RECEIPT, "Mal kabul", "MK-"),
                new Spec(STOCK_COUNT, "Stok sayım", "SYM-"),
                new Spec(SALES_DELIVERY, "Satış irsaliyesi", "IRS-"));
    }

    public static Spec specOf(String documentType) {
        if (documentType == null) {
            return null;
        }
        for (Spec spec : catalog()) {
            if (documentType.equals(spec.type())) {
                return spec;
            }
        }
        return null;
    }

    public static String labelOf(String documentType) {
        Spec spec = specOf(documentType);
        return spec == null ? documentType : spec.label();
    }

    public static String defaultPrefix(String documentType) {
        Spec spec = specOf(documentType);
        return spec == null ? "" : spec.prefix();
    }

    /**
     * Allocate from the series when the field is blank; otherwise keep the typed number.
     */
    public static String assign(String documentType, String suggested) {
        String typed = suggested == null ? null : suggested.trim();
        if (typed != null && typed.isEmpty()) {
            typed = null;
        }
        IDocumentNumbers numbers = TerpApplication.getInstance().getDocumentNumbers();
        if (numbers == null) {
            return typed;
        }
        try {
            return numbers.allocate(documentType, typed);
        } catch (RuntimeException ex) {
            return typed;
        }
    }

    public record Spec(String type, String label, String prefix) {
    }
}

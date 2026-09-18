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
package com.terp.core.data;

import com.terp.plugin.CompanyScope;
import com.terp.plugin.data.ICommonDao;
import java.util.Date;
import java.util.List;

public final class DocumentTemplates {

    private static final String STOCK_LABEL_BODY = """
            {ITEM_CODE} {ITEM_DESC}
            Barkod: {BARCODE}
            Birim: {UNIT}
            """;

    private static final String SALES_INVOICE_BODY = """
            Fatura {DOCUMENT_NO}
            Cari: {ACCOUNT}
            Tarih: {DATE}
            {LINES}
            Toplam: {TOTAL}
            """;

    private DocumentTemplates() {
    }

    public static void ensureSeeded(ICommonDao<DocumentTemplate> dao) {
        if (dao == null || !CompanyScope.hasCompany()) {
            return;
        }
        List<DocumentTemplate> existing = dao.findAll(CompanyScope.from("DocumentTemplate"));
        if (existing != null && !existing.isEmpty()) {
            return;
        }
        Date now = new Date();
        seed(dao, DocumentTemplate.TYPE_STOCK_LABEL, "Stok etiketi",
                DocumentTemplate.TYPE_STOCK_LABEL, STOCK_LABEL_BODY, now);
        seed(dao, DocumentTemplate.TYPE_SALES_INVOICE, "Satış faturası",
                DocumentTemplate.TYPE_SALES_INVOICE, SALES_INVOICE_BODY, now);
    }

    private static void seed(ICommonDao<DocumentTemplate> dao, String code, String name,
            String documentType, String body, Date now) {
        DocumentTemplate row = dao.getEmpty();
        if (row == null) {
            return;
        }
        CompanyScope.stamp(row);
        row.setTemplateCode(code);
        row.setTemplateName(name);
        row.setDocumentType(documentType);
        row.setBody(body);
        row.setStatus(DocumentTemplate.STATUS_ACTIVE);
        row.setAddedDate(now);
        row.setLastUpdateDate(now);
        dao.addOrUpdate(row);
    }
}

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
import com.terp.plugin.data.DocumentNumbers;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.IDocumentNumbers;
import java.util.Date;
import java.util.List;

public class DocumentNumbering implements IDocumentNumbers {

    private final ICommonDao<DocumentSeries> dao;

    public DocumentNumbering(ICommonDao<DocumentSeries> dao) {
        this.dao = dao;
    }

    public void ensureSeeded() {
        if (dao == null || !CompanyScope.hasCompany()) {
            return;
        }
        for (DocumentNumbers.Spec spec : DocumentNumbers.catalog()) {
            findOrCreate(spec.type());
        }
    }

    @Override
    public synchronized String allocate(String documentType, String suggested) {
        if (suggested != null && !suggested.isBlank()) {
            return suggested.trim();
        }
        if (documentType == null || documentType.isBlank()) {
            throw new IllegalArgumentException("Belge türü boş");
        }
        if (!CompanyScope.hasCompany()) {
            throw new IllegalStateException("Firma seçilmedi");
        }
        DocumentSeries series = findOrCreate(documentType);
        long next = series.getLastNumber() + 1;
        series.setLastNumber(next);
        Date now = new Date();
        series.setLastUpdateDate(now);
        if (series.getAddedDate() == null) {
            series.setAddedDate(now);
        }
        dao.addOrUpdate(series);
        return DocumentSeries.format(series.getPrefix(), series.getPadding(), next);
    }

    private DocumentSeries findOrCreate(String documentType) {
        String escaped = documentType.replace("'", "''");
        DocumentSeries existing = dao.firstOrDefault(
                "from DocumentSeries e where " + CompanyScope.predicate("e")
                        + " and e.documentType = '" + escaped + "'");
        if (existing != null) {
            return existing;
        }
        DocumentSeries created = dao.getEmpty();
        if (created == null) {
            created = new DocumentSeries();
        }
        Date now = new Date();
        CompanyScope.stamp(created);
        created.setDocumentType(documentType);
        created.setPrefix(DocumentNumbers.defaultPrefix(documentType));
        created.setPadding(DocumentNumbers.DEFAULT_PADDING);
        created.setLastNumber(0L);
        created.setAddedDate(now);
        created.setLastUpdateDate(now);
        DocumentSeries saved = dao.addOrUpdate(created);
        return saved == null ? created : saved;
    }

    public List<DocumentSeries> list() {
        ensureSeeded();
        if (dao == null) {
            return List.of();
        }
        List<DocumentSeries> rows = dao.findAll(CompanyScope.from("DocumentSeries")
                + " order by e.documentType");
        return rows == null ? List.of() : rows;
    }
}

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
import com.terp.plugin.data.IDocumentTemplates;
import com.terp.plugin.data.model.IDocumentTemplate;
import java.util.List;

public class DocumentTemplateLookup implements IDocumentTemplates {

    private final ICommonDao<DocumentTemplate> dao;

    public DocumentTemplateLookup(ICommonDao<DocumentTemplate> dao) {
        this.dao = dao;
    }

    @Override
    public IDocumentTemplate findByType(String documentType) {
        if (documentType == null || documentType.isBlank()) {
            return null;
        }
        String wanted = documentType.trim();
        DocumentTemplates.ensureSeeded(dao);
        List<DocumentTemplate> rows = dao.findAll(CompanyScope.from("DocumentTemplate")
                + " order by e.templateCode");
        if (rows == null) {
            return null;
        }
        for (DocumentTemplate row : rows) {
            if (row == null || row.getStatus() != DocumentTemplate.STATUS_ACTIVE) {
                continue;
            }
            if (row.getDocumentType() != null
                    && wanted.equalsIgnoreCase(row.getDocumentType().trim())) {
                return row;
            }
        }
        return null;
    }
}

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
package com.terp.plugin;

import com.terp.plugin.data.ICompanyScoped;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.model.ICompany;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Active-company filter for plugin masters ({@code firma_ref}).
 */
public final class CompanyScope {

    private CompanyScope() {
    }

    public static Long currentId() {
        ICompany company = TerpApplication.getInstance().getCurrentCompany();
        return company == null ? null : company.getRowId();
    }

    public static boolean hasCompany() {
        return currentId() != null;
    }

    public static boolean belongs(Long companyId) {
        Long current = currentId();
        return current != null && current.equals(companyId);
    }

    public static boolean stamp(ICompanyScoped row) {
        Long id = currentId();
        if (id == null || row == null) {
            return false;
        }
        row.setCompanyId(id);
        return true;
    }

    public static boolean requireCompany() {
        if (hasCompany()) {
            return true;
        }
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Firma");
        alert.setHeaderText("Firma seçilmedi");
        alert.setContentText("Önce araç çubuğundan çalışma firmasını seçin.");
        alert.show();
        return false;
    }

    public static String predicate(String alias) {
        Long id = currentId();
        if (id == null) {
            return "1=0";
        }
        return alias + ".companyId = " + id;
    }

    public static String from(String entityName) {
        StringBuilder sql = new StringBuilder("from ").append(entityName).append(" e");
        append(sql, false);
        return sql.toString();
    }

    public static boolean append(StringBuilder sql, boolean whereAdded) {
        if (!whereAdded) {
            sql.append(" where ");
        } else {
            sql.append(" and ");
        }
        sql.append(predicate("e"));
        return true;
    }

    public static String existingCode(String entityName, String field, String code, Long excludeRowId) {
        if (code == null || code.isBlank() || currentId() == null) {
            return null;
        }
        String escaped = code.replace("'", "''");
        StringBuilder sql = new StringBuilder("from ").append(entityName).append(" e where ")
                .append(predicate("e"))
                .append(" and e.").append(field).append(" = '").append(escaped).append("'");
        if (excludeRowId != null) {
            sql.append(" and e.rowId <> ").append(excludeRowId);
        }
        return sql.toString();
    }

    public static <T> T findDuplicate(ICommonDao<T> dao, String entityName, String field,
            String code, Long excludeRowId) {
        String hql = existingCode(entityName, field, code, excludeRowId);
        if (hql == null || dao == null) {
            return null;
        }
        return dao.firstOrDefault(hql);
    }

    public static boolean rejectDuplicate(Object existing, String header) {
        if (existing == null) {
            return false;
        }
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Kayıt hatası");
        alert.setHeaderText(header);
        alert.setContentText("Bu kod bu firmada zaten kullanılıyor.");
        alert.show();
        return true;
    }
}

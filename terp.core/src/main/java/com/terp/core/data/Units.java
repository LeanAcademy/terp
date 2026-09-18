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

public final class Units {

    private Units() {
    }

    public static void ensureSeeded(ICommonDao<UnitOfMeasure> dao) {
        if (dao == null || !CompanyScope.hasCompany()) {
            return;
        }
        List<UnitOfMeasure> existing = dao.findAll(CompanyScope.from("UnitOfMeasure"));
        if (existing != null && !existing.isEmpty()) {
            return;
        }
        Date now = new Date();
        seed(dao, "ADET", "Adet", now);
        seed(dao, "KOLI", "Koli", now);
        seed(dao, "KG", "Kilogram", now);
    }

    private static void seed(ICommonDao<UnitOfMeasure> dao, String code, String name, Date now) {
        UnitOfMeasure row = dao.getEmpty();
        if (row == null) {
            return;
        }
        CompanyScope.stamp(row);
        row.setUnitCode(code);
        row.setUnitName(name);
        row.setStatus(UnitOfMeasure.STATUS_ACTIVE);
        row.setAddedDate(now);
        row.setLastUpdateDate(now);
        dao.addOrUpdate(row);
    }
}

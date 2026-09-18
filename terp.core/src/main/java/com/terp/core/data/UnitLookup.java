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
import com.terp.plugin.data.IUnitLookup;
import com.terp.plugin.data.model.IUnitOfMeasure;
import java.util.ArrayList;
import java.util.List;

public class UnitLookup implements IUnitLookup {

    private final ICommonDao<UnitOfMeasure> dao;

    public UnitLookup(ICommonDao<UnitOfMeasure> dao) {
        this.dao = dao;
    }

    @Override
    public List<IUnitOfMeasure> findActive() {
        Units.ensureSeeded(dao);
        List<UnitOfMeasure> rows = dao.findAll(CompanyScope.from("UnitOfMeasure") + " order by e.unitCode");
        List<IUnitOfMeasure> result = new ArrayList<>();
        if (rows == null) {
            return result;
        }
        for (UnitOfMeasure row : rows) {
            if (row != null && row.getStatus() == UnitOfMeasure.STATUS_ACTIVE) {
                result.add(row);
            }
        }
        return result;
    }

    @Override
    public IUnitOfMeasure findByCode(String unitCode) {
        if (unitCode == null || unitCode.isBlank()) {
            return null;
        }
        Units.ensureSeeded(dao);
        String escaped = unitCode.replace("'", "''");
        return dao.firstOrDefault("from UnitOfMeasure e where " + CompanyScope.predicate("e")
                + " and e.unitCode = '" + escaped + "'");
    }
}

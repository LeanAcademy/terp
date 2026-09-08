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

import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.ICompanyLookup;
import com.terp.plugin.data.model.ICompany;
import java.util.ArrayList;
import java.util.List;

public class CompanyLookup implements ICompanyLookup {

    private final ICommonDao<Company> dao;

    public CompanyLookup(ICommonDao<Company> dao) {
        this.dao = dao;
    }

    @Override
    public List<ICompany> findActive() {
        List<Company> rows = dao.findAll();
        List<ICompany> result = new ArrayList<>();
        if (rows == null) {
            return result;
        }
        for (Company row : rows) {
            if (row != null && row.getStatus() == 0) {
                result.add(row);
            }
        }
        return result;
    }

    @Override
    public ICompany findById(Long companyId) {
        if (companyId == null) {
            return null;
        }
        return dao.firstOrDefault(companyId);
    }
}

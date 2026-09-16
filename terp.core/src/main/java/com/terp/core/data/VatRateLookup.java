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
import com.terp.plugin.data.IVatRateLookup;
import com.terp.plugin.data.model.IVatRate;
import java.util.ArrayList;
import java.util.List;

public class VatRateLookup implements IVatRateLookup {

    private final ICommonDao<VatRate> dao;

    public VatRateLookup(ICommonDao<VatRate> dao) {
        this.dao = dao;
    }

    @Override
    public List<IVatRate> findActive() {
        VatRates.ensureSeeded(dao);
        List<VatRate> rows = dao.findAll(CompanyScope.from("VatRate") + " order by e.rate");
        List<IVatRate> result = new ArrayList<>();
        if (rows == null) {
            return result;
        }
        for (VatRate row : rows) {
            if (row != null && row.getStatus() == VatRate.STATUS_ACTIVE) {
                result.add(row);
            }
        }
        return result;
    }

    @Override
    public IVatRate findByCode(String rateCode) {
        if (rateCode == null || rateCode.isBlank()) {
            return null;
        }
        VatRates.ensureSeeded(dao);
        String escaped = rateCode.replace("'", "''");
        return dao.firstOrDefault("from VatRate e where " + CompanyScope.predicate("e")
                + " and e.rateCode = '" + escaped + "'");
    }

    @Override
    public IVatRate findByRate(Double rate) {
        if (rate == null) {
            return null;
        }
        VatRates.ensureSeeded(dao);
        List<IVatRate> rows = findActive();
        for (IVatRate row : rows) {
            if (row != null && row.getRate() != null && Math.abs(row.getRate() - rate) < 0.0001d) {
                return row;
            }
        }
        return null;
    }
}

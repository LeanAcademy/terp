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
import com.terp.plugin.data.model.IVatRate;
import java.util.Date;
import java.util.List;

public final class VatRates {

    private VatRates() {
    }

    public static void ensureSeeded(ICommonDao<VatRate> dao) {
        if (dao == null || !CompanyScope.hasCompany()) {
            return;
        }
        List<VatRate> existing = dao.findAll(CompanyScope.from("VatRate"));
        if (existing != null && !existing.isEmpty()) {
            return;
        }
        Date now = new Date();
        seed(dao, IVatRate.CODE_ZERO, "%0 — İstisna / ihracat", 0d, now);
        seed(dao, IVatRate.CODE_SUPER_REDUCED, "%1 — İndirimli (süper)", 1d, now);
        seed(dao, IVatRate.CODE_REDUCED, "%10 — İndirimli", 10d, now);
        seed(dao, IVatRate.CODE_STANDARD, "%20 — Genel", 20d, now);
    }

    private static void seed(ICommonDao<VatRate> dao, String code, String name, double rate, Date now) {
        VatRate row = dao.getEmpty();
        if (row == null) {
            return;
        }
        CompanyScope.stamp(row);
        row.setRateCode(code);
        row.setRateName(name);
        row.setRate(rate);
        row.setStatus(VatRate.STATUS_ACTIVE);
        row.setAddedDate(now);
        row.setLastUpdateDate(now);
        dao.addOrUpdate(row);
    }
}

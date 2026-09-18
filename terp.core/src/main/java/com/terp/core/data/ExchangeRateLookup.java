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
import com.terp.plugin.data.IExchangeRateLookup;
import com.terp.plugin.data.WorkingPeriod;
import com.terp.plugin.data.model.IExchangeRate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExchangeRateLookup implements IExchangeRateLookup {

    private final ICommonDao<ExchangeRate> dao;

    public ExchangeRateLookup(ICommonDao<ExchangeRate> dao) {
        this.dao = dao;
    }

    @Override
    public List<IExchangeRate> findLatest() {
        List<ExchangeRate> rows = dao.findAll(CompanyScope.from("ExchangeRate")
                + " order by e.rateDate desc");
        Map<String, IExchangeRate> latest = new LinkedHashMap<>();
        if (rows == null) {
            return List.of();
        }
        for (ExchangeRate row : rows) {
            if (row == null || row.getStatus() != ExchangeRate.STATUS_ACTIVE) {
                continue;
            }
            String code = row.getCurrencyCode() == null ? "" : row.getCurrencyCode().trim().toUpperCase();
            if (code.isEmpty() || latest.containsKey(code)) {
                continue;
            }
            latest.put(code, row);
        }
        return new ArrayList<>(latest.values());
    }

    @Override
    public Double rateOn(String currencyCode, Date onDate) {
        if (currencyCode == null || currencyCode.isBlank()
                || "TRY".equalsIgnoreCase(currencyCode.trim())) {
            return 1.0d;
        }
        String code = currencyCode.trim();
        LocalDate limitDay = WorkingPeriod.toLocalDate(onDate == null ? new Date() : onDate);
        List<ExchangeRate> rows = dao.findAll(CompanyScope.from("ExchangeRate"));
        ExchangeRate best = null;
        LocalDate bestDay = null;
        if (rows != null) {
            for (ExchangeRate row : rows) {
                if (row == null || row.getStatus() != ExchangeRate.STATUS_ACTIVE) {
                    continue;
                }
                if (row.getCurrencyCode() == null
                        || !code.equalsIgnoreCase(row.getCurrencyCode().trim())) {
                    continue;
                }
                if (row.getRateDate() == null) {
                    continue;
                }
                LocalDate day = WorkingPeriod.toLocalDate(row.getRateDate());
                if (day.isAfter(limitDay)) {
                    continue;
                }
                if (bestDay == null || day.isAfter(bestDay)) {
                    best = row;
                    bestDay = day;
                }
            }
        }
        return best == null ? null : best.getRate();
    }
}

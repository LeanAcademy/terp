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
package com.terp.plugin.data;

import com.terp.plugin.TerpApplication;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Active fiscal window stored on {@link TerpApplication}. Empty dates mean no lock.
 */
public final class WorkingPeriod {

    private WorkingPeriod() {
    }

    public static String rejectIfOutside(Date documentDate) {
        LocalDate from = TerpApplication.getInstance().getPeriodFrom();
        LocalDate to = TerpApplication.getInstance().getPeriodTo();
        if (from == null && to == null) {
            return null;
        }
        LocalDate day = toLocalDate(documentDate);
        if (from != null && day.isBefore(from)) {
            return "Belge tarihi çalışma dönemi dışında (" + from + " — "
                    + (to == null ? "…" : to) + ").";
        }
        if (to != null && day.isAfter(to)) {
            return "Belge tarihi çalışma dönemi dışında ("
                    + (from == null ? "…" : from) + " — " + to + ").";
        }
        return null;
    }

    public static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return LocalDate.now();
        }
        if (date instanceof java.sql.Date sql) {
            return sql.toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}

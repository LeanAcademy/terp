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
package com.terp.stok.data;

import com.terp.plugin.CompanyScope;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.StockDirection;
import com.terp.plugin.data.model.IMovementReason;
import java.util.Date;
import java.util.List;

public final class MovementReasons {

    private MovementReasons() {
    }

    public static void ensureSeeded(ICommonDao<MovementReason> dao) {
        if (dao == null || !CompanyScope.hasCompany()) {
            return;
        }
        Date now = new Date();
        List<MovementReason> existing = dao.findAll(CompanyScope.from("MovementReason"));
        remapLegacy(dao, existing, now);
        existing = dao.findAll(CompanyScope.from("MovementReason"));
        seedIfMissing(dao, existing, IMovementReason.CODE_OPENING, "Açılış fişi",
                StockDirection.IN, now);
        seedIfMissing(dao, existing, IMovementReason.CODE_PURCHASE, "Satınalma girişi",
                StockDirection.IN, now);
        seedIfMissing(dao, existing, "SATIS_IADE", "Satışlardan iade", StockDirection.IN, now);
        seedIfMissing(dao, existing, IMovementReason.CODE_PRODUCTION_IN, "Üretim girişi",
                StockDirection.IN, now);
        seedIfMissing(dao, existing, IMovementReason.CODE_SALES, "Satış çıkışı (irsaliye)",
                StockDirection.OUT, now);
        seedIfMissing(dao, existing, IMovementReason.CODE_PRODUCTION_OUT, "Üretime sarf",
                StockDirection.OUT, now);
        seedIfMissing(dao, existing, "ALIM_IADE", "Alımlardan iade", StockDirection.OUT, now);
        seedIfMissing(dao, existing, IMovementReason.CODE_TRANSFER_OUT, "Transfer çıkışı",
                StockDirection.OUT, now);
        seedIfMissing(dao, existing, IMovementReason.CODE_TRANSFER_IN, "Transfer girişi",
                StockDirection.IN, now);
        seedIfMissing(dao, existing, IMovementReason.CODE_COUNT_IN, "Sayım artış",
                StockDirection.IN, now);
        seedIfMissing(dao, existing, IMovementReason.CODE_COUNT_OUT, "Sayım azalış",
                StockDirection.OUT, now);
        seedIfMissing(dao, existing, "FIRE", "Fire", StockDirection.OUT, now);
    }

    private static void remapLegacy(ICommonDao<MovementReason> dao, List<MovementReason> existing,
            Date now) {
        if (existing == null) {
            return;
        }
        for (MovementReason row : existing) {
            if (row == null) {
                continue;
            }
            boolean dirty = false;
            if (IMovementReason.CODE_TRANSFER_LEGACY.equals(row.getReasonCode())) {
                row.setReasonCode(IMovementReason.CODE_TRANSFER_OUT);
                if (row.getReasonName() == null || "Transfer".equals(row.getReasonName())) {
                    row.setReasonName("Transfer çıkışı");
                }
                dirty = true;
            }
            int next = StockDirection.of(row.getDirection());
            if (next != row.getDirection()) {
                row.setDirection(next);
                dirty = true;
            }
            if (dirty) {
                row.setLastUpdateDate(now);
                dao.addOrUpdate(row);
            }
        }
    }

    private static void seedIfMissing(ICommonDao<MovementReason> dao, List<MovementReason> existing,
            String code, String name, int direction, Date now) {
        if (findCode(existing, code) != null) {
            return;
        }
        seed(dao, code, name, direction, now);
        MovementReason created = dao.getEmpty();
        if (created != null) {
            created.setReasonCode(code);
            existing.add(created);
        }
    }

    private static MovementReason findCode(List<MovementReason> existing, String code) {
        if (existing == null) {
            return null;
        }
        for (MovementReason row : existing) {
            if (row != null && code.equals(row.getReasonCode())) {
                return row;
            }
        }
        return null;
    }

    private static void seed(ICommonDao<MovementReason> dao, String code, String name,
            int direction, Date now) {
        MovementReason row = dao.getEmpty();
        if (row == null) {
            return;
        }
        CompanyScope.stamp(row);
        row.setReasonCode(code);
        row.setReasonName(name);
        row.setDirection(direction);
        row.setStatus(0);
        row.setAddedDate(now);
        row.setLastUpdateDate(now);
        dao.addOrUpdate(row);
    }
}

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
        List<MovementReason> existing = dao.findAll(CompanyScope.from("MovementReason"));
        if (existing != null && !existing.isEmpty()) {
            return;
        }
        Date now = new Date();
        seed(dao, IMovementReason.CODE_PURCHASE, "Satınalma girişi", StockDirection.IN, now);
        seed(dao, "SATIS_IADE", "Satışlardan iade", StockDirection.IN, now);
        seed(dao, "URETIM_GIRIS", "Üretim girişi", StockDirection.IN, now);
        seed(dao, IMovementReason.CODE_SALES, "Satış çıkışı (irsaliye)", StockDirection.OUT, now);
        seed(dao, "URETIM_SARF", "Üretime sarf", StockDirection.OUT, now);
        seed(dao, "ALIM_IADE", "Alımlardan iade", StockDirection.OUT, now);
        seed(dao, "TRANSFER", "Transfer", StockDirection.TRANSFER, now);
        seed(dao, IMovementReason.CODE_COUNT_IN, "Sayım artış", StockDirection.COUNT_IN, now);
        seed(dao, IMovementReason.CODE_COUNT_OUT, "Sayım azalış", StockDirection.COUNT_OUT, now);
        seed(dao, "FIRE", "Fire", StockDirection.SCRAP, now);
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

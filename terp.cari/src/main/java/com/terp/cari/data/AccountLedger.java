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
package com.terp.cari.data;

import com.terp.plugin.CompanyScope;
import com.terp.plugin.data.AccountDirection;
import com.terp.plugin.data.AccountPosting;
import com.terp.plugin.data.IAccountLedger;
import com.terp.plugin.data.IAccountLookup;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.model.IAccount;
import java.util.Date;
import java.util.List;

public class AccountLedger implements IAccountLedger {

    private final ICommonDao<AccountMovement> movementDao;
    private final IAccountLookup accounts;

    public AccountLedger(ICommonDao<AccountMovement> movementDao, IAccountLookup accounts) {
        this.movementDao = movementDao;
        this.accounts = accounts;
    }

    @Override
    public void post(AccountPosting posting) {
        if (posting == null) {
            throw new IllegalArgumentException("Cari fişi boş");
        }
        if (posting.getAccountCode() == null || posting.getAccountCode().isBlank()) {
            throw new IllegalArgumentException("Cari zorunlu");
        }
        if (posting.getAmount() == null || posting.getAmount() <= 0d) {
            throw new IllegalArgumentException("Tutar sıfırdan büyük olmalıdır");
        }
        int direction = posting.getDirection();
        if (direction != AccountDirection.DEBIT && direction != AccountDirection.CREDIT) {
            throw new IllegalArgumentException("Hareket türü borç veya alacak olmalıdır");
        }
        AccountMovement row = movementDao.getEmpty();
        if (row == null) {
            throw new IllegalStateException("Cari hareketi oluşturulamadı");
        }
        Date now = new Date();
        row.setAddedDate(now);
        row.setLastUpdateDate(now);
        if (!CompanyScope.stamp(row)) {
            throw new IllegalStateException("Firma seçilmedi");
        }
        row.setMovementDate(posting.getMovementDate() == null ? now : posting.getMovementDate());
        row.setMovementType(direction);
        row.setAccountCode(posting.getAccountCode());
        IAccount account = accounts == null ? null : accounts.findByCode(posting.getAccountCode());
        row.setAccountName(firstNonBlank(posting.getAccountName(),
                account == null ? null : account.getAccountName()));
        row.setAmount(posting.getAmount());
        row.setDocumentNo(posting.getDocumentNo());
        row.setNotes(posting.getNotes());
        row.setSourceType(posting.getSourceType());
        row.setSourceId(posting.getSourceId());
        movementDao.addOrUpdate(row);
    }

    @Override
    public void reverseByDocument(String sourceType, Long sourceId) {
        if (sourceType == null || sourceType.isBlank() || sourceId == null) {
            return;
        }
        String escaped = sourceType.replace("'", "''");
        List<AccountMovement> rows = movementDao.findAll(
                "from AccountMovement e where " + CompanyScope.predicate("e")
                        + " and e.sourceType = '" + escaped + "' and e.sourceId = " + sourceId);
        if (rows == null) {
            return;
        }
        for (AccountMovement row : rows) {
            if (row != null && row.getRowId() != null) {
                movementDao.delete(row.getRowId());
            }
        }
    }

    private static String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        return fallback;
    }
}

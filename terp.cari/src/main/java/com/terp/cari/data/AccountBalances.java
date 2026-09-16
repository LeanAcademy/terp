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

import com.terp.plugin.data.AccountDirection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AccountBalances {

    private AccountBalances() {
    }

    public static List<AccountBalanceRow> report(List<Cari> accounts, List<AccountMovement> movements,
            boolean onlyNonZero) {
        Map<String, Totals> byCode = new HashMap<>();
        if (movements != null) {
            for (AccountMovement movement : movements) {
                if (movement == null || movement.getAccountCode() == null) {
                    continue;
                }
                double amount = movement.getAmount() == null ? 0d : movement.getAmount();
                Totals totals = byCode.computeIfAbsent(movement.getAccountCode(), key -> new Totals());
                if (movement.getMovementType() == AccountDirection.CREDIT) {
                    totals.credit += amount;
                } else {
                    totals.debit += amount;
                }
            }
        }
        List<AccountBalanceRow> rows = new ArrayList<>();
        if (accounts == null) {
            return rows;
        }
        for (Cari account : accounts) {
            if (account == null || account.getAccountCode() == null) {
                continue;
            }
            Totals totals = byCode.getOrDefault(account.getAccountCode(), Totals.EMPTY);
            double balance = totals.debit - totals.credit;
            if (onlyNonZero && Math.abs(balance) <= 0.0000001d) {
                continue;
            }
            AccountBalanceRow row = new AccountBalanceRow();
            row.setAccountCode(account.getAccountCode());
            row.setAccountName(account.getAccountName());
            row.setRoleLabel(account.getRoleLabel());
            row.setDebitTotal(totals.debit);
            row.setCreditTotal(totals.credit);
            row.setBalance(Math.abs(balance));
            if (balance > 0.0000001d) {
                row.setBalanceLabel("Borç");
            } else if (balance < -0.0000001d) {
                row.setBalanceLabel("Alacak");
            } else {
                row.setBalanceLabel("Kapalı");
            }
            rows.add(row);
        }
        return rows;
    }

    private static final class Totals {
        static final Totals EMPTY = new Totals();
        double debit;
        double credit;
    }
}

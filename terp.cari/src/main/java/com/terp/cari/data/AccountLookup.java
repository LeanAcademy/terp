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
import com.terp.plugin.data.IAccountLookup;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.model.IAccount;
import java.util.ArrayList;
import java.util.List;

public class AccountLookup implements IAccountLookup {

    private final ICommonDao<Cari> dao;

    public AccountLookup(ICommonDao<Cari> dao) {
        this.dao = dao;
    }

    @Override
    public List<IAccount> findActiveCustomers() {
        return filter(true, false);
    }

    @Override
    public List<IAccount> findActiveSuppliers() {
        return filter(false, true);
    }

    @Override
    public IAccount findByCode(String accountCode) {
        if (accountCode == null || accountCode.isBlank()) {
            return null;
        }
        String escaped = accountCode.replace("'", "''");
        return dao.firstOrDefault("from Cari e where " + CompanyScope.predicate("e")
                + " and e.accountCode = '" + escaped + "'");
    }

    private List<IAccount> filter(boolean customers, boolean suppliers) {
        List<Cari> rows = dao.findAll(CompanyScope.from("Cari"));
        List<IAccount> result = new ArrayList<>();
        if (rows == null) {
            return result;
        }
        for (Cari row : rows) {
            if (row == null || row.getStatus() != 0) {
                continue;
            }
            if (customers && row.isCustomer()) {
                result.add(row);
            } else if (suppliers && row.isSupplier()) {
                result.add(row);
            }
        }
        return result;
    }
}

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

import com.terp.plugin.data.model.IAccount;
import java.util.List;

/**
 * Cross-plugin lookup for current accounts. Registered by the cari plugin.
 * Other plugins (stock, sales) must not depend on the cari entity class.
 */
public interface IAccountLookup {

    List<IAccount> findActiveCustomers();

    List<IAccount> findActiveSuppliers();

    IAccount findByCode(String accountCode);
}

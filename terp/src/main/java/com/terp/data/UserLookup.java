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
package com.terp.data;

import com.terp.data.model.Employee;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.IUserLookup;

public class UserLookup implements IUserLookup {

    private final ICommonDao<Employee> dao = new CommonDaoImpl<>(Employee.class);

    @Override
    public String findUserName(Long userId) {
        if (userId == null) {
            return null;
        }
        Employee row = dao.firstOrDefault(userId);
        if (row == null) {
            return null;
        }
        if (row.getName() != null && !row.getName().isBlank()) {
            return row.getName().trim();
        }
        if (row.getUserName() != null && !row.getUserName().isBlank()) {
            return row.getUserName().trim();
        }
        return null;
    }
}

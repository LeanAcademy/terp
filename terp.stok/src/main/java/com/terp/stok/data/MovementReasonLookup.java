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
import com.terp.plugin.data.IMovementReasonLookup;
import com.terp.plugin.data.model.IMovementReason;
import java.util.ArrayList;
import java.util.List;

public class MovementReasonLookup implements IMovementReasonLookup {

    private final ICommonDao<MovementReason> dao;

    public MovementReasonLookup(ICommonDao<MovementReason> dao) {
        this.dao = dao;
    }

    @Override
    public List<IMovementReason> findByDirection(int direction) {
        MovementReasons.ensureSeeded(dao);
        List<MovementReason> rows = dao.findAll(CompanyScope.from("MovementReason"));
        List<IMovementReason> result = new ArrayList<>();
        if (rows == null) {
            return result;
        }
        for (MovementReason row : rows) {
            if (row != null && row.getStatus() == 0 && row.getDirection() == direction) {
                result.add(row);
            }
        }
        return result;
    }

    @Override
    public IMovementReason findByCode(String reasonCode) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return null;
        }
        MovementReasons.ensureSeeded(dao);
        String escaped = reasonCode.replace("'", "''");
        return dao.firstOrDefault("from MovementReason e where " + CompanyScope.predicate("e")
                + " and e.reasonCode = '" + escaped + "'");
    }
}

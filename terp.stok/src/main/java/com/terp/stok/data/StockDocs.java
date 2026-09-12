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
import com.terp.plugin.data.ICommonFields;
import java.util.List;

public final class StockDocs {

    private StockDocs() {
    }

    public static List<StockMovement> movements(ICommonDao<StockMovement> movementDao) {
        if (movementDao == null) {
            return List.of();
        }
        List<StockMovement> rows = movementDao.findAll(CompanyScope.from("StockMovement"));
        return rows == null ? List.of() : rows;
    }

    public static double onHand(List<StockMovement> movements, String warehouseCode, String itemCode) {
        return StockBalances.onHand(movements, warehouseCode, itemCode, null);
    }

    public static <T> void deleteByParent(ICommonDao<T> lineDao, String entityName, String field,
            Long parentId) {
        if (lineDao == null || parentId == null) {
            return;
        }
        List<T> rows = lineDao.findAll("from " + entityName + " e where e." + field + " = " + parentId);
        if (rows == null) {
            return;
        }
        for (T row : rows) {
            if (row instanceof ICommonFields fields && fields.getRowId() != null) {
                lineDao.delete(fields.getRowId());
            }
        }
    }
}

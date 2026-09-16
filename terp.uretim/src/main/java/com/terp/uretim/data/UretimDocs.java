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
package com.terp.uretim.data;

import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.ICommonFields;
import com.terp.plugin.data.model.IStockItem;
import java.util.List;

public final class UretimDocs {

    private UretimDocs() {
    }

    public static boolean isProducible(IStockItem item) {
        if (item == null) {
            return false;
        }
        int type = item.getItemType();
        return type == IStockItem.TYPE_SEMI || type == IStockItem.TYPE_FINISHED;
    }

    public static boolean isComponent(IStockItem item) {
        if (item == null) {
            return false;
        }
        int type = item.getItemType();
        return type == IStockItem.TYPE_RAW
                || type == IStockItem.TYPE_SEMI
                || type == IStockItem.TYPE_CONSUMABLE;
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

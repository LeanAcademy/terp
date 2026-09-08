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
import com.terp.plugin.data.IItemLookup;
import com.terp.plugin.data.model.IStockItem;
import java.util.ArrayList;
import java.util.List;

public class ItemLookup implements IItemLookup {

    private final ICommonDao<Item> dao;

    public ItemLookup(ICommonDao<Item> dao) {
        this.dao = dao;
    }

    @Override
    public List<IStockItem> findActive() {
        List<Item> rows = dao.findAll(CompanyScope.from("Item"));
        List<IStockItem> result = new ArrayList<>();
        if (rows == null) {
            return result;
        }
        for (Item row : rows) {
            if (row != null && row.getStatus() == 0 && row.getItemType() != Item.TYPE_SERVICE) {
                result.add(row);
            }
        }
        return result;
    }

    @Override
    public IStockItem findByCode(String itemCode) {
        if (itemCode == null || itemCode.isBlank()) {
            return null;
        }
        String escaped = itemCode.replace("'", "''");
        return dao.firstOrDefault("from Item e where " + CompanyScope.predicate("e")
                + " and e.itemId = '" + escaped + "'");
    }
}

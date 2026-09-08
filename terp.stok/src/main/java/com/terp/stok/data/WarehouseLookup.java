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
import com.terp.plugin.data.IWarehouseLookup;
import com.terp.plugin.data.model.IStockWarehouse;
import java.util.ArrayList;
import java.util.List;

public class WarehouseLookup implements IWarehouseLookup {

    private final ICommonDao<Warehouse> dao;

    public WarehouseLookup(ICommonDao<Warehouse> dao) {
        this.dao = dao;
    }

    @Override
    public List<IStockWarehouse> findActive() {
        List<Warehouse> rows = dao.findAll(CompanyScope.from("Warehouse"));
        List<IStockWarehouse> result = new ArrayList<>();
        if (rows == null) {
            return result;
        }
        for (Warehouse row : rows) {
            if (row != null && row.getStatus() == 0) {
                result.add(row);
            }
        }
        return result;
    }

    @Override
    public IStockWarehouse findByCode(String warehouseCode) {
        if (warehouseCode == null || warehouseCode.isBlank()) {
            return null;
        }
        String escaped = warehouseCode.replace("'", "''");
        return dao.firstOrDefault("from Warehouse e where " + CompanyScope.predicate("e")
                + " and e.warehouseCode = '" + escaped + "'");
    }
}

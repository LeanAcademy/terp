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
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.IItemLookup;
import com.terp.plugin.data.IPersistence;
import com.terp.plugin.data.model.IStockItem;
import java.util.ArrayList;
import java.util.List;

public class ItemLookup implements IItemLookup {

    private final ICommonDao<Item> dao;
    private final ICommonDao<ItemBarcode> barcodeDao;

    public ItemLookup(ICommonDao<Item> dao) {
        this(dao, null);
    }

    public ItemLookup(ICommonDao<Item> dao, ICommonDao<ItemBarcode> barcodeDao) {
        this.dao = dao;
        this.barcodeDao = barcodeDao;
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

    @Override
    public IStockItem findByBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return null;
        }
        String escaped = barcode.replace("'", "''");
        Item byField = dao.firstOrDefault("from Item e where " + CompanyScope.predicate("e")
                + " and e.barcode = '" + escaped + "'");
        if (byField != null) {
            return byField;
        }
        ICommonDao<ItemBarcode> barcodes = barcodeDao;
        if (barcodes == null) {
            IPersistence persistence = TerpApplication.getInstance().getPersistence();
            if (persistence != null) {
                barcodes = persistence.createDao(ItemBarcode.class);
            }
        }
        if (barcodes == null) {
            return null;
        }
        ItemBarcode extra = barcodes.firstOrDefault("from ItemBarcode e where "
                + CompanyScope.predicate("e") + " and e.barcode = '" + escaped + "'");
        if (extra == null || extra.getItemCode() == null || extra.getItemCode().isBlank()) {
            return null;
        }
        return findByCode(extra.getItemCode());
    }
}

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
package com.terp.uretim.plugin;

import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.IProductionOrders;
import com.terp.plugin.data.ProductionFromSales;
import com.terp.uretim.data.ProductionOrder;
import com.terp.uretim.data.Recipe;
import com.terp.uretim.data.UretimDocs;
import com.terp.uretim.gui.UretimDates;
import com.terp.uretim.gui.UretimWindows;

public final class ProductionOrders implements IProductionOrders {

    @Override
    public boolean hasActiveRecipe(String productCode) {
        return UretimDocs.findActiveRecipe(recipeDao(), productCode) != null;
    }

    @Override
    public double allocatedForSalesLine(Long sourceOrderLineId) {
        return UretimDocs.allocatedForSalesLine(orderDao(), sourceOrderLineId);
    }

    @Override
    public void openFromSales(ProductionFromSales request) {
        if (request == null || request.getProductCode() == null || request.getProductCode().isBlank()) {
            UretimDates.showError("Üretim emri", "Satır yok", "Üretilecek mamul yok.");
            return;
        }
        if (UretimDocs.findActiveRecipe(recipeDao(), request.getProductCode()) == null) {
            UretimDates.showError("Üretim emri", "Reçete yok",
                    request.getProductCode() + " için aktif reçete yok.");
            return;
        }
        UretimWindows.openOrderEditor(null, request, null);
    }

    private static ICommonDao<Recipe> recipeDao() {
        return TerpApplication.getInstance().getPersistence().createDao(Recipe.class);
    }

    private static ICommonDao<ProductionOrder> orderDao() {
        return TerpApplication.getInstance().getPersistence().createDao(ProductionOrder.class);
    }
}

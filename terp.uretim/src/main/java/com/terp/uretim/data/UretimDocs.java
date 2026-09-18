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

import com.terp.plugin.CompanyScope;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.ICommonFields;
import com.terp.plugin.data.model.IStockItem;
import java.util.ArrayList;
import java.util.List;

public final class UretimDocs {

    public static final double EPS = 0.0000001d;

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

    public static double remaining(double planned, double done) {
        double value = planned - done;
        return value > EPS ? value : 0d;
    }

    public static double scale(double plannedMaterial, double produced, double plannedProduct) {
        if (plannedProduct <= EPS) {
            return 0d;
        }
        return plannedMaterial * (produced / plannedProduct);
    }

    public static Recipe findActiveRecipe(ICommonDao<Recipe> recipeDao, String productCode) {
        if (recipeDao == null || productCode == null || productCode.isBlank()) {
            return null;
        }
        String hql = "from Recipe e where " + CompanyScope.predicate("e")
                + " and e.productCode = '" + productCode.replace("'", "''") + "'"
                + " and e.status = " + Recipe.STATUS_ACTIVE;
        List<Recipe> rows = recipeDao.findAll(hql);
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        return rows.get(0);
    }

    public static List<ProductionOrderMaterial> explode(Recipe recipe, List<RecipeLine> recipeLines,
            double plannedQty) {
        List<ProductionOrderMaterial> materials = new ArrayList<>();
        if (recipe == null || recipeLines == null) {
            return materials;
        }
        double yield = recipe.getYieldQuantity() == null || recipe.getYieldQuantity() <= EPS
                ? 1d : recipe.getYieldQuantity();
        int no = 1;
        for (RecipeLine recipeLine : recipeLines) {
            if (recipeLine == null || recipeLine.getItemCode() == null) {
                continue;
            }
            ProductionOrderMaterial line = new ProductionOrderMaterial();
            line.setLineNo(no++);
            line.setItemCode(recipeLine.getItemCode());
            line.setItemDesc(recipeLine.getItemDesc());
            line.setItemUnit(recipeLine.getItemUnit());
            line.setRecipeLineId(recipeLine.getRowId());
            line.setPlannedQty(scale(nz(recipeLine.getQuantity()), plannedQty, yield));
            materials.add(line);
        }
        return materials;
    }

    public static List<RecipeLine> recipeLines(ICommonDao<RecipeLine> lineDao, Long recipeId) {
        if (lineDao == null || recipeId == null) {
            return List.of();
        }
        List<RecipeLine> rows = lineDao.findAll(
                "from RecipeLine e where e.recipeId = " + recipeId + " order by e.lineNo");
        return rows == null ? List.of() : rows;
    }

    public static List<ProductionOrderMaterial> materialsOf(ICommonDao<ProductionOrderMaterial> dao,
            Long orderId) {
        if (dao == null || orderId == null) {
            return List.of();
        }
        List<ProductionOrderMaterial> rows = dao.findAll(
                "from ProductionOrderMaterial e where e.orderId = " + orderId + " order by e.lineNo");
        return rows == null ? List.of() : rows;
    }

    public static List<ProductionCompletion> completionsOf(ICommonDao<ProductionCompletion> dao,
            Long orderId) {
        if (dao == null || orderId == null) {
            return List.of();
        }
        List<ProductionCompletion> rows = dao.findAll(
                "from ProductionCompletion e where e.orderId = " + orderId + " order by e.rowId");
        return rows == null ? List.of() : rows;
    }

    public static List<ProductionOrder> completableOrders(ICommonDao<ProductionOrder> dao) {
        if (dao == null) {
            return List.of();
        }
        List<ProductionOrder> rows = dao.findAll(
                "from ProductionOrder e where " + CompanyScope.predicate("e")
                        + " and e.status = " + ProductionOrder.STATUS_APPROVED
                        + " order by e.documentNo");
        List<ProductionOrder> result = new ArrayList<>();
        if (rows == null) {
            return result;
        }
        for (ProductionOrder row : rows) {
            if (row != null && row.canComplete()) {
                result.add(row);
            }
        }
        return result;
    }

    public static void decorateCompletions(ICommonDao<ProductionOrder> orderDao,
            List<ProductionCompletion> rows) {
        if (rows == null) {
            return;
        }
        for (ProductionCompletion row : rows) {
            decorateCompletion(orderDao, row);
        }
    }

    public static void decorateCompletion(ICommonDao<ProductionOrder> orderDao,
            ProductionCompletion row) {
        if (orderDao == null || row == null || row.getOrderId() == null) {
            return;
        }
        ProductionOrder order = orderDao.firstOrDefault(row.getOrderId());
        if (order == null) {
            return;
        }
        row.setOrderDocumentNo(order.getDocumentNo());
        row.setProductCode(order.getProductCode());
        row.setProductName(order.getProductName());
    }

    public static List<ProductionCompletionMaterial> completionMaterialsOf(
            ICommonDao<ProductionCompletionMaterial> dao, Long completionId) {
        if (dao == null || completionId == null) {
            return List.of();
        }
        List<ProductionCompletionMaterial> rows = dao.findAll(
                "from ProductionCompletionMaterial e where e.completionId = " + completionId
                        + " order by e.lineNo");
        return rows == null ? List.of() : rows;
    }

    public static double postedCompletedQty(ICommonDao<ProductionCompletion> dao, Long orderId) {
        double total = 0d;
        for (ProductionCompletion row : completionsOf(dao, orderId)) {
            if (row != null && row.isPosted()) {
                total += nz(row.getProducedQty());
            }
        }
        return total;
    }

    public static double postedConsume(ICommonDao<ProductionCompletion> completionDao,
            ICommonDao<ProductionCompletionMaterial> materialDao, Long orderId, String itemCode) {
        if (itemCode == null) {
            return 0d;
        }
        double total = 0d;
        for (ProductionCompletion completion : completionsOf(completionDao, orderId)) {
            if (completion == null || !completion.isPosted() || completion.getRowId() == null) {
                continue;
            }
            for (ProductionCompletionMaterial line : completionMaterialsOf(materialDao, completion.getRowId())) {
                if (line != null && itemCode.equalsIgnoreCase(line.getItemCode())) {
                    total += nz(line.getQuantity());
                }
            }
        }
        return total;
    }

    public static double allocatedForSalesLine(ICommonDao<ProductionOrder> orderDao, Long sourceOrderLineId) {
        if (orderDao == null || sourceOrderLineId == null) {
            return 0d;
        }
        String hql = "from ProductionOrder e where " + CompanyScope.predicate("e")
                + " and e.sourceOrderLineId = " + sourceOrderLineId
                + " and e.status <> " + ProductionOrder.STATUS_CANCELLED;
        List<ProductionOrder> rows = orderDao.findAll(hql);
        double total = 0d;
        if (rows == null) {
            return 0d;
        }
        for (ProductionOrder row : rows) {
            if (row != null) {
                total += nz(row.getPlannedQty());
            }
        }
        return total;
    }

    public static boolean hasPostedCompletion(ICommonDao<ProductionCompletion> dao, Long orderId) {
        for (ProductionCompletion row : completionsOf(dao, orderId)) {
            if (row != null && row.isPosted()) {
                return true;
            }
        }
        return false;
    }

    public static void deleteCompletion(ICommonDao<ProductionCompletion> completionDao,
            ICommonDao<ProductionCompletionMaterial> lineDao, Long completionId) {
        deleteByParent(lineDao, "ProductionCompletionMaterial", "completionId", completionId);
        if (completionDao != null && completionId != null) {
            completionDao.delete(completionId);
        }
    }

    public static void deleteOrder(ICommonDao<ProductionOrder> orderDao,
            ICommonDao<ProductionOrderMaterial> materialDao,
            ICommonDao<ProductionCompletion> completionDao,
            ICommonDao<ProductionCompletionMaterial> completionLineDao, Long orderId) {
        if (hasPostedCompletion(completionDao, orderId)) {
            throw new IllegalStateException("Önce iş bitiş kayıtlarını iptal edin.");
        }
        for (ProductionCompletion row : completionsOf(completionDao, orderId)) {
            if (row != null && row.getRowId() != null) {
                deleteCompletion(completionDao, completionLineDao, row.getRowId());
            }
        }
        deleteByParent(materialDao, "ProductionOrderMaterial", "orderId", orderId);
        if (orderDao != null && orderId != null) {
            orderDao.delete(orderId);
        }
    }

    public static void syncStatus(ICommonDao<ProductionOrder> orderDao,
            ICommonDao<ProductionCompletion> completionDao, ProductionOrder order) {
        if (orderDao == null || order == null || order.getRowId() == null || order.isCancelled()) {
            return;
        }
        double completed = postedCompletedQty(completionDao, order.getRowId());
        order.setCompletedQty(completed);
        if (!order.isDraft()) {
            if (remaining(nz(order.getPlannedQty()), completed) <= EPS) {
                order.setStatus(ProductionOrder.STATUS_CLOSED);
            } else {
                order.setStatus(ProductionOrder.STATUS_APPROVED);
            }
        }
        orderDao.addOrUpdate(order);
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

    private static double nz(Double value) {
        return value == null ? 0d : value;
    }
}

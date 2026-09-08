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
package com.terp.satin.data;

import com.terp.plugin.CompanyScope;
import com.terp.plugin.data.ICommonDao;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PurchaseDocs {

    private PurchaseDocs() {
    }

    public static boolean hasOpenOrder(ICommonDao<PurchaseOrder> orderDao, Long requestId) {
        return findOpenOrder(orderDao, requestId) != null;
    }

    public static PurchaseOrder findOpenOrder(ICommonDao<PurchaseOrder> orderDao, Long requestId) {
        if (orderDao == null || requestId == null) {
            return null;
        }
        return orderDao.firstOrDefault(
                "from PurchaseOrder e where e.sourceRequestId = " + requestId
                        + " and e.status <> " + PurchaseOrder.STATUS_CANCELLED);
    }

    public static PurchaseOrder findOrderByNo(ICommonDao<PurchaseOrder> orderDao, String documentNo) {
        if (orderDao == null || documentNo == null || documentNo.isBlank()) {
            return null;
        }
        String escaped = documentNo.replace("'", "''");
        return orderDao.firstOrDefault("from PurchaseOrder e where " + CompanyScope.predicate("e")
                + " and e.documentNo = '" + escaped + "'");
    }

    public static PurchaseOrderLine findOrderLine(ICommonDao<PurchaseOrderLine> orderLineDao,
            Long orderId, String itemCode) {
        if (orderLineDao == null || orderId == null || itemCode == null || itemCode.isBlank()) {
            return null;
        }
        String escaped = itemCode.replace("'", "''");
        return orderLineDao.firstOrDefault(
                "from PurchaseOrderLine e where e.orderId = " + orderId
                        + " and e.itemCode = '" + escaped + "'");
    }

    public static boolean hasPostedReceipt(ICommonDao<PurchaseReceipt> receiptDao,
            ICommonDao<PurchaseReceiptLine> lineDao, ICommonDao<PurchaseOrderLine> orderLineDao,
            Long orderId) {
        if (receiptDao == null || orderId == null) {
            return false;
        }
        PurchaseReceipt headerHit = receiptDao.firstOrDefault(
                "from PurchaseReceipt e where e.sourceOrderId = " + orderId
                        + " and e.status = " + PurchaseReceipt.STATUS_POSTED);
        if (headerHit != null) {
            return true;
        }
        List<PurchaseOrderLine> orderLines = orderLineDao == null ? null
                : orderLineDao.findAll("from PurchaseOrderLine e where e.orderId = " + orderId);
        if (orderLines == null || lineDao == null) {
            return false;
        }
        for (PurchaseOrderLine orderLine : orderLines) {
            if (orderLine == null || orderLine.getRowId() == null) {
                continue;
            }
            if (postedQuantity(receiptDao, lineDao, orderLine.getRowId(), null) > 0d) {
                return true;
            }
        }
        return false;
    }

    public static double postedQuantity(ICommonDao<PurchaseReceipt> receiptDao,
            ICommonDao<PurchaseReceiptLine> lineDao, Long orderLineId, Long excludeReceiptId) {
        return sumQuantity(receiptDao, lineDao, orderLineId, excludeReceiptId, false);
    }

    public static double allocatedQuantity(ICommonDao<PurchaseReceipt> receiptDao,
            ICommonDao<PurchaseReceiptLine> lineDao, Long orderLineId, Long excludeReceiptId) {
        return sumQuantity(receiptDao, lineDao, orderLineId, excludeReceiptId, true);
    }

    public static double remainingOrdered(double ordered, double posted) {
        return Math.max(0d, ordered - posted);
    }

    public static double maxReceivable(double ordered, double posted, double overPercent) {
        double factor = 1d + Math.max(0d, overPercent) / 100d;
        return Math.max(0d, ordered * factor - posted);
    }

    public static double overPercent(ICommonDao<PurchaseSettings> settingsDao) {
        PurchaseSettings settings = loadSettings(settingsDao);
        return settings == null ? 0d : settings.getOverReceiptPercent();
    }

    public static PurchaseSettings loadSettings(ICommonDao<PurchaseSettings> settingsDao) {
        if (settingsDao == null || !CompanyScope.hasCompany()) {
            return null;
        }
        PurchaseSettings existing = settingsDao.firstOrDefault(CompanyScope.from("PurchaseSettings"));
        if (existing != null) {
            return existing;
        }
        PurchaseSettings created = settingsDao.getEmpty();
        if (created == null) {
            return null;
        }
        Date now = new Date();
        CompanyScope.stamp(created);
        created.setOverReceiptPercent(0d);
        created.setAddedDate(now);
        created.setLastUpdateDate(now);
        PurchaseSettings saved = settingsDao.addOrUpdate(created);
        return saved == null ? created : saved;
    }

    public static boolean hasReceivableBalance(PurchaseOrder order,
            ICommonDao<PurchaseOrderLine> orderLineDao, ICommonDao<PurchaseReceipt> receiptDao,
            ICommonDao<PurchaseReceiptLine> lineDao, ICommonDao<PurchaseSettings> settingsDao) {
        if (order == null || order.getRowId() == null || order.isDraft() || order.isCancelled()) {
            return false;
        }
        return !remainingLines(order.getRowId(), orderLineDao, receiptDao, lineDao, settingsDao,
                null).isEmpty();
    }

    public static List<PurchaseOrderLine> remainingLines(Long orderId,
            ICommonDao<PurchaseOrderLine> orderLineDao, ICommonDao<PurchaseReceipt> receiptDao,
            ICommonDao<PurchaseReceiptLine> lineDao, ICommonDao<PurchaseSettings> settingsDao,
            Long excludeReceiptId) {
        List<PurchaseOrderLine> result = new ArrayList<>();
        if (orderId == null || orderLineDao == null) {
            return result;
        }
        double percent = overPercent(settingsDao);
        List<PurchaseOrderLine> rows = orderLineDao.findAll(
                "from PurchaseOrderLine e where e.orderId = " + orderId + " order by e.lineNo");
        if (rows == null) {
            return result;
        }
        for (PurchaseOrderLine line : rows) {
            if (line == null) {
                continue;
            }
            double ordered = line.getQuantity() == null ? 0d : line.getQuantity();
            double allocated = allocatedQuantity(receiptDao, lineDao, line.getRowId(), excludeReceiptId);
            if (maxReceivable(ordered, allocated, percent) > 0d) {
                result.add(line);
            }
        }
        return result;
    }

    public static List<PurchaseOrder> receivableOrders(ICommonDao<PurchaseOrder> orderDao,
            ICommonDao<PurchaseOrderLine> orderLineDao, ICommonDao<PurchaseReceipt> receiptDao,
            ICommonDao<PurchaseReceiptLine> lineDao, ICommonDao<PurchaseSettings> settingsDao) {
        List<PurchaseOrder> result = new ArrayList<>();
        if (orderDao == null) {
            return result;
        }
        List<PurchaseOrder> rows = orderDao.findAll(CompanyScope.from("PurchaseOrder")
                + " and e.status in (" + PurchaseOrder.STATUS_APPROVED
                + "," + PurchaseOrder.STATUS_RECEIVED + ")");
        if (rows == null) {
            return result;
        }
        for (PurchaseOrder order : rows) {
            if (hasReceivableBalance(order, orderLineDao, receiptDao, lineDao, settingsDao)) {
                result.add(order);
            }
        }
        return result;
    }

    public static void syncOrderStatus(ICommonDao<PurchaseOrder> orderDao,
            ICommonDao<PurchaseOrderLine> orderLineDao, ICommonDao<PurchaseReceipt> receiptDao,
            ICommonDao<PurchaseReceiptLine> lineDao, Long orderId) {
        if (orderDao == null || orderId == null) {
            return;
        }
        PurchaseOrder order = orderDao.firstOrDefault(orderId);
        if (order == null || order.isCancelled() || order.isDraft()) {
            return;
        }
        boolean remaining = hasOpenOrderedBalance(orderId, orderLineDao, receiptDao, lineDao);
        int next = remaining ? PurchaseOrder.STATUS_APPROVED : PurchaseOrder.STATUS_RECEIVED;
        if (order.getStatus() == next) {
            return;
        }
        order.setStatus(next);
        order.setLastUpdateDate(new Date());
        orderDao.addOrUpdate(order);
    }

    public static void syncOrdersForLines(ICommonDao<PurchaseOrder> orderDao,
            ICommonDao<PurchaseOrderLine> orderLineDao, ICommonDao<PurchaseReceipt> receiptDao,
            ICommonDao<PurchaseReceiptLine> lineDao, Long headerOrderId,
            List<PurchaseReceiptLine> receiptLines) {
        Set<Long> ids = new LinkedHashSet<>();
        if (headerOrderId != null) {
            ids.add(headerOrderId);
        }
        if (receiptLines != null) {
            for (PurchaseReceiptLine line : receiptLines) {
                if (line != null && line.getSourceOrderId() != null) {
                    ids.add(line.getSourceOrderId());
                }
            }
        }
        for (Long id : ids) {
            syncOrderStatus(orderDao, orderLineDao, receiptDao, lineDao, id);
        }
    }

    public static void syncRequestStatus(ICommonDao<PurchaseRequest> requestDao,
            ICommonDao<PurchaseOrder> orderDao, Long requestId) {
        if (requestDao == null || requestId == null) {
            return;
        }
        PurchaseRequest request = requestDao.firstOrDefault(requestId);
        if (request == null || request.isCancelled() || request.isDraft()) {
            return;
        }
        boolean open = hasOpenOrder(orderDao, requestId);
        int next = open ? PurchaseRequest.STATUS_CONVERTED : PurchaseRequest.STATUS_APPROVED;
        if (request.getStatus() == next) {
            return;
        }
        request.setStatus(next);
        request.setLastUpdateDate(new Date());
        requestDao.addOrUpdate(request);
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
            if (row instanceof com.terp.plugin.data.ICommonFields fields && fields.getRowId() != null) {
                lineDao.delete(fields.getRowId());
            }
        }
    }

    private static double sumQuantity(ICommonDao<PurchaseReceipt> receiptDao,
            ICommonDao<PurchaseReceiptLine> lineDao, Long orderLineId, Long excludeReceiptId,
            boolean includeDraft) {
        if (receiptDao == null || lineDao == null || orderLineId == null) {
            return 0d;
        }
        List<PurchaseReceiptLine> rows = lineDao.findAll(
                "from PurchaseReceiptLine e where e.sourceOrderLineId = " + orderLineId);
        if (rows == null) {
            return 0d;
        }
        double sum = 0d;
        for (PurchaseReceiptLine line : rows) {
            if (line == null || line.getReceiptId() == null) {
                continue;
            }
            if (excludeReceiptId != null && excludeReceiptId.equals(line.getReceiptId())) {
                continue;
            }
            PurchaseReceipt receipt = receiptDao.firstOrDefault(line.getReceiptId());
            if (receipt == null || receipt.isCancelled()) {
                continue;
            }
            if (receipt.isPosted() || (includeDraft && receipt.isDraft())) {
                sum += line.getQuantity() == null ? 0d : line.getQuantity();
            }
        }
        return sum;
    }

    private static boolean hasOpenOrderedBalance(Long orderId, ICommonDao<PurchaseOrderLine> orderLineDao,
            ICommonDao<PurchaseReceipt> receiptDao, ICommonDao<PurchaseReceiptLine> lineDao) {
        if (orderLineDao == null) {
            return true;
        }
        List<PurchaseOrderLine> rows = orderLineDao.findAll(
                "from PurchaseOrderLine e where e.orderId = " + orderId);
        if (rows == null || rows.isEmpty()) {
            return false;
        }
        for (PurchaseOrderLine line : rows) {
            if (line == null) {
                continue;
            }
            double ordered = line.getQuantity() == null ? 0d : line.getQuantity();
            double posted = postedQuantity(receiptDao, lineDao, line.getRowId(), null);
            if (remainingOrdered(ordered, posted) > 0.0000001d) {
                return true;
            }
        }
        return false;
    }
}

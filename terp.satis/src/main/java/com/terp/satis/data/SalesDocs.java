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
package com.terp.satis.data;

import com.terp.plugin.CompanyScope;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.ICommonFields;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SalesDocs {

    private SalesDocs() {
    }

    public static SalesOrder findOrderByNo(ICommonDao<SalesOrder> orderDao, String documentNo) {
        if (orderDao == null || documentNo == null || documentNo.isBlank()) {
            return null;
        }
        String escaped = documentNo.replace("'", "''");
        return orderDao.firstOrDefault("from SalesOrder e where " + CompanyScope.predicate("e")
                + " and e.documentNo = '" + escaped + "'");
    }

    public static boolean hasOpenOrder(ICommonDao<SalesOrder> orderDao, Long quoteId) {
        return findOpenOrder(orderDao, quoteId) != null;
    }

    public static SalesOrder findOpenOrder(ICommonDao<SalesOrder> orderDao, Long quoteId) {
        if (orderDao == null || quoteId == null) {
            return null;
        }
        return orderDao.firstOrDefault(
                "from SalesOrder e where e.sourceQuoteId = " + quoteId
                        + " and e.status <> " + SalesOrder.STATUS_CANCELLED);
    }

    public static void syncQuoteStatus(ICommonDao<SalesQuote> quoteDao,
            ICommonDao<SalesOrder> orderDao, Long quoteId) {
        if (quoteDao == null || quoteId == null) {
            return;
        }
        SalesQuote quote = quoteDao.firstOrDefault(quoteId);
        if (quote == null || quote.isCancelled() || quote.isDraft()) {
            return;
        }
        boolean open = hasOpenOrder(orderDao, quoteId);
        int next = open ? SalesQuote.STATUS_CONVERTED : SalesQuote.STATUS_APPROVED;
        if (quote.getStatus() == next) {
            return;
        }
        quote.setStatus(next);
        quote.setLastUpdateDate(new Date());
        quoteDao.addOrUpdate(quote);
    }

    public static SalesOrderLine findOrderLine(ICommonDao<SalesOrderLine> orderLineDao,
            Long orderId, String itemCode) {
        if (orderLineDao == null || orderId == null || itemCode == null || itemCode.isBlank()) {
            return null;
        }
        String escaped = itemCode.replace("'", "''");
        return orderLineDao.firstOrDefault(
                "from SalesOrderLine e where e.orderId = " + orderId
                        + " and e.itemCode = '" + escaped + "'");
    }

    public static boolean hasPostedDelivery(ICommonDao<SalesDelivery> deliveryDao,
            ICommonDao<SalesDeliveryLine> lineDao, ICommonDao<SalesOrderLine> orderLineDao,
            Long orderId) {
        if (deliveryDao == null || orderId == null) {
            return false;
        }
        List<SalesDelivery> posted = deliveryDao.findAll(
                "from SalesDelivery e where e.sourceOrderId = " + orderId
                        + " and e.status = " + SalesDelivery.STATUS_POSTED);
        if (posted != null && !posted.isEmpty()) {
            return true;
        }
        List<SalesOrderLine> orderLines = orderLineDao == null ? null
                : orderLineDao.findAll("from SalesOrderLine e where e.orderId = " + orderId);
        if (orderLines == null || lineDao == null) {
            return false;
        }
        for (SalesOrderLine orderLine : orderLines) {
            if (orderLine == null || orderLine.getRowId() == null) {
                continue;
            }
            if (postedQuantity(deliveryDao, lineDao, orderLine.getRowId(), null) > 0d) {
                return true;
            }
        }
        return false;
    }

    public static double postedQuantity(ICommonDao<SalesDelivery> deliveryDao,
            ICommonDao<SalesDeliveryLine> lineDao, Long orderLineId, Long excludeDeliveryId) {
        return sumQuantity(deliveryDao, lineDao, orderLineId, excludeDeliveryId, false);
    }

    public static double allocatedQuantity(ICommonDao<SalesDelivery> deliveryDao,
            ICommonDao<SalesDeliveryLine> lineDao, Long orderLineId, Long excludeDeliveryId) {
        return sumQuantity(deliveryDao, lineDao, orderLineId, excludeDeliveryId, true);
    }

    public static double remainingOrdered(double ordered, double posted) {
        return Math.max(0d, ordered - posted);
    }

    public static double roundMoney(double value) {
        return Math.round(value * 100d) / 100d;
    }

    public static double netOf(Double quantity, Double unitPrice) {
        double qty = quantity == null ? 0d : quantity;
        double price = unitPrice == null ? 0d : unitPrice;
        return roundMoney(qty * price);
    }

    public static double vatOf(Double quantity, Double unitPrice, Double vatRate) {
        double rate = vatRate == null ? 0d : vatRate;
        return roundMoney(netOf(quantity, unitPrice) * rate / 100d);
    }

    public static double grossOf(Double quantity, Double unitPrice, Double vatRate) {
        return roundMoney(netOf(quantity, unitPrice) + vatOf(quantity, unitPrice, vatRate));
    }

    public static double netOf(List<SalesInvoiceLine> lines) {
        if (lines == null) {
            return 0d;
        }
        double sum = 0d;
        for (SalesInvoiceLine line : lines) {
            if (line != null) {
                sum += netOf(line.getQuantity(), line.getUnitPrice());
            }
        }
        return roundMoney(sum);
    }

    public static double vatOf(List<SalesInvoiceLine> lines) {
        if (lines == null) {
            return 0d;
        }
        double sum = 0d;
        for (SalesInvoiceLine line : lines) {
            if (line != null) {
                sum += vatOf(line.getQuantity(), line.getUnitPrice(), line.getVatRate());
            }
        }
        return roundMoney(sum);
    }

    public static double amountOf(List<SalesInvoiceLine> lines) {
        if (lines == null) {
            return 0d;
        }
        double sum = 0d;
        for (SalesInvoiceLine line : lines) {
            if (line != null) {
                sum += grossOf(line.getQuantity(), line.getUnitPrice(), line.getVatRate());
            }
        }
        return roundMoney(sum);
    }

    public static double quoteNetOf(List<SalesQuoteLine> lines) {
        if (lines == null) {
            return 0d;
        }
        double sum = 0d;
        for (SalesQuoteLine line : lines) {
            if (line != null) {
                sum += netOf(line.getQuantity(), line.getUnitPrice());
            }
        }
        return roundMoney(sum);
    }

    public static double quoteVatOf(List<SalesQuoteLine> lines) {
        if (lines == null) {
            return 0d;
        }
        double sum = 0d;
        for (SalesQuoteLine line : lines) {
            if (line != null) {
                sum += vatOf(line.getQuantity(), line.getUnitPrice(), line.getVatRate());
            }
        }
        return roundMoney(sum);
    }

    public static double quoteGrossOf(List<SalesQuoteLine> lines) {
        if (lines == null) {
            return 0d;
        }
        double sum = 0d;
        for (SalesQuoteLine line : lines) {
            if (line != null) {
                sum += grossOf(line.getQuantity(), line.getUnitPrice(), line.getVatRate());
            }
        }
        return roundMoney(sum);
    }

    public static List<SalesOrderStatusRow> orderStatus(
            ICommonDao<SalesOrder> orderDao, ICommonDao<SalesOrderLine> orderLineDao,
            ICommonDao<SalesDelivery> deliveryDao, ICommonDao<SalesDeliveryLine> deliveryLineDao) {
        List<SalesOrderStatusRow> result = new ArrayList<>();
        if (orderDao == null || orderLineDao == null) {
            return result;
        }
        List<SalesOrder> orders = orderDao.findAll(CompanyScope.from("SalesOrder")
                + " and e.status in (" + SalesOrder.STATUS_APPROVED
                + "," + SalesOrder.STATUS_DELIVERED + ") order by e.documentNo");
        if (orders == null || orders.isEmpty()) {
            return result;
        }
        Map<Long, SalesOrder> byId = new HashMap<>();
        StringBuilder ids = new StringBuilder();
        for (SalesOrder order : orders) {
            if (order == null || order.getRowId() == null) {
                continue;
            }
            byId.put(order.getRowId(), order);
            if (ids.length() > 0) {
                ids.append(',');
            }
            ids.append(order.getRowId());
        }
        if (byId.isEmpty()) {
            return result;
        }
        List<SalesOrderLine> lines = orderLineDao.findAll(
                "from SalesOrderLine e where e.orderId in (" + ids + ") order by e.orderId, e.lineNo");
        if (lines == null) {
            return result;
        }
        Map<Long, Double> deliveredByLine = postedByOrderLine(deliveryDao, deliveryLineDao);
        for (SalesOrderLine line : lines) {
            if (line == null) {
                continue;
            }
            SalesOrder order = byId.get(line.getOrderId());
            if (order == null) {
                continue;
            }
            double ordered = line.getQuantity() == null ? 0d : line.getQuantity();
            double delivered = deliveredByLine.getOrDefault(line.getRowId(), 0d);
            double remaining = remainingOrdered(ordered, delivered);
            SalesOrderStatusRow row = new SalesOrderStatusRow();
            row.setDocumentNo(order.getDocumentNo());
            row.setDateLabel(order.getDateLabel());
            row.setAccountCode(order.getAccountCode());
            row.setAccountName(order.getAccountName());
            row.setWarehouseCode(order.getWarehouseCode());
            row.setDocumentStatusLabel(order.getStatusLabel());
            row.setLineNo(line.getLineNo());
            row.setItemCode(line.getItemCode());
            row.setItemDesc(line.getItemDesc());
            row.setItemUnit(line.getItemUnit());
            row.setOrderedQty(ordered);
            row.setDeliveredQty(delivered);
            row.setRemainingQty(remaining);
            row.setBalanceLabel(remaining > 0.0000001d ? "Açık" : "Kapalı");
            result.add(row);
        }
        return result;
    }

    public static boolean hasDeliverableBalance(SalesOrder order,
            ICommonDao<SalesOrderLine> orderLineDao, ICommonDao<SalesDelivery> deliveryDao,
            ICommonDao<SalesDeliveryLine> lineDao) {
        if (order == null || order.getRowId() == null || order.isDraft() || order.isCancelled()) {
            return false;
        }
        return !remainingLines(order.getRowId(), orderLineDao, deliveryDao, lineDao, null).isEmpty();
    }

    public static List<SalesOrderLine> remainingLines(Long orderId,
            ICommonDao<SalesOrderLine> orderLineDao, ICommonDao<SalesDelivery> deliveryDao,
            ICommonDao<SalesDeliveryLine> lineDao, Long excludeDeliveryId) {
        List<SalesOrderLine> result = new ArrayList<>();
        if (orderId == null || orderLineDao == null) {
            return result;
        }
        List<SalesOrderLine> rows = orderLineDao.findAll(
                "from SalesOrderLine e where e.orderId = " + orderId + " order by e.lineNo");
        if (rows == null) {
            return result;
        }
        for (SalesOrderLine line : rows) {
            if (line == null) {
                continue;
            }
            double ordered = line.getQuantity() == null ? 0d : line.getQuantity();
            double allocated = allocatedQuantity(deliveryDao, lineDao, line.getRowId(), excludeDeliveryId);
            if (remainingOrdered(ordered, allocated) > 0.0000001d) {
                result.add(line);
            }
        }
        return result;
    }

    public static List<SalesOrder> deliverableOrders(ICommonDao<SalesOrder> orderDao,
            ICommonDao<SalesOrderLine> orderLineDao, ICommonDao<SalesDelivery> deliveryDao,
            ICommonDao<SalesDeliveryLine> lineDao) {
        List<SalesOrder> result = new ArrayList<>();
        if (orderDao == null) {
            return result;
        }
        List<SalesOrder> rows = orderDao.findAll(CompanyScope.from("SalesOrder")
                + " and e.status in (" + SalesOrder.STATUS_APPROVED
                + "," + SalesOrder.STATUS_DELIVERED + ")");
        if (rows == null) {
            return result;
        }
        for (SalesOrder order : rows) {
            if (hasDeliverableBalance(order, orderLineDao, deliveryDao, lineDao)) {
                result.add(order);
            }
        }
        return result;
    }

    public static void syncOrderStatus(ICommonDao<SalesOrder> orderDao,
            ICommonDao<SalesOrderLine> orderLineDao, ICommonDao<SalesDelivery> deliveryDao,
            ICommonDao<SalesDeliveryLine> lineDao, Long orderId) {
        if (orderDao == null || orderId == null) {
            return;
        }
        SalesOrder order = orderDao.firstOrDefault(orderId);
        if (order == null || order.isCancelled() || order.isDraft()) {
            return;
        }
        boolean remaining = hasOpenOrderedBalance(orderId, orderLineDao, deliveryDao, lineDao);
        int next = remaining ? SalesOrder.STATUS_APPROVED : SalesOrder.STATUS_DELIVERED;
        if (order.getStatus() == next) {
            return;
        }
        order.setStatus(next);
        order.setLastUpdateDate(new Date());
        orderDao.addOrUpdate(order);
    }

    public static void syncOrdersForLines(ICommonDao<SalesOrder> orderDao,
            ICommonDao<SalesOrderLine> orderLineDao, ICommonDao<SalesDelivery> deliveryDao,
            ICommonDao<SalesDeliveryLine> lineDao, Long headerOrderId,
            List<SalesDeliveryLine> deliveryLines) {
        Set<Long> ids = new LinkedHashSet<>();
        if (headerOrderId != null) {
            ids.add(headerOrderId);
        }
        if (deliveryLines != null) {
            for (SalesDeliveryLine line : deliveryLines) {
                if (line != null && line.getSourceOrderId() != null) {
                    ids.add(line.getSourceOrderId());
                }
            }
        }
        for (Long id : ids) {
            syncOrderStatus(orderDao, orderLineDao, deliveryDao, lineDao, id);
        }
    }

    public static SalesDelivery findDeliveryByNo(ICommonDao<SalesDelivery> deliveryDao, String documentNo) {
        if (deliveryDao == null || documentNo == null || documentNo.isBlank()) {
            return null;
        }
        String escaped = documentNo.replace("'", "''");
        return deliveryDao.firstOrDefault("from SalesDelivery e where " + CompanyScope.predicate("e")
                + " and e.documentNo = '" + escaped + "'");
    }

    public static SalesDeliveryLine findDeliveryLine(ICommonDao<SalesDeliveryLine> deliveryLineDao,
            Long deliveryId, String itemCode) {
        if (deliveryLineDao == null || deliveryId == null || itemCode == null || itemCode.isBlank()) {
            return null;
        }
        String escaped = itemCode.replace("'", "''");
        return deliveryLineDao.firstOrDefault(
                "from SalesDeliveryLine e where e.deliveryId = " + deliveryId
                        + " and e.itemCode = '" + escaped + "'");
    }

    public static boolean hasLinkedInvoice(ICommonDao<SalesInvoice> invoiceDao,
            ICommonDao<SalesInvoiceLine> invoiceLineDao, ICommonDao<SalesDeliveryLine> deliveryLineDao,
            Long deliveryId) {
        if (invoiceDao == null || deliveryId == null) {
            return false;
        }
        List<SalesInvoice> linked = invoiceDao.findAll(
                "from SalesInvoice e where e.sourceDeliveryId = " + deliveryId
                        + " and e.status <> " + SalesInvoice.STATUS_CANCELLED);
        if (linked != null && !linked.isEmpty()) {
            return true;
        }
        if (invoiceLineDao == null) {
            return false;
        }
        List<SalesInvoiceLine> lines = invoiceLineDao.findAll(
                "from SalesInvoiceLine e where e.sourceDeliveryId = " + deliveryId);
        if (lines != null) {
            for (SalesInvoiceLine line : lines) {
                if (line == null || line.getInvoiceId() == null) {
                    continue;
                }
                SalesInvoice invoice = invoiceDao.firstOrDefault(line.getInvoiceId());
                if (invoice != null && !invoice.isCancelled()) {
                    return true;
                }
            }
        }
        List<SalesDeliveryLine> deliveryLines = deliveryLineDao == null ? null
                : deliveryLineDao.findAll("from SalesDeliveryLine e where e.deliveryId = " + deliveryId);
        if (deliveryLines == null) {
            return false;
        }
        for (SalesDeliveryLine deliveryLine : deliveryLines) {
            if (deliveryLine == null || deliveryLine.getRowId() == null) {
                continue;
            }
            if (allocatedInvoiceQuantity(invoiceDao, invoiceLineDao, deliveryLine.getRowId(), null) > 0d) {
                return true;
            }
        }
        return false;
    }

    public static double postedInvoiceQuantity(ICommonDao<SalesInvoice> invoiceDao,
            ICommonDao<SalesInvoiceLine> lineDao, Long deliveryLineId, Long excludeInvoiceId) {
        return sumInvoiceQuantity(invoiceDao, lineDao, deliveryLineId, excludeInvoiceId, false);
    }

    public static double allocatedInvoiceQuantity(ICommonDao<SalesInvoice> invoiceDao,
            ICommonDao<SalesInvoiceLine> lineDao, Long deliveryLineId, Long excludeInvoiceId) {
        return sumInvoiceQuantity(invoiceDao, lineDao, deliveryLineId, excludeInvoiceId, true);
    }

    public static boolean hasInvoiceableBalance(SalesDelivery delivery,
            ICommonDao<SalesDeliveryLine> deliveryLineDao, ICommonDao<SalesInvoice> invoiceDao,
            ICommonDao<SalesInvoiceLine> invoiceLineDao) {
        if (delivery == null || delivery.getRowId() == null || !delivery.canInvoice()) {
            return false;
        }
        return !remainingInvoiceLines(delivery.getRowId(), deliveryLineDao, invoiceDao, invoiceLineDao, null)
                .isEmpty();
    }

    public static List<SalesDeliveryLine> remainingInvoiceLines(Long deliveryId,
            ICommonDao<SalesDeliveryLine> deliveryLineDao, ICommonDao<SalesInvoice> invoiceDao,
            ICommonDao<SalesInvoiceLine> invoiceLineDao, Long excludeInvoiceId) {
        List<SalesDeliveryLine> result = new ArrayList<>();
        if (deliveryId == null || deliveryLineDao == null) {
            return result;
        }
        List<SalesDeliveryLine> rows = deliveryLineDao.findAll(
                "from SalesDeliveryLine e where e.deliveryId = " + deliveryId + " order by e.lineNo");
        if (rows == null) {
            return result;
        }
        for (SalesDeliveryLine line : rows) {
            if (line == null) {
                continue;
            }
            double delivered = line.getQuantity() == null ? 0d : line.getQuantity();
            double allocated = allocatedInvoiceQuantity(invoiceDao, invoiceLineDao, line.getRowId(),
                    excludeInvoiceId);
            if (remainingOrdered(delivered, allocated) > 0.0000001d) {
                result.add(line);
            }
        }
        return result;
    }

    public static List<SalesDelivery> invoiceableDeliveries(ICommonDao<SalesDelivery> deliveryDao,
            ICommonDao<SalesDeliveryLine> deliveryLineDao, ICommonDao<SalesInvoice> invoiceDao,
            ICommonDao<SalesInvoiceLine> invoiceLineDao) {
        List<SalesDelivery> result = new ArrayList<>();
        if (deliveryDao == null) {
            return result;
        }
        List<SalesDelivery> rows = deliveryDao.findAll(CompanyScope.from("SalesDelivery")
                + " and e.status in (" + SalesDelivery.STATUS_POSTED
                + "," + SalesDelivery.STATUS_INVOICED + ")");
        if (rows == null) {
            return result;
        }
        for (SalesDelivery delivery : rows) {
            if (hasInvoiceableBalance(delivery, deliveryLineDao, invoiceDao, invoiceLineDao)) {
                result.add(delivery);
            }
        }
        return result;
    }

    public static void syncDeliveryStatus(ICommonDao<SalesDelivery> deliveryDao,
            ICommonDao<SalesDeliveryLine> deliveryLineDao, ICommonDao<SalesInvoice> invoiceDao,
            ICommonDao<SalesInvoiceLine> invoiceLineDao, Long deliveryId) {
        if (deliveryDao == null || deliveryId == null) {
            return;
        }
        SalesDelivery delivery = deliveryDao.firstOrDefault(deliveryId);
        if (delivery == null || delivery.isCancelled() || delivery.isDraft()) {
            return;
        }
        boolean remaining = hasOpenInvoiceBalance(deliveryId, deliveryLineDao, invoiceDao, invoiceLineDao);
        int next = remaining ? SalesDelivery.STATUS_POSTED : SalesDelivery.STATUS_INVOICED;
        if (delivery.getStatus() == next) {
            return;
        }
        delivery.setStatus(next);
        delivery.setLastUpdateDate(new Date());
        deliveryDao.addOrUpdate(delivery);
    }

    public static void syncDeliveriesForLines(ICommonDao<SalesDelivery> deliveryDao,
            ICommonDao<SalesDeliveryLine> deliveryLineDao, ICommonDao<SalesInvoice> invoiceDao,
            ICommonDao<SalesInvoiceLine> invoiceLineDao, Long headerDeliveryId,
            List<SalesInvoiceLine> invoiceLines) {
        Set<Long> ids = new LinkedHashSet<>();
        if (headerDeliveryId != null) {
            ids.add(headerDeliveryId);
        }
        if (invoiceLines != null) {
            for (SalesInvoiceLine line : invoiceLines) {
                if (line != null && line.getSourceDeliveryId() != null) {
                    ids.add(line.getSourceDeliveryId());
                }
            }
        }
        for (Long id : ids) {
            syncDeliveryStatus(deliveryDao, deliveryLineDao, invoiceDao, invoiceLineDao, id);
        }
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

    private static double sumQuantity(ICommonDao<SalesDelivery> deliveryDao,
            ICommonDao<SalesDeliveryLine> lineDao, Long orderLineId, Long excludeDeliveryId,
            boolean includeDraft) {
        if (deliveryDao == null || lineDao == null || orderLineId == null) {
            return 0d;
        }
        List<SalesDeliveryLine> rows = lineDao.findAll(
                "from SalesDeliveryLine e where e.sourceOrderLineId = " + orderLineId);
        if (rows == null) {
            return 0d;
        }
        double sum = 0d;
        for (SalesDeliveryLine line : rows) {
            if (line == null || line.getDeliveryId() == null) {
                continue;
            }
            if (excludeDeliveryId != null && excludeDeliveryId.equals(line.getDeliveryId())) {
                continue;
            }
            SalesDelivery delivery = deliveryDao.firstOrDefault(line.getDeliveryId());
            if (delivery == null || delivery.isCancelled()) {
                continue;
            }
            if (delivery.isPosted() || (includeDraft && delivery.isDraft())) {
                sum += line.getQuantity() == null ? 0d : line.getQuantity();
            }
        }
        return sum;
    }

    private static double sumInvoiceQuantity(ICommonDao<SalesInvoice> invoiceDao,
            ICommonDao<SalesInvoiceLine> lineDao, Long deliveryLineId, Long excludeInvoiceId,
            boolean includeDraft) {
        if (invoiceDao == null || lineDao == null || deliveryLineId == null) {
            return 0d;
        }
        List<SalesInvoiceLine> rows = lineDao.findAll(
                "from SalesInvoiceLine e where e.sourceDeliveryLineId = " + deliveryLineId);
        if (rows == null) {
            return 0d;
        }
        double sum = 0d;
        for (SalesInvoiceLine line : rows) {
            if (line == null || line.getInvoiceId() == null) {
                continue;
            }
            if (excludeInvoiceId != null && excludeInvoiceId.equals(line.getInvoiceId())) {
                continue;
            }
            SalesInvoice invoice = invoiceDao.firstOrDefault(line.getInvoiceId());
            if (invoice == null || invoice.isCancelled()) {
                continue;
            }
            if (invoice.isPosted() || (includeDraft && invoice.isDraft())) {
                sum += line.getQuantity() == null ? 0d : line.getQuantity();
            }
        }
        return sum;
    }

    private static boolean hasOpenInvoiceBalance(Long deliveryId,
            ICommonDao<SalesDeliveryLine> deliveryLineDao, ICommonDao<SalesInvoice> invoiceDao,
            ICommonDao<SalesInvoiceLine> invoiceLineDao) {
        if (deliveryLineDao == null) {
            return true;
        }
        List<SalesDeliveryLine> rows = deliveryLineDao.findAll(
                "from SalesDeliveryLine e where e.deliveryId = " + deliveryId);
        if (rows == null || rows.isEmpty()) {
            return false;
        }
        for (SalesDeliveryLine line : rows) {
            if (line == null) {
                continue;
            }
            double delivered = line.getQuantity() == null ? 0d : line.getQuantity();
            double posted = postedInvoiceQuantity(invoiceDao, invoiceLineDao, line.getRowId(), null);
            if (remainingOrdered(delivered, posted) > 0.0000001d) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasOpenOrderedBalance(Long orderId, ICommonDao<SalesOrderLine> orderLineDao,
            ICommonDao<SalesDelivery> deliveryDao, ICommonDao<SalesDeliveryLine> lineDao) {
        if (orderLineDao == null) {
            return true;
        }
        List<SalesOrderLine> rows = orderLineDao.findAll(
                "from SalesOrderLine e where e.orderId = " + orderId);
        if (rows == null || rows.isEmpty()) {
            return false;
        }
        for (SalesOrderLine line : rows) {
            if (line == null) {
                continue;
            }
            double ordered = line.getQuantity() == null ? 0d : line.getQuantity();
            double posted = postedQuantity(deliveryDao, lineDao, line.getRowId(), null);
            if (remainingOrdered(ordered, posted) > 0.0000001d) {
                return true;
            }
        }
        return false;
    }

    private static Map<Long, Double> postedByOrderLine(ICommonDao<SalesDelivery> deliveryDao,
            ICommonDao<SalesDeliveryLine> lineDao) {
        Map<Long, Double> totals = new HashMap<>();
        if (deliveryDao == null || lineDao == null) {
            return totals;
        }
        List<SalesDelivery> deliveries = deliveryDao.findAll(CompanyScope.from("SalesDelivery")
                + " and e.status = " + SalesDelivery.STATUS_POSTED);
        if (deliveries == null || deliveries.isEmpty()) {
            return totals;
        }
        Set<Long> postedIds = new LinkedHashSet<>();
        StringBuilder ids = new StringBuilder();
        for (SalesDelivery delivery : deliveries) {
            if (delivery == null || delivery.getRowId() == null) {
                continue;
            }
            postedIds.add(delivery.getRowId());
            if (ids.length() > 0) {
                ids.append(',');
            }
            ids.append(delivery.getRowId());
        }
        if (postedIds.isEmpty()) {
            return totals;
        }
        List<SalesDeliveryLine> lines = lineDao.findAll(
                "from SalesDeliveryLine e where e.deliveryId in (" + ids + ")");
        if (lines == null) {
            return totals;
        }
        for (SalesDeliveryLine line : lines) {
            if (line == null || line.getSourceOrderLineId() == null) {
                continue;
            }
            double qty = line.getQuantity() == null ? 0d : line.getQuantity();
            totals.merge(line.getSourceOrderLineId(), qty, Double::sum);
        }
        return totals;
    }
}

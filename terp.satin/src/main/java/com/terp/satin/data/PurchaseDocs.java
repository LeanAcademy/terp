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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
        List<PurchaseReceipt> posted = receiptDao.findAll(
                "from PurchaseReceipt e where e.sourceOrderId = " + orderId
                        + " and e.status = " + PurchaseReceipt.STATUS_POSTED);
        if (posted != null && !posted.isEmpty()) {
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

    public static double invoiceNetOf(List<PurchaseInvoiceLine> lines) {
        if (lines == null) {
            return 0d;
        }
        double sum = 0d;
        for (PurchaseInvoiceLine line : lines) {
            if (line != null) {
                sum += netOf(line.getQuantity(), line.getUnitPrice());
            }
        }
        return roundMoney(sum);
    }

    public static double invoiceVatOf(List<PurchaseInvoiceLine> lines) {
        if (lines == null) {
            return 0d;
        }
        double sum = 0d;
        for (PurchaseInvoiceLine line : lines) {
            if (line != null) {
                sum += vatOf(line.getQuantity(), line.getUnitPrice(), line.getVatRate());
            }
        }
        return roundMoney(sum);
    }

    public static double invoiceAmountOf(List<PurchaseInvoiceLine> lines) {
        if (lines == null) {
            return 0d;
        }
        double sum = 0d;
        for (PurchaseInvoiceLine line : lines) {
            if (line != null) {
                sum += grossOf(line.getQuantity(), line.getUnitPrice(), line.getVatRate());
            }
        }
        return roundMoney(sum);
    }

    public static double amountOf(List<PurchaseReceiptLine> lines) {
        if (lines == null) {
            return 0d;
        }
        double sum = 0d;
        for (PurchaseReceiptLine line : lines) {
            if (line == null) {
                continue;
            }
            double qty = line.getQuantity() == null ? 0d : line.getQuantity();
            double price = line.getUnitPrice() == null ? 0d : line.getUnitPrice();
            sum += qty * price;
        }
        return sum;
    }

    public static List<PurchaseOrderStatusRow> orderStatus(
            ICommonDao<PurchaseOrder> orderDao, ICommonDao<PurchaseOrderLine> orderLineDao,
            ICommonDao<PurchaseReceipt> receiptDao, ICommonDao<PurchaseReceiptLine> receiptLineDao) {
        List<PurchaseOrderStatusRow> result = new ArrayList<>();
        if (orderDao == null || orderLineDao == null) {
            return result;
        }
        List<PurchaseOrder> orders = orderDao.findAll(CompanyScope.from("PurchaseOrder")
                + " and e.status in (" + PurchaseOrder.STATUS_APPROVED
                + "," + PurchaseOrder.STATUS_RECEIVED + ") order by e.documentNo");
        if (orders == null || orders.isEmpty()) {
            return result;
        }
        Map<Long, PurchaseOrder> byId = new HashMap<>();
        StringBuilder ids = new StringBuilder();
        for (PurchaseOrder order : orders) {
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
        List<PurchaseOrderLine> lines = orderLineDao.findAll(
                "from PurchaseOrderLine e where e.orderId in (" + ids + ") order by e.orderId, e.lineNo");
        if (lines == null) {
            return result;
        }
        Map<Long, Double> deliveredByLine = postedByOrderLine(receiptDao, receiptLineDao);
        for (PurchaseOrderLine line : lines) {
            if (line == null) {
                continue;
            }
            PurchaseOrder order = byId.get(line.getOrderId());
            if (order == null) {
                continue;
            }
            double ordered = line.getQuantity() == null ? 0d : line.getQuantity();
            double delivered = deliveredByLine.getOrDefault(line.getRowId(), 0d);
            double remaining = remainingOrdered(ordered, delivered);
            PurchaseOrderStatusRow row = new PurchaseOrderStatusRow();
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

    public static PurchaseReceipt findReceiptByNo(ICommonDao<PurchaseReceipt> receiptDao, String documentNo) {
        if (receiptDao == null || documentNo == null || documentNo.isBlank()) {
            return null;
        }
        String escaped = documentNo.replace("'", "''");
        return receiptDao.firstOrDefault("from PurchaseReceipt e where " + CompanyScope.predicate("e")
                + " and e.documentNo = '" + escaped + "'");
    }

    public static PurchaseReceiptLine findReceiptLine(ICommonDao<PurchaseReceiptLine> receiptLineDao,
            Long receiptId, String itemCode) {
        if (receiptLineDao == null || receiptId == null || itemCode == null || itemCode.isBlank()) {
            return null;
        }
        String escaped = itemCode.replace("'", "''");
        return receiptLineDao.firstOrDefault(
                "from PurchaseReceiptLine e where e.receiptId = " + receiptId
                        + " and e.itemCode = '" + escaped + "'");
    }

    public static boolean hasLinkedInvoice(ICommonDao<PurchaseInvoice> invoiceDao,
            ICommonDao<PurchaseInvoiceLine> invoiceLineDao, ICommonDao<PurchaseReceiptLine> receiptLineDao,
            Long receiptId) {
        if (invoiceDao == null || receiptId == null) {
            return false;
        }
        List<PurchaseInvoice> linked = invoiceDao.findAll(
                "from PurchaseInvoice e where e.sourceReceiptId = " + receiptId
                        + " and e.status <> " + PurchaseInvoice.STATUS_CANCELLED);
        if (linked != null && !linked.isEmpty()) {
            return true;
        }
        if (invoiceLineDao == null) {
            return false;
        }
        List<PurchaseInvoiceLine> lines = invoiceLineDao.findAll(
                "from PurchaseInvoiceLine e where e.sourceReceiptId = " + receiptId);
        if (lines != null) {
            for (PurchaseInvoiceLine line : lines) {
                if (line == null || line.getInvoiceId() == null) {
                    continue;
                }
                PurchaseInvoice invoice = invoiceDao.firstOrDefault(line.getInvoiceId());
                if (invoice != null && !invoice.isCancelled()) {
                    return true;
                }
            }
        }
        List<PurchaseReceiptLine> receiptLines = receiptLineDao == null ? null
                : receiptLineDao.findAll("from PurchaseReceiptLine e where e.receiptId = " + receiptId);
        if (receiptLines == null) {
            return false;
        }
        for (PurchaseReceiptLine receiptLine : receiptLines) {
            if (receiptLine == null || receiptLine.getRowId() == null) {
                continue;
            }
            if (allocatedInvoiceQuantity(invoiceDao, invoiceLineDao, receiptLine.getRowId(), null) > 0d) {
                return true;
            }
        }
        return false;
    }

    public static double postedInvoiceQuantity(ICommonDao<PurchaseInvoice> invoiceDao,
            ICommonDao<PurchaseInvoiceLine> lineDao, Long receiptLineId, Long excludeInvoiceId) {
        return sumInvoiceQuantity(invoiceDao, lineDao, receiptLineId, excludeInvoiceId, false);
    }

    public static double allocatedInvoiceQuantity(ICommonDao<PurchaseInvoice> invoiceDao,
            ICommonDao<PurchaseInvoiceLine> lineDao, Long receiptLineId, Long excludeInvoiceId) {
        return sumInvoiceQuantity(invoiceDao, lineDao, receiptLineId, excludeInvoiceId, true);
    }

    public static boolean hasInvoiceableBalance(PurchaseReceipt receipt,
            ICommonDao<PurchaseReceiptLine> receiptLineDao, ICommonDao<PurchaseInvoice> invoiceDao,
            ICommonDao<PurchaseInvoiceLine> invoiceLineDao) {
        if (receipt == null || receipt.getRowId() == null || !receipt.canInvoice()) {
            return false;
        }
        return !remainingInvoiceLines(receipt.getRowId(), receiptLineDao, invoiceDao, invoiceLineDao, null)
                .isEmpty();
    }

    public static List<PurchaseReceiptLine> remainingInvoiceLines(Long receiptId,
            ICommonDao<PurchaseReceiptLine> receiptLineDao, ICommonDao<PurchaseInvoice> invoiceDao,
            ICommonDao<PurchaseInvoiceLine> invoiceLineDao, Long excludeInvoiceId) {
        List<PurchaseReceiptLine> result = new ArrayList<>();
        if (receiptId == null || receiptLineDao == null) {
            return result;
        }
        List<PurchaseReceiptLine> rows = receiptLineDao.findAll(
                "from PurchaseReceiptLine e where e.receiptId = " + receiptId + " order by e.lineNo");
        if (rows == null) {
            return result;
        }
        for (PurchaseReceiptLine line : rows) {
            if (line == null) {
                continue;
            }
            double received = line.getQuantity() == null ? 0d : line.getQuantity();
            double allocated = allocatedInvoiceQuantity(invoiceDao, invoiceLineDao, line.getRowId(),
                    excludeInvoiceId);
            if (remainingOrdered(received, allocated) > 0.0000001d) {
                result.add(line);
            }
        }
        return result;
    }

    public static List<PurchaseReceipt> invoiceableReceipts(ICommonDao<PurchaseReceipt> receiptDao,
            ICommonDao<PurchaseReceiptLine> receiptLineDao, ICommonDao<PurchaseInvoice> invoiceDao,
            ICommonDao<PurchaseInvoiceLine> invoiceLineDao) {
        List<PurchaseReceipt> result = new ArrayList<>();
        if (receiptDao == null) {
            return result;
        }
        List<PurchaseReceipt> rows = receiptDao.findAll(CompanyScope.from("PurchaseReceipt")
                + " and e.status in (" + PurchaseReceipt.STATUS_POSTED
                + "," + PurchaseReceipt.STATUS_INVOICED + ")");
        if (rows == null) {
            return result;
        }
        for (PurchaseReceipt receipt : rows) {
            if (hasInvoiceableBalance(receipt, receiptLineDao, invoiceDao, invoiceLineDao)) {
                result.add(receipt);
            }
        }
        return result;
    }

    public static void syncReceiptStatus(ICommonDao<PurchaseReceipt> receiptDao,
            ICommonDao<PurchaseReceiptLine> receiptLineDao, ICommonDao<PurchaseInvoice> invoiceDao,
            ICommonDao<PurchaseInvoiceLine> invoiceLineDao, Long receiptId) {
        if (receiptDao == null || receiptId == null) {
            return;
        }
        PurchaseReceipt receipt = receiptDao.firstOrDefault(receiptId);
        if (receipt == null || receipt.isCancelled() || receipt.isDraft()) {
            return;
        }
        boolean remaining = hasOpenInvoiceBalance(receiptId, receiptLineDao, invoiceDao, invoiceLineDao);
        int next = remaining ? PurchaseReceipt.STATUS_POSTED : PurchaseReceipt.STATUS_INVOICED;
        if (receipt.getStatus() == next) {
            return;
        }
        receipt.setStatus(next);
        receipt.setLastUpdateDate(new Date());
        receiptDao.addOrUpdate(receipt);
    }

    public static void syncReceiptsForLines(ICommonDao<PurchaseReceipt> receiptDao,
            ICommonDao<PurchaseReceiptLine> receiptLineDao, ICommonDao<PurchaseInvoice> invoiceDao,
            ICommonDao<PurchaseInvoiceLine> invoiceLineDao, Long headerReceiptId,
            List<PurchaseInvoiceLine> invoiceLines) {
        Set<Long> ids = new LinkedHashSet<>();
        if (headerReceiptId != null) {
            ids.add(headerReceiptId);
        }
        if (invoiceLines != null) {
            for (PurchaseInvoiceLine line : invoiceLines) {
                if (line != null && line.getSourceReceiptId() != null) {
                    ids.add(line.getSourceReceiptId());
                }
            }
        }
        for (Long id : ids) {
            syncReceiptStatus(receiptDao, receiptLineDao, invoiceDao, invoiceLineDao, id);
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
            if (row instanceof com.terp.plugin.data.ICommonFields fields && fields.getRowId() != null) {
                lineDao.delete(fields.getRowId());
            }
        }
    }

    private static double sumInvoiceQuantity(ICommonDao<PurchaseInvoice> invoiceDao,
            ICommonDao<PurchaseInvoiceLine> lineDao, Long receiptLineId, Long excludeInvoiceId,
            boolean includeDraft) {
        if (invoiceDao == null || lineDao == null || receiptLineId == null) {
            return 0d;
        }
        List<PurchaseInvoiceLine> rows = lineDao.findAll(
                "from PurchaseInvoiceLine e where e.sourceReceiptLineId = " + receiptLineId);
        if (rows == null) {
            return 0d;
        }
        double sum = 0d;
        for (PurchaseInvoiceLine line : rows) {
            if (line == null || line.getInvoiceId() == null) {
                continue;
            }
            if (excludeInvoiceId != null && excludeInvoiceId.equals(line.getInvoiceId())) {
                continue;
            }
            PurchaseInvoice invoice = invoiceDao.firstOrDefault(line.getInvoiceId());
            if (invoice == null || invoice.isCancelled()) {
                continue;
            }
            if (invoice.isPosted() || (includeDraft && invoice.isDraft())) {
                sum += line.getQuantity() == null ? 0d : line.getQuantity();
            }
        }
        return sum;
    }

    private static boolean hasOpenInvoiceBalance(Long receiptId,
            ICommonDao<PurchaseReceiptLine> receiptLineDao, ICommonDao<PurchaseInvoice> invoiceDao,
            ICommonDao<PurchaseInvoiceLine> invoiceLineDao) {
        if (receiptLineDao == null) {
            return true;
        }
        List<PurchaseReceiptLine> rows = receiptLineDao.findAll(
                "from PurchaseReceiptLine e where e.receiptId = " + receiptId);
        if (rows == null || rows.isEmpty()) {
            return false;
        }
        for (PurchaseReceiptLine line : rows) {
            if (line == null) {
                continue;
            }
            double received = line.getQuantity() == null ? 0d : line.getQuantity();
            double posted = postedInvoiceQuantity(invoiceDao, invoiceLineDao, line.getRowId(), null);
            if (remainingOrdered(received, posted) > 0.0000001d) {
                return true;
            }
        }
        return false;
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

    private static Map<Long, Double> postedByOrderLine(ICommonDao<PurchaseReceipt> receiptDao,
            ICommonDao<PurchaseReceiptLine> lineDao) {
        Map<Long, Double> totals = new HashMap<>();
        if (receiptDao == null || lineDao == null) {
            return totals;
        }
        List<PurchaseReceipt> receipts = receiptDao.findAll(CompanyScope.from("PurchaseReceipt")
                + " and e.status = " + PurchaseReceipt.STATUS_POSTED);
        if (receipts == null || receipts.isEmpty()) {
            return totals;
        }
        Set<Long> postedIds = new LinkedHashSet<>();
        StringBuilder ids = new StringBuilder();
        for (PurchaseReceipt receipt : receipts) {
            if (receipt == null || receipt.getRowId() == null) {
                continue;
            }
            postedIds.add(receipt.getRowId());
            if (ids.length() > 0) {
                ids.append(',');
            }
            ids.append(receipt.getRowId());
        }
        if (postedIds.isEmpty()) {
            return totals;
        }
        List<PurchaseReceiptLine> lines = lineDao.findAll(
                "from PurchaseReceiptLine e where e.receiptId in (" + ids + ")");
        if (lines == null) {
            return totals;
        }
        for (PurchaseReceiptLine line : lines) {
            if (line == null || line.getSourceOrderLineId() == null) {
                continue;
            }
            double qty = line.getQuantity() == null ? 0d : line.getQuantity();
            totals.merge(line.getSourceOrderLineId(), qty, Double::sum);
        }
        return totals;
    }
}

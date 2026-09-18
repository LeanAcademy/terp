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
package com.terp.satis.gui;

import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.IProductionOrders;
import com.terp.plugin.data.ProductionFromSales;
import com.terp.satis.data.SalesDelivery;
import com.terp.satis.data.SalesDeliveryLine;
import com.terp.satis.data.SalesDocs;
import com.terp.satis.data.SalesInvoice;
import com.terp.satis.data.SalesInvoiceLine;
import com.terp.satis.data.SalesOrder;
import com.terp.satis.data.SalesOrderLine;
import com.terp.satis.data.SalesQuote;
import com.terp.satis.data.SalesQuoteLine;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

final class SatisWindows {

    private static final Logger LOG = Logger.getLogger(SatisWindows.class.getName());

    private SatisWindows() {
    }

    static void convertQuote(SalesQuote quote, ICommonDao<SalesQuote> quoteDao,
            ICommonDao<SalesQuoteLine> lineDao, ICommonDao<SalesOrder> orderDao,
            Runnable afterClose) {
        if (quote == null || quote.getRowId() == null) {
            return;
        }
        SalesQuote fresh = quoteDao.firstOrDefault(quote.getRowId());
        if (fresh == null || !fresh.canConvert()) {
            SatisDates.showError("Satış teklifi", "Çevrilemez",
                    "Yalnız onaylı teklifler siparişe çevrilir.");
            return;
        }
        SalesOrder existing = SalesDocs.findOpenOrder(orderDao, fresh.getRowId());
        if (existing != null) {
            SatisDates.showError("Satış teklifi", "Sipariş var",
                    "Bu teklif için açık sipariş zaten var: " + existing.getDocumentNo());
            return;
        }
        List<SalesQuoteLine> lines = lineDao.findAll(
                "from SalesQuoteLine e where e.quoteId = " + fresh.getRowId()
                        + " order by e.lineNo");
        if (lines == null || lines.isEmpty()) {
            SatisDates.showError("Satış teklifi", "Satır yok", "Teklifin malzeme satırı yok.");
            return;
        }
        openOrderEditor(null, fresh, lines, afterClose);
    }

    static void convertOrder(SalesOrder order, ICommonDao<SalesOrder> orderDao,
            ICommonDao<SalesOrderLine> lineDao, ICommonDao<SalesDelivery> deliveryDao,
            Runnable afterClose) {
        if (order == null || order.getRowId() == null) {
            return;
        }
        SalesOrder fresh = orderDao.firstOrDefault(order.getRowId());
        if (fresh == null || !fresh.canDeliver()) {
            SatisDates.showError("Satış siparişi", "Çevrilemez",
                    "Yalnız onaylı veya bakiyesi kalan siparişler irsaliyeye alınır.");
            return;
        }
        ICommonDao<SalesDeliveryLine> deliveryLineDao = TerpApplication.getInstance()
                .getPersistence().createDao(SalesDeliveryLine.class);
        if (!SalesDocs.hasDeliverableBalance(fresh, lineDao, deliveryDao, deliveryLineDao)) {
            SatisDates.showError("Satış siparişi", "Bakiye yok",
                    "Bu siparişte kalan miktar yok.");
            return;
        }
        List<SalesOrderLine> lines = SalesDocs.remainingLines(fresh.getRowId(), lineDao,
                deliveryDao, deliveryLineDao, null);
        openDeliveryEditor(null, fresh, lines, afterClose);
    }

    static void convertToProduction(SalesOrder order, ICommonDao<SalesOrder> orderDao,
            ICommonDao<SalesOrderLine> lineDao, Runnable afterClose) {
        IProductionOrders production = TerpApplication.getInstance().getProductionOrders();
        if (production == null) {
            SatisDates.showError("Satış siparişi", "Üretim eklentisi yok",
                    "Üretim emri için terp.uretim yüklü olmalıdır.");
            return;
        }
        if (order == null || order.getRowId() == null) {
            return;
        }
        SalesOrder fresh = orderDao.firstOrDefault(order.getRowId());
        if (fresh == null || !fresh.canDeliver()) {
            SatisDates.showError("Satış siparişi", "Çevrilemez",
                    "Yalnız onaylı siparişler üretim emrine alınır.");
            return;
        }
        List<SalesOrderLine> lines = lineDao.findAll(
                "from SalesOrderLine e where e.orderId = " + fresh.getRowId()
                        + " order by e.lineNo");
        if (lines == null || lines.isEmpty()) {
            SatisDates.showError("Satış siparişi", "Satır yok", "Siparişin malzeme satırı yok.");
            return;
        }
        for (SalesOrderLine line : lines) {
            if (line == null || line.getItemCode() == null || !production.hasActiveRecipe(line.getItemCode())) {
                continue;
            }
            double remaining = (line.getQuantity() == null ? 0d : line.getQuantity())
                    - production.allocatedForSalesLine(line.getRowId());
            if (remaining <= 0.0000001d) {
                continue;
            }
            ProductionFromSales request = new ProductionFromSales();
            request.setSourceOrderId(fresh.getRowId());
            request.setSourceOrderNo(fresh.getDocumentNo());
            request.setSourceOrderLineId(line.getRowId());
            request.setWarehouseCode(fresh.getWarehouseCode());
            request.setWarehouseName(fresh.getWarehouseName());
            request.setProductCode(line.getItemCode());
            request.setProductName(line.getItemDesc());
            request.setProductUnit(line.getItemUnit());
            request.setQuantity(remaining);
            production.openFromSales(request);
            if (afterClose != null) {
                afterClose.run();
            }
            return;
        }
        SatisDates.showError("Satış siparişi", "Bakiye yok",
                "Reçetesi olan ve henüz üretim emrine alınmamış kalan mamul yok.");
    }

    static boolean hasProducibleBalance(SalesOrder order, ICommonDao<SalesOrderLine> lineDao) {
        IProductionOrders production = TerpApplication.getInstance().getProductionOrders();
        if (production == null || order == null || order.getRowId() == null || !order.canDeliver()) {
            return false;
        }
        List<SalesOrderLine> lines = lineDao.findAll(
                "from SalesOrderLine e where e.orderId = " + order.getRowId());
        if (lines == null) {
            return false;
        }
        for (SalesOrderLine line : lines) {
            if (line == null || line.getItemCode() == null || !production.hasActiveRecipe(line.getItemCode())) {
                continue;
            }
            double remaining = (line.getQuantity() == null ? 0d : line.getQuantity())
                    - production.allocatedForSalesLine(line.getRowId());
            if (remaining > 0.0000001d) {
                return true;
            }
        }
        return false;
    }

    static void convertDelivery(SalesDelivery delivery, ICommonDao<SalesDelivery> deliveryDao,
            ICommonDao<SalesDeliveryLine> lineDao, ICommonDao<SalesInvoice> invoiceDao,
            Runnable afterClose) {
        if (delivery == null || delivery.getRowId() == null) {
            return;
        }
        SalesDelivery fresh = deliveryDao.firstOrDefault(delivery.getRowId());
        if (fresh == null || !fresh.canInvoice()) {
            SatisDates.showError("Satış irsaliyesi", "Çevrilemez",
                    "Yalnız onaylı veya bakiyesi kalan irsaliyeler faturaya alınır.");
            return;
        }
        ICommonDao<SalesInvoiceLine> invoiceLineDao = TerpApplication.getInstance()
                .getPersistence().createDao(SalesInvoiceLine.class);
        if (!SalesDocs.hasInvoiceableBalance(fresh, lineDao, invoiceDao, invoiceLineDao)) {
            SatisDates.showError("Satış irsaliyesi", "Bakiye yok",
                    "Bu irsaliyede faturalanacak miktar yok.");
            return;
        }
        List<SalesDeliveryLine> lines = SalesDocs.remainingInvoiceLines(fresh.getRowId(), lineDao,
                invoiceDao, invoiceLineDao, null);
        openInvoiceEditor(null, fresh, lines, afterClose);
    }

    static void openQuoteEditor(SalesQuote current, Runnable afterClose) {
        try {
            FXMLLoader loader = SatisDates.pluginLoader(SatisWindows.class, "/fxml/QuoteEditForm.fxml");
            Node node = loader.load();
            QuoteEditFormController controller = loader.getController();
            controller.initializeForm(current);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(new Scene((Parent) node));
            stage.setTitle(current == null ? "Yeni satış teklifi" : "Satış teklifi");
            stage.showAndWait();
            if (afterClose != null) {
                afterClose.run();
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    static void openOrderEditor(SalesOrder current, Runnable afterClose) {
        openOrderEditor(current, null, null, afterClose);
    }

    private static void openOrderEditor(SalesOrder current, SalesQuote fromQuote,
            List<SalesQuoteLine> quoteLines, Runnable afterClose) {
        try {
            FXMLLoader loader = SatisDates.pluginLoader(SatisWindows.class, "/fxml/OrderEditForm.fxml");
            Node node = loader.load();
            OrderEditFormController controller = loader.getController();
            if (fromQuote != null) {
                controller.initializeFromQuote(fromQuote, quoteLines);
            } else {
                controller.initializeForm(current);
            }
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(new Scene((Parent) node));
            stage.setTitle(fromQuote != null ? "Tekliften sipariş"
                    : (current == null ? "Yeni satış siparişi" : "Satış siparişi"));
            stage.showAndWait();
            if (afterClose != null) {
                afterClose.run();
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    static void openDeliveryEditor(SalesDelivery current, Runnable afterClose) {
        openDeliveryEditor(current, null, null, afterClose);
    }

    static void openDeliveryFromOrder(Runnable afterClose) {
        openDeliveryEditor(null, null, null, afterClose, "Siparişten irsaliye");
    }

    private static void openDeliveryEditor(SalesDelivery current, SalesOrder fromOrder,
            List<SalesOrderLine> orderLines, Runnable afterClose) {
        String title = fromOrder != null ? "Siparişten irsaliye"
                : (current == null ? "Yeni satış irsaliyesi" : "Satış irsaliyesi");
        openDeliveryEditor(current, fromOrder, orderLines, afterClose, title);
    }

    private static void openDeliveryEditor(SalesDelivery current, SalesOrder fromOrder,
            List<SalesOrderLine> orderLines, Runnable afterClose, String title) {
        try {
            FXMLLoader loader = SatisDates.pluginLoader(SatisWindows.class, "/fxml/DeliveryEditForm.fxml");
            Node node = loader.load();
            DeliveryEditFormController controller = loader.getController();
            if (fromOrder != null) {
                controller.initializeFromOrder(fromOrder, orderLines);
            } else {
                controller.initializeForm(current);
            }
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(new Scene((Parent) node));
            stage.setTitle(title);
            stage.showAndWait();
            if (afterClose != null) {
                afterClose.run();
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    static void openInvoiceEditor(SalesInvoice current, Runnable afterClose) {
        openInvoiceEditor(current, null, null, afterClose);
    }

    static void openInvoiceFromDelivery(Runnable afterClose) {
        openInvoiceEditor(null, null, null, afterClose, "İrsaliyeden fatura");
    }

    private static void openInvoiceEditor(SalesInvoice current, SalesDelivery fromDelivery,
            List<SalesDeliveryLine> deliveryLines, Runnable afterClose) {
        String title = fromDelivery != null ? "İrsaliyeden fatura"
                : (current == null ? "Yeni satış faturası" : "Satış faturası");
        openInvoiceEditor(current, fromDelivery, deliveryLines, afterClose, title);
    }

    private static void openInvoiceEditor(SalesInvoice current, SalesDelivery fromDelivery,
            List<SalesDeliveryLine> deliveryLines, Runnable afterClose, String title) {
        try {
            FXMLLoader loader = SatisDates.pluginLoader(SatisWindows.class, "/fxml/InvoiceEditForm.fxml");
            Node node = loader.load();
            InvoiceEditFormController controller = loader.getController();
            if (fromDelivery != null) {
                controller.initializeFromDelivery(fromDelivery, deliveryLines);
            } else {
                controller.initializeForm(current);
            }
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(new Scene((Parent) node));
            stage.setTitle(title);
            stage.showAndWait();
            if (afterClose != null) {
                afterClose.run();
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}

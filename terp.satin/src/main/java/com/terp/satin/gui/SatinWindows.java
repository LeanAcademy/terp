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
package com.terp.satin.gui;

import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.satin.data.PurchaseDocs;
import com.terp.satin.data.PurchaseOrder;
import com.terp.satin.data.PurchaseOrderLine;
import com.terp.satin.data.PurchaseReceipt;
import com.terp.satin.data.PurchaseReceiptLine;
import com.terp.satin.data.PurchaseRequest;
import com.terp.satin.data.PurchaseRequestLine;
import com.terp.satin.data.PurchaseSettings;
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

final class SatinWindows {

    private static final Logger LOG = Logger.getLogger(SatinWindows.class.getName());

    private SatinWindows() {
    }

    static void convertRequest(PurchaseRequest request, ICommonDao<PurchaseRequest> requestDao,
            ICommonDao<PurchaseRequestLine> lineDao, ICommonDao<PurchaseOrder> orderDao,
            Runnable afterClose) {
        if (request == null || request.getRowId() == null) {
            return;
        }
        PurchaseRequest fresh = requestDao.firstOrDefault(request.getRowId());
        if (fresh == null || !fresh.canConvert()) {
            SatinDates.showError("Satınalma talebi", "Çevrilemez",
                    "Yalnız onaylı talepler siparişe çevrilir.");
            return;
        }
        PurchaseOrder existing = PurchaseDocs.findOpenOrder(orderDao, fresh.getRowId());
        if (existing != null) {
            SatinDates.showError("Satınalma talebi", "Sipariş var",
                    "Bu talep için açık sipariş zaten var: " + existing.getDocumentNo());
            return;
        }
        List<PurchaseRequestLine> lines = lineDao.findAll(
                "from PurchaseRequestLine e where e.requestId = " + fresh.getRowId()
                        + " order by e.lineNo");
        if (lines == null || lines.isEmpty()) {
            SatinDates.showError("Satınalma talebi", "Satır yok", "Talebin malzeme satırı yok.");
            return;
        }
        openOrderEditor(null, fresh, lines, afterClose);
    }

    static void convertOrder(PurchaseOrder order, ICommonDao<PurchaseOrder> orderDao,
            ICommonDao<PurchaseOrderLine> lineDao, ICommonDao<PurchaseReceipt> receiptDao,
            Runnable afterClose) {
        if (order == null || order.getRowId() == null) {
            return;
        }
        PurchaseOrder fresh = orderDao.firstOrDefault(order.getRowId());
        if (fresh == null || !fresh.canReceive()) {
            SatinDates.showError("Satınalma siparişi", "Çevrilemez",
                    "Yalnız onaylı veya bakiyesi kalan siparişler mal kabule alınır.");
            return;
        }
        ICommonDao<PurchaseReceiptLine> receiptLineDao = TerpApplication.getInstance()
                .getPersistence().createDao(PurchaseReceiptLine.class);
        ICommonDao<PurchaseSettings> settingsDao = TerpApplication.getInstance()
                .getPersistence().createDao(PurchaseSettings.class);
        if (!PurchaseDocs.hasReceivableBalance(fresh, lineDao, receiptDao, receiptLineDao, settingsDao)) {
            SatinDates.showError("Satınalma siparişi", "Bakiye yok",
                    "Bu siparişte kalan (fazla kabul dahil) miktar yok.");
            return;
        }
        List<PurchaseOrderLine> lines = PurchaseDocs.remainingLines(fresh.getRowId(), lineDao,
                receiptDao, receiptLineDao, settingsDao, null);
        openReceiptEditor(null, fresh, lines, afterClose);
    }

    static void openRequestEditor(PurchaseRequest current, Runnable afterClose) {
        try {
            FXMLLoader loader = SatinDates.pluginLoader(SatinWindows.class, "/fxml/RequestEditForm.fxml");
            Node node = loader.load();
            RequestEditFormController controller = loader.getController();
            controller.initializeForm(current);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(new Scene((Parent) node));
            stage.setTitle(current == null ? "Yeni satınalma talebi" : "Satınalma talebi");
            stage.showAndWait();
            if (afterClose != null) {
                afterClose.run();
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    static void openOrderEditor(PurchaseOrder current, Runnable afterClose) {
        openOrderEditor(current, null, null, afterClose);
    }

    static void openReceiptEditor(PurchaseReceipt current, Runnable afterClose) {
        openReceiptEditor(current, null, null, afterClose);
    }

    static void openReceiptFromOrder(Runnable afterClose) {
        openReceiptEditor(null, null, null, afterClose, "Siparişten mal kabul");
    }

    private static void openOrderEditor(PurchaseOrder current, PurchaseRequest fromRequest,
            List<PurchaseRequestLine> requestLines, Runnable afterClose) {
        try {
            FXMLLoader loader = SatinDates.pluginLoader(SatinWindows.class, "/fxml/OrderEditForm.fxml");
            Node node = loader.load();
            OrderEditFormController controller = loader.getController();
            if (fromRequest != null) {
                controller.initializeFromRequest(fromRequest, requestLines);
            } else {
                controller.initializeForm(current);
            }
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(new Scene((Parent) node));
            stage.setTitle(fromRequest != null ? "Talepten sipariş"
                    : (current == null ? "Yeni satınalma siparişi" : "Satınalma siparişi"));
            stage.showAndWait();
            if (afterClose != null) {
                afterClose.run();
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private static void openReceiptEditor(PurchaseReceipt current, PurchaseOrder fromOrder,
            List<PurchaseOrderLine> orderLines, Runnable afterClose) {
        String title = fromOrder != null ? "Siparişten mal kabul"
                : (current == null ? "Yeni mal kabul" : "Mal kabul");
        openReceiptEditor(current, fromOrder, orderLines, afterClose, title);
    }

    private static void openReceiptEditor(PurchaseReceipt current, PurchaseOrder fromOrder,
            List<PurchaseOrderLine> orderLines, Runnable afterClose, String title) {
        try {
            FXMLLoader loader = SatinDates.pluginLoader(SatinWindows.class, "/fxml/ReceiptEditForm.fxml");
            Node node = loader.load();
            ReceiptEditFormController controller = loader.getController();
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
}

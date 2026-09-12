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
import com.terp.plugin.gui.IIconFactory;
import com.terp.satin.data.PurchaseDocs;
import com.terp.satin.data.PurchaseOrder;
import com.terp.satin.data.PurchaseOrderLine;
import com.terp.satin.data.PurchaseOrderStatusRow;
import com.terp.satin.data.PurchaseReceipt;
import com.terp.satin.data.PurchaseReceiptLine;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class OrderStatusFormController implements Initializable {

    @FXML
    private Button btnRefresh;
    @FXML
    private TextField txtDocumentNo;
    @FXML
    private TextField txtItemCode;
    @FXML
    private TextField txtAccountCode;
    @FXML
    private CheckBox chkOpenOnly;
    @FXML
    private TableView<PurchaseOrderStatusRow> tblvStatusView;
    @FXML
    private TableColumn<PurchaseOrderStatusRow, String> tcDocumentNo;
    @FXML
    private TableColumn<PurchaseOrderStatusRow, String> tcDateLabel;
    @FXML
    private TableColumn<PurchaseOrderStatusRow, String> tcAccountName;
    @FXML
    private TableColumn<PurchaseOrderStatusRow, String> tcItemCode;
    @FXML
    private TableColumn<PurchaseOrderStatusRow, String> tcItemDesc;
    @FXML
    private TableColumn<PurchaseOrderStatusRow, String> tcItemUnit;
    @FXML
    private TableColumn<PurchaseOrderStatusRow, Double> tcOrderedQty;
    @FXML
    private TableColumn<PurchaseOrderStatusRow, Double> tcDeliveredQty;
    @FXML
    private TableColumn<PurchaseOrderStatusRow, Double> tcRemainingQty;
    @FXML
    private TableColumn<PurchaseOrderStatusRow, String> tcBalanceLabel;
    @FXML
    private TableColumn<PurchaseOrderStatusRow, String> tcDocumentStatusLabel;

    private ICommonDao<PurchaseOrder> orderDao;
    private ICommonDao<PurchaseOrderLine> orderLineDao;
    private ICommonDao<PurchaseReceipt> receiptDao;
    private ICommonDao<PurchaseReceiptLine> receiptLineDao;

    @FXML
    public void onActionBtnRefresh(ActionEvent event) {
        refreshView();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.orderDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseOrder.class);
        this.orderLineDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseOrderLine.class);
        this.receiptDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseReceipt.class);
        this.receiptLineDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseReceiptLine.class);
        IIconFactory icons = TerpApplication.getInstance().getIconFactory();
        if (icons != null) {
            try {
                this.btnRefresh.setGraphic(icons.getIcon("REFRESH"));
            } catch (RuntimeException ignored) {
                // keep text-only if the glyph name is unknown
            }
        }
        this.tcDocumentNo.setCellValueFactory(new PropertyValueFactory<>("documentNo"));
        this.tcDateLabel.setCellValueFactory(new PropertyValueFactory<>("dateLabel"));
        this.tcAccountName.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        this.tcItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcOrderedQty.setCellValueFactory(new PropertyValueFactory<>("orderedQty"));
        this.tcDeliveredQty.setCellValueFactory(new PropertyValueFactory<>("deliveredQty"));
        this.tcRemainingQty.setCellValueFactory(new PropertyValueFactory<>("remainingQty"));
        this.tcBalanceLabel.setCellValueFactory(new PropertyValueFactory<>("balanceLabel"));
        this.tcDocumentStatusLabel.setCellValueFactory(new PropertyValueFactory<>("documentStatusLabel"));
        refreshView();
    }

    private void refreshView() {
        List<PurchaseOrderStatusRow> rows = PurchaseDocs.orderStatus(
                orderDao, orderLineDao, receiptDao, receiptLineDao);
        String orderFilter = normalize(txtDocumentNo);
        String itemFilter = normalize(txtItemCode);
        String accountFilter = normalize(txtAccountCode);
        boolean openOnly = chkOpenOnly != null && chkOpenOnly.isSelected();
        List<PurchaseOrderStatusRow> filtered = new ArrayList<>();
        for (PurchaseOrderStatusRow row : rows) {
            if (row == null) {
                continue;
            }
            if (openOnly && (row.getRemainingQty() == null || row.getRemainingQty() <= 0.0000001d)) {
                continue;
            }
            if (!orderFilter.isEmpty() && !contains(row.getDocumentNo(), orderFilter)) {
                continue;
            }
            if (!itemFilter.isEmpty()
                    && !contains(row.getItemCode(), itemFilter)
                    && !contains(row.getItemDesc(), itemFilter)) {
                continue;
            }
            if (!accountFilter.isEmpty()
                    && !contains(row.getAccountCode(), accountFilter)
                    && !contains(row.getAccountName(), accountFilter)) {
                continue;
            }
            filtered.add(row);
        }
        tblvStatusView.setItems(FXCollections.observableArrayList(filtered));
    }

    private static String normalize(TextField field) {
        if (field == null || field.getText() == null) {
            return "";
        }
        return field.getText().trim().toLowerCase();
    }

    private static boolean contains(String value, String filter) {
        return value != null && value.toLowerCase().contains(filter);
    }
}

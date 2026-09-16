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
import com.terp.plugin.gui.IIconFactory;
import com.terp.satis.data.SalesDelivery;
import com.terp.satis.data.SalesDeliveryLine;
import com.terp.satis.data.SalesDocs;
import com.terp.satis.data.SalesOrder;
import com.terp.satis.data.SalesOrderLine;
import com.terp.satis.data.SalesOrderStatusRow;
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
    private TableView<SalesOrderStatusRow> tblvStatusView;
    @FXML
    private TableColumn<SalesOrderStatusRow, String> tcDocumentNo;
    @FXML
    private TableColumn<SalesOrderStatusRow, String> tcDateLabel;
    @FXML
    private TableColumn<SalesOrderStatusRow, String> tcAccountName;
    @FXML
    private TableColumn<SalesOrderStatusRow, String> tcItemCode;
    @FXML
    private TableColumn<SalesOrderStatusRow, String> tcItemDesc;
    @FXML
    private TableColumn<SalesOrderStatusRow, String> tcItemUnit;
    @FXML
    private TableColumn<SalesOrderStatusRow, Double> tcOrderedQty;
    @FXML
    private TableColumn<SalesOrderStatusRow, Double> tcDeliveredQty;
    @FXML
    private TableColumn<SalesOrderStatusRow, Double> tcRemainingQty;
    @FXML
    private TableColumn<SalesOrderStatusRow, String> tcBalanceLabel;
    @FXML
    private TableColumn<SalesOrderStatusRow, String> tcDocumentStatusLabel;

    private ICommonDao<SalesOrder> orderDao;
    private ICommonDao<SalesOrderLine> orderLineDao;
    private ICommonDao<SalesDelivery> deliveryDao;
    private ICommonDao<SalesDeliveryLine> deliveryLineDao;

    @FXML
    public void onActionBtnRefresh(ActionEvent event) {
        refreshView();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.orderDao = TerpApplication.getInstance().getPersistence().createDao(SalesOrder.class);
        this.orderLineDao = TerpApplication.getInstance().getPersistence().createDao(SalesOrderLine.class);
        this.deliveryDao = TerpApplication.getInstance().getPersistence().createDao(SalesDelivery.class);
        this.deliveryLineDao = TerpApplication.getInstance().getPersistence().createDao(SalesDeliveryLine.class);
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
        List<SalesOrderStatusRow> rows = SalesDocs.orderStatus(
                orderDao, orderLineDao, deliveryDao, deliveryLineDao);
        String orderFilter = normalize(txtDocumentNo);
        String itemFilter = normalize(txtItemCode);
        String accountFilter = normalize(txtAccountCode);
        boolean openOnly = chkOpenOnly != null && chkOpenOnly.isSelected();
        List<SalesOrderStatusRow> filtered = new ArrayList<>();
        for (SalesOrderStatusRow row : rows) {
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

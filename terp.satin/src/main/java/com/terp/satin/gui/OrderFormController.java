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

import com.terp.plugin.CompanyScope;
import com.terp.plugin.FormRights;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.gui.IIconFactory;
import com.terp.satin.data.PurchaseDocs;
import com.terp.satin.data.PurchaseOrder;
import com.terp.satin.data.PurchaseOrderLine;
import com.terp.satin.data.PurchaseReceipt;
import com.terp.satin.data.PurchaseReceiptLine;
import com.terp.satin.data.PurchaseRequest;
import com.terp.satin.data.PurchaseSettings;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class OrderFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(OrderFormController.class.getName());
    private static final int DEFAULT_ROWS_PER_PAGE = 20;
    private static final String ALL_STATUS = "Tümü";

    @FXML
    private Button btnSearch;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnReceive;
    @FXML
    private Button btnDelete;
    @FXML
    private TextField txtDocumentNo;
    @FXML
    private TextField txtAccountCode;
    @FXML
    private ComboBox<String> cmbSearchStatus;
    @FXML
    private TableView<PurchaseOrder> tblvOrderView;
    @FXML
    private Pagination pgnOrderData;
    @FXML
    private TableColumn<PurchaseOrder, String> tcDateLabel;
    @FXML
    private TableColumn<PurchaseOrder, String> tcDocumentNo;
    @FXML
    private TableColumn<PurchaseOrder, String> tcAccountCode;
    @FXML
    private TableColumn<PurchaseOrder, String> tcAccountName;
    @FXML
    private TableColumn<PurchaseOrder, String> tcWarehouseCode;
    @FXML
    private TableColumn<PurchaseOrder, String> tcSourceRequestNo;
    @FXML
    private TableColumn<PurchaseOrder, String> tcStatusLabel;

    private ICommonDao<PurchaseOrder> orderDao;
    private ICommonDao<PurchaseOrderLine> lineDao;
    private ICommonDao<PurchaseRequest> requestDao;
    private ICommonDao<PurchaseReceipt> receiptDao;
    private ICommonDao<PurchaseReceiptLine> receiptLineDao;
    private ICommonDao<PurchaseSettings> settingsDao;
    private FormRights rights = FormRights.forMenu("SAT04");
    private FormRights receiptRights = FormRights.forMenu("SAT02");
    private FormRights fromOrderRights = FormRights.forMenu("SAT05");
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from PurchaseOrder e");
        CompanyScope.append(sql, false);
        SatinDates.appendLike(sql, true, "e.documentNo", txtDocumentNo.getText());
        SatinDates.appendLike(sql, true, "e.accountCode", txtAccountCode.getText());
        String status = cmbSearchStatus.getValue();
        if ("Taslak".equals(status)) {
            sql.append(" and e.status = ").append(PurchaseOrder.STATUS_DRAFT);
        } else if ("Onaylı".equals(status)) {
            sql.append(" and e.status = ").append(PurchaseOrder.STATUS_APPROVED);
        } else if ("Kapalı".equals(status)) {
            sql.append(" and e.status = ").append(PurchaseOrder.STATUS_RECEIVED);
        } else if ("İptal".equals(status)) {
            sql.append(" and e.status = ").append(PurchaseOrder.STATUS_CANCELLED);
        }
        this.searchSqlStatement = sql.toString();
        this.currentPageNum = 1;
        refreshView();
    }

    @FXML
    public void onActionBtnAdd(ActionEvent event) {
        if (!CompanyScope.requireCompany()) {
            return;
        }
        SatinWindows.openOrderEditor(null, this::refreshView);
    }

    @FXML
    public void onActionBtnEdit(ActionEvent event) {
        PurchaseOrder selected = tblvOrderView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            SatinWindows.openOrderEditor(selected, this::refreshView);
        }
    }

    @FXML
    public void onActionBtnReceive(ActionEvent event) {
        PurchaseOrder selected = tblvOrderView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        SatinWindows.convertOrder(selected, orderDao, lineDao, receiptDao, this::refreshView);
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        PurchaseOrder selected = tblvOrderView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (PurchaseDocs.hasPostedReceipt(receiptDao, receiptLineDao, lineDao, selected.getRowId())) {
            SatinDates.showError("Satınalma siparişi", "Mal kabule bağlı",
                    "Önce bağlı mal kabul belgesini iptal veya silin.");
            return;
        }
        String message = selected.isApproved()
                ? "Onaylı sipariş iptal edilecek: " + selected.getDocumentNo()
                : "Seçilen sipariş silinecek: " + selected.getDocumentNo();
        Alert alert = new Alert(AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }
            try {
                Long requestId = selected.getSourceRequestId();
                if (selected.isApproved()) {
                    selected.setStatus(PurchaseOrder.STATUS_CANCELLED);
                    selected.setLastUpdateDate(new Date());
                    orderDao.addOrUpdate(selected);
                } else {
                    PurchaseDocs.deleteByParent(lineDao, "PurchaseOrderLine", "orderId",
                            selected.getRowId());
                    orderDao.delete(selected.getRowId());
                }
                PurchaseDocs.syncRequestStatus(requestDao, orderDao, requestId);
                refreshView();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.orderDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseOrder.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseOrderLine.class);
        this.requestDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseRequest.class);
        this.receiptDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseReceipt.class);
        this.receiptLineDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseReceiptLine.class);
        this.settingsDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseSettings.class);
        IIconFactory icons = TerpApplication.getInstance().getIconFactory();
        if (icons != null) {
            this.btnSearch.setGraphic(icons.getIcon("SEARCH"));
            this.btnAdd.setGraphic(icons.getIcon("PLUS"));
            this.btnEdit.setGraphic(icons.getIcon("EDIT"));
            this.btnDelete.setGraphic(icons.getIcon("SEARCH"));
        }
        cmbSearchStatus.getItems().addAll(ALL_STATUS, "Taslak", "Onaylı", "Kapalı", "İptal");
        cmbSearchStatus.getSelectionModel().select(ALL_STATUS);
        this.tcDateLabel.setCellValueFactory(new PropertyValueFactory<>("dateLabel"));
        this.tcDocumentNo.setCellValueFactory(new PropertyValueFactory<>("documentNo"));
        this.tcAccountCode.setCellValueFactory(new PropertyValueFactory<>("accountCode"));
        this.tcAccountName.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        this.tcWarehouseCode.setCellValueFactory(new PropertyValueFactory<>("warehouseCode"));
        this.tcSourceRequestNo.setCellValueFactory(new PropertyValueFactory<>("sourceRequestNo"));
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        this.pgnOrderData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvOrderView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        this.searchSqlStatement = CompanyScope.from("PurchaseOrder");
        this.btnAdd.setDisable(!rights.add);
        updateButtons(true, true, true);
        refreshView();
    }

    private void refreshView() {
        String hql = scopedQuery();
        long count = orderDao.getRecordCount(hql);
        int pages = (int) (count / rowsPerPage + 1);
        this.pgnOrderData.setPageCount(Math.max(1, pages));
        this.tblvOrderView.setItems(currentPage());
    }

    private ObservableList<PurchaseOrder> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<PurchaseOrder> rows = orderDao.findPage(pageNum, rowsPerPage, scopedQuery());
        return FXCollections.observableArrayList(rows == null ? List.of() : rows);
    }

    private String scopedQuery() {
        if (searchSqlStatement == null || searchSqlStatement.isBlank()) {
            return CompanyScope.from("PurchaseOrder");
        }
        return searchSqlStatement;
    }

    private void selectionChanged(Change<? extends PurchaseOrder> change) {
        int size = change.getList().size();
        if (size == 0) {
            updateButtons(true, true, true);
            return;
        }
        PurchaseOrder selected = change.getList().get(0);
        boolean receiveDisabled = size != 1 || selected == null
                || !PurchaseDocs.hasReceivableBalance(selected, lineDao, receiptDao, receiptLineDao, settingsDao)
                || (!receiptRights.add && !fromOrderRights.add);
        updateButtons(size != 1, size == 0, receiveDisabled);
    }

    private void updateButtons(boolean editDisabled, boolean deleteDisabled, boolean receiveDisabled) {
        this.btnEdit.setDisable(editDisabled || !rights.edit);
        this.btnDelete.setDisable(deleteDisabled || !rights.delete);
        this.btnReceive.setDisable(receiveDisabled);
    }
}

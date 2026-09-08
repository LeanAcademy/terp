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
import com.terp.plugin.data.IStockLedger;
import com.terp.plugin.data.StockPosting;
import com.terp.plugin.gui.IIconFactory;
import com.terp.satin.data.PurchaseDocs;
import com.terp.satin.data.PurchaseOrder;
import com.terp.satin.data.PurchaseOrderLine;
import com.terp.satin.data.PurchaseReceipt;
import com.terp.satin.data.PurchaseReceiptLine;
import java.net.URL;
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

public class ReceiptFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ReceiptFormController.class.getName());
    private static final int DEFAULT_ROWS_PER_PAGE = 20;
    private static final String ALL_STATUS = "Tümü";

    @FXML
    private Button btnSearch;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;
    @FXML
    private TextField txtDocumentNo;
    @FXML
    private TextField txtAccountCode;
    @FXML
    private ComboBox<String> cmbSearchStatus;
    @FXML
    private TableView<PurchaseReceipt> tblvReceiptView;
    @FXML
    private Pagination pgnReceiptData;
    @FXML
    private TableColumn<PurchaseReceipt, String> tcDateLabel;
    @FXML
    private TableColumn<PurchaseReceipt, String> tcDocumentNo;
    @FXML
    private TableColumn<PurchaseReceipt, String> tcAccountCode;
    @FXML
    private TableColumn<PurchaseReceipt, String> tcAccountName;
    @FXML
    private TableColumn<PurchaseReceipt, String> tcWarehouseCode;
    @FXML
    private TableColumn<PurchaseReceipt, String> tcSourceOrderNo;
    @FXML
    private TableColumn<PurchaseReceipt, String> tcStatusLabel;

    private ICommonDao<PurchaseReceipt> receiptDao;
    private ICommonDao<PurchaseReceiptLine> lineDao;
    private ICommonDao<PurchaseOrder> orderDao;
    private ICommonDao<PurchaseOrderLine> orderLineDao;
    private FormRights rights = FormRights.forMenu("SAT02");
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from PurchaseReceipt e");
        boolean whereAdded = CompanyScope.append(sql, false);
        whereAdded = appendLike(sql, whereAdded, "e.documentNo", txtDocumentNo.getText());
        whereAdded = appendLike(sql, whereAdded, "e.accountCode", txtAccountCode.getText());
        String status = cmbSearchStatus.getValue();
        if ("Taslak".equals(status)) {
            sql.append(" and e.status = ").append(PurchaseReceipt.STATUS_DRAFT);
        } else if ("Onaylı".equals(status)) {
            sql.append(" and e.status = ").append(PurchaseReceipt.STATUS_POSTED);
        } else if ("İptal".equals(status)) {
            sql.append(" and e.status = ").append(PurchaseReceipt.STATUS_CANCELLED);
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
        SatinWindows.openReceiptEditor(null, this::refreshView);
    }

    @FXML
    public void onActionBtnEdit(ActionEvent event) {
        PurchaseReceipt selected = tblvReceiptView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            SatinWindows.openReceiptEditor(selected, this::refreshView);
        }
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        PurchaseReceipt selected = tblvReceiptView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        String message = selected.isPosted()
                ? "Onaylı belge iptal edilecek ve stok fişleri geri alınacak: " + selected.getDocumentNo()
                : "Seçilen belge silinecek: " + selected.getDocumentNo();
        Alert alert = new Alert(AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }
            try {
                Long orderId = selected.getSourceOrderId();
                List<PurchaseReceiptLine> stored = selected.getRowId() == null ? List.of()
                        : lineDao.findAll("from PurchaseReceiptLine e where e.receiptId = "
                                + selected.getRowId());
                if (selected.isPosted()) {
                    IStockLedger ledger = TerpApplication.getInstance().getStockLedger();
                    if (ledger == null) {
                        showError("Stok eklentisi yok", "Mal kabul iptali için terp.stok yüklü olmalıdır.");
                        return;
                    }
                    ledger.reverseByDocument(StockPosting.SOURCE_PURCHASE_RECEIPT, selected.getRowId());
                    selected.setStatus(PurchaseReceipt.STATUS_CANCELLED);
                    receiptDao.addOrUpdate(selected);
                } else {
                    deleteLines(selected.getRowId());
                    receiptDao.delete(selected.getRowId());
                }
                PurchaseDocs.syncOrdersForLines(orderDao, orderLineDao, receiptDao, lineDao, orderId, stored);
                refreshView();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.receiptDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseReceipt.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseReceiptLine.class);
        this.orderDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseOrder.class);
        this.orderLineDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseOrderLine.class);
        IIconFactory icons = TerpApplication.getInstance().getIconFactory();
        if (icons != null) {
            this.btnSearch.setGraphic(icons.getIcon("SEARCH"));
            this.btnAdd.setGraphic(icons.getIcon("PLUS"));
            this.btnEdit.setGraphic(icons.getIcon("EDIT"));
            this.btnDelete.setGraphic(icons.getIcon("SEARCH"));
        }
        cmbSearchStatus.getItems().addAll(ALL_STATUS, "Taslak", "Onaylı", "İptal");
        cmbSearchStatus.getSelectionModel().select(ALL_STATUS);
        this.tcDateLabel.setCellValueFactory(new PropertyValueFactory<>("dateLabel"));
        this.tcDocumentNo.setCellValueFactory(new PropertyValueFactory<>("documentNo"));
        this.tcAccountCode.setCellValueFactory(new PropertyValueFactory<>("accountCode"));
        this.tcAccountName.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        this.tcWarehouseCode.setCellValueFactory(new PropertyValueFactory<>("warehouseCode"));
        this.tcSourceOrderNo.setCellValueFactory(new PropertyValueFactory<>("sourceOrderNo"));
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        this.pgnReceiptData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvReceiptView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        this.searchSqlStatement = CompanyScope.from("PurchaseReceipt");
        this.btnAdd.setDisable(!rights.add);
        updateButtons(true, true);
        refreshView();
    }

    private void deleteLines(Long receiptId) {
        if (receiptId == null) {
            return;
        }
        List<PurchaseReceiptLine> lines = lineDao.findAll(
                "from PurchaseReceiptLine e where e.receiptId = " + receiptId);
        if (lines == null) {
            return;
        }
        for (PurchaseReceiptLine line : lines) {
            if (line != null && line.getRowId() != null) {
                lineDao.delete(line.getRowId());
            }
        }
    }

    private void refreshView() {
        String hql = scopedQuery();
        long count = receiptDao.getRecordCount(hql);
        int pages = (int) (count / rowsPerPage + 1);
        this.pgnReceiptData.setPageCount(Math.max(1, pages));
        this.tblvReceiptView.setItems(currentPage());
    }

    private ObservableList<PurchaseReceipt> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<PurchaseReceipt> rows = receiptDao.findPage(pageNum, rowsPerPage, scopedQuery());
        return FXCollections.observableArrayList(rows == null ? List.of() : rows);
    }

    private String scopedQuery() {
        if (searchSqlStatement == null || searchSqlStatement.isBlank()) {
            return CompanyScope.from("PurchaseReceipt");
        }
        return searchSqlStatement;
    }

    private void selectionChanged(Change<? extends PurchaseReceipt> change) {
        int size = change.getList().size();
        if (size == 0) {
            updateButtons(true, true);
        } else if (size == 1) {
            updateButtons(false, false);
        } else {
            updateButtons(true, false);
        }
    }

    private void updateButtons(boolean editDisabled, boolean deleteDisabled) {
        this.btnEdit.setDisable(editDisabled || !rights.edit);
        this.btnDelete.setDisable(deleteDisabled || !rights.delete);
    }

    private static void showError(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Mal kabul");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();
    }

    private static boolean appendLike(StringBuilder sql, boolean whereAdded, String field, String value) {
        if (value == null || value.isBlank()) {
            return whereAdded;
        }
        if (!whereAdded) {
            sql.append(" where");
        } else {
            sql.append(" and");
        }
        sql.append(' ').append(field).append(" like '")
                .append(value.replace("'", "''")).append('\'');
        return true;
    }
}

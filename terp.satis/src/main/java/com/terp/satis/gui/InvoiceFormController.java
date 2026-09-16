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

import com.terp.plugin.CompanyScope;
import com.terp.plugin.FormRights;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.IAccountLedger;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.AccountPosting;
import com.terp.plugin.gui.IIconFactory;
import com.terp.satis.data.SalesDelivery;
import com.terp.satis.data.SalesDeliveryLine;
import com.terp.satis.data.SalesDocs;
import com.terp.satis.data.SalesInvoice;
import com.terp.satis.data.SalesInvoiceLine;
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
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class InvoiceFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(InvoiceFormController.class.getName());
    private static final int DEFAULT_ROWS_PER_PAGE = 20;
    private static final String ALL_STATUS = "Tümü";

    @FXML
    private Button btnSearch;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnFromDelivery;
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
    private TableView<SalesInvoice> tblvInvoiceView;
    @FXML
    private Pagination pgnInvoiceData;
    @FXML
    private TableColumn<SalesInvoice, String> tcDateLabel;
    @FXML
    private TableColumn<SalesInvoice, String> tcDocumentNo;
    @FXML
    private TableColumn<SalesInvoice, String> tcAccountCode;
    @FXML
    private TableColumn<SalesInvoice, String> tcAccountName;
    @FXML
    private TableColumn<SalesInvoice, String> tcWarehouseCode;
    @FXML
    private TableColumn<SalesInvoice, String> tcSourceDeliveryNo;
    @FXML
    private TableColumn<SalesInvoice, String> tcStatusLabel;
    @FXML
    private TableView<SalesInvoiceLine> tblvLineView;
    @FXML
    private TableColumn<SalesInvoiceLine, Integer> tcLineNo;
    @FXML
    private TableColumn<SalesInvoiceLine, String> tcLineSourceDeliveryNo;
    @FXML
    private TableColumn<SalesInvoiceLine, String> tcLineItemCode;
    @FXML
    private TableColumn<SalesInvoiceLine, String> tcLineItemDesc;
    @FXML
    private TableColumn<SalesInvoiceLine, String> tcLineItemUnit;
    @FXML
    private TableColumn<SalesInvoiceLine, Double> tcLineQuantity;
    @FXML
    private TableColumn<SalesInvoiceLine, Double> tcLineUnitPrice;

    private ICommonDao<SalesInvoice> invoiceDao;
    private ICommonDao<SalesInvoiceLine> lineDao;
    private ICommonDao<SalesDelivery> deliveryDao;
    private ICommonDao<SalesDeliveryLine> deliveryLineDao;
    private FormRights rights = FormRights.forMenu("STS05");
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";
    private boolean syncingSelection;

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from SalesInvoice e");
        boolean whereAdded = CompanyScope.append(sql, false);
        whereAdded = SatisDates.appendLike(sql, whereAdded, "e.documentNo", txtDocumentNo.getText());
        whereAdded = SatisDates.appendLike(sql, whereAdded, "e.accountCode", txtAccountCode.getText());
        String status = cmbSearchStatus.getValue();
        if ("Taslak".equals(status)) {
            sql.append(" and e.status = ").append(SalesInvoice.STATUS_DRAFT);
        } else if ("Onaylı".equals(status)) {
            sql.append(" and e.status = ").append(SalesInvoice.STATUS_POSTED);
        } else if ("İptal".equals(status)) {
            sql.append(" and e.status = ").append(SalesInvoice.STATUS_CANCELLED);
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
        SatisWindows.openInvoiceEditor(null, this::refreshView);
    }

    @FXML
    public void onActionBtnFromDelivery(ActionEvent event) {
        if (!CompanyScope.requireCompany()) {
            return;
        }
        SatisWindows.openInvoiceFromDelivery(this::refreshView);
    }

    @FXML
    public void onActionBtnEdit(ActionEvent event) {
        SalesInvoice selected = tblvInvoiceView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            SatisWindows.openInvoiceEditor(selected, this::refreshView);
        }
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        SalesInvoice selected = tblvInvoiceView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        String message = selected.isPosted()
                ? "Onaylı belge iptal edilecek ve cari fişi geri alınacak: " + selected.getDocumentNo()
                : "Seçilen belge silinecek: " + selected.getDocumentNo();
        Alert alert = new Alert(AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }
            try {
                Long deliveryId = selected.getSourceDeliveryId();
                List<SalesInvoiceLine> stored = selected.getRowId() == null ? List.of()
                        : lineDao.findAll("from SalesInvoiceLine e where e.invoiceId = "
                                + selected.getRowId());
                if (selected.isPosted()) {
                    IAccountLedger ledger = TerpApplication.getInstance().getAccountLedger();
                    if (ledger == null) {
                        SatisDates.showError("Satış faturası", "Cari eklentisi yok",
                                "İptal için terp.cari yüklü olmalıdır.");
                        return;
                    }
                    ledger.reverseByDocument(AccountPosting.SOURCE_SALES_INVOICE, selected.getRowId());
                    selected.setStatus(SalesInvoice.STATUS_CANCELLED);
                    invoiceDao.addOrUpdate(selected);
                } else {
                    SalesDocs.deleteByParent(lineDao, "SalesInvoiceLine", "invoiceId", selected.getRowId());
                    invoiceDao.delete(selected.getRowId());
                }
                SalesDocs.syncDeliveriesForLines(deliveryDao, deliveryLineDao, invoiceDao, lineDao,
                        deliveryId, stored);
                refreshView();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.invoiceDao = TerpApplication.getInstance().getPersistence().createDao(SalesInvoice.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(SalesInvoiceLine.class);
        this.deliveryDao = TerpApplication.getInstance().getPersistence().createDao(SalesDelivery.class);
        this.deliveryLineDao = TerpApplication.getInstance().getPersistence()
                .createDao(SalesDeliveryLine.class);
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
        this.tcSourceDeliveryNo.setCellValueFactory(new PropertyValueFactory<>("sourceDeliveryNo"));
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        this.tcLineNo.setCellValueFactory(new PropertyValueFactory<>("lineNo"));
        this.tcLineSourceDeliveryNo.setCellValueFactory(new PropertyValueFactory<>("sourceDeliveryNo"));
        this.tcLineItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcLineItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcLineItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcLineQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        this.tcLineUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        this.tblvLineView.setPlaceholder(new Label("Belge satırı yok"));
        this.pgnInvoiceData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvInvoiceView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        this.searchSqlStatement = CompanyScope.from("SalesInvoice");
        this.btnAdd.setDisable(!rights.add);
        if (this.btnFromDelivery != null) {
            this.btnFromDelivery.setDisable(!rights.add);
        }
        updateButtons(true, true);
        refreshView();
        if (!tblvInvoiceView.getItems().isEmpty()) {
            tblvInvoiceView.getSelectionModel().select(0);
        }
    }

    private void showLines(SalesInvoice row) {
        if (row == null || row.getRowId() == null) {
            tblvLineView.setItems(FXCollections.observableArrayList());
            return;
        }
        List<SalesInvoiceLine> stored = lineDao.findAll(
                "from SalesInvoiceLine e where e.invoiceId = " + row.getRowId()
                        + " order by e.lineNo");
        tblvLineView.setItems(FXCollections.observableArrayList(stored == null ? List.of() : stored));
    }

    private void refreshView() {
        Long selectedId = selectedRowId();
        String hql = scopedQuery();
        long count = invoiceDao.getRecordCount(hql);
        int pages = (int) (count / rowsPerPage + 1);
        this.pgnInvoiceData.setPageCount(Math.max(1, pages));
        this.tblvInvoiceView.setItems(currentPage());
        restoreSelection(selectedId);
        showLines(tblvInvoiceView.getSelectionModel().getSelectedItem());
    }

    private Long selectedRowId() {
        SalesInvoice selected = tblvInvoiceView.getSelectionModel().getSelectedItem();
        return selected == null ? null : selected.getRowId();
    }

    private SalesInvoice findOnPage(Long rowId) {
        if (rowId == null) {
            return null;
        }
        for (SalesInvoice row : tblvInvoiceView.getItems()) {
            if (row != null && rowId.equals(row.getRowId())) {
                return row;
            }
        }
        return null;
    }

    private void restoreSelection(Long rowId) {
        SalesInvoice match = findOnPage(rowId);
        if (match == null) {
            return;
        }
        syncingSelection = true;
        tblvInvoiceView.getSelectionModel().select(match);
        syncingSelection = false;
    }

    private ObservableList<SalesInvoice> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<SalesInvoice> rows = invoiceDao.findPage(pageNum, rowsPerPage, scopedQuery());
        return FXCollections.observableArrayList(rows == null ? List.of() : rows);
    }

    private String scopedQuery() {
        if (searchSqlStatement == null || searchSqlStatement.isBlank()) {
            return CompanyScope.from("SalesInvoice");
        }
        return searchSqlStatement;
    }

    private void selectionChanged(Change<? extends SalesInvoice> change) {
        int size = change.getList().size();
        if (size == 0) {
            updateButtons(true, true);
            if (!syncingSelection) {
                showLines(null);
            }
        } else if (size == 1) {
            updateButtons(false, false);
            if (!syncingSelection) {
                showLines(change.getList().get(0));
            }
        } else {
            updateButtons(true, false);
        }
    }

    private void updateButtons(boolean editDisabled, boolean deleteDisabled) {
        this.btnEdit.setDisable(editDisabled || !rights.edit);
        this.btnDelete.setDisable(deleteDisabled || !rights.delete);
    }
}

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
import com.terp.plugin.data.IAccountLedger;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.AccountPosting;
import com.terp.plugin.gui.IIconFactory;
import com.terp.satin.data.PurchaseReceipt;
import com.terp.satin.data.PurchaseReceiptLine;
import com.terp.satin.data.PurchaseDocs;
import com.terp.satin.data.PurchaseInvoice;
import com.terp.satin.data.PurchaseInvoiceLine;
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
import com.terp.plugin.gui.NumericCells;

public class PurchaseInvoiceFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(PurchaseInvoiceFormController.class.getName());
    private static final int DEFAULT_ROWS_PER_PAGE = 20;
    private static final String ALL_STATUS = "Tümü";

    @FXML
    private Button btnSearch;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnFromReceipt;
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
    private TableView<PurchaseInvoice> tblvInvoiceView;
    @FXML
    private Pagination pgnInvoiceData;
    @FXML
    private TableColumn<PurchaseInvoice, String> tcDateLabel;
    @FXML
    private TableColumn<PurchaseInvoice, String> tcDocumentNo;
    @FXML
    private TableColumn<PurchaseInvoice, String> tcOfficialNo;
    @FXML
    private TableColumn<PurchaseInvoice, String> tcAccountCode;
    @FXML
    private TableColumn<PurchaseInvoice, String> tcAccountName;
    @FXML
    private TableColumn<PurchaseInvoice, String> tcWarehouseCode;
    @FXML
    private TableColumn<PurchaseInvoice, String> tcSourceReceiptNo;
    @FXML
    private TableColumn<PurchaseInvoice, String> tcStatusLabel;
    @FXML
    private TableView<PurchaseInvoiceLine> tblvLineView;
    @FXML
    private TableColumn<PurchaseInvoiceLine, Integer> tcLineNo;
    @FXML
    private TableColumn<PurchaseInvoiceLine, String> tcLineSourceReceiptNo;
    @FXML
    private TableColumn<PurchaseInvoiceLine, String> tcLineItemCode;
    @FXML
    private TableColumn<PurchaseInvoiceLine, String> tcLineItemDesc;
    @FXML
    private TableColumn<PurchaseInvoiceLine, String> tcLineItemUnit;
    @FXML
    private TableColumn<PurchaseInvoiceLine, Double> tcLineQuantity;
    @FXML
    private TableColumn<PurchaseInvoiceLine, Double> tcLineUnitPrice;

    private ICommonDao<PurchaseInvoice> invoiceDao;
    private ICommonDao<PurchaseInvoiceLine> lineDao;
    private ICommonDao<PurchaseReceipt> receiptDao;
    private ICommonDao<PurchaseReceiptLine> receiptLineDao;
    private FormRights rights = FormRights.forMenu("SAT07");
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";
    private boolean syncingSelection;

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from PurchaseInvoice e");
        boolean whereAdded = CompanyScope.append(sql, false);
        whereAdded = SatinDates.appendNumberSearch(sql, whereAdded, txtDocumentNo.getText());
        whereAdded = SatinDates.appendLike(sql, whereAdded, "e.accountCode", txtAccountCode.getText());
        String status = cmbSearchStatus.getValue();
        if ("Taslak".equals(status)) {
            sql.append(" and e.status = ").append(PurchaseInvoice.STATUS_DRAFT);
        } else if ("Onaylı".equals(status)) {
            sql.append(" and e.status = ").append(PurchaseInvoice.STATUS_POSTED);
        } else if ("İptal".equals(status)) {
            sql.append(" and e.status = ").append(PurchaseInvoice.STATUS_CANCELLED);
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
        SatinWindows.openInvoiceEditor(null, this::refreshView);
    }

    @FXML
    public void onActionBtnFromReceipt(ActionEvent event) {
        if (!CompanyScope.requireCompany()) {
            return;
        }
        SatinWindows.openInvoiceFromReceipt(this::refreshView);
    }

    @FXML
    public void onActionBtnEdit(ActionEvent event) {
        PurchaseInvoice selected = tblvInvoiceView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            SatinWindows.openInvoiceEditor(selected, this::refreshView);
        }
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        PurchaseInvoice selected = tblvInvoiceView.getSelectionModel().getSelectedItem();
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
                Long deliveryId = selected.getSourceReceiptId();
                List<PurchaseInvoiceLine> stored = selected.getRowId() == null ? List.of()
                        : lineDao.findAll("from PurchaseInvoiceLine e where e.invoiceId = "
                                + selected.getRowId());
                if (selected.isPosted()) {
                    IAccountLedger ledger = TerpApplication.getInstance().getAccountLedger();
                    if (ledger == null) {
                        SatinDates.showError("Alım faturası", "Cari eklentisi yok",
                                "İptal için terp.cari yüklü olmalıdır.");
                        return;
                    }
                    ledger.reverseByDocument(AccountPosting.SOURCE_PURCHASE_INVOICE, selected.getRowId());
                    selected.setStatus(PurchaseInvoice.STATUS_CANCELLED);
                    invoiceDao.addOrUpdate(selected);
                } else {
                    PurchaseDocs.deleteByParent(lineDao, "PurchaseInvoiceLine", "invoiceId", selected.getRowId());
                    invoiceDao.delete(selected.getRowId());
                }
                PurchaseDocs.syncReceiptsForLines(receiptDao, receiptLineDao, invoiceDao, lineDao,
                        deliveryId, stored);
                refreshView();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.invoiceDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseInvoice.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseInvoiceLine.class);
        this.receiptDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseReceipt.class);
        this.receiptLineDao = TerpApplication.getInstance().getPersistence()
                .createDao(PurchaseReceiptLine.class);
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
        this.tcOfficialNo.setCellValueFactory(new PropertyValueFactory<>("officialNo"));
        this.tcAccountCode.setCellValueFactory(new PropertyValueFactory<>("accountCode"));
        this.tcAccountName.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        this.tcWarehouseCode.setCellValueFactory(new PropertyValueFactory<>("warehouseCode"));
        this.tcSourceReceiptNo.setCellValueFactory(new PropertyValueFactory<>("sourceReceiptNo"));
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        this.tcLineNo.setCellValueFactory(new PropertyValueFactory<>("lineNo"));
        this.tcLineSourceReceiptNo.setCellValueFactory(new PropertyValueFactory<>("sourceReceiptNo"));
        this.tcLineItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcLineItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcLineItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcLineQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        NumericCells.apply(this.tcLineQuantity);
        this.tcLineUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        NumericCells.apply(this.tcLineUnitPrice);
        this.tblvLineView.setPlaceholder(new Label("Belge satırı yok"));
        this.pgnInvoiceData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvInvoiceView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        this.searchSqlStatement = CompanyScope.from("PurchaseInvoice");
        this.btnAdd.setDisable(!rights.add);
        if (this.btnFromReceipt != null) {
            this.btnFromReceipt.setDisable(!rights.add);
        }
        updateButtons(true, true);
        refreshView();
        if (!tblvInvoiceView.getItems().isEmpty()) {
            tblvInvoiceView.getSelectionModel().select(0);
        }
    }

    private void showLines(PurchaseInvoice row) {
        if (row == null || row.getRowId() == null) {
            tblvLineView.setItems(FXCollections.observableArrayList());
            return;
        }
        List<PurchaseInvoiceLine> stored = lineDao.findAll(
                "from PurchaseInvoiceLine e where e.invoiceId = " + row.getRowId()
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
        PurchaseInvoice selected = tblvInvoiceView.getSelectionModel().getSelectedItem();
        return selected == null ? null : selected.getRowId();
    }

    private PurchaseInvoice findOnPage(Long rowId) {
        if (rowId == null) {
            return null;
        }
        for (PurchaseInvoice row : tblvInvoiceView.getItems()) {
            if (row != null && rowId.equals(row.getRowId())) {
                return row;
            }
        }
        return null;
    }

    private void restoreSelection(Long rowId) {
        PurchaseInvoice match = findOnPage(rowId);
        if (match == null) {
            return;
        }
        syncingSelection = true;
        tblvInvoiceView.getSelectionModel().select(match);
        syncingSelection = false;
    }

    private ObservableList<PurchaseInvoice> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<PurchaseInvoice> rows = invoiceDao.findPage(pageNum, rowsPerPage, scopedQuery());
        return FXCollections.observableArrayList(rows == null ? List.of() : rows);
    }

    private String scopedQuery() {
        if (searchSqlStatement == null || searchSqlStatement.isBlank()) {
            return CompanyScope.from("PurchaseInvoice");
        }
        return searchSqlStatement;
    }

    private void selectionChanged(Change<? extends PurchaseInvoice> change) {
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

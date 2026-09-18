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
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.gui.IIconFactory;
import com.terp.satis.data.SalesDocs;
import com.terp.satis.data.SalesOrder;
import com.terp.satis.data.SalesQuote;
import com.terp.satis.data.SalesQuoteLine;
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
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import com.terp.plugin.gui.NumericCells;

public class QuoteFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(QuoteFormController.class.getName());
    private static final int DEFAULT_ROWS_PER_PAGE = 20;
    private static final String ALL_STATUS = "Tümü";

    @FXML
    private Button btnSearch;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnConvert;
    @FXML
    private Button btnDelete;
    @FXML
    private TextField txtDocumentNo;
    @FXML
    private TextField txtAccountCode;
    @FXML
    private ComboBox<String> cmbSearchStatus;
    @FXML
    private TableView<SalesQuote> tblvQuoteView;
    @FXML
    private Pagination pgnQuoteData;
    @FXML
    private TableColumn<SalesQuote, String> tcDateLabel;
    @FXML
    private TableColumn<SalesQuote, String> tcDocumentNo;
    @FXML
    private TableColumn<SalesQuote, String> tcAccountCode;
    @FXML
    private TableColumn<SalesQuote, String> tcAccountName;
    @FXML
    private TableColumn<SalesQuote, String> tcWarehouseCode;
    @FXML
    private TableColumn<SalesQuote, String> tcStatusLabel;
    @FXML
    private TableView<SalesQuoteLine> tblvLineView;
    @FXML
    private TableColumn<SalesQuoteLine, Integer> tcLineNo;
    @FXML
    private TableColumn<SalesQuoteLine, String> tcLineItemCode;
    @FXML
    private TableColumn<SalesQuoteLine, String> tcLineItemDesc;
    @FXML
    private TableColumn<SalesQuoteLine, String> tcLineItemUnit;
    @FXML
    private TableColumn<SalesQuoteLine, Double> tcLineQuantity;
    @FXML
    private TableColumn<SalesQuoteLine, Double> tcLineUnitPrice;

    private ICommonDao<SalesQuote> quoteDao;
    private ICommonDao<SalesQuoteLine> lineDao;
    private ICommonDao<SalesOrder> orderDao;
    private FormRights rights = FormRights.forMenu("STS06");
    private FormRights orderRights = FormRights.forMenu("STS03");
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";
    private boolean syncingSelection;

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from SalesQuote e");
        CompanyScope.append(sql, false);
        SatisDates.appendLike(sql, true, "e.documentNo", txtDocumentNo.getText());
        SatisDates.appendLike(sql, true, "e.accountCode", txtAccountCode.getText());
        String status = cmbSearchStatus.getValue();
        if ("Taslak".equals(status)) {
            sql.append(" and e.status = ").append(SalesQuote.STATUS_DRAFT);
        } else if ("Onaylı".equals(status)) {
            sql.append(" and e.status = ").append(SalesQuote.STATUS_APPROVED);
        } else if ("Sipariş".equals(status)) {
            sql.append(" and e.status = ").append(SalesQuote.STATUS_CONVERTED);
        } else if ("İptal".equals(status)) {
            sql.append(" and e.status = ").append(SalesQuote.STATUS_CANCELLED);
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
        SatisWindows.openQuoteEditor(null, this::refreshView);
    }

    @FXML
    public void onActionBtnEdit(ActionEvent event) {
        SalesQuote selected = tblvQuoteView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            SatisWindows.openQuoteEditor(selected, this::refreshView);
        }
    }

    @FXML
    public void onActionBtnConvert(ActionEvent event) {
        SalesQuote selected = tblvQuoteView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        SatisWindows.convertQuote(selected, quoteDao, lineDao, orderDao, this::refreshView);
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        SalesQuote selected = tblvQuoteView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (selected.isConverted() || SalesDocs.hasOpenOrder(orderDao, selected.getRowId())) {
            SatisDates.showError("Satış teklifi", "Siparişe bağlı",
                    "Önce bağlı satış siparişini iptal veya silin.");
            return;
        }
        String message = selected.isApproved()
                ? "Onaylı teklif iptal edilecek: " + selected.getDocumentNo()
                : "Seçilen teklif silinecek: " + selected.getDocumentNo();
        Alert alert = new Alert(AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }
            try {
                if (selected.isApproved()) {
                    selected.setStatus(SalesQuote.STATUS_CANCELLED);
                    selected.setLastUpdateDate(new Date());
                    quoteDao.addOrUpdate(selected);
                } else {
                    SalesDocs.deleteByParent(lineDao, "SalesQuoteLine", "quoteId", selected.getRowId());
                    quoteDao.delete(selected.getRowId());
                }
                refreshView();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.quoteDao = TerpApplication.getInstance().getPersistence().createDao(SalesQuote.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(SalesQuoteLine.class);
        this.orderDao = TerpApplication.getInstance().getPersistence().createDao(SalesOrder.class);
        IIconFactory icons = TerpApplication.getInstance().getIconFactory();
        if (icons != null) {
            this.btnSearch.setGraphic(icons.getIcon("SEARCH"));
            this.btnAdd.setGraphic(icons.getIcon("PLUS"));
            this.btnEdit.setGraphic(icons.getIcon("EDIT"));
            this.btnDelete.setGraphic(icons.getIcon("SEARCH"));
        }
        cmbSearchStatus.getItems().addAll(ALL_STATUS, "Taslak", "Onaylı", "Sipariş", "İptal");
        cmbSearchStatus.getSelectionModel().select(ALL_STATUS);
        this.tcDateLabel.setCellValueFactory(new PropertyValueFactory<>("dateLabel"));
        this.tcDocumentNo.setCellValueFactory(new PropertyValueFactory<>("documentNo"));
        this.tcAccountCode.setCellValueFactory(new PropertyValueFactory<>("accountCode"));
        this.tcAccountName.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        this.tcWarehouseCode.setCellValueFactory(new PropertyValueFactory<>("warehouseCode"));
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        this.tcLineNo.setCellValueFactory(new PropertyValueFactory<>("lineNo"));
        this.tcLineItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcLineItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcLineItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcLineQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        NumericCells.apply(this.tcLineQuantity);
        this.tcLineUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        NumericCells.apply(this.tcLineUnitPrice);
        this.tblvLineView.setPlaceholder(new Label("Belge satırı yok"));
        this.pgnQuoteData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvQuoteView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        this.searchSqlStatement = CompanyScope.from("SalesQuote");
        this.btnAdd.setDisable(!rights.add);
        updateButtons(true, true, true);
        refreshView();
        if (!tblvQuoteView.getItems().isEmpty()) {
            tblvQuoteView.getSelectionModel().select(0);
        }
    }

    private void showLines(SalesQuote row) {
        if (row == null || row.getRowId() == null) {
            tblvLineView.setItems(FXCollections.observableArrayList());
            return;
        }
        List<SalesQuoteLine> stored = lineDao.findAll(
                "from SalesQuoteLine e where e.quoteId = " + row.getRowId()
                        + " order by e.lineNo");
        tblvLineView.setItems(FXCollections.observableArrayList(stored == null ? List.of() : stored));
    }

    private void refreshView() {
        Long selectedId = selectedRowId();
        String hql = scopedQuery();
        long count = quoteDao.getRecordCount(hql);
        int pages = (int) (count / rowsPerPage + 1);
        this.pgnQuoteData.setPageCount(Math.max(1, pages));
        this.tblvQuoteView.setItems(currentPage());
        restoreSelection(selectedId);
        showLines(tblvQuoteView.getSelectionModel().getSelectedItem());
    }

    private Long selectedRowId() {
        SalesQuote selected = tblvQuoteView.getSelectionModel().getSelectedItem();
        return selected == null ? null : selected.getRowId();
    }

    private SalesQuote findOnPage(Long rowId) {
        if (rowId == null) {
            return null;
        }
        for (SalesQuote row : tblvQuoteView.getItems()) {
            if (row != null && rowId.equals(row.getRowId())) {
                return row;
            }
        }
        return null;
    }

    private void restoreSelection(Long rowId) {
        SalesQuote match = findOnPage(rowId);
        if (match == null) {
            return;
        }
        syncingSelection = true;
        tblvQuoteView.getSelectionModel().select(match);
        syncingSelection = false;
    }

    private ObservableList<SalesQuote> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<SalesQuote> rows = quoteDao.findPage(pageNum, rowsPerPage, scopedQuery());
        return FXCollections.observableArrayList(rows == null ? List.of() : rows);
    }

    private String scopedQuery() {
        if (searchSqlStatement == null || searchSqlStatement.isBlank()) {
            return CompanyScope.from("SalesQuote");
        }
        return searchSqlStatement;
    }

    private void selectionChanged(Change<? extends SalesQuote> change) {
        int size = change.getList().size();
        if (size == 0) {
            updateButtons(true, true, true);
            if (!syncingSelection) {
                showLines(null);
            }
            return;
        }
        SalesQuote selected = change.getList().get(0);
        boolean convertDisabled = size != 1 || selected == null || !selected.canConvert()
                || !orderRights.add;
        updateButtons(size != 1, size == 0, convertDisabled);
        if (!syncingSelection && size == 1) {
            showLines(selected);
        }
    }

    private void updateButtons(boolean editDisabled, boolean deleteDisabled, boolean convertDisabled) {
        this.btnEdit.setDisable(editDisabled || !rights.edit);
        this.btnDelete.setDisable(deleteDisabled || !rights.delete);
        this.btnConvert.setDisable(convertDisabled);
    }
}

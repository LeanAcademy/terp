/*
 * Copyright (C) 2026 LeanAcademy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.terp.stok.gui;

import com.terp.plugin.CompanyScope;
import com.terp.plugin.FormRights;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.IStockLedger;
import com.terp.plugin.data.StockPosting;
import com.terp.plugin.gui.IIconFactory;
import com.terp.stok.data.StockDocs;
import com.terp.stok.data.StockOpening;
import com.terp.stok.data.StockOpeningLine;
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

public class OpeningFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(OpeningFormController.class.getName());
    private static final int DEFAULT_ROWS_PER_PAGE = 20;
    private static final String ALL_STATUS = "Tümü";
    private static final String MENU_ID = "STK08";

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
    private TextField txtWarehouseCode;
    @FXML
    private ComboBox<String> cmbSearchStatus;
    @FXML
    private TableView<StockOpening> tblvOpeningView;
    @FXML
    private Pagination pgnOpeningData;
    @FXML
    private TableColumn<StockOpening, String> tcDateLabel;
    @FXML
    private TableColumn<StockOpening, String> tcDocumentNo;
    @FXML
    private TableColumn<StockOpening, String> tcWarehouseCode;
    @FXML
    private TableColumn<StockOpening, String> tcWarehouseName;
    @FXML
    private TableColumn<StockOpening, String> tcStatusLabel;
    @FXML
    private TableView<StockOpeningLine> tblvLineView;
    @FXML
    private TableColumn<StockOpeningLine, Integer> tcLineNo;
    @FXML
    private TableColumn<StockOpeningLine, String> tcLineItemCode;
    @FXML
    private TableColumn<StockOpeningLine, String> tcLineItemDesc;
    @FXML
    private TableColumn<StockOpeningLine, String> tcLineItemUnit;
    @FXML
    private TableColumn<StockOpeningLine, Double> tcLineQuantity;
    @FXML
    private TableColumn<StockOpeningLine, Double> tcLineUnitPrice;
    @FXML
    private TableColumn<StockOpeningLine, String> tcLineLotNo;
    @FXML
    private TableColumn<StockOpeningLine, String> tcLineSerialNo;
    @FXML
    private TableColumn<StockOpeningLine, String> tcLineExpiry;

    private ICommonDao<StockOpening> openingDao;
    private ICommonDao<StockOpeningLine> lineDao;
    private FormRights rights = FormRights.forMenu(MENU_ID);
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";
    private boolean syncingSelection;

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from StockOpening e");
        boolean whereAdded = CompanyScope.append(sql, false);
        whereAdded = StockForms.appendLike(sql, whereAdded, "e.documentNo", txtDocumentNo.getText());
        whereAdded = StockForms.appendLike(sql, whereAdded, "e.warehouseCode", txtWarehouseCode.getText());
        String status = cmbSearchStatus.getValue();
        if ("Taslak".equals(status)) {
            sql.append(" and e.status = ").append(StockOpening.STATUS_DRAFT);
        } else if ("Onaylı".equals(status)) {
            sql.append(" and e.status = ").append(StockOpening.STATUS_POSTED);
        } else if ("İptal".equals(status)) {
            sql.append(" and e.status = ").append(StockOpening.STATUS_CANCELLED);
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
        StockWindows.openOpeningEditor(null, this::refreshView);
    }

    @FXML
    public void onActionBtnEdit(ActionEvent event) {
        StockOpening selected = tblvOpeningView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            StockWindows.openOpeningEditor(selected, this::refreshView);
        }
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        StockOpening selected = tblvOpeningView.getSelectionModel().getSelectedItem();
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
                if (selected.isPosted()) {
                    IStockLedger ledger = TerpApplication.getInstance().getStockLedger();
                    if (ledger == null) {
                        StockForms.showError("Stok açılış", "Stok defteri yok",
                                "Açılış iptali için stok defteri gerekir.");
                        return;
                    }
                    ledger.reverseByDocument(StockPosting.SOURCE_STOCK_OPENING, selected.getRowId());
                    selected.setStatus(StockOpening.STATUS_CANCELLED);
                    openingDao.addOrUpdate(selected);
                } else {
                    StockDocs.deleteByParent(lineDao, "StockOpeningLine", "openingId", selected.getRowId());
                    openingDao.delete(selected.getRowId());
                }
                refreshView();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.openingDao = TerpApplication.getInstance().getPersistence().createDao(StockOpening.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(StockOpeningLine.class);
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
        this.tcWarehouseCode.setCellValueFactory(new PropertyValueFactory<>("warehouseCode"));
        this.tcWarehouseName.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        this.tcLineNo.setCellValueFactory(new PropertyValueFactory<>("lineNo"));
        this.tcLineItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcLineItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcLineItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcLineQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        NumericCells.apply(this.tcLineQuantity);
        this.tcLineUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        NumericCells.apply(this.tcLineUnitPrice);
        this.tcLineLotNo.setCellValueFactory(new PropertyValueFactory<>("lotNo"));
        this.tcLineSerialNo.setCellValueFactory(new PropertyValueFactory<>("serialNo"));
        this.tcLineExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryLabel"));
        this.tblvLineView.setPlaceholder(new Label("Belge satırı yok"));
        this.pgnOpeningData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvOpeningView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        this.searchSqlStatement = CompanyScope.from("StockOpening");
        this.btnAdd.setDisable(!rights.add);
        updateButtons(true, true);
        refreshView();
        if (!tblvOpeningView.getItems().isEmpty()) {
            tblvOpeningView.getSelectionModel().select(0);
        }
    }

    private void showLines(StockOpening row) {
        if (row == null || row.getRowId() == null) {
            tblvLineView.setItems(FXCollections.observableArrayList());
            return;
        }
        List<StockOpeningLine> stored = lineDao.findAll(
                "from StockOpeningLine e where e.openingId = " + row.getRowId()
                        + " order by e.lineNo");
        tblvLineView.setItems(FXCollections.observableArrayList(stored == null ? List.of() : stored));
    }

    private void refreshView() {
        Long selectedId = selectedRowId();
        String hql = scopedQuery();
        long count = openingDao.getRecordCount(hql);
        int pages = (int) (count / rowsPerPage + 1);
        this.pgnOpeningData.setPageCount(Math.max(1, pages));
        this.tblvOpeningView.setItems(currentPage());
        restoreSelection(selectedId);
        showLines(tblvOpeningView.getSelectionModel().getSelectedItem());
    }

    private Long selectedRowId() {
        StockOpening selected = tblvOpeningView.getSelectionModel().getSelectedItem();
        return selected == null ? null : selected.getRowId();
    }

    private StockOpening findOnPage(Long rowId) {
        if (rowId == null) {
            return null;
        }
        for (StockOpening row : tblvOpeningView.getItems()) {
            if (row != null && rowId.equals(row.getRowId())) {
                return row;
            }
        }
        return null;
    }

    private void restoreSelection(Long rowId) {
        StockOpening match = findOnPage(rowId);
        if (match == null) {
            return;
        }
        syncingSelection = true;
        tblvOpeningView.getSelectionModel().select(match);
        syncingSelection = false;
    }

    private ObservableList<StockOpening> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<StockOpening> rows = openingDao.findPage(pageNum, rowsPerPage, scopedQuery());
        return FXCollections.observableArrayList(rows == null ? List.of() : rows);
    }

    private String scopedQuery() {
        if (searchSqlStatement == null || searchSqlStatement.isBlank()) {
            return CompanyScope.from("StockOpening");
        }
        return searchSqlStatement;
    }

    private void selectionChanged(Change<? extends StockOpening> change) {
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

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
import com.terp.satin.data.PurchaseRequest;
import com.terp.satin.data.PurchaseRequestLine;
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

public class RequestFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(RequestFormController.class.getName());
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
    private ComboBox<String> cmbSearchStatus;
    @FXML
    private TableView<PurchaseRequest> tblvRequestView;
    @FXML
    private Pagination pgnRequestData;
    @FXML
    private TableColumn<PurchaseRequest, String> tcDateLabel;
    @FXML
    private TableColumn<PurchaseRequest, String> tcDocumentNo;
    @FXML
    private TableColumn<PurchaseRequest, String> tcWarehouseCode;
    @FXML
    private TableColumn<PurchaseRequest, String> tcStatusLabel;
    @FXML
    private TableView<PurchaseRequestLine> tblvLineView;
    @FXML
    private TableColumn<PurchaseRequestLine, Integer> tcLineNo;
    @FXML
    private TableColumn<PurchaseRequestLine, String> tcLineItemCode;
    @FXML
    private TableColumn<PurchaseRequestLine, String> tcLineItemDesc;
    @FXML
    private TableColumn<PurchaseRequestLine, String> tcLineItemUnit;
    @FXML
    private TableColumn<PurchaseRequestLine, Double> tcLineQuantity;

    private ICommonDao<PurchaseRequest> requestDao;
    private ICommonDao<PurchaseRequestLine> lineDao;
    private ICommonDao<PurchaseOrder> orderDao;
    private FormRights rights = FormRights.forMenu("SAT03");
    private FormRights orderRights = FormRights.forMenu("SAT04");
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";
    private boolean syncingSelection;

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from PurchaseRequest e");
        CompanyScope.append(sql, false);
        SatinDates.appendLike(sql, true, "e.documentNo", txtDocumentNo.getText());
        String status = cmbSearchStatus.getValue();
        if ("Taslak".equals(status)) {
            sql.append(" and e.status = ").append(PurchaseRequest.STATUS_DRAFT);
        } else if ("Onaylı".equals(status)) {
            sql.append(" and e.status = ").append(PurchaseRequest.STATUS_APPROVED);
        } else if ("Sipariş".equals(status)) {
            sql.append(" and e.status = ").append(PurchaseRequest.STATUS_CONVERTED);
        } else if ("İptal".equals(status)) {
            sql.append(" and e.status = ").append(PurchaseRequest.STATUS_CANCELLED);
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
        SatinWindows.openRequestEditor(null, this::refreshView);
    }

    @FXML
    public void onActionBtnEdit(ActionEvent event) {
        PurchaseRequest selected = tblvRequestView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            SatinWindows.openRequestEditor(selected, this::refreshView);
        }
    }

    @FXML
    public void onActionBtnConvert(ActionEvent event) {
        PurchaseRequest selected = tblvRequestView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        SatinWindows.convertRequest(selected, requestDao, lineDao, orderDao, this::refreshView);
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        PurchaseRequest selected = tblvRequestView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (selected.isConverted() || PurchaseDocs.hasOpenOrder(orderDao, selected.getRowId())) {
            SatinDates.showError("Satınalma talebi", "Siparişe bağlı",
                    "Önce bağlı satınalma siparişini iptal veya silin.");
            return;
        }
        String message = selected.isApproved()
                ? "Onaylı talep iptal edilecek: " + selected.getDocumentNo()
                : "Seçilen talep silinecek: " + selected.getDocumentNo();
        Alert alert = new Alert(AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }
            try {
                if (selected.isApproved()) {
                    selected.setStatus(PurchaseRequest.STATUS_CANCELLED);
                    requestDao.addOrUpdate(selected);
                } else {
                    PurchaseDocs.deleteByParent(lineDao, "PurchaseRequestLine", "requestId",
                            selected.getRowId());
                    requestDao.delete(selected.getRowId());
                }
                refreshView();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.requestDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseRequest.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseRequestLine.class);
        this.orderDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseOrder.class);
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
        this.tcWarehouseCode.setCellValueFactory(new PropertyValueFactory<>("warehouseCode"));
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        this.tcLineNo.setCellValueFactory(new PropertyValueFactory<>("lineNo"));
        this.tcLineItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcLineItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcLineItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcLineQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        this.tblvLineView.setPlaceholder(new Label("Belge satırı yok"));
        this.pgnRequestData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvRequestView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        this.searchSqlStatement = CompanyScope.from("PurchaseRequest");
        this.btnAdd.setDisable(!rights.add);
        updateButtons(true, true, true);
        refreshView();
        if (!tblvRequestView.getItems().isEmpty()) {
            tblvRequestView.getSelectionModel().select(0);
        }
    }

    private void showLines(PurchaseRequest row) {
        if (row == null || row.getRowId() == null) {
            tblvLineView.setItems(FXCollections.observableArrayList());
            return;
        }
        List<PurchaseRequestLine> stored = lineDao.findAll(
                "from PurchaseRequestLine e where e.requestId = " + row.getRowId()
                        + " order by e.lineNo");
        tblvLineView.setItems(FXCollections.observableArrayList(stored == null ? List.of() : stored));
    }

    private void refreshView() {
        Long selectedId = selectedRowId();
        String hql = scopedQuery();
        long count = requestDao.getRecordCount(hql);
        int pages = (int) (count / rowsPerPage + 1);
        this.pgnRequestData.setPageCount(Math.max(1, pages));
        this.tblvRequestView.setItems(currentPage());
        restoreSelection(selectedId);
        showLines(tblvRequestView.getSelectionModel().getSelectedItem());
    }

    private Long selectedRowId() {
        PurchaseRequest selected = tblvRequestView.getSelectionModel().getSelectedItem();
        return selected == null ? null : selected.getRowId();
    }

    private PurchaseRequest findOnPage(Long rowId) {
        if (rowId == null) {
            return null;
        }
        for (PurchaseRequest row : tblvRequestView.getItems()) {
            if (row != null && rowId.equals(row.getRowId())) {
                return row;
            }
        }
        return null;
    }

    private void restoreSelection(Long rowId) {
        PurchaseRequest match = findOnPage(rowId);
        if (match == null) {
            return;
        }
        syncingSelection = true;
        tblvRequestView.getSelectionModel().select(match);
        syncingSelection = false;
    }

    private ObservableList<PurchaseRequest> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<PurchaseRequest> rows = requestDao.findPage(pageNum, rowsPerPage, scopedQuery());
        return FXCollections.observableArrayList(rows == null ? List.of() : rows);
    }

    private String scopedQuery() {
        if (searchSqlStatement == null || searchSqlStatement.isBlank()) {
            return CompanyScope.from("PurchaseRequest");
        }
        return searchSqlStatement;
    }

    private void selectionChanged(Change<? extends PurchaseRequest> change) {
        int size = change.getList().size();
        if (size == 0) {
            updateButtons(true, true, true);
            if (!syncingSelection) {
                showLines(null);
            }
            return;
        }
        PurchaseRequest selected = change.getList().get(0);
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

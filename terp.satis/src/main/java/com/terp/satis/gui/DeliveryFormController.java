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
import com.terp.plugin.data.IStockLedger;
import com.terp.plugin.data.StockPosting;
import com.terp.plugin.gui.IIconFactory;
import com.terp.satis.data.SalesDelivery;
import com.terp.satis.data.SalesDeliveryLine;
import com.terp.satis.data.SalesDocs;
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

public class DeliveryFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(DeliveryFormController.class.getName());
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
    private TableView<SalesDelivery> tblvDeliveryView;
    @FXML
    private Pagination pgnDeliveryData;
    @FXML
    private TableColumn<SalesDelivery, String> tcDateLabel;
    @FXML
    private TableColumn<SalesDelivery, String> tcDocumentNo;
    @FXML
    private TableColumn<SalesDelivery, String> tcAccountCode;
    @FXML
    private TableColumn<SalesDelivery, String> tcAccountName;
    @FXML
    private TableColumn<SalesDelivery, String> tcWarehouseCode;
    @FXML
    private TableColumn<SalesDelivery, String> tcStatusLabel;
    @FXML
    private TableView<SalesDeliveryLine> tblvLineView;
    @FXML
    private TableColumn<SalesDeliveryLine, Integer> tcLineNo;
    @FXML
    private TableColumn<SalesDeliveryLine, String> tcLineItemCode;
    @FXML
    private TableColumn<SalesDeliveryLine, String> tcLineItemDesc;
    @FXML
    private TableColumn<SalesDeliveryLine, String> tcLineItemUnit;
    @FXML
    private TableColumn<SalesDeliveryLine, Double> tcLineQuantity;
    @FXML
    private TableColumn<SalesDeliveryLine, Double> tcLineUnitPrice;

    private ICommonDao<SalesDelivery> deliveryDao;
    private ICommonDao<SalesDeliveryLine> lineDao;
    private FormRights rights = FormRights.forMenu("STS02");
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";
    private boolean syncingSelection;

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from SalesDelivery e");
        boolean whereAdded = CompanyScope.append(sql, false);
        whereAdded = SatisDates.appendLike(sql, whereAdded, "e.documentNo", txtDocumentNo.getText());
        whereAdded = SatisDates.appendLike(sql, whereAdded, "e.accountCode", txtAccountCode.getText());
        String status = cmbSearchStatus.getValue();
        if ("Taslak".equals(status)) {
            sql.append(" and e.status = ").append(SalesDelivery.STATUS_DRAFT);
        } else if ("Onaylı".equals(status)) {
            sql.append(" and e.status = ").append(SalesDelivery.STATUS_POSTED);
        } else if ("İptal".equals(status)) {
            sql.append(" and e.status = ").append(SalesDelivery.STATUS_CANCELLED);
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
        SatisWindows.openDeliveryEditor(null, this::refreshView);
    }

    @FXML
    public void onActionBtnEdit(ActionEvent event) {
        SalesDelivery selected = tblvDeliveryView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            SatisWindows.openDeliveryEditor(selected, this::refreshView);
        }
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        SalesDelivery selected = tblvDeliveryView.getSelectionModel().getSelectedItem();
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
                        showError("Stok eklentisi yok", "İrsaliye iptali için terp.stok yüklü olmalıdır.");
                        return;
                    }
                    ledger.reverseByDocument(StockPosting.SOURCE_SALES_DELIVERY, selected.getRowId());
                    selected.setStatus(SalesDelivery.STATUS_CANCELLED);
                    deliveryDao.addOrUpdate(selected);
                } else {
                    SalesDocs.deleteByParent(lineDao, "SalesDeliveryLine", "deliveryId", selected.getRowId());
                    deliveryDao.delete(selected.getRowId());
                }
                refreshView();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.deliveryDao = TerpApplication.getInstance().getPersistence().createDao(SalesDelivery.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(SalesDeliveryLine.class);
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
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        this.tcLineNo.setCellValueFactory(new PropertyValueFactory<>("lineNo"));
        this.tcLineItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcLineItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcLineItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcLineQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        this.tcLineUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        this.tblvLineView.setPlaceholder(new Label("Belge satırı yok"));
        this.pgnDeliveryData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvDeliveryView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        this.searchSqlStatement = CompanyScope.from("SalesDelivery");
        this.btnAdd.setDisable(!rights.add);
        updateButtons(true, true);
        refreshView();
        if (!tblvDeliveryView.getItems().isEmpty()) {
            tblvDeliveryView.getSelectionModel().select(0);
        }
    }

    private void showLines(SalesDelivery row) {
        if (row == null || row.getRowId() == null) {
            tblvLineView.setItems(FXCollections.observableArrayList());
            return;
        }
        List<SalesDeliveryLine> stored = lineDao.findAll(
                "from SalesDeliveryLine e where e.deliveryId = " + row.getRowId()
                        + " order by e.lineNo");
        tblvLineView.setItems(FXCollections.observableArrayList(stored == null ? List.of() : stored));
    }

    private void refreshView() {
        Long selectedId = selectedRowId();
        String hql = scopedQuery();
        long count = deliveryDao.getRecordCount(hql);
        int pages = (int) (count / rowsPerPage + 1);
        this.pgnDeliveryData.setPageCount(Math.max(1, pages));
        this.tblvDeliveryView.setItems(currentPage());
        restoreSelection(selectedId);
        showLines(tblvDeliveryView.getSelectionModel().getSelectedItem());
    }

    private Long selectedRowId() {
        SalesDelivery selected = tblvDeliveryView.getSelectionModel().getSelectedItem();
        return selected == null ? null : selected.getRowId();
    }

    private SalesDelivery findOnPage(Long rowId) {
        if (rowId == null) {
            return null;
        }
        for (SalesDelivery row : tblvDeliveryView.getItems()) {
            if (row != null && rowId.equals(row.getRowId())) {
                return row;
            }
        }
        return null;
    }

    private void restoreSelection(Long rowId) {
        SalesDelivery match = findOnPage(rowId);
        if (match == null) {
            return;
        }
        syncingSelection = true;
        tblvDeliveryView.getSelectionModel().select(match);
        syncingSelection = false;
    }

    private ObservableList<SalesDelivery> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<SalesDelivery> rows = deliveryDao.findPage(pageNum, rowsPerPage, scopedQuery());
        return FXCollections.observableArrayList(rows == null ? List.of() : rows);
    }

    private String scopedQuery() {
        if (searchSqlStatement == null || searchSqlStatement.isBlank()) {
            return CompanyScope.from("SalesDelivery");
        }
        return searchSqlStatement;
    }

    private void selectionChanged(Change<? extends SalesDelivery> change) {
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

    private static void showError(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Satış irsaliyesi");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();
    }
}

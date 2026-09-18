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
import com.terp.satis.data.SalesDelivery;
import com.terp.satis.data.SalesDeliveryLine;
import com.terp.satis.data.SalesDocs;
import com.terp.satis.data.SalesOrder;
import com.terp.satis.data.SalesOrderLine;
import com.terp.satis.data.SalesQuote;
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
    private Button btnDeliver;
    @FXML
    private Button btnProduce;
    @FXML
    private Button btnDelete;
    @FXML
    private TextField txtDocumentNo;
    @FXML
    private TextField txtAccountCode;
    @FXML
    private ComboBox<String> cmbSearchStatus;
    @FXML
    private TableView<SalesOrder> tblvOrderView;
    @FXML
    private Pagination pgnOrderData;
    @FXML
    private TableColumn<SalesOrder, String> tcDateLabel;
    @FXML
    private TableColumn<SalesOrder, String> tcDocumentNo;
    @FXML
    private TableColumn<SalesOrder, String> tcAccountCode;
    @FXML
    private TableColumn<SalesOrder, String> tcAccountName;
    @FXML
    private TableColumn<SalesOrder, String> tcWarehouseCode;
    @FXML
    private TableColumn<SalesOrder, String> tcSourceQuoteNo;
    @FXML
    private TableColumn<SalesOrder, String> tcStatusLabel;
    @FXML
    private TableView<SalesOrderLine> tblvLineView;
    @FXML
    private TableColumn<SalesOrderLine, Integer> tcLineNo;
    @FXML
    private TableColumn<SalesOrderLine, String> tcLineItemCode;
    @FXML
    private TableColumn<SalesOrderLine, String> tcLineItemDesc;
    @FXML
    private TableColumn<SalesOrderLine, String> tcLineItemUnit;
    @FXML
    private TableColumn<SalesOrderLine, Double> tcLineQuantity;
    @FXML
    private TableColumn<SalesOrderLine, Double> tcLineUnitPrice;

    private ICommonDao<SalesOrder> orderDao;
    private ICommonDao<SalesOrderLine> lineDao;
    private ICommonDao<SalesDelivery> deliveryDao;
    private ICommonDao<SalesDeliveryLine> deliveryLineDao;
    private ICommonDao<SalesQuote> quoteDao;
    private FormRights rights = FormRights.forMenu("STS03");
    private FormRights deliveryRights = FormRights.forMenu("STS02");
    private FormRights productionRights = FormRights.forMenu("URT03");
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";
    private boolean syncingSelection;

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from SalesOrder e");
        CompanyScope.append(sql, false);
        SatisDates.appendLike(sql, true, "e.documentNo", txtDocumentNo.getText());
        SatisDates.appendLike(sql, true, "e.accountCode", txtAccountCode.getText());
        String status = cmbSearchStatus.getValue();
        if ("Taslak".equals(status)) {
            sql.append(" and e.status = ").append(SalesOrder.STATUS_DRAFT);
        } else if ("Onaylı".equals(status)) {
            sql.append(" and e.status = ").append(SalesOrder.STATUS_APPROVED);
        } else if ("Kapalı".equals(status)) {
            sql.append(" and e.status = ").append(SalesOrder.STATUS_DELIVERED);
        } else if ("İptal".equals(status)) {
            sql.append(" and e.status = ").append(SalesOrder.STATUS_CANCELLED);
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
        SatisWindows.openOrderEditor(null, this::refreshView);
    }

    @FXML
    public void onActionBtnEdit(ActionEvent event) {
        SalesOrder selected = tblvOrderView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            SatisWindows.openOrderEditor(selected, this::refreshView);
        }
    }

    @FXML
    public void onActionBtnDeliver(ActionEvent event) {
        SalesOrder selected = tblvOrderView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        SatisWindows.convertOrder(selected, orderDao, lineDao, deliveryDao, this::refreshView);
    }

    @FXML
    public void onActionBtnProduce(ActionEvent event) {
        SalesOrder selected = tblvOrderView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        SatisWindows.convertToProduction(selected, orderDao, lineDao, this::refreshView);
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        SalesOrder selected = tblvOrderView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (selected.isDelivered()
                || SalesDocs.hasPostedDelivery(deliveryDao, deliveryLineDao, lineDao, selected.getRowId())) {
            SatisDates.showError("Satış siparişi", "İrsaliyeye bağlı",
                    "Teslim edilmiş sipariş silinemez. Önce bağlı satış irsaliyesini iptal veya silin.");
            return;
        }
        if (selected.isCancelled()) {
            SatisDates.showError("Satış siparişi", "İptal", "İptal edilmiş sipariş silinemez.");
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
                Long quoteId = selected.getSourceQuoteId();
                if (selected.isApproved()) {
                    selected.setStatus(SalesOrder.STATUS_CANCELLED);
                    selected.setLastUpdateDate(new Date());
                    orderDao.addOrUpdate(selected);
                } else if (selected.isDraft()) {
                    SalesDocs.deleteByParent(lineDao, "SalesOrderLine", "orderId", selected.getRowId());
                    orderDao.delete(selected.getRowId());
                }
                SalesDocs.syncQuoteStatus(quoteDao, orderDao, quoteId);
                refreshView();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.orderDao = TerpApplication.getInstance().getPersistence().createDao(SalesOrder.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(SalesOrderLine.class);
        this.deliveryDao = TerpApplication.getInstance().getPersistence().createDao(SalesDelivery.class);
        this.deliveryLineDao = TerpApplication.getInstance().getPersistence().createDao(SalesDeliveryLine.class);
        this.quoteDao = TerpApplication.getInstance().getPersistence().createDao(SalesQuote.class);
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
        this.tcSourceQuoteNo.setCellValueFactory(new PropertyValueFactory<>("sourceQuoteNo"));
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
        this.pgnOrderData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvOrderView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        this.searchSqlStatement = CompanyScope.from("SalesOrder");
        this.btnAdd.setDisable(!rights.add);
        updateButtons(true, true, true, true);
        refreshView();
        if (!tblvOrderView.getItems().isEmpty()) {
            tblvOrderView.getSelectionModel().select(0);
        }
    }

    private void showLines(SalesOrder row) {
        if (row == null || row.getRowId() == null) {
            tblvLineView.setItems(FXCollections.observableArrayList());
            return;
        }
        List<SalesOrderLine> stored = lineDao.findAll(
                "from SalesOrderLine e where e.orderId = " + row.getRowId()
                        + " order by e.lineNo");
        tblvLineView.setItems(FXCollections.observableArrayList(stored == null ? List.of() : stored));
    }

    private void refreshView() {
        Long selectedId = selectedRowId();
        String hql = scopedQuery();
        long count = orderDao.getRecordCount(hql);
        int pages = (int) (count / rowsPerPage + 1);
        this.pgnOrderData.setPageCount(Math.max(1, pages));
        this.tblvOrderView.setItems(currentPage());
        restoreSelection(selectedId);
        showLines(tblvOrderView.getSelectionModel().getSelectedItem());
    }

    private Long selectedRowId() {
        SalesOrder selected = tblvOrderView.getSelectionModel().getSelectedItem();
        return selected == null ? null : selected.getRowId();
    }

    private SalesOrder findOnPage(Long rowId) {
        if (rowId == null) {
            return null;
        }
        for (SalesOrder row : tblvOrderView.getItems()) {
            if (row != null && rowId.equals(row.getRowId())) {
                return row;
            }
        }
        return null;
    }

    private void restoreSelection(Long rowId) {
        SalesOrder match = findOnPage(rowId);
        if (match == null) {
            return;
        }
        syncingSelection = true;
        tblvOrderView.getSelectionModel().select(match);
        syncingSelection = false;
    }

    private ObservableList<SalesOrder> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<SalesOrder> rows = orderDao.findPage(pageNum, rowsPerPage, scopedQuery());
        return FXCollections.observableArrayList(rows == null ? List.of() : rows);
    }

    private String scopedQuery() {
        if (searchSqlStatement == null || searchSqlStatement.isBlank()) {
            return CompanyScope.from("SalesOrder");
        }
        return searchSqlStatement;
    }

    private void selectionChanged(Change<? extends SalesOrder> change) {
        int size = change.getList().size();
        if (size == 0) {
            updateButtons(true, true, true, true);
            if (!syncingSelection) {
                showLines(null);
            }
            return;
        }
        SalesOrder selected = change.getList().get(0);
        boolean deliverDisabled = size != 1 || selected == null
                || !SalesDocs.hasDeliverableBalance(selected, lineDao, deliveryDao, deliveryLineDao)
                || !deliveryRights.add;
        boolean produceDisabled = size != 1 || selected == null
                || !SatisWindows.hasProducibleBalance(selected, lineDao)
                || !productionRights.add;
        boolean deleteDisabled = size == 0 || selected == null || selected.isDelivered()
                || selected.isCancelled()
                || SalesDocs.hasPostedDelivery(deliveryDao, deliveryLineDao, lineDao, selected.getRowId());
        updateButtons(size != 1, deleteDisabled, deliverDisabled, produceDisabled);
        if (!syncingSelection && size == 1) {
            showLines(selected);
        }
    }

    private void updateButtons(boolean editDisabled, boolean deleteDisabled, boolean deliverDisabled,
            boolean produceDisabled) {
        this.btnEdit.setDisable(editDisabled || !rights.edit);
        this.btnDelete.setDisable(deleteDisabled || !rights.delete);
        this.btnDeliver.setDisable(deliverDisabled);
        this.btnProduce.setDisable(produceDisabled);
    }
}

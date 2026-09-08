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
import java.io.IOException;
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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

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

    private ICommonDao<PurchaseRequest> requestDao;
    private ICommonDao<PurchaseRequestLine> lineDao;
    private ICommonDao<PurchaseOrder> orderDao;
    private FormRights rights = FormRights.forMenu("SAT03");
    private FormRights orderRights = FormRights.forMenu("SAT04");
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";

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
        openEditor(null);
    }

    @FXML
    public void onActionBtnEdit(ActionEvent event) {
        PurchaseRequest selected = tblvRequestView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openEditor(selected);
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
    }

    private void openEditor(PurchaseRequest current) {
        try {
            FXMLLoader loader = SatinDates.pluginLoader(RequestFormController.class, "/fxml/RequestEditForm.fxml");
            Node node = loader.load();
            RequestEditFormController controller = loader.getController();
            controller.initializeForm(current);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(new Scene((Parent) node));
            stage.setTitle(current == null ? "Yeni satınalma talebi" : "Satınalma talebi");
            stage.showAndWait();
            refreshView();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void refreshView() {
        String hql = scopedQuery();
        long count = requestDao.getRecordCount(hql);
        int pages = (int) (count / rowsPerPage + 1);
        this.pgnRequestData.setPageCount(Math.max(1, pages));
        this.tblvRequestView.setItems(currentPage());
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
            return;
        }
        PurchaseRequest selected = change.getList().get(0);
        boolean convertDisabled = size != 1 || selected == null || !selected.canConvert()
                || !orderRights.add;
        updateButtons(size != 1, size == 0, convertDisabled);
    }

    private void updateButtons(boolean editDisabled, boolean deleteDisabled, boolean convertDisabled) {
        this.btnEdit.setDisable(editDisabled || !rights.edit);
        this.btnDelete.setDisable(deleteDisabled || !rights.delete);
        this.btnConvert.setDisable(convertDisabled);
    }
}

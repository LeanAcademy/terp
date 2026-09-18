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
package com.terp.uretim.gui;

import com.terp.plugin.CompanyScope;
import com.terp.plugin.FormRights;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.gui.IIconFactory;
import com.terp.uretim.data.ProductionCompletion;
import com.terp.uretim.data.ProductionCompletionMaterial;
import com.terp.uretim.data.ProductionOrder;
import com.terp.uretim.data.ProductionOrderMaterial;
import com.terp.uretim.data.UretimDocs;
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

public class ProductionOrderFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ProductionOrderFormController.class.getName());
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
    private TextField txtProductCode;
    @FXML
    private ComboBox<String> cmbSearchStatus;
    @FXML
    private TableView<ProductionOrder> tblvOrderView;
    @FXML
    private Pagination pgnOrderData;
    @FXML
    private TableColumn<ProductionOrder, String> tcDocumentNo;
    @FXML
    private TableColumn<ProductionOrder, String> tcDateLabel;
    @FXML
    private TableColumn<ProductionOrder, String> tcProductCode;
    @FXML
    private TableColumn<ProductionOrder, String> tcProductName;
    @FXML
    private TableColumn<ProductionOrder, Double> tcPlannedQty;
    @FXML
    private TableColumn<ProductionOrder, Double> tcCompletedQty;
    @FXML
    private TableColumn<ProductionOrder, String> tcSourceOrderNo;
    @FXML
    private TableColumn<ProductionOrder, String> tcStatusLabel;
    @FXML
    private TableView<ProductionOrderMaterial> tblvLineView;
    @FXML
    private TableColumn<ProductionOrderMaterial, Integer> tcLineNo;
    @FXML
    private TableColumn<ProductionOrderMaterial, String> tcLineItemCode;
    @FXML
    private TableColumn<ProductionOrderMaterial, String> tcLineItemDesc;
    @FXML
    private TableColumn<ProductionOrderMaterial, String> tcLineItemUnit;
    @FXML
    private TableColumn<ProductionOrderMaterial, Double> tcLinePlannedQty;

    private ICommonDao<ProductionOrder> orderDao;
    private ICommonDao<ProductionOrderMaterial> materialDao;
    private ICommonDao<ProductionCompletion> completionDao;
    private ICommonDao<ProductionCompletionMaterial> completionMaterialDao;
    private FormRights rights = FormRights.forMenu("URT03");
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";
    private boolean syncingSelection;

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from ProductionOrder e");
        CompanyScope.append(sql, false);
        UretimDates.appendLike(sql, true, "e.documentNo", txtDocumentNo.getText());
        UretimDates.appendLike(sql, true, "e.productCode", txtProductCode.getText());
        String status = cmbSearchStatus.getValue();
        if ("Taslak".equals(status)) {
            sql.append(" and e.status = ").append(ProductionOrder.STATUS_DRAFT);
        } else if ("Onaylı".equals(status)) {
            sql.append(" and e.status = ").append(ProductionOrder.STATUS_APPROVED);
        } else if ("Kapalı".equals(status)) {
            sql.append(" and e.status = ").append(ProductionOrder.STATUS_CLOSED);
        } else if ("İptal".equals(status)) {
            sql.append(" and e.status = ").append(ProductionOrder.STATUS_CANCELLED);
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
        UretimWindows.openOrderEditor(null, null, this::refreshView);
    }

    @FXML
    public void onActionBtnEdit(ActionEvent event) {
        ProductionOrder selected = tblvOrderView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            UretimWindows.openOrderEditor(selected, null, this::refreshView);
        }
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        ProductionOrder selected = tblvOrderView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (UretimDocs.hasPostedCompletion(completionDao, selected.getRowId())) {
            UretimDates.showError("Üretim emri", "İş bitiş var",
                    "Önce iş bitiş kayıtlarını iptal edin.");
            return;
        }
        boolean remove = selected.isDraft() || selected.isCancelled();
        String message = remove
                ? "Seçilen üretim emri silinecek: " + selected.getDocumentNo()
                : "Onaylı üretim emri iptal edilecek: " + selected.getDocumentNo();
        Alert alert = new Alert(AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }
            try {
                if (remove) {
                    UretimDocs.deleteOrder(orderDao, materialDao, completionDao, completionMaterialDao,
                            selected.getRowId());
                } else {
                    selected.setStatus(ProductionOrder.STATUS_CANCELLED);
                    selected.setLastUpdateDate(new Date());
                    orderDao.addOrUpdate(selected);
                }
                refreshView();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, null, ex);
                UretimDates.showError("Üretim emri", "Silinemedi",
                        ex.getMessage() == null ? "İşlem başarısız." : ex.getMessage());
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.orderDao = TerpApplication.getInstance().getPersistence().createDao(ProductionOrder.class);
        this.materialDao = TerpApplication.getInstance().getPersistence()
                .createDao(ProductionOrderMaterial.class);
        this.completionDao = TerpApplication.getInstance().getPersistence()
                .createDao(ProductionCompletion.class);
        this.completionMaterialDao = TerpApplication.getInstance().getPersistence()
                .createDao(ProductionCompletionMaterial.class);
        IIconFactory icons = TerpApplication.getInstance().getIconFactory();
        if (icons != null) {
            this.btnSearch.setGraphic(icons.getIcon("SEARCH"));
            this.btnAdd.setGraphic(icons.getIcon("PLUS"));
            this.btnEdit.setGraphic(icons.getIcon("EDIT"));
            this.btnDelete.setGraphic(icons.getIcon("SEARCH"));
        }
        cmbSearchStatus.getItems().addAll(ALL_STATUS, "Taslak", "Onaylı", "Kapalı", "İptal");
        cmbSearchStatus.getSelectionModel().select(ALL_STATUS);
        this.tcDocumentNo.setCellValueFactory(new PropertyValueFactory<>("documentNo"));
        this.tcDateLabel.setCellValueFactory(new PropertyValueFactory<>("dateLabel"));
        this.tcProductCode.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        this.tcProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        this.tcPlannedQty.setCellValueFactory(new PropertyValueFactory<>("plannedQty"));
        NumericCells.apply(this.tcPlannedQty);
        this.tcCompletedQty.setCellValueFactory(new PropertyValueFactory<>("completedQty"));
        NumericCells.apply(this.tcCompletedQty);
        this.tcSourceOrderNo.setCellValueFactory(new PropertyValueFactory<>("sourceOrderNo"));
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        this.tcLineNo.setCellValueFactory(new PropertyValueFactory<>("lineNo"));
        this.tcLineItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcLineItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcLineItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcLinePlannedQty.setCellValueFactory(new PropertyValueFactory<>("plannedQty"));
        NumericCells.apply(this.tcLinePlannedQty);
        this.tblvLineView.setPlaceholder(new Label("Malzeme çekme listesi yok"));
        this.pgnOrderData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvOrderView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        this.searchSqlStatement = CompanyScope.from("ProductionOrder");
        this.btnAdd.setDisable(!rights.add);
        updateButtons(true, true);
        refreshView();
        if (!tblvOrderView.getItems().isEmpty()) {
            tblvOrderView.getSelectionModel().select(0);
        }
    }

    private void showLines(ProductionOrder row) {
        tblvLineView.setItems(FXCollections.observableArrayList(
                UretimDocs.materialsOf(materialDao, row == null ? null : row.getRowId())));
    }

    private void refreshView() {
        Long selectedId = selectedRowId();
        long count = orderDao.getRecordCount(scopedQuery());
        int pages = (int) (count / rowsPerPage + 1);
        this.pgnOrderData.setPageCount(Math.max(1, pages));
        this.tblvOrderView.setItems(currentPage());
        restoreSelection(selectedId);
        showLines(tblvOrderView.getSelectionModel().getSelectedItem());
    }

    private Long selectedRowId() {
        ProductionOrder selected = tblvOrderView.getSelectionModel().getSelectedItem();
        return selected == null ? null : selected.getRowId();
    }

    private ProductionOrder findOnPage(Long rowId) {
        if (rowId == null) {
            return null;
        }
        for (ProductionOrder row : tblvOrderView.getItems()) {
            if (row != null && rowId.equals(row.getRowId())) {
                return row;
            }
        }
        return null;
    }

    private void restoreSelection(Long rowId) {
        ProductionOrder match = findOnPage(rowId);
        if (match == null) {
            return;
        }
        syncingSelection = true;
        tblvOrderView.getSelectionModel().select(match);
        syncingSelection = false;
    }

    private ObservableList<ProductionOrder> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<ProductionOrder> rows = orderDao.findPage(pageNum, rowsPerPage, scopedQuery());
        return FXCollections.observableArrayList(rows == null ? List.of() : rows);
    }

    private String scopedQuery() {
        if (searchSqlStatement == null || searchSqlStatement.isBlank()) {
            return CompanyScope.from("ProductionOrder");
        }
        return searchSqlStatement;
    }

    private void selectionChanged(Change<? extends ProductionOrder> change) {
        int size = change.getList().size();
        if (size == 0) {
            updateButtons(true, true);
            if (!syncingSelection) {
                showLines(null);
            }
            return;
        }
        ProductionOrder selected = change.getList().get(0);
        updateButtons(size != 1, size == 0);
        if (!syncingSelection && size == 1) {
            showLines(selected);
        }
    }

    private void updateButtons(boolean editDisabled, boolean deleteDisabled) {
        this.btnEdit.setDisable(editDisabled || !rights.edit);
        this.btnDelete.setDisable(deleteDisabled || !rights.delete);
    }
}

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
import com.terp.plugin.data.IStockLedger;
import com.terp.plugin.data.StockPosting;
import com.terp.plugin.gui.IIconFactory;
import com.terp.uretim.data.ProductionCompletion;
import com.terp.uretim.data.ProductionCompletionMaterial;
import com.terp.uretim.data.ProductionOrder;
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

public class ProductionCompletionListFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ProductionCompletionListFormController.class.getName());
    private static final int DEFAULT_ROWS_PER_PAGE = 20;
    private static final String ALL_STATUS = "Tümü";

    @FXML
    private Button btnSearch;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnDelete;
    @FXML
    private TextField txtOrderNo;
    @FXML
    private TextField txtProductCode;
    @FXML
    private ComboBox<String> cmbSearchStatus;
    @FXML
    private TableView<ProductionCompletion> tblvCompletionView;
    @FXML
    private Pagination pgnCompletionData;
    @FXML
    private TableColumn<ProductionCompletion, String> tcDateLabel;
    @FXML
    private TableColumn<ProductionCompletion, String> tcOrderDocumentNo;
    @FXML
    private TableColumn<ProductionCompletion, String> tcProductCode;
    @FXML
    private TableColumn<ProductionCompletion, String> tcProductName;
    @FXML
    private TableColumn<ProductionCompletion, Double> tcProducedQty;
    @FXML
    private TableColumn<ProductionCompletion, String> tcStatusLabel;
    @FXML
    private TableView<ProductionCompletionMaterial> tblvLineView;
    @FXML
    private TableColumn<ProductionCompletionMaterial, Integer> tcLineNo;
    @FXML
    private TableColumn<ProductionCompletionMaterial, String> tcLineItemCode;
    @FXML
    private TableColumn<ProductionCompletionMaterial, String> tcLineItemDesc;
    @FXML
    private TableColumn<ProductionCompletionMaterial, String> tcLineItemUnit;
    @FXML
    private TableColumn<ProductionCompletionMaterial, Double> tcLineQuantity;

    private ICommonDao<ProductionOrder> orderDao;
    private ICommonDao<ProductionCompletion> completionDao;
    private ICommonDao<ProductionCompletionMaterial> completionMaterialDao;
    private FormRights rights = FormRights.forMenu("URT04");
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";
    private boolean syncingSelection;

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from ProductionCompletion e");
        CompanyScope.append(sql, false);
        appendOrderFilter(sql, "o.documentNo", txtOrderNo.getText());
        appendOrderFilter(sql, "o.productCode", txtProductCode.getText());
        String status = cmbSearchStatus.getValue();
        if ("İşlendi".equals(status)) {
            sql.append(" and e.status = ").append(ProductionCompletion.STATUS_POSTED);
        } else if ("İptal".equals(status)) {
            sql.append(" and e.status = ").append(ProductionCompletion.STATUS_CANCELLED);
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
        if (UretimDocs.completableOrders(orderDao).isEmpty()) {
            UretimDates.showError("İş bitti", "Açık emir yok",
                    "Onaylı ve kalan miktarı olan üretim emri yok.");
            return;
        }
        UretimWindows.openCompletion(null, this::refreshView);
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        ProductionCompletion selected = tblvCompletionView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getRowId() == null) {
            return;
        }
        if (selected.isCancelled()) {
            if (!UretimDates.confirm("İş bitiş sil", "İptal edilmiş iş bitiş kaydı silinecek.")) {
                return;
            }
            UretimDocs.deleteCompletion(completionDao, completionMaterialDao, selected.getRowId());
            refreshView();
            return;
        }
        if (!selected.isPosted()) {
            return;
        }
        Alert alert = new Alert(AlertType.CONFIRMATION,
                "Seçilen iş bitiş iptal edilecek.",
                ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }
            try {
                IStockLedger ledger = TerpApplication.getInstance().getStockLedger();
                if (ledger != null) {
                    ledger.reverseByDocument(StockPosting.SOURCE_PRODUCTION_COMPLETION, selected.getRowId());
                }
                selected.setStatus(ProductionCompletion.STATUS_CANCELLED);
                selected.setLastUpdateDate(new Date());
                completionDao.addOrUpdate(selected);
                ProductionOrder order = orderDao.firstOrDefault(selected.getOrderId());
                UretimDocs.syncStatus(orderDao, completionDao, order);
                refreshView();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, null, ex);
                UretimDates.showError("İş bitti", "İptal edilemedi",
                        ex.getMessage() == null ? "İşlem başarısız." : ex.getMessage());
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.orderDao = TerpApplication.getInstance().getPersistence().createDao(ProductionOrder.class);
        this.completionDao = TerpApplication.getInstance().getPersistence()
                .createDao(ProductionCompletion.class);
        this.completionMaterialDao = TerpApplication.getInstance().getPersistence()
                .createDao(ProductionCompletionMaterial.class);
        IIconFactory icons = TerpApplication.getInstance().getIconFactory();
        if (icons != null) {
            this.btnSearch.setGraphic(icons.getIcon("SEARCH"));
            this.btnAdd.setGraphic(icons.getIcon("PLUS"));
            this.btnDelete.setGraphic(icons.getIcon("SEARCH"));
        }
        cmbSearchStatus.getItems().addAll(ALL_STATUS, "İşlendi", "İptal");
        cmbSearchStatus.getSelectionModel().select(ALL_STATUS);
        this.tcDateLabel.setCellValueFactory(new PropertyValueFactory<>("dateLabel"));
        this.tcOrderDocumentNo.setCellValueFactory(new PropertyValueFactory<>("orderDocumentNo"));
        this.tcProductCode.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        this.tcProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        this.tcProducedQty.setCellValueFactory(new PropertyValueFactory<>("producedQty"));
        NumericCells.apply(this.tcProducedQty);
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        this.tcLineNo.setCellValueFactory(new PropertyValueFactory<>("lineNo"));
        this.tcLineItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcLineItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcLineItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcLineQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        NumericCells.apply(this.tcLineQuantity);
        this.tblvLineView.setPlaceholder(new Label("Sarf satırı yok"));
        this.pgnCompletionData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvCompletionView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        this.searchSqlStatement = CompanyScope.from("ProductionCompletion");
        this.btnAdd.setDisable(!rights.add);
        updateButtons(true);
        refreshView();
        if (!tblvCompletionView.getItems().isEmpty()) {
            tblvCompletionView.getSelectionModel().select(0);
        }
    }

    private void showLines(ProductionCompletion row) {
        tblvLineView.setItems(FXCollections.observableArrayList(
                UretimDocs.completionMaterialsOf(completionMaterialDao,
                        row == null ? null : row.getRowId())));
    }

    private void refreshView() {
        Long selectedId = selectedRowId();
        long count = completionDao.getRecordCount(scopedQuery());
        int pages = (int) (count / rowsPerPage + 1);
        this.pgnCompletionData.setPageCount(Math.max(1, pages));
        this.tblvCompletionView.setItems(currentPage());
        restoreSelection(selectedId);
        showLines(tblvCompletionView.getSelectionModel().getSelectedItem());
    }

    private Long selectedRowId() {
        ProductionCompletion selected = tblvCompletionView.getSelectionModel().getSelectedItem();
        return selected == null ? null : selected.getRowId();
    }

    private ProductionCompletion findOnPage(Long rowId) {
        if (rowId == null) {
            return null;
        }
        for (ProductionCompletion row : tblvCompletionView.getItems()) {
            if (row != null && rowId.equals(row.getRowId())) {
                return row;
            }
        }
        return null;
    }

    private void restoreSelection(Long rowId) {
        ProductionCompletion match = findOnPage(rowId);
        if (match == null) {
            return;
        }
        syncingSelection = true;
        tblvCompletionView.getSelectionModel().select(match);
        syncingSelection = false;
    }

    private ObservableList<ProductionCompletion> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<ProductionCompletion> rows = completionDao.findPage(pageNum, rowsPerPage, scopedQuery());
        if (rows == null) {
            rows = List.of();
        }
        UretimDocs.decorateCompletions(orderDao, rows);
        return FXCollections.observableArrayList(rows);
    }

    private String scopedQuery() {
        if (searchSqlStatement == null || searchSqlStatement.isBlank()) {
            return CompanyScope.from("ProductionCompletion");
        }
        return searchSqlStatement;
    }

    private void selectionChanged(Change<? extends ProductionCompletion> change) {
        int size = change.getList().size();
        if (size == 0) {
            updateButtons(true);
            if (!syncingSelection) {
                showLines(null);
            }
            return;
        }
        ProductionCompletion selected = change.getList().get(0);
        boolean deleteDisabled = size != 1 || selected == null
                || (selected.isPosted() && !rights.edit)
                || (selected.isCancelled() && !rights.delete);
        updateButtons(deleteDisabled);
        if (!syncingSelection && size == 1) {
            showLines(selected);
        }
    }

    private void updateButtons(boolean deleteDisabled) {
        this.btnDelete.setDisable(deleteDisabled);
    }

    private static void appendOrderFilter(StringBuilder sql, String field, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        sql.append(" and e.orderId in (select o.rowId from ProductionOrder o where ")
                .append(CompanyScope.predicate("o"))
                .append(" and ").append(field).append(" like '")
                .append(value.replace("'", "''")).append("')");
    }
}

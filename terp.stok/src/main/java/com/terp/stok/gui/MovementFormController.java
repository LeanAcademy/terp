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
package com.terp.stok.gui;

import com.terp.plugin.CompanyScope;
import com.terp.plugin.FormRights;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.gui.IIconFactory;
import com.terp.stok.data.StockMovement;
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

public class MovementFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(MovementFormController.class.getName());
    private static final int DEFAULT_ROWS_PER_PAGE = 20;
    private static final String ALL_TYPES = "Tümü";

    @FXML
    private Button btnSearch;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;
    @FXML
    private TextField txtItemCode;
    @FXML
    private TextField txtWarehouseCode;
    @FXML
    private ComboBox<String> cmbSearchType;
    @FXML
    private TextField txtDocumentNo;
    @FXML
    private TableView<StockMovement> tblvMovementView;
    @FXML
    private Pagination pgnMovementData;
    @FXML
    private TableColumn<StockMovement, String> tcDateLabel;
    @FXML
    private TableColumn<StockMovement, String> tcTypeLabel;
    @FXML
    private TableColumn<StockMovement, String> tcReasonName;
    @FXML
    private TableColumn<StockMovement, String> tcWarehouseCode;
    @FXML
    private TableColumn<StockMovement, String> tcTargetWarehouseCode;
    @FXML
    private TableColumn<StockMovement, String> tcItemCode;
    @FXML
    private TableColumn<StockMovement, String> tcItemDesc;
    @FXML
    private TableColumn<StockMovement, Double> tcQuantity;
    @FXML
    private TableColumn<StockMovement, String> tcItemUnit;
    @FXML
    private TableColumn<StockMovement, String> tcDocumentNo;

    private ICommonDao<StockMovement> movementDao;
    private FormRights rights = FormRights.forMenu("STK04");
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from StockMovement e");
        boolean whereAdded = CompanyScope.append(sql, false);
        whereAdded = appendLike(sql, whereAdded, "e.itemCode", txtItemCode.getText());
        whereAdded = appendLike(sql, whereAdded, "e.warehouseCode", txtWarehouseCode.getText());
        whereAdded = appendLike(sql, whereAdded, "e.documentNo", txtDocumentNo.getText());
        String type = cmbSearchType.getValue();
        if (type != null && !type.isBlank() && !ALL_TYPES.equals(type)) {
            int index = java.util.Arrays.asList(StockMovement.TYPE_LABELS).indexOf(type);
            if (index >= 0) {
                if (!whereAdded) {
                    sql.append(" where");
                } else {
                    sql.append(" and");
                }
                sql.append(" e.movementType = ").append(index);
            }
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
        StockMovement selected = tblvMovementView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (selected.isDocumentPosted()) {
            Alert blocked = new Alert(AlertType.WARNING);
            blocked.setTitle("Belge hareketi");
            blocked.setHeaderText("Bu satır bir belgeden geldi");
            blocked.setContentText("Mal kabul belgesinden düzeltin veya iptal edin.");
            blocked.show();
            return;
        }
        openEditor(selected);
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        StockMovement selected = tblvMovementView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Alert alert = new Alert(AlertType.CONFIRMATION,
                "Seçilen stok işlemini sil: " + selected.getTypeLabel() + " / " + selected.getItemCode(),
                ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }
            try {
                if (selected.isDocumentPosted()) {
                    Alert blocked = new Alert(AlertType.ERROR);
                    blocked.setTitle("Silinemez");
                    blocked.setHeaderText("Bu satır bir belgeden geldi");
                    blocked.setContentText("Mal kabul belgesini iptal edin.");
                    blocked.show();
                    return;
                }
                movementDao.delete(selected.getRowId());
                refreshView();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.movementDao = TerpApplication.getInstance().getPersistence().createDao(StockMovement.class);
        IIconFactory icons = TerpApplication.getInstance().getIconFactory();
        if (icons != null) {
            this.btnSearch.setGraphic(icons.getIcon("SEARCH"));
            this.btnAdd.setGraphic(icons.getIcon("PLUS"));
            this.btnEdit.setGraphic(icons.getIcon("EDIT"));
            this.btnDelete.setGraphic(icons.getIcon("SEARCH"));
        }
        cmbSearchType.getItems().add(ALL_TYPES);
        cmbSearchType.getItems().addAll(StockMovement.TYPE_LABELS);
        cmbSearchType.getSelectionModel().select(ALL_TYPES);
        this.tcDateLabel.setCellValueFactory(new PropertyValueFactory<>("dateLabel"));
        this.tcTypeLabel.setCellValueFactory(new PropertyValueFactory<>("typeLabel"));
        this.tcReasonName.setCellValueFactory(new PropertyValueFactory<>("reasonName"));
        this.tcWarehouseCode.setCellValueFactory(new PropertyValueFactory<>("warehouseCode"));
        this.tcTargetWarehouseCode.setCellValueFactory(new PropertyValueFactory<>("targetWarehouseCode"));
        this.tcItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        this.tcItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcDocumentNo.setCellValueFactory(new PropertyValueFactory<>("documentNo"));
        this.pgnMovementData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvMovementView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        this.searchSqlStatement = CompanyScope.from("StockMovement");
        this.btnAdd.setDisable(!rights.add);
        updateButtons(true, true);
        refreshView();
    }

    private void openEditor(StockMovement current) {
        try {
            FXMLLoader loader = pluginLoader("/fxml/MovementEditForm.fxml");
            Node node = loader.load();
            MovementEditFormController controller = loader.getController();
            controller.initializeForm(current);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(new Scene((Parent) node));
            stage.setTitle(current == null ? "Yeni stok işlemi" : "Stok işlemi");
            stage.showAndWait();
            refreshView();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void refreshView() {
        String hql = scopedQuery();
        long count = movementDao.getRecordCount(hql);
        int pages = (int) (count / rowsPerPage + 1);
        this.pgnMovementData.setPageCount(Math.max(1, pages));
        this.tblvMovementView.setItems(currentPage());
    }

    private ObservableList<StockMovement> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<StockMovement> rows = movementDao.findPage(pageNum, rowsPerPage, scopedQuery());
        return FXCollections.observableArrayList(rows == null ? List.of() : rows);
    }

    private String scopedQuery() {
        if (searchSqlStatement == null || searchSqlStatement.isBlank()) {
            return CompanyScope.from("StockMovement");
        }
        return searchSqlStatement;
    }

    private void selectionChanged(Change<? extends StockMovement> change) {
        int size = change.getList().size();
        if (size == 0) {
            updateButtons(true, true);
        } else if (size == 1) {
            updateButtons(false, false);
        } else {
            updateButtons(true, false);
        }
    }

    private void updateButtons(boolean editDisabled, boolean deleteDisabled) {
        this.btnEdit.setDisable(editDisabled || !rights.edit);
        this.btnDelete.setDisable(deleteDisabled || !rights.delete);
    }

    private static boolean appendLike(StringBuilder sql, boolean whereAdded, String field, String value) {
        if (value == null || value.isBlank()) {
            return whereAdded;
        }
        if (!whereAdded) {
            sql.append(" where");
        } else {
            sql.append(" and");
        }
        sql.append(' ').append(field).append(" like '")
                .append(value.replace("'", "''")).append('\'');
        return true;
    }

    private static FXMLLoader pluginLoader(String fxml) {
        FXMLLoader loader = new FXMLLoader(MovementFormController.class.getResource(fxml));
        loader.setClassLoader(MovementFormController.class.getClassLoader());
        return loader;
    }
}

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

import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.gui.IIconFactory;
import com.terp.stok.data.StockMovement;
import com.terp.stok.data.Warehouse;
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

public class WarehouseFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(WarehouseFormController.class.getName());
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
    private TextField txtWarehouseCode;
    @FXML
    private TextField txtWarehouseName;
    @FXML
    private ComboBox<String> cmbSearchType;
    @FXML
    private TableView<Warehouse> tblvWarehouseView;
    @FXML
    private Pagination pgnWarehouseData;
    @FXML
    private TableColumn<Warehouse, String> tcWarehouseCode;
    @FXML
    private TableColumn<Warehouse, String> tcWarehouseName;
    @FXML
    private TableColumn<Warehouse, String> tcTypeLabel;
    @FXML
    private TableColumn<Warehouse, String> tcCity;
    @FXML
    private TableColumn<Warehouse, String> tcStatusLabel;

    private ICommonDao<Warehouse> warehouseDao;
    private ICommonDao<StockMovement> movementDao;
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from Warehouse e");
        boolean whereAdded = false;
        whereAdded = appendLike(sql, whereAdded, "e.warehouseCode", txtWarehouseCode.getText());
        whereAdded = appendLike(sql, whereAdded, "e.warehouseName", txtWarehouseName.getText());
        String type = cmbSearchType.getValue();
        if (type != null && !type.isBlank() && !ALL_TYPES.equals(type)) {
            int index = java.util.Arrays.asList(Warehouse.TYPE_LABELS).indexOf(type);
            if (index >= 0) {
                whereAdded = appendEquals(sql, whereAdded, "e.warehouseType", Integer.toString(index));
            }
        }
        this.searchSqlStatement = sql.toString();
        this.currentPageNum = 1;
        refreshView();
    }

    @FXML
    public void onActionBtnAdd(ActionEvent event) {
        openEditor(null);
    }

    @FXML
    public void onActionBtnEdit(ActionEvent event) {
        Warehouse selected = tblvWarehouseView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openEditor(selected);
        }
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        Warehouse selected = tblvWarehouseView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Alert alert = new Alert(AlertType.CONFIRMATION,
                "Seçilen depoyu sil: " + selected.getWarehouseCode(),
                ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }
            try {
                if (hasMovements(selected.getWarehouseCode())) {
                    Alert blocked = new Alert(AlertType.ERROR);
                    blocked.setTitle("Silinemez");
                    blocked.setHeaderText("Depoda stok hareketi var");
                    blocked.setContentText("Önce ilgili stok işlemlerini silin.");
                    blocked.show();
                    return;
                }
                warehouseDao.delete(selected.getRowId());
                refreshView();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.warehouseDao = TerpApplication.getInstance().getPersistence().createDao(Warehouse.class);
        this.movementDao = TerpApplication.getInstance().getPersistence().createDao(StockMovement.class);
        IIconFactory icons = TerpApplication.getInstance().getIconFactory();
        if (icons != null) {
            this.btnSearch.setGraphic(icons.getIcon("SEARCH"));
            this.btnAdd.setGraphic(icons.getIcon("PLUS"));
            this.btnEdit.setGraphic(icons.getIcon("EDIT"));
            this.btnDelete.setGraphic(icons.getIcon("SEARCH"));
        }
        cmbSearchType.getItems().add(ALL_TYPES);
        cmbSearchType.getItems().addAll(Warehouse.TYPE_LABELS);
        cmbSearchType.getSelectionModel().select(ALL_TYPES);
        this.tcWarehouseCode.setCellValueFactory(new PropertyValueFactory<>("warehouseCode"));
        this.tcWarehouseName.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        this.tcTypeLabel.setCellValueFactory(new PropertyValueFactory<>("typeLabel"));
        this.tcCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        this.pgnWarehouseData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvWarehouseView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        updateButtons(true, true);
        refreshView();
    }

    private boolean hasMovements(String warehouseCode) {
        if (warehouseCode == null || warehouseCode.isBlank()) {
            return false;
        }
        String escaped = warehouseCode.replace("'", "''");
        List<StockMovement> rows = movementDao.findAll(
                "from StockMovement e where e.warehouseCode = '" + escaped
                        + "' or e.targetWarehouseCode = '" + escaped + "'");
        return rows != null && !rows.isEmpty();
    }

    private void openEditor(Warehouse current) {
        try {
            FXMLLoader loader = pluginLoader("/fxml/WarehouseEditForm.fxml");
            Node node = loader.load();
            WarehouseEditFormController controller = loader.getController();
            controller.initializeForm(current);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(new Scene((Parent) node));
            stage.setTitle(current == null ? "Yeni depo" : "Depo tanımı");
            stage.showAndWait();
            refreshView();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void refreshView() {
        long count = warehouseDao.getRecordCount();
        int pages = (int) (count / rowsPerPage + 1);
        this.pgnWarehouseData.setPageCount(Math.max(1, pages));
        this.tblvWarehouseView.setItems(currentPage());
    }

    private ObservableList<Warehouse> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<Warehouse> rows;
        if (searchSqlStatement != null && !searchSqlStatement.isEmpty()
                && !"from Warehouse e".equals(searchSqlStatement)) {
            rows = warehouseDao.findPage(pageNum, rowsPerPage, searchSqlStatement);
        } else {
            rows = warehouseDao.findPage(pageNum, rowsPerPage);
        }
        return FXCollections.observableArrayList(rows == null ? List.of() : rows);
    }

    private void selectionChanged(Change<? extends Warehouse> change) {
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
        this.btnEdit.setDisable(editDisabled);
        this.btnDelete.setDisable(deleteDisabled);
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

    private static boolean appendEquals(StringBuilder sql, boolean whereAdded, String field, String value) {
        if (!whereAdded) {
            sql.append(" where");
        } else {
            sql.append(" and");
        }
        sql.append(' ').append(field).append(" = ").append(value);
        return true;
    }

    private static FXMLLoader pluginLoader(String fxml) {
        FXMLLoader loader = new FXMLLoader(WarehouseFormController.class.getResource(fxml));
        loader.setClassLoader(WarehouseFormController.class.getClassLoader());
        return loader;
    }
}

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
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.gui.IIconFactory;
import com.terp.stok.data.Item;
import com.terp.stok.data.StockBalanceRow;
import com.terp.stok.data.StockBalances;
import com.terp.stok.data.StockMovement;
import com.terp.stok.data.Warehouse;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class StockStatusFormController implements Initializable {

    private static final String ALL_WAREHOUSES = "Tümü";

    @FXML
    private Button btnRefresh;
    @FXML
    private ComboBox<String> cmbWarehouse;
    @FXML
    private TextField txtItemCode;
    @FXML
    private CheckBox chkOnlyNonZero;
    @FXML
    private CheckBox chkOnlyBelowMin;
    @FXML
    private TableView<StockBalanceRow> tblvStatusView;
    @FXML
    private TableColumn<StockBalanceRow, String> tcWarehouseCode;
    @FXML
    private TableColumn<StockBalanceRow, String> tcWarehouseName;
    @FXML
    private TableColumn<StockBalanceRow, String> tcItemCode;
    @FXML
    private TableColumn<StockBalanceRow, String> tcItemDesc;
    @FXML
    private TableColumn<StockBalanceRow, String> tcItemUnit;
    @FXML
    private TableColumn<StockBalanceRow, Double> tcQuantity;
    @FXML
    private TableColumn<StockBalanceRow, Double> tcMinStock;
    @FXML
    private TableColumn<StockBalanceRow, Double> tcMaxStock;
    @FXML
    private TableColumn<StockBalanceRow, String> tcStatusLabel;

    private ICommonDao<Warehouse> warehouseDao;
    private ICommonDao<Item> itemDao;
    private ICommonDao<StockMovement> movementDao;

    @FXML
    public void onActionBtnRefresh(ActionEvent event) {
        refreshView();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.warehouseDao = TerpApplication.getInstance().getPersistence().createDao(Warehouse.class);
        this.itemDao = TerpApplication.getInstance().getPersistence().createDao(Item.class);
        this.movementDao = TerpApplication.getInstance().getPersistence().createDao(StockMovement.class);
        IIconFactory icons = TerpApplication.getInstance().getIconFactory();
        if (icons != null) {
            try {
                this.btnRefresh.setGraphic(icons.getIcon("REFRESH"));
            } catch (RuntimeException ignored) {
                // keep text-only if the glyph name is unknown
            }
        }
        this.tcWarehouseCode.setCellValueFactory(new PropertyValueFactory<>("warehouseCode"));
        this.tcWarehouseName.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        this.tcItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        this.tcMinStock.setCellValueFactory(new PropertyValueFactory<>("minStock"));
        this.tcMaxStock.setCellValueFactory(new PropertyValueFactory<>("maxStock"));
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        fillWarehouses();
        refreshView();
    }

    private void fillWarehouses() {
        List<String> labels = new ArrayList<>();
        labels.add(ALL_WAREHOUSES);
        List<Warehouse> warehouses = warehouseDao.findAll(CompanyScope.from("Warehouse"));
        if (warehouses != null) {
            for (Warehouse warehouse : warehouses) {
                if (warehouse != null && warehouse.getStatus() == 0) {
                    labels.add(warehouse.getDisplayLabel());
                }
            }
        }
        StockCombos.fill(cmbWarehouse, labels);
        cmbWarehouse.getSelectionModel().select(ALL_WAREHOUSES);
    }

    private void refreshView() {
        List<Warehouse> warehouses = warehouseDao.findAll(CompanyScope.from("Warehouse"));
        List<Item> items = itemDao.findAll(CompanyScope.from("Item"));
        List<StockMovement> movements = movementDao.findAll(CompanyScope.from("StockMovement"));
        boolean onlyNonZero = chkOnlyNonZero != null && chkOnlyNonZero.isSelected();
        boolean onlyBelowMin = chkOnlyBelowMin != null && chkOnlyBelowMin.isSelected();
        List<StockBalanceRow> rows = StockBalances.report(
                warehouses == null ? List.of() : warehouses,
                items == null ? List.of() : items,
                movements,
                onlyNonZero,
                onlyBelowMin);
        String warehouseCode = selectedWarehouseCode();
        String itemFilter = txtItemCode == null || txtItemCode.getText() == null
                ? "" : txtItemCode.getText().trim().toLowerCase();
        List<StockBalanceRow> filtered = new ArrayList<>();
        for (StockBalanceRow row : rows) {
            if (warehouseCode != null && !warehouseCode.equals(row.getWarehouseCode())) {
                continue;
            }
            if (!itemFilter.isEmpty() && !matchesItem(row, itemFilter)) {
                continue;
            }
            filtered.add(row);
        }
        tblvStatusView.setItems(FXCollections.observableArrayList(filtered));
    }

    private String selectedWarehouseCode() {
        String code = StockCombos.codeOf(cmbWarehouse);
        if (code == null || ALL_WAREHOUSES.equals(code)) {
            return null;
        }
        return code;
    }

    private static boolean matchesItem(StockBalanceRow row, String filter) {
        String code = row.getItemCode() == null ? "" : row.getItemCode().toLowerCase();
        String desc = row.getItemDesc() == null ? "" : row.getItemDesc().toLowerCase();
        return code.contains(filter) || desc.contains(filter);
    }
}

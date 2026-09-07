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
import com.terp.stok.data.Item;
import com.terp.stok.data.ItemPartnerCode;
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

public class ItemFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ItemFormController.class.getName());
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
    private TextField txtItemId;
    @FXML
    private TextField txtItemDesc;
    @FXML
    private TextField txtItemGroup;
    @FXML
    private TextField txtBarcode;
    @FXML
    private ComboBox<String> cmbSearchType;
    @FXML
    private TableView<Item> tblvItemView;
    @FXML
    private Pagination pgnItemData;
    @FXML
    private TableColumn<Item, String> tcItemId;
    @FXML
    private TableColumn<Item, String> tcItemDesc;
    @FXML
    private TableColumn<Item, String> tcTypeLabel;
    @FXML
    private TableColumn<Item, String> tcItemGroup;
    @FXML
    private TableColumn<Item, String> tcItemUnit;
    @FXML
    private TableColumn<Item, String> tcBarcode;
    @FXML
    private TableColumn<Item, Double> tcVatRate;
    @FXML
    private TableColumn<Item, Double> tcSalesPrice;
    @FXML
    private TableColumn<Item, String> tcStatusLabel;

    private ICommonDao<Item> itemDao;
    private ICommonDao<ItemPartnerCode> partnerCodeDao;
    private ICommonDao<StockMovement> movementDao;
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from Item e");
        boolean whereAdded = false;
        whereAdded = appendLike(sql, whereAdded, "e.itemId", txtItemId.getText());
        whereAdded = appendLike(sql, whereAdded, "e.itemDesc", txtItemDesc.getText());
        whereAdded = appendLike(sql, whereAdded, "e.itemGroup", txtItemGroup.getText());
        whereAdded = appendLike(sql, whereAdded, "e.barcode", txtBarcode.getText());
        String type = cmbSearchType.getValue();
        if (type != null && !type.isBlank() && !ALL_TYPES.equals(type)) {
            int index = java.util.Arrays.asList(Item.TYPE_LABELS).indexOf(type);
            if (index >= 0) {
                if (!whereAdded) {
                    sql.append(" where");
                } else {
                    sql.append(" and");
                }
                sql.append(" e.itemType = ").append(index);
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
        Item selected = tblvItemView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        openEditor(selected);
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        Item selected = tblvItemView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Alert alert = new Alert(AlertType.CONFIRMATION,
                "Seçilen kaydı sil: " + selected.getItemId(),
                ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }
            try {
                if (hasMovements(selected.getItemId())) {
                    Alert blocked = new Alert(AlertType.ERROR);
                    blocked.setTitle("Silinemez");
                    blocked.setHeaderText("Malzemede stok hareketi var");
                    blocked.setContentText("Önce ilgili stok işlemlerini silin.");
                    blocked.show();
                    return;
                }
                if (selected.getRowId() != null) {
                    List<ItemPartnerCode> codes = partnerCodeDao.findAll(
                            "from ItemPartnerCode e where e.itemRowId = " + selected.getRowId());
                    if (codes != null) {
                        for (ItemPartnerCode code : codes) {
                            if (code.getRowId() != null) {
                                partnerCodeDao.delete(code.getRowId());
                            }
                        }
                    }
                }
                itemDao.delete(selected.getRowId());
                refreshView();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.itemDao = TerpApplication.getInstance().getPersistence().createDao(Item.class);
        this.partnerCodeDao = TerpApplication.getInstance().getPersistence().createDao(ItemPartnerCode.class);
        this.movementDao = TerpApplication.getInstance().getPersistence().createDao(StockMovement.class);
        IIconFactory icons = TerpApplication.getInstance().getIconFactory();
        if (icons != null) {
            this.btnSearch.setGraphic(icons.getIcon("SEARCH"));
            this.btnAdd.setGraphic(icons.getIcon("PLUS"));
            this.btnEdit.setGraphic(icons.getIcon("EDIT"));
            this.btnDelete.setGraphic(icons.getIcon("SEARCH"));
        }

        cmbSearchType.getItems().add(ALL_TYPES);
        cmbSearchType.getItems().addAll(Item.TYPE_LABELS);
        cmbSearchType.getSelectionModel().select(ALL_TYPES);

        this.tcItemId.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        this.tcItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcTypeLabel.setCellValueFactory(new PropertyValueFactory<>("typeLabel"));
        this.tcItemGroup.setCellValueFactory(new PropertyValueFactory<>("itemGroup"));
        this.tcItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        this.tcVatRate.setCellValueFactory(new PropertyValueFactory<>("vatRate"));
        this.tcSalesPrice.setCellValueFactory(new PropertyValueFactory<>("salesPrice"));
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));

        this.pgnItemData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvItemView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        updateButtons(true, true);
        refreshView();
    }

    private boolean hasMovements(String itemCode) {
        if (itemCode == null || itemCode.isBlank()) {
            return false;
        }
        String escaped = itemCode.replace("'", "''");
        List<StockMovement> rows = movementDao.findAll(
                "from StockMovement e where e.itemCode = '" + escaped + "'");
        return rows != null && !rows.isEmpty();
    }

    private void openEditor(Item current) {
        try {
            FXMLLoader loader = pluginLoader("/fxml/ItemEditForm.fxml");
            Node node = loader.load();
            ItemEditFormController controller = loader.getController();
            controller.initializeForm(current);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(new Scene((Parent) node));
            stage.setTitle(current == null ? "Yeni malzeme" : "Malzeme kartı");
            stage.showAndWait();
            refreshView();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void refreshView() {
        long count = itemDao.getRecordCount();
        int pages = (int) (count / rowsPerPage + 1);
        this.pgnItemData.setPageCount(Math.max(1, pages));
        this.tblvItemView.setItems(currentPage());
    }

    private ObservableList<Item> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<Item> rows;
        if (searchSqlStatement != null && !searchSqlStatement.isEmpty()
                && !"from Item e".equals(searchSqlStatement)) {
            rows = itemDao.findPage(pageNum, rowsPerPage, searchSqlStatement);
        } else {
            rows = itemDao.findPage(pageNum, rowsPerPage);
        }
        return FXCollections.observableArrayList(rows == null ? List.of() : rows);
    }

    private void selectionChanged(Change<? extends Item> change) {
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

    private static FXMLLoader pluginLoader(String fxml) {
        FXMLLoader loader = new FXMLLoader(ItemFormController.class.getResource(fxml));
        loader.setClassLoader(ItemFormController.class.getClassLoader());
        return loader;
    }
}

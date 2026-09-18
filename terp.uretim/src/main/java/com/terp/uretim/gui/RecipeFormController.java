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
import com.terp.uretim.data.Recipe;
import com.terp.uretim.data.RecipeLine;
import com.terp.uretim.data.UretimDocs;
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
import com.terp.plugin.gui.NumericCells;

public class RecipeFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(RecipeFormController.class.getName());
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
    private TextField txtProductCode;
    @FXML
    private TextField txtProductName;
    @FXML
    private ComboBox<String> cmbSearchStatus;
    @FXML
    private TableView<Recipe> tblvRecipeView;
    @FXML
    private Pagination pgnRecipeData;
    @FXML
    private TableColumn<Recipe, String> tcProductCode;
    @FXML
    private TableColumn<Recipe, String> tcProductName;
    @FXML
    private TableColumn<Recipe, String> tcProductUnit;
    @FXML
    private TableColumn<Recipe, Double> tcYieldQuantity;
    @FXML
    private TableColumn<Recipe, String> tcStatusLabel;
    @FXML
    private TableView<RecipeLine> tblvLineView;
    @FXML
    private TableColumn<RecipeLine, Integer> tcLineNo;
    @FXML
    private TableColumn<RecipeLine, String> tcLineItemCode;
    @FXML
    private TableColumn<RecipeLine, String> tcLineItemDesc;
    @FXML
    private TableColumn<RecipeLine, String> tcLineItemUnit;
    @FXML
    private TableColumn<RecipeLine, Double> tcLineQuantity;

    private ICommonDao<Recipe> recipeDao;
    private ICommonDao<RecipeLine> lineDao;
    private FormRights rights = FormRights.forMenu("URT02");
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";
    private boolean syncingSelection;

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from Recipe e");
        CompanyScope.append(sql, false);
        UretimDates.appendLike(sql, true, "e.productCode", txtProductCode.getText());
        UretimDates.appendLike(sql, true, "e.productName", txtProductName.getText());
        String status = cmbSearchStatus.getValue();
        if ("Aktif".equals(status)) {
            sql.append(" and e.status = ").append(Recipe.STATUS_ACTIVE);
        } else if ("Pasif".equals(status)) {
            sql.append(" and e.status = ").append(Recipe.STATUS_INACTIVE);
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
        UretimWindows.openRecipeEditor(null, this::refreshView);
    }

    @FXML
    public void onActionBtnEdit(ActionEvent event) {
        Recipe selected = tblvRecipeView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            UretimWindows.openRecipeEditor(selected, this::refreshView);
        }
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        Recipe selected = tblvRecipeView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Alert alert = new Alert(AlertType.CONFIRMATION,
                "Seçilen reçete silinecek: " + selected.getProductCode(),
                ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }
            try {
                UretimDocs.deleteByParent(lineDao, "RecipeLine", "recipeId", selected.getRowId());
                recipeDao.delete(selected.getRowId());
                refreshView();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.recipeDao = TerpApplication.getInstance().getPersistence().createDao(Recipe.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(RecipeLine.class);
        IIconFactory icons = TerpApplication.getInstance().getIconFactory();
        if (icons != null) {
            this.btnSearch.setGraphic(icons.getIcon("SEARCH"));
            this.btnAdd.setGraphic(icons.getIcon("PLUS"));
            this.btnEdit.setGraphic(icons.getIcon("EDIT"));
            this.btnDelete.setGraphic(icons.getIcon("SEARCH"));
        }
        cmbSearchStatus.getItems().addAll(ALL_STATUS, "Aktif", "Pasif");
        cmbSearchStatus.getSelectionModel().select(ALL_STATUS);
        this.tcProductCode.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        this.tcProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        this.tcProductUnit.setCellValueFactory(new PropertyValueFactory<>("productUnit"));
        this.tcYieldQuantity.setCellValueFactory(new PropertyValueFactory<>("yieldQuantity"));
        NumericCells.apply(this.tcYieldQuantity);
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        this.tcLineNo.setCellValueFactory(new PropertyValueFactory<>("lineNo"));
        this.tcLineItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcLineItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcLineItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcLineQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        NumericCells.apply(this.tcLineQuantity);
        this.tblvLineView.setPlaceholder(new Label("Reçete satırı yok"));
        this.pgnRecipeData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvRecipeView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        this.searchSqlStatement = CompanyScope.from("Recipe");
        this.btnAdd.setDisable(!rights.add);
        updateButtons(true, true);
        refreshView();
        if (!tblvRecipeView.getItems().isEmpty()) {
            tblvRecipeView.getSelectionModel().select(0);
        }
    }

    private void showLines(Recipe row) {
        if (row == null || row.getRowId() == null) {
            tblvLineView.setItems(FXCollections.observableArrayList());
            return;
        }
        List<RecipeLine> stored = lineDao.findAll(
                "from RecipeLine e where e.recipeId = " + row.getRowId()
                        + " order by e.lineNo");
        tblvLineView.setItems(FXCollections.observableArrayList(stored == null ? List.of() : stored));
    }

    private void refreshView() {
        Long selectedId = selectedRowId();
        String hql = scopedQuery();
        long count = recipeDao.getRecordCount(hql);
        int pages = (int) (count / rowsPerPage + 1);
        this.pgnRecipeData.setPageCount(Math.max(1, pages));
        this.tblvRecipeView.setItems(currentPage());
        restoreSelection(selectedId);
        showLines(tblvRecipeView.getSelectionModel().getSelectedItem());
    }

    private Long selectedRowId() {
        Recipe selected = tblvRecipeView.getSelectionModel().getSelectedItem();
        return selected == null ? null : selected.getRowId();
    }

    private Recipe findOnPage(Long rowId) {
        if (rowId == null) {
            return null;
        }
        for (Recipe row : tblvRecipeView.getItems()) {
            if (row != null && rowId.equals(row.getRowId())) {
                return row;
            }
        }
        return null;
    }

    private void restoreSelection(Long rowId) {
        Recipe match = findOnPage(rowId);
        if (match == null) {
            return;
        }
        syncingSelection = true;
        tblvRecipeView.getSelectionModel().select(match);
        syncingSelection = false;
    }

    private ObservableList<Recipe> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<Recipe> rows = recipeDao.findPage(pageNum, rowsPerPage, scopedQuery());
        return FXCollections.observableArrayList(rows == null ? List.of() : rows);
    }

    private String scopedQuery() {
        if (searchSqlStatement == null || searchSqlStatement.isBlank()) {
            return CompanyScope.from("Recipe");
        }
        return searchSqlStatement;
    }

    private void selectionChanged(Change<? extends Recipe> change) {
        int size = change.getList().size();
        if (size == 0) {
            updateButtons(true, true);
            if (!syncingSelection) {
                showLines(null);
            }
            return;
        }
        updateButtons(size != 1, size == 0);
        if (!syncingSelection && size == 1) {
            showLines(change.getList().get(0));
        }
    }

    private void updateButtons(boolean editDisabled, boolean deleteDisabled) {
        this.btnEdit.setDisable(editDisabled || !rights.edit);
        this.btnDelete.setDisable(deleteDisabled || !rights.delete);
    }
}

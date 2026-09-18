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
package com.terp.core.gui;

import com.terp.plugin.CompanyScope;
import com.terp.plugin.FormRights;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.gui.IIconFactory;
import com.terp.core.data.UnitOfMeasure;
import com.terp.core.data.Units;
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
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class UnitFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(UnitFormController.class.getName());
    private static final int DEFAULT_ROWS_PER_PAGE = 20;

    @FXML
    private Button btnSearch;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;
    @FXML
    private TextField txtUnitCode;
    @FXML
    private TextField txtUnitName;
    @FXML
    private TableView<UnitOfMeasure> tblvUnitView;
    @FXML
    private Pagination pgnUnitData;
    @FXML
    private TableColumn<UnitOfMeasure, String> tcUnitCode;
    @FXML
    private TableColumn<UnitOfMeasure, String> tcUnitName;
    @FXML
    private TableColumn<UnitOfMeasure, String> tcStatusLabel;

    private ICommonDao<UnitOfMeasure> unitDao;
    private FormRights rights = FormRights.forMenu("SYS09");
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from UnitOfMeasure e");
        boolean whereAdded = CompanyScope.append(sql, false);
        whereAdded = appendLike(sql, whereAdded, "e.unitCode", txtUnitCode.getText());
        appendLike(sql, whereAdded, "e.unitName", txtUnitName.getText());
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
        UnitOfMeasure selected = tblvUnitView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openEditor(selected);
        }
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        UnitOfMeasure selected = tblvUnitView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Alert alert = new Alert(AlertType.CONFIRMATION,
                "Seçilen ölçü birimini sil: " + selected.getUnitCode(),
                ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }
            try {
                unitDao.delete(selected.getRowId());
                refreshView();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.unitDao = TerpApplication.getInstance().getPersistence().createDao(UnitOfMeasure.class);
        Units.ensureSeeded(unitDao);
        IIconFactory icons = TerpApplication.getInstance().getIconFactory();
        if (icons != null) {
            this.btnSearch.setGraphic(icons.getIcon("SEARCH"));
            this.btnAdd.setGraphic(icons.getIcon("PLUS"));
            this.btnEdit.setGraphic(icons.getIcon("EDIT"));
            this.btnDelete.setGraphic(icons.getIcon("SEARCH"));
        }
        this.tcUnitCode.setCellValueFactory(new PropertyValueFactory<>("unitCode"));
        this.tcUnitName.setCellValueFactory(new PropertyValueFactory<>("unitName"));
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        this.pgnUnitData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvUnitView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        this.searchSqlStatement = CompanyScope.from("UnitOfMeasure");
        this.btnAdd.setDisable(!rights.add);
        updateButtons(true, true);
        refreshView();
    }

    private void openEditor(UnitOfMeasure current) {
        try {
            FXMLLoader loader = pluginLoader("/fxml/UnitEditForm.fxml");
            Node node = loader.load();
            UnitEditFormController controller = loader.getController();
            controller.initializeForm(current);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(new Scene((Parent) node));
            stage.setTitle(current == null ? "Yeni ölçü birimi" : "Ölçü birimi");
            stage.showAndWait();
            refreshView();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void refreshView() {
        String hql = scopedQuery();
        long count = unitDao.getRecordCount(hql);
        int pages = (int) (count / rowsPerPage + 1);
        this.pgnUnitData.setPageCount(Math.max(1, pages));
        this.tblvUnitView.setItems(currentPage());
    }

    private ObservableList<UnitOfMeasure> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<UnitOfMeasure> rows = unitDao.findPage(pageNum, rowsPerPage, scopedQuery());
        return FXCollections.observableArrayList(rows == null ? List.of() : rows);
    }

    private String scopedQuery() {
        if (searchSqlStatement == null || searchSqlStatement.isBlank()) {
            return CompanyScope.from("UnitOfMeasure");
        }
        return searchSqlStatement;
    }

    private void selectionChanged(Change<? extends UnitOfMeasure> change) {
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
        FXMLLoader loader = new FXMLLoader(UnitFormController.class.getResource(fxml));
        loader.setClassLoader(UnitFormController.class.getClassLoader());
        return loader;
    }
}

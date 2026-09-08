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
import com.terp.plugin.data.StockDirection;
import com.terp.plugin.gui.IIconFactory;
import com.terp.stok.data.MovementReason;
import com.terp.stok.data.MovementReasons;
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

public class ReasonFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ReasonFormController.class.getName());
    private static final int DEFAULT_ROWS_PER_PAGE = 20;
    private static final String ALL_DIRECTIONS = "Tümü";

    @FXML
    private Button btnSearch;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;
    @FXML
    private TextField txtReasonCode;
    @FXML
    private TextField txtReasonName;
    @FXML
    private ComboBox<String> cmbSearchDirection;
    @FXML
    private TableView<MovementReason> tblvReasonView;
    @FXML
    private Pagination pgnReasonData;
    @FXML
    private TableColumn<MovementReason, String> tcReasonCode;
    @FXML
    private TableColumn<MovementReason, String> tcReasonName;
    @FXML
    private TableColumn<MovementReason, String> tcDirectionLabel;
    @FXML
    private TableColumn<MovementReason, String> tcStatusLabel;

    private ICommonDao<MovementReason> reasonDao;
    private FormRights rights = FormRights.forMenu("STK06");
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from MovementReason e");
        boolean whereAdded = CompanyScope.append(sql, false);
        whereAdded = appendLike(sql, whereAdded, "e.reasonCode", txtReasonCode.getText());
        whereAdded = appendLike(sql, whereAdded, "e.reasonName", txtReasonName.getText());
        String direction = cmbSearchDirection.getValue();
        if (direction != null && !direction.isBlank() && !ALL_DIRECTIONS.equals(direction)) {
            int index = java.util.Arrays.asList(StockDirection.LABELS).indexOf(direction);
            if (index >= 0) {
                sql.append(" and e.direction = ").append(index);
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
        MovementReason selected = tblvReasonView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openEditor(selected);
        }
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        MovementReason selected = tblvReasonView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Alert alert = new Alert(AlertType.CONFIRMATION,
                "Seçilen nedeni sil: " + selected.getReasonCode(),
                ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }
            try {
                reasonDao.delete(selected.getRowId());
                refreshView();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.reasonDao = TerpApplication.getInstance().getPersistence().createDao(MovementReason.class);
        MovementReasons.ensureSeeded(reasonDao);
        IIconFactory icons = TerpApplication.getInstance().getIconFactory();
        if (icons != null) {
            this.btnSearch.setGraphic(icons.getIcon("SEARCH"));
            this.btnAdd.setGraphic(icons.getIcon("PLUS"));
            this.btnEdit.setGraphic(icons.getIcon("EDIT"));
            this.btnDelete.setGraphic(icons.getIcon("SEARCH"));
        }
        cmbSearchDirection.getItems().add(ALL_DIRECTIONS);
        cmbSearchDirection.getItems().addAll(StockDirection.LABELS);
        cmbSearchDirection.getSelectionModel().select(ALL_DIRECTIONS);
        this.tcReasonCode.setCellValueFactory(new PropertyValueFactory<>("reasonCode"));
        this.tcReasonName.setCellValueFactory(new PropertyValueFactory<>("reasonName"));
        this.tcDirectionLabel.setCellValueFactory(new PropertyValueFactory<>("directionLabel"));
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        this.pgnReasonData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvReasonView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        this.searchSqlStatement = CompanyScope.from("MovementReason");
        this.btnAdd.setDisable(!rights.add);
        updateButtons(true, true);
        refreshView();
    }

    private void openEditor(MovementReason current) {
        try {
            FXMLLoader loader = pluginLoader("/fxml/ReasonEditForm.fxml");
            Node node = loader.load();
            ReasonEditFormController controller = loader.getController();
            controller.initializeForm(current);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(new Scene((Parent) node));
            stage.setTitle(current == null ? "Yeni hareket nedeni" : "Hareket nedeni");
            stage.showAndWait();
            refreshView();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void refreshView() {
        String hql = scopedQuery();
        long count = reasonDao.getRecordCount(hql);
        int pages = (int) (count / rowsPerPage + 1);
        this.pgnReasonData.setPageCount(Math.max(1, pages));
        this.tblvReasonView.setItems(currentPage());
    }

    private ObservableList<MovementReason> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<MovementReason> rows = reasonDao.findPage(pageNum, rowsPerPage, scopedQuery());
        return FXCollections.observableArrayList(rows == null ? List.of() : rows);
    }

    private String scopedQuery() {
        if (searchSqlStatement == null || searchSqlStatement.isBlank()) {
            return CompanyScope.from("MovementReason");
        }
        return searchSqlStatement;
    }

    private void selectionChanged(Change<? extends MovementReason> change) {
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
        FXMLLoader loader = new FXMLLoader(ReasonFormController.class.getResource(fxml));
        loader.setClassLoader(ReasonFormController.class.getClassLoader());
        return loader;
    }
}

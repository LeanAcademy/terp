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
package com.terp.cari.gui;

import com.terp.cari.data.AccountMovement;
import com.terp.plugin.CompanyScope;
import com.terp.plugin.FormRights;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.AccountDirection;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.gui.IIconFactory;
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
    private TextField txtAccountCode;
    @FXML
    private TextField txtDocumentNo;
    @FXML
    private ComboBox<String> cmbSearchType;
    @FXML
    private TableView<AccountMovement> tblvMovementView;
    @FXML
    private Pagination pgnMovementData;
    @FXML
    private TableColumn<AccountMovement, String> tcDateLabel;
    @FXML
    private TableColumn<AccountMovement, String> tcTypeLabel;
    @FXML
    private TableColumn<AccountMovement, String> tcAccountCode;
    @FXML
    private TableColumn<AccountMovement, String> tcAccountName;
    @FXML
    private TableColumn<AccountMovement, Double> tcAmount;
    @FXML
    private TableColumn<AccountMovement, String> tcDocumentNo;
    @FXML
    private TableColumn<AccountMovement, String> tcSourceLabel;

    private ICommonDao<AccountMovement> movementDao;
    private FormRights rights = FormRights.forMenu("CRI03");
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from AccountMovement e");
        boolean whereAdded = CompanyScope.append(sql, false);
        whereAdded = CariDates.appendLike(sql, whereAdded, "e.accountCode", txtAccountCode.getText());
        whereAdded = CariDates.appendLike(sql, whereAdded, "e.documentNo", txtDocumentNo.getText());
        String type = cmbSearchType.getValue();
        if (type != null && !type.isBlank() && !ALL_TYPES.equals(type)) {
            int index = java.util.Arrays.asList(AccountDirection.LABELS).indexOf(type);
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
        AccountMovement selected = tblvMovementView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (selected.isDocumentPosted()) {
            Alert blocked = new Alert(AlertType.WARNING);
            blocked.setTitle("Belge hareketi");
            blocked.setHeaderText("Bu satır bir belgeden geldi");
            blocked.setContentText("Kaynak belgeden düzeltin veya iptal edin.");
            blocked.show();
            return;
        }
        openEditor(selected);
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        AccountMovement selected = tblvMovementView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Alert alert = new Alert(AlertType.CONFIRMATION,
                "Seçilen cari hareketi sil: " + selected.getTypeLabel() + " / " + selected.getAccountCode(),
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
                    blocked.setContentText("Kaynak belgeyi iptal edin.");
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
        this.movementDao = TerpApplication.getInstance().getPersistence().createDao(AccountMovement.class);
        IIconFactory icons = TerpApplication.getInstance().getIconFactory();
        if (icons != null) {
            this.btnSearch.setGraphic(icons.getIcon("SEARCH"));
            this.btnAdd.setGraphic(icons.getIcon("PLUS"));
            this.btnEdit.setGraphic(icons.getIcon("EDIT"));
            this.btnDelete.setGraphic(icons.getIcon("SEARCH"));
        }
        cmbSearchType.getItems().add(ALL_TYPES);
        cmbSearchType.getItems().addAll(AccountDirection.LABELS);
        cmbSearchType.getSelectionModel().select(ALL_TYPES);
        this.tcDateLabel.setCellValueFactory(new PropertyValueFactory<>("dateLabel"));
        this.tcTypeLabel.setCellValueFactory(new PropertyValueFactory<>("typeLabel"));
        this.tcAccountCode.setCellValueFactory(new PropertyValueFactory<>("accountCode"));
        this.tcAccountName.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        this.tcAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        this.tcDocumentNo.setCellValueFactory(new PropertyValueFactory<>("documentNo"));
        this.tcSourceLabel.setCellValueFactory(new PropertyValueFactory<>("sourceLabel"));
        this.pgnMovementData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvMovementView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        this.searchSqlStatement = CompanyScope.from("AccountMovement");
        this.btnAdd.setDisable(!rights.add);
        updateButtons(true, true);
        refreshView();
    }

    private void openEditor(AccountMovement current) {
        try {
            FXMLLoader loader = CariDates.pluginLoader(MovementFormController.class,
                    "/fxml/MovementEditForm.fxml");
            Node node = loader.load();
            MovementEditFormController controller = loader.getController();
            controller.initializeForm(current);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(new Scene((Parent) node));
            stage.setTitle(current == null ? "Yeni cari hareket" : "Cari hareket");
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

    private ObservableList<AccountMovement> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<AccountMovement> rows = movementDao.findPage(pageNum, rowsPerPage, scopedQuery());
        return FXCollections.observableArrayList(rows == null ? List.of() : rows);
    }

    private String scopedQuery() {
        if (searchSqlStatement == null || searchSqlStatement.isBlank()) {
            return CompanyScope.from("AccountMovement");
        }
        return searchSqlStatement;
    }

    private void selectionChanged(Change<? extends AccountMovement> change) {
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
}

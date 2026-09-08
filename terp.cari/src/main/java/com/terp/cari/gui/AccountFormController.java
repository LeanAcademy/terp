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

import com.terp.cari.data.Cari;
import com.terp.plugin.CompanyScope;
import com.terp.plugin.FormRights;
import com.terp.plugin.TerpApplication;
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

public class AccountFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(AccountFormController.class.getName());
    private static final int DEFAULT_ROWS_PER_PAGE = 20;
    private static final String ALL_ROLES = "Tümü";
    private static final String ROLE_CUSTOMER = "Müşteri";
    private static final String ROLE_SUPPLIER = "Tedarikçi";

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
    private TextField txtAccountName;
    @FXML
    private ComboBox<String> cmbSearchRole;
    @FXML
    private TextField txtTaxId;
    @FXML
    private TableView<Cari> tblvAccountView;
    @FXML
    private Pagination pgnAccountData;
    @FXML
    private TableColumn<Cari, String> tcAccountCode;
    @FXML
    private TableColumn<Cari, String> tcAccountName;
    @FXML
    private TableColumn<Cari, String> tcRoleLabel;
    @FXML
    private TableColumn<Cari, String> tcTaxId;
    @FXML
    private TableColumn<Cari, String> tcCity;
    @FXML
    private TableColumn<Cari, String> tcStatusLabel;

    private ICommonDao<Cari> accountDao;
    private FormRights rights = FormRights.forMenu("CRI02");
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int currentPageNum = 1;
    private String searchSqlStatement = "";

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        StringBuilder sql = new StringBuilder("from Cari e");
        boolean whereAdded = CompanyScope.append(sql, false);
        whereAdded = appendLike(sql, whereAdded, "e.accountCode", txtAccountCode.getText());
        whereAdded = appendLike(sql, whereAdded, "e.accountName", txtAccountName.getText());
        whereAdded = appendLike(sql, whereAdded, "e.taxId", txtTaxId.getText());
        String role = cmbSearchRole.getValue();
        if (ROLE_CUSTOMER.equals(role)) {
            whereAdded = appendEquals(sql, whereAdded, "e.customerFlag", "1");
        } else if (ROLE_SUPPLIER.equals(role)) {
            whereAdded = appendEquals(sql, whereAdded, "e.supplierFlag", "1");
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
        Cari selected = tblvAccountView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        openEditor(selected);
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        Cari selected = tblvAccountView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Alert alert = new Alert(AlertType.CONFIRMATION,
                "Seçilen kaydı sil: " + selected.getAccountCode(),
                ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }
            try {
                accountDao.delete(selected.getRowId());
                refreshView();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.accountDao = TerpApplication.getInstance().getPersistence().createDao(Cari.class);
        IIconFactory icons = TerpApplication.getInstance().getIconFactory();
        if (icons != null) {
            this.btnSearch.setGraphic(icons.getIcon("SEARCH"));
            this.btnAdd.setGraphic(icons.getIcon("PLUS"));
            this.btnEdit.setGraphic(icons.getIcon("EDIT"));
            this.btnDelete.setGraphic(icons.getIcon("SEARCH"));
        }

        cmbSearchRole.getItems().addAll(ALL_ROLES, ROLE_CUSTOMER, ROLE_SUPPLIER);
        cmbSearchRole.getSelectionModel().select(ALL_ROLES);

        this.tcAccountCode.setCellValueFactory(new PropertyValueFactory<>("accountCode"));
        this.tcAccountName.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        this.tcRoleLabel.setCellValueFactory(new PropertyValueFactory<>("roleLabel"));
        this.tcTaxId.setCellValueFactory(new PropertyValueFactory<>("taxId"));
        this.tcCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));

        this.pgnAccountData.currentPageIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.currentPageNum = newValue.intValue() + 1;
                    refreshView();
                });
        this.tblvAccountView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        this.searchSqlStatement = CompanyScope.from("Cari");
        this.btnAdd.setDisable(!rights.add);
        updateButtons(true, true);
        refreshView();
    }

    private void openEditor(Cari current) {
        try {
            FXMLLoader loader = pluginLoader("/fxml/AccountEditForm.fxml");
            Node node = loader.load();
            AccountEditFormController controller = loader.getController();
            controller.initializeForm(current);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(new Scene((Parent) node));
            stage.setTitle(current == null ? "Yeni cari" : "Cari kartı");
            stage.showAndWait();
            refreshView();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void refreshView() {
        String hql = scopedQuery();
        long count = accountDao.getRecordCount(hql);
        int pages = (int) (count / rowsPerPage + 1);
        this.pgnAccountData.setPageCount(Math.max(1, pages));
        this.tblvAccountView.setItems(currentPage());
    }

    private ObservableList<Cari> currentPage() {
        int pageNum = Math.max(1, this.currentPageNum);
        List<Cari> rows = accountDao.findPage(pageNum, rowsPerPage, scopedQuery());
        return FXCollections.observableArrayList(rows == null ? List.of() : rows);
    }

    private String scopedQuery() {
        if (searchSqlStatement == null || searchSqlStatement.isBlank()) {
            return CompanyScope.from("Cari");
        }
        return searchSqlStatement;
    }

    private void selectionChanged(Change<? extends Cari> change) {
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
        FXMLLoader loader = new FXMLLoader(AccountFormController.class.getResource(fxml));
        loader.setClassLoader(AccountFormController.class.getClassLoader());
        return loader;
    }
}

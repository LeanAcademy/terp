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
package com.terp.gui.controllers;

import com.terp.data.CommonDaoImpl;
import com.terp.data.model.Employee;
import com.terp.plugin.IUser;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.gui.IIconFactory;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class UserFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(UserFormController.class.getName());
    private static final String MENU_ID = "SYS05";

    @FXML
    private Button btnSearch;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;
    @FXML
    private TextField txtUserName;
    @FXML
    private TextField txtName;
    @FXML
    private TableView<Employee> tblvUserView;
    @FXML
    private TableColumn<Employee, String> tcUserName;
    @FXML
    private TableColumn<Employee, String> tcName;
    @FXML
    private TableColumn<Employee, String> tcGroupName;
    @FXML
    private TableColumn<Employee, String> tcStatusLabel;

    private ICommonDao<Employee> userDao;
    private boolean allowAdd;
    private boolean allowEdit;
    private boolean allowDelete;

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        refreshView();
    }

    @FXML
    public void onActionBtnAdd(ActionEvent event) {
        if (allowAdd) {
            openEditor(null);
        }
    }

    @FXML
    public void onActionBtnEdit(ActionEvent event) {
        Employee selected = tblvUserView.getSelectionModel().getSelectedItem();
        if (selected != null && allowEdit) {
            openEditor(selected);
        }
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        Employee selected = tblvUserView.getSelectionModel().getSelectedItem();
        if (selected == null || !allowDelete) {
            return;
        }
        IUser current = TerpApplication.getInstance().getUser();
        if (current != null && selected.getUserName() != null
                && selected.getUserName().equals(current.getUserName())) {
            showError("Kendi hesabınızı silemezsiniz.");
            return;
        }
        Alert alert = new Alert(AlertType.CONFIRMATION,
                "Kullanıcıyı sil: " + selected.getUserName(), ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                userDao.delete(selected.getRowId());
                refreshView();
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.userDao = new CommonDaoImpl<>(Employee.class);
        IUser user = TerpApplication.getInstance().getUser();
        this.allowAdd = user == null || user.canAdd(MENU_ID);
        this.allowEdit = user == null || user.canEdit(MENU_ID);
        this.allowDelete = user == null || user.canDelete(MENU_ID);
        this.btnAdd.setDisable(!allowAdd);
        IIconFactory icons = TerpApplication.getInstance().getIconFactory();
        if (icons != null) {
            this.btnSearch.setGraphic(icons.getIcon("SEARCH"));
            this.btnAdd.setGraphic(icons.getIcon("PLUS"));
            this.btnEdit.setGraphic(icons.getIcon("EDIT"));
        }
        this.tcUserName.setCellValueFactory(new PropertyValueFactory<>("userName"));
        this.tcName.setCellValueFactory(new PropertyValueFactory<>("name"));
        this.tcGroupName.setCellValueFactory(new PropertyValueFactory<>("groupName"));
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        this.tblvUserView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        updateButtons(true, true);
        refreshView();
    }

    private void openEditor(Employee current) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserEditForm.fxml"));
            Node node = loader.load();
            UserEditFormController controller = loader.getController();
            controller.initializeForm(current);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(new Scene((Parent) node));
            stage.setTitle(current == null ? "Yeni kullanıcı" : "Kullanıcı");
            stage.showAndWait();
            refreshView();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void refreshView() {
        StringBuilder sql = new StringBuilder("from Employee e join fetch e.group g");
        boolean where = false;
        where = appendLike(sql, where, "e.userName", txtUserName.getText());
        appendLike(sql, where, "e.name", txtName.getText());
        List<Employee> rows = userDao.findAll(sql.toString());
        tblvUserView.setItems(FXCollections.observableArrayList(rows == null ? List.of() : rows));
    }

    private void selectionChanged(Change<? extends Employee> change) {
        int size = change.getList().size();
        updateButtons(size != 1, size != 1);
    }

    private void updateButtons(boolean editDisabled, boolean deleteDisabled) {
        this.btnEdit.setDisable(editDisabled || !allowEdit);
        this.btnDelete.setDisable(deleteDisabled || !allowDelete);
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

    private static void showError(String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Kullanıcı");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}

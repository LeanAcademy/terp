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
import com.terp.data.model.EmployeeGroup;
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

public class GroupFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(GroupFormController.class.getName());
    private static final String MENU_ID = "SYS03";

    @FXML
    private Button btnSearch;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;
    @FXML
    private TextField txtGroupName;
    @FXML
    private TableView<EmployeeGroup> tblvGroupView;
    @FXML
    private TableColumn<EmployeeGroup, String> tcGroupName;
    @FXML
    private TableColumn<EmployeeGroup, String> tcSystemLabel;
    @FXML
    private TableColumn<EmployeeGroup, String> tcStatusLabel;

    private ICommonDao<EmployeeGroup> groupDao;
    private ICommonDao<Employee> employeeDao;
    private boolean allowAdd;
    private boolean allowEdit;
    private boolean allowDelete;

    @FXML
    public void onActionBtnSearch(ActionEvent event) {
        refreshView();
    }

    @FXML
    public void onActionBtnAdd(ActionEvent event) {
        if (!allowAdd) {
            return;
        }
        openEditor(null);
    }

    @FXML
    public void onActionBtnEdit(ActionEvent event) {
        EmployeeGroup selected = tblvGroupView.getSelectionModel().getSelectedItem();
        if (selected != null && allowEdit) {
            openEditor(selected);
        }
    }

    @FXML
    public void onActionBtnDelete(ActionEvent event) {
        EmployeeGroup selected = tblvGroupView.getSelectionModel().getSelectedItem();
        if (selected == null || !allowDelete) {
            return;
        }
        if (selected.isSystemAdmin()) {
            showError("Sistem grubu silinemez.");
            return;
        }
        if (hasUsers(selected)) {
            showError("Gruba bağlı kullanıcı var.");
            return;
        }
        Alert alert = new Alert(AlertType.CONFIRMATION, "Grubu sil: " + selected.getGroupName(),
                ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                groupDao.delete(selected.getRowId());
                refreshView();
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.groupDao = new CommonDaoImpl<>(EmployeeGroup.class);
        this.employeeDao = new CommonDaoImpl<>(Employee.class);
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
        this.tcGroupName.setCellValueFactory(new PropertyValueFactory<>("groupName"));
        this.tcSystemLabel.setCellValueFactory(new PropertyValueFactory<>("systemLabel"));
        this.tcStatusLabel.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        this.tblvGroupView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        updateButtons(true, true);
        refreshView();
    }

    private void openEditor(EmployeeGroup current) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GroupEditForm.fxml"));
            Node node = loader.load();
            GroupEditFormController controller = loader.getController();
            controller.initializeForm(current);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(new Scene((Parent) node));
            stage.setTitle(current == null ? "Yeni grup" : "Grup yetkileri");
            stage.showAndWait();
            refreshView();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void refreshView() {
        String name = txtGroupName.getText();
        List<EmployeeGroup> rows;
        if (name == null || name.isBlank()) {
            rows = groupDao.findAll();
        } else {
            rows = groupDao.findAll("from EmployeeGroup e where e.groupName like '"
                    + name.replace("'", "''") + "'");
        }
        tblvGroupView.setItems(FXCollections.observableArrayList(rows == null ? List.of() : rows));
    }

    private boolean hasUsers(EmployeeGroup group) {
        if (group.getRowId() == null) {
            return false;
        }
        List<Employee> users = employeeDao.findAll(
                "from Employee e where e.group.rowId = " + group.getRowId());
        return users != null && !users.isEmpty();
    }

    private void selectionChanged(Change<? extends EmployeeGroup> change) {
        int size = change.getList().size();
        if (size == 1) {
            updateButtons(false, false);
        } else {
            updateButtons(true, true);
        }
    }

    private void updateButtons(boolean editDisabled, boolean deleteDisabled) {
        this.btnEdit.setDisable(editDisabled || !allowEdit);
        this.btnDelete.setDisable(deleteDisabled || !allowDelete);
    }

    private static void showError(String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Yetki yönetimi");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}

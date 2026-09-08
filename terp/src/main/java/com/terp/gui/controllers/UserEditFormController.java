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
import com.terp.users.PasswordHashes;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UserEditFormController implements Initializable {

    @FXML
    private Button btnSubmit;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField txtUserName;
    @FXML
    private TextField txtName;
    @FXML
    private ComboBox<String> cmbGroup;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private CheckBox chkActive;
    @FXML
    private Label lblPasswordHint;

    private ICommonDao<Employee> userDao;
    private ICommonDao<EmployeeGroup> groupDao;
    private Employee currentRow;
    private List<EmployeeGroup> groups = List.of();
    private boolean callerIsAdmin;

    public void initializeForm(Employee row) {
        this.currentRow = row;
        IUser user = TerpApplication.getInstance().getUser();
        this.callerIsAdmin = user != null && user.isAdministrator();
        fillGroups();
        if (row == null) {
            chkActive.setSelected(true);
            lblPasswordHint.setText("Yeni kullanıcı için şifre zorunludur.");
            return;
        }
        txtUserName.setText(empty(row.getUserName()));
        txtName.setText(empty(row.getName()));
        chkActive.setSelected(row.getStatus() == 0);
        if (row.getGroup() != null) {
            selectGroup(row.getGroup().getRowId());
        }
        lblPasswordHint.setText("Düzenlemede boş bırakılırsa şifre değişmez.");
        if (row.getGroup() instanceof EmployeeGroup eg && eg.isSystemAdmin() && !callerIsAdmin) {
            btnSubmit.setDisable(true);
        }
    }

    @FXML
    private void onActionBtnSubmit(ActionEvent event) {
        String userName = trim(txtUserName.getText());
        String name = trim(txtName.getText());
        EmployeeGroup group = selectedGroup();
        if (userName == null || name == null || group == null) {
            showError("Kullanıcı adı, ad ve grup zorunludur.");
            return;
        }
        if (group.isSystemAdmin() && !callerIsAdmin) {
            showError("Sistem grubuna yalnızca sistem yöneticisi kullanıcı ekleyebilir.");
            return;
        }
        String password = txtPassword.getText();
        Employee row = this.currentRow;
        Date now = new Date();
        if (row == null) {
            if (password == null || password.isBlank()) {
                showError("Yeni kullanıcı için şifre girin.");
                return;
            }
            row = userDao.getEmpty();
            row.setAddedDate(now);
            row.setType(1);
            row.setPassword(PasswordHashes.md5(password));
        } else if (password != null && !password.isBlank()) {
            row.setPassword(PasswordHashes.md5(password));
        }
        row.setUserName(userName);
        row.setName(name);
        row.setGroup(group);
        row.setStatus(chkActive.isSelected() ? 0 : 1);
        row.setLastUpdateDate(now);
        userDao.addOrUpdate(row);
        close();
    }

    @FXML
    private void onActionBtnCancel(ActionEvent event) {
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.userDao = new CommonDaoImpl<>(Employee.class);
        this.groupDao = new CommonDaoImpl<>(EmployeeGroup.class);
    }

    private void fillGroups() {
        List<EmployeeGroup> rows = groupDao.findAll();
        this.groups = rows == null ? List.of() : rows;
        cmbGroup.getItems().clear();
        for (EmployeeGroup group : groups) {
            if (group.getStatus() == 0) {
                cmbGroup.getItems().add(group.getGroupName());
            }
        }
    }

    private void selectGroup(Long rowId) {
        if (rowId == null) {
            return;
        }
        for (EmployeeGroup group : groups) {
            if (rowId.equals(group.getRowId())) {
                cmbGroup.getSelectionModel().select(group.getGroupName());
                return;
            }
        }
    }

    private EmployeeGroup selectedGroup() {
        String name = cmbGroup.getValue();
        if (name == null) {
            return null;
        }
        for (EmployeeGroup group : groups) {
            if (name.equals(group.getGroupName())) {
                return group;
            }
        }
        return null;
    }

    private void close() {
        Stage stage = (Stage) btnSubmit.getScene().getWindow();
        stage.close();
    }

    private static String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String empty(String value) {
        return value == null ? "" : value;
    }

    private static void showError(String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Kullanıcı");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}

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
import com.terp.data.model.EmployeeGroup;
import com.terp.data.model.GroupCompany;
import com.terp.data.model.GroupPermission;
import com.terp.data.model.MenuSource;
import com.terp.gui.auth.CompanyAccessRow;
import com.terp.gui.auth.MenuAccessRow;
import com.terp.plugin.IUser;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.ICompanyLookup;
import com.terp.plugin.data.model.ICompany;
import com.terp.plugin.gui.RecordAuditBar;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class GroupEditFormController implements Initializable {

    @FXML
    private Button btnSubmit;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField txtGroupName;
    @FXML
    private CheckBox chkActive;
    @FXML
    private CheckBox chkSystemAdmin;
    @FXML
    private Label lblSystemHint;
    @FXML
    private TableView<CompanyAccessRow> tblvCompanyView;
    @FXML
    private TableColumn<CompanyAccessRow, Boolean> tcCompanySelected;
    @FXML
    private TableColumn<CompanyAccessRow, String> tcCompanyLabel;
    @FXML
    private TableView<MenuAccessRow> tblvMenuView;
    @FXML
    private TableColumn<MenuAccessRow, String> tcMenuId;
    @FXML
    private TableColumn<MenuAccessRow, String> tcMenuTitle;
    @FXML
    private TableColumn<MenuAccessRow, Boolean> tcView;
    @FXML
    private TableColumn<MenuAccessRow, Boolean> tcAdd;
    @FXML
    private TableColumn<MenuAccessRow, Boolean> tcEdit;
    @FXML
    private TableColumn<MenuAccessRow, Boolean> tcDelete;

    private ICommonDao<EmployeeGroup> groupDao;
    private ICommonDao<GroupCompany> companyRightDao;
    private ICommonDao<GroupPermission> permissionDao;
    private ICommonDao<MenuSource> menuDao;
    private EmployeeGroup currentRow;
    private boolean callerIsAdmin;
    private RecordAuditBar auditBar;

    public void initializeForm(EmployeeGroup row) {
        this.currentRow = row;
        IUser user = TerpApplication.getInstance().getUser();
        this.callerIsAdmin = user != null && user.isAdministrator();
        chkSystemAdmin.setDisable(!callerIsAdmin);
        if (row == null) {
            chkActive.setSelected(true);
            chkSystemAdmin.setSelected(false);
            fillTables(Set.of(), List.of());
            updateSystemHint();
            showAudit();
            return;
        }
        txtGroupName.setText(row.getGroupName() == null ? "" : row.getGroupName());
        chkActive.setSelected(row.getStatus() == 0);
        chkSystemAdmin.setSelected(row.isSystemAdmin());
        Set<Long> assigned = new HashSet<>();
        if (row.getRowId() != null) {
            List<GroupCompany> companyRows = companyRightDao.findAll(
                    "from GroupCompany e where e.group.rowId = " + row.getRowId());
            if (companyRows != null) {
                for (GroupCompany item : companyRows) {
                    if (item.getCompanyId() != null) {
                        assigned.add(item.getCompanyId());
                    }
                }
            }
        }
        List<GroupPermission> perms = row.getRowId() == null ? List.of()
                : permissionDao.findAll("from GroupPermission e where e.group.rowId = " + row.getRowId());
        fillTables(assigned, perms == null ? List.of() : perms);
        updateSystemHint();
        if (row.isSystemAdmin() && !callerIsAdmin) {
            btnSubmit.setDisable(true);
            txtGroupName.setDisable(true);
            tblvCompanyView.setDisable(true);
            tblvMenuView.setDisable(true);
        }
        showAudit();
    }

    @FXML
    private void onActionBtnSubmit(ActionEvent event) {
        String name = txtGroupName.getText() == null ? "" : txtGroupName.getText().trim();
        if (name.isEmpty()) {
            showError("Grup adı zorunludur.");
            return;
        }
        boolean systemAdmin = chkSystemAdmin.isSelected();
        if (systemAdmin && !callerIsAdmin) {
            showError("Sistem yönetimi grubunu yalnızca sistem yöneticisi işaretleyebilir.");
            return;
        }
        Date now = new Date();
        EmployeeGroup row = this.currentRow;
        if (row == null) {
            row = groupDao.getEmpty();
            row.setAddedDate(now);
        }
        row.setGroupName(name);
        row.setStatus(chkActive.isSelected() ? 0 : 1);
        if (callerIsAdmin) {
            row.setSystemAdmin(systemAdmin ? 1 : 0);
        }
        row.setLastUpdateDate(now);
        EmployeeGroup saved = groupDao.addOrUpdate(row);
        if (saved == null || saved.getRowId() == null) {
            showError("Grup kaydedilemedi.");
            return;
        }
        replaceCompanies(saved, systemAdmin);
        replacePermissions(saved, systemAdmin);
        close();
    }

    @FXML
    private void onActionBtnCancel(ActionEvent event) {
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.groupDao = new CommonDaoImpl<>(EmployeeGroup.class);
        this.companyRightDao = new CommonDaoImpl<>(GroupCompany.class);
        this.permissionDao = new CommonDaoImpl<>(GroupPermission.class);
        this.menuDao = new CommonDaoImpl<>(MenuSource.class);
        tcCompanySelected.setCellValueFactory(cell -> cell.getValue().selectedProperty());
        tcCompanySelected.setCellFactory(CheckBoxTableCell.forTableColumn(tcCompanySelected));
        tcCompanyLabel.setCellValueFactory(new PropertyValueFactory<>("label"));
        tcMenuId.setCellValueFactory(new PropertyValueFactory<>("menuId"));
        tcMenuTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        tcView.setCellValueFactory(cell -> cell.getValue().viewProperty());
        tcView.setCellFactory(CheckBoxTableCell.forTableColumn(tcView));
        tcAdd.setCellValueFactory(cell -> cell.getValue().addProperty());
        tcAdd.setCellFactory(CheckBoxTableCell.forTableColumn(tcAdd));
        tcEdit.setCellValueFactory(cell -> cell.getValue().editProperty());
        tcEdit.setCellFactory(CheckBoxTableCell.forTableColumn(tcEdit));
        tcDelete.setCellValueFactory(cell -> cell.getValue().deleteProperty());
        tcDelete.setCellFactory(CheckBoxTableCell.forTableColumn(tcDelete));
        chkSystemAdmin.selectedProperty().addListener((obs, old, value) -> updateSystemHint());
        this.auditBar = RecordAuditBar.install(txtGroupName);
        showAudit();
    }

    private void showAudit() {
        if (auditBar != null) {
            auditBar.bind(currentRow);
        }
    }

    private void fillTables(Set<Long> assignedCompanies, List<GroupPermission> permissions) {
        List<CompanyAccessRow> companyRows = new ArrayList<>();
        ICompanyLookup lookup = TerpApplication.getInstance().getCompanyLookup();
        if (lookup != null) {
            for (ICompany company : lookup.findActive()) {
                if (company == null || company.getRowId() == null) {
                    continue;
                }
                companyRows.add(new CompanyAccessRow(company.getRowId(), company.getDisplayLabel(),
                        assignedCompanies.contains(company.getRowId())));
            }
        }
        tblvCompanyView.setItems(FXCollections.observableArrayList(companyRows));

        List<MenuAccessRow> menuRows = new ArrayList<>();
        List<MenuSource> menus = menuDao.findAll("from MenuSource e where e.menuType = 1");
        if (menus != null) {
            menus.sort(java.util.Comparator.comparing(MenuSource::getMenuId,
                    java.util.Comparator.nullsLast(String::compareTo)));
            for (MenuSource menu : menus) {
                if (menu == null || menu.getMenuId() == null) {
                    continue;
                }
                GroupPermission match = findPermission(permissions, menu.getMenuId());
                menuRows.add(new MenuAccessRow(menu.getMenuId(), menu.getMenuName(),
                        match != null && match.isViewAllowed(),
                        match != null && match.isAddAllowed(),
                        match != null && match.isEditAllowed(),
                        match != null && match.isDeleteAllowed()));
            }
        }
        tblvMenuView.setItems(FXCollections.observableArrayList(menuRows));
    }

    private void replaceCompanies(EmployeeGroup group, boolean systemAdmin) {
        List<GroupCompany> existing = companyRightDao.findAll(
                "from GroupCompany e where e.group.rowId = " + group.getRowId());
        if (existing != null) {
            for (GroupCompany row : existing) {
                if (row.getRowId() != null) {
                    companyRightDao.delete(row.getRowId());
                }
            }
        }
        if (systemAdmin) {
            return;
        }
        Date now = new Date();
        for (CompanyAccessRow row : tblvCompanyView.getItems()) {
            if (!row.isSelected() || row.getCompanyId() == null) {
                continue;
            }
            GroupCompany item = companyRightDao.getEmpty();
            item.setGroup(group);
            item.setCompanyId(row.getCompanyId());
            item.setAddedDate(now);
            item.setLastUpdateDate(now);
            companyRightDao.addOrUpdate(item);
        }
    }

    private void replacePermissions(EmployeeGroup group, boolean systemAdmin) {
        List<GroupPermission> existing = permissionDao.findAll(
                "from GroupPermission e where e.group.rowId = " + group.getRowId());
        if (existing != null) {
            for (GroupPermission row : existing) {
                if (row.getRowId() != null) {
                    permissionDao.delete(row.getRowId());
                }
            }
        }
        if (systemAdmin) {
            return;
        }
        Date now = new Date();
        for (MenuAccessRow row : tblvMenuView.getItems()) {
            if (!row.isView()) {
                continue;
            }
            GroupPermission item = permissionDao.getEmpty();
            item.setGroup(group);
            item.setMenuId(row.getMenuId());
            item.setCanView(1);
            item.setCanAdd(row.isAdd() ? 1 : 0);
            item.setCanEdit(row.isEdit() ? 1 : 0);
            item.setCanDelete(row.isDelete() ? 1 : 0);
            item.setAddedDate(now);
            item.setLastUpdateDate(now);
            permissionDao.addOrUpdate(item);
        }
    }

    private void updateSystemHint() {
        boolean system = chkSystemAdmin.isSelected();
        tblvCompanyView.setDisable(system);
        tblvMenuView.setDisable(system);
        if (lblSystemHint != null) {
            lblSystemHint.setText(system
                    ? "Sistem yönetimi grubu tüm firmalara ve işlemlere sahiptir."
                    : "İş grubu yalnızca işaretlenen firma ve işlemleri görür.");
        }
    }

    private void close() {
        Stage stage = (Stage) btnSubmit.getScene().getWindow();
        stage.close();
    }

    private static GroupPermission findPermission(List<GroupPermission> rows, String menuId) {
        for (GroupPermission row : rows) {
            if (row != null && menuId.equals(row.getMenuId())) {
                return row;
            }
        }
        return null;
    }

    private static void showError(String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Grup");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}

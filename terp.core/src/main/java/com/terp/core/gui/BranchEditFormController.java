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

import com.terp.core.data.Branch;
import com.terp.core.data.Company;
import com.terp.plugin.CompanyScope;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.model.ICompany;
import com.terp.plugin.gui.RecordAuditBar;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class BranchEditFormController implements Initializable {

    private static final String TYPE_HEAD = "Merkez";
    private static final String TYPE_BRANCH = "Şube";

    @FXML
    private Button btnSubmit;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField txtBranchName;
    @FXML
    private TextField txtBranchLongName;
    @FXML
    private ComboBox<String> cmbType;
    @FXML
    private CheckBox chkActive;
    @FXML
    private TextField txtAddress1;
    @FXML
    private TextField txtCity;
    @FXML
    private TextField txtState;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextField txtEmail;

    private ICommonDao<Branch> branchDao;
    private ICommonDao<Company> companyDao;
    private Branch currentRow;
    private RecordAuditBar auditBar;

    public void initializeForm(Branch row) {
        this.currentRow = row;
        cmbType.getItems().setAll(TYPE_HEAD, TYPE_BRANCH);
        if (row == null) {
            chkActive.setSelected(true);
            cmbType.getSelectionModel().select(TYPE_BRANCH);
            showAudit();
            return;
        }
        txtBranchName.setText(empty(row.getBranchName()));
        txtBranchLongName.setText(empty(row.getBranchLongName()));
        cmbType.getSelectionModel().select(row.getBranchType() == 1 ? TYPE_HEAD : TYPE_BRANCH);
        chkActive.setSelected(row.getStatus() == 0);
        txtAddress1.setText(empty(row.getAddress1()));
        txtCity.setText(empty(row.getCity()));
        txtState.setText(empty(row.getState()));
        txtPhone.setText(empty(row.getPhoneNum1()));
        txtEmail.setText(empty(row.getEmail1()));
        showAudit();
    }

    @FXML
    private void onActionBtnSubmit(ActionEvent event) {
        String name = trimToNull(txtBranchName.getText());
        String title = trimToNull(txtBranchLongName.getText());
        if (name == null || title == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Şube");
            alert.setHeaderText("Zorunlu alanlar eksik");
            alert.setContentText("Şube adı ve ünvan zorunludur.");
            alert.show();
            return;
        }
        if (!CompanyScope.requireCompany()) {
            return;
        }
        ICompany current = TerpApplication.getInstance().getCurrentCompany();
        Company company = companyDao.firstOrDefault(current.getRowId());
        if (company == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Şube");
            alert.setHeaderText("Firma");
            alert.setContentText("Çalışma firması bulunamadı.");
            alert.show();
            return;
        }
        Date now = new Date();
        Branch row = currentRow;
        if (row == null) {
            row = branchDao.getEmpty();
            if (row == null) {
                row = new Branch();
            }
            row.setAddedDate(now);
        }
        row.setCompany(company);
        row.setBranchName(name);
        row.setBranchLongName(title);
        row.setBranchType(TYPE_HEAD.equals(cmbType.getValue()) ? 1 : 0);
        row.setStatus(chkActive.isSelected() ? 0 : 1);
        row.setAddress1(empty(txtAddress1.getText()));
        row.setAddress2("");
        row.setAddress3("");
        row.setCity(empty(txtCity.getText()));
        row.setState(empty(txtState.getText()));
        row.setZip("");
        row.setCountry("");
        row.setPhoneNum1(empty(txtPhone.getText()));
        row.setPhoneNum2("");
        row.setFaxNum("");
        row.setEmail1(empty(txtEmail.getText()));
        row.setEmail2("");
        row.setLastUpdateDate(now);
        branchDao.addOrUpdate(row);
        close();
    }

    @FXML
    private void onActionBtnCancel(ActionEvent event) {
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.branchDao = TerpApplication.getInstance().getPersistence().createDao(Branch.class);
        this.companyDao = TerpApplication.getInstance().getPersistence().createDao(Company.class);
        this.auditBar = RecordAuditBar.install(txtBranchName);
    }

    private void showAudit() {
        if (auditBar != null) {
            auditBar.bind(currentRow);
        }
    }

    private void close() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private static String empty(String value) {
        return value == null ? "" : value;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

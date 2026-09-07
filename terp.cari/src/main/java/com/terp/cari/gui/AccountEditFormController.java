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
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

public class AccountEditFormController implements Initializable {

    @FXML
    private Button btnSubmit;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField txtAccountCode;
    @FXML
    private TextField txtAccountName;
    @FXML
    private TextField txtLongName;
    @FXML
    private CheckBox chkCustomer;
    @FXML
    private CheckBox chkSupplier;
    @FXML
    private CheckBox chkActive;
    @FXML
    private TextField txtTaxId;
    @FXML
    private TextField txtTaxOffice;
    @FXML
    private TextArea txtAddress;
    @FXML
    private TextField txtCountry;
    @FXML
    private TextField txtCity;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextArea txtNotes;

    private ICommonDao<Cari> accountDao;
    private Cari currentRow;
    private ValidationSupport validationSupport;

    public void initializeForm(Cari row) {
        this.currentRow = row;
        if (row == null) {
            chkCustomer.setSelected(true);
            chkSupplier.setSelected(true);
            chkActive.setSelected(true);
            return;
        }
        txtAccountCode.setText(empty(row.getAccountCode()));
        txtAccountName.setText(empty(row.getAccountName()));
        txtLongName.setText(empty(row.getLongName()));
        chkCustomer.setSelected(row.isCustomer());
        chkSupplier.setSelected(row.isSupplier());
        chkActive.setSelected(row.getStatus() == 0);
        txtTaxId.setText(empty(row.getTaxId()));
        txtTaxOffice.setText(empty(row.getTaxOffice()));
        txtAddress.setText(empty(row.getAddress()));
        txtCountry.setText(empty(row.getCountry()));
        txtCity.setText(empty(row.getCity()));
        txtPhone.setText(empty(row.getPhone()));
        txtEmail.setText(empty(row.getEmail()));
        txtNotes.setText(empty(row.getNotes()));
    }

    @FXML
    private void onActionBtnSubmit(ActionEvent event) {
        if (validationSupport.isInvalid()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Form hatası");
            alert.setHeaderText("Zorunlu alanlar eksik");
            alert.setContentText("Cari kodu ve unvan zorunludur.");
            alert.show();
            return;
        }
        if (!chkCustomer.isSelected() && !chkSupplier.isSelected()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Form hatası");
            alert.setHeaderText("Rol seçilmedi");
            alert.setContentText("Hesap müşteri, tedarikçi veya her ikisi olmalıdır.");
            alert.show();
            return;
        }
        Cari row = this.currentRow;
        Date now = new Date();
        if (row == null) {
            row = accountDao.getEmpty();
            if (row == null) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Kayıt hatası");
                alert.setHeaderText("Cari oluşturulamadı");
                alert.show();
                return;
            }
            row.setAddedDate(now);
        }
        row.setAccountCode(txtAccountCode.getText().trim());
        row.setAccountName(txtAccountName.getText().trim());
        row.setLongName(trimToNull(txtLongName.getText()));
        row.setCustomerFlag(chkCustomer.isSelected() ? 1 : 0);
        row.setSupplierFlag(chkSupplier.isSelected() ? 1 : 0);
        row.setStatus(chkActive.isSelected() ? 0 : 1);
        row.setTaxId(trimToNull(txtTaxId.getText()));
        row.setTaxOffice(trimToNull(txtTaxOffice.getText()));
        row.setAddress(trimToNull(txtAddress.getText()));
        row.setCountry(trimToNull(txtCountry.getText()));
        row.setCity(trimToNull(txtCity.getText()));
        row.setPhone(trimToNull(txtPhone.getText()));
        row.setEmail(trimToNull(txtEmail.getText()));
        row.setNotes(trimToNull(txtNotes.getText()));
        row.setLastUpdateDate(now);
        accountDao.addOrUpdate(row);
        close();
    }

    @FXML
    private void onActionBtnCancel(ActionEvent event) {
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.accountDao = TerpApplication.getInstance().getPersistence().createDao(Cari.class);
        this.validationSupport = new ValidationSupport();
        this.validationSupport.registerValidator(txtAccountCode, true,
                Validator.createEmptyValidator("Kod zorunlu"));
        this.validationSupport.registerValidator(txtAccountName, true,
                Validator.createEmptyValidator("Unvan zorunlu"));
    }

    private void close() {
        Stage stage = (Stage) btnSubmit.getScene().getWindow();
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

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

import com.terp.core.data.UnitOfMeasure;
import com.terp.plugin.CompanyScope;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
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
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

public class UnitEditFormController implements Initializable {

    @FXML
    private Button btnSubmit;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField txtUnitCode;
    @FXML
    private TextField txtUnitName;
    @FXML
    private CheckBox chkActive;

    private ICommonDao<UnitOfMeasure> unitDao;
    private UnitOfMeasure currentRow;
    private ValidationSupport validationSupport;
    private RecordAuditBar auditBar;

    public void initializeForm(UnitOfMeasure row) {
        this.currentRow = row;
        if (row == null) {
            chkActive.setSelected(true);
            showAudit();
            return;
        }
        txtUnitCode.setText(empty(row.getUnitCode()));
        txtUnitName.setText(empty(row.getUnitName()));
        chkActive.setSelected(row.getStatus() == UnitOfMeasure.STATUS_ACTIVE);
        showAudit();
    }

    @FXML
    private void onActionBtnSubmit(ActionEvent event) {
        if (validationSupport.isInvalid()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Form hatası");
            alert.setHeaderText("Zorunlu alanlar eksik");
            alert.setContentText("Kod ve ad zorunludur.");
            alert.show();
            return;
        }
        UnitOfMeasure row = this.currentRow;
        Date now = new Date();
        if (row == null) {
            row = unitDao.getEmpty();
            if (row == null) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Kayıt hatası");
                alert.setHeaderText("Ölçü birimi oluşturulamadı");
                alert.show();
                return;
            }
            row.setAddedDate(now);
        }
        if (!CompanyScope.stamp(row)) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Firma");
            alert.setHeaderText("Firma seçilmedi");
            alert.setContentText("Önce araç çubuğundan çalışma firmasını seçin.");
            alert.show();
            return;
        }
        row.setUnitCode(txtUnitCode.getText().trim());
        row.setUnitName(txtUnitName.getText().trim());
        row.setStatus(chkActive.isSelected() ? UnitOfMeasure.STATUS_ACTIVE : UnitOfMeasure.STATUS_INACTIVE);
        row.setLastUpdateDate(now);
        if (CompanyScope.rejectDuplicate(
                CompanyScope.findDuplicate(unitDao, "UnitOfMeasure", "unitCode",
                        row.getUnitCode(), row.getRowId()),
                "Ölçü birim kodu tekrar ediyor")) {
            return;
        }
        unitDao.addOrUpdate(row);
        close();
    }

    @FXML
    private void onActionBtnCancel(ActionEvent event) {
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.unitDao = TerpApplication.getInstance().getPersistence().createDao(UnitOfMeasure.class);
        this.validationSupport = new ValidationSupport();
        this.validationSupport.registerValidator(txtUnitCode, true,
                Validator.createEmptyValidator("Kod zorunlu"));
        this.validationSupport.registerValidator(txtUnitName, true,
                Validator.createEmptyValidator("Ad zorunlu"));
        this.auditBar = RecordAuditBar.install(txtUnitCode);
        showAudit();
    }

    private void showAudit() {
        if (auditBar != null) {
            auditBar.bind(currentRow);
        }
    }

    private void close() {
        Stage stage = (Stage) btnSubmit.getScene().getWindow();
        stage.close();
    }

    private static String empty(String value) {
        return value == null ? "" : value;
    }
}

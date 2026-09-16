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

import com.terp.core.data.VatRate;
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

public class VatRateEditFormController implements Initializable {

    @FXML
    private Button btnSubmit;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField txtRateCode;
    @FXML
    private TextField txtRateName;
    @FXML
    private TextField txtRate;
    @FXML
    private CheckBox chkActive;

    private ICommonDao<VatRate> vatRateDao;
    private VatRate currentRow;
    private ValidationSupport validationSupport;
    private RecordAuditBar auditBar;

    public void initializeForm(VatRate row) {
        this.currentRow = row;
        if (row == null) {
            chkActive.setSelected(true);
            showAudit();
            return;
        }
        txtRateCode.setText(empty(row.getRateCode()));
        txtRateName.setText(empty(row.getRateName()));
        txtRate.setText(row.getRate() == null ? "" : String.valueOf(row.getRate()));
        chkActive.setSelected(row.getStatus() == VatRate.STATUS_ACTIVE);
        showAudit();
    }

    @FXML
    private void onActionBtnSubmit(ActionEvent event) {
        if (validationSupport.isInvalid()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Form hatası");
            alert.setHeaderText("Zorunlu alanlar eksik");
            alert.setContentText("Kod, ad ve oran zorunludur.");
            alert.show();
            return;
        }
        Double rate = parseRate(txtRate.getText());
        if (rate == null || rate < 0d) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Form hatası");
            alert.setHeaderText("Geçersiz oran");
            alert.setContentText("KDV oranı 0 veya daha büyük bir sayı olmalıdır.");
            alert.show();
            return;
        }
        VatRate row = this.currentRow;
        Date now = new Date();
        if (row == null) {
            row = vatRateDao.getEmpty();
            if (row == null) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Kayıt hatası");
                alert.setHeaderText("KDV oranı oluşturulamadı");
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
        row.setRateCode(txtRateCode.getText().trim());
        row.setRateName(txtRateName.getText().trim());
        row.setRate(rate);
        row.setStatus(chkActive.isSelected() ? VatRate.STATUS_ACTIVE : VatRate.STATUS_INACTIVE);
        row.setLastUpdateDate(now);
        if (CompanyScope.rejectDuplicate(
                CompanyScope.findDuplicate(vatRateDao, "VatRate", "rateCode",
                        row.getRateCode(), row.getRowId()),
                "KDV oranı kodu tekrar ediyor")) {
            return;
        }
        vatRateDao.addOrUpdate(row);
        close();
    }

    @FXML
    private void onActionBtnCancel(ActionEvent event) {
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.vatRateDao = TerpApplication.getInstance().getPersistence().createDao(VatRate.class);
        this.validationSupport = new ValidationSupport();
        this.validationSupport.registerValidator(txtRateCode, true,
                Validator.createEmptyValidator("Kod zorunlu"));
        this.validationSupport.registerValidator(txtRateName, true,
                Validator.createEmptyValidator("Ad zorunlu"));
        this.validationSupport.registerValidator(txtRate, true,
                Validator.createEmptyValidator("Oran zorunlu"));
        this.auditBar = RecordAuditBar.install(txtRateCode);
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

    private static Double parseRate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(text.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

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
package com.terp.satin.gui;

import com.terp.plugin.CompanyScope;
import com.terp.plugin.FormRights;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.satin.data.PurchaseDocs;
import com.terp.satin.data.PurchaseSettings;
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
import javafx.scene.control.TextField;

public class SettingsFormController implements Initializable {

    @FXML
    private Button btnSave;
    @FXML
    private TextField txtOverReceiptPercent;

    private ICommonDao<PurchaseSettings> settingsDao;
    private FormRights rights = FormRights.forMenu("SAT06");
    private RecordAuditBar auditBar;

    @FXML
    private void onActionBtnSave(ActionEvent event) {
        if (!CompanyScope.requireCompany()) {
            return;
        }
        Double percent = SatinDates.parseDouble(txtOverReceiptPercent.getText());
        if (percent == null || percent < 0d) {
            SatinDates.showError("Satınalma ayarları", "Oran geçersiz",
                    "Fazla kabul oranı sıfır veya pozitif bir yüzde olmalıdır.");
            return;
        }
        PurchaseSettings row = PurchaseDocs.loadSettings(settingsDao);
        if (row == null) {
            SatinDates.showError("Satınalma ayarları", "Kayıt hatası", "Ayar satırı oluşturulamadı.");
            return;
        }
        row.setOverReceiptPercent(percent);
        row.setLastUpdateDate(new Date());
        if (row.getAddedDate() == null) {
            row.setAddedDate(new Date());
        }
        CompanyScope.stamp(row);
        settingsDao.addOrUpdate(row);
        showAudit(row);
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Satınalma ayarları");
        alert.setHeaderText("Kaydedildi");
        alert.setContentText("Fazla mal kabul oranı %" + percent + " olarak kaydedildi.");
        alert.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.settingsDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseSettings.class);
        this.btnSave.setDisable(!rights.add && !rights.edit);
        this.auditBar = RecordAuditBar.install(txtOverReceiptPercent);
        if (!CompanyScope.hasCompany()) {
            txtOverReceiptPercent.setText("0");
            showAudit(null);
            return;
        }
        PurchaseSettings row = PurchaseDocs.loadSettings(settingsDao);
        txtOverReceiptPercent.setText(row == null ? "0" : Double.toString(row.getOverReceiptPercent()));
        showAudit(row);
    }

    private void showAudit(PurchaseSettings row) {
        if (auditBar != null) {
            auditBar.bind(row);
        }
    }
}

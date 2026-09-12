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
package com.terp.stok.gui;

import com.terp.plugin.CompanyScope;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.StockDirection;
import com.terp.plugin.gui.RecordAuditBar;
import com.terp.stok.data.MovementReason;
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
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

public class ReasonEditFormController implements Initializable {

    @FXML
    private Button btnSubmit;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField txtReasonCode;
    @FXML
    private TextField txtReasonName;
    @FXML
    private ComboBox<String> cmbDirection;
    @FXML
    private CheckBox chkActive;

    private ICommonDao<MovementReason> reasonDao;
    private MovementReason currentRow;
    private ValidationSupport validationSupport;
    private RecordAuditBar auditBar;

    public void initializeForm(MovementReason row) {
        this.currentRow = row;
        if (row == null) {
            cmbDirection.getSelectionModel().select(StockDirection.IN);
            chkActive.setSelected(true);
            showAudit();
            return;
        }
        txtReasonCode.setText(empty(row.getReasonCode()));
        txtReasonName.setText(empty(row.getReasonName()));
        cmbDirection.getSelectionModel().select(row.getDirection());
        chkActive.setSelected(row.getStatus() == 0);
        showAudit();
    }

    @FXML
    private void onActionBtnSubmit(ActionEvent event) {
        if (validationSupport.isInvalid()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Form hatası");
            alert.setHeaderText("Zorunlu alanlar eksik");
            alert.setContentText("Neden kodu ve adı zorunludur.");
            alert.show();
            return;
        }
        MovementReason row = this.currentRow;
        Date now = new Date();
        if (row == null) {
            row = reasonDao.getEmpty();
            if (row == null) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Kayıt hatası");
                alert.setHeaderText("Neden oluşturulamadı");
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
        row.setReasonCode(txtReasonCode.getText().trim());
        row.setReasonName(txtReasonName.getText().trim());
        int direction = cmbDirection.getSelectionModel().getSelectedIndex();
        row.setDirection(direction < 0 ? StockDirection.IN : direction);
        row.setStatus(chkActive.isSelected() ? 0 : 1);
        row.setLastUpdateDate(now);
        if (CompanyScope.rejectDuplicate(
                CompanyScope.findDuplicate(reasonDao, "MovementReason", "reasonCode",
                        row.getReasonCode(), row.getRowId()),
                "Neden kodu tekrar ediyor")) {
            return;
        }
        reasonDao.addOrUpdate(row);
        close();
    }

    @FXML
    private void onActionBtnCancel(ActionEvent event) {
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.reasonDao = TerpApplication.getInstance().getPersistence().createDao(MovementReason.class);
        cmbDirection.getItems().addAll(StockDirection.LABELS);
        cmbDirection.getSelectionModel().select(StockDirection.IN);
        this.validationSupport = new ValidationSupport();
        this.validationSupport.registerValidator(txtReasonCode, true,
                Validator.createEmptyValidator("Kod zorunlu"));
        this.validationSupport.registerValidator(txtReasonName, true,
                Validator.createEmptyValidator("Ad zorunlu"));
        this.auditBar = RecordAuditBar.install(txtReasonCode);
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

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

import com.terp.core.data.ExchangeRate;
import com.terp.plugin.CompanyScope;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.WorkingPeriod;
import com.terp.plugin.gui.NumericCells;
import com.terp.plugin.gui.RecordAuditBar;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

public class ExchangeRateEditFormController implements Initializable {

    @FXML
    private Button btnSubmit;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField txtCurrencyCode;
    @FXML
    private DatePicker dtRateDate;
    @FXML
    private TextField txtRate;
    @FXML
    private CheckBox chkActive;

    private ICommonDao<ExchangeRate> exchangeRateDao;
    private ExchangeRate currentRow;
    private ValidationSupport validationSupport;
    private RecordAuditBar auditBar;

    public void initializeForm(ExchangeRate row) {
        this.currentRow = row;
        if (row == null) {
            chkActive.setSelected(true);
            dtRateDate.setValue(LocalDate.now());
            txtRate.setText(NumericCells.format(1d));
            showAudit();
            return;
        }
        txtCurrencyCode.setText(empty(row.getCurrencyCode()));
        dtRateDate.setValue(WorkingPeriod.toLocalDate(row.getRateDate()));
        txtRate.setText(NumericCells.text(row.getRate()));
        chkActive.setSelected(row.getStatus() == ExchangeRate.STATUS_ACTIVE);
        showAudit();
    }

    @FXML
    private void onActionBtnSubmit(ActionEvent event) {
        if (validationSupport.isInvalid() || dtRateDate.getValue() == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Form hatası");
            alert.setHeaderText("Zorunlu alanlar eksik");
            alert.setContentText("Döviz kodu, tarih ve kur zorunludur.");
            alert.show();
            return;
        }
        Double rate = parseRate(txtRate.getText());
        if (rate == null || rate <= 0d) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Form hatası");
            alert.setHeaderText("Geçersiz kur");
            alert.setContentText("Kur 0'dan büyük bir sayı olmalıdır.");
            alert.show();
            return;
        }
        ExchangeRate row = this.currentRow;
        Date now = new Date();
        if (row == null) {
            row = exchangeRateDao.getEmpty();
            if (row == null) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Kayıt hatası");
                alert.setHeaderText("Döviz kuru oluşturulamadı");
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
        row.setCurrencyCode(txtCurrencyCode.getText().trim().toUpperCase(Locale.ROOT));
        row.setRateDate(toDate(dtRateDate.getValue()));
        row.setRate(rate);
        row.setStatus(chkActive.isSelected() ? ExchangeRate.STATUS_ACTIVE : ExchangeRate.STATUS_INACTIVE);
        row.setLastUpdateDate(now);
        if (CompanyScope.rejectDuplicate(
                findDuplicate(row.getCurrencyCode(), row.getRateDate(), row.getRowId()),
                "Döviz kodu ve tarih tekrar ediyor")) {
            return;
        }
        exchangeRateDao.addOrUpdate(row);
        close();
    }

    @FXML
    private void onActionBtnCancel(ActionEvent event) {
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.exchangeRateDao = TerpApplication.getInstance().getPersistence().createDao(ExchangeRate.class);
        this.validationSupport = new ValidationSupport();
        this.validationSupport.registerValidator(txtCurrencyCode, true,
                Validator.createEmptyValidator("Döviz kodu zorunlu"));
        this.validationSupport.registerValidator(txtRate, true,
                Validator.createEmptyValidator("Kur zorunlu"));
        this.auditBar = RecordAuditBar.install(txtCurrencyCode);
        showAudit();
    }

    private ExchangeRate findDuplicate(String currencyCode, Date rateDate, Long excludeRowId) {
        if (currencyCode == null || currencyCode.isBlank() || rateDate == null) {
            return null;
        }
        String escaped = currencyCode.replace("'", "''");
        List<ExchangeRate> rows = exchangeRateDao.findAll("from ExchangeRate e where "
                + CompanyScope.predicate("e")
                + " and e.currencyCode = '" + escaped + "'");
        if (rows == null) {
            return null;
        }
        LocalDate day = WorkingPeriod.toLocalDate(rateDate);
        for (ExchangeRate row : rows) {
            if (row == null || row.getRateDate() == null) {
                continue;
            }
            if (excludeRowId != null && excludeRowId.equals(row.getRowId())) {
                continue;
            }
            if (day.equals(WorkingPeriod.toLocalDate(row.getRateDate()))) {
                return row;
            }
        }
        return null;
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
        return NumericCells.parse(text);
    }

    private static Date toDate(LocalDate local) {
        if (local == null) {
            return new Date();
        }
        return Date.from(local.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}

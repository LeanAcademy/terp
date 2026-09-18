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

import com.terp.cari.data.AccountMovement;
import com.terp.plugin.CompanyScope;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.AccountDirection;
import com.terp.plugin.data.AccountPosting;
import com.terp.plugin.data.DocumentNumbers;
import com.terp.plugin.data.IAccountLookup;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.model.IAccount;
import com.terp.plugin.gui.RecordAuditBar;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.terp.plugin.gui.NumericCells;

public class MovementEditFormController implements Initializable {

    @FXML
    private Button btnSubmit;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField txtDocumentNo;
    @FXML
    private DatePicker dtMovementDate;
    @FXML
    private ComboBox<String> cmbMovementType;
    @FXML
    private TextField txtAmount;
    @FXML
    private ComboBox<String> cmbAccount;
    @FXML
    private TextArea txtNotes;

    private ICommonDao<AccountMovement> movementDao;
    private AccountMovement currentRow;
    private RecordAuditBar auditBar;

    public void initializeForm(AccountMovement row) {
        this.currentRow = row;
        fillLookups();
        if (row != null && row.isDocumentPosted()) {
            CariDates.showError("Cari hareket", "Belge hareketi",
                    "Bu satır bir belgeden geldi. Değişiklik belge üzerinden yapılır.");
            btnSubmit.setDisable(true);
        }
        if (row == null) {
            dtMovementDate.setValue(LocalDate.now());
            cmbMovementType.getSelectionModel().select(AccountDirection.DEBIT);
            showAudit();
            return;
        }
        txtDocumentNo.setText(CariDates.empty(row.getDocumentNo()));
        dtMovementDate.setValue(CariDates.toLocalDate(row.getMovementDate()));
        cmbMovementType.getSelectionModel().select(row.getMovementType());
        txtAmount.setText(NumericCells.text(row.getAmount()));
        CariCombos.select(cmbAccount, row.getAccountCode());
        txtNotes.setText(CariDates.empty(row.getNotes()));
        showAudit();
    }

    @FXML
    private void onActionBtnSubmit(ActionEvent event) {
        String accountCode = CariCombos.codeOf(cmbAccount);
        if (accountCode == null) {
            CariDates.showError("Cari hareket", "Cari eksik", "Cari seçin.");
            return;
        }
        Double amount = CariDates.parseDouble(txtAmount.getText());
        if (amount == null || amount <= 0d) {
            CariDates.showError("Cari hareket", "Tutar", "Tutar sıfırdan büyük olmalıdır.");
            return;
        }
        int typeIndex = cmbMovementType.getSelectionModel().getSelectedIndex();
        if (typeIndex < 0) {
            CariDates.showError("Cari hareket", "Tür", "Borç veya alacak seçin.");
            return;
        }
        Date now = new Date();
        AccountMovement row = this.currentRow;
        if (row == null) {
            row = movementDao.getEmpty();
            if (row == null) {
                CariDates.showError("Cari hareket", "Kayıt hatası", "Hareket oluşturulamadı.");
                return;
            }
            row.setAddedDate(now);
        }
        if (row.isDocumentPosted()) {
            CariDates.showError("Cari hareket", "Kilitli", "Belge hareketi elle değiştirilemez.");
            return;
        }
        if (!CompanyScope.stamp(row)) {
            CariDates.showError("Cari hareket", "Firma",
                    "Önce araç çubuğundan çalışma firmasını seçin.");
            return;
        }
        String documentNo = CariDates.assignDocumentNo(DocumentNumbers.ACCOUNT_MOVEMENT, txtDocumentNo);
        if (documentNo == null || documentNo.isBlank()) {
            CariDates.showError("Cari hareket", "Belge no",
                    "Belge no üretilemedi. Elle yazın veya Sistem yönetimi → Belge numaraları serisini kontrol edin.");
            return;
        }
        row.setDocumentNo(documentNo);
        row.setMovementDate(CariDates.toDate(dtMovementDate.getValue()));
        row.setMovementType(typeIndex);
        row.setAccountCode(accountCode);
        IAccountLookup lookup = TerpApplication.getInstance().getAccountLookup();
        IAccount account = lookup == null ? null : lookup.findByCode(accountCode);
        row.setAccountName(account == null ? CariCombos.nameOf(cmbAccount) : account.getAccountName());
        row.setAmount(amount);
        row.setNotes(CariDates.trimToNull(txtNotes.getText()));
        row.setSourceType(AccountPosting.SOURCE_MANUAL);
        row.setSourceId(null);
        row.setLastUpdateDate(now);
        if (row.getAddedDate() == null) {
            row.setAddedDate(now);
        }
        AccountMovement saved = movementDao.addOrUpdate(row);
        if (saved == null) {
            CariDates.showError("Cari hareket", "Kayıt hatası", "Hareket kaydedilemedi.");
            return;
        }
        close();
    }

    @FXML
    private void onActionBtnCancel(ActionEvent event) {
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.movementDao = TerpApplication.getInstance().getPersistence().createDao(AccountMovement.class);
        cmbMovementType.getItems().addAll(AccountDirection.LABELS);
        fillLookups();
        if (dtMovementDate.getValue() == null) {
            dtMovementDate.setValue(LocalDate.now());
        }
        this.auditBar = RecordAuditBar.install(txtDocumentNo);
        showAudit();
    }

    private void fillLookups() {
        List<String> labels = new ArrayList<>();
        IAccountLookup lookup = TerpApplication.getInstance().getAccountLookup();
        if (lookup != null) {
            List<IAccount> rows = lookup.findActive();
            if (rows != null) {
                for (IAccount account : rows) {
                    labels.add(account.getDisplayLabel());
                }
            }
        }
        CariCombos.fill(cmbAccount, labels);
    }

    private void showAudit() {
        if (auditBar != null) {
            auditBar.bind(currentRow);
        }
    }

    private void close() {
        if (btnCancel != null && btnCancel.getScene() != null
                && btnCancel.getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }
}

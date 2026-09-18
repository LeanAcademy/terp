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

import com.terp.core.data.DocumentTemplate;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

public class TemplateEditFormController implements Initializable {

    @FXML
    private Button btnSubmit;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField txtTemplateCode;
    @FXML
    private TextField txtTemplateName;
    @FXML
    private TextField txtDocumentType;
    @FXML
    private TextArea txtBody;
    @FXML
    private CheckBox chkActive;

    private ICommonDao<DocumentTemplate> templateDao;
    private DocumentTemplate currentRow;
    private ValidationSupport validationSupport;
    private RecordAuditBar auditBar;

    public void initializeForm(DocumentTemplate row) {
        this.currentRow = row;
        if (row == null) {
            chkActive.setSelected(true);
            showAudit();
            return;
        }
        txtTemplateCode.setText(empty(row.getTemplateCode()));
        txtTemplateName.setText(empty(row.getTemplateName()));
        txtDocumentType.setText(empty(row.getDocumentType()));
        txtBody.setText(empty(row.getBody()));
        chkActive.setSelected(row.getStatus() == DocumentTemplate.STATUS_ACTIVE);
        showAudit();
    }

    @FXML
    private void onActionBtnSubmit(ActionEvent event) {
        if (validationSupport.isInvalid()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Form hatası");
            alert.setHeaderText("Zorunlu alanlar eksik");
            alert.setContentText("Kod, ad ve tür zorunludur.");
            alert.show();
            return;
        }
        DocumentTemplate row = this.currentRow;
        Date now = new Date();
        if (row == null) {
            row = templateDao.getEmpty();
            if (row == null) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Kayıt hatası");
                alert.setHeaderText("Belge şablonu oluşturulamadı");
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
        row.setTemplateCode(txtTemplateCode.getText().trim());
        row.setTemplateName(txtTemplateName.getText().trim());
        row.setDocumentType(txtDocumentType.getText().trim());
        row.setBody(txtBody.getText() == null ? "" : txtBody.getText());
        row.setStatus(chkActive.isSelected() ? DocumentTemplate.STATUS_ACTIVE : DocumentTemplate.STATUS_INACTIVE);
        row.setLastUpdateDate(now);
        if (CompanyScope.rejectDuplicate(
                CompanyScope.findDuplicate(templateDao, "DocumentTemplate", "templateCode",
                        row.getTemplateCode(), row.getRowId()),
                "Şablon kodu tekrar ediyor")) {
            return;
        }
        templateDao.addOrUpdate(row);
        close();
    }

    @FXML
    private void onActionBtnCancel(ActionEvent event) {
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.templateDao = TerpApplication.getInstance().getPersistence().createDao(DocumentTemplate.class);
        this.validationSupport = new ValidationSupport();
        this.validationSupport.registerValidator(txtTemplateCode, true,
                Validator.createEmptyValidator("Kod zorunlu"));
        this.validationSupport.registerValidator(txtTemplateName, true,
                Validator.createEmptyValidator("Ad zorunlu"));
        this.validationSupport.registerValidator(txtDocumentType, true,
                Validator.createEmptyValidator("Tür zorunlu"));
        this.auditBar = RecordAuditBar.install(txtTemplateCode);
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

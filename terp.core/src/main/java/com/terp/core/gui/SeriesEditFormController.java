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

import com.terp.core.data.DocumentSeries;
import com.terp.plugin.CompanyScope;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.DocumentNumbers;
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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SeriesEditFormController implements Initializable {

    @FXML
    private Button btnSubmit;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField txtDocumentType;
    @FXML
    private TextField txtTypeLabel;
    @FXML
    private TextField txtPrefix;
    @FXML
    private TextField txtPadding;
    @FXML
    private TextField txtLastNumber;
    @FXML
    private Label lblSample;

    private ICommonDao<DocumentSeries> seriesDao;
    private DocumentSeries currentRow;
    private RecordAuditBar auditBar;

    public void initializeForm(DocumentSeries row) {
        this.currentRow = row;
        if (row == null) {
            showAudit();
            return;
        }
        txtDocumentType.setText(empty(row.getDocumentType()));
        txtTypeLabel.setText(empty(row.getTypeLabel()));
        txtPrefix.setText(empty(row.getPrefix()));
        txtPadding.setText(Integer.toString(row.getPadding()));
        txtLastNumber.setText(Long.toString(row.getLastNumber()));
        updateSample();
        showAudit();
    }

    @FXML
    private void onActionBtnSubmit(ActionEvent event) {
        if (currentRow == null) {
            return;
        }
        Integer padding = parseInt(txtPadding.getText());
        Long lastNumber = parseLong(txtLastNumber.getText());
        if (padding == null || padding < 1 || padding > 12) {
            showError("Hane 1 ile 12 arasında olmalıdır.");
            return;
        }
        if (lastNumber == null || lastNumber < 0L) {
            showError("Son no sıfır veya pozitif olmalıdır.");
            return;
        }
        if (!CompanyScope.stamp(currentRow)) {
            showError("Önce araç çubuğundan çalışma firmasını seçin.");
            return;
        }
        Date now = new Date();
        currentRow.setPrefix(trimToNull(txtPrefix.getText()) == null ? "" : txtPrefix.getText().trim());
        currentRow.setPadding(padding);
        currentRow.setLastNumber(lastNumber);
        currentRow.setLastUpdateDate(now);
        if (currentRow.getAddedDate() == null) {
            currentRow.setAddedDate(now);
        }
        seriesDao.addOrUpdate(currentRow);
        close();
    }

    @FXML
    private void onActionBtnCancel(ActionEvent event) {
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.seriesDao = TerpApplication.getInstance().getPersistence().createDao(DocumentSeries.class);
        txtPrefix.textProperty().addListener((obs, old, value) -> updateSample());
        txtPadding.textProperty().addListener((obs, old, value) -> updateSample());
        txtLastNumber.textProperty().addListener((obs, old, value) -> updateSample());
        this.auditBar = RecordAuditBar.install(txtPrefix);
        showAudit();
    }

    private void updateSample() {
        if (lblSample == null) {
            return;
        }
        Integer padding = parseInt(txtPadding.getText());
        Long last = parseLong(txtLastNumber.getText());
        if (padding == null || last == null) {
            lblSample.setText("");
            return;
        }
        String next = DocumentSeries.format(txtPrefix.getText(), padding, last + 1);
        lblSample.setText("Sonraki: " + next);
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

    private static void showError(String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Belge serisi");
        alert.setHeaderText("Kayıt hatası");
        alert.setContentText(content);
        alert.show();
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

    private static Integer parseInt(String text) {
        if (text == null || text.isBlank()) {
            return DocumentNumbers.DEFAULT_PADDING;
        }
        try {
            return Integer.valueOf(text.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Long parseLong(String text) {
        if (text == null || text.isBlank()) {
            return 0L;
        }
        try {
            return Long.valueOf(text.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

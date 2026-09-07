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

import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.stok.data.Warehouse;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

public class WarehouseEditFormController implements Initializable {

    @FXML
    private Button btnSubmit;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField txtWarehouseCode;
    @FXML
    private TextField txtWarehouseName;
    @FXML
    private ComboBox<String> cmbWarehouseType;
    @FXML
    private CheckBox chkActive;
    @FXML
    private CheckBox chkAllowNegative;
    @FXML
    private TextField txtCity;
    @FXML
    private TextArea txtAddress;
    @FXML
    private TextArea txtNotes;

    private ICommonDao<Warehouse> warehouseDao;
    private Warehouse currentRow;
    private ValidationSupport validationSupport;

    public void initializeForm(Warehouse row) {
        this.currentRow = row;
        if (row == null) {
            cmbWarehouseType.getSelectionModel().select(Warehouse.TYPE_NORMAL);
            chkActive.setSelected(true);
            chkAllowNegative.setSelected(false);
            return;
        }
        txtWarehouseCode.setText(empty(row.getWarehouseCode()));
        txtWarehouseName.setText(empty(row.getWarehouseName()));
        cmbWarehouseType.getSelectionModel().select(row.getWarehouseType());
        chkActive.setSelected(row.getStatus() == 0);
        chkAllowNegative.setSelected(row.isNegativeAllowed());
        txtCity.setText(empty(row.getCity()));
        txtAddress.setText(empty(row.getAddress()));
        txtNotes.setText(empty(row.getNotes()));
    }

    @FXML
    private void onActionBtnSubmit(ActionEvent event) {
        if (validationSupport.isInvalid()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Form hatası");
            alert.setHeaderText("Zorunlu alanlar eksik");
            alert.setContentText("Depo kodu ve adı zorunludur.");
            alert.show();
            return;
        }
        Warehouse row = this.currentRow;
        Date now = new Date();
        if (row == null) {
            row = warehouseDao.getEmpty();
            if (row == null) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Kayıt hatası");
                alert.setHeaderText("Depo oluşturulamadı");
                alert.show();
                return;
            }
            row.setAddedDate(now);
        }
        row.setWarehouseCode(txtWarehouseCode.getText().trim());
        row.setWarehouseName(txtWarehouseName.getText().trim());
        int typeIndex = cmbWarehouseType.getSelectionModel().getSelectedIndex();
        row.setWarehouseType(typeIndex < 0 ? Warehouse.TYPE_NORMAL : typeIndex);
        row.setStatus(chkActive.isSelected() ? 0 : 1);
        row.setAllowNegative(chkAllowNegative.isSelected() ? 1 : 0);
        row.setCity(trimToNull(txtCity.getText()));
        row.setAddress(trimToNull(txtAddress.getText()));
        row.setNotes(trimToNull(txtNotes.getText()));
        row.setLastUpdateDate(now);
        warehouseDao.addOrUpdate(row);
        close();
    }

    @FXML
    private void onActionBtnCancel(ActionEvent event) {
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.warehouseDao = TerpApplication.getInstance().getPersistence().createDao(Warehouse.class);
        cmbWarehouseType.getItems().addAll(Warehouse.TYPE_LABELS);
        cmbWarehouseType.getSelectionModel().select(Warehouse.TYPE_NORMAL);
        this.validationSupport = new ValidationSupport();
        this.validationSupport.registerValidator(txtWarehouseCode, true,
                Validator.createEmptyValidator("Kod zorunlu"));
        this.validationSupport.registerValidator(txtWarehouseName, true,
                Validator.createEmptyValidator("Ad zorunlu"));
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

/*
 * Copyright (C) 2017 cevdet
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.terp.core.gui;

import com.terp.core.data.Company;
import com.terp.core.model.CompanyTableModel;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.model.ICompany;
import com.terp.plugin.gui.RecordAuditBar;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

/**
 * FXML Controller class
 *
 * @author cevdet
 */
public class EditCompanyFormController implements Initializable {
    
//<editor-fold defaultstate="collapsed" desc="FXML variables">
    @FXML
    private TextArea txtNotes;
    
    @FXML
    private TextField txtStateTaxId;
    
    @FXML
    private TextArea txtAddress;
    
    @FXML
    private TextField txtEmail;
    
    @FXML
    private TextField txtCompanyLongName;
    
    @FXML
    private TextField txtCompanyName;
    
    @FXML
    private TextField txtStateTaxRegion;
    
    @FXML
    private CheckBox chkActive;
    
    @FXML
    private TextField txtFax;
    
    @FXML
    private TextField txtPhone;
    
    @FXML
    private Button btnCancel;
    
    @FXML
    private Button btnSubmit;
    
    @FXML
    private TextField txtRowId;
    
    @FXML
    private TextField txtCountry;
    
    @FXML
    private TextField txtRegion;
    
    @FXML
    private TextField txtCity;
    
//</editor-fold>
    
    // Company data access object
    ICommonDao<ICompany> companyDao;
    private RecordAuditBar auditBar;

    private ICompany currentRow;
    
    // field validation support
    ValidationSupport validationSupport;
    
    // email regex
    private static final String EMAIL_REGEX = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]"
            + "+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
    
    /**
     * Cancel button click action handler
     * close dialog and clear all data
     */
    @FXML
    private void onActionBtnCancel(){
        //unload form and exit
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Submit button click action handler
     * 
     * Validate data and save them into database
     */
    @FXML
    private void onActionBtnSubmit(){
        assert (this.companyDao != null) : "Database connetion not set";
        if(this.validationSupport.isInvalid()){
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Page error");
            alert.setHeaderText("Form data error");
            alert.setContentText("Form data has error. Please corect and try again");
            alert.show();
            return;
        }
        
        // save and commit
        
        // create new company object
        ICompany newCompany = this.currentRow;
        if (newCompany == null) {
            newCompany = this.companyDao.getEmpty();
        }
        if (newCompany == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Save error");
            alert.setHeaderText("Could not update company record");
            alert.show();
            return;
        }
        
        applyIfPresent(txtCompanyName, newCompany::setCompanyName);
        applyIfPresent(txtCompanyLongName, newCompany::setCompanyLongName);
        applyIfPresent(txtStateTaxRegion, newCompany::setStateTaxRegion);
        applyIfPresent(txtStateTaxId, newCompany::setStateTaxCode);
        applyIfPresent(txtAddress, newCompany::setAddress);
        applyIfPresent(txtCity, newCompany::setCity);
        applyIfPresent(txtRegion, newCompany::setRegion);
        applyIfPresent(txtCountry, newCompany::setCountry);
        applyIfPresent(txtPhone, newCompany::setPhone);
        applyIfPresent(txtFax, newCompany::setFax);
        applyIfPresent(txtEmail, newCompany::setEmail);
        newCompany.setNotes(textOf(txtNotes));
        newCompany.setStatus(this.chkActive.isSelected() ? 0 : 1);
        
        // save and commit
        this.companyDao.addOrUpdate(newCompany);
        Stage stage = (Stage) btnSubmit.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // create model
        CompanyTableModel tableModel = new CompanyTableModel();
        
        // TODO : create data from database
        this.companyDao = TerpApplication.getInstance()
                .getPersistence().<ICompany>createDao(Company.class);
        this.auditBar = RecordAuditBar.install(txtCompanyName);
        this.auditBar.bind(null);
        
        // set validation for text fields
        this.validationSupport = new ValidationSupport();
        this.validationSupport.registerValidator(txtCompanyName, true, 
                Validator.createEmptyValidator("Company name is required"));
        this.validationSupport.registerValidator(txtCompanyLongName, true, 
                Validator.createEmptyValidator("Company long name is required"));
        this.validationSupport.registerValidator(txtStateTaxRegion, true, 
                Validator.createEmptyValidator("Tax region is required"));
        this.validationSupport.registerValidator(txtStateTaxId, true, 
                Validator.createEmptyValidator("Tax ID is required"));
        this.validationSupport.registerValidator(txtAddress, true, 
                Validator.createEmptyValidator("Address is required"));
        this.validationSupport.registerValidator(txtCountry, true, 
                Validator.createEmptyValidator("Country is required"));
        this.validationSupport.registerValidator(txtRegion, true, 
                Validator.createEmptyValidator("Region is required"));
        this.validationSupport.registerValidator(txtCity, true, 
                Validator.createEmptyValidator("City is required"));
        this.validationSupport.registerValidator(txtEmail, true, 
                Validator.createRegexValidator("Wrong email", EMAIL_REGEX, 
                Severity.ERROR));
    }

    public void initializeForm(ICompany row) {
        this.currentRow = row;
        this.txtAddress.setText(empty(row.getAddress()));
        this.txtCity.setText(empty(row.getCity()));
        this.txtCompanyLongName.setText(empty(row.getCompanyLongName()));
        this.txtCompanyName.setText(empty(row.getCompanyName()));
        this.txtCountry.setText(empty(row.getCountry()));
        this.txtEmail.setText(empty(row.getEmail()));
        this.txtFax.setText(empty(row.getFax()));
        this.txtNotes.setText(empty(row.getNotes()));
        this.txtPhone.setText(empty(row.getPhone()));
        this.txtRegion.setText(empty(row.getRegion()));
        this.txtRowId.setText(row.getRowId() == null ? "" : row.getRowId().toString());
        this.txtStateTaxId.setText(empty(row.getStateTaxCode()));
        this.txtStateTaxRegion.setText(empty(row.getStateTaxRegion()));
        this.chkActive.setSelected(row.getStatus() == 0);
        if (auditBar != null) {
            auditBar.bind(row);
        }
    }

    private static void applyIfPresent(TextInputControl field, Consumer<String> setter) {
        String value = textOf(field);
        if (!value.isEmpty()) {
            setter.accept(value);
        }
    }

    private static String textOf(TextInputControl field) {
        return empty(field == null ? null : field.getText());
    }

    private static String empty(String value) {
        return value == null ? "" : value;
    }
}

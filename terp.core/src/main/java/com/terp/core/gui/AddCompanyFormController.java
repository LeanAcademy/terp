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

import com.terp.core.model.CompanyTableModel;
import com.terp.plugin.IUser;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.IDatabaseFactory;
import com.terp.plugin.data.dao.ICompanyDao;
import com.terp.plugin.data.model.ICompany;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

/**
 * FXML Controller class
 *
 * @author cevdet
 */
public class AddCompanyFormController implements Initializable {
    
//<editor-fold defaultstate="collapsed" desc="FXML variables">
    @FXML
    private TextArea txtNotes;
    
    @FXML
    private Label lblChangedByAtDate;
    
    @FXML
    private TextField txtStateTaxId;
    
    @FXML
    private TextArea txtAddress;
    
    @FXML
    private TextField txtEmail;
    
    @FXML
    private TextField txtCompanyLongName;
    
    @FXML
    private Label lblAddedByAtDate;
    
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
    
    // Data base factory object
    IDatabaseFactory db;
    
    // Active user
    IUser user;
    
    // Company data access object
    ICompanyDao companyDao;
    
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
        ICompany newCompany = this.companyDao.getEmpty();
        if (newCompany == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Save error");
            alert.setHeaderText("Could not create company record");
            alert.setContentText("The company entity could not be created.");
            alert.show();
            return;
        }
        
        // check company name and set it
        if(!this.txtCompanyName.getText().isEmpty()){
            newCompany.setCompanyName(this.txtCompanyName.getText());
        }
        
        // check company long name
        if(!this.txtCompanyLongName.getText().isEmpty()){
            newCompany.setCompanyLongName(this.txtCompanyLongName.getText());
        }
        
        // check tax region
        if(!this.txtStateTaxRegion.getText().isEmpty()){
            newCompany.setStateTaxRegion(this.txtStateTaxRegion.getText());
        }
        
        // check tax id
        if(!this.txtStateTaxId.getText().isEmpty()){
            newCompany.setStateTaxCode(this.txtStateTaxId.getText());
        }
        
        // check adress
        if(!this.txtAddress.getText().isEmpty()){
            newCompany.setAddress(this.txtAddress.getText());
        }
        
        // check city
        if(!this.txtCity.getText().isEmpty()){
            newCompany.setCity(this.txtCity.getText());
        }
        
        // check region
        if(!this.txtRegion.getText().isEmpty()){
            newCompany.setRegion(this.txtRegion.getText());
        }
        
        // check country
        if(!this.txtCountry.getText().isEmpty()){
            newCompany.setCountry(this.txtCountry.getText());
        }
        
        // check phone
        if(!this.txtPhone.getText().isEmpty()){
            newCompany.setPhone(this.txtPhone.getText());
        }
        
        // check fax
        if(!this.txtFax.getText().isEmpty()){
            newCompany.setFax(this.txtFax.getText());
        }
        
        // check email
        if(!this.txtEmail.getText().isEmpty()){
            newCompany.setEmail(this.txtEmail.getText());
        }

        newCompany.setNotes(this.txtNotes.getText());
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
        this.db = TerpApplication.getInstance().getDatabaseFactory();
        
        // get active user
        this.user = TerpApplication.getInstance().getUser();
        
        // get company table DAO
        this.companyDao = this.db.getCompanyDao();
        
        // set user information
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        
        String strAddedByAtDate = "Added by " + this.user.getUserName() + " at "
                + dtf.format(date);
        this.lblAddedByAtDate.setText(strAddedByAtDate);
        
        String strChangedByAtDate = "Changed by " + this.user.getUserName() + " at "
                + dtf.format(date);
        this.lblChangedByAtDate.setText(strChangedByAtDate); 
        
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
                Validator.createRegexValidator("Wrong email", EMAIL_REGEX, Severity.ERROR));
    }
}

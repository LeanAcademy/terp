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
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
    
    @FXML
    private void onActionBtnCancel(){
        //unload form and exit
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void onActionBtnSubmit(){
        //save and commit
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
        
        // set user information
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        
        String strAddedByAtDate = "Added by " + this.user.getUserName() + " at "
                + dtf.format(date);
        this.lblAddedByAtDate.setText(strAddedByAtDate);
        
        String strChangedByAtDate = "Changed by " + this.user.getUserName() + " at "
                + dtf.format(date);
        this.lblChangedByAtDate.setText(strChangedByAtDate);
    }
}

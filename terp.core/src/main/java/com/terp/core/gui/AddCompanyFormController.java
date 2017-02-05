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

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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
//</editor-fold>
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}

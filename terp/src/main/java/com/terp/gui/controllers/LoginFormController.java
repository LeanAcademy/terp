/*
 * Copyright (C) 2016 cevdet
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
package com.terp.gui.controllers;

import com.terp.main.TerpMainApplication;
import com.terp.plugin.TerpApplication;
import com.terp.users.User;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author cevdet
 */
public class LoginFormController implements Initializable {
    
    private TerpMainApplication mainApp;
    
    @FXML private TextField txtUserName;

    @FXML private Button btnSubmit;

    @FXML private PasswordField txtPassword;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.txtPassword.setText("Admin");
        this.txtUserName.setText("Admin");
    }    
    
    @FXML private void btnSubmitOnAction(ActionEvent event){
        
        //local variables
        String _usr;
        String _pwd;
        String _hashedPwd;
        MessageDigest _md;
        byte [] _bytes;
            
        try {    
            //get application
            TerpApplication app = TerpApplication.getInstance();
            
            
            
            //get inputs
            _usr = this.txtUserName.getText();
            _pwd = this.txtPassword.getText();
            
            //validate inputs
            if(_usr == null || "".equals(_usr)
                    || _pwd == null || "".equals(_pwd)){
                //error
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Input error");
                alert.setHeaderText("User name or pasword is missing");
                alert.setContentText("Please complete the inputs!");
                alert.showAndWait();
                
                //return
                return;
            }
            
            //change password text to MD5 hash
            _md = MessageDigest.getInstance("MD5");
            _bytes = _pwd.getBytes();
            _md.update(_bytes);
            _hashedPwd = new String(_md.digest());
            
            //check user
            User mUser = new User();
            mUser.setUsername(_usr);
            mUser.setPassword(_hashedPwd);
            mUser.login();
            
            //show error
            if(mUser.isAuthenticated()){
                app.setUser(mUser);
                
                //show main frame
                this.mainApp.startMainGui();
                
            }else{
                // TODO: add user not authenticated exception
                LOG.log(Level.INFO, "User not authenticated");
            }
            
        } catch (NoSuchAlgorithmException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    public void setApplication(TerpMainApplication mainApp){
        this.mainApp = mainApp;
    }
    
    private static final Logger LOG = Logger.getLogger(LoginFormController.class.getName());
}


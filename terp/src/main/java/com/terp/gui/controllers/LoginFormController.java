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
import com.terp.users.PasswordHashes;
import com.terp.users.User;
import java.net.URL;
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

    @FXML private Button btnCancel;

    @FXML private PasswordField txtPassword;

    private boolean switchUser;
    private Runnable onAuthenticated;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.txtPassword.setText("Admin");
        this.txtUserName.setText("Admin");
    }

    public void prepareSwitchUser(Runnable onAuthenticated) {
        this.switchUser = true;
        this.onAuthenticated = onAuthenticated;
        this.txtUserName.clear();
        this.txtPassword.clear();
        this.btnSubmit.setText("Giriş");
        if (this.btnCancel != null) {
            this.btnCancel.setVisible(true);
            this.btnCancel.setManaged(true);
        }
    }

    @FXML
    private void btnCancelOnAction(ActionEvent event) {
        closeWindow();
    }

    @FXML private void btnSubmitOnAction(ActionEvent event){
        
        //local variables
        String _usr;
        String _pwd;
            
        try {    
            TerpApplication app = TerpApplication.getInstance();
            _usr = this.txtUserName.getText();
            _pwd = this.txtPassword.getText();
            
            if(_usr == null || "".equals(_usr)
                    || _pwd == null || "".equals(_pwd)){
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Input error");
                alert.setHeaderText("User name or pasword is missing");
                alert.setContentText("Please complete the inputs!");
                alert.showAndWait();
                return;
            }

            User mUser = new User();
            mUser.setUsername(_usr);
            mUser.setPassword(PasswordHashes.md5(_pwd));
            mUser.login();
            
            if(mUser.isAuthenticated()){
                app.setUser(mUser);
                if (this.switchUser) {
                    if (this.onAuthenticated != null) {
                        this.onAuthenticated.run();
                    }
                    closeWindow();
                } else if (this.mainApp != null) {
                    this.mainApp.startMainGui();
                }
            }else{
                LOG.log(Level.INFO, "User not authenticated");
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Giriş");
                alert.setHeaderText("Kullanıcı doğrulanamadı");
                alert.setContentText("Kullanıcı adı, şifre veya hesap durumu hatalı.");
                alert.showAndWait();
            }
            
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    public void setApplication(TerpMainApplication mainApp){
        this.mainApp = mainApp;
    }

    private void closeWindow() {
        if (this.btnSubmit == null || this.btnSubmit.getScene() == null) {
            return;
        }
        javafx.stage.Window window = this.btnSubmit.getScene().getWindow();
        if (window instanceof javafx.stage.Stage stage) {
            stage.close();
        }
    }
    
    private static final Logger LOG = Logger.getLogger(LoginFormController.class.getName());
}


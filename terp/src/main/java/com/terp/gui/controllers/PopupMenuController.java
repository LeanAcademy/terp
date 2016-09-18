/*
 * Copyright (C) 2016 Your Organisation
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

import com.terp.plugin.TerpApplication;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author cevdet
 */
public class PopupMenuController implements Initializable {
    
    @FXML
    private Button btnServerSetup;

    @FXML
    private AnchorPane apPopupContainer;

    @FXML
    private Button btnProgramSettings;

    @FXML
    private Button btnExitProgram;

    @FXML
    private Button btnSqlEditor;

    @FXML
    private Button btnUserSettings;

    @FXML
    private Button btnCompnay;

    @FXML
    void btnServerOnAction(ActionEvent event) {
        
        TerpApplication terpApp = TerpApplication.getInstance();
        
        try {
            // load program setting dialog
            Stage stage = new Stage();
            Parent parent = FXMLLoader.load(getClass()
                    .getResource("/fxml/ServerForm.fxml"));
            stage.setScene(new Scene(parent));
            stage.setTitle("Server settings");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(terpApp.getDesktopManager().getPrimaryStage());
            stage.show();
            
        } catch (IOException ex) {
            Logger.getLogger(PopupMenuController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    void btnProgramSettingsOnAction(ActionEvent event) {

    }

    @FXML
    void btnExitProgramOnAction(ActionEvent event) {

    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}

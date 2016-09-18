package com.terp.gui.controllers;

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

import com.terp.plugin.TerpApplication;
import com.terp.util.TerpProperties;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

/**
 * FXML Controller class
 *
 * @author cevdet
 */
public class ServerFormController implements Initializable {

    private String mainClass = null;
    private JarFile jarFile = null;
    
    @FXML
    private TextField txtPort;

    @FXML
    private TextField txtServerName;

    @FXML
    private Button btnCancel;

    @FXML
    private TextField txtCatalog;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnBrowse;

    @FXML
    private Button btnTest;

    @FXML
    private TextArea txtaInformation;

    @FXML
    private TextField txtDriver;

    @FXML
    void btnBrowseOnAction(ActionEvent event) {
        
        // get primary stage
        TerpApplication terpApp = TerpApplication.getInstance();
        TerpProperties terpProps = TerpProperties.getInstance();
        

        // find file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select jdbc driver file");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Java application file (*.jar)", "*.jar")
            );
        File file = fileChooser.showOpenDialog(
                terpApp.getDesktopManager().getPrimaryStage());
        
        // load manifest file
        try {
            // read manifest file from jar archive
            this.txtaInformation.setText("Loading file : " + file.getAbsolutePath());
            this.jarFile = new JarFile(file);
            Manifest mf = this.jarFile.getManifest();
            
            // check version
            Attributes attr = mf.getMainAttributes();
            
            
            // get main class
            this.mainClass = mf.getMainAttributes().getValue("Main-Class");
            
            // add main class name to text field
            this.txtDriver.setText(this.mainClass);
            terpProps.getHibernateProps()
                    .setProperty("hibernate.connection.driver_class", mainClass);
            
            // TODO check version of file
            
            
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            
            // TODO : add alert dialog
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Driver class loading error");
            alert.setContentText(ex.getMessage());
            
        }
        
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {

    }

    @FXML
    void btnTestOnAction(ActionEvent event) {

    }

    @FXML
    void btnCancelOnAction(ActionEvent event) {

    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    private final static Logger LOG = Logger.getLogger(
            ServerFormController.class.getName());
}

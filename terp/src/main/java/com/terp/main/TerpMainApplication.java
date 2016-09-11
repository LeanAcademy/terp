/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.terp.main;

import com.terp.data.impl.Database;
import com.terp.gui.controllers.LoginFormController;
import com.terp.plugin.TerpApplication;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author cevdet
 */
public class TerpMainApplication extends Application {
    // main stage
    private Stage stage = null;
    
    /**
     * show main form 
     */
    public void showMainForm(){
        
        //load main frame
        try {
            Parent root = FXMLLoader.load(getClass()
                    .getResource("/fxml/TerpMainForm.fxml"));
            
            Scene scene = new Scene(root);
            
            this.stage.setScene(scene);
            this.stage.setMaximized(true);
            this.stage.show();
            
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * start 
     * @param stage
     * @throws Exception 
     */
    @Override
    public void start(Stage stage) throws Exception {
        
        // create database and save it
        Database db = new Database();
        TerpApplication terpApp = TerpApplication.getInstance();
        terpApp.setDatabase(db);
        
        // stage
        this.stage = stage;
        this.stage.setOnCloseRequest(onCloseRequest);
        showLoginForm();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * show login form 
     */
    public void showLoginForm(){
        
        try {
            // load login form fxml
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/fxml/LoginForm.fxml"));
            Parent root = (Parent)loader.load();
            
            // passing application instance to login form
            LoginFormController controller = loader.<LoginFormController>getController();
            controller.setApplication(this);
            
            // show form
            Scene scene = new Scene(root);
            this.stage.setScene(scene);
            this.stage.show();
            
            
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    private final EventHandler onCloseRequest = new EventHandler<WindowEvent>(){
        @Override
        public void handle(WindowEvent event) {
            System.out.print("Terp ending");
            System.exit(0);
        }
        
    };
    
    private static final Logger LOG = Logger.getLogger(LoginFormController.class.getName());
}

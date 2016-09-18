/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.terp.main;

import com.terp.data.Database;
import com.terp.gui.controllers.LoginFormController;
import com.terp.gui.controllers.TerpMainFormController;
import com.terp.plugin.TerpApplication;
import com.terp.plugins.PluginFactoryImpl;
import com.terp.util.TerpClassLoader;
import com.terp.util.TerpProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author cevdet
 */
public class TerpMainApplication extends Application {
    
    // application header
    private final String APP_TITLE = "T - ERP SYSTEM";
    
    // main stage
    private Stage stage = null;
    
    // properties of all application
    TerpProperties terpProp = TerpProperties.getInstance();
    
    /**
     * show main form 
     */
    public void startMainGui(){
        
        //load plugins
        loadPlugins();
        
        //load main frame
        try {
            
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/fxml/TerpMainForm.fxml"));
            Parent root = loader.load();
            
            TerpMainFormController controller = loader.<TerpMainFormController>getController();
            
            Scene scene = new Scene(root);
            
            this.stage.setScene(scene);
            this.stage.setMaximized(true);
            this.stage.setTitle(APP_TITLE);
            controller.setPrimaryStage(stage);
            this.stage.show();
            
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * load properties
     */
    private void loadProperties() throws IOException{
        //load hibernate database connection properties
        Properties props = new Properties();
            
        props.load(new FileInputStream("../terp/etc/hibernate.properties"));
        
        // save hibernate properties in to class        
        terpProp.setHibernateProps(props);
    }
    
    /**
     * save properties
     */
    private void saveProperties(){}
    
    /**
     * load plugins
     */
    private void loadPlugins(){
        PluginFactoryImpl pluginFactory = new PluginFactoryImpl();
        pluginFactory.loadAllPlugin();
        TerpApplication terpApp = TerpApplication.getInstance();
        terpApp.setPluginFactory(pluginFactory);
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
        
        // load properties
        try {
            
            loadProperties();
            
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("File \"hibernate.properties\" not found.");
            alert.setContentText(ex.getMessage());
            alert.show();
            return;
        }
        
        // check driver file
        if(terpProp.getHibernateProps().getProperty("driver.jarfile.name") == null){
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Driver file is not setup. "
                    + "Please edit \"hibernate.properties\" file and setup it.");
            alert.show();
            return;
        }
        
        // load driver file
        File driver = new File(
                terpProp.getHibernateProps().getProperty("driver.jarfile.name")
        );
        
        // load driver class and register in classpath
        // TerpClassLoader is used to do. Driver will be automaticaly 
        // added to classpath
        try {
            TerpClassLoader.addFile(driver);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Driver file is not found.\n"
                    + "Please edit \"hibernate.properties\" \nfile " 
                    + driver.getPath());
            alert.show();
            return;
        }
        
        // login form load and show
        // set main application to login controller to allow access
        // main application is used as callback after rigt authorization
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
            this.stage.setTitle(APP_TITLE);
            this.stage.show();
            
            
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Login form cannot be loaded");
            alert.show();
        }
    }
    
    /**
     * close event
     * it is used because of main thread do not terminate w.o. this
     */
    private final EventHandler onCloseRequest = new EventHandler<WindowEvent>(){
        @Override
        public void handle(WindowEvent event) {
            System.out.print("Terp ending");
            System.exit(0);
        }
        
    };
    
    /**
     * logger
     */
    private static final Logger LOG = Logger.getLogger(TerpMainApplication.class.getName());
}

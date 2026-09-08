/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.terp.main;

import com.terp.data.DatabaseFactoryImpl;
import com.terp.data.PersistenceImpl;
import com.terp.gui.IconFactoryImpl;
import com.terp.gui.controllers.LoginFormController;
import com.terp.gui.controllers.TerpMainFormController;
import com.terp.plugin.TerpApplication;
import com.terp.plugins.PluginFactoryImpl;
import com.terp.util.HibernateUtil;
import com.terp.util.TerpHome;
import com.terp.util.TerpProperties;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

// <editor-fold defaultstate="collapsed" desc=" Private variables ">
    // application header
    private final String APP_TITLE = "T - ERP SYSTEM";

    // main stage
    private Stage stage = null;

    // properties of all application
    TerpProperties terpProp = TerpProperties.getInstance();

    // terp main application holder
    TerpApplication app = TerpApplication.getInstance();

    private PluginFactoryImpl pluginFactory;

    /**
     * logger
     */
    private static final Logger LOG = Logger.getLogger(
            TerpMainApplication.class.getName());

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Private routines ">
    /**
     * load properties
     */
    private void loadProperties() throws IOException {
        Path hibernateFile = TerpHome.etcFile("hibernate.properties");
        Path exampleFile = TerpHome.etcFile("hibernate.properties.example");
        if (!Files.exists(hibernateFile) && Files.exists(exampleFile)) {
            Files.copy(exampleFile, hibernateFile);
            LOG.log(Level.INFO, "Created {0} from example", hibernateFile);
        }

        Properties hibernateProps = new Properties();
        try (InputStream in = Files.newInputStream(hibernateFile)) {
            hibernateProps.load(in);
        }
        resolveEmbeddedDerbyUrl(hibernateProps);
        terpProp.setHibernateProps(hibernateProps);

        Path viewFile = TerpHome.etcFile("application.properties");
        Properties guiProps = new Properties();
        if (Files.exists(viewFile)) {
            try (InputStream in = Files.newInputStream(viewFile)) {
                guiProps.load(in);
            }
        }
        terpProp.setViewProps(guiProps);

        Files.createDirectories(TerpHome.pluginsDir());
        Files.createDirectories(TerpHome.libDir());
    }

    /**
     * Make relative embedded Derby URLs absolute under terp.home.
     */
    private void resolveEmbeddedDerbyUrl(Properties props) {
        String url = props.getProperty("hibernate.connection.url");
        if (url == null || !url.startsWith("jdbc:derby:")) {
            return;
        }
        String rest = url.substring("jdbc:derby:".length());
        if (rest.startsWith("//") || rest.startsWith("memory:")) {
            return;
        }
        int semi = rest.indexOf(';');
        String dbPath = semi < 0 ? rest : rest.substring(0, semi);
        String suffix = semi < 0 ? "" : rest.substring(semi);
        Path resolved = Path.of(dbPath);
        if (!resolved.isAbsolute()) {
            resolved = TerpHome.get().resolve(dbPath).normalize();
        }
        props.setProperty("hibernate.connection.url", "jdbc:derby:" + resolved + suffix);
    }

    /**
     * save properties
     */
    private void saveProperties() throws IOException {
        Properties guiProps = terpProp.getViewProps();
        if (guiProps == null) {
            return;
        }
        Path viewFile = TerpHome.etcFile("application.properties");
        Files.createDirectories(viewFile.getParent());
        try (OutputStream out = Files.newOutputStream(viewFile)) {
            guiProps.store(out, null);
        }
    }

    private void scanPluginsAndInitHibernate() {
        pluginFactory = new PluginFactoryImpl();
        pluginFactory.scanJars();
        app.setPluginFactory(pluginFactory);
        app.setPersistence(new PersistenceImpl());
        HibernateUtil.initialize(pluginFactory.persistentClasses(), pluginFactory.classLoaders());
    }

    /**
     * close event it is used because of main thread do not terminate w.o. this
     */
    private final EventHandler onCloseRequest = new EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent event) {
            try {
                saveProperties();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            System.out.print("Terp ending");
            System.exit(0);
        }

    };

// </editor-fold>    
// <editor-fold defaultstate="collapsed" desc=" Overrides ">
    /**
     * start
     *
     * @param stage
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception {
        // stage
        this.stage = stage;
        this.stage.setOnCloseRequest(onCloseRequest);
        showLoginForm();
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Public routines ">
    /**
     * show main form
     */
    public void startMainGui() {

        if (pluginFactory != null) {
            pluginFactory.bindRegistry();
        }

        //create database factory
        app.setDatabaseFactory(new DatabaseFactoryImpl());

        //create icon factory
        app.setIconFactory(new IconFactoryImpl());

        //load main frame
        try {

            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/fxml/TerpMainForm.fxml"));
            loader.setClassLoader(getClass().getClassLoader());

            Parent root = loader.load();

            TerpMainFormController controller = loader.<TerpMainFormController>getController();

            Scene scene = new Scene(root);

            this.stage.setScene(scene);
            this.stage.setMaximized(true);
            this.stage.setTitle(APP_TITLE);
            controller.setPrimaryStage(stage);

            if (pluginFactory != null) {
                pluginFactory.runBoundPlugins();
            }

            controller.applySessionContext();

            this.stage.show();

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

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
    public void showLoginForm() {

        try {
            loadProperties();
            scanPluginsAndInitHibernate();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);

            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("File \"hibernate.properties\" not found.");
            alert.setContentText("Copy terp/etc/hibernate.properties.example to "
                    + "terp/etc/hibernate.properties\n" + ex.getMessage());
            alert.show();
            return;
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, null, ex);
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Database or plugin startup failed");
            alert.setContentText(ex.getMessage());
            alert.show();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/fxml/LoginForm.fxml"));
            loader.setClassLoader(getClass().getClassLoader());
            Parent root = (Parent) loader.load();

            LoginFormController controller = loader.<LoginFormController>getController();
            controller.setApplication(this);

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

// </editor-fold>
}

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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import com.terp.plugin.TerpApplication;
import com.terp.util.JdbcDriverSupport;
import com.terp.util.TerpHome;
import com.terp.util.TerpProperties;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.ResourceBundle;
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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ServerFormController implements Initializable {

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
    private TextField txtUser;
    @FXML
    private PasswordField txtPassword;

    @FXML
    void btnBrowseOnAction(ActionEvent event) {
        TerpApplication terpApp = TerpApplication.getInstance();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("JDBC sürücü JAR");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Java arşivi (*.jar)", "*.jar"));
        File file = fileChooser.showOpenDialog(
                terpApp.getDesktopManager().getPrimaryStage());
        if (file == null) {
            return;
        }
        try (JarFile jarFile = new JarFile(file)) {
            Manifest mf = jarFile.getManifest();
            String mainClass = mf == null ? null : mf.getMainAttributes().getValue("Main-Class");
            if (mainClass != null && !mainClass.isBlank()) {
                txtDriver.setText(mainClass);
            }
            txtaInformation.setText("Sürücü dosyası: " + file.getAbsolutePath()
                    + (mainClass == null ? "" : "\nMain-Class: " + mainClass)
                    + "\nJAR'ı terp/lib altına kopyalayın; bağlantı yeniden başlatınca yüklenir.");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            showError("Sürücü okunamadı", ex.getMessage());
        }
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        Path file = TerpHome.etcFile("hibernate.properties");
        Properties stored = loadHibernateFile();
        stored.setProperty("hibernate.connection.url", buildUrl());
        stored.setProperty("hibernate.connection.username", text(txtUser));
        stored.setProperty("hibernate.connection.password", txtPassword.getText() == null ? "" : txtPassword.getText());
        String driver = text(txtDriver);
        if (!driver.isEmpty()) {
            stored.setProperty("hibernate.connection.driver_class", driver);
        }
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream out = Files.newOutputStream(file)) {
                stored.store(out, "TERP Hibernate");
            }
            TerpProperties.getInstance().getHibernateProps().putAll(stored);
            txtaInformation.setText("Kaydedildi. Yeni bağlantı için uygulamayı yeniden başlatın.");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            showError("Kaydedilemedi", ex.getMessage());
        }
    }

    @FXML
    void btnTestOnAction(ActionEvent event) {
        String url = buildUrl();
        String user = text(txtUser);
        String password = txtPassword.getText() == null ? "" : txtPassword.getText();
        Properties probe = new Properties();
        probe.putAll(TerpProperties.getInstance().getHibernateProps());
        probe.setProperty("hibernate.connection.url", url);
        probe.setProperty("hibernate.connection.username", user);
        probe.setProperty("hibernate.connection.password", password);
        String driver = text(txtDriver);
        if (!driver.isEmpty()) {
            probe.setProperty("hibernate.connection.driver_class", driver);
        }
        try {
            JdbcDriverSupport.install(probe);
            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                txtaInformation.setText("Bağlantı başarılı.\n" + connection.getMetaData().getURL());
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, null, ex);
            txtaInformation.setText("Bağlantı başarısız: " + ex.getMessage());
            showError("Bağlantı denemesi", ex.getMessage());
        }
    }

    @FXML
    void btnCancelOnAction(ActionEvent event) {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Properties props = TerpProperties.getInstance().getHibernateProps();
        if (props == null) {
            return;
        }
        txtUser.setText(props.getProperty("hibernate.connection.username", ""));
        txtPassword.setText(props.getProperty("hibernate.connection.password", ""));
        txtDriver.setText(props.getProperty("hibernate.connection.driver_class", ""));
        fillFromUrl(props.getProperty("hibernate.connection.url", ""));
        txtaInformation.setText("Kayıt uygulamayı yeniden başlatınca geçerli olur.");
    }

    private void fillFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        String rest = url;
        if (rest.startsWith("jdbc:")) {
            rest = rest.substring(5);
        }
        int schemeEnd = rest.indexOf("://");
        if (schemeEnd < 0) {
            txtServerName.setText(url);
            txtPort.clear();
            txtCatalog.clear();
            return;
        }
        String after = rest.substring(schemeEnd + 3);
        int slash = after.indexOf('/');
        String hostPort = slash < 0 ? after : after.substring(0, slash);
        String catalog = slash < 0 ? "" : after.substring(slash + 1);
        int semi = catalog.indexOf(';');
        if (semi >= 0) {
            catalog = catalog.substring(0, semi);
        }
        int colon = hostPort.lastIndexOf(':');
        if (colon > 0) {
            txtServerName.setText(hostPort.substring(0, colon));
            txtPort.setText(hostPort.substring(colon + 1));
        } else {
            txtServerName.setText(hostPort);
            txtPort.clear();
        }
        txtCatalog.setText(catalog);
    }

    private String buildUrl() {
        String server = text(txtServerName);
        String port = text(txtPort);
        String catalog = text(txtCatalog);
        if (server.startsWith("jdbc:")) {
            return server;
        }
        if (port.isEmpty() && catalog.isEmpty()) {
            return server;
        }
        String driver = text(txtDriver).toLowerCase();
        String scheme = "postgresql";
        if (driver.contains("mysql") || driver.contains("mariadb")) {
            scheme = "mysql";
        } else if (driver.contains("sqlserver")) {
            scheme = "sqlserver";
        } else if (driver.contains("oracle")) {
            scheme = "oracle:thin:@";
            return "jdbc:" + scheme + server + (port.isEmpty() ? "" : ":" + port) + ":" + catalog;
        } else if (driver.contains("derby") || server.contains("derby")) {
            return catalog.isEmpty() ? server : "jdbc:derby:" + catalog + ";create=true";
        }
        String host = server;
        if (!port.isEmpty()) {
            host = server + ":" + port;
        }
        return "jdbc:" + scheme + "://" + host + "/" + catalog;
    }

    private static String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private static Properties loadHibernateFile() {
        Properties stored = new Properties();
        Path file = TerpHome.etcFile("hibernate.properties");
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                stored.load(in);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
        }
        return stored;
    }

    private static void showError(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Veritabanı");
        alert.setHeaderText(header);
        alert.setContentText(content == null ? "" : content);
        alert.show();
    }

    private static final Logger LOG = Logger.getLogger(ServerFormController.class.getName());
}

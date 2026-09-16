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
package com.terp.gui.controllers;

import com.terp.plugin.IPlugin;
import com.terp.plugin.IUser;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.gui.IDesktopManager;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class PopupMenuController implements Initializable {

    private static final Logger LOG = Logger.getLogger(PopupMenuController.class.getName());

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
    private Button btnCompany;
    @FXML
    private Button btnBranch;
    @FXML
    private Button btnPeriod;

    @FXML
    void btnUserOnAction(ActionEvent event) {
        openHostProgram("UserForm", "SYS05");
    }

    @FXML
    void btnCompanyOnAction(ActionEvent event) {
        openPluginProgram("terp.core", "CompanyForm", "FRMYS02");
    }

    @FXML
    void btnBranchOnAction(ActionEvent event) {
        openPluginProgram("terp.core", "BranchForm", "FRMYS03");
    }

    @FXML
    void btnPeriodOnAction(ActionEvent event) {
        openDialog("/fxml/PeriodForm.fxml", "Çalışma dönemi");
    }

    @FXML
    void btnServerOnAction(ActionEvent event) {
        if (!requireAdmin()) {
            return;
        }
        openDialog("/fxml/ServerForm.fxml", "Veritabanı bağlantısı");
    }

    @FXML
    void btnSqlOnAction(ActionEvent event) {
        if (!requireAdmin()) {
            return;
        }
        openDialog("/fxml/SqlEditorForm.fxml", "SQL");
    }

    @FXML
    void btnProgramSettingsOnAction(ActionEvent event) {
        openDialog("/fxml/ProgramSettingsForm.fxml", "Ayarlar");
    }

    @FXML
    void btnExitProgramOnAction(ActionEvent event) {
        Alert confirm = new Alert(AlertType.CONFIRMATION,
                "Uygulama kapatılsın mı?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Çıkış");
        confirm.setHeaderText("Oturum sonlanacak");
        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }
            hidePopup();
            Stage stage = TerpApplication.getInstance().getDesktopManager().getPrimaryStage();
            if (stage != null) {
                stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        IUser user = TerpApplication.getInstance().getUser();
        boolean admin = user != null && user.isAdministrator();
        boolean canUsers = canOpen("SYS05");
        boolean canCompany = canOpen("FRMYS02");
        boolean canBranch = canOpen("FRMYS03");
        if (btnUserSettings != null) {
            btnUserSettings.setDisable(!canUsers);
        }
        if (btnCompany != null) {
            btnCompany.setDisable(!canCompany);
        }
        if (btnBranch != null) {
            btnBranch.setDisable(!canBranch);
        }
        if (btnServerSetup != null) {
            btnServerSetup.setDisable(!admin);
        }
        if (btnSqlEditor != null) {
            btnSqlEditor.setDisable(!admin);
        }
    }

    private void openHostProgram(String fxml, String menuId) {
        if (!canOpen(menuId)) {
            showDenied();
            return;
        }
        hidePopup();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxml + ".fxml"));
            loader.setClassLoader(getClass().getClassLoader());
            Node node = loader.load();
            TerpApplication.getInstance().getDesktopManager().addToDesktop(node, menuId);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            showError("Program açılamadı", ex.getMessage());
        }
    }

    private void openPluginProgram(String pluginName, String program, String menuId) {
        if (!canOpen(menuId)) {
            showDenied();
            return;
        }
        hidePopup();
        IPlugin plugin = TerpApplication.getInstance().getPluginFactory().getPlugin(pluginName);
        if (plugin == null) {
            showError("Eklenti yok", pluginName + " yüklü değil.");
            return;
        }
        plugin.loadProgram(program);
    }

    private void openDialog(String fxml, String title) {
        hidePopup();
        TerpApplication app = TerpApplication.getInstance();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            loader.setClassLoader(getClass().getClassLoader());
            Parent parent = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(parent));
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            IDesktopManager desktop = app.getDesktopManager();
            if (desktop != null) {
                stage.initOwner(desktop.getPrimaryStage());
            }
            stage.show();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            showError("Pencere açılamadı", ex.getMessage());
        }
    }

    private void hidePopup() {
        if (apPopupContainer == null || apPopupContainer.getScene() == null) {
            return;
        }
        Window window = apPopupContainer.getScene().getWindow();
        if (window != null) {
            window.hide();
        }
    }

    private static boolean canOpen(String menuId) {
        IUser user = TerpApplication.getInstance().getUser();
        return user != null && user.canOpen(menuId);
    }

    private static boolean requireAdmin() {
        IUser user = TerpApplication.getInstance().getUser();
        if (user != null && user.isAdministrator()) {
            return true;
        }
        showDenied();
        return false;
    }

    private static void showDenied() {
        showError("Yetki yok", "Bu işlem için yetkiniz yok.");
    }

    private static void showError(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Sistem");
        alert.setHeaderText(header);
        alert.setContentText(content == null ? "" : content);
        alert.show();
    }
}

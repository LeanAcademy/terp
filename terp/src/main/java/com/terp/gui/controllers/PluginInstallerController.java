/*
 * Copyright (C) 2026 LeanAcademy
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

import com.terp.plugin.FormRights;
import com.terp.plugin.IPluginFactory;
import com.terp.plugin.TerpApplication;
import com.terp.plugins.PluginFactoryImpl;
import com.terp.plugins.PluginInstallInfo;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Lists plugins under {@code plugins/} merged with {@code eklenti}, and adds or removes JARs.
 */
public class PluginInstallerController implements Initializable {

    private static final String MENU_ID = "SYS02";
    private static final String RESTART_HINT = "Değişikliğin yüklenmesi için TERP'i yeniden başlatın.";

    @FXML
    private Button btnAdd;
    @FXML
    private Button btnRemove;
    @FXML
    private Button btnRefresh;
    @FXML
    private TableView<PluginInstallInfo> tblvPluginView;
    @FXML
    private TableColumn<PluginInstallInfo, String> tcName;
    @FXML
    private TableColumn<PluginInstallInfo, String> tcVersion;
    @FXML
    private TableColumn<PluginInstallInfo, String> tcType;
    @FXML
    private TableColumn<PluginInstallInfo, String> tcJar;
    @FXML
    private TableColumn<PluginInstallInfo, String> tcStatus;

    private FormRights rights = FormRights.forMenu(MENU_ID);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tcName.setCellValueFactory(new PropertyValueFactory<>("pluginName"));
        tcVersion.setCellValueFactory(new PropertyValueFactory<>("pluginVersion"));
        tcType.setCellValueFactory(new PropertyValueFactory<>("typeLabel"));
        tcJar.setCellValueFactory(new PropertyValueFactory<>("jarFileName"));
        tcStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tblvPluginView.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> updateButtons());
        btnAdd.setDisable(!rights.add);
        refreshView();
    }

    @FXML
    void onActionBtnAdd(ActionEvent event) {
        if (!rights.add) {
            return;
        }
        PluginFactoryImpl impl = factory();
        if (impl == null) {
            showError("Eklenti fabrikası yok.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Eklenti JAR seçin");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Plugin JAR", "*.jar"));
        Stage owner = ownerStage();
        File chosen = chooser.showOpenDialog(owner);
        if (chosen == null) {
            return;
        }
        try {
            String log = impl.installJar(Path.of(chosen.getAbsolutePath()));
            refreshView();
            showInfo("Eklenti eklendi", log + "\n" + RESTART_HINT);
        } catch (IOException | RuntimeException ex) {
            showError(ex.getMessage() == null ? "Kurulum başarısız." : ex.getMessage());
        }
    }

    @FXML
    void onActionBtnRemove(ActionEvent event) {
        if (!rights.delete) {
            return;
        }
        PluginInstallInfo selected = tblvPluginView.getSelectionModel().getSelectedItem();
        if (selected == null || !selected.isRemovable()) {
            return;
        }
        PluginFactoryImpl impl = factory();
        if (impl == null) {
            showError("Eklenti fabrikası yok.");
            return;
        }
        Alert confirm = new Alert(AlertType.CONFIRMATION,
                selected.getPluginName() + " çıkarılsın mı?\nJAR ve eklenti kaydı silinir; menü ve iş tabloları kalır.",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Eklenti çıkar");
        confirm.setHeaderText("Eklentiyi çıkar");
        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }
            try {
                String log = impl.uninstall(selected.getPluginName());
                refreshView();
                showInfo("Eklenti çıkarıldı", log);
            } catch (IOException | RuntimeException ex) {
                showError(ex.getMessage() == null ? "Çıkarma başarısız." : ex.getMessage());
            }
        });
    }

    @FXML
    void onActionBtnRefresh(ActionEvent event) {
        refreshView();
    }

    private void refreshView() {
        PluginFactoryImpl impl = factory();
        List<PluginInstallInfo> rows = impl == null ? List.of() : impl.listPlugins();
        tblvPluginView.setItems(FXCollections.observableArrayList(rows));
        updateButtons();
    }

    private void updateButtons() {
        PluginInstallInfo selected = tblvPluginView.getSelectionModel().getSelectedItem();
        btnRemove.setDisable(!rights.delete || selected == null || !selected.isRemovable());
    }

    private static PluginFactoryImpl factory() {
        IPluginFactory factory = TerpApplication.getInstance().getPluginFactory();
        return factory instanceof PluginFactoryImpl impl ? impl : null;
    }

    private static Stage ownerStage() {
        return TerpApplication.getInstance().getDesktopManager() == null
                ? null
                : TerpApplication.getInstance().getDesktopManager().getPrimaryStage();
    }

    private static void showInfo(String header, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Eklenti yönetimi");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();
    }

    private static void showError(String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Eklenti yönetimi");
        alert.setHeaderText("İşlem başarısız");
        alert.setContentText(content);
        alert.show();
    }
}

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

import com.terp.plugin.IPluginFactory;
import com.terp.plugin.TerpApplication;
import com.terp.plugins.PluginFactoryImpl;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Copies a plugin JAR into {@code plugins/} and registers it in {@code eklenti}.
 */
public class PluginInstallerController implements Initializable {

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnInstall;

    @FXML
    private TextArea txtResult;

    @FXML
    private TextField txtPluginFileName;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtResult.setEditable(false);
        btnInstall.setDisable(true);
    }

    @FXML
    void btnBrowseOnAction(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select plugin JAR");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Plugin JAR", "*.jar"));
        Stage owner = TerpApplication.getInstance().getDesktopManager() != null
                ? TerpApplication.getInstance().getDesktopManager().getPrimaryStage()
                : null;
        File chosen = chooser.showOpenDialog(owner);
        if (chosen != null) {
            txtPluginFileName.setText(chosen.getAbsolutePath());
            btnInstall.setDisable(false);
            append("Selected " + chosen.getAbsolutePath());
        }
    }

    @FXML
    void btnInstallOnAction(ActionEvent event) {
        String pathText = txtPluginFileName.getText();
        if (pathText == null || pathText.isBlank()) {
            append("Choose a plugin JAR first.");
            return;
        }
        Path source = Path.of(pathText.trim());
        IPluginFactory factory = TerpApplication.getInstance().getPluginFactory();
        if (!(factory instanceof PluginFactoryImpl impl)) {
            append("Plugin factory is not available.");
            return;
        }
        try {
            String log = impl.installJar(source);
            append(log);
        } catch (IOException | RuntimeException ex) {
            append("Install failed: " + ex.getMessage());
        }
    }

    @FXML
    void btnCancelOnAction(ActionEvent event) {
        closeTab(btnCancel);
    }

    private void append(String line) {
        if (txtResult.getText() == null || txtResult.getText().isEmpty()) {
            txtResult.setText(line);
        } else {
            txtResult.appendText("\n" + line);
        }
    }

    private static void closeTab(Node node) {
        Parent parent = node.getParent();
        while (parent != null) {
            if (parent instanceof TabPane pane) {
                Tab selected = pane.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    pane.getTabs().remove(selected);
                }
                return;
            }
            parent = parent.getParent();
        }
    }
}

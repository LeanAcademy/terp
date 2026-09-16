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

import com.terp.gui.UiAppearance;
import com.terp.plugin.TerpApplication;
import com.terp.util.TerpProperties;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

public class ProgramSettingsFormController implements Initializable {

    private static final String THEME_LIGHT = "Açık";
    private static final String THEME_DARK = "Koyu";
    private static final String THEME_CONTRAST = "Yüksek kontrast";
    private static final String DENSITY_COMFORTABLE = "Rahat";
    private static final String DENSITY_COMPACT = "Sıkışık";

    @FXML
    private Button btnSave;
    @FXML
    private Button btnClose;
    @FXML
    private CheckBox chkShowMenu;
    @FXML
    private ComboBox<String> cmbTheme;
    @FXML
    private ComboBox<String> cmbFont;
    @FXML
    private ComboBox<Integer> cmbFontSize;
    @FXML
    private ComboBox<String> cmbDensity;

    @FXML
    private void onActionBtnSave(ActionEvent event) {
        boolean show = chkShowMenu.isSelected();
        Properties props = TerpProperties.getInstance().getViewProps();
        if (props == null) {
            props = new Properties();
            TerpProperties.getInstance().setViewProps(props);
        }
        props.setProperty("terp.main.showmainmenu", show ? "true" : "false");
        props.setProperty(UiAppearance.PROP_THEME, themeValue());
        props.setProperty(UiAppearance.PROP_FONT_FAMILY, fontValue());
        props.setProperty(UiAppearance.PROP_FONT_SIZE, String.valueOf(fontSizeValue()));
        props.setProperty(UiAppearance.PROP_DENSITY, densityValue());
        TerpProperties.getInstance().saveViewProps();
        UiAppearance.applyAllWindows();
        applyMenuVisibility(show);
        close();
    }

    @FXML
    private void onActionBtnClose(ActionEvent event) {
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Properties props = TerpProperties.getInstance().getViewProps();
        String showMenu = props == null ? "true" : props.getProperty("terp.main.showmainmenu", "true");
        chkShowMenu.setSelected(!"false".equals(showMenu));
        cmbTheme.setItems(FXCollections.observableArrayList(THEME_LIGHT, THEME_DARK, THEME_CONTRAST));
        cmbTheme.getSelectionModel().select(themeLabel(value(props, UiAppearance.PROP_THEME, UiAppearance.THEME_LIGHT)));
        cmbFont.setItems(FXCollections.observableArrayList(UiAppearance.fontChoices()));
        String family = value(props, UiAppearance.PROP_FONT_FAMILY, "System");
        if (!cmbFont.getItems().contains(family)) {
            cmbFont.getItems().add(0, family);
        }
        cmbFont.getSelectionModel().select(family);
        cmbFontSize.setItems(FXCollections.observableArrayList(11, 12, 13, 14, 16, 18));
        int savedSize = fontSizeValue(props);
        if (!cmbFontSize.getItems().contains(savedSize)) {
            cmbFontSize.getItems().add(savedSize);
            FXCollections.sort(cmbFontSize.getItems());
        }
        cmbFontSize.getSelectionModel().select(Integer.valueOf(savedSize));
        cmbDensity.setItems(FXCollections.observableArrayList(DENSITY_COMFORTABLE, DENSITY_COMPACT));
        cmbDensity.getSelectionModel().select(
                UiAppearance.DENSITY_COMPACT.equals(value(props, UiAppearance.PROP_DENSITY, UiAppearance.DENSITY_COMFORTABLE))
                ? DENSITY_COMPACT : DENSITY_COMFORTABLE);
        cmbTheme.valueProperty().addListener((obs, old, value) -> preview());
        cmbFont.valueProperty().addListener((obs, old, value) -> preview());
        cmbFontSize.valueProperty().addListener((obs, old, value) -> preview());
        cmbDensity.valueProperty().addListener((obs, old, value) -> preview());
    }

    private void preview() {
        if (btnSave == null || btnSave.getScene() == null) {
            return;
        }
        UiAppearance.apply(btnSave.getScene(), themeValue(), fontValue(), fontSizeValue(), densityValue());
    }

    private void applyMenuVisibility(boolean show) {
        Stage stage = TerpApplication.getInstance().getDesktopManager().getPrimaryStage();
        if (stage == null || stage.getScene() == null) {
            return;
        }
        Object node = stage.getScene().lookup("#tbtnShowHideMenu");
        if (node instanceof ToggleButton toggle && toggle.isSelected() != show) {
            toggle.setSelected(show);
            toggle.fire();
        }
    }

    private void close() {
        ((Stage) btnClose.getScene().getWindow()).close();
    }

    private String themeValue() {
        String label = cmbTheme.getValue();
        if (THEME_DARK.equals(label)) {
            return UiAppearance.THEME_DARK;
        }
        if (THEME_CONTRAST.equals(label)) {
            return UiAppearance.THEME_CONTRAST;
        }
        return UiAppearance.THEME_LIGHT;
    }

    private static String themeLabel(String value) {
        if (UiAppearance.THEME_DARK.equals(value)) {
            return THEME_DARK;
        }
        if (UiAppearance.THEME_CONTRAST.equals(value)) {
            return THEME_CONTRAST;
        }
        return THEME_LIGHT;
    }

    private String fontValue() {
        String family = cmbFont.getValue();
        return family == null || family.isBlank() ? "System" : family;
    }

    private int fontSizeValue() {
        Integer size = cmbFontSize.getValue();
        return size == null ? 12 : size;
    }

    private static int fontSizeValue(Properties props) {
        try {
            return Integer.parseInt(value(props, UiAppearance.PROP_FONT_SIZE, "12"));
        } catch (NumberFormatException ex) {
            return 12;
        }
    }

    private String densityValue() {
        return DENSITY_COMPACT.equals(cmbDensity.getValue())
                ? UiAppearance.DENSITY_COMPACT : UiAppearance.DENSITY_COMFORTABLE;
    }

    private static String value(Properties props, String key, String fallback) {
        if (props == null) {
            return fallback;
        }
        String value = props.getProperty(key, fallback);
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}

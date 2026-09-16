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
package com.terp.gui;

import com.terp.util.TerpProperties;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import javafx.collections.ListChangeListener;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Window;

public final class UiAppearance {

    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_CONTRAST = "contrast";
    public static final String DENSITY_COMFORTABLE = "comfortable";
    public static final String DENSITY_COMPACT = "compact";
    public static final String PROP_THEME = "terp.ui.theme";
    public static final String PROP_FONT_FAMILY = "terp.ui.font.family";
    public static final String PROP_FONT_SIZE = "terp.ui.font.size";
    public static final String PROP_DENSITY = "terp.ui.density";

    private static final String CLASS_COMPACT = "density-compact";
    private static final String CLASS_COMFORTABLE = "density-comfortable";
    private static final List<String> THEME_FILES = List.of(
            "theme-light.css", "theme-dark.css", "theme-contrast.css");

    private static boolean watchingWindows;

    private UiAppearance() {
    }

    public static void install() {
        watchWindows();
        applyAllWindows();
    }

    public static void applyAllWindows() {
        for (Window window : Window.getWindows()) {
            apply(window.getScene());
        }
    }

    public static void apply(Scene scene) {
        Properties props = TerpProperties.getInstance().getViewProps();
        apply(scene,
                value(props, PROP_THEME, THEME_LIGHT),
                value(props, PROP_FONT_FAMILY, "System"),
                fontSize(props),
                value(props, PROP_DENSITY, DENSITY_COMFORTABLE));
    }

    public static void apply(Scene scene, String theme, String fontFamily, int fontSize, String density) {
        if (scene == null) {
            return;
        }
        scene.getStylesheets().removeIf(UiAppearance::isThemeStylesheet);
        String themeUrl = themeUrl(theme);
        if (themeUrl != null && !scene.getStylesheets().contains(themeUrl)) {
            scene.getStylesheets().add(themeUrl);
        }
        Parent root = scene.getRoot();
        if (root == null) {
            return;
        }
        root.getStyleClass().removeAll(CLASS_COMPACT, CLASS_COMFORTABLE);
        root.getStyleClass().add(DENSITY_COMPACT.equals(density) ? CLASS_COMPACT : CLASS_COMFORTABLE);
        int size = Math.min(20, Math.max(10, fontSize));
        root.setStyle("-fx-font-family: \"" + cssEscape(resolveFamily(fontFamily))
                + "\"; -fx-font-size: " + size + "px;");
    }

    public static List<String> fontChoices() {
        return List.of("System", "SansSerif", "Serif", "Monospaced",
                "Ubuntu", "DejaVu Sans", "Noto Sans", "Liberation Sans");
    }

    private static void watchWindows() {
        if (watchingWindows) {
            return;
        }
        watchingWindows = true;
        for (Window window : Window.getWindows()) {
            attach(window);
        }
        Window.getWindows().addListener((ListChangeListener<Window>) change -> {
            while (change.next()) {
                if (!change.wasAdded()) {
                    continue;
                }
                for (Window window : change.getAddedSubList()) {
                    attach(window);
                }
            }
        });
    }

    private static void attach(Window window) {
        window.sceneProperty().addListener((obs, oldScene, newScene) -> apply(newScene));
        apply(window.getScene());
    }

    private static String themeUrl(String theme) {
        String file = "theme-light.css";
        if (THEME_DARK.equals(theme)) {
            file = "theme-dark.css";
        } else if (THEME_CONTRAST.equals(theme)) {
            file = "theme-contrast.css";
        }
        URL url = UiAppearance.class.getResource("/styles/" + file);
        return url == null ? null : url.toExternalForm();
    }

    private static boolean isThemeStylesheet(String stylesheet) {
        if (stylesheet == null) {
            return false;
        }
        for (String file : THEME_FILES) {
            if (stylesheet.contains(file)) {
                return true;
            }
        }
        return false;
    }

    private static String resolveFamily(String stored) {
        if (stored == null || stored.isBlank()
                || "System".equalsIgnoreCase(stored)
                || "Sistem".equalsIgnoreCase(stored)) {
            return Font.getDefault().getFamily();
        }
        return stored;
    }

    private static int fontSize(Properties props) {
        try {
            return Integer.parseInt(value(props, PROP_FONT_SIZE, "12"));
        } catch (NumberFormatException ex) {
            return 12;
        }
    }

    private static String value(Properties props, String key, String fallback) {
        if (props == null) {
            return fallback;
        }
        String value = props.getProperty(key, fallback);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String cssEscape(String family) {
        return family.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

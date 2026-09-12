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
package com.terp.stok.gui;

import com.terp.plugin.TerpApplication;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.Window;

final class StockEmbed {

    private StockEmbed() {
    }

    static void hideCloseIfDesktop(Button btnClose) {
        if (btnClose == null || btnClose.getScene() == null) {
            return;
        }
        Window window = btnClose.getScene().getWindow();
        Stage primary = TerpApplication.getInstance().getDesktopManager().getPrimaryStage();
        boolean modal = window instanceof Stage stage && stage != primary;
        btnClose.setVisible(modal);
        btnClose.setManaged(modal);
    }

    static void closeIfModal(Button btnClose) {
        if (btnClose == null || btnClose.getScene() == null) {
            return;
        }
        Window window = btnClose.getScene().getWindow();
        Stage primary = TerpApplication.getInstance().getDesktopManager().getPrimaryStage();
        if (window instanceof Stage stage && stage != primary) {
            stage.close();
        }
    }
}

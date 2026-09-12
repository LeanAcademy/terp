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
import com.terp.stok.data.StockCount;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

final class StockWindows {

    private static final Logger LOG = Logger.getLogger(StockWindows.class.getName());

    private StockWindows() {
    }

    static void openCountEditor(StockCount current, Runnable afterClose) {
        try {
            FXMLLoader loader = StockForms.pluginLoader(StockWindows.class, "/fxml/CountEditForm.fxml");
            Node node = loader.load();
            CountEditFormController controller = loader.getController();
            controller.initializeForm(current);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(new Scene((Parent) node));
            stage.setTitle(current == null ? "Yeni stok sayım" : "Stok sayım");
            stage.showAndWait();
            if (afterClose != null) {
                afterClose.run();
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}

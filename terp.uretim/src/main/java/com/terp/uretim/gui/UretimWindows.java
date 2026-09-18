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
package com.terp.uretim.gui;

import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ProductionFromSales;
import com.terp.uretim.data.ProductionOrder;
import com.terp.uretim.data.Recipe;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public final class UretimWindows {

    private static final Logger LOG = Logger.getLogger(UretimWindows.class.getName());

    private UretimWindows() {
    }

    public static void openRecipeEditor(Recipe current, Runnable afterClose) {
        try {
            FXMLLoader loader = UretimDates.pluginLoader(UretimWindows.class, "/fxml/RecipeEditForm.fxml");
            Node node = loader.load();
            RecipeEditFormController controller = loader.getController();
            controller.initializeForm(current);
            showModal(node, current == null ? "Yeni reçete" : "Reçete", afterClose);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public static void openOrderEditor(ProductionOrder current, ProductionFromSales fromSales, Runnable afterClose) {
        try {
            FXMLLoader loader = UretimDates.pluginLoader(UretimWindows.class, "/fxml/ProductionOrderEditForm.fxml");
            Node node = loader.load();
            ProductionOrderEditFormController controller = loader.getController();
            controller.initializeForm(current, fromSales);
            String title = fromSales != null ? "Satış siparişinden üretim emri"
                    : (current == null ? "Yeni üretim emri" : "Üretim emri");
            showModal(node, title, afterClose);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public static void openCompletion(ProductionOrder order, Runnable afterClose) {
        try {
            FXMLLoader loader = UretimDates.pluginLoader(UretimWindows.class, "/fxml/ProductionCompletionForm.fxml");
            Node node = loader.load();
            ProductionCompletionFormController controller = loader.getController();
            controller.initializeForm(order);
            showModal(node, "İş bitti", afterClose);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private static void showModal(Node node, String title, Runnable afterClose) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
        stage.setScene(new Scene((Parent) node));
        stage.setTitle(title);
        stage.showAndWait();
        if (afterClose != null) {
            afterClose.run();
        }
    }
}

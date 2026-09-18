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
package com.terp.uretim.plugin;

import com.terp.plugin.IPlugin;
import com.terp.plugin.PluginMenu;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.gui.IDesktopManager;
import com.terp.uretim.data.ProductionCompletion;
import com.terp.uretim.data.ProductionCompletionMaterial;
import com.terp.uretim.data.ProductionOrder;
import com.terp.uretim.data.ProductionOrderMaterial;
import com.terp.uretim.data.Recipe;
import com.terp.uretim.data.RecipeLine;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public class Plugin implements IPlugin {

    private static final Logger LOG = Logger.getLogger(Plugin.class.getName());

    private final String name = "terp.uretim";
    private final String sysVersion = "1.0";
    private final String pluginVersion = "1.0";
    private final int type = 0;

    private TerpApplication app;
    private IDesktopManager desktopManager;

    @Override
    public void run() {
        this.app = TerpApplication.getInstance();
        this.desktopManager = app.getDesktopManager();
        app.setProductionOrders(new ProductionOrders());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSystemVersion() {
        return this.sysVersion;
    }

    @Override
    public String getPluginVersion() {
        return this.pluginVersion;
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public void install() {
        // Host PluginInstaller copies the JAR.
    }

    @Override
    public boolean isInstalled() {
        return true;
    }

    @Override
    public List<Class<?>> getPersistentClasses() {
        return List.of(Recipe.class, RecipeLine.class,
                ProductionOrder.class, ProductionOrderMaterial.class,
                ProductionCompletion.class, ProductionCompletionMaterial.class);
    }

    @Override
    public List<PluginMenu> getMenus() {
        return List.of(
                PluginMenu.folder("URT01", "Üretim"),
                PluginMenu.program("URT02", "Reçete", "URT01", "RecipeForm"),
                PluginMenu.program("URT03", "Üretim emri", "URT01", "ProductionOrderForm"),
                PluginMenu.program("URT04", "İş bitti", "URT01", "ProductionCompletionListForm"));
    }

    @Override
    public void loadProgram(String program) {
        if (this.app == null) {
            this.app = TerpApplication.getInstance();
        }
        if (this.desktopManager == null) {
            this.desktopManager = this.app.getDesktopManager();
        }
        String form = program == null ? "" : program.trim();
        try {
            URL fxml = getClass().getResource("/fxml/" + form + ".fxml");
            if (fxml == null) {
                LOG.log(Level.SEVERE, "FXML not found for {0}", form);
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxml);
            loader.setClassLoader(getClass().getClassLoader());
            Node node = loader.load();
            this.desktopManager.addToDesktop(node, programMenuId(form));
        } catch (IOException | RuntimeException ex) {
            LOG.log(Level.SEVERE, "Could not load program " + form, ex);
        }
    }

    private static String programMenuId(String program) {
        if ("RecipeForm".equals(program)) {
            return "URT02";
        }
        if ("ProductionOrderForm".equals(program)) {
            return "URT03";
        }
        if ("ProductionCompletionListForm".equals(program)) {
            return "URT04";
        }
        return program;
    }
}

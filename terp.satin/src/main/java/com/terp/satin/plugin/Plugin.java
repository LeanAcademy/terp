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
package com.terp.satin.plugin;

import com.terp.plugin.IPlugin;
import com.terp.plugin.PluginMenu;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.gui.IDesktopManager;
import com.terp.plugin.gui.IIconFactory;
import com.terp.plugin.gui.IMenuManager;
import com.terp.satin.data.PurchaseOrder;
import com.terp.satin.data.PurchaseOrderLine;
import com.terp.satin.data.PurchaseReceipt;
import com.terp.satin.data.PurchaseReceiptLine;
import com.terp.satin.data.PurchaseRequest;
import com.terp.satin.data.PurchaseRequestLine;
import com.terp.satin.data.PurchaseSettings;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;

public class Plugin implements IPlugin {

    private static final Logger LOG = Logger.getLogger(Plugin.class.getName());

    private final String name = "terp.satin";
    private final String sysVersion = "1.0";
    private final String pluginVersion = "1.0";
    private final int type = 0;

    private TerpApplication app;
    private IDesktopManager desktopManager;
    private IMenuManager menuManager;

    @Override
    public void run() {
        this.app = TerpApplication.getInstance();
        this.desktopManager = app.getDesktopManager();
        this.menuManager = app.getMenuManager();
        if (this.menuManager != null) {
            addProgramTool("Talep", "CLIPBOARD", "RequestForm");
            addProgramTool("Sipariş", "SHOPPING_CART", "OrderForm");
            addProgramTool("Durum", "LIST", "OrderStatusForm");
            addProgramTool("Mal kabul", "TRUCK", "ReceiptForm");
        }
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
        return List.of(PurchaseRequest.class, PurchaseRequestLine.class,
                PurchaseOrder.class, PurchaseOrderLine.class,
                PurchaseReceipt.class, PurchaseReceiptLine.class,
                PurchaseSettings.class);
    }

    @Override
    public List<PluginMenu> getMenus() {
        return List.of(
                PluginMenu.folder("SAT01", "Satınalma"),
                PluginMenu.program("SAT03", "Satınalma talebi", "SAT01", "RequestForm"),
                PluginMenu.program("SAT04", "Satınalma siparişi", "SAT01", "OrderForm"),
                PluginMenu.program("SAT05", "Sipariş durumu", "SAT01", "OrderStatusForm"),
                PluginMenu.program("SAT02", "Mal kabul", "SAT01", "ReceiptForm"),
                PluginMenu.program("SAT06", "Satınalma ayarları", "SYS01", "SettingsForm"));
    }

    @Override
    public void loadProgram(String program) {
        if (this.app == null) {
            this.app = TerpApplication.getInstance();
        }
        if (this.desktopManager == null) {
            this.desktopManager = this.app.getDesktopManager();
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/fxml/" + program + ".fxml"));
            loader.setClassLoader(getClass().getClassLoader());
            Node node = loader.load();
            this.desktopManager.addToDesktop(node, program);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void addProgramTool(String text, String iconName, String program) {
        if (this.menuManager == null) {
            return;
        }
        Button button = new Button(text);
        button.setPrefHeight(24);
        button.setMnemonicParsing(false);
        IIconFactory icons = this.app.getIconFactory();
        if (icons != null && iconName != null) {
            try {
                button.setGraphic(icons.getIcon(iconName));
            } catch (RuntimeException ignored) {
                // keep text-only if the glyph name is unknown
            }
        }
        button.setOnAction(event -> loadProgram(program));
        this.menuManager.addToolKit(button, programMenuId(program));
    }

    private static String programMenuId(String program) {
        if ("ReceiptForm".equals(program)) {
            return "SAT02";
        }
        if ("RequestForm".equals(program)) {
            return "SAT03";
        }
        if ("OrderForm".equals(program)) {
            return "SAT04";
        }
        if ("OrderStatusForm".equals(program)) {
            return "SAT05";
        }
        if ("SettingsForm".equals(program)) {
            return "SAT06";
        }
        return program;
    }
}

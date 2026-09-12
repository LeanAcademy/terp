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
package com.terp.stok.plugin;

import com.terp.plugin.IPlugin;
import com.terp.plugin.PluginMenu;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.gui.IDesktopManager;
import com.terp.plugin.gui.IIconFactory;
import com.terp.plugin.gui.IMenuManager;
import com.terp.stok.data.Item;
import com.terp.stok.data.ItemLookup;
import com.terp.stok.data.ItemPartnerCode;
import com.terp.stok.data.MovementReason;
import com.terp.stok.data.MovementReasonLookup;
import com.terp.stok.data.StockCount;
import com.terp.stok.data.StockCountLine;
import com.terp.stok.data.StockLedger;
import com.terp.stok.data.StockMovement;
import com.terp.stok.data.Warehouse;
import com.terp.stok.data.WarehouseLookup;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;

public class Plugin implements IPlugin {

    private static final Logger LOG = Logger.getLogger(Plugin.class.getName());

    private final String name = "terp.stok";
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
        if (app.getPersistence() != null) {
            ItemLookup items = new ItemLookup(app.getPersistence().createDao(Item.class));
            WarehouseLookup warehouses = new WarehouseLookup(app.getPersistence().createDao(Warehouse.class));
            MovementReasonLookup reasons = new MovementReasonLookup(
                    app.getPersistence().createDao(MovementReason.class));
            app.setItemLookup(items);
            app.setWarehouseLookup(warehouses);
            app.setMovementReasonLookup(reasons);
            app.setStockLedger(new StockLedger(
                    app.getPersistence().createDao(StockMovement.class),
                    items, warehouses, reasons));
        }
        addProgramTool("Malzeme", "CUBE", "ItemForm");
        addProgramTool("İşlem", "EXCHANGE", "MovementForm");
        addProgramTool("Sayım", "CHECK", "CountForm");
        addProgramTool("Durum", "BAR_CHART", "StockStatusForm");
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
        return List.of(Item.class, ItemPartnerCode.class, Warehouse.class, StockMovement.class,
                MovementReason.class, StockCount.class, StockCountLine.class);
    }

    @Override
    public List<PluginMenu> getMenus() {
        return List.of(
                PluginMenu.folder("STK01", "Stok yönetimi"),
                PluginMenu.program("STK02", "Malzeme kartı", "STK01", "ItemForm"),
                PluginMenu.program("STK04", "Stok işlemleri", "STK01", "MovementForm"),
                PluginMenu.program("STK07", "Stok sayım", "STK01", "CountForm"),
                PluginMenu.program("STK05", "Stok durum", "STK01", "StockStatusForm"),
                PluginMenu.program("STK03", "Depo tanımı", "SYS01", "WarehouseForm"),
                PluginMenu.program("STK06", "Hareket nedeni", "SYS01", "ReasonForm"));
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
        if ("ItemForm".equals(program)) {
            return "STK02";
        }
        if ("WarehouseForm".equals(program)) {
            return "STK03";
        }
        if ("MovementForm".equals(program)) {
            return "STK04";
        }
        if ("StockStatusForm".equals(program)) {
            return "STK05";
        }
        if ("CountForm".equals(program)) {
            return "STK07";
        }
        if ("ReasonForm".equals(program)) {
            return "STK06";
        }
        return program;
    }
}

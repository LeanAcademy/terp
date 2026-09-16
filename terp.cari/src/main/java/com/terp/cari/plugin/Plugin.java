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
package com.terp.cari.plugin;

import com.terp.cari.data.AccountLedger;
import com.terp.cari.data.AccountLookup;
import com.terp.cari.data.AccountMovement;
import com.terp.cari.data.Cari;
import com.terp.plugin.IPlugin;
import com.terp.plugin.PluginMenu;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.gui.IDesktopManager;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public class Plugin implements IPlugin {

    private static final Logger LOG = Logger.getLogger(Plugin.class.getName());

    private final String name = "terp.cari";
    private final String sysVersion = "1.0";
    private final String pluginVersion = "1.0";
    private final int type = 0;

    private TerpApplication app;
    private IDesktopManager desktopManager;

    @Override
    public void run() {
        this.app = TerpApplication.getInstance();
        this.desktopManager = app.getDesktopManager();
        if (app.getPersistence() != null) {
            AccountLookup lookup = new AccountLookup(app.getPersistence().createDao(Cari.class));
            app.setAccountLookup(lookup);
            app.setAccountLedger(new AccountLedger(
                    app.getPersistence().createDao(AccountMovement.class), lookup));
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
        return List.of(Cari.class, AccountMovement.class);
    }

    @Override
    public List<PluginMenu> getMenus() {
        return List.of(
                PluginMenu.folder("CRI01", "Cari hesaplar"),
                PluginMenu.program("CRI02", "Cari kartı", "CRI01", "AccountForm"),
                PluginMenu.program("CRI03", "Cari hareket", "CRI01", "AccountMovementForm"),
                PluginMenu.program("CRI04", "Cari durum", "CRI01", "AccountStatusForm"));
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
        if ("MovementForm".equals(form)) {
            form = "AccountMovementForm";
        }
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
        if ("AccountForm".equals(program)) {
            return "CRI02";
        }
        if ("AccountMovementForm".equals(program)) {
            return "CRI03";
        }
        if ("AccountStatusForm".equals(program)) {
            return "CRI04";
        }
        return program;
    }
}

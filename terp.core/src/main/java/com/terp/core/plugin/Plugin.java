/*
 * Copyright (C) 2014 ilknur
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.terp.core.plugin;

import java.util.logging.Logger;
import com.terp.core.data.Branch;
import com.terp.core.data.Company;
import com.terp.core.data.CompanyLookup;
import com.terp.core.data.DocumentNumbering;
import com.terp.core.data.DocumentSeries;
import com.terp.core.data.DocumentTemplate;
import com.terp.core.data.DocumentTemplateLookup;
import com.terp.core.data.ExchangeRate;
import com.terp.core.data.ExchangeRateLookup;
import com.terp.core.data.UnitLookup;
import com.terp.core.data.UnitOfMeasure;
import com.terp.core.data.VatRate;
import com.terp.core.data.VatRateLookup;
import com.terp.plugin.PluginMenu;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.gui.IDesktopManager;
import com.terp.plugin.IPlugin;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/**
 *
 * @author ilknur
 */
public class Plugin implements IPlugin{
    
    private final String name = "terp.core";
    private final String sysVersion = "1.0";
    private final String pluginVersion = "1.0";
    private final int type = 1; //core plugin
    
    private TerpApplication app = null;
    private IDesktopManager desktopManager = null;
    //private IUser user = null;
    //private IDatabase database = null;
    
    /**
     * run plugin
     */
    @Override
    public void run() {        
            
        // get application
        this.app = TerpApplication.getInstance();
        
        // set desktop manager
        this.desktopManager = app.getDesktopManager();
        if (app.getPersistence() != null) {
            app.setCompanyLookup(new CompanyLookup(app.getPersistence().createDao(Company.class)));
            app.setDocumentNumbers(new DocumentNumbering(
                    app.getPersistence().createDao(DocumentSeries.class)));
            app.setVatRateLookup(new VatRateLookup(app.getPersistence().createDao(VatRate.class)));
            app.setUnitLookup(new UnitLookup(app.getPersistence().createDao(UnitOfMeasure.class)));
            app.setExchangeRateLookup(new ExchangeRateLookup(
                    app.getPersistence().createDao(ExchangeRate.class)));
            app.setDocumentTemplates(new DocumentTemplateLookup(
                    app.getPersistence().createDao(DocumentTemplate.class)));
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
        // Core plugin ships with the host; nothing to copy.
    }

    @Override
    public boolean isInstalled() {
        return true;
    }

    @Override
    public List<Class<?>> getPersistentClasses() {
        return List.of(Company.class, Branch.class, DocumentSeries.class, VatRate.class,
                UnitOfMeasure.class, ExchangeRate.class, DocumentTemplate.class);
    }

    @Override
    public List<PluginMenu> getMenus() {
        return List.of(
                PluginMenu.folder("FRMYS01", "Firma yönetimi"),
                PluginMenu.program("FRMYS02", "Firma tanımı", "FRMYS01", "CompanyForm"),
                PluginMenu.program("FRMYS03", "Şube tanımı", "FRMYS01", "BranchForm"),
                PluginMenu.program("SYS07", "Belge numaraları", "SYS01", "SeriesForm"),
                PluginMenu.program("SYS08", "KDV oranları", "SYS01", "VatRateForm"),
                PluginMenu.program("SYS09", "Ölçü birimleri", "SYS01", "UnitForm"),
                PluginMenu.program("SYS10", "Döviz kurları", "SYS01", "ExchangeRateForm"),
                PluginMenu.program("SYS11", "Belge şablonları", "SYS01", "TemplateForm"));
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
        if ("CompanyForm".equals(program)) {
            return "FRMYS02";
        }
        if ("BranchForm".equals(program)) {
            return "FRMYS03";
        }
        if ("SeriesForm".equals(program)) {
            return "SYS07";
        }
        if ("VatRateForm".equals(program)) {
            return "SYS08";
        }
        if ("UnitForm".equals(program)) {
            return "SYS09";
        }
        if ("ExchangeRateForm".equals(program)) {
            return "SYS10";
        }
        if ("TemplateForm".equals(program)) {
            return "SYS11";
        }
        return program;
    }
    
    //logger
    private static final Logger LOG = Logger.getLogger(Plugin.class.getName());
   
}

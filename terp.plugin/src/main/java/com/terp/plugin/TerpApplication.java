/*
 * Copyright (C) 2015 ilknur
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.terp.plugin;

import com.terp.plugin.gui.IIconFactory;

/**
 *
 * @author ilknur
 */
public class TerpApplication {
    
    private static TerpApplication app;
    private IMenuManager menuManager;
    private IUser user;
    private IDesktopManager desktop;
    private IStatusbarManager statusbar;
    private IPluginFactory pluginFactory;
    private IDatabaseFactory databaseFactory;
    private IIconFactory iconFactory;
    
    private TerpApplication(){
        
    }
    
    /**
     * get instance of application
     * @return 
     */
    public static synchronized TerpApplication getInstance(){
        if(app == null){
            app = new TerpApplication();
        }
        
        return app;
    }
    
    /**
     * get menu manager
     * @return 
     */
    public synchronized IMenuManager getMenuManager(){
        return this.menuManager;
    }
    
    /**
     * get user
     * @return 
     */
    public synchronized IUser getUser(){
        return this.user;
    }
    
    /**
     * return desktop pane of related main frame of application
     * @return 
     */
    public synchronized IDesktopManager getDesktopManager(){
        return this.desktop;
    }
    
    /**
     * set menu manager
     * @param manager 
     */
    public void setMenuManager(IMenuManager manager){
        this.menuManager = manager;
    }
    
    /**
     * set user
     * @param user 
     */
    public void setUser(IUser user){
        this.user = user;
    }
    
    /**
     * set desktop pane
     * @param desktop 
     */
    public void setDesktop(IDesktopManager desktop){
        this.desktop = desktop;
    }

    public IStatusbarManager getStatusbar() {
        return statusbar;
    }

    public void setStatusbar(IStatusbarManager statusbar) {
        this.statusbar = statusbar;
    }

    public IPluginFactory getPluginFactory() {
        return pluginFactory;
    }

    public void setPluginFactory(IPluginFactory pluginFactory) {
        this.pluginFactory = pluginFactory;
    }

    public IDatabaseFactory getDatabaseFactory() {
        return databaseFactory;
    }

    public void setDatabaseFactory(IDatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    public IIconFactory getIconFactory() {
        return iconFactory;
    }

    public void setIconFactory(IIconFactory iconFactory) {
        this.iconFactory = iconFactory;
    }
    
}

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

package terp.plugin;

import terp.plugin.data.IDatabase;

/**
 *
 * @author ilknur
 */
public class Application {
    
    private static Application app;
    private IDatabase database;
    private IMenuManager menuManager;
    private IUser user;
    private IDesktopManager desktop;
    
    private Application(){
        
    }
    
    /**
     * get instance of application
     * @return 
     */
    public static synchronized Application getInstance(){
        if(app == null){
            app = new Application();
        }
        
        return app;
    }
    
    /**
     * get database
     * @return 
     */
    public synchronized IDatabase getDatabase(){
        return this.database;
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
     * set database context
     * @param db 
     */
    public void setDatabase(IDatabase db){
        this.database = db;
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
}

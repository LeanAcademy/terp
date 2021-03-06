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

package com.terp.plugin;

/**
 *
 * @author ilknur
 */
public interface IPlugin {
    
    /**
     * name of plugin
     * @return 
     */
    public String getName();
    
    /**
     * Version of T-ERP 
     * @return 
     */
    public String getSystemVersion();
    
    
    /**
     * Version of plugin
     * @return 
     */
    public String getPluginVersion();
    
    /**
     * type of plugin
     * core = 1 :it cannot be removed
     * user defined = 0
     * @return 
     */
    public int getType();
            
    /*
    * run plugin
    */
    public void run();
    
    /**
     * install plugin
     */
    public void install();
    
    /**
     * get if plugin already installed
     * @return 
     */
    public boolean isInstalled();
    
    /**
     * load programs related to this plugin
     * @param program 
     */
    public void loadProgram(String program);
}

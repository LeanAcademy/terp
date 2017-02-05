/*
 * Copyright (C) 2014 ilknur
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

package com.terp.plugins;

import com.terp.data.dao.PluginSourceDao;
import com.terp.util.TerpClassLoader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.terp.data.model.PluginSource;
import com.terp.plugin.*;

/**
 * IPlugin manager class
 * @author ilknur
 */
public class PluginFactoryImpl implements IPluginFactory {
    
    //private variables
    private static final Map<Long, IPlugin> pluginList = new HashMap<>();
    private final String version = "1.0";
    
    /**
     * constructor
     */
    public PluginFactoryImpl(){}
    
    /**
     * Plug in loader. 
     * @param pluginId
     * @return  IPlugin
     */
    private IPlugin loadPlugin(final Long pluginId){

        URL url = null;
        
        // get plugin details
        //PluginSource pluginSource = new PluginSource();
        PluginSourceDao pluginSourceDao = new PluginSourceDao();
        PluginSource pluginSource = (PluginSource) pluginSourceDao.firstOrDefault(pluginId);
        
        String jarName = pluginSource.getPluginName() + ".jar";
        
        try {
            Path filePath = Paths.get("../terp/plugins");
            
            DirectoryStream<Path> stream = 
                    Files.newDirectoryStream(filePath, "*.jar");
            
            // TODO : implement jar package download
            for(Path file : stream) {
                
                //save file name
                String strFile = file.getFileName().toString();
                
                //debug logger
                LOG.log(Level.INFO, file.getFileName().toString());
                
                if(strFile == null ? jarName == null : strFile.equals(jarName)){
                    url = file.toUri().toURL();
            
                    //URL[] urls = new URL[]{url};
                    TerpClassLoader.addFile(file.toFile());
                    
                    
                    //URLClassLoader cl = new URLClassLoader(urls);
                    Class<IPlugin> plg = (Class<IPlugin>)ClassLoader
                            .getSystemClassLoader()
                            .loadClass(pluginSource.getMainClassName());
                    //plg.newInstance().run(); // TODO : delete test
                    return plg.newInstance();
                } 
            }
            
        } catch (MalformedURLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (IOException | ClassNotFoundException | InstantiationException 
                | IllegalAccessException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    /**
     * load all pluginList into memory
     */
    public void loadAllPlugin(){
        
        int count = 0;
        
        //create database connection
        PluginSourceDao db = new PluginSourceDao();
        
        // load installed pluginList
        List<PluginSource> plgList = db.findAll();
        
        for(Object plg : plgList){
            
            //load plugin
            IPlugin p = this.loadPlugin(((PluginSource)plg).getRowId());
            
            //check plugin version
            if(p.getSystemVersion() == null 
                    || !p.getSystemVersion().equals(this.version)){
                //log and show error
                Object[] params = new Object[]{p.getName(), p.getSystemVersion()};
                LOG.log(Level.SEVERE, "{0} version error : {1}", params);

            } else {
                
                //add plugin into list
                pluginList.put(((PluginSource)plg).getRowId(), p);
                
                // run plugin's main class and method
                try{                    
                    p.run();                    
                }catch(Exception e){
                    LOG.log(Level.SEVERE, null, e);
                }
                
                // information to debug system
                LOG.log(Level.INFO, "Plugin {0} is loaded", p.getName());
            }
        }
        
    }
    
    /**
     * Find plugin and load it if not already done.
     * @param pluginId
     * @return new instance IPlugin
     */
    @Override
    public IPlugin getPlugin(final Long pluginId){
        
        //check if plugin already loaded
        if(pluginList.containsKey(pluginId)){
            return pluginList.get(pluginId);
        } else {
            IPlugin plg;
            plg = loadPlugin(pluginId);
            return plg;
        }
    }
    
    /**
     * logger
     */
    private static final Logger LOG = Logger.getLogger(PluginFactoryImpl.class.getName());
    
}



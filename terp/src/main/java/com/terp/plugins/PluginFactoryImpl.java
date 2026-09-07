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
import com.terp.data.model.PluginSource;
import com.terp.plugin.IPlugin;
import com.terp.plugin.IPluginFactory;
import com.terp.util.TerpHome;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Discovers plugin JARs under {@code plugins/} via {@link ServiceLoader}.
 */
public class PluginFactoryImpl implements IPluginFactory {

    private static final Logger LOG = Logger.getLogger(PluginFactoryImpl.class.getName());

    private final Map<Long, IPlugin> pluginsById = new HashMap<>();
    private final Map<String, IPlugin> pluginsByName = new HashMap<>();
    private final List<URLClassLoader> pluginLoaders = new ArrayList<>();
    private final String version = "1.0";

    public void loadAllPlugin() {
        Path pluginsDir = TerpHome.pluginsDir();
        if (!Files.isDirectory(pluginsDir)) {
            LOG.log(Level.WARNING, "Plugin directory does not exist: {0}", pluginsDir);
            return;
        }

        Map<String, PluginSource> registryByName = loadRegistryByName();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pluginsDir, "*.jar")) {
            for (Path jar : stream) {
                loadJar(jar, registryByName);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to scan plugin directory " + pluginsDir, ex);
        }
    }

    @Override
    public IPlugin getPlugin(final Long pluginId) {
        IPlugin plugin = pluginsById.get(pluginId);
        if (plugin != null) {
            return plugin;
        }

        try {
            PluginSourceDao dao = new PluginSourceDao();
            PluginSource source = dao.firstOrDefault(pluginId);
            if (source != null && source.getPluginName() != null) {
                plugin = pluginsByName.get(source.getPluginName());
                if (plugin != null) {
                    pluginsById.put(pluginId, plugin);
                }
            }
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "Could not resolve plugin id " + pluginId, ex);
        }

        return plugin;
    }

    @Override
    public IPlugin getPlugin(final String pluginName) {
        if (pluginName == null) {
            return null;
        }
        return pluginsByName.get(pluginName);
    }

    private Map<String, PluginSource> loadRegistryByName() {
        Map<String, PluginSource> byName = new HashMap<>();
        try {
            PluginSourceDao dao = new PluginSourceDao();
            List<PluginSource> rows = dao.findAll();
            if (rows == null) {
                return byName;
            }
            for (PluginSource source : rows) {
                if (source.getPluginName() != null) {
                    byName.put(source.getPluginName(), source);
                }
            }
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "Plugin registry table could not be read; loading JARs by name only", ex);
        }
        return byName;
    }

    private void loadJar(Path jar, Map<String, PluginSource> registryByName) {
        LOG.log(Level.INFO, "Scanning plugin jar {0}", jar.getFileName());
        try {
            URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{jar.toUri().toURL()},
                    IPlugin.class.getClassLoader());
            pluginLoaders.add(classLoader);

            ServiceLoader<IPlugin> loader = ServiceLoader.load(IPlugin.class, classLoader);
            boolean found = false;
            for (IPlugin plugin : loader) {
                found = true;
                accept(plugin, registryByName);
            }
            if (!found) {
                LOG.log(Level.WARNING,
                        "No IPlugin service in {0}. Add META-INF/services/com.terp.plugin.IPlugin",
                        jar.getFileName());
            }
        } catch (IOException | RuntimeException | ServiceConfigurationError ex) {
            LOG.log(Level.SEVERE, "Failed to load plugin jar " + jar, ex);
        }
    }

    private void accept(IPlugin plugin, Map<String, PluginSource> registryByName) {
        if (plugin.getSystemVersion() == null || !plugin.getSystemVersion().equals(this.version)) {
            LOG.log(Level.SEVERE, "{0} version error : {1}",
                    new Object[]{plugin.getName(), plugin.getSystemVersion()});
            return;
        }

        pluginsByName.put(plugin.getName(), plugin);
        PluginSource registered = registryByName.get(plugin.getName());
        if (registered != null && registered.getRowId() != null) {
            pluginsById.put(registered.getRowId(), plugin);
        }

        try {
            plugin.run();
            LOG.log(Level.INFO, "Plugin {0} is loaded", plugin.getName());
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Plugin " + plugin.getName() + " failed during run()", ex);
        }
    }
}

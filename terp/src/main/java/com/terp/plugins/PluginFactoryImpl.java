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
        if (pluginId == null) {
            return null;
        }

        IPlugin plugin = pluginsById.get(pluginId);
        if (plugin != null) {
            return plugin;
        }

        try {
            PluginSourceDao dao = new PluginSourceDao();
            PluginSource source = dao.firstOrDefault(pluginId);
            plugin = findLoadedPlugin(source);
            if (plugin != null) {
                pluginsById.put(pluginId, plugin);
                return plugin;
            }
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "Could not resolve plugin id " + pluginId, ex);
        }

        return fallbackLoadedPlugin();
    }

    @Override
    public IPlugin getPlugin(final String pluginName) {
        if (pluginName == null) {
            return null;
        }
        IPlugin plugin = pluginsByName.get(pluginName);
        if (plugin != null) {
            return plugin;
        }
        for (Map.Entry<String, IPlugin> entry : pluginsByName.entrySet()) {
            if (namesMatch(entry.getKey(), pluginName)) {
                return entry.getValue();
            }
        }
        return null;
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
        PluginSource registered = findRegistry(plugin, registryByName);
        if (registered == null) {
            registered = persistRegistry(plugin);
            if (registered != null && registered.getPluginName() != null) {
                registryByName.put(registered.getPluginName(), registered);
            }
        }
        if (registered != null && registered.getRowId() != null) {
            pluginsById.put(registered.getRowId(), plugin);
            LOG.log(Level.INFO, "Plugin {0} mapped to registry id {1}",
                    new Object[]{plugin.getName(), registered.getRowId()});
        }

        try {
            plugin.run();
            LOG.log(Level.INFO, "Plugin {0} is loaded", plugin.getName());
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Plugin " + plugin.getName() + " failed during run()", ex);
        }
    }

    private PluginSource findRegistry(IPlugin plugin, Map<String, PluginSource> registryByName) {
        PluginSource exact = registryByName.get(plugin.getName());
        if (exact != null) {
            return exact;
        }
        String pluginClass = plugin.getClass().getName();
        for (PluginSource source : registryByName.values()) {
            if (namesMatch(plugin.getName(), source.getPluginName())
                    || pluginClass.equals(source.getMainClassName())) {
                return source;
            }
        }
        return null;
    }

    private PluginSource persistRegistry(IPlugin plugin) {
        try {
            PluginSource source = new PluginSource();
            source.setPluginName(plugin.getName());
            source.setType(plugin.getType());
            source.setMainClassName(plugin.getClass().getName());
            PluginSource saved = new PluginSourceDao().addOrUpdate(source);
            if (saved != null && saved.getRowId() != null) {
                LOG.log(Level.INFO, "Registered plugin {0} as eklenti id {1}",
                        new Object[]{plugin.getName(), saved.getRowId()});
            }
            return saved;
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "Could not register plugin " + plugin.getName() + " in eklenti table", ex);
            return null;
        }
    }

    private IPlugin findLoadedPlugin(PluginSource source) {
        if (source == null) {
            return null;
        }
        IPlugin plugin = getPlugin(source.getPluginName());
        if (plugin != null) {
            return plugin;
        }
        if (source.getMainClassName() != null) {
            for (IPlugin loaded : pluginsByName.values()) {
                if (source.getMainClassName().equals(loaded.getClass().getName())) {
                    return loaded;
                }
            }
        }
        return null;
    }

    private IPlugin fallbackLoadedPlugin() {
        if (pluginsByName.size() == 1) {
            return pluginsByName.values().iterator().next();
        }
        return pluginsByName.get("terp.core");
    }

    static boolean namesMatch(String pluginName, String registeredName) {
        if (pluginName == null || registeredName == null) {
            return false;
        }
        if (pluginName.equals(registeredName)) {
            return true;
        }
        String left = canonicalPluginName(pluginName);
        String right = canonicalPluginName(registeredName);
        return left.equals(right)
                || registeredName.startsWith(pluginName + "-")
                || pluginName.startsWith(registeredName + "-");
    }

    static String canonicalPluginName(String name) {
        return name.replaceAll("-(?:\\d+(?:\\.\\d+)*).*$", "");
    }
}

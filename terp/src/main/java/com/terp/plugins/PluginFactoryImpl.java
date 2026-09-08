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

import com.terp.data.dao.MenuSourceDao;
import com.terp.data.dao.PluginSourceDao;
import com.terp.data.model.MenuSource;
import com.terp.data.model.PluginSource;
import com.terp.plugin.IPlugin;
import com.terp.plugin.IPluginFactory;
import com.terp.plugin.PluginMenu;
import com.terp.util.HibernateUtil;
import com.terp.util.TerpHome;
import com.terp.util.TerpProperties;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Session;

/**
 * Discovers plugin JARs under {@code plugins/} via {@link ServiceLoader}.
 */
public class PluginFactoryImpl implements IPluginFactory {

    private static final Logger LOG = Logger.getLogger(PluginFactoryImpl.class.getName());

    private final Map<Long, IPlugin> pluginsById = new HashMap<>();
    private final Map<String, IPlugin> pluginsByName = new LinkedHashMap<>();
    private final List<URLClassLoader> pluginLoaders = new ArrayList<>();
    private final Set<Class<?>> persistentClasses = new LinkedHashSet<>();
    private final String version = "1.0";
    private boolean scanned;
    private boolean bound;
    private boolean ran;

    /**
     * Discover plugin JARs and collect entity classes. Does not touch the database.
     */
    public void scanJars() {
        if (scanned) {
            return;
        }
        Path pluginsDir = TerpHome.pluginsDir();
        if (!Files.isDirectory(pluginsDir)) {
            LOG.log(Level.WARNING, "Plugin directory does not exist: {0}", pluginsDir);
            scanned = true;
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pluginsDir, "*.jar")) {
            for (Path jar : stream) {
                scanJar(jar);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to scan plugin directory " + pluginsDir, ex);
        }
        scanned = true;
    }

    /**
     * Map scanned plugins to {@code eklenti} rows and write missing menus.
     * Does not call {@link IPlugin#run()}; the host must do that after the
     * main toolbar exists so plugins can use {@code addToolKit}.
     */
    public void bindRegistry() {
        if (bound) {
            return;
        }
        realignSeededIdentity("terp.eklenti");
        realignSeededIdentity("terp.menu");
        Map<String, PluginSource> registryByName = loadRegistryByName();
        for (IPlugin plugin : new ArrayList<>(pluginsByName.values())) {
            bind(plugin, registryByName);
        }
        ensureHostMenus();
        bound = true;
    }

    /**
     * Call {@link IPlugin#run()} after the desktop and menu manager are set.
     */
    public void runBoundPlugins() {
        if (ran) {
            return;
        }
        if (!bound) {
            bindRegistry();
        }
        for (IPlugin plugin : new ArrayList<>(pluginsByName.values())) {
            try {
                plugin.run();
                LOG.log(Level.INFO, "Plugin {0} is loaded", plugin.getName());
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, "Plugin " + plugin.getName() + " failed during run()", ex);
            }
        }
        ran = true;
    }

    /**
     * Map scanned plugins to {@code eklenti} rows and call {@link IPlugin#run()}.
     */
    public void bindRegistryAndRun() {
        bindRegistry();
        runBoundPlugins();
    }

    public List<Class<?>> persistentClasses() {
        return List.copyOf(persistentClasses);
    }

    /**
     * Class loaders that defined plugin types. Hibernate must use these; it
     * reloads entity classes by name and cannot see {@code plugins/*.jar} otherwise.
     */
    public List<ClassLoader> classLoaders() {
        return List.copyOf(pluginLoaders);
    }

    /**
     * Copy a plugin JAR into {@code plugins/}, require an IPlugin service, and
     * register it in the eklenti table when Hibernate is ready.
     *
     * @return human-readable install log
     */
    public String installJar(Path source) throws IOException {
        if (source == null || !Files.isRegularFile(source)) {
            throw new IOException("Plugin file not found: " + source);
        }
        Path pluginsDir = TerpHome.pluginsDir();
        Files.createDirectories(pluginsDir);
        Path dest = pluginsDir.resolve(source.getFileName()).normalize();
        if (!dest.startsWith(pluginsDir.toAbsolutePath().normalize())) {
            throw new IOException("Invalid plugin destination");
        }
        Files.copy(source, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        if (!hasPluginService(dest)) {
            Files.deleteIfExists(dest);
            throw new IOException("No META-INF/services/com.terp.plugin.IPlugin in " + dest.getFileName());
        }

        InspectedPlugin inspected = inspectJar(dest);
        if (inspected.plugin == null) {
            Files.deleteIfExists(dest);
            throw new IOException("Could not load IPlugin from " + dest.getFileName());
        }
        if (inspected.plugin.getSystemVersion() == null
                || !version.equals(inspected.plugin.getSystemVersion())) {
            Files.deleteIfExists(dest);
            throw new IOException("Plugin system version must be " + version
                    + ", got " + inspected.plugin.getSystemVersion());
        }

        StringBuilder log = new StringBuilder();
        log.append("Copied ").append(dest.getFileName()).append(" to ").append(pluginsDir).append('\n');
        log.append("Plugin name: ").append(inspected.plugin.getName()).append('\n');
        log.append("Plugin version: ").append(inspected.plugin.getPluginVersion()).append('\n');

        if (com.terp.util.HibernateUtil.isInitialized()) {
            Map<String, PluginSource> registry = loadRegistryByName();
            PluginSource existing = findRegistry(inspected.plugin, registry);
            if (existing == null) {
                PluginSource saved = persistRegistry(inspected.plugin);
                if (saved != null) {
                    log.append("Registered as eklenti id ").append(saved.getRowId()).append('\n');
                } else {
                    log.append("Could not write eklenti row.\n");
                }
            } else {
                log.append("Already registered as eklenti id ").append(existing.getRowId()).append('\n');
            }
        }

        int entities = inspected.entityCount;
        if (entities > 0) {
            log.append(entities).append(" persistent class(es). Restart TERP so Hibernate can map new tables.\n");
        } else {
            log.append("Restart TERP to load the plugin.\n");
        }
        return log.toString();
    }

    private static boolean hasPluginService(Path jar) throws IOException {
        try (java.util.jar.JarFile jarFile = new java.util.jar.JarFile(jar.toFile())) {
            return jarFile.getEntry("META-INF/services/com.terp.plugin.IPlugin") != null;
        }
    }

    private InspectedPlugin inspectJar(Path jar) throws IOException {
        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{jar.toUri().toURL()},
                IPlugin.class.getClassLoader());
        pluginLoaders.add(classLoader);
        try {
            ServiceLoader<IPlugin> loader = ServiceLoader.load(IPlugin.class, classLoader);
            for (IPlugin plugin : loader) {
                int count = 0;
                List<Class<?>> classes = plugin.getPersistentClasses();
                if (classes != null) {
                    count = (int) classes.stream().filter(c -> c != null).count();
                }
                return new InspectedPlugin(plugin, count);
            }
        } catch (RuntimeException | ServiceConfigurationError ex) {
            throw new IOException("Failed to inspect " + jar.getFileName(), ex);
        }
        return new InspectedPlugin(null, 0);
    }

    private static final class InspectedPlugin {
        private final IPlugin plugin;
        private final int entityCount;

        private InspectedPlugin(IPlugin plugin, int entityCount) {
            this.plugin = plugin;
            this.entityCount = entityCount;
        }
    }

    public void loadAllPlugin() {
        scanJars();
        bindRegistryAndRun();
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

    private void scanJar(Path jar) {
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
                acceptScan(plugin);
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

    private void acceptScan(IPlugin plugin) {
        if (plugin.getSystemVersion() == null || !plugin.getSystemVersion().equals(this.version)) {
            LOG.log(Level.SEVERE, "{0} version error : {1}",
                    new Object[]{plugin.getName(), plugin.getSystemVersion()});
            return;
        }
        pluginsByName.put(plugin.getName(), plugin);
        List<Class<?>> classes = plugin.getPersistentClasses();
        if (classes != null) {
            for (Class<?> type : classes) {
                if (type != null) {
                    persistentClasses.add(type);
                }
            }
        }
        LOG.log(Level.INFO, "Plugin {0} is scanned", plugin.getName());
    }

    private void bind(IPlugin plugin, Map<String, PluginSource> registryByName) {
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
            ensureMenus(plugin, registered.getRowId());
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

    private void ensureMenus(IPlugin plugin, Long pluginRowId) {
        List<PluginMenu> menus = plugin.getMenus();
        if (menus == null || menus.isEmpty()) {
            return;
        }
        MenuSourceDao dao = new MenuSourceDao();
        List<PluginMenu> folders = new ArrayList<>();
        List<PluginMenu> programs = new ArrayList<>();
        for (PluginMenu menu : menus) {
            if (menu == null || menu.getMenuId() == null || menu.getMenuId().isBlank()) {
                continue;
            }
            if (menu.isFolder()) {
                folders.add(menu);
            } else {
                programs.add(menu);
            }
        }
        for (PluginMenu menu : folders) {
            insertMenuIfMissing(dao, menu, pluginRowId, 0L);
        }
        for (PluginMenu menu : programs) {
            Long parentRowId = 0L;
            if (menu.getParentMenuId() != null && !menu.getParentMenuId().isBlank()) {
                MenuSource parent = findMenuByCode(dao, menu.getParentMenuId());
                if (parent == null || parent.getRowId() == null) {
                    LOG.log(Level.WARNING, "Plugin {0} menu {1} skipped: parent {2} not found",
                            new Object[]{plugin.getName(), menu.getMenuId(), menu.getParentMenuId()});
                    continue;
                }
                parentRowId = parent.getRowId();
            }
            insertMenuIfMissing(dao, menu, pluginRowId, parentRowId);
        }
    }

    private void insertMenuIfMissing(MenuSourceDao dao, PluginMenu spec, Long pluginRowId,
            Long parentRowId) {
        if (findMenuByCode(dao, spec.getMenuId()) != null) {
            return;
        }
        MenuSource row = new MenuSource();
        row.setMenuId(spec.getMenuId());
        row.setMenuName(spec.getTitle() == null ? spec.getMenuId() : spec.getTitle());
        row.setMenuType(spec.isFolder() ? 0 : 1);
        row.setMenuParent(parentRowId == null ? 0L : parentRowId);
        row.setProgramName(spec.isFolder() ? null : spec.getProgramName());
        row.setIsPlugin(1);
        row.setPluginId(pluginRowId);
        row.setStatus(0);
        MenuSource saved = dao.addOrUpdate(row);
        if (saved != null) {
            LOG.log(Level.INFO, "Inserted menu {0} for plugin id {1}",
                    new Object[]{spec.getMenuId(), pluginRowId});
        }
    }

    private static MenuSource findMenuByCode(MenuSourceDao dao, String menuId) {
        String escaped = menuId.replace("'", "''");
        return dao.firstOrDefault("from MenuSource e where e.menuId = '" + escaped + "'");
    }

    private void ensureHostMenus() {
        MenuSourceDao dao = new MenuSourceDao();
        MenuSource sys01 = findMenuByCode(dao, "SYS01");
        Long parentId = sys01 == null || sys01.getRowId() == null ? 1L : sys01.getRowId();
        MenuSource sys03 = findMenuByCode(dao, "SYS03");
        if (sys03 != null && (sys03.getProgramName() == null || sys03.getProgramName().isBlank())) {
            sys03.setProgramName("GroupForm");
            sys03.setIsPlugin(0);
            dao.addOrUpdate(sys03);
        }
        if (findMenuByCode(dao, "SYS05") == null) {
            MenuSource users = new MenuSource();
            users.setMenuId("SYS05");
            users.setMenuName("Kullanıcılar");
            users.setMenuType(1);
            users.setMenuParent(parentId);
            users.setProgramName("UserForm");
            users.setIsPlugin(0);
            users.setStatus(0);
            dao.addOrUpdate(users);
        }
    }

    private PluginSource persistRegistry(IPlugin plugin) {
        PluginSourceDao dao = new PluginSourceDao();
        PluginSource existing = lookupRegistry(dao, plugin);
        if (existing != null) {
            return existing;
        }
        try {
            PluginSource source = new PluginSource();
            source.setPluginName(plugin.getName());
            source.setType(plugin.getType());
            source.setMainClassName(plugin.getClass().getName());
            PluginSource saved = dao.addOrUpdate(source);
            if (saved != null && saved.getRowId() != null) {
                LOG.log(Level.INFO, "Registered plugin {0} as eklenti id {1}",
                        new Object[]{plugin.getName(), saved.getRowId()});
            }
            return saved;
        } catch (RuntimeException ex) {
            PluginSource retry = lookupRegistry(dao, plugin);
            if (retry != null) {
                LOG.log(Level.INFO, "Plugin {0} already registered as eklenti id {1}",
                        new Object[]{plugin.getName(), retry.getRowId()});
                return retry;
            }
            LOG.log(Level.WARNING, "Could not register plugin " + plugin.getName() + " in eklenti table", ex);
            return null;
        }
    }

    private static PluginSource lookupRegistry(PluginSourceDao dao, IPlugin plugin) {
        String name = plugin.getName() == null ? "" : plugin.getName().replace("'", "''");
        PluginSource byName = dao.firstOrDefault("from PluginSource e where e.pluginName = '" + name + "'");
        if (byName != null) {
            return byName;
        }
        String className = plugin.getClass().getName().replace("'", "''");
        return dao.firstOrDefault("from PluginSource e where e.mainClassName = '" + className + "'");
    }

    /**
     * import.sql inserts explicit {@code ref_num} values; Derby's identity
     * counter stays at 1, so the next INSERT hits a duplicate primary key.
     */
    private static void realignSeededIdentity(String table) {
        if (!isDerby()) {
            return;
        }
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            Object maxObj = session.createNativeQuery(
                    "select coalesce(max(ref_num), 0) from " + table, Long.class)
                    .uniqueResult();
            long next = (maxObj instanceof Number n ? n.longValue() : 0L) + 1L;
            session.createNativeMutationQuery(
                    "ALTER TABLE " + table + " ALTER COLUMN ref_num RESTART WITH " + next)
                    .executeUpdate();
            session.getTransaction().commit();
            LOG.log(Level.INFO, "Derby identity for {0}.ref_num restarted at {1}",
                    new Object[]{table, next});
        } catch (RuntimeException ex) {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            LOG.log(Level.FINE, "Could not realign identity for " + table, ex);
        } finally {
            session.close();
        }
    }

    private static boolean isDerby() {
        Properties hibernate = TerpProperties.getInstance().getHibernateProps();
        if (hibernate == null) {
            return false;
        }
        String url = hibernate.getProperty("hibernate.connection.url", "");
        if (url.isEmpty()) {
            url = hibernate.getProperty("jakarta.persistence.jdbc.url", "");
        }
        return url.contains("derby");
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

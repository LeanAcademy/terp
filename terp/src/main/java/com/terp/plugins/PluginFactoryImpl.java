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
    private final Map<String, String> jarByPluginName = new LinkedHashMap<>();
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
        if (inspected == null || inspected.name == null) {
            Files.deleteIfExists(dest);
            throw new IOException("Could not load IPlugin from " + dest.getFileName());
        }
        if (inspected.systemVersion == null || !version.equals(inspected.systemVersion)) {
            Files.deleteIfExists(dest);
            throw new IOException("Plugin system version must be " + version
                    + ", got " + inspected.systemVersion);
        }

        StringBuilder log = new StringBuilder();
        log.append("Kopyalandı: ").append(dest.getFileName()).append(" → ").append(pluginsDir).append('\n');
        log.append("Eklenti: ").append(inspected.name).append('\n');
        log.append("Sürüm: ").append(inspected.pluginVersion).append('\n');

        String jarName = dest.getFileName().toString();
        if (com.terp.util.HibernateUtil.isInitialized()) {
            Map<String, PluginSource> registry = loadRegistryByName();
            PluginSource existing = findRegistry(inspected.name, inspected.mainClassName, registry);
            if (existing == null) {
                PluginSource saved = persistRegistry(inspected.name, inspected.type,
                        inspected.mainClassName, jarName);
                if (saved != null) {
                    log.append("eklenti kaydı id ").append(saved.getRowId()).append('\n');
                } else {
                    log.append("eklenti satırı yazılamadı.\n");
                }
            } else {
                existing.setJarFileName(jarName);
                existing.setType(inspected.type);
                existing.setMainClassName(inspected.mainClassName);
                new PluginSourceDao().addOrUpdate(existing);
                log.append("Kayıt güncellendi, eklenti id ").append(existing.getRowId()).append('\n');
            }
        }

        int entities = inspected.entityCount;
        if (entities > 0) {
            log.append(entities).append(" kalıcı sınıf. Yeni tablolar için TERP'i yeniden başlatın.\n");
        } else {
            log.append("Eklentinin yüklenmesi için TERP'i yeniden başlatın.\n");
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
        try {
            ServiceLoader<IPlugin> loader = ServiceLoader.load(IPlugin.class, classLoader);
            for (IPlugin plugin : loader) {
                int count = 0;
                List<Class<?>> classes = plugin.getPersistentClasses();
                if (classes != null) {
                    count = (int) classes.stream().filter(c -> c != null).count();
                }
                return new InspectedPlugin(plugin.getName(), plugin.getPluginVersion(),
                        plugin.getSystemVersion(), plugin.getType(),
                        plugin.getClass().getName(), count);
            }
        } catch (RuntimeException | ServiceConfigurationError ex) {
            throw new IOException("Failed to inspect " + jar.getFileName(), ex);
        } finally {
            try {
                classLoader.close();
            } catch (IOException ex) {
                LOG.log(Level.FINE, "Could not close inspector for " + jar.getFileName(), ex);
            }
        }
        return null;
    }

    /**
     * Merge {@code plugins/*.jar} with {@code eklenti} rows for the management list.
     */
    public List<PluginInstallInfo> listPlugins() {
        Map<String, PluginInstallInfo> byName = new LinkedHashMap<>();
        Path pluginsDir = TerpHome.pluginsDir();
        if (Files.isDirectory(pluginsDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(pluginsDir, "*.jar")) {
                for (Path jar : stream) {
                    try {
                        InspectedPlugin inspected = inspectJar(jar);
                        if (inspected == null || inspected.name == null) {
                            continue;
                        }
                        PluginInstallInfo info = byName.computeIfAbsent(inspected.name,
                                key -> new PluginInstallInfo());
                        info.setPluginName(inspected.name);
                        info.setPluginVersion(inspected.pluginVersion == null
                                || inspected.pluginVersion.isBlank() ? "—" : inspected.pluginVersion);
                        info.setType(inspected.type);
                        info.setJarFileName(jar.getFileName().toString());
                        info.setHasJar(true);
                    } catch (IOException | RuntimeException ex) {
                        LOG.log(Level.WARNING, "Could not inspect " + jar.getFileName(), ex);
                    }
                }
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Could not list plugin directory " + pluginsDir, ex);
            }
        }
        for (PluginSource source : loadRegistryByName().values()) {
            if (source.getPluginName() == null) {
                continue;
            }
            PluginInstallInfo info = findListed(byName, source);
            if (info == null) {
                info = new PluginInstallInfo();
                info.setPluginName(source.getPluginName());
                info.setPluginVersion("—");
                info.setType(source.getType());
                info.setJarFileName(source.getJarFileName());
                byName.put(source.getPluginName(), info);
            }
            info.setHasRegistry(true);
            if ((info.getJarFileName() == null || info.getJarFileName().isBlank())
                    && source.getJarFileName() != null) {
                info.setJarFileName(source.getJarFileName());
            }
            if (!info.isHasJar()) {
                info.setType(source.getType());
            }
        }
        return new ArrayList<>(byName.values());
    }

    /**
     * Delete the plugin JAR and {@code eklenti} row. Menus and business tables stay.
     * The running session is unchanged; TERP must be restarted.
     */
    public String uninstall(String pluginName) throws IOException {
        if (pluginName == null || pluginName.isBlank()) {
            throw new IOException("Eklenti adı boş");
        }
        PluginInstallInfo info = null;
        for (PluginInstallInfo row : listPlugins()) {
            if (pluginName.equals(row.getPluginName()) || namesMatch(pluginName, row.getPluginName())) {
                info = row;
                break;
            }
        }
        if (info == null) {
            throw new IOException("Eklenti bulunamadı: " + pluginName);
        }
        if (info.getType() == 1) {
            throw new IOException("Çekirdek eklenti çıkarılamaz: " + info.getPluginName());
        }
        StringBuilder log = new StringBuilder();
        Path pluginsDir = TerpHome.pluginsDir();
        String jarName = info.getJarFileName();
        if (jarName != null && !jarName.isBlank() && Files.isDirectory(pluginsDir)) {
            Path dest = pluginsDir.resolve(jarName).normalize();
            if (!dest.startsWith(pluginsDir.toAbsolutePath().normalize())) {
                throw new IOException("Geçersiz eklenti yolu");
            }
            if (Files.deleteIfExists(dest)) {
                log.append("JAR silindi: ").append(jarName).append('\n');
            }
        }
        PluginSourceDao dao = new PluginSourceDao();
        String escaped = info.getPluginName().replace("'", "''");
        PluginSource row = dao.firstOrDefault(
                "from PluginSource e where e.pluginName = '" + escaped + "'");
        if (row != null && row.getRowId() != null) {
            dao.delete(row.getRowId());
            log.append("eklenti kaydı silindi (id ").append(row.getRowId()).append(").\n");
        }
        if (log.isEmpty()) {
            log.append("Silinecek JAR veya kayıt yok.\n");
        }
        log.append("Menü ve iş tabloları duruyor. TERP'i yeniden başlatın.\n");
        return log.toString();
    }

    private static PluginInstallInfo findListed(Map<String, PluginInstallInfo> byName, PluginSource source) {
        PluginInstallInfo exact = byName.get(source.getPluginName());
        if (exact != null) {
            return exact;
        }
        for (PluginInstallInfo info : byName.values()) {
            if (namesMatch(info.getPluginName(), source.getPluginName())
                    || (source.getMainClassName() != null && source.getMainClassName().equals(info.getPluginName()))) {
                return info;
            }
        }
        return null;
    }

    private static final class InspectedPlugin {
        private final String name;
        private final String pluginVersion;
        private final String systemVersion;
        private final int type;
        private final String mainClassName;
        private final int entityCount;

        private InspectedPlugin(String name, String pluginVersion, String systemVersion,
                int type, String mainClassName, int entityCount) {
            this.name = name;
            this.pluginVersion = pluginVersion;
            this.systemVersion = systemVersion;
            this.type = type;
            this.mainClassName = mainClassName;
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
                if (plugin.getName() != null) {
                    jarByPluginName.put(plugin.getName(), jar.getFileName().toString());
                }
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
            registered = persistRegistry(plugin.getName(), plugin.getType(),
                    plugin.getClass().getName(), jarByPluginName.get(plugin.getName()));
            if (registered != null && registered.getPluginName() != null) {
                registryByName.put(registered.getPluginName(), registered);
            }
        } else {
            fillJarFileName(registered, jarByPluginName.get(plugin.getName()));
        }
        if (registered != null && registered.getRowId() != null) {
            pluginsById.put(registered.getRowId(), plugin);
            LOG.log(Level.INFO, "Plugin {0} mapped to registry id {1}",
                    new Object[]{plugin.getName(), registered.getRowId()});
            ensureMenus(plugin, registered.getRowId());
        }
    }

    private PluginSource findRegistry(IPlugin plugin, Map<String, PluginSource> registryByName) {
        return findRegistry(plugin.getName(), plugin.getClass().getName(), registryByName);
    }

    private PluginSource findRegistry(String pluginName, String mainClassName,
            Map<String, PluginSource> registryByName) {
        PluginSource exact = registryByName.get(pluginName);
        if (exact != null) {
            return exact;
        }
        for (PluginSource source : registryByName.values()) {
            if (namesMatch(pluginName, source.getPluginName())
                    || (mainClassName != null && mainClassName.equals(source.getMainClassName()))) {
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
        retireUndeclaredMenus(dao, plugin, pluginRowId);
    }

    private void retireUndeclaredMenus(MenuSourceDao dao, IPlugin plugin, Long pluginRowId) {
        if (pluginRowId == null) {
            return;
        }
        Set<String> declared = new LinkedHashSet<>();
        for (PluginMenu menu : plugin.getMenus()) {
            if (menu != null && menu.getMenuId() != null && !menu.getMenuId().isBlank()) {
                declared.add(menu.getMenuId());
            }
        }
        List<MenuSource> rows = dao.findAll("from MenuSource e where e.pluginId = " + pluginRowId);
        if (rows == null) {
            return;
        }
        for (MenuSource row : rows) {
            if (row == null || row.getRowId() == null || row.getMenuId() == null) {
                continue;
            }
            if (declared.contains(row.getMenuId())) {
                continue;
            }
            dao.delete(row.getRowId());
            LOG.log(Level.INFO, "Removed retired menu {0} for plugin {1}",
                    new Object[]{row.getMenuId(), plugin.getName()});
        }
    }

    private void insertMenuIfMissing(MenuSourceDao dao, PluginMenu spec, Long pluginRowId,
            Long parentRowId) {
        Long parent = parentRowId == null ? 0L : parentRowId;
        MenuSource existing = findMenuByCode(dao, spec.getMenuId());
        if (existing != null) {
            Long currentParent = existing.getMenuParent() == null ? 0L : existing.getMenuParent();
            boolean dirty = false;
            if (!parent.equals(currentParent)) {
                existing.setMenuParent(parent);
                dirty = true;
            }
            if (spec.getTitle() != null && !spec.getTitle().equals(existing.getMenuName())) {
                existing.setMenuName(spec.getTitle());
                dirty = true;
            }
            if (dirty) {
                dao.addOrUpdate(existing);
                LOG.log(Level.INFO, "Updated menu {0} parent to {1}",
                        new Object[]{spec.getMenuId(), parent});
            }
            return;
        }
        MenuSource row = new MenuSource();
        row.setMenuId(spec.getMenuId());
        row.setMenuName(spec.getTitle() == null ? spec.getMenuId() : spec.getTitle());
        row.setMenuType(spec.isFolder() ? 0 : 1);
        row.setMenuParent(parent);
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
        MenuSource sys02 = findMenuByCode(dao, "SYS02");
        if (sys02 != null && !"Eklenti yönetimi".equals(sys02.getMenuName())) {
            sys02.setMenuName("Eklenti yönetimi");
            dao.addOrUpdate(sys02);
        }
    }

    private PluginSource persistRegistry(String pluginName, int type, String mainClassName,
            String jarFileName) {
        PluginSourceDao dao = new PluginSourceDao();
        PluginSource existing = lookupRegistry(dao, pluginName, mainClassName);
        if (existing != null) {
            fillJarFileName(existing, jarFileName);
            return existing;
        }
        try {
            PluginSource source = new PluginSource();
            source.setPluginName(pluginName);
            source.setType(type);
            source.setMainClassName(mainClassName);
            source.setJarFileName(jarFileName);
            PluginSource saved = dao.addOrUpdate(source);
            if (saved != null && saved.getRowId() != null) {
                LOG.log(Level.INFO, "Registered plugin {0} as eklenti id {1}",
                        new Object[]{pluginName, saved.getRowId()});
            }
            return saved;
        } catch (RuntimeException ex) {
            PluginSource retry = lookupRegistry(dao, pluginName, mainClassName);
            if (retry != null) {
                fillJarFileName(retry, jarFileName);
                LOG.log(Level.INFO, "Plugin {0} already registered as eklenti id {1}",
                        new Object[]{pluginName, retry.getRowId()});
                return retry;
            }
            LOG.log(Level.WARNING, "Could not register plugin " + pluginName + " in eklenti table", ex);
            return null;
        }
    }

    private static void fillJarFileName(PluginSource source, String jarFileName) {
        if (source == null || jarFileName == null || jarFileName.isBlank()) {
            return;
        }
        if (jarFileName.equals(source.getJarFileName())) {
            return;
        }
        source.setJarFileName(jarFileName);
        new PluginSourceDao().addOrUpdate(source);
    }

    private static PluginSource lookupRegistry(PluginSourceDao dao, String pluginName,
            String mainClassName) {
        String name = pluginName == null ? "" : pluginName.replace("'", "''");
        PluginSource byName = dao.firstOrDefault("from PluginSource e where e.pluginName = '" + name + "'");
        if (byName != null) {
            return byName;
        }
        if (mainClassName == null || mainClassName.isBlank()) {
            return null;
        }
        String className = mainClassName.replace("'", "''");
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

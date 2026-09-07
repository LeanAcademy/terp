/*
 * Copyright (C) 2026 LeanAcademy
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
package com.terp.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;

/**
 * Registers any JDBC 4 driver: bundled modules (for example Derby), JARs in
 * {@code lib/}, and optional extra paths in hibernate.properties.
 */
public final class JdbcDriverSupport {

    public static final String DRIVER_JARS_PROPERTY = "terp.jdbc.driver.jars";
    public static final String LEGACY_DRIVER_JARS_PROPERTY = "driver.jarfile.name";

    private static final Logger LOG = Logger.getLogger(JdbcDriverSupport.class.getName());
    private static final List<URLClassLoader> DRIVER_LOADERS = new ArrayList<>();

    private JdbcDriverSupport() {
    }

    public static void install(Properties hibernateProps) {
        URLClassLoader extraLoader = loaderForExternalJars(hibernateProps);
        int count = 0;
        count += registerFrom(ServiceLoader.load(Driver.class));
        if (extraLoader != null) {
            count += registerFrom(ServiceLoader.load(Driver.class, extraLoader));
        }
        count += registerExplicitClass(hibernateProps, extraLoader);

        hibernateProps.remove("hibernate.connection.driver_class");
        hibernateProps.remove("hibernate.connection.provider_class");

        if (count == 0) {
            throw new HibernateException(
                    "No JDBC driver registered. Use a JDBC 4 driver on the module path, "
                    + "drop a driver JAR into " + TerpHome.libDir()
                    + ", or set " + DRIVER_JARS_PROPERTY + " / "
                    + LEGACY_DRIVER_JARS_PROPERTY + " in hibernate.properties.");
        }
    }

    private static URLClassLoader loaderForExternalJars(Properties hibernateProps) {
        Set<Path> jars = new LinkedHashSet<>();
        jars.addAll(jarsIn(TerpHome.libDir()));
        jars.addAll(jarsFromProperty(hibernateProps.getProperty(DRIVER_JARS_PROPERTY)));
        jars.addAll(jarsFromProperty(hibernateProps.getProperty(LEGACY_DRIVER_JARS_PROPERTY)));
        if (jars.isEmpty()) {
            return null;
        }
        List<URL> urls = new ArrayList<>();
        for (Path jar : jars) {
            try {
                urls.add(jar.toUri().toURL());
                LOG.log(Level.INFO, "JDBC driver jar {0}", jar);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Skipping JDBC jar " + jar, ex);
            }
        }
        if (urls.isEmpty()) {
            return null;
        }
        URLClassLoader loader = new URLClassLoader(
                urls.toArray(URL[]::new),
                JdbcDriverSupport.class.getClassLoader());
        DRIVER_LOADERS.add(loader);
        return loader;
    }

    private static List<Path> jarsIn(Path dir) {
        List<Path> jars = new ArrayList<>();
        if (!Files.isDirectory(dir)) {
            return jars;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.jar")) {
            for (Path jar : stream) {
                jars.add(jar.toAbsolutePath().normalize());
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Could not scan " + dir, ex);
        }
        return jars;
    }

    private static List<Path> jarsFromProperty(String value) {
        List<Path> jars = new ArrayList<>();
        if (value == null || value.isBlank()) {
            return jars;
        }
        for (String token : value.split(",")) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            Path path = Path.of(trimmed);
            if (!path.isAbsolute()) {
                path = TerpHome.get().resolve(trimmed);
            }
            path = path.normalize();
            if (Files.isRegularFile(path)) {
                jars.add(path);
            } else {
                LOG.log(Level.WARNING, "JDBC driver jar not found: {0}", path);
            }
        }
        return jars;
    }

    private static int registerFrom(Iterable<Driver> drivers) {
        int count = 0;
        for (Driver driver : drivers) {
            if (register(driver)) {
                count++;
            }
        }
        return count;
    }

    private static int registerExplicitClass(Properties hibernateProps, ClassLoader extraLoader) {
        String driverClass = hibernateProps.getProperty("hibernate.connection.driver_class");
        if (driverClass == null || driverClass.isBlank()) {
            return 0;
        }
        ClassLoader[] loaders = extraLoader == null
                ? new ClassLoader[]{JdbcDriverSupport.class.getClassLoader()}
                : new ClassLoader[]{extraLoader, JdbcDriverSupport.class.getClassLoader()};
        for (ClassLoader loader : loaders) {
            try {
                Driver driver = (Driver) Class.forName(driverClass, true, loader)
                        .getDeclaredConstructor()
                        .newInstance();
                if (register(driver)) {
                    return 1;
                }
            } catch (ReflectiveOperationException ex) {
                LOG.log(Level.FINE, "Driver class {0} not in {1}",
                        new Object[]{driverClass, loader});
            }
        }
        throw new HibernateException("Could not load JDBC driver class " + driverClass
                + ". Place the vendor JAR in " + TerpHome.libDir()
                + " or set " + DRIVER_JARS_PROPERTY + ".");
    }

    private static boolean register(Driver driver) {
        try {
            DriverManager.registerDriver(new DriverShim(driver));
            LOG.log(Level.INFO, "Registered JDBC driver {0}", driver.getClass().getName());
            return true;
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, "Could not register " + driver.getClass().getName(), ex);
            return false;
        }
    }

    /**
     * DriverManager only accepts drivers loaded by the caller classloader.
     */
    private static final class DriverShim implements Driver {
        private final Driver delegate;

        private DriverShim(Driver delegate) {
            this.delegate = delegate;
        }

        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            return delegate.connect(url, info);
        }

        @Override
        public boolean acceptsURL(String url) throws SQLException {
            return delegate.acceptsURL(url);
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            return delegate.getPropertyInfo(url, info);
        }

        @Override
        public int getMajorVersion() {
            return delegate.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return delegate.getMinorVersion();
        }

        @Override
        public boolean jdbcCompliant() {
            return delegate.jdbcCompliant();
        }

        @Override
        public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return delegate.getParentLogger();
        }
    }
}

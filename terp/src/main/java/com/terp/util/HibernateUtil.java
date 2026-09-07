/*
 * Copyright (C) 2014 ilknur
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
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
package com.terp.util;

import com.terp.data.model.Employee;
import com.terp.data.model.EmployeeGroup;
import com.terp.data.model.MenuSource;
import com.terp.data.model.MenuTranslations;
import com.terp.data.model.PluginSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class HibernateUtil {

    private static final Object LOCK = new Object();
    private static SessionFactory sessionFactory;
    private static ServiceRegistry serviceRegistry;

    private HibernateUtil() {
    }

    /**
     * Build the SessionFactory once, after plugin JARs have been scanned.
     */
    public static void initialize(Collection<Class<?>> extraEntities) {
        initialize(extraEntities, classLoadersOf(extraEntities));
    }

    /**
     * @param extraLoaders plugin {@link ClassLoader}s that defined {@code extraEntities}
     */
    public static void initialize(Collection<Class<?>> extraEntities,
            Collection<? extends ClassLoader> extraLoaders) {
        synchronized (LOCK) {
            if (sessionFactory != null) {
                return;
            }
            sessionFactory = buildSessionFactory(extraEntities, extraLoaders);
        }
    }

    public static boolean isInitialized() {
        return sessionFactory != null;
    }

    private static SessionFactory buildSessionFactory(Collection<Class<?>> extraEntities,
            Collection<? extends ClassLoader> extraLoaders) {
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        ClassLoader pluginAware = composeClassLoader(previous, extraLoaders, extraEntities);
        Thread.currentThread().setContextClassLoader(pluginAware);
        try {
            Configuration configuration = new Configuration();
            Properties props = new Properties();
            props.putAll(TerpProperties.getInstance().getHibernateProps());
            JdbcDriverSupport.install(props);
            migrateJpaPropertyNames(configuration.getProperties());

            configuration
                    .addProperties(props)
                    .addPackage("com.terp.data.model")
                    .addAnnotatedClass(PluginSource.class)
                    .addAnnotatedClass(Employee.class)
                    .addAnnotatedClass(EmployeeGroup.class)
                    .addAnnotatedClass(MenuSource.class)
                    .addAnnotatedClass(MenuTranslations.class);

            Set<Class<?>> host = Set.of(
                    PluginSource.class, Employee.class,
                    EmployeeGroup.class, MenuSource.class, MenuTranslations.class);
            if (extraEntities != null) {
                Set<Class<?>> seen = new LinkedHashSet<>();
                for (Class<?> type : extraEntities) {
                    if (type == null || host.contains(type) || !seen.add(type)) {
                        continue;
                    }
                    configuration.addAnnotatedClass(type);
                }
            }

            migrateJpaPropertyNames(configuration.getProperties());
            Map<String, Object> settings = new LinkedHashMap<>();
            for (String name : configuration.getProperties().stringPropertyNames()) {
                settings.put(name, configuration.getProperties().get(name));
            }
            List<ClassLoader> hibernateLoaders = new ArrayList<>();
            hibernateLoaders.add(pluginAware);
            settings.put("hibernate.classLoaders", hibernateLoaders);
            serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(settings)
                    .build();

            return configuration.buildSessionFactory(serviceRegistry);
        } catch (HibernateException ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new IllegalStateException("Initial SessionFactory creation failed.", ex);
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    private static List<ClassLoader> classLoadersOf(Collection<Class<?>> extraEntities) {
        Set<ClassLoader> loaders = new LinkedHashSet<>();
        if (extraEntities != null) {
            for (Class<?> type : extraEntities) {
                if (type != null && type.getClassLoader() != null) {
                    loaders.add(type.getClassLoader());
                }
            }
        }
        return List.copyOf(loaders);
    }

    /**
     * Hibernate re-resolves entity names via TCCL / AggregatedClassLoader, which
     * does not include plugin {@link java.net.URLClassLoader}s unless we chain them.
     */
    private static ClassLoader composeClassLoader(ClassLoader parent,
            Collection<? extends ClassLoader> extraLoaders,
            Collection<Class<?>> extraEntities) {
        Set<ClassLoader> extras = new LinkedHashSet<>();
        if (extraLoaders != null) {
            for (ClassLoader loader : extraLoaders) {
                if (loader != null && loader != parent) {
                    extras.add(loader);
                }
            }
        }
        extras.addAll(classLoadersOf(extraEntities));
        extras.remove(parent);
        if (extras.isEmpty()) {
            return parent;
        }
        return new PluginAwareClassLoader(parent, extras);
    }

    private static final class PluginAwareClassLoader extends ClassLoader {
        private final ClassLoader[] extras;

        private PluginAwareClassLoader(ClassLoader parent, Collection<ClassLoader> extras) {
            super(parent);
            this.extras = extras.toArray(ClassLoader[]::new);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            for (ClassLoader extra : extras) {
                try {
                    return extra.loadClass(name);
                } catch (ClassNotFoundException ignored) {
                    // try next plugin loader
                }
            }
            throw new ClassNotFoundException(name);
        }
    }

    /**
     * Hibernate 6 still seeds javax.persistence.* aliases; rewrite them so
     * HHH90000021 is not logged for every query.
     */
    private static void migrateJpaPropertyNames(Properties props) {
        List<String> oldKeys = new ArrayList<>();
        for (String name : props.stringPropertyNames()) {
            if (name.startsWith("javax.persistence.")) {
                oldKeys.add(name);
            }
        }
        for (String oldKey : oldKeys) {
            String newKey = "jakarta.persistence." + oldKey.substring("javax.persistence.".length());
            if (!props.containsKey(newKey)) {
                Object value = props.get(oldKey);
                if (value != null) {
                    props.put(newKey, value);
                }
            }
            props.remove(oldKey);
        }
    }

    public static SessionFactory getSessionFactory() {
        SessionFactory factory = sessionFactory;
        if (factory == null) {
            throw new IllegalStateException(
                    "HibernateUtil.initialize() must run after plugin scan and before DAO use");
        }
        return factory;
    }
}

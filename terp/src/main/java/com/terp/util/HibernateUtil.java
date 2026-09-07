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
package com.terp.util;

import com.terp.data.model.Branch;
import com.terp.data.model.Company;
import com.terp.data.model.Employee;
import com.terp.data.model.EmployeeGroup;
import com.terp.data.model.Item;
import com.terp.data.model.MenuSource;
import com.terp.data.model.MenuTranslations;
import com.terp.data.model.PluginSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;



public class HibernateUtil {

    private static final SessionFactory sessionFactory = buildSessionFactory();
    private static ServiceRegistry serviceRegistry;
        
    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            Properties props = new Properties();
            props.putAll(TerpProperties.getInstance().getHibernateProps());
            JdbcDriverSupport.install(props);
            migrateJpaPropertyNames(configuration.getProperties());

            configuration
                    .addProperties(props)
                    .addPackage("com.terp.data.model")
                    .addAnnotatedClass(Company.class)
                    .addAnnotatedClass(Branch.class)
                    .addAnnotatedClass(PluginSource.class)
                    .addAnnotatedClass(Employee.class)
                    .addAnnotatedClass(EmployeeGroup.class)
                    .addAnnotatedClass(MenuSource.class)
                    .addAnnotatedClass(MenuTranslations.class)
                    .addAnnotatedClass(Item.class);

            migrateJpaPropertyNames(configuration.getProperties());
            serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

            return configuration.buildSessionFactory(serviceRegistry);
        }
        catch (HibernateException ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
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
        return sessionFactory;
    }
}

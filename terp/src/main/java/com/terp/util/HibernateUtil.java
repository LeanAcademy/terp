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
import java.util.Properties;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;



public class HibernateUtil {

    private static SessionFactory sessionFactory = buildSessionFactory();
    private static ServiceRegistry serviceRegistry;
        
    private static SessionFactory buildSessionFactory() {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            //Configuration configuration = new Configuration().configure();
            Configuration configuration = new Configuration();
            Properties props = TerpProperties.getInstance().getHibernateProps();
            
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
            
            serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();
            
            /*
            serviceRegistry = new ServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .buildServiceRegistry();
            */
            
            return configuration.buildSessionFactory(serviceRegistry);
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}

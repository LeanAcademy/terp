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

package terp.data;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import terp.util.HibernateUtil;

/**
 * database context to transact with
 * @author ilknur
 */

/**
 * database context to transact with
 * @author ilknur
 * @param <T> table object
 */
public class DbContext<T extends Object> {

    private SessionFactory sessionFactory;
    private Session session = null;
    private Class<T> typeClass = null;
    private T objTable = null;
    
    private void createNewInstace(Class<T> tClass) {
        try {
            this.objTable = typeClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(DbContext.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    public DbContext(){}
    
    public DbContext(Class<T> tClass){
        sessionFactory = HibernateUtil.getSessionFactory();
        session = sessionFactory.openSession();
        this.typeClass = tClass;
        createNewInstace(tClass);
    }
    
    
    public List<T> findAll(){
        
        // begin transaction
        session.getTransaction().begin();
        
        // get all rows
        List<T> list = (List<T>)session.createCriteria(typeClass).list();
        
        // commit
        session.getTransaction().commit();
        
        return list;
            
    }
    
    public List<T> findAll(int startRowNum, int pageRowCount ){
        
        //begin transaction
        session.beginTransaction();
        
        //create criteria
        List<T> list = session.createCriteria(typeClass)
                .list();
        
        return list;
    }
    
    public T firstOrDefault(long key){
        
        // begin transaction
        session.getTransaction().begin();
        
        // find by primary key
        T obj = (T) session.get(typeClass, key);
        
        // commit
        session.getTransaction().commit();
        
        // return result
        return obj;
    }
    
    public T firstOrDefault(String sql){
        
        // begin transaction
        session.getTransaction().begin();
        
        // get named query for find all
        Query qry = session.createQuery(sql);
        
        // find by primary key
        T obj = (T)qry.uniqueResult();
        
        // commit
        session.getTransaction().commit();
        
        // return result
        return obj;        
    }

    public T addOrUpdate(T row){
        
        // begin transaction
        session.getTransaction().begin();
        
        // try to find given row
        T obj = (T) session.merge(row);
        
        // comit
        session.getTransaction().commit();        
        
        return obj;
    }
}

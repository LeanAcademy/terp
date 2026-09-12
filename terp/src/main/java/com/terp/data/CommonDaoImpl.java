/*
 * Copyright (C) 2016 Your Organisation
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
package com.terp.data;

import com.terp.plugin.IUser;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.ICommonFields;
import com.terp.util.HibernateUtil;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.persistence.criteria.CriteriaQuery;
import org.hibernate.query.Query;
import org.hibernate.Session;

/**
 *
 * @author cevdet
 * @param <T>
 */
public class CommonDaoImpl<T> implements ICommonDao<T>{

    private final Class<? extends T> instance;
    
    public CommonDaoImpl(Class<? extends T> instance){
        this.instance = instance; 
    }
    //////////////////////////////////////////////////////////////////////////
    // abstract methods
    //////////////////////////////////////////////////////////////////////////
    
    /**
     * find all records 
     * @return list
     */
    @Override
    public List<T> findAll(){
        
        // get current session
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        // check session
        if(session == null){
            return null;
        }
        
        // begin transaction
        session.getTransaction().begin();
        
        // get all rows
        //List<T> list = session.createCriteria(this.instance).list();
        CriteriaQuery<T> criteriaQuery = session.getCriteriaBuilder().createQuery(resultType());
        criteriaQuery.from(this.instance);
        List<T> list = session.createQuery(criteriaQuery).getResultList();
        
        // commint transaction
        session.getTransaction().commit();
        
        // return list
        return list;        
    }

    
    /**
     * find all records with given sql
     * @param sql
     * @return 
     */
    @Override
    public List<T> findAll(String sql){
        // get current session
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        // check session
        if(session == null){
            return null;
        }
        
        // begin transaction
        session.getTransaction().begin();
        
        // find by primary key
        Query qry = session.createQuery(sql);
        List<T> list = qry.list();
        
        // commint transaction
        session.getTransaction().commit();
        
        // return object
        return list;
    }
    
    /**
     * find records according to given page num and rows per page count 
     * @param pageNum
     * @param rowsPerPage
     * @return 
     */
    @Override
    public List<T> findPage(int pageNum, int rowsPerPage){
        // get current session
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        // check session
        if(session == null){
            return null;
        }
        
        // begin transaction
        session.getTransaction().begin();
        
        // find by page num
        CriteriaQuery<T> criteriaQuery = session.getCriteriaBuilder().createQuery(resultType());
        criteriaQuery.from(this.instance);

        List<T> list = session.createQuery(criteriaQuery)
                .setFirstResult(pageNum-1)
                .setMaxResults(rowsPerPage)
                .getResultList();
        
        // commint transaction
        session.getTransaction().commit();
        
        // return object
        return list;
    }
    
    /**
     * find records according to given page num and rows per page count
     * and given sql. This routine can be used to filter records
     * @param pageNum, number started 0
     * @param rowsPerPage
     * @param sql
     * @return 
     */
    @Override
    public List<T> findPage(int pageNum, int rowsPerPage, String sql) {
        // get current session
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        // check session
        if(session == null){
            return null;
        }
        
        // begin transaction
        session.getTransaction().begin();
        
        // find by page num
        Query query = session.createQuery(sql);
        query.setFirstResult(pageNum-1);
        query.setMaxResults(rowsPerPage);
        List<T> list = query.list();
        
        // commint transaction
        session.getTransaction().commit();
        
        // return object
        return list;
    }
    
    /**
     * find record for given key
     * @param key
     * @return 
     */
    @Override
    public T firstOrDefault(long key) {
        
        // get current session
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        // check session
        if(session == null){
            return null;
        }
        
        // begin transaction
        session.getTransaction().begin();
        
        // find by primary key
        T obj = (T) session.get(this.instance, key);
        
        
        // commint transaction
        session.getTransaction().commit();
        
        // return object
        return obj;
    }
    
    /**
     * find records for given sql
     * @param sql
     * @return 
     */
    @Override
    public T firstOrDefault(String sql) {
        
        // get current session
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        // check session
        if(session == null){
            return null;
        }
        
        // begin transaction
        session.getTransaction().begin();
        Query qry = session.createQuery(sql);
        qry.setMaxResults(1);
        List<T> list = qry.list();
        T obj = list == null || list.isEmpty() ? null : list.get(0);
        session.getTransaction().commit();
        
        // return object
        return obj;
    }

    /**
     * add or update given object and its table
     * @param row
     * @return 
     */
    @Override
    public T addOrUpdate(T row) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        if (session == null) {
            return null;
        }
        try {
            stampAudit(row);
            session.getTransaction().begin();
            T obj = session.merge(row);
            session.getTransaction().commit();
            return obj;
        } catch (RuntimeException ex) {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            throw ex;
        } finally {
            session.close();
        }
    }

    /**
     * delete given row from table
     * @param rowId 
     */
    @Override
    public void delete(long rowId) {
        
        // get current session
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        // check session
        if(session == null){
            return;
        }
        
        // begin transaction
        session.getTransaction().begin();
        
        // find by primary key
        Object obj = session.get(this.instance, rowId);
        if (obj != null) {
            session.remove(obj);
        }
        
        
        // commint transaction
        session.getTransaction().commit();

    }
    
    /**
     * find record count
     * @return 
     */
    @Override
    public long getRecordCount(){
        
        long recordCount = -1;
        
        // get current session
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        // check session
        if(session == null){
            return -1;
        }
        
        // begin transaction
        session.getTransaction().begin();
        
        // find record count
        Query<Long> query = session.createQuery(
                "select count(*) from " + this.instance.getName(), Long.class);
        recordCount = query.uniqueResult();
        
        // commint transaction
        session.getTransaction().commit();
        
        return recordCount;
    }

    @Override
    public long getRecordCount(String fromHql) {
        if (fromHql == null || fromHql.isBlank()) {
            return getRecordCount();
        }
        String hql = fromHql.trim();
        if (!hql.toLowerCase().startsWith("select")) {
            hql = "select count(*) " + hql;
        }
        Session session = HibernateUtil.getSessionFactory().openSession();
        if (session == null) {
            return -1;
        }
        session.getTransaction().begin();
        Query<Long> query = session.createQuery(hql, Long.class);
        Long recordCount = query.uniqueResult();
        session.getTransaction().commit();
        return recordCount == null ? 0 : recordCount;
    }

    /**
     * create empty record
     * @return 
     */
    @Override
    public T getEmpty() {
        try {
            return this.instance.getDeclaredConstructor().newInstance();
        } catch (InstantiationException 
                | IllegalAccessException 
                | NoSuchMethodException 
                | SecurityException 
                | IllegalArgumentException 
                | InvocationTargetException ex) {
            Logger.getLogger(CommonDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @SuppressWarnings("unchecked")
    private Class<T> resultType() {
        return (Class<T>) this.instance;
    }

    private static void stampAudit(Object row) {
        if (row instanceof ICommonFields fields) {
            fields.applyAudit(currentUserId());
        }
    }

    private static Long currentUserId() {
        TerpApplication app = TerpApplication.getInstance();
        IUser user = app == null ? null : app.getUser();
        return user == null ? null : user.getUserId();
    }
}

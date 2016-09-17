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

import com.terp.util.HibernateUtil;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author cevdet
 */
@MappedSuperclass
public abstract class CommonFields implements Serializable {
    
    @Id
    @GeneratedValue(strategy=IDENTITY)
    @Column(name="ref_num", nullable = false, updatable=false)
    private Long rowId;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="deg_tarihi", nullable = true)    
    private Date lastUpdateDate;    
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="ek_tarihi", nullable = true)
    private Date addedDate;
    
    @Column(name="deg_kul_id", nullable = true)
    private Long updatedByUserId;
    
    @Column(name="ekl_kul_id", nullable = true)
    private Long addedByUserId;

    public Long getRowId() {
        return rowId;
    }

    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    public Long getUpdatedByUserId() {
        return updatedByUserId;
    }

    public void setUpdatedByUserId(Long updatedByUserId) {
        this.updatedByUserId = updatedByUserId;
    }

    public Long getAddedByUserId() {
        return addedByUserId;
    }

    public void setAddedByUserId(Long addedByUserId) {
        this.addedByUserId = addedByUserId;
    }
    
    //////////////////////////////////////////////////////////////////////////
    // abstract methods
    //////////////////////////////////////////////////////////////////////////
    
    /**
     * gets all list of declared fields.
     * use this method to get field list to fill up tableview, listview etc.
     * and convert data into meaningfull list
     * @return 
     */
    public List<String> fieldList(){
        return null;
    }
    
    /**
     * gets all list of classes related to fields.
     * use this method to contevert data to java data type
     * @return 
     */
    public List<Class> fieldClasses(){
        return null;
    }
    
    /**
     * find all records in subclass.
     * it can not be used for this clas. 
     * @return 
     */
    public List<Object> findAll(){
        
        // get current session
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        // check session
        if(session == null){
            return null;
        }
        
        // begin transaction
        session.getTransaction().begin();
        
        // get all rows
        List<Object> list = session.createCriteria(this.getClass()
                .asSubclass(CommonFields.class)).list();
        
        // commint transaction
        session.getTransaction().commit();
        
        // return list
        return list;        
    }
    
    public Object firstOrDefault(long key) {
        
        // get current session
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        // check session
        if(session == null){
            return null;
        }
        
        // begin transaction
        session.getTransaction().begin();
        
        // find by primary key
        Object obj = (Object) session.get(this.getClass()
                .asSubclass(CommonFields.class), key);
        
        
        // commint transaction
        session.getTransaction().commit();
        
        // return object
        return obj;
    }
    
    public Object firstOrDefault(String sql) {
        
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
        Object obj = qry.uniqueResult();
        
        // commint transaction
        session.getTransaction().commit();
        
        // return object
        return obj;
    }

    public Object addOrUpdate(Object row) {
        
        // get current session
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        // check session
        if(session == null){
            return null;
        }
        
        // begin transaction
        session.getTransaction().begin();
        
        // update object
        Object obj = (Object) session.merge(this.getClass()
                .asSubclass(CommonFields.class).toString(), row);
        
        
        // commint transaction
        session.getTransaction().commit();
        
        // return object
        return obj;
    }

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
        Object obj = (Object) session.get(this.getClass()
                .asSubclass(CommonFields.class).toString(), rowId);
        
        // delete it
        session.delete(obj);
        
        
        // commint transaction
        session.getTransaction().commit();

    }
    
}

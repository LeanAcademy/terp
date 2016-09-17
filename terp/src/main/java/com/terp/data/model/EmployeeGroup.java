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

package com.terp.data.model;

import com.terp.data.CommonFields;
import com.terp.plugin.IEmployeeGroup;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * employee group entity
 * @author ilknur
 */
@Entity
@Table(name="grup",catalog="terp", uniqueConstraints = {
        @UniqueConstraint(name="pk_grup", columnNames="ref_num"),
        @UniqueConstraint(name="ix_grup", columnNames="grup_adi")
    })
@NamedQueries ({
    @NamedQuery(name="EMPLOYEEGROUP_FIND_ALL", 
            query="from EmployeeGroup eg"),
    @NamedQuery(name="EMPLOYEEGROUP_FIND_BY_PK", 
            query="from EmployeeGroup eg "
            + "where eg.rowid = :id")
})
public  class EmployeeGroup extends CommonFields implements Serializable, IEmployeeGroup {

    private Long rowid;
    private String groupName;
    private int status;
    private Set<Employee> employee = new HashSet<>();
    
    public EmployeeGroup(){
    }
    
    
    @Id
    @Column(name="ref_num", nullable=false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long getRowid() {
        return this.rowid;
    }

    public void setRowid (Long rowid) {
        this.rowid = rowid;
    }
    
    @Column(name="grup_adi",nullable=false,length=50)
    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName (String groupName) {
        this.groupName = groupName;
    }

    @Column(name="durum")
    public int getStatus(){
        return this.status;
    }
    
    public void setStatus(int status){
        this.status = status;
    }
    
    @OneToMany(fetch=FetchType.LAZY, mappedBy="group")
    public Set<Employee> getEmployee(){
        return this.employee;
    }
    
    public void setEmployee(Set<Employee> employee){
        this.employee = employee;
    }

}


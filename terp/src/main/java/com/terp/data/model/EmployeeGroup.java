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
import com.terp.plugin.data.model.IEmployee;
import com.terp.plugin.data.model.IEmployeeGroup;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
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
public  class EmployeeGroup extends CommonFields implements Serializable, IEmployeeGroup {

    @Column(name="grup_adi",nullable=false,length=50)
    private String groupName;
    
    @Column(name="durum")
    private int status;
    
    @OneToMany(fetch=FetchType.LAZY, mappedBy="group")
    private Set<Employee> employee;
    
    public EmployeeGroup(){
    }
    
    @Override
    public String getGroupName() {
        return this.groupName;
    }

    @Override
    public void setGroupName (String groupName) {
        this.groupName = groupName;
    }

    @Override
    public int getStatus(){
        return this.status;
    }
    
    @Override
    public void setStatus(int status){
        this.status = status;
    }    
    
    public Set<Employee> getEmployee(){
        return this.employee;
    }
    
    public void setEmployee (Set<Employee> employee){
        this.employee = employee;
    }

    @Override
    @Transient
    public Set<IEmployee> getEmployeeSet() {
        Set<IEmployee> setIEmployee = new HashSet<>();
        for(Employee emp : this.employee){
            setIEmployee.add((IEmployee)emp);
        }
        return setIEmployee;
    }

    @Override
    @Transient
    public void setEmployeeId(Set<IEmployee> employeeSet) {
        
        for(IEmployee item : employeeSet){
            Employee emp = new Employee();
            
            emp.setRowid(item.getRowid());
            emp.setUserName(item.getUserName());
            emp.setName(item.getName());            
            emp.setGroup(item.getGroup());            
            emp.setPassword(item.getPassword());
            emp.setStatus(item.getStatus());
            emp.setType(item.getType());
            emp.setAddedByUserId(item.getAddedByUserId());
            emp.setUpdatedByUserId(item.getUpdatedByUserId());
            emp.setAddedDate(item.getAddedDate());
            emp.setLastUpdateDate(item.getLastUpdateDate());
            
            this.employee.remove(emp);
            this.employee.add(emp);
        }
    }
}


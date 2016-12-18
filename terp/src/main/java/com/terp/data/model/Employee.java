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
import com.terp.plugin.IEmployee;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="kullanici", catalog="terp", 
        uniqueConstraints = {
            @UniqueConstraint(name="ix_kullanici", columnNames="kullanici_adi")
        }
)
public  class Employee extends CommonFields implements Serializable, IEmployee {

    @Id
    @GeneratedValue(strategy=IDENTITY)
    @Column(name="ref_num", nullable = false)
    private Long rowid;
    
    @Column(name = "kullanici_adi", nullable = false, length = 50)
    private String userName;
    
    @Column(name = "sifre", nullable = false, length = 128)
    private String password;
    
    @Column(name = "isim", nullable = false, length = 20)
    private String name;

    @Column(name = "tip", nullable = false)
    private int type;    

    @Column(name="durum", nullable = false)
    private int status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name="grup_ref",
            nullable=false,
            referencedColumnName="ref_num",
            foreignKey = @ForeignKey(name="fk_kul_grup"))
    private EmployeeGroup group;

    public Employee(){
    }

    public Long getRowid() {
        return this.rowid;
    }

    public void setRowid (Long rowid) {
        this.rowid = rowid;
    }
        
    public String getUserName() {
        return this.userName;
    }

    public void setUserName (String userName) {
        this.userName = userName;
    }
    
    
    public String getPassword() {
        return this.password;
    }

    public void setPassword (String password) {
        this.password = password;
    }
    
    public String getName() {
        return this.name;
    }

    public void setName (String name) {
        this.name = name;
    }
    
    public int getType() {
        return this.type;
    }

    public void setType (int type) {
        this.type = type;
    }
    
    public int getStatus() {
        return this.status;
    }

    public void setStatus (int status) {
        this.status = status;
    }
    
    public EmployeeGroup getGroup() {
        return this.group;
    }

    public void setGroup (EmployeeGroup employeeGroup) {
        this.group = employeeGroup;
    }

}


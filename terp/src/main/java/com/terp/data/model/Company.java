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
import com.terp.plugin.ICompany;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author ilknur
 */

@Entity
@Table(name="firma", catalog="terp",
        uniqueConstraints = {
            @UniqueConstraint(name="ix_firma", columnNames="firma_adi")
        }
)
public class Company extends CommonFields implements Serializable, ICompany {
    
    private long rowid;
    private String companyName;
    private String companyLongName;
    private int status;

    @Id
    @GeneratedValue(strategy=IDENTITY)
    @Column(name="ref_num", nullable = false)
    public long getRowid() {
        return rowid;
    }

    public void setRowid(long rowid) {
        this.rowid = rowid;
    }
    
    @Column(name="firma_adi", nullable = false, length=50)
    public String getCompanyName() {
        return this.companyName;
    }
    
    public void setCompanyName(String companyName){
        this.companyName = companyName;
    }
    
    @Column(name="firma_unv", nullable = false, length=128 )
    public String getCompanyLongName() {
        return this.companyLongName;
    }
    
    public void setCompanyLongName(String companyLongName){
        this.companyLongName = companyLongName;
    }
    
    @Column(name="durum", nullable = false )
    public int getStatus() {
        return this.status;
    }
    
    public void setStatus(int status){
        this.status = status;
    }
    
}

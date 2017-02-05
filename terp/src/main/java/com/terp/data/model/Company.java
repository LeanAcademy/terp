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
import com.terp.plugin.data.model.ICompany;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author ilknur
 */

@Entity
@Table(name="firma", catalog="terp",
        uniqueConstraints = {
            @UniqueConstraint(name="ix_firma", columnNames="firma_unv")
        }
)
public class Company extends CommonFields implements Serializable, ICompany {

    @Column(name="firma_unv", nullable = false, length=50)
    private String companyName;
    
    @Column(name="firma_adi", nullable = false, length=128 )
    private String companyLongName;
    
    @Column(name="aciklama", nullable = true, length=256 )
    private String notes;
    
    @Column(name="durum", nullable = false )
    private int status;
    
    @Column(name="vergi_no", nullable = false, length=20 )
    private String stateTaxCode;

    @Column(name="vergi_dairesi", nullable = false, length=20 )
    private String stateTaxRegion;    
    
    @Override
    public String getCompanyName() {
        return this.companyName;
    }
    
    @Override
    public void setCompanyName(String companyName){
        this.companyName = companyName;
    }
    
    @Override
    public String getCompanyLongName() {
        return this.companyLongName;
    }
    
    @Override
    public void setCompanyLongName(String companyLongName){
        this.companyLongName = companyLongName;
    }    
    
    @Override
    public int getStatus() {
        return this.status;
    }
    
    @Override
    public void setStatus(int status){
        this.status = status;
    }

    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String getStateTaxCode() {
        return stateTaxCode;
    }

    @Override
    public void setStateTaxCode(String stateTaxCode) {
        this.stateTaxCode = stateTaxCode;
    }

    @Override
    public String getStateTaxRegion() {
        return stateTaxRegion;
    }

    @Override
    public void setStateTaxRegion(String stateTaxRegion) {
        this.stateTaxRegion = stateTaxRegion;
    }
    
}

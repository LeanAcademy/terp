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

    /* address */
    @Column(name="merkez_adresi", nullable = false, length=256 )
    private String address; 
    
    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public void setAddress(String address) {
        this.address = address;
    }

    /* country */
    @Column(name="merkez_ulke", nullable = false, length=128 )
    private String country;
    
    @Override
    public String getCountry() {
        return country;
    }

    @Override
    public void setCountry(String country) {
        this.country = country;
    }

    /* region */
    
    @Column(name="merkez_sehir", nullable = false, length=128 )
    private String region;
    
    @Override
    public String getRegion() {
        return region;
    }    
    
    @Override
    public void setRegion(String region) {
        this.region = region;
    }
    
    /* city */
    @Column(name="merkez_ilce", nullable = false, length=128 )
    private String city;
    
    @Override
    public String getCity() {
        return city;
    }

    @Override
    public void setCity(String city) {
        this.city = city;
    }
    
    /* company name */
    @Column(name="firma_unv", nullable = false, length=50)
    private String companyName;
    
    @Override
    public String getCompanyName() {
        return this.companyName;
    }
    
    @Override
    public void setCompanyName(String companyName){
        this.companyName = companyName;
    }
    
    /* company long name */
    @Column(name="firma_adi", nullable = false, length=128 )
    private String companyLongName;
    
    @Override
    public String getCompanyLongName() {
        return this.companyLongName;
    }
    
    @Override
    public void setCompanyLongName(String companyLongName){
        this.companyLongName = companyLongName;
    }    
    
    /* status */
    @Column(name="durum", nullable = false )
    private int status;
    
    @Override
    public int getStatus() {
        return this.status;
    }
    
    @Override
    public void setStatus(int status){
        this.status = status;
    }

    /* notes */
    @Column(name="aciklama", nullable = true, length=256 )
    private String notes;
    
    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /* tax code */
    @Column(name="vergi_no", nullable = false, length=20 )
    private String stateTaxCode;
    
    @Override
    public String getStateTaxCode() {
        return stateTaxCode;
    }

    @Override
    public void setStateTaxCode(String stateTaxCode) {
        this.stateTaxCode = stateTaxCode;
    }

    /* tax region */
    @Column(name="vergi_dairesi", nullable = false, length=20 )
    private String stateTaxRegion;
    
    @Override
    public String getStateTaxRegion() {
        return stateTaxRegion;
    }
    
    @Override
    public void setStateTaxRegion(String stateTaxRegion) {
        this.stateTaxRegion = stateTaxRegion;
    }

    /* phone */
    @Column(name="telefon", nullable = true, length=15 )
    private String phone;
            
    @Override
    public String getPhone() {
        return this.phone;
    }

    @Override
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /* fax */
    @Column(name="faks", nullable = true, length=15 )
    private String fax;
    
    @Override
    public String getFax() {
        return this.fax;
    }

    @Override
    public void setFax(String fax) {
        this.fax = fax;
    }

    /* email */
    @Column(name="email", nullable = false, length=126 )
    private String email;
    
    @Override
    public String getEmail() {
        return this.email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }
    
}

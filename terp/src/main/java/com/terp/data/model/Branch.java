/*
 * Copyright (C) 2014 ilknur
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.terp.data.model;

import com.terp.data.CommonFields;
import com.terp.plugin.data.model.IBranch;
import com.terp.plugin.data.model.ICompany;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author ilknur
 */
@Entity
@Table(name = "sube", catalog = "terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_sube", 
                    columnNames = {"firma_ref", "sube_adi"})
        }
)
public class Branch extends CommonFields implements Serializable, IBranch{
        
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "firma_ref",
            nullable = false,
            referencedColumnName = "ref_num",
            foreignKey = @ForeignKey(name = "fk_sube_firma")
    )
    private Company company;
    
    @Column(name = "sube_adi", nullable = false, length = 50)
    private String branchName;
    
    @Column(name = "sube_aciklama", nullable = false, length = 50)
    private String branchLongName;
    
    /**
     * branchType 1=headoffice, 0=normal branch
     */
    @Column(name = "sube_tipi", nullable = false)
    private int branchType;
    
    @Column(name = "durum", nullable = false)
    private int status;

    @Column(name = "adres1", nullable = false, length = 50)
    private String address1;
    
    @Column(name = "adres2", nullable = false, length = 50)
    private String address2;
    
    @Column(name = "adres3", nullable = false, length = 50)
    private String address3;
    
    @Column(name = "ilce", nullable = false, length = 50)
    private String city;
    
    @Column(name = "sehir", nullable = false, length = 50)
    private String state;
    
    @Column(name = "posta_kodu", nullable = false, length = 10)
    private String zip;
    
    @Column(name = "ulke", nullable = false, length = 20)
    private String country;
    
    @Column(name = "sube_tel_no1", nullable = false, length = 20)
    private String phoneNum1;
    
    @Column(name = "sube_tel_no2", nullable = false, length = 20)
    private String phoneNum2;
    
    @Column(name = "sube_bg_no", nullable = false, length = 20)
    private String faxNum;
    
    @Column(name = "sube_email1", nullable = false, length = 100)
    private String email1;
    
    @Column(name = "sube_email2", nullable = false, length = 100)
    private String email2;
    
    @Override
    public ICompany getCompany(){
        return this.company;
    }
    
    @Override
    public void setCompany(ICompany company){
        this.company = (Company) company;
    }    
        
    @Override
    public String getBranchName() {
        return this.branchName;
    }

    @Override
    public void setBranchName(String  branchName) {
        this.branchName = branchName;
    }    
    
    @Override
    public String getBranchLongName() {
        return this.branchLongName;
    }

    @Override
    public void setBranchLongName(String  branchLongName) {
        this.branchLongName = branchLongName;
    }    
    
    @Override
    public long getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int getBranchType() {
        return branchType;
    }

    @Override
    public void setBranchType(int branchType) {
        this.branchType = branchType;
    }

    @Override
    public String getAddress1() {
        return address1;
    }

    @Override
    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    @Override
    public String getAddress2() {
        return address2;
    }

    @Override
    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    @Override
    public String getAddress3() {
        return address3;
    }

    @Override
    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    @Override
    public String getCity() {
        return city;
    }

    @Override
    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String getZip() {
        return zip;
    }

    @Override
    public void setZip(String zip) {
        this.zip = zip;
    }

    @Override
    public String getCountry() {
        return country;
    }

    @Override
    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String getPhoneNum1() {
        return phoneNum1;
    }

    @Override
    public void setPhoneNum1(String phoneNum1) {
        this.phoneNum1 = phoneNum1;
    }

    @Override
    public String getPhoneNum2() {
        return phoneNum2;
    }

    @Override
    public void setPhoneNum2(String phoneNum2) {
        this.phoneNum2 = phoneNum2;
    }

    @Override
    public String getFaxNum() {
        return faxNum;
    }

    @Override
    public void setFaxNum(String faxNum) {
        this.faxNum = faxNum;
    }

    @Override
    public String getEmail1() {
        return email1;
    }

    @Override
    public void setEmail1(String email1) {
        this.email1 = email1;
    }

    @Override
    public String getEmail2() {
        return email2;
    }

    @Override
    public void setEmail2(String email2) {
        this.email2 = email2;
    }

}

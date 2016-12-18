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
import com.terp.plugin.IBranch;
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
    
    public Company getCompany(){
        return this.company;
    }
    
    public void setCompany(Company company){
        this.company = company;
    }    
        
    public String getBranchName() {
        return this.branchName;
    }

    public void setBranchName(String  branchName) {
        this.branchName = branchName;
    }    
    
    public String getBranchLongName() {
        return this.branchLongName;
    }

    public void setBranchLongName(String  branchLongName) {
        this.branchLongName = branchLongName;
    }    
    
    public long getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getBranchType() {
        return branchType;
    }

    public void setBranchType(int branchType) {
        this.branchType = branchType;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhoneNum1() {
        return phoneNum1;
    }

    public void setPhoneNum1(String phoneNum1) {
        this.phoneNum1 = phoneNum1;
    }

    public String getPhoneNum2() {
        return phoneNum2;
    }

    public void setPhoneNum2(String phoneNum2) {
        this.phoneNum2 = phoneNum2;
    }

    public String getFaxNum() {
        return faxNum;
    }

    public void setFaxNum(String faxNum) {
        this.faxNum = faxNum;
    }

    public String getEmail1() {
        return email1;
    }

    public void setEmail1(String email1) {
        this.email1 = email1;
    }

    public String getEmail2() {
        return email2;
    }

    public void setEmail2(String email2) {
        this.email2 = email2;
    }

}

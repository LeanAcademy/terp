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

/**
 *
 * @author ilknur
 */
@Entity
@Table(name = "tedarikci", catalog = "terp", 
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_tedarikci", 
                    columnNames = {"firma_ref", "sube_ref", "td_kodu"}
            )
        }
)
public class Vendor implements Serializable{
    
    private long rowid;
    private Company company;
    private Branch branch;
    private String venId;
    private String venName;
    private String venCountry;
    private String venCity;
    private String venAddress1;
    private String venAddress2;
    private String venPostalCode;
    private String taxId;
    private String taxOffice;
    private double riskLevel;
    private int venType;
    private String tel;    
    private String Fax;
    private String email;
    private int paymentTerm;
    private int status;
    
    /**
     * entity id
     * @return 
     */
    @Id
    @GeneratedValue(strategy=IDENTITY)    
    @Column(name = "ref_num")
    public long getRowid(){
        return this.rowid;
    }
    
    public void setRowid(long rowid) {
        this.rowid = rowid;
    }
    
    /**
     * company field
     * @return 
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "firma_ref", 
            nullable = false,
            referencedColumnName = "ref_num",
            foreignKey = @ForeignKey(name = "fk_tedarikci_firma")
    )
    public Company getCompany(){
        return this.company;
    }
    
    public void setCompany(Company company){
        this.company = company;
    }
    
    /**
     * branch field
     * @return 
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "sube_ref", 
            nullable = false,
            referencedColumnName = "ref_num",
            foreignKey = @ForeignKey(name = "fk_tedarikci_sube")
    )
    public Branch getBranch(){
        return this.branch;
    }
    
    public void setBranch(Branch branch){
        this.branch = branch;
    }
    
    /**
     * vendo id field
     * @return 
     */
    @Column(name = "td_kodu", nullable = false, length = 50)
    public String getVenId(){
        return this.venId;
    }
    
    public void setVenId(String venId){
        this.venId = venId;
    }
    
    /**
     * vendor name field
     * @return 
     */
    @Column(name = "td_adi", nullable = false, length = 128)
    public String getVenName(){
        return this.venName;
    }
    
    public void setVeName(String venName){
        this.venName = venName;
    }
    
    @Column(name = "td_ulke", nullable = false, length = 5)
    public String getVenCountry(){
        return this.venCountry;
    }
    
    public void setVenCountry(String venCountry){
        this.venCountry = venCountry;
    }
    
    // TODO : Contineue to define other fields
}

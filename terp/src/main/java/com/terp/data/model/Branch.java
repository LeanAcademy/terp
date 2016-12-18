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
    
    @Column(name = "durum", nullable = false)
    private int status;    
    
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

}

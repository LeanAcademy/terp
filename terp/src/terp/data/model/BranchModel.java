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

package terp.data.model;

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
@Table(name = "sube", catalog = "terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_sube", 
                    columnNames = {"firma_ref", "sube_adi"})
        }
)
public class BranchModel implements Serializable{
    
    private long rowid;
    private CompanyModel company;
    private String branchName;
    private String branchLongName;
    private int status;

    @Id
    @GeneratedValue(strategy=IDENTITY)
    @Column(name = "ref_num", nullable = false)
    public long getRowid() {
        return this.rowid;
    }

    public void setRowid(long rowid) {
        this.rowid = rowid;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "firma_ref", 
            nullable = false,
            referencedColumnName = "ref_num",
            foreignKey = @ForeignKey(name = "fk_sube_firma")
    )
    public CompanyModel getCompany(){
        return this.company;
    }
    
    public void setCompany(CompanyModel company){
        this.company = company;
    }
    
    @Column(name = "sube_adi", nullable = false, length = 50)
    public String getBranchName() {
        return this.branchName;
    }

    public void setBranchName(String  branchName) {
        this.branchName = branchName;
    }
    
    @Column(name = "sube_aciklama", nullable = false, length = 50)
    public String getBranchLongName() {
        return this.branchLongName;
    }

    public void setBranchLongName(String  branchLongName) {
        this.branchLongName = branchLongName;
    }
    
    @Column(name = "durum", nullable = false)
    public long getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author ilknur
 */
@Entity
@Table(name = "malzeme", catalog="terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_malzeme", columnNames="mlz_kodu")
        }
)
public class Item extends CommonFields{
    
    @Column(name="mlz_kodu", nullable=false)
    private String itemId;
    @Column(name="mlz_tanimi", nullable=false)
    private String itemDesc;
    @Column(name="mlz_birimi", nullable=false)
    private String itemUnit;    
    
    public Item(){}
    
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }

    public String getItemUnit() {
        return itemUnit;
    }

    public void setItemUnit(String itemUnit) {
        this.itemUnit = itemUnit;
    }
}

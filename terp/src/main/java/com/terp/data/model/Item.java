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
@Table(name = "malzeme", catalog="terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_malzeme", columnNames="mlz_kodu")
        }
)
public class Item implements Serializable {
   
    private long rowid;
    private String itemId;
    private String itemDesc;
    private String itemUnit;

    @Id
    @GeneratedValue(strategy=IDENTITY)    
    @Column(name = "ref_num")
    public long getRowid(){
        return this.rowid;
    }
    
    public void setRowid(long rowid) {
        this.rowid = rowid;
    }
}

/*
 * Copyright (C) 2016 Your Organisation
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
package com.terp.data;

import com.terp.plugin.data.ICommonFields;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author cevdet
 * @param <T>
 */
@MappedSuperclass
public abstract class CommonFields implements Serializable, ICommonFields {
    
    @Id
    @GeneratedValue(strategy=IDENTITY)
    @Column(name="ref_num", nullable = false, updatable=false)
    private Long rowId;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="deg_tarihi", nullable = true)    
    private Date lastUpdateDate;    
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="ek_tarihi", nullable = true)
    private Date addedDate;
    
    @Column(name="deg_kul_id", nullable = true)
    private Long updatedByUserId;
    
    @Column(name="ekl_kul_id", nullable = true)
    private Long addedByUserId;

    @Override
    public Long getRowId() {
        return rowId;
    }

    @Override
    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    @Override
    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    @Override
    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public Date getAddedDate() {
        return addedDate;
    }

    @Override
    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    @Override
    public Long getUpdatedByUserId() {
        return updatedByUserId;
    }

    @Override
    public void setUpdatedByUserId(Long updatedByUserId) {
        this.updatedByUserId = updatedByUserId;
    }

    @Override
    public Long getAddedByUserId() {
        return addedByUserId;
    }

    @Override
    public void setAddedByUserId(Long addedByUserId) {
        this.addedByUserId = addedByUserId;
    }
    
}

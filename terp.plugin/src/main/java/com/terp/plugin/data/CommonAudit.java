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
package com.terp.plugin.data;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Audit columns shared by every persistent row ({@code ek_tarihi}, {@code deg_tarihi},
 * {@code ekl_kul_id}, {@code deg_kul_id}). Entities with their own primary key
 * extend this class; typical TERP rows use {@link CommonFields} which also
 * provides {@code ref_num}.
 */
@MappedSuperclass
public abstract class CommonAudit implements Serializable, ICommonFields {

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "deg_tarihi", nullable = true)
    private Date lastUpdateDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ek_tarihi", nullable = true)
    private Date addedDate;

    @Column(name = "deg_kul_id", nullable = true)
    private Long updatedByUserId;

    @Column(name = "ekl_kul_id", nullable = true)
    private Long addedByUserId;

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

/*
 * Copyright (C) 2017 Lean Danışmanlık
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.terp.data.model;

import com.terp.plugin.data.CommonAudit;
import com.terp.plugin.data.model.ICities;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 *
 * @author cevdet
 */
@Entity
@Table(name = "ilceler", catalog = "terp", schema = "terp")
public class Cities extends CommonAudit implements ICities {

    @Id
    private Long id;

    @Column(length = 2)
    private String country;

    @Column(length = 2)
    private String region;

    @Column(length = 150)
    private String name;

    private Double latitude;

    private Double longitude;

    @Override
    public Long getRowId() {
        return id;
    }

    @Override
    public void setRowId(Long rowId) {
        this.id = rowId;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
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
    public String getRegion() {
        return country;
    }

    @Override
    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Double getLatitude() {
        return latitude;
    }

    @Override
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @Override
    public Double getLongitude() {
        return longitude;
    }

    @Override
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}

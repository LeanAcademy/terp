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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.terp.plugin.data.model;

/**
 *
 * @author cevdet
 */
public interface IRegions {
    public Long getId();
    public void setId(Long id);
    public String getCountry();
    public void setCountry(String country);
    public String getCode();
    public void setCode(String code);
    public String getName();
    public void setName(String name);
    public Double getLatitude();
    public void setLatitude(Double latitude);
    public Double getLongitude();
    public void setLongitude(Double longitude);
    public int getCities();
    public void setCities(int cities);
}

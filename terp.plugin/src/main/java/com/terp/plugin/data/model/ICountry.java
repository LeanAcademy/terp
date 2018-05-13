/*
 * Copyright (C) 2017 cevdet
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
public interface ICountry {
    public Long getId();
    public void setId(Long id);
    public String getIso();
    public void setIso(String iso);
    public String getName();
    public void setName(String name);
    public String getNiceName();
    public void setNiceName(String niceName);
    public String getIso3();
    public void setIso3(String iso3);
    public String getNumCode();
    public void setNumCode(String numCode);
    public String getPhoneCode();
    public void setPhoneCode(String phoneCode);
}

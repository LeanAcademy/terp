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
package com.terp.data.model;

import java.io.Serializable;
import com.terp.plugin.data.model.ICountry;

/**
 *
 * @author cevdet
 */
public class Country implements Serializable, ICountry {

    private Long id;
    
    private String iso;
    
    private String name;

    private String niceName;

    private String iso3;
    
    private String numCode;
    
    private String phoneCode;
    
    @Override
    public Long getId() {
        return id;
    }
    
    @Override
    public void setId(Long id) {
        this.id = id;
    }
    
    @Override
    public String getIso() {
        return iso;
    }
    
    @Override
    public void setIso(String iso) {
        this.iso = iso;
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
    public String getNiceName() {
        return niceName;
    }

    @Override
    public void setNiceName(String niceName) {
        this.niceName = niceName;
    }

    @Override
    public String getIso3() {
        return iso3;
    }

    @Override
    public void setIso3(String iso3) {
        this.iso3 = iso3;
    }

    @Override
    public String getNumCode() {
        return numCode;
    }

    @Override
    public void setNumCode(String numCode) {
        this.numCode = numCode;
    }

    @Override
    public String getPhoneCode() {
        return phoneCode;
    }

    @Override
    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }
    
}

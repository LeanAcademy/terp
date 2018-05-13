/*
 * Copyright (C) 2017 Cevdet Dal
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

import com.terp.plugin.data.ICommonWebService;

/**
 *
 * @author Cevdet Dal
 * @param <T>
 */
public class CommonWebServiceImpl<T> implements ICommonWebService<T> {

    private final Class<T> instance;
    
    private String errorText;

    public CommonWebServiceImpl(Class<T> instance) {
        this.instance = instance;
        this.errorText = "";
    }

    @Override
    public String getError() {
        return this.errorText;
    }

    @Override
    public Boolean isFailed() {
        return (this.errorText == "");
    }
    
    public void setError(String err){
        this.errorText = err;
    }
    
}

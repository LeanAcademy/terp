/*
 * Copyright (C) 2017 Your Organisation
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
package com.terp.data.dao;

import com.terp.data.CommonWebServiceImpl;
import com.terp.plugin.data.dao.ICountriesDao;
import java.util.ArrayList;
import java.util.List;
import com.terp.plugin.data.model.ICountry;

/**
 *
 * @author cevdet
 */
public class CountriesDaoImpl extends CommonWebServiceImpl<ICountry> implements ICountriesDao {

    private static final String WS_URI = "http://www.webservicex.net/country.asmx?WSDL";
    private static final String WS_NAME_SPACE = "http://www.webservicex.net/country.asmx";
    private static final String WS_SERVICE_NAME = "GetCountries";
 
    public CountriesDaoImpl() {
        super(ICountry.class);
    }

    @Override
    public List<ICountry> findAll() {
        List<ICountry> list = new ArrayList();
                
        
        return list;
    }

    @Override
    public List<ICountry> find(String sql) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

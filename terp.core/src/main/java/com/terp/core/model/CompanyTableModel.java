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
package com.terp.core.model;

import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.dao.ICitiesDao;
import com.terp.plugin.data.dao.ICompanyDao;
import com.terp.plugin.data.dao.ICountriesDao;
import com.terp.plugin.data.dao.IRegionsDao;
import com.terp.plugin.data.model.ICities;
import com.terp.plugin.data.model.IRegions;
import java.util.Iterator;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.terp.plugin.data.model.ICountry;

/**
 *
 * @author Cevdet Dal
 */
public class CompanyTableModel {
    
    private final ICompanyDao companyDao;
    
    private final  ICountriesDao countriesDao;
    
    private IRegionsDao regionsDao;
    
    private ICitiesDao citiesDao;
    
    public CompanyTableModel(){
        
        this.companyDao = TerpApplication.getInstance()
                .getDatabaseFactory().getCompanyDao();
        this.countriesDao = TerpApplication.getInstance()
                .getDatabaseFactory().getCountriesDao();
    }
    
    public ObservableList<String> getCountries(){
        
        ObservableList list = FXCollections.observableArrayList();
        
        List<ICountry> countries = countriesDao.findAll();
        
        for(Iterator iterator = countries.iterator(); iterator.hasNext();){
            ICountry country = (ICountry)iterator.next();
            list.add(country.getName());
        }
        
        
        return list;
    }
    
    public ObservableList<String> getRegions(String sql){
        ObservableList list = FXCollections.observableArrayList();
        
        List<IRegions> regions = regionsDao.findAll(sql);
        
        for(Iterator iterator = regions.iterator(); iterator.hasNext();){
            IRegions region = (IRegions)iterator.next();
            list.add(region.getName());
        }
        
        return list;
    }
    
    public ObservableList<String> getCities(String sql){
        ObservableList list = FXCollections.observableArrayList();
        
        List<ICities> cities = citiesDao.findAll(sql);
        
        for(Iterator iterator = cities.iterator(); iterator.hasNext();){
            ICities city = (ICities)iterator.next();
            list.add(city.getName());
        }
        
        return list;
    }
}

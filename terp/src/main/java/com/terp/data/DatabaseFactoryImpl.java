/*
 * Copyright (C) 2016 Lean Danışmanlık Cevdet Dal
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

import com.terp.data.dao.BranchDaoImpl;
import com.terp.data.dao.CitiesDaoImpl;
import com.terp.data.dao.CompanyDaoImpl;
import com.terp.data.dao.CountriesDaoImpl;
import com.terp.data.dao.RegionsDaoImpl;
import com.terp.plugin.data.IDatabaseFactory;
import com.terp.plugin.data.dao.IBranchDao;
import com.terp.plugin.data.dao.ICitiesDao;
import com.terp.plugin.data.dao.ICompanyDao;
import com.terp.plugin.data.dao.ICountriesDao;
import com.terp.plugin.data.dao.IRegionsDao;

/**
 *
 * @author cevdet
 */
public class DatabaseFactoryImpl implements IDatabaseFactory {

    @Override
    public ICompanyDao getCompanyDao() {
        return new CompanyDaoImpl();
    }

    @Override
    public IBranchDao getBranchDao() {
        return new BranchDaoImpl();
    }

    @Override
    public ICountriesDao getCountriesDao() {
        return new CountriesDaoImpl();
    }

    @Override
    public IRegionsDao getRegionsDao() {
        return new RegionsDaoImpl();
    }

    @Override
    public ICitiesDao getCitiesDao() {
        return new CitiesDaoImpl();
    }
    
}

/*
 * Copyright (C) 2016 cevdet
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
package com.terp.plugin.data.dao;

import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.model.ICompany;

/**
 * this interface reprasent company table in the TERP system.
 * CRUD operations can be done with this interface
 * additionally find record count and create empty class as well
 * to use this class you have to get it from TerpApplication factory calss
 * use following code
 * TerpApplication app = TerpApplication.getInstance();
 * ICompanyDao dao = app.getDatabaseFactory().getCompanyDao();
 * 
 * then you can use all CRUD applicaitons
 * @author cevdet
 */
public interface ICompanyDao extends ICommonDao<ICompany> {
    
}

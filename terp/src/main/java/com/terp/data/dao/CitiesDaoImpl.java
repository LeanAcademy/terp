/*
 * Copyright (C) 2017 Lean Danışmanlık www.leanacademy.com.tr
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

import com.terp.data.CommonDaoImpl;
import com.terp.plugin.data.dao.ICitiesDao;
import com.terp.plugin.data.model.ICities;

/**
 *
 * @author Cevdet Dal
 */
public class CitiesDaoImpl extends CommonDaoImpl<ICities> implements ICitiesDao{
    
    public CitiesDaoImpl() {
        super(ICities.class);
    }
}

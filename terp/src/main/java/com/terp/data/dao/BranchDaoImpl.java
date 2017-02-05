/*
 * Copyright (C) 2016 Your Organisation
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
import com.terp.plugin.data.dao.IBranchDao;
import com.terp.plugin.data.model.IBranch;

/**
 *
 * @author cevdet
 */
public class BranchDaoImpl extends CommonDaoImpl<IBranch> implements IBranchDao {
    public BranchDaoImpl(){
        super(IBranch.class);
    }
}

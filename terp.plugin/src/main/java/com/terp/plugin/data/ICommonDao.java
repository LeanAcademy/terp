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
package com.terp.plugin.data;

import java.util.List;

/**
 * this is interface for common CRUD operations for tables. 
 * It is paren interface and cannot be used alone. 
 * 
 * @author cevdet
 * @param <T> : it model class
 */
public interface ICommonDao<T> {
    public List<T> findAll();
    public List<T> findAll(String sql);
    public List<T> findPage(int pageNum, int rowsPerPage);
    public List<T> findPage(int pageNum, int rowsPerPage, String sql);
    public Object firstOrDefault(long key);
    public Object firstOrDefault(String sql);
    public Object addOrUpdate(T row);
    public void delete(long rowId);
    public long getRecordCount();
    public T getEmpty();
}

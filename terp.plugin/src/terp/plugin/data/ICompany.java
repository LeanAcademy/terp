/*
 * Copyright (C) 2015 Lean Academy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */package terp.plugin.data;

import java.util.List;

/**
 *
 * @author cevdet
 */


public interface ICompany {
    /**
     * Gets fields names of Company table as String - List object
     * @return 
     */
    public List<String> getColumns();
    
    /**
     * Gets all company object exsisting in Company table
     * @return 
     */
    public List<Company> findAll();
    public List<Company> findAll(int pageNum, int rowNum);
    
    /**
     * Excutes a sql statement for company table and rerturn first or default 
     * row as result.
     * @param sql
     * @return 
     */
    public Company firstOrDefault(String sql);
    public void addOrUpdate(Company company);
    public void delete(long rowId);
}

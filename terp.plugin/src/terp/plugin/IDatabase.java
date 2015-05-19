/*
 * Copyright (C) 2014 ilknur
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

/*
 * created by cevdet <dal.cevdet@gmail.com>
 */
package terp.plugin;

import java.util.Map;

/**
 *
 * @author ilknur
 */

public interface IDatabase {
    
    /**
     * find all record
     * @return d
     */
    public Map<String, Object> findAll();
    
    /**
     * Find all records according to row count and page number. This is useful
     * for navigation
     * 
     * @param rowCount
     * @param pageNum
     * @return 
     */
    public Map<String, Object> findAll(int rowCount, int pageNum);
    
    /**
     * Find record for given row id.
     * 
     * @param id
     * @return 
     */
    public Map<String, Object> find(long id);
   
    /**
     * add new row or updata existing one in database
     * @param newRow 
     */
    public void addOrUpdate(Map<String, Object> newRow);
}

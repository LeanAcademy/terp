/*
 * Copyright (C) 2014 Lean Danışmanlık
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

package terp.plugin.data;

/**
 *
 * @author cevdet
 */


public interface IEmployee {
    public Object[] findAll();
    public Object[] findAll(int pageNum, int rowNum);
    public Object firstOrDefault(String sql);
    public void addOrUpdate(Object company);
    public void delete(long rowId);
}

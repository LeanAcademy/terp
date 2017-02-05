/*
 * Copyright (C) 2015 lean danışmanlık
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
 */
package com.terp.plugin.data.model;

import com.terp.plugin.data.ICommonFields;
import java.util.Set;

/**
 *
 * @author cevdet
 */
public interface IEmployeeGroup extends ICommonFields {
    public String getGroupName();
    public void setGroupName (String groupName);
    public int getStatus();    
    public void setStatus(int status);    
    public Set<IEmployee> getEmployeeSet();    
    public void setEmployeeId(Set<IEmployee> employeeSet);
}

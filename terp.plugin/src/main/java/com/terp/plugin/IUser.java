/*
 * Copyright (C) 2014 ilknur
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
package com.terp.plugin;

import java.util.List;

/**
 *
 * @author ilknur
 */


public interface IUser {
    
    /**
     * gets user name
     * @return 
     */
    public String getUserName();
    
    //TODO add other fields
    
    /**
     * gets user group name
     * @return 
     */
    public long getUserGroupId();
    
    /**
     * gets if user is already authenticated
     * @return 
     */
    public boolean isAuthenticated();
    
    /**
     * gets if user is in admin group
     * @return 
     */
    public boolean isAdministrator();
    
    /**
     * get if user uthorized for given object
     * @param menuId
     * @return 
     */
    public boolean isAuthorized(String menuId);
    
}

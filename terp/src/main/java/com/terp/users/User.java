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

package com.terp.users;

import com.terp.data.dao.EmployeeDao;
import com.terp.data.model.Employee;
import com.terp.plugin.IUser;

/**
 *
 * @author ilknur
 */


public final class User implements IUser {
    
    private String userName;
    private long groupId;
    private boolean authenticated;
    private boolean administrator;
    private EmployeeDao employeeDao;
    private Object password;
    
    public User(){
        this.userName = null;
        this.groupId = -1;
        this.authenticated = false;
        this.administrator = false;
        
        //create eployee dao
        this.employeeDao = new EmployeeDao();
        
    }    
    
    public User(String name, String pwd){
        this.userName = null;
        this.groupId = -1;
        this.authenticated = false;
        this.administrator = false;
        
        //create eployee dao
        this.employeeDao = new EmployeeDao();
        
        //set user name and password
        this.userName = name;
        this.password = pwd;
    }
    
    public void setUsername(String name){
        this.userName = name;
    }
    
    public void setPassword(String pwd){
        this.password = pwd;
    }
    
    @Override
    public String getUserName() {
        return this.userName;
    }

    @Override
    public long getUserGroupId() {
        return this.groupId;
    }

    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    @Override
    public boolean isAdministrator() {        
        return this.administrator;
    }

    @Override
    public boolean isAuthorized(String menuId) {
        //TODO  create menu authorizing system
        return false;
    }

    /**
     * login this user
     */
    public void login() {
        //check if user is already logged.
        if(this.authenticated) 
            return;
        
        String sql = "from Employee e "
                + "where e.userName = '" + this.userName + "' and "
                + "e.type = 1";
        Employee emp = employeeDao.firstOrDefault(sql);
        
        if(emp == null)
            return;
        
        if(emp.getUserName().equals(this.userName) &&
                emp.getPassword().equals(this.password)){
            
            this.authenticated = true;
            this.groupId = emp.getGroup().getRowid();
            this.administrator = (emp.getGroup().getGroupName()
                    .equals("Administrators"));            
        } else {
            this.authenticated = false;
            this.groupId = -1;
            this.administrator = false;
        }
    }

    /**
     * change pasword of this user
     * @param oldPwd
     * @param newPwd
     * @return 
     */
    public boolean changePassword(String oldPwd, String newPwd) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

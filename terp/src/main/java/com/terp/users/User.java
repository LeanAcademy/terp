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

import com.terp.data.CommonDaoImpl;
import com.terp.data.dao.EmployeeDaoImpl;
import com.terp.data.model.EmployeeGroup;
import com.terp.data.model.GroupCompany;
import com.terp.data.model.GroupPermission;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.model.IEmployee;
import com.terp.plugin.IUser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class User implements IUser {

    private String userName;
    private long groupId;
    private boolean authenticated;
    private boolean administrator;
    private IEmployee employee;
    private final EmployeeDaoImpl employeeDao;
    private Object password;
    private final Set<Long> companyIds = new HashSet<>();
    private final Map<String, GroupPermission> menuRights = new HashMap<>();

    public User() {
        this.userName = null;
        this.groupId = -1;
        this.authenticated = false;
        this.administrator = false;
        this.employeeDao = new EmployeeDaoImpl();
    }

    public User(String name, String pwd) {
        this();
        this.userName = name;
        this.password = pwd;
    }

    public void setUsername(String name) {
        this.userName = name;
    }

    public void setPassword(String pwd) {
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
        return canOpen(menuId);
    }

    @Override
    public boolean canOpen(String menuId) {
        if (administrator) {
            return true;
        }
        if (menuId == null || menuId.isBlank()) {
            return false;
        }
        GroupPermission row = menuRights.get(menuId);
        return row != null && row.isViewAllowed();
    }

    @Override
    public boolean canAdd(String menuId) {
        if (administrator) {
            return true;
        }
        GroupPermission row = menuRights.get(menuId);
        return row != null && row.isAddAllowed();
    }

    @Override
    public boolean canEdit(String menuId) {
        if (administrator) {
            return true;
        }
        GroupPermission row = menuRights.get(menuId);
        return row != null && row.isEditAllowed();
    }

    @Override
    public boolean canDelete(String menuId) {
        if (administrator) {
            return true;
        }
        GroupPermission row = menuRights.get(menuId);
        return row != null && row.isDeleteAllowed();
    }

    @Override
    public boolean hasAllCompanies() {
        return administrator;
    }

    @Override
    public boolean canAccessCompany(Long companyId) {
        if (administrator) {
            return true;
        }
        return companyId != null && companyIds.contains(companyId);
    }

    @Override
    public List<Long> getAllowedCompanyIds() {
        return new ArrayList<>(companyIds);
    }

    public void login() {
        if (this.authenticated) {
            return;
        }
        String escaped = this.userName == null ? "" : this.userName.replace("'", "''");
        String sql = "from Employee e join fetch e.group g "
                + "where e.userName = '" + escaped + "' and e.type = 1";
        IEmployee emp = employeeDao.firstOrDefault(sql);
        if (emp == null || emp.getStatus() != 0) {
            return;
        }
        if (emp.getUserName() != null && emp.getUserName().equals(this.userName)
                && emp.getPassword() != null && emp.getPassword().equals(this.password)) {
            this.authenticated = true;
            this.employee = emp;
            EmployeeGroup group = emp.getGroup() instanceof EmployeeGroup eg ? eg : null;
            this.groupId = group == null || group.getRowId() == null ? -1 : group.getRowId();
            this.administrator = group != null && group.isSystemAdmin();
            loadRights();
        } else {
            this.authenticated = false;
            this.groupId = -1;
            this.administrator = false;
        }
    }

    private void loadRights() {
        companyIds.clear();
        menuRights.clear();
        if (administrator || groupId < 0) {
            return;
        }
        ICommonDao<GroupCompany> companyDao = new CommonDaoImpl<>(GroupCompany.class);
        List<GroupCompany> companies = companyDao.findAll(
                "from GroupCompany e where e.group.rowId = " + groupId);
        if (companies != null) {
            for (GroupCompany row : companies) {
                if (row != null && row.getCompanyId() != null) {
                    companyIds.add(row.getCompanyId());
                }
            }
        }
        ICommonDao<GroupPermission> permDao = new CommonDaoImpl<>(GroupPermission.class);
        List<GroupPermission> perms = permDao.findAll(
                "from GroupPermission e where e.group.rowId = " + groupId);
        if (perms != null) {
            for (GroupPermission row : perms) {
                if (row != null && row.getMenuId() != null) {
                    menuRights.put(row.getMenuId(), row);
                }
            }
        }
    }

    public boolean changePassword(String oldPwd, String newPwd) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

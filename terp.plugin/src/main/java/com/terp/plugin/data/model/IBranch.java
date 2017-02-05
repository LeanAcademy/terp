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

/**
 *
 * @author cevdet
 */
public interface IBranch extends ICommonFields{
    public ICompany getCompany();
    public void setCompany(ICompany company);
    public String getBranchName();
    public void setBranchName(String  branchName);
    public String getBranchLongName();
    public void setBranchLongName(String  branchLongName);
    public long getStatus();
    public void setStatus(int status);
    public int getBranchType();
    public void setBranchType(int branchType);
    public String getAddress1();
    public void setAddress1(String address1);
    public String getAddress2();
    public void setAddress2(String address2);
    public String getAddress3();
    public void setAddress3(String address3);
    public String getCity();
    public void setCity(String city);
    public String getState();
    public void setState(String state);
    public String getZip();
    public void setZip(String zip);
    public String getCountry();
    public void setCountry(String country);
    public String getPhoneNum1();
    public void setPhoneNum1(String phoneNum1);
    public String getPhoneNum2();
    public void setPhoneNum2(String phoneNum2);
    public String getFaxNum();
    public void setFaxNum(String faxNum);
    public String getEmail1();
    public void setEmail1(String email1);
    public String getEmail2();
    public void setEmail2(String email2);
}

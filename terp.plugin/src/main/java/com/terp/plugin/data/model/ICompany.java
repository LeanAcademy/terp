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
import javafx.beans.property.StringProperty;


/**
 *
 * @author cevdet
 */


public interface ICompany extends ICommonFields{
    public String getCompanyName();
    public void setCompanyName(String companyName);
    public String getCompanyLongName();
    public void setCompanyLongName(String companyLongName);
    public int getStatus();
    public void setStatus(int status);
    public String getNotes();
    public void setNotes(String notes);
    public String getStateTaxCode();
    public void setStateTaxCode(String stateTaxCode);
    public String getStateTaxRegion();
    public void setStateTaxRegion(String stateTaxRegion);
}

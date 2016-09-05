/*
 * Copyright (C) 2015 cevdet
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
package com.terp.data.impl;

import com.terp.plugin.ICompany;
import com.terp.plugin.IDatabase;

/**
 *
 * @author cevdet
 */
public class Database implements IDatabase {
    /**
     * company table interface
     */
    private ICompany company = null;
    
    /**
     * return company interface
     * @return 
     */
    @Override
    public ICompany getCompany() {
        if(this.company != null) {
            return this.company;
        } else {
            this.company = new CompanyImpl();
            return this.company;
        }
    }
    
}

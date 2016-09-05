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

import java.util.HashMap;
import java.util.Map;
import com.terp.data.dao.CompanyDao;
import com.terp.data.model.Company;
import com.terp.plugin.ICompany;
import com.terp.plugin.ResultData;

/**
 *
 * @author cevdet
 */
public class CompanyImpl implements ICompany {

    private final CompanyDao dao = new CompanyDao();
    
    @Override
    public ResultData findAll() {
        ResultData veri = new ResultData();
        Integer i = 0;
        
        veri.fields = dao.getFields();
        
        i  = 0;
        for(Company cmp: dao.findAll()){
            Map<Integer, Object> row = companyToMap(cmp);
            veri.data.put(i, row);
            i++;
        }
        
        return veri;
    }

    @Override
    public ResultData addOrUpdate(Map<Integer, Object> row) {
        //TODO add or update
        ResultData resultData;
        
        //find data 
        resultData = this.firstOrDefault((long)row.get(0));
        if(resultData != null){
            dao.addOrUpdate(mapToCompany(row));            
        } else {
            resultData = new ResultData();
            resultData.data.put(0,companyToMap(dao.addOrUpdate(mapToCompany(row))));
        }
        return resultData;
    }

    @Override
    public void delete(long rowId) {
        dao.delete(rowId);
    }

    @Override
    public ResultData firstOrDefault(long key) {
        //variables
        ResultData resultData = new ResultData();
        
        //find data
        Company result = dao.firstOrDefault(key);
        if(result != null){
            Map<Integer, Object> temp = companyToMap(result);
            resultData.data.put(0, temp);
            return resultData;
        }
        return null;
    }
    
    private Company mapToCompany(Map<Integer, Object> data){
        Company cmp = new Company();
        cmp.setRowid( (long)data.get(0));
        cmp.setCompanyName((String) data.get(1));
        cmp.setCompanyLongName((String)data.get(2));
        cmp.setStatus((int)data.get(3));
        
        return cmp;
    }
    
    private Map<Integer, Object> companyToMap(Company cmp){
        Map<Integer, Object> temp = new HashMap();
        temp.put(0, cmp.getRowid());
        temp.put(1, cmp.getCompanyName());
        temp.put(2, cmp.getCompanyLongName());
        temp.put(3, cmp.getStatus());
        return temp;
    }
    
}

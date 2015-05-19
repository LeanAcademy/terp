/*
 * Copyright (C) 2015 Lean Academy
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
package terp.data.intfaces;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import terp.data.dao.CompanyDao;
import terp.data.model.CompanyModel;
import terp.plugin.data.Company;
import terp.plugin.data.ICompany;

/**
 *
 * @author cevdet
 */
public class CompanyImp implements ICompany {

    private final CompanyDao companyDao = new CompanyDao();
    
    public CompanyImp(){
    }
    
    @Override
    public List<String> getColumns() {
        List<String> fields = new ArrayList<>();
        
        //get field list
        for(Field field:Company.class.getFields()){
            fields.add(field.getName());
        }    
        return fields;
    }

    @Override
    public List<Company> findAll() {
       List<Company> list = new ArrayList<>();
       
       companyDao.findAll().stream().forEach((cmp) -> {
           list.add(cmp);
        });
       
       return list;
    }

    @Override
    public List<Company> findAll(int pageNum, int rowNum) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Company firstOrDefault(String sql) {
        return this.companyDao.firstOrDefault(sql);
    }

    @Override
    public void addOrUpdate(Company company) {
        CompanyModel companyModel = new CompanyModel();
        companyModel.setRowid(company.rowid);
        companyModel.setCompanyName(company.companyName);
        companyModel.setCompanyLongName(company.companyLongName);
        companyModel.setStatus(company.status);
        this.companyDao.addOrUpdate(companyModel);
    }

    @Override
    public void delete(long rowId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

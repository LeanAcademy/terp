/*
 * Copyright (C) 2015 cevdet
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
package com.terp.core.gui;

import java.util.HashMap;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import com.terp.plugin.ICompany;
import com.terp.plugin.ResultData;

/**
 *
 * @author cevdet
 */
public class CompanyTableModel extends AbstractTableModel {
    
    //fields
    private ICompany cmp;
    private Map<Integer, Map<Integer, Object>> cache;
    private int colCount;
    private String[] headers;

    public CompanyTableModel(ICompany cmp) {
        cache = new HashMap();
        this.cmp = cmp;
        ResultData rd = cmp.findAll();
        this.headers = new String[rd.fields.size()];
        this.headers = rd.fields.toArray(headers);
        this.cache =  rd.data;
        this.colCount = rd.fields.size();
    }

    @Override
    public int getRowCount() {
        return cache.size();
    }

    @Override
    public int getColumnCount() {
        return colCount;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return cache.get(rowIndex).get(columnIndex);
    }
    
    @Override
    public String getColumnName(int index) {
		return headers[index];
    }
    /*
    @Override
    public void setValueAt(Object value, int row, int col){
        Map<Integer, Object> rowNew = new HashMap();
        
        if(cache.containsKey(row)){            
            Map<Integer, Object> rowOld = cache.get(row);
            rowNew.putAll(rowOld);
            rowNew.replace(col, value);
        } else {
            for(Integer i=0; i<this.colCount; i++){
                rowNew.put(i, null);
            }
            rowNew.replace(col, value);
        }
        fireTableDataChanged();
    }*/
    
    @Override
    public Class getColumnClass(int col){
        switch(col){
            default:
                return Object.class;
        }
    }
    
    @Override
     public boolean isCellEditable(int row, int column) {
        return column != 0;
     }
     
     public void update(){
         ResultData rd = cmp.findAll();
         this.cache = rd.data;
         
         fireTableDataChanged();
     }
}

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
package terp.core.model;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import terp.plugin.data.Company;

/**
 *
 * @author cevdet
 */
public class CompanyTableModel extends AbstractTableModel {

    private List<String> columns;
    private List<Company> rows;

    public CompanyTableModel(){}
    
    public CompanyTableModel(List<String> lstColumns){
        this.columns = lstColumns;
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
        
    }
    
    @Override
    public String getColumnName(int col){
        return this.columns.get(col);
    }
    
    @Override
    public int getRowCount() {
        return this.rows.size();
    }

    @Override
    public int getColumnCount() {
        return this.columns.size();
    }
    
    @Override
    public Class getColumnClass(int col){
        Field field = Company.class.getFields()[col];
        return field.getClass();
    }

    @Override
    public boolean isCellEditable(int row, int col){
        //TODO chage so that is disable id column
        return true;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = null;
        
        if(rowIndex >=0 && columnIndex >=0){
            Company row = rows.get(rowIndex);
            Field field = Company.class.getFields()[columnIndex];
            try {
                value = field.get(row);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(CompanyTableModel.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        } 
        
        return value;
    }
    
    public void updateData(List<Company> rows){
        
        //update data source
        this.rows = rows;
        
        //update gui
        this.fireTableDataChanged();
    }
}

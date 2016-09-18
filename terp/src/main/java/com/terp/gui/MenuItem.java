/*
 * Copyright (C) 2016 Cevdet Dal
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
package com.terp.gui;

import com.terp.data.model.MenuSource;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 *
 * @author cevdet
 */
public class MenuItem extends TreeItem<String> {
   
    private boolean hasLoadedChildren = false;
    private MenuSource currentItem;

    public MenuSource getCurrentItem() {
        return currentItem;
    }

    public void setCurrentItem(MenuSource currentItem) {
        this.currentItem = currentItem;
    }
    private final MenuSource menuSource = new MenuSource();
    
    
    private final ChangeListener<Boolean> expandedPropertyListener = new ChangeListener<Boolean>(){
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, 
                Boolean oldValue, Boolean newValue) {
            if(hasLoadedChildren == false) {
                    loadChildren();
                }
        }
    
    };       
    
    public MenuItem(MenuSource item){
        super(item.getMenuId() + " - " + item.getMenuName());
        if(item.getMenuType() == 0){
            super.setValue(item.getMenuName());
        }
        super.expandedProperty().addListener(expandedPropertyListener);
        this.currentItem = item;
    }   

    @Override
    public ObservableList<TreeItem<String>> getChildren() {
        if(!hasLoadedChildren){
            loadChildren();
        }
                
        return super.getChildren();
    }
    
    
    @Override
    public boolean isLeaf() {
        if (hasLoadedChildren) {
            return super.getChildren().isEmpty();
        } else {
        
            // check if there is child to load
            String sql = "from MenuSource e where e.menuType=1 and e.menuParent=" 
                + this.currentItem.getRowId();
            return menuSource.findAll(sql).isEmpty();
        }
    }    

    private void loadChildren() {
        hasLoadedChildren = true;
        
        String sql = "from MenuSource e where e.menuType=1 and e.menuParent=" 
                + this.currentItem.getRowId();
        for(Object menuItem : menuSource.findAll(sql)){
            MenuItem subitem = new MenuItem((MenuSource)menuItem);
            
            // add to children
            super.getChildren().add(subitem);
        }
    
    }
    
}

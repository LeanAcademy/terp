/*
 * Copyright (C) 2016 Cevdet Dal
The regular expression classes are not available.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
The regular expression classes are not available.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
The regular expression classes are not available.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.terp.data.model;

import com.terp.data.CommonFields;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author cevdet
 */
@Entity
@Table(name="menu", catalog="terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_menu", 
                    columnNames = {"menu_kodu"})
        })
public class MenuSource extends CommonFields implements Serializable {
    
    @Column(name="menu_kodu", nullable=false, length=20)
    private String menuId;
    
    @Column(name="menu_aciklama", nullable=false, length=128)
    private String menuName;
    
    @Column(name="menu_tipi", nullable=false, columnDefinition ="int default 0")
    private int menuType;
    
    @Column(name="ust_menu", nullable=false, columnDefinition ="int default 0")
    private Long menuParent;
    
    @Column(name="program", length=128, nullable=true)
    private String programName;
    
    @Column(name="eklenti", nullable=true, columnDefinition="int default 0")
    private int isPlugin;
    
    @Column(name="ekl_ref_num", nullable=true)
    private Long pluginId;
    
    @Column(name="durum", nullable=false)
    private int status;

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public int getMenuType() {
        return menuType;
    }

    public void setMenuType(int menuType) {
        this.menuType = menuType;
    }

    public Long getMenuParent() {
        return menuParent;
    }

    public void setMenuParent(Long menuParent) {
        this.menuParent = menuParent;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public int getIsPlugin() {
        return isPlugin;
    }

    public void setIsPlugin(int isPlugin) {
        this.isPlugin = isPlugin;
    }

    public Long getPluginId() {
        return pluginId;
    }

    public void setPluginId(Long pluginId) {
        this.pluginId = pluginId;
    }
    
}

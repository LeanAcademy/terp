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

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
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
public class MenuSource implements Serializable {

    @Id
    @GeneratedValue(strategy=IDENTITY)
    @Column(name = "ref_num", nullable = false)
    private Long rowid;
    
    @Column(name="menu_kodu", nullable=false, length=20)
    private String menuId;
    
    @Column(name="menu_aciklama", nullable=false, length=128)
    private String menuName;
    
    @Column(name="menu_tipi", nullable=false, columnDefinition ="int default 0")
    private int menuType;
    
    @Column(name="ust_menu", nullable=false, columnDefinition ="int default 0")
    private int menuParent;
    
    @Column(name="durum", nullable=false)
    private int status;

    
    public Long getRowid() {
        return rowid;
    }

    public void setRowid(Long rowid) {
        this.rowid = rowid;
    }

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

    public int getMenuParent() {
        return menuParent;
    }

    public void setMenuParent(int menuParent) {
        this.menuParent = menuParent;
    }
    
}

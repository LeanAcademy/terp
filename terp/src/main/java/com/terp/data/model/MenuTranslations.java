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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 *
 * @author cevdet
 */
@Entity
@Table(name="menu_tercumeleri", catalog="terp", schema="terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_menu_tercumeleri", 
                    columnNames = {"menu_ref", "dil_kodu"})
        })
public class MenuTranslations extends CommonFields implements Serializable {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "menu_ref", 
            nullable = false,
            referencedColumnName = "ref_num",
            foreignKey = @ForeignKey(name = "fk_menu_menu_tercumeleri"))
    private MenuSource menu;
    
    @Column(name = "dil_kodu", nullable = false, length=3)
    private String langId;
    
    @Column(name = "tercume", nullable = false, length=128)
    private String translation;

    public MenuSource getMenu() {
        return menu;
    }

    public void setMenu(MenuSource menu) {
        this.menu = menu;
    }

    public String getLangId() {
        return langId;
    }

    public void setLangId(String langId) {
        this.langId = langId;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }
    
}

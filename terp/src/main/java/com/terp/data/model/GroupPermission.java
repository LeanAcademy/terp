/*
 * Copyright (C) 2026 LeanAcademy
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.terp.data.model;

import com.terp.plugin.data.CommonFields;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "grup_yetki", catalog = "terp", schema = "terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_grup_yetki", columnNames = {"grup_ref", "menu_kodu"})
        })
public class GroupPermission extends CommonFields implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grup_ref", nullable = false, referencedColumnName = "ref_num",
            foreignKey = @ForeignKey(name = "fk_grup_yetki_grup"))
    private EmployeeGroup group;

    @Column(name = "menu_kodu", nullable = false, length = 20)
    private String menuId;

    @Column(name = "goruntule")
    private Integer canView;

    @Column(name = "ekle")
    private Integer canAdd;

    @Column(name = "duzenle")
    private Integer canEdit;

    @Column(name = "sil")
    private Integer canDelete;

    public EmployeeGroup getGroup() {
        return group;
    }

    public void setGroup(EmployeeGroup group) {
        this.group = group;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public boolean isViewAllowed() {
        return canView != null && canView != 0;
    }

    public boolean isAddAllowed() {
        return canAdd != null && canAdd != 0;
    }

    public boolean isEditAllowed() {
        return canEdit != null && canEdit != 0;
    }

    public boolean isDeleteAllowed() {
        return canDelete != null && canDelete != 0;
    }

    public void setCanView(int canView) {
        this.canView = canView;
    }

    public void setCanAdd(int canAdd) {
        this.canAdd = canAdd;
    }

    public void setCanEdit(int canEdit) {
        this.canEdit = canEdit;
    }

    public void setCanDelete(int canDelete) {
        this.canDelete = canDelete;
    }
}

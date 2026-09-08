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
@Table(name = "grup_firma", catalog = "terp", schema = "terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_grup_firma", columnNames = {"grup_ref", "firma_ref"})
        })
public class GroupCompany extends CommonFields implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grup_ref", nullable = false, referencedColumnName = "ref_num",
            foreignKey = @ForeignKey(name = "fk_grup_firma_grup"))
    private EmployeeGroup group;

    @Column(name = "firma_ref", nullable = false)
    private Long companyId;

    public EmployeeGroup getGroup() {
        return group;
    }

    public void setGroup(EmployeeGroup group) {
        this.group = group;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}

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
package com.terp.satin.data;

import com.terp.plugin.data.CommonFields;
import com.terp.plugin.data.ICompanyScoped;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "satin_ayar", catalog = "terp", schema = "terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_satin_ayar_firma", columnNames = {"firma_ref"})
        })
public class PurchaseSettings extends CommonFields implements Serializable, ICompanyScoped {

    @Column(name = "firma_ref")
    private Long companyId;

    @Column(name = "fazla_kabul_oran")
    private Double overReceiptPercent;

    public PurchaseSettings() {
        this.overReceiptPercent = 0d;
    }

    public double getOverReceiptPercent() {
        return overReceiptPercent == null ? 0d : overReceiptPercent;
    }

    public void setOverReceiptPercent(Double overReceiptPercent) {
        this.overReceiptPercent = overReceiptPercent;
    }

    @Override
    public Long getCompanyId() {
        return companyId;
    }

    @Override
    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}

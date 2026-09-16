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
package com.terp.uretim.data;

import com.terp.plugin.data.CommonFields;
import com.terp.plugin.data.ICompanyScoped;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "uretim_recete", catalog = "terp", schema = "terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_uretim_recete_firma", columnNames = {"firma_ref", "mamul_kodu"})
        })
public class Recipe extends CommonFields implements Serializable, ICompanyScoped {

    public static final int STATUS_ACTIVE = 0;
    public static final int STATUS_INACTIVE = 1;

    @Column(name = "firma_ref")
    private Long companyId;

    @Column(name = "mamul_kodu", nullable = false, length = 50)
    private String productCode;

    @Column(name = "mamul_adi", length = 128)
    private String productName;

    @Column(name = "mamul_birimi", length = 20)
    private String productUnit;

    @Column(name = "miktar", nullable = false)
    private Double yieldQuantity;

    @Column(name = "aciklama", length = 512)
    private String notes;

    @Column(name = "durum")
    private Integer status;

    public Recipe() {
        this.status = STATUS_ACTIVE;
        this.yieldQuantity = 1d;
    }

    public String getStatusLabel() {
        return isActive() ? "Aktif" : "Pasif";
    }

    public boolean isActive() {
        return getStatus() == STATUS_ACTIVE;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductUnit() {
        return productUnit;
    }

    public void setProductUnit(String productUnit) {
        this.productUnit = productUnit;
    }

    public Double getYieldQuantity() {
        return yieldQuantity;
    }

    public void setYieldQuantity(Double yieldQuantity) {
        this.yieldQuantity = yieldQuantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getStatus() {
        return status == null ? STATUS_ACTIVE : status;
    }

    public void setStatus(int status) {
        this.status = status;
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

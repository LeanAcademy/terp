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
package com.terp.core.data;

import com.terp.plugin.data.CommonFields;
import com.terp.plugin.data.ICompanyScoped;
import com.terp.plugin.data.model.IVatRate;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "kdv_oran", catalog = "terp", schema = "terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_kdv_oran_firma", columnNames = {"firma_ref", "oran_kodu"})
        })
public class VatRate extends CommonFields implements Serializable, ICompanyScoped, IVatRate {

    public static final int STATUS_ACTIVE = 0;
    public static final int STATUS_INACTIVE = 1;

    @Column(name = "firma_ref")
    private Long companyId;

    @Column(name = "oran_kodu", nullable = false, length = 50)
    private String rateCode;

    @Column(name = "oran_adi", nullable = false, length = 128)
    private String rateName;

    @Column(name = "oran", nullable = false)
    private Double rate;

    @Column(name = "durum")
    private Integer status;

    public VatRate() {
        this.status = STATUS_ACTIVE;
        this.rate = 0d;
    }

    public String getStatusLabel() {
        return getStatus() == STATUS_ACTIVE ? "Aktif" : "Pasif";
    }

    @Override
    public String getRateCode() {
        return rateCode;
    }

    public void setRateCode(String rateCode) {
        this.rateCode = rateCode;
    }

    @Override
    public String getRateName() {
        return rateName;
    }

    public void setRateName(String rateName) {
        this.rateName = rateName;
    }

    @Override
    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    @Override
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

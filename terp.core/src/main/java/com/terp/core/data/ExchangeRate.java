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
import com.terp.plugin.data.model.IExchangeRate;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "doviz_kur", catalog = "terp", schema = "terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_doviz_kur_firma",
                    columnNames = {"firma_ref", "doviz_kodu", "kur_tarihi"})
        })
public class ExchangeRate extends CommonFields implements Serializable, ICompanyScoped, IExchangeRate {

    public static final int STATUS_ACTIVE = 0;
    public static final int STATUS_INACTIVE = 1;

    @Column(name = "firma_ref")
    private Long companyId;

    @Column(name = "doviz_kodu", nullable = false, length = 10)
    private String currencyCode;

    @Temporal(TemporalType.DATE)
    @Column(name = "kur_tarihi", nullable = false)
    private Date rateDate;

    @Column(name = "kur", nullable = false)
    private Double rate;

    @Column(name = "durum")
    private Integer status;

    public ExchangeRate() {
        this.status = STATUS_ACTIVE;
        this.rate = 1d;
        this.rateDate = new Date();
    }

    public String getStatusLabel() {
        return getStatus() == STATUS_ACTIVE ? "Aktif" : "Pasif";
    }

    public String getDateLabel() {
        if (rateDate == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(rateDate);
    }

    @Override
    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @Override
    public Date getRateDate() {
        return rateDate;
    }

    public void setRateDate(Date rateDate) {
        this.rateDate = rateDate;
    }

    @Override
    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
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

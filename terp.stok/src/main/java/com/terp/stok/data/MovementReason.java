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
package com.terp.stok.data;

import com.terp.plugin.data.CommonFields;
import com.terp.plugin.data.ICompanyScoped;
import com.terp.plugin.data.StockDirection;
import com.terp.plugin.data.model.IMovementReason;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "hareket_neden", catalog = "terp", schema = "terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_neden_firma", columnNames = {"firma_ref", "neden_kodu"})
        })
public class MovementReason extends CommonFields implements Serializable, ICompanyScoped, IMovementReason {

    @Column(name = "firma_ref")
    private Long companyId;

    @Column(name = "neden_kodu", nullable = false, length = 50)
    private String reasonCode;

    @Column(name = "neden_adi", nullable = false, length = 128)
    private String reasonName;

    @Column(name = "yon")
    private Integer direction;

    @Column(name = "durum")
    private Integer status;

    public MovementReason() {
        this.direction = StockDirection.IN;
        this.status = 0;
    }

    public String getStatusLabel() {
        return getStatus() == 0 ? "Aktif" : "Pasif";
    }

    @Override
    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    @Override
    public String getReasonName() {
        return reasonName;
    }

    public void setReasonName(String reasonName) {
        this.reasonName = reasonName;
    }

    @Override
    public int getDirection() {
        return direction == null ? StockDirection.IN : direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    @Override
    public int getStatus() {
        return status == null ? 0 : status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDirectionLabel() {
        return StockDirection.labelOf(getDirection());
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

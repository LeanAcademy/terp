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
import com.terp.plugin.data.model.IStockWarehouse;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "depo", catalog = "terp", schema = "terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_depo_firma", columnNames = {"firma_ref", "depo_kodu"})
        })
public class Warehouse extends CommonFields implements Serializable, ICompanyScoped, IStockWarehouse {

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_SHIPPING = 1;
    public static final int TYPE_PRODUCTION = 2;
    public static final int TYPE_CONSIGNMENT = 3;
    public static final int TYPE_SCRAP = 4;

    public static final String[] TYPE_LABELS = {
        "Normal", "Sevkiyat", "Üretim", "Konsinye", "Fire"
    };

    @Column(name = "firma_ref")
    private Long companyId;

    @Column(name = "depo_kodu", nullable = false, length = 50)
    private String warehouseCode;

    @Column(name = "depo_adi", nullable = false, length = 128)
    private String warehouseName;

    @Column(name = "depo_turu")
    private Integer warehouseType;

    @Column(name = "adres", length = 256)
    private String address;

    @Column(name = "sehir", length = 64)
    private String city;

    @Column(name = "negatif")
    private Integer allowNegative;

    @Column(name = "durum")
    private Integer status;

    @Column(name = "aciklama", length = 512)
    private String notes;

    public Warehouse() {
        this.warehouseType = TYPE_NORMAL;
        this.allowNegative = 0;
        this.status = 0;
    }

    public String getTypeLabel() {
        int type = getWarehouseType();
        if (type < 0 || type >= TYPE_LABELS.length) {
            return Integer.toString(type);
        }
        return TYPE_LABELS[type];
    }

    public String getStatusLabel() {
        return getStatus() == 0 ? "Aktif" : "Pasif";
    }

    public String getDisplayLabel() {
        String code = warehouseCode == null ? "" : warehouseCode;
        String name = warehouseName == null ? "" : warehouseName;
        if (code.isBlank()) {
            return name;
        }
        if (name.isBlank()) {
            return code;
        }
        return code + " — " + name;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public int getWarehouseType() {
        return warehouseType == null ? TYPE_NORMAL : warehouseType;
    }

    public void setWarehouseType(int warehouseType) {
        this.warehouseType = warehouseType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getAllowNegative() {
        return allowNegative == null ? 0 : allowNegative;
    }

    public void setAllowNegative(int allowNegative) {
        this.allowNegative = allowNegative;
    }

    public boolean isNegativeAllowed() {
        return getAllowNegative() != 0;
    }

    public int getStatus() {
        return status == null ? 0 : status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

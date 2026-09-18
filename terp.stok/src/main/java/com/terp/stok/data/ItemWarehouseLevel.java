/*
 * Copyright (C) 2026 LeanAcademy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.terp.stok.data;

import com.terp.plugin.data.ICompanyScoped;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "stok_depo_seviye", catalog = "terp", schema = "terp", uniqueConstraints = {
    @UniqueConstraint(name = "ix_stok_depo_seviye", columnNames = {"firma_ref", "mlz_kodu", "depo_kodu"})
})
public class ItemWarehouseLevel implements Serializable, ICompanyScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "row_id")
    private Long rowId;

    @Column(name = "firma_ref")
    private Long companyId;

    @Column(name = "mlz_kodu", length = 50)
    private String itemCode;

    @Column(name = "depo_kodu", length = 20)
    private String warehouseCode;

    @Column(name = "min_stok")
    private Double minStock;

    @Column(name = "siparis_seviye")
    private Double orderLevel;

    @Column(name = "hedef_stok")
    private Double targetStock;

    public Long getRowId() {
        return rowId;
    }

    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    @Override
    public Long getCompanyId() {
        return companyId;
    }

    @Override
    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public Double getMinStock() {
        return minStock;
    }

    public void setMinStock(Double minStock) {
        this.minStock = minStock;
    }

    public Double getOrderLevel() {
        return orderLevel;
    }

    public void setOrderLevel(Double orderLevel) {
        this.orderLevel = orderLevel;
    }

    public Double getTargetStock() {
        return targetStock;
    }

    public void setTargetStock(Double targetStock) {
        this.targetStock = targetStock;
    }
}

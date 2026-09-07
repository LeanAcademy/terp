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
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "stok_hareket", catalog = "terp", schema = "terp")
public class StockMovement extends CommonFields implements Serializable {

    public static final int TYPE_IN = 0;
    public static final int TYPE_OUT = 1;
    public static final int TYPE_TRANSFER = 2;
    public static final int TYPE_COUNT_IN = 3;
    public static final int TYPE_COUNT_OUT = 4;
    public static final int TYPE_SCRAP = 5;

    public static final String[] TYPE_LABELS = {
        "Giriş", "Çıkış", "Transfer", "Sayım artış", "Sayım azalış", "Fire"
    };

    @Temporal(TemporalType.DATE)
    @Column(name = "hareket_tarihi")
    private Date movementDate;

    @Column(name = "hareket_turu")
    private Integer movementType;

    @Column(name = "depo_kodu", nullable = false, length = 50)
    private String warehouseCode;

    @Column(name = "depo_adi", length = 128)
    private String warehouseName;

    @Column(name = "hedef_depo", length = 50)
    private String targetWarehouseCode;

    @Column(name = "hedef_depo_adi", length = 128)
    private String targetWarehouseName;

    @Column(name = "mlz_kodu", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "mlz_tanimi", length = 128)
    private String itemDesc;

    @Column(name = "mlz_birimi", length = 20)
    private String itemUnit;

    @Column(name = "miktar", nullable = false)
    private Double quantity;

    @Column(name = "birim_fiyat")
    private Double unitPrice;

    @Column(name = "cari_kodu", length = 50)
    private String accountCode;

    @Column(name = "cari_unvan", length = 128)
    private String accountName;

    @Column(name = "lot_no", length = 50)
    private String lotNo;

    @Column(name = "seri_no", length = 50)
    private String serialNo;

    @Column(name = "belge_no", length = 50)
    private String documentNo;

    @Column(name = "aciklama", length = 512)
    private String notes;

    public StockMovement() {
        this.movementType = TYPE_IN;
        this.movementDate = new Date();
        this.quantity = 0d;
    }

    public String getTypeLabel() {
        int type = getMovementType();
        if (type < 0 || type >= TYPE_LABELS.length) {
            return Integer.toString(type);
        }
        return TYPE_LABELS[type];
    }

    public String getDateLabel() {
        if (movementDate == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(movementDate);
    }

    public double signedQuantityFor(String warehouse, String item) {
        if (warehouse == null || item == null || !item.equals(itemCode)) {
            return 0d;
        }
        double qty = quantity == null ? 0d : quantity;
        int type = getMovementType();
        if (type == TYPE_IN || type == TYPE_COUNT_IN) {
            return warehouse.equals(warehouseCode) ? qty : 0d;
        }
        if (type == TYPE_OUT || type == TYPE_COUNT_OUT || type == TYPE_SCRAP) {
            return warehouse.equals(warehouseCode) ? -qty : 0d;
        }
        if (type == TYPE_TRANSFER) {
            if (warehouse.equals(warehouseCode)) {
                return -qty;
            }
            if (warehouse.equals(targetWarehouseCode)) {
                return qty;
            }
        }
        return 0d;
    }

    public boolean isOutbound() {
        int type = getMovementType();
        return type == TYPE_OUT || type == TYPE_TRANSFER || type == TYPE_COUNT_OUT || type == TYPE_SCRAP;
    }

    public Date getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(Date movementDate) {
        this.movementDate = movementDate;
    }

    public int getMovementType() {
        return movementType == null ? TYPE_IN : movementType;
    }

    public void setMovementType(int movementType) {
        this.movementType = movementType;
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

    public String getTargetWarehouseCode() {
        return targetWarehouseCode;
    }

    public void setTargetWarehouseCode(String targetWarehouseCode) {
        this.targetWarehouseCode = targetWarehouseCode;
    }

    public String getTargetWarehouseName() {
        return targetWarehouseName;
    }

    public void setTargetWarehouseName(String targetWarehouseName) {
        this.targetWarehouseName = targetWarehouseName;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }

    public String getItemUnit() {
        return itemUnit;
    }

    public void setItemUnit(String itemUnit) {
        this.itemUnit = itemUnit;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getLotNo() {
        return lotNo;
    }

    public void setLotNo(String lotNo) {
        this.lotNo = lotNo;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getDocumentNo() {
        return documentNo;
    }

    public void setDocumentNo(String documentNo) {
        this.documentNo = documentNo;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

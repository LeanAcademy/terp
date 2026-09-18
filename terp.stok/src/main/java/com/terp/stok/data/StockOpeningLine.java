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

import com.terp.plugin.data.CommonFields;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

@Entity
@Table(name = "stok_acilis_satir", catalog = "terp", schema = "terp")
public class StockOpeningLine extends CommonFields implements Serializable {

    @Column(name = "belge_ref", nullable = false)
    private Long openingId;

    @Column(name = "satir_no")
    private Integer lineNo;

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

    @Column(name = "lot_no", length = 50)
    private String lotNo;

    @Column(name = "seri_no", length = 50)
    private String serialNo;

    @Temporal(TemporalType.DATE)
    @Column(name = "skt")
    private Date expiryDate;

    public StockOpeningLine() {
        this.lineNo = 1;
        this.quantity = 0d;
    }

    public Long getOpeningId() {
        return openingId;
    }

    public void setOpeningId(Long openingId) {
        this.openingId = openingId;
    }

    public int getLineNo() {
        return lineNo == null ? 1 : lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
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

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Transient
    public String getExpiryLabel() {
        if (expiryDate == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(expiryDate);
    }
}

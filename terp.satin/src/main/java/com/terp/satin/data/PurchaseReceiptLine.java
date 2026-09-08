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
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "satin_kabul_satir", catalog = "terp", schema = "terp")
public class PurchaseReceiptLine extends CommonFields implements Serializable {

    @Column(name = "belge_ref", nullable = false)
    private Long receiptId;

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

    @Column(name = "siparis_satir_ref")
    private Long sourceOrderLineId;

    @Column(name = "siparis_ref")
    private Long sourceOrderId;

    @Column(name = "siparis_no", length = 50)
    private String sourceOrderNo;

    public PurchaseReceiptLine() {
        this.quantity = 0d;
        this.lineNo = 1;
    }

    public Long getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(Long receiptId) {
        this.receiptId = receiptId;
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

    public Long getSourceOrderLineId() {
        return sourceOrderLineId;
    }

    public void setSourceOrderLineId(Long sourceOrderLineId) {
        this.sourceOrderLineId = sourceOrderLineId;
    }

    public Long getSourceOrderId() {
        return sourceOrderId;
    }

    public void setSourceOrderId(Long sourceOrderId) {
        this.sourceOrderId = sourceOrderId;
    }

    public String getSourceOrderNo() {
        return sourceOrderNo;
    }

    public void setSourceOrderNo(String sourceOrderNo) {
        this.sourceOrderNo = sourceOrderNo;
    }
}

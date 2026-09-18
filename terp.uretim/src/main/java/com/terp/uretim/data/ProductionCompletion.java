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
import java.text.SimpleDateFormat;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "uretim_bitis", catalog = "terp", schema = "terp")
public class ProductionCompletion extends CommonFields implements Serializable, ICompanyScoped {

    public static final int STATUS_POSTED = 1;
    public static final int STATUS_CANCELLED = 2;

    @Column(name = "firma_ref")
    private Long companyId;

    @Column(name = "emri_ref", nullable = false)
    private Long orderId;

    @Temporal(TemporalType.DATE)
    @Column(name = "belge_tarihi")
    private Date documentDate;

    @Column(name = "miktar", nullable = false)
    private Double producedQty;

    @Column(name = "durum")
    private Integer status;

    @Column(name = "aciklama", length = 512)
    private String notes;

    @Column(name = "lot_no", length = 50)
    private String lotNo;

    @Column(name = "seri_no", length = 50)
    private String serialNo;

    @Temporal(TemporalType.DATE)
    @Column(name = "skt")
    private Date expiryDate;

    @jakarta.persistence.Transient
    private String orderDocumentNo;
    @jakarta.persistence.Transient
    private String productCode;
    @jakarta.persistence.Transient
    private String productName;

    public ProductionCompletion() {
        this.status = STATUS_POSTED;
        this.documentDate = new Date();
        this.producedQty = 0d;
    }

    public String getStatusLabel() {
        return isCancelled() ? "İptal" : "İşlendi";
    }

    public String getDateLabel() {
        if (documentDate == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(documentDate);
    }

    public boolean isPosted() {
        return getStatus() == STATUS_POSTED;
    }

    public boolean isCancelled() {
        return getStatus() == STATUS_CANCELLED;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Date getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(Date documentDate) {
        this.documentDate = documentDate;
    }

    public Double getProducedQty() {
        return producedQty == null ? 0d : producedQty;
    }

    public void setProducedQty(Double producedQty) {
        this.producedQty = producedQty;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public String getOrderDocumentNo() {
        return orderDocumentNo;
    }

    public void setOrderDocumentNo(String orderDocumentNo) {
        this.orderDocumentNo = orderDocumentNo;
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

    public int getStatus() {
        return status == null ? STATUS_POSTED : status;
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

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
package com.terp.satis.data;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "satis_siparis", catalog = "terp", schema = "terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_satis_siparis_firma", columnNames = {"firma_ref", "belge_no"})
        })
public class SalesOrder extends CommonFields implements Serializable, ICompanyScoped {

    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_CANCELLED = 2;
    public static final int STATUS_DELIVERED = 3;

    @Column(name = "firma_ref")
    private Long companyId;

    @Column(name = "belge_no", nullable = false, length = 50)
    private String documentNo;

    @Temporal(TemporalType.DATE)
    @Column(name = "belge_tarihi")
    private Date documentDate;

    @Column(name = "cari_kodu", length = 50)
    private String accountCode;

    @Column(name = "cari_unvan", length = 128)
    private String accountName;

    @Column(name = "depo_kodu", nullable = false, length = 50)
    private String warehouseCode;

    @Column(name = "depo_adi", length = 128)
    private String warehouseName;

    @Column(name = "teklif_ref")
    private Long sourceQuoteId;

    @Column(name = "teklif_no", length = 50)
    private String sourceQuoteNo;

    @Column(name = "durum")
    private Integer status;

    @Column(name = "aciklama", length = 512)
    private String notes;

    public SalesOrder() {
        this.status = STATUS_DRAFT;
        this.documentDate = new Date();
    }

    public String getStatusLabel() {
        int value = getStatus();
        if (value == STATUS_APPROVED) {
            return "Onaylı";
        }
        if (value == STATUS_DELIVERED) {
            return "Kapalı";
        }
        if (value == STATUS_CANCELLED) {
            return "İptal";
        }
        return "Taslak";
    }

    public String getDateLabel() {
        if (documentDate == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(documentDate);
    }

    public boolean isDraft() {
        return getStatus() == STATUS_DRAFT;
    }

    public boolean isApproved() {
        return getStatus() == STATUS_APPROVED;
    }

    public boolean isCancelled() {
        return getStatus() == STATUS_CANCELLED;
    }

    public boolean isDelivered() {
        return getStatus() == STATUS_DELIVERED;
    }

    public boolean canDeliver() {
        return isApproved() || isDelivered();
    }

    public String getDocumentNo() {
        return documentNo;
    }

    public void setDocumentNo(String documentNo) {
        this.documentNo = documentNo;
    }

    public Date getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(Date documentDate) {
        this.documentDate = documentDate;
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

    public Long getSourceQuoteId() {
        return sourceQuoteId;
    }

    public void setSourceQuoteId(Long sourceQuoteId) {
        this.sourceQuoteId = sourceQuoteId;
    }

    public String getSourceQuoteNo() {
        return sourceQuoteNo;
    }

    public void setSourceQuoteNo(String sourceQuoteNo) {
        this.sourceQuoteNo = sourceQuoteNo;
    }

    public int getStatus() {
        return status == null ? STATUS_DRAFT : status;
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

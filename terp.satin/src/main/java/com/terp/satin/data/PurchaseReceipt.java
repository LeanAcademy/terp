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
@Table(name = "satin_kabul", catalog = "terp", schema = "terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_satin_kabul_firma", columnNames = {"firma_ref", "belge_no"})
        })
public class PurchaseReceipt extends CommonFields implements Serializable, ICompanyScoped {

    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_POSTED = 1;
    public static final int STATUS_CANCELLED = 2;

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

    @Column(name = "siparis_ref")
    private Long sourceOrderId;

    @Column(name = "siparis_no", length = 50)
    private String sourceOrderNo;

    @Column(name = "durum")
    private Integer status;

    @Column(name = "aciklama", length = 512)
    private String notes;

    public PurchaseReceipt() {
        this.status = STATUS_DRAFT;
        this.documentDate = new Date();
    }

    public String getStatusLabel() {
        int value = getStatus();
        if (value == STATUS_POSTED) {
            return "Onaylı";
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

    public boolean isPosted() {
        return getStatus() == STATUS_POSTED;
    }

    public boolean isCancelled() {
        return getStatus() == STATUS_CANCELLED;
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

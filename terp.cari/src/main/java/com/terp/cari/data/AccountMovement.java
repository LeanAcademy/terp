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
package com.terp.cari.data;

import com.terp.plugin.data.AccountDirection;
import com.terp.plugin.data.AccountPosting;
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
@Table(name = "cari_hareket", catalog = "terp", schema = "terp")
public class AccountMovement extends CommonFields implements Serializable, ICompanyScoped {

    public static final String[] TYPE_LABELS = AccountDirection.LABELS;

    @Column(name = "firma_ref")
    private Long companyId;

    @Temporal(TemporalType.DATE)
    @Column(name = "hareket_tarihi")
    private Date movementDate;

    @Column(name = "hareket_turu")
    private Integer movementType;

    @Column(name = "cari_kodu", nullable = false, length = 50)
    private String accountCode;

    @Column(name = "cari_unvan", length = 128)
    private String accountName;

    @Column(name = "tutar", nullable = false)
    private Double amount;

    @Column(name = "belge_no", length = 50)
    private String documentNo;

    @Column(name = "kaynak_turu", length = 50)
    private String sourceType;

    @Column(name = "kaynak_ref")
    private Long sourceId;

    @Column(name = "aciklama", length = 512)
    private String notes;

    public AccountMovement() {
        this.movementType = AccountDirection.DEBIT;
        this.movementDate = new Date();
        this.amount = 0d;
    }

    public String getTypeLabel() {
        return AccountDirection.labelOf(getMovementType());
    }

    public String getDateLabel() {
        if (movementDate == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(movementDate);
    }

    public String getSourceLabel() {
        if (sourceType == null || sourceType.isBlank()) {
            return "Elle";
        }
        if (AccountPosting.SOURCE_SALES_INVOICE.equals(sourceType)) {
            return "Satış faturası";
        }
        if (AccountPosting.SOURCE_PURCHASE_INVOICE.equals(sourceType)) {
            return "Alım faturası";
        }
        if (AccountPosting.SOURCE_PURCHASE_RECEIPT.equals(sourceType)) {
            return "Mal kabul";
        }
        if (AccountPosting.SOURCE_MANUAL.equals(sourceType)) {
            return "Elle";
        }
        return sourceType;
    }

    public double signedAmount() {
        double value = amount == null ? 0d : amount;
        return getMovementType() == AccountDirection.CREDIT ? -value : value;
    }

    public boolean isDocumentPosted() {
        return sourceType != null && !sourceType.isBlank() && sourceId != null
                && !AccountPosting.SOURCE_MANUAL.equals(sourceType);
    }

    public Date getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(Date movementDate) {
        this.movementDate = movementDate;
    }

    public int getMovementType() {
        return movementType == null ? AccountDirection.DEBIT : movementType;
    }

    public void setMovementType(int movementType) {
        this.movementType = movementType;
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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDocumentNo() {
        return documentNo;
    }

    public void setDocumentNo(String documentNo) {
        this.documentNo = documentNo;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
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

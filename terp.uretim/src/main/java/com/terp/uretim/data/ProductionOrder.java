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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "uretim_emri", catalog = "terp", schema = "terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_uretim_emri_firma", columnNames = {"firma_ref", "belge_no"})
        })
public class ProductionOrder extends CommonFields implements Serializable, ICompanyScoped {

    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_CANCELLED = 2;
    public static final int STATUS_CLOSED = 3;

    @Column(name = "firma_ref")
    private Long companyId;

    @Column(name = "belge_no", nullable = false, length = 50)
    private String documentNo;

    @Temporal(TemporalType.DATE)
    @Column(name = "belge_tarihi")
    private Date documentDate;

    @Column(name = "depo_kodu", nullable = false, length = 50)
    private String warehouseCode;

    @Column(name = "depo_adi", length = 128)
    private String warehouseName;

    @Column(name = "mamul_kodu", nullable = false, length = 50)
    private String productCode;

    @Column(name = "mamul_adi", length = 128)
    private String productName;

    @Column(name = "mamul_birimi", length = 20)
    private String productUnit;

    @Column(name = "miktar", nullable = false)
    private Double plannedQty;

    @Column(name = "uretilen")
    private Double completedQty;

    @Column(name = "recete_ref")
    private Long recipeId;

    @Column(name = "recete_miktar")
    private Double yieldQuantity;

    @Column(name = "siparis_ref")
    private Long sourceOrderId;

    @Column(name = "siparis_no", length = 50)
    private String sourceOrderNo;

    @Column(name = "siparis_satir_ref")
    private Long sourceOrderLineId;

    @Column(name = "durum")
    private Integer status;

    @Column(name = "aciklama", length = 512)
    private String notes;

    public ProductionOrder() {
        this.status = STATUS_DRAFT;
        this.documentDate = new Date();
        this.plannedQty = 0d;
        this.completedQty = 0d;
        this.yieldQuantity = 1d;
    }

    public String getStatusLabel() {
        int value = getStatus();
        if (value == STATUS_APPROVED) {
            return "Onaylı";
        }
        if (value == STATUS_CLOSED) {
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

    public String getDisplayLabel() {
        String no = documentNo == null ? "" : documentNo;
        String product = productCode == null ? "" : productCode;
        String name = productName == null ? "" : productName;
        StringBuilder label = new StringBuilder(no);
        if (!product.isBlank()) {
            if (label.length() > 0) {
                label.append(" — ");
            }
            label.append(product);
            if (!name.isBlank()) {
                label.append(" — ").append(name);
            }
        }
        double remaining = remainingQty();
        if (remaining > UretimDocs.EPS) {
            label.append(" (kalan ").append(remaining).append(')');
        }
        return label.toString();
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

    public boolean isClosed() {
        return getStatus() == STATUS_CLOSED;
    }

    public boolean canComplete() {
        return (isApproved() || isClosed()) && remainingQty() > UretimDocs.EPS;
    }

    public double remainingQty() {
        return UretimDocs.remaining(getPlannedQty(), getCompletedQty());
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

    public String getProductUnit() {
        return productUnit;
    }

    public void setProductUnit(String productUnit) {
        this.productUnit = productUnit;
    }

    public Double getPlannedQty() {
        return plannedQty == null ? 0d : plannedQty;
    }

    public void setPlannedQty(Double plannedQty) {
        this.plannedQty = plannedQty;
    }

    public Double getCompletedQty() {
        return completedQty == null ? 0d : completedQty;
    }

    public void setCompletedQty(Double completedQty) {
        this.completedQty = completedQty;
    }

    public Long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Long recipeId) {
        this.recipeId = recipeId;
    }

    public Double getYieldQuantity() {
        return yieldQuantity == null || yieldQuantity <= 0d ? 1d : yieldQuantity;
    }

    public void setYieldQuantity(Double yieldQuantity) {
        this.yieldQuantity = yieldQuantity;
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

    public Long getSourceOrderLineId() {
        return sourceOrderLineId;
    }

    public void setSourceOrderLineId(Long sourceOrderLineId) {
        this.sourceOrderLineId = sourceOrderLineId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getStatus() {
        return status == null ? STATUS_DRAFT : status;
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

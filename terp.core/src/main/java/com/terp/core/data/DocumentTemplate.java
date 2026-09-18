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
package com.terp.core.data;

import com.terp.plugin.data.CommonFields;
import com.terp.plugin.data.ICompanyScoped;
import com.terp.plugin.data.model.IDocumentTemplate;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "belge_sablon", catalog = "terp", schema = "terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_belge_sablon_firma", columnNames = {"firma_ref", "sablon_kodu"})
        })
public class DocumentTemplate extends CommonFields implements Serializable, ICompanyScoped, IDocumentTemplate {

    public static final int STATUS_ACTIVE = 0;
    public static final int STATUS_INACTIVE = 1;

    public static final String TYPE_SALES_INVOICE = "SATIS_FATURA";
    public static final String TYPE_SALES_DELIVERY = "SATIS_IRSALIYE";
    public static final String TYPE_PURCHASE_INVOICE = "ALIM_FATURA";
    public static final String TYPE_PURCHASE_RECEIPT = "ALIM_IRSALIYE";
    public static final String TYPE_STOCK_LABEL = "STOK_ETIKET";

    @Column(name = "firma_ref")
    private Long companyId;

    @Column(name = "sablon_kodu", nullable = false, length = 50)
    private String templateCode;

    @Column(name = "sablon_adi", nullable = false, length = 128)
    private String templateName;

    @Column(name = "belge_turu", nullable = false, length = 50)
    private String documentType;

    @Column(name = "govde", length = 4000)
    private String body;

    @Column(name = "durum")
    private Integer status;

    public DocumentTemplate() {
        this.status = STATUS_ACTIVE;
    }

    public String getStatusLabel() {
        return getStatus() == STATUS_ACTIVE ? "Aktif" : "Pasif";
    }

    @Override
    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    @Override
    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    @Override
    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    @Override
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getStatus() {
        return status == null ? STATUS_ACTIVE : status;
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

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
import com.terp.plugin.data.DocumentNumbers;
import com.terp.plugin.data.ICompanyScoped;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "belge_seri", catalog = "terp", schema = "terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_belge_seri_firma", columnNames = {"firma_ref", "belge_turu"})
        })
public class DocumentSeries extends CommonFields implements Serializable, ICompanyScoped {

    @Column(name = "firma_ref")
    private Long companyId;

    @Column(name = "belge_turu", nullable = false, length = 50)
    private String documentType;

    @Column(name = "onek", length = 20)
    private String prefix;

    @Column(name = "hane")
    private Integer padding;

    @Column(name = "son_no")
    private Long lastNumber;

    public DocumentSeries() {
        this.padding = DocumentNumbers.DEFAULT_PADDING;
        this.lastNumber = 0L;
        this.prefix = "";
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    @Transient
    public String getTypeLabel() {
        return DocumentNumbers.labelOf(documentType);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getPadding() {
        return padding == null ? DocumentNumbers.DEFAULT_PADDING : padding;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public long getLastNumber() {
        return lastNumber == null ? 0L : lastNumber;
    }

    public void setLastNumber(long lastNumber) {
        this.lastNumber = lastNumber;
    }

    @Transient
    public String getSampleLabel() {
        return format(getPrefix(), getPadding(), getLastNumber() + 1);
    }

    public static String format(String prefix, int padding, long number) {
        int pad = Math.max(1, Math.min(12, padding));
        String digits = String.format("%0" + pad + "d", number);
        if (prefix == null || prefix.isBlank()) {
            return digits;
        }
        return prefix + digits;
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

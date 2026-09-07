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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * A supplier's or customer's own item code/name for a material
 * ({@code malzeme_cari_kod}).
 */
@Entity
@Table(name = "malzeme_cari_kod", catalog = "terp", schema = "terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_mlz_cari_kod",
                    columnNames = {"mlz_ref", "rol", "cari_kodu", "cari_mlz_kodu"})
        })
public class ItemPartnerCode extends CommonFields {

    public static final int ROLE_SUPPLIER = 0;
    public static final int ROLE_CUSTOMER = 1;

    @Column(name = "mlz_ref", nullable = false)
    private Long itemRowId;

    @Column(name = "rol", nullable = false)
    private Integer role;

    @Column(name = "cari_kodu", nullable = false, length = 50)
    private String accountCode;

    @Column(name = "cari_unvan", length = 128)
    private String accountName;

    @Column(name = "cari_mlz_kodu", nullable = false, length = 50)
    private String partnerItemCode;

    @Column(name = "cari_mlz_adi", length = 128)
    private String partnerItemName;

    public Long getItemRowId() {
        return itemRowId;
    }

    public void setItemRowId(Long itemRowId) {
        this.itemRowId = itemRowId;
    }

    public int getRole() {
        return role == null ? ROLE_SUPPLIER : role;
    }

    public void setRole(int role) {
        this.role = role;
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

    public String getPartnerItemCode() {
        return partnerItemCode;
    }

    public void setPartnerItemCode(String partnerItemCode) {
        this.partnerItemCode = partnerItemCode;
    }

    public String getPartnerItemName() {
        return partnerItemName;
    }

    public void setPartnerItemName(String partnerItemName) {
        this.partnerItemName = partnerItemName;
    }
}

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
package com.terp.plugin.data.model;

import com.terp.plugin.data.ICommonFields;

/**
 * Current account (cari). One unique code is used as customer and/or
 * supplier code depending on the role flags.
 */
public interface IAccount extends ICommonFields {

    String getAccountCode();

    void setAccountCode(String accountCode);

    String getAccountName();

    void setAccountName(String accountName);

    String getLongName();

    void setLongName(String longName);

    int getCustomerFlag();

    void setCustomerFlag(int customerFlag);

    int getSupplierFlag();

    void setSupplierFlag(int supplierFlag);

    int getStatus();

    void setStatus(int status);

    String getTaxId();

    void setTaxId(String taxId);

    String getTaxOffice();

    void setTaxOffice(String taxOffice);

    String getAddress();

    void setAddress(String address);

    String getCountry();

    void setCountry(String country);

    String getCity();

    void setCity(String city);

    String getPhone();

    void setPhone(String phone);

    String getEmail();

    void setEmail(String email);

    String getNotes();

    void setNotes(String notes);

    default boolean isCustomer() {
        return getCustomerFlag() != 0;
    }

    default boolean isSupplier() {
        return getSupplierFlag() != 0;
    }

    default String getStatusLabel() {
        return getStatus() == 0 ? "Aktif" : "Pasif";
    }

    default String getRoleLabel() {
        boolean customer = isCustomer();
        boolean supplier = isSupplier();
        if (customer && supplier) {
            return "Müşteri / Tedarikçi";
        }
        if (customer) {
            return "Müşteri";
        }
        if (supplier) {
            return "Tedarikçi";
        }
        return "";
    }

    default String getDisplayLabel() {
        String code = getAccountCode() == null ? "" : getAccountCode();
        String name = getAccountName() == null ? "" : getAccountName();
        if (code.isBlank()) {
            return name;
        }
        if (name.isBlank()) {
            return code;
        }
        return code + " — " + name;
    }
}

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

import com.terp.plugin.data.CommonFields;
import com.terp.plugin.data.model.IAccount;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Current account master ({@code cari}). {@code cari_kodu} is the unique
 * customer and/or supplier code.
 */
@Entity
@Table(name = "cari", catalog = "terp", schema = "terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_cari", columnNames = "cari_kodu")
        })
public class Cari extends CommonFields implements Serializable, IAccount {

    @Column(name = "cari_kodu", nullable = false, length = 50)
    private String accountCode;

    @Column(name = "cari_unvan", nullable = false, length = 128)
    private String accountName;

    @Column(name = "uzun_unvan", length = 256)
    private String longName;

    @Column(name = "musteri")
    private Integer customerFlag;

    @Column(name = "tedarikci")
    private Integer supplierFlag;

    @Column(name = "durum")
    private Integer status;

    @Column(name = "vergi_no", length = 20)
    private String taxId;

    @Column(name = "vergi_dairesi", length = 64)
    private String taxOffice;

    @Column(name = "adres", length = 256)
    private String address;

    @Column(name = "ulke", length = 64)
    private String country;

    @Column(name = "sehir", length = 64)
    private String city;

    @Column(name = "telefon", length = 30)
    private String phone;

    @Column(name = "email", length = 128)
    private String email;

    @Column(name = "aciklama", length = 512)
    private String notes;

    public Cari() {
        this.customerFlag = 1;
        this.supplierFlag = 1;
        this.status = 0;
    }

    @Override
    public String getStatusLabel() {
        return getStatus() == 0 ? "Aktif" : "Pasif";
    }

    @Override
    public String getRoleLabel() {
        return IAccount.super.getRoleLabel();
    }

    @Override
    public String getAccountCode() {
        return accountCode;
    }

    @Override
    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    @Override
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @Override
    public String getLongName() {
        return longName;
    }

    @Override
    public void setLongName(String longName) {
        this.longName = longName;
    }

    @Override
    public int getCustomerFlag() {
        return customerFlag == null ? 0 : customerFlag;
    }

    @Override
    public void setCustomerFlag(int customerFlag) {
        this.customerFlag = customerFlag;
    }

    @Override
    public int getSupplierFlag() {
        return supplierFlag == null ? 0 : supplierFlag;
    }

    @Override
    public void setSupplierFlag(int supplierFlag) {
        this.supplierFlag = supplierFlag;
    }

    @Override
    public int getStatus() {
        return status == null ? 0 : status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String getTaxId() {
        return taxId;
    }

    @Override
    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    @Override
    public String getTaxOffice() {
        return taxOffice;
    }

    @Override
    public void setTaxOffice(String taxOffice) {
        this.taxOffice = taxOffice;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String getCountry() {
        return country;
    }

    @Override
    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String getCity() {
        return city;
    }

    @Override
    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    @Override
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public void setNotes(String notes) {
        this.notes = notes;
    }
}

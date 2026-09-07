/*
 * Copyright (C) 2026 LeanAcademy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.terp.stok.data;

import com.terp.plugin.data.CommonFields;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Material master ({@code malzeme}). Warehouse and movements live in
 * {@link Warehouse} / {@link StockMovement}.
 */
@Entity
@Table(name = "malzeme", catalog = "terp", schema = "terp",
        uniqueConstraints = {
            @UniqueConstraint(name = "ix_malzeme", columnNames = "mlz_kodu")
        })
public class Item extends CommonFields {

    public static final int TYPE_TRADE = 0;
    public static final int TYPE_RAW = 1;
    public static final int TYPE_SEMI = 2;
    public static final int TYPE_FINISHED = 3;
    public static final int TYPE_SERVICE = 4;
    public static final int TYPE_CONSUMABLE = 5;

    public static final String[] TYPE_LABELS = {
        "Ticari mal", "Hammadde", "Yarı mamul", "Mamul", "Hizmet", "Sarf"
    };

    @Column(name = "mlz_kodu", nullable = false, length = 50)
    private String itemId;

    @Column(name = "mlz_tanimi", nullable = false, length = 128)
    private String itemDesc;

    @Column(name = "mlz_uzun_tanim", length = 256)
    private String longDesc;

    @Column(name = "mlz_birimi", nullable = false, length = 20)
    private String itemUnit;

    @Column(name = "mlz_birim2", length = 20)
    private String secondUnit;

    @Column(name = "mlz_cevrim")
    private Double unitFactor;

    @Column(name = "mlz_turu")
    private Integer itemType;

    @Column(name = "mlz_grup", length = 50)
    private String itemGroup;

    @Column(name = "mlz_sinif", length = 50)
    private String itemClass;

    @Column(name = "mlz_marka", length = 50)
    private String brand;

    @Column(name = "mlz_barkod", length = 64)
    private String barcode;

    @Column(name = "mlz_gtin", length = 20)
    private String gtin;

    @Column(name = "durum")
    private Integer status;

    @Column(name = "mlz_kdv")
    private Double vatRate;

    @Column(name = "mlz_alis_fiyat")
    private Double purchasePrice;

    @Column(name = "mlz_satis_fiyat")
    private Double salesPrice;

    @Column(name = "mlz_para_birimi", length = 8)
    private String currency;

    @Column(name = "mlz_min_stok")
    private Double minStock;

    @Column(name = "mlz_max_stok")
    private Double maxStock;

    @Column(name = "mlz_emniyet")
    private Double safetyStock;

    @Column(name = "mlz_lot")
    private Integer lotTracked;

    @Column(name = "mlz_seri")
    private Integer serialTracked;

    @Column(name = "mlz_raf_omru")
    private Integer shelfLifeDays;

    @Column(name = "mlz_mensei", length = 50)
    private String originCountry;

    @Column(name = "mlz_agirlik")
    private Double weight;

    @Column(name = "mlz_hacim")
    private Double volume;

    @Column(name = "mlz_en")
    private Double width;

    @Column(name = "mlz_boy")
    private Double length;

    @Column(name = "mlz_yukseklik")
    private Double height;

    @Column(name = "mlz_hesap_stok", length = 30)
    private String stockAccount;

    @Column(name = "mlz_hesap_alis", length = 30)
    private String purchaseAccount;

    @Column(name = "mlz_hesap_satis", length = 30)
    private String salesAccount;

    @Column(name = "aciklama", length = 512)
    private String notes;

    public Item() {
        this.itemType = TYPE_TRADE;
        this.status = 0;
        this.currency = "TRY";
    }

    public String getTypeLabel() {
        int type = getItemType();
        if (type < 0 || type >= TYPE_LABELS.length) {
            return Integer.toString(type);
        }
        return TYPE_LABELS[type];
    }

    public String getStatusLabel() {
        return getStatus() == 0 ? "Aktif" : "Pasif";
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }

    public String getLongDesc() {
        return longDesc;
    }

    public void setLongDesc(String longDesc) {
        this.longDesc = longDesc;
    }

    public String getItemUnit() {
        return itemUnit;
    }

    public void setItemUnit(String itemUnit) {
        this.itemUnit = itemUnit;
    }

    public String getSecondUnit() {
        return secondUnit;
    }

    public void setSecondUnit(String secondUnit) {
        this.secondUnit = secondUnit;
    }

    public Double getUnitFactor() {
        return unitFactor;
    }

    public void setUnitFactor(Double unitFactor) {
        this.unitFactor = unitFactor;
    }

    public int getItemType() {
        return itemType == null ? TYPE_TRADE : itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public String getItemGroup() {
        return itemGroup;
    }

    public void setItemGroup(String itemGroup) {
        this.itemGroup = itemGroup;
    }

    public String getItemClass() {
        return itemClass;
    }

    public void setItemClass(String itemClass) {
        this.itemClass = itemClass;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getGtin() {
        return gtin;
    }

    public void setGtin(String gtin) {
        this.gtin = gtin;
    }

    public int getStatus() {
        return status == null ? 0 : status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Double getVatRate() {
        return vatRate;
    }

    public void setVatRate(Double vatRate) {
        this.vatRate = vatRate;
    }

    public Double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(Double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Double getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(Double salesPrice) {
        this.salesPrice = salesPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getMinStock() {
        return minStock;
    }

    public void setMinStock(Double minStock) {
        this.minStock = minStock;
    }

    public Double getMaxStock() {
        return maxStock;
    }

    public void setMaxStock(Double maxStock) {
        this.maxStock = maxStock;
    }

    public Double getSafetyStock() {
        return safetyStock;
    }

    public void setSafetyStock(Double safetyStock) {
        this.safetyStock = safetyStock;
    }

    public int getLotTracked() {
        return lotTracked == null ? 0 : lotTracked;
    }

    public void setLotTracked(int lotTracked) {
        this.lotTracked = lotTracked;
    }

    public int getSerialTracked() {
        return serialTracked == null ? 0 : serialTracked;
    }

    public void setSerialTracked(int serialTracked) {
        this.serialTracked = serialTracked;
    }

    public Integer getShelfLifeDays() {
        return shelfLifeDays;
    }

    public void setShelfLifeDays(Integer shelfLifeDays) {
        this.shelfLifeDays = shelfLifeDays;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public String getStockAccount() {
        return stockAccount;
    }

    public void setStockAccount(String stockAccount) {
        this.stockAccount = stockAccount;
    }

    public String getPurchaseAccount() {
        return purchaseAccount;
    }

    public void setPurchaseAccount(String purchaseAccount) {
        this.purchaseAccount = purchaseAccount;
    }

    public String getSalesAccount() {
        return salesAccount;
    }

    public void setSalesAccount(String salesAccount) {
        this.salesAccount = salesAccount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

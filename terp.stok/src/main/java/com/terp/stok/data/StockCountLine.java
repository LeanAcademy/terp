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
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "stok_sayim_satir", catalog = "terp", schema = "terp")
public class StockCountLine extends CommonFields implements Serializable {

    public static final double ZERO_EPS = 0.0000001d;

    @Column(name = "belge_ref", nullable = false)
    private Long countId;

    @Column(name = "satir_no")
    private Integer lineNo;

    @Column(name = "mlz_kodu", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "mlz_tanimi", length = 128)
    private String itemDesc;

    @Column(name = "mlz_birimi", length = 20)
    private String itemUnit;

    @Column(name = "eldeki_miktar")
    private Double onHandQty;

    @Column(name = "sayilan_miktar", nullable = false)
    private Double countedQty;

    public StockCountLine() {
        this.lineNo = 1;
        this.onHandQty = 0d;
        this.countedQty = 0d;
    }

    public Long getCountId() {
        return countId;
    }

    public void setCountId(Long countId) {
        this.countId = countId;
    }

    public int getLineNo() {
        return lineNo == null ? 1 : lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }

    public String getItemUnit() {
        return itemUnit;
    }

    public void setItemUnit(String itemUnit) {
        this.itemUnit = itemUnit;
    }

    public Double getOnHandQty() {
        return onHandQty;
    }

    public void setOnHandQty(Double onHandQty) {
        this.onHandQty = onHandQty;
    }

    public Double getCountedQty() {
        return countedQty;
    }

    public void setCountedQty(Double countedQty) {
        this.countedQty = countedQty;
    }

    @Transient
    public double getDifference() {
        double counted = countedQty == null ? 0d : countedQty;
        double onHand = onHandQty == null ? 0d : onHandQty;
        return counted - onHand;
    }

    @Transient
    public boolean hasDifference() {
        return Math.abs(getDifference()) > ZERO_EPS;
    }
}

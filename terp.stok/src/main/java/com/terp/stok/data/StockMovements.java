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

import com.terp.plugin.CompanyScope;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.model.IMovementReason;
import java.util.Date;
import java.util.List;

/**
 * Fold legacy hareket_turu values to giriş/çıkış and split transfer rows.
 */
public final class StockMovements {

    private StockMovements() {
    }

    public static void normalize(ICommonDao<StockMovement> dao) {
        if (dao == null || !CompanyScope.hasCompany()) {
            return;
        }
        List<StockMovement> rows = dao.findAll(CompanyScope.from("StockMovement"));
        if (rows == null) {
            return;
        }
        Date now = new Date();
        for (StockMovement row : rows) {
            if (row == null || row.getRowId() == null) {
                continue;
            }
            if (row.getMovementType() == StockMovement.TYPE_TRANSFER) {
                splitTransfer(dao, row, now);
                continue;
            }
            int next = StockMovement.normalizedType(row.getMovementType());
            boolean dirty = next != row.getMovementType();
            if (IMovementReason.CODE_TRANSFER_LEGACY.equals(row.getReasonCode())) {
                row.setReasonCode(IMovementReason.CODE_TRANSFER_OUT);
                if (row.getReasonName() == null || "Transfer".equals(row.getReasonName())) {
                    row.setReasonName("Transfer çıkışı");
                }
                dirty = true;
            }
            if (dirty) {
                row.setMovementType(next);
                row.setLastUpdateDate(now);
                dao.addOrUpdate(row);
            }
        }
    }

    private static void splitTransfer(ICommonDao<StockMovement> dao, StockMovement out, Date now) {
        String targetCode = out.getTargetWarehouseCode();
        String documentNo = out.getDocumentNo();
        if (documentNo == null || documentNo.isBlank()) {
            documentNo = "TR-" + out.getRowId();
            out.setDocumentNo(documentNo);
        }
        if (targetCode != null && !targetCode.isBlank()) {
            StockMovement in = dao.getEmpty();
            if (in != null) {
                CompanyScope.stamp(in);
                in.setAddedDate(out.getAddedDate() == null ? now : out.getAddedDate());
                in.setLastUpdateDate(now);
                in.setAddedByUserId(out.getAddedByUserId());
                in.setUpdatedByUserId(out.getUpdatedByUserId());
                in.setMovementDate(out.getMovementDate());
                in.setMovementType(StockMovement.TYPE_IN);
                in.setWarehouseCode(targetCode);
                in.setWarehouseName(out.getTargetWarehouseName());
                in.setItemCode(out.getItemCode());
                in.setItemDesc(out.getItemDesc());
                in.setItemUnit(out.getItemUnit());
                in.setQuantity(out.getQuantity());
                in.setUnitPrice(out.getUnitPrice());
                in.setAccountCode(out.getAccountCode());
                in.setAccountName(out.getAccountName());
                in.setDocumentNo(documentNo);
                in.setLotNo(out.getLotNo());
                in.setSerialNo(out.getSerialNo());
                in.setNotes(out.getNotes());
                in.setSourceType(out.getSourceType());
                in.setSourceId(out.getSourceId());
                in.setReasonCode(IMovementReason.CODE_TRANSFER_IN);
                in.setReasonName("Transfer girişi");
                dao.addOrUpdate(in);
            }
        }
        out.setMovementType(StockMovement.TYPE_OUT);
        out.setReasonCode(IMovementReason.CODE_TRANSFER_OUT);
        if (out.getReasonName() == null || "Transfer".equals(out.getReasonName())) {
            out.setReasonName("Transfer çıkışı");
        }
        out.setTargetWarehouseCode(null);
        out.setTargetWarehouseName(null);
        out.setLastUpdateDate(now);
        dao.addOrUpdate(out);
    }
}

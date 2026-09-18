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
import com.terp.plugin.IUser;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.IItemLookup;
import com.terp.plugin.data.IMovementReasonLookup;
import com.terp.plugin.data.IStockLedger;
import com.terp.plugin.data.IWarehouseLookup;
import com.terp.plugin.data.StockCard;
import com.terp.plugin.data.StockDirection;
import com.terp.plugin.data.StockPosting;
import com.terp.plugin.data.WorkingPeriod;
import com.terp.plugin.data.model.IMovementReason;
import com.terp.plugin.data.model.IStockItem;
import com.terp.plugin.data.model.IStockWarehouse;
import java.util.Date;
import java.util.List;

public class StockLedger implements IStockLedger {

    private final ICommonDao<StockMovement> movementDao;
    private final IItemLookup items;
    private final IWarehouseLookup warehouses;
    private final IMovementReasonLookup reasons;

    public StockLedger(ICommonDao<StockMovement> movementDao, IItemLookup items,
            IWarehouseLookup warehouses, IMovementReasonLookup reasons) {
        this.movementDao = movementDao;
        this.items = items;
        this.warehouses = warehouses;
        this.reasons = reasons;
    }

    @Override
    public void post(StockPosting posting) {
        validate(posting);
        StockMovement row = movementDao.getEmpty();
        if (row == null) {
            throw new IllegalStateException("Stok hareketi oluşturulamadı");
        }
        Date now = new Date();
        row.setAddedDate(now);
        row.setLastUpdateDate(now);
        if (!CompanyScope.stamp(row)) {
            throw new IllegalStateException("Firma seçilmedi");
        }
        row.setMovementDate(posting.getMovementDate() == null ? now : posting.getMovementDate());
        row.setMovementType(posting.getDirection());
        row.setWarehouseCode(posting.getWarehouseCode());
        row.setWarehouseName(firstNonBlank(posting.getWarehouseName(), warehouseName(posting.getWarehouseCode())));
        row.setTargetWarehouseCode(posting.getTargetWarehouseCode());
        row.setTargetWarehouseName(firstNonBlank(posting.getTargetWarehouseName(),
                warehouseName(posting.getTargetWarehouseCode())));
        row.setItemCode(posting.getItemCode());
        IStockItem item = items == null ? null : items.findByCode(posting.getItemCode());
        row.setItemDesc(firstNonBlank(posting.getItemDesc(), item == null ? null : item.getItemDesc()));
        String documentUnit = firstNonBlank(posting.getItemUnit(), item == null ? null : item.getItemUnit());
        double baseQty = StockCard.baseQuantity(item, documentUnit, posting.getQuantity());
        row.setItemUnit(item == null || item.getItemUnit() == null ? documentUnit : item.getItemUnit());
        row.setQuantity(baseQty);
        Double price = posting.getUnitPrice();
        if (price == null && StockDirection.isOut(posting.getDirection())) {
            List<StockMovement> history = movementDao.findAll(CompanyScope.from("StockMovement"));
            price = StockBalances.averageCost(history, posting.getWarehouseCode(), posting.getItemCode());
        }
        if (price == null) {
            price = StockCard.fillPrice(item, null, !StockDirection.isOut(posting.getDirection()));
        }
        row.setUnitPrice(price);
        row.setAccountCode(posting.getAccountCode());
        row.setAccountName(posting.getAccountName());
        row.setDocumentNo(posting.getDocumentNo());
        row.setLotNo(posting.getLotNo());
        row.setSerialNo(posting.getSerialNo());
        row.setExpiryDate(posting.getExpiryDate());
        if (row.getExpiryDate() == null && item != null && item.getShelfLifeDays() != null
                && item.getShelfLifeDays() > 0 && !StockDirection.isOut(posting.getDirection())) {
            Date start = row.getMovementDate() == null ? now : row.getMovementDate();
            row.setExpiryDate(new Date(start.getTime() + item.getShelfLifeDays() * 24L * 60L * 60L * 1000L));
        }
        row.setNotes(posting.getNotes());
        row.setSourceType(posting.getSourceType());
        row.setSourceId(posting.getSourceId());
        String reasonCode = posting.getReasonCode();
        IMovementReason reason = reasons == null || reasonCode == null ? null : reasons.findByCode(reasonCode);
        row.setReasonCode(reason == null ? reasonCode : reason.getReasonCode());
        row.setReasonName(reason == null ? null : reason.getReasonName());
        movementDao.addOrUpdate(row);
    }

    private void validate(StockPosting posting) {
        if (posting == null) {
            throw new IllegalArgumentException("Stok fişi boş");
        }
        if (posting.getWarehouseCode() == null || posting.getWarehouseCode().isBlank()) {
            throw new IllegalArgumentException("Depo zorunlu");
        }
        if (posting.getItemCode() == null || posting.getItemCode().isBlank()) {
            throw new IllegalArgumentException("Malzeme zorunlu");
        }
        if (posting.getQuantity() == null || posting.getQuantity() <= 0d) {
            throw new IllegalArgumentException("Miktar sıfırdan büyük olmalıdır");
        }
        String periodError = WorkingPeriod.rejectIfOutside(posting.getMovementDate());
        if (periodError != null) {
            throw new IllegalArgumentException(periodError);
        }
        IUser user = TerpApplication.getInstance().getUser();
        if (user != null && !user.canUseWarehouse(posting.getWarehouseCode())) {
            throw new IllegalArgumentException("Bu depo için yetkiniz yok: " + posting.getWarehouseCode());
        }
        IStockItem item = items == null ? null : items.findByCode(posting.getItemCode());
        String tracking = StockCard.trackingError(item, posting.getLotNo(), posting.getSerialNo());
        if (tracking != null) {
            throw new IllegalArgumentException(tracking);
        }
        posting.setDirection(StockDirection.of(posting.getDirection()));
        rejectIfInsufficient(posting, item);
    }

    private void rejectIfInsufficient(StockPosting posting, IStockItem item) {
        if (!StockDirection.isOut(posting.getDirection())) {
            return;
        }
        IStockWarehouse warehouse = warehouses == null ? null
                : warehouses.findByCode(posting.getWarehouseCode());
        if (warehouse != null && warehouse.allowsNegative()) {
            return;
        }
        List<StockMovement> rows = movementDao.findAll(CompanyScope.from("StockMovement"));
        double qty = StockCard.baseQuantity(item, posting.getItemUnit(), posting.getQuantity());
        double onHand = StockBalances.onHand(rows, posting.getWarehouseCode(), posting.getItemCode(), null);
        if (item != null && item.isLotTracked() && posting.getLotNo() != null && !posting.getLotNo().isBlank()) {
            double lotOnHand = StockBalances.onHandLot(rows, posting.getWarehouseCode(),
                    posting.getItemCode(), posting.getLotNo());
            if (lotOnHand - qty < -0.000001d) {
                throw new IllegalArgumentException(posting.getItemCode() + " lot " + posting.getLotNo()
                        + " bakiyesi " + formatQty(lotOnHand) + ".");
            }
        }
        if (onHand - qty < -0.000001d) {
            throw new IllegalArgumentException(
                    (posting.getItemCode() == null ? "" : posting.getItemCode() + " ")
                    + "için depodaki miktar " + formatQty(onHand)
                    + ". Negatif stoka izin verilmiyor.");
        }
    }

    private static String formatQty(double qty) {
        if (Math.abs(qty - Math.round(qty)) < 0.000001d) {
            return Long.toString(Math.round(qty));
        }
        return Double.toString(qty);
    }

    @Override
    public void reverseByDocument(String sourceType, Long sourceId) {
        if (sourceType == null || sourceType.isBlank() || sourceId == null) {
            return;
        }
        String escaped = sourceType.replace("'", "''");
        List<StockMovement> rows = movementDao.findAll(
                "from StockMovement e where " + CompanyScope.predicate("e")
                        + " and e.sourceType = '" + escaped + "' and e.sourceId = " + sourceId);
        if (rows == null) {
            return;
        }
        for (StockMovement row : rows) {
            if (row != null && row.getRowId() != null) {
                movementDao.delete(row.getRowId());
            }
        }
    }

    @Override
    public List<String> fefoLots(String warehouseCode, String itemCode) {
        List<String> labels = new java.util.ArrayList<>();
        List<StockMovement> rows = movementDao.findAll(CompanyScope.from("StockMovement"));
        for (StockBalances.LotBalance lot : StockBalances.fefoLots(rows, warehouseCode, itemCode)) {
            if (lot == null || lot.lotNo == null) {
                continue;
            }
            String skt = lot.expiryDate == null ? ""
                    : new java.text.SimpleDateFormat("yyyy-MM-dd").format(lot.expiryDate);
            labels.add(lot.lotNo + " — SKT " + (skt.isEmpty() ? "-" : skt) + " (" + formatQty(lot.quantity) + ")");
        }
        return labels;
    }

    private String warehouseName(String code) {
        if (warehouses == null || code == null) {
            return null;
        }
        IStockWarehouse warehouse = warehouses.findByCode(code);
        return warehouse == null ? null : warehouse.getWarehouseName();
    }

    private static String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        return fallback;
    }
}

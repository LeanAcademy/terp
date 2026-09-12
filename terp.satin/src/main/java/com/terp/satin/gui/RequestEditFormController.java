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
package com.terp.satin.gui;

import com.terp.plugin.CompanyScope;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.DocumentNumbers;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.IItemLookup;
import com.terp.plugin.data.IWarehouseLookup;
import com.terp.plugin.data.model.IStockItem;
import com.terp.plugin.data.model.IStockWarehouse;
import com.terp.plugin.gui.RecordAuditBar;
import com.terp.satin.data.PurchaseDocs;
import com.terp.satin.data.PurchaseOrder;
import com.terp.satin.data.PurchaseRequest;
import com.terp.satin.data.PurchaseRequestLine;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class RequestEditFormController implements Initializable {

    @FXML
    private Button btnSave;
    @FXML
    private Button btnApprove;
    @FXML
    private Button btnConvert;
    @FXML
    private Button btnCancelPosted;
    @FXML
    private Button btnClose;
    @FXML
    private TextField txtDocumentNo;
    @FXML
    private DatePicker dtDocumentDate;
    @FXML
    private ComboBox<String> cmbWarehouse;
    @FXML
    private TextArea txtNotes;
    @FXML
    private ComboBox<String> cmbItem;
    @FXML
    private TextField txtQuantity;
    @FXML
    private TextField txtUnit;
    @FXML
    private Button btnAddLine;
    @FXML
    private Button btnRemoveLine;
    @FXML
    private TableView<PurchaseRequestLine> tblvLines;
    @FXML
    private TableColumn<PurchaseRequestLine, String> tcItemCode;
    @FXML
    private TableColumn<PurchaseRequestLine, String> tcItemDesc;
    @FXML
    private TableColumn<PurchaseRequestLine, String> tcItemUnit;
    @FXML
    private TableColumn<PurchaseRequestLine, Double> tcQuantity;

    private ICommonDao<PurchaseRequest> requestDao;
    private ICommonDao<PurchaseRequestLine> lineDao;
    private ICommonDao<PurchaseOrder> orderDao;
    private PurchaseRequest currentRow;
    private final ObservableList<PurchaseRequestLine> lines = FXCollections.observableArrayList();
    private Runnable afterChange;
    private boolean notifying;
    private RecordAuditBar auditBar;

    public void embedInList(Runnable afterChange) {
        this.afterChange = afterChange;
        if (btnApprove != null) {
            btnApprove.setDefaultButton(false);
        }
    }

    public Long currentRowId() {
        return currentRow == null ? null : currentRow.getRowId();
    }

    public void initializeForm(PurchaseRequest row) {
        fillLookups();
        lines.clear();
        txtDocumentNo.clear();
        txtNotes.clear();
        txtQuantity.clear();
        txtUnit.clear();
        cmbItem.getSelectionModel().clearSelection();
        cmbWarehouse.getSelectionModel().clearSelection();
        dtDocumentDate.setValue(LocalDate.now());
        this.currentRow = row;
        if (row == null) {
            applyLock(PurchaseRequest.STATUS_DRAFT);
            showAudit();
            return;
        }
        txtDocumentNo.setText(SatinDates.empty(row.getDocumentNo()));
        dtDocumentDate.setValue(SatinDates.toLocalDate(row.getDocumentDate()));
        SatinCombos.select(cmbWarehouse, row.getWarehouseCode());
        txtNotes.setText(SatinDates.empty(row.getNotes()));
        if (row.getRowId() != null) {
            List<PurchaseRequestLine> stored = lineDao.findAll(
                    "from PurchaseRequestLine e where e.requestId = " + row.getRowId()
                            + " order by e.lineNo");
            if (stored != null) {
                lines.setAll(stored);
            }
        }
        applyLock(row.getStatus());
        showAudit();
    }

    @FXML
    private void onActionBtnSave(ActionEvent event) {
        if (saveDraft()) {
            notifyChanged();
        }
    }

    @FXML
    private void onActionBtnApprove(ActionEvent event) {
        if (!saveDraft()) {
            return;
        }
        currentRow.setStatus(PurchaseRequest.STATUS_APPROVED);
        currentRow.setLastUpdateDate(new Date());
        requestDao.addOrUpdate(currentRow);
        applyLock(currentRow.getStatus());
        showAudit();
        notifyChanged();
    }

    @FXML
    private void onActionBtnConvert(ActionEvent event) {
        if (currentRow == null || currentRow.getRowId() == null) {
            return;
        }
        SatinWindows.convertRequest(currentRow, requestDao, lineDao, orderDao, () -> {
            PurchaseRequest reloaded = requestDao.firstOrDefault(currentRow.getRowId());
            if (reloaded != null) {
                this.currentRow = reloaded;
                applyLock(reloaded.getStatus());
                showAudit();
            }
            notifyChanged();
        });
    }

    @FXML
    private void onActionBtnCancelPosted(ActionEvent event) {
        if (currentRow == null || currentRow.getRowId() == null || !currentRow.isApproved()) {
            return;
        }
        if (PurchaseDocs.hasOpenOrder(orderDao, currentRow.getRowId())) {
            SatinDates.showError("Satınalma talebi", "Siparişe bağlı",
                    "Önce bağlı satınalma siparişini iptal veya silin.");
            return;
        }
        currentRow.setStatus(PurchaseRequest.STATUS_CANCELLED);
        currentRow.setLastUpdateDate(new Date());
        requestDao.addOrUpdate(currentRow);
        close();
    }

    @FXML
    private void onActionBtnClose(ActionEvent event) {
        close();
    }

    @FXML
    private void onActionBtnAddLine(ActionEvent event) {
        String itemCode = SatinCombos.codeOf(cmbItem);
        Double quantity = SatinDates.parseDouble(txtQuantity.getText());
        if (itemCode == null || quantity == null || quantity <= 0d) {
            SatinDates.showError("Satınalma talebi", "Satır eksik", "Malzeme ve miktar zorunludur.");
            return;
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        PurchaseRequestLine line = new PurchaseRequestLine();
        line.setItemCode(itemCode);
        line.setItemDesc(item == null ? SatinCombos.nameOf(cmbItem) : item.getItemDesc());
        String unit = SatinDates.trimToNull(txtUnit.getText());
        line.setItemUnit(unit == null && item != null ? item.getItemUnit() : unit);
        line.setQuantity(quantity);
        line.setLineNo(lines.size() + 1);
        lines.add(line);
        txtQuantity.clear();
    }

    @FXML
    private void onActionBtnRemoveLine(ActionEvent event) {
        PurchaseRequestLine selected = tblvLines.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lines.remove(selected);
            int no = 1;
            for (PurchaseRequestLine line : lines) {
                line.setLineNo(no++);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.requestDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseRequest.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseRequestLine.class);
        this.orderDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseOrder.class);
        this.tcItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        this.tblvLines.setItems(lines);
        cmbItem.valueProperty().addListener((obs, old, value) -> applyItem(SatinCombos.codeOf(cmbItem)));
        this.auditBar = RecordAuditBar.install(txtDocumentNo);
        showAudit();
        Platform.runLater(() -> SatinEmbed.hideCloseIfDesktop(btnClose));
    }

    private void showAudit() {
        if (auditBar != null) {
            auditBar.bind(currentRow);
        }
    }

    private boolean saveDraft() {
        if (lines.isEmpty()) {
            SatinDates.showError("Satınalma talebi", "Satır yok", "En az bir malzeme satırı ekleyin.");
            return false;
        }
        Date now = new Date();
        PurchaseRequest row = this.currentRow;
        if (row == null) {
            row = requestDao.getEmpty();
            if (row == null) {
                SatinDates.showError("Satınalma talebi", "Kayıt hatası", "Belge oluşturulamadı.");
                return false;
            }
            row.setAddedDate(now);
            row.setStatus(PurchaseRequest.STATUS_DRAFT);
        }
        if (!row.isDraft()) {
            SatinDates.showError("Satınalma talebi", "Kilitli belge",
                    "Onaylı veya iptal talep taslak olarak kaydedilemez.");
            return false;
        }
        if (!CompanyScope.stamp(row)) {
            SatinDates.showError("Satınalma talebi", "Firma",
                    "Önce araç çubuğundan çalışma firmasını seçin.");
            return false;
        }
        String documentNo = SatinDates.assignDocumentNo(DocumentNumbers.PURCHASE_REQUEST, txtDocumentNo);
        if (documentNo == null || documentNo.isBlank()) {
            SatinDates.showError("Satınalma talebi", "Belge no",
                    "Belge no üretilemedi. Elle yazın veya Sistem yönetimi → Belge numaraları serisini kontrol edin.");
            return false;
        }
        row.setDocumentNo(documentNo);
        row.setDocumentDate(SatinDates.toDate(dtDocumentDate.getValue()));
        String warehouseCode = SatinCombos.codeOf(cmbWarehouse);
        row.setWarehouseCode(warehouseCode);
        IWarehouseLookup warehouses = TerpApplication.getInstance().getWarehouseLookup();
        IStockWarehouse warehouse = warehouses == null || warehouseCode == null
                ? null : warehouses.findByCode(warehouseCode);
        row.setWarehouseName(warehouse == null ? SatinCombos.nameOf(cmbWarehouse)
                : warehouse.getWarehouseName());
        row.setNotes(SatinDates.trimToNull(txtNotes.getText()));
        row.setLastUpdateDate(now);
        if (CompanyScope.rejectDuplicate(
                CompanyScope.findDuplicate(requestDao, "PurchaseRequest", "documentNo",
                        row.getDocumentNo(), row.getRowId()),
                "Belge numarası tekrar ediyor")) {
            return false;
        }
        PurchaseRequest saved = requestDao.addOrUpdate(row);
        if (saved == null || saved.getRowId() == null) {
            SatinDates.showError("Satınalma talebi", "Kayıt hatası", "Belge kaydedilemedi.");
            return false;
        }
        this.currentRow = saved;
        replaceLines(saved.getRowId(), now);
        showAudit();
        return true;
    }

    private void replaceLines(Long requestId, Date now) {
        PurchaseDocs.deleteByParent(lineDao, "PurchaseRequestLine", "requestId", requestId);
        int no = 1;
        List<PurchaseRequestLine> stored = new ArrayList<>();
        for (PurchaseRequestLine line : lines) {
            PurchaseRequestLine copy = lineDao.getEmpty();
            if (copy == null) {
                continue;
            }
            copy.setAddedDate(now);
            copy.setLastUpdateDate(now);
            copy.setRequestId(requestId);
            copy.setLineNo(no++);
            copy.setItemCode(line.getItemCode());
            copy.setItemDesc(line.getItemDesc());
            copy.setItemUnit(line.getItemUnit());
            copy.setQuantity(line.getQuantity());
            PurchaseRequestLine saved = lineDao.addOrUpdate(copy);
            stored.add(saved == null ? copy : saved);
        }
        lines.setAll(stored);
    }

    private void fillLookups() {
        List<String> warehouseLabels = new ArrayList<>();
        IWarehouseLookup warehouses = TerpApplication.getInstance().getWarehouseLookup();
        if (warehouses != null) {
            List<IStockWarehouse> rows = warehouses.findActive();
            if (rows != null) {
                for (IStockWarehouse warehouse : rows) {
                    warehouseLabels.add(warehouse.getDisplayLabel());
                }
            }
        }
        SatinCombos.fill(cmbWarehouse, warehouseLabels);

        List<String> itemLabels = new ArrayList<>();
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        if (items != null) {
            List<IStockItem> rows = items.findActive();
            if (rows != null) {
                for (IStockItem item : rows) {
                    itemLabels.add(item.getDisplayLabel());
                }
            }
        }
        SatinCombos.fill(cmbItem, itemLabels);
    }

    private void applyItem(String itemCode) {
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (item != null && (txtUnit.getText() == null || txtUnit.getText().isBlank())) {
            txtUnit.setText(SatinDates.empty(item.getItemUnit()));
        }
    }

    private void applyLock(int status) {
        boolean draft = status == PurchaseRequest.STATUS_DRAFT;
        boolean approved = status == PurchaseRequest.STATUS_APPROVED;
        txtDocumentNo.setDisable(!draft);
        dtDocumentDate.setDisable(!draft);
        cmbWarehouse.setDisable(!draft);
        txtNotes.setDisable(!draft);
        cmbItem.setDisable(!draft);
        txtQuantity.setDisable(!draft);
        txtUnit.setDisable(!draft);
        btnAddLine.setDisable(!draft);
        btnRemoveLine.setDisable(!draft);
        btnSave.setDisable(!draft);
        btnApprove.setDisable(!draft);
        btnConvert.setDisable(!approved);
        btnConvert.setVisible(approved || status == PurchaseRequest.STATUS_CONVERTED);
        btnConvert.setManaged(approved || status == PurchaseRequest.STATUS_CONVERTED);
        if (status == PurchaseRequest.STATUS_CONVERTED) {
            btnConvert.setDisable(true);
        }
        btnCancelPosted.setDisable(!approved);
        btnCancelPosted.setVisible(approved);
        btnCancelPosted.setManaged(approved);
    }

    private void close() {
        notifyChanged();
        SatinEmbed.closeIfModal(btnClose);
    }

    private void notifyChanged() {
        if (afterChange == null || notifying) {
            return;
        }
        notifying = true;
        try {
            afterChange.run();
        } finally {
            notifying = false;
        }
    }
}

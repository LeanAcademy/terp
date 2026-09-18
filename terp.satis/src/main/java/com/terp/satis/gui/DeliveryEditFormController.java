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
package com.terp.satis.gui;

import com.terp.plugin.CompanyScope;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.DocumentNumbers;
import com.terp.plugin.data.IAccountLookup;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.IItemLookup;
import com.terp.plugin.data.IStockLedger;
import com.terp.plugin.data.IWarehouseLookup;
import com.terp.plugin.data.StockCard;
import com.terp.plugin.data.StockDirection;
import com.terp.plugin.data.StockPosting;
import com.terp.plugin.data.WorkingPeriod;
import com.terp.plugin.gui.DocumentPrint;
import com.terp.plugin.data.model.IAccount;
import com.terp.plugin.data.model.IMovementReason;
import com.terp.plugin.data.model.IStockItem;
import com.terp.plugin.data.model.IStockWarehouse;
import com.terp.plugin.gui.RecordAuditBar;
import com.terp.satis.data.SalesDelivery;
import com.terp.satis.data.SalesDeliveryLine;
import com.terp.satis.data.SalesDocs;
import com.terp.satis.data.SalesInvoice;
import com.terp.satis.data.SalesInvoiceLine;
import com.terp.satis.data.SalesOrder;
import com.terp.satis.data.SalesOrderLine;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import com.terp.plugin.gui.NumericCells;

public class DeliveryEditFormController implements Initializable {

    @FXML
    private Button btnSave;
    @FXML
    private Button btnApprove;
    @FXML
    private Button btnInvoice;
    @FXML
    private Button btnCancelPosted;
    @FXML
    private Button btnClose;
    @FXML
    private TextField txtDocumentNo;
    @FXML
    private DatePicker dtDocumentDate;
    @FXML
    private DatePicker dtShipmentDate;
    @FXML
    private ComboBox<String> cmbAccount;
    @FXML
    private ComboBox<String> cmbWarehouse;
    @FXML
    private ComboBox<String> cmbSourceOrder;
    @FXML
    private Button btnLoadRemaining;
    @FXML
    private TextArea txtNotes;
    @FXML
    private ComboBox<String> cmbOrder;
    @FXML
    private ComboBox<String> cmbItem;
    @FXML
    private TextField txtQuantity;
    @FXML
    private TextField txtUnit;
    @FXML
    private TextField txtUnitPrice;
    @FXML
    private ComboBox<String> cmbLot;
    @FXML
    private TextField txtSerialNo;
    @FXML
    private DatePicker dtExpiry;
    @FXML
    private Label lblRemaining;
    @FXML
    private Button btnAddLine;
    @FXML
    private Button btnRemoveLine;
    @FXML
    private TableView<SalesDeliveryLine> tblvLines;
    @FXML
    private TableColumn<SalesDeliveryLine, String> tcSourceOrderNo;
    @FXML
    private TableColumn<SalesDeliveryLine, String> tcItemCode;
    @FXML
    private TableColumn<SalesDeliveryLine, String> tcItemDesc;
    @FXML
    private TableColumn<SalesDeliveryLine, String> tcItemUnit;
    @FXML
    private TableColumn<SalesDeliveryLine, Double> tcQuantity;
    @FXML
    private TableColumn<SalesDeliveryLine, Double> tcUnitPrice;
    @FXML
    private TableColumn<SalesDeliveryLine, String> tcLotNo;
    @FXML
    private Button btnPrint;

    private ICommonDao<SalesDelivery> deliveryDao;
    private ICommonDao<SalesDeliveryLine> lineDao;
    private ICommonDao<SalesOrder> orderDao;
    private ICommonDao<SalesOrderLine> orderLineDao;
    private ICommonDao<SalesInvoice> invoiceDao;
    private ICommonDao<SalesInvoiceLine> invoiceLineDao;
    private SalesDelivery currentRow;
    private final ObservableList<SalesDeliveryLine> lines = FXCollections.observableArrayList();
    private boolean applyingLookup;
    private Runnable afterChange;
    private boolean notifying;
    private RecordAuditBar auditBar;

    public void initializeForm(SalesDelivery row) {
        fillLookups();
        lines.clear();
        txtDocumentNo.clear();
        txtNotes.clear();
        txtQuantity.clear();
        txtUnit.clear();
        txtUnitPrice.clear();
        if (lblRemaining != null) {
            lblRemaining.setText("");
        }
        cmbItem.getSelectionModel().clearSelection();
        cmbAccount.getSelectionModel().clearSelection();
        cmbWarehouse.getSelectionModel().clearSelection();
        cmbSourceOrder.getSelectionModel().clearSelection();
        cmbOrder.getSelectionModel().clearSelection();
        dtDocumentDate.setValue(LocalDate.now());
        dtShipmentDate.setValue(LocalDate.now());
        this.currentRow = row;
        if (row == null) {
            applyLock(SalesDelivery.STATUS_DRAFT);
            showAudit();
            return;
        }
        txtDocumentNo.setText(SatisDates.empty(row.getDocumentNo()));
        dtDocumentDate.setValue(SatisDates.toLocalDate(row.getDocumentDate()));
        dtShipmentDate.setValue(SatisDates.toLocalDate(row.effectiveShipmentDate()));
        SatisCombos.select(cmbAccount, row.getAccountCode());
        SatisCombos.select(cmbWarehouse, row.getWarehouseCode());
        txtNotes.setText(SatisDates.empty(row.getNotes()));
        if (row.getRowId() != null) {
            List<SalesDeliveryLine> stored = lineDao.findAll(
                    "from SalesDeliveryLine e where e.deliveryId = " + row.getRowId()
                            + " order by e.lineNo");
            if (stored != null) {
                lines.setAll(stored);
            }
        }
        applyLock(row.getStatus());
        showAudit();
    }

    public void initializeFromOrder(SalesOrder order, List<SalesOrderLine> orderLines) {
        fillLookups();
        this.currentRow = deliveryDao.getEmpty();
        if (this.currentRow == null) {
            this.currentRow = new SalesDelivery();
        }
        this.currentRow.setStatus(SalesDelivery.STATUS_DRAFT);
        this.currentRow.setAccountCode(order.getAccountCode());
        this.currentRow.setAccountName(order.getAccountName());
        this.currentRow.setWarehouseCode(order.getWarehouseCode());
        this.currentRow.setWarehouseName(order.getWarehouseName());
        this.currentRow.setNotes(order.getNotes());
        dtDocumentDate.setValue(LocalDate.now());
        dtShipmentDate.setValue(LocalDate.now());
        txtDocumentNo.clear();
        SatisCombos.select(cmbAccount, order.getAccountCode());
        SatisCombos.select(cmbWarehouse, order.getWarehouseCode());
        SatisCombos.select(cmbSourceOrder, order.getDocumentNo());
        SatisCombos.select(cmbOrder, order.getDocumentNo());
        txtNotes.setText(SatisDates.empty(order.getNotes()));
        lines.clear();
        appendRemaining(order, orderLines);
        applyLock(SalesDelivery.STATUS_DRAFT);
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
        IStockLedger ledger = TerpApplication.getInstance().getStockLedger();
        if (ledger == null) {
            SatisDates.showError("Satış irsaliyesi", "Stok eklentisi yok",
                    "Onay için terp.stok yüklü olmalıdır.");
            return;
        }
        try {
            for (SalesDeliveryLine line : lines) {
                ledger.post(toPosting(line));
            }
            currentRow.setStatus(SalesDelivery.STATUS_POSTED);
            currentRow.setLastUpdateDate(new Date());
            deliveryDao.addOrUpdate(currentRow);
            SalesDocs.syncOrdersForLines(orderDao, orderLineDao, deliveryDao, lineDao,
                    currentRow.getSourceOrderId(), lines);
            close();
        } catch (RuntimeException ex) {
            ledger.reverseByDocument(StockPosting.SOURCE_SALES_DELIVERY, currentRow.getRowId());
            SatisDates.showError("Satış irsaliyesi", "Stok fişi yazılamadı",
                    ex.getMessage() == null ? "Onay başarısız." : ex.getMessage());
        }
    }

    @FXML
    private void onActionBtnInvoice(ActionEvent event) {
        if (currentRow == null || currentRow.getRowId() == null) {
            return;
        }
        SatisWindows.convertDelivery(currentRow, deliveryDao, lineDao, invoiceDao, () -> {
            SalesDelivery reloaded = deliveryDao.firstOrDefault(currentRow.getRowId());
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
        if (currentRow == null || currentRow.getRowId() == null || !currentRow.isPosted()) {
            return;
        }
        if (currentRow.isInvoiced()
                || SalesDocs.hasLinkedInvoice(invoiceDao, invoiceLineDao, lineDao, currentRow.getRowId())) {
            SatisDates.showError("Satış irsaliyesi", "Faturaya bağlı",
                    "Önce bağlı satış faturasını iptal veya silin.");
            return;
        }
        IStockLedger ledger = TerpApplication.getInstance().getStockLedger();
        if (ledger == null) {
            SatisDates.showError("Satış irsaliyesi", "Stok eklentisi yok",
                    "İptal için terp.stok yüklü olmalıdır.");
            return;
        }
        ledger.reverseByDocument(StockPosting.SOURCE_SALES_DELIVERY, currentRow.getRowId());
        Long orderId = currentRow.getSourceOrderId();
        currentRow.setStatus(SalesDelivery.STATUS_CANCELLED);
        currentRow.setLastUpdateDate(new Date());
        deliveryDao.addOrUpdate(currentRow);
        SalesDocs.syncOrdersForLines(orderDao, orderLineDao, deliveryDao, lineDao, orderId, lines);
        close();
    }

    @FXML
    private void onActionBtnPrint(ActionEvent event) {
        if (currentRow == null) {
            return;
        }
        StringBuilder linesText = new StringBuilder();
        for (SalesDeliveryLine line : lines) {
            if (line == null) {
                continue;
            }
            linesText.append(line.getItemCode()).append("  ").append(line.getItemDesc())
                    .append("  ").append(line.getQuantity()).append(" ")
                    .append(line.getItemUnit() == null ? "" : line.getItemUnit()).append('\n');
        }
        java.util.Map<String, String> values = new java.util.LinkedHashMap<>();
        values.put("DOCUMENT_NO", SatisDates.empty(currentRow.getDocumentNo()));
        values.put("ACCOUNT", SatisDates.empty(currentRow.getAccountName()));
        values.put("DATE", currentRow.getDateLabel());
        values.put("SHIP_DATE", currentRow.getShipmentDateLabel());
        values.put("LINES", linesText.toString());
        javafx.stage.Window owner = btnPrint.getScene() == null ? null : btnPrint.getScene().getWindow();
        DocumentPrint.print(owner, "SATIS_IRSALIYE", "İrsaliye " + SatisDates.empty(currentRow.getDocumentNo()),
                "İrsaliye {DOCUMENT_NO}\nCari: {ACCOUNT}\nOluşturma: {DATE}\nSevk: {SHIP_DATE}\n\n{LINES}", values);
    }

    @FXML
    private void onActionBtnClose(ActionEvent event) {
        close();
    }

    @FXML
    private void onActionBtnLoadRemaining(ActionEvent event) {
        SalesOrder order = resolveOrder(SatisCombos.codeOf(cmbSourceOrder));
        if (!usableOrder(order)) {
            return;
        }
        applyOrderHeader(order);
        appendRemaining(order, SalesDocs.remainingLines(order.getRowId(), orderLineDao, deliveryDao,
                lineDao, currentRow == null ? null : currentRow.getRowId()));
        refreshRemainingHint();
    }

    @FXML
    private void onActionBtnAddLine(ActionEvent event) {
        SalesDeliveryLine selected = tblvLines.getSelectionModel().getSelectedItem();
        String orderNo = SatisCombos.codeOf(cmbOrder);
        String itemCode = SatisCombos.codeOf(cmbItem);
        Double quantity = SatisDates.parseDouble(txtQuantity.getText());
        Double price = SatisDates.parseDouble(txtUnitPrice.getText());
        if (itemCode == null || quantity == null || quantity <= 0d) {
            SatisDates.showError("Satış irsaliyesi", "Satır eksik", "Malzeme ve miktar zorunludur.");
            return;
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (SatisDates.rejectIfNotSellable("Satış irsaliyesi", item)) {
            return;
        }
        String lotNo = SatisCombos.codeOf(cmbLot);
        String tracking = StockCard.trackingError(item, lotNo, SatisDates.trimToNull(txtSerialNo.getText()));
        if (tracking != null) {
            SatisDates.showError("Satış irsaliyesi", "İzleme", tracking);
            return;
        }
        SalesOrder order = null;
        SalesOrderLine orderLine = null;
        if (orderNo != null) {
            order = resolveOrder(orderNo);
            boolean sameSource = selected != null && order != null
                    && order.getRowId() != null && order.getRowId().equals(selected.getSourceOrderId());
            if (!sameSource && !usableOrder(order)) {
                return;
            }
            orderLine = SalesDocs.findOrderLine(orderLineDao, order.getRowId(), itemCode);
            if (orderLine == null) {
                SatisDates.showError("Satış irsaliyesi", "Sipariş satırı yok",
                        itemCode + " bu siparişte yok: " + order.getDocumentNo());
                return;
            }
            String error = validateQty(orderLine, quantity, selected);
            if (error != null) {
                SatisDates.showError("Satış irsaliyesi", "Miktar", error);
                return;
            }
        }
        SalesDeliveryLine line = selected != null ? selected : new SalesDeliveryLine();
        line.setItemCode(itemCode);
        line.setItemDesc(item == null ? SatisCombos.nameOf(cmbItem) : item.getItemDesc());
        String unit = SatisDates.trimToNull(txtUnit.getText());
        line.setItemUnit(unit == null && item != null ? item.getItemUnit() : unit);
        line.setQuantity(quantity);
        line.setUnitPrice(StockCard.fillPrice(item, price, false));
        line.setLotNo(lotNo);
        line.setSerialNo(SatisDates.trimToNull(txtSerialNo.getText()));
        line.setExpiryDate(dtExpiry.getValue() == null ? null : SatisDates.toDate(dtExpiry.getValue()));
        if (order != null) {
            line.setSourceOrderId(order.getRowId());
            line.setSourceOrderNo(order.getDocumentNo());
            line.setSourceOrderLineId(orderLine.getRowId());
            if (line.getItemUnit() == null) {
                line.setItemUnit(orderLine.getItemUnit());
            }
            if (line.getUnitPrice() == null) {
                line.setUnitPrice(orderLine.getUnitPrice());
            }
            applyOrderHeader(order);
        } else {
            line.setSourceOrderId(null);
            line.setSourceOrderNo(null);
            line.setSourceOrderLineId(null);
        }
        if (selected == null) {
            line.setLineNo(lines.size() + 1);
            lines.add(line);
        } else {
            tblvLines.refresh();
        }
        txtQuantity.clear();
        txtUnitPrice.clear();
        cmbLot.getSelectionModel().clearSelection();
        txtSerialNo.clear();
        dtExpiry.setValue(null);
        tblvLines.getSelectionModel().clearSelection();
        refreshRemainingHint();
    }

    @FXML
    private void onActionBtnRemoveLine(ActionEvent event) {
        SalesDeliveryLine selected = tblvLines.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lines.remove(selected);
            int no = 1;
            for (SalesDeliveryLine line : lines) {
                line.setLineNo(no++);
            }
            refreshRemainingHint();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.deliveryDao = TerpApplication.getInstance().getPersistence().createDao(SalesDelivery.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(SalesDeliveryLine.class);
        this.orderDao = TerpApplication.getInstance().getPersistence().createDao(SalesOrder.class);
        this.orderLineDao = TerpApplication.getInstance().getPersistence().createDao(SalesOrderLine.class);
        this.invoiceDao = TerpApplication.getInstance().getPersistence().createDao(SalesInvoice.class);
        this.invoiceLineDao = TerpApplication.getInstance().getPersistence().createDao(SalesInvoiceLine.class);
        this.tcSourceOrderNo.setCellValueFactory(new PropertyValueFactory<>("sourceOrderNo"));
        this.tcItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        NumericCells.apply(this.tcQuantity);
        this.tcUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        NumericCells.apply(this.tcUnitPrice);
        this.tcLotNo.setCellValueFactory(new PropertyValueFactory<>("lotNo"));
        NumericCells.applyEditable(this.tcQuantity);
        this.tcQuantity.setOnEditCommit(event -> {
            SalesDeliveryLine line = event.getRowValue();
            Double value = event.getNewValue();
            if (line == null) {
                return;
            }
            if (value == null || value <= 0d) {
                SatisDates.showError("Satış irsaliyesi", "Miktar", "Miktar sıfırdan büyük olmalıdır.");
                tblvLines.refresh();
                return;
            }
            if (line.getSourceOrderLineId() != null) {
                SalesOrderLine orderLine = orderLineDao.firstOrDefault(line.getSourceOrderLineId());
                String error = validateQty(orderLine, value, line);
                if (error != null) {
                    SatisDates.showError("Satış irsaliyesi", "Miktar", error);
                    tblvLines.refresh();
                    return;
                }
            }
            line.setQuantity(value);
            refreshRemainingHint();
        });
        this.tblvLines.setItems(lines);
        this.tblvLines.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                loadLineEditor(selected);
            }
        });
        cmbItem.valueProperty().addListener((obs, old, value) -> {
            applyItem(SatisCombos.codeOf(cmbItem));
            applyOrderDefaults();
        });
        cmbWarehouse.valueProperty().addListener((obs, old, value) -> fillFefoLots());
        cmbOrder.valueProperty().addListener((obs, old, value) -> applyOrderDefaults());
        fillLookups();
        if (dtDocumentDate.getValue() == null) {
            dtDocumentDate.setValue(LocalDate.now());
        }
        if (dtShipmentDate.getValue() == null) {
            dtShipmentDate.setValue(dtDocumentDate.getValue());
        }
        applyLock(SalesDelivery.STATUS_DRAFT);
        this.auditBar = RecordAuditBar.install(txtDocumentNo);
        showAudit();
        Platform.runLater(() -> SatisEmbed.hideCloseIfDesktop(btnClose));
    }

    private void showAudit() {
        if (auditBar != null) {
            auditBar.bind(currentRow);
        }
    }

    private boolean saveDraft() {
        String accountCode = SatisCombos.codeOf(cmbAccount);
        if (accountCode == null) {
            SatisDates.showError("Satış irsaliyesi", "Müşteri eksik", "Müşteri seçin.");
            return false;
        }
        String warehouseCode = SatisCombos.codeOf(cmbWarehouse);
        if (warehouseCode == null) {
            SatisDates.showError("Satış irsaliyesi", "Depo eksik", "Depo seçin.");
            return false;
        }
        String periodError = WorkingPeriod.rejectIfOutside(SatisDates.toDate(dtDocumentDate.getValue()));
        if (periodError == null) {
            periodError = WorkingPeriod.rejectIfOutside(SatisDates.toDate(dtShipmentDate.getValue()));
        }
        if (periodError != null) {
            SatisDates.showError("Satış irsaliyesi", "Dönem", periodError);
            return false;
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        for (SalesDeliveryLine line : lines) {
            IStockItem item = items == null || line == null ? null : items.findByCode(line.getItemCode());
            String tracking = StockCard.trackingError(item, line == null ? null : line.getLotNo(),
                    line == null ? null : line.getSerialNo());
            if (tracking != null) {
                SatisDates.showError("Satış irsaliyesi", "İzleme", tracking);
                return false;
            }
        }
        if (lines.isEmpty()) {
            SatisDates.showError("Satış irsaliyesi", "Satır yok", "En az bir malzeme satırı ekleyin.");
            return false;
        }
        String qtyError = validateAllLines();
        if (qtyError != null) {
            SatisDates.showError("Satış irsaliyesi", "Miktar", qtyError);
            return false;
        }
        IItemLookup itemLookup = TerpApplication.getInstance().getItemLookup();
        for (SalesDeliveryLine line : lines) {
            if (line == null) {
                continue;
            }
            IStockItem item = itemLookup == null ? null : itemLookup.findByCode(line.getItemCode());
            if (SatisDates.rejectIfNotSellable("Satış irsaliyesi", item)) {
                return false;
            }
        }
        Date now = new Date();
        SalesDelivery row = this.currentRow;
        if (row == null) {
            row = deliveryDao.getEmpty();
            if (row == null) {
                SatisDates.showError("Satış irsaliyesi", "Kayıt hatası", "Belge oluşturulamadı.");
                return false;
            }
            row.setAddedDate(now);
            row.setStatus(SalesDelivery.STATUS_DRAFT);
        }
        if (!row.isDraft() && !row.isCancelled()) {
            SatisDates.showError("Satış irsaliyesi", "Kilitli belge",
                    "Onaylı belge taslak olarak kaydedilemez.");
            return false;
        }
        if (!CompanyScope.stamp(row)) {
            SatisDates.showError("Satış irsaliyesi", "Firma",
                    "Önce araç çubuğundan çalışma firmasını seçin.");
            return false;
        }
        stampHeaderOrder(row);
        String documentNo = SatisDates.assignDocumentNo(DocumentNumbers.SALES_DELIVERY, txtDocumentNo);
        if (documentNo == null || documentNo.isBlank()) {
            SatisDates.showError("Satış irsaliyesi", "Belge no",
                    "Belge no üretilemedi. Elle yazın veya Sistem yönetimi → Belge numaraları serisini kontrol edin.");
            return false;
        }
        row.setDocumentNo(documentNo);
        row.setDocumentDate(SatisDates.toDate(dtDocumentDate.getValue()));
        Date shipmentDate = SatisDates.toDate(dtShipmentDate.getValue());
        if (shipmentDate == null) {
            shipmentDate = row.getDocumentDate();
        }
        row.setShipmentDate(shipmentDate);
        row.setAccountCode(accountCode);
        IAccountLookup accounts = TerpApplication.getInstance().getAccountLookup();
        IAccount account = accounts == null ? null : accounts.findByCode(accountCode);
        row.setAccountName(account == null ? SatisCombos.nameOf(cmbAccount) : account.getAccountName());
        row.setWarehouseCode(warehouseCode);
        IWarehouseLookup warehouses = TerpApplication.getInstance().getWarehouseLookup();
        IStockWarehouse warehouse = warehouses == null ? null : warehouses.findByCode(warehouseCode);
        row.setWarehouseName(warehouse == null ? SatisCombos.nameOf(cmbWarehouse) : warehouse.getWarehouseName());
        row.setNotes(SatisDates.trimToNull(txtNotes.getText()));
        row.setLastUpdateDate(now);
        if (row.getAddedDate() == null) {
            row.setAddedDate(now);
        }
        if (CompanyScope.rejectDuplicate(
                CompanyScope.findDuplicate(deliveryDao, "SalesDelivery", "documentNo",
                        row.getDocumentNo(), row.getRowId()),
                "Belge numarası tekrar ediyor")) {
            return false;
        }
        SalesDelivery saved = deliveryDao.addOrUpdate(row);
        if (saved == null || saved.getRowId() == null) {
            SatisDates.showError("Satış irsaliyesi", "Kayıt hatası", "Belge kaydedilemedi.");
            return false;
        }
        this.currentRow = saved;
        replaceLines(saved.getRowId(), now);
        SalesDocs.syncOrdersForLines(orderDao, orderLineDao, deliveryDao, lineDao,
                saved.getSourceOrderId(), lines);
        showAudit();
        return true;
    }

    private void replaceLines(Long deliveryId, Date now) {
        SalesDocs.deleteByParent(lineDao, "SalesDeliveryLine", "deliveryId", deliveryId);
        int no = 1;
        List<SalesDeliveryLine> stored = new ArrayList<>();
        for (SalesDeliveryLine line : lines) {
            SalesDeliveryLine copy = lineDao.getEmpty();
            if (copy == null) {
                continue;
            }
            copy.setAddedDate(now);
            copy.setLastUpdateDate(now);
            copy.setDeliveryId(deliveryId);
            copy.setLineNo(no++);
            copy.setItemCode(line.getItemCode());
            copy.setItemDesc(line.getItemDesc());
            copy.setItemUnit(line.getItemUnit());
            copy.setQuantity(line.getQuantity());
            copy.setUnitPrice(line.getUnitPrice());
            copy.setLotNo(line.getLotNo());
            copy.setSerialNo(line.getSerialNo());
            copy.setExpiryDate(line.getExpiryDate());
            copy.setSourceOrderLineId(line.getSourceOrderLineId());
            copy.setSourceOrderId(line.getSourceOrderId());
            copy.setSourceOrderNo(line.getSourceOrderNo());
            SalesDeliveryLine saved = lineDao.addOrUpdate(copy);
            stored.add(saved == null ? copy : saved);
        }
        lines.setAll(stored);
    }

    private StockPosting toPosting(SalesDeliveryLine line) {
        StockPosting posting = new StockPosting();
        posting.setMovementDate(currentRow.effectiveShipmentDate());
        posting.setDirection(StockDirection.OUT);
        posting.setReasonCode(IMovementReason.CODE_SALES);
        posting.setWarehouseCode(currentRow.getWarehouseCode());
        posting.setWarehouseName(currentRow.getWarehouseName());
        posting.setItemCode(line.getItemCode());
        posting.setItemDesc(line.getItemDesc());
        posting.setItemUnit(line.getItemUnit());
        posting.setQuantity(line.getQuantity());
        posting.setUnitPrice(line.getUnitPrice());
        posting.setLotNo(line.getLotNo());
        posting.setSerialNo(line.getSerialNo());
        posting.setExpiryDate(line.getExpiryDate());
        posting.setAccountCode(currentRow.getAccountCode());
        posting.setAccountName(currentRow.getAccountName());
        posting.setDocumentNo(currentRow.getDocumentNo());
        posting.setSourceType(StockPosting.SOURCE_SALES_DELIVERY);
        posting.setSourceId(currentRow.getRowId());
        return posting;
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
        SatisCombos.fill(cmbWarehouse, warehouseLabels);

        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        if (items != null) {
            SatisCombos.fillSellableItems(cmbItem, items.findActive());
        } else {
            SatisCombos.fill(cmbItem, List.of());
        }

        List<String> accountLabels = new ArrayList<>();
        IAccountLookup accounts = TerpApplication.getInstance().getAccountLookup();
        if (accounts != null) {
            List<IAccount> rows = accounts.findActiveCustomers();
            if (rows != null) {
                for (IAccount account : rows) {
                    accountLabels.add(account.getDisplayLabel());
                }
            }
        }
        SatisCombos.fill(cmbAccount, accountLabels);

        List<String> orderLabels = new ArrayList<>();
        List<SalesOrder> orders = SalesDocs.deliverableOrders(orderDao, orderLineDao, deliveryDao, lineDao);
        for (SalesOrder order : orders) {
            String name = order.getAccountName() == null ? "" : order.getAccountName();
            orderLabels.add(name.isBlank() ? order.getDocumentNo() : order.getDocumentNo() + " — " + name);
        }
        SatisCombos.fill(cmbSourceOrder, orderLabels);
        SatisCombos.fill(cmbOrder, orderLabels);
    }

    private void loadLineEditor(SalesDeliveryLine line) {
        applyingLookup = true;
        try {
            SatisCombos.select(cmbOrder, line.getSourceOrderNo());
            SatisCombos.select(cmbItem, line.getItemCode());
            txtUnit.setText(SatisDates.empty(line.getItemUnit()));
            txtQuantity.setText(NumericCells.text(line.getQuantity()));
            txtUnitPrice.setText(NumericCells.text(line.getUnitPrice()));
            fillFefoLots();
            SatisCombos.select(cmbLot, line.getLotNo());
            txtSerialNo.setText(SatisDates.empty(line.getSerialNo()));
            dtExpiry.setValue(line.getExpiryDate() == null ? null : SatisDates.toLocalDate(line.getExpiryDate()));
            refreshRemainingHint();
        } finally {
            applyingLookup = false;
        }
    }

    private void applyItem(String itemCode) {
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (item != null && (txtUnit.getText() == null || txtUnit.getText().isBlank())) {
            txtUnit.setText(SatisDates.empty(item.getItemUnit()));
        }
        if (item != null && (txtUnitPrice.getText() == null || txtUnitPrice.getText().isBlank())
                && item.getSalesPrice() != null) {
            txtUnitPrice.setText(NumericCells.text(item.getSalesPrice()));
        }
        fillFefoLots();
    }

    private void fillFefoLots() {
        if (cmbLot == null) {
            return;
        }
        String current = SatisCombos.codeOf(cmbLot);
        List<String> labels = new ArrayList<>();
        IStockLedger ledger = TerpApplication.getInstance().getStockLedger();
        if (ledger != null) {
            List<String> lots = ledger.fefoLots(SatisCombos.codeOf(cmbWarehouse), SatisCombos.codeOf(cmbItem));
            if (lots != null) {
                labels.addAll(lots);
            }
        }
        SatisCombos.fill(cmbLot, labels);
        SatisCombos.select(cmbLot, current);
    }

    private void applyOrderDefaults() {
        if (applyingLookup) {
            return;
        }
        applyingLookup = true;
        try {
            SalesOrder order = resolveOrder(SatisCombos.codeOf(cmbOrder));
            if (order != null && !order.canDeliver()) {
                order = null;
            }
            String itemCode = SatisCombos.codeOf(cmbItem);
            if (order != null) {
                applyOrderHeader(order);
            }
            if (order != null && itemCode != null) {
                SalesOrderLine orderLine = SalesDocs.findOrderLine(orderLineDao, order.getRowId(), itemCode);
                if (orderLine != null) {
                    double allocated = SalesDocs.allocatedQuantity(deliveryDao, lineDao, orderLine.getRowId(),
                            currentRow == null ? null : currentRow.getRowId());
                    allocated += qtyOnThisDocument(orderLine.getRowId(), null);
                    double remaining = SalesDocs.remainingOrdered(
                            orderLine.getQuantity() == null ? 0d : orderLine.getQuantity(), allocated);
                    if (txtQuantity.getText() == null || txtQuantity.getText().isBlank()) {
                        if (remaining > 0d) {
                            txtQuantity.setText(NumericCells.format(remaining));
                        }
                    }
                    if (txtUnit.getText() == null || txtUnit.getText().isBlank()) {
                        txtUnit.setText(SatisDates.empty(orderLine.getItemUnit()));
                    }
                    if (txtUnitPrice.getText() == null || txtUnitPrice.getText().isBlank()) {
                        txtUnitPrice.setText(NumericCells.text(orderLine.getUnitPrice()));
                    }
                }
            }
            refreshRemainingHint();
        } finally {
            applyingLookup = false;
        }
    }

    private void applyOrderHeader(SalesOrder order) {
        if (order == null) {
            return;
        }
        if (SatisCombos.codeOf(cmbAccount) == null) {
            SatisCombos.select(cmbAccount, order.getAccountCode());
        }
        if (SatisCombos.codeOf(cmbWarehouse) == null) {
            SatisCombos.select(cmbWarehouse, order.getWarehouseCode());
        }
    }

    private void appendRemaining(SalesOrder order, List<SalesOrderLine> orderLines) {
        if (order == null || orderLines == null) {
            return;
        }
        int no = lines.size() + 1;
        Long exclude = currentRow == null ? null : currentRow.getRowId();
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        for (SalesOrderLine source : orderLines) {
            if (source == null || hasOrderLine(source.getRowId())) {
                continue;
            }
            IStockItem item = items == null ? null : items.findByCode(source.getItemCode());
            if (item != null && !item.isSellable()) {
                continue;
            }
            double allocated = SalesDocs.allocatedQuantity(deliveryDao, lineDao, source.getRowId(), exclude);
            allocated += qtyOnThisDocument(source.getRowId(), null);
            double remaining = SalesDocs.remainingOrdered(
                    source.getQuantity() == null ? 0d : source.getQuantity(), allocated);
            if (remaining <= 0d) {
                continue;
            }
            SalesDeliveryLine line = new SalesDeliveryLine();
            line.setLineNo(no++);
            line.setItemCode(source.getItemCode());
            line.setItemDesc(source.getItemDesc());
            line.setItemUnit(source.getItemUnit());
            line.setQuantity(remaining);
            line.setUnitPrice(source.getUnitPrice());
            line.setSourceOrderLineId(source.getRowId());
            line.setSourceOrderId(order.getRowId());
            line.setSourceOrderNo(order.getDocumentNo());
            lines.add(line);
        }
    }

    private String validateAllLines() {
        for (SalesDeliveryLine line : lines) {
            if (line == null || line.getQuantity() == null || line.getQuantity() <= 0d) {
                return "Miktar sıfırdan büyük olmalıdır.";
            }
            if (line.getSourceOrderLineId() == null) {
                continue;
            }
            SalesOrderLine orderLine = orderLineDao.firstOrDefault(line.getSourceOrderLineId());
            String error = validateQty(orderLine, line.getQuantity(), line);
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    private String validateQty(SalesOrderLine orderLine, Double quantity, SalesDeliveryLine current) {
        if (orderLine == null) {
            return "Sipariş satırı bulunamadı.";
        }
        if (quantity == null || quantity <= 0d) {
            return "Miktar sıfırdan büyük olmalıdır.";
        }
        double ordered = orderLine.getQuantity() == null ? 0d : orderLine.getQuantity();
        double allocated = SalesDocs.allocatedQuantity(deliveryDao, lineDao, orderLine.getRowId(),
                currentRow == null ? null : currentRow.getRowId());
        allocated += qtyOnThisDocument(orderLine.getRowId(), current);
        double remaining = SalesDocs.remainingOrdered(ordered, allocated);
        if (quantity > remaining + 0.0000001d) {
            return orderLine.getItemCode() + " için kalan " + remaining;
        }
        return null;
    }

    private double qtyOnThisDocument(Long orderLineId, SalesDeliveryLine skip) {
        double sum = 0d;
        for (SalesDeliveryLine line : lines) {
            if (line == skip || line == null || orderLineId == null) {
                continue;
            }
            if (orderLineId.equals(line.getSourceOrderLineId())) {
                sum += line.getQuantity() == null ? 0d : line.getQuantity();
            }
        }
        return sum;
    }

    private void stampHeaderOrder(SalesDelivery row) {
        LinkedHashSet<String> numbers = new LinkedHashSet<>();
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (SalesDeliveryLine line : lines) {
            if (line == null) {
                continue;
            }
            if (line.getSourceOrderNo() != null && !line.getSourceOrderNo().isBlank()) {
                numbers.add(line.getSourceOrderNo().trim());
            }
            if (line.getSourceOrderId() != null) {
                ids.add(line.getSourceOrderId());
            }
        }
        row.setSourceOrderNo(joinOrderNos(numbers));
        row.setSourceOrderId(ids.size() == 1 ? ids.iterator().next() : null);
    }

    private static String joinOrderNos(LinkedHashSet<String> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        for (String no : numbers) {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(no);
        }
        if (text.length() <= 128) {
            return text.toString();
        }
        return text.substring(0, 125) + "...";
    }

    private SalesOrder resolveOrder(String documentNo) {
        return SalesDocs.findOrderByNo(orderDao, documentNo);
    }

    private boolean usableOrder(SalesOrder order) {
        if (order == null) {
            SatisDates.showError("Satış irsaliyesi", "Sipariş bulunamadı", "Sipariş numarasını kontrol edin.");
            return false;
        }
        if (!order.canDeliver()) {
            SatisDates.showError("Satış irsaliyesi", "Sipariş uygun değil",
                    "Taslak veya iptal siparişten irsaliye yapılamaz.");
            return false;
        }
        return true;
    }

    private boolean hasOrderLine(Long orderLineId) {
        if (orderLineId == null) {
            return false;
        }
        for (SalesDeliveryLine line : lines) {
            if (line != null && orderLineId.equals(line.getSourceOrderLineId())) {
                return true;
            }
        }
        return false;
    }

    private void refreshRemainingHint() {
        if (lblRemaining == null) {
            return;
        }
        SalesOrder order = resolveOrder(SatisCombos.codeOf(cmbOrder));
        String itemCode = SatisCombos.codeOf(cmbItem);
        if (order == null || itemCode == null) {
            lblRemaining.setText("");
            return;
        }
        SalesOrderLine orderLine = SalesDocs.findOrderLine(orderLineDao, order.getRowId(), itemCode);
        if (orderLine == null) {
            lblRemaining.setText("Siparişte yok");
            return;
        }
        double allocated = SalesDocs.allocatedQuantity(deliveryDao, lineDao, orderLine.getRowId(),
                currentRow == null ? null : currentRow.getRowId());
        allocated += qtyOnThisDocument(orderLine.getRowId(), null);
        double remaining = SalesDocs.remainingOrdered(
                orderLine.getQuantity() == null ? 0d : orderLine.getQuantity(), allocated);
        lblRemaining.setText("Kalan " + NumericCells.format(remaining));
    }

    private void applyLock(int status) {
        boolean draft = status == SalesDelivery.STATUS_DRAFT;
        boolean posted = status == SalesDelivery.STATUS_POSTED;
        boolean invoiced = status == SalesDelivery.STATUS_INVOICED;
        boolean editable = draft || status == SalesDelivery.STATUS_CANCELLED;
        txtDocumentNo.setDisable(!editable);
        dtDocumentDate.setDisable(!editable);
        dtShipmentDate.setDisable(!editable);
        cmbAccount.setDisable(!editable);
        cmbWarehouse.setDisable(!editable);
        cmbSourceOrder.setDisable(!editable);
        btnLoadRemaining.setDisable(!editable);
        txtNotes.setDisable(!editable);
        cmbOrder.setDisable(!editable);
        cmbItem.setDisable(!editable);
        txtQuantity.setDisable(!editable);
        txtUnit.setDisable(!editable);
        txtUnitPrice.setDisable(!editable);
        cmbLot.setDisable(!editable);
        txtSerialNo.setDisable(!editable);
        dtExpiry.setDisable(!editable);
        btnAddLine.setDisable(!editable);
        btnRemoveLine.setDisable(!editable);
        tblvLines.setEditable(editable);
        btnSave.setDisable(!editable);
        btnApprove.setDisable(!editable);
        boolean canInvoice = currentRow != null && currentRow.getRowId() != null
                && SalesDocs.hasInvoiceableBalance(currentRow, lineDao, invoiceDao, invoiceLineDao);
        if (btnInvoice != null) {
            btnInvoice.setDisable(!canInvoice);
            btnInvoice.setVisible(posted || invoiced);
            btnInvoice.setManaged(posted || invoiced);
        }
        boolean linked = currentRow != null && currentRow.getRowId() != null
                && SalesDocs.hasLinkedInvoice(invoiceDao, invoiceLineDao, lineDao, currentRow.getRowId());
        btnCancelPosted.setDisable(!posted || linked);
        btnCancelPosted.setVisible(posted);
        btnCancelPosted.setManaged(posted);
    }

    private void close() {
        notifyChanged();
        SatisEmbed.closeIfModal(btnClose);
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

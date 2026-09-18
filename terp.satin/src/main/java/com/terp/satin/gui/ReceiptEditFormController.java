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
import com.terp.plugin.data.AccountPosting;
import com.terp.plugin.data.DocumentNumbers;
import com.terp.plugin.data.IAccountLedger;
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
import com.terp.satin.data.PurchaseDocs;
import com.terp.satin.data.PurchaseInvoice;
import com.terp.satin.data.PurchaseInvoiceLine;
import com.terp.satin.data.PurchaseOrder;
import com.terp.satin.data.PurchaseOrderLine;
import com.terp.satin.data.PurchaseReceipt;
import com.terp.satin.data.PurchaseReceiptLine;
import com.terp.satin.data.PurchaseSettings;
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

public class ReceiptEditFormController implements Initializable {

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
    private TextField txtOfficialNo;
    @FXML
    private DatePicker dtDocumentDate;
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
    private TextField txtLotNo;
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
    private TableView<PurchaseReceiptLine> tblvLines;
    @FXML
    private TableColumn<PurchaseReceiptLine, String> tcSourceOrderNo;
    @FXML
    private TableColumn<PurchaseReceiptLine, String> tcItemCode;
    @FXML
    private TableColumn<PurchaseReceiptLine, String> tcItemDesc;
    @FXML
    private TableColumn<PurchaseReceiptLine, String> tcItemUnit;
    @FXML
    private TableColumn<PurchaseReceiptLine, Double> tcQuantity;
    @FXML
    private TableColumn<PurchaseReceiptLine, Double> tcUnitPrice;
    @FXML
    private TableColumn<PurchaseReceiptLine, String> tcLotNo;
    @FXML
    private Button btnPrint;

    private ICommonDao<PurchaseReceipt> receiptDao;
    private ICommonDao<PurchaseReceiptLine> lineDao;
    private ICommonDao<PurchaseOrder> orderDao;
    private ICommonDao<PurchaseOrderLine> orderLineDao;
    private ICommonDao<PurchaseInvoice> invoiceDao;
    private ICommonDao<PurchaseInvoiceLine> invoiceLineDao;
    private ICommonDao<PurchaseSettings> settingsDao;
    private PurchaseReceipt currentRow;
    private final ObservableList<PurchaseReceiptLine> lines = FXCollections.observableArrayList();
    private boolean applyingLookup;
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

    public void initializeForm(PurchaseReceipt row) {
        fillLookups();
        lines.clear();
        txtDocumentNo.clear();
        txtOfficialNo.clear();
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
        this.currentRow = row;
        if (row == null) {
            applyLock(PurchaseReceipt.STATUS_DRAFT);
            showAudit();
            return;
        }
        txtDocumentNo.setText(SatinDates.empty(row.getDocumentNo()));
        txtOfficialNo.setText(SatinDates.empty(row.getOfficialNo()));
        dtDocumentDate.setValue(SatinDates.toLocalDate(row.getDocumentDate()));
        SatinCombos.select(cmbAccount, row.getAccountCode());
        SatinCombos.select(cmbWarehouse, row.getWarehouseCode());
        txtNotes.setText(SatinDates.empty(row.getNotes()));
        if (row.getRowId() != null) {
            List<PurchaseReceiptLine> stored = lineDao.findAll(
                    "from PurchaseReceiptLine e where e.receiptId = " + row.getRowId()
                            + " order by e.lineNo");
            if (stored != null) {
                lines.setAll(stored);
            }
        }
        applyLock(row.getStatus());
        showAudit();
    }

    public void initializeFromOrder(PurchaseOrder order, List<PurchaseOrderLine> orderLines) {
        fillLookups();
        this.currentRow = receiptDao.getEmpty();
        if (this.currentRow == null) {
            this.currentRow = new PurchaseReceipt();
        }
        this.currentRow.setStatus(PurchaseReceipt.STATUS_DRAFT);
        this.currentRow.setAccountCode(order.getAccountCode());
        this.currentRow.setAccountName(order.getAccountName());
        this.currentRow.setWarehouseCode(order.getWarehouseCode());
        this.currentRow.setWarehouseName(order.getWarehouseName());
        this.currentRow.setNotes(order.getNotes());
        dtDocumentDate.setValue(LocalDate.now());
        txtDocumentNo.clear();
        txtOfficialNo.clear();
        SatinCombos.select(cmbAccount, order.getAccountCode());
        SatinCombos.select(cmbWarehouse, order.getWarehouseCode());
        SatinCombos.select(cmbSourceOrder, order.getDocumentNo());
        SatinCombos.select(cmbOrder, order.getDocumentNo());
        txtNotes.setText(SatinDates.empty(order.getNotes()));
        lines.clear();
        appendRemaining(order, orderLines);
        applyLock(PurchaseReceipt.STATUS_DRAFT);
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
            SatinDates.showError("Mal kabul", "Stok eklentisi yok", "Onay için terp.stok yüklü olmalıdır.");
            return;
        }
        try {
            for (PurchaseReceiptLine line : lines) {
                ledger.post(toPosting(line));
            }
            currentRow.setStatus(PurchaseReceipt.STATUS_POSTED);
            currentRow.setLastUpdateDate(new Date());
            receiptDao.addOrUpdate(currentRow);
            PurchaseDocs.syncOrdersForLines(orderDao, orderLineDao, receiptDao, lineDao,
                    currentRow.getSourceOrderId(), lines);
            close();
        } catch (RuntimeException ex) {
            ledger.reverseByDocument(StockPosting.SOURCE_PURCHASE_RECEIPT, currentRow.getRowId());
            SatinDates.showError("Mal kabul", "Stok fişi yazılamadı",
                    ex.getMessage() == null ? "Onay başarısız." : ex.getMessage());
        }
    }

    @FXML
    private void onActionBtnInvoice(ActionEvent event) {
        if (currentRow == null || currentRow.getRowId() == null) {
            return;
        }
        SatinWindows.convertReceipt(currentRow, receiptDao, lineDao, invoiceDao, () -> {
            PurchaseReceipt reloaded = receiptDao.firstOrDefault(currentRow.getRowId());
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
                || PurchaseDocs.hasLinkedInvoice(invoiceDao, invoiceLineDao, lineDao, currentRow.getRowId())) {
            SatinDates.showError("Mal kabul", "Faturaya bağlı",
                    "Önce bağlı alım faturasını iptal veya silin.");
            return;
        }
        IStockLedger ledger = TerpApplication.getInstance().getStockLedger();
        if (ledger == null) {
            SatinDates.showError("Mal kabul", "Stok eklentisi yok", "İptal için terp.stok yüklü olmalıdır.");
            return;
        }
        ledger.reverseByDocument(StockPosting.SOURCE_PURCHASE_RECEIPT, currentRow.getRowId());
        IAccountLedger accounts = TerpApplication.getInstance().getAccountLedger();
        if (accounts != null) {
            accounts.reverseByDocument(AccountPosting.SOURCE_PURCHASE_RECEIPT, currentRow.getRowId());
        }
        Long orderId = currentRow.getSourceOrderId();
        currentRow.setStatus(PurchaseReceipt.STATUS_CANCELLED);
        currentRow.setLastUpdateDate(new Date());
        receiptDao.addOrUpdate(currentRow);
        PurchaseDocs.syncOrdersForLines(orderDao, orderLineDao, receiptDao, lineDao, orderId, lines);
        close();
    }

    @FXML
    private void onActionBtnPrint(ActionEvent event) {
        if (currentRow == null) {
            return;
        }
        StringBuilder linesText = new StringBuilder();
        for (PurchaseReceiptLine line : lines) {
            if (line == null) {
                continue;
            }
            linesText.append(line.getItemCode()).append("  ").append(line.getItemDesc())
                    .append("  ").append(line.getQuantity()).append(" ")
                    .append(line.getItemUnit() == null ? "" : line.getItemUnit()).append('\n');
        }
        java.util.Map<String, String> values = new java.util.LinkedHashMap<>();
        values.put("DOCUMENT_NO", SatinDates.empty(currentRow.getDocumentNo()));
        values.put("OFFICIAL_NO", SatinDates.empty(currentRow.getOfficialNo()));
        values.put("ACCOUNT", SatinDates.empty(currentRow.getAccountName()));
        values.put("DATE", currentRow.getDateLabel());
        values.put("LINES", linesText.toString());
        values.put("TOTAL", "");
        javafx.stage.Window owner = btnPrint.getScene() == null ? null : btnPrint.getScene().getWindow();
        DocumentPrint.print(owner, "ALIM_IRSALIYE", "Mal kabul " + SatinDates.empty(currentRow.getDocumentNo()),
                "Mal kabul {DOCUMENT_NO}\nEvrak: {OFFICIAL_NO}\nCari: {ACCOUNT}\nTarih: {DATE}\n\n{LINES}",
                values);
    }

    @FXML
    private void onActionBtnClose(ActionEvent event) {
        close();
    }

    @FXML
    private void onActionBtnLoadRemaining(ActionEvent event) {
        PurchaseOrder order = resolveOrder(SatinCombos.codeOf(cmbSourceOrder));
        if (!usableOrder(order)) {
            return;
        }
        applyOrderHeader(order);
        appendRemaining(order, PurchaseDocs.remainingLines(order.getRowId(), orderLineDao, receiptDao,
                lineDao, settingsDao, currentRow == null ? null : currentRow.getRowId()));
        refreshRemainingHint();
    }

    @FXML
    private void onActionBtnAddLine(ActionEvent event) {
        PurchaseReceiptLine selected = tblvLines.getSelectionModel().getSelectedItem();
        String orderNo = SatinCombos.codeOf(cmbOrder);
        String itemCode = SatinCombos.codeOf(cmbItem);
        Double quantity = SatinDates.parseDouble(txtQuantity.getText());
        Double price = SatinDates.parseDouble(txtUnitPrice.getText());
        if (itemCode == null || quantity == null || quantity <= 0d) {
            SatinDates.showError("Mal kabul", "Satır eksik", "Malzeme ve miktar zorunludur.");
            return;
        }
        PurchaseOrder order = null;
        PurchaseOrderLine orderLine = null;
        if (orderNo != null) {
            order = resolveOrder(orderNo);
            boolean sameSource = selected != null && order != null
                    && order.getRowId() != null && order.getRowId().equals(selected.getSourceOrderId());
            if (!sameSource && !usableOrder(order)) {
                return;
            }
            orderLine = PurchaseDocs.findOrderLine(orderLineDao, order.getRowId(), itemCode);
            if (orderLine == null) {
                SatinDates.showError("Mal kabul", "Sipariş satırı yok",
                        itemCode + " bu siparişte yok: " + order.getDocumentNo());
                return;
            }
            String error = validateQty(orderLine, quantity, selected);
            if (error != null) {
                SatinDates.showError("Mal kabul", "Miktar", error);
                return;
            }
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        String tracking = StockCard.trackingError(item, SatinDates.trimToNull(txtLotNo.getText()),
                SatinDates.trimToNull(txtSerialNo.getText()));
        if (tracking != null) {
            SatinDates.showError("Mal kabul", "İzleme", tracking);
            return;
        }
        PurchaseReceiptLine line = selected != null ? selected : new PurchaseReceiptLine();
        line.setItemCode(itemCode);
        line.setItemDesc(item == null ? SatinCombos.nameOf(cmbItem) : item.getItemDesc());
        String unit = SatinDates.trimToNull(txtUnit.getText());
        line.setItemUnit(unit == null && item != null ? item.getItemUnit() : unit);
        line.setQuantity(quantity);
        line.setUnitPrice(StockCard.fillPrice(item, price, true));
        line.setLotNo(SatinDates.trimToNull(txtLotNo.getText()));
        line.setSerialNo(SatinDates.trimToNull(txtSerialNo.getText()));
        line.setExpiryDate(dtExpiry.getValue() == null ? null : SatinDates.toDate(dtExpiry.getValue()));
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
        txtLotNo.clear();
        txtSerialNo.clear();
        dtExpiry.setValue(null);
        tblvLines.getSelectionModel().clearSelection();
        refreshRemainingHint();
    }

    @FXML
    private void onActionBtnRemoveLine(ActionEvent event) {
        PurchaseReceiptLine selected = tblvLines.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lines.remove(selected);
            int no = 1;
            for (PurchaseReceiptLine line : lines) {
                line.setLineNo(no++);
            }
            refreshRemainingHint();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.receiptDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseReceipt.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseReceiptLine.class);
        this.orderDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseOrder.class);
        this.orderLineDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseOrderLine.class);
        this.invoiceDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseInvoice.class);
        this.invoiceLineDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseInvoiceLine.class);
        this.settingsDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseSettings.class);
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
            PurchaseReceiptLine line = event.getRowValue();
            Double value = event.getNewValue();
            if (line == null) {
                return;
            }
            if (value == null || value <= 0d) {
                SatinDates.showError("Mal kabul", "Miktar", "Miktar sıfırdan büyük olmalıdır.");
                tblvLines.refresh();
                return;
            }
            if (line.getSourceOrderLineId() != null) {
                PurchaseOrderLine orderLine = orderLineDao.firstOrDefault(line.getSourceOrderLineId());
                String error = validateQty(orderLine, value, line);
                if (error != null) {
                    SatinDates.showError("Mal kabul", "Miktar", error);
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
            applyItem(SatinCombos.codeOf(cmbItem));
            applyOrderDefaults();
        });
        cmbOrder.valueProperty().addListener((obs, old, value) -> applyOrderDefaults());
        fillLookups();
        if (dtDocumentDate.getValue() == null) {
            dtDocumentDate.setValue(LocalDate.now());
        }
        applyLock(PurchaseReceipt.STATUS_DRAFT);
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
        String warehouseCode = SatinCombos.codeOf(cmbWarehouse);
        if (warehouseCode == null) {
            SatinDates.showError("Mal kabul", "Depo eksik", "Depo seçin.");
            return false;
        }
        String periodError = WorkingPeriod.rejectIfOutside(SatinDates.toDate(dtDocumentDate.getValue()));
        if (periodError != null) {
            SatinDates.showError("Mal kabul", "Dönem", periodError);
            return false;
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        for (PurchaseReceiptLine line : lines) {
            IStockItem item = items == null || line == null ? null : items.findByCode(line.getItemCode());
            String tracking = StockCard.trackingError(item, line == null ? null : line.getLotNo(),
                    line == null ? null : line.getSerialNo());
            if (tracking != null) {
                SatinDates.showError("Mal kabul", "İzleme", tracking);
                return false;
            }
        }
        if (lines.isEmpty()) {
            SatinDates.showError("Mal kabul", "Satır yok", "En az bir malzeme satırı ekleyin.");
            return false;
        }
        String qtyError = validateAllLines();
        if (qtyError != null) {
            SatinDates.showError("Mal kabul", "Miktar", qtyError);
            return false;
        }
        Date now = new Date();
        PurchaseReceipt row = this.currentRow;
        if (row == null) {
            row = receiptDao.getEmpty();
            if (row == null) {
                SatinDates.showError("Mal kabul", "Kayıt hatası", "Belge oluşturulamadı.");
                return false;
            }
            row.setAddedDate(now);
            row.setStatus(PurchaseReceipt.STATUS_DRAFT);
        }
        if (!row.isDraft() && !row.isCancelled()) {
            SatinDates.showError("Mal kabul", "Kilitli belge",
                    "Onaylı belge taslak olarak kaydedilemez.");
            return false;
        }
        if (!CompanyScope.stamp(row)) {
            SatinDates.showError("Mal kabul", "Firma", "Önce araç çubuğundan çalışma firmasını seçin.");
            return false;
        }
        stampHeaderOrder(row);
        String documentNo = SatinDates.assignDocumentNo(DocumentNumbers.PURCHASE_RECEIPT, txtDocumentNo);
        if (documentNo == null || documentNo.isBlank()) {
            SatinDates.showError("Mal kabul", "Sistem no",
                    "Sistem no üretilemedi. Elle yazın veya Sistem yönetimi → Belge numaraları serisini kontrol edin.");
            return false;
        }
        row.setDocumentNo(documentNo);
        row.setOfficialNo(SatinDates.trimToNull(txtOfficialNo.getText()));
        row.setDocumentDate(SatinDates.toDate(dtDocumentDate.getValue()));
        row.setAccountCode(SatinCombos.codeOf(cmbAccount));
        row.setAccountName(SatinCombos.nameOf(cmbAccount));
        row.setWarehouseCode(warehouseCode);
        IWarehouseLookup warehouses = TerpApplication.getInstance().getWarehouseLookup();
        IStockWarehouse warehouse = warehouses == null ? null : warehouses.findByCode(warehouseCode);
        row.setWarehouseName(warehouse == null ? SatinCombos.nameOf(cmbWarehouse) : warehouse.getWarehouseName());
        row.setNotes(SatinDates.trimToNull(txtNotes.getText()));
        row.setLastUpdateDate(now);
        if (row.getAddedDate() == null) {
            row.setAddedDate(now);
        }
        if (CompanyScope.rejectDuplicate(
                CompanyScope.findDuplicate(receiptDao, "PurchaseReceipt", "documentNo",
                        row.getDocumentNo(), row.getRowId()),
                "Sistem numarası tekrar ediyor")) {
            return false;
        }
        PurchaseReceipt saved = receiptDao.addOrUpdate(row);
        if (saved == null || saved.getRowId() == null) {
            SatinDates.showError("Mal kabul", "Kayıt hatası", "Belge kaydedilemedi.");
            return false;
        }
        this.currentRow = saved;
        replaceLines(saved.getRowId(), now);
        PurchaseDocs.syncOrdersForLines(orderDao, orderLineDao, receiptDao, lineDao,
                saved.getSourceOrderId(), lines);
        showAudit();
        return true;
    }

    private void replaceLines(Long receiptId, Date now) {
        PurchaseDocs.deleteByParent(lineDao, "PurchaseReceiptLine", "receiptId", receiptId);
        int no = 1;
        List<PurchaseReceiptLine> stored = new ArrayList<>();
        for (PurchaseReceiptLine line : lines) {
            PurchaseReceiptLine copy = lineDao.getEmpty();
            if (copy == null) {
                continue;
            }
            copy.setAddedDate(now);
            copy.setLastUpdateDate(now);
            copy.setReceiptId(receiptId);
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
            PurchaseReceiptLine saved = lineDao.addOrUpdate(copy);
            stored.add(saved == null ? copy : saved);
        }
        lines.setAll(stored);
    }

    private StockPosting toPosting(PurchaseReceiptLine line) {
        StockPosting posting = new StockPosting();
        posting.setMovementDate(currentRow.getDocumentDate());
        posting.setDirection(StockDirection.IN);
        posting.setReasonCode(IMovementReason.CODE_PURCHASE);
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
        posting.setSourceType(StockPosting.SOURCE_PURCHASE_RECEIPT);
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

        List<String> accountLabels = new ArrayList<>();
        IAccountLookup accounts = TerpApplication.getInstance().getAccountLookup();
        if (accounts != null) {
            List<IAccount> rows = accounts.findActiveSuppliers();
            if (rows != null) {
                for (IAccount account : rows) {
                    accountLabels.add(account.getDisplayLabel());
                }
            }
        }
        SatinCombos.fill(cmbAccount, accountLabels);

        List<String> orderLabels = new ArrayList<>();
        List<PurchaseOrder> orders = PurchaseDocs.receivableOrders(orderDao, orderLineDao, receiptDao,
                lineDao, settingsDao);
        for (PurchaseOrder order : orders) {
            String name = order.getAccountName() == null ? "" : order.getAccountName();
            orderLabels.add(name.isBlank() ? order.getDocumentNo() : order.getDocumentNo() + " — " + name);
        }
        SatinCombos.fill(cmbSourceOrder, orderLabels);
        SatinCombos.fill(cmbOrder, orderLabels);
    }

    private void loadLineEditor(PurchaseReceiptLine line) {
        applyingLookup = true;
        try {
            SatinCombos.select(cmbOrder, line.getSourceOrderNo());
            SatinCombos.select(cmbItem, line.getItemCode());
            txtUnit.setText(SatinDates.empty(line.getItemUnit()));
            txtQuantity.setText(NumericCells.text(line.getQuantity()));
            txtUnitPrice.setText(NumericCells.text(line.getUnitPrice()));
            txtLotNo.setText(SatinDates.empty(line.getLotNo()));
            txtSerialNo.setText(SatinDates.empty(line.getSerialNo()));
            dtExpiry.setValue(line.getExpiryDate() == null ? null : SatinDates.toLocalDate(line.getExpiryDate()));
            refreshRemainingHint();
        } finally {
            applyingLookup = false;
        }
    }

    private void applyItem(String itemCode) {
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (item != null && (txtUnit.getText() == null || txtUnit.getText().isBlank())) {
            txtUnit.setText(SatinDates.empty(item.getItemUnit()));
        }
        if (item != null && (txtUnitPrice.getText() == null || txtUnitPrice.getText().isBlank())
                && item.getPurchasePrice() != null) {
            txtUnitPrice.setText(NumericCells.text(item.getPurchasePrice()));
        }
    }

    private void applyOrderDefaults() {
        if (applyingLookup) {
            return;
        }
        applyingLookup = true;
        try {
            PurchaseOrder order = resolveOrder(SatinCombos.codeOf(cmbOrder));
            if (order != null && !order.canReceive()) {
                order = null;
            }
            String itemCode = SatinCombos.codeOf(cmbItem);
            if (order != null) {
                applyOrderHeader(order);
            }
            if (order != null && itemCode != null) {
                PurchaseOrderLine orderLine = PurchaseDocs.findOrderLine(orderLineDao, order.getRowId(),
                        itemCode);
                if (orderLine != null) {
                    double allocated = PurchaseDocs.allocatedQuantity(receiptDao, lineDao, orderLine.getRowId(),
                            currentRow == null ? null : currentRow.getRowId());
                    allocated += qtyOnThisDocument(orderLine.getRowId(), null);
                    double remaining = PurchaseDocs.remainingOrdered(
                            orderLine.getQuantity() == null ? 0d : orderLine.getQuantity(), allocated);
                    if (txtQuantity.getText() == null || txtQuantity.getText().isBlank()) {
                        if (remaining > 0d) {
                            txtQuantity.setText(NumericCells.format(remaining));
                        }
                    }
                    if (txtUnit.getText() == null || txtUnit.getText().isBlank()) {
                        txtUnit.setText(SatinDates.empty(orderLine.getItemUnit()));
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

    private void applyOrderHeader(PurchaseOrder order) {
        if (order == null) {
            return;
        }
        if (SatinCombos.codeOf(cmbAccount) == null) {
            SatinCombos.select(cmbAccount, order.getAccountCode());
        }
        if (SatinCombos.codeOf(cmbWarehouse) == null) {
            SatinCombos.select(cmbWarehouse, order.getWarehouseCode());
        }
    }

    private void appendRemaining(PurchaseOrder order, List<PurchaseOrderLine> orderLines) {
        if (order == null || orderLines == null) {
            return;
        }
        int no = lines.size() + 1;
        Long exclude = currentRow == null ? null : currentRow.getRowId();
        for (PurchaseOrderLine source : orderLines) {
            if (source == null || hasOrderLine(source.getRowId())) {
                continue;
            }
            double allocated = PurchaseDocs.allocatedQuantity(receiptDao, lineDao, source.getRowId(), exclude);
            allocated += qtyOnThisDocument(source.getRowId(), null);
            double remaining = PurchaseDocs.remainingOrdered(
                    source.getQuantity() == null ? 0d : source.getQuantity(), allocated);
            if (remaining <= 0d) {
                continue;
            }
            PurchaseReceiptLine line = new PurchaseReceiptLine();
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
        for (PurchaseReceiptLine line : lines) {
            if (line == null || line.getSourceOrderLineId() == null) {
                continue;
            }
            PurchaseOrderLine orderLine = orderLineDao.firstOrDefault(line.getSourceOrderLineId());
            String error = validateQty(orderLine, line.getQuantity(), line);
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    private String validateQty(PurchaseOrderLine orderLine, Double quantity, PurchaseReceiptLine current) {
        if (orderLine == null) {
            return "Sipariş satırı bulunamadı.";
        }
        if (quantity == null || quantity <= 0d) {
            return "Miktar sıfırdan büyük olmalıdır.";
        }
        double ordered = orderLine.getQuantity() == null ? 0d : orderLine.getQuantity();
        double allocated = PurchaseDocs.allocatedQuantity(receiptDao, lineDao, orderLine.getRowId(),
                currentRow == null ? null : currentRow.getRowId());
        allocated += qtyOnThisDocument(orderLine.getRowId(), current);
        double max = PurchaseDocs.maxReceivable(ordered, allocated, PurchaseDocs.overPercent(settingsDao));
        if (quantity > max + 0.0000001d) {
            return orderLine.getItemCode() + " için kalan (fazla kabul dahil) " + max;
        }
        return null;
    }

    private double qtyOnThisDocument(Long orderLineId, PurchaseReceiptLine skip) {
        double sum = 0d;
        for (PurchaseReceiptLine line : lines) {
            if (line == skip || line == null || orderLineId == null) {
                continue;
            }
            if (orderLineId.equals(line.getSourceOrderLineId())) {
                sum += line.getQuantity() == null ? 0d : line.getQuantity();
            }
        }
        return sum;
    }

    private void stampHeaderOrder(PurchaseReceipt row) {
        LinkedHashSet<String> numbers = new LinkedHashSet<>();
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (PurchaseReceiptLine line : lines) {
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

    private PurchaseOrder resolveOrder(String documentNo) {
        return PurchaseDocs.findOrderByNo(orderDao, documentNo);
    }

    private boolean usableOrder(PurchaseOrder order) {
        if (order == null) {
            SatinDates.showError("Mal kabul", "Sipariş bulunamadı", "Sipariş numarasını kontrol edin.");
            return false;
        }
        if (!order.canReceive()) {
            SatinDates.showError("Mal kabul", "Sipariş uygun değil",
                    "Taslak veya iptal siparişten mal kabul yapılamaz.");
            return false;
        }
        return true;
    }

    private boolean hasOrderLine(Long orderLineId) {
        if (orderLineId == null) {
            return false;
        }
        for (PurchaseReceiptLine line : lines) {
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
        PurchaseOrder order = resolveOrder(SatinCombos.codeOf(cmbOrder));
        String itemCode = SatinCombos.codeOf(cmbItem);
        if (order == null || itemCode == null) {
            lblRemaining.setText("");
            return;
        }
        PurchaseOrderLine orderLine = PurchaseDocs.findOrderLine(orderLineDao, order.getRowId(), itemCode);
        if (orderLine == null) {
            lblRemaining.setText("Siparişte yok");
            return;
        }
        double allocated = PurchaseDocs.allocatedQuantity(receiptDao, lineDao, orderLine.getRowId(),
                currentRow == null ? null : currentRow.getRowId());
        allocated += qtyOnThisDocument(orderLine.getRowId(), null);
        double remaining = PurchaseDocs.remainingOrdered(
                orderLine.getQuantity() == null ? 0d : orderLine.getQuantity(), allocated);
        double max = PurchaseDocs.maxReceivable(orderLine.getQuantity() == null ? 0d : orderLine.getQuantity(),
                allocated, PurchaseDocs.overPercent(settingsDao));
        lblRemaining.setText("Kalan " + NumericCells.format(remaining) + " / max " + NumericCells.format(max));
    }

    private void applyLock(int status) {
        boolean draft = status == PurchaseReceipt.STATUS_DRAFT;
        boolean posted = status == PurchaseReceipt.STATUS_POSTED;
        boolean invoiced = status == PurchaseReceipt.STATUS_INVOICED;
        boolean editable = draft || status == PurchaseReceipt.STATUS_CANCELLED;
        txtDocumentNo.setDisable(!editable);
        txtOfficialNo.setDisable(!editable);
        dtDocumentDate.setDisable(!editable);
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
        txtLotNo.setDisable(!editable);
        txtSerialNo.setDisable(!editable);
        dtExpiry.setDisable(!editable);
        btnAddLine.setDisable(!editable);
        btnRemoveLine.setDisable(!editable);
        tblvLines.setEditable(editable);
        btnSave.setDisable(!editable);
        btnApprove.setDisable(!editable);
        boolean canInvoice = currentRow != null && currentRow.getRowId() != null
                && PurchaseDocs.hasInvoiceableBalance(currentRow, lineDao, invoiceDao, invoiceLineDao);
        if (btnInvoice != null) {
            btnInvoice.setDisable(!canInvoice);
            btnInvoice.setVisible(posted || invoiced);
            btnInvoice.setManaged(posted || invoiced);
        }
        boolean linked = currentRow != null && currentRow.getRowId() != null
                && PurchaseDocs.hasLinkedInvoice(invoiceDao, invoiceLineDao, lineDao, currentRow.getRowId());
        btnCancelPosted.setDisable(!posted || linked);
        btnCancelPosted.setVisible(posted);
        btnCancelPosted.setManaged(posted);
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

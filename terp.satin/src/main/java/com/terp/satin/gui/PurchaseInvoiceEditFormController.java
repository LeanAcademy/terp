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
import com.terp.plugin.data.AccountDirection;
import com.terp.plugin.data.AccountPosting;
import com.terp.plugin.data.DocumentNumbers;
import com.terp.plugin.data.IAccountLedger;
import com.terp.plugin.data.IAccountLookup;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.IItemLookup;
import com.terp.plugin.data.IWarehouseLookup;
import com.terp.plugin.data.model.IAccount;
import com.terp.plugin.data.model.IStockItem;
import com.terp.plugin.data.model.IStockWarehouse;
import com.terp.plugin.gui.RecordAuditBar;
import com.terp.satin.data.PurchaseReceipt;
import com.terp.satin.data.PurchaseReceiptLine;
import com.terp.satin.data.PurchaseDocs;
import com.terp.satin.data.PurchaseInvoice;
import com.terp.satin.data.PurchaseInvoiceLine;
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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

public class PurchaseInvoiceEditFormController implements Initializable {

    @FXML
    private Button btnSave;
    @FXML
    private Button btnApprove;
    @FXML
    private Button btnCancelPosted;
    @FXML
    private Button btnClose;
    @FXML
    private TextField txtDocumentNo;
    @FXML
    private DatePicker dtDocumentDate;
    @FXML
    private ComboBox<String> cmbAccount;
    @FXML
    private ComboBox<String> cmbWarehouse;
    @FXML
    private ComboBox<String> cmbSourceReceipt;
    @FXML
    private Button btnLoadRemaining;
    @FXML
    private TextArea txtNotes;
    @FXML
    private ComboBox<String> cmbReceipt;
    @FXML
    private ComboBox<String> cmbItem;
    @FXML
    private TextField txtQuantity;
    @FXML
    private TextField txtUnit;
    @FXML
    private TextField txtUnitPrice;
    @FXML
    private ComboBox<String> cmbVatRate;
    @FXML
    private Label lblRemaining;
    @FXML
    private Button btnAddLine;
    @FXML
    private Button btnRemoveLine;
    @FXML
    private TableView<PurchaseInvoiceLine> tblvLines;
    @FXML
    private TableColumn<PurchaseInvoiceLine, String> tcSourceReceiptNo;
    @FXML
    private TableColumn<PurchaseInvoiceLine, String> tcItemCode;
    @FXML
    private TableColumn<PurchaseInvoiceLine, String> tcItemDesc;
    @FXML
    private TableColumn<PurchaseInvoiceLine, String> tcItemUnit;
    @FXML
    private TableColumn<PurchaseInvoiceLine, Double> tcQuantity;
    @FXML
    private TableColumn<PurchaseInvoiceLine, Double> tcUnitPrice;
    @FXML
    private TableColumn<PurchaseInvoiceLine, Double> tcVatRate;
    @FXML
    private TableColumn<PurchaseInvoiceLine, Double> tcNetAmount;
    @FXML
    private TableColumn<PurchaseInvoiceLine, Double> tcVatAmount;
    @FXML
    private TableColumn<PurchaseInvoiceLine, Double> tcGrossAmount;
    @FXML
    private Label lblNetTotal;
    @FXML
    private Label lblVatTotal;
    @FXML
    private Label lblGrossTotal;

    private ICommonDao<PurchaseInvoice> invoiceDao;
    private ICommonDao<PurchaseInvoiceLine> lineDao;
    private ICommonDao<PurchaseReceipt> receiptDao;
    private ICommonDao<PurchaseReceiptLine> receiptLineDao;
    private PurchaseInvoice currentRow;
    private final ObservableList<PurchaseInvoiceLine> lines = FXCollections.observableArrayList();
    private boolean applyingLookup;
    private Runnable afterChange;
    private boolean notifying;
    private RecordAuditBar auditBar;

    public void initializeForm(PurchaseInvoice row) {
        fillLookups();
        lines.clear();
        txtDocumentNo.clear();
        txtNotes.clear();
        txtQuantity.clear();
        txtUnit.clear();
        txtUnitPrice.clear();
        SatinCombos.selectVat(cmbVatRate, 20d);
        if (lblRemaining != null) {
            lblRemaining.setText("");
        }
        cmbItem.getSelectionModel().clearSelection();
        cmbAccount.getSelectionModel().clearSelection();
        cmbWarehouse.getSelectionModel().clearSelection();
        cmbSourceReceipt.getSelectionModel().clearSelection();
        cmbReceipt.getSelectionModel().clearSelection();
        dtDocumentDate.setValue(LocalDate.now());
        this.currentRow = row;
        if (row == null) {
            applyLock(PurchaseInvoice.STATUS_DRAFT);
            refreshTotals();
            showAudit();
            return;
        }
        txtDocumentNo.setText(SatinDates.empty(row.getDocumentNo()));
        dtDocumentDate.setValue(SatinDates.toLocalDate(row.getDocumentDate()));
        SatinCombos.select(cmbAccount, row.getAccountCode());
        SatinCombos.select(cmbWarehouse, row.getWarehouseCode());
        txtNotes.setText(SatinDates.empty(row.getNotes()));
        if (row.getRowId() != null) {
            List<PurchaseInvoiceLine> stored = lineDao.findAll(
                    "from PurchaseInvoiceLine e where e.invoiceId = " + row.getRowId()
                            + " order by e.lineNo");
            if (stored != null) {
                lines.setAll(stored);
            }
        }
        refreshTotals();
        applyLock(row.getStatus());
        showAudit();
    }

    public void initializeFromReceipt(PurchaseReceipt delivery, List<PurchaseReceiptLine> receiptLines) {
        fillLookups();
        this.currentRow = invoiceDao.getEmpty();
        if (this.currentRow == null) {
            this.currentRow = new PurchaseInvoice();
        }
        this.currentRow.setStatus(PurchaseInvoice.STATUS_DRAFT);
        this.currentRow.setAccountCode(delivery.getAccountCode());
        this.currentRow.setAccountName(delivery.getAccountName());
        this.currentRow.setWarehouseCode(delivery.getWarehouseCode());
        this.currentRow.setWarehouseName(delivery.getWarehouseName());
        this.currentRow.setNotes(delivery.getNotes());
        dtDocumentDate.setValue(LocalDate.now());
        txtDocumentNo.clear();
        SatinCombos.select(cmbAccount, delivery.getAccountCode());
        SatinCombos.select(cmbWarehouse, delivery.getWarehouseCode());
        SatinCombos.select(cmbSourceReceipt, delivery.getDocumentNo());
        SatinCombos.select(cmbReceipt, delivery.getDocumentNo());
        txtNotes.setText(SatinDates.empty(delivery.getNotes()));
        lines.clear();
        appendRemaining(delivery, receiptLines);
        applyLock(PurchaseInvoice.STATUS_DRAFT);
        refreshTotals();
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
        IAccountLedger ledger = TerpApplication.getInstance().getAccountLedger();
        if (ledger == null) {
            SatinDates.showError("Alım faturası", "Cari eklentisi yok",
                    "Onay için terp.cari yüklü olmalıdır.");
            return;
        }
        double amount = PurchaseDocs.invoiceAmountOf(lines);
        if (amount <= 0d) {
            SatinDates.showError("Alım faturası", "Tutar sıfır",
                    "Cari fişi için satırlarda birim fiyat girin.");
            return;
        }
        try {
            ledger.post(toAccountPosting(amount));
            currentRow.setStatus(PurchaseInvoice.STATUS_POSTED);
            currentRow.setLastUpdateDate(new Date());
            invoiceDao.addOrUpdate(currentRow);
            PurchaseDocs.syncReceiptsForLines(receiptDao, receiptLineDao, invoiceDao, lineDao,
                    currentRow.getSourceReceiptId(), lines);
            close();
        } catch (RuntimeException ex) {
            ledger.reverseByDocument(AccountPosting.SOURCE_PURCHASE_INVOICE, currentRow.getRowId());
            SatinDates.showError("Alım faturası", "Cari fişi yazılamadı",
                    ex.getMessage() == null ? "Onay başarısız." : ex.getMessage());
        }
    }

    @FXML
    private void onActionBtnCancelPosted(ActionEvent event) {
        if (currentRow == null || currentRow.getRowId() == null || !currentRow.isPosted()) {
            return;
        }
        IAccountLedger ledger = TerpApplication.getInstance().getAccountLedger();
        if (ledger == null) {
            SatinDates.showError("Alım faturası", "Cari eklentisi yok",
                    "İptal için terp.cari yüklü olmalıdır.");
            return;
        }
        ledger.reverseByDocument(AccountPosting.SOURCE_PURCHASE_INVOICE, currentRow.getRowId());
        Long deliveryId = currentRow.getSourceReceiptId();
        currentRow.setStatus(PurchaseInvoice.STATUS_CANCELLED);
        currentRow.setLastUpdateDate(new Date());
        invoiceDao.addOrUpdate(currentRow);
        PurchaseDocs.syncReceiptsForLines(receiptDao, receiptLineDao, invoiceDao, lineDao,
                deliveryId, lines);
        close();
    }

    @FXML
    private void onActionBtnClose(ActionEvent event) {
        close();
    }

    @FXML
    private void onActionBtnLoadRemaining(ActionEvent event) {
        PurchaseReceipt delivery = resolveReceipt(SatinCombos.codeOf(cmbSourceReceipt));
        if (!usableReceipt(delivery)) {
            return;
        }
        applyReceiptHeader(delivery);
        appendRemaining(delivery, PurchaseDocs.remainingInvoiceLines(delivery.getRowId(), receiptLineDao,
                invoiceDao, lineDao, currentRow == null ? null : currentRow.getRowId()));
        refreshRemainingHint();
    }

    @FXML
    private void onActionBtnAddLine(ActionEvent event) {
        String deliveryNo = SatinCombos.codeOf(cmbReceipt);
        String itemCode = SatinCombos.codeOf(cmbItem);
        Double quantity = SatinDates.parseDouble(txtQuantity.getText());
        Double price = SatinDates.parseDouble(txtUnitPrice.getText());
        Double vatRate = SatinCombos.vatRateOf(cmbVatRate);
        if (itemCode == null || quantity == null || quantity <= 0d) {
            SatinDates.showError("Alım faturası", "Satır eksik", "Malzeme ve miktar zorunludur.");
            return;
        }
        if (vatRate == null) {
            SatinDates.showError("Alım faturası", "KDV eksik", "KDV oranı seçin.");
            return;
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        PurchaseReceipt delivery = null;
        PurchaseReceiptLine deliveryLine = null;
        if (deliveryNo != null) {
            delivery = resolveReceipt(deliveryNo);
            if (!usableReceipt(delivery)) {
                return;
            }
            deliveryLine = PurchaseDocs.findReceiptLine(receiptLineDao, delivery.getRowId(), itemCode);
            if (deliveryLine == null) {
                SatinDates.showError("Alım faturası", "Mal kabul satırı yok",
                        itemCode + " bu mal kabulde yok: " + delivery.getDocumentNo());
                return;
            }
            String error = validateQty(deliveryLine, quantity, null);
            if (error != null) {
                SatinDates.showError("Alım faturası", "Miktar", error);
                return;
            }
        }
        PurchaseInvoiceLine line = new PurchaseInvoiceLine();
        line.setItemCode(itemCode);
        line.setItemDesc(item == null ? SatinCombos.nameOf(cmbItem) : item.getItemDesc());
        String unit = SatinDates.trimToNull(txtUnit.getText());
        line.setItemUnit(unit == null && item != null ? item.getItemUnit() : unit);
        line.setQuantity(quantity);
        line.setUnitPrice(price);
        line.setVatRate(vatRate);
        if (delivery != null) {
            line.setSourceReceiptId(delivery.getRowId());
            line.setSourceReceiptNo(delivery.getDocumentNo());
            line.setSourceReceiptLineId(deliveryLine.getRowId());
            line.setSourceOrderId(deliveryLine.getSourceOrderId());
            line.setSourceOrderNo(deliveryLine.getSourceOrderNo());
            line.setSourceOrderLineId(deliveryLine.getSourceOrderLineId());
            if (line.getItemUnit() == null) {
                line.setItemUnit(deliveryLine.getItemUnit());
            }
            if (line.getUnitPrice() == null) {
                line.setUnitPrice(deliveryLine.getUnitPrice());
            }
            if (line.getVatRate() == null && item != null) {
                line.setVatRate(item.vatPercent());
            }
            applyReceiptHeader(delivery);
        }
        line.setLineNo(lines.size() + 1);
        lines.add(line);
        txtQuantity.clear();
        txtUnitPrice.clear();
        refreshRemainingHint();
        refreshTotals();
    }

    @FXML
    private void onActionBtnRemoveLine(ActionEvent event) {
        PurchaseInvoiceLine selected = tblvLines.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lines.remove(selected);
            int no = 1;
            for (PurchaseInvoiceLine line : lines) {
                line.setLineNo(no++);
            }
            refreshRemainingHint();
            refreshTotals();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.invoiceDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseInvoice.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseInvoiceLine.class);
        this.receiptDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseReceipt.class);
        this.receiptLineDao = TerpApplication.getInstance().getPersistence()
                .createDao(PurchaseReceiptLine.class);
        this.tcSourceReceiptNo.setCellValueFactory(new PropertyValueFactory<>("sourceReceiptNo"));
        this.tcItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        this.tcUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        this.tcVatRate.setCellValueFactory(new PropertyValueFactory<>("vatRate"));
        this.tcNetAmount.setCellValueFactory(new PropertyValueFactory<>("netAmount"));
        this.tcVatAmount.setCellValueFactory(new PropertyValueFactory<>("vatAmount"));
        this.tcGrossAmount.setCellValueFactory(new PropertyValueFactory<>("grossAmount"));
        this.tcQuantity.setCellFactory(TextFieldTableCell.forTableColumn(qtyConverter()));
        this.tcQuantity.setOnEditCommit(event -> {
            PurchaseInvoiceLine line = event.getRowValue();
            Double value = event.getNewValue();
            if (line == null) {
                return;
            }
            if (value == null || value <= 0d) {
                SatinDates.showError("Alım faturası", "Miktar", "Miktar sıfırdan büyük olmalıdır.");
                tblvLines.refresh();
                return;
            }
            if (line.getSourceReceiptLineId() != null) {
                PurchaseReceiptLine deliveryLine = receiptLineDao.firstOrDefault(line.getSourceReceiptLineId());
                String error = validateQty(deliveryLine, value, line);
                if (error != null) {
                    SatinDates.showError("Alım faturası", "Miktar", error);
                    tblvLines.refresh();
                    return;
                }
            }
            line.setQuantity(value);
            refreshRemainingHint();
            refreshTotals();
            tblvLines.refresh();
        });
        this.tblvLines.setItems(lines);
        cmbItem.valueProperty().addListener((obs, old, value) -> {
            applyItem(SatinCombos.codeOf(cmbItem));
            applyReceiptDefaults();
        });
        cmbReceipt.valueProperty().addListener((obs, old, value) -> applyReceiptDefaults());
        fillLookups();
        if (dtDocumentDate.getValue() == null) {
            dtDocumentDate.setValue(LocalDate.now());
        }
        applyLock(PurchaseInvoice.STATUS_DRAFT);
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
        String accountCode = SatinCombos.codeOf(cmbAccount);
        if (accountCode == null) {
            SatinDates.showError("Alım faturası", "Tedarikçi eksik", "Tedarikçi seçin.");
            return false;
        }
        if (lines.isEmpty()) {
            SatinDates.showError("Alım faturası", "Satır yok", "En az bir malzeme satırı ekleyin.");
            return false;
        }
        String qtyError = validateAllLines();
        if (qtyError != null) {
            SatinDates.showError("Alım faturası", "Miktar", qtyError);
            return false;
        }
        IItemLookup itemLookup = TerpApplication.getInstance().getItemLookup();
        for (PurchaseInvoiceLine line : lines) {
            if (line == null || line.getItemCode() == null || line.getItemCode().isBlank()) {
                SatinDates.showError("Alım faturası", "Satır eksik", "Malzeme kodu zorunludur.");
                return false;
            }
            if (itemLookup != null && itemLookup.findByCode(line.getItemCode()) == null) {
                SatinDates.showError("Alım faturası", "Malzeme yok",
                        "Malzeme bulunamadı: " + line.getItemCode());
                return false;
            }
        }
        Date now = new Date();
        PurchaseInvoice row = this.currentRow;
        if (row == null) {
            row = invoiceDao.getEmpty();
            if (row == null) {
                SatinDates.showError("Alım faturası", "Kayıt hatası", "Belge oluşturulamadı.");
                return false;
            }
            row.setAddedDate(now);
            row.setStatus(PurchaseInvoice.STATUS_DRAFT);
        }
        if (!row.isDraft()) {
            SatinDates.showError("Alım faturası", "Kilitli belge",
                    "Onaylı veya iptal belge taslak olarak kaydedilemez.");
            return false;
        }
        if (!CompanyScope.stamp(row)) {
            SatinDates.showError("Alım faturası", "Firma",
                    "Önce araç çubuğundan çalışma firmasını seçin.");
            return false;
        }
        stampHeaderDelivery(row);
        String documentNo = SatinDates.assignDocumentNo(DocumentNumbers.PURCHASE_INVOICE, txtDocumentNo);
        if (documentNo == null || documentNo.isBlank()) {
            SatinDates.showError("Alım faturası", "Belge no",
                    "Belge no üretilemedi. Elle yazın veya Sistem yönetimi → Belge numaraları serisini kontrol edin.");
            return false;
        }
        row.setDocumentNo(documentNo);
        row.setDocumentDate(SatinDates.toDate(dtDocumentDate.getValue()));
        row.setAccountCode(accountCode);
        IAccountLookup accounts = TerpApplication.getInstance().getAccountLookup();
        IAccount account = accounts == null ? null : accounts.findByCode(accountCode);
        row.setAccountName(account == null ? SatinCombos.nameOf(cmbAccount) : account.getAccountName());
        String warehouseCode = SatinCombos.codeOf(cmbWarehouse);
        row.setWarehouseCode(warehouseCode);
        IWarehouseLookup warehouses = TerpApplication.getInstance().getWarehouseLookup();
        IStockWarehouse warehouse = warehouseCode == null || warehouses == null
                ? null : warehouses.findByCode(warehouseCode);
        row.setWarehouseName(warehouse == null ? SatinCombos.nameOf(cmbWarehouse) : warehouse.getWarehouseName());
        row.setNotes(SatinDates.trimToNull(txtNotes.getText()));
        row.setLastUpdateDate(now);
        if (row.getAddedDate() == null) {
            row.setAddedDate(now);
        }
        if (CompanyScope.rejectDuplicate(
                CompanyScope.findDuplicate(invoiceDao, "PurchaseInvoice", "documentNo",
                        row.getDocumentNo(), row.getRowId()),
                "Belge numarası tekrar ediyor")) {
            return false;
        }
        PurchaseInvoice saved = invoiceDao.addOrUpdate(row);
        if (saved == null || saved.getRowId() == null) {
            SatinDates.showError("Alım faturası", "Kayıt hatası", "Belge kaydedilemedi.");
            return false;
        }
        this.currentRow = saved;
        replaceLines(saved.getRowId(), now);
        PurchaseDocs.syncReceiptsForLines(receiptDao, receiptLineDao, invoiceDao, lineDao,
                saved.getSourceReceiptId(), lines);
        showAudit();
        return true;
    }

    private void replaceLines(Long invoiceId, Date now) {
        PurchaseDocs.deleteByParent(lineDao, "PurchaseInvoiceLine", "invoiceId", invoiceId);
        int no = 1;
        List<PurchaseInvoiceLine> stored = new ArrayList<>();
        for (PurchaseInvoiceLine line : lines) {
            PurchaseInvoiceLine copy = lineDao.getEmpty();
            if (copy == null) {
                continue;
            }
            copy.setAddedDate(now);
            copy.setLastUpdateDate(now);
            copy.setInvoiceId(invoiceId);
            copy.setLineNo(no++);
            copy.setItemCode(line.getItemCode());
            copy.setItemDesc(line.getItemDesc());
            copy.setItemUnit(line.getItemUnit());
            copy.setQuantity(line.getQuantity());
            copy.setUnitPrice(line.getUnitPrice());
            copy.setVatRate(line.getVatRate());
            copy.setSourceReceiptLineId(line.getSourceReceiptLineId());
            copy.setSourceReceiptId(line.getSourceReceiptId());
            copy.setSourceReceiptNo(line.getSourceReceiptNo());
            copy.setSourceOrderLineId(line.getSourceOrderLineId());
            copy.setSourceOrderId(line.getSourceOrderId());
            copy.setSourceOrderNo(line.getSourceOrderNo());
            PurchaseInvoiceLine saved = lineDao.addOrUpdate(copy);
            stored.add(saved == null ? copy : saved);
        }
        lines.setAll(stored);
    }

    private AccountPosting toAccountPosting(double amount) {
        AccountPosting posting = new AccountPosting();
        posting.setMovementDate(currentRow.getDocumentDate());
        posting.setDirection(AccountDirection.CREDIT);
        posting.setAccountCode(currentRow.getAccountCode());
        posting.setAccountName(currentRow.getAccountName());
        posting.setAmount(amount);
        posting.setDocumentNo(currentRow.getDocumentNo());
        posting.setSourceType(AccountPosting.SOURCE_PURCHASE_INVOICE);
        posting.setSourceId(currentRow.getRowId());
        posting.setNotes(currentRow.getNotes());
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

        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        if (items != null) {
            SatinCombos.fillItems(cmbItem, items.findActive());
        } else {
            SatinCombos.fill(cmbItem, List.of());
        }

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

        List<String> deliveryLabels = new ArrayList<>();
        List<PurchaseReceipt> deliveries = PurchaseDocs.invoiceableReceipts(
                receiptDao, receiptLineDao, invoiceDao, lineDao);
        for (PurchaseReceipt delivery : deliveries) {
            String name = delivery.getAccountName() == null ? "" : delivery.getAccountName();
            deliveryLabels.add(name.isBlank() ? delivery.getDocumentNo()
                    : delivery.getDocumentNo() + " — " + name);
        }
        SatinCombos.fill(cmbSourceReceipt, deliveryLabels);
        SatinCombos.fill(cmbReceipt, deliveryLabels);
        SatinCombos.fillVatRates(cmbVatRate);
        SatinCombos.selectVat(cmbVatRate, 20d);
    }

    private void applyItem(String itemCode) {
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (item != null && (txtUnit.getText() == null || txtUnit.getText().isBlank())) {
            txtUnit.setText(SatinDates.empty(item.getItemUnit()));
        }
        if (item != null) {
            SatinCombos.selectVat(cmbVatRate, item.getVatRate());
        }
    }

    private void refreshTotals() {
        if (lblNetTotal == null) {
            return;
        }
        lblNetTotal.setText("Ara toplam: " + SatinDates.money(PurchaseDocs.invoiceNetOf(lines)));
        lblVatTotal.setText("KDV: " + SatinDates.money(PurchaseDocs.invoiceVatOf(lines)));
        lblGrossTotal.setText("Genel toplam: " + SatinDates.money(PurchaseDocs.invoiceAmountOf(lines)));
    }

    private void applyReceiptDefaults() {
        if (applyingLookup) {
            return;
        }
        applyingLookup = true;
        try {
            PurchaseReceipt delivery = resolveReceipt(SatinCombos.codeOf(cmbReceipt));
            if (delivery != null && !delivery.canInvoice()) {
                delivery = null;
            }
            String itemCode = SatinCombos.codeOf(cmbItem);
            if (delivery != null) {
                applyReceiptHeader(delivery);
            }
            if (delivery != null && itemCode != null) {
                PurchaseReceiptLine deliveryLine = PurchaseDocs.findReceiptLine(
                        receiptLineDao, delivery.getRowId(), itemCode);
                if (deliveryLine != null) {
                    double allocated = PurchaseDocs.allocatedInvoiceQuantity(invoiceDao, lineDao,
                            deliveryLine.getRowId(), currentRow == null ? null : currentRow.getRowId());
                    allocated += qtyOnThisDocument(deliveryLine.getRowId(), null);
                    double remaining = PurchaseDocs.remainingOrdered(
                            deliveryLine.getQuantity() == null ? 0d : deliveryLine.getQuantity(), allocated);
                    if (txtQuantity.getText() == null || txtQuantity.getText().isBlank()) {
                        if (remaining > 0d) {
                            txtQuantity.setText(Double.toString(remaining));
                        }
                    }
                    if (txtUnit.getText() == null || txtUnit.getText().isBlank()) {
                        txtUnit.setText(SatinDates.empty(deliveryLine.getItemUnit()));
                    }
                    if (txtUnitPrice.getText() == null || txtUnitPrice.getText().isBlank()) {
                        txtUnitPrice.setText(deliveryLine.getUnitPrice() == null ? ""
                                : deliveryLine.getUnitPrice().toString());
                    }
                }
            }
            refreshRemainingHint();
        } finally {
            applyingLookup = false;
        }
    }

    private void applyReceiptHeader(PurchaseReceipt delivery) {
        if (delivery == null) {
            return;
        }
        if (SatinCombos.codeOf(cmbAccount) == null) {
            SatinCombos.select(cmbAccount, delivery.getAccountCode());
        }
        if (SatinCombos.codeOf(cmbWarehouse) == null) {
            SatinCombos.select(cmbWarehouse, delivery.getWarehouseCode());
        }
    }

    private void appendRemaining(PurchaseReceipt delivery, List<PurchaseReceiptLine> receiptLines) {
        if (delivery == null || receiptLines == null) {
            return;
        }
        int no = lines.size() + 1;
        Long exclude = currentRow == null ? null : currentRow.getRowId();
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        for (PurchaseReceiptLine source : receiptLines) {
            if (source == null || hasReceiptLine(source.getRowId())) {
                continue;
            }
            IStockItem item = items == null ? null : items.findByCode(source.getItemCode());
            double allocated = PurchaseDocs.allocatedInvoiceQuantity(invoiceDao, lineDao, source.getRowId(),
                    exclude);
            allocated += qtyOnThisDocument(source.getRowId(), null);
            double remaining = PurchaseDocs.remainingOrdered(
                    source.getQuantity() == null ? 0d : source.getQuantity(), allocated);
            if (remaining <= 0d) {
                continue;
            }
            PurchaseInvoiceLine line = new PurchaseInvoiceLine();
            line.setLineNo(no++);
            line.setItemCode(source.getItemCode());
            line.setItemDesc(source.getItemDesc());
            line.setItemUnit(source.getItemUnit());
            line.setQuantity(remaining);
            line.setUnitPrice(source.getUnitPrice());
            line.setVatRate(item == null ? 20d : item.vatPercent());
            line.setSourceReceiptLineId(source.getRowId());
            line.setSourceReceiptId(delivery.getRowId());
            line.setSourceReceiptNo(delivery.getDocumentNo());
            line.setSourceOrderLineId(source.getSourceOrderLineId());
            line.setSourceOrderId(source.getSourceOrderId());
            line.setSourceOrderNo(source.getSourceOrderNo());
            lines.add(line);
        }
        refreshTotals();
    }

    private String validateAllLines() {
        for (PurchaseInvoiceLine line : lines) {
            if (line == null || line.getQuantity() == null || line.getQuantity() <= 0d) {
                return "Miktar sıfırdan büyük olmalıdır.";
            }
            if (line.getSourceReceiptLineId() == null) {
                continue;
            }
            PurchaseReceiptLine deliveryLine = receiptLineDao.firstOrDefault(line.getSourceReceiptLineId());
            String error = validateQty(deliveryLine, line.getQuantity(), line);
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    private String validateQty(PurchaseReceiptLine deliveryLine, Double quantity, PurchaseInvoiceLine current) {
        if (deliveryLine == null) {
            return "Mal kabul satırı bulunamadı.";
        }
        if (quantity == null || quantity <= 0d) {
            return "Miktar sıfırdan büyük olmalıdır.";
        }
        double delivered = deliveryLine.getQuantity() == null ? 0d : deliveryLine.getQuantity();
        double allocated = PurchaseDocs.allocatedInvoiceQuantity(invoiceDao, lineDao, deliveryLine.getRowId(),
                currentRow == null ? null : currentRow.getRowId());
        allocated += qtyOnThisDocument(deliveryLine.getRowId(), current);
        double remaining = PurchaseDocs.remainingOrdered(delivered, allocated);
        if (quantity > remaining + 0.0000001d) {
            return deliveryLine.getItemCode() + " için kalan " + remaining;
        }
        return null;
    }

    private double qtyOnThisDocument(Long deliveryLineId, PurchaseInvoiceLine skip) {
        double sum = 0d;
        for (PurchaseInvoiceLine line : lines) {
            if (line == skip || line == null || deliveryLineId == null) {
                continue;
            }
            if (deliveryLineId.equals(line.getSourceReceiptLineId())) {
                sum += line.getQuantity() == null ? 0d : line.getQuantity();
            }
        }
        return sum;
    }

    private void stampHeaderDelivery(PurchaseInvoice row) {
        LinkedHashSet<String> numbers = new LinkedHashSet<>();
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (PurchaseInvoiceLine line : lines) {
            if (line == null) {
                continue;
            }
            if (line.getSourceReceiptNo() != null && !line.getSourceReceiptNo().isBlank()) {
                numbers.add(line.getSourceReceiptNo().trim());
            }
            if (line.getSourceReceiptId() != null) {
                ids.add(line.getSourceReceiptId());
            }
        }
        row.setSourceReceiptNo(joinNos(numbers));
        row.setSourceReceiptId(ids.size() == 1 ? ids.iterator().next() : null);
    }

    private static String joinNos(LinkedHashSet<String> numbers) {
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

    private PurchaseReceipt resolveReceipt(String documentNo) {
        return PurchaseDocs.findReceiptByNo(receiptDao, documentNo);
    }

    private boolean usableReceipt(PurchaseReceipt delivery) {
        if (delivery == null) {
            SatinDates.showError("Alım faturası", "Mal kabul bulunamadı", "Mal kabul numarasını kontrol edin.");
            return false;
        }
        if (!delivery.canInvoice()) {
            SatinDates.showError("Alım faturası", "Mal kabul uygun değil",
                    "Taslak veya iptal mal kabulden fatura yapılamaz.");
            return false;
        }
        return true;
    }

    private boolean hasReceiptLine(Long deliveryLineId) {
        if (deliveryLineId == null) {
            return false;
        }
        for (PurchaseInvoiceLine line : lines) {
            if (line != null && deliveryLineId.equals(line.getSourceReceiptLineId())) {
                return true;
            }
        }
        return false;
    }

    private void refreshRemainingHint() {
        if (lblRemaining == null) {
            return;
        }
        PurchaseReceipt delivery = resolveReceipt(SatinCombos.codeOf(cmbReceipt));
        String itemCode = SatinCombos.codeOf(cmbItem);
        if (delivery == null || itemCode == null) {
            lblRemaining.setText("");
            return;
        }
        PurchaseReceiptLine deliveryLine = PurchaseDocs.findReceiptLine(
                receiptLineDao, delivery.getRowId(), itemCode);
        if (deliveryLine == null) {
            lblRemaining.setText("Mal kabulde yok");
            return;
        }
        double allocated = PurchaseDocs.allocatedInvoiceQuantity(invoiceDao, lineDao, deliveryLine.getRowId(),
                currentRow == null ? null : currentRow.getRowId());
        allocated += qtyOnThisDocument(deliveryLine.getRowId(), null);
        double remaining = PurchaseDocs.remainingOrdered(
                deliveryLine.getQuantity() == null ? 0d : deliveryLine.getQuantity(), allocated);
        lblRemaining.setText("Kalan " + remaining);
    }

    private void applyLock(int status) {
        boolean draft = status == PurchaseInvoice.STATUS_DRAFT;
        boolean posted = status == PurchaseInvoice.STATUS_POSTED;
        txtDocumentNo.setDisable(!draft);
        dtDocumentDate.setDisable(!draft);
        cmbAccount.setDisable(!draft);
        cmbWarehouse.setDisable(!draft);
        cmbSourceReceipt.setDisable(!draft);
        btnLoadRemaining.setDisable(!draft);
        txtNotes.setDisable(!draft);
        cmbReceipt.setDisable(!draft);
        cmbItem.setDisable(!draft);
        txtQuantity.setDisable(!draft);
        txtUnit.setDisable(!draft);
        txtUnitPrice.setDisable(!draft);
        cmbVatRate.setDisable(!draft);
        btnAddLine.setDisable(!draft);
        btnRemoveLine.setDisable(!draft);
        tblvLines.setEditable(draft);
        btnSave.setDisable(!draft);
        btnApprove.setDisable(!draft);
        btnCancelPosted.setDisable(!posted);
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

    private static StringConverter<Double> qtyConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Double value) {
                return value == null ? "" : value.toString();
            }

            @Override
            public Double fromString(String text) {
                return SatinDates.parseDouble(text);
            }
        };
    }
}

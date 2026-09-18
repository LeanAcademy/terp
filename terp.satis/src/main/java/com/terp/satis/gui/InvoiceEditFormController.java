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
import com.terp.plugin.data.WorkingPeriod;
import com.terp.plugin.gui.DocumentPrint;
import com.terp.plugin.gui.RecordAuditBar;
import com.terp.satis.data.SalesDelivery;
import com.terp.satis.data.SalesDeliveryLine;
import com.terp.satis.data.SalesDocs;
import com.terp.satis.data.SalesInvoice;
import com.terp.satis.data.SalesInvoiceLine;
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

public class InvoiceEditFormController implements Initializable {

    @FXML
    private Button btnSave;
    @FXML
    private Button btnApprove;
    @FXML
    private Button btnCancelPosted;
    @FXML
    private Button btnPrint;
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
    private ComboBox<String> cmbSourceDelivery;
    @FXML
    private Button btnLoadRemaining;
    @FXML
    private TextArea txtNotes;
    @FXML
    private ComboBox<String> cmbDelivery;
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
    private TableView<SalesInvoiceLine> tblvLines;
    @FXML
    private TableColumn<SalesInvoiceLine, String> tcSourceDeliveryNo;
    @FXML
    private TableColumn<SalesInvoiceLine, String> tcItemCode;
    @FXML
    private TableColumn<SalesInvoiceLine, String> tcItemDesc;
    @FXML
    private TableColumn<SalesInvoiceLine, String> tcItemUnit;
    @FXML
    private TableColumn<SalesInvoiceLine, Double> tcQuantity;
    @FXML
    private TableColumn<SalesInvoiceLine, Double> tcUnitPrice;
    @FXML
    private TableColumn<SalesInvoiceLine, Double> tcVatRate;
    @FXML
    private TableColumn<SalesInvoiceLine, Double> tcNetAmount;
    @FXML
    private TableColumn<SalesInvoiceLine, Double> tcVatAmount;
    @FXML
    private TableColumn<SalesInvoiceLine, Double> tcGrossAmount;
    @FXML
    private Label lblNetTotal;
    @FXML
    private Label lblVatTotal;
    @FXML
    private Label lblGrossTotal;

    private ICommonDao<SalesInvoice> invoiceDao;
    private ICommonDao<SalesInvoiceLine> lineDao;
    private ICommonDao<SalesDelivery> deliveryDao;
    private ICommonDao<SalesDeliveryLine> deliveryLineDao;
    private SalesInvoice currentRow;
    private final ObservableList<SalesInvoiceLine> lines = FXCollections.observableArrayList();
    private boolean applyingLookup;
    private Runnable afterChange;
    private boolean notifying;
    private RecordAuditBar auditBar;

    public void initializeForm(SalesInvoice row) {
        fillLookups();
        lines.clear();
        txtDocumentNo.clear();
        txtOfficialNo.clear();
        txtNotes.clear();
        txtQuantity.clear();
        txtUnit.clear();
        txtUnitPrice.clear();
        SatisCombos.selectVat(cmbVatRate, 20d);
        if (lblRemaining != null) {
            lblRemaining.setText("");
        }
        cmbItem.getSelectionModel().clearSelection();
        cmbAccount.getSelectionModel().clearSelection();
        cmbWarehouse.getSelectionModel().clearSelection();
        cmbSourceDelivery.getSelectionModel().clearSelection();
        cmbDelivery.getSelectionModel().clearSelection();
        dtDocumentDate.setValue(LocalDate.now());
        this.currentRow = row;
        if (row == null) {
            applyLock(SalesInvoice.STATUS_DRAFT);
            refreshTotals();
            showAudit();
            return;
        }
        txtDocumentNo.setText(SatisDates.empty(row.getDocumentNo()));
        txtOfficialNo.setText(SatisDates.empty(row.getOfficialNo()));
        dtDocumentDate.setValue(SatisDates.toLocalDate(row.getDocumentDate()));
        SatisCombos.select(cmbAccount, row.getAccountCode());
        SatisCombos.select(cmbWarehouse, row.getWarehouseCode());
        txtNotes.setText(SatisDates.empty(row.getNotes()));
        if (row.getRowId() != null) {
            List<SalesInvoiceLine> stored = lineDao.findAll(
                    "from SalesInvoiceLine e where e.invoiceId = " + row.getRowId()
                            + " order by e.lineNo");
            if (stored != null) {
                lines.setAll(stored);
            }
        }
        refreshTotals();
        applyLock(row.getStatus());
        showAudit();
    }

    public void initializeFromDelivery(SalesDelivery delivery, List<SalesDeliveryLine> deliveryLines) {
        fillLookups();
        this.currentRow = invoiceDao.getEmpty();
        if (this.currentRow == null) {
            this.currentRow = new SalesInvoice();
        }
        this.currentRow.setStatus(SalesInvoice.STATUS_DRAFT);
        this.currentRow.setAccountCode(delivery.getAccountCode());
        this.currentRow.setAccountName(delivery.getAccountName());
        this.currentRow.setWarehouseCode(delivery.getWarehouseCode());
        this.currentRow.setWarehouseName(delivery.getWarehouseName());
        this.currentRow.setNotes(delivery.getNotes());
        dtDocumentDate.setValue(LocalDate.now());
        txtDocumentNo.clear();
        txtOfficialNo.clear();
        SatisCombos.select(cmbAccount, delivery.getAccountCode());
        SatisCombos.select(cmbWarehouse, delivery.getWarehouseCode());
        SatisCombos.select(cmbSourceDelivery, delivery.getDocumentNo());
        SatisCombos.select(cmbDelivery, delivery.getDocumentNo());
        txtNotes.setText(SatisDates.empty(delivery.getNotes()));
        lines.clear();
        appendRemaining(delivery, deliveryLines);
        applyLock(SalesInvoice.STATUS_DRAFT);
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
            SatisDates.showError("Satış faturası", "Cari eklentisi yok",
                    "Onay için terp.cari yüklü olmalıdır.");
            return;
        }
        double amount = SalesDocs.amountOf(lines);
        if (amount <= 0d) {
            SatisDates.showError("Satış faturası", "Tutar sıfır",
                    "Cari fişi için satırlarda birim fiyat girin.");
            return;
        }
        try {
            ledger.post(toAccountPosting(amount));
            currentRow.setStatus(SalesInvoice.STATUS_POSTED);
            currentRow.setLastUpdateDate(new Date());
            invoiceDao.addOrUpdate(currentRow);
            SalesDocs.syncDeliveriesForLines(deliveryDao, deliveryLineDao, invoiceDao, lineDao,
                    currentRow.getSourceDeliveryId(), lines);
            close();
        } catch (RuntimeException ex) {
            ledger.reverseByDocument(AccountPosting.SOURCE_SALES_INVOICE, currentRow.getRowId());
            SatisDates.showError("Satış faturası", "Cari fişi yazılamadı",
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
            SatisDates.showError("Satış faturası", "Cari eklentisi yok",
                    "İptal için terp.cari yüklü olmalıdır.");
            return;
        }
        ledger.reverseByDocument(AccountPosting.SOURCE_SALES_INVOICE, currentRow.getRowId());
        Long deliveryId = currentRow.getSourceDeliveryId();
        currentRow.setStatus(SalesInvoice.STATUS_CANCELLED);
        currentRow.setLastUpdateDate(new Date());
        invoiceDao.addOrUpdate(currentRow);
        SalesDocs.syncDeliveriesForLines(deliveryDao, deliveryLineDao, invoiceDao, lineDao,
                deliveryId, lines);
        close();
    }

    @FXML
    private void onActionBtnPrint(ActionEvent event) {
        if (currentRow == null) {
            return;
        }
        StringBuilder linesText = new StringBuilder();
        for (SalesInvoiceLine line : lines) {
            if (line == null) {
                continue;
            }
            linesText.append(line.getItemCode()).append("  ").append(line.getItemDesc())
                    .append("  ").append(line.getQuantity()).append('\n');
        }
        java.util.Map<String, String> values = new java.util.LinkedHashMap<>();
        values.put("DOCUMENT_NO", SatisDates.empty(currentRow.getDocumentNo()));
        values.put("ACCOUNT", SatisDates.empty(currentRow.getAccountName()));
        values.put("DATE", currentRow.getDateLabel());
        values.put("LINES", linesText.toString());
        values.put("TOTAL", "");
        javafx.stage.Window owner = btnPrint.getScene() == null ? null : btnPrint.getScene().getWindow();
        DocumentPrint.print(owner, "SATIS_FATURA", "Fatura " + SatisDates.empty(currentRow.getDocumentNo()),
                "Fatura {DOCUMENT_NO}\nCari: {ACCOUNT}\nTarih: {DATE}\n\n{LINES}\nToplam: {TOTAL}", values);
    }

    @FXML
    private void onActionBtnClose(ActionEvent event) {
        close();
    }

    @FXML
    private void onActionBtnLoadRemaining(ActionEvent event) {
        SalesDelivery delivery = resolveDelivery(SatisCombos.codeOf(cmbSourceDelivery));
        if (!usableDelivery(delivery)) {
            return;
        }
        applyDeliveryHeader(delivery);
        appendRemaining(delivery, SalesDocs.remainingInvoiceLines(delivery.getRowId(), deliveryLineDao,
                invoiceDao, lineDao, currentRow == null ? null : currentRow.getRowId()));
        refreshRemainingHint();
    }

    @FXML
    private void onActionBtnAddLine(ActionEvent event) {
        SalesInvoiceLine selected = tblvLines.getSelectionModel().getSelectedItem();
        String deliveryNo = SatisCombos.codeOf(cmbDelivery);
        String itemCode = SatisCombos.codeOf(cmbItem);
        Double quantity = SatisDates.parseDouble(txtQuantity.getText());
        Double price = SatisDates.parseDouble(txtUnitPrice.getText());
        Double vatRate = SatisCombos.vatRateOf(cmbVatRate);
        if (itemCode == null || quantity == null || quantity <= 0d) {
            SatisDates.showError("Satış faturası", "Satır eksik", "Malzeme ve miktar zorunludur.");
            return;
        }
        if (vatRate == null) {
            SatisDates.showError("Satış faturası", "KDV eksik", "KDV oranı seçin.");
            return;
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (SatisDates.rejectIfNotSellable("Satış faturası", item)) {
            return;
        }
        SalesDelivery delivery = null;
        SalesDeliveryLine deliveryLine = null;
        if (deliveryNo != null) {
            delivery = resolveDelivery(deliveryNo);
            boolean sameSource = selected != null && delivery != null
                    && delivery.getRowId() != null && delivery.getRowId().equals(selected.getSourceDeliveryId());
            if (!sameSource && !usableDelivery(delivery)) {
                return;
            }
            deliveryLine = SalesDocs.findDeliveryLine(deliveryLineDao, delivery.getRowId(), itemCode);
            if (deliveryLine == null) {
                SatisDates.showError("Satış faturası", "İrsaliye satırı yok",
                        itemCode + " bu irsaliyede yok: " + delivery.getDocumentNo());
                return;
            }
            String error = validateQty(deliveryLine, quantity, selected);
            if (error != null) {
                SatisDates.showError("Satış faturası", "Miktar", error);
                return;
            }
        }
        SalesInvoiceLine line = selected != null ? selected : new SalesInvoiceLine();
        line.setItemCode(itemCode);
        line.setItemDesc(item == null ? SatisCombos.nameOf(cmbItem) : item.getItemDesc());
        String unit = SatisDates.trimToNull(txtUnit.getText());
        line.setItemUnit(unit == null && item != null ? item.getItemUnit() : unit);
        line.setQuantity(quantity);
        line.setUnitPrice(price);
        line.setVatRate(vatRate);
        if (delivery != null) {
            line.setSourceDeliveryId(delivery.getRowId());
            line.setSourceDeliveryNo(delivery.getDocumentNo());
            line.setSourceDeliveryLineId(deliveryLine.getRowId());
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
            applyDeliveryHeader(delivery);
        } else {
            line.setSourceDeliveryId(null);
            line.setSourceDeliveryNo(null);
            line.setSourceDeliveryLineId(null);
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
        tblvLines.getSelectionModel().clearSelection();
        refreshRemainingHint();
        refreshTotals();
    }

    @FXML
    private void onActionBtnRemoveLine(ActionEvent event) {
        SalesInvoiceLine selected = tblvLines.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lines.remove(selected);
            int no = 1;
            for (SalesInvoiceLine line : lines) {
                line.setLineNo(no++);
            }
            refreshRemainingHint();
            refreshTotals();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.invoiceDao = TerpApplication.getInstance().getPersistence().createDao(SalesInvoice.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(SalesInvoiceLine.class);
        this.deliveryDao = TerpApplication.getInstance().getPersistence().createDao(SalesDelivery.class);
        this.deliveryLineDao = TerpApplication.getInstance().getPersistence()
                .createDao(SalesDeliveryLine.class);
        this.tcSourceDeliveryNo.setCellValueFactory(new PropertyValueFactory<>("sourceDeliveryNo"));
        this.tcItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        NumericCells.apply(this.tcQuantity);
        this.tcUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        NumericCells.apply(this.tcUnitPrice);
        this.tcVatRate.setCellValueFactory(new PropertyValueFactory<>("vatRate"));
        NumericCells.apply(this.tcVatRate);
        this.tcNetAmount.setCellValueFactory(new PropertyValueFactory<>("netAmount"));
        NumericCells.apply(this.tcNetAmount);
        this.tcVatAmount.setCellValueFactory(new PropertyValueFactory<>("vatAmount"));
        NumericCells.apply(this.tcVatAmount);
        this.tcGrossAmount.setCellValueFactory(new PropertyValueFactory<>("grossAmount"));
        NumericCells.apply(this.tcGrossAmount);
        NumericCells.applyEditable(this.tcQuantity);
        this.tcQuantity.setOnEditCommit(event -> {
            SalesInvoiceLine line = event.getRowValue();
            Double value = event.getNewValue();
            if (line == null) {
                return;
            }
            if (value == null || value <= 0d) {
                SatisDates.showError("Satış faturası", "Miktar", "Miktar sıfırdan büyük olmalıdır.");
                tblvLines.refresh();
                return;
            }
            if (line.getSourceDeliveryLineId() != null) {
                SalesDeliveryLine deliveryLine = deliveryLineDao.firstOrDefault(line.getSourceDeliveryLineId());
                String error = validateQty(deliveryLine, value, line);
                if (error != null) {
                    SatisDates.showError("Satış faturası", "Miktar", error);
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
        this.tblvLines.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                loadLineEditor(selected);
            }
        });
        cmbItem.valueProperty().addListener((obs, old, value) -> {
            applyItem(SatisCombos.codeOf(cmbItem));
            applyDeliveryDefaults();
        });
        cmbDelivery.valueProperty().addListener((obs, old, value) -> applyDeliveryDefaults());
        fillLookups();
        if (dtDocumentDate.getValue() == null) {
            dtDocumentDate.setValue(LocalDate.now());
        }
        applyLock(SalesInvoice.STATUS_DRAFT);
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
            SatisDates.showError("Satış faturası", "Müşteri eksik", "Müşteri seçin.");
            return false;
        }
        String periodError = WorkingPeriod.rejectIfOutside(SatisDates.toDate(dtDocumentDate.getValue()));
        if (periodError != null) {
            SatisDates.showError("Satış faturası", "Dönem", periodError);
            return false;
        }
        if (lines.isEmpty()) {
            SatisDates.showError("Satış faturası", "Satır yok", "En az bir malzeme satırı ekleyin.");
            return false;
        }
        String qtyError = validateAllLines();
        if (qtyError != null) {
            SatisDates.showError("Satış faturası", "Miktar", qtyError);
            return false;
        }
        IItemLookup itemLookup = TerpApplication.getInstance().getItemLookup();
        for (SalesInvoiceLine line : lines) {
            if (line == null) {
                continue;
            }
            IStockItem item = itemLookup == null ? null : itemLookup.findByCode(line.getItemCode());
            if (SatisDates.rejectIfNotSellable("Satış faturası", item)) {
                return false;
            }
        }
        Date now = new Date();
        SalesInvoice row = this.currentRow;
        if (row == null) {
            row = invoiceDao.getEmpty();
            if (row == null) {
                SatisDates.showError("Satış faturası", "Kayıt hatası", "Belge oluşturulamadı.");
                return false;
            }
            row.setAddedDate(now);
            row.setStatus(SalesInvoice.STATUS_DRAFT);
        }
        if (!row.isDraft() && !row.isCancelled()) {
            SatisDates.showError("Satış faturası", "Kilitli belge",
                    "Onaylı belge taslak olarak kaydedilemez.");
            return false;
        }
        if (!CompanyScope.stamp(row)) {
            SatisDates.showError("Satış faturası", "Firma",
                    "Önce araç çubuğundan çalışma firmasını seçin.");
            return false;
        }
        stampHeaderDelivery(row);
        String documentNo = SatisDates.assignDocumentNo(DocumentNumbers.SALES_INVOICE, txtDocumentNo);
        if (documentNo == null || documentNo.isBlank()) {
            SatisDates.showError("Satış faturası", "Sistem no",
                    "Sistem no üretilemedi. Elle yazın veya Sistem yönetimi → Belge numaraları serisini kontrol edin.");
            return false;
        }
        row.setDocumentNo(documentNo);
        row.setOfficialNo(SatisDates.trimToNull(txtOfficialNo.getText()));
        row.setDocumentDate(SatisDates.toDate(dtDocumentDate.getValue()));
        row.setAccountCode(accountCode);
        IAccountLookup accounts = TerpApplication.getInstance().getAccountLookup();
        IAccount account = accounts == null ? null : accounts.findByCode(accountCode);
        row.setAccountName(account == null ? SatisCombos.nameOf(cmbAccount) : account.getAccountName());
        String warehouseCode = SatisCombos.codeOf(cmbWarehouse);
        row.setWarehouseCode(warehouseCode);
        IWarehouseLookup warehouses = TerpApplication.getInstance().getWarehouseLookup();
        IStockWarehouse warehouse = warehouseCode == null || warehouses == null
                ? null : warehouses.findByCode(warehouseCode);
        row.setWarehouseName(warehouse == null ? SatisCombos.nameOf(cmbWarehouse) : warehouse.getWarehouseName());
        row.setNotes(SatisDates.trimToNull(txtNotes.getText()));
        row.setLastUpdateDate(now);
        if (row.getAddedDate() == null) {
            row.setAddedDate(now);
        }
        if (CompanyScope.rejectDuplicate(
                CompanyScope.findDuplicate(invoiceDao, "SalesInvoice", "documentNo",
                        row.getDocumentNo(), row.getRowId()),
                "Sistem numarası tekrar ediyor")) {
            return false;
        }
        SalesInvoice saved = invoiceDao.addOrUpdate(row);
        if (saved == null || saved.getRowId() == null) {
            SatisDates.showError("Satış faturası", "Kayıt hatası", "Belge kaydedilemedi.");
            return false;
        }
        this.currentRow = saved;
        replaceLines(saved.getRowId(), now);
        SalesDocs.syncDeliveriesForLines(deliveryDao, deliveryLineDao, invoiceDao, lineDao,
                saved.getSourceDeliveryId(), lines);
        showAudit();
        return true;
    }

    private void replaceLines(Long invoiceId, Date now) {
        SalesDocs.deleteByParent(lineDao, "SalesInvoiceLine", "invoiceId", invoiceId);
        int no = 1;
        List<SalesInvoiceLine> stored = new ArrayList<>();
        for (SalesInvoiceLine line : lines) {
            SalesInvoiceLine copy = lineDao.getEmpty();
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
            copy.setSourceDeliveryLineId(line.getSourceDeliveryLineId());
            copy.setSourceDeliveryId(line.getSourceDeliveryId());
            copy.setSourceDeliveryNo(line.getSourceDeliveryNo());
            copy.setSourceOrderLineId(line.getSourceOrderLineId());
            copy.setSourceOrderId(line.getSourceOrderId());
            copy.setSourceOrderNo(line.getSourceOrderNo());
            SalesInvoiceLine saved = lineDao.addOrUpdate(copy);
            stored.add(saved == null ? copy : saved);
        }
        lines.setAll(stored);
    }

    private AccountPosting toAccountPosting(double amount) {
        AccountPosting posting = new AccountPosting();
        posting.setMovementDate(currentRow.getDocumentDate());
        posting.setDirection(AccountDirection.DEBIT);
        posting.setAccountCode(currentRow.getAccountCode());
        posting.setAccountName(currentRow.getAccountName());
        posting.setAmount(amount);
        posting.setDocumentNo(currentRow.getDocumentNo());
        posting.setSourceType(AccountPosting.SOURCE_SALES_INVOICE);
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

        List<String> deliveryLabels = new ArrayList<>();
        List<SalesDelivery> deliveries = SalesDocs.invoiceableDeliveries(
                deliveryDao, deliveryLineDao, invoiceDao, lineDao);
        for (SalesDelivery delivery : deliveries) {
            String name = delivery.getAccountName() == null ? "" : delivery.getAccountName();
            deliveryLabels.add(name.isBlank() ? delivery.getDocumentNo()
                    : delivery.getDocumentNo() + " — " + name);
        }
        SatisCombos.fill(cmbSourceDelivery, deliveryLabels);
        SatisCombos.fill(cmbDelivery, deliveryLabels);
        SatisCombos.fillVatRates(cmbVatRate);
        SatisCombos.selectVat(cmbVatRate, 20d);
    }

    private void loadLineEditor(SalesInvoiceLine line) {
        applyingLookup = true;
        try {
            SatisCombos.select(cmbDelivery, line.getSourceDeliveryNo());
            SatisCombos.select(cmbItem, line.getItemCode());
            txtUnit.setText(SatisDates.empty(line.getItemUnit()));
            txtQuantity.setText(NumericCells.text(line.getQuantity()));
            txtUnitPrice.setText(NumericCells.text(line.getUnitPrice()));
            SatisCombos.selectVat(cmbVatRate, line.getVatRate());
            refreshRemainingHint();
        } finally {
            applyingLookup = false;
        }
    }

    private void applyItem(String itemCode) {
        if (applyingLookup) {
            return;
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (item != null && (txtUnit.getText() == null || txtUnit.getText().isBlank())) {
            txtUnit.setText(SatisDates.empty(item.getItemUnit()));
        }
        if (item != null) {
            SatisCombos.selectVat(cmbVatRate, item.getVatRate());
        }
    }

    private void refreshTotals() {
        if (lblNetTotal == null) {
            return;
        }
        lblNetTotal.setText("Ara toplam: " + SatisDates.money(SalesDocs.netOf(lines)));
        lblVatTotal.setText("KDV: " + SatisDates.money(SalesDocs.vatOf(lines)));
        lblGrossTotal.setText("Genel toplam: " + SatisDates.money(SalesDocs.amountOf(lines)));
    }

    private void applyDeliveryDefaults() {
        if (applyingLookup) {
            return;
        }
        applyingLookup = true;
        try {
            SalesDelivery delivery = resolveDelivery(SatisCombos.codeOf(cmbDelivery));
            if (delivery != null && !delivery.canInvoice()) {
                delivery = null;
            }
            String itemCode = SatisCombos.codeOf(cmbItem);
            if (delivery != null) {
                applyDeliveryHeader(delivery);
            }
            if (delivery != null && itemCode != null) {
                SalesDeliveryLine deliveryLine = SalesDocs.findDeliveryLine(
                        deliveryLineDao, delivery.getRowId(), itemCode);
                if (deliveryLine != null) {
                    double allocated = SalesDocs.allocatedInvoiceQuantity(invoiceDao, lineDao,
                            deliveryLine.getRowId(), currentRow == null ? null : currentRow.getRowId());
                    allocated += qtyOnThisDocument(deliveryLine.getRowId(), null);
                    double remaining = SalesDocs.remainingOrdered(
                            deliveryLine.getQuantity() == null ? 0d : deliveryLine.getQuantity(), allocated);
                    if (txtQuantity.getText() == null || txtQuantity.getText().isBlank()) {
                        if (remaining > 0d) {
                            txtQuantity.setText(NumericCells.format(remaining));
                        }
                    }
                    if (txtUnit.getText() == null || txtUnit.getText().isBlank()) {
                        txtUnit.setText(SatisDates.empty(deliveryLine.getItemUnit()));
                    }
                    if (txtUnitPrice.getText() == null || txtUnitPrice.getText().isBlank()) {
                        txtUnitPrice.setText(NumericCells.text(deliveryLine.getUnitPrice()));
                    }
                }
            }
            refreshRemainingHint();
        } finally {
            applyingLookup = false;
        }
    }

    private void applyDeliveryHeader(SalesDelivery delivery) {
        if (delivery == null) {
            return;
        }
        if (SatisCombos.codeOf(cmbAccount) == null) {
            SatisCombos.select(cmbAccount, delivery.getAccountCode());
        }
        if (SatisCombos.codeOf(cmbWarehouse) == null) {
            SatisCombos.select(cmbWarehouse, delivery.getWarehouseCode());
        }
    }

    private void appendRemaining(SalesDelivery delivery, List<SalesDeliveryLine> deliveryLines) {
        if (delivery == null || deliveryLines == null) {
            return;
        }
        int no = lines.size() + 1;
        Long exclude = currentRow == null ? null : currentRow.getRowId();
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        for (SalesDeliveryLine source : deliveryLines) {
            if (source == null || hasDeliveryLine(source.getRowId())) {
                continue;
            }
            IStockItem item = items == null ? null : items.findByCode(source.getItemCode());
            if (item != null && !item.isSellable()) {
                continue;
            }
            double allocated = SalesDocs.allocatedInvoiceQuantity(invoiceDao, lineDao, source.getRowId(),
                    exclude);
            allocated += qtyOnThisDocument(source.getRowId(), null);
            double remaining = SalesDocs.remainingOrdered(
                    source.getQuantity() == null ? 0d : source.getQuantity(), allocated);
            if (remaining <= 0d) {
                continue;
            }
            SalesInvoiceLine line = new SalesInvoiceLine();
            line.setLineNo(no++);
            line.setItemCode(source.getItemCode());
            line.setItemDesc(source.getItemDesc());
            line.setItemUnit(source.getItemUnit());
            line.setQuantity(remaining);
            line.setUnitPrice(source.getUnitPrice());
            line.setVatRate(item == null ? 20d : item.vatPercent());
            line.setSourceDeliveryLineId(source.getRowId());
            line.setSourceDeliveryId(delivery.getRowId());
            line.setSourceDeliveryNo(delivery.getDocumentNo());
            line.setSourceOrderLineId(source.getSourceOrderLineId());
            line.setSourceOrderId(source.getSourceOrderId());
            line.setSourceOrderNo(source.getSourceOrderNo());
            lines.add(line);
        }
        refreshTotals();
    }

    private String validateAllLines() {
        for (SalesInvoiceLine line : lines) {
            if (line == null || line.getQuantity() == null || line.getQuantity() <= 0d) {
                return "Miktar sıfırdan büyük olmalıdır.";
            }
            if (line.getSourceDeliveryLineId() == null) {
                continue;
            }
            SalesDeliveryLine deliveryLine = deliveryLineDao.firstOrDefault(line.getSourceDeliveryLineId());
            String error = validateQty(deliveryLine, line.getQuantity(), line);
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    private String validateQty(SalesDeliveryLine deliveryLine, Double quantity, SalesInvoiceLine current) {
        if (deliveryLine == null) {
            return "İrsaliye satırı bulunamadı.";
        }
        if (quantity == null || quantity <= 0d) {
            return "Miktar sıfırdan büyük olmalıdır.";
        }
        double delivered = deliveryLine.getQuantity() == null ? 0d : deliveryLine.getQuantity();
        double allocated = SalesDocs.allocatedInvoiceQuantity(invoiceDao, lineDao, deliveryLine.getRowId(),
                currentRow == null ? null : currentRow.getRowId());
        allocated += qtyOnThisDocument(deliveryLine.getRowId(), current);
        double remaining = SalesDocs.remainingOrdered(delivered, allocated);
        if (quantity > remaining + 0.0000001d) {
            return deliveryLine.getItemCode() + " için kalan " + remaining;
        }
        return null;
    }

    private double qtyOnThisDocument(Long deliveryLineId, SalesInvoiceLine skip) {
        double sum = 0d;
        for (SalesInvoiceLine line : lines) {
            if (line == skip || line == null || deliveryLineId == null) {
                continue;
            }
            if (deliveryLineId.equals(line.getSourceDeliveryLineId())) {
                sum += line.getQuantity() == null ? 0d : line.getQuantity();
            }
        }
        return sum;
    }

    private void stampHeaderDelivery(SalesInvoice row) {
        LinkedHashSet<String> numbers = new LinkedHashSet<>();
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (SalesInvoiceLine line : lines) {
            if (line == null) {
                continue;
            }
            if (line.getSourceDeliveryNo() != null && !line.getSourceDeliveryNo().isBlank()) {
                numbers.add(line.getSourceDeliveryNo().trim());
            }
            if (line.getSourceDeliveryId() != null) {
                ids.add(line.getSourceDeliveryId());
            }
        }
        row.setSourceDeliveryNo(joinNos(numbers));
        row.setSourceDeliveryId(ids.size() == 1 ? ids.iterator().next() : null);
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

    private SalesDelivery resolveDelivery(String documentNo) {
        return SalesDocs.findDeliveryByNo(deliveryDao, documentNo);
    }

    private boolean usableDelivery(SalesDelivery delivery) {
        if (delivery == null) {
            SatisDates.showError("Satış faturası", "İrsaliye bulunamadı", "İrsaliye numarasını kontrol edin.");
            return false;
        }
        if (!delivery.canInvoice()) {
            SatisDates.showError("Satış faturası", "İrsaliye uygun değil",
                    "Taslak veya iptal irsaliyeden fatura yapılamaz.");
            return false;
        }
        return true;
    }

    private boolean hasDeliveryLine(Long deliveryLineId) {
        if (deliveryLineId == null) {
            return false;
        }
        for (SalesInvoiceLine line : lines) {
            if (line != null && deliveryLineId.equals(line.getSourceDeliveryLineId())) {
                return true;
            }
        }
        return false;
    }

    private void refreshRemainingHint() {
        if (lblRemaining == null) {
            return;
        }
        SalesDelivery delivery = resolveDelivery(SatisCombos.codeOf(cmbDelivery));
        String itemCode = SatisCombos.codeOf(cmbItem);
        if (delivery == null || itemCode == null) {
            lblRemaining.setText("");
            return;
        }
        SalesDeliveryLine deliveryLine = SalesDocs.findDeliveryLine(
                deliveryLineDao, delivery.getRowId(), itemCode);
        if (deliveryLine == null) {
            lblRemaining.setText("İrsaliyede yok");
            return;
        }
        double allocated = SalesDocs.allocatedInvoiceQuantity(invoiceDao, lineDao, deliveryLine.getRowId(),
                currentRow == null ? null : currentRow.getRowId());
        allocated += qtyOnThisDocument(deliveryLine.getRowId(), null);
        double remaining = SalesDocs.remainingOrdered(
                deliveryLine.getQuantity() == null ? 0d : deliveryLine.getQuantity(), allocated);
        lblRemaining.setText("Kalan " + NumericCells.format(remaining));
    }

    private void applyLock(int status) {
        boolean draft = status == SalesInvoice.STATUS_DRAFT;
        boolean posted = status == SalesInvoice.STATUS_POSTED;
        boolean editable = draft || status == SalesInvoice.STATUS_CANCELLED;
        txtDocumentNo.setDisable(!editable);
        txtOfficialNo.setDisable(!editable);
        dtDocumentDate.setDisable(!editable);
        cmbAccount.setDisable(!editable);
        cmbWarehouse.setDisable(!editable);
        cmbSourceDelivery.setDisable(!editable);
        btnLoadRemaining.setDisable(!editable);
        txtNotes.setDisable(!editable);
        cmbDelivery.setDisable(!editable);
        cmbItem.setDisable(!editable);
        txtQuantity.setDisable(!editable);
        txtUnit.setDisable(!editable);
        txtUnitPrice.setDisable(!editable);
        cmbVatRate.setDisable(!editable);
        btnAddLine.setDisable(!editable);
        btnRemoveLine.setDisable(!editable);
        tblvLines.setEditable(editable);
        btnSave.setDisable(!editable);
        btnApprove.setDisable(!editable);
        btnCancelPosted.setDisable(!posted);
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

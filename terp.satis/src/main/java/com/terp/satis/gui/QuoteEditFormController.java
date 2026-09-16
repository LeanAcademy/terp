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
import com.terp.plugin.data.IWarehouseLookup;
import com.terp.plugin.data.model.IAccount;
import com.terp.plugin.data.model.IStockItem;
import com.terp.plugin.data.model.IStockWarehouse;
import com.terp.plugin.gui.RecordAuditBar;
import com.terp.satis.data.SalesDocs;
import com.terp.satis.data.SalesOrder;
import com.terp.satis.data.SalesQuote;
import com.terp.satis.data.SalesQuoteLine;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class QuoteEditFormController implements Initializable {

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
    private ComboBox<String> cmbAccount;
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
    private TextField txtUnitPrice;
    @FXML
    private ComboBox<String> cmbVatRate;
    @FXML
    private Button btnAddLine;
    @FXML
    private Button btnRemoveLine;
    @FXML
    private TableView<SalesQuoteLine> tblvLines;
    @FXML
    private TableColumn<SalesQuoteLine, String> tcItemCode;
    @FXML
    private TableColumn<SalesQuoteLine, String> tcItemDesc;
    @FXML
    private TableColumn<SalesQuoteLine, String> tcItemUnit;
    @FXML
    private TableColumn<SalesQuoteLine, Double> tcQuantity;
    @FXML
    private TableColumn<SalesQuoteLine, Double> tcUnitPrice;
    @FXML
    private TableColumn<SalesQuoteLine, Double> tcVatRate;
    @FXML
    private TableColumn<SalesQuoteLine, Double> tcNetAmount;
    @FXML
    private TableColumn<SalesQuoteLine, Double> tcVatAmount;
    @FXML
    private TableColumn<SalesQuoteLine, Double> tcGrossAmount;
    @FXML
    private Label lblNetTotal;
    @FXML
    private Label lblVatTotal;
    @FXML
    private Label lblGrossTotal;

    private ICommonDao<SalesQuote> quoteDao;
    private ICommonDao<SalesQuoteLine> lineDao;
    private ICommonDao<SalesOrder> orderDao;
    private SalesQuote currentRow;
    private final ObservableList<SalesQuoteLine> lines = FXCollections.observableArrayList();
    private Runnable afterChange;
    private boolean notifying;
    private RecordAuditBar auditBar;

    public void initializeForm(SalesQuote row) {
        fillLookups();
        lines.clear();
        txtDocumentNo.clear();
        txtNotes.clear();
        txtQuantity.clear();
        txtUnit.clear();
        txtUnitPrice.clear();
        cmbItem.getSelectionModel().clearSelection();
        SatisCombos.selectVat(cmbVatRate, 20d);
        cmbAccount.getSelectionModel().clearSelection();
        cmbWarehouse.getSelectionModel().clearSelection();
        dtDocumentDate.setValue(LocalDate.now());
        this.currentRow = row;
        if (row == null) {
            applyLock(SalesQuote.STATUS_DRAFT);
            refreshTotals();
            showAudit();
            return;
        }
        txtDocumentNo.setText(SatisDates.empty(row.getDocumentNo()));
        dtDocumentDate.setValue(SatisDates.toLocalDate(row.getDocumentDate()));
        SatisCombos.select(cmbAccount, row.getAccountCode());
        SatisCombos.select(cmbWarehouse, row.getWarehouseCode());
        txtNotes.setText(SatisDates.empty(row.getNotes()));
        if (row.getRowId() != null) {
            List<SalesQuoteLine> stored = lineDao.findAll(
                    "from SalesQuoteLine e where e.quoteId = " + row.getRowId()
                            + " order by e.lineNo");
            if (stored != null) {
                lines.setAll(stored);
            }
        }
        refreshTotals();
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
        currentRow.setStatus(SalesQuote.STATUS_APPROVED);
        currentRow.setLastUpdateDate(new Date());
        quoteDao.addOrUpdate(currentRow);
        applyLock(currentRow.getStatus());
        showAudit();
        notifyChanged();
    }

    @FXML
    private void onActionBtnConvert(ActionEvent event) {
        if (currentRow == null || currentRow.getRowId() == null) {
            return;
        }
        SatisWindows.convertQuote(currentRow, quoteDao, lineDao, orderDao, () -> {
            SalesQuote reloaded = quoteDao.firstOrDefault(currentRow.getRowId());
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
        if (SalesDocs.hasOpenOrder(orderDao, currentRow.getRowId())) {
            SatisDates.showError("Satış teklifi", "Siparişe bağlı",
                    "Önce bağlı satış siparişini iptal veya silin.");
            return;
        }
        currentRow.setStatus(SalesQuote.STATUS_CANCELLED);
        currentRow.setLastUpdateDate(new Date());
        quoteDao.addOrUpdate(currentRow);
        close();
    }

    @FXML
    private void onActionBtnClose(ActionEvent event) {
        close();
    }

    @FXML
    private void onActionBtnAddLine(ActionEvent event) {
        String itemCode = SatisCombos.codeOf(cmbItem);
        Double quantity = SatisDates.parseDouble(txtQuantity.getText());
        Double price = SatisDates.parseDouble(txtUnitPrice.getText());
        Double vatRate = SatisCombos.vatRateOf(cmbVatRate);
        if (itemCode == null || quantity == null || quantity <= 0d) {
            SatisDates.showError("Satış teklifi", "Satır eksik", "Malzeme ve miktar zorunludur.");
            return;
        }
        if (vatRate == null) {
            SatisDates.showError("Satış teklifi", "KDV eksik", "KDV oranı seçin.");
            return;
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (SatisDates.rejectIfNotSellable("Satış teklifi", item)) {
            return;
        }
        SalesQuoteLine line = new SalesQuoteLine();
        line.setItemCode(itemCode);
        line.setItemDesc(item == null ? SatisCombos.nameOf(cmbItem) : item.getItemDesc());
        String unit = SatisDates.trimToNull(txtUnit.getText());
        line.setItemUnit(unit == null && item != null ? item.getItemUnit() : unit);
        line.setQuantity(quantity);
        line.setUnitPrice(price);
        line.setVatRate(vatRate);
        line.setLineNo(lines.size() + 1);
        lines.add(line);
        txtQuantity.clear();
        txtUnitPrice.clear();
        refreshTotals();
    }

    @FXML
    private void onActionBtnRemoveLine(ActionEvent event) {
        SalesQuoteLine selected = tblvLines.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lines.remove(selected);
            int no = 1;
            for (SalesQuoteLine line : lines) {
                line.setLineNo(no++);
            }
            refreshTotals();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.quoteDao = TerpApplication.getInstance().getPersistence().createDao(SalesQuote.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(SalesQuoteLine.class);
        this.orderDao = TerpApplication.getInstance().getPersistence().createDao(SalesOrder.class);
        this.tcItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        this.tcUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        this.tcVatRate.setCellValueFactory(new PropertyValueFactory<>("vatRate"));
        this.tcNetAmount.setCellValueFactory(new PropertyValueFactory<>("netAmount"));
        this.tcVatAmount.setCellValueFactory(new PropertyValueFactory<>("vatAmount"));
        this.tcGrossAmount.setCellValueFactory(new PropertyValueFactory<>("grossAmount"));
        this.tblvLines.setItems(lines);
        cmbItem.valueProperty().addListener((obs, old, value) -> applyItem(SatisCombos.codeOf(cmbItem)));
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
        String warehouseCode = SatisCombos.codeOf(cmbWarehouse);
        if (warehouseCode == null) {
            SatisDates.showError("Satış teklifi", "Depo eksik", "Depo seçin.");
            return false;
        }
        String accountCode = SatisCombos.codeOf(cmbAccount);
        if (accountCode == null) {
            SatisDates.showError("Satış teklifi", "Müşteri eksik", "Müşteri seçin.");
            return false;
        }
        if (lines.isEmpty()) {
            SatisDates.showError("Satış teklifi", "Satır yok", "En az bir malzeme satırı ekleyin.");
            return false;
        }
        IItemLookup lookup = TerpApplication.getInstance().getItemLookup();
        for (SalesQuoteLine line : lines) {
            if (line == null) {
                continue;
            }
            IStockItem item = lookup == null ? null : lookup.findByCode(line.getItemCode());
            if (SatisDates.rejectIfNotSellable("Satış teklifi", item)) {
                return false;
            }
        }
        Date now = new Date();
        SalesQuote row = this.currentRow;
        if (row == null) {
            row = quoteDao.getEmpty();
            if (row == null) {
                SatisDates.showError("Satış teklifi", "Kayıt hatası", "Belge oluşturulamadı.");
                return false;
            }
            row.setAddedDate(now);
            row.setStatus(SalesQuote.STATUS_DRAFT);
        }
        if (!row.isDraft()) {
            SatisDates.showError("Satış teklifi", "Kilitli belge",
                    "Onaylı veya iptal teklif taslak olarak kaydedilemez.");
            return false;
        }
        if (!CompanyScope.stamp(row)) {
            SatisDates.showError("Satış teklifi", "Firma",
                    "Önce araç çubuğundan çalışma firmasını seçin.");
            return false;
        }
        String documentNo = SatisDates.assignDocumentNo(DocumentNumbers.SALES_QUOTE, txtDocumentNo);
        if (documentNo == null || documentNo.isBlank()) {
            SatisDates.showError("Satış teklifi", "Belge no",
                    "Belge no üretilemedi. Elle yazın veya Sistem yönetimi → Belge numaraları serisini kontrol edin.");
            return false;
        }
        row.setDocumentNo(documentNo);
        row.setDocumentDate(SatisDates.toDate(dtDocumentDate.getValue()));
        row.setAccountCode(accountCode);
        IAccountLookup accounts = TerpApplication.getInstance().getAccountLookup();
        IAccount account = accounts == null ? null : accounts.findByCode(accountCode);
        row.setAccountName(account == null ? SatisCombos.nameOf(cmbAccount) : account.getAccountName());
        row.setWarehouseCode(warehouseCode);
        IWarehouseLookup warehouses = TerpApplication.getInstance().getWarehouseLookup();
        IStockWarehouse warehouse = warehouses == null ? null : warehouses.findByCode(warehouseCode);
        row.setWarehouseName(warehouse == null ? SatisCombos.nameOf(cmbWarehouse)
                : warehouse.getWarehouseName());
        row.setNotes(SatisDates.trimToNull(txtNotes.getText()));
        row.setLastUpdateDate(now);
        if (row.getAddedDate() == null) {
            row.setAddedDate(now);
        }
        if (CompanyScope.rejectDuplicate(
                CompanyScope.findDuplicate(quoteDao, "SalesQuote", "documentNo",
                        row.getDocumentNo(), row.getRowId()),
                "Belge numarası tekrar ediyor")) {
            return false;
        }
        SalesQuote saved = quoteDao.addOrUpdate(row);
        if (saved == null || saved.getRowId() == null) {
            SatisDates.showError("Satış teklifi", "Kayıt hatası", "Belge kaydedilemedi.");
            return false;
        }
        this.currentRow = saved;
        replaceLines(saved.getRowId(), now);
        showAudit();
        return true;
    }

    private void replaceLines(Long quoteId, Date now) {
        SalesDocs.deleteByParent(lineDao, "SalesQuoteLine", "quoteId", quoteId);
        int no = 1;
        List<SalesQuoteLine> stored = new ArrayList<>();
        for (SalesQuoteLine line : lines) {
            SalesQuoteLine copy = lineDao.getEmpty();
            if (copy == null) {
                continue;
            }
            copy.setAddedDate(now);
            copy.setLastUpdateDate(now);
            copy.setQuoteId(quoteId);
            copy.setLineNo(no++);
            copy.setItemCode(line.getItemCode());
            copy.setItemDesc(line.getItemDesc());
            copy.setItemUnit(line.getItemUnit());
            copy.setQuantity(line.getQuantity());
            copy.setUnitPrice(line.getUnitPrice());
            copy.setVatRate(line.getVatRate());
            SalesQuoteLine saved = lineDao.addOrUpdate(copy);
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
        SatisCombos.fillVatRates(cmbVatRate);
        SatisCombos.selectVat(cmbVatRate, 20d);
    }

    private void applyItem(String itemCode) {
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
        lblNetTotal.setText("Ara toplam: " + SatisDates.money(SalesDocs.quoteNetOf(lines)));
        lblVatTotal.setText("KDV: " + SatisDates.money(SalesDocs.quoteVatOf(lines)));
        lblGrossTotal.setText("Genel toplam: " + SatisDates.money(SalesDocs.quoteGrossOf(lines)));
    }

    private void applyLock(int status) {
        boolean draft = status == SalesQuote.STATUS_DRAFT;
        boolean approved = status == SalesQuote.STATUS_APPROVED;
        txtDocumentNo.setDisable(!draft);
        dtDocumentDate.setDisable(!draft);
        cmbAccount.setDisable(!draft);
        cmbWarehouse.setDisable(!draft);
        txtNotes.setDisable(!draft);
        cmbItem.setDisable(!draft);
        txtQuantity.setDisable(!draft);
        txtUnit.setDisable(!draft);
        txtUnitPrice.setDisable(!draft);
        cmbVatRate.setDisable(!draft);
        btnAddLine.setDisable(!draft);
        btnRemoveLine.setDisable(!draft);
        btnSave.setDisable(!draft);
        btnApprove.setDisable(!draft);
        btnConvert.setDisable(!approved);
        btnConvert.setVisible(approved || status == SalesQuote.STATUS_CONVERTED);
        btnConvert.setManaged(approved || status == SalesQuote.STATUS_CONVERTED);
        if (status == SalesQuote.STATUS_CONVERTED) {
            btnConvert.setDisable(true);
        }
        btnCancelPosted.setDisable(!approved);
        btnCancelPosted.setVisible(approved);
        btnCancelPosted.setManaged(approved);
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

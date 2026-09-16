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
import com.terp.satis.data.SalesDelivery;
import com.terp.satis.data.SalesDeliveryLine;
import com.terp.satis.data.SalesDocs;
import com.terp.satis.data.SalesOrder;
import com.terp.satis.data.SalesOrderLine;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class OrderEditFormController implements Initializable {

    @FXML
    private Button btnSave;
    @FXML
    private Button btnApprove;
    @FXML
    private Button btnDeliver;
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
    private TextField txtSourceQuoteNo;
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
    private Button btnAddLine;
    @FXML
    private Button btnRemoveLine;
    @FXML
    private TableView<SalesOrderLine> tblvLines;
    @FXML
    private TableColumn<SalesOrderLine, String> tcItemCode;
    @FXML
    private TableColumn<SalesOrderLine, String> tcItemDesc;
    @FXML
    private TableColumn<SalesOrderLine, String> tcItemUnit;
    @FXML
    private TableColumn<SalesOrderLine, Double> tcQuantity;
    @FXML
    private TableColumn<SalesOrderLine, Double> tcUnitPrice;

    private ICommonDao<SalesOrder> orderDao;
    private ICommonDao<SalesOrderLine> lineDao;
    private ICommonDao<SalesDelivery> deliveryDao;
    private ICommonDao<SalesDeliveryLine> deliveryLineDao;
    private ICommonDao<SalesQuote> quoteDao;
    private SalesOrder currentRow;
    private final ObservableList<SalesOrderLine> lines = FXCollections.observableArrayList();
    private Runnable afterChange;
    private boolean notifying;
    private RecordAuditBar auditBar;

    public void initializeForm(SalesOrder row) {
        fillLookups();
        lines.clear();
        txtDocumentNo.clear();
        txtNotes.clear();
        txtQuantity.clear();
        txtUnit.clear();
        txtUnitPrice.clear();
        cmbItem.getSelectionModel().clearSelection();
        cmbAccount.getSelectionModel().clearSelection();
        cmbWarehouse.getSelectionModel().clearSelection();
        txtSourceQuoteNo.clear();
        dtDocumentDate.setValue(LocalDate.now());
        this.currentRow = row;
        if (row == null) {
            applyLock(SalesOrder.STATUS_DRAFT);
            showAudit();
            return;
        }
        txtDocumentNo.setText(SatisDates.empty(row.getDocumentNo()));
        dtDocumentDate.setValue(SatisDates.toLocalDate(row.getDocumentDate()));
        SatisCombos.select(cmbAccount, row.getAccountCode());
        SatisCombos.select(cmbWarehouse, row.getWarehouseCode());
        txtSourceQuoteNo.setText(SatisDates.empty(row.getSourceQuoteNo()));
        txtNotes.setText(SatisDates.empty(row.getNotes()));
        if (row.getRowId() != null) {
            List<SalesOrderLine> stored = lineDao.findAll(
                    "from SalesOrderLine e where e.orderId = " + row.getRowId()
                            + " order by e.lineNo");
            if (stored != null) {
                lines.setAll(stored);
            }
        }
        applyLock(row.getStatus());
        showAudit();
    }

    public void initializeFromQuote(SalesQuote quote, List<SalesQuoteLine> quoteLines) {
        fillLookups();
        this.currentRow = orderDao.getEmpty();
        if (this.currentRow == null) {
            this.currentRow = new SalesOrder();
        }
        this.currentRow.setStatus(SalesOrder.STATUS_DRAFT);
        this.currentRow.setSourceQuoteId(quote.getRowId());
        this.currentRow.setSourceQuoteNo(quote.getDocumentNo());
        this.currentRow.setAccountCode(quote.getAccountCode());
        this.currentRow.setAccountName(quote.getAccountName());
        this.currentRow.setWarehouseCode(quote.getWarehouseCode());
        this.currentRow.setWarehouseName(quote.getWarehouseName());
        this.currentRow.setNotes(quote.getNotes());
        dtDocumentDate.setValue(LocalDate.now());
        txtDocumentNo.clear();
        SatisCombos.select(cmbAccount, quote.getAccountCode());
        SatisCombos.select(cmbWarehouse, quote.getWarehouseCode());
        txtSourceQuoteNo.setText(SatisDates.empty(quote.getDocumentNo()));
        txtNotes.setText(SatisDates.empty(quote.getNotes()));
        lines.clear();
        if (quoteLines != null) {
            int no = 1;
            for (SalesQuoteLine source : quoteLines) {
                SalesOrderLine line = new SalesOrderLine();
                line.setLineNo(no++);
                line.setItemCode(source.getItemCode());
                line.setItemDesc(source.getItemDesc());
                line.setItemUnit(source.getItemUnit());
                line.setQuantity(source.getQuantity());
                line.setUnitPrice(source.getUnitPrice());
                line.setSourceQuoteLineId(source.getRowId());
                lines.add(line);
            }
        }
        applyLock(SalesOrder.STATUS_DRAFT);
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
        currentRow.setStatus(SalesOrder.STATUS_APPROVED);
        currentRow.setLastUpdateDate(new Date());
        orderDao.addOrUpdate(currentRow);
        SalesDocs.syncQuoteStatus(quoteDao, orderDao, currentRow.getSourceQuoteId());
        close();
    }

    @FXML
    private void onActionBtnDeliver(ActionEvent event) {
        if (currentRow == null || currentRow.getRowId() == null) {
            return;
        }
        SatisWindows.convertOrder(currentRow, orderDao, lineDao, deliveryDao, () -> {
            SalesOrder reloaded = orderDao.firstOrDefault(currentRow.getRowId());
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
        if (SalesDocs.hasPostedDelivery(deliveryDao, deliveryLineDao, lineDao, currentRow.getRowId())) {
            SatisDates.showError("Satış siparişi", "İrsaliyeye bağlı",
                    "Önce bağlı satış irsaliyesini iptal veya silin.");
            return;
        }
        Long quoteId = currentRow.getSourceQuoteId();
        currentRow.setStatus(SalesOrder.STATUS_CANCELLED);
        currentRow.setLastUpdateDate(new Date());
        orderDao.addOrUpdate(currentRow);
        SalesDocs.syncQuoteStatus(quoteDao, orderDao, quoteId);
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
        if (itemCode == null || quantity == null || quantity <= 0d) {
            SatisDates.showError("Satış siparişi", "Satır eksik", "Malzeme ve miktar zorunludur.");
            return;
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (SatisDates.rejectIfNotSellable("Satış siparişi", item)) {
            return;
        }
        SalesOrderLine line = new SalesOrderLine();
        line.setItemCode(itemCode);
        line.setItemDesc(item == null ? SatisCombos.nameOf(cmbItem) : item.getItemDesc());
        String unit = SatisDates.trimToNull(txtUnit.getText());
        line.setItemUnit(unit == null && item != null ? item.getItemUnit() : unit);
        line.setQuantity(quantity);
        line.setUnitPrice(price);
        line.setLineNo(lines.size() + 1);
        lines.add(line);
        txtQuantity.clear();
        txtUnitPrice.clear();
    }

    @FXML
    private void onActionBtnRemoveLine(ActionEvent event) {
        SalesOrderLine selected = tblvLines.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lines.remove(selected);
            int no = 1;
            for (SalesOrderLine line : lines) {
                line.setLineNo(no++);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.orderDao = TerpApplication.getInstance().getPersistence().createDao(SalesOrder.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(SalesOrderLine.class);
        this.deliveryDao = TerpApplication.getInstance().getPersistence().createDao(SalesDelivery.class);
        this.deliveryLineDao = TerpApplication.getInstance().getPersistence().createDao(SalesDeliveryLine.class);
        this.quoteDao = TerpApplication.getInstance().getPersistence().createDao(SalesQuote.class);
        this.tcItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        this.tcUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
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
            SatisDates.showError("Satış siparişi", "Depo eksik", "Depo seçin.");
            return false;
        }
        String accountCode = SatisCombos.codeOf(cmbAccount);
        if (accountCode == null) {
            SatisDates.showError("Satış siparişi", "Müşteri eksik", "Müşteri seçin.");
            return false;
        }
        if (lines.isEmpty()) {
            SatisDates.showError("Satış siparişi", "Satır yok", "En az bir malzeme satırı ekleyin.");
            return false;
        }
        IItemLookup lookup = TerpApplication.getInstance().getItemLookup();
        for (SalesOrderLine line : lines) {
            if (line == null) {
                continue;
            }
            IStockItem item = lookup == null ? null : lookup.findByCode(line.getItemCode());
            if (SatisDates.rejectIfNotSellable("Satış siparişi", item)) {
                return false;
            }
        }
        Date now = new Date();
        SalesOrder row = this.currentRow;
        if (row == null) {
            row = orderDao.getEmpty();
            if (row == null) {
                SatisDates.showError("Satış siparişi", "Kayıt hatası", "Belge oluşturulamadı.");
                return false;
            }
            row.setAddedDate(now);
            row.setStatus(SalesOrder.STATUS_DRAFT);
        }
        if (!row.isDraft()) {
            SatisDates.showError("Satış siparişi", "Kilitli belge",
                    "Onaylı veya iptal sipariş taslak olarak kaydedilemez.");
            return false;
        }
        if (!CompanyScope.stamp(row)) {
            SatisDates.showError("Satış siparişi", "Firma",
                    "Önce araç çubuğundan çalışma firmasını seçin.");
            return false;
        }
        String documentNo = SatisDates.assignDocumentNo(DocumentNumbers.SALES_ORDER, txtDocumentNo);
        if (documentNo == null || documentNo.isBlank()) {
            SatisDates.showError("Satış siparişi", "Belge no",
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
                CompanyScope.findDuplicate(orderDao, "SalesOrder", "documentNo",
                        row.getDocumentNo(), row.getRowId()),
                "Belge numarası tekrar ediyor")) {
            return false;
        }
        SalesOrder saved = orderDao.addOrUpdate(row);
        if (saved == null || saved.getRowId() == null) {
            SatisDates.showError("Satış siparişi", "Kayıt hatası", "Belge kaydedilemedi.");
            return false;
        }
        this.currentRow = saved;
        replaceLines(saved.getRowId(), now);
        SalesDocs.syncQuoteStatus(quoteDao, orderDao, saved.getSourceQuoteId());
        showAudit();
        return true;
    }

    private void replaceLines(Long orderId, Date now) {
        SalesDocs.deleteByParent(lineDao, "SalesOrderLine", "orderId", orderId);
        int no = 1;
        List<SalesOrderLine> stored = new ArrayList<>();
        for (SalesOrderLine line : lines) {
            SalesOrderLine copy = lineDao.getEmpty();
            if (copy == null) {
                continue;
            }
            copy.setAddedDate(now);
            copy.setLastUpdateDate(now);
            copy.setOrderId(orderId);
            copy.setLineNo(no++);
            copy.setItemCode(line.getItemCode());
            copy.setItemDesc(line.getItemDesc());
            copy.setItemUnit(line.getItemUnit());
            copy.setQuantity(line.getQuantity());
            copy.setUnitPrice(line.getUnitPrice());
            copy.setSourceQuoteLineId(line.getSourceQuoteLineId());
            SalesOrderLine saved = lineDao.addOrUpdate(copy);
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
    }

    private void applyItem(String itemCode) {
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (item != null && (txtUnit.getText() == null || txtUnit.getText().isBlank())) {
            txtUnit.setText(SatisDates.empty(item.getItemUnit()));
        }
    }

    private void applyLock(int status) {
        boolean draft = status == SalesOrder.STATUS_DRAFT;
        boolean approved = status == SalesOrder.STATUS_APPROVED;
        boolean delivered = status == SalesOrder.STATUS_DELIVERED;
        txtDocumentNo.setDisable(!draft);
        dtDocumentDate.setDisable(!draft);
        cmbAccount.setDisable(!draft);
        cmbWarehouse.setDisable(!draft);
        txtNotes.setDisable(!draft);
        cmbItem.setDisable(!draft);
        txtQuantity.setDisable(!draft);
        txtUnit.setDisable(!draft);
        txtUnitPrice.setDisable(!draft);
        btnAddLine.setDisable(!draft);
        btnRemoveLine.setDisable(!draft);
        btnSave.setDisable(!draft);
        btnApprove.setDisable(!draft);
        boolean canDeliver = currentRow != null && currentRow.getRowId() != null
                && SalesDocs.hasDeliverableBalance(currentRow, lineDao, deliveryDao, deliveryLineDao);
        btnDeliver.setDisable(!canDeliver);
        btnDeliver.setVisible(approved || delivered);
        btnDeliver.setManaged(approved || delivered);
        boolean posted = currentRow != null && currentRow.getRowId() != null
                && SalesDocs.hasPostedDelivery(deliveryDao, deliveryLineDao, lineDao, currentRow.getRowId());
        btnCancelPosted.setDisable(!approved || posted);
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

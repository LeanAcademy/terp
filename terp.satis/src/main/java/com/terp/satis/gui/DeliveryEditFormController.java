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
import com.terp.plugin.data.StockDirection;
import com.terp.plugin.data.StockPosting;
import com.terp.plugin.data.model.IAccount;
import com.terp.plugin.data.model.IMovementReason;
import com.terp.plugin.data.model.IStockItem;
import com.terp.plugin.data.model.IStockWarehouse;
import com.terp.plugin.gui.RecordAuditBar;
import com.terp.satis.data.SalesDelivery;
import com.terp.satis.data.SalesDeliveryLine;
import com.terp.satis.data.SalesDocs;
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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

public class DeliveryEditFormController implements Initializable {

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
    private TableView<SalesDeliveryLine> tblvLines;
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

    private ICommonDao<SalesDelivery> deliveryDao;
    private ICommonDao<SalesDeliveryLine> lineDao;
    private SalesDelivery currentRow;
    private final ObservableList<SalesDeliveryLine> lines = FXCollections.observableArrayList();
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

    public void initializeForm(SalesDelivery row) {
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
        dtDocumentDate.setValue(LocalDate.now());
        this.currentRow = row;
        if (row == null) {
            applyLock(SalesDelivery.STATUS_DRAFT);
            showAudit();
            return;
        }
        txtDocumentNo.setText(SatisDates.empty(row.getDocumentNo()));
        dtDocumentDate.setValue(SatisDates.toLocalDate(row.getDocumentDate()));
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
            close();
        } catch (RuntimeException ex) {
            ledger.reverseByDocument(StockPosting.SOURCE_SALES_DELIVERY, currentRow.getRowId());
            SatisDates.showError("Satış irsaliyesi", "Stok fişi yazılamadı",
                    ex.getMessage() == null ? "Onay başarısız." : ex.getMessage());
        }
    }

    @FXML
    private void onActionBtnCancelPosted(ActionEvent event) {
        if (currentRow == null || currentRow.getRowId() == null || !currentRow.isPosted()) {
            return;
        }
        IStockLedger ledger = TerpApplication.getInstance().getStockLedger();
        if (ledger == null) {
            SatisDates.showError("Satış irsaliyesi", "Stok eklentisi yok",
                    "İptal için terp.stok yüklü olmalıdır.");
            return;
        }
        ledger.reverseByDocument(StockPosting.SOURCE_SALES_DELIVERY, currentRow.getRowId());
        currentRow.setStatus(SalesDelivery.STATUS_CANCELLED);
        currentRow.setLastUpdateDate(new Date());
        deliveryDao.addOrUpdate(currentRow);
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
            SatisDates.showError("Satış irsaliyesi", "Satır eksik", "Malzeme ve miktar zorunludur.");
            return;
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (item != null && item.getItemType() == IStockItem.TYPE_SERVICE) {
            SatisDates.showError("Satış irsaliyesi", "Hizmet", "Hizmet kartı irsaliyeye yazılamaz.");
            return;
        }
        SalesDeliveryLine line = new SalesDeliveryLine();
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
        SalesDeliveryLine selected = tblvLines.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lines.remove(selected);
            int no = 1;
            for (SalesDeliveryLine line : lines) {
                line.setLineNo(no++);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.deliveryDao = TerpApplication.getInstance().getPersistence().createDao(SalesDelivery.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(SalesDeliveryLine.class);
        this.tcItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        this.tcUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        this.tcQuantity.setCellFactory(TextFieldTableCell.forTableColumn(qtyConverter()));
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
            line.setQuantity(value);
        });
        this.tblvLines.setItems(lines);
        cmbItem.valueProperty().addListener((obs, old, value) -> applyItem(SatisCombos.codeOf(cmbItem)));
        fillLookups();
        if (dtDocumentDate.getValue() == null) {
            dtDocumentDate.setValue(LocalDate.now());
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
        if (lines.isEmpty()) {
            SatisDates.showError("Satış irsaliyesi", "Satır yok", "En az bir malzeme satırı ekleyin.");
            return false;
        }
        for (SalesDeliveryLine line : lines) {
            if (line == null || line.getQuantity() == null || line.getQuantity() <= 0d) {
                SatisDates.showError("Satış irsaliyesi", "Miktar", "Miktar sıfırdan büyük olmalıdır.");
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
        if (!row.isDraft()) {
            SatisDates.showError("Satış irsaliyesi", "Kilitli belge",
                    "Onaylı veya iptal belge taslak olarak kaydedilemez.");
            return false;
        }
        if (!CompanyScope.stamp(row)) {
            SatisDates.showError("Satış irsaliyesi", "Firma",
                    "Önce araç çubuğundan çalışma firmasını seçin.");
            return false;
        }
        String documentNo = SatisDates.assignDocumentNo(DocumentNumbers.SALES_DELIVERY, txtDocumentNo);
        if (documentNo == null || documentNo.isBlank()) {
            SatisDates.showError("Satış irsaliyesi", "Belge no",
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
            SalesDeliveryLine saved = lineDao.addOrUpdate(copy);
            stored.add(saved == null ? copy : saved);
        }
        lines.setAll(stored);
    }

    private StockPosting toPosting(SalesDeliveryLine line) {
        StockPosting posting = new StockPosting();
        posting.setMovementDate(currentRow.getDocumentDate());
        posting.setDirection(StockDirection.OUT);
        posting.setReasonCode(IMovementReason.CODE_SALES);
        posting.setWarehouseCode(currentRow.getWarehouseCode());
        posting.setWarehouseName(currentRow.getWarehouseName());
        posting.setItemCode(line.getItemCode());
        posting.setItemDesc(line.getItemDesc());
        posting.setItemUnit(line.getItemUnit());
        posting.setQuantity(line.getQuantity());
        posting.setUnitPrice(line.getUnitPrice());
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
        SatisCombos.fill(cmbItem, itemLabels);

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
        boolean draft = status == SalesDelivery.STATUS_DRAFT;
        boolean posted = status == SalesDelivery.STATUS_POSTED;
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
        tblvLines.setEditable(draft);
        btnSave.setDisable(!draft);
        btnApprove.setDisable(!draft);
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

    private static StringConverter<Double> qtyConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Double value) {
                return value == null ? "" : value.toString();
            }

            @Override
            public Double fromString(String text) {
                return SatisDates.parseDouble(text);
            }
        };
    }
}

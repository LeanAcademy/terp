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
import com.terp.plugin.data.IAccountLookup;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.IItemLookup;
import com.terp.plugin.data.IWarehouseLookup;
import com.terp.plugin.data.model.IAccount;
import com.terp.plugin.data.model.IStockItem;
import com.terp.plugin.data.model.IStockWarehouse;
import com.terp.satin.data.PurchaseDocs;
import com.terp.satin.data.PurchaseOrder;
import com.terp.satin.data.PurchaseOrderLine;
import com.terp.satin.data.PurchaseReceipt;
import com.terp.satin.data.PurchaseReceiptLine;
import com.terp.satin.data.PurchaseRequest;
import com.terp.satin.data.PurchaseRequestLine;
import com.terp.satin.data.PurchaseSettings;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
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
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

public class OrderEditFormController implements Initializable {

    @FXML
    private Button btnSave;
    @FXML
    private Button btnApprove;
    @FXML
    private Button btnReceive;
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
    private TextField txtSourceRequestNo;
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
    private TableView<PurchaseOrderLine> tblvLines;
    @FXML
    private TableColumn<PurchaseOrderLine, String> tcItemCode;
    @FXML
    private TableColumn<PurchaseOrderLine, String> tcItemDesc;
    @FXML
    private TableColumn<PurchaseOrderLine, String> tcItemUnit;
    @FXML
    private TableColumn<PurchaseOrderLine, Double> tcQuantity;
    @FXML
    private TableColumn<PurchaseOrderLine, Double> tcUnitPrice;

    private ICommonDao<PurchaseOrder> orderDao;
    private ICommonDao<PurchaseOrderLine> lineDao;
    private ICommonDao<PurchaseRequest> requestDao;
    private ICommonDao<PurchaseReceipt> receiptDao;
    private ICommonDao<PurchaseReceiptLine> receiptLineDao;
    private ICommonDao<PurchaseSettings> settingsDao;
    private PurchaseOrder currentRow;
    private final ObservableList<PurchaseOrderLine> lines = FXCollections.observableArrayList();
    private ValidationSupport validationSupport;

    public void initializeForm(PurchaseOrder row) {
        this.currentRow = row;
        fillLookups();
        if (row == null) {
            dtDocumentDate.setValue(LocalDate.now());
            applyLock(PurchaseOrder.STATUS_DRAFT);
            return;
        }
        fillHeader(row);
        if (row.getRowId() != null) {
            List<PurchaseOrderLine> stored = lineDao.findAll(
                    "from PurchaseOrderLine e where e.orderId = " + row.getRowId()
                            + " order by e.lineNo");
            if (stored != null) {
                lines.setAll(stored);
            }
        }
        applyLock(row.getStatus());
    }

    public void initializeFromRequest(PurchaseRequest request, List<PurchaseRequestLine> requestLines) {
        fillLookups();
        this.currentRow = orderDao.getEmpty();
        if (this.currentRow == null) {
            this.currentRow = new PurchaseOrder();
        }
        this.currentRow.setStatus(PurchaseOrder.STATUS_DRAFT);
        this.currentRow.setSourceRequestId(request.getRowId());
        this.currentRow.setSourceRequestNo(request.getDocumentNo());
        this.currentRow.setWarehouseCode(request.getWarehouseCode());
        this.currentRow.setWarehouseName(request.getWarehouseName());
        this.currentRow.setNotes(request.getNotes());
        dtDocumentDate.setValue(LocalDate.now());
        txtDocumentNo.setText("SIP-" + SatinDates.empty(request.getDocumentNo()));
        SatinCombos.select(cmbWarehouse, request.getWarehouseCode());
        txtSourceRequestNo.setText(SatinDates.empty(request.getDocumentNo()));
        txtNotes.setText(SatinDates.empty(request.getNotes()));
        lines.clear();
        if (requestLines != null) {
            int no = 1;
            for (PurchaseRequestLine source : requestLines) {
                PurchaseOrderLine line = new PurchaseOrderLine();
                line.setLineNo(no++);
                line.setItemCode(source.getItemCode());
                line.setItemDesc(source.getItemDesc());
                line.setItemUnit(source.getItemUnit());
                line.setQuantity(source.getQuantity());
                line.setSourceRequestLineId(source.getRowId());
                lines.add(line);
            }
        }
        applyLock(PurchaseOrder.STATUS_DRAFT);
    }

    @FXML
    private void onActionBtnSave(ActionEvent event) {
        saveDraft();
    }

    @FXML
    private void onActionBtnApprove(ActionEvent event) {
        if (!saveDraft()) {
            return;
        }
        currentRow.setStatus(PurchaseOrder.STATUS_APPROVED);
        currentRow.setLastUpdateDate(new Date());
        orderDao.addOrUpdate(currentRow);
        PurchaseDocs.syncRequestStatus(requestDao, orderDao, currentRow.getSourceRequestId());
        close();
    }

    @FXML
    private void onActionBtnReceive(ActionEvent event) {
        if (currentRow == null || currentRow.getRowId() == null) {
            return;
        }
        SatinWindows.convertOrder(currentRow, orderDao, lineDao, receiptDao, () -> {
            PurchaseOrder reloaded = orderDao.firstOrDefault(currentRow.getRowId());
            if (reloaded != null) {
                this.currentRow = reloaded;
                applyLock(reloaded.getStatus());
            }
        });
    }

    @FXML
    private void onActionBtnCancelPosted(ActionEvent event) {
        if (currentRow == null || currentRow.getRowId() == null || !currentRow.isApproved()) {
            return;
        }
        if (PurchaseDocs.hasPostedReceipt(receiptDao, receiptLineDao, lineDao, currentRow.getRowId())) {
            SatinDates.showError("Satınalma siparişi", "Mal kabule bağlı",
                    "Önce bağlı mal kabul belgesini iptal veya silin.");
            return;
        }
        Long requestId = currentRow.getSourceRequestId();
        currentRow.setStatus(PurchaseOrder.STATUS_CANCELLED);
        currentRow.setLastUpdateDate(new Date());
        orderDao.addOrUpdate(currentRow);
        PurchaseDocs.syncRequestStatus(requestDao, orderDao, requestId);
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
        Double price = SatinDates.parseDouble(txtUnitPrice.getText());
        if (itemCode == null || quantity == null || quantity <= 0d) {
            SatinDates.showError("Satınalma siparişi", "Satır eksik", "Malzeme ve miktar zorunludur.");
            return;
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        PurchaseOrderLine line = new PurchaseOrderLine();
        line.setItemCode(itemCode);
        line.setItemDesc(item == null ? SatinCombos.nameOf(cmbItem) : item.getItemDesc());
        String unit = SatinDates.trimToNull(txtUnit.getText());
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
        PurchaseOrderLine selected = tblvLines.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lines.remove(selected);
            int no = 1;
            for (PurchaseOrderLine line : lines) {
                line.setLineNo(no++);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.orderDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseOrder.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseOrderLine.class);
        this.requestDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseRequest.class);
        this.receiptDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseReceipt.class);
        this.receiptLineDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseReceiptLine.class);
        this.settingsDao = TerpApplication.getInstance().getPersistence().createDao(PurchaseSettings.class);
        this.tcItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        this.tcUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        this.tblvLines.setItems(lines);
        cmbItem.valueProperty().addListener((obs, old, value) -> applyItem(SatinCombos.codeOf(cmbItem)));
        this.validationSupport = new ValidationSupport();
        this.validationSupport.registerValidator(txtDocumentNo, true,
                Validator.createEmptyValidator("Belge no zorunlu"));
    }

    private boolean saveDraft() {
        if (validationSupport.isInvalid()) {
            SatinDates.showError("Satınalma siparişi", "Zorunlu alanlar eksik", "Belge no zorunludur.");
            return false;
        }
        String warehouseCode = SatinCombos.codeOf(cmbWarehouse);
        if (warehouseCode == null) {
            SatinDates.showError("Satınalma siparişi", "Depo eksik", "Depo seçin.");
            return false;
        }
        String accountCode = SatinCombos.codeOf(cmbAccount);
        if (accountCode == null) {
            SatinDates.showError("Satınalma siparişi", "Tedarikçi eksik", "Tedarikçi seçin.");
            return false;
        }
        if (lines.isEmpty()) {
            SatinDates.showError("Satınalma siparişi", "Satır yok", "En az bir malzeme satırı ekleyin.");
            return false;
        }
        Date now = new Date();
        PurchaseOrder row = this.currentRow;
        if (row == null) {
            row = orderDao.getEmpty();
            if (row == null) {
                SatinDates.showError("Satınalma siparişi", "Kayıt hatası", "Belge oluşturulamadı.");
                return false;
            }
            row.setAddedDate(now);
            row.setStatus(PurchaseOrder.STATUS_DRAFT);
        }
        if (!row.isDraft()) {
            SatinDates.showError("Satınalma siparişi", "Kilitli belge",
                    "Onaylı veya iptal sipariş taslak olarak kaydedilemez.");
            return false;
        }
        if (!CompanyScope.stamp(row)) {
            SatinDates.showError("Satınalma siparişi", "Firma",
                    "Önce araç çubuğundan çalışma firmasını seçin.");
            return false;
        }
        row.setDocumentNo(txtDocumentNo.getText().trim());
        row.setDocumentDate(SatinDates.toDate(dtDocumentDate.getValue()));
        row.setAccountCode(accountCode);
        row.setAccountName(SatinCombos.nameOf(cmbAccount));
        row.setWarehouseCode(warehouseCode);
        IWarehouseLookup warehouses = TerpApplication.getInstance().getWarehouseLookup();
        IStockWarehouse warehouse = warehouses == null ? null : warehouses.findByCode(warehouseCode);
        row.setWarehouseName(warehouse == null ? SatinCombos.nameOf(cmbWarehouse)
                : warehouse.getWarehouseName());
        row.setNotes(SatinDates.trimToNull(txtNotes.getText()));
        row.setLastUpdateDate(now);
        if (row.getAddedDate() == null) {
            row.setAddedDate(now);
        }
        if (CompanyScope.rejectDuplicate(
                CompanyScope.findDuplicate(orderDao, "PurchaseOrder", "documentNo",
                        row.getDocumentNo(), row.getRowId()),
                "Belge numarası tekrar ediyor")) {
            return false;
        }
        PurchaseOrder saved = orderDao.addOrUpdate(row);
        if (saved == null || saved.getRowId() == null) {
            SatinDates.showError("Satınalma siparişi", "Kayıt hatası", "Belge kaydedilemedi.");
            return false;
        }
        this.currentRow = saved;
        replaceLines(saved.getRowId(), now);
        PurchaseDocs.syncRequestStatus(requestDao, orderDao, saved.getSourceRequestId());
        return true;
    }

    private void replaceLines(Long orderId, Date now) {
        PurchaseDocs.deleteByParent(lineDao, "PurchaseOrderLine", "orderId", orderId);
        int no = 1;
        List<PurchaseOrderLine> stored = new ArrayList<>();
        for (PurchaseOrderLine line : lines) {
            PurchaseOrderLine copy = lineDao.getEmpty();
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
            copy.setSourceRequestLineId(line.getSourceRequestLineId());
            PurchaseOrderLine saved = lineDao.addOrUpdate(copy);
            stored.add(saved == null ? copy : saved);
        }
        lines.setAll(stored);
    }

    private void fillHeader(PurchaseOrder row) {
        txtDocumentNo.setText(SatinDates.empty(row.getDocumentNo()));
        dtDocumentDate.setValue(SatinDates.toLocalDate(row.getDocumentDate()));
        SatinCombos.select(cmbAccount, row.getAccountCode());
        SatinCombos.select(cmbWarehouse, row.getWarehouseCode());
        txtSourceRequestNo.setText(SatinDates.empty(row.getSourceRequestNo()));
        txtNotes.setText(SatinDates.empty(row.getNotes()));
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
    }

    private void applyItem(String itemCode) {
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (item != null && (txtUnit.getText() == null || txtUnit.getText().isBlank())) {
            txtUnit.setText(SatinDates.empty(item.getItemUnit()));
        }
    }

    private void applyLock(int status) {
        boolean draft = status == PurchaseOrder.STATUS_DRAFT;
        boolean approved = status == PurchaseOrder.STATUS_APPROVED;
        boolean received = status == PurchaseOrder.STATUS_RECEIVED;
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
        boolean canReceive = currentRow != null && currentRow.getRowId() != null
                && PurchaseDocs.hasReceivableBalance(currentRow, lineDao, receiptDao, receiptLineDao,
                        settingsDao);
        btnReceive.setDisable(!canReceive);
        btnReceive.setVisible(approved || received);
        btnReceive.setManaged(approved || received);
        boolean postedGr = currentRow != null && currentRow.getRowId() != null
                && PurchaseDocs.hasPostedReceipt(receiptDao, receiptLineDao, lineDao, currentRow.getRowId());
        btnCancelPosted.setDisable(!approved || postedGr);
        btnCancelPosted.setVisible(approved);
        btnCancelPosted.setManaged(approved);
    }

    private void close() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}

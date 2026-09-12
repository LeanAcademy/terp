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
package com.terp.stok.gui;

import com.terp.plugin.CompanyScope;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.DocumentNumbers;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.IItemLookup;
import com.terp.plugin.data.IStockLedger;
import com.terp.plugin.data.IWarehouseLookup;
import com.terp.plugin.data.StockDirection;
import com.terp.plugin.data.StockPosting;
import com.terp.plugin.data.model.IMovementReason;
import com.terp.plugin.data.model.IStockItem;
import com.terp.plugin.data.model.IStockWarehouse;
import com.terp.plugin.gui.RecordAuditBar;
import com.terp.stok.data.StockCount;
import com.terp.stok.data.StockCountLine;
import com.terp.stok.data.StockDocs;
import com.terp.stok.data.StockMovement;
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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

public class CountEditFormController implements Initializable {

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
    private ComboBox<String> cmbWarehouse;
    @FXML
    private Button btnFillWarehouse;
    @FXML
    private Button btnRefreshOnHand;
    @FXML
    private TextArea txtNotes;
    @FXML
    private ComboBox<String> cmbItem;
    @FXML
    private TextField txtCounted;
    @FXML
    private TextField txtUnit;
    @FXML
    private Label lblOnHand;
    @FXML
    private Button btnAddLine;
    @FXML
    private Button btnRemoveLine;
    @FXML
    private TableView<StockCountLine> tblvLines;
    @FXML
    private TableColumn<StockCountLine, Integer> tcLineNo;
    @FXML
    private TableColumn<StockCountLine, String> tcItemCode;
    @FXML
    private TableColumn<StockCountLine, String> tcItemDesc;
    @FXML
    private TableColumn<StockCountLine, String> tcItemUnit;
    @FXML
    private TableColumn<StockCountLine, Double> tcOnHandQty;
    @FXML
    private TableColumn<StockCountLine, Double> tcCountedQty;
    @FXML
    private TableColumn<StockCountLine, Double> tcDifference;

    private ICommonDao<StockCount> countDao;
    private ICommonDao<StockCountLine> lineDao;
    private ICommonDao<StockMovement> movementDao;
    private StockCount currentRow;
    private final ObservableList<StockCountLine> lines = FXCollections.observableArrayList();
    private RecordAuditBar auditBar;
    private boolean loading;

    public void initializeForm(StockCount row) {
        loading = true;
        try {
            fillLookups();
            lines.clear();
            txtDocumentNo.clear();
            txtNotes.clear();
            txtCounted.clear();
            txtUnit.clear();
            lblOnHand.setText("");
            cmbItem.getSelectionModel().clearSelection();
            cmbWarehouse.getSelectionModel().clearSelection();
            dtDocumentDate.setValue(LocalDate.now());
            this.currentRow = row;
            if (row == null) {
                applyLock(StockCount.STATUS_DRAFT);
                showAudit();
                return;
            }
            txtDocumentNo.setText(StockForms.empty(row.getDocumentNo()));
            dtDocumentDate.setValue(StockForms.toLocalDate(row.getDocumentDate()));
            StockCombos.select(cmbWarehouse, row.getWarehouseCode());
            txtNotes.setText(StockForms.empty(row.getNotes()));
            if (row.getRowId() != null) {
                List<StockCountLine> stored = lineDao.findAll(
                        "from StockCountLine e where e.countId = " + row.getRowId()
                                + " order by e.lineNo");
                if (stored != null) {
                    lines.setAll(stored);
                }
            }
            applyLock(row.getStatus());
            showAudit();
        } finally {
            loading = false;
        }
    }

    @FXML
    private void onActionBtnSave(ActionEvent event) {
        saveDraft();
    }

    @FXML
    private void onActionBtnApprove(ActionEvent event) {
        refreshOnHand();
        if (!saveDraft()) {
            return;
        }
        IStockLedger ledger = TerpApplication.getInstance().getStockLedger();
        if (ledger == null) {
            StockForms.showError("Stok sayım", "Stok defteri yok", "Onay için stok defteri gerekir.");
            return;
        }
        try {
            for (StockCountLine line : lines) {
                StockPosting posting = toPosting(line);
                if (posting != null) {
                    ledger.post(posting);
                }
            }
            currentRow.setStatus(StockCount.STATUS_POSTED);
            currentRow.setLastUpdateDate(new Date());
            countDao.addOrUpdate(currentRow);
            close();
        } catch (RuntimeException ex) {
            ledger.reverseByDocument(StockPosting.SOURCE_STOCK_COUNT, currentRow.getRowId());
            StockForms.showError("Stok sayım", "Stok fişi yazılamadı",
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
            StockForms.showError("Stok sayım", "Stok defteri yok", "İptal için stok defteri gerekir.");
            return;
        }
        ledger.reverseByDocument(StockPosting.SOURCE_STOCK_COUNT, currentRow.getRowId());
        currentRow.setStatus(StockCount.STATUS_CANCELLED);
        currentRow.setLastUpdateDate(new Date());
        countDao.addOrUpdate(currentRow);
        close();
    }

    @FXML
    private void onActionBtnClose(ActionEvent event) {
        close();
    }

    @FXML
    private void onActionBtnFillWarehouse(ActionEvent event) {
        String warehouseCode = StockCombos.codeOf(cmbWarehouse);
        if (warehouseCode == null) {
            StockForms.showError("Stok sayım", "Depo eksik", "Önce depo seçin.");
            return;
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        if (items == null) {
            return;
        }
        List<IStockItem> catalog = items.findActive();
        if (catalog == null || catalog.isEmpty()) {
            StockForms.showError("Stok sayım", "Malzeme yok", "Aktif stok malzemesi yok.");
            return;
        }
        List<StockMovement> movements = StockDocs.movements(movementDao);
        int added = 0;
        int no = lines.size() + 1;
        for (IStockItem item : catalog) {
            if (item == null || item.getItemId() == null || hasItem(item.getItemId())) {
                continue;
            }
            double onHand = StockDocs.onHand(movements, warehouseCode, item.getItemId());
            if (Math.abs(onHand) <= StockCountLine.ZERO_EPS) {
                continue;
            }
            StockCountLine line = new StockCountLine();
            line.setLineNo(no++);
            line.setItemCode(item.getItemId());
            line.setItemDesc(item.getItemDesc());
            line.setItemUnit(item.getItemUnit());
            line.setOnHandQty(onHand);
            line.setCountedQty(onHand);
            lines.add(line);
            added++;
        }
        if (added == 0) {
            StockForms.showError("Stok sayım", "Satır yok",
                    "Bu depoda bakiyeli malzeme yok. Sıfır bakiyeli malzeme Satır ekle ile eklenir.");
        }
        tblvLines.refresh();
    }

    @FXML
    private void onActionBtnRefreshOnHand(ActionEvent event) {
        refreshOnHand();
        tblvLines.refresh();
    }

    @FXML
    private void onActionBtnAddLine(ActionEvent event) {
        String itemCode = StockCombos.codeOf(cmbItem);
        if (itemCode == null) {
            StockForms.showError("Stok sayım", "Satır eksik", "Malzeme seçin.");
            return;
        }
        if (hasItem(itemCode)) {
            StockForms.showError("Stok sayım", "Tekrar", itemCode + " bu belgede zaten var.");
            return;
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (item != null && item.getItemType() == IStockItem.TYPE_SERVICE) {
            StockForms.showError("Stok sayım", "Hizmet", "Hizmet kartı sayılamaz.");
            return;
        }
        String warehouseCode = StockCombos.codeOf(cmbWarehouse);
        if (warehouseCode == null) {
            StockForms.showError("Stok sayım", "Depo eksik", "Önce depo seçin.");
            return;
        }
        Double counted = StockForms.parseDouble(txtCounted.getText());
        double onHand = StockDocs.onHand(StockDocs.movements(movementDao), warehouseCode, itemCode);
        if (counted == null) {
            counted = onHand;
        }
        if (counted < 0d) {
            StockForms.showError("Stok sayım", "Miktar", "Sayılan miktar negatif olamaz.");
            return;
        }
        StockCountLine line = new StockCountLine();
        line.setItemCode(itemCode);
        line.setItemDesc(item == null ? StockCombos.nameOf(cmbItem) : item.getItemDesc());
        String unit = StockForms.trimToNull(txtUnit.getText());
        line.setItemUnit(unit == null && item != null ? item.getItemUnit() : unit);
        line.setOnHandQty(onHand);
        line.setCountedQty(counted);
        line.setLineNo(lines.size() + 1);
        lines.add(line);
        txtCounted.clear();
        refreshOnHandHint();
    }

    @FXML
    private void onActionBtnRemoveLine(ActionEvent event) {
        StockCountLine selected = tblvLines.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lines.remove(selected);
            int no = 1;
            for (StockCountLine line : lines) {
                line.setLineNo(no++);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.countDao = TerpApplication.getInstance().getPersistence().createDao(StockCount.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(StockCountLine.class);
        this.movementDao = TerpApplication.getInstance().getPersistence().createDao(StockMovement.class);
        this.tcLineNo.setCellValueFactory(new PropertyValueFactory<>("lineNo"));
        this.tcItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcOnHandQty.setCellValueFactory(new PropertyValueFactory<>("onHandQty"));
        this.tcCountedQty.setCellValueFactory(new PropertyValueFactory<>("countedQty"));
        this.tcDifference.setCellValueFactory(new PropertyValueFactory<>("difference"));
        this.tcCountedQty.setCellFactory(TextFieldTableCell.forTableColumn(qtyConverter()));
        this.tcCountedQty.setOnEditCommit(event -> {
            StockCountLine line = event.getRowValue();
            Double value = event.getNewValue();
            if (line == null) {
                return;
            }
            if (value == null || value < 0d) {
                StockForms.showError("Stok sayım", "Miktar", "Sayılan miktar negatif olamaz.");
                tblvLines.refresh();
                return;
            }
            line.setCountedQty(value);
            tblvLines.refresh();
        });
        this.tblvLines.setItems(lines);
        cmbItem.valueProperty().addListener((obs, old, value) -> {
            applyItem(StockCombos.codeOf(cmbItem));
            refreshOnHandHint();
        });
        cmbWarehouse.valueProperty().addListener((obs, old, value) -> {
            refreshOnHandHint();
            if (!loading && (currentRow == null || currentRow.isDraft()) && !lines.isEmpty()) {
                refreshOnHand();
                tblvLines.refresh();
            }
        });
        fillLookups();
        if (dtDocumentDate.getValue() == null) {
            dtDocumentDate.setValue(LocalDate.now());
        }
        applyLock(StockCount.STATUS_DRAFT);
        this.auditBar = RecordAuditBar.install(txtDocumentNo);
        showAudit();
        Platform.runLater(() -> StockEmbed.hideCloseIfDesktop(btnClose));
    }

    private void showAudit() {
        if (auditBar != null) {
            auditBar.bind(currentRow);
        }
    }

    private boolean saveDraft() {
        String warehouseCode = StockCombos.codeOf(cmbWarehouse);
        if (warehouseCode == null) {
            StockForms.showError("Stok sayım", "Depo eksik", "Depo seçin.");
            return false;
        }
        if (lines.isEmpty()) {
            StockForms.showError("Stok sayım", "Satır yok", "En az bir malzeme satırı ekleyin.");
            return false;
        }
        String qtyError = validateLines();
        if (qtyError != null) {
            StockForms.showError("Stok sayım", "Miktar", qtyError);
            return false;
        }
        Date now = new Date();
        StockCount row = this.currentRow;
        if (row == null) {
            row = countDao.getEmpty();
            if (row == null) {
                StockForms.showError("Stok sayım", "Kayıt hatası", "Belge oluşturulamadı.");
                return false;
            }
            row.setAddedDate(now);
            row.setStatus(StockCount.STATUS_DRAFT);
        }
        if (!row.isDraft()) {
            StockForms.showError("Stok sayım", "Kilitli belge",
                    "Onaylı veya iptal belge taslak olarak kaydedilemez.");
            return false;
        }
        if (!CompanyScope.stamp(row)) {
            StockForms.showError("Stok sayım", "Firma", "Önce araç çubuğundan çalışma firmasını seçin.");
            return false;
        }
        String documentNo = StockForms.assignDocumentNo(DocumentNumbers.STOCK_COUNT, txtDocumentNo);
        if (documentNo == null || documentNo.isBlank()) {
            StockForms.showError("Stok sayım", "Belge no",
                    "Belge no üretilemedi. Elle yazın veya Sistem yönetimi → Belge numaraları serisini kontrol edin.");
            return false;
        }
        row.setDocumentNo(documentNo);
        row.setDocumentDate(StockForms.toDate(dtDocumentDate.getValue()));
        row.setWarehouseCode(warehouseCode);
        IWarehouseLookup warehouses = TerpApplication.getInstance().getWarehouseLookup();
        IStockWarehouse warehouse = warehouses == null ? null : warehouses.findByCode(warehouseCode);
        row.setWarehouseName(warehouse == null ? StockCombos.nameOf(cmbWarehouse) : warehouse.getWarehouseName());
        row.setNotes(StockForms.trimToNull(txtNotes.getText()));
        row.setLastUpdateDate(now);
        if (row.getAddedDate() == null) {
            row.setAddedDate(now);
        }
        if (CompanyScope.rejectDuplicate(
                CompanyScope.findDuplicate(countDao, "StockCount", "documentNo",
                        row.getDocumentNo(), row.getRowId()),
                "Belge numarası tekrar ediyor")) {
            return false;
        }
        StockCount saved = countDao.addOrUpdate(row);
        if (saved == null || saved.getRowId() == null) {
            StockForms.showError("Stok sayım", "Kayıt hatası", "Belge kaydedilemedi.");
            return false;
        }
        this.currentRow = saved;
        replaceLines(saved.getRowId(), now);
        showAudit();
        return true;
    }

    private void replaceLines(Long countId, Date now) {
        StockDocs.deleteByParent(lineDao, "StockCountLine", "countId", countId);
        int no = 1;
        List<StockCountLine> stored = new ArrayList<>();
        for (StockCountLine line : lines) {
            StockCountLine copy = lineDao.getEmpty();
            if (copy == null) {
                continue;
            }
            copy.setAddedDate(now);
            copy.setLastUpdateDate(now);
            copy.setCountId(countId);
            copy.setLineNo(no++);
            copy.setItemCode(line.getItemCode());
            copy.setItemDesc(line.getItemDesc());
            copy.setItemUnit(line.getItemUnit());
            copy.setOnHandQty(line.getOnHandQty());
            copy.setCountedQty(line.getCountedQty());
            StockCountLine saved = lineDao.addOrUpdate(copy);
            stored.add(saved == null ? copy : saved);
        }
        lines.setAll(stored);
    }

    private StockPosting toPosting(StockCountLine line) {
        if (line == null || !line.hasDifference()) {
            return null;
        }
        double diff = line.getDifference();
        StockPosting posting = new StockPosting();
        posting.setMovementDate(currentRow.getDocumentDate());
        if (diff > 0d) {
            posting.setDirection(StockDirection.COUNT_IN);
            posting.setReasonCode(IMovementReason.CODE_COUNT_IN);
            posting.setQuantity(diff);
        } else {
            posting.setDirection(StockDirection.COUNT_OUT);
            posting.setReasonCode(IMovementReason.CODE_COUNT_OUT);
            posting.setQuantity(Math.abs(diff));
        }
        posting.setWarehouseCode(currentRow.getWarehouseCode());
        posting.setWarehouseName(currentRow.getWarehouseName());
        posting.setItemCode(line.getItemCode());
        posting.setItemDesc(line.getItemDesc());
        posting.setItemUnit(line.getItemUnit());
        posting.setDocumentNo(currentRow.getDocumentNo());
        posting.setSourceType(StockPosting.SOURCE_STOCK_COUNT);
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
        StockCombos.fill(cmbWarehouse, warehouseLabels);

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
        StockCombos.fill(cmbItem, itemLabels);
    }

    private void applyItem(String itemCode) {
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (item != null && (txtUnit.getText() == null || txtUnit.getText().isBlank())) {
            txtUnit.setText(StockForms.empty(item.getItemUnit()));
        }
    }

    private void refreshOnHand() {
        String warehouseCode = StockCombos.codeOf(cmbWarehouse);
        if (warehouseCode == null || lines.isEmpty()) {
            return;
        }
        List<StockMovement> movements = StockDocs.movements(movementDao);
        for (StockCountLine line : lines) {
            if (line == null || line.getItemCode() == null) {
                continue;
            }
            line.setOnHandQty(StockDocs.onHand(movements, warehouseCode, line.getItemCode()));
        }
    }

    private void refreshOnHandHint() {
        if (lblOnHand == null) {
            return;
        }
        String warehouseCode = StockCombos.codeOf(cmbWarehouse);
        String itemCode = StockCombos.codeOf(cmbItem);
        if (warehouseCode == null || itemCode == null) {
            lblOnHand.setText("");
            return;
        }
        double onHand = StockDocs.onHand(StockDocs.movements(movementDao), warehouseCode, itemCode);
        lblOnHand.setText("Eldeki " + onHand);
        if (txtCounted.getText() == null || txtCounted.getText().isBlank()) {
            txtCounted.setText(Double.toString(onHand));
        }
    }

    private String validateLines() {
        for (StockCountLine line : lines) {
            if (line == null || line.getItemCode() == null || line.getItemCode().isBlank()) {
                return "Malzeme kodu boş satır var.";
            }
            if (line.getCountedQty() == null || line.getCountedQty() < 0d) {
                return line.getItemCode() + " sayılan miktarı negatif olamaz.";
            }
        }
        return null;
    }

    private boolean hasItem(String itemCode) {
        if (itemCode == null) {
            return false;
        }
        for (StockCountLine line : lines) {
            if (line != null && itemCode.equals(line.getItemCode())) {
                return true;
            }
        }
        return false;
    }

    private void applyLock(int status) {
        boolean draft = status == StockCount.STATUS_DRAFT;
        boolean posted = status == StockCount.STATUS_POSTED;
        txtDocumentNo.setDisable(!draft);
        dtDocumentDate.setDisable(!draft);
        cmbWarehouse.setDisable(!draft);
        btnFillWarehouse.setDisable(!draft);
        btnRefreshOnHand.setDisable(!draft);
        txtNotes.setDisable(!draft);
        cmbItem.setDisable(!draft);
        txtCounted.setDisable(!draft);
        txtUnit.setDisable(!draft);
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
        StockEmbed.closeIfModal(btnClose);
    }

    private static StringConverter<Double> qtyConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Double value) {
                return value == null ? "" : value.toString();
            }

            @Override
            public Double fromString(String text) {
                return StockForms.parseDouble(text);
            }
        };
    }
}

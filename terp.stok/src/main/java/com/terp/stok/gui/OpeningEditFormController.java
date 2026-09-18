/*
 * Copyright (C) 2026 LeanAcademy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.terp.stok.gui;

import com.terp.plugin.CompanyScope;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.DocumentNumbers;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.IItemLookup;
import com.terp.plugin.data.IStockLedger;
import com.terp.plugin.data.IWarehouseLookup;
import com.terp.plugin.data.StockCard;
import com.terp.plugin.data.StockDirection;
import com.terp.plugin.data.StockPosting;
import com.terp.plugin.data.WorkingPeriod;
import com.terp.plugin.data.model.IMovementReason;
import com.terp.plugin.data.model.IStockItem;
import com.terp.plugin.data.model.IStockWarehouse;
import com.terp.plugin.gui.RecordAuditBar;
import com.terp.stok.data.StockDocs;
import com.terp.stok.data.StockMovement;
import com.terp.stok.data.StockOpening;
import com.terp.stok.data.StockOpeningLine;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import com.terp.plugin.gui.NumericCells;

public class OpeningEditFormController implements Initializable {

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
    private TextArea txtNotes;
    @FXML
    private ComboBox<String> cmbItem;
    @FXML
    private TextField txtQuantity;
    @FXML
    private ComboBox<String> cmbUnit;
    @FXML
    private TextField txtUnitPrice;
    @FXML
    private TextField txtLotNo;
    @FXML
    private TextField txtSerialNo;
    @FXML
    private DatePicker dtExpiry;
    @FXML
    private Button btnAddLine;
    @FXML
    private Button btnRemoveLine;
    @FXML
    private TableView<StockOpeningLine> tblvLines;
    @FXML
    private TableColumn<StockOpeningLine, Integer> tcLineNo;
    @FXML
    private TableColumn<StockOpeningLine, String> tcItemCode;
    @FXML
    private TableColumn<StockOpeningLine, String> tcItemDesc;
    @FXML
    private TableColumn<StockOpeningLine, String> tcItemUnit;
    @FXML
    private TableColumn<StockOpeningLine, Double> tcQuantity;
    @FXML
    private TableColumn<StockOpeningLine, Double> tcUnitPrice;
    @FXML
    private TableColumn<StockOpeningLine, String> tcLotNo;
    @FXML
    private TableColumn<StockOpeningLine, String> tcSerialNo;
    @FXML
    private TableColumn<StockOpeningLine, String> tcExpiry;

    private ICommonDao<StockOpening> openingDao;
    private ICommonDao<StockOpeningLine> lineDao;
    private ICommonDao<StockMovement> movementDao;
    private StockOpening currentRow;
    private final ObservableList<StockOpeningLine> lines = FXCollections.observableArrayList();
    private RecordAuditBar auditBar;
    private boolean loading;

    public void initializeForm(StockOpening row) {
        loading = true;
        try {
            fillLookups();
            lines.clear();
            txtDocumentNo.clear();
            txtNotes.clear();
            txtQuantity.clear();
            txtUnitPrice.clear();
            txtLotNo.clear();
            txtSerialNo.clear();
            dtExpiry.setValue(null);
            cmbItem.getSelectionModel().clearSelection();
            cmbUnit.getSelectionModel().clearSelection();
            cmbWarehouse.getSelectionModel().clearSelection();
            dtDocumentDate.setValue(LocalDate.now());
            this.currentRow = row;
            if (row == null) {
                applyLock(StockOpening.STATUS_DRAFT);
                showAudit();
                return;
            }
            txtDocumentNo.setText(StockForms.empty(row.getDocumentNo()));
            dtDocumentDate.setValue(StockForms.toLocalDate(row.getDocumentDate()));
            StockCombos.select(cmbWarehouse, row.getWarehouseCode());
            txtNotes.setText(StockForms.empty(row.getNotes()));
            if (row.getRowId() != null) {
                List<StockOpeningLine> stored = lineDao.findAll(
                        "from StockOpeningLine e where e.openingId = " + row.getRowId()
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
        if (!saveDraft()) {
            return;
        }
        IStockLedger ledger = TerpApplication.getInstance().getStockLedger();
        if (ledger == null) {
            StockForms.showError("Stok açılış", "Stok defteri yok", "Onay için stok defteri gerekir.");
            return;
        }
        List<StockPosting> postings = new ArrayList<>();
        StringBuilder warnings = new StringBuilder();
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        List<StockMovement> movements = StockDocs.movements(movementDao);
        for (StockOpeningLine line : lines) {
            StockPosting posting = toPosting(line);
            if (posting == null) {
                continue;
            }
            postings.add(posting);
            IStockItem item = items == null ? null : items.findByCode(line.getItemCode());
            double qty = StockCard.baseQuantity(item, line.getItemUnit(), line.getQuantity());
            double after = StockDocs.onHand(movements, currentRow.getWarehouseCode(), line.getItemCode()) + qty;
            String warning = StockCard.minStockWarning(item, after);
            if (warning != null) {
                if (warnings.length() > 0) {
                    warnings.append('\n');
                }
                warnings.append(warning);
            }
        }
        if (warnings.length() > 0) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Stok açılış");
            alert.setHeaderText("Min. stok uyarısı");
            alert.setContentText(warnings.toString());
            alert.show();
        }
        try {
            for (StockPosting posting : postings) {
                ledger.post(posting);
            }
            currentRow.setStatus(StockOpening.STATUS_POSTED);
            currentRow.setLastUpdateDate(new Date());
            openingDao.addOrUpdate(currentRow);
            close();
        } catch (RuntimeException ex) {
            ledger.reverseByDocument(StockPosting.SOURCE_STOCK_OPENING, currentRow.getRowId());
            StockForms.showError("Stok açılış", "Stok fişi yazılamadı",
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
            StockForms.showError("Stok açılış", "Stok defteri yok", "İptal için stok defteri gerekir.");
            return;
        }
        ledger.reverseByDocument(StockPosting.SOURCE_STOCK_OPENING, currentRow.getRowId());
        currentRow.setStatus(StockOpening.STATUS_CANCELLED);
        currentRow.setLastUpdateDate(new Date());
        openingDao.addOrUpdate(currentRow);
        close();
    }

    @FXML
    private void onActionBtnClose(ActionEvent event) {
        close();
    }

    @FXML
    private void onActionBtnAddLine(ActionEvent event) {
        StockOpeningLine selected = tblvLines.getSelectionModel().getSelectedItem();
        String itemCode = StockCombos.codeOf(cmbItem);
        if (itemCode == null) {
            StockForms.showError("Stok açılış", "Satır eksik", "Malzeme seçin.");
            return;
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (item != null && item.getItemType() == IStockItem.TYPE_SERVICE) {
            StockForms.showError("Stok açılış", "Hizmet", "Hizmet kartı açılışa yazılamaz.");
            return;
        }
        Double quantity = StockForms.parseDouble(txtQuantity.getText());
        if (quantity == null || quantity <= 0d) {
            StockForms.showError("Stok açılış", "Miktar", "Miktar sıfırdan büyük olmalıdır.");
            return;
        }
        String lotNo = StockForms.trimToNull(txtLotNo.getText());
        String serialNo = StockForms.trimToNull(txtSerialNo.getText());
        String tracking = StockCard.trackingError(item, lotNo, serialNo);
        if (tracking != null) {
            StockForms.showError("Stok açılış", "Takip", tracking);
            return;
        }
        Double price = StockCard.fillPrice(item, StockForms.parseDouble(txtUnitPrice.getText()), true);
        String unit = StockCombos.codeOf(cmbUnit);
        if (unit == null && item != null) {
            unit = item.getItemUnit();
        }
        StockOpeningLine line = selected != null ? selected : new StockOpeningLine();
        line.setItemCode(itemCode);
        line.setItemDesc(item == null ? StockCombos.nameOf(cmbItem) : item.getItemDesc());
        line.setItemUnit(unit);
        line.setQuantity(quantity);
        line.setUnitPrice(price);
        line.setLotNo(lotNo);
        line.setSerialNo(serialNo);
        line.setExpiryDate(StockForms.toDateOrNull(dtExpiry.getValue()));
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
    }

    @FXML
    private void onActionBtnRemoveLine(ActionEvent event) {
        StockOpeningLine selected = tblvLines.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lines.remove(selected);
            int no = 1;
            for (StockOpeningLine line : lines) {
                line.setLineNo(no++);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.openingDao = TerpApplication.getInstance().getPersistence().createDao(StockOpening.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(StockOpeningLine.class);
        this.movementDao = TerpApplication.getInstance().getPersistence().createDao(StockMovement.class);
        this.tcLineNo.setCellValueFactory(new PropertyValueFactory<>("lineNo"));
        this.tcItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        NumericCells.apply(this.tcQuantity);
        this.tcUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        NumericCells.apply(this.tcUnitPrice);
        this.tcLotNo.setCellValueFactory(new PropertyValueFactory<>("lotNo"));
        this.tcSerialNo.setCellValueFactory(new PropertyValueFactory<>("serialNo"));
        this.tcExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryLabel"));
        this.tblvLines.setItems(lines);
        this.tblvLines.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                loadLineEditor(selected);
            }
        });
        cmbItem.valueProperty().addListener((obs, old, value) -> {
            if (!loading) {
                applyItem(StockCombos.codeOf(cmbItem));
            }
        });
        fillLookups();
        if (dtDocumentDate.getValue() == null) {
            dtDocumentDate.setValue(LocalDate.now());
        }
        applyLock(StockOpening.STATUS_DRAFT);
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
            StockForms.showError("Stok açılış", "Depo eksik", "Depo seçin.");
            return false;
        }
        if (lines.isEmpty()) {
            StockForms.showError("Stok açılış", "Satır yok", "En az bir malzeme satırı ekleyin.");
            return false;
        }
        String lineError = validateLines();
        if (lineError != null) {
            StockForms.showError("Stok açılış", "Satır", lineError);
            return false;
        }
        Date now = new Date();
        StockOpening row = this.currentRow;
        if (row == null) {
            row = openingDao.getEmpty();
            if (row == null) {
                StockForms.showError("Stok açılış", "Kayıt hatası", "Belge oluşturulamadı.");
                return false;
            }
            row.setAddedDate(now);
            row.setStatus(StockOpening.STATUS_DRAFT);
        }
        if (!row.isDraft() && !row.isCancelled()) {
            StockForms.showError("Stok açılış", "Kilitli belge",
                    "Onaylı belge taslak olarak kaydedilemez.");
            return false;
        }
        if (!CompanyScope.stamp(row)) {
            StockForms.showError("Stok açılış", "Firma", "Önce araç çubuğundan çalışma firmasını seçin.");
            return false;
        }
        String documentNo = StockForms.assignDocumentNo(DocumentNumbers.STOCK_OPENING, txtDocumentNo);
        if (documentNo == null || documentNo.isBlank()) {
            StockForms.showError("Stok açılış", "Belge no",
                    "Belge no üretilemedi. Elle yazın veya Sistem yönetimi → Belge numaraları serisini kontrol edin.");
            return false;
        }
        row.setDocumentNo(documentNo);
        row.setDocumentDate(StockForms.toDate(dtDocumentDate.getValue()));
        String periodError = WorkingPeriod.rejectIfOutside(row.getDocumentDate());
        if (periodError != null) {
            StockForms.showError("Stok açılış", "Çalışma dönemi", periodError);
            return false;
        }
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
                CompanyScope.findDuplicate(openingDao, "StockOpening", "documentNo",
                        row.getDocumentNo(), row.getRowId()),
                "Belge numarası tekrar ediyor")) {
            return false;
        }
        StockOpening saved = openingDao.addOrUpdate(row);
        if (saved == null || saved.getRowId() == null) {
            StockForms.showError("Stok açılış", "Kayıt hatası", "Belge kaydedilemedi.");
            return false;
        }
        this.currentRow = saved;
        replaceLines(saved.getRowId(), now);
        showAudit();
        return true;
    }

    private void replaceLines(Long openingId, Date now) {
        StockDocs.deleteByParent(lineDao, "StockOpeningLine", "openingId", openingId);
        int no = 1;
        List<StockOpeningLine> stored = new ArrayList<>();
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        for (StockOpeningLine line : lines) {
            StockOpeningLine copy = lineDao.getEmpty();
            if (copy == null) {
                continue;
            }
            IStockItem item = items == null ? null : items.findByCode(line.getItemCode());
            copy.setAddedDate(now);
            copy.setLastUpdateDate(now);
            copy.setOpeningId(openingId);
            copy.setLineNo(no++);
            copy.setItemCode(line.getItemCode());
            copy.setItemDesc(line.getItemDesc());
            copy.setItemUnit(line.getItemUnit());
            copy.setQuantity(line.getQuantity());
            copy.setUnitPrice(StockCard.fillPrice(item, line.getUnitPrice(), true));
            copy.setLotNo(line.getLotNo());
            copy.setSerialNo(line.getSerialNo());
            copy.setExpiryDate(line.getExpiryDate());
            StockOpeningLine saved = lineDao.addOrUpdate(copy);
            stored.add(saved == null ? copy : saved);
        }
        lines.setAll(stored);
    }

    private StockPosting toPosting(StockOpeningLine line) {
        if (line == null || line.getQuantity() == null || line.getQuantity() <= 0d) {
            return null;
        }
        StockPosting posting = new StockPosting();
        posting.setMovementDate(currentRow.getDocumentDate());
        posting.setDirection(StockDirection.IN);
        posting.setReasonCode(IMovementReason.CODE_OPENING);
        posting.setQuantity(line.getQuantity());
        posting.setWarehouseCode(currentRow.getWarehouseCode());
        posting.setWarehouseName(currentRow.getWarehouseName());
        posting.setItemCode(line.getItemCode());
        posting.setItemDesc(line.getItemDesc());
        posting.setItemUnit(line.getItemUnit());
        posting.setUnitPrice(line.getUnitPrice());
        posting.setLotNo(line.getLotNo());
        posting.setSerialNo(line.getSerialNo());
        posting.setExpiryDate(line.getExpiryDate());
        posting.setDocumentNo(currentRow.getDocumentNo());
        posting.setSourceType(StockPosting.SOURCE_STOCK_OPENING);
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
        StockForms.fillUnits(cmbUnit, List.of());
    }

    private void loadLineEditor(StockOpeningLine line) {
        StockCombos.select(cmbItem, line.getItemCode());
        applyItem(line.getItemCode());
        StockCombos.select(cmbUnit, line.getItemUnit());
        txtQuantity.setText(NumericCells.text(line.getQuantity()));
        txtUnitPrice.setText(NumericCells.text(line.getUnitPrice()));
        txtLotNo.setText(StockForms.empty(line.getLotNo()));
        txtSerialNo.setText(StockForms.empty(line.getSerialNo()));
        dtExpiry.setValue(line.getExpiryDate() == null ? null : StockForms.toLocalDate(line.getExpiryDate()));
    }

    private void applyItem(String itemCode) {
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        StockForms.fillUnits(cmbUnit, StockCard.units(item));
        if (item != null && StockCombos.codeOf(cmbUnit) == null) {
            StockCombos.select(cmbUnit, item.getItemUnit());
        }
        if (item != null && (txtUnitPrice.getText() == null || txtUnitPrice.getText().isBlank())) {
            Double price = StockCard.fillPrice(item, null, true);
            txtUnitPrice.setText(NumericCells.text(price));
        }
    }

    private String validateLines() {
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        for (StockOpeningLine line : lines) {
            if (line == null || line.getItemCode() == null || line.getItemCode().isBlank()) {
                return "Malzeme kodu boş satır var.";
            }
            if (line.getQuantity() == null || line.getQuantity() <= 0d) {
                return line.getItemCode() + " miktarı sıfırdan büyük olmalıdır.";
            }
            IStockItem item = items == null ? null : items.findByCode(line.getItemCode());
            String tracking = StockCard.trackingError(item, line.getLotNo(), line.getSerialNo());
            if (tracking != null) {
                return tracking;
            }
            line.setUnitPrice(StockCard.fillPrice(item, line.getUnitPrice(), true));
        }
        return null;
    }

    private void applyLock(int status) {
        boolean posted = status == StockOpening.STATUS_POSTED;
        boolean editable = status == StockOpening.STATUS_DRAFT || status == StockOpening.STATUS_CANCELLED;
        txtDocumentNo.setDisable(!editable);
        dtDocumentDate.setDisable(!editable);
        cmbWarehouse.setDisable(!editable);
        txtNotes.setDisable(!editable);
        cmbItem.setDisable(!editable);
        txtQuantity.setDisable(!editable);
        cmbUnit.setDisable(!editable);
        txtUnitPrice.setDisable(!editable);
        txtLotNo.setDisable(!editable);
        txtSerialNo.setDisable(!editable);
        dtExpiry.setDisable(!editable);
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
        StockEmbed.closeIfModal(btnClose);
    }
}

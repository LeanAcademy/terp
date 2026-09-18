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
package com.terp.uretim.gui;

import com.terp.plugin.CompanyScope;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.DocumentNumbers;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.IItemLookup;
import com.terp.plugin.data.IWarehouseLookup;
import com.terp.plugin.data.ProductionFromSales;
import com.terp.plugin.data.model.IStockItem;
import com.terp.plugin.gui.RecordAuditBar;
import com.terp.uretim.data.ProductionCompletion;
import com.terp.uretim.data.ProductionOrder;
import com.terp.uretim.data.ProductionOrderMaterial;
import com.terp.uretim.data.Recipe;
import com.terp.uretim.data.RecipeLine;
import com.terp.uretim.data.UretimDocs;
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
import com.terp.plugin.gui.NumericCells;

public class ProductionOrderEditFormController implements Initializable {

    @FXML
    private Button btnSave;
    @FXML
    private Button btnApprove;
    @FXML
    private Button btnExplode;
    @FXML
    private Button btnCancelPosted;
    @FXML
    private Button btnClose;
    @FXML
    private TextField txtDocumentNo;
    @FXML
    private DatePicker dtDocumentDate;
    @FXML
    private ComboBox<String> cmbProduct;
    @FXML
    private ComboBox<String> cmbWarehouse;
    @FXML
    private TextField txtPlannedQty;
    @FXML
    private TextField txtCompletedQty;
    @FXML
    private TextField txtSourceOrderNo;
    @FXML
    private TextField txtProductUnit;
    @FXML
    private TextArea txtNotes;
    @FXML
    private ComboBox<String> cmbItem;
    @FXML
    private TextField txtQuantity;
    @FXML
    private TextField txtUnit;
    @FXML
    private Button btnAddLine;
    @FXML
    private Button btnRemoveLine;
    @FXML
    private TableView<ProductionOrderMaterial> tblvLines;
    @FXML
    private TableColumn<ProductionOrderMaterial, String> tcItemCode;
    @FXML
    private TableColumn<ProductionOrderMaterial, String> tcItemDesc;
    @FXML
    private TableColumn<ProductionOrderMaterial, String> tcItemUnit;
    @FXML
    private TableColumn<ProductionOrderMaterial, Double> tcPlannedQty;

    private ICommonDao<ProductionOrder> orderDao;
    private ICommonDao<ProductionOrderMaterial> materialDao;
    private ICommonDao<ProductionCompletion> completionDao;
    private ICommonDao<Recipe> recipeDao;
    private ICommonDao<RecipeLine> recipeLineDao;
    private ProductionOrder currentRow;
    private Long sourceOrderId;
    private Long sourceOrderLineId;
    private final ObservableList<ProductionOrderMaterial> lines = FXCollections.observableArrayList();
    private RecordAuditBar auditBar;
    private Runnable afterChange;
    private boolean notifying;

    public void initializeForm(ProductionOrder row, ProductionFromSales fromSales) {
        fillLookups();
        lines.clear();
        this.currentRow = row;
        this.sourceOrderId = null;
        this.sourceOrderLineId = null;
        txtDocumentNo.clear();
        dtDocumentDate.setValue(LocalDate.now());
        txtPlannedQty.clear();
        txtCompletedQty.setText(NumericCells.format(0d));
        txtSourceOrderNo.clear();
        txtProductUnit.clear();
        txtNotes.clear();
        txtQuantity.clear();
        txtUnit.clear();
        cmbProduct.getSelectionModel().clearSelection();
        cmbWarehouse.getSelectionModel().clearSelection();
        cmbItem.getSelectionModel().clearSelection();
        if (fromSales != null) {
            UretimCombos.select(cmbProduct, fromSales.getProductCode());
            UretimCombos.select(cmbWarehouse, fromSales.getWarehouseCode());
            txtPlannedQty.setText(NumericCells.text(fromSales.getQuantity()));
            txtProductUnit.setText(UretimDates.empty(fromSales.getProductUnit()));
            txtSourceOrderNo.setText(UretimDates.empty(fromSales.getSourceOrderNo()));
            this.sourceOrderId = fromSales.getSourceOrderId();
            this.sourceOrderLineId = fromSales.getSourceOrderLineId();
            explodeRecipe(false);
        } else if (row != null) {
            txtDocumentNo.setText(UretimDates.empty(row.getDocumentNo()));
            dtDocumentDate.setValue(UretimDates.toLocalDate(row.getDocumentDate()));
            UretimCombos.select(cmbProduct, row.getProductCode());
            UretimCombos.select(cmbWarehouse, row.getWarehouseCode());
            txtPlannedQty.setText(NumericCells.text(row.getPlannedQty()));
            txtCompletedQty.setText(NumericCells.text(row.getCompletedQty()));
            txtProductUnit.setText(UretimDates.empty(row.getProductUnit()));
            txtSourceOrderNo.setText(UretimDates.empty(row.getSourceOrderNo()));
            txtNotes.setText(UretimDates.empty(row.getNotes()));
            this.sourceOrderId = row.getSourceOrderId();
            this.sourceOrderLineId = row.getSourceOrderLineId();
            lines.setAll(UretimDocs.materialsOf(materialDao, row.getRowId()));
        }
        applyLock(currentRow == null ? ProductionOrder.STATUS_DRAFT : currentRow.getStatus());
        showAudit();
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
        currentRow.setStatus(ProductionOrder.STATUS_APPROVED);
        currentRow.setLastUpdateDate(new Date());
        UretimDocs.syncStatus(orderDao, completionDao, currentRow);
        applyLock(currentRow.getStatus());
        showAudit();
        notifyChanged();
    }

    @FXML
    private void onActionBtnExplode(ActionEvent event) {
        explodeRecipe(true);
    }

    @FXML
    private void onActionBtnCancelPosted(ActionEvent event) {
        if (currentRow == null || currentRow.getRowId() == null || currentRow.isDraft()) {
            return;
        }
        if (UretimDocs.hasPostedCompletion(completionDao, currentRow.getRowId())) {
            UretimDates.showError("Üretim emri", "İş bitiş var",
                    "Önce iş bitiş kayıtlarını iptal edin.");
            return;
        }
        currentRow.setStatus(ProductionOrder.STATUS_CANCELLED);
        currentRow.setLastUpdateDate(new Date());
        orderDao.addOrUpdate(currentRow);
        applyLock(currentRow.getStatus());
        showAudit();
        notifyChanged();
        close();
    }

    @FXML
    private void onActionBtnClose(ActionEvent event) {
        close();
    }

    @FXML
    private void onActionBtnAddLine(ActionEvent event) {
        ProductionOrderMaterial selected = tblvLines.getSelectionModel().getSelectedItem();
        String itemCode = UretimCombos.codeOf(cmbItem);
        Double quantity = UretimDates.parseDouble(txtQuantity.getText());
        if (itemCode == null || quantity == null || quantity <= 0d) {
            UretimDates.showError("Üretim emri", "Satır eksik", "Malzeme ve sarf miktarı zorunludur.");
            return;
        }
        String productCode = UretimCombos.codeOf(cmbProduct);
        if (productCode != null && productCode.equalsIgnoreCase(itemCode)) {
            UretimDates.showError("Üretim emri", "Geçersiz satır", "Mamul kendi çekme listesinde olamaz.");
            return;
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (!UretimDocs.isComponent(item)) {
            UretimDates.showError("Üretim emri", "Geçersiz malzeme",
                    "Çekme listesi hammadde, yarı mamul veya sarf olmalıdır.");
            return;
        }
        ProductionOrderMaterial line = selected != null ? selected : new ProductionOrderMaterial();
        line.setItemCode(itemCode);
        line.setItemDesc(item == null ? UretimCombos.nameOf(cmbItem) : item.getItemDesc());
        String unit = UretimDates.trimToNull(txtUnit.getText());
        line.setItemUnit(unit == null && item != null ? item.getItemUnit() : unit);
        line.setPlannedQty(quantity);
        if (selected == null) {
            line.setLineNo(lines.size() + 1);
            lines.add(line);
        } else {
            tblvLines.refresh();
        }
        txtQuantity.clear();
        tblvLines.getSelectionModel().clearSelection();
    }

    @FXML
    private void onActionBtnRemoveLine(ActionEvent event) {
        ProductionOrderMaterial selected = tblvLines.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lines.remove(selected);
            int no = 1;
            for (ProductionOrderMaterial line : lines) {
                line.setLineNo(no++);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.orderDao = TerpApplication.getInstance().getPersistence().createDao(ProductionOrder.class);
        this.materialDao = TerpApplication.getInstance().getPersistence()
                .createDao(ProductionOrderMaterial.class);
        this.completionDao = TerpApplication.getInstance().getPersistence()
                .createDao(ProductionCompletion.class);
        this.recipeDao = TerpApplication.getInstance().getPersistence().createDao(Recipe.class);
        this.recipeLineDao = TerpApplication.getInstance().getPersistence().createDao(RecipeLine.class);
        this.tcItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcPlannedQty.setCellValueFactory(new PropertyValueFactory<>("plannedQty"));
        NumericCells.apply(this.tcPlannedQty);
        this.tblvLines.setItems(lines);
        this.tblvLines.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                loadLineEditor(selected);
            }
        });
        cmbProduct.valueProperty().addListener((obs, old, value) -> applyProduct(UretimCombos.codeOf(cmbProduct)));
        cmbItem.valueProperty().addListener((obs, old, value) -> applyItem(UretimCombos.codeOf(cmbItem)));
        this.auditBar = RecordAuditBar.install(txtDocumentNo);
        showAudit();
        Platform.runLater(() -> UretimEmbed.hideCloseIfDesktop(btnClose));
    }

    private void explodeRecipe(boolean confirmIfFilled) {
        String productCode = UretimCombos.codeOf(cmbProduct);
        Double planned = UretimDates.parseDouble(txtPlannedQty.getText());
        if (productCode == null || planned == null || planned <= 0d) {
            UretimDates.showError("Üretim emri", "Mamul / miktar",
                    "Önce mamul ve planlanan miktarı girin.");
            return;
        }
        Recipe recipe = UretimDocs.findActiveRecipe(recipeDao, productCode);
        if (recipe == null) {
            UretimDates.showError("Üretim emri", "Reçete yok",
                    productCode + " için aktif reçete yok.");
            return;
        }
        if (confirmIfFilled && !lines.isEmpty()
                && !UretimDates.confirm("Reçeteyi patlat", "Çekme listesi reçeteden yeniden doldurulacak.")) {
            return;
        }
        List<RecipeLine> recipeLines = UretimDocs.recipeLines(recipeLineDao, recipe.getRowId());
        if (recipeLines.isEmpty()) {
            UretimDates.showError("Üretim emri", "Reçete boş", "Reçetede bileşen yok.");
            return;
        }
        lines.setAll(UretimDocs.explode(recipe, recipeLines, planned));
        if (currentRow != null) {
            currentRow.setRecipeId(recipe.getRowId());
            currentRow.setYieldQuantity(recipe.getYieldQuantity());
        }
    }

    private boolean saveDraft() {
        String productCode = UretimCombos.codeOf(cmbProduct);
        String warehouseCode = UretimCombos.codeOf(cmbWarehouse);
        Double planned = UretimDates.parseDouble(txtPlannedQty.getText());
        if (productCode == null || warehouseCode == null || planned == null || planned <= 0d) {
            UretimDates.showError("Üretim emri", "Eksik bilgi",
                    "Mamul, depo ve planlanan miktar zorunludur.");
            return false;
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem product = items == null ? null : items.findByCode(productCode);
        if (!UretimDocs.isProducible(product)) {
            UretimDates.showError("Üretim emri", "Geçersiz mamul",
                    "Yalnız yarı mamul ve mamul üretilebilir.");
            return false;
        }
        if (lines.isEmpty()) {
            UretimDates.showError("Üretim emri", "Çekme listesi yok",
                    "Reçeteyi patlatın veya malzeme satırı ekleyin.");
            return false;
        }
        Date now = new Date();
        ProductionOrder row = this.currentRow;
        if (row == null) {
            row = orderDao.getEmpty();
            if (row == null) {
                UretimDates.showError("Üretim emri", "Kayıt hatası", "Emir oluşturulamadı.");
                return false;
            }
            row.setAddedDate(now);
            row.setStatus(ProductionOrder.STATUS_DRAFT);
        }
        if (!row.isDraft() && !row.isCancelled()) {
            return true;
        }
        if (!CompanyScope.stamp(row)) {
            UretimDates.showError("Üretim emri", "Firma",
                    "Önce araç çubuğundan çalışma firmasını seçin.");
            return false;
        }
        String documentNo = UretimDates.assignDocumentNo(DocumentNumbers.PRODUCTION_ORDER, txtDocumentNo);
        if (documentNo == null || documentNo.isBlank()) {
            UretimDates.showError("Üretim emri", "Belge no", "Belge numarası alınamadı.");
            return false;
        }
        Recipe recipe = UretimDocs.findActiveRecipe(recipeDao, productCode);
        row.setDocumentNo(documentNo);
        row.setDocumentDate(UretimDates.toDate(dtDocumentDate.getValue()));
        row.setWarehouseCode(warehouseCode);
        row.setWarehouseName(UretimCombos.nameOf(cmbWarehouse));
        row.setProductCode(productCode);
        row.setProductName(product == null ? UretimCombos.nameOf(cmbProduct) : product.getItemDesc());
        row.setProductUnit(product == null ? UretimDates.trimToNull(txtProductUnit.getText())
                : product.getItemUnit());
        row.setPlannedQty(planned);
        row.setNotes(UretimDates.trimToNull(txtNotes.getText()));
        row.setSourceOrderId(sourceOrderId);
        row.setSourceOrderNo(UretimDates.trimToNull(txtSourceOrderNo.getText()));
        row.setSourceOrderLineId(sourceOrderLineId);
        if (recipe != null) {
            row.setRecipeId(recipe.getRowId());
            row.setYieldQuantity(recipe.getYieldQuantity());
        }
        row.setLastUpdateDate(now);
        if (row.getAddedDate() == null) {
            row.setAddedDate(now);
        }
        if (CompanyScope.rejectDuplicate(
                CompanyScope.findDuplicate(orderDao, "ProductionOrder", "documentNo",
                        row.getDocumentNo(), row.getRowId()),
                "Belge numarası tekrar ediyor")) {
            return false;
        }
        ProductionOrder saved = orderDao.addOrUpdate(row);
        if (saved == null || saved.getRowId() == null) {
            UretimDates.showError("Üretim emri", "Kayıt hatası", "Emir kaydedilemedi.");
            return false;
        }
        this.currentRow = saved;
        replaceLines(saved.getRowId(), now);
        applyLock(saved.getStatus());
        showAudit();
        notifyChanged();
        return true;
    }

    private void replaceLines(Long orderId, Date now) {
        UretimDocs.deleteByParent(materialDao, "ProductionOrderMaterial", "orderId", orderId);
        int no = 1;
        List<ProductionOrderMaterial> stored = new ArrayList<>();
        for (ProductionOrderMaterial line : lines) {
            ProductionOrderMaterial copy = materialDao.getEmpty();
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
            copy.setPlannedQty(line.getPlannedQty());
            copy.setRecipeLineId(line.getRecipeLineId());
            ProductionOrderMaterial saved = materialDao.addOrUpdate(copy);
            stored.add(saved == null ? copy : saved);
        }
        lines.setAll(stored);
    }

    private void fillLookups() {
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        List<IStockItem> rows = items == null ? List.of() : items.findActive();
        UretimCombos.fillProducts(cmbProduct, rows);
        UretimCombos.fillComponents(cmbItem, rows);
        IWarehouseLookup warehouses = TerpApplication.getInstance().getWarehouseLookup();
        UretimCombos.fillWarehouses(cmbWarehouse, warehouses == null ? List.of() : warehouses.findActive());
    }

    private void applyProduct(String itemCode) {
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (item != null) {
            txtProductUnit.setText(UretimDates.empty(item.getItemUnit()));
        }
    }

    private void loadLineEditor(ProductionOrderMaterial line) {
        UretimCombos.select(cmbItem, line.getItemCode());
        txtUnit.setText(UretimDates.empty(line.getItemUnit()));
        txtQuantity.setText(NumericCells.text(line.getPlannedQty()));
    }

    private void applyItem(String itemCode) {
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (item != null && (txtUnit.getText() == null || txtUnit.getText().isBlank())) {
            txtUnit.setText(UretimDates.empty(item.getItemUnit()));
        }
    }

    private void applyLock(int status) {
        boolean draft = status == ProductionOrder.STATUS_DRAFT;
        boolean approved = status == ProductionOrder.STATUS_APPROVED;
        boolean editable = draft || status == ProductionOrder.STATUS_CANCELLED;
        txtDocumentNo.setDisable(!editable);
        dtDocumentDate.setDisable(!editable);
        cmbProduct.setDisable(!editable);
        cmbWarehouse.setDisable(!editable);
        txtPlannedQty.setDisable(!editable);
        txtNotes.setDisable(!editable);
        cmbItem.setDisable(!editable);
        txtQuantity.setDisable(!editable);
        txtUnit.setDisable(!editable);
        btnAddLine.setDisable(!editable);
        btnRemoveLine.setDisable(!editable);
        btnSave.setDisable(!editable);
        btnApprove.setDisable(!editable);
        btnExplode.setDisable(!editable);
        boolean posted = UretimDocs.hasPostedCompletion(completionDao,
                currentRow == null ? null : currentRow.getRowId());
        btnCancelPosted.setDisable(!approved || posted);
        btnCancelPosted.setVisible(approved);
        btnCancelPosted.setManaged(approved);
    }

    private void showAudit() {
        if (auditBar != null) {
            auditBar.bind(currentRow);
        }
    }

    private void close() {
        notifyChanged();
        UretimEmbed.closeIfModal(btnClose);
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

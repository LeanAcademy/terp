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
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.IItemLookup;
import com.terp.plugin.data.IStockLedger;
import com.terp.plugin.data.StockCard;
import com.terp.plugin.data.StockDirection;
import com.terp.plugin.data.StockPosting;
import com.terp.plugin.data.WorkingPeriod;
import com.terp.plugin.data.model.IMovementReason;
import com.terp.plugin.data.model.IStockItem;
import com.terp.uretim.data.ProductionCompletion;
import com.terp.uretim.data.ProductionCompletionMaterial;
import com.terp.uretim.data.ProductionOrder;
import com.terp.uretim.data.ProductionOrderMaterial;
import com.terp.uretim.data.UretimDocs;
import java.net.URL;
import java.time.LocalDate;
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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import com.terp.plugin.gui.NumericCells;

public class ProductionCompletionFormController implements Initializable {

    @FXML
    private Button btnApprove;
    @FXML
    private Button btnClose;
    @FXML
    private ComboBox<String> cmbOrder;
    @FXML
    private TextField txtRemaining;
    @FXML
    private DatePicker dtDocumentDate;
    @FXML
    private TextField txtProducedQty;
    @FXML
    private TextField txtLotNo;
    @FXML
    private TextField txtSerialNo;
    @FXML
    private DatePicker dtExpiry;
    @FXML
    private TableView<ProductionCompletionMaterial> tblvLines;
    @FXML
    private TableColumn<ProductionCompletionMaterial, String> tcItemCode;
    @FXML
    private TableColumn<ProductionCompletionMaterial, String> tcItemDesc;
    @FXML
    private TableColumn<ProductionCompletionMaterial, String> tcItemUnit;
    @FXML
    private TableColumn<ProductionCompletionMaterial, Double> tcQuantity;
    @FXML
    private TableColumn<ProductionCompletionMaterial, String> tcLotNo;
    @FXML
    private TableColumn<ProductionCompletionMaterial, String> tcSerialNo;

    private ICommonDao<ProductionOrder> orderDao;
    private ICommonDao<ProductionOrderMaterial> materialDao;
    private ICommonDao<ProductionCompletion> completionDao;
    private ICommonDao<ProductionCompletionMaterial> completionMaterialDao;
    private ProductionOrder currentOrder;
    private final ObservableList<ProductionCompletionMaterial> lines = FXCollections.observableArrayList();

    public void initializeForm(ProductionOrder order) {
        lines.clear();
        dtDocumentDate.setValue(LocalDate.now());
        UretimCombos.fillCompletableOrders(cmbOrder, UretimDocs.completableOrders(orderDao));
        if (order != null && order.getDocumentNo() != null) {
            UretimCombos.select(cmbOrder, order.getDocumentNo());
        }
        applySelectedOrder();
    }

    @FXML
    private void onActionBtnApprove(ActionEvent event) {
        if (currentOrder == null || currentOrder.getRowId() == null) {
            UretimDates.showError("İş bitti", "Emir", "Üretim emri seçin.");
            return;
        }
        if (!currentOrder.canComplete()) {
            UretimDates.showError("İş bitti", "Kapalı", "Bu emirde üretilecek kalan yok.");
            return;
        }
        Double produced = UretimDates.parseDouble(txtProducedQty.getText());
        if (produced == null || produced <= UretimDocs.EPS) {
            UretimDates.showError("İş bitti", "Miktar", "Üretilen miktar sıfırdan büyük olmalıdır.");
            return;
        }
        if (produced > currentOrder.remainingQty() + UretimDocs.EPS) {
            UretimDates.showError("İş bitti", "Miktar",
                    "Üretilen miktar kalan " + currentOrder.remainingQty() + " değerini aşamaz.");
            return;
        }
        String periodError = WorkingPeriod.rejectIfOutside(UretimDates.toDate(dtDocumentDate.getValue()));
        if (periodError != null) {
            UretimDates.showError("İş bitti", "Dönem", periodError);
            return;
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem product = items == null ? null : items.findByCode(currentOrder.getProductCode());
        String productTracking = StockCard.trackingError(product, UretimDates.trimToNull(txtLotNo.getText()),
                UretimDates.trimToNull(txtSerialNo.getText()));
        if (productTracking != null) {
            UretimDates.showError("İş bitti", "İzleme", productTracking);
            return;
        }
        for (ProductionCompletionMaterial line : lines) {
            if (line == null || line.getQuantity() == null || line.getQuantity() <= UretimDocs.EPS) {
                continue;
            }
            IStockItem material = items == null ? null : items.findByCode(line.getItemCode());
            String tracking = StockCard.trackingError(material, line.getLotNo(), line.getSerialNo());
            if (tracking != null) {
                UretimDates.showError("İş bitti", "İzleme", tracking);
                return;
            }
        }
        IStockLedger ledger = TerpApplication.getInstance().getStockLedger();
        if (ledger == null) {
            UretimDates.showError("İş bitti", "Stok eklentisi yok",
                    "Sarf ve giriş fişi için terp.stok yüklü olmalıdır.");
            return;
        }
        for (ProductionCompletionMaterial line : lines) {
            if (line != null && line.getQuantity() != null && line.getQuantity() < 0d) {
                UretimDates.showError("İş bitti", "Sarf", "Sarf miktarı negatif olamaz.");
                return;
            }
        }
        Date now = new Date();
        ProductionCompletion completion = completionDao.getEmpty();
        if (completion == null) {
            UretimDates.showError("İş bitti", "Kayıt hatası", "İş bitiş oluşturulamadı.");
            return;
        }
        if (!CompanyScope.stamp(completion)) {
            UretimDates.showError("İş bitti", "Firma",
                    "Önce araç çubuğundan çalışma firmasını seçin.");
            return;
        }
        completion.setAddedDate(now);
        completion.setLastUpdateDate(now);
        completion.setOrderId(currentOrder.getRowId());
        completion.setDocumentDate(UretimDates.toDate(dtDocumentDate.getValue()));
        completion.setProducedQty(produced);
        completion.setLotNo(UretimDates.trimToNull(txtLotNo.getText()));
        completion.setSerialNo(UretimDates.trimToNull(txtSerialNo.getText()));
        completion.setExpiryDate(dtExpiry.getValue() == null ? null : UretimDates.toDate(dtExpiry.getValue()));
        completion.setStatus(ProductionCompletion.STATUS_POSTED);
        ProductionCompletion saved = null;
        try {
            saved = completionDao.addOrUpdate(completion);
            if (saved == null || saved.getRowId() == null) {
                throw new IllegalStateException("İş bitiş kaydedilemedi.");
            }
            int no = 1;
            for (ProductionCompletionMaterial line : lines) {
                if (line == null || line.getItemCode() == null) {
                    continue;
                }
                double qty = line.getQuantity() == null ? 0d : line.getQuantity();
                ProductionCompletionMaterial copy = completionMaterialDao.getEmpty();
                if (copy == null) {
                    continue;
                }
                copy.setAddedDate(now);
                copy.setLastUpdateDate(now);
                copy.setCompletionId(saved.getRowId());
                copy.setLineNo(no++);
                copy.setItemCode(line.getItemCode());
                copy.setItemDesc(line.getItemDesc());
                copy.setItemUnit(line.getItemUnit());
                copy.setQuantity(qty);
                copy.setLotNo(line.getLotNo());
                copy.setSerialNo(line.getSerialNo());
                copy.setExpiryDate(line.getExpiryDate());
                completionMaterialDao.addOrUpdate(copy);
            }
            for (ProductionCompletionMaterial line : lines) {
                if (line == null || line.getQuantity() == null || line.getQuantity() <= UretimDocs.EPS) {
                    continue;
                }
                ledger.post(consumePosting(saved, line));
            }
            ledger.post(outputPosting(saved, produced));
            UretimDocs.syncStatus(orderDao, completionDao, currentOrder);
            close();
        } catch (RuntimeException ex) {
            if (saved != null && saved.getRowId() != null) {
                ledger.reverseByDocument(StockPosting.SOURCE_PRODUCTION_COMPLETION, saved.getRowId());
                UretimDocs.deleteCompletion(completionDao, completionMaterialDao, saved.getRowId());
            }
            UretimDates.showError("İş bitti", "Stok fişi yazılamadı",
                    ex.getMessage() == null ? "Onay başarısız." : ex.getMessage());
        }
    }

    @FXML
    private void onActionBtnClose(ActionEvent event) {
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.orderDao = TerpApplication.getInstance().getPersistence().createDao(ProductionOrder.class);
        this.materialDao = TerpApplication.getInstance().getPersistence()
                .createDao(ProductionOrderMaterial.class);
        this.completionDao = TerpApplication.getInstance().getPersistence()
                .createDao(ProductionCompletion.class);
        this.completionMaterialDao = TerpApplication.getInstance().getPersistence()
                .createDao(ProductionCompletionMaterial.class);
        this.tcItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        NumericCells.apply(this.tcQuantity);
        this.tcLotNo.setCellValueFactory(new PropertyValueFactory<>("lotNo"));
        this.tcSerialNo.setCellValueFactory(new PropertyValueFactory<>("serialNo"));
        NumericCells.applyEditable(this.tcQuantity);
        this.tcLotNo.setCellFactory(TextFieldTableCell.forTableColumn());
        this.tcSerialNo.setCellFactory(TextFieldTableCell.forTableColumn());
        this.tcQuantity.setOnEditCommit(event -> {
            ProductionCompletionMaterial line = event.getRowValue();
            if (line != null) {
                line.setQuantity(event.getNewValue());
            }
        });
        this.tcLotNo.setOnEditCommit(event -> {
            ProductionCompletionMaterial line = event.getRowValue();
            if (line != null) {
                line.setLotNo(event.getNewValue());
            }
        });
        this.tcSerialNo.setOnEditCommit(event -> {
            ProductionCompletionMaterial line = event.getRowValue();
            if (line != null) {
                line.setSerialNo(event.getNewValue());
            }
        });
        this.tblvLines.setItems(lines);
        this.cmbOrder.valueProperty().addListener((obs, old, value) -> applySelectedOrder());
        this.txtProducedQty.focusedProperty().addListener((obs, was, focused) -> {
            if (was && !focused) {
                fillDefaults();
            }
        });
    }

    private void applySelectedOrder() {
        this.currentOrder = orderByDocumentNo(UretimCombos.codeOf(cmbOrder));
        if (currentOrder != null && currentOrder.getRowId() != null) {
            this.currentOrder = orderDao.firstOrDefault(currentOrder.getRowId());
        }
        if (currentOrder == null || !currentOrder.canComplete()) {
            this.currentOrder = null;
            txtRemaining.clear();
            txtProducedQty.clear();
            lines.clear();
            return;
        }
        txtRemaining.setText(NumericCells.format(currentOrder.remainingQty()));
        txtProducedQty.setText(NumericCells.format(currentOrder.remainingQty()));
        fillDefaults();
    }

    private ProductionOrder orderByDocumentNo(String documentNo) {
        if (documentNo == null || documentNo.isBlank()) {
            return null;
        }
        String escaped = documentNo.replace("'", "''");
        return orderDao.firstOrDefault(
                "from ProductionOrder e where " + CompanyScope.predicate("e")
                        + " and e.documentNo = '" + escaped + "'");
    }

    private void fillDefaults() {
        if (currentOrder == null) {
            return;
        }
        Double produced = UretimDates.parseDouble(txtProducedQty.getText());
        double remainingProduce = currentOrder.remainingQty();
        if (produced == null || produced <= 0d || remainingProduce <= UretimDocs.EPS) {
            return;
        }
        lines.clear();
        List<ProductionOrderMaterial> materials = UretimDocs.materialsOf(materialDao, currentOrder.getRowId());
        int no = 1;
        for (ProductionOrderMaterial material : materials) {
            if (material == null || material.getItemCode() == null) {
                continue;
            }
            double posted = UretimDocs.postedConsume(completionDao, completionMaterialDao,
                    currentOrder.getRowId(), material.getItemCode());
            double remainingMaterial = UretimDocs.remaining(material.getPlannedQty(), posted);
            ProductionCompletionMaterial line = new ProductionCompletionMaterial();
            line.setLineNo(no++);
            line.setItemCode(material.getItemCode());
            line.setItemDesc(material.getItemDesc());
            line.setItemUnit(material.getItemUnit());
            line.setQuantity(UretimDocs.scale(remainingMaterial, produced, remainingProduce));
            IStockLedger ledger = TerpApplication.getInstance().getStockLedger();
            if (ledger != null && currentOrder.getWarehouseCode() != null) {
                List<String> lots = ledger.fefoLots(currentOrder.getWarehouseCode(), material.getItemCode());
                if (lots != null && !lots.isEmpty() && lots.get(0) != null) {
                    String label = lots.get(0);
                    int sep = label.indexOf(" — ");
                    line.setLotNo(sep < 0 ? label : label.substring(0, sep));
                }
            }
            lines.add(line);
        }
    }

    private StockPosting consumePosting(ProductionCompletion completion, ProductionCompletionMaterial line) {
        StockPosting posting = new StockPosting();
        posting.setMovementDate(completion.getDocumentDate());
        posting.setDirection(StockDirection.OUT);
        posting.setReasonCode(IMovementReason.CODE_PRODUCTION_OUT);
        posting.setWarehouseCode(currentOrder.getWarehouseCode());
        posting.setWarehouseName(currentOrder.getWarehouseName());
        posting.setItemCode(line.getItemCode());
        posting.setItemDesc(line.getItemDesc());
        posting.setItemUnit(line.getItemUnit());
        posting.setQuantity(line.getQuantity());
        posting.setLotNo(line.getLotNo());
        posting.setSerialNo(line.getSerialNo());
        posting.setExpiryDate(line.getExpiryDate());
        posting.setDocumentNo(currentOrder.getDocumentNo());
        posting.setSourceType(StockPosting.SOURCE_PRODUCTION_COMPLETION);
        posting.setSourceId(completion.getRowId());
        return posting;
    }

    private StockPosting outputPosting(ProductionCompletion completion, double produced) {
        StockPosting posting = new StockPosting();
        posting.setMovementDate(completion.getDocumentDate());
        posting.setDirection(StockDirection.IN);
        posting.setReasonCode(IMovementReason.CODE_PRODUCTION_IN);
        posting.setWarehouseCode(currentOrder.getWarehouseCode());
        posting.setWarehouseName(currentOrder.getWarehouseName());
        posting.setItemCode(currentOrder.getProductCode());
        posting.setItemDesc(currentOrder.getProductName());
        posting.setItemUnit(currentOrder.getProductUnit());
        posting.setQuantity(produced);
        posting.setLotNo(completion.getLotNo());
        posting.setSerialNo(completion.getSerialNo());
        posting.setExpiryDate(completion.getExpiryDate());
        posting.setDocumentNo(currentOrder.getDocumentNo());
        posting.setSourceType(StockPosting.SOURCE_PRODUCTION_COMPLETION);
        posting.setSourceId(completion.getRowId());
        return posting;
    }

    private void close() {
        UretimEmbed.closeIfModal(btnClose);
    }
}

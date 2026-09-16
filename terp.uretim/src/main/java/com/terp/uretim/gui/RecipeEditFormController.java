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
import com.terp.plugin.data.model.IStockItem;
import com.terp.plugin.gui.RecordAuditBar;
import com.terp.uretim.data.Recipe;
import com.terp.uretim.data.RecipeLine;
import com.terp.uretim.data.UretimDocs;
import java.net.URL;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class RecipeEditFormController implements Initializable {

    private static final String STATUS_ACTIVE = "Aktif";
    private static final String STATUS_INACTIVE = "Pasif";

    @FXML
    private Button btnSave;
    @FXML
    private Button btnClose;
    @FXML
    private ComboBox<String> cmbProduct;
    @FXML
    private TextField txtYieldQuantity;
    @FXML
    private TextField txtProductUnit;
    @FXML
    private ComboBox<String> cmbStatus;
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
    private TableView<RecipeLine> tblvLines;
    @FXML
    private TableColumn<RecipeLine, String> tcItemCode;
    @FXML
    private TableColumn<RecipeLine, String> tcItemDesc;
    @FXML
    private TableColumn<RecipeLine, String> tcItemUnit;
    @FXML
    private TableColumn<RecipeLine, Double> tcQuantity;

    private ICommonDao<Recipe> recipeDao;
    private ICommonDao<RecipeLine> lineDao;
    private Recipe currentRow;
    private final ObservableList<RecipeLine> lines = FXCollections.observableArrayList();
    private RecordAuditBar auditBar;

    public void initializeForm(Recipe row) {
        fillLookups();
        lines.clear();
        txtNotes.clear();
        txtQuantity.clear();
        txtUnit.clear();
        txtProductUnit.clear();
        txtYieldQuantity.setText("1");
        cmbItem.getSelectionModel().clearSelection();
        cmbProduct.getSelectionModel().clearSelection();
        cmbStatus.getSelectionModel().select(STATUS_ACTIVE);
        this.currentRow = row;
        if (row == null) {
            showAudit();
            return;
        }
        UretimCombos.select(cmbProduct, row.getProductCode());
        txtProductUnit.setText(UretimDates.empty(row.getProductUnit()));
        txtYieldQuantity.setText(row.getYieldQuantity() == null ? "1" : String.valueOf(row.getYieldQuantity()));
        cmbStatus.getSelectionModel().select(row.isActive() ? STATUS_ACTIVE : STATUS_INACTIVE);
        txtNotes.setText(UretimDates.empty(row.getNotes()));
        if (row.getRowId() != null) {
            List<RecipeLine> stored = lineDao.findAll(
                    "from RecipeLine e where e.recipeId = " + row.getRowId()
                            + " order by e.lineNo");
            if (stored != null) {
                lines.setAll(stored);
            }
        }
        showAudit();
    }

    @FXML
    private void onActionBtnSave(ActionEvent event) {
        if (save()) {
            close();
        }
    }

    @FXML
    private void onActionBtnClose(ActionEvent event) {
        close();
    }

    @FXML
    private void onActionBtnAddLine(ActionEvent event) {
        String itemCode = UretimCombos.codeOf(cmbItem);
        Double quantity = UretimDates.parseDouble(txtQuantity.getText());
        if (itemCode == null || quantity == null || quantity <= 0d) {
            UretimDates.showError("Reçete", "Satır eksik", "Bileşen ve miktar zorunludur.");
            return;
        }
        String productCode = UretimCombos.codeOf(cmbProduct);
        if (productCode != null && productCode.equalsIgnoreCase(itemCode)) {
            UretimDates.showError("Reçete", "Geçersiz satır", "Mamul kendi reçetesinde bileşen olamaz.");
            return;
        }
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (!UretimDocs.isComponent(item)) {
            UretimDates.showError("Reçete", "Geçersiz bileşen",
                    "Bileşen hammadde, yarı mamul veya sarf olmalıdır.");
            return;
        }
        RecipeLine line = new RecipeLine();
        line.setItemCode(itemCode);
        line.setItemDesc(item == null ? UretimCombos.nameOf(cmbItem) : item.getItemDesc());
        String unit = UretimDates.trimToNull(txtUnit.getText());
        line.setItemUnit(unit == null && item != null ? item.getItemUnit() : unit);
        line.setQuantity(quantity);
        line.setLineNo(lines.size() + 1);
        lines.add(line);
        txtQuantity.clear();
    }

    @FXML
    private void onActionBtnRemoveLine(ActionEvent event) {
        RecipeLine selected = tblvLines.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lines.remove(selected);
            int no = 1;
            for (RecipeLine line : lines) {
                line.setLineNo(no++);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.recipeDao = TerpApplication.getInstance().getPersistence().createDao(Recipe.class);
        this.lineDao = TerpApplication.getInstance().getPersistence().createDao(RecipeLine.class);
        this.tcItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        this.tcItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        this.tcItemUnit.setCellValueFactory(new PropertyValueFactory<>("itemUnit"));
        this.tcQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        this.tblvLines.setItems(lines);
        cmbStatus.getItems().addAll(STATUS_ACTIVE, STATUS_INACTIVE);
        cmbStatus.getSelectionModel().select(STATUS_ACTIVE);
        cmbProduct.valueProperty().addListener((obs, old, value) -> applyProduct(UretimCombos.codeOf(cmbProduct)));
        cmbItem.valueProperty().addListener((obs, old, value) -> applyItem(UretimCombos.codeOf(cmbItem)));
        this.auditBar = RecordAuditBar.install(cmbProduct);
        showAudit();
        Platform.runLater(() -> UretimEmbed.hideCloseIfDesktop(btnClose));
    }

    private void showAudit() {
        if (auditBar != null) {
            auditBar.bind(currentRow);
        }
    }

    private boolean save() {
        String productCode = UretimCombos.codeOf(cmbProduct);
        if (productCode == null) {
            UretimDates.showError("Reçete", "Mamul eksik", "Yarı mamul veya mamul seçin.");
            return false;
        }
        IItemLookup lookup = TerpApplication.getInstance().getItemLookup();
        IStockItem product = lookup == null ? null : lookup.findByCode(productCode);
        if (!UretimDocs.isProducible(product)) {
            UretimDates.showError("Reçete", "Geçersiz mamul",
                    "Reçete yalnız yarı mamul ve mamul için tanımlanır.");
            return false;
        }
        Double yield = UretimDates.parseDouble(txtYieldQuantity.getText());
        if (yield == null || yield <= 0d) {
            UretimDates.showError("Reçete", "Miktar eksik", "Mamul miktarı sıfırdan büyük olmalıdır.");
            return false;
        }
        if (lines.isEmpty()) {
            UretimDates.showError("Reçete", "Satır yok", "En az bir bileşen satırı ekleyin.");
            return false;
        }
        for (RecipeLine line : lines) {
            if (line == null) {
                continue;
            }
            if (productCode.equalsIgnoreCase(line.getItemCode())) {
                UretimDates.showError("Reçete", "Geçersiz satır",
                        "Mamul kendi reçetesinde bileşen olamaz.");
                return false;
            }
            IStockItem component = lookup == null ? null : lookup.findByCode(line.getItemCode());
            if (!UretimDocs.isComponent(component)) {
                UretimDates.showError("Reçete", "Geçersiz bileşen",
                        "Bileşen hammadde, yarı mamul veya sarf olmalıdır.");
                return false;
            }
        }
        Date now = new Date();
        Recipe row = this.currentRow;
        if (row == null) {
            row = recipeDao.getEmpty();
            if (row == null) {
                UretimDates.showError("Reçete", "Kayıt hatası", "Reçete oluşturulamadı.");
                return false;
            }
            row.setAddedDate(now);
            row.setStatus(Recipe.STATUS_ACTIVE);
        }
        if (!CompanyScope.stamp(row)) {
            UretimDates.showError("Reçete", "Firma",
                    "Önce araç çubuğundan çalışma firmasını seçin.");
            return false;
        }
        row.setProductCode(productCode);
        row.setProductName(product == null ? UretimCombos.nameOf(cmbProduct) : product.getItemDesc());
        row.setProductUnit(product == null ? UretimDates.trimToNull(txtProductUnit.getText())
                : product.getItemUnit());
        row.setYieldQuantity(yield);
        row.setNotes(UretimDates.trimToNull(txtNotes.getText()));
        row.setStatus(STATUS_INACTIVE.equals(cmbStatus.getValue())
                ? Recipe.STATUS_INACTIVE : Recipe.STATUS_ACTIVE);
        row.setLastUpdateDate(now);
        if (row.getAddedDate() == null) {
            row.setAddedDate(now);
        }
        if (CompanyScope.rejectDuplicate(
                CompanyScope.findDuplicate(recipeDao, "Recipe", "productCode",
                        row.getProductCode(), row.getRowId()),
                "Bu mamul için reçete zaten var")) {
            return false;
        }
        Recipe saved = recipeDao.addOrUpdate(row);
        if (saved == null || saved.getRowId() == null) {
            UretimDates.showError("Reçete", "Kayıt hatası", "Reçete kaydedilemedi.");
            return false;
        }
        this.currentRow = saved;
        replaceLines(saved.getRowId(), now);
        showAudit();
        return true;
    }

    private void replaceLines(Long recipeId, Date now) {
        UretimDocs.deleteByParent(lineDao, "RecipeLine", "recipeId", recipeId);
        int no = 1;
        List<RecipeLine> stored = new ArrayList<>();
        for (RecipeLine line : lines) {
            RecipeLine copy = lineDao.getEmpty();
            if (copy == null) {
                continue;
            }
            copy.setAddedDate(now);
            copy.setLastUpdateDate(now);
            copy.setRecipeId(recipeId);
            copy.setLineNo(no++);
            copy.setItemCode(line.getItemCode());
            copy.setItemDesc(line.getItemDesc());
            copy.setItemUnit(line.getItemUnit());
            copy.setQuantity(line.getQuantity());
            RecipeLine saved = lineDao.addOrUpdate(copy);
            stored.add(saved == null ? copy : saved);
        }
        lines.setAll(stored);
    }

    private void fillLookups() {
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        List<IStockItem> rows = items == null ? List.of() : items.findActive();
        UretimCombos.fillProducts(cmbProduct, rows);
        UretimCombos.fillComponents(cmbItem, rows);
    }

    private void applyProduct(String itemCode) {
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (item != null) {
            txtProductUnit.setText(UretimDates.empty(item.getItemUnit()));
        }
    }

    private void applyItem(String itemCode) {
        IItemLookup items = TerpApplication.getInstance().getItemLookup();
        IStockItem item = items == null ? null : items.findByCode(itemCode);
        if (item != null && (txtUnit.getText() == null || txtUnit.getText().isBlank())) {
            txtUnit.setText(UretimDates.empty(item.getItemUnit()));
        }
    }

    private void close() {
        UretimEmbed.closeIfModal(btnClose);
    }
}

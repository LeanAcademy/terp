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

import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.IAccountLookup;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.model.IAccount;
import com.terp.stok.data.Item;
import com.terp.stok.data.StockBalances;
import com.terp.stok.data.StockMovement;
import com.terp.stok.data.Warehouse;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

public class MovementEditFormController implements Initializable {

    @FXML
    private Button btnSubmit;
    @FXML
    private Button btnCancel;
    @FXML
    private DatePicker dtMovementDate;
    @FXML
    private ComboBox<String> cmbMovementType;
    @FXML
    private ComboBox<String> cmbWarehouse;
    @FXML
    private ComboBox<String> cmbTargetWarehouse;
    @FXML
    private ComboBox<String> cmbItem;
    @FXML
    private TextField txtQuantity;
    @FXML
    private TextField txtItemUnit;
    @FXML
    private TextField txtUnitPrice;
    @FXML
    private ComboBox<String> cmbAccount;
    @FXML
    private TextField txtDocumentNo;
    @FXML
    private TextField txtLotNo;
    @FXML
    private TextField txtSerialNo;
    @FXML
    private TextArea txtNotes;

    private ICommonDao<StockMovement> movementDao;
    private ICommonDao<Warehouse> warehouseDao;
    private ICommonDao<Item> itemDao;
    private StockMovement currentRow;
    private ValidationSupport validationSupport;
    private List<Warehouse> warehouses = List.of();
    private List<Item> items = List.of();
    private boolean parseFailed;

    public void initializeForm(StockMovement row) {
        this.currentRow = row;
        fillLookups();
        if (row == null) {
            dtMovementDate.setValue(LocalDate.now());
            cmbMovementType.getSelectionModel().select(StockMovement.TYPE_IN);
            updateTargetEnabled();
            return;
        }
        dtMovementDate.setValue(toLocalDate(row.getMovementDate()));
        cmbMovementType.getSelectionModel().select(row.getMovementType());
        StockCombos.select(cmbWarehouse, row.getWarehouseCode());
        StockCombos.select(cmbTargetWarehouse, row.getTargetWarehouseCode());
        StockCombos.select(cmbItem, row.getItemCode());
        txtQuantity.setText(row.getQuantity() == null ? "" : row.getQuantity().toString());
        txtItemUnit.setText(empty(row.getItemUnit()));
        txtUnitPrice.setText(row.getUnitPrice() == null ? "" : row.getUnitPrice().toString());
        StockCombos.select(cmbAccount, row.getAccountCode());
        txtDocumentNo.setText(empty(row.getDocumentNo()));
        txtLotNo.setText(empty(row.getLotNo()));
        txtSerialNo.setText(empty(row.getSerialNo()));
        txtNotes.setText(empty(row.getNotes()));
        updateTargetEnabled();
    }

    @FXML
    private void onActionBtnSubmit(ActionEvent event) {
        if (validationSupport.isInvalid()) {
            showError("Zorunlu alanlar eksik", "Depo, malzeme ve miktar zorunludur.");
            return;
        }
        String warehouseCode = StockCombos.codeOf(cmbWarehouse);
        String itemCode = StockCombos.codeOf(cmbItem);
        Double quantity = parseDouble(txtQuantity.getText(), "Miktar");
        if (parseFailed) {
            return;
        }
        Double unitPrice = parseDouble(txtUnitPrice.getText(), "Birim fiyat");
        if (parseFailed) {
            return;
        }
        if (warehouseCode == null || itemCode == null) {
            showError("Zorunlu alanlar eksik", "Depo ve malzeme seçin.");
            return;
        }
        if (quantity == null || quantity <= 0d) {
            showError("Miktar hatası", "Miktar sıfırdan büyük olmalıdır.");
            return;
        }
        int typeIndex = cmbMovementType.getSelectionModel().getSelectedIndex();
        if (typeIndex < 0) {
            typeIndex = StockMovement.TYPE_IN;
        }
        String targetCode = StockCombos.codeOf(cmbTargetWarehouse);
        if (typeIndex == StockMovement.TYPE_TRANSFER) {
            if (targetCode == null) {
                showError("Transfer", "Hedef depo seçin.");
                return;
            }
            if (targetCode.equals(warehouseCode)) {
                showError("Transfer", "Kaynak ve hedef depo aynı olamaz.");
                return;
            }
        } else {
            targetCode = null;
        }
        if (!allowOutbound(typeIndex, warehouseCode, itemCode, quantity)) {
            return;
        }
        StockMovement row = this.currentRow;
        Date now = new Date();
        if (row == null) {
            row = movementDao.getEmpty();
            if (row == null) {
                showError("Kayıt hatası", "Stok işlemi oluşturulamadı.");
                return;
            }
            row.setAddedDate(now);
        }
        row.setMovementDate(toDate(dtMovementDate.getValue()));
        row.setMovementType(typeIndex);
        row.setWarehouseCode(warehouseCode);
        row.setWarehouseName(nameOfWarehouse(warehouseCode));
        row.setTargetWarehouseCode(targetCode);
        row.setTargetWarehouseName(targetCode == null ? null : nameOfWarehouse(targetCode));
        row.setItemCode(itemCode);
        Item item = findItem(itemCode);
        row.setItemDesc(item == null ? StockCombos.nameOf(cmbItem) : item.getItemDesc());
        String unit = trimToNull(txtItemUnit.getText());
        row.setItemUnit(unit == null && item != null ? item.getItemUnit() : unit);
        row.setQuantity(quantity);
        row.setUnitPrice(unitPrice);
        row.setAccountCode(StockCombos.codeOf(cmbAccount));
        row.setAccountName(StockCombos.nameOf(cmbAccount));
        row.setDocumentNo(trimToNull(txtDocumentNo.getText()));
        row.setLotNo(trimToNull(txtLotNo.getText()));
        row.setSerialNo(trimToNull(txtSerialNo.getText()));
        row.setNotes(trimToNull(txtNotes.getText()));
        row.setLastUpdateDate(now);
        movementDao.addOrUpdate(row);
        close();
    }

    @FXML
    private void onActionBtnCancel(ActionEvent event) {
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.movementDao = TerpApplication.getInstance().getPersistence().createDao(StockMovement.class);
        this.warehouseDao = TerpApplication.getInstance().getPersistence().createDao(Warehouse.class);
        this.itemDao = TerpApplication.getInstance().getPersistence().createDao(Item.class);
        cmbMovementType.getItems().addAll(StockMovement.TYPE_LABELS);
        cmbMovementType.getSelectionModel().select(StockMovement.TYPE_IN);
        cmbMovementType.valueProperty().addListener((obs, old, value) -> {
            updateTargetEnabled();
            fillAccounts();
        });
        cmbItem.valueProperty().addListener((obs, old, value) -> applyItem(StockCombos.codeOf(cmbItem)));
        this.validationSupport = new ValidationSupport();
        this.validationSupport.registerValidator(txtQuantity, true,
                Validator.createEmptyValidator("Miktar zorunlu"));
    }

    private void fillLookups() {
        List<Warehouse> warehouseRows = warehouseDao.findAll();
        this.warehouses = warehouseRows == null ? List.of() : warehouseRows;
        List<String> warehouseLabels = new ArrayList<>();
        for (Warehouse warehouse : warehouses) {
            if (warehouse.getStatus() == 0) {
                warehouseLabels.add(warehouse.getDisplayLabel());
            }
        }
        StockCombos.fill(cmbWarehouse, warehouseLabels);
        StockCombos.fill(cmbTargetWarehouse, warehouseLabels);

        List<Item> itemRows = itemDao.findAll();
        this.items = itemRows == null ? List.of() : itemRows;
        List<String> itemLabels = new ArrayList<>();
        for (Item item : items) {
            if (item.getStatus() == 0 && item.getItemType() != Item.TYPE_SERVICE) {
                itemLabels.add((item.getItemId() == null ? "" : item.getItemId())
                        + " — " + (item.getItemDesc() == null ? "" : item.getItemDesc()));
            }
        }
        StockCombos.fill(cmbItem, itemLabels);
        fillAccounts();
    }

    private void fillAccounts() {
        List<String> accountLabels = new ArrayList<>();
        IAccountLookup lookup = TerpApplication.getInstance().getAccountLookup();
        if (lookup != null) {
            int type = cmbMovementType.getSelectionModel().getSelectedIndex();
            if (type == StockMovement.TYPE_IN || type == StockMovement.TYPE_COUNT_IN) {
                addAccounts(accountLabels, lookup.findActiveSuppliers());
            } else if (type == StockMovement.TYPE_OUT || type == StockMovement.TYPE_SCRAP) {
                addAccounts(accountLabels, lookup.findActiveCustomers());
            } else {
                addAccounts(accountLabels, lookup.findActiveSuppliers());
                addAccounts(accountLabels, lookup.findActiveCustomers());
            }
        }
        String current = StockCombos.codeOf(cmbAccount);
        StockCombos.fill(cmbAccount, accountLabels);
        StockCombos.select(cmbAccount, current);
    }

    private void updateTargetEnabled() {
        boolean transfer = cmbMovementType.getSelectionModel().getSelectedIndex()
                == StockMovement.TYPE_TRANSFER;
        cmbTargetWarehouse.setDisable(!transfer);
        if (!transfer) {
            cmbTargetWarehouse.getSelectionModel().clearSelection();
            if (cmbTargetWarehouse.getEditor() != null) {
                cmbTargetWarehouse.getEditor().clear();
            }
        }
    }

    private void applyItem(String itemCode) {
        Item item = findItem(itemCode);
        if (item != null && (txtItemUnit.getText() == null || txtItemUnit.getText().isBlank())) {
            txtItemUnit.setText(empty(item.getItemUnit()));
        }
    }

    private boolean allowOutbound(int type, String warehouseCode, String itemCode, double quantity) {
        boolean outbound = type == StockMovement.TYPE_OUT
                || type == StockMovement.TYPE_TRANSFER
                || type == StockMovement.TYPE_COUNT_OUT
                || type == StockMovement.TYPE_SCRAP;
        if (!outbound) {
            return true;
        }
        Warehouse warehouse = findWarehouse(warehouseCode);
        if (warehouse != null && warehouse.isNegativeAllowed()) {
            return true;
        }
        List<StockMovement> all = movementDao.findAll();
        Long exclude = currentRow == null ? null : currentRow.getRowId();
        double onHand = StockBalances.onHand(all, warehouseCode, itemCode, exclude);
        if (onHand - quantity < -0.000001) {
            showError("Yetersiz stok",
                    "Depodaki miktar " + formatQty(onHand) + ". Negatif stoka izin verilmiyor.");
            return false;
        }
        return true;
    }

    private Item findItem(String code) {
        if (code == null) {
            return null;
        }
        for (Item item : items) {
            if (code.equals(item.getItemId())) {
                return item;
            }
        }
        return null;
    }

    private Warehouse findWarehouse(String code) {
        if (code == null) {
            return null;
        }
        for (Warehouse warehouse : warehouses) {
            if (code.equals(warehouse.getWarehouseCode())) {
                return warehouse;
            }
        }
        return null;
    }

    private String nameOfWarehouse(String code) {
        Warehouse warehouse = findWarehouse(code);
        return warehouse == null ? null : warehouse.getWarehouseName();
    }

    private Double parseDouble(String text, String field) {
        parseFailed = false;
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(text.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            parseFailed = true;
            showError("Sayı hatası", field + " için geçerli bir sayı girin.");
            return null;
        }
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Form hatası");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();
    }

    private void close() {
        Stage stage = (Stage) btnSubmit.getScene().getWindow();
        stage.close();
    }

    private static void addAccounts(List<String> labels, List<IAccount> accounts) {
        if (accounts == null) {
            return;
        }
        for (IAccount account : accounts) {
            if (account != null && account.getDisplayLabel() != null
                    && !labels.contains(account.getDisplayLabel())) {
                labels.add(account.getDisplayLabel());
            }
        }
    }

    private static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return LocalDate.now();
        }
        if (date instanceof java.sql.Date sql) {
            return sql.toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date toDate(LocalDate local) {
        if (local == null) {
            return new Date();
        }
        return Date.from(local.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static String formatQty(double value) {
        if (value == (long) value) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }

    private static String empty(String value) {
        return value == null ? "" : value;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

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
import com.terp.plugin.data.IAccountLookup;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.data.model.IAccount;
import com.terp.plugin.gui.RecordAuditBar;
import com.terp.stok.data.Item;
import com.terp.stok.data.ItemPartnerCode;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

public class ItemEditFormController implements Initializable {

    @FXML
    private Button btnSubmit;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField txtItemId;
    @FXML
    private TextField txtItemDesc;
    @FXML
    private CheckBox chkActive;
    @FXML
    private TextField txtLongDesc;
    @FXML
    private ComboBox<String> cmbItemType;
    @FXML
    private TextField txtItemGroup;
    @FXML
    private TextField txtItemClass;
    @FXML
    private TextField txtBrand;
    @FXML
    private TextField txtBarcode;
    @FXML
    private TextField txtGtin;
    @FXML
    private TextField txtOriginCountry;
    @FXML
    private TextArea txtNotes;
    @FXML
    private ComboBox<String> cmbSupplierAccount;
    @FXML
    private TextField txtSupplierItemCode;
    @FXML
    private TextField txtSupplierItemName;
    @FXML
    private TableView<ItemPartnerCode> tblSupplierCodes;
    @FXML
    private TableColumn<ItemPartnerCode, String> tcSupplierAccountCode;
    @FXML
    private TableColumn<ItemPartnerCode, String> tcSupplierAccountName;
    @FXML
    private TableColumn<ItemPartnerCode, String> tcSupplierItemCode;
    @FXML
    private TableColumn<ItemPartnerCode, String> tcSupplierItemName;
    @FXML
    private ComboBox<String> cmbCustomerAccount;
    @FXML
    private TextField txtCustomerItemCode;
    @FXML
    private TextField txtCustomerItemName;
    @FXML
    private TableView<ItemPartnerCode> tblCustomerCodes;
    @FXML
    private TableColumn<ItemPartnerCode, String> tcCustomerAccountCode;
    @FXML
    private TableColumn<ItemPartnerCode, String> tcCustomerAccountName;
    @FXML
    private TableColumn<ItemPartnerCode, String> tcCustomerItemCode;
    @FXML
    private TableColumn<ItemPartnerCode, String> tcCustomerItemName;
    @FXML
    private TextField txtItemUnit;
    @FXML
    private TextField txtSecondUnit;
    @FXML
    private TextField txtUnitFactor;
    @FXML
    private TextField txtWeight;
    @FXML
    private TextField txtVolume;
    @FXML
    private TextField txtWidth;
    @FXML
    private TextField txtLength;
    @FXML
    private TextField txtHeight;
    @FXML
    private TextField txtMinStock;
    @FXML
    private TextField txtMaxStock;
    @FXML
    private TextField txtSafetyStock;
    @FXML
    private TextField txtShelfLifeDays;
    @FXML
    private CheckBox chkLotTracked;
    @FXML
    private CheckBox chkSerialTracked;
    @FXML
    private TextField txtVatRate;
    @FXML
    private TextField txtCurrency;
    @FXML
    private TextField txtPurchasePrice;
    @FXML
    private TextField txtSalesPrice;
    @FXML
    private TextField txtStockAccount;
    @FXML
    private TextField txtPurchaseAccount;
    @FXML
    private TextField txtSalesAccount;

    private ICommonDao<Item> itemDao;
    private ICommonDao<ItemPartnerCode> partnerCodeDao;
    private Item currentRow;
    private ValidationSupport validationSupport;
    private final ObservableList<ItemPartnerCode> supplierCodes = FXCollections.observableArrayList();
    private final ObservableList<ItemPartnerCode> customerCodes = FXCollections.observableArrayList();
    private final Set<Long> loadedPartnerIds = new HashSet<>();
    private boolean hadParseError;
    private RecordAuditBar auditBar;

    public void initializeForm(Item row) {
        this.currentRow = row;
        supplierCodes.clear();
        customerCodes.clear();
        loadedPartnerIds.clear();
        if (row == null) {
            cmbItemType.getSelectionModel().select(Item.TYPE_TRADE);
            chkActive.setSelected(true);
            txtCurrency.setText("TRY");
            showAudit();
            return;
        }
        txtItemId.setText(empty(row.getItemId()));
        txtItemDesc.setText(empty(row.getItemDesc()));
        chkActive.setSelected(row.getStatus() == 0);
        txtLongDesc.setText(empty(row.getLongDesc()));
        cmbItemType.getSelectionModel().select(row.getItemType());
        txtItemGroup.setText(empty(row.getItemGroup()));
        txtItemClass.setText(empty(row.getItemClass()));
        txtBrand.setText(empty(row.getBrand()));
        txtBarcode.setText(empty(row.getBarcode()));
        txtGtin.setText(empty(row.getGtin()));
        txtOriginCountry.setText(empty(row.getOriginCountry()));
        txtNotes.setText(empty(row.getNotes()));
        txtItemUnit.setText(empty(row.getItemUnit()));
        txtSecondUnit.setText(empty(row.getSecondUnit()));
        txtUnitFactor.setText(num(row.getUnitFactor()));
        txtWeight.setText(num(row.getWeight()));
        txtVolume.setText(num(row.getVolume()));
        txtWidth.setText(num(row.getWidth()));
        txtLength.setText(num(row.getLength()));
        txtHeight.setText(num(row.getHeight()));
        txtMinStock.setText(num(row.getMinStock()));
        txtMaxStock.setText(num(row.getMaxStock()));
        txtSafetyStock.setText(num(row.getSafetyStock()));
        txtShelfLifeDays.setText(row.getShelfLifeDays() == null ? "" : Integer.toString(row.getShelfLifeDays()));
        chkLotTracked.setSelected(row.getLotTracked() != 0);
        chkSerialTracked.setSelected(row.getSerialTracked() != 0);
        txtVatRate.setText(num(row.getVatRate()));
        txtCurrency.setText(empty(row.getCurrency()));
        txtPurchasePrice.setText(num(row.getPurchasePrice()));
        txtSalesPrice.setText(num(row.getSalesPrice()));
        txtStockAccount.setText(empty(row.getStockAccount()));
        txtPurchaseAccount.setText(empty(row.getPurchaseAccount()));
        txtSalesAccount.setText(empty(row.getSalesAccount()));
        if (row.getRowId() != null) {
            List<ItemPartnerCode> rows = partnerCodeDao.findAll(
                    "from ItemPartnerCode e where e.itemRowId = " + row.getRowId());
            if (rows != null) {
                for (ItemPartnerCode code : rows) {
                    if (code.getRowId() != null) {
                        loadedPartnerIds.add(code.getRowId());
                    }
                    if (code.getRole() == ItemPartnerCode.ROLE_CUSTOMER) {
                        customerCodes.add(code);
                    } else {
                        supplierCodes.add(code);
                    }
                }
            }
        }
        showAudit();
    }

    @FXML
    private void onActionAddSupplierCode(ActionEvent event) {
        addPartnerRow(cmbSupplierAccount, txtSupplierItemCode, txtSupplierItemName,
                supplierCodes, ItemPartnerCode.ROLE_SUPPLIER);
    }

    @FXML
    private void onActionRemoveSupplierCode(ActionEvent event) {
        ItemPartnerCode selected = tblSupplierCodes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            supplierCodes.remove(selected);
        }
    }

    @FXML
    private void onActionAddCustomerCode(ActionEvent event) {
        addPartnerRow(cmbCustomerAccount, txtCustomerItemCode, txtCustomerItemName,
                customerCodes, ItemPartnerCode.ROLE_CUSTOMER);
    }

    @FXML
    private void onActionRemoveCustomerCode(ActionEvent event) {
        ItemPartnerCode selected = tblCustomerCodes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            customerCodes.remove(selected);
        }
    }

    @FXML
    private void onActionBtnSubmit(ActionEvent event) {
        if (validationSupport.isInvalid()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Form hatası");
            alert.setHeaderText("Zorunlu alanlar eksik");
            alert.setContentText("Kod, tanım ve ana birim zorunludur.");
            alert.show();
            return;
        }
        hadParseError = false;
        Double unitFactor = parseDouble(txtUnitFactor.getText(), "Çevrim katsayısı");
        Double weight = parseDouble(txtWeight.getText(), "Ağırlık");
        Double volume = parseDouble(txtVolume.getText(), "Hacim");
        Double width = parseDouble(txtWidth.getText(), "En");
        Double length = parseDouble(txtLength.getText(), "Boy");
        Double height = parseDouble(txtHeight.getText(), "Yükseklik");
        Double minStock = parseDouble(txtMinStock.getText(), "Min. stok");
        Double maxStock = parseDouble(txtMaxStock.getText(), "Max. stok");
        Double safety = parseDouble(txtSafetyStock.getText(), "Emniyet stoğu");
        Integer shelf = parseInt(txtShelfLifeDays.getText(), "Raf ömrü");
        Double vat = parseDouble(txtVatRate.getText(), "KDV");
        Double purchase = parseDouble(txtPurchasePrice.getText(), "Alış fiyatı");
        Double sales = parseDouble(txtSalesPrice.getText(), "Satış fiyatı");
        if (hadParseError) {
            return;
        }

        Item row = this.currentRow;
        Date now = new Date();
        if (row == null) {
            row = itemDao.getEmpty();
            if (row == null) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Kayıt hatası");
                alert.setHeaderText("Malzeme oluşturulamadı");
                alert.show();
                return;
            }
            row.setAddedDate(now);
        }
        if (!CompanyScope.stamp(row)) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Firma");
            alert.setHeaderText("Firma seçilmedi");
            alert.setContentText("Önce araç çubuğundan çalışma firmasını seçin.");
            alert.show();
            return;
        }
        row.setItemId(txtItemId.getText().trim());
        row.setItemDesc(txtItemDesc.getText().trim());
        row.setStatus(chkActive.isSelected() ? 0 : 1);
        row.setLongDesc(trimToNull(txtLongDesc.getText()));
        int typeIndex = cmbItemType.getSelectionModel().getSelectedIndex();
        row.setItemType(typeIndex < 0 ? Item.TYPE_TRADE : typeIndex);
        row.setItemGroup(trimToNull(txtItemGroup.getText()));
        row.setItemClass(trimToNull(txtItemClass.getText()));
        row.setBrand(trimToNull(txtBrand.getText()));
        row.setBarcode(trimToNull(txtBarcode.getText()));
        row.setGtin(trimToNull(txtGtin.getText()));
        row.setOriginCountry(trimToNull(txtOriginCountry.getText()));
        row.setNotes(trimToNull(txtNotes.getText()));
        row.setItemUnit(txtItemUnit.getText().trim());
        row.setSecondUnit(trimToNull(txtSecondUnit.getText()));
        row.setUnitFactor(unitFactor);
        row.setWeight(weight);
        row.setVolume(volume);
        row.setWidth(width);
        row.setLength(length);
        row.setHeight(height);
        row.setMinStock(minStock);
        row.setMaxStock(maxStock);
        row.setSafetyStock(safety);
        row.setShelfLifeDays(shelf);
        row.setLotTracked(chkLotTracked.isSelected() ? 1 : 0);
        row.setSerialTracked(chkSerialTracked.isSelected() ? 1 : 0);
        row.setVatRate(vat);
        String currency = trimToNull(txtCurrency.getText());
        row.setCurrency(currency == null ? "TRY" : currency);
        row.setPurchasePrice(purchase);
        row.setSalesPrice(sales);
        row.setStockAccount(trimToNull(txtStockAccount.getText()));
        row.setPurchaseAccount(trimToNull(txtPurchaseAccount.getText()));
        row.setSalesAccount(trimToNull(txtSalesAccount.getText()));
        row.setLastUpdateDate(now);
        if (CompanyScope.rejectDuplicate(
                CompanyScope.findDuplicate(itemDao, "Item", "itemId", row.getItemId(), row.getRowId()),
                "Malzeme kodu tekrar ediyor")) {
            return;
        }
        Item saved = itemDao.addOrUpdate(row);
        if (saved == null || saved.getRowId() == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Kayıt hatası");
            alert.setHeaderText("Malzeme kaydedilemedi");
            alert.show();
            return;
        }
        persistPartnerCodes(saved.getRowId(), now);
        close();
    }

    @FXML
    private void onActionBtnCancel(ActionEvent event) {
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.itemDao = TerpApplication.getInstance().getPersistence().createDao(Item.class);
        this.partnerCodeDao = TerpApplication.getInstance().getPersistence().createDao(ItemPartnerCode.class);
        cmbItemType.getItems().addAll(Item.TYPE_LABELS);
        cmbItemType.getSelectionModel().select(Item.TYPE_TRADE);
        bindPartnerTable(tblSupplierCodes, tcSupplierAccountCode, tcSupplierAccountName,
                tcSupplierItemCode, tcSupplierItemName, supplierCodes);
        bindPartnerTable(tblCustomerCodes, tcCustomerAccountCode, tcCustomerAccountName,
                tcCustomerItemCode, tcCustomerItemName, customerCodes);
        fillAccountCombo(cmbSupplierAccount, true);
        fillAccountCombo(cmbCustomerAccount, false);
        this.validationSupport = new ValidationSupport();
        this.validationSupport.registerValidator(txtItemId, true,
                Validator.createEmptyValidator("Kod zorunlu"));
        this.validationSupport.registerValidator(txtItemDesc, true,
                Validator.createEmptyValidator("Tanım zorunlu"));
        this.validationSupport.registerValidator(txtItemUnit, true,
                Validator.createEmptyValidator("Ana birim zorunlu"));
        this.auditBar = RecordAuditBar.install(txtItemId);
        showAudit();
    }

    private void showAudit() {
        if (auditBar != null) {
            auditBar.bind(currentRow);
        }
    }

    private void persistPartnerCodes(Long itemRowId, Date now) {
        Set<Long> keep = new HashSet<>();
        for (ItemPartnerCode code : supplierCodes) {
            keep.add(savePartner(code, itemRowId, ItemPartnerCode.ROLE_SUPPLIER, now));
        }
        for (ItemPartnerCode code : customerCodes) {
            keep.add(savePartner(code, itemRowId, ItemPartnerCode.ROLE_CUSTOMER, now));
        }
        for (Long oldId : loadedPartnerIds) {
            if (oldId != null && !keep.contains(oldId)) {
                partnerCodeDao.delete(oldId);
            }
        }
    }

    private Long savePartner(ItemPartnerCode code, Long itemRowId, int role, Date now) {
        code.setItemRowId(itemRowId);
        code.setRole(role);
        code.setLastUpdateDate(now);
        if (code.getAddedDate() == null) {
            code.setAddedDate(now);
        }
        ItemPartnerCode saved = partnerCodeDao.addOrUpdate(code);
        return saved == null ? null : saved.getRowId();
    }

    private void addPartnerRow(ComboBox<String> accountCombo, TextField codeField,
            TextField nameField, ObservableList<ItemPartnerCode> target, int role) {
        String accountCode = comboCode(accountCombo);
        String partnerCode = trimToNull(codeField.getText());
        String partnerName = trimToNull(nameField.getText());
        if (accountCode == null || partnerCode == null || partnerName == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Form hatası");
            alert.setHeaderText("Eksik satır");
            alert.setContentText("Cari, stok kodu ve stok adı zorunludur.");
            alert.show();
            return;
        }
        for (ItemPartnerCode existing : target) {
            if (accountCode.equals(existing.getAccountCode())
                    && partnerCode.equals(existing.getPartnerItemCode())) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Form hatası");
                alert.setHeaderText("Mükerrer satır");
                alert.setContentText("Bu cari ve stok kodu zaten listede.");
                alert.show();
                return;
            }
        }
        ItemPartnerCode row = new ItemPartnerCode();
        row.setRole(role);
        row.setAccountCode(accountCode);
        row.setAccountName(accountNameOf(accountCombo, accountCode));
        row.setPartnerItemCode(partnerCode);
        row.setPartnerItemName(partnerName);
        target.add(row);
        accountCombo.getSelectionModel().clearSelection();
        if (accountCombo.getEditor() != null) {
            accountCombo.getEditor().clear();
        }
        codeField.clear();
        nameField.clear();
    }

    private static void bindPartnerTable(TableView<ItemPartnerCode> table,
            TableColumn<ItemPartnerCode, String> accountCode,
            TableColumn<ItemPartnerCode, String> accountName,
            TableColumn<ItemPartnerCode, String> itemCode,
            TableColumn<ItemPartnerCode, String> itemName,
            ObservableList<ItemPartnerCode> items) {
        accountCode.setCellValueFactory(new PropertyValueFactory<>("accountCode"));
        accountName.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        itemCode.setCellValueFactory(new PropertyValueFactory<>("partnerItemCode"));
        itemName.setCellValueFactory(new PropertyValueFactory<>("partnerItemName"));
        table.setItems(items);
    }

    private void fillAccountCombo(ComboBox<String> combo, boolean suppliers) {
        combo.getItems().clear();
        IAccountLookup lookup = TerpApplication.getInstance().getAccountLookup();
        if (lookup == null) {
            return;
        }
        List<IAccount> accounts = suppliers ? lookup.findActiveSuppliers() : lookup.findActiveCustomers();
        if (accounts == null) {
            return;
        }
        for (IAccount account : accounts) {
            if (account != null && account.getAccountCode() != null) {
                combo.getItems().add(account.getDisplayLabel());
            }
        }
    }

    private Double parseDouble(String text, String field) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(text.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            hadParseError = true;
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Sayı hatası");
            alert.setHeaderText(field);
            alert.setContentText("Geçerli bir sayı girin.");
            alert.show();
            return null;
        }
    }

    private Integer parseInt(String text, String field) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(text.trim());
        } catch (NumberFormatException ex) {
            hadParseError = true;
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Sayı hatası");
            alert.setHeaderText(field);
            alert.setContentText("Geçerli bir tam sayı girin.");
            alert.show();
            return null;
        }
    }

    private void close() {
        Stage stage = (Stage) btnSubmit.getScene().getWindow();
        stage.close();
    }

    private static String empty(String value) {
        return value == null ? "" : value;
    }

    private static String num(Double value) {
        return value == null ? "" : value.toString();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String comboCode(ComboBox<String> combo) {
        String value = combo.getValue();
        if (value == null || value.isBlank()) {
            value = combo.getEditor() == null ? null : combo.getEditor().getText();
        }
        if (value == null || value.isBlank()) {
            return null;
        }
        int sep = value.indexOf(" — ");
        String code = sep > 0 ? value.substring(0, sep) : value;
        return trimToNull(code);
    }

    private static String accountNameOf(ComboBox<String> combo, String accountCode) {
        String value = combo.getValue();
        if (value == null || value.isBlank()) {
            value = combo.getEditor() == null ? null : combo.getEditor().getText();
        }
        if (value != null) {
            int sep = value.indexOf(" — ");
            if (sep > 0) {
                return trimToNull(value.substring(sep + 3));
            }
        }
        IAccountLookup lookup = TerpApplication.getInstance().getAccountLookup();
        if (lookup != null) {
            IAccount account = lookup.findByCode(accountCode);
            if (account != null) {
                return account.getAccountName();
            }
        }
        return null;
    }
}

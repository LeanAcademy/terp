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
package com.terp.cari.gui;

import com.terp.cari.data.AccountBalanceRow;
import com.terp.cari.data.AccountBalances;
import com.terp.cari.data.AccountMovement;
import com.terp.cari.data.Cari;
import com.terp.plugin.CompanyScope;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.ICommonDao;
import com.terp.plugin.gui.IIconFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import com.terp.plugin.gui.NumericCells;

public class AccountStatusFormController implements Initializable {

    @FXML
    private Button btnRefresh;
    @FXML
    private TextField txtAccountCode;
    @FXML
    private CheckBox chkOnlyNonZero;
    @FXML
    private TableView<AccountBalanceRow> tblvStatusView;
    @FXML
    private TableColumn<AccountBalanceRow, String> tcAccountCode;
    @FXML
    private TableColumn<AccountBalanceRow, String> tcAccountName;
    @FXML
    private TableColumn<AccountBalanceRow, String> tcRoleLabel;
    @FXML
    private TableColumn<AccountBalanceRow, Double> tcDebitTotal;
    @FXML
    private TableColumn<AccountBalanceRow, Double> tcCreditTotal;
    @FXML
    private TableColumn<AccountBalanceRow, Double> tcBalance;
    @FXML
    private TableColumn<AccountBalanceRow, String> tcBalanceLabel;

    private ICommonDao<Cari> accountDao;
    private ICommonDao<AccountMovement> movementDao;

    @FXML
    public void onActionBtnRefresh(ActionEvent event) {
        refreshView();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.accountDao = TerpApplication.getInstance().getPersistence().createDao(Cari.class);
        this.movementDao = TerpApplication.getInstance().getPersistence().createDao(AccountMovement.class);
        IIconFactory icons = TerpApplication.getInstance().getIconFactory();
        if (icons != null) {
            try {
                this.btnRefresh.setGraphic(icons.getIcon("REFRESH"));
            } catch (RuntimeException ignored) {
                // keep text-only if the glyph name is unknown
            }
        }
        this.tcAccountCode.setCellValueFactory(new PropertyValueFactory<>("accountCode"));
        this.tcAccountName.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        this.tcRoleLabel.setCellValueFactory(new PropertyValueFactory<>("roleLabel"));
        this.tcDebitTotal.setCellValueFactory(new PropertyValueFactory<>("debitTotal"));
        NumericCells.apply(this.tcDebitTotal);
        this.tcCreditTotal.setCellValueFactory(new PropertyValueFactory<>("creditTotal"));
        NumericCells.apply(this.tcCreditTotal);
        this.tcBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        NumericCells.apply(this.tcBalance);
        this.tcBalanceLabel.setCellValueFactory(new PropertyValueFactory<>("balanceLabel"));
        refreshView();
    }

    private void refreshView() {
        List<Cari> accounts = accountDao.findAll(CompanyScope.from("Cari"));
        List<AccountMovement> movements = movementDao.findAll(CompanyScope.from("AccountMovement"));
        boolean onlyNonZero = chkOnlyNonZero != null && chkOnlyNonZero.isSelected();
        List<AccountBalanceRow> rows = AccountBalances.report(
                accounts == null ? List.of() : accounts,
                movements,
                onlyNonZero);
        String filter = txtAccountCode == null || txtAccountCode.getText() == null
                ? "" : txtAccountCode.getText().trim().toLowerCase();
        List<AccountBalanceRow> filtered = new ArrayList<>();
        for (AccountBalanceRow row : rows) {
            if (!filter.isEmpty() && !matches(row, filter)) {
                continue;
            }
            filtered.add(row);
        }
        tblvStatusView.setItems(FXCollections.observableArrayList(filtered));
    }

    private static boolean matches(AccountBalanceRow row, String filter) {
        String code = row.getAccountCode() == null ? "" : row.getAccountCode().toLowerCase();
        String name = row.getAccountName() == null ? "" : row.getAccountName().toLowerCase();
        return code.contains(filter) || name.contains(filter);
    }
}

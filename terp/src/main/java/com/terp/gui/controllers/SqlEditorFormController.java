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
package com.terp.gui.controllers;

import com.terp.util.HibernateUtil;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.hibernate.Session;

public class SqlEditorFormController implements Initializable {

    private static final int MAX_ROWS = 500;

    @FXML
    private TextArea txtSql;
    @FXML
    private Button btnRun;
    @FXML
    private Button btnClose;
    @FXML
    private Label lblStatus;
    @FXML
    private TableView<ObservableList<String>> tblvResult;

    @FXML
    private void onActionBtnRun(ActionEvent event) {
        String sql = txtSql.getText() == null ? "" : txtSql.getText().trim();
        if (sql.isEmpty()) {
            lblStatus.setText("SQL yazın.");
            return;
        }
        tblvResult.getColumns().clear();
        tblvResult.getItems().clear();
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.doWork(connection -> {
                try (Statement statement = connection.createStatement()) {
                    statement.setMaxRows(MAX_ROWS);
                    boolean hasResult = statement.execute(sql);
                    if (hasResult) {
                        try (ResultSet resultSet = statement.getResultSet()) {
                            fillTable(resultSet);
                        }
                    } else {
                        lblStatus.setText("Etkilenen satır: " + statement.getUpdateCount());
                    }
                }
            });
        } catch (RuntimeException ex) {
            lblStatus.setText("Hata");
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("SQL");
            alert.setHeaderText("Sorgu çalışmadı");
            alert.setContentText(ex.getMessage());
            alert.show();
        } finally {
            session.close();
        }
    }

    @FXML
    private void onActionBtnClose(ActionEvent event) {
        ((Stage) btnClose.getScene().getWindow()).close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tblvResult.setPlaceholder(new Label("Sonuç yok"));
    }

    private void fillTable(ResultSet resultSet) throws java.sql.SQLException {
        ResultSetMetaData meta = resultSet.getMetaData();
        int columns = meta.getColumnCount();
        for (int i = 1; i <= columns; i++) {
            final int index = i - 1;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(meta.getColumnLabel(i));
            column.setCellValueFactory(cell -> {
                ObservableList<String> row = cell.getValue();
                String value = row == null || index >= row.size() ? "" : row.get(index);
                return new SimpleStringProperty(value == null ? "" : value);
            });
            column.setPrefWidth(140);
            tblvResult.getColumns().add(column);
        }
        List<ObservableList<String>> rows = new ArrayList<>();
        int count = 0;
        while (resultSet.next() && count < MAX_ROWS) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int i = 1; i <= columns; i++) {
                Object value = resultSet.getObject(i);
                row.add(value == null ? "" : String.valueOf(value));
            }
            rows.add(row);
            count++;
        }
        tblvResult.setItems(FXCollections.observableArrayList(rows));
        lblStatus.setText(count + " satır");
    }
}

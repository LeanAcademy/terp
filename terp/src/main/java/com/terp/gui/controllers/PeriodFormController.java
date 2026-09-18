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

import com.terp.plugin.TerpApplication;
import com.terp.util.TerpProperties;
import java.net.URL;
import java.time.LocalDate;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;

public class PeriodFormController implements Initializable {

    @FXML
    private Button btnSave;
    @FXML
    private Button btnClose;
    @FXML
    private DatePicker dtFrom;
    @FXML
    private DatePicker dtTo;

    @FXML
    private void onActionBtnSave(ActionEvent event) {
        LocalDate from = dtFrom.getValue();
        LocalDate to = dtTo.getValue();
        if (from == null || to == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Dönem");
            alert.setHeaderText("Tarih eksik");
            alert.setContentText("Başlangıç ve bitiş tarihlerini seçin.");
            alert.show();
            return;
        }
        if (to.isBefore(from)) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Dönem");
            alert.setHeaderText("Tarih aralığı");
            alert.setContentText("Bitiş, başlangıçtan önce olamaz.");
            alert.show();
            return;
        }
        Properties props = TerpProperties.getInstance().getViewProps();
        if (props == null) {
            props = new Properties();
            TerpProperties.getInstance().setViewProps(props);
        }
        props.setProperty("terp.period.from", from.toString());
        props.setProperty("terp.period.to", to.toString());
        TerpProperties.getInstance().saveViewProps();
        applySavedPeriod();
        close();
    }

    @FXML
    private void onActionBtnClose(ActionEvent event) {
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Properties props = TerpProperties.getInstance().getViewProps();
        LocalDate from = parse(props == null ? null : props.getProperty("terp.period.from"));
        LocalDate to = parse(props == null ? null : props.getProperty("terp.period.to"));
        int year = LocalDate.now().getYear();
        dtFrom.setValue(from == null ? LocalDate.of(year, 1, 1) : from);
        dtTo.setValue(to == null ? LocalDate.of(year, 12, 31) : to);
    }

    private void close() {
        ((Stage) btnClose.getScene().getWindow()).close();
    }

    public static void applySavedPeriod() {
        Properties props = TerpProperties.getInstance().getViewProps();
        TerpApplication app = TerpApplication.getInstance();
        app.setPeriodFrom(parse(props == null ? null : props.getProperty("terp.period.from")));
        app.setPeriodTo(parse(props == null ? null : props.getProperty("terp.period.to")));
    }

    private static LocalDate parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (RuntimeException ex) {
            return null;
        }
    }
}

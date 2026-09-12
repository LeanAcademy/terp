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
package com.terp.core.gui;

import com.terp.core.data.DocumentNumbering;
import com.terp.core.data.DocumentSeries;
import com.terp.plugin.CompanyScope;
import com.terp.plugin.FormRights;
import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.IDocumentNumbers;
import com.terp.plugin.gui.IIconFactory;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SeriesFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(SeriesFormController.class.getName());
    private static final String MENU_ID = "SYS07";

    @FXML
    private Button btnEdit;
    @FXML
    private Button btnRefresh;
    @FXML
    private TableView<DocumentSeries> tblvSeriesView;
    @FXML
    private TableColumn<DocumentSeries, String> tcDocumentType;
    @FXML
    private TableColumn<DocumentSeries, String> tcTypeLabel;
    @FXML
    private TableColumn<DocumentSeries, String> tcPrefix;
    @FXML
    private TableColumn<DocumentSeries, Integer> tcPadding;
    @FXML
    private TableColumn<DocumentSeries, Long> tcLastNumber;
    @FXML
    private TableColumn<DocumentSeries, String> tcSampleLabel;

    private FormRights rights = FormRights.forMenu(MENU_ID);

    @FXML
    public void onActionBtnEdit(ActionEvent event) {
        DocumentSeries selected = tblvSeriesView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openEditor(selected);
        }
    }

    @FXML
    public void onActionBtnRefresh(ActionEvent event) {
        refreshView();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        IIconFactory icons = TerpApplication.getInstance().getIconFactory();
        if (icons != null) {
            try {
                this.btnEdit.setGraphic(icons.getIcon("EDIT"));
                this.btnRefresh.setGraphic(icons.getIcon("REFRESH"));
            } catch (RuntimeException ignored) {
                // keep text-only if the glyph name is unknown
            }
        }
        this.tcDocumentType.setCellValueFactory(new PropertyValueFactory<>("documentType"));
        this.tcTypeLabel.setCellValueFactory(new PropertyValueFactory<>("typeLabel"));
        this.tcPrefix.setCellValueFactory(new PropertyValueFactory<>("prefix"));
        this.tcPadding.setCellValueFactory(new PropertyValueFactory<>("padding"));
        this.tcLastNumber.setCellValueFactory(new PropertyValueFactory<>("lastNumber"));
        this.tcSampleLabel.setCellValueFactory(new PropertyValueFactory<>("sampleLabel"));
        this.tblvSeriesView.getSelectionModel().getSelectedItems()
                .addListener(this::selectionChanged);
        this.btnEdit.setDisable(true);
        refreshView();
    }

    private void openEditor(DocumentSeries current) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SeriesEditForm.fxml"));
            loader.setClassLoader(getClass().getClassLoader());
            Node node = loader.load();
            SeriesEditFormController controller = loader.getController();
            controller.initializeForm(current);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(TerpApplication.getInstance().getDesktopManager().getPrimaryStage());
            stage.setScene(new Scene((Parent) node));
            stage.setTitle("Belge serisi");
            stage.showAndWait();
            refreshView();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void refreshView() {
        if (!CompanyScope.hasCompany()) {
            tblvSeriesView.setItems(FXCollections.observableArrayList());
            return;
        }
        DocumentNumbering numbering = numbering();
        List<DocumentSeries> rows = numbering == null ? List.of() : numbering.list();
        tblvSeriesView.setItems(FXCollections.observableArrayList(rows));
    }

    private void selectionChanged(Change<? extends DocumentSeries> change) {
        boolean none = change.getList().size() != 1;
        btnEdit.setDisable(none || !rights.edit);
    }

    private static DocumentNumbering numbering() {
        IDocumentNumbers numbers = TerpApplication.getInstance().getDocumentNumbers();
        return numbers instanceof DocumentNumbering impl ? impl : null;
    }
}

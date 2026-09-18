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
package com.terp.plugin.gui;

import com.terp.plugin.TerpApplication;
import com.terp.plugin.data.IDocumentTemplates;
import com.terp.plugin.data.model.IDocumentTemplate;
import java.util.Map;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Window;

/**
 * Prints a filled document template (or a default body) through the system printer.
 */
public final class DocumentPrint {

    private DocumentPrint() {
    }

    public static void print(Window owner, String documentType, String title, String defaultBody,
            Map<String, String> values) {
        String body = defaultBody == null ? "" : defaultBody;
        IDocumentTemplates templates = TerpApplication.getInstance().getDocumentTemplates();
        IDocumentTemplate template = templates == null ? null : templates.findByType(documentType);
        if (template != null && template.getBody() != null && !template.getBody().isBlank()) {
            body = template.getBody();
        }
        if (values != null) {
            for (Map.Entry<String, String> entry : values.entrySet()) {
                String key = entry.getKey() == null ? "" : entry.getKey();
                String value = entry.getValue() == null ? "" : entry.getValue();
                body = body.replace("{" + key + "}", value);
            }
        }
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            showError(owner, "Yazıcı bulunamadı.");
            return;
        }
        if (owner != null && !job.showPrintDialog(owner)) {
            job.endJob();
            return;
        }
        PageLayout layout = job.getJobSettings().getPageLayout();
        Label heading = new Label(title == null ? "" : title);
        heading.setFont(Font.font("Monospaced", 14));
        heading.setWrapText(true);
        heading.setPrefWidth(layout.getPrintableWidth());
        Label text = new Label(body);
        text.setFont(Font.font("Monospaced", 11));
        text.setWrapText(true);
        text.setPrefWidth(layout.getPrintableWidth());
        VBox page = new VBox(8, heading, text);
        page.setPrefWidth(layout.getPrintableWidth());
        boolean ok = job.printPage(layout, page);
        if (ok) {
            job.endJob();
        } else {
            job.endJob();
            showError(owner, "Yazdırma tamamlanamadı.");
        }
    }

    private static void showError(Window owner, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Yazdır");
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.show();
    }
}

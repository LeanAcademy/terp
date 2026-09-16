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
package com.terp.gui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javafx.geometry.Insets;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public final class DesktopListActions {

    private DesktopListActions() {
    }

    public static void print(Window owner, Tab tab) {
        List<SheetData> sheets = sheetsOf(tab);
        if (sheets.isEmpty()) {
            showError("Yazdır", "Açık listede yazdırılacak tablo yok.");
            return;
        }
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            showError("Yazdır", "Yazıcı bulunamadı.");
            return;
        }
        if (!job.showPrintDialog(owner)) {
            return;
        }
        PageLayout layout = job.getJobSettings().getPageLayout();
        String title = tabTitle(tab);
        boolean ok = true;
        for (SheetData sheet : sheets) {
            int pageRows = rowsPerPage(layout);
            int from = 0;
            while (from < sheet.rows.size() || from == 0) {
                int to = Math.min(sheet.rows.size(), from + pageRows);
                Node page = printPage(title, sheet, from, to);
                layoutPrintNode(page, layout);
                ok = job.printPage(layout, page);
                if (!ok) {
                    break;
                }
                from = to;
                if (from == 0) {
                    break;
                }
            }
            if (!ok) {
                break;
            }
        }
        if (ok) {
            job.endJob();
        } else {
            showError("Yazdır", "Yazdırma tamamlanamadı.");
        }
    }

    public static void exportExcel(Window owner, Tab tab) {
        List<SheetData> sheets = sheetsOf(tab);
        if (sheets.isEmpty()) {
            showError("Excel", "Açık listede aktarılacak tablo yok.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Excel'e aktar");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        String base = safeFileName(tabTitle(tab));
        chooser.setInitialFileName(base + ".xlsx");
        java.io.File target = chooser.showSaveDialog(owner);
        if (target == null) {
            return;
        }
        if (!target.getName().toLowerCase().endsWith(".xlsx")) {
            target = new java.io.File(target.getAbsolutePath() + ".xlsx");
        }
        try {
            writeXlsx(target, sheets);
        } catch (IOException ex) {
            showError("Excel", ex.getMessage() == null ? "Dosya yazılamadı." : ex.getMessage());
        }
    }

    private static List<SheetData> sheetsOf(Tab tab) {
        List<SheetData> sheets = new ArrayList<>();
        if (tab == null || tab.getContent() == null) {
            return sheets;
        }
        int index = 1;
        for (TableView<?> table : findTables(tab.getContent())) {
            SheetData sheet = sheetOf(table, index++);
            if (sheet != null) {
                sheets.add(sheet);
            }
        }
        return sheets;
    }

    private static List<TableView<?>> findTables(Node node) {
        List<TableView<?>> tables = new ArrayList<>();
        collectTables(node, tables);
        return tables;
    }

    private static void collectTables(Node node, List<TableView<?>> tables) {
        if (node == null) {
            return;
        }
        if (node instanceof TableView<?> table && table.isVisible()) {
            tables.add(table);
        }
        if (node instanceof javafx.scene.Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectTables(child, tables);
            }
        }
    }

    private static SheetData sheetOf(TableView<?> table, int index) {
        List<TableColumn<?, ?>> columns = visibleColumns(table);
        if (columns.isEmpty()) {
            return null;
        }
        SheetData sheet = new SheetData();
        sheet.name = sheetName(table, index);
        for (TableColumn<?, ?> column : columns) {
            sheet.headers.add(textOf(column.getText()));
        }
        for (Object row : table.getItems()) {
            List<Object> values = new ArrayList<>();
            for (TableColumn<?, ?> column : columns) {
                values.add(cellValue(column, row));
            }
            sheet.rows.add(values);
        }
        return sheet;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List<TableColumn<?, ?>> visibleColumns(TableView<?> table) {
        List<TableColumn<?, ?>> columns = new ArrayList<>();
        for (TableColumn column : table.getVisibleLeafColumns()) {
            if (column != null && column.isVisible()) {
                columns.add(column);
            }
        }
        return columns;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object cellValue(TableColumn column, Object row) {
        if (row == null) {
            return null;
        }
        Object value = column.getCellData(row);
        if (value instanceof javafx.beans.value.ObservableValue<?> observable) {
            return observable.getValue();
        }
        return value;
    }

    private static String sheetName(TableView<?> table, int index) {
        String id = table.getId();
        if (id != null && !id.isBlank()) {
            return clipSheetName(id);
        }
        return index == 1 ? "Liste" : (index == 2 ? "Satirlar" : "Tablo " + index);
    }

    private static String clipSheetName(String name) {
        String cleaned = name.replaceAll("[\\\\/:?*\\[\\]]", " ").trim();
        if (cleaned.isEmpty()) {
            return "Liste";
        }
        return cleaned.length() > 31 ? cleaned.substring(0, 31) : cleaned;
    }

    private static String tabTitle(Tab tab) {
        if (tab == null || tab.getText() == null || tab.getText().isBlank()) {
            return "Liste";
        }
        return tab.getText();
    }

    private static int rowsPerPage(PageLayout layout) {
        double height = layout.getPrintableHeight();
        int rows = (int) Math.floor((height - 48d) / 18d) - 1;
        return Math.max(8, rows);
    }

    private static Node printPage(String title, SheetData sheet, int from, int to) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(8));
        Label heading = new Label(title + " — " + sheet.name);
        heading.setFont(Font.font("System", FontWeight.BOLD, 12));
        box.getChildren().add(heading);
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(2);
        for (int col = 0; col < sheet.headers.size(); col++) {
            Label header = new Label(sheet.headers.get(col));
            header.setFont(Font.font("System", FontWeight.BOLD, 10));
            grid.add(header, col, 0);
        }
        int rowIndex = 1;
        for (int i = from; i < to; i++) {
            List<Object> row = sheet.rows.get(i);
            for (int col = 0; col < row.size(); col++) {
                Label cell = new Label(display(row.get(col)));
                cell.setFont(Font.font("System", 10));
                grid.add(cell, col, rowIndex);
            }
            rowIndex++;
        }
        if (sheet.rows.isEmpty()) {
            grid.add(new Label("(kayıt yok)"), 0, 1);
        }
        box.getChildren().add(grid);
        return box;
    }

    private static void layoutPrintNode(Node page, PageLayout layout) {
        if (!(page instanceof Parent parent)) {
            return;
        }
        new Scene(parent);
        parent.applyCss();
        parent.layout();
        double width = parent.prefWidth(-1);
        double printable = layout.getPrintableWidth();
        if (width > printable && width > 0d) {
            double scale = printable / width;
            parent.getTransforms().add(new Scale(scale, scale));
        }
    }

    private static void writeXlsx(java.io.File target, List<SheetData> sheets) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(new java.io.FileOutputStream(target))) {
            put(zip, "[Content_Types].xml", contentTypes(sheets.size()));
            put(zip, "_rels/.rels", rootRels());
            put(zip, "xl/workbook.xml", workbook(sheets));
            put(zip, "xl/_rels/workbook.xml.rels", workbookRels(sheets.size()));
            for (int i = 0; i < sheets.size(); i++) {
                put(zip, "xl/worksheets/sheet" + (i + 1) + ".xml", worksheet(sheets.get(i)));
            }
        }
    }

    private static String contentTypes(int sheetCount) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xml.append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">");
        xml.append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>");
        xml.append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>");
        xml.append("<Override PartName=\"/xl/workbook.xml\" ");
        xml.append("ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>");
        for (int i = 1; i <= sheetCount; i++) {
            xml.append("<Override PartName=\"/xl/worksheets/sheet").append(i).append(".xml\" ");
            xml.append("ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        }
        xml.append("</Types>");
        return xml.toString();
    }

    private static String rootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" "
                + "Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" "
                + "Target=\"xl/workbook.xml\"/></Relationships>";
    }

    private static String workbook(List<SheetData> sheets) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xml.append("<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" ");
        xml.append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">");
        xml.append("<sheets>");
        for (int i = 0; i < sheets.size(); i++) {
            xml.append("<sheet name=\"").append(xmlEscape(sheets.get(i).name))
                    .append("\" sheetId=\"").append(i + 1)
                    .append("\" r:id=\"rId").append(i + 1).append("\"/>");
        }
        xml.append("</sheets></workbook>");
        return xml.toString();
    }

    private static String workbookRels(int sheetCount) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xml.append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
        for (int i = 1; i <= sheetCount; i++) {
            xml.append("<Relationship Id=\"rId").append(i).append("\" ");
            xml.append("Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" ");
            xml.append("Target=\"worksheets/sheet").append(i).append(".xml\"/>");
        }
        xml.append("</Relationships>");
        return xml.toString();
    }

    private static String worksheet(SheetData sheet) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xml.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        xml.append("<sheetData>");
        xml.append("<row r=\"1\">");
        for (int col = 0; col < sheet.headers.size(); col++) {
            xml.append(inlineCell(1, col, sheet.headers.get(col)));
        }
        xml.append("</row>");
        for (int row = 0; row < sheet.rows.size(); row++) {
            xml.append("<row r=\"").append(row + 2).append("\">");
            List<Object> values = sheet.rows.get(row);
            for (int col = 0; col < values.size(); col++) {
                xml.append(valueCell(row + 2, col, values.get(col)));
            }
            xml.append("</row>");
        }
        xml.append("</sheetData></worksheet>");
        return xml.toString();
    }

    private static String inlineCell(int row, int col, String text) {
        return "<c r=\"" + cellRef(row, col) + "\" t=\"inlineStr\"><is><t>"
                + xmlEscape(textOf(text)) + "</t></is></c>";
    }

    private static String valueCell(int row, int col, Object value) {
        if (value instanceof Number number) {
            return "<c r=\"" + cellRef(row, col) + "\" t=\"n\"><v>"
                    + number.toString() + "</v></c>";
        }
        return inlineCell(row, col, display(value));
    }

    private static String cellRef(int row, int col) {
        return columnName(col) + row;
    }

    private static String columnName(int index) {
        StringBuilder name = new StringBuilder();
        int current = index;
        while (current >= 0) {
            name.insert(0, (char) ('A' + (current % 26)));
            current = current / 26 - 1;
        }
        return name.toString();
    }

    private static void put(ZipOutputStream zip, String path, String xml) throws IOException {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(xml.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String display(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Date date) {
            return new SimpleDateFormat("yyyy-MM-dd").format(date);
        }
        return String.valueOf(value);
    }

    private static String textOf(String value) {
        return value == null ? "" : value;
    }

    private static String xmlEscape(String value) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '&') {
                out.append("&amp;");
            } else if (ch == '<') {
                out.append("&lt;");
            } else if (ch == '>') {
                out.append("&gt;");
            } else if (ch == '"') {
                out.append("&quot;");
            } else if (ch < 0x20 && ch != '\t' && ch != '\n' && ch != '\r') {
                continue;
            } else {
                out.append(ch);
            }
        }
        return out.toString();
    }

    private static String safeFileName(String value) {
        String cleaned = value.replaceAll("[\\\\/:*?\"<>|]", " ").trim();
        return cleaned.isEmpty() ? "liste" : cleaned;
    }

    private static void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.show();
    }

    private static final class SheetData {
        private String name = "Liste";
        private final List<String> headers = new ArrayList<>();
        private final List<List<Object>> rows = new ArrayList<>();
    }
}

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
import com.terp.plugin.data.ICommonFields;
import com.terp.plugin.data.IUserLookup;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Read-only footer showing who added/changed a record and when.
 */
public class RecordAuditBar extends VBox {

    public static final double HEIGHT = 44;

    private final Label added = new Label();
    private final Label changed = new Label();
    private boolean attached;

    public RecordAuditBar() {
        setSpacing(2);
        setPadding(new Insets(6, 10, 6, 10));
        setPrefHeight(HEIGHT);
        setMinHeight(HEIGHT);
        setMaxHeight(HEIGHT);
        setMinWidth(280);
        setMaxWidth(Double.MAX_VALUE);
        getStyleClass().add("record-audit-bar");
        setFooterStyle();
        added.setWrapText(true);
        changed.setWrapText(true);
        added.setStyle("-fx-font-size: 11px; -fx-text-fill: #333333;");
        changed.setStyle("-fx-font-size: 11px; -fx-text-fill: #333333;");
        getChildren().addAll(added, changed);
        VBox.setVgrow(added, Priority.NEVER);
        bind(null);
    }

    public void bind(ICommonFields row) {
        if (row == null || row.getRowId() == null) {
            added.setText("Ekleyen: henüz kaydedilmedi");
            changed.setText("Değiştiren: —");
            return;
        }
        added.setText("Ekleyen: " + whoWhen(row.getAddedByUserId(), row.getAddedDate()));
        changed.setText("Değiştiren: " + whoWhen(row.getUpdatedByUserId(), row.getLastUpdateDate()));
    }

    public static RecordAuditBar install(Node node) {
        RecordAuditBar bar = new RecordAuditBar();
        if (!bar.attachTo(resolvePane(node))) {
            scheduleAttach(bar, node);
        }
        return bar;
    }

    private static void scheduleAttach(RecordAuditBar bar, Node node) {
        if (node == null) {
            return;
        }
        Runnable retry = () -> bar.attachTo(resolvePane(node));
        InvalidationListener listener = obs -> retry.run();
        node.parentProperty().addListener(listener);
        node.sceneProperty().addListener(listener);
        Platform.runLater(retry);
    }

    private boolean attachTo(AnchorPane pane) {
        if (attached) {
            return true;
        }
        if (pane == null) {
            return false;
        }
        for (Node child : pane.getChildren()) {
            if (child instanceof RecordAuditBar existing && existing != this) {
                return false;
            }
        }
        setFooterStyle();
        AnchorPane.setLeftAnchor(this, 0.0);
        AnchorPane.setRightAnchor(this, 0.0);
        AnchorPane.setBottomAnchor(this, 0.0);
        pane.getChildren().add(this);
        for (Node child : pane.getChildren()) {
            if (child == this) {
                continue;
            }
            Double bottom = AnchorPane.getBottomAnchor(child);
            if (bottom != null) {
                AnchorPane.setBottomAnchor(child, bottom + HEIGHT);
            }
        }
        if (pane.getPrefHeight() > 0) {
            pane.setPrefHeight(pane.getPrefHeight() + HEIGHT);
        }
        attached = true;
        return true;
    }

    private void setFooterStyle() {
        setStyle("-fx-background-color: #eeeeee; -fx-border-color: #bdbdbd; -fx-border-width: 1 0 0 0;");
    }

    private static AnchorPane resolvePane(Node node) {
        AnchorPane pane = findAnchorPane(node);
        if (pane != null) {
            return pane;
        }
        Scene scene = node == null ? null : node.getScene();
        if (scene != null && scene.getRoot() instanceof AnchorPane root) {
            return root;
        }
        return null;
    }

    private static AnchorPane findAnchorPane(Node node) {
        Node current = node;
        while (current != null) {
            if (current instanceof AnchorPane pane) {
                return pane;
            }
            current = current.getParent();
        }
        return null;
    }

    private static String whoWhen(Long userId, Date date) {
        String user = userLabel(userId);
        String when = dateLabel(date);
        if ("—".equals(user) && "—".equals(when)) {
            return "—";
        }
        if ("—".equals(user)) {
            return when;
        }
        if ("—".equals(when)) {
            return user;
        }
        return user + " — " + when;
    }

    private static String userLabel(Long userId) {
        if (userId == null) {
            return "—";
        }
        IUserLookup lookup = TerpApplication.getInstance().getUserLookup();
        String name = lookup == null ? null : lookup.findUserName(userId);
        if (name != null && !name.isBlank()) {
            return name;
        }
        return "kul#" + userId;
    }

    private static String dateLabel(Date date) {
        if (date == null) {
            return "—";
        }
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date);
    }
}

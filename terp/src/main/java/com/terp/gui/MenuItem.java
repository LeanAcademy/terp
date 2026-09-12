/*
 * Copyright (C) 2016 Cevdet Dal
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.terp.gui;

import com.terp.data.dao.MenuSourceDao;
import com.terp.data.model.MenuSource;
import com.terp.plugin.IUser;
import com.terp.plugin.TerpApplication;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 *
 * @author cevdet
 */
public class MenuItem extends TreeItem<String> {

    private boolean hasLoadedChildren;
    private boolean leafChecked;
    private boolean leaf;
    private MenuSource currentItem;
    private final MenuSourceDao menuSourceDao = new MenuSourceDao();

    public MenuSource getCurrentItem() {
        return currentItem;
    }

    public void setCurrentItem(MenuSource currentItem) {
        this.currentItem = currentItem;
    }

    public MenuItem(MenuSource item) {
        super(item.getMenuId() + " - " + item.getMenuName());
        if (item.getMenuType() == 0) {
            super.setValue(item.getMenuName());
        }
        this.currentItem = item;
    }

    @Override
    public ObservableList<TreeItem<String>> getChildren() {
        if (!hasLoadedChildren) {
            loadChildren();
        }
        return super.getChildren();
    }

    @Override
    public boolean isLeaf() {
        if (!leafChecked) {
            leafChecked = true;
            leaf = currentItem == null || currentItem.getMenuType() != 0 || !hasVisibleChild();
        }
        return leaf;
    }

    private void loadChildren() {
        hasLoadedChildren = true;
        leafChecked = true;
        List<TreeItem<String>> loaded = new ArrayList<>();
        if (currentItem != null && currentItem.getRowId() != null) {
            List<?> children = menuSourceDao.findAll(
                    "from MenuSource e where e.menuType=1 and e.menuParent="
                            + currentItem.getRowId());
            if (children != null) {
                for (Object menuItem : children) {
                    MenuSource source = (MenuSource) menuItem;
                    if (!allowed(source)) {
                        continue;
                    }
                    loaded.add(new MenuItem(source));
                }
            }
        }
        leaf = loaded.isEmpty();
        super.getChildren().setAll(loaded);
    }

    private boolean hasVisibleChild() {
        if (currentItem.getRowId() == null) {
            return false;
        }
        List<?> children = menuSourceDao.findAll(
                "from MenuSource e where e.menuType=1 and e.menuParent="
                        + currentItem.getRowId());
        if (children == null) {
            return false;
        }
        for (Object row : children) {
            if (allowed((MenuSource) row)) {
                return true;
            }
        }
        return false;
    }

    private static boolean allowed(MenuSource menu) {
        IUser user = TerpApplication.getInstance().getUser();
        return user != null && menu != null && user.canOpen(menu.getMenuId());
    }
}

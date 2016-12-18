/*
 * Copyright (C) 2016 Lean Academy - Cevdet Dal
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.terp.gui;

import com.terp.plugin.gui.IIconFactory;
import javafx.scene.image.Image;

/**
 *
 * @author cevdet
 */
public class IconFactoryImpl implements IIconFactory {

    @Override
    public Image getIconList() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-115-list.png"));
    }

    @Override
    public Image getIconCogWheel() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-137-cogwheel.png"));
    }

    @Override
    public Image getIconBin() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-17-bin.png"));
    }

    @Override
    public Image getIconStepBackward() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-171-step-backward.png"));
    }

    @Override
    public Image getIconFastBackward() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-172-fast-backward.png"));
    }

    @Override
    public Image getIconFastForward() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-178-fast-forward.png"));
    }

    @Override
    public Image getIconStepForward() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-179-step-forward.png"));
    }

    @Override
    public Image getIconQuestionSign() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-195-question-sign.png"));
    }

    @Override
    public Image getIconInfoSign() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-196-info-sign.png"));
    }

    @Override
    public Image getIconOkCircle() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-199-ok-circle.png"));
    }

    @Override
    public Image getIconOk() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-207-ok.png"));
    }

    @Override
    public Image getIconRemove() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-208-remove.png"));
    }

    @Override
    public Image getIconChevronRight() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-224-chevron-right.png"));
    }

    @Override
    public Image getIconChevronLeft() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-225-chevron-left.png"));
    }

    @Override
    public Image getIconFileImport() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-359-file-import.png"));
    }

    @Override
    public Image getIconFileExport() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-360-file-export.png"));
    }

    @Override
    public Image getIconLogin() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-387-log-in.png"));
    }

    @Override
    public Image getIconExit() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-389-exit.png"));
    }

    @Override
    public Image getIconUser() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-4-user.png"));
    }

    @Override
    public Image getIconSortByAlphabet() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-405-sort-by-alphabet.png"));
    }

    @Override
    public Image getIconSortByOrder() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-407-sort-by-order.png"));
    }

    @Override
    public Image getIcondDiskSave() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-415-disk-save.png"));
    }

    @Override
    public Image getIconPlus() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-433-plus.png"));
    }

    @Override
    public Image getIconServer() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-464-server.png"));
    }

    @Override
    public Image getIconMenuHamburger() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-517-menu-hamburger.png"));
    }

    @Override
    public Image getIconQuote() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-547-quote.png"));
    }

    @Override
    public Image getIconClock() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-55-clock.png"));
    }

    @Override
    public Image getIconMixedBuildings() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-619-mixed-buildings.png"));
    }

    @Override
    public Image getIconImportantDay() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-649-important-day.png"));
    }

    @Override
    public Image getIconBuilding() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-90-building.png"));
    }

    @Override
    public Image getIconSearch() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-28-search.png"));
    }

    @Override
    public Image getIconPencil() {
        return new Image(getClass().getResourceAsStream("/images/glyphicons-31-pencil.png"));
    }
    
}

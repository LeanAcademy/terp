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
package com.terp.plugin.data.model;

/**
 * Company VAT rate master. Concrete {@code VatRate} stays in terp.core.
 */
public interface IVatRate {

    String CODE_ZERO = "KDV0";
    String CODE_SUPER_REDUCED = "KDV1";
    String CODE_REDUCED = "KDV10";
    String CODE_STANDARD = "KDV20";

    String getRateCode();

    String getRateName();

    Double getRate();

    int getStatus();

    default String getDisplayLabel() {
        String code = getRateCode() == null ? "" : getRateCode();
        String name = getRateName() == null ? "" : getRateName();
        if (code.isBlank()) {
            return name;
        }
        if (name.isBlank()) {
            return code;
        }
        return code + " — " + name;
    }
}

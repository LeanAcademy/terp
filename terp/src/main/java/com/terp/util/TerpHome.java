/*
 * Copyright (C) 2026 LeanAcademy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.terp.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves the TERP application home directory.
 * Override with {@code -Dterp.home=/path/to/terp}.
 */
public final class TerpHome {

    private TerpHome() {
    }

    public static Path get() {
        String specified = System.getProperty("terp.home");
        if (specified != null && !specified.isBlank()) {
            return Paths.get(specified).toAbsolutePath().normalize();
        }

        Path cwd = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        if (looksLikeHome(cwd)) {
            return cwd;
        }

        Path nested = cwd.resolve("terp");
        if (looksLikeHome(nested)) {
            return nested;
        }

        Path parent = cwd.getParent();
        if (parent != null && looksLikeHome(parent)) {
            return parent;
        }

        return cwd;
    }

    public static Path etcFile(String name) {
        return get().resolve("etc").resolve(name);
    }

    public static Path pluginsDir() {
        return get().resolve("plugins");
    }

    public static Path libDir() {
        return get().resolve("lib");
    }

    private static boolean looksLikeHome(Path dir) {
        return Files.isDirectory(dir.resolve("etc"))
                || Files.isDirectory(dir.resolve("plugins"));
    }
}

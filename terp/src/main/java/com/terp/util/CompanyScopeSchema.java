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
package com.terp.util;

import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Drop global unique indexes on business codes and backfill {@code firma_ref}.
 */
public final class CompanyScopeSchema {

    private static final Logger LOG = Logger.getLogger(CompanyScopeSchema.class.getName());

    private CompanyScopeSchema() {
    }

    public static void align(Long companyId) {
        if (!HibernateUtil.isInitialized()) {
            return;
        }
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            session.doWork(connection -> {
                try (Statement st = connection.createStatement()) {
                    dropQuiet(st, "ALTER TABLE terp.malzeme DROP CONSTRAINT ix_malzeme");
                    dropQuiet(st, "ALTER TABLE terp.malzeme DROP CONSTRAINT IX_MALZEME");
                    dropQuiet(st, "ALTER TABLE terp.cari DROP CONSTRAINT ix_cari");
                    dropQuiet(st, "ALTER TABLE terp.cari DROP CONSTRAINT IX_CARI");
                    dropQuiet(st, "ALTER TABLE terp.depo DROP CONSTRAINT ix_depo");
                    dropQuiet(st, "ALTER TABLE terp.depo DROP CONSTRAINT IX_DEPO");
                    if (companyId != null) {
                        st.executeUpdate("UPDATE terp.malzeme SET firma_ref = " + companyId
                                + " WHERE firma_ref IS NULL");
                        st.executeUpdate("UPDATE terp.cari SET firma_ref = " + companyId
                                + " WHERE firma_ref IS NULL");
                        st.executeUpdate("UPDATE terp.depo SET firma_ref = " + companyId
                                + " WHERE firma_ref IS NULL");
                        st.executeUpdate("UPDATE terp.stok_hareket SET firma_ref = " + companyId
                                + " WHERE firma_ref IS NULL");
                    }
                }
            });
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOG.log(Level.WARNING, "Company-scope schema align failed", ex);
        } finally {
            session.close();
        }
    }

    private static void dropQuiet(Statement st, String sql) {
        try {
            st.executeUpdate(sql);
        } catch (Exception ignored) {
            // already dropped or dialect-specific name
        }
    }
}

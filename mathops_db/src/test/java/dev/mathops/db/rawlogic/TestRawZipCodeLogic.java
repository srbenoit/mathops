package dev.mathops.db.rawlogic;

import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.DbContext;
import dev.mathops.db.cfg.ContextMap;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.db.cfg.ESchemaUse;
import dev.mathops.db.rawrecord.RawZipCode;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Tests for the {@code RawZipCodeLogic} class.
 */
final class TestRawZipCodeLogic {

    /** The database profile. */
    private static DbProfile dbProfile = null;

    /** The database context. */
    private static DbContext ctx = null;

    /** Initialize the test class. */
    @BeforeAll
    static void initTests() {

        dbProfile = ContextMap.getDefaultInstance().getCodeProfile(Contexts.INFORMIX_TEST_PATH);
        if (dbProfile == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_TEST_PROFILE));
        }

        ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);
        if (ctx == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PRIMARY_CONTEXT));
        }

        // Make sure we're in the TEST database
        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement();
                     final ResultSet rs = stmt.executeQuery("SELECT descr FROM which_db")) {

                    if (rs.next()) {
                        final String which = rs.getString(1);
                        if (which != null && !"TEST".equals(which.trim())) {
                            throw new IllegalArgumentException(
                                    TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, which));
                        }
                    } else {
                        throw new IllegalArgumentException(TestRes.get(TestRes.ERR_CANT_QUERY_WHICH_DB));
                    }
                }
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            throw new IllegalArgumentException(ex);
        }

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM zip_code");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawZipCode raw1 = new RawZipCode("80111", "City1", "AA");
                final RawZipCode raw2 = new RawZipCode("80222", "City2", "BB");
                final RawZipCode raw3 = new RawZipCode("80333", "City3", "CC");

                assertTrue(RawZipCodeLogic.INSTANCE.insert(cache, raw1), "Failed to insert zip code 1");
                assertTrue(RawZipCodeLogic.INSTANCE.insert(cache, raw2), "Failed to insert zip code 2");
                assertTrue(RawZipCodeLogic.INSTANCE.insert(cache, raw3), "Failed to insert zip code 3");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing tables: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0003() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawZipCode> all = RawZipCodeLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawZipCode test : all) {

                    if ("80111".equals(test.zipCode) && "City1".equals(test.city) && "AA".equals(test.state)) {
                        found1 = true;
                    } else if ("80222".equals(test.zipCode) && "City2".equals(test.city) && "BB".equals(test.state)) {
                        found2 = true;
                    } else if ("80333".equals(test.zipCode) && "City3".equals(test.city) && "CC".equals(test.state)) {
                        found3 = true;
                    } else {
                        Log.warning("Unexpected zipCode ", test.zipCode);
                        Log.warning("Unexpected test.city ", test.city);
                        Log.warning("Unexpected state ", test.state);
                    }
                }

                assertTrue(found1, "ZipCode 1 not found");
                assertTrue(found2, "ZipCode 2 not found");
                assertTrue(found3, "ZipCode 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all zip_code rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results were correct")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawZipCode raw2 = new RawZipCode("80222", "City2", "BB");

                final boolean result = RawZipCodeLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawZipCode> all = RawZipCodeLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;

                for (final RawZipCode test : all) {
                    if ("80111".equals(test.zipCode) && "City1".equals(test.city) && "AA".equals(test.state)) {
                        found1 = true;
                    } else if ("80333".equals(test.zipCode) && "City3".equals(test.city) && "CC".equals(test.state)) {
                        found3 = true;
                    } else {
                        Log.warning("Unexpected zipCode ", test.zipCode);
                        Log.warning("Unexpected test.city ", test.city);
                        Log.warning("Unexpected state ", test.state);
                    }
                }

                assertTrue(found1, "ZipCode 1 not found");
                assertTrue(found3, "ZipCode 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting zip_code: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM zip_code");
                }

                conn.commit();

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning table: " + ex.getMessage());
        }
    }
}

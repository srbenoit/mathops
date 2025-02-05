package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawLogins;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawLoginsLogic} class.
 */
final class TestRawLoginsLogic {

    /** A date/time used in test records. */
    private static final LocalDateTime datetime1 = LocalDateTime.of(2021, 1, 2, 3, 4);

    /** A date/time used in test records. */
    private static final LocalDateTime datetime2 = LocalDateTime.of(2021, 3, 4, 5, 6);

    /** A date/time used in test records. */
    private static final LocalDateTime datetime3 = LocalDateTime.of(2022, 1, 2, 3, 4);

    /** A date/time used in test records. */
    private static final LocalDateTime datetime4 = LocalDateTime.of(2021, 2, 3, 4, 5);

    /** A date/time used in test records. */
    private static final LocalDateTime datetime5 = LocalDateTime.of(2022, 3, 4, 5, 6);

    /** A date/time used in test records. */
    private static final LocalDateTime datetime6 = LocalDateTime.of(2022, 2, 3, 4, 5);

    /** The database profile. */
    private static Profile profile = null;

    /** Initialize the test class. */
    @BeforeAll
    static void initTests() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        profile = config.getCodeProfile(Contexts.INFORMIX_TEST_PATH);
        if (profile == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_TEST_PROFILE));
        }

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();
        final Cache cache = new Cache(profile);

        // Make sure we're in the TEST database
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

            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM logins");
            }
            conn.commit();

            final RawLogins raw1 = new RawLogins("111111111", "STU", "zippy", "a", "b", datetime1, datetime4,
                    datetime2, "Y", "email@server.com", "c", Integer.valueOf(1));

            final RawLogins raw2 = new RawLogins("222222222", "GUE", "floppy", "d", "e", datetime3, datetime6,
                    datetime5, "N", "email@mail.net", "f", Integer.valueOf(2));

            assertTrue(RawLoginsLogic.insert(cache, raw1), "Failed to insert logins");
            assertTrue(RawLoginsLogic.insert(cache, raw2), "Failed to insert logins");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing tables: " + ex.getMessage());
        } finally {
            login.checkInConnection(conn);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0003() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawLogins> all = RawLoginsLogic.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawLogins r : all) {

                if ("111111111".equals(r.userId)
                    && "STU".equals(r.userType)
                    && "zippy".equals(r.userName)
                    && "a".equals(r.storedKey)
                    && "b".equals(r.serverKey)
                    && datetime1.equals(r.dtimeCreated)
                    && datetime4.equals(r.dtimeExpires)
                    && datetime2.equals(r.dtimeLastLogin)
                    && "Y".equals(r.forcePwChange)
                    && "email@server.com".equals(r.email)
                    && "c".equals(r.salt)
                    && Integer.valueOf(1).equals(r.nbrInvalidAtmpts)) {

                    found1 = true;

                } else if ("222222222".equals(r.userId)
                           && "GUE".equals(r.userType)
                           && "floppy".equals(r.userName)
                           && "d".equals(r.storedKey)
                           && "e".equals(r.serverKey)
                           && datetime3.equals(r.dtimeCreated)
                           && datetime6.equals(r.dtimeExpires)
                           && datetime5.equals(r.dtimeLastLogin)
                           && "N".equals(r.forcePwChange)
                           && "email@mail.net".equals(r.email)
                           && "f".equals(r.salt)
                           && Integer.valueOf(2).equals(r.nbrInvalidAtmpts)) {

                    found2 = true;

                } else {
                    Log.warning("Unexpected userId ", r.userId);
                    Log.warning("Unexpected userType ", r.userType);
                    Log.warning("Unexpected userName ", r.userName);
                    Log.warning("Unexpected storedKey ", r.storedKey);
                    Log.warning("Unexpected serverKey ", r.serverKey);
                    Log.warning("Unexpected dtimeCreated ", r.dtimeCreated);
                    Log.warning("Unexpected dtimeExpires ", r.dtimeExpires);
                    Log.warning("Unexpected dtimeLastLogin ", r.dtimeLastLogin);
                    Log.warning("Unexpected forcePwChange ", r.forcePwChange);
                    Log.warning("Unexpected email ", r.email);
                    Log.warning("Unexpected salt ", r.salt);
                    Log.warning("Unexpected nbrInvalidAtmpts ", r.nbrInvalidAtmpts);
                }
            }

            assertTrue(found1, "logins 1 not found");
            assertTrue(found2, "logins 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all logins rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final RawLogins login1 = RawLoginsLogic.query(cache, "zippy");

            assertNotNull(login1, "No record from query");

            final boolean found = "111111111".equals(login1.userId)
                                  && "STU".equals(login1.userType)
                                  && "zippy".equals(login1.userName)
                                  && "a".equals(login1.storedKey)
                                  && "b".equals(login1.serverKey)
                                  && datetime1.equals(login1.dtimeCreated)
                                  && datetime4.equals(login1.dtimeExpires)
                                  && datetime2.equals(login1.dtimeLastLogin)
                                  && "Y".equals(login1.forcePwChange)
                                  && "email@server.com".equals(login1.email)
                                  && "c".equals(login1.salt)
                                  && Integer.valueOf(1).equals(login1.nbrInvalidAtmpts);

            assertTrue(found, "logins not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying logins rows by username: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateLastLoginTime results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final RawLogins login1 = RawLoginsLogic.query(cache, "zippy");

            assertNotNull(login1, "No record from query");

            final LocalDateTime before = LocalDateTime.now().minusSeconds(1L);
            assertTrue(RawLoginsLogic.updateLastLoginTime(cache, login1), "updateLastLoginTime returned false");
            final LocalDateTime after = LocalDateTime.now().plusSeconds(1L);

            final RawLogins updated = RawLoginsLogic.query(cache, "zippy");

            assertNotNull(updated, "No record from query");

            boolean found = false;

            if ("111111111".equals(login1.userId)
                && "STU".equals(login1.userType)
                && "zippy".equals(login1.userName)
                && "a".equals(login1.storedKey)
                && "b".equals(login1.serverKey)
                && datetime1.equals(login1.dtimeCreated)
                && datetime4.equals(login1.dtimeExpires)
                && "Y".equals(login1.forcePwChange)
                && "email@server.com".equals(login1.email)
                && "c".equals(login1.salt)
                && Integer.valueOf(1).equals(login1.nbrInvalidAtmpts)) {

                final LocalDateTime when = updated.dtimeLastLogin;

                Log.info(before);
                Log.info(when);
                Log.info(after);

                found = !when.isBefore(before) && !when.isAfter(after);
            }

            assertTrue(found, "logins not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying logins rows by username: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updatePasswordFails results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final RawLogins login1 = RawLoginsLogic.query(cache, "floppy");

            assertNotNull(login1, "No record from query");

            assertTrue(RawLoginsLogic.updatePasswordFails(cache, "floppy", Integer.valueOf(10)),
                    "updatePasswordFails returned false");

            final RawLogins updated = RawLoginsLogic.query(cache, "floppy");

            assertNotNull(updated, "No record from query");

            final boolean found = "222222222".equals(updated.userId)
                                  && "GUE".equals(updated.userType)
                                  && "floppy".equals(updated.userName)
                                  && "d".equals(updated.storedKey)
                                  && "e".equals(updated.serverKey)
                                  && datetime3.equals(updated.dtimeCreated)
                                  && datetime6.equals(updated.dtimeExpires)
                                  && datetime5.equals(updated.dtimeLastLogin)
                                  && "N".equals(updated.forcePwChange)
                                  && "email@mail.net".equals(updated.email)
                                  && "f".equals(updated.salt)
                                  && Integer.valueOf(10).equals(updated.nbrInvalidAtmpts);

            assertTrue(found, "logins not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying logins rows by username: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0007() {

        final Cache cache = new Cache(profile);

        try {
            final RawLogins raw2 = new RawLogins("222222222", "GUE", "floppy", "d", "e", datetime3, datetime6,
                    datetime5, "N", "email@mail.net", "f", Integer.valueOf(2));

            final boolean result = RawLoginsLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawLogins> all = RawLoginsLogic.queryAll(cache);

            assertEquals(1, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;

            for (final RawLogins r : all) {

                if ("111111111".equals(r.userId)
                    && "STU".equals(r.userType)
                    && "zippy".equals(r.userName)
                    && "a".equals(r.storedKey)
                    && "b".equals(r.serverKey)
                    && datetime1.equals(r.dtimeCreated)
                    && datetime4.equals(r.dtimeExpires)
                    && "Y".equals(r.forcePwChange)
                    && "email@server.com".equals(r.email)
                    && "c".equals(r.salt)
                    && Integer.valueOf(1).equals(r.nbrInvalidAtmpts)) {

                    found1 = true;

                } else {
                    Log.warning("Unexpected userId ", r.userId);
                    Log.warning("Unexpected userType ", r.userType);
                    Log.warning("Unexpected userName ", r.userName);
                    Log.warning("Unexpected storedKey ", r.storedKey);
                    Log.warning("Unexpected serverKey ", r.serverKey);
                    Log.warning("Unexpected dtimeCreated ", r.dtimeCreated);
                    Log.warning("Unexpected dtimeExpires ", r.dtimeExpires);
                    Log.warning("Unexpected dtimeLastLogin ", r.dtimeLastLogin);
                    Log.warning("Unexpected forcePwChange ", r.forcePwChange);
                    Log.warning("Unexpected email ", r.email);
                    Log.warning("Unexpected salt ", r.salt);
                    Log.warning("Unexpected nbrInvalidAtmpts ", r.nbrInvalidAtmpts);
                }
            }

            assertTrue(found1, "logins 1 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting users: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM logins");
            }

            conn.commit();
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning tables: " + ex.getMessage());
        } finally {
            login.checkInConnection(conn);
        }
    }
}

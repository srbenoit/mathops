package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawStetext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawStetextLogic} class.
 */
final class TestRawStetextLogic {

    /** A date used in test records. */
    private static final LocalDate date9912 = LocalDate.of(1999, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date9934 = LocalDate.of(1999, 3, 4);

    /** A date used in test records. */
    private static final LocalDate date9956 = LocalDate.of(1999, 5, 6);

    /** A date used in test records. */
    private static final LocalDate date0011 = LocalDate.of(2000, 1, 1);

    /** A date used in test records. */
    private static final LocalDate date0023 = LocalDate.of(2000, 2, 3);

    /** A date used in test records. */
    private static final LocalDate date0022 = LocalDate.of(2000, 2, 2);

    /** A date used in test records. */
    private static final LocalDate date0145 = LocalDate.of(2001, 4, 5);

    /** A date used in test records. */
    private static final LocalDate date0155 = LocalDate.of(2001, 5, 5);

    /** A date used in test records. */
    private static final LocalDate date0156 = LocalDate.of(2001, 5, 6);

    /** A date used in test records. */
    private static final LocalDate date0167 = LocalDate.of(2001, 6, 7);

    /** A date used in test records. */
    private static final LocalDate date01910 = LocalDate.of(2001, 9, 10);

    /** A date used in test records. */
    private static final LocalDate date011110 = LocalDate.of(2001, 11, 10);

    /** A date used in test records. */
    private static final LocalDate date0191112 = LocalDate.of(2001, 11, 12);

    /** A date used in test records. */
    private static final LocalDate date20991231 = LocalDate.of(2099, 12, 31);

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
                stmt.executeUpdate("DELETE FROM stetext");
            }
            conn.commit();

            // An expired record
            final RawStetext raw1 = new RawStetext("111111111", // stu_id
                    "ID1", // etext_id
                    date9912, // active_dt
                    "AAAAAA", // etextKey
                    date9934, // expirationDt
                    date9956, // refundDeadlineDt
                    null, // refundDt
                    null); // refundReason

            // A returned record
            final RawStetext raw2 = new RawStetext("111111111", // stu_id
                    "ID1", // etext_id
                    date0011, // active_dt
                    "BBBBB", // etextKey
                    null, // expirationDt
                    date0023, // refundDeadlineDt
                    date0022, // refundDt
                    "Because"); // refundReason

            // An active record with no expiration
            final RawStetext raw3 = new RawStetext("111111111", // stu_id
                    "ID1", // etext_id
                    date0145, // active_dt
                    "CCCCC", // etextKey
                    null, // expirationDt
                    date0156, // refundDeadlineDt
                    null, // refundDt
                    null); // refundReason

            // An active record with future expiration
            final RawStetext raw4 = new RawStetext("222222222", // stu_id
                    "ID2", // etext_id
                    date0156, // active_dt
                    "DDDDD", // etextKey
                    date20991231, // expirationDt
                    date0155, // refundDeadlineDt
                    null, // refundDt
                    null); // refundReason

            // An active record with no expiration
            final RawStetext raw5 = new RawStetext("333333333", // stu_id
                    "ID3", // etext_id
                    date0167, // active_dt
                    "EEEEE", // etextKey
                    null, // expirationDt
                    date01910, // refundDeadlineDt
                    null, // refundDt
                    null); // refundReason

            assertTrue(RawStetextLogic.insert(cache, raw1), "Failed to insert stetext 1");
            assertTrue(RawStetextLogic.insert(cache, raw2), "Failed to insert stetext 2");
            assertTrue(RawStetextLogic.insert(cache, raw3), "Failed to insert stetext 3");
            assertTrue(RawStetextLogic.insert(cache, raw4), "Failed to insert stetext 4");
            assertTrue(RawStetextLogic.insert(cache, raw5), "Failed to insert stetext 5");
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
            final List<RawStetext> all = RawStetextLogic.queryAll(cache);

            assertEquals(5, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final RawStetext test : all) {
                if ("111111111".equals(test.stuId)
                    && "ID1".equals(test.etextId)
                    && date9912.equals(test.activeDt)
                    && "AAAAAA".equals(test.etextKey)
                    && date9934.equals(test.expirationDt)
                    && date9956.equals(test.refundDeadlineDt)
                    && test.refundDt == null
                    && test.refundReason == null) {

                    found1 = true;
                } else if ("111111111".equals(test.stuId)
                           && "ID1".equals(test.etextId)
                           && date0011.equals(test.activeDt)
                           && "BBBBB".equals(test.etextKey)
                           && test.expirationDt == null
                           && date0023.equals(test.refundDeadlineDt)
                           && date0022.equals(test.refundDt)
                           && "Because".equals(test.refundReason)) {

                    found2 = true;
                } else if ("111111111".equals(test.stuId)
                           && "ID1".equals(test.etextId)
                           && date0145.equals(test.activeDt)
                           && "CCCCC".equals(test.etextKey)
                           && test.expirationDt == null
                           && date0156.equals(test.refundDeadlineDt)
                           && test.refundDt == null
                           && test.refundReason == null) {

                    found3 = true;
                } else if ("222222222".equals(test.stuId)
                           && "ID2".equals(test.etextId)
                           && date0156.equals(test.activeDt)
                           && "DDDDD".equals(test.etextKey)
                           && date20991231.equals(test.expirationDt)
                           && date0155.equals(test.refundDeadlineDt)
                           && test.refundDt == null
                           && test.refundReason == null) {

                    found4 = true;
                } else if ("333333333".equals(test.stuId)
                           && "ID3".equals(test.etextId)
                           && date0167.equals(test.activeDt)
                           && "EEEEE".equals(test.etextKey)
                           && test.expirationDt == null
                           && date01910.equals(test.refundDeadlineDt)
                           && test.refundDt == null
                           && test.refundReason == null) {

                    found5 = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected etextId ", test.etextId);
                    Log.warning("Unexpected activeDt ", test.activeDt);
                    Log.warning("Unexpected etextKey ", test.etextKey);
                    Log.warning("Unexpected expirationDt ", test.expirationDt);
                    Log.warning("Unexpected refundDeadlineDt ", test.refundDeadlineDt);
                    Log.warning("Unexpected refundDt ", test.refundDt);
                    Log.warning("Unexpected refundReason ", test.refundReason);
                }
            }

            assertTrue(found1, "Stetext 1 not found");
            assertTrue(found2, "Stetext 2 not found");
            assertTrue(found3, "Stetext 3 not found");
            assertTrue(found4, "Stetext 4 not found");
            assertTrue(found5, "Stetext 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stetext rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudentEtext results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final LocalDateTime newTime = LocalDateTime.of(2023, 1, 2, 3, 4, 5);
            final List<RawStetext> all = RawStetextLogic.queryByStudentEtext(cache, newTime, "111111111", "ID1");

            assertEquals(1, all.size(), "Incorrect record count from queryByStudentEtext");

            boolean found3 = false;

            for (final RawStetext test : all) {
                if ("111111111".equals(test.stuId)
                    && "ID1".equals(test.etextId)
                    && date0145.equals(test.activeDt)
                    && "CCCCC".equals(test.etextKey)
                    && test.expirationDt == null
                    && date0156.equals(test.refundDeadlineDt)
                    && test.refundDt == null
                    && test.refundReason == null) {

                    found3 = true;
                    break;
                }
            }

            assertTrue(found3, "Stetext 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stetext rows by term: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawStetext> all = RawStetextLogic.queryByStudent(cache, "111111111");

            assertEquals(3, all.size(), "Incorrect record count from queryByStudent");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawStetext test : all) {
                if ("111111111".equals(test.stuId)
                    && "ID1".equals(test.etextId)
                    && date9912.equals(test.activeDt)
                    && "AAAAAA".equals(test.etextKey)
                    && date9934.equals(test.expirationDt)
                    && date9956.equals(test.refundDeadlineDt)
                    && test.refundDt == null
                    && test.refundReason == null) {

                    found1 = true;
                } else if ("111111111".equals(test.stuId)
                           && "ID1".equals(test.etextId)
                           && date0011.equals(test.activeDt)
                           && "BBBBB".equals(test.etextKey)
                           && test.expirationDt == null
                           && date0023.equals(test.refundDeadlineDt)
                           && date0022.equals(test.refundDt)
                           && "Because".equals(test.refundReason)) {

                    found2 = true;
                } else if ("111111111".equals(test.stuId)
                           && "ID1".equals(test.etextId)
                           && date0145.equals(test.activeDt)
                           && "CCCCC".equals(test.etextKey)
                           && test.expirationDt == null
                           && date0156.equals(test.refundDeadlineDt)
                           && test.refundDt == null
                           && test.refundReason == null) {

                    found3 = true;
                }
            }

            assertTrue(found1, "Stetext 1 not found");
            assertTrue(found2, "Stetext 2 not found");
            assertTrue(found3, "Stetext 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stetext rows for student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryUnrefundedByKey results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawStetext> all = RawStetextLogic.queryUnrefundedByKey(cache, "CCCCC");

            assertEquals(1, all.size(), "Incorrect record count from queryAll");

            boolean found3 = false;

            for (final RawStetext test : all) {
                if ("111111111".equals(test.stuId)
                    && "ID1".equals(test.etextId)
                    && date0145.equals(test.activeDt)
                    && "CCCCC".equals(test.etextKey)
                    && test.expirationDt == null
                    && date0156.equals(test.refundDeadlineDt)
                    && test.refundDt == null
                    && test.refundReason == null) {

                    found3 = true;
                    break;
                }
            }

            assertTrue(found3, "Stetext 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying unrefunded stetext rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("deactivate results")
    void test0008() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawStetext> all = RawStetextLogic.queryByStudent(cache, "222222222");

            assertEquals(1, all.size(), "Incorrect record count before deactivate");

            final LocalDateTime now = LocalDateTime.now();

            assertTrue(RawStetextLogic.deactivate(cache, now, all.getFirst(), "Reason"),
                    "Incorrect record count from delete");

            final List<RawStetext> after = RawStetextLogic.queryByStudent(cache, "222222222");

            assertEquals(1, after.size(), "Incorrect record count after deactivate");

            boolean found4 = false;

            for (final RawStetext test : after) {
                if ("222222222".equals(test.stuId)
                    && "ID2".equals(test.etextId)
                    && date0156.equals(test.activeDt)
                    && "DDDDD".equals(test.etextKey)
                    && date20991231.equals(test.expirationDt)
                    && date0155.equals(test.refundDeadlineDt)
                    && now.toLocalDate().equals(test.refundDt)
                    && "Reason".equals(test.refundReason)) {

                    found4 = true;
                    break;
                }
            }

            assertTrue(found4, "Stetext 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deactivating stetext row: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateRefundDeadline results")
    void test0009() {

        final Cache cache = new Cache(profile);

        try {
            assertTrue(RawStetextLogic.updateRefundDeadline(cache, "333333333", "ID3", date0167,
                    date0191112), "Incorrect record count from updateRefundDeadline");

            final List<RawStetext> after = RawStetextLogic.queryByStudent(cache, "333333333");

            assertEquals(1, after.size(), "Incorrect record count after updateRefundDeadline");

            boolean found5 = false;

            for (final RawStetext test : after) {
                if ("333333333".equals(test.stuId)
                    && "ID3".equals(test.etextId)
                    && date0167.equals(test.activeDt)
                    && "EEEEE".equals(test.etextKey)
                    && test.expirationDt == null
                    && date0191112.equals(test.refundDeadlineDt)
                    && test.refundDt == null
                    && test.refundReason == null) {

                    found5 = true;
                    break;
                }
            }

            assertTrue(found5, "Stetext 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updatint refund deadline of stetext row: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateRefund results")
    void test0010() {

        final Cache cache = new Cache(profile);

        try {
            assertTrue(RawStetextLogic.updateRefund(cache, "333333333", "ID3", date0167,
                    date011110, "Reason"), "Incorrect record count from updateRefund");

            final List<RawStetext> after = RawStetextLogic.queryByStudent(cache, "333333333");

            assertEquals(1, after.size(), "Incorrect record count after updateRefund");

            boolean found5 = false;

            for (final RawStetext test : after) {
                if ("333333333".equals(test.stuId)
                    && "ID3".equals(test.etextId)
                    && date0167.equals(test.activeDt)
                    && "EEEEE".equals(test.etextKey)
                    && test.expirationDt == null
                    && date0191112.equals(test.refundDeadlineDt)
                    && date011110.equals(test.refundDt)
                    && "Reason".equals(test.refundReason)) {

                    found5 = true;
                    break;
                }
            }

            assertTrue(found5, "Stetext 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating refund of stetext row: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0011() {

        final Cache cache = new Cache(profile);

        try {
            final RawStetext raw2 = new RawStetext("111111111", // stu_id
                    "ID1", // etext_id
                    date0011, // active_dt
                    "BBBBB", // etextKey
                    null, // expirationDt
                    date0023, // refundDeadlineDt
                    date0022, // refundDt
                    "Because"); // refundReason

            final boolean result = RawStetextLogic.delete(cache, raw2);

            assertTrue(result, "delete returned false");

            final List<RawStetext> all = RawStetextLogic.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            final LocalDate today = LocalDate.now();

            for (final RawStetext test : all) {
                if ("111111111".equals(test.stuId)
                    && "ID1".equals(test.etextId)
                    && date9912.equals(test.activeDt)
                    && "AAAAAA".equals(test.etextKey)
                    && date9934.equals(test.expirationDt)
                    && date9956.equals(test.refundDeadlineDt)
                    && test.refundDt == null
                    && test.refundReason == null) {

                    found1 = true;
                } else if ("111111111".equals(test.stuId)
                           && "ID1".equals(test.etextId)
                           && date0145.equals(test.activeDt)
                           && "CCCCC".equals(test.etextKey)
                           && test.expirationDt == null
                           && date0156.equals(test.refundDeadlineDt)
                           && test.refundDt == null
                           && test.refundReason == null) {

                    found3 = true;
                } else if ("222222222".equals(test.stuId)
                           && "ID2".equals(test.etextId)
                           && date0156.equals(test.activeDt)
                           && "DDDDD".equals(test.etextKey)
                           && date20991231.equals(test.expirationDt)
                           && date0155.equals(test.refundDeadlineDt)
                           && today.equals(test.refundDt)
                           && "Reason".equals(test.refundReason)) {

                    found4 = true;
                } else if ("333333333".equals(test.stuId)
                           && "ID3".equals(test.etextId)
                           && date0167.equals(test.activeDt)
                           && "EEEEE".equals(test.etextKey)
                           && test.expirationDt == null
                           && date0191112.equals(test.refundDeadlineDt)
                           && date011110.equals(test.refundDt)
                           && "Reason".equals(test.refundReason)) {

                    found5 = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected etextId ", test.etextId);
                    Log.warning("Unexpected activeDt ", test.activeDt);
                    Log.warning("Unexpected etextKey ", test.etextKey);
                    Log.warning("Unexpected expirationDt ", test.expirationDt);
                    Log.warning("Unexpected refundDeadlineDt ", test.refundDeadlineDt);
                    Log.warning("Unexpected refundDt ", test.refundDt);
                    Log.warning("Unexpected refundReason ", test.refundReason);
                }
            }

            assertTrue(found1, "Stetext 1 not found");
            assertTrue(found3, "Stetext 3 not found");
            assertTrue(found4, "Stetext 4 not found");
            assertTrue(found5, "Stetext 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all sthomework rows afer delete: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM stetext");
            }

            conn.commit();
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning table: " + ex.getMessage());
        } finally {
            login.checkInConnection(conn);
        }
    }
}

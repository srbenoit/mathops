package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawMpscorequeue;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawMpscorequeueLogic} class.
 */
final class TestRawMpscorequeueLogic {

    /** A date-time used in test records. */
    private static final LocalDateTime datetime1 = LocalDateTime.of(2020, 1, 2, 12, 34, 56);

    /** A date-time used in test records. */
    private static final LocalDateTime datetime2 = LocalDateTime.of(2021, 2, 3, 4, 5, 6);

    /** A date-time used in test records. */
    private static final LocalDateTime datetime3 = LocalDateTime.of(2021, 12, 11, 10, 9, 8);

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
                stmt.executeUpdate("DELETE FROM mpscorequeue");
            }
            conn.commit();

            final RawMpscorequeue raw1 = new RawMpscorequeue(Integer.valueOf(123456), "MC00", datetime1, "2");
            final RawMpscorequeue raw2 = new RawMpscorequeue(Integer.valueOf(123456), "MC17", datetime1, "1");
            final RawMpscorequeue raw3 = new RawMpscorequeue(Integer.valueOf(456789), "MC26", datetime2, "0");

            assertTrue(RawMpscorequeueLogic.insert(cache, raw1), "Failed to insert mpscorequeue");
            assertTrue(RawMpscorequeueLogic.insert(cache, raw2), "Failed to insert mpscorequeue");
            assertTrue(RawMpscorequeueLogic.insert(cache, raw3), "Failed to insert mpscorequeue");
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
            final List<RawMpscorequeue> all = RawMpscorequeueLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawMpscorequeue test : all) {

                if (Integer.valueOf(123456).equals(test.pidm)
                    && "MC00".equals(test.testCode)
                    && datetime1.equals(test.testDate)
                    && "2".equals(test.testScore)) {
                    found1 = true;
                } else if (Integer.valueOf(123456).equals(test.pidm)
                           && "MC17".equals(test.testCode)
                           && datetime1.equals(test.testDate)
                           && "1".equals(test.testScore)) {
                    found2 = true;
                } else if (Integer.valueOf(456789).equals(test.pidm)
                           && "MC26".equals(test.testCode)
                           && datetime2.equals(test.testDate)
                           && "0".equals(test.testScore)) {
                    found3 = true;
                } else {
                    Log.warning("Unexpected pidm ", test.pidm);
                    Log.warning("Unexpected testCode ", test.testCode);
                    Log.warning("Unexpected testDate ", test.testDate);
                    Log.warning("Unexpected testScore ", test.testScore);
                }
            }

            assertTrue(found1, "mpscorequeue 1 not found");
            assertTrue(found2, "mpscorequeue 2 not found");
            assertTrue(found3, "mpscorequeue 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all mpscorequeue rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByPidm results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawMpscorequeue> all = RawMpscorequeueLogic.queryByPidm(cache, Integer.valueOf(123456));

            boolean found1 = false;
            boolean found2 = false;

            for (final RawMpscorequeue test : all) {

                if (Integer.valueOf(123456).equals(test.pidm)
                    && "MC00".equals(test.testCode)
                    && datetime1.equals(test.testDate)
                    && "2".equals(test.testScore)) {
                    found1 = true;
                } else if (Integer.valueOf(123456).equals(test.pidm)
                           && "MC17".equals(test.testCode)
                           && datetime1.equals(test.testDate)
                           && "1".equals(test.testScore)) {
                    found2 = true;
                } else {
                    Log.warning("Unexpected pidm ", test.pidm);
                    Log.warning("Unexpected testCode ", test.testCode);
                    Log.warning("Unexpected testDate ", test.testDate);
                    Log.warning("Unexpected testScore ", test.testScore);
                }
            }

            assertTrue(found1, "mpscorequeue 1 not found");
            assertTrue(found2, "mpscorequeue 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying mpscorequeue rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("insertMpscorequeue results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final RawMpscorequeue toInsert = new RawMpscorequeue(Integer.valueOf(99999), "MC12", datetime3, "3");

            assertTrue(RawMpscorequeueLogic.insert(cache, toInsert), "insertMpscorequeue failed");

            final List<RawMpscorequeue> all = RawMpscorequeueLogic.queryByPidm(cache, Integer.valueOf(99999));

            assertEquals(1, all.size(), "query after insertMpscorequeue returned wrong number of rows");

            final RawMpscorequeue rec = all.getFirst();

            assertEquals(rec.pidm, toInsert.pidm, "Invalid PIDM after query");
            assertEquals(rec.testCode, toInsert.testCode, "Invalid test code after query");
            assertEquals(rec.testDate, toInsert.testDate, "Invalid test date after query");
            assertEquals(rec.testScore, toInsert.testScore, "Invalid test score after query");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while inserting mpscorequeue rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("deleteMpscorequeue results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final RawMpscorequeue toDelete = new RawMpscorequeue(Integer.valueOf(99999), "MC12", datetime3, "3");

            assertTrue(RawMpscorequeueLogic.delete(cache, toDelete), "deleteMpscorequeue failed");

            final List<RawMpscorequeue> all = RawMpscorequeueLogic.queryByPidm(cache, Integer.valueOf(99999));

            assertEquals(0, all.size(), "query after deleteMpscorequeue returned wrong number of rows");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting mpscorequeue rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("postChallengeCredit results with Banner down")
    void test0007() {

        final Integer pidm = Integer.valueOf(10567708);

        final LocalDateTime nowWithMs = LocalDateTime.now();
        final LocalDateTime now = LocalDateTime.of(nowWithMs.getYear(), nowWithMs.getMonth(), nowWithMs.getDayOfMonth(),
                nowWithMs.getHour(), nowWithMs.getMinute(), nowWithMs.getSecond());

        final Cache cache = new Cache(profile);
        LogicUtils.indicateBannerDown();

        final Login liveLogin = profile.getLogin(ESchema.LIVE);

        try {
            final DbConnection liveConn = liveLogin.checkOutConnection();
            try {
                RawMpscorequeueLogic.postChallengeCredit(cache, liveConn, pidm, RawRecordConstants.M117, now);

                final List<RawMpscorequeue> all = RawMpscorequeueLogic.queryByPidm(cache, pidm);

                assertEquals(1, all.size(),
                        "query after postChallengeCredit with Banner down returned wrong number of rows");

                final RawMpscorequeue rec = all.getFirst();

                assertEquals(pidm, rec.pidm, "Invalid PIDM after query");
                assertEquals(RawMpscorequeueLogic.MC17, rec.testCode, "Invalid test code after query");
                assertEquals(now, rec.testDate, "Invalid test date after query");
                assertEquals("2", rec.testScore, "Invalid test score after query");

                assertTrue(RawMpscorequeueLogic.delete(cache, rec), "deleteMpscorequeue failed");

                final List<RawMpscorequeue> sortest = RawMpscorequeueLogic.querySORTESTByStudent(liveConn, pidm);

                assertEquals(0, sortest.size(),
                        "query SORTEST after postChallengeCredit with Banner down returned wrong number of rows");

            } finally {
                liveLogin.checkInConnection(liveConn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while posting challenge credit: " + ex.getMessage());
        } finally {
            LogicUtils.indicateBannerUp();
        }
    }

    // TODO: THE FOLLOWING TEST CASE INSERTS DATA INTO BANNER FOR PIDM '10567708' (Steve Benoit). IF YOU ENABLE THIS
    // TODO: TEST CASE AND RUN THE TEST, BE SURE TO MANUALLY CLEAN OUT ANY POSTED SCORES WITH THE TEST RUN DATE.

    /** Test case. */
    @Disabled("Disabled since this actually inserts into Banner - data will need manual cleanup when rum")
    @Test
    @DisplayName("postChallengeCredit results with Banner up")
    void test0008() {

        final Integer pidm = Integer.valueOf(10567708);

        final LocalDateTime nowWithMs = LocalDateTime.now();
        final LocalDateTime now =
                LocalDateTime.of(nowWithMs.getYear(), nowWithMs.getMonth(), nowWithMs.getDayOfMonth(),
                        nowWithMs.getHour(), nowWithMs.getMinute(), nowWithMs.getSecond());

        final Cache cache = new Cache(profile);
        LogicUtils.indicateBannerUp();

        final Login liveLogin = profile.getLogin(ESchema.LIVE);

        try {
            final DbConnection liveConn = liveLogin.checkOutConnection();
            try {
                RawMpscorequeueLogic.postChallengeCredit(cache, liveConn, pidm, RawRecordConstants.M117, now);

                final List<RawMpscorequeue> all = RawMpscorequeueLogic.queryByPidm(cache, pidm);

                assertEquals(0, all.size(),
                        "query after postChallengeCredit with Banner up returned wrong number of rows");

                final List<RawMpscorequeue> sortest =
                        RawMpscorequeueLogic.querySORTESTByStudent(liveConn, pidm);

                assertEquals(1, sortest.size(),
                        "query SORTEST after postChallengeCredit with Banner up returned wrong number of rows");

                final RawMpscorequeue rec = sortest.getFirst();

                assertEquals(pidm, rec.pidm, "Invalid PIDM after query");
                assertEquals(RawMpscorequeueLogic.MC17, rec.testCode, "Invalid test code after query");
                assertEquals(now, rec.testDate, "Invalid test date after query");
                assertEquals("2", rec.testScore, "Invalid test score after query");
            } finally {
                liveLogin.checkInConnection(liveConn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while posting challenge credit: " + ex.getMessage());
        } finally {
            LogicUtils.indicateBannerUp();
        }
    }

    /** Test case. */
    @Test
    @DisplayName("postPlacementToolResult results with Banner down")
    void test0009() {

        final Integer pidm = Integer.valueOf(10567708);

        final LocalDateTime nowWithMs = LocalDateTime.now();
        final LocalDateTime now = LocalDateTime.of(nowWithMs.getYear(), nowWithMs.getMonth(), nowWithMs.getDayOfMonth(),
                nowWithMs.getHour(), nowWithMs.getMinute(), nowWithMs.getSecond());

        final Collection<String> earned = new ArrayList<>(4);
        earned.add(RawRecordConstants.M100C);
        earned.add(RawRecordConstants.M117);
        earned.add(RawRecordConstants.M118);
        earned.add(RawRecordConstants.M124);

        final Cache cache = new Cache(profile);
        LogicUtils.indicateBannerDown();

        final Login liveLogin = profile.getLogin(ESchema.LIVE);

        try {
            final DbConnection liveConn = liveLogin.checkOutConnection();
            try {
                RawMpscorequeueLogic.postPlacementToolResult(cache, liveConn, pidm, earned, now);

                final List<RawMpscorequeue> all = RawMpscorequeueLogic.queryByPidm(cache, pidm);

                assertEquals(6, all.size(),
                        "query after postPlacementToolResult with Banner down returned wrong number of rows");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;
                boolean found6 = false;

                for (final RawMpscorequeue test : all) {

                    if (pidm.equals(test.pidm)
                        && "MC17".equals(test.testCode)
                        && now.equals(test.testDate)
                        && "1".equals(test.testScore)) {
                        found1 = true;
                    } else if (pidm.equals(test.pidm)
                               && "MC18".equals(test.testCode)
                               && now.equals(test.testDate)
                               && "1".equals(test.testScore)) {
                        found2 = true;
                    } else if (pidm.equals(test.pidm)
                               && "MC24".equals(test.testCode)
                               && now.equals(test.testDate)
                               && "1".equals(test.testScore)) {
                        found3 = true;
                    } else if (pidm.equals(test.pidm)
                               && "MC25".equals(test.testCode)
                               && now.equals(test.testDate)
                               && "0".equals(test.testScore)) {
                        found4 = true;
                    } else if (pidm.equals(test.pidm)
                               && "MC26".equals(test.testCode)
                               && now.equals(test.testDate)
                               && "0".equals(test.testScore)) {
                        found5 = true;
                    } else if (pidm.equals(test.pidm)
                               && "MC00".equals(test.testCode)
                               && now.equals(test.testDate)
                               && "2".equals(test.testScore)) {
                        found6 = true;
                    } else {
                        Log.warning("Unexpected pidm ", test.pidm);
                        Log.warning("Unexpected testCode ", test.testCode);
                        Log.warning("Unexpected testDate ", test.testDate);
                        Log.warning("Unexpected testScore ", test.testScore);
                    }
                }

                assertTrue(found1, "mpscorequeue 1 not found");
                assertTrue(found2, "mpscorequeue 2 not found");
                assertTrue(found3, "mpscorequeue 3 not found");
                assertTrue(found4, "mpscorequeue 4 not found");
                assertTrue(found5, "mpscorequeue 5 not found");
                assertTrue(found6, "mpscorequeue 6 not found");

                for (final RawMpscorequeue test : all) {
                    assertTrue(RawMpscorequeueLogic.delete(cache, test), "deleteMpscorequeue failed");
                }

                final List<RawMpscorequeue> sortest =
                        RawMpscorequeueLogic.querySORTESTByStudent(liveConn, pidm);

                assertEquals(0, sortest.size(), "query SORTEST after postPlacementToolResult with Banner down "
                                                + "returned wrong number of rows");

            } finally {
                liveLogin.checkInConnection(liveConn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while posting placement result: " + ex.getMessage());
        } finally {
            LogicUtils.indicateBannerUp();
        }
    }

    // TODO: THE FOLLOWING TEST CASE INSERTS DATA INTO BANNER FOR PIDM '10567708' (Steve Benoit). IF YOU ENABLE THIS
    // TODO: TEST CASE AND RUN THE TEST, BE SURE TO MANUALLY CLEAN OUT ANY POSTED SCORES WITH THE TEST RUN DATE.

    /** Test case. */
    @Disabled("Disabled since this actually inserts into Banner - data will need manual cleanup when rum")
    @Test
    @DisplayName("postPlacementToolResult results with Banner up")
    void test0010() {

        final Integer pidm = Integer.valueOf(10567708);

        final LocalDateTime nowWithMs = LocalDateTime.now();
        final LocalDateTime now =
                LocalDateTime.of(nowWithMs.getYear(), nowWithMs.getMonth(), nowWithMs.getDayOfMonth(),
                        nowWithMs.getHour(), nowWithMs.getMinute(), nowWithMs.getSecond());

        final List<String> earned = new ArrayList<>(4);
        earned.add(RawRecordConstants.M100C);
        earned.add(RawRecordConstants.M117);
        earned.add(RawRecordConstants.M118);
        earned.add(RawRecordConstants.M124);

        final Cache cache = new Cache(profile);
        LogicUtils.indicateBannerUp();

        final Login liveLogin = profile.getLogin(ESchema.LIVE);

        try {
            final DbConnection liveConn = liveLogin.checkOutConnection();
            try {
                RawMpscorequeueLogic.postPlacementToolResult(cache, liveConn, pidm, earned, now);

                final List<RawMpscorequeue> all = RawMpscorequeueLogic.queryByPidm(cache, pidm);

                assertEquals(0, all.size(),
                        "query after postPlacementToolResult with Banner up returned wrong number of rows");

                final List<RawMpscorequeue> sortest =
                        RawMpscorequeueLogic.querySORTESTByStudent(liveConn, pidm);

                assertEquals(6, sortest.size(),
                        "query SORTEST after postPlacementToolResult with Banner up returned wrong number of rows");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;
                boolean found6 = false;

                for (final RawMpscorequeue test : sortest) {

                    if (pidm.equals(test.pidm) //
                        && "MC17".equals(test.testCode)
                        && now.equals(test.testDate) //
                        && "1".equals(test.testScore)) {
                        found1 = true;
                    } else if (pidm.equals(test.pidm) //
                               && "MC18".equals(test.testCode)
                               && now.equals(test.testDate) //
                               && "1".equals(test.testScore)) {
                        found2 = true;
                    } else if (pidm.equals(test.pidm) //
                               && "MC24".equals(test.testCode)
                               && now.equals(test.testDate) //
                               && "1".equals(test.testScore)) {
                        found3 = true;
                    } else if (pidm.equals(test.pidm) //
                               && "MC25".equals(test.testCode)
                               && now.equals(test.testDate) //
                               && "0".equals(test.testScore)) {
                        found4 = true;
                    } else if (pidm.equals(test.pidm) //
                               && "MC26".equals(test.testCode)
                               && now.equals(test.testDate) //
                               && "0".equals(test.testScore)) {
                        found5 = true;
                    } else if (pidm.equals(test.pidm) //
                               && "MC00".equals(test.testCode)
                               && now.equals(test.testDate) //
                               && "2".equals(test.testScore)) {
                        found6 = true;
                    } else {
                        Log.warning("Unexpected pidm ", test.pidm);
                        Log.warning("Unexpected testCode ", test.testCode);
                        Log.warning("Unexpected testDate ", test.testDate);
                        Log.warning("Unexpected testScore ", test.testScore);
                    }
                }

                assertTrue(found1, "mpscorequeue 1 not found");
                assertTrue(found2, "mpscorequeue 2 not found");
                assertTrue(found3, "mpscorequeue 3 not found");
                assertTrue(found4, "mpscorequeue 4 not found");
                assertTrue(found5, "mpscorequeue 5 not found");
                assertTrue(found6, "mpscorequeue 6 not found");

            } finally {
                liveLogin.checkInConnection(liveConn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while posting placement result: " + ex.getMessage());
        } finally {
            LogicUtils.indicateBannerUp();
        }
    }

    /** Test case. */
    @Test
    @DisplayName("postPrecalcTutorialResult results with Banner down")
    void test0011() {

        final Integer pidm = Integer.valueOf(10567708);

        final LocalDateTime nowWithMs = LocalDateTime.now();
        final LocalDateTime now = LocalDateTime.of(nowWithMs.getYear(), nowWithMs.getMonth(), nowWithMs.getDayOfMonth(),
                nowWithMs.getHour(), nowWithMs.getMinute(), nowWithMs.getSecond());

        final Cache cache = new Cache(profile);
        LogicUtils.indicateBannerDown();

        final Login liveLogin = profile.getLogin(ESchema.LIVE);

        try {
            final DbConnection liveConn = liveLogin.checkOutConnection();
            try {
                RawMpscorequeueLogic.postPrecalcTutorialResult(cache, liveConn, pidm,
                        RawRecordConstants.M117, now);

                final List<RawMpscorequeue> all = RawMpscorequeueLogic.queryByPidm(cache, pidm);

                assertEquals(1, all.size(), "query after postPrecalcTutorialResult "
                                            + "with Banner down returned wrong number of rows");

                final RawMpscorequeue rec = all.getFirst();

                assertEquals(pidm, rec.pidm, "Invalid PIDM after query");
                assertEquals(RawMpscorequeueLogic.MC17, rec.testCode, "Invalid test code after query");
                assertEquals(now, rec.testDate, "Invalid test date after query");
                assertEquals("1", rec.testScore, "Invalid test score after query");

                assertTrue(RawMpscorequeueLogic.delete(cache, rec), "deleteMpscorequeue failed");

                final List<RawMpscorequeue> sortest = RawMpscorequeueLogic.querySORTESTByStudent(liveConn, pidm);

                assertEquals(0, sortest.size(), "query SORTEST after postPrecalcTutorialResult with Banner down "
                                                + "returned wrong number of rows");

            } finally {
                liveLogin.checkInConnection(liveConn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while posting precalculus tutorial result: " + ex.getMessage());
        } finally {
            LogicUtils.indicateBannerUp();
        }
    }

    // TODO: THE FOLLOWING TEST CASE INSERTS DATA INTO BANNER FOR PIDM '10567708' (Steve Benoit). IF YOU ENABLE THIS
    // TODO: TEST CASE AND RUN THE TEST, BE SURE TO MANUALLY CLEAN OUT ANY POSTED SCORES WITH THE TEST RUN DATE.

    /** Test case. */
    @Disabled("Disabled since this actually inserts into Banner - data will need manual cleanup when rum")
    @Test
    @DisplayName("postPrecalcTutorialResult results with Banner up")
    void test0012() {

        final Integer pidm = Integer.valueOf(10567708);
        final LocalDateTime nowWithMs = LocalDateTime.now();
        final LocalDateTime now = LocalDateTime.of(nowWithMs.getYear(), nowWithMs.getMonth(), nowWithMs.getDayOfMonth(),
                nowWithMs.getHour(), nowWithMs.getMinute(), nowWithMs.getSecond());

        final Cache cache = new Cache(profile);
        LogicUtils.indicateBannerUp();

        final Login liveLogin = profile.getLogin(ESchema.LIVE);

        try {
            final DbConnection liveConn = liveLogin.checkOutConnection();
            try {
                RawMpscorequeueLogic.postPrecalcTutorialResult(cache, liveConn, pidm,
                        RawRecordConstants.M117, now);

                final List<RawMpscorequeue> all = RawMpscorequeueLogic.queryByPidm(cache, pidm);

                assertEquals(0, all.size(),
                        "query after postPrecalcTutorialResult with Banner up returned wrong number of rows");

                final List<RawMpscorequeue> sortest = RawMpscorequeueLogic.querySORTESTByStudent(liveConn, pidm);

                assertEquals(1, sortest.size(), "query SORTEST after postPrecalcTutorialResult with Banner up "
                                                + "returned wrong number of rows");

                final RawMpscorequeue rec = sortest.getFirst();

                assertEquals(pidm, rec.pidm, "Invalid PIDM after query");
                assertEquals(RawMpscorequeueLogic.MC17, rec.testCode, "Invalid test code after query");
                assertEquals(now, rec.testDate, "Invalid test date after query");
                assertEquals("1", rec.testScore, "Invalid test score after query");

            } finally {
                liveLogin.checkInConnection(liveConn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while posting precalculus tutorial result: " + ex.getMessage());
        } finally {
            LogicUtils.indicateBannerUp();
        }
    }

    /** Test case. */
    @Test
    @DisplayName("postELMTutorialResult results with Banner down")
    void test0013() {

        final Integer pidm = Integer.valueOf(10567708);

        final LocalDateTime nowWithMs = LocalDateTime.now();
        final LocalDateTime now =
                LocalDateTime.of(nowWithMs.getYear(), nowWithMs.getMonth(), nowWithMs.getDayOfMonth(),
                        nowWithMs.getHour(), nowWithMs.getMinute(), nowWithMs.getSecond());

        final Cache cache = new Cache(profile);
        LogicUtils.indicateBannerDown();

        final Login liveLogin = profile.getLogin(ESchema.LIVE);

        try {
            final DbConnection liveConn = liveLogin.checkOutConnection();
            try {
                RawMpscorequeueLogic.postELMTutorialResult(cache, liveConn, pidm, now);

                final List<RawMpscorequeue> all = RawMpscorequeueLogic.queryByPidm(cache, pidm);

                assertEquals(1, all.size(),
                        "query after postELMTutorialResult with Banner down returned wrong number of rows");

                final RawMpscorequeue rec = all.getFirst();

                assertEquals(pidm, rec.pidm, "Invalid PIDM after query");
                assertEquals(RawMpscorequeueLogic.MC00, rec.testCode, "Invalid test code after query");
                assertEquals(now, rec.testDate, "Invalid test date after query");
                assertEquals("2", rec.testScore, "Invalid test score after query");

                assertTrue(RawMpscorequeueLogic.delete(cache, rec), "deleteMpscorequeue failed");

                final List<RawMpscorequeue> sortest = RawMpscorequeueLogic.querySORTESTByStudent(liveConn, pidm);

                assertEquals(0, sortest.size(), "query SORTEST after postELMTutorialResult with Banner down "
                                                + "returned wrong number of rows");

            } finally {
                liveLogin.checkInConnection(liveConn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while posting ELM tutorial result: " + ex.getMessage());
        } finally {
            LogicUtils.indicateBannerUp();
        }
    }

    // TODO: THE FOLLOWING TEST CASE INSERTS DATA INTO BANNER FOR PIDM '10567708' (Steve Benoit). IF YOU ENABLE THIS
    // TODO: TEST CASE AND RUN THE TEST, BE SURE TO MANUALLY CLEAN OUT ANY POSTED SCORES WITH THE TEST RUN DATE.

    /** Test case. */
    @Disabled("Disabled since this actually inserts into Banner - data will need manual cleanup when rum")
    @Test
    @DisplayName("postELMTutorialResult results with Banner up")
    void test0014() {

        final Integer pidm = Integer.valueOf(10567708);

        final LocalDateTime nowWithMs = LocalDateTime.now();
        final LocalDateTime now = LocalDateTime.of(nowWithMs.getYear(), nowWithMs.getMonth(), nowWithMs.getDayOfMonth(),
                nowWithMs.getHour(), nowWithMs.getMinute(), nowWithMs.getSecond());

        final Cache cache = new Cache(profile);
        LogicUtils.indicateBannerUp();

        final Login liveLogin = profile.getLogin(ESchema.LIVE);

        try {
            final DbConnection liveConn = liveLogin.checkOutConnection();
            try {
                RawMpscorequeueLogic.postELMTutorialResult(cache, liveConn, pidm, now);

                final List<RawMpscorequeue> all = RawMpscorequeueLogic.queryByPidm(cache, pidm);

                assertEquals(0, all.size(),
                        "query after postELMTutorialResult with Banner up returned wrong number of rows");

                final List<RawMpscorequeue> sortest = RawMpscorequeueLogic.querySORTESTByStudent(liveConn, pidm);

                assertEquals(1, sortest.size(),
                        "query SORTEST after postELMTutorialResult with Banner up returned wrong number of rows");

                final RawMpscorequeue rec = sortest.getFirst();

                assertEquals(pidm, rec.pidm, "Invalid PIDM after query");
                assertEquals(RawMpscorequeueLogic.MC00, rec.testCode, "Invalid test code after query");
                assertEquals(now, rec.testDate, "Invalid test date after query");
                assertEquals("2", rec.testScore, "Invalid test score after query");

            } finally {
                liveLogin.checkInConnection(liveConn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while posting ELM tutorial result: " + ex.getMessage());
        } finally {
            LogicUtils.indicateBannerUp();
        }
    }

    /** Test case. */
    @Test
    @DisplayName("postELMUnit3ReviewPassed results with Banner down")
    void test0015() {

        final Integer pidm = Integer.valueOf(10567708);

        final LocalDateTime nowWithMs = LocalDateTime.now();
        final LocalDateTime now = LocalDateTime.of(nowWithMs.getYear(), nowWithMs.getMonth(), nowWithMs.getDayOfMonth(),
                nowWithMs.getHour(), nowWithMs.getMinute(), nowWithMs.getSecond());

        final Cache cache = new Cache(profile);
        LogicUtils.indicateBannerDown();

        final Login liveLogin = profile.getLogin(ESchema.LIVE);


        try {
            final DbConnection liveConn = liveLogin.checkOutConnection();
            try {
                RawMpscorequeueLogic.postELMUnit3ReviewPassed(cache, liveConn, pidm, now);

                final List<RawMpscorequeue> all = RawMpscorequeueLogic.queryByPidm(cache, pidm);

                assertEquals(1, all.size(),
                        "query after postELMUnit3ReviewPassed with Banner down returned wrong number of rows");

                final RawMpscorequeue rec = all.getFirst();

                assertEquals(pidm, rec.pidm, "Invalid PIDM after query");
                assertEquals(RawMpscorequeueLogic.MC00, rec.testCode, "Invalid test code after query");
                assertEquals(now, rec.testDate, "Invalid test date after query");
                assertEquals("4", rec.testScore, "Invalid test score after query");

                assertTrue(RawMpscorequeueLogic.delete(cache, rec), "deleteMpscorequeue failed");

                final List<RawMpscorequeue> sortest = RawMpscorequeueLogic.querySORTESTByStudent(liveConn, pidm);

                assertEquals(0, sortest.size(), "query SORTEST after postELMUnit3ReviewPassed with Banner down "
                                                + "returned wrong number of rows");

            } finally {
                liveLogin.checkInConnection(liveConn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while posting ELM tutorial result: " + ex.getMessage());
        } finally {
            LogicUtils.indicateBannerUp();
        }
    }

    // TODO: THE FOLLOWING TEST CASE INSERTS DATA INTO BANNER FOR PIDM '10567708' (Steve Benoit). IF YOU ENABLE THIS
    // TODO: TEST CASE AND RUN THE TEST, BE SURE TO MANUALLY CLEAN OUT ANY POSTED SCORES WITH THE TEST RUN DATE.

    /** Test case. */
    @Disabled("Disabled since this actually inserts into Banner - data will need manual cleanup when rum")
    @Test
    @DisplayName("postELMUnit3ReviewPassed results with Banner up")
    void test0016() {

        final Integer pidm = Integer.valueOf(10567708);

        final LocalDateTime nowWithMs = LocalDateTime.now();
        final LocalDateTime now = LocalDateTime.of(nowWithMs.getYear(), nowWithMs.getMonth(), nowWithMs.getDayOfMonth(),
                nowWithMs.getHour(), nowWithMs.getMinute(), nowWithMs.getSecond());

        final Cache cache = new Cache(profile);

        final boolean origBannerDown = LogicUtils.isBannerDown();
        LogicUtils.indicateBannerUp();

        final Login liveLogin = profile.getLogin(ESchema.LIVE);

        try {
            final DbConnection liveConn = liveLogin.checkOutConnection();
            try {
                RawMpscorequeueLogic.postELMUnit3ReviewPassed(cache, liveConn, pidm, now);

                final List<RawMpscorequeue> all = RawMpscorequeueLogic.queryByPidm(cache, pidm);

                assertEquals(0, all.size(), "query after postELMUnit3ReviewPassed "
                                            + "with Banner up returned wrong number of rows");

                final List<RawMpscorequeue> sortest = RawMpscorequeueLogic.querySORTESTByStudent(liveConn, pidm);

                assertEquals(1, sortest.size(), "query SORTEST after postELMTutorialResult with Banner up "
                                                + "returned wrong number of rows");

                final RawMpscorequeue rec = sortest.getFirst();

                assertEquals(pidm, rec.pidm, "Invalid PIDM after query");
                assertEquals(RawMpscorequeueLogic.MC00, rec.testCode, "Invalid test code after query");
                assertEquals(now, rec.testDate, "Invalid test date after query");
                assertEquals("4", rec.testScore, "Invalid test score after query");

            } finally {
                liveLogin.checkInConnection(liveConn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while posting ELM review 3 result: " + ex.getMessage());
        } finally {
            if (origBannerDown) {
                LogicUtils.indicateBannerDown();
            }
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM mpscorequeue");
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

package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.logic.DbConnection;
import dev.mathops.db.logic.DbContext;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawrecord.RawSemesterCalendar;

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
import java.time.LocalDate;
import java.util.List;

/**
 * Tests for the {@code IvtRawSemesterCalendarLogic} class.
 */
final class TestRawSemesterCalendarLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 7, 1);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 7, 7);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 7, 8);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2021, 7, 14);

    /** A date used in test records. */
    private static final LocalDate date5 = LocalDate.of(2021, 7, 15);

    /** A date used in test records. */
    private static final LocalDate date6 = LocalDate.of(2021, 7, 21);

    /** A term key. */
    private static final TermKey fa21 = new TermKey(ETermName.FALL, 2023);

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
                    stmt.executeUpdate("DELETE FROM semester_calendar");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawSemesterCalendar raw1 = new RawSemesterCalendar(fa21, Integer.valueOf(1), date1, date2);
                final RawSemesterCalendar raw2 = new RawSemesterCalendar(fa21, Integer.valueOf(2), date3, date4);
                final RawSemesterCalendar raw3 = new RawSemesterCalendar(fa21, Integer.valueOf(3), date5, date6);

                assertTrue(RawSemesterCalendarLogic.INSTANCE.insert(cache, raw1), "Failed to insert semester_calendar");
                assertTrue(RawSemesterCalendarLogic.INSTANCE.insert(cache, raw2), "Failed to insert semester_calendar");
                assertTrue(RawSemesterCalendarLogic.INSTANCE.insert(cache, raw3), "Failed to insert semester_calendar");
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
                final List<RawSemesterCalendar> all = RawSemesterCalendarLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawSemesterCalendar r : all) {

                    if (fa21.equals(r.termKey) && Integer.valueOf(1).equals(r.weekNbr)
                            && date1.equals(r.startDt)
                            && date2.equals(r.endDt)) {
                        found1 = true;

                    } else if (fa21.equals(r.termKey) && Integer.valueOf(2).equals(r.weekNbr)
                            && date3.equals(r.startDt)
                            && date4.equals(r.endDt)) {
                        found2 = true;

                    } else if (fa21.equals(r.termKey) && Integer.valueOf(3).equals(r.weekNbr)
                            && date5.equals(r.startDt)
                            && date6.equals(r.endDt)) {
                        found3 = true;

                    } else {
                        Log.warning("Unexpected termKey ", r.termKey);
                        Log.warning("Unexpected weekNbr ", r.weekNbr);
                        Log.warning("Unexpected startDt ", r.startDt);
                        Log.warning("Unexpected endDt ", r.endDt);
                    }
                }

                assertTrue(found1, "semester_calendar 1 not found");
                assertTrue(found2, "semester_calendar 2 not found");
                assertTrue(found3, "semester_calendar 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all semester_calendar rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawSemesterCalendar raw2 = new RawSemesterCalendar(fa21, Integer.valueOf(2),
                        date3, date4);

                final boolean result = RawSemesterCalendarLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawSemesterCalendar> all = RawSemesterCalendarLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;

                for (final RawSemesterCalendar r : all) {

                    if (fa21.equals(r.termKey) && Integer.valueOf(1).equals(r.weekNbr)
                            && date1.equals(r.startDt)
                            && date2.equals(r.endDt)) {
                        found1 = true;

                    } else if (fa21.equals(r.termKey) && Integer.valueOf(3).equals(r.weekNbr)
                            && date5.equals(r.startDt)
                            && date6.equals(r.endDt)) {
                        found3 = true;

                    } else {
                        Log.warning("Unexpected termKey ", r.termKey);
                        Log.warning("Unexpected weekNbr ", r.weekNbr);
                        Log.warning("Unexpected startDt ", r.startDt);
                        Log.warning("Unexpected endDt ", r.endDt);
                    }
                }

                assertTrue(found1, "semester_calendar 1 not found");
                assertTrue(found3, "semester_calendar 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting semester_calendar: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM semester_calendar");
                }

                conn.commit();

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning tables: " + ex.getMessage());
        }
    }
}

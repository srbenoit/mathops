package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;

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
 * Tests for the {@code IvtRawCampusCalendarLogic} class.
 */
final class TestRawCampusCalendarLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 2, 3);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 3, 4);

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
                    stmt.executeUpdate("DELETE FROM campus_calendar");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawCampusCalendar raw1 = new RawCampusCalendar(date1, RawCampusCalendar.DT_DESC_BOOKSTORE,
                        "9:00 am", "6:30 pm", "4:00 pm", "9:00 pm", "M-F", "M-R");

                final RawCampusCalendar raw2 = new RawCampusCalendar(date2, RawCampusCalendar.DT_DESC_HOLIDAY,
                        "9:01 am", "6:31 pm", "4:01 pm", "9:01 pm", "Mon-Fri", "Mon-Thu");

                final RawCampusCalendar raw3 = new RawCampusCalendar(date3, RawCampusCalendar.DT_DESC_HOLIDAY,
                        "9:02 am", "6:32 pm", "4:02 pm", "9:02 pm", "Monday-Friday", "Monday-Thursday");

                assertTrue(RawCampusCalendarLogic.INSTANCE.insert(cache, raw1), "Failed to insert campus_calendar");
                assertTrue(RawCampusCalendarLogic.INSTANCE.insert(cache, raw2), "Failed to insert campus_calendar");
                assertTrue(RawCampusCalendarLogic.INSTANCE.insert(cache, raw3), "Failed to insert campus_calendar");
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
                final List<RawCampusCalendar> all = RawCampusCalendarLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawCampusCalendar r : all) {

                    if (date1.equals(r.campusDt)
                            && RawCampusCalendar.DT_DESC_BOOKSTORE.equals(r.dtDesc)
                            && "9:00 am".equals(r.openTime1)
                            && "6:30 pm".equals(r.openTime2)
                            && "4:00 pm".equals(r.closeTime1)
                            && "9:00 pm".equals(r.closeTime2)
                            && "M-F".equals(r.weekdays1)
                            && "M-R".equals(r.weekdays2)) {
                        found1 = true;

                    } else if (date2.equals(r.campusDt)
                            && RawCampusCalendar.DT_DESC_HOLIDAY.equals(r.dtDesc)
                            && "9:01 am".equals(r.openTime1)
                            && "6:31 pm".equals(r.openTime2)
                            && "4:01 pm".equals(r.closeTime1)
                            && "9:01 pm".equals(r.closeTime2)
                            && "Mon-Fri".equals(r.weekdays1)
                            && "Mon-Thu".equals(r.weekdays2)) {
                        found2 = true;

                    } else if (date3.equals(r.campusDt)
                            && RawCampusCalendar.DT_DESC_HOLIDAY.equals(r.dtDesc)
                            && "9:02 am".equals(r.openTime1)
                            && "6:32 pm".equals(r.openTime2)
                            && "4:02 pm".equals(r.closeTime1)
                            && "9:02 pm".equals(r.closeTime2)
                            && "Monday-Friday".equals(r.weekdays1)
                            && "Monday-Thursday".equals(r.weekdays2)) {
                        found3 = true;

                    } else {
                        Log.warning("Unexpected campus_dt ", r.campusDt);
                        Log.warning("Unexpected dtDesc ", r.dtDesc);
                        Log.warning("Unexpected openTime1 ", r.openTime1);
                        Log.warning("Unexpected openTime2 ", r.openTime2);
                        Log.warning("Unexpected closeTime1 ", r.closeTime1);
                        Log.warning("Unexpected closeTime2 ", r.closeTime2);
                        Log.warning("Unexpected weekdays1 ", r.weekdays1);
                        Log.warning("Unexpected weekdays2 ", r.weekdays2);
                    }
                }

                assertTrue(found1, "campus_calendar 1 not found");
                assertTrue(found2, "campus_calendar 2 not found");
                assertTrue(found3, "campus_calendar 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all campus_calendar rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByType results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawCampusCalendar> tc2 =
                        RawCampusCalendarLogic.queryByType(cache, RawCampusCalendar.DT_DESC_HOLIDAY);

                assertEquals(2, tc2.size(), "Incorrect record count from queryByType");

                boolean found2 = false;
                boolean found3 = false;

                for (final RawCampusCalendar r : tc2) {

                    if (date2.equals(r.campusDt)
                            && RawCampusCalendar.DT_DESC_HOLIDAY.equals(r.dtDesc)
                            && "9:01 am".equals(r.openTime1)
                            && "6:31 pm".equals(r.openTime2)
                            && "4:01 pm".equals(r.closeTime1)
                            && "9:01 pm".equals(r.closeTime2)
                            && "Mon-Fri".equals(r.weekdays1)
                            && "Mon-Thu".equals(r.weekdays2)) {
                        found2 = true;

                    } else if (date3.equals(r.campusDt)
                            && RawCampusCalendar.DT_DESC_HOLIDAY.equals(r.dtDesc)
                            && "9:02 am".equals(r.openTime1)
                            && "6:32 pm".equals(r.openTime2)
                            && "4:02 pm".equals(r.closeTime1)
                            && "9:02 pm".equals(r.closeTime2)
                            && "Monday-Friday".equals(r.weekdays1)
                            && "Monday-Thursday".equals(r.weekdays2)) {
                        found3 = true;

                    } else {
                        Log.warning("Unexpected campus_dt ", r.campusDt);
                        Log.warning("Unexpected dtDesc ", r.dtDesc);
                        Log.warning("Unexpected openTime1 ", r.openTime1);
                        Log.warning("Unexpected openTime2 ", r.openTime2);
                        Log.warning("Unexpected closeTime1 ", r.closeTime1);
                        Log.warning("Unexpected closeTime2 ", r.closeTime2);
                        Log.warning("Unexpected weekdays1 ", r.weekdays1);
                        Log.warning("Unexpected weekdays2 ", r.weekdays2);
                    }
                }

                assertTrue(found2, "campus_calendar 2 not found");
                assertTrue(found3, "campus_calendar 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all campus_calendar rows of type: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawCampusCalendar raw2 = new RawCampusCalendar(date2, RawCampusCalendar.DT_DESC_HOLIDAY,
                        "9:01 am", "6:31 pm", "4:01 pm", "9:01 pm", "Mon-Fri", "Mon-Thu");

                final boolean result = RawCampusCalendarLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawCampusCalendar> all = RawCampusCalendarLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;

                for (final RawCampusCalendar r : all) {

                    if (date1.equals(r.campusDt)
                            && RawCampusCalendar.DT_DESC_BOOKSTORE.equals(r.dtDesc)
                            && "9:00 am".equals(r.openTime1)
                            && "6:30 pm".equals(r.openTime2)
                            && "4:00 pm".equals(r.closeTime1)
                            && "9:00 pm".equals(r.closeTime2)
                            && "M-F".equals(r.weekdays1)
                            && "M-R".equals(r.weekdays2)) {
                        found1 = true;

                    } else if (date3.equals(r.campusDt)
                            && RawCampusCalendar.DT_DESC_HOLIDAY.equals(r.dtDesc)
                            && "9:02 am".equals(r.openTime1)
                            && "6:32 pm".equals(r.openTime2)
                            && "4:02 pm".equals(r.closeTime1)
                            && "9:02 pm".equals(r.closeTime2)
                            && "Monday-Friday".equals(r.weekdays1)
                            && "Monday-Thursday".equals(r.weekdays2)) {
                        found3 = true;

                    } else {
                        Log.warning("Unexpected campus_dt ", r.campusDt);
                        Log.warning("Unexpected dtDesc ", r.dtDesc);
                        Log.warning("Unexpected openTime1 ", r.openTime1);
                        Log.warning("Unexpected openTime2 ", r.openTime2);
                        Log.warning("Unexpected closeTime1 ", r.closeTime1);
                        Log.warning("Unexpected closeTime2 ", r.closeTime2);
                        Log.warning("Unexpected weekdays1 ", r.weekdays1);
                        Log.warning("Unexpected weekdays2 ", r.weekdays2);
                    }
                }

                assertTrue(found1, "campus_calendar 1 not found");
                assertTrue(found3, "campus_calendar 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting campus_calendar: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM campus_calendar");
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

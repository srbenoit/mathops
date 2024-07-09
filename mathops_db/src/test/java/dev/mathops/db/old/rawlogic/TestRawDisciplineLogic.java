package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawDiscipline;
import dev.mathops.db.old.rawrecord.RawRecordConstants;

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
 * Tests for the {@code RawDisciplineLogic} classes.
 */
final class TestRawDisciplineLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(1995, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(1996, 3, 4);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(1997, 5, 6);

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
                    stmt.executeUpdate("DELETE FROM discipline");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawDiscipline raw1 = new RawDiscipline("888888881", date1, "01", RawRecordConstants.M117,
                        Integer.valueOf(1), "Crib sheet", "99", "Cake or death - chose cake", "Benoit", "John Doe");

                final RawDiscipline raw2 = new RawDiscipline("888888882", date2, "02", RawRecordConstants.M118,
                        Integer.valueOf(2), "Phone", "98", "Cake or death - chose death", "Pattison", "Jane Doe");

                final RawDiscipline raw3 = new RawDiscipline("888888883", date3, "03", RawRecordConstants.M124,
                        Integer.valueOf(3), "Emotional support nerd", "97", "Cake or death - chose both", "Bromley",
                        "Juan Doe");

                assertTrue(RawDisciplineLogic.INSTANCE.insert(cache, raw1), "Failed to insert discipline");
                assertTrue(RawDisciplineLogic.INSTANCE.insert(cache, raw2), "Failed to insert discipline");
                assertTrue(RawDisciplineLogic.INSTANCE.insert(cache, raw3), "Failed to insert discipline");
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
                final List<RawDiscipline> all = RawDisciplineLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawDiscipline test : all) {

                    if ("888888881".equals(test.stuId)
                            && date1.equals(test.dtIncident)
                            && "01".equals(test.incidentType)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "Crib sheet".equals(test.cheatDesc)
                            && "99".equals(test.actionType)
                            && "Cake or death - chose cake".equals(test.actionComment)
                            && "Benoit".equals(test.interviewer)
                            && "John Doe".equals(test.proctor)) {
                        found1 = true;
                    } else if ("888888882".equals(test.stuId)
                            && date2.equals(test.dtIncident)
                            && "02".equals(test.incidentType)
                            && RawRecordConstants.M118.equals(test.course)
                            && Integer.valueOf(2).equals(test.unit)
                            && "Phone".equals(test.cheatDesc)
                            && "98".equals(test.actionType)
                            && "Cake or death - chose death".equals(test.actionComment)
                            && "Pattison".equals(test.interviewer)
                            && "Jane Doe".equals(test.proctor)) {
                        found2 = true;
                    } else if ("888888883".equals(test.stuId)
                            && date3.equals(test.dtIncident)
                            && "03".equals(test.incidentType)
                            && RawRecordConstants.M124.equals(test.course)
                            && Integer.valueOf(3).equals(test.unit)
                            && "Emotional support nerd".equals(test.cheatDesc)
                            && "97".equals(test.actionType)
                            && "Cake or death - chose both".equals(test.actionComment)
                            && "Bromley".equals(test.interviewer)
                            && "Juan Doe".equals(test.proctor)) {
                        found3 = true;
                    } else {
                        Log.warning("Unexpected discipline: ", test);
                    }
                }

                assertTrue(found1, "Discipline for 881 not found");
                assertTrue(found2, "Discipline for 882 not found");
                assertTrue(found3, "Discipline for 883 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all discipline rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawDiscipline> all = RawDisciplineLogic.queryByStudent(cache, "888888881");

                assertEquals(1, all.size(), "Incorrect record count from queryByStudent");

                boolean found1 = false;

                for (final RawDiscipline test : all) {

                    if ("888888881".equals(test.stuId)
                            && date1.equals(test.dtIncident)
                            && "01".equals(test.incidentType)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && "Crib sheet".equals(test.cheatDesc)
                            && "99".equals(test.actionType)
                            && "Cake or death - chose cake".equals(test.actionComment)
                            && "Benoit".equals(test.interviewer)
                            && "John Doe".equals(test.proctor)) {
                        found1 = true;
                        break;
                    }
                }

                assertTrue(found1, "Discipline for 881 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying single discipline rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0006() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawDiscipline toDelete = new RawDiscipline("888888881", date1, "01", RawRecordConstants.M117,
                        Integer.valueOf(1), null, null, null, null, null);

                assertTrue(RawDisciplineLogic.INSTANCE.delete(cache, toDelete), "Delete discipline failed");

                final List<RawDiscipline> all = RawDisciplineLogic.queryByStudent(cache, "888888881");

                assertEquals(0, all.size(), "Incorrect record count from queryByStudent");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting discipline rows: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM discipline");
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

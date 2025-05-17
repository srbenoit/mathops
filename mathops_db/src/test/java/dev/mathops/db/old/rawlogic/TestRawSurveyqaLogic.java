package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawrecord.RawSurveyqa;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawSurveyqa} classes.
 */
final class TestRawSurveyqaLogic {

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
                stmt.executeUpdate("DELETE FROM surveyqa");
                stmt.executeUpdate("DELETE FROM term");
            }
            conn.commit();

            final TermKey termKey = new TermKey(ETermName.SPRING, 2022);

            final TermRec rawTerm = new TermRec(termKey, LocalDate.of(2022, 1, 1), LocalDate.of(2022, 5, 6), "2122",
                    Integer.valueOf(0), LocalDate.of(2021, 11, 13), LocalDate.of(2021, 11, 14),
                    LocalDate.of(2022, 10, 12));

            assertTrue(cache.getSystemData().insertTerm(rawTerm), "Failed to insert active term");

            final RawSurveyqa raw1 = new RawSurveyqa(termKey, "POOOO", Integer.valueOf(1), "Time spent preparing",
                    null, "1", "None at all", null, null, null);

            final RawSurveyqa raw2 = new RawSurveyqa(termKey, "POOOO", Integer.valueOf(1), "Time spent preparing",
                    null, "2", "Less than 2 hours", null, null, null);

            final RawSurveyqa raw3 = new RawSurveyqa(termKey, "POOOO", Integer.valueOf(2),
                    "Resources used to prepare", null, "8", "Web site study guide", null, null, null);

            final RawSurveyqa raw4 = new RawSurveyqa(termKey, "POOOO", Integer.valueOf(2),
                    "Resources used to prepare", null, "4", "Tutoring", null, null, null);

            final RawSurveyqa raw5 = new RawSurveyqa(termKey, "UOOOO", Integer.valueOf(1),
                    "Expected Calculus Course", "CC", "A", "M 160", "E", "Y", null);

            assertTrue(RawSurveyqaLogic.insert(cache, raw1), "Failed to insert surveyqa 1");
            assertTrue(RawSurveyqaLogic.insert(cache, raw2), "Failed to insert surveyqa 2");
            assertTrue(RawSurveyqaLogic.insert(cache, raw3), "Failed to insert surveyqa 3");
            assertTrue(RawSurveyqaLogic.insert(cache, raw4), "Failed to insert surveyqa 4");
            assertTrue(RawSurveyqaLogic.insert(cache, raw5), "Failed to insert surveyqa 5");
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
            final List<RawSurveyqa> all = RawSurveyqaLogic.queryAll(cache);

            assertEquals(5, all.size(), "Incorrect record count from queryAll");
            final TermKey termKey = new TermKey(ETermName.SPRING, 2022);

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final RawSurveyqa test : all) {

                if (termKey.equals(test.termKey)
                    && "POOOO".equals(test.version)
                    && Integer.valueOf(1).equals(test.surveyNbr)
                    && "Time spent preparing".equals(test.questionDesc)
                    && test.typeQuestion == null
                    && "1".equals(test.answer)
                    && "None at all".equals(test.answerDesc)
                    && test.answerMeaning == null
                    && test.mustAnswer == null
                    && test.treeRef == null) {

                    found1 = true;
                } else if (termKey.equals(test.termKey)
                           && "POOOO".equals(test.version)
                           && Integer.valueOf(1).equals(test.surveyNbr)
                           && "Time spent preparing".equals(test.questionDesc)
                           && test.typeQuestion == null
                           && "2".equals(test.answer)
                           && "Less than 2 hours".equals(test.answerDesc)
                           && test.answerMeaning == null
                           && test.mustAnswer == null
                           && test.treeRef == null) {

                    found2 = true;
                } else if (termKey.equals(test.termKey)
                           && "POOOO".equals(test.version)
                           && Integer.valueOf(2).equals(test.surveyNbr)
                           && "Resources used to prepare".equals(test.questionDesc)
                           && test.typeQuestion == null
                           && "8".equals(test.answer)
                           && "Web site study guide".equals(test.answerDesc)
                           && test.answerMeaning == null
                           && test.mustAnswer == null
                           && test.treeRef == null) {

                    found3 = true;
                } else if (termKey.equals(test.termKey)
                           && "POOOO".equals(test.version)
                           && Integer.valueOf(2).equals(test.surveyNbr)
                           && "Resources used to prepare".equals(test.questionDesc)
                           && test.typeQuestion == null
                           && "4".equals(test.answer)
                           && "Tutoring".equals(test.answerDesc)
                           && test.answerMeaning == null
                           && test.mustAnswer == null
                           && test.treeRef == null) {

                    found4 = true;
                } else if (termKey.equals(test.termKey)
                           && "UOOOO".equals(test.version)
                           && Integer.valueOf(1).equals(test.surveyNbr)
                           && "Expected Calculus Course".equals(test.questionDesc)
                           && "CC".equals(test.typeQuestion)
                           && "A".equals(test.answer)
                           && "M 160".equals(test.answerDesc)
                           && "E".equals(test.answerMeaning)
                           && "Y".equals(test.mustAnswer)
                           && test.treeRef == null) {

                    found5 = true;

                } else {
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected surveyNbr ", test.surveyNbr);
                    Log.warning("Unexpected questionDesc ", test.questionDesc);
                    Log.warning("Unexpected typeQuestion ", test.typeQuestion);
                    Log.warning("Unexpected answer ", test.answer);
                    Log.warning("Unexpected answerDesc ", test.answerDesc);
                    Log.warning("Unexpected answerMeaning ", test.answerMeaning);
                    Log.warning("Unexpected mustAnswer ", test.mustAnswer);
                    Log.warning("Unexpected treeRef ", test.treeRef);
                }
            }

            assertTrue(found1, "Surveyqa 1 not found");
            assertTrue(found2, "Surveyqa 2 not found");
            assertTrue(found3, "Surveyqa 3 not found");
            assertTrue(found4, "Surveyqa 4 not found");
            assertTrue(found5, "Surveyqa 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all surveyqa rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByVersion results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSurveyqa> all = RawSurveyqaLogic.queryByVersion(cache, "POOOO");

            assertEquals(4, all.size(), "Incorrect record count from queryByVersion");
            final TermKey termKey = new TermKey(ETermName.SPRING, 2022);

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final RawSurveyqa test : all) {

                if (termKey.equals(test.termKey)
                    && "POOOO".equals(test.version)
                    && Integer.valueOf(1).equals(test.surveyNbr)
                    && "Time spent preparing".equals(test.questionDesc)
                    && test.typeQuestion == null
                    && "1".equals(test.answer)
                    && "None at all".equals(test.answerDesc)
                    && test.answerMeaning == null
                    && test.mustAnswer == null
                    && test.treeRef == null) {

                    found1 = true;
                } else if (termKey.equals(test.termKey)
                           && "POOOO".equals(test.version)
                           && Integer.valueOf(1).equals(test.surveyNbr)
                           && "Time spent preparing".equals(test.questionDesc)
                           && test.typeQuestion == null
                           && "2".equals(test.answer)
                           && "Less than 2 hours".equals(test.answerDesc)
                           && test.answerMeaning == null
                           && test.mustAnswer == null
                           && test.treeRef == null) {

                    found2 = true;
                } else if (termKey.equals(test.termKey)
                           && "POOOO".equals(test.version)
                           && Integer.valueOf(2).equals(test.surveyNbr)
                           && "Resources used to prepare".equals(test.questionDesc)
                           && test.typeQuestion == null
                           && "8".equals(test.answer)
                           && "Web site study guide".equals(test.answerDesc)
                           && test.answerMeaning == null
                           && test.mustAnswer == null
                           && test.treeRef == null) {

                    found3 = true;
                } else if (termKey.equals(test.termKey)
                           && "POOOO".equals(test.version)
                           && Integer.valueOf(2).equals(test.surveyNbr)
                           && "Resources used to prepare".equals(test.questionDesc)
                           && test.typeQuestion == null
                           && "4".equals(test.answer)
                           && "Tutoring".equals(test.answerDesc)
                           && test.answerMeaning == null
                           && test.mustAnswer == null
                           && test.treeRef == null) {

                    found4 = true;

                } else {
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected surveyNbr ", test.surveyNbr);
                    Log.warning("Unexpected questionDesc ", test.questionDesc);
                    Log.warning("Unexpected typeQuestion ", test.typeQuestion);
                    Log.warning("Unexpected answer ", test.answer);
                    Log.warning("Unexpected answerDesc ", test.answerDesc);
                    Log.warning("Unexpected answerMeaning ", test.answerMeaning);
                    Log.warning("Unexpected mustAnswer ", test.mustAnswer);
                    Log.warning("Unexpected treeRef ", test.treeRef);
                }
            }

            assertTrue(found1, "Surveyqa 1 not found");
            assertTrue(found2, "Surveyqa 2 not found");
            assertTrue(found3, "Surveyqa 3 not found");
            assertTrue(found4, "Surveyqa 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all surveyqa by version: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryUniqueQuestionsByVersion results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSurveyqa> all =
                    RawSurveyqaLogic.queryUniqueQuestionsByVersion(cache, "POOOO");

            assertEquals(2, all.size(), "Incorrect record count from queryUniqueQuestionsByVersion");
            final TermKey termKey = new TermKey(ETermName.SPRING, 2022);

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final RawSurveyqa test : all) {

                if (termKey.equals(test.termKey)
                    && "POOOO".equals(test.version)
                    && Integer.valueOf(1).equals(test.surveyNbr)
                    && "Time spent preparing".equals(test.questionDesc)
                    && test.typeQuestion == null
                    && "1".equals(test.answer)
                    && "None at all".equals(test.answerDesc)
                    && test.answerMeaning == null
                    && test.mustAnswer == null
                    && test.treeRef == null) {

                    found1 = true;
                } else if (termKey.equals(test.termKey)
                           && "POOOO".equals(test.version)
                           && Integer.valueOf(1).equals(test.surveyNbr)
                           && "Time spent preparing".equals(test.questionDesc)
                           && test.typeQuestion == null
                           && "2".equals(test.answer)
                           && "Less than 2 hours".equals(test.answerDesc)
                           && test.answerMeaning == null
                           && test.mustAnswer == null
                           && test.treeRef == null) {

                    found2 = true;
                } else if (termKey.equals(test.termKey)
                           && "POOOO".equals(test.version)
                           && Integer.valueOf(2).equals(test.surveyNbr)
                           && "Resources used to prepare".equals(test.questionDesc)
                           && test.typeQuestion == null
                           && "8".equals(test.answer)
                           && "Web site study guide".equals(test.answerDesc)
                           && test.answerMeaning == null
                           && test.mustAnswer == null
                           && test.treeRef == null) {

                    found3 = true;
                } else if (termKey.equals(test.termKey)
                           && "POOOO".equals(test.version)
                           && Integer.valueOf(2).equals(test.surveyNbr)
                           && "Resources used to prepare".equals(test.questionDesc)
                           && test.typeQuestion == null
                           && "4".equals(test.answer)
                           && "Tutoring".equals(test.answerDesc)
                           && test.answerMeaning == null
                           && test.mustAnswer == null
                           && test.treeRef == null) {

                    found4 = true;

                } else {
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected surveyNbr ", test.surveyNbr);
                    Log.warning("Unexpected questionDesc ", test.questionDesc);
                    Log.warning("Unexpected typeQuestion ", test.typeQuestion);
                    Log.warning("Unexpected answer ", test.answer);
                    Log.warning("Unexpected answerDesc ", test.answerDesc);
                    Log.warning("Unexpected answerMeaning ", test.answerMeaning);
                    Log.warning("Unexpected mustAnswer ", test.mustAnswer);
                    Log.warning("Unexpected treeRef ", test.treeRef);
                }
            }

            assertTrue(found1 || found2, "Surveyqa 1/2 not found");
            assertTrue(found3 || found4, "Surveyqa 3/4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all unique surveyqa questions by version: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByVersionAndQuestion results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSurveyqa> all = RawSurveyqaLogic.queryByVersionAndQuestion(cache, "POOOO",
                    Integer.valueOf(1));

            assertEquals(2, all.size(), "Incorrect record count from queryByVersionAndQuestion");
            final TermKey termKey = new TermKey(ETermName.SPRING, 2022);

            boolean found1 = false;
            boolean found2 = false;

            for (final RawSurveyqa test : all) {

                if (termKey.equals(test.termKey)
                    && "POOOO".equals(test.version)
                    && Integer.valueOf(1).equals(test.surveyNbr)
                    && "Time spent preparing".equals(test.questionDesc)
                    && test.typeQuestion == null
                    && "1".equals(test.answer)
                    && "None at all".equals(test.answerDesc)
                    && test.answerMeaning == null
                    && test.mustAnswer == null
                    && test.treeRef == null) {

                    found1 = true;
                } else if (termKey.equals(test.termKey)
                           && "POOOO".equals(test.version)
                           && Integer.valueOf(1).equals(test.surveyNbr)
                           && "Time spent preparing".equals(test.questionDesc)
                           && test.typeQuestion == null
                           && "2".equals(test.answer)
                           && "Less than 2 hours".equals(test.answerDesc)
                           && test.answerMeaning == null
                           && test.mustAnswer == null
                           && test.treeRef == null) {

                    found2 = true;
                } else {
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected surveyNbr ", test.surveyNbr);
                    Log.warning("Unexpected questionDesc ", test.questionDesc);
                    Log.warning("Unexpected typeQuestion ", test.typeQuestion);
                    Log.warning("Unexpected answer ", test.answer);
                    Log.warning("Unexpected answerDesc ", test.answerDesc);
                    Log.warning("Unexpected answerMeaning ", test.answerMeaning);
                    Log.warning("Unexpected mustAnswer ", test.mustAnswer);
                    Log.warning("Unexpected treeRef ", test.treeRef);
                }
            }

            assertTrue(found1, "Surveyqa 1 not found");
            assertTrue(found2, "Surveyqa 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all surveyqa by question: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0007() {

        final Cache cache = new Cache(profile);

        try {
            final TermKey termKey = new TermKey(ETermName.SPRING, 2022);
            final RawSurveyqa raw2 = new RawSurveyqa(termKey, "POOOO", Integer.valueOf(1), "Time spent preparing",
                    null, "2", "Less than 2 hours", null, null, null);

            final boolean result = RawSurveyqaLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawSurveyqa> all = RawSurveyqaLogic.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final RawSurveyqa test : all) {

                if (termKey.equals(test.termKey)
                    && "POOOO".equals(test.version)
                    && Integer.valueOf(1).equals(test.surveyNbr)
                    && "Time spent preparing".equals(test.questionDesc)
                    && test.typeQuestion == null
                    && "1".equals(test.answer)
                    && "None at all".equals(test.answerDesc)
                    && test.answerMeaning == null
                    && test.mustAnswer == null
                    && test.treeRef == null) {

                    found1 = true;
                } else if (termKey.equals(test.termKey)
                           && "POOOO".equals(test.version)
                           && Integer.valueOf(2).equals(test.surveyNbr)
                           && "Resources used to prepare".equals(test.questionDesc)
                           && test.typeQuestion == null
                           && "8".equals(test.answer)
                           && "Web site study guide".equals(test.answerDesc)
                           && test.answerMeaning == null
                           && test.mustAnswer == null
                           && test.treeRef == null) {

                    found3 = true;
                } else if (termKey.equals(test.termKey)
                           && "POOOO".equals(test.version)
                           && Integer.valueOf(2).equals(test.surveyNbr)
                           && "Resources used to prepare".equals(test.questionDesc)
                           && test.typeQuestion == null
                           && "4".equals(test.answer)
                           && "Tutoring".equals(test.answerDesc)
                           && test.answerMeaning == null
                           && test.mustAnswer == null
                           && test.treeRef == null) {

                    found4 = true;
                } else if (termKey.equals(test.termKey)
                           && "UOOOO".equals(test.version)
                           && Integer.valueOf(1).equals(test.surveyNbr)
                           && "Expected Calculus Course".equals(test.questionDesc)
                           && "CC".equals(test.typeQuestion)
                           && "A".equals(test.answer)
                           && "M 160".equals(test.answerDesc)
                           && "E".equals(test.answerMeaning)
                           && "Y".equals(test.mustAnswer)
                           && test.treeRef == null) {

                    found5 = true;

                } else {
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected version ", test.version);
                    Log.warning("Unexpected surveyNbr ", test.surveyNbr);
                    Log.warning("Unexpected questionDesc ", test.questionDesc);
                    Log.warning("Unexpected typeQuestion ", test.typeQuestion);
                    Log.warning("Unexpected answer ", test.answer);
                    Log.warning("Unexpected answerDesc ", test.answerDesc);
                    Log.warning("Unexpected answerMeaning ", test.answerMeaning);
                    Log.warning("Unexpected mustAnswer ", test.mustAnswer);
                    Log.warning("Unexpected treeRef ", test.treeRef);
                }
            }

            assertTrue(found1, "Surveyqa 1 not found");
            assertTrue(found3, "Surveyqa 3 not found");
            assertTrue(found4, "Surveyqa 4 not found");
            assertTrue(found5, "Surveyqa 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting surveyqa: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM surveyqa");
                stmt.executeUpdate("DELETE FROM term");
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

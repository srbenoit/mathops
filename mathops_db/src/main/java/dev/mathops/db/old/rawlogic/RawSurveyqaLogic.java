package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawSurveyqa;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A utility class to look up surveyqa by student, create new hold records, and delete hold records.
 *
 * <pre>
 * Table:  'surveyqa'
 *
 * Column name          Type              Nulls   Key
 * -------------------  ----------------  ------  -----
 * term                 char(2)           no      PK
 * term_yr              smallint          no      PK
 * version              char(5)           no      PK
 * survey_nbr           smallint          no      PK
 * question_desc        char(30)          no
 * type_question        char(6)           yes
 * answer               char(5)           no      PK
 * answer_desc          char(30)          no
 * answer_meaning       char(6)           yes
 * must_answer          char(1)           yes
 * tree_ref             char(40)          yes
 * </pre>
 */
public enum RawSurveyqaLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawSurveyqa record) throws SQLException {

        if (record.termKey == null || record.version == null || record.surveyNbr == null
                || record.questionDesc == null || record.answer == null || record.answerDesc == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO surveyqa (term,term_yr,version,survey_nbr,question_desc,type_question,answer,",
                "answer_desc,answer_meaning,must_answer,tree_ref) VALUES (",
                LogicUtils.sqlStringValue(record.termKey.termCode), ",",
                LogicUtils.sqlIntegerValue(record.termKey.shortYear), ",",
                LogicUtils.sqlStringValue(record.version), ",",
                LogicUtils.sqlIntegerValue(record.surveyNbr), ",",
                LogicUtils.sqlStringValue(record.questionDesc), ",",
                LogicUtils.sqlStringValue(record.typeQuestion), ",",
                LogicUtils.sqlStringValue(record.answer), ",",
                LogicUtils.sqlStringValue(record.answerDesc), ",",
                LogicUtils.sqlStringValue(record.answerMeaning), ",",
                LogicUtils.sqlStringValue(record.mustAnswer), ",",
                LogicUtils.sqlStringValue(record.treeRef), ")");

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement s = conn.createStatement()) {
            final boolean result = s.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }

            return result;
        } finally {
            Cache.checkInConnection(conn);
        }
    }

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean delete(final Cache cache, final RawSurveyqa record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM surveyqa ",
                "WHERE term=", LogicUtils.sqlStringValue(record.termKey.termCode),
                "  AND term_yr=", LogicUtils.sqlIntegerValue(record.termKey.shortYear),
                "  AND version=", LogicUtils.sqlStringValue(record.version),
                "  AND survey_nbr=", LogicUtils.sqlIntegerValue(record.surveyNbr),
                "  AND answer=", LogicUtils.sqlStringValue(record.answer));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }

            return result;
        } finally {
            Cache.checkInConnection(conn);
        }
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawSurveyqa> queryAll(final Cache cache) throws SQLException {

        return executeQuery(cache, "SELECT * FROM surveyqa");
    }

    /**
     * Gets all questions in a term. WARNING: This method will return one row for every possible answer to a  question,
     * so if you want just the questions, the list needs to be filtered to eliminate duplicates.
     *
     * @param cache   the data cache
     * @param termKey the term key whose questions to retrieve
     * @return the list of models that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawSurveyqa> queryByTerm(final Cache cache, final TermKey termKey) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM surveyqa WHERE term=", LogicUtils.sqlStringValue(termKey.termCode),
                "   AND term_yr=", LogicUtils.sqlIntegerValue(termKey.shortYear));

        return executeQuery(cache, sql);
    }

    /**
     * Gets the questions for a profile. WARNING: This method will return one row for every possible answer to a
     * question, so if you want just the questions, the list needs to be filtered to eliminate duplicates.
     *
     * @param cache      the data cache
     * @param theVersion the profile ID whose questions to retrieve
     * @return the list of models that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawSurveyqa> queryByVersion(final Cache cache, final String theVersion) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM surveyqa WHERE version=", LogicUtils.sqlStringValue(theVersion),
                "   AND term=", LogicUtils.sqlStringValue(active.term.termCode),
                "   AND term_yr=", LogicUtils.sqlIntegerValue(active.term.shortYear));

        return executeQuery(cache, sql);
    }

    /**
     * Gets all questions for a survey, with one record per question (that record will have answer-related fields from
     * an arbitrary record). Results are ordered by question number.
     *
     * @param cache      the data cache
     * @param theVersion the profile ID whose questions to retrieve
     * @return the list of models that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawSurveyqa> queryUniqueQuestionsByVersion(final Cache cache,
                                                                  final String theVersion) throws SQLException {

        final List<RawSurveyqa> all = queryByVersion(cache, theVersion);

        final Map<Integer, RawSurveyqa> map = new TreeMap<>();
        for (final RawSurveyqa record : all) {
            map.put(record.surveyNbr, record);
        }

        return new ArrayList<>(map.values());
    }

    /**
     * Gets the questions for a profile. WARNING: This method will return one row for every possible answer to a
     * question, so if you want just the questions, the list needs to be filtered to eliminate duplicates.
     *
     * @param cache        the data cache
     * @param theVersion   the profile ID whose questions to retrieve
     * @param theSurveyNbr the question number
     * @return the list of models that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawSurveyqa> queryByVersionAndQuestion(final Cache cache, final String theVersion,
                                                              final Integer theSurveyNbr) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM surveyqa WHERE version=", LogicUtils.sqlStringValue(theVersion),
                "   AND survey_nbr=", LogicUtils.sqlIntegerValue(theSurveyNbr),
                "   AND term=", LogicUtils.sqlStringValue(active.term.termCode),
                "   AND term_yr=", LogicUtils.sqlIntegerValue(active.term.shortYear),
                " ORDER BY answer DESC");

        return executeQuery(cache, sql);
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawSurveyqa> executeQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawSurveyqa> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawSurveyqa.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}

package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawSurveyqa;
import dev.mathops.db.old.svc.term.TermRec;
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
public final class RawSurveyqaLogic extends AbstractRawLogic<RawSurveyqa> {

    /** A single instance. */
    public static final RawSurveyqaLogic INSTANCE = new RawSurveyqaLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawSurveyqaLogic() {

        super();
    }

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public boolean insert(final Cache cache, final RawSurveyqa record) throws SQLException {

        if (record.termKey == null || record.version == null || record.surveyNbr == null
                || record.questionDesc == null || record.answer == null || record.answerDesc == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO surveyqa (term,term_yr,version,survey_nbr,question_desc,type_question,answer,",
                "answer_desc,answer_meaning,must_answer,tree_ref) VALUES (",
                sqlStringValue(record.termKey.termCode), ",",
                sqlIntegerValue(record.termKey.shortYear), ",",
                sqlStringValue(record.version), ",",
                sqlIntegerValue(record.surveyNbr), ",",
                sqlStringValue(record.questionDesc), ",",
                sqlStringValue(record.typeQuestion), ",",
                sqlStringValue(record.answer), ",",
                sqlStringValue(record.answerDesc), ",",
                sqlStringValue(record.answerMeaning), ",",
                sqlStringValue(record.mustAnswer), ",",
                sqlStringValue(record.treeRef), ")");

        try (final Statement s = cache.conn.createStatement()) {
            final boolean result = s.executeUpdate(sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }

            return result;
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
    @Override
    public boolean delete(final Cache cache, final RawSurveyqa record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM surveyqa ",
                "WHERE term=", sqlStringValue(record.termKey.termCode),
                "  AND term_yr=", sqlIntegerValue(record.termKey.shortYear),
                "  AND version=", sqlStringValue(record.version),
                "  AND survey_nbr=", sqlIntegerValue(record.surveyNbr),
                "  AND answer=", sqlStringValue(record.answer));

        try (final Statement stmt = cache.conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }

            return result;
        }
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public List<RawSurveyqa> queryAll(final Cache cache) throws SQLException {

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
                "SELECT * FROM surveyqa",
                " WHERE term=", sqlStringValue(termKey.termCode),
                "   AND term_yr=", sqlIntegerValue(termKey.shortYear));

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
                "SELECT * FROM surveyqa",
                " WHERE version=", sqlStringValue(theVersion),
                "   AND term=", sqlStringValue(active.term.termCode),
                "   AND term_yr=", sqlIntegerValue(active.term.shortYear));

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
                "SELECT * FROM surveyqa",
                " WHERE version=", sqlStringValue(theVersion),
                "   AND survey_nbr=", sqlIntegerValue(theSurveyNbr),
                "   AND term=", sqlStringValue(active.term.termCode),
                "   AND term_yr=", sqlIntegerValue(active.term.shortYear),
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

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawSurveyqa.fromResultSet(rs));
            }
        }

        return result;
    }
}

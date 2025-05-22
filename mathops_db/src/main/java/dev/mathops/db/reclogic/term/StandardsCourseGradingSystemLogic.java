package dev.mathops.db.reclogic.term;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.term.StandardsCourseGradingSystemRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "standards course grading system" records.
 *
 * <pre>
 * CREATE TABLE term_202510.standards_course_grading_system (
 *     grading_system_id         CHAR(6)         NOT NULL,  -- A unique ID for the grading system
 *     nbr_standards             smallint        NOT NULL,  -- The number of standards in the course
 *     min_standards             smallint        NOT NULL,  -- The minimum number of standards mastered to complete
 *                                                          --     course
 *     nbr_essential_standards   smallint        NOT NULL,  -- The number of "essential" standards in the course
 *     min_essential_standards   smallint        NOT NULL,  -- The minimum number of "essential" standards mastered to
 *                                                          --     complete the course
 *     homework_pts              smallint        NOT NULL,  -- The number of points for completing each homework
 *                                                          --     assignment
 *     on_time_mastery_pts       smallint        NOT NULL,  -- The number of points for mastering a standard on time
 *     late_mastery_pts          smallint        NOT NULL,  -- The number of points for mastering a standard late
 *     a_min_score               smallint        NOT NULL,  -- The minimum score needed to earn an A grade
 *     b_min_score               smallint        NOT NULL,  -- The minimum score needed to earn a B grade
 *     c_min_score               smallint        NOT NULL,  -- The minimum score needed to earn a C grade
 *     d_min_score               smallint,                  -- The minimum score needed to earn a D grade (null if no
 *                                                          --     D's)
 *     u_min_score               smallint,                  -- The minimum score needed to earn a U grade (rather than
 *                                                          --     F, null if no F's given for too few standards)
 *     min_standards_for_inc     smallint,                  -- If the course allows Incompletes for students who do not
 *                                                          --     pass (for second and later courses in a term), the
 *                                                          --     minimum number of standards mastered to be eligible
 *                                                          --     for Incomplete, null if Incompletes not automatic
 *     PRIMARY KEY (grading_system_id)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class StandardsCourseGradingSystemLogic implements IRecLogic<StandardsCourseGradingSystemRec> {

    /** A single instance. */
    public static final StandardsCourseGradingSystemLogic INSTANCE = new StandardsCourseGradingSystemLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StandardsCourseGradingSystemLogic() {

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
    public boolean insert(final Cache cache, final StandardsCourseGradingSystemRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".standards_course_grading_system (grading_system_id,nbr_standards,min_standards,",
                    "max_unmastered_essential,homework_pts,on_time_mastery_pts,late_mastery_pts,a_min_score,",
                    "b_min_score,c_min_score,d_min_score,u_min_score,min_standards_for_inc) VALUES (",
                    sqlStringValue(record.gradingSystemId), ",",
                    sqlIntegerValue(record.nbrStandards), ",",
                    sqlIntegerValue(record.minStandards), ",",
                    sqlIntegerValue(record.maxUnmasteredEssential), ",",
                    sqlIntegerValue(record.homeworkPts), ",",
                    sqlIntegerValue(record.onTimeMasteryPts), ",",
                    sqlIntegerValue(record.lateMasteryPts), ",",
                    sqlIntegerValue(record.aMinScore), ",",
                    sqlIntegerValue(record.bMinScore), ",",
                    sqlIntegerValue(record.cMinScore), ",",
                    sqlIntegerValue(record.dMinScore), ",",
                    sqlIntegerValue(record.uMinScore), ",",
                    sqlIntegerValue(record.minStandardsForInc), ")");

            result = doUpdateOneRow(cache, ESchema.TERM, sql);
        }

        return result;
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
    public boolean delete(final Cache cache, final StandardsCourseGradingSystemRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".standards_course_grading_system WHERE grading_system_id=",
                    sqlStringValue(record.gradingSystemId));

            result = doUpdateOneRow(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries every record in the database.
     *
     * @param cache the data cache
     * @return the complete set of records in the database
     * @throws SQLException if there is an error performing the query
     */
    @Override
    public List<StandardsCourseGradingSystemRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<StandardsCourseGradingSystemRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".standards_course_grading_system");

            result = doListQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries for a standards course grading system by its ID.
     *
     * @param cache           the data cache
     * @param gradingSystemId the grading system ID for which to query
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public StandardsCourseGradingSystemRec query(final Cache cache, final String gradingSystemId) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final StandardsCourseGradingSystemRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".standards_course_grading_system WHERE grading_system_id=", sqlStringValue(gradingSystemId));

            result = doSingleQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Updates a record.
     *
     * @param cache  the data cache
     * @param record the record to update
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public boolean update(final Cache cache, final StandardsCourseGradingSystemRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".standards_course_grading_system SET nbr_standards=", sqlIntegerValue(record.nbrStandards),
                    ",min_standards=", sqlIntegerValue(record.minStandards),
                    ",max_unmastered_essential=", sqlIntegerValue(record.maxUnmasteredEssential),
                    ",homework_pts=", sqlIntegerValue(record.homeworkPts),
                    ",on_time_mastery_pts=", sqlIntegerValue(record.onTimeMasteryPts),
                    ",late_mastery_pts=", sqlIntegerValue(record.lateMasteryPts),
                    ",a_min_score=", sqlIntegerValue(record.aMinScore),
                    ",b_min_score=", sqlIntegerValue(record.bMinScore),
                    ",c_min_score=", sqlIntegerValue(record.cMinScore),
                    ",d_min_score=", sqlIntegerValue(record.dMinScore),
                    ",u_min_score=", sqlIntegerValue(record.uMinScore),
                    ",min_standards_for_inc=", sqlIntegerValue(record.minStandardsForInc),
                    " WHERE grading_system_id=", sqlStringValue(record.gradingSystemId));

            result = doUpdateOneRow(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Extracts a record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public StandardsCourseGradingSystemRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theGradingSystemId = getStringField(rs, DataDict.FLD_GRADING_SYSTEM_ID);
        final Integer theNbrStandards = getIntegerField(rs, DataDict.FLD_NBR_STANDARDS);
        final Integer theMinStandards = getIntegerField(rs, DataDict.FLD_MIN_STANDARDS);
        final Integer theMaxUnmasteredEssential = getIntegerField(rs, DataDict.FLD_MAX_UNMASTERED_ESSENTIAL);
        final Integer theHomeworkPts = getIntegerField(rs, DataDict.FLD_HOMEWORK_PTS);
        final Integer theOnTimeMasteryPts = getIntegerField(rs, DataDict.FLD_ON_TIME_MASTERY_PTS);
        final Integer theLateMasteryPts = getIntegerField(rs, DataDict.FLD_LATE_MASTERY_PTS);
        final Integer theAMinScore = getIntegerField(rs, DataDict.FLD_A_MIN_SCORE);
        final Integer theBMinScore = getIntegerField(rs, DataDict.FLD_B_MIN_SCORE);
        final Integer theCMinScore = getIntegerField(rs, DataDict.FLD_C_MIN_SCORE);
        final Integer theDMinScore = getIntegerField(rs, DataDict.FLD_D_MIN_SCORE);
        final Integer theUMinScore = getIntegerField(rs, DataDict.FLD_U_MIN_SCORE);
        final Integer theMinStandardsForInc = getIntegerField(rs, DataDict.FLD_MIN_STANDARDS_FOR_INC);

        return new StandardsCourseGradingSystemRec(theGradingSystemId, theNbrStandards, theMinStandards,
                theMaxUnmasteredEssential, theHomeworkPts, theOnTimeMasteryPts, theLateMasteryPts, theAMinScore,
                theBMinScore, theCMinScore, theDMinScore, theUMinScore, theMinStandardsForInc);
    }
}

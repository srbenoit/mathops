package dev.mathops.db.old.reclogic;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.old.rec.StudentStandardMilestoneRec;
import dev.mathops.db.old.reclogic.iface.IInformixRecLogic;
import dev.mathops.db.old.reclogic.iface.IPostgresRecLogic;
import dev.mathops.db.old.reclogic.iface.IRecLogic;
import dev.mathops.db.old.reclogic.query.DateCriteria;
import dev.mathops.db.old.reclogic.query.IntegerCriteria;
import dev.mathops.db.old.reclogic.query.StringCriteria;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * A utility class to work with stu_std_milestone records.
 */
public abstract class StudentStandardMilestoneLogic
        implements IRecLogic<StudentStandardMilestoneRec> {

    /** A single instance. */
    public static final Informix INFORMIX = new Informix();

    /** A single instance. */
    public static final Postgres POSTGRES = new Postgres();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StudentStandardMilestoneLogic() {

        super();
    }

    /**
     * Gets the instance of {@code StandardMilestoneLogic} appropriate to a cache. The result will depend on the
     * database installation type of the PRIMARY schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code StandardMilestoneLogic} object (null if none found)
     */
    public static StudentStandardMilestoneLogic get(final Cache cache) {

        final EDbProduct type = IRecLogic.getDbType(cache);

        StudentStandardMilestoneLogic result = null;
        if (type == EDbProduct.INFORMIX) {
            result = INFORMIX;
        } else if (type == EDbProduct.POSTGRESQL) {
            result = POSTGRES;
        }

        return result;
    }

    /**
     * Updates the milestone date on a student standard milestone record.
     *
     * @param cache   the data cache
     * @param record  the record to be updated
     * @param newDate the new date
     * @return true if successful; false if not
     * @throws SQLException if there is an error performing the update
     */
    public abstract boolean updateDate(Cache cache, StudentStandardMilestoneRec record,
                                       LocalDate newDate) throws SQLException;

    /**
     * Queries for all student standard milestones for a specified student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<StudentStandardMilestoneRec> queryByStudent(Cache cache, String stuId) throws SQLException;

    /**
     * Queries for all student standard milestones for a specified pace track and pace. This will include milestones for
     * all pace indexes in that pace.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @param track the pace track
     * @param pace  the pace
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<StudentStandardMilestoneRec> queryByStuPaceTrackPace(Cache cache, String stuId,
                                                                              String track, Integer pace)
            throws SQLException;

    /**
     * Queries for all student standard milestones for a specified pace track, pace, and pace index.
     *
     * @param cache     the data cache
     * @param stuId     the student ID
     * @param track     the pace track
     * @param pace      the pace
     * @param paceIndex the pace index
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<StudentStandardMilestoneRec> queryByStuPaceTrackPaceIndex(Cache cache, String stuId,
                                                                                   String track, Integer pace,
                                                                                   Integer paceIndex)
            throws SQLException;

    /**
     * Queries for a single student standard milestone based on student ID, pace track, pace, pace index, unit,
     * objective, and milestone type.
     *
     * @param cache     the data cache
     * @param stuId     the student ID
     * @param track     the pace track
     * @param pace      the pace
     * @param paceIndex the pace index
     * @param unit      the unit
     * @param objective the objective
     * @param msType    the milestone type
     * @return the record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public abstract StudentStandardMilestoneRec query(Cache cache, String stuId, String track,
                                                      Integer pace, Integer paceIndex, Integer unit,
                                                      Integer objective, String msType)
            throws SQLException;

    /**
     * Queries for all records matching given criteria.
     *
     * @param cache         the data cache
     * @param queryCriteria the general query criteria
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<StudentStandardMilestoneRec> generalQuery(Cache cache,
                                                                   Criteria queryCriteria) throws SQLException;

    /**
     * A "standard milestone" criteria record used to perform arbitrary queries.
     */
    public static class Criteria {

        /** The criteria for the 'stu_id' field. */
        StringCriteria stuId;

        /** The criteria for the 'pace_track' field. */
        StringCriteria paceTrack;

        /** The criteria for the 'pace' field. */
        IntegerCriteria pace;

        /** The criteria for the 'pace_index' field. */
        IntegerCriteria paceIndex;

        /** The criteria for the 'unit' field. */
        IntegerCriteria unit;

        /** The criteria for the 'objective' field. */
        IntegerCriteria objective;

        /** The criteria for the 'ms_type' field. */
        StringCriteria msType;

        /** The criteria for the 'ms_date' field. */
        DateCriteria msDate;

        /**
         * Constructs a new {@code Criteria}.
         */
        public Criteria() {

            // No action
        }
    }

    /**
     * A subclass of {@code StandardMilestoneLogic} designed for the Informix schema.
     */
    public static final class Informix extends StudentStandardMilestoneLogic
            implements IInformixRecLogic<StudentStandardMilestoneRec> {

        /** A field name. */
        private static final String FLD_STU_ID = "stu_id";

        /** A field name. */
        private static final String FLD_PACE_TRACK = "pace_track";

        /** A field name. */
        private static final String FLD_PACE = "pace";

        /** A field name. */
        private static final String FLD_PACE_INDEX = "pace_index";

        /** A field name. */
        private static final String FLD_UNIT = "unit";

        /** A field name. */
        private static final String FLD_OBJECTIVE = "objective";

        /** A field name. */
        private static final String FLD_MS_TYPE = "ms_type";

        /** A field name. */
        private static final String FLD_MS_DATE = "ms_date";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final StudentStandardMilestoneRec record)
                throws SQLException {

            if (record.stuId == null || record.paceTrack == null || record.pace == null
                    || record.paceIndex == null || record.unit == null || record.objective == null
                    || record.msType == null) {
                throw new SQLException("Null value in primary key or required field.");
            }

            final String sql = SimpleBuilder.concat("INSERT INTO stu_std_milestone (stu_id,pace_track,pace,pace_index,",
                    "unit,objective,ms_type,ms_date) VALUES (",
                    sqlStringValue(record.stuId), ",",
                    sqlStringValue(record.paceTrack), ",",
                    sqlIntegerValue(record.pace), ",",
                    sqlIntegerValue(record.paceIndex), ",",
                    sqlIntegerValue(record.unit), ",",
                    sqlIntegerValue(record.objective), ",",
                    sqlStringValue(record.msType), ",",
                    sqlDateValue(record.msDate), ")");

            return doUpdateOneRow(cache, sql);
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
        public boolean delete(final Cache cache, final StudentStandardMilestoneRec record)
                throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM stu_std_milestone ",
                    "WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND pace_track=", sqlStringValue(record.paceTrack),
                    " AND pace=", sqlIntegerValue(record.pace),
                    " AND pace_index=", sqlIntegerValue(record.paceIndex),
                    " AND unit=", sqlIntegerValue(record.unit),
                    " AND objective=", sqlIntegerValue(record.objective),
                    " AND ms_type=", sqlStringValue(record.msType));

            return doUpdateOneRow(cache, sql);
        }

        /**
         * Queries every record in the database.
         *
         * @param cache the data cache
         * @return the complete set of records in the database
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StudentStandardMilestoneRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, "SELECT * FROM stu_std_milestone");
        }

        /**
         * Updates the milestone date on a student standard milestone record.
         *
         * @param cache   the data cache
         * @param record  the record to be updated
         * @param newDate the new date
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the update
         */
        @Override
        public boolean updateDate(final Cache cache, final StudentStandardMilestoneRec record,
                                  final LocalDate newDate) throws SQLException {

            final String sql = SimpleBuilder.concat("UPDATE stu_std_milestone ",
                    "SET ms_date=", sqlDateValue(newDate),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND pace_track=", sqlStringValue(record.paceTrack),
                    " AND pace=", sqlIntegerValue(record.pace),
                    " AND pace_index=", sqlIntegerValue(record.paceIndex),
                    " AND unit=", sqlIntegerValue(record.unit),
                    " AND objective=", sqlIntegerValue(record.objective),
                    " AND ms_type=", sqlStringValue(record.msType));

            return doUpdateOneRow(cache, sql);
        }

        /**
         * Queries for all student standard milestones for a specified student.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StudentStandardMilestoneRec> queryByStudent(final Cache cache,
                                                                final String stuId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM stu_std_milestone ",
                    "WHERE stu_id=", sqlStringValue(stuId));

            return doListQuery(cache, sql);
        }

        /**
         * Queries for all standard milestones for a specified pace track and pace. This will include milestones for all
         * pace indexes in that pace.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @param track the pace track
         * @param pace  the pace
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StudentStandardMilestoneRec> queryByStuPaceTrackPace(final Cache cache, final String stuId,
                                                                         final String track, final Integer pace)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM stu_std_milestone ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND pace_track=", sqlStringValue(track),
                    " AND pace=", sqlIntegerValue(pace));

            return doListQuery(cache, sql);
        }

        /**
         * Queries for all standard milestones for a specified pace track, pace, and pace index.
         *
         * @param cache     the data cache
         * @param stuId     the student ID
         * @param track     the pace track
         * @param pace      the pace
         * @param paceIndex the pace index
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StudentStandardMilestoneRec> queryByStuPaceTrackPaceIndex(final Cache cache, final String stuId,
                                                                              final String track, final Integer pace,
                                                                              final Integer paceIndex)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM stu_std_milestone ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND pace_track=", sqlStringValue(track),
                    " AND pace=", sqlIntegerValue(pace),
                    " AND pace_index=", sqlIntegerValue(paceIndex));

            return doListQuery(cache, sql);
        }

        /**
         * Queries for a single standard milestone pace track, pace, pace index, unit, objective, and milestone type.
         *
         * @param cache     the data cache
         * @param stuId     the student ID
         * @param track     the pace track
         * @param pace      the pace
         * @param paceIndex the pace index
         * @param unit      the unit
         * @param objective the objective
         * @param msType    the milestone type
         * @return the record; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public StudentStandardMilestoneRec query(final Cache cache, final String stuId, final String track,
                                                 final Integer pace, final Integer paceIndex, final Integer unit,
                                                 final Integer objective, final String msType) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM stu_std_milestone ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND pace_track=", sqlStringValue(track),
                    " AND pace=", sqlIntegerValue(pace),
                    " AND pace_index=", sqlIntegerValue(paceIndex),
                    " AND unit=", sqlIntegerValue(unit),
                    " AND objective=", sqlIntegerValue(objective),
                    " AND ms_type=", sqlStringValue(msType));

            return doSingleQuery(cache, sql);
        }

        /**
         * Queries for all records matching given criteria.
         *
         * @param cache         the data cache
         * @param queryCriteria the general query criteria
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StudentStandardMilestoneRec> generalQuery(final Cache cache,
                                                              final Criteria queryCriteria) throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(150);

            sql.add("SELECT * FROM stu_std_milestone");

            String w = stringWhere(sql, WHERE, "stu_id", queryCriteria.stuId);
            w = stringWhere(sql, w, "pace_track", queryCriteria.paceTrack);
            w = integerWhere(sql, w, "pace", queryCriteria.pace);
            w = integerWhere(sql, w, "pace_index", queryCriteria.paceIndex);
            w = integerWhere(sql, w, "unit", queryCriteria.unit);
            w = integerWhere(sql, w, "objective", queryCriteria.objective);
            w = stringWhere(sql, w, "ms_type", queryCriteria.msType);
            dateWhere(sql, w, "ms_date", queryCriteria.msDate);

            return doListQuery(cache, sql.toString());
        }

        /**
         * Extracts a record from a result set.
         *
         * @param rs the result set from which to retrieve the record
         * @return the record
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public StudentStandardMilestoneRec fromResultSet(final ResultSet rs) throws SQLException {

            final StudentStandardMilestoneRec result = new StudentStandardMilestoneRec();

            result.stuId = getStringField(rs, FLD_STU_ID);
            result.paceTrack = getStringField(rs, FLD_PACE_TRACK);
            result.pace = getIntegerField(rs, FLD_PACE);
            result.paceIndex = getIntegerField(rs, FLD_PACE_INDEX);
            result.unit = getIntegerField(rs, FLD_UNIT);
            result.objective = getIntegerField(rs, FLD_OBJECTIVE);
            result.msType = getStringField(rs, FLD_MS_TYPE);
            result.msDate = getDateField(rs, FLD_MS_DATE);

            return result;
        }
    }

    /**
     * A subclass of {@code StandardMilestoneLogic} designed for the PostgreSQL schema.
     */
    public static final class Postgres extends StudentStandardMilestoneLogic
            implements IPostgresRecLogic<StudentStandardMilestoneRec> {

        /** A field name. */
        private static final String FLD_STU_ID = "stu_id";

        /** A field name. */
        private static final String FLD_PACE_TRACK = "pace_track";

        /** A field name. */
        private static final String FLD_PACE = "pace";

        /** A field name. */
        private static final String FLD_PACE_INDEX = "pace_index";

        /** A field name. */
        private static final String FLD_UNIT = "unit";

        /** A field name. */
        private static final String FLD_OBJECTIVE = "objective";

        /** A field name. */
        private static final String FLD_MS_TYPE = "ms_type";

        /** A field name. */
        private static final String FLD_MS_DATE = "ms_date";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final StudentStandardMilestoneRec record) throws SQLException {

            if (record.stuId == null || record.paceTrack == null || record.pace == null || record.paceIndex == null
                    || record.unit == null || record.objective == null || record.msType == null) {
                throw new SQLException("Null value in primary key or required field.");
            }

            final String sql = SimpleBuilder.concat("INSERT INTO ", cache.termSchemaName,
                    ".stu_std_milestone (stu_id,pace_track,pace,pace_index,unit,objective,ms_type,ms_date) VALUES (",
                    sqlStringValue(record.stuId), ",",
                    sqlStringValue(record.paceTrack), ",",
                    sqlIntegerValue(record.pace), ",",
                    sqlIntegerValue(record.paceIndex), ",",
                    sqlIntegerValue(record.unit), ",",
                    sqlIntegerValue(record.objective), ",",
                    sqlStringValue(record.msType), ",",
                    sqlDateValue(record.msDate), ")");

            return doUpdateOneRow(cache, sql);
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
        public boolean delete(final Cache cache, final StudentStandardMilestoneRec record)
                throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM ", cache.termSchemaName, ".stu_std_milestone ",
                    "WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND pace_track=", sqlStringValue(record.paceTrack),
                    " AND pace=", sqlIntegerValue(record.pace),
                    " AND pace_index=", sqlIntegerValue(record.paceIndex),
                    " AND unit=", sqlIntegerValue(record.unit),
                    " AND objective=", sqlIntegerValue(record.objective),
                    " AND ms_type=", sqlStringValue(record.msType));

            return doUpdateOneRow(cache, sql);
        }

        /**
         * Queries every record in the database.
         *
         * @param cache the data cache
         * @return the complete set of records in the database
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StudentStandardMilestoneRec> queryAll(final Cache cache) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ", cache.termSchemaName, ".stu_std_milestone");

            return doListQuery(cache, sql);
        }

        /**
         * Updates the milestone date on a student standard milestone record.
         *
         * @param cache   the data cache
         * @param record  the record to be updated
         * @param newDate the new date
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the update
         */
        @Override
        public boolean updateDate(final Cache cache, final StudentStandardMilestoneRec record,
                                  final LocalDate newDate) throws SQLException {

            final String sql = SimpleBuilder.concat("UPDATE ", cache.termSchemaName, ".stu_std_milestone ",
                    "SET ms_date=", sqlDateValue(newDate),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND pace_track=", sqlStringValue(record.paceTrack),
                    " AND pace=", sqlIntegerValue(record.pace),
                    " AND pace_index=", sqlIntegerValue(record.paceIndex),
                    " AND unit=", sqlIntegerValue(record.unit),
                    " AND objective=", sqlIntegerValue(record.objective),
                    " AND ms_type=", sqlStringValue(record.msType));

            return doUpdateOneRow(cache, sql);
        }

        /**
         * Queries for all student standard milestones for a specified student.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StudentStandardMilestoneRec> queryByStudent(final Cache cache,
                                                                final String stuId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ", cache.termSchemaName, ".stu_std_milestone ",
                    "WHERE stu_id=", sqlStringValue(stuId));

            return doListQuery(cache, sql);
        }

        /**
         * Queries for all standard milestones for a specified pace track and pace. This will include milestones for all
         * pace indexes in that pace.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @param track the pace track
         * @param pace  the pace
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StudentStandardMilestoneRec> queryByStuPaceTrackPace(final Cache cache, final String stuId,
                                                                         final String track, final Integer pace)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ", cache.termSchemaName, ".stu_std_milestone ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND pace_track=", sqlStringValue(track),
                    " AND pace=", sqlIntegerValue(pace));

            return doListQuery(cache, sql);
        }

        /**
         * Queries for all standard milestones for a specified pace track, pace, and pace index.
         *
         * @param cache     the data cache
         * @param stuId     the student ID
         * @param track     the pace track
         * @param pace      the pace
         * @param paceIndex the pace index
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StudentStandardMilestoneRec> queryByStuPaceTrackPaceIndex(final Cache cache, final String stuId,
                                                                              final String track, final Integer pace,
                                                                              final Integer paceIndex)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ", cache.termSchemaName, ".stu_std_milestone ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND pace_track=", sqlStringValue(track),
                    " AND pace=", sqlIntegerValue(pace),
                    " AND pace_index=", sqlIntegerValue(paceIndex));

            return doListQuery(cache, sql);
        }

        /**
         * Queries for a single standard milestone pace track, pace, pace index, unit, objective, and milestone type.
         *
         * @param cache     the data cache
         * @param stuId     the student ID
         * @param track     the pace track
         * @param pace      the pace
         * @param paceIndex the pace index
         * @param unit      the unit
         * @param objective the objective
         * @param msType    the milestone type
         * @return the record; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public StudentStandardMilestoneRec query(final Cache cache, final String stuId, final String track,
                                                 final Integer pace, final Integer paceIndex, final Integer unit,
                                                 final Integer objective, final String msType) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ", cache.termSchemaName, ".stu_std_milestone ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND pace_track=", sqlStringValue(track),
                    " AND pace=", sqlIntegerValue(pace),
                    " AND pace_index=", sqlIntegerValue(paceIndex),
                    " AND unit=", sqlIntegerValue(unit),
                    " AND objective=", sqlIntegerValue(objective),
                    " AND ms_type=", sqlStringValue(msType));

            return doSingleQuery(cache, sql);
        }

        /**
         * Queries for all records matching given criteria.
         *
         * @param cache the data cache
         * @param queryCriteria  the general query criteria
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StudentStandardMilestoneRec> generalQuery(final Cache cache,
                                                              final Criteria queryCriteria) throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(150);

            sql.add("SELECT * FROM  ", cache.termSchemaName, ".stu_std_milestone");

            String w = stringWhere(sql, WHERE, "stu_id", queryCriteria.stuId);
            w = stringWhere(sql, w, "pace_track", queryCriteria.paceTrack);
            w = integerWhere(sql, w, "pace", queryCriteria.pace);
            w = integerWhere(sql, w, "pace_index", queryCriteria.paceIndex);
            w = integerWhere(sql, w, "unit", queryCriteria.unit);
            w = integerWhere(sql, w, "objective", queryCriteria.objective);
            w = stringWhere(sql, w, "ms_type", queryCriteria.msType);
            dateWhere(sql, w, "ms_date", queryCriteria.msDate);

            return doListQuery(cache, sql.toString());
        }

        /**
         * Extracts a record from a result set.
         *
         * @param rs the result set from which to retrieve the record
         * @return the record
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public StudentStandardMilestoneRec fromResultSet(final ResultSet rs) throws SQLException {

            final StudentStandardMilestoneRec result = new StudentStandardMilestoneRec();

            result.stuId = getStringField(rs, FLD_STU_ID);
            result.paceTrack = getStringField(rs, FLD_PACE_TRACK);
            result.pace = getIntegerField(rs, FLD_PACE);
            result.paceIndex = getIntegerField(rs, FLD_PACE_INDEX);
            result.unit = getIntegerField(rs, FLD_UNIT);
            result.objective = getIntegerField(rs, FLD_OBJECTIVE);
            result.msType = getStringField(rs, FLD_MS_TYPE);
            result.msDate = getDateField(rs, FLD_MS_DATE);

            return result;
        }
    }
}

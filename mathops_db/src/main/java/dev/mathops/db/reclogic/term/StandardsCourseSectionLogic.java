package dev.mathops.db.reclogic.term;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.term.StandardsCourseSectionRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "standards course section" records.
 *
 * <pre>
 * CREATE TABLE term_202510.standards_course_section (
 *     course_id                 char(10)        NOT NULL,  -- The unique course ID (references standards_course)
 *     section_nbr               char(4)         NOT NULL,  -- The section number (from the registration system)
 *     crn                       char(6)         NOT NULL,  -- The CRN (from the registration system)
 *     aries_start_date          date            NOT NULL,  -- The "official" start date of the course
 *     aries_end_date            date            NOT NULL,  -- The "official" end date of the course
 *     first_class_date          date            NOT NULL,  -- The first date the course is available to students
 *     last_class_date           date            NOT NULL,  -- The last date the course is available to students
 *     subterm                   char(5)         NOT NULL,  -- The subterm ('FULL', 'HALF1', 'HALF2', 'NN:MM' for weeks
 *                                                          --     NN through MM)
 *     grading_system_id         char(6)         NOT NULL,  -- The grading system to use for the section
 *     campus                    char(2)         NOT NULL,  -- The campus code ('FC'=Fort Collins, 'SP'=Spur,
 *                                                          --     'CE'=Continuing Ed.)
 *     delivery_mode             char(2)         NOT NULL,  -- The delivery mode ('RF'=Resident Face-to-Face,
 *                                                          --     'RH'=Resident Hybrid, 'RO'=Resident Online,
 *                                                          --     'DO'=Distance Online)
 *     canvas_id                 varchar(40),               -- The ID of the associated Canvas course
 *     instructor                varchar(30),               -- The name of the instructor assigned to the section
 *     building_name             varchar(40),               -- The name of the building where class sessions meet
 *     room_nbr                  varchar(20),               -- The room number where classes meet
 *     weekdays                  smallint,                  -- The weekdays the class meets (logical OR of 1=Sun, 2=Mon,
 *                                                          --     4=Tue, 8=Wed, 16=Thu, 32=Fri, 64=Sat)
 *     PRIMARY KEY (course_id, section_nbr)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class StandardsCourseSectionLogic implements IRecLogic<StandardsCourseSectionRec> {

    /** A single instance. */
    public static final StandardsCourseSectionLogic INSTANCE = new StandardsCourseSectionLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StandardsCourseSectionLogic() {

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
    public boolean insert(final Cache cache, final StandardsCourseSectionRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".standards_course_section (course_id,section_nbr,crn,aries_start_date,aries_end_date,",
                    "first_class_date,last_class_date,subterm,grading_system_id,campus,delivery_mode,canvas_id,",
                    "instructor,building_name,room_nbr,weekdays) VALUES (",
                    sqlStringValue(record.courseId), ",",
                    sqlStringValue(record.sectionNbr), ",",
                    sqlStringValue(record.crn), ",",
                    sqlDateValue(record.ariesStartDate), ",",
                    sqlDateValue(record.ariesEndDate), ",",
                    sqlDateValue(record.firstClassDate), ",",
                    sqlDateValue(record.lastClassDate), ",",
                    sqlStringValue(record.subterm), ",",
                    sqlStringValue(record.gradingSystemId), ",",
                    sqlStringValue(record.campus), ",",
                    sqlStringValue(record.deliveryMode), ",",
                    sqlStringValue(record.canvasId), ",",
                    sqlStringValue(record.instructor), ",",
                    sqlStringValue(record.buildingName), ",",
                    sqlStringValue(record.roomNbr), ",",
                    sqlIntegerValue(record.weekdays), ")");

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
    public boolean delete(final Cache cache, final StandardsCourseSectionRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".standards_course_section WHERE course_id=", sqlStringValue(record.courseId),
                    " AND section_nbr=", sqlStringValue(record.sectionNbr));

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
    public List<StandardsCourseSectionRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<StandardsCourseSectionRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".standards_course_section");

            result = doListQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries for a standards course section by its ID.
     *
     * @param cache      the data cache
     * @param courseId   the course ID for which to query
     * @param sectionNbr the section number for which to query
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public StandardsCourseSectionRec query(final Cache cache, final String courseId, final String sectionNbr)
            throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final StandardsCourseSectionRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".standards_course_section WHERE course_id=", sqlStringValue(courseId),
                    " AND section_nbr=", sqlStringValue(sectionNbr));

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
    public boolean update(final Cache cache, final StandardsCourseSectionRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".standards_course_section SET crn=", sqlStringValue(record.crn),
                    ",aries_start_date=", sqlDateValue(record.ariesStartDate),
                    ",aries_end_date=", sqlDateValue(record.ariesEndDate),
                    ",first_class_date=", sqlDateValue(record.firstClassDate),
                    ",last_class_date=", sqlDateValue(record.lastClassDate),
                    ",subterm=", sqlStringValue(record.subterm),
                    ",grading_system_id=", sqlStringValue(record.gradingSystemId),
                    ",campus=", sqlStringValue(record.campus),
                    ",delivery_mode=", sqlStringValue(record.deliveryMode),
                    ",canvas_id=", sqlStringValue(record.canvasId),
                    ",instructor=", sqlStringValue(record.instructor),
                    ",building_name=", sqlStringValue(record.buildingName),
                    ",room_nbr=", sqlStringValue(record.roomNbr),
                    ",weekdays=", sqlIntegerValue(record.weekdays),
                    " WHERE course_id=", sqlStringValue(record.courseId),
                    " AND section_nbr=", sqlStringValue(record.sectionNbr));

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
    public StandardsCourseSectionRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theCourseId = getStringField(rs, DataDict.FLD_COURSE_ID);
        final String theSectionNbr = getStringField(rs, DataDict.FLD_SECTION_NBR);
        final String theCrn = getStringField(rs, DataDict.FLD_CRN);
        final LocalDate theAriesStartDate = getDateField(rs, DataDict.FLD_ARIES_START_DATE);
        final LocalDate theAriesEndDate = getDateField(rs, DataDict.FLD_ARIES_END_DATE);
        final LocalDate theFirstClassDate = getDateField(rs, DataDict.FLD_FIRST_CLASS_DATE);
        final LocalDate theLastClassDate = getDateField(rs, DataDict.FLD_LAST_CLASS_DATE);
        final String theSubterm = getStringField(rs, DataDict.FLD_SUBTERM);
        final String theGradingSystemId = getStringField(rs, DataDict.FLD_GRADING_SYSTEM_ID);
        final String theCampus = getStringField(rs, DataDict.FLD_CAMPUS);
        final String theDeliveryMode = getStringField(rs, DataDict.FLD_DELIVERY_MODE);
        final String theCanvasId = getStringField(rs, DataDict.FLD_CANVAS_ID);
        final String theInstructor = getStringField(rs, DataDict.FLD_INSTRUCTOR);
        final String theBuildingName = getStringField(rs, DataDict.FLD_BUILDING_NAME);
        final String theRoomNbr = getStringField(rs, DataDict.FLD_ROOM_NBR);
        final Integer theWeekdays = getIntegerField(rs, DataDict.FLD_WEEKDAYS);

        return new StandardsCourseSectionRec(theCourseId, theSectionNbr, theCrn, theAriesStartDate, theAriesEndDate,
                theFirstClassDate, theLastClassDate, theSubterm, theGradingSystemId, theCampus, theDeliveryMode,
                theCanvasId, theInstructor, theBuildingName, theRoomNbr, theWeekdays);
    }
}

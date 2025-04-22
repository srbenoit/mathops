package dev.mathops.db.logic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawWhichDb;
import dev.mathops.db.rec.main.CourseSurveyItemChoiceRec;
import dev.mathops.db.rec.main.CourseSurveyItemRec;
import dev.mathops.db.rec.main.CourseSurveyRec;
import dev.mathops.db.rec.main.FacilityClosureRec;
import dev.mathops.db.rec.main.FacilityHoursRec;
import dev.mathops.db.rec.main.FacilityRec;
import dev.mathops.db.rec.main.StandardAssignmentRec;
import dev.mathops.db.rec.main.StandardsCourseModuleRec;
import dev.mathops.db.rec.main.StandardsCourseRec;
import dev.mathops.db.reclogic.main.CourseSurveyItemChoiceLogic;
import dev.mathops.db.reclogic.main.CourseSurveyItemLogic;
import dev.mathops.db.reclogic.main.CourseSurveyLogic;
import dev.mathops.db.reclogic.main.FacilityClosureLogic;
import dev.mathops.db.reclogic.main.FacilityHoursLogic;
import dev.mathops.db.reclogic.main.FacilityLogic;
import dev.mathops.db.reclogic.main.StandardAssignmentLogic;
import dev.mathops.db.reclogic.main.StandardsCourseLogic;
import dev.mathops.db.reclogic.main.StandardsCourseModuleLogic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A data container for data from the "MAIN" schema for use in a single webpage generation or business process.  Data is
 * loaded lazily when queried, and can be "forgotten" to trigger a re-query if underlying data is changed.
 */
public final class MainData {

    /** The cache. */
    private final Cache cache;

    /** The database to which the cache is connected. */
    private RawWhichDb whichDb = null;

    /** The facilities. */
    private List<FacilityRec> facilities = null;

    /** The facility hours. */
    private List<FacilityHoursRec> facilityHours = null;

    /** The facility closures. */
    private List<FacilityClosureRec> facilityClosures = null;

    /** The standards courses. */
    private List<StandardsCourseRec> standardsCourses = null;

    /** Map from standards-based course ID to the courses modules for that course. */
    private Map<String, List<StandardsCourseModuleRec>> standardsCourseModules = null;

    /** The standards assignments. */
    private List<StandardAssignmentRec> standardsAssignments = null;

    /** The course surveys. */
    private List<CourseSurveyRec> courseSurveys = null;

    /** Map from survey ID to the list of items for that course survey. */
    private Map<String, List<CourseSurveyItemRec>> courseSurveyItems = null;

    /** Map from survey ID to the list of choices for all items in that course survey. */
    private Map<String, List<CourseSurveyItemChoiceRec>> courseSurveyItemChoices = null;

    /**
     * Constructs a new {@code MainData}.
     *
     * @param theCache the cache
     */
    public MainData(final Cache theCache) {

        if (theCache == null) {
            throw new IllegalArgumentException("Cache may not be null");
        }

        this.cache = theCache;
    }

    /**
     * Gets the database descriptor for the database to which this object's cache is connected.
     *
     * @return the database descriptor
     * @throws SQLException if there is an error accessing the database
     */
    public RawWhichDb getWhichDb() throws SQLException {

        if (this.whichDb == null) {
            final String prefix = this.cache.getSchemaPrefix(ESchema.MAIN);
            if (prefix == null) {
                throw new SQLException("Cache has no implementation of the MAIN schema.");
            }

            final String sql = "SELECT descr FROM " + prefix + ".which_db";

            final DbConnection conn = this.cache.checkOutConnection(ESchema.MAIN);
            try (final Statement stmt = conn.createStatement();
                 final ResultSet rs = stmt.executeQuery(sql)) {

                if (rs.next()) {
                    final String description = rs.getString(1);
                    this.whichDb = new RawWhichDb(description);
                } else {
                    throw new SQLException("Failed to query WHICH_DB table in MAIN schema.");
                }
            } finally {
                Cache.checkInConnection(conn);
            }
        }

        return this.whichDb;
    }

    /**
     * Gets all facilities.
     *
     * @return the list of facilities
     * @throws SQLException if there is an error accessing the database
     */
    public List<FacilityRec> getFacilities() throws SQLException {

        if (this.facilities == null) {
            this.facilities = FacilityLogic.INSTANCE.queryAll(this.cache);
        }

        return this.facilities;
    }

    /**
     * Gets a single facility by its ID.
     *
     * @param facilityId the facility ID
     * @return the facility record; null if none found
     * @throws SQLException if there is an error accessing the database
     */
    public FacilityRec getFacility(final String facilityId) throws SQLException {

        FacilityRec result = null;

        final List<FacilityRec> all = getFacilities();

        for (final FacilityRec rec : all) {
            if (rec.facilityId.equals(facilityId)) {
                result = rec;
                break;
            }
        }

        return result;
    }

    /**
     * Gets the hours for a single facility.
     *
     * @param facilityId the facility ID
     * @return the list of facility hours records for the specified facility
     * @throws SQLException if there is an error accessing the database
     */
    public List<FacilityHoursRec> getFacilityHours(final String facilityId) throws SQLException {

        final List<FacilityHoursRec> result = new ArrayList<>(10);

        if (this.facilityHours == null) {
            this.facilityHours = FacilityHoursLogic.INSTANCE.queryAll(this.cache);
        }

        for (final FacilityHoursRec rec : this.facilityHours) {
            if (rec.facilityId.equals(facilityId)) {
                result.add(rec);
            }
        }

        return result;
    }

    /**
     * Gets the closures for a single facility.
     *
     * @param facilityId the facility ID
     * @return the list of facility closure records for the specified facility
     * @throws SQLException if there is an error accessing the database
     */
    public List<FacilityClosureRec> getFacilityClosures(final String facilityId) throws SQLException {

        final List<FacilityClosureRec> result = new ArrayList<>(10);

        if (this.facilityClosures == null) {
            this.facilityClosures = FacilityClosureLogic.INSTANCE.queryAll(this.cache);
        }

        for (final FacilityClosureRec rec : this.facilityClosures) {
            if (rec.facilityId.equals(facilityId)) {
                result.add(rec);
            }
        }

        return result;
    }

    /**
     * Gets all standards-based courses.
     *
     * @return the list of standards-based courses
     * @throws SQLException if there is an error accessing the database
     */
    public List<StandardsCourseRec> getStandardsCourses() throws SQLException {

        if (this.standardsCourses == null) {
            this.standardsCourses = StandardsCourseLogic.INSTANCE.queryAll(this.cache);
        }

        return this.standardsCourses;
    }

    /**
     * Gets a single standards-based course by its ID.
     *
     * @param courseId the course ID
     * @return the standards-based course record; null if none found
     * @throws SQLException if there is an error accessing the database
     */
    public StandardsCourseRec getStandardsCourse(final String courseId) throws SQLException {

        StandardsCourseRec result = null;

        final List<StandardsCourseRec> all = getStandardsCourses();

        for (final StandardsCourseRec rec : all) {
            if (rec.courseId.equals(courseId)) {
                result = rec;
                break;
            }
        }

        return result;
    }

    /**
     * Gets all modules in a standards-based course.
     *
     * @param courseId the course ID
     * @return the list of modules (result will be sorted by module number)
     * @throws SQLException if there is an error accessing the database
     */
    public List<StandardsCourseModuleRec> getStandardsCourseModules(final String courseId) throws SQLException {

        if (this.standardsCourseModules == null) {
            this.standardsCourseModules = new HashMap<>(10);

            final List<StandardsCourseModuleRec> all = StandardsCourseModuleLogic.INSTANCE.queryAll(this.cache);
            for (final StandardsCourseModuleRec rec : all) {
                final List<StandardsCourseModuleRec> inner = this.standardsCourseModules.computeIfAbsent(rec.courseId,
                        x -> new ArrayList<>(10));
                inner.add(rec);
            }

            for (final List<StandardsCourseModuleRec> list : this.standardsCourseModules.values()) {
                list.sort(null);
            }
        }

        final List<StandardsCourseModuleRec> result = this.standardsCourseModules.get(courseId);

        return result == null ? new ArrayList<>(0) : result;
    }

    /**
     * Gets a single module in a standards-based course.
     *
     * @param courseId  the course ID
     * @param moduleNbr the module number
     * @return the module
     * @throws SQLException if there is an error accessing the database
     */
    public StandardsCourseModuleRec getStandardsCourseModule(final String courseId, final Integer moduleNbr)
            throws SQLException {

        final List<StandardsCourseModuleRec> all = getStandardsCourseModules(courseId);

        StandardsCourseModuleRec result = null;
        for (final StandardsCourseModuleRec test : all) {
            if (test.moduleNbr.equals(moduleNbr)) {
                result = test;
                break;
            }
        }

        return result;
    }

    /**
     * Gets all assignments for a standards-based course.
     *
     * @param courseId the course ID
     * @return the list of standards-based assignments
     * @throws SQLException if there is an error accessing the database
     */
    public List<StandardAssignmentRec> getStandardAssignments(final String courseId) throws SQLException {

        if (this.standardsAssignments == null) {
            this.standardsAssignments = StandardAssignmentLogic.INSTANCE.queryAll(this.cache);
        }

        final List<StandardAssignmentRec> result = new ArrayList<>(20);

        for (final StandardAssignmentRec rec : this.standardsAssignments) {
            if (rec.courseId.equals(courseId)) {
                result.add(rec);
            }
        }

        return result;
    }

    /**
     * Gets all course survey records.
     *
     * @return the list of course survey records
     * @throws SQLException if there is an error accessing the database
     */
    public List<CourseSurveyRec> getCourseSurveys() throws SQLException {

        if (this.courseSurveys == null) {
            this.courseSurveys = CourseSurveyLogic.INSTANCE.queryAll(this.cache);
        }

        return this.courseSurveys;
    }

    /**
     * Gets a single course survey by its ID.
     *
     * @param surveyId the survey ID
     * @return the facility record; null if none found
     * @throws SQLException if there is an error accessing the database
     */
    public CourseSurveyRec getCourseSurvey(final String surveyId) throws SQLException {

        CourseSurveyRec result = null;

        final List<CourseSurveyRec> all = getCourseSurveys();

        for (final CourseSurveyRec rec : all) {
            if (rec.surveyId.equals(surveyId)) {
                result = rec;
                break;
            }
        }

        return result;
    }

    /**
     * Gets all items in a course survey.
     *
     * @param surveyId the survey ID
     * @return the list of survey items
     * @throws SQLException if there is an error accessing the database
     */
    public List<CourseSurveyItemRec> getCourseSurveyItems(final String surveyId) throws SQLException {

        if (this.courseSurveyItems == null) {
            this.courseSurveyItems = new HashMap<>(10);

            final List<CourseSurveyItemRec> all = CourseSurveyItemLogic.INSTANCE.queryAll(this.cache);
            for (final CourseSurveyItemRec rec : all) {
                final List<CourseSurveyItemRec> inner = this.courseSurveyItems.computeIfAbsent(rec.surveyId,
                        x -> new ArrayList<>(10));
                inner.add(rec);
            }
        }

        final List<CourseSurveyItemRec> result = this.courseSurveyItems.get(surveyId);

        return result == null ? new ArrayList<>(0) : result;
    }

    /**
     * Gets all choices for a single item in a course survey.
     *
     * @param surveyId the survey ID
     * @param itemNbr  the item number
     * @return the list choices for the of survey item
     * @throws SQLException if there is an error accessing the database
     */
    public List<CourseSurveyItemChoiceRec> getCourseSurveyItems(final String surveyId, Integer itemNbr) throws SQLException {

        if (this.courseSurveyItemChoices == null) {
            this.courseSurveyItemChoices = new HashMap<>(10);

            final List<CourseSurveyItemChoiceRec> all = CourseSurveyItemChoiceLogic.INSTANCE.queryAll(this.cache);
            for (final CourseSurveyItemChoiceRec rec : all) {
                final List<CourseSurveyItemChoiceRec> inner = this.courseSurveyItemChoices.computeIfAbsent(rec.surveyId,
                        x -> new ArrayList<>(10));
                inner.add(rec);
            }
        }

        final List<CourseSurveyItemChoiceRec> result = new ArrayList<>(10);

        final List<CourseSurveyItemChoiceRec> allForSurvey = this.courseSurveyItemChoices.get(surveyId);

        if (allForSurvey != null) {
            for (final CourseSurveyItemChoiceRec rec : allForSurvey) {
                if (rec.itemNbr.equals(itemNbr)) {
                    result.add(rec);
                }
            }
        }

        return result;
    }
}

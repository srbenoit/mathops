package dev.mathops.db.logic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawWhichDb;
import dev.mathops.db.rec.main.FacilityHoursRec;
import dev.mathops.db.rec.main.FacilityRec;
import dev.mathops.db.rec.term.StandardsCourseGradingSystemRec;
import dev.mathops.db.rec.term.StandardsCourseSectionRec;
import dev.mathops.db.rec.term.StudentPreferenceRec;
import dev.mathops.db.reclogic.main.FacilityHoursLogic;
import dev.mathops.db.reclogic.main.FacilityLogic;
import dev.mathops.db.reclogic.term.StandardsCourseGradingSystemLogic;
import dev.mathops.db.reclogic.term.StandardsCourseSectionLogic;
import dev.mathops.db.reclogic.term.StudentPreferenceLogic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A data container for data from the "TERM" schema for use in a single webpage generation or business process.  Data is
 * loaded lazily when queried, and can be "forgotten" to trigger a re-query if underlying data is changed.
 */
public final class TermData {

    /** The cache. */
    private final Cache cache;

    /** The database to which the cache is connected. */
    private RawWhichDb whichDb = null;

    /** A map from course ID to the list of sections of that course. */
    private final Map<String, List<StandardsCourseSectionRec>> standardsCourseSections;

    /** A map from student ID to that student's preferences. */
    private final Map<String, List<StudentPreferenceRec>> studentPreferences;

    /** The grading systems defined for standards-based courses. */
    private List<StandardsCourseGradingSystemRec> standardsCourseGradingSystems = null;

    /**
     * Constructs a new {@code TermData} with initial capacities appropriate to queries that will involve 2 students or
     * fewer (a common use-case).  Use the constructor that specifies an initial capacity for other use cases.
     *
     * @param theCache the cache
     */
    public TermData(final Cache theCache) {

        this(theCache, 2);
    }

    /**
     * Constructs a new {@code TermData} with initial capacities appropriate to queries that will involve 2 students or
     * fewer (a common use-case).  Use the constructor that specifies an initial capacity for other use cases.
     *
     * @param theCache            the cache
     * @param expectedNbrStudents the expected number of students
     */
    public TermData(final Cache theCache, final int expectedNbrStudents) {

        if (theCache == null) {
            throw new IllegalArgumentException("Cache may not be null");
        }

        this.cache = theCache;
        this.standardsCourseSections = new HashMap<>(6);
        this.studentPreferences = new HashMap<>(expectedNbrStudents);
    }

    /**
     * Gets the database descriptor for the database to which this object's cache is connected.
     *
     * @return the database descriptor
     * @throws SQLException if there is an error accessing the database
     */
    public RawWhichDb getWhichDb() throws SQLException {

        if (this.whichDb == null) {
            final String prefix = this.cache.getSchemaPrefix(ESchema.TERM);
            if (prefix == null) {
                throw new SQLException("Cache has no implementation of the TERM schema.");
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
     * Gets all sections in a standards-based course.
     *
     * @param courseId the course ID
     * @return the list of standards-based course sections
     * @throws SQLException if there is an error accessing the database
     */
    public List<StandardsCourseSectionRec> getStandardsCourseSections(final String courseId) throws SQLException {

        if (this.standardsCourseSections.isEmpty()) {
            final List<StandardsCourseSectionRec> all = StandardsCourseSectionLogic.INSTANCE.queryAll(this.cache);

            for (final StandardsCourseSectionRec rec : all) {
                final List<StandardsCourseSectionRec> list = this.standardsCourseSections.computeIfAbsent(rec.courseId,
                        x -> new ArrayList<>(4));
                list.add(rec);
            }
        }

        return this.standardsCourseSections.get(courseId);
    }

    /**
     * Gets a single standards-based course section by its course ID and section number.
     *
     * @param courseId   the course ID
     * @param sectionNbr the section number
     * @return the standards-based course section record; null if none found
     * @throws SQLException if there is an error accessing the database
     */
    public StandardsCourseSectionRec getStandardsCourseSection(final String courseId, final String sectionNbr)
            throws SQLException {

        StandardsCourseSectionRec result = null;

        final List<StandardsCourseSectionRec> sections = getStandardsCourseSections(courseId);
        if (sections != null) {
            for (final StandardsCourseSectionRec rec : sections) {
                if (rec.sectionNbr.equals(sectionNbr)) {
                    result = rec;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Gets all standards-based course grading systems.
     *
     * @return the list of standards-based course grading systems
     * @throws SQLException if there is an error accessing the database
     */
    public List<StandardsCourseGradingSystemRec> getStandardsCourseGradingSystems() throws SQLException {

        if (this.standardsCourseGradingSystems == null) {
            this.standardsCourseGradingSystems = StandardsCourseGradingSystemLogic.INSTANCE.queryAll(this.cache);
        }

        return this.standardsCourseGradingSystems;
    }

    /**
     * Gets a single standards-based course grading system by its ID.
     *
     * @param gradingSystemId the grading system ID
     * @return the grading system record; null if none found
     * @throws SQLException if there is an error accessing the database
     */
    public StandardsCourseGradingSystemRec getStandardsCourseGradingSystem(final String gradingSystemId)
            throws SQLException {

        StandardsCourseGradingSystemRec result = null;

        final List<StandardsCourseGradingSystemRec> all = getStandardsCourseGradingSystems();

        for (final StandardsCourseGradingSystemRec rec : all) {
            if (rec.gradingSystemId.equals(gradingSystemId)) {
                result = rec;
                break;
            }
        }

        return result;
    }

    /**
     * Gets all student preferences for a single student.
     *
     * @param studentId the student ID for which to query
     * @return the list of preferences for that student
     * @throws SQLException if there is an error accessing the database
     */
    public List<StudentPreferenceRec> getStudentPreferences(final String studentId) throws SQLException {

        List<StudentPreferenceRec> result = this.studentPreferences.get(studentId);

        if (result == null) {
            result = StudentPreferenceLogic.INSTANCE.queryByStudent(this.cache, studentId);
            this.studentPreferences.put(studentId, result);
        }

        return result;
    }

    /**
     * Gets a student preference.
     *
     * @param studentId the student ID for which to query
     * @param prefKey   the preference key
     * @return the preference value; {@code null} if none found
     * @throws SQLException if there is an error accessing the database
     */
    public Integer getStudentPreference(final String studentId, final String prefKey) throws SQLException {

        Integer result = null;

        final List<StudentPreferenceRec> list = getStudentPreferences(studentId);
        if (list != null) {
            for (final StudentPreferenceRec rec : list) {
                if (rec.prefKey.equals(prefKey)) {
                    result = rec.prefValue;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Forgets the preferences for a single student.  This should be called if preference data in the underlying
     * database is updated.
     *
     * @param studentId the student ID whose preferences to forget
     */
    public void forgetStudentPreferences(final String studentId) {

        this.studentPreferences.remove(studentId);
    }

}

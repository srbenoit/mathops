package dev.mathops.session.sitelogic.data;

import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCsection;

import java.sql.SQLException;
import java.util.List;

/**
 * A container for the context-oriented data relating to a {@code SiteData} object.
 */
public final class SiteDataContext {

    /** The student data object. */
    private final StudentData studentData;

    /**
     * Constructs a new {@code SiteDataContext}.
     *
     * @param theStudentData the student data object
     */
    SiteDataContext(final StudentData theStudentData) {

        this.studentData = theStudentData;
    }

    /**
     * Gets the course record for a course in the context.
     *
     * @param courseId the ID of the course
     * @return the course record; null if none found
     */
    public RawCourse getCourse(final String courseId) {

        final SystemData sysData = this.studentData.getSystemData();
        RawCourse result = null;

        try {
            result = sysData.getCourse(courseId);
        } catch (final SQLException ex) {
            Log.severe("Failed to query for course", ex);
        }

        return result;
    }

    /**
     * Gets the course section record for a specified course and section in a specified term.
     *
     * @param courseId the course ID
     * @param section  the section number
     * @param term     the term
     * @return the course section record; null if none found
     */
    public RawCsection getCourseSection(final String courseId, final String section, final TermKey term) {

        final SystemData sysData = this.studentData.getSystemData();
        RawCsection result = null;

        try {
            result = sysData.getCourseSection(courseId, section, term);
        } catch (final SQLException ex) {
            Log.severe("Failed to query for course section", ex);
        }

        return result;
    }

    /**
     * Gets all course section records for a specified course in a specified term.
     *
     * @param courseId the course ID
     * @param term     the term
     * @return the course section record; null if none found
     */
    List<RawCsection> getAllCourseSections(final String courseId, final TermKey term) {

        final SystemData sysData = this.studentData.getSystemData();
        List<RawCsection> result = null;

        try {
            result = sysData.getCourseSectionsByCourse(courseId, term);
        } catch (final SQLException ex) {
            Log.severe("Failed to query for course sections", ex);
        }

        return result;
    }
}

package dev.mathops.session.sitelogic.data;

import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawlogic.RawCourseLogic;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawlogic.RawPacingStructureLogic;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.svc.term.TermLogic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A container for the context-oriented data relating to a {@code SiteData} object.
 */
public final class SiteDataContext {

    /** The data object that owns this object. */
    private final SiteData owner;

    /** In-context Course records. */
    private List<RawCourse> courses;

    /** In-context Course section records. */
    private List<RawCsection> courseSections;

    /** Map from course section to pacing structure record. */
    private Map<RawCsection, RawPacingStructure> coursePacingStructures;

    /**
     * Constructs a new {@code SiteDataContext}.
     *
     * @param theOwner the data object that owns this object
     */
    SiteDataContext(final SiteData theOwner) {

        this.owner = theOwner;

        this.courses = null;
        this.courseSections = null;
        this.coursePacingStructures = null;
    }

    /**
     * Gets the course records corresponding to the context course records.
     *
     * @return the course records (CCourse), with same length and indexing as the list returned by
     *         {@code getContextCourses})
     */
    public List<RawCourse> getCourses() {

        return new ArrayList<>(this.courses);
    }

    /**
     * Gets the course record for a course in the context.
     *
     * @param courseId the ID of the course
     * @return the course (CCourse)
     */
    public RawCourse getCourse(final String courseId) {

        RawCourse result = null;

        if (this.courses != null) {
            for (final RawCourse crs : this.courses) {
                if (courseId.equals(crs.course)) {
                    result = crs;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Gets the course section records corresponding to the context course records.
     *
     * @return the course section records, with same length and indexing as the list returned by
     *         {@code getContextCourses})
     */
    public List<RawCsection> getCourseSections() {

        return new ArrayList<>(this.courseSections);
    }

    /**
     * Queries all database data relevant to a session's effective user ID within the session's context.
     * <p>
     * At the time this method is called; the {@code SiteData} object will have loaded the active term, all calendar
     * records, and all pace track rules.
     *
     * @param cache      the data cache
     * @param ignoreOT   true to skip "OT" section of indicated courses
     * @param theCourses a list of courses to include
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    boolean loadData(final Cache cache, final boolean ignoreOT, final String... theCourses)
            throws SQLException {

        return loadCoursesInContext(cache, theCourses) //
                && loadRawCsectionsInContext(cache, ignoreOT, theCourses) //
                && loadCourseRuleSetsInContext(cache);
    }

    /**
     * Populates course records for all courses included in the context.
     *
     * @param cache      the data cache
     * @param theCourses the list of courses to include
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    private boolean loadCoursesInContext(final Cache cache, final String... theCourses)
            throws SQLException {

        this.courses = new ArrayList<>(theCourses.length);

        boolean success = true;
        for (final String courseId : theCourses) {
            final RawCourse course = RawCourseLogic.query(cache, courseId);

            if (course == null) {
                this.owner.setError("Course " + courseId + " not found");
                success = false;
                break;
            }

            this.courses.add(course);
        }

        return success;
    }

    /**
     * Populates course section records for all courses included in the context.
     *
     * @param cache      the data cache
     * @param ignoreOT   true to ignore OT sections of courses
     * @param theCourses the list of courses to include
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if there was an error accessing the database
     */
    private boolean loadRawCsectionsInContext(final Cache cache, final boolean ignoreOT,
                                              final String... theCourses) throws SQLException {

        this.courseSections = new ArrayList<>(theCourses.length << 2);

        final TermKey key = TermLogic.get(cache).queryActive(cache).term;

        final List<RawCsection> all = RawCsectionLogic.queryByTerm(cache, key);

        final boolean success = true;

        for (final String course : theCourses) {
            for (final RawCsection test : all) {
                if (ignoreOT && "OT".equals(test.instrnType)) {
                    continue;
                }

                if (test.course.equals(course)) {
                    this.courseSections.add(test);
                }
            }
        }

        return success;
    }

    /**
     * Populates course pacing structure records for all courses included in the context.
     *
     * @param cache the data cache
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    private boolean loadCourseRuleSetsInContext(final Cache cache) throws SQLException {

        this.coursePacingStructures = new HashMap<>(this.courseSections.size());

        final Iterator<RawCsection> iter = this.courseSections.iterator();
        while (iter.hasNext()) {
            final RawCsection csect = iter.next();
            if ("OT".equals(csect.instrnType)) {
                continue;
            }

            final String ruleSetId = csect.pacingStructure;

            if (ruleSetId == null) {
                Log.warning("No pacing structure configured for course ", csect.course,
                        " section ", csect.sect);
                iter.remove();
            } else {
                final RawPacingStructure record = RawPacingStructureLogic.query(cache, ruleSetId);

                if (record == null) {
                    Log.warning("Unable to query for pacing structure ", ruleSetId, " for course ", csect.course,
                            " section ", csect.sect);
                    iter.remove();
                } else {
                    this.coursePacingStructures.put(csect, record);
                }
            }
        }

        return true;
    }
}

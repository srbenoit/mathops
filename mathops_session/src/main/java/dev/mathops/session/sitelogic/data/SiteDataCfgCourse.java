package dev.mathops.session.sitelogic.data;

import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawPacingRules;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawPacingStructure;

import java.sql.SQLException;
import java.util.List;

/**
 * A container for the configuration records associated with a course.
 */
public final class SiteDataCfgCourse {

    /** The owning {@code SiteData}. */
    private final SiteData owner;

    /** The course record. */
    public final RawCourse course;

    /** The course section record. */
    public final RawCsection courseSection;

    /** The rule set record. */
    public final RawPacingStructure pacingStructure;

    /** The pacing rules. */
    public final List<RawPacingRules> pacingRules;

    /** Flag indicating the student must take the user's exam before beginning this course. */
    boolean mustTakeUsersExam;

    /** Flag indicating the student should access the course in practice mode. */
    boolean practiceMode = false;

    /** Flag indicating the student has open access to the entire course, as for tutors. */
    boolean openAccess = false;

    /**
     * Constructs a new {@code SiteDataCfgCourse}.
     *
     * @param cache      the data cache
     * @param courseId   the course ID
     * @param sectionNum the section number
     * @param termKey    the term key in which the course was taken
     * @param siteData   the site data, from which some data may be retrieved without doing new queries
     * @throws SQLException if there is an error accessing the database
     */
    SiteDataCfgCourse(final Cache cache, final String courseId, final String sectionNum,
                      final TermKey termKey, final SiteData siteData) throws SQLException {

        this.owner = siteData;

        final RawCourse theCourse = cache.getSystemData().getCourse(courseId);
        final RawCsection theSect = loadCourseSection(cache, courseId, sectionNum, termKey);

        if (theCourse == null || theSect == null) {
            this.course = null;
            this.courseSection = null;
            this.pacingStructure = null;
            this.pacingRules = null;
        } else {
            this.course = theCourse;
            this.courseSection = theSect;
            if (this.courseSection.pacingStructure == null) {
                if (!"550".equals(this.courseSection.sect)) {
                    Log.warning("NO RULE SET FOR COURSE ",
                            this.courseSection.course, ", SECT ",
                            this.courseSection.sect);
                }
                this.pacingStructure = null;
                this.pacingRules = null;
            } else {
                final SystemData systemData = cache.getSystemData();
                final TermRec active = systemData.getActiveTerm();

                this.pacingStructure = systemData.getPacingStructure(this.courseSection.pacingStructure, active.term);
                this.pacingRules = systemData.getPacingRulesByTermAndPacing(active.term,
                        this.courseSection.pacingStructure);
            }
        }

        this.mustTakeUsersExam = false;
    }

    /**
     * Loads the course section record (or fetches it from existing data if already loaded).
     *
     * @param cache      the data cache
     * @param courseId   the course ID
     * @param sectionNum the section number
     * @param termKey    the term in which the course was taken
     * @return the loaded course record; {@code null} on error
     * @throws SQLException if there is an error accessing the database
     */
    private RawCsection loadCourseSection(final Cache cache, final String courseId, final String sectionNum,
                                          final TermKey termKey) throws SQLException {

        final RawCsection result = cache.getSystemData().getCourseSection(courseId, sectionNum, termKey);

        if (result == null) {
            this.owner.setError("Unable to query for course " + courseId + " section " + sectionNum + " in term "
                    + termKey);
        }

        return result;
    }
}

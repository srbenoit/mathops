package dev.mathops.session.sitelogic.data;

import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.TermKey;
import dev.mathops.db.rawlogic.RawCourseLogic;
import dev.mathops.db.rawlogic.RawCsectionLogic;
import dev.mathops.db.rawlogic.RawPacingStructureLogic;
import dev.mathops.db.rawrecord.RawCourse;
import dev.mathops.db.rawrecord.RawCsection;
import dev.mathops.db.rawrecord.RawPacingStructure;

import java.sql.SQLException;

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

    /** Flag indicating the student must take the user's exam before beginning this course. */
    boolean mustTakeUsersExam;

    /** Flag indicating the student should access the course in practice mode. */
    boolean practiceMode;

    /** Flag indicating the student has open access to the entire course, as for tutors. */
    boolean openAccess;

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

        final RawCourse theCourse = RawCourseLogic.query(cache, courseId);
        final RawCsection theSect = loadCourseSection(cache, courseId, sectionNum, termKey);

        if (theCourse == null || theSect == null) {
            this.course = null;
            this.courseSection = null;
            this.pacingStructure = null;
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
            } else {
                this.pacingStructure =
                        RawPacingStructureLogic.query(cache, this.courseSection.pacingStructure);
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
    private RawCsection loadCourseSection(final Cache cache, final String courseId,
                                          final String sectionNum, final TermKey termKey) throws SQLException {

        RawCsection theCourseSection = null;

        // See if the course section has already been loaded
        for (final RawCsection ctxCrs : this.owner.contextData.getCourseSections()) {
            if (courseId.equals(ctxCrs.course) && courseId.equals(ctxCrs.sect)
                    && termKey.equals(ctxCrs.termKey)) {
                theCourseSection = ctxCrs;
                break;
            }
        }

        if (theCourseSection == null) {
            theCourseSection = RawCsectionLogic.query(cache, courseId, sectionNum, termKey);
            if (theCourseSection == null) {
                this.owner.setError("Unable to query for course " + courseId + " section "
                        + sectionNum + " in term " + termKey);
            }
        }

        return theCourseSection;
    }
}

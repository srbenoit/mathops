package dev.mathops.session.sitelogic.servlet;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Marks a course as being in-progress.
 */
public final class StartCourse extends LogicBase {

    /**
     * Constructs a new {@code StartCourse}.
     *
     * @param theDbProfile the database profile under which this site is accessed
     */
    public StartCourse(final DbProfile theDbProfile) {

        super(theDbProfile);
    }

    /**
     * Performs the database tests and updates to mark a course as open.
     *
     * @param cache        the data cache
     * @param now          the date/time to consider as "now"
     * @param theStudentId the student ID
     * @param theCourse    the course
     * @return {@code true} if successful; {@code false} on any error
     * @throws SQLException if there was an error accessing the database
     */
    public boolean startCourse(final Cache cache, final ZonedDateTime now, final String theStudentId,
                               final String theCourse) throws SQLException {

        if ("GUEST".equals(theStudentId) || "AACTUTOR".equals(theStudentId) || theStudentId.startsWith("99")) {
            setErrorText("Guest logins may not start new courses");
            return false;
        }

        final RawStudent stu = RawStudentLogic.query(cache, theStudentId, true);
        if (stu == null) {
            setErrorText("Unable to query student record");
            return false;
        }

        final SystemData systemData = cache.getSystemData();

        // Load the current active term
        final TermRec activeTerm = systemData.getActiveTerm();
        if (activeTerm == null) {
            setErrorText("Unable to query the current term.");
            return false;
        }
        final TermKey key = activeTerm.term;

        // Get all course sections the student has available
        final List<RawStcourse> current = RawStcourseLogic.queryByStudent(cache, theStudentId, key, true, false);
        if (current == null) {
            setErrorText("Unable to look up course registrations.");
            return false;
        }

        // From this list, extract all that are open & not completed, then set them to null to avoid further
        // consideration. Also, set any that are completed or open="N" to null to avoid consideration of those courses
        // for availability.
        RawStcourse stCourse = null;
        int numOpen = 0;

        // Count the number of open courses that are "counted" toward max open, and while we loop, try to find the
        // registration for the course the user wants to open

        for (final RawStcourse stc : current) {
            final RawCsection csect = systemData.getCourseSection(stc.course, stc.sect, key);

            if (csect == null) {
                continue;
            }

            if (theCourse.equals(stc.course)) {
                stCourse = stc;
            }

            if ("Y".equals(stc.openStatus) && "Y".equals(csect.countInMaxCourses) && "N".equals(stc.completed)) {
                ++numOpen;
            }
        }

        if (stCourse == null) {
            setErrorText("Unable to look up course registration.");
            return false;
        }

        // If the course is already open, do nothing (but succeed, so the menu will get
        // refreshed, and it will appear right).
        if ("Y".equals(stCourse.openStatus)) {
            return true;
        }

        // If the student record has no rule set, use the structure designated for the course.
        if (stu.pacingStructure == null) {

            final RawCsection sect = systemData.getCourseSection(theCourse, stCourse.sect, key);
            if (sect == null) {
                setErrorText("No data for course section.");
                return false;
            }

            // Don't let students open courses that have not yet started.
            if (sect.startDt != null) {
                final LocalDate today = now.toLocalDate();
                final LocalDate start = sect.startDt;

                if (today.isBefore(start)) {
                    setErrorText("Course begins " + TemporalUtils.FMT_MDY.format(sect.startDt) + CoreConstants.DOT);
                    return false;
                }
            }

            if (sect.pacingStructure != null) {
                Log.info("Setting student pacing to ", sect.pacingStructure, " as student starts course ", theCourse);
                RawStudentLogic.updatePacingStructure(cache, stu.stuId, sect.pacingStructure);
            }
        }

        final RawPacingStructure pacingStructure = systemData.getPacingStructure(stu.pacingStructure, activeTerm.term);
        if (pacingStructure == null) {
            setErrorText("Unable to look up rule set.");
            return false;
        }

        if (pacingStructure.nbrOpenAllowed == null) {
            setErrorText("Rule set does not define max open courses");
            return false;
        }

        if (numOpen >= pacingStructure.nbrOpenAllowed.intValue()) {
            // Max number of open courses are already open
            setErrorText("You may only have " + pacingStructure.nbrOpenAllowed + " courses open at a time.");
            return false;
        }

        // TODO: Use exception student table to check for course

        if (RawStcourseLogic.updateOpenStatusAndFinalClassRoll(cache, stCourse.stuId, stCourse.course, stCourse.sect,
                stCourse.termKey, "Y", stCourse.finalClassRoll, stCourse.lastClassRollDt)) {
            stCourse.openStatus = "Y";
        }

        return "Y".equals(stCourse.openStatus);
    }

    ///**
    // * Main method to execute the bean for a particular session and course.
    // *
    // * @param args Command-line arguments.
    // */
    // public static void main(final String... args) {
    //
    // ContextMap.getDefaultInstance();
    // DbConnection.registerDrivers();
    //
    // final Context ctx = Context.get(ContextList.PRECALC_HOST, ContextList.INSTRUCTION);
    // final StartCourse start = new StartCourse(ctx);
    // Log.info(Boolean.toString(start.startCourse(ZonedDateTime.now(), "1112223333",
    // RawRecordConstants.M126)));
    // Log.info(start.getErrorText());
    // }
}

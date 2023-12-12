package dev.mathops.db.old.logic;

import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawCampusCalendarLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A container for logic relating to a student's status with respect to the Winter Precalculus Tutorial.
 *
 * <p>
 * A student is eligible for a Precalculus Tutorial over Winter break if they were enrolled in the course during the
 * Fall semester, did not complete that course (or completed a MATH 124 o4 126 course with a grade lower than "B"), and
 * did sufficient work to qualify for access to this tutorial. This is handled outside this class - students are scanned
 * at the end of the Fall term and installed in the "special_stus" table with the following keys to provide access to
 * the Winter Precalculus Tutorial:
 *
 * <ul>
 * <li><b>M 1170W</b> - Allowed to access the M 1170 Tutorial
 * <li><b>M 1180W</b> - Allowed to access the M 1180 Tutorial
 * <li><b>M 1240W</b> - Allowed to access the M 1240 Tutorial
 * <li><b>M 1250W</b> - Allowed to access the M 1250 Tutorial
 * <li><b>M 1260W</b> - Allowed to access the M 1260 Tutorial
 * </ul>
 * <p>
 * In addition, students with <b>ADMIN</b> or <b>TUTOR</b> special_stus records can access all five
 * tutorials.
 */
final class WinterTutorialLogic {

    /** The student's status with respect to the winter precalculus tutorial. */
    private final WinterTutorialStatus status;

    /**
     * Constructs a new {@code WinterTutorialLogic}.
     *
     * @param cache        the data cache
     * @param theStudentId the student ID
     * @param today        the date/time to consider "now"
     * @throws SQLException if there is an error accessing the database
     */
    private WinterTutorialLogic(final Cache cache, final String theStudentId, final LocalDate today)
            throws SQLException {

        final RawStudent student = RawStudentLogic.query(cache, theStudentId, false);

        this.status = new WinterTutorialStatus(student);
        this.status.allSpecials.addAll(RawSpecialStusLogic.queryActiveByStudent(cache, theStudentId, today));

        computeStatus(cache, today);
    }

    /**
     * Computes the status over all tutorials.
     *
     * <p>
     * The Winter Precalculus Tutorial is available if the following conditions are satisfied:
     * <ul>
     * <li>The student has a Math Placement attempt on record
     * <li>The student is eligible for the corresponding course
     * <li>The student has not placed out of or earned credit in the corresponding course
     * </ul>
     *
     * @param cache the data cache
     * @param today the current day
     * @throws SQLException if there is an error accessing the database
     */
    private void computeStatus(final Cache cache, final LocalDate today) throws SQLException {

        boolean pct117 = false;
        boolean pct118 = false;
        boolean pct124 = false;
        boolean pct125 = false;
        boolean pct126 = false;

        // "Normal" eligibility: student's application term is after the current term (or equal to the current
        // term and the current date is before the "last add" date in that term), and the student is eligible for
        // the corresponding course.

        final RawStudent stu = this.status.getStudent();

        final TermKey aplnTerm = stu.aplnTerm;
        final TermRec activeTerm = TermLogic.get(cache).queryActive(cache);

        // "comparison" will be negative if active term is before application term (in which case student is eligible),
        // or 0 if they are the same (in which case student is eligible if today is before the last add date)
        final int comparison = activeTerm.term.compareTo(aplnTerm);
        boolean eligibleByAplnTerm = false;
        if (comparison < 0) {
            eligibleByAplnTerm = true;
        } else if (comparison == 0) {
            if (!today.isAfter(activeTerm.dropDeadline)) {
                eligibleByAplnTerm = true;
            }
        }

        if (eligibleByAplnTerm) {
            final PrerequisiteLogic prereqLogic = new PrerequisiteLogic(cache, stu.stuId);

            // Find the lowest-numbered course for which they have the prereq but not credit
            if (prereqLogic.hasSatisfiedPrereqsFor(RawRecordConstants.M117)) {
                if (!prereqLogic.hasCreditFor(RawRecordConstants.M117)) {
                    pct117 = true;
                } else if (prereqLogic.hasSatisfiedPrereqsFor(RawRecordConstants.M118)) {
                    if (!prereqLogic.hasCreditFor(RawRecordConstants.M118)) {
                        pct118 = true;
                    } else if (prereqLogic.hasSatisfiedPrereqsFor(RawRecordConstants.M124)) {
                        if (!prereqLogic.hasCreditFor(RawRecordConstants.M124)) {
                            pct124 = true;
                        } else if (prereqLogic.hasSatisfiedPrereqsFor(RawRecordConstants.M125)) {
                            if (!prereqLogic.hasCreditFor(RawRecordConstants.M125)) {
                                pct125 = true;
                            } else if (prereqLogic.hasSatisfiedPrereqsFor(RawRecordConstants.M126)) {
                                if (!prereqLogic.hasCreditFor(RawRecordConstants.M126)) {
                                    pct126 = true;
                                }
                            }
                        }
                    }
                }
            } else {
                // Has not satisfied 117 prereq - let them in, they will get ELM as first part of Tutorial
                if (!prereqLogic.hasCreditFor(RawRecordConstants.M117)) {
                    pct117 = true;
                }
            }
        }

        // "Special" eligibility: student in special student categories can access

        for (final RawSpecialStus spec : this.status.allSpecials) {
            if (spec.isActive(today)) {
                if ("ADMIN".equals(spec.stuType) || ("TUTOR".equals(spec.stuType))) {
                    pct117 = true;
                    pct118 = true;
                    pct124 = true;
                    pct125 = true;
                    pct126 = true;
                } else if ("M 1170W".equals(spec.stuType)) {
                    pct117 = true;
                } else if ("M 1180W".equals(spec.stuType)) {
                    pct118 = true;
                } else if ("M 1240W".equals(spec.stuType)) {
                    pct124 = true;
                } else if ("M 1250W".equals(spec.stuType)) {
                    pct125 = true;
                } else if ("M 1260W".equals(spec.stuType)) {
                    pct126 = true;
                }
            }
        }

        if (pct117) {
            this.status.eligibleTutorials.add(RawRecordConstants.M1170);
        }
        if (pct118) {
            this.status.eligibleTutorials.add(RawRecordConstants.M1180);
        }
        if (pct124) {
            this.status.eligibleTutorials.add(RawRecordConstants.M1240);
        }
        if (pct125) {
            this.status.eligibleTutorials.add(RawRecordConstants.M1250);
        }
        if (pct126) {
            this.status.eligibleTutorials.add(RawRecordConstants.M1260);
        }

        this.status.holds.addAll(RawAdminHoldLogic.queryByStudent(cache, this.status.getStudent().stuId));

        LocalDate start = null;
        LocalDate end = null;
        final List<RawCampusCalendar> calendars = RawCampusCalendarLogic.INSTANCE.queryAll(cache);
        for (final RawCampusCalendar cal : calendars) {
            if (RawCampusCalendar.DT_DESC_WINTER_PCT_START.equals(cal.dtDesc)) {
                start = cal.campusDt;
            } else if (RawCampusCalendar.DT_DESC_WINTER_PCT_END.equals(cal.dtDesc)) {
                end = cal.campusDt;
            }
        }

        final List<DateRange> web = new ArrayList<>(1);
        if (start != null && end != null) {
            web.add(new DateRange(start, end));
        }
        this.status.webSiteAvailability = new DateRangeGroups(web, today);
    }

    /**
     * Main method to exercise the logic object.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final ContextMap map = ContextMap.getDefaultInstance();
        DbConnection.registerDrivers();

        final DbProfile dbProfile = Objects.requireNonNull(
                map.getWebSiteProfile(Contexts.PLACEMENT_HOST, Contexts.ROOT_PATH)).dbProfile;

        final DbContext ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final WinterTutorialLogic logic = new WinterTutorialLogic(cache, "888888888", LocalDate.now());

                mainPrintStatus(logic.status);
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Prints the contents of a {@code WinterTutorialStatus}.
     *
     * @param status the {@code WinterTutorialStatus} whose contents to print
     */
    private static void mainPrintStatus(final WinterTutorialStatus status) {

        Log.fine("Student ", status.getStudent().stuId);

        final Set<String> courses = status.eligibleTutorials;
        if (courses.isEmpty()) {
            Log.fine(" Not Eligible for any Winter Precalc Tutorials during Winter break");
        } else {
            for (final String s : courses) {
                Log.fine(" Eligible for Winter Precalc Tutorial : ", s);
            }
        }

        final DateRangeGroups site = status.webSiteAvailability;
        for (final DateRange r : site.past) {
            Log.fine(" Winter Precalc site was available : ", r);
        }
        if (site.current != null) {
            Log.fine(" Winter Precalc site is available : ", site.current);
        }
        for (final DateRange r : site.future) {
            Log.fine(" Winter Precalc site will be available : ", r);
        }
    }
}

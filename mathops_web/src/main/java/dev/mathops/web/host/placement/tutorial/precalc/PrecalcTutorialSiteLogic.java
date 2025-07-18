package dev.mathops.web.host.placement.tutorial.precalc;

import dev.mathops.commons.log.Log;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.course.PrerequisiteLogic;
import dev.mathops.db.logic.tutorial.PrecalcTutorialLogic;
import dev.mathops.db.logic.tutorial.PrecalcTutorialStatus;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.session.ImmutableSessionInfo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A container for data abd business logic associated with the Precalculus Tutorials.
 */
final class PrecalcTutorialSiteLogic {

    /** The student ID. */
    private final String studentId;

    /** The active term. */
    private TermRec activeTerm;

    /** The student record. */
    private RawStudent student;

    /** Flag indicating student has attempted the Math Placement Tool. */
    private boolean attemptedPlacement;

    /** Flag indicating student has "tutor" access and can see all tutorials. */
    private boolean tutorAccess;

    /** The course for which the student is eligible; {@code null} if none. */
    private RawCourse eligibleCourse;

    /** The list of all special student categories. */
    private List<RawSpecialStus> specials;

    /** The list of all holds on the student account. */
    private List<RawAdminHold> studentHolds;

    /** Prerequisite status. */
    private final PrerequisiteLogic prereqLogic;

    /**
     * Constructs a new {@code PrecalcTutorialSiteLogic}.
     *
     * @param session the login session
     */
    PrecalcTutorialSiteLogic(final ImmutableSessionInfo session, final Cache theCache) {

        this.studentId = session.getEffectiveUserId();

        try {
            this.activeTerm = theCache.getSystemData().getActiveTerm();
        } catch (final SQLException ex) {
            Log.warning("Failed to query active term", ex);
        }

        try {
            this.student = RawStudentLogic.query(theCache, this.studentId, false);
        } catch (final SQLException ex) {
            Log.warning("Failed to query student data", ex);
        }

        final LocalDate today = session.getNow().toLocalDate();
        try {
            this.tutorAccess = "AACTUTOR".equals(this.studentId)
                    || RawSpecialStusLogic.isSpecialType(theCache, this.studentId, today,
                    RawSpecialStus.TUTOR, RawSpecialStus.ADMIN, RawSpecialStus.M384);
        } catch (final SQLException ex) {
            Log.warning("Failed to test for tutor access", ex);
        }

        try {
            final List<RawStmpe> placementTries = RawStmpeLogic.queryLegalByStudent(theCache, this.studentId);
            this.attemptedPlacement = !placementTries.isEmpty();
        } catch (final SQLException ex) {
            Log.warning("Failed to test for Math Placement attempts", ex);
        }

        // Gather a list of courses that the student has either (1) placed out of, (2) earned credit for, or (3)
        // has transfer credit for
        PrerequisiteLogic localPrereqLogic = null;
        try {
            localPrereqLogic = new PrerequisiteLogic(theCache, this.studentId);

            final PrecalcTutorialLogic tutorialLogic = new PrecalcTutorialLogic(theCache, this.studentId, today,
                    localPrereqLogic);
            final PrecalcTutorialStatus status = tutorialLogic.status;

            final String courseId = status.nextPrecalcTutorial == null ? null : status.nextPrecalcTutorial;
            if (courseId != null) {
                this.eligibleCourse = theCache.getSystemData().getCourse(courseId);
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to check student prerequisite status", ex);
        }
        this.prereqLogic = localPrereqLogic;

        try {
            this.specials = RawSpecialStusLogic.queryActiveByStudent(theCache, this.studentId, today);
        } catch (final SQLException ex) {
            Log.warning("Failed to query special student categories", ex);
            this.specials = new ArrayList<>(0);
        }

        try {
            this.studentHolds = RawAdminHoldLogic.queryByStudent(theCache, this.studentId);
        } catch (final SQLException ex) {
            Log.warning("Failed to query student holds", ex);
            this.studentHolds = new ArrayList<>(0);
        }
    }

    /**
     * Gets the student ID.
     *
     * @return the student ID
     */
    public String getStudentId() {

        return this.studentId;
    }

    /**
     * Gets the active term record.
     *
     * @return the active term record
     */
    public TermRec getActiveTerm() {

        return this.activeTerm;
    }

    /**
     * Gets the student record.
     *
     * @return the student record
     */
    public RawStudent getStudent() {

        return this.student;
    }

//    /**
//     * Tests whether this student has attempted the Math Placement tool.
//     *
//     * @return {@code true} if student has attempted the tool
//     */
//    boolean hasAttemptedPlacement() {
//
//        return this.attemptedPlacement;
//    }

    /**
     * Tests whether this student has "TUTOR" access, and can see all tutorials.
     *
     * @return {@code true} if student has TUTOR access
     */
    boolean hasTutorAccess() {

        return this.tutorAccess;
    }

    /**
     * Tests whether the student is eligible for the Precalculus Tutorial based on application term.
     *
     * @param today today's date
     * @return {@code true} if eligible
     */
    boolean isEligible(final ChronoLocalDate today) {

        boolean result = false;

        if (isPossiblyEligible()) {
            final TermKey activeTermKey = this.activeTerm.term;
            TermKey aplnTerm = this.student.aplnTerm;

            if (aplnTerm != null) {
                for (final RawSpecialStus spec : this.specials) {
                    final String type = spec.stuType;

                    if ("PLCSP".equals(type)) {
                        // Force a "SPRING" application term
                        if (aplnTerm.name != ETermName.SPRING) {
                            aplnTerm = new TermKey(ETermName.SPRING, aplnTerm.year.intValue() + 1);
                        }
                    } else if ("PLCSM".equals(type)) {
                        // Force a "SUMMER" application term
                        if (aplnTerm.name == ETermName.SPRING) {
                            aplnTerm = new TermKey(ETermName.SUMMER, aplnTerm.year.intValue());
                        } else if (aplnTerm.name == ETermName.FALL) {
                            aplnTerm = new TermKey(ETermName.SUMMER, aplnTerm.year.intValue() + 1);
                        }
                    } else if ("PLCFA".equals(type) || "PCT117".equals(type)
                            || "PCT118".equals(type) || "PCT124".equals(type)
                            || "PCT125".equals(type) || "PCT126".equals(type)) {

                        // Force a "FALL" application term
                        if (aplnTerm.name != ETermName.FALL) {
                            aplnTerm = new TermKey(ETermName.FALL, aplnTerm.year.intValue());
                        }
                    }
                }

                final int comparison = activeTermKey.compareTo(aplnTerm);
                if (comparison < 0) {
                    // Application term is in the future
                    result = true;
                } else if (comparison == 0) {
                    // Application term is in current term - eligible if before free drop date
                    result = !today.isAfter(this.activeTerm.dropDeadline);
                }
            }
        }

        return result;
    }

    /**
     * Tests whether the student might be eligible.  This tests that the student has completed Math placement, and we
     * have data on the active term and the student's application term.
     *
     * @return {@code true} if the student might be eligible
     */
    private boolean isPossiblyEligible() {

        return this.attemptedPlacement && this.activeTerm != null && this.student != null
                && this.student.aplnTerm != null;
    }

    /**
     * Determines the deadline date by which the tutorial must be completed. This is the drop deadline for the student's
     * application term.
     *
     * @return the deadline date; {@code null} if the deadline cannot be determined
     */
    LocalDate getDeadline(final Cache cache) {

        LocalDate result;

        if (this.activeTerm == null || this.student == null || this.student.aplnTerm == null) {
            Log.warning("Missing active term, student, or application term");
            result = null;
        } else {
            final TermKey activeTermKey = this.activeTerm.term;
            final TermKey applicationTermKey = this.student.aplnTerm;

            final int comparison = activeTermKey.compareTo(applicationTermKey);
            if (comparison < 0) {
                // Application term is in the future
                try {
                    final TermRec appTerm = cache.getSystemData().getTerm(applicationTermKey);
                    if (appTerm == null) {
                        Log.warning(applicationTermKey.shortString, " term record not found");
                        result = null;
                    } else {
                        result = appTerm.dropDeadline;
                        if (result == null) {
                            Log.warning("The ", applicationTermKey.shortString, " term has no drop deadline!");
                        }
                    }
                } catch (final SQLException ex) {
                    Log.warning("Failed to query for ", applicationTermKey.shortString, " term record");
                    result = null;
                }
            } else if (comparison == 0) {
                // Application term is in current term - eligible if before free drop date
                result = this.activeTerm.dropDeadline;
                if (result == null) {
                    Log.warning("The active term has no drop deadline!");
                }
            } else {
                Log.warning("Application term is in the past - no deadline date");
                result = null;
            }
        }

        return result;
    }

    /**
     * Tests whether the student has placed into MATH 117 (in which case they get a shorter Skills Review).
     *
     * @return {@code true} if student has placed into MATH 117
     */
    boolean hasPlacedInto117() {

        return this.prereqLogic.hasSatisfiedPrerequisitesFor(RawRecordConstants.M117);
    }

    /**
     * Retrieves the course for which the student is eligible to complete the tutorial.
     *
     * @return the eligible course; {@code null} if the student has already placed out of or earned credit in all
     *         courses
     */
    RawCourse getEligibleCourse() {

        return this.eligibleCourse;
    }

    /**
     * Retrieves the list of holds on the student account.
     *
     * @return the list of holds
     */
    List<RawAdminHold> getStudentHolds() {

        return Collections.unmodifiableList(this.studentHolds);
    }

    /**
     * Gets the name of the course associated with a tutorial course number.
     *
     * @param tutorialCourse the tutorial course number
     * @return the associated course name
     */
    static String getAssociatedCourse(final RawCourse tutorialCourse) {

        return switch (tutorialCourse.course) {
            case RawRecordConstants.M1170 -> "MATH 117";
            case RawRecordConstants.M1180 -> "MATH 118";
            case RawRecordConstants.M1240 -> "MATH 124";
            case RawRecordConstants.M1250 -> "MATH 125";
            case null, default -> "MATH 126";
        };
    }
}

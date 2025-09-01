package dev.mathops.session.sitelogic.bogus;

import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStetextLogic;
import dev.mathops.db.schema.legacy.RawEtext;
import dev.mathops.db.schema.legacy.RawEtextCourse;
import dev.mathops.db.schema.legacy.RawSpecialStus;
import dev.mathops.db.schema.legacy.RawStetext;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.schema.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Manages e-texts purchased by students.
 */
public enum ETextLogic {
    ;

//    /** A formatter that can parse LocalDateTime values. */
//    private static final DateTimeFormatter PARSE_FMT =
//            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

    /**
     * Tests whether a student has an e-text purchased that grants that student access to a particular course as of a
     * given date/time.
     *
     * @param cache    the data cache
     * @param session  the login session
     * @param courseId the course ID
     * @return {@code true} if the student has access to the specified course at the specified date/time; {@code false}
     *         if not or if an error occurred
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean canStudentAccessCourse(final Cache cache, final ImmutableSessionInfo session,
                                                 final String courseId) throws SQLException {

        final String studentId = session.getEffectiveUserId();
        boolean canAccess = false;

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)
                || "GUEST".equalsIgnoreCase(studentId)
                || "AACTUTOR".equals(studentId)) {

            // Policy: Administrative or guest IDs don't need to purchase textbook
            canAccess = true;
        } else {
            final TermRec activeTerm = cache.getSystemData().getActiveTerm();

            if (activeTerm != null) {

                // Some special student types do not need book purchase
                final ZonedDateTime now = session.getNow();
                final LocalDate today = now.toLocalDate();

                final List<RawSpecialStus> specials = RawSpecialStusLogic.queryActiveByStudent(cache, studentId, today);

                for (final RawSpecialStus spec : specials) {
                    final String type = spec.stuType;

                    if ("TUTOR".equals(type) || "ADMIN".equals(type) || "M384".equals(type)) {
                        canAccess = true;
                        break;
                    }
                }

                final Boolean reqEtext = cache.getSystemData().isETextRequired(courseId);
                if (reqEtext != null) {
                    if (reqEtext.booleanValue()) {
                        final List<RawStetext> active = RawStetextLogic.getStudentETexts(cache, now, studentId,
                                courseId);
                        canAccess = canAccess || !active.isEmpty();
                    } else {
                        canAccess = true;
                    }
                }
            }
        }

        return canAccess;
    }

    /**
     * Called when a course is completed. This method scans for any e-text records that grant the student access to the
     * indicated course. If there is a non-refunded and active record whose retention is 'C' that grants access to the
     * course, then this method searches for an e-text whose retention is 'Y' that would grant access to just this
     * course, and inserts a new record for that e-text for the student with a key and an activation date equal to that
     * of the 'C' record, with no expiration, and a refund deadline date in the past. It also updated the refund
     * deadline date on the 'C' record to be in the past if that date is in the future to prevent refunds after
     * completion of a course.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @param courseId  the course ID
     * @param now       the date/time to consider "now"
     * @throws SQLException if there is an error accessing the database
     */
    public static void courseCompleted(final Cache cache, final String studentId, final String courseId,
                                       final ZonedDateTime now) throws SQLException {

        // Completing a tutorial grants permanent access to that course's e-text
        final String actualCourseId = switch (courseId) {
            case RawRecordConstants.M1170 -> RawRecordConstants.M117;
            case RawRecordConstants.M1180 -> RawRecordConstants.M118;
            case RawRecordConstants.M1240 -> RawRecordConstants.M124;
            case RawRecordConstants.M1250 -> RawRecordConstants.M125;
            case RawRecordConstants.M1260 -> RawRecordConstants.M126;
            case null, default -> courseId;
        };

        final List<RawStetext> active = RawStetextLogic.getStudentETexts(cache, now, studentId, actualCourseId);

        final SystemData systemData = cache.getSystemData();

        // See if there is a record with retention 'C':
        RawStetext toChange = null;
        for (final RawStetext act : active) {
            final RawEtext etext = systemData.getEText(act.etextId);

            if (etext != null && "C".equals(etext.retention)) {
                toChange = act;
                break;
            }
        }

        // See whether there exists an e-text with retention "Y" to convert to
        RawEtext newEtext = null;
        for (final RawEtext etext : systemData.getETexts()) {
            if ("Y".equals(etext.retention)) {
                final List<RawEtextCourse> courses = systemData.getETextCoursesByETextId(etext.etextId);

                if (courses.size() == 1 && actualCourseId.equals(courses.getFirst().course)) {
                    newEtext = etext;
                    break;
                }
            }
        }

        if (newEtext != null) {

            final String key;
            final LocalDate activeDate;
            final LocalDate refundDeadline;

            if (toChange == null) {
                // Student did not have an e-text row - assume "inclusive access"
                key = "InclAccess";
                activeDate = now.toLocalDate();
                refundDeadline = now.toLocalDate().minusDays(1L);
            } else {
                // Preserve the key and activation date from the original record, make refund
                // deadline in the past
                key = toChange.etextKey;
                activeDate = toChange.activeDt;

                // If the original refund deadline is in the past, keep it. Otherwise, mark
                // the refund date for the new row as yesterday to prevent refunds.
                final LocalDate curDate = toChange.refundDeadlineDt;
                if (curDate != null && curDate.isBefore(now.toLocalDate())) {
                    refundDeadline = curDate;
                } else {
                    refundDeadline = now.toLocalDate().minusDays(1L);
                }
            }

            // Create a new e-text with retention "Y". Preserve the key and activation date from the
            // original record, make refund deadline in the past

            if (toChange == null) {
                final RawStetext record = new RawStetext(studentId, newEtext.etextId, activeDate,
                        key, null, refundDeadline, null, null);

                RawStetextLogic.insert(cache, record);
            } else {
                final RawStetext record = new RawStetext(studentId, newEtext.etextId, activeDate,
                        key, toChange.expirationDt, refundDeadline, toChange.refundDt,
                        toChange.refundReason);

                if (RawStetextLogic.insert(cache, record)) {

                    // Now make sure the original record can no longer be refunded
                    final LocalDate deadline = toChange.refundDeadlineDt;

                    if (deadline == null || !deadline.isBefore(now.toLocalDate())) {
                        RawStetextLogic.updateRefundDeadline(cache, toChange.stuId,
                                toChange.etextId, toChange.activeDt, now.toLocalDate().minusDays(1L));
                    }
                }
            }
        } else {
            Log.warning("An e-text had retention 'C', but there is no ",
                    "corresponding e-text with retention 'Y' to which to convert");
        }
    }
}

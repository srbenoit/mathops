package dev.mathops.session.sitelogic.bogus;

import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawlogic.RawCourseLogic;
import dev.mathops.db.old.rawlogic.RawEtextCourseLogic;
import dev.mathops.db.old.rawlogic.RawEtextLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStetextLogic;
import dev.mathops.db.old.rawrecord.RawEtext;
import dev.mathops.db.old.rawrecord.RawEtextCourse;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStetext;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;
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
            final TermRec activeTerm = TermLogic.get(cache).queryActive(cache);

            if (activeTerm != null) {

                // Some special student types do not need book purchase
                final List<RawSpecialStus> specials = RawSpecialStusLogic
                        .queryActiveByStudent(cache, studentId, session.getNow().toLocalDate());

                for (final RawSpecialStus spec : specials) {
                    final String type = spec.stuType;

                    if ("TUTOR".equals(type) || "ADMIN".equals(type) || "M384".equals(type)) {
                        canAccess = true;
                        break;
                    }
                }

                final Boolean reqEtext = RawCourseLogic.isEtextRequired(cache, courseId);
                if (reqEtext != null) {
                    if (reqEtext.booleanValue()) {
                        final List<RawStetext> active = RawStetextLogic.getStudentETexts(cache,
                                session.getNow(), studentId, courseId);
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
    public static void courseCompleted(final Cache cache, final String studentId,
                                       final String courseId, final ZonedDateTime now) throws SQLException {

        // Completing a tutorial grants permanent access to that course's e-text
        final String actualCourseId;
        if (RawRecordConstants.M1170.equals(courseId)) {
            actualCourseId = RawRecordConstants.M117;
        } else if (RawRecordConstants.M1180.equals(courseId)) {
            actualCourseId = RawRecordConstants.M118;
        } else if (RawRecordConstants.M1240.equals(courseId)) {
            actualCourseId = RawRecordConstants.M124;
        } else if (RawRecordConstants.M1250.equals(courseId)) {
            actualCourseId = RawRecordConstants.M125;
        } else if (RawRecordConstants.M1260.equals(courseId)) {
            actualCourseId = RawRecordConstants.M126;
        } else {
            actualCourseId = courseId;
        }

        final List<RawStetext> active = RawStetextLogic.getStudentETexts(cache, now, studentId, actualCourseId);

        // See if there is a record with retention 'C':
        RawStetext toChange = null;
        for (final RawStetext act : active) {
            final RawEtext etext = RawEtextLogic.query(cache, act.etextId);

            if (etext != null && "C".equals(etext.retention)) {
                toChange = act;
                break;
            }
        }

        // See whether there exists an e-text with retention "Y" to convert to
        RawEtext newEtext = null;
        for (final RawEtext etext : RawEtextLogic.INSTANCE.queryAll(cache)) {
            if ("Y".equals(etext.retention)) {
                final List<RawEtextCourse> courses =
                        RawEtextCourseLogic.queryByEtext(cache, etext.etextId);

                if (courses.size() == 1 && actualCourseId.equals(courses.get(0).course)) {
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

                RawStetextLogic.INSTANCE.insert(cache, record);
            } else {
                final RawStetext record = new RawStetext(studentId, newEtext.etextId, activeDate,
                        key, toChange.expirationDt, refundDeadline, toChange.refundDt,
                        toChange.refundReason);

                if (RawStetextLogic.INSTANCE.insert(cache, record)) {

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

//    /**
//     * Called when a student enters a key into the e-text purchase page.
//     *
//     * @param cache             the data cache
//     * @param studentId         the student ID
//     * @param key               the key
//     * @param now               the date/time to consider "now"
//     * @param expiresInNextTerm true to use the end of the subsequent term as the expiration date (if retention is 'C')
//     *                          rather than the end of the current term
//     * @return an error message if activation failed; {@code null} on success
//     * @throws SQLException if there is an error accessing the database
//     */
//    public static String activateEText(final Cache cache, final String studentId, final String key,
//                                       final ZonedDateTime now, final boolean expiresInNextTerm) throws SQLException {
//
//        String error;
//
//        if (key == null || key.isEmpty()) {
//            error = "No key provided";
//        } else if (studentId == null) {
//            error = "No student ID provided";
//        } else {
//            RawEtextKey etextKey = RawEtextKeyLogic.query(cache, key);
//
//            // HACK: Try to correct mistake in bookstore email
//            if (etextKey == null && key.length() == 10 //
//                    && key.toUpperCase().startsWith("CSU")) {
//                etextKey = RawEtextKeyLogic.query(cache, key.substring(3));
//            }
//
//            if (etextKey == null) {
//                error = "The e-Text key was not found";
//            } else if (etextKey.activeDt == null) {
//                // Key was found and is not active
//                error = activateInactiveEText(cache, etextKey, studentId, now, expiresInNextTerm);
//            } else {
//                // Key was found and is already activated - see if it was this student who
//                // activated it.
//                error = activateActiveEText(cache, studentId, key, now);
//            }
//        }
//
//        return error;
//    }

//    /**
//     * Called when activating a new e-text, where the e-text key is not already marked as active. In this case, we
//     * activate the text and place it on the student's record. There are two cases: the student has activated the key
//     * before and is re-activating, in which case we update the existing record; or the student is activating for the
//     * first time, in which case we create a new record.
//     *
//     * @param cache             the data cache
//     * @param etextKey          the CEtextKey model
//     * @param studentId         the student ID
//     * @param now               the date/time to consider as "now"
//     * @param expiresInNextTerm true to use the end of the subsequent term as the expiration date (if retention is 'C')
//     *                          rather than the end of the current term
//     * @return an error message if activation failed; {@code null} on success
//     * @throws SQLException if there is an error accessing the database
//     */
//    private static String activateInactiveEText(final Cache cache, final RawEtextKey etextKey,
//                                                final String studentId, final ZonedDateTime now,
//                                                final boolean expiresInNextTerm)
//            throws SQLException {
//
//        final boolean[] wasReactivate = new boolean[1];
//        final String key = etextKey.etextKey;
//
//        String error = checkForReactivate(cache, etextKey, studentId, now, wasReactivate);
//
//        if (error == null && !wasReactivate[0]) {
//
//            // This is a new activation of a key not on the student's record.
//
//            final RawEtext etext = RawEtextLogic.query(cache, etextKey.etextId);
//            if (etext == null) {
//                error = "Internal error: The key is not associated with any e-texts";
//            } else {
//                // Key is valid and not already activated, so activate it
//
//                // Calculate the refund deadline date
//                LocalDate refundDeadline = null;
//                final Integer refund = etext.refundPeriod;
//                if (refund != null && refund.intValue() >= 0 && refund.intValue() < 1000) {
//                    refundDeadline = now.toLocalDate();
//                    for (int i = 0; i < refund.intValue(); ++i) {
//                        refundDeadline = refundDeadline.plusDays(1);
//                    }
//                }
//
//                final List<RawCampusCalendar> cals =
//                        RawCampusCalendarLogic.queryByType(cache, RawCampusCalendar.DT_DESC_BOOKSTORE);
//
//                for (final RawCampusCalendar cal : cals) {
//                    final LocalDate closeDate = cal.campusDt;
//                    if (closeDate != null) {
//                        if ((refundDeadline == null) || refundDeadline.isAfter(closeDate)) {
//                            refundDeadline = closeDate;
//                        }
//                        break;
//                    }
//                }
//
//                LocalDate expiration = null;
//
//                if ("C".equals(etext.retention)) {
//                    final TermRec active = TermLogic.get(cache).queryActive(cache);
//                    final TermRec term = active.term.name == ETermName.SUMMER && expiresInNextTerm
//                            ? TermLogic.get(cache).queryNext(cache) : active;
//                    expiration = term.endDate;
//                }
//
//                // Create the new student e-text record
//                final RawStetext stetext = new RawStetext(studentId, etextKey.etextId,
//                        now.toLocalDate(), key, expiration, refundDeadline, null, null);
//
//                // If retention is 'C', record should expire at end of current term (or the end
//                // of the subsequent term if this is a precalc tutorial)
//
//                if (!RawStetextLogic.INSTANCE.insert(cache, stetext)) {
//                    error = "Internal error: Activation of e-text failed.";
//                }
//
//                // Finally, mark the e-text as having been activated
//                RawEtextKeyLogic.updateActiveDt(cache, etextKey.etextKey, now.toLocalDateTime());
//            }
//        }
//
//        return error;
//    }

//    /**
//     * Called when activating a new e-text, but the e-text key is found to have already been marked as active. Either
//     * the student owns the text, and is entering the code a second time, or a different student is attempting to use a
//     * code owned by another student.
//     *
//     * @param cache     the data cache
//     * @param studentId the student ID
//     * @param key       the e-text key
//     * @param now       the date/time to consider as "now"
//     * @return an error message if activation failed; {@code null} on success
//     * @throws SQLException if there is an error accessing the database
//     */
//    private static String activateActiveEText(final Cache cache, final String studentId,
//                                              final String key, final ChronoZonedDateTime<LocalDate> now) throws SQLException {
//
//        String error;
//
//        // See if there is an e-text on the student's record with this key
//        final List<RawStetext> etexts = RawStetextLogic.queryByStudent(cache, studentId);
//        RawStetext found = null;
//        for (final RawStetext etext : etexts) {
//            if (key.equals(etext.etextKey)) {
//                found = etext;
//                break;
//            }
//        }
//
//        if (found == null) {
//            // This student does not own the text, so it must already be owned by another
//            error = "The e-Text key has already been activated by another student.";
//        } else {
//            // Either the e-text is currently active for the student, or it is an expired code
//            // the student is attempting to re-activate.
//            final LocalDate expiration = found.expirationDt;
//            final LocalDate refunded = found.refundDt;
//
//            if (refunded == null) {
//                if ((expiration == null) || !now.toLocalDate().isAfter(expiration)) {
//                    // student already has e-text in a valid state, no expiration date
//                    error = "Key has already been entered and e-text is currently available.";
//                } else {
//                    // student has e-text, but it has expired
//                    error = "You have activated this e-text in the past, and it is now expired.";
//                }
//            } else {
//                // student had purchased the e-text, but it was refunded, so the fact that it is currently
//                // marked as active means it must now be owned by another student
//                error = "The e-Text key has already been activated by another student.";
//            }
//        }
//
//        return error;
//    }
//
//    /**
//     * When a key is being activated, this checks whether the key was already activated by the student and then
//     * deactivated. In that case, we want to simply "undo" the deactivation that was done earlier rather than insert a
//     * new student e-text record/.
//     * <p>
//     * This method should only be called after it is verified that the key being activated is not currently activated
//     * elsewhere.
//     *
//     * @param cache           the data cache
//     * @param etextKey        the e-text key record
//     * @param studentId       the student ID
//     * @param now             the date/time to consider as "now"
//     * @param wasReactivation an array of boolean whose [0] element will be set to true if the student was indeed
//     *                        reactivating a code even if an error occurred; false if not
//     * @return {@code true} if the student was reactivating a deactivated key
//     * @throws SQLException if there is an error accessing the database
//     */
//    private static String checkForReactivate(final Cache cache, final RawEtextKey etextKey,
//                                             final String studentId, final ZonedDateTime now,
//                                             final boolean[] wasReactivation)
//            throws SQLException {
//
//        String error = null;
//
//        // Look for an e-text with the same key on the student's record
//        final List<RawStetext> stetexts = RawStetextLogic.queryByStudent(cache, studentId);
//        RawStetext found = null;
//        for (final RawStetext etext : stetexts) {
//            if (Objects.equals(etextKey.etextKey, etext.etextKey)) {
//                found = etext;
//                break;
//            }
//        }
//
//        if (found == null) {
//            // Not a reactivation - no further action within this method
//            wasReactivation[0] = false;
//        } else {
//            // This is a reactivation of an existing key
//            wasReactivation[0] = true;
//
//            if (RawStetextLogic.updateRefund(cache, found.stuId, found.etextId, found.activeDt,
//                    null, null)) {
//
//                RawEtextKeyLogic.updateActiveDt(cache, etextKey.etextKey, now.toLocalDateTime());
//            } else {
//                error = "Internal error: Failed to reactivate e-text key";
//            }
//        }
//
//        return error;
//    }

//    /**
//     * Processes the sale.
//     *
//     * @param cache     the data cache
//     * @param studentId the student ID
//     * @param etextId   the e-text ID
//     * @param dateTime  the date/time of the sale
//     * @param key       the key for the purchased etext
//     * @param now       the date/time to consider as "now"
//     * @return {@code true} if successful; {@code false} otherwise
//     * @throws SQLException if there is an error accessing the database
//     */
//    public static boolean processSale(final Cache cache, final String studentId,
//                                      final String etextId, final String key, final CharSequence dateTime,
//                                      final ZonedDateTime now)
//            throws SQLException {
//
//        final boolean result;
//
//        if (studentId == null) {
//            Log.info("EText.getProcessSale called without a student ID");
//            result = false;
//        } else if (etextId == null) {
//            Log.info("EText.getProcessSale called without a course");
//            result = false;
//        } else if (key == null) {
//            Log.info("EText.getProcessSale called without a key");
//            result = false;
//        } else if (dateTime == null) {
//            Log.info("EText.getProcessSale called without a date/time");
//            result = false;
//        } else {
//            // Parse the date/time
//            LocalDateTime purchaseDateTime;
//
//            try {
//                purchaseDateTime = LocalDateTime.parse(dateTime, PARSE_FMT);
//            } catch (final DateTimeParseException e) {
//                Log.warning("Invalid purchase date/time - using current date/time.");
//                purchaseDateTime = LocalDateTime.now();
//            }
//
//            final LocalDate purchaseDate = purchaseDateTime.toLocalDate();
//            final LocalDate refundDeadline = purchaseDate.plusDays(10);
//
//            final RawStetext text = new RawStetext(studentId, etextId, purchaseDate, key, null,
//                    refundDeadline, null, null);
//
//            final List<RawStetext> active =
//                    RawStetextLogic.getStudentETexts(cache, now, studentId, etextId);
//
//            boolean found = false;
//            for (final RawStetext test : active) {
//                if (test.activeDt.isEqual(purchaseDateTime.toLocalDate())) {
//                    found = true;
//                    break;
//                }
//            }
//
//            if (found) {
//                Log.warning("Duplicate e-text record with same student ",
//                        studentId, ", course ", etextId, ", date found");
//                result = false;
//            } else if (RawStetextLogic.INSTANCE.insert(cache, text)) {
//                Log.warning("Successful e-text purchase by student ",
//                        studentId, " in ", etextId);
//                result = true;
//            } else {
//                result = false;
//            }
//        }
//
//        return result;
//    }

//    /**
//     * Processes the refund.
//     *
//     * @param cache     the data cache
//     * @param studentId the student ID
//     * @param etextId   the e-text ID
//     * @param now       the date/time to consider as "now"
//     * @return {@code true} if successful; {@code false} otherwise.
//     * @throws SQLException if there is an error accessing the database
//     */
//    public static boolean processRefund(final Cache cache, final String studentId,
//                                        final String etextId, final ZonedDateTime now) throws SQLException {
//
//        boolean ok = false;
//
//        // Verify all required data is present.
//        if (studentId == null) {
//            Log.info("EText.processRefund called without student ID");
//        } else if (etextId == null) {
//            Log.info("EText.processRefund called without a course");
//        } else {
//            // See if the student already has a purchased book for the course
//            final List<RawStetext> result =
//                    RawStetextLogic.getStudentETexts(cache, now, studentId, etextId);
//            final RawStetext text = result.isEmpty() ? null : result.get(0);
//
//            if (text == null) {
//                ok = false;
//            } else {
//                ok = RawStetextLogic.deactivate(cache, now.toLocalDateTime(), text,
//                        "Online Refund");
//            }
//        }
//
//        return ok;
//    }
}

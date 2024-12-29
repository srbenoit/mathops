package dev.mathops.app.ops.snapin.messaging.factory;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.app.ops.snapin.messaging.EMilestone;
import dev.mathops.app.ops.snapin.messaging.EMsg;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;
import dev.mathops.app.ops.snapin.messaging.MsgUtils;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;
import dev.mathops.text.builder.HtmlBuilder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * A factory class that can generate messages to students who are "on time" (urgency < 1)
 */
public enum OnTimeMessageFactory {
    ;

    /**
     * Generates an appropriate message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    public static MessageToSend generate(final MessagingContext context, final MessagingCourseStatus status) {

        final LocalDate today = context.today;
        LocalDate plusThree = today;
        for (int i = 0; i < 3; ++i) {
            if (plusThree.getDayOfWeek() == DayOfWeek.FRIDAY) {
                plusThree = plusThree.plusDays(3L);
            } else if (plusThree.getDayOfWeek() == DayOfWeek.SATURDAY) {
                plusThree = plusThree.plusDays(2L);
            } else {
                plusThree = plusThree.plusDays(1L);
            }
        }

        final MessageToSend result;

        if (plusThree.isBefore(status.milestones.r1)) {
            // No message due
            result = null;
        } else if (!today.isAfter(status.milestones.r1)) {
            if (!status.passedRE1 && MsgUtils.hasNoMsgForMilestone(context.messages, EMilestone.RE1)) {
                // R1 due date reminder
                result = generateR1rOK0(context, status);
            } else {
                result = null;
            }
        } else if (plusThree.isBefore(status.milestones.r2)) {
            // No message due
            result = null;
        } else if (!today.isAfter(status.milestones.r2)) {
            if (!status.passedRE2 && MsgUtils.hasNoMsgForMilestone(context.messages, EMilestone.RE2)) {
                // R2 due date reminder
                result = generateR2rOK0(context, status);
            } else {
                result = null;
            }
        } else if (plusThree.isBefore(status.milestones.r3)) {
            // No message due
            result = null;
        } else if (!today.isAfter(status.milestones.r3)) {
            if (MsgUtils.hasNoMsgForMilestone(context.messages, EMilestone.RE3)) {
                if (status.passedRE3) {
                    if (status.passedFIN) {
                        result = null;
                    } else {
                        // Congratulations, how are things going?
                        result = generateR3rOK1(context, status);
                    }
                } else {
                    // R3 due date reminder
                    result = generateR3rOK0(context, status);
                }
            } else {
                result = null;
            }
        } else if (plusThree.isBefore(status.milestones.r4)) {
            // No message due
            result = null;
        } else if (!today.isAfter(status.milestones.r4)) {
            if (!status.passedRE4 && MsgUtils.hasNoMsgForMilestone(context.messages, EMilestone.RE4)) {
                // R4 due date reminder
                result = generateR4rOK0(context, status);
            } else {
                result = null;
            }
        } else if (plusThree.isBefore(status.milestones.fin)) {
            // No message due
            result = null;
        } else if (!today.isAfter(status.milestones.fin)) {
            if (!status.passedFIN && MsgUtils.hasNoMsgForMilestone(context.messages, EMilestone.FIN)) {
                // FIN due date reminder
                result = generateFErOK0(context, status);
            } else {
                result = null;
            }
        } else if (status.passedFIN) {
            if (status.totalScore < 62) {
                if (status.maxPossibleScore >= 65) {
                    // Grade is C, can earn A or B
                    result = generateGRCOK0(context, status);
                } else if (status.maxPossibleScore >= 62) {
                    // Grade is C, can earn B
                    result = generateGRCOK1(context, status);
                } else {
                    result = null;
                }
            } else if (status.totalScore < 65 && status.maxPossibleScore >= 65) {
                // Grade is B, can earn A
                result = generateGRBOK0(context, status);
            } else {
                // Grade is C, and C is highest that can be earned
                result = null;
            }
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a "**R1rOK0" message. This is sent when the RE1 due date is 1-3 days in the future,
     * but RE1 has not yet been passed.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateR1rOK0(final MessagingContext context,
                                                final MessagingCourseStatus status) {

        final String subject = "Due date reminder";

        // Only message for RE 1 for the first two courses - after that, assume they're OK.

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        if (context.currentRegIndex == 0) {
            // First experience with a Review Exam
            body.addln("It looks like you're moving along well in ", crsName, " - well done. ",
                    "The Unit 1 review exam is coming up soon.  This is like a preview of the Unit ",
                    "Exam - it's ten questions, of the same kind of questions you've already seen.  ",
                    "Once you pass the Review Exam (with an 8 or higher), you can move on to the ",
                    "proctored Unit Exam.");
            body.addln();

            final String dayName =
                    status.milestones.r1.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);

            body.addln("It's worth 3 points to get that review exam passed by then (you ",
                    "have until the end of the day on ", dayName, ").");
            body.addln();

        } else {
            final String dayName =
                    status.milestones.r1.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);

            body.addln("You're making good progress in ", crsName, " - good work.  ",
                    "Just a reminder that the Unit 1 Review Exam due date is coming up on ", dayName,
                    ".");
            body.addln();
        }

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.RE1, EMsg.RE1Rok00, subject, body.toString());
    }

    /**
     * Generates a report row for a "**R2rOK0" message. This is sent when the RE2 due date is 1-3 days in the future,
     * but RE2 has not yet been passed.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateR2rOK0(final MessagingContext context,
                                                final MessagingCourseStatus status) {

        final String subject = "Due date reminder";

        // Only message for RE 2 for the first two courses - after that, assume they're OK.

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        final String dayName =
                status.milestones.r2.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);

        body.addln("This is just a reminder about the due date for the Unit 2 Review Exam that's ",
                "coming up on ", dayName, ".");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.RE2, EMsg.RE2Rok00, subject, body.toString());
    }

    /**
     * Generates a report row for a "**R3rOK0" message. This is sent when the RE3 due date is 1-3 days in the future,
     * but RE3 has not yet been passed.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateR3rOK0(final MessagingContext context,
                                                final MessagingCourseStatus status) {

        final String subject = "Due date reminder";

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        final String dayName =
                status.milestones.r3.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);

        body.addln("This is just a reminder about the Unit 3 Review Exam that's due on ", dayName,
                "...");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.RE3, EMsg.RE3Rok00, subject, body.toString());
    }

    /**
     * Generates a report row for a "**R3rOK1" message. This is sent when the RE3 due date is 1-3 days in the future,
     * RE3 has been passed, but the final exam has not been passed, as a congratulations on doing so well.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateR3rOK1(final MessagingContext context,
                                                final MessagingCourseStatus status) {

        final String subject = "Checking in";

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("I just wanted to touch base - it seems like you're doing great, but if ",
                "there's anything you need, just let me know.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.RE3, EMsg.RE3Rok01, subject, body.toString());
    }

    /**
     * Generates a report row for a "**R4rOK0" message. This is sent when the RE4 due date is 1-3 days in the future,
     * but RE4 has not yet been passed.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateR4rOK0(final MessagingContext context,
                                                final MessagingCourseStatus status) {

        final String subject = "Due date reminder";

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        final String dayName = status.milestones.r4.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);

        body.addln("This is just a reminder about the Unit 4 Review Exam that's due ", dayName,
                "...");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.RE4, EMsg.RE4Rok00, subject, body.toString());
    }

    /**
     * Generates a report row for a "**FErOK0" message. This is sent when the FIN due date is 1-3 days in the future,
     * but FIN has not yet been passed.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateFErOK0(final MessagingContext context,
                                                final MessagingCourseStatus status) {

        final String subject = "Course deadline coming up";

        final LocalDate finalMs = status.milestones.fin;
        final String dateStr = TemporalUtils.FMT_WMD.format(finalMs);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("As the course completion deadline (", dateStr, ") is coming up, I just ",
                "wanted to make sure that date was on your radar.  You need to pass the Final exam ",
                "(with a 16) by that date.");
        body.addln();

        body.addln("If you pass the Final exam by then, but don't have 54 points, you can keep ",
                "re-taking exams to try to raise your score until the last day of classes.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.FIN, EMsg.FINRok00, subject, body.toString());
    }

    /**
     * Generates a report row for a "**GRCOK0" message. This is sent when the final exam has been passed, there has been
     * no message since the Welcome, the student has a C grade but could earn a B or A.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateGRCOK0(final MessagingContext context,
                                                final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final String subject = "Re-testing to increase score";

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Good job passing ", crsName, "!  Your point total will currently give you a ",
                "C grade for the course.  You can still re-take Unit and Final exams if you want to ",
                "try to increase your score.  You can re-test until the last class day of the ",
                "semester, and we only count your highest score on each exam.");
        body.addln();

        body.addln("If you can get to 62 points, your grade will become a B, and if you can get ",
                "to 65, it will become an A.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.MAX, EMsg.GRDCok00, subject, body.toString());
    }

    /**
     * Generates a report row for a "**GRCOK0" message. This is sent when the final exam has been passed, there has been
     * no message since the Welcome, the student has a C grade but could earn a B but not an A.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateGRCOK1(final MessagingContext context,
                                                final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final String subject = "Re-testing to increase score";

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Good job passing ", crsName, "!  Your point total will currently give you a ",
                "C grade for the course.  You can still re-take Unit and Final exams if you want to ",
                "try to increase your score.  You can re-test until the last class day of the ",
                "semester, and we only count your highest score on each exam.");
        body.addln();

        body.addln("If you can get to 62 points, your grade will become a B.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.MAX, EMsg.GRDCok00, subject, body.toString());
    }

    /**
     * Generates a report row for a "**GRCOK0" message. This is sent when the final exam has been passed, there has been
     * no message since the Welcome, the student has a B grade but could earn an A.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateGRBOK0(final MessagingContext context,
                                                final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final String subject = "Re-testing to increase score";

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Good job passing ", crsName, "!  Your point total will currently give you a ",
                "B grade for the course.  You can still re-take Unit and Final exams if you want to ",
                "try to increase your score.  You can re-test until the last class day of the ",
                "semester, and we only count your highest score on each exam.");
        body.addln();

        body.addln("If you can get to 65 points, your grade will become an A.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.MAX, EMsg.GRDCok00, subject, body.toString());
    }
}

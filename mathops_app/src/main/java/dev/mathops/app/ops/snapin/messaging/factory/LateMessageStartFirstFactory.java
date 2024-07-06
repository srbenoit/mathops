package dev.mathops.app.ops.snapin.messaging.factory;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.app.ops.snapin.messaging.EMilestone;
import dev.mathops.app.ops.snapin.messaging.EMsg;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;
import dev.mathops.app.ops.snapin.messaging.MsgUtils;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;

import java.time.LocalDate;

/**
 * A factory class that can generate messages to students who are "late" (urgency >= 1) and who have not yet started
 * their first course.
 */
enum LateMessageStartFirstFactory {
    ;

    /**
     * Generates an appropriate message when the active course has not been started.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    public static MessageToSend generate(final MessagingContext context, final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (status.daysSinceLastMessage > 3) {
            if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.STRTst00)) {
                result = generateSTRTst00(context, status);
            } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.STRTst01)) {
                result = generateSTRTst01(context, status);
            } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.STRTst02)) {
                result = generateSTRTst02(context, status);
            } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.STRTst03)) {
                result = generateSTRTst03(context, status);
            } else {
                Log.warning("Student ", context.student.stuId, " stuck not having started the first course");
            }
        }

        return result;
    }

    /**
     * Generates a report row for a STRTst00 message. This applies to students who have not started a course, the H1.1
     * milestone has arrived, and no previous STRTst00 message has been sent.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSTRTst00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.getFirst().course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("I wanted to see if you had found the course web site from the Canvas course ",
                "(https://precalc.math.colostate.edu/), and taken a look at the Student Guide.  ",
                "You can start ", crsName, " by clicking the green [Start ", crsName,
                "] button on the left-hand side of the course web page.");
        body.addln();

        body.addln("Be sure to check the 'Getting Help' link to see our help hours.  We will be ",
                "updating the online help hours over time, so check back once in a while to see when ",
                "you can get live help online (through Microsoft Teams).");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.START,
                EMsg.STRTst00, "Getting started", body.toString());
    }

    /**
     * Generates a report row for a STRTst01 message. This applies to students who have not started a course, and no
     * STRTst01 message has been sent.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSTRTst01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.getFirst().course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Just checking to see if you were able to find the course web page to start ",
                crsName, ".  It's good to get started as soon as you can so you don't end up ",
                "having to rush to meet due dates.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.START,
                EMsg.STRTst01, "Getting started", body.toString());
    }

    /**
     * Generates a report row for a STRTst02 message. This applies to students who have not started a course, and no
     * STRTst02 message has been sent.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSTRTst02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.getFirst().course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("Just another reminder to log in to start ", crsName, ".  ");

        if (context.today.isAfter(status.milestones.r2)) {
            body.addln("The course due dates are happening, and the first two Review Exam due ",
                    "dates have already come and gone - missing those dates costs 3 points each ",
                    "toward your course grade.");
        } else if (context.today.isAfter(status.milestones.r1)) {
            body.addln("The course due dates are happening, and the first Review Exam due date ",
                    "has already come and gone - missing those dates costs 3 points toward your ",
                    "course grade.");
        } else if (context.today.equals(status.milestones.r1)) {
            body.addln("The course due dates are happening, and the first Review Exam due date ",
                    "is today - missing those dates costs 3 points toward your course grade.");
        } else {
            final LocalDate cutoff = status.milestones.r1.minusDays(3L);

            if (context.today.isBefore(cutoff)) {
                body.addln(
                        "The course due dates are starting to come along, and I want to make sure ",
                        "you don't end up behind schedule and having to rush to get caught up.");
            } else {
                final String dateStr = TemporalUtils.FMT_WMD.format(status.milestones.r1);
                body.addln(
                        "The course due dates are happening, and the first Review Exam due date is ",
                        "already coming up on ", dateStr, ".");
            }
        }
        body.addln();

        body.addln("Let me know what I can do to help you get started...");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.START,
                EMsg.STRTst02, "Getting started", body.toString());
    }

    /**
     * Generates a report row for a STRTst03 message. This applies to students who have not started a course, and no
     * STRTst03 message has been sent.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSTRTst03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.getFirst().course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Do you think you will be able to complete ", crsName, " this semester?  If ",
                "you end up not finishing, we will submit a 'U' grade at the end of the term.");
        body.addln();

        if (context.pace == 2 && context.currentRegIndex == 0) {
            body.addln("If you need to, you could drop or withdraw from your second course, and ",
                    "that would make the due date schedule for ", crsName,
                    " stretch out to give you more time.");
            body.addln();
        } else if (context.pace > 2 && context.currentRegIndex + 1 < context.pace) {
            body.addln(
                    "If you need to, you could drop or withdraw from your last course, and that ",
                    "would make the remaining course due dates stretch out to give you more time.");
            body.addln();
        }

        body.addln("Let me know what you would like to do.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.START,
                EMsg.STRTst03, "Getting started", body.toString());
    }
}

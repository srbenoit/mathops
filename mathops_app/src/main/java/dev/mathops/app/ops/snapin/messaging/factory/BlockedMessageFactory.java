package dev.mathops.app.ops.snapin.messaging.factory;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.app.ops.snapin.messaging.EMilestone;
import dev.mathops.app.ops.snapin.messaging.EMsg;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;
import dev.mathops.app.ops.snapin.messaging.MsgUtils;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;

import java.time.LocalDate;

/**
 * A factory class that can generate messages to students who have not passed the final exam and are blocked until they
 * make a change in registration.
 */
public enum BlockedMessageFactory {
    ;

    /**
     * Generates an appropriate message for a student who has not yet passed the final exam.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    public static MessageToSend generate(final MessagingContext context,
                                         final MessagingCourseStatus status) {

        MessageToSend result = null;

        final LocalDate wDeadline = context.activeTerm.withdrawDeadline;
        final boolean pastWDeadline = wDeadline != null && context.today.isAfter(wDeadline);

        final int count = MsgUtils.countMsgOfCodes(context.messages, EMsg.BLOKwd00);

        if (count == 0) {

            final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
            final String crsName = MsgUtils.courseName(courseId);

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("The Final Exam for ", crsName, " was not passed by its due date, so ",
                    "the system has locked you out of your course.  At this point, we will submit ",
                    "a U grade for that course (which does not affect GPA, but does appear on a ",
                    "transcript).");
            body.addln();

            if (context.currentRegIndex == context.pace - 1) {
                // On the last course

                if (status.passedRE4 && context.today.plusDays(4L).isAfter(status.milestones.last)) {
                    body.add("You were very close to finishing - ");

                    if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
                        body.addln("please stop in at the Precalculus Center office to talk about ",
                                "what options are available and next steps.");
                    } else {
                        body.addln("do you want to talk about options and possible next steps?");
                    }
                    body.addln();
                }
            } else if (status.passedRE4
                    && context.today.plusDays(4L).isAfter(status.milestones.last)) {
                // Not on the last course
                if (!pastWDeadline) {
                    body.add("You can withdraw from ");
                    if (context.pace > 2) {
                        body.add("your last course");
                    } else {
                        body.add("your second course ");
                    }
                    body.add(" to get more time to finish this course, but you were very ",
                            "close to finishing - ");

                    if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
                        body.addln("please stop in at the Precalculus Center office to talk about ",
                                "what options are available and next steps.");
                    } else {
                        body.addln("do you want to talk about options and possible next steps?");
                    }
                    body.addln();
                }
            } else if (!pastWDeadline) {
                // Not close to passing - should withdraw from a later course
                body.add("In order to get more time to finish this course, you will need to ",
                        "withdraw from a later course (via RamWeb).  That will make the deadline ",
                        "schedule stretch out for ");
                if (context.pace > 2) {
                    body.addln("the courses that remain.");
                } else {
                    body.addln("this course.");
                }
                body.addln();
            }

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex),
                    EMilestone.BLOK, EMsg.BLOKwd00, "Locked out of course", body.toString());

        } else if (count == 1 && MsgUtils.daysSinceCode(context.messages, context.today, EMsg.BLOKwd00) > 4) {

            if (pastWDeadline && context.sortedRegs.size() < 3) {
                // Student is done - no message
            } else {
                final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

                if (pastWDeadline) {
                    body.addln(
                            "The withdrawal deadline has passed, but we have a way you can take a U ",
                            "grade in your last course and still work on the prior course - let me know ",
                            "if you want me to do that.");
                    body.addln();
                } else {
                    body.addln(
                            "In order to keep making progress in math this term, you will need to ",
                            "withdraw from a Precalculus course. You can do that via RamWeb - let me ",
                            "know if you need help with that process.");
                    body.addln();

                    if (context.activeTerm.withdrawDeadline != null) {
                        body.addln("The deadline to do a withdraw is ",
                                TemporalUtils.FMT_WMD_LONG.format(context.activeTerm.withdrawDeadline), ".");
                        body.addln();
                    }

                    body.addln(
                            "If you can't withdraw because you need to remain enrolled in a certain ",
                            "number of credits, let me know. We have a way you can take a U grade in ",
                            "a course and still make some progress in math.");
                    body.addln();
                }

                MsgUtils.emitSimpleClosing(body);

                result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex),
                                EMilestone.BLOK, EMsg.BLOKwd00, "Locked out of course", body.toString());
            }
        }

        return result;
    }
}

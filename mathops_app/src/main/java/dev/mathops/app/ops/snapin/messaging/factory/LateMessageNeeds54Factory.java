package dev.mathops.app.ops.snapin.messaging.factory;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.app.ops.snapin.messaging.EMilestone;
import dev.mathops.app.ops.snapin.messaging.EMsg;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;
import dev.mathops.app.ops.snapin.messaging.MsgUtils;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;

/**
 * A factory class that can generate messages to students who have passed the Final Exam but do not yet have 54 points.
 */
public enum LateMessageNeeds54Factory {
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

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.PNTSrt00)) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("You've passed the Final Exam (good work!) but you don't quite have the ",
                    "54 points needed to pass the course.  You can increase your total score by ",
                    "re-taking Unit exams or the Final Exam - we only count the higher score earned.");
            body.addln();

            body.addln("You have until the last day of classes (the Friday before Finals week) ",
                    "to retake those exams and try to get to the 54 point threshold.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.PASS, EMsg.PNTSrt00, "Course point total", body.toString());
        } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.PNTSrt99)
                && MsgUtils.daysSinceCode(context.messages, context.today, EMsg.PNTSrt00) > 7) {

            // FIXME: Adjust "7-day" trigger above based on end of semester date (add "last test
            // date" to TERM table)

            final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
            final String crsName = MsgUtils.courseName(courseId);

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.add("It looks like you're stuck trying to get to the 54 point passing score in ",
                    crsName, ".  I'd like to help - ");

            if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
                body.addln("can you come into the Precalculus Center to talk about things?");
            } else {
                body.addln("do you want to set up a Teams meeting to talk about things?");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status,
                    Integer.valueOf(context.currentRegIndex + 1), EMilestone.PASS, EMsg.PNTSrt99,
                    "Stuck finishing " + crsName + "?", body.toString());
        }

        return result;
    }

}

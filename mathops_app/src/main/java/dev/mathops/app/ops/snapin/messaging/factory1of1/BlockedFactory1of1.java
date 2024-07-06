package dev.mathops.app.ops.snapin.messaging.factory1of1;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.app.ops.snapin.messaging.EMilestone;
import dev.mathops.app.ops.snapin.messaging.EMsg;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;
import dev.mathops.app.ops.snapin.messaging.MsgUtils;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;

/**
 * A factory class that can generate messages to 1-course students who have not passed the final exam and are blocked.
 */
enum BlockedFactory1of1 {
    ;

    /**
     * Generates an appropriate message for a student who has not yet passed the final exam.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    static MessageToSend generate(final MessagingContext context, final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.BLOKwd00)) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            final String courseId = context.sortedRegs.getFirst().course;
            final String crsName = MsgUtils.courseName(courseId);

            body.addln("The Final Exam for ", crsName, " was not passed by its due date, so ",
                    "the system has locked you out of your course.  At this point, we will submit ",
                    "a U grade for that course (which does not affect GPA, but will appear on your ",
                    "transcript).");
            body.addln();

            if (status.passedRE4 && context.today.plusDays(4L).isAfter(status.milestones.last)) {
                body.add("You were very close to finishing - ");

                if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
                    body.addln("please stop in to the Precalculus Center office to talk about ",
                            "what options might be available and next steps.");
                } else {
                    body.addln("do you want to talk about options and possible next steps?");
                }
                body.addln();
            }

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(0), EMilestone.BLOK,
                    EMsg.BLOKwd00, "Locked out of course", body.toString());
        }

        return result;
    }
}

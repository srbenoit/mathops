package dev.mathops.app.ops.snapin.messaging.factory1of1;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.app.ops.snapin.messaging.EMilestone;
import dev.mathops.app.ops.snapin.messaging.EffectiveMilestones;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;
import dev.mathops.app.ops.snapin.messaging.MsgUtils;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;

import java.util.Map;

/**
 * The main factory that generates messages for students in 1 course.
 */
public enum Factory1of1 {
    ;

    /** The maximum urgency to merit an email every 7 days. */
    private static final int MAX_URGENCY_EVERY_8_DAYS = 3;

    /** The maximum urgency to merit an email every 5 days. */
    private static final int MAX_URGENCY_EVERY_6_DAYS = 5;

    /** The maximum urgency to merit an email every 3 days. */
    private static final int MAX_URGENCY_EVERY_4_DAYS = 10;

    /**
     * Processes a student in a 1-course pace.
     *
     * @param cache       the data cache
     * @param context     the messaging context
     * @param instrName   the name of the instructor assigned to the student's pace/track
     * @param messagesDue a map from student ID to message to be sent
     */
    public static void processPace1Student(final Cache cache, final MessagingContext context, final String instrName,
                                           final Map<? super String, ? super MessageToSend> messagesDue) {

        if (context.currentRegIndex != 0) {
            Log.info("Current reg is not zero for 1-course student " + context.student.stuId);
        }

        final RawStcourse reg1 = context.sortedRegs.get(0);
        final EffectiveMilestones ms1 = new EffectiveMilestones(cache, 1, 1, context);
        final MessagingCourseStatus current =
                new MessagingCourseStatus(context, reg1, ms1, instrName);

        MessageToSend row = null;

        if (MsgUtils.hasNoMsgForMilestone(context.messages, EMilestone.WELCOME)) {
            row = WelcomeMessageFactory1of1.generate(context, current);
        } else if (current.blocked) {
            row = BlockedFactory1of1.generate(context, current);
        } else {
            final int urgency = current.urgency;
            final int msgDays = current.daysSinceLastMessage;

            // TODO: Adjust delays for "late" messages near end of term.

            if (urgency <= 0) {
                // Student is on or ahead of schedule.
                if (msgDays > 1) {
                    row = OnTimeMessageFactory1of1.generate(context, current);
                }
            } else if (urgency <= MAX_URGENCY_EVERY_8_DAYS) {
                if (msgDays > 7) {
                    row = LateMessageFactory1of1.generate(cache, context, current);
                }
            } else if (urgency <= MAX_URGENCY_EVERY_6_DAYS) {
                if (msgDays > 5) {
                    row = LateMessageFactory1of1.generate(cache, context, current);
                }
            } else if (urgency <= MAX_URGENCY_EVERY_4_DAYS) {
                if (msgDays > 3) {
                    row = LateMessageFactory1of1.generate(cache, context, current);
                }
            } else if (msgDays > 2) {
                row = LateMessageFactory1of1.generate(cache, context, current);
            }
        }

        if (row != null) {
            messagesDue.put(context.student.stuId, row);
        }
    }
}

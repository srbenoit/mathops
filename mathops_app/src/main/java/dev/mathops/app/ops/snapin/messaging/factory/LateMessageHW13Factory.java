package dev.mathops.app.ops.snapin.messaging.factory;

import dev.mathops.commons.log.Log;
import dev.mathops.app.ops.snapin.messaging.EMilestone;
import dev.mathops.app.ops.snapin.messaging.EMsg;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;
import dev.mathops.app.ops.snapin.messaging.MsgUtils;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;
import dev.mathops.text.builder.HtmlBuilder;

import java.time.Period;

/**
 * A factory class that can generate messages to students who are "late" (urgency >= 1) who have not passed homework
 * 1.3.
 */
public enum LateMessageHW13Factory {
    ;

    /**
     * Generates an appropriate message for a student who has not yet passed homework 1.3.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    public static MessageToSend generate(final MessagingContext context,
                                         final MessagingCourseStatus status) {

        final MessageToSend result;

        final Integer hwTries = status.triesOnUnit1HW.get(MsgUtils.THREE);
        final int tries = hwTries == null ? 0 : hwTries.intValue();

        if (tries == 0) {
            // HW 1.3 not attempted
            result = generateNotTried(context, status);
        } else if (tries < 4) {
            // HW 1.3 attempted <4 times, not passed
            result = generateFailedFew(context, status);
        } else {
            // HW 1.3 attempted >=4 times, not passed
            result = generateFailedMany(context, status);
        }

        return result;
    }

    /**
     * Generates an appropriate message when Homework 1.3 has not been attempted.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateNotTried(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (status.daysSinceLastActivity > 3) {
            final boolean noHW0 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H13Rhw00);
            final boolean noHW1 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H13Rhw01);
            final boolean noHW2 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H13Rhw02);
            final boolean noHW3 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H13Rhw03);

            if (noHW0) {
                result = generateH13Rhw00(context, status);
            } else if (noHW1) {
                result = generateH13Rhw01(context, status);
            } else if (noHW2) {
                result = generateH13Rhw02(context, status);
            } else if (noHW3) {
                result = generateH13Rhw03(context, status);
            } else if (status.daysSinceLastActivity > 3) {
                final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H13Rhw99);

                if (no99) {
                    result = generateH13Rhw99(context, status);
                } else {
                    Log.info("Student ", context.student.stuId, " stuck on HW 1.3");
                }
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when Homework 1.3 has been attempted less than 4 times.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFailedFew(final MessagingContext context,
                                                   final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (status.daysSinceLastActivity > 3) {
            final Period sinceLastTry = Period.between(status.lastH13Try, context.today);
            final int daysSinceLastTry = sinceLastTry.getDays();

            if (daysSinceLastTry >= 2) {
                final boolean noHW04 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H13Rhw00, EMsg.H13Rhw04);
                final boolean noHW15 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H13Rhw01, EMsg.H13Rhw05);
                final boolean noHW26 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H13Rhw02, EMsg.H13Rhw06);
                final boolean noHW37 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H13Rhw03, EMsg.H13Rhw07);

                if (noHW04) {
                    result = generateH13Rhw04(context, status);
                } else if (noHW15) {
                    result = generateH13Rhw05(context, status);
                } else if (noHW26) {
                    result = generateH13Rhw06(context, status);
                } else if (noHW37) {
                    result = generateH13Rhw07(context, status);
                } else if (status.daysSinceLastActivity > 3) {
                    final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H13Rhw99);

                    if (no99) {
                        result = generateH13Rhw99(context, status);
                    } else {
                        Log.info("Student ", context.student.stuId, " stuck on HW 1.3");
                    }
                }
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when Homework 1.3 has been attempted 4 or more times but not yet passed.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFailedMany(final MessagingContext context,
                                                    final MessagingCourseStatus status) {

        MessageToSend result = null;

        final boolean noHW040 =
                MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H13Rhw00, EMsg.H13Rhw04, EMsg.H13Xhw00);
        final boolean noHW1512 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H13Rhw01, EMsg.H13Rhw05,
                EMsg.H13Xhw01, EMsg.H13Xhw02);
        final boolean noHW37 =
                MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H13Rhw03, EMsg.H13Rhw07, EMsg.H13Xhw03);

        if (noHW040) {
            result = generateH13Xhw00(context, status);
        } else if (noHW1512) {
            final Period sinceLastTry = Period.between(status.lastH13Try, context.today);
            final int daysSinceLastTry = sinceLastTry.getDays();

            if (daysSinceLastTry > 2) {
                result = generateH13Xhw01(context, status);
            } else {
                result = generateH13Xhw02(context, status);
            }
        } else if (noHW37) {
            result = generateH13Xhw03(context, status);
        } else if (status.daysSinceLastActivity > 3) {
            final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H13Rhw99);

            if (no99) {
                result = generateH13Rhw99(context, status);
            } else {
                Log.info("Student ", context.student.stuId, " stuck on HW 1.3");
            }
        }

        return result;
    }

    /**
     * Generates a report row for a H13Rhw00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH13Rhw00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // Give a break in messaging for a moment

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW13, EMsg.H13Rhw00, null, null);
    }

    /**
     * Generates a report row for a H13Rhw01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH13Rhw01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to try to tackle the Objective 1.3 assignment.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW13, EMsg.H13Rhw01, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H13Rhw02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH13Rhw02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // We're going to skip this for all students

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW13, EMsg.H13Rhw02, null, null);
    }

    /**
     * Generates a report row for a H13Rhw03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH13Rhw03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln(
                    "It looks like you've paused your progress through Unit 1 - is everything OK?");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW13, EMsg.H13Rhw03, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H13Rhw04 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH13Rhw04(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // Give a break in messaging for a moment

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW13, EMsg.H13Rhw00, null, null);
    }

    /**
     * Generates a report row for a H13Rhw05 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH13Rhw05(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to try to complete the Objective 1.3 assignment.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW13, EMsg.H13Rhw05, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H13Rhw06 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH13Rhw06(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // We're going to skip this for all students

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW13, EMsg.H13Rhw06, null, null);
    }

    /**
     * Generates a report row for a H13Rhw07 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH13Rhw07(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("It looks like you might be stalled - is everything OK?");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW13, EMsg.H13Rhw07, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H13Xhw00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH13Xhw00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // Give a break in messaging for a moment

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW13, EMsg.H13Xhw00, null, null);
    }

    /**
     * Generates a report row for a H13Xhw01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH13Xhw01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to try to complete the Objective 1.3 assignment.");
            body.addln();

            body.add("If this objective is causing trouble, ");
            if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
                body.addln("the Precalculus Center LAs (in-person and online) are here to help.");
            } else {
                body.addln("the online Precalculus Center LAs are here to help.");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW13, EMsg.H13Xhw01, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H13Xhw02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH13Xhw02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to try to complete the Objective 1.3 assignment.");
            body.addln();

            body.add("If this objective is causing trouble, ");
            if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
                body.addln("the Precalculus Center LAs (in-person and online) are here to help.");
            } else {
                body.addln("the online Precalculus Center LAs are here to help.");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW13, EMsg.H13Xhw02, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H13Xhw03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH13Xhw03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("It looks like you might be stalled - is everything OK?");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result =  new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW13, EMsg.H13Xhw03, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H13Rhw99 message. This applies to students who have been stuck on homework 1.3
     * through all 4 messages.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH13Rhw99(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("It looks like you're stuck in the objective 1.3 assignment in ", crsName,
                ".  I'd like to help - ");

        if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
            body.addln("can you come into the Precalculus Center to talk about things?");
        } else {
            body.addln("do you want to set up a Teams meeting to talk about things?");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW13, EMsg.H13Rhw99, "Stuck on " + crsName + " assignment?",
                body.toString());
    }
}

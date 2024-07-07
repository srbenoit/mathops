package dev.mathops.app.ops.snapin.messaging.factory;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.app.ops.snapin.messaging.EMilestone;
import dev.mathops.app.ops.snapin.messaging.EMsg;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;
import dev.mathops.app.ops.snapin.messaging.MsgUtils;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;

import java.time.Period;

/**
 * A factory class that can generate messages to students who are "late" (urgency >= 1) who have not passed homework
 * 4.4.
 */
public enum LateMessageHW44Factory {
    ;

    /**
     * Generates an appropriate message for a student who has not yet passed homework 4.4.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    public static MessageToSend generate(final MessagingContext context,
                                         final MessagingCourseStatus status) {

        final MessageToSend result;

        final Integer hwTries = status.triesOnUnit4HW.get(MsgUtils.FOUR);
        final int tries = hwTries == null ? 0 : hwTries.intValue();

        if (tries == 0) {
            // HW 4.4 not attempted
            result = generateNotTried(context, status);
        } else if (tries < 4) {
            // HW 4.4 attempted <4 times, not passed
            result = generateFailedFew(context, status);
        } else {
            // HW 4.4 attempted >=4 times, not passed
            result = generateFailedMany(context, status);
        }

        return result;
    }

    /**
     * Generates an appropriate message when Homework 4.4 has not been attempted.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateNotTried(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (status.daysSinceLastActivity > 3) {
            final boolean noHW0 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H44Rhw00);
            final boolean noHW1 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H44Rhw01);
            final boolean noHW2 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H44Rhw02);
            final boolean noHW3 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H44Rhw03);

            if (noHW0) {
                result = generateH44Rhw00(context, status);
            } else if (noHW1) {
                result = generateH44Rhw01(context, status);
            } else if (noHW2) {
                result = generateH44Rhw02(context, status);
            } else if (noHW3) {
                result = generateH44Rhw03(context, status);
            } else if (status.daysSinceLastActivity > 3) {
                final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H44Rhw99);

                if (no99) {
                    result = generateH44Rhw99(context, status);
                } else {
                    Log.info("Student ", context.student.stuId, " stuck on HW 4.4");
                }
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when Homework 4.4 has been attempted less than 4 times.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFailedFew(final MessagingContext context,
                                                   final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (status.daysSinceLastActivity > 3) {
            final Period sinceLastTry = Period.between(status.lastH44Try, context.today);
            final int daysSinceLastTry = sinceLastTry.getDays();

            if (daysSinceLastTry >= 2) {
                final boolean noHW04 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H44Rhw00, EMsg.H44Rhw04);
                final boolean noHW15 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H44Rhw01, EMsg.H44Rhw05);
                final boolean noHW26 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H44Rhw02, EMsg.H44Rhw06);
                final boolean noHW37 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H44Rhw03, EMsg.H44Rhw07);

                if (noHW04) {
                    result = generateH44Rhw04(context, status);
                } else if (noHW15) {
                    result = generateH44Rhw05(context, status);
                } else if (noHW26) {
                    result = generateH44Rhw06(context, status);
                } else if (noHW37) {
                    result = generateH44Rhw07(context, status);
                } else if (status.daysSinceLastActivity > 3) {
                    final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H44Rhw99);

                    if (no99) {
                        result = generateH44Rhw99(context, status);
                    } else {
                        Log.info("Student ", context.student.stuId, " stuck on HW 4.4");
                    }
                }
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when Homework 4.4 has been attempted 4 or more times but not yet passed.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFailedMany(final MessagingContext context,
                                                    final MessagingCourseStatus status) {

        MessageToSend result = null;

        final boolean noHW040 =
                MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H44Rhw00, EMsg.H44Rhw04, EMsg.H44Xhw00);
        final boolean noHW1512 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H44Rhw01, EMsg.H44Rhw05,
                EMsg.H44Xhw01, EMsg.H44Xhw02);
        final boolean noHW37 =
                MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H44Rhw03, EMsg.H44Rhw07, EMsg.H44Xhw03);

        if (noHW040) {
            result = generateH44Xhw00(context, status);
        } else if (noHW1512) {
            final Period sinceLastTry = Period.between(status.lastH44Try, context.today);
            final int daysSinceLastTry = sinceLastTry.getDays();

            if (daysSinceLastTry > 2) {
                result = generateH44Xhw01(context, status);
            } else {
                result = generateH44Xhw02(context, status);
            }
        } else if (noHW37) {
            result = generateH44Xhw03(context, status);
        } else if (status.daysSinceLastActivity > 3) {
            final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H44Rhw99);

            if (no99) {
                result = generateH44Rhw99(context, status);
            } else {
                Log.info("Student ", context.student.stuId, " stuck on HW 4.4");
            }
        }

        return result;
    }

    /**
     * Generates a report row for a H44Rhw00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH44Rhw00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // Give a break in messaging for a moment

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW44, EMsg.H44Rhw00, null, null);
    }

    /**
     * Generates a report row for a H44Rhw01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH44Rhw01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to start work on the Objective 4.4 assignment.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW44, EMsg.H44Rhw01, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H44Rhw02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH44Rhw02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // We're going to skip this for all students

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW44, EMsg.H44Rhw02, null, null);
    }

    /**
     * Generates a report row for a H44Rhw03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH44Rhw03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln(
                    "It looks like Unit 2 has slowed down your momentum - is everything going OK?");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW44, EMsg.H44Rhw03, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H44Rhw04 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH44Rhw04(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // Give a break in messaging for a moment

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW44, EMsg.H44Rhw00, null, null);
    }

    /**
     * Generates a report row for a H44Rhw05 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH44Rhw05(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to try to finish up the Objective 4.4 assignment.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW44, EMsg.H44Rhw05, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H44Rhw06 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH44Rhw06(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // We're going to skip this for all students

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW44, EMsg.H44Rhw06, null, null);
    }

    /**
     * Generates a report row for a H44Rhw07 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH44Rhw07(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln(
                    "It looks like Unit 2 has slowed down your momentum - is everything going OK?");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW44, EMsg.H44Rhw07, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H44Xhw00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH44Xhw00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // Give a break in messaging for a moment

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW44, EMsg.H44Xhw00, null, null);
    }

    /**
     * Generates a report row for a H44Xhw01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH44Xhw01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to try to finish up the Objective 4.4 assignment.");
            body.addln();

            body.add("If this objective is holding you up, ");
            if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
                body.addln("the Precalculus Center LAs (in-person and online) are here to help.");
            } else {
                body.addln("the online Precalculus Center LAs are here to help.");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW44, EMsg.H44Xhw01, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H44Xhw02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH44Xhw02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to try to complete the Objective 4.4 assignment.");
            body.addln();

            body.add("If this objective is holding you up, ");
            if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
                body.addln("the Precalculus Center LAs (in-person and online) are here to help.");
            } else {
                body.addln("the online Precalculus Center LAs are here to help.");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW44, EMsg.H44Xhw02, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H44Xhw03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH44Xhw03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln(
                    "It looks like Unit 2 has slowed down your momentum - is everything going OK?");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW44, EMsg.H44Xhw03, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H44Rhw99 message. This applies to students who have been stuck on homework 4.4
     * through all 4 messages.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH44Rhw99(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("It looks like you're stuck in the objective 4.4 assignment in ", crsName,
                ".  I'd like to help - ");

        if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
            body.addln("can you come into the Precalculus Center to talk about things?");
        } else {
            body.addln("do you want to set up a Teams meeting to talk about things?");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW44, EMsg.H44Rhw99, "Stuck on " + crsName + " assignment?",
                body.toString());
    }
}

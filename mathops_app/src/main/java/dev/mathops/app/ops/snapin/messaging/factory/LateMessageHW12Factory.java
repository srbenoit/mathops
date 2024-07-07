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
 * 1.2.
 */
public enum LateMessageHW12Factory {
    ;

    /**
     * Generates an appropriate message for a student who has not yet passed homework 1.2.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    public static MessageToSend generate(final MessagingContext context,
                                         final MessagingCourseStatus status) {

        final MessageToSend result;

        final Integer hwTries = status.triesOnUnit1HW.get(MsgUtils.TWO);
        final int tries = hwTries == null ? 0 : hwTries.intValue();

        if (tries == 0) {
            // HW 1.2 not attempted
            result = generateNotTried(context, status);
        } else if (tries < 4) {
            // HW 1.2 attempted <4 times, not passed
            result = generateFailedFew(context, status);
        } else {
            // HW 1.2 attempted >=4 times, not passed
            result = generateFailedMany(context, status);
        }

        return result;
    }

    /**
     * Generates an appropriate message when Homework 1.2 has not been attempted.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateNotTried(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (status.daysSinceLastActivity > 3) {
            final boolean noHW0 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H12Rhw00);
            final boolean noHW1 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H12Rhw01);
            final boolean noHW2 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H12Rhw02);
            final boolean noHW3 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H12Rhw03);

            if (noHW0) {
                result = generateH12Rhw00(context, status);
            } else if (noHW1) {
                result = generateH12Rhw01(context, status);
            } else if (noHW2) {
                result = generateH12Rhw02(context, status);
            } else if (noHW3) {
                result = generateH12Rhw03(context, status);
            } else if (status.daysSinceLastActivity > 3) {
                final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H12Rhw99);

                if (no99) {
                    result = generateH12Rhw99(context, status);
                } else {
                    Log.info("Student ", context.student.stuId, " stuck on HW 1.2");
                }
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when Homework 1.2 has been attempted less than 4 times.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFailedFew(final MessagingContext context,
                                                   final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (status.daysSinceLastActivity > 3) {
            final Period sinceLastTry = Period.between(status.lastH12Try, context.today);
            final int daysSinceLastTry = sinceLastTry.getDays();

            if (daysSinceLastTry >= 2) {
                final boolean noHW04 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H12Rhw00, EMsg.H12Rhw04);
                final boolean noHW15 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H12Rhw01, EMsg.H12Rhw05);
                final boolean noHW26 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H12Rhw02, EMsg.H12Rhw06);
                final boolean noHW37 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H12Rhw03, EMsg.H12Rhw07);

                if (noHW04) {
                    result = generateH12Rhw04(context, status);
                } else if (noHW15) {
                    result = generateH12Rhw05(context, status);
                } else if (noHW26) {
                    result = generateH12Rhw06(context, status);
                } else if (noHW37) {
                    result = generateH12Rhw07(context, status);
                } else if (status.daysSinceLastActivity > 3) {
                    final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H12Rhw99);

                    if (no99) {
                        result = generateH12Rhw99(context, status);
                    } else {
                        Log.info("Student ", context.student.stuId, " stuck on HW 1.2");
                    }
                }
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when Homework 1.2 has been attempted 4 or more times but not yet passed.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFailedMany(final MessagingContext context,
                                                    final MessagingCourseStatus status) {

        MessageToSend result = null;

        final boolean noHW040 =
                MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H12Rhw00, EMsg.H12Rhw04, EMsg.H12Xhw00);
        final boolean noHW1512 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H12Rhw01, EMsg.H12Rhw05,
                EMsg.H12Xhw01, EMsg.H12Xhw02);
        final boolean noHW37 =
                MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H12Rhw03, EMsg.H12Rhw07, EMsg.H12Xhw03);

        if (noHW040) {
            result = generateH12Xhw00(context, status);
        } else if (noHW1512) {
            final Period sinceLastTry = Period.between(status.lastH12Try, context.today);
            final int daysSinceLastTry = sinceLastTry.getDays();

            if (daysSinceLastTry > 2) {
                result = generateH12Xhw01(context, status);
            } else {
                result = generateH12Xhw02(context, status);
            }
        } else if (noHW37) {
            result = generateH12Xhw03(context, status);
        } else if (status.daysSinceLastActivity > 3) {
            final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H12Rhw99);

            if (no99) {
                result = generateH12Rhw99(context, status);
            } else {
                Log.info("Student ", context.student.stuId, " stuck on HW 1.2");
            }
        }

        return result;
    }

    /**
     * Generates a report row for a H12Rhw00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH12Rhw00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // Give a break in messaging for a moment

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW12, EMsg.H12Rhw00, null, null);
    }

    /**
     * Generates a report row for a H12Rhw01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH12Rhw01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            if (context.currentRegIndex == 0) {
                // First course, second objective
                body.addln("Just a reminder to try to tackle the Objective 1.2 assignment.  As ",
                        "before, you can try the assignment first, then review the videos for topics ",
                        "where it feels like you need some explanation.");
            } else {
                // Subsequent course
                body.addln("Just a reminder that the Objective 1.2 assignment awaits...");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW12, EMsg.H12Rhw01, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H12Rhw02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH12Rhw02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // We're going to skip this for all students

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW12, EMsg.H12Rhw02, null, null);
    }

    /**
     * Generates a report row for a H12Rhw03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH12Rhw03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("It looks like you've paused your progress through Unit 1 - is everything OK?");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW12, EMsg.H12Rhw03, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H12Rhw04 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH12Rhw04(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // Give a break in messaging for a moment

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW12, EMsg.H12Rhw00, null, null);
    }

    /**
     * Generates a report row for a H12Rhw05 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH12Rhw05(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to try to complete the Objective 1.2 assignment.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW12, EMsg.H12Rhw05, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H12Rhw06 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH12Rhw06(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // We're going to skip this for all students

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW12, EMsg.H12Rhw06, null, null);
    }

    /**
     * Generates a report row for a H12Rhw07 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH12Rhw07(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln(
                    "It looks like you've paused your progress through Unit 1 - is everything OK?");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW12, EMsg.H12Rhw07, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H12Xhw00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH12Xhw00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // Give a break in messaging for a moment

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW12, EMsg.H12Xhw00, null, null);
    }

    /**
     * Generates a report row for a H12Xhw01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH12Xhw01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to try to complete the Objective 1.2 assignment.");
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
                            EMilestone.HW12, EMsg.H12Xhw01, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;

    }

    /**
     * Generates a report row for a H12Xhw02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH12Xhw02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to try to complete the Objective 1.2 assignment.");
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
                            EMilestone.HW12, EMsg.H12Xhw02, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H12Xhw03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH12Xhw03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("It looks like you've paused your progress through Unit 1 - is everything OK?");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW12, EMsg.H12Xhw03, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H12Rhw99 message. This applies to students who have been stuck on homework 1.2
     * through all 4 messages.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH12Rhw99(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("It looks like you're stuck in the objective 1.2 assignment in ", crsName,
                ".  I'd like to help - ");

        if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
            body.addln("can you come into the Precalculus Center to talk about things?");
        } else {
            body.addln("do you want to set up a Teams meeting to talk about things?");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW12, EMsg.H12Rhw99, "Stuck on " + crsName + " assignment?",
                body.toString());
    }
}

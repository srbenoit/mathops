package dev.mathops.app.ops.snapin.messaging.factory;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.app.ops.snapin.messaging.EMilestone;
import dev.mathops.app.ops.snapin.messaging.EMsg;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;
import dev.mathops.app.ops.snapin.messaging.MsgUtils;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;

import java.time.Period;

/**
 * A factory class that can generate messages to students who are "late" (urgency >= 1) who have not passed the unit 4
 * exam.
 */
public enum LateMessageUE4Factory {
    ;

    /**
     * Generates an appropriate message for a student who has not yet passed unit exam 4.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    public static MessageToSend generate(final MessagingContext context, final MessagingCourseStatus status) {

        final MessageToSend result;

        final int tries = status.failedTriesOnUE4;

        if (tries == 0) {
            // RE 4 not attempted
            result = generateNotTried(context, status);
        } else if (tries < 4) {
            // RE 4 attempted <4 times, not passed
            result = generateFailedFew(context, status);
        } else {
            // RE 4 attempted >=4 times, not passed
            result = generateFailedMany(context, status);
        }

        return result;
    }

    /**
     * Generates an appropriate message when Unit Exam 2 has not been attempted.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateNotTried(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        final boolean noUE0 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE4Rue00);
        final boolean noUE1 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE4Rue01);
        final boolean noUE4 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE4Rue02);
        final boolean noUE3 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE4Rue03);

        if (status.daysSinceLastActivity > 3) {
            if (noUE0) {
                result = generateUE4Rue00(context, status);
            } else if (noUE1) {
                result = generateUE4Rue01(context, status);
            } else if (noUE4) {
                result = generateUE4Rue02(context, status);
            } else if (noUE3) {
                result = generateUE4Rue03(context, status);
            } else {
                final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE4Rue99);

                if (no99) {
                    result = generateUE4Rue99(context, status);
                } else {
                    Log.info("Student ", context.student.stuId, " stuck on Unit Exam 4");
                }
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when Unit Exam 2 has been attempted less than 4 times.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFailedFew(final MessagingContext context,
                                                   final MessagingCourseStatus status) {

        MessageToSend result = null;

        final Period sinceLastTry = Period.between(status.lastU4Try, context.today);
        final int daysSinceLastTry = sinceLastTry.getDays();

        if (status.daysSinceLastActivity > 3) {
            if (daysSinceLastTry >= 2) {
                final boolean noUE04 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE4Rue00, EMsg.UE4Rue04);
                final boolean noUE45 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE4Rue01, EMsg.UE4Rue05);
                final boolean noUE46 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE4Rue02, EMsg.UE4Rue06);
                final boolean noUE37 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE4Rue03, EMsg.UE4Rue07);

                if (noUE04) {
                    result = generateUE4Rue04(context, status);
                } else if (noUE45) {
                    result = generateUE4Rue05(context, status);
                } else if (noUE46) {
                    result = generateUE4Rue06(context, status);
                } else if (noUE37) {
                    result = generateUE4Rue07(context, status);
                } else if (status.daysSinceLastActivity > 3) {
                    final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE4Rue99);

                    if (no99) {
                        result = generateUE4Rue99(context, status);
                    } else {
                        Log.info("Student ", context.student.stuId, " stuck on Unit Exam 4");
                    }
                }
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when Unit Exam 2 has been attempted 4 or more times but not yet passed.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFailedMany(final MessagingContext context,
                                                    final MessagingCourseStatus status) {

        MessageToSend result = null;

        final boolean noUE040 = MsgUtils.hasNoMsgOfCodes(context.messages,
                EMsg.UE4Rue00, EMsg.UE4Rue04, EMsg.UE4Xue00);
        final boolean noUE4512 = MsgUtils.hasNoMsgOfCodes(context.messages,
                EMsg.UE4Rue01, EMsg.UE4Rue05, EMsg.UE4Xue01, EMsg.UE4Xue02);
        final boolean noUE37 = MsgUtils.hasNoMsgOfCodes(context.messages,
                EMsg.UE4Rue03, EMsg.UE4Rue07, EMsg.UE4Xue03);

        if (noUE040) {
            result = generateUE4Xue00(context, status);
        } else if (noUE4512) {
            final Period sinceLastTry = Period.between(status.lastU4Try, context.today);
            final int daysSinceLastTry = sinceLastTry.getDays();

            if (daysSinceLastTry > 2) {
                result = generateUE4Xue01(context, status);
            } else {
                result = generateUE4Xue02(context, status);
            }
        } else if (noUE37) {
            result = generateUE4Xue03(context, status);
        } else if (status.daysSinceLastActivity > 3) {
            final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE4Rue99);

            if (no99) {
                result = generateUE4Rue99(context, status);
            } else {
                Log.info("Student ", context.student.stuId, " stuck on Unit Exam 4");
            }
        }

        return result;
    }

    /**
     * Generates a report row for a UE4Rue00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE4Rue00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String subject = "Checking in";

        // Only message for RE 4 for the first two courses - after that, assume they're OK.

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Good job on the Unit 4 Review exam - the proctored Unit 4 Exam is next...");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.UE4, EMsg.UE4Rue00, subject, body.toString());
    }

    /**
     * Generates a report row for a UE4Rue01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE4Rue01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to try to complete the Unit 4 exam...");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.UE4, EMsg.UE4Rue01, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a UE4Rue02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE4Rue02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // We're going to skip this for all students

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.UE4, EMsg.UE4Rue02, null, null);
    }

    /**
     * Generates a report row for a UE4Rue03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE4Rue03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("I've noticed you haven't tried the Unit 4 Exam yet - are ",
                "things going OK?  If there's anything you need, let me know.");
        body.addln();

        if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
            body.addln("Do you want to come in and talk about your situation?  We might be able ",
                    "to suggest strategies to move forward, or talk about options.");
        } else {
            body.addln("Do you want to try to schedule a Teams or Zoom meeting to talk about ",
                    "your situation?  We might be able to suggest strategies to move forward, or talk ",
                    "about options.");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.UE4, EMsg.UE4Rue03, "Checking in", body.toString());
    }

    /**
     * Generates a report row for a UE4Rue04 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE4Rue04(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String subject = "Checking in";

        // Only message for RE 4 for the first two courses - after that, assume they're OK.

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("You've passed the Unit 4 Review exam (good job!), and it looks like you've ",
                "tried out the proctored Unit 4 exam.  Let me know if you have any questions.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.UE4, EMsg.UE4Rue04, subject, body.toString());
    }

    /**
     * Generates a report row for a UE4Rue05 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE4Rue05(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to try to complete the Unit 4 exam...");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.UE4, EMsg.UE4Rue05, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a UE4Rue06 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE4Rue06(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // We're going to skip this for all students

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.UE4, EMsg.UE4Rue06, null, null);
    }

    /**
     * Generates a report row for a UE4Rue07 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE4Rue07(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("I've noticed you haven't gotten past the Unit 4 Exam yet - are ",
                "things going OK?  If there's anything you need, let me know.");
        body.addln();

        if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
            body.addln("Do you want to come in and talk about your situation?  We might be able ",
                    "to suggest strategies to move forward, or talk about options.");
        } else {
            body.addln("Do you want to try to schedule a Teams or Zoom meeting to talk about ",
                    "your situation?  We might be able to suggest strategies to move forward, or talk ",
                    "about options.");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.UE4, EMsg.UE4Rue07, "Checking in", body.toString());
    }

    /**
     * Generates a report row for a UE4Xue00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE4Xue00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String subject = "Checking in";

        // Only message for RE 4 for the first two courses - after that, assume they're OK.

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("You've passed the Unit 4 Review exam (good job!), and it looks like you've ",
                "tried out the proctored Unit 4 exam - let me know if you have any questions.");
        body.addln();

        if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
            body.addln("If you're stuck on some items, please swing by the Precalculus Center ",
                    "and sit down with a Learning Assistant, or tap in to the online help ",
                    "if you can.");
        } else {
            body.addln("If you're stuck on some items, please tap in to the online help if ",
                    "you can.");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.UE4, EMsg.UE4Xue00, subject, body.toString());
    }

    /**
     * Generates a report row for a UE4Xue01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE4Xue01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to try to complete the Unit 4 Exam...");
            body.addln();

            body.addln(
                    "If you're struggling to get through this exam, let me know.  I'll try to help ",
                    "if I can.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.UE4, EMsg.UE4Xue01, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a UE4Xue02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE4Xue02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to try to complete the Unit 4 Exam...");
            body.addln();

            body.addln(
                    "If you're struggling to get through this exam, let me know.  I'll try to help ",
                    "if I can.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.UE4, EMsg.UE4Xue02, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a UE4Xue03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE4Xue03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("It seems like this Unit Exam is causing some difficulty.  ");

        if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
            body.addln("Do you want to come in and talk about your situation?  We might be able ",
                    "to suggest strategies to move forward, or talk about options.");
        } else {
            body.addln("Do you want to try to schedule a Teams or Zoom meeting to talk about ",
                    "your situation?  We might be able to suggest strategies to move forward, or talk ",
                    "about options.");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.UE4, EMsg.UE4Xue03, "Checking in", body.toString());
    }

    /**
     * Generates a report row for a UE4Rue99 message. This applies to students who have been stuck on the Unit 4 Exam
     * through all 4 messages.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE4Rue99(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("It looks like you're stuck on the ", crsName,
                " Unit 4 Exam.  I'd like to help - ");

        if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
            body.addln("can you come into the Precalculus Center to talk about things?");
        } else {
            body.addln("do you want to set up a Teams meeting to talk about things?");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.UE4, EMsg.UE4Rue99, "Stuck on Unit 4 Exam?", body.toString());
    }
}

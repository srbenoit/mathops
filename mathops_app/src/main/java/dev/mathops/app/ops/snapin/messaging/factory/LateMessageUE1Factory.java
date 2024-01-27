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
 * A factory class that can generate messages to students who are "late" (urgency >= 1) who have not passed the unit 1
 * exam.
 */
public enum LateMessageUE1Factory {
    ;

    /**
     * Generates an appropriate message for a student who has not yet passed unit exam 1.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    public static MessageToSend generate(final MessagingContext context, final MessagingCourseStatus status) {

        final MessageToSend result;

        final int tries = status.failedTriesOnUE1;

        if (tries == 0) {
            // RE 1 not attempted
            result = generateNotTried(context, status);
        } else if (tries < 4) {
            // RE 1 attempted <4 times, not passed
            result = generateFailedFew(context, status);
        } else {
            // RE 1 attempted >=4 times, not passed
            result = generateFailedMany(context, status);
        }

        return result;
    }

    /**
     * Generates an appropriate message when Unit Exam 1 has not been attempted.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateNotTried(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (status.daysSinceLastActivity > 3) {
            final boolean noUE0 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE1Rue00);
            final boolean noUE1 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE1Rue01);
            final boolean noUE2 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE1Rue02);
            final boolean noUE3 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE1Rue03);

            if (noUE0) {
                result = generateUE1Rue00(context, status);
            } else if (noUE1) {
                result = generateUE1Rue01(context, status);
            } else if (noUE2) {
                result = generateUE1Rue02(context, status);
            } else if (noUE3) {
                result = generateUE1Rue03(context, status);
            } else if (status.daysSinceLastActivity > 3) {
                final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE1Rue99);

                if (no99) {
                    result = generateUE1Rue99(context, status);
                } else {
                    Log.info("Student ", context.student.stuId, " stuck on Unit Exam 1");
                }
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when Unit Exam 1 has been attempted less than 4 times.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFailedFew(final MessagingContext context,
                                                   final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (status.daysSinceLastActivity > 3) {
            final Period sinceLastTry = Period.between(status.lastU1Try, context.today);
            final int daysSinceLastTry = sinceLastTry.getDays();

            if (daysSinceLastTry >= 2) {
                final boolean noUE04 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE1Rue00, EMsg.UE1Rue04);
                final boolean noUE15 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE1Rue01, EMsg.UE1Rue05);
                final boolean noUE26 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE1Rue02, EMsg.UE1Rue06);
                final boolean noUE37 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE1Rue03, EMsg.UE1Rue07);

                if (noUE04) {
                    result = generateUE1Rue04(context, status);
                } else if (noUE15) {
                    result = generateUE1Rue05(context, status);
                } else if (noUE26) {
                    result = generateUE1Rue06(context, status);
                } else if (noUE37) {
                    result = generateUE1Rue07(context, status);
                } else if (status.daysSinceLastActivity > 3) {
                    final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE1Rue99);

                    if (no99) {
                        result = generateUE1Rue99(context, status);
                    } else {
                        Log.info("Student ", context.student.stuId, " stuck on Unit Exam 1");
                    }
                }
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when Unit Exam 1 has been attempted 4 or more times but not yet passed.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFailedMany(final MessagingContext context,
                                                    final MessagingCourseStatus status) {

        MessageToSend result = null;

        final boolean noUE040 =
                MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE1Rue00, EMsg.UE1Rue04, EMsg.UE1Xue00);
        final boolean noUE1512 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE1Rue01, EMsg.UE1Rue05,
                EMsg.UE1Xue01, EMsg.UE1Xue02);
        final boolean noUE37 =
                MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE1Rue03, EMsg.UE1Rue07, EMsg.UE1Xue03);

        if (noUE040) {
            result = generateUE1Xue00(context, status);
        } else if (noUE1512) {
            final Period sinceLastTry = Period.between(status.lastU1Try, context.today);
            final int daysSinceLastTry = sinceLastTry.getDays();

            if (daysSinceLastTry > 2) {
                result = generateUE1Xue01(context, status);
            } else {
                result = generateUE1Xue02(context, status);
            }
        } else if (noUE37) {
            result = generateUE1Xue03(context, status);
        } else if (status.daysSinceLastActivity > 3) {
            final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE1Rue99);

            if (no99) {
                result = generateUE1Rue99(context, status);
            } else {
                Log.info("Student ", context.student.stuId, " stuck on Unit Exam 1");
            }
        }

        return result;
    }

    /**
     * Generates a report row for a UE1Rue00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE1Rue00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String subject = "Checking in";

        // Only message for RE 1 for the first two courses - after that, assume they're OK.

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        if (context.currentRegIndex == 0) {
            // First experience with a Unit Exam
            body.addln("You've passed the Unit 1 Review exam - well done!  ",
                    "The proctored Unit 1 exam is next.");
            body.addln();

            if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
                if (status.isInSpecial("RAMWORK")) {
                    body.addln("You can take that exam through the online proctoring system.  ",
                            "You can't use reference materials, but you can use blank scratch paper ",
                            "and your own calculator.");
                } else {
                    body.addln(
                            "You can take that exam in the Precalculus Center (Weber 137).  All ",
                            "you need is your RamCard and something to write with - we will provide ",
                            "scratch paper, and there will be a TI-84 calculator on-screen on the testing ",
                            "station.  We have lockers if you need to lock up your things to take that ",
                            "exam.");
                }
                body.addln();
            } else {
                body.addln("You should be able to take that exam online using a Webcam for ",
                        "proctoring.  You won't be able to use reference materials, but you can use ",
                        "blank scratch paper and your own calculator.");
                body.addln();
            }

            body.addln("You get 2 tries on the Unit exam initially, and if you don't pass on ",
                    "those two tries, you would need to re-pass the Review Exam to earn two more ",
                    "tries.  There is no limit to total attempts on a Unit exam.");
            body.addln();
        } else {
            body.addln("Good job on the Unit 1 Review exam - the proctored Unit 1 Exam is next...");
            body.addln();
        }

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.UE1, EMsg.UE1Rue00, subject, body.toString());
    }

    /**
     * Generates a report row for a UE1Rue01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE1Rue01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to try to complete the Unit 1 exam...");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.UE1, EMsg.UE1Rue01, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a UE1Rue02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE1Rue02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // We're going to skip this for all students

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.UE1, EMsg.UE1Rue02, null, null);
    }

    /**
     * Generates a report row for a UE1Rue03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE1Rue03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("I've noticed you haven't tried the Unit 1 Exam yet - are ",
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
                EMilestone.UE1, EMsg.UE1Rue03, "Checking in", body.toString());
    }

    /**
     * Generates a report row for a UE1Rue04 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE1Rue04(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String subject = "Checking in";

        // Only message for RE 1 for the first two courses - after that, assume they're OK.

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("You've passed the Unit 1 Review exam (well done), and it looks like you've ",
                "tried out the proctored Unit 1 exam.");
        body.addln();

        if (context.currentRegIndex == 0) {
            body.addln("You get 2 tries on the Unit exam initially, and if you don't pass on ",
                    "those two tries, you would need to re-pass the Review Exam to earn two more ",
                    "tries.  There is no limit to total attempts on a Unit exam.");
        } else {
            body.addln("Let me know if you need help with anything.");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.UE1, EMsg.UE1Rue04, subject, body.toString());
    }

    /**
     * Generates a report row for a UE1Rue05 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE1Rue05(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to try to complete the Unit 1 exam...");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.UE1, EMsg.UE1Rue05, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a UE1Rue06 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE1Rue06(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // We're going to skip this for all students

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.UE1, EMsg.UE1Rue06, null, null);
    }

    /**
     * Generates a report row for a UE1Rue07 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE1Rue07(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("I've noticed you haven't gotten past the Unit 1 Exam yet - are ",
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
                EMilestone.UE1, EMsg.UE1Rue07, "Checking in", body.toString());
    }

    /**
     * Generates a report row for a UE1Xue00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE1Xue00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String subject = "Checking in";

        // Only message for RE 1 for the first two courses - after that, assume they're OK.

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("You've passed the Unit 1 Review exam (well done), and it looks like you've ",
                "tried out the proctored Unit 1 exam.");
        body.addln();

        if (context.currentRegIndex == 0) {
            body.addln("You get 2 tries on the Unit exam initially, and if you don't pass on ",
                    "those two tries, you would need to re-pass the Review Exam to earn two more ",
                    "tries.  There is no limit to total attempts on a Unit exam.");
            body.addln();
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
                EMilestone.UE1, EMsg.UE1Xue00, subject, body.toString());
    }

    /**
     * Generates a report row for a UE1Xue01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE1Xue01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to try to complete the Unit 1 Exam...");
            body.addln();

            body.addln("If you're struggling to get through this exam, let me know.  I'll try to help ",
                    "if I can.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.UE1, EMsg.UE1Xue01, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a UE1Xue02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE1Xue02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to try to complete the Unit 1 Exam...");
            body.addln();

            body.addln("If you're struggling to get through this exam, let me know.  I'll try to help ",
                    "if I can.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.UE1, EMsg.UE1Xue02, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a UE1Xue03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE1Xue03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("It seems like this Unit Exam has slowed you down a bit.  ");

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
                EMilestone.UE1, EMsg.UE1Xue03, "Checking in", body.toString());
    }

    /**
     * Generates a report row for a UE1Rue99 message. This applies to students who have been stuck on the Unit 1 Exam
     * through all 4 messages.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUE1Rue99(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("It looks like you're stuck on the ", crsName,
                " Unit 1 Exam.  I'd like to help - ");

        if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
            body.addln("can you come into the Precalculus Center to talk about things?");
        } else {
            body.addln("do you want to set up a Teams meeting to talk about things?");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.UE1, EMsg.UE1Rue99, "Stuck on Unit 1 Exam?", body.toString());
    }
}

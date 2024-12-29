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
 * 1.1.
 */
public enum LateMessageHW11Factory {
    ;

    /**
     * Generates an appropriate message for a student who has not yet passed homework 1.1.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    public static MessageToSend generate(final MessagingContext context,
                                         final MessagingCourseStatus status) {

        final MessageToSend result;

        final Integer hwTries = status.triesOnUnit1HW.get(MsgUtils.ONE);
        final int tries = hwTries == null ? 0 : hwTries.intValue();

        if (tries == 0) {
            // HW 1.1 not attempted
            result = generateNotTried(context, status);
        } else if (tries < 4) {
            // HW 1.1 attempted <4 times, not passed
            result = generateFailedFew(context, status);
        } else {
            // HW 1.1 attempted >=4 times, not passed
            result = generateFailedMany(context, status);
        }

        return result;
    }

    /**
     * Generates an appropriate message when Homework 1.1 has not been attempted.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateNotTried(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (status.daysSinceLastActivity > 3) {
            final boolean noHW0 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H11Rhw00);
            final boolean noHW1 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H11Rhw01);
            final boolean noHW2 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H11Rhw02);
            final boolean noHW3 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H11Rhw03);

            if (noHW0) {
                result = generateH11Rhw00(context, status);
            } else if (noHW1) {
                result = generateH11Rhw01(context, status);
            } else if (noHW2) {
                result = generateH11Rhw02(context, status);
            } else if (noHW3) {
                result = generateH11Rhw03(context, status);
            } else if (status.daysSinceLastActivity > 3) {
                final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H11Rhw99);

                if (no99) {
                    result = generateH11Rhw99(context, status);
                } else {
                    Log.info("Student ", context.student.stuId, " stuck on HW 1.1");
                }
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when Homework 1.1 has been attempted less than 4 times.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFailedFew(final MessagingContext context,
                                                   final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (status.daysSinceLastActivity > 3) {
            final Period sinceLastTry = Period.between(status.lastH11Try, context.today);
            final int daysSinceLastTry = sinceLastTry.getDays();

            if (daysSinceLastTry >= 2) {
                final boolean noHW04 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H11Rhw00, EMsg.H11Rhw04);
                final boolean noHW15 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H11Rhw01, EMsg.H11Rhw05);
                final boolean noHW26 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H11Rhw02, EMsg.H11Rhw06);
                final boolean noHW37 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H11Rhw03, EMsg.H11Rhw07);

                if (noHW04) {
                    result = generateH11Rhw04(context, status);
                } else if (noHW15) {
                    result = generateH11Rhw05(context, status);
                } else if (noHW26) {
                    result = generateH11Rhw06(context, status);
                } else if (noHW37) {
                    result = generateH11Rhw07(context, status);
                } else if (status.daysSinceLastActivity > 3) {
                    final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H11Rhw99);

                    if (no99) {
                        result = generateH11Rhw99(context, status);
                    } else {
                        Log.info("Student ", context.student.stuId, " stuck on HW 1.1");
                    }
                }
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when Homework 1.1 has been attempted 4 or more times but not yet passed.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFailedMany(final MessagingContext context,
                                                    final MessagingCourseStatus status) {

        MessageToSend result = null;

        final boolean noHW040 =
                MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H11Rhw00, EMsg.H11Rhw04, EMsg.H11Xhw00);
        final boolean noHW1512 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H11Rhw01, EMsg.H11Rhw05,
                EMsg.H11Xhw01, EMsg.H11Xhw02);
        final boolean noHW37 =
                MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H11Rhw03, EMsg.H11Rhw07, EMsg.H11Xhw03);

        if (noHW040) {
            result = generateH11Xhw00(context, status);
        } else if (noHW1512) {
            final Period sinceLastTry = Period.between(status.lastH11Try, context.today);
            final int daysSinceLastTry = sinceLastTry.getDays();

            if (daysSinceLastTry > 2) {
                result = generateH11Xhw01(context, status);
            } else {
                result = generateH11Xhw02(context, status);
            }
        } else if (noHW37) {
            result = generateH11Xhw03(context, status);
        } else if (status.daysSinceLastActivity > 3) {
            final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.H11Rhw99);

            if (no99) {
                result = generateH11Rhw99(context, status);
            } else {
                Log.info("Student ", context.student.stuId, " stuck on HW 1.1");
            }
        }

        return result;
    }

    /**
     * Generates a report row for a H11Rhw00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH11Rhw00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            String subject = "Checking in";

            // Only message for HW 1.1 for the first two courses - after that, assume they're OK.

            final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
            final String crsName = MsgUtils.courseName(courseId);

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            if (context.currentRegIndex == 0) {
                // First experience with a Homework
                body.addln("You've gotten through the ", crsName,
                        " Skills Review - well done!  The ",
                        "next step is to complete the Objective assignments for Unit 1.  Each objective ",
                        "has a lecture video and some PDF materials, and then (once you've seen the ",
                        "lecture video), you will have access to a 3-question assignment over that ",
                        "material.");
                body.addln();

                body.addln(
                        "You have to get all three correct, but you get unlimited tries on each ",
                        "item.  Once you pass one, the next objective will open up.");
                body.addln();
                MsgUtils.emitSimpleClosing(body);
            } else if (context.currentRegIndex == 1) {
                body.addln("Well-done getting through the ", crsName, " Skills Review - the ",
                        "Objective 1.1 assignment is next...");
                body.addln();
                MsgUtils.emitSimpleClosing(body);
            } else {
                // A non-sent placeholder to track that we've decided to send nothing
                subject = null;
                body.reset();
            }

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW11, EMsg.H11Rhw00, subject, body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H11Rhw01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH11Rhw01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
            final String crsName = MsgUtils.courseName(courseId);

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            if (context.currentRegIndex <= 1) {
                // This population will have gotten the earlier message
                body.addln("Just a reminder to try to tackle the Objective 1.1 assignment.  You ",
                        "probably don't need to watch every example video before trying the assignment, ",
                        "you can use them to review the types of problem that cause trouble.");
            } else {
                // This will be the first message for courses 3-5
                body.addln("Now that the ", crsName,
                        " Skills Review is out of the way (well-done), ",
                        "the Unit 1 objective assignments are available.");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW11, EMsg.H11Rhw01, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H11Rhw02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH11Rhw02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // We're going to skip this for all students

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW11, EMsg.H11Rhw02, null, null);
    }

    /**
     * Generates a report row for a H11Rhw03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH11Rhw03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("I've noticed you haven't started the Unit 1 objective assignments yet - are ",
                    "things going OK?  If there's anything you need, let me know.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW11, EMsg.H11Rhw03, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H11Rhw04 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH11Rhw04(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            String subject = "Checking in";

            // Only message for HW 1.1 for the first two courses - after that, assume they're OK.

            final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
            final String crsName = MsgUtils.courseName(courseId);

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            if (context.currentRegIndex == 0) {
                // First experience with a Homework
                body.addln("You've gotten through the ", crsName,
                        " Skills Review - well done!  And ",
                        "you've gotten into the Objective assignments for Unit 1.");
                body.addln();

                body.addln(
                        "All of these assignments will be three questions.  You have to get all ",
                        "three correct, but you get unlimited tries on each item.  Once you pass one, ",
                        "the next objective will open up.");
                body.addln();
                MsgUtils.emitSimpleClosing(body);
            } else if (context.currentRegIndex == 1) {
                body.addln("Well-done getting through the ", crsName, " Skills Review and getting ",
                        "started on Objective 1.1.");
                body.addln();
                MsgUtils.emitSimpleClosing(body);
            } else {
                // A non-sent placeholder to track that we've decided to send nothing
                subject = null;
                body.reset();
            }

            result =
                    new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW11, EMsg.H11Rhw04, subject, body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H11Rhw05 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH11Rhw05(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            if (context.currentRegIndex <= 1) {
                // This population will have gotten the earlier message
                body.addln(
                        "Just a reminder to try to finish up the Objective 1.1 assignment.  You ",
                        "probably don't need to watch every example video before trying the assignment, ",
                        "you can use them to review the types of problem that cause trouble.");
            } else {
                // This will be the first message for courses 3-5
                body.addln("I see you've jumped into the Objective 1.1 material - very good.  You ",
                        "probably don't need to watch every example video before trying the assignment, ",
                        "you can use them to review the types of problem that cause trouble.");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result =
                    new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW11, EMsg.H11Rhw05, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H11Rhw06 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH11Rhw06(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // We're going to skip this for all students

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW11, EMsg.H11Rhw06, null, null);
    }

    /**
     * Generates a report row for a H11Rhw07 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH11Rhw07(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.add("I've noticed you haven't completed the Objective 1.1 assignment yet - if any ",
                    "of those questions are causing trouble, ");

            if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
                body.addln("you can drop in and work with our Learning Assistants, or jump into the ",
                        "online help hours to get some help.");
            } else {
                body.addln("you can jump into the online help hours to get some help.");
            }

            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW11, EMsg.H11Rhw07, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H11Xhw00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH11Xhw00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            String subject = "Checking in";

            // Only message for HW 1.1 for the first two courses - after that, assume they're OK.

            final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
            final String crsName = MsgUtils.courseName(courseId);

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            if (context.currentRegIndex == 0) {
                // First experience with a Homework
                body.addln("You've gotten through the ", crsName,
                        " Skills Review - well done!  And ",
                        "you've gotten into the Objective assignments for Unit 1.");
                body.addln();

                body.addln("All of these assignments will be three questions.  You have to get all ",
                        "three correct, but you get unlimited tries on each item.  Once you pass one, ",
                        "the next objective will open up.");
                body.addln();

                if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
                    body.addln("If the questions are causing trouble, please stop by the Precalculus ",
                            "Center and work with a Learning Assistant, or connect with the online help ",
                            "if you can.");
                } else {
                    body.addln("If the questions are causing trouble, please connect with the ",
                            "online help if you can.");
                }
                body.addln();
                MsgUtils.emitSimpleClosing(body);
            } else if (context.currentRegIndex == 1) {
                body.addln("Well-done getting through the ", crsName, " Skills Review and getting ",
                        "started on Objective 1.1.");
                body.addln();

                if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
                    body.addln(
                            "If the questions are causing trouble, please stop by the Precalculus ",
                            "Center and work with a Learning Assistant, or connect with the online help ",
                            "if you can.");
                } else {
                    body.addln("If the questions are causing trouble, please connect with the ",
                            "online help if you can.");
                }
                body.addln();
                MsgUtils.emitSimpleClosing(body);
            } else {
                // A non-sent placeholder to track that we've decided to send nothing
                subject = null;
                body.reset();
            }

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW11, EMsg.H11Xhw00, subject, body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H11Xhw01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH11Xhw01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            if (context.currentRegIndex <= 1) {
                // This population will have gotten the earlier message
                body.addln("Just a reminder to try to finish up the Objective 1.1 assignment.");
            } else {
                // This will be the first message for courses 3-5
                body.addln("I see you've jumped into the Objective 1.1 material - very good.");
            }
            body.addln();

            body.add("If this objective is causing trouble, don't give up - we're here to help!  ");
            if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
                body.addln("You can stop by the Precalculus Center and work with a Learning ",
                        "Assistant, or connect with the online help to get things figured out.");
            } else {
                body.addln("You can connect with the online help to get things figured out.");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW11, EMsg.H11Xhw01, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H11Xhw02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH11Xhw02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            if (context.currentRegIndex <= 1) {
                // This population will have gotten the earlier message
                body.addln("Just a reminder to try to finish up the Objective 1.1 assignment.");
            } else {
                // This will be the first message for courses 3-5
                body.addln("I see you've jumped into the Objective 1.1 material - very good.");
            }
            body.addln();

            body.add("If this objective is causing trouble, we're here to help!  ");
            if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
                body.addln("You can stop by the Precalculus Center and work with a Learning ",
                        "Assistant, or connect with the online help to get things figured out.");
            } else {
                body.addln("You can connect with the online help to get things figured out.");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW11, EMsg.H11Xhw02, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H11Xhw03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateH11Xhw03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 3) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.add("It looks like Objective 1.1 is getting you bogged down - I'd like to help you ",
                    "get through that one so you don't get too far behind going forward.  Let me know ",
                    "what I can do.");

            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.HW11, EMsg.H11Xhw03, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a H11Rhw99 message. This applies to students who have been stuck on homework 1.1
     * through all 4 messages.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateH11Rhw99(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("It looks like you're stuck in the objective 1.1 assignment in ", crsName,
                ".  I'd like to help - ");

        if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
            body.addln("can you come into the Precalculus Center to talk about things?");
        } else {
            body.addln("do you want to set up a Teams meeting to talk about things?");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.HW11, EMsg.H11Rhw99, "Stuck on " + crsName + " assignment?",
                body.toString());
    }
}

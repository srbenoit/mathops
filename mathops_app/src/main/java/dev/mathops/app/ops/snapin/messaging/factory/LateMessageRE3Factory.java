package dev.mathops.app.ops.snapin.messaging.factory;

import dev.mathops.commons.log.Log;
import dev.mathops.app.ops.snapin.messaging.EMilestone;
import dev.mathops.app.ops.snapin.messaging.EMsg;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;
import dev.mathops.app.ops.snapin.messaging.MsgUtils;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;
import dev.mathops.text.builder.HtmlBuilder;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * A factory class that can generate messages to students who are "late" (urgency >= 1) who have not passed the unit 3
 * review.
 */
public enum LateMessageRE3Factory {
    ;

    /**
     * Generates an appropriate message for a student who has not yet passed review exam 1.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    public static MessageToSend generate(final MessagingContext context, final MessagingCourseStatus status) {

        final MessageToSend result;

        final int tries = status.failedTriesOnRE3;

        if (tries == 0) {
            // RE 3 not attempted
            result = generateNotTried(context, status);
        } else if (tries < 4) {
            // RE 3 attempted <4 times, not passed
            result = generateFailedFew(context, status);
        } else {
            // RE 3 attempted >=4 times, not passed
            result = generateFailedMany(context, status);
        }

        return result;
    }

    /**
     * Generates an appropriate message when Review Exam 3 has not been attempted.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateNotTried(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (status.daysSinceLastActivity > 3) {
            final boolean noRE0 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE3Rre00);
            final boolean noRE1 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE3Rre01);
            final boolean noRE2 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE3Rre02);
            final boolean noRE3 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE3Rre03);

            if (noRE0) {
                result = generateRE3Rre00(context, status);
            } else if (noRE1) {
                result = generateRE3Rre01(context, status);
            } else if (noRE2) {
                result = generateRE3Rre02(context, status);
            } else if (noRE3) {
                result = generateRE3Rre03(context, status);
            } else if (status.daysSinceLastActivity > 3) {
                final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE3Rre99);

                if (no99) {
                    result = generateRE3Rre99(context, status);
                } else {
                    Log.info("Student ", context.student.stuId, " stuck on Unit 3 Review exam");
                }
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when Review Exam 3 has been attempted less than 4 times.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFailedFew(final MessagingContext context,
                                                   final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (status.daysSinceLastActivity > 3) {
            final Period sinceLastTry = Period.between(status.lastR3Try, context.today);
            final int daysSinceLastTry = sinceLastTry.getDays();

            if (daysSinceLastTry >= 2) {
                final boolean noRE04 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE3Rre00, EMsg.RE3Rre04);
                final boolean noRE35 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE3Rre01, EMsg.RE3Rre05);
                final boolean noRE26 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE3Rre02, EMsg.RE3Rre06);
                final boolean noRE37 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE3Rre03, EMsg.RE3Rre07);

                if (noRE04) {
                    result = generateRE3Rre04(context, status);
                } else if (noRE35) {
                    result = generateRE3Rre05(context, status);
                } else if (noRE26) {
                    result = generateRE3Rre06(context, status);
                } else if (noRE37) {
                    result = generateRE3Rre07(context, status);
                } else if (status.daysSinceLastActivity > 3) {
                    final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE3Rre99);

                    if (no99) {
                        result = generateRE3Rre99(context, status);
                    } else {
                        Log.info("Student ", context.student.stuId, " stuck on Unit 3 Review exam");
                    }
                }
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when Review Exam 3 has been attempted 4 or more times but not yet passed.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFailedMany(final MessagingContext context,
                                                    final MessagingCourseStatus status) {

        MessageToSend result = null;

        final boolean noRE040 =
                MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE3Rre00, EMsg.RE3Rre04, EMsg.RE3Xre00);
        final boolean noRE3512 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE3Rre01, EMsg.RE3Rre05,
                EMsg.RE3Xre01, EMsg.RE3Xre02);
        final boolean noRE37 =
                MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE3Rre03, EMsg.RE3Rre07, EMsg.RE3Xre03);

        if (noRE040) {
            result = generateRE3Xre00(context, status);
        } else if (noRE3512) {
            final Period sinceLastTry = Period.between(status.lastR3Try, context.today);
            final int daysSinceLastTry = sinceLastTry.getDays();

            if (daysSinceLastTry > 2) {
                result = generateRE3Xre01(context, status);
            } else {
                result = generateRE3Xre02(context, status);
            }
        } else if (noRE37) {
            result = generateRE3Xre03(context, status);
        } else if (status.daysSinceLastActivity > 3) {
            final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE3Rre99);

            if (no99) {
                result = generateRE3Rre99(context, status);
            } else {
                Log.info("Student ", context.student.stuId, " stuck on Unit 3 Review exam");
            }
        }

        return result;
    }

    /**
     * Generates a report row for a RE3Rre00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateRE3Rre00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String subject = "Checking in";

        // Only message for RE 3 for the first two courses - after that, assume they're OK.

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Good job getting through all the Unit 3 objectives - ",
                "the Unit 3 Review Exam is next...");
        body.addln();

        final LocalDate plus4 = context.today.plusDays(4L);

        if (plus4.isAfter(status.milestones.r3) && !context.today.isAfter(status.milestones.r3)) {
            final String dayName =
                    status.milestones.r3.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);

            if (context.today.equals(status.milestones.r3)) {
                body.addln("The due date for that Review Exam is today.");
            } else {
                body.addln("The due date for that Review Exam is coming up on ", dayName, ".");
            }
            body.addln();
        }

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.RE3, EMsg.RE3Rre00, subject, body.toString());
    }

    /**
     * Generates a report row for a RE3Rre01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateRE3Rre01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        final LocalDate plus4 = context.today.plusDays(4L);
        if (status.daysSinceLastActivity > 3 || plus4.isAfter(status.milestones.r4)
                && !context.today.isAfter(status.milestones.r4)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.add("Just a reminder to try to get the Unit 3 Review exam passed.");

            if (plus4.isAfter(status.milestones.r3)) {
                final String dayName = context.today.equals(status.milestones.r3) ? "today"
                        : status.milestones.r3.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
                body.add(
                        "  The deadline to get the 3 points for completing the Review Exam on time ",
                        "is ", dayName, ".");
            }

            body.addln();
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result =
                    new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.RE3, EMsg.RE3Rre01, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a RE3Rre02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateRE3Rre02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // We're going to skip this for all students

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.RE3, EMsg.RE3Rre02, null, null);
    }

    /**
     * Generates a report row for a RE3Rre03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateRE3Rre03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("I've noticed you haven't tried the Unit 3 Review Exam yet - are ",
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
                EMilestone.RE3, EMsg.RE3Rre03, "Checking in", body.toString());
    }

    /**
     * Generates a report row for a RE3Rre04 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateRE3Rre04(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String subject = "Checking in";

        // Only message for RE 3 for the first two courses - after that, assume they're OK.

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Good job getting through all the Unit 3 objectives and trying out ",
                "the Unit 3 Review Exam.");
        body.addln();

        final LocalDate plus4 = context.today.plusDays(4L);

        if (plus4.isAfter(status.milestones.r3) && !context.today.isAfter(status.milestones.r3)) {
            final String dayName =
                    status.milestones.r3.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);

            if (context.today.equals(status.milestones.r3)) {
                body.addln("The due date for that Review Exam is today.");
            } else {
                body.addln("The due date for that Review Exam is coming up on ", dayName, ".");
            }
            body.addln();
        } else {
            body.addln("Let me know if you need anything.");
            body.addln();
        }

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.RE3, EMsg.RE3Rre04, subject, body.toString());
    }

    /**
     * Generates a report row for a RE3Rre05 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateRE3Rre05(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        final LocalDate plus4 = context.today.plusDays(4L);
        if (status.daysSinceLastActivity > 3 || plus4.isAfter(status.milestones.r4)
                && !context.today.isAfter(status.milestones.r4)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.add("Just a reminder to try to get the Unit 3 Review exam passed.");

            if (plus4.isAfter(status.milestones.r3)
                    && !context.today.isAfter(status.milestones.r3)) {
                final String dayName = context.today.equals(status.milestones.r3) ? "today"
                        : status.milestones.r3.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
                body.add("  The deadline to get the 3 points for completing the Review Exam on time ",
                        "is ", dayName, ".");
            }

            body.addln();
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.RE3, EMsg.RE3Rre05, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a RE3Rre06 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateRE3Rre06(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // We're going to skip this for all students

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.RE3, EMsg.RE3Rre06, null, null);
    }

    /**
     * Generates a report row for a RE3Rre07 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateRE3Rre07(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("I've noticed you haven't gotten past the Unit 3 Review Exam yet - are ",
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
                EMilestone.RE3, EMsg.RE3Rre07, "Checking in", body.toString());
    }

    /**
     * Generates a report row for a RE3Xre00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateRE3Xre00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String subject = "Checking in";

        // Only message for RE 3 for the first two courses - after that, assume they're OK.

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Good job getting through all the Unit 3 objectives and trying out  ",
                "the Unit 3 Review Exam.");
        body.addln();

        final LocalDate plus4 = context.today.plusDays(4L);

        if (plus4.isAfter(status.milestones.r3) && !context.today.isAfter(status.milestones.r3)) {
            final String dayName =
                    status.milestones.r3.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);

            if (context.today.equals(status.milestones.r3)) {
                body.addln("The due date for that Review Exam is today.");
            } else {
                body.addln("The due date for that Review Exam is coming up on ", dayName, ".");
            }
            body.addln();
        }

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
                EMilestone.RE3, EMsg.RE3Xre00, subject, body.toString());
    }

    /**
     * Generates a report row for a RE3Xre01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateRE3Xre01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        final LocalDate plus4 = context.today.plusDays(4L);
        if (status.daysSinceLastActivity > 3 || plus4.isAfter(status.milestones.r4)
                && !context.today.isAfter(status.milestones.r4)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.add("Just a reminder to try to get the Unit 3 Review exam passed.");

            if (plus4.isAfter(status.milestones.r3)
                    && !context.today.isAfter(status.milestones.r3)) {
                final String dayName = context.today.equals(status.milestones.r3) ? "today"
                        : status.milestones.r3.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
                body.add("  The deadline to get the 3 points for completing the Review Exam on time ",
                        "is ", dayName, ".");
            }
            body.addln();
            body.addln();

            body.addln("If you're struggling to get through this exam, let me know.  I'll try to help ",
                    "if I can.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.RE3, EMsg.RE3Xre01, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a RE3Xre02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateRE3Xre02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        final LocalDate plus4 = context.today.plusDays(4L);
        if (status.daysSinceLastActivity > 3 || plus4.isAfter(status.milestones.r4)
                && !context.today.isAfter(status.milestones.r4)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.add("Just a reminder to try to get the Unit 3 Review exam passed.");

            if (plus4.isAfter(status.milestones.r3)
                    && !context.today.isAfter(status.milestones.r3)) {
                final String dayName = context.today.equals(status.milestones.r3) ? "today"
                        : status.milestones.r3.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
                body.add("  The deadline to get the 3 points for completing the Review Exam on time ",
                        "is ", dayName, ".");
            }
            body.addln();
            body.addln();

            body.addln("If you're struggling to get through this exam, let me know.  I'll try to help ",
                    "if I can.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.RE3, EMsg.RE3Xre02, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a RE3Xre03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateRE3Xre03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("It seems like this Review Exam is a tough one.  ");

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
                EMilestone.RE3, EMsg.RE3Xre03, "Checking in", body.toString());
    }

    /**
     * Generates a report row for a RE3Rre99 message. This applies to students who have been stuck on the Unit 3 Review
     * Exam through all 4 messages.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generateRE3Rre99(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("It looks like you're stuck on the ", crsName,
                " Unit 3 Review Exam.  I'd like to help - ");

        if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
            body.addln("can you come into the Precalculus Center to talk about things?");
        } else {
            body.addln("do you want to set up a Teams meeting to talk about things?");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.RE3, EMsg.RE3Rre99, "Stuck on Unit 3 Review Exam?", body.toString());
    }
}

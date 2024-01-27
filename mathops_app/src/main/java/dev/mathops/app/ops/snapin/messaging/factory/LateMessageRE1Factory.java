package dev.mathops.app.ops.snapin.messaging.factory;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.app.ops.snapin.messaging.EMilestone;
import dev.mathops.app.ops.snapin.messaging.EMsg;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;
import dev.mathops.app.ops.snapin.messaging.MsgUtils;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * A factory class that can generate messages to students who are "late" (urgency >= 1) who have not passed the unit 1
 * review.
 */
public enum LateMessageRE1Factory {
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

        final int tries = status.failedTriesOnRE1;

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
     * Generates an appropriate message when Review Exam 1 has not been attempted.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateNotTried(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (status.daysSinceLastActivity > 3) {
            final boolean noRE0 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE1Rre00);
            final boolean noRE1 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE1Rre01);
            final boolean noRE2 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE1Rre02);
            final boolean noRE3 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE1Rre03);

            if (noRE0) {
                result = generateRE1Rre00(context, status);
            } else if (noRE1) {
                result = generateRE1Rre01(context, status);
            } else if (noRE2) {
                result = generateRE1Rre02(context, status);
            } else if (noRE3) {
                result = generateRE1Rre03(context, status);
            } else if (status.daysSinceLastActivity > 3) {
                final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE1Rre99);

                if (no99) {
                    result = generateRE1Rre99(context, status);
                } else {
                    Log.info("Student ", context.student.stuId, " stuck on Unit 1 Review exam");
                }
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when Review Exam 1 has been attempted less than 4 times.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFailedFew(final MessagingContext context,
                                                   final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (status.daysSinceLastActivity > 3) {
            final Period sinceLastTry = Period.between(status.lastR1Try, context.today);
            final int daysSinceLastTry = sinceLastTry.getDays();

            if (daysSinceLastTry >= 2) {
                final boolean noRE04 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE1Rre00, EMsg.RE1Rre04);
                final boolean noRE15 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE1Rre01, EMsg.RE1Rre05);
                final boolean noRE26 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE1Rre02, EMsg.RE1Rre06);
                final boolean noRE37 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE1Rre03, EMsg.RE1Rre07);

                if (noRE04) {
                    result = generateRE1Rre04(context, status);
                } else if (noRE15) {
                    result = generateRE1Rre05(context, status);
                } else if (noRE26) {
                    result = generateRE1Rre06(context, status);
                } else if (noRE37) {
                    result = generateRE1Rre07(context, status);
                } else if (status.daysSinceLastActivity > 3) {
                    final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE1Rre99);

                    if (no99) {
                        result = generateRE1Rre99(context, status);
                    } else {
                        Log.info("Student ", context.student.stuId, " stuck on Unit 1 Review exam");
                    }
                }
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when Review Exam 1 has been attempted 4 or more times but not yet passed.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFailedMany(final MessagingContext context,
                                                    final MessagingCourseStatus status) {

        MessageToSend result = null;

        final boolean noRE040 =
                MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE1Rre00, EMsg.RE1Rre04, EMsg.RE1Xre00);
        final boolean noRE1512 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE1Rre01, EMsg.RE1Rre05,
                EMsg.RE1Xre01, EMsg.RE1Xre02);
        final boolean noRE37 =
                MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE1Rre03, EMsg.RE1Rre07, EMsg.RE1Xre03);

        if (noRE040) {
            result = generateRE1Xre00(context, status);
        } else if (noRE1512) {
            final Period sinceLastTry = Period.between(status.lastR1Try, context.today);
            final int daysSinceLastTry = sinceLastTry.getDays();

            if (daysSinceLastTry > 2) {
                result = generateRE1Xre01(context, status);
            } else {
                result = generateRE1Xre02(context, status);
            }
        } else if (noRE37) {
            result = generateRE1Xre03(context, status);
        } else if (status.daysSinceLastActivity > 3) {
            final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE1Rre99);

            if (no99) {
                result = generateRE1Rre99(context, status);
            } else {
                Log.info("Student ", context.student.stuId, " stuck on Unit 1 Review exam");
            }
        }

        return result;
    }

    /**
     * Generates a report row for a RE1Rre00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateRE1Rre00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String subject = "Checking in";

        // Only message for RE 1 for the first two courses - after that, assume they're OK.

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        if (context.currentRegIndex == 0) {
            // First experience with a Review Exam
            body.addln("You've gotten through all of the ", crsName,
                    " unit 1 objectives - good job!  ",
                    "The Unit 1 review exam is next.  This is like a preview of the Unit Exam - it's ",
                    "ten questions, of the same kind of questions you've already seen.  Once you pass ",
                    "the Review Exam (with an 8 or higher), you can move on to the proctored Unit ",
                    "Exam.");
            body.addln();

            if (context.today.isAfter(status.milestones.r1)) {
                body.addln("Since the due date for the Review Exam has already passed, you won't ",
                        " get the 3 points for getting it done on time, but you're still in good ",
                        "shape.  You can miss a couple of these due dates and still earn an A grade ",
                        "in the course.  Just try to get caught back up before the next review exam, ",
                        "and you should be fine.");
                body.addln();
            } else {
                final LocalDate plus4 = context.today.plusDays(4L);

                if (plus4.isAfter(status.milestones.r1)) {
                    final String dayName = status.milestones.r1.getDayOfWeek()
                            .getDisplayName(TextStyle.FULL, Locale.US);

                    if (context.today.equals(status.milestones.r1)) {
                        body.add("The due date for the Review Exam is today.");
                    } else {
                        body.add("The due date for the Review Exam is coming up on ", dayName, ".");
                    }
                    body.addln(" It's worth 3 points to get that review exam passed by then (you ",
                            "have until the end of the day on ", dayName, ").");
                    body.addln();
                }
            }

        } else {
            body.addln("Good job getting through all the Unit 1 objectives -",
                    "The Unit 1 Review Exam is next...");
            body.addln();

            if (context.today.isAfter(status.milestones.r1)) {
                // First experience with a Review Exam
                body.addln(
                        "This Review Exam due date has already passed, but you're still in good ",
                        "shape.  You can miss a couple of these due dates and still earn an A grade ",
                        "in the course.  Just try to get caught back up before the next review exam, ",
                        "and you should be fine.");
                body.addln();
            } else {
                final LocalDate plus4 = context.today.plusDays(4L);

                if (plus4.isAfter(status.milestones.r1)) {
                    final String dayName = status.milestones.r1.getDayOfWeek()
                            .getDisplayName(TextStyle.FULL, Locale.US);

                    if (context.today.equals(status.milestones.r1)) {
                        body.add("The due date for the Review Exam is today.");
                    } else {
                        body.add("The due date for the Review Exam is coming up on ", dayName, ".");
                    }
                    body.addln(" It's worth 3 points to get that review exam passed by then (you ",
                            "have until the end of the day on ", dayName, ").");
                    body.addln();
                }
            }
        }

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.RE1, EMsg.RE1Rre00, subject, body.toString());
    }

    /**
     * Generates a report row for a RE1Rre01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateRE1Rre01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        final LocalDate plus4 = context.today.plusDays(4L);
        if (status.daysSinceLastActivity > 3 || plus4.isAfter(status.milestones.r4)
                && !context.today.isAfter(status.milestones.r4)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.add("Just a reminder to try to get the Unit 1 Review exam passed.");

            if (plus4.isAfter(status.milestones.r1)
                    && !context.today.isAfter(status.milestones.r1)) {
                final String dayName = context.today.equals(status.milestones.r1) ? "today"
                        : status.milestones.r1.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
                body.add(
                        "  The deadline to get the 3 points for completing the Review Exam on time ",
                        "is ", dayName, ".");
            }

            body.addln();
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.RE1, EMsg.RE1Rre01, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a RE1Rre02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateRE1Rre02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // We're going to skip this for all students

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.RE1, EMsg.RE1Rre02, null, null);
    }

    /**
     * Generates a report row for a RE1Rre03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateRE1Rre03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("I've noticed you haven't tried the Unit 1 Review Exam yet - are ",
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
                EMilestone.RE1, EMsg.RE1Rre03, "Checking in", body.toString());
    }

    /**
     * Generates a report row for a RE1Rre04 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateRE1Rre04(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String subject = "Checking in";

        // Only message for RE 1 for the first two courses - after that, assume they're OK.

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        if (context.currentRegIndex == 0) {
            // First experience with a Review Exam
            body.addln("You've gotten through all of the ", crsName,
                    " unit 1 objectives - good job!  And you've tried out the Unit 1 review exam.  ",
                    "This exam is a preview of the 10-question Unit Exam.  Once you pass the Review ",
                    "Exam (with an 8 or higher), you can move on to the proctored Unit Exam.");
            body.addln();

            if (context.today.isAfter(status.milestones.r1)) {
                body.addln("Since the due date for the Review Exam has already passed, you won't ",
                        " get the 3 points for getting it done on time, but you're still in good ",
                        "shape.  You can miss a couple of these due dates and still earn an A grade ",
                        "in the course.  Just try to get caught back up before the next review exam, ",
                        "and you should be fine.");
                body.addln();
            } else {
                final LocalDate plus4 = context.today.plusDays(4L);

                if (plus4.isAfter(status.milestones.r1)) {
                    final String dayName = status.milestones.r1.getDayOfWeek()
                            .getDisplayName(TextStyle.FULL, Locale.US);

                    if (context.today.equals(status.milestones.r1)) {
                        body.add("The due date for the Review Exam is today.");
                    } else {
                        body.add("The due date for the Review Exam is coming up on ", dayName, ".");
                    }
                    body.addln(" It's worth 3 points to get that review exam passed by then (you ",
                            "have until the end of the day on ", dayName, ").");
                    body.addln();
                }
            }

        } else {
            body.addln("Good job getting through all the Unit 1 objectives and having a go at ",
                    "the Unit 1 Review Exam.");
            body.addln();

            if (context.today.isAfter(status.milestones.r1)) {
                // First experience with a Review Exam
                body.addln(
                        "This Review Exam due date has already passed, but you're still in good ",
                        "shape.  You can miss a couple of these due dates and still earn an A grade ",
                        "in the course.  Just try to get caught back up before the next review exam, ",
                        "and you should be fine.");
                body.addln();
            } else {
                final LocalDate plus4 = context.today.plusDays(4L);

                if (plus4.isAfter(status.milestones.r1)) {
                    final String dayName = status.milestones.r1.getDayOfWeek()
                            .getDisplayName(TextStyle.FULL, Locale.US);

                    if (context.today.equals(status.milestones.r1)) {
                        body.add("The due date for the Review Exam is today.");
                    } else {
                        body.add("The due date for the Review Exam is coming up on ", dayName, ".");
                    }
                    body.addln(" It's worth 3 points to get that review exam passed by then (you ",
                            "have until the end of the day on ", dayName, ").");
                    body.addln();
                }
            }
        }

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.RE1, EMsg.RE1Rre04, subject, body.toString());
    }

    /**
     * Generates a report row for a RE1Rre05 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateRE1Rre05(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        final LocalDate plus4 = context.today.plusDays(4L);
        if (status.daysSinceLastActivity > 3 || plus4.isAfter(status.milestones.r4)
                && !context.today.isAfter(status.milestones.r4)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.add("Just a reminder to try to get the Unit 1 Review exam passed.");

            if (plus4.isAfter(status.milestones.r1)
                    && !context.today.isAfter(status.milestones.r1)) {
                final String dayName = context.today.equals(status.milestones.r1) ? "today"
                        : status.milestones.r1.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
                body.add(
                        "  The deadline to get the 3 points for completing the Review Exam on time ",
                        "is ", dayName, ".");
            }

            body.addln();
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.RE1, EMsg.RE1Rre05, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a RE1Rre06 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateRE1Rre06(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        // We're going to skip this for all students

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.RE1, EMsg.RE1Rre06, null, null);
    }

    /**
     * Generates a report row for a RE1Rre07 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateRE1Rre07(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("I've noticed you haven't gotten past the Unit 1 Review Exam yet - are ",
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
                EMilestone.RE1, EMsg.RE1Rre07, "Checking in", body.toString());
    }

    /**
     * Generates a report row for a RE1Xre00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateRE1Xre00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String subject = "Checking in";

        // Only message for RE 1 for the first two courses - after that, assume they're OK.

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        if (context.currentRegIndex == 0) {
            // First experience with a Review Exam
            body.addln("You've gotten through all of the ", crsName,
                    " unit 1 objectives - good job!  ",
                    "And you've tried out the Unit 1 review exam.  This exam is a preview of ",
                    "the 10-question Unit Exam.  Once you pass the Review Exam (with an 8 or higher), ",
                    "you can move on to the proctored Unit Exam.");
            body.addln();

            if (context.today.isAfter(status.milestones.r1)) {
                body.addln("Since the due date for the Review Exam has already passed, you won't ",
                        " get the 3 points for getting it done on time, but you're still in good ",
                        "shape.  You can miss a couple of these due dates and still earn an A grade ",
                        "in the course.  Just try to get caught back up before the next review exam, ",
                        "and you should be fine.");
                body.addln();
            } else {
                final LocalDate plus4 = context.today.plusDays(4L);

                if (plus4.isAfter(status.milestones.r1)) {
                    final String dayName = status.milestones.r1.getDayOfWeek()
                            .getDisplayName(TextStyle.FULL, Locale.US);

                    if (context.today.equals(status.milestones.r1)) {
                        body.add("The due date for the Review Exam is today.");
                    } else {
                        body.add("The due date for the Review Exam is coming up on ", dayName, ".");
                    }
                    body.addln(" It's worth 3 points to get that review exam passed by then (you ",
                            "have until the end of the day on ", dayName, ").");
                    body.addln();
                }
            }
        } else {
            body.addln("Good job getting through all the Unit 1 objectives and having a go at ",
                    "the Unit 1 Review Exam.");
            body.addln();

            if (context.today.isAfter(status.milestones.r1)) {
                // First experience with a Review Exam
                body.addln(
                        "This Review Exam due date has already passed, but you're still in good ",
                        "shape.  You can miss a couple of these due dates and still earn an A grade ",
                        "in the course.  Just try to get caught back up before the next review exam, ",
                        "and you should be fine.");
                body.addln();
            } else {
                final LocalDate plus4 = context.today.plusDays(4L);

                if (plus4.isAfter(status.milestones.r1)) {
                    final String dayName = status.milestones.r1.getDayOfWeek()
                            .getDisplayName(TextStyle.FULL, Locale.US);

                    if (context.today.equals(status.milestones.r1)) {
                        body.add("The due date for the Review Exam is today.");
                    } else {
                        body.add("The due date for the Review Exam is coming up on ", dayName, ".");
                    }
                    body.addln(" It's worth 3 points to get that review exam passed by then (you ",
                            "have until the end of the day on ", dayName, ").");
                    body.addln();
                }
            }
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
                EMilestone.RE1, EMsg.RE1Xre00, subject, body.toString());
    }

    /**
     * Generates a report row for a RE1Xre01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateRE1Xre01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        final LocalDate plus4 = context.today.plusDays(4L);
        if (status.daysSinceLastActivity > 3 || plus4.isAfter(status.milestones.r4)
                && !context.today.isAfter(status.milestones.r4)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.add("Just a reminder to try to get the Unit 1 Review exam passed.");

            if (plus4.isAfter(status.milestones.r1)
                    && !context.today.isAfter(status.milestones.r1)) {
                final String dayName = context.today.equals(status.milestones.r1) ? "today"
                        : status.milestones.r1.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
                body.add(
                        "  The deadline to get the 3 points for completing the Review Exam on time ",
                        "is ", dayName, ".");
            }
            body.addln();
            body.addln();

            body.addln(
                    "If you're struggling to get through this exam, let me know.  I'll try to help ",
                    "if I can.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.RE1, EMsg.RE1Xre01, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a RE1Xre02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateRE1Xre02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        final LocalDate plus4 = context.today.plusDays(4L);
        if (status.daysSinceLastActivity > 3 || plus4.isAfter(status.milestones.r4)
                && !context.today.isAfter(status.milestones.r4)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.add("Just a reminder to try to get the Unit 1 Review exam passed.");

            if (plus4.isAfter(status.milestones.r1)
                    && !context.today.isAfter(status.milestones.r1)) {
                final String dayName = context.today.equals(status.milestones.r1) ? "today"
                        : status.milestones.r1.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
                body.add(
                        "  The deadline to get the 3 points for completing the Review Exam on time ",
                        "is ", dayName, ".");
            }
            body.addln();
            body.addln();

            body.addln(
                    "If you're struggling to get through this exam, let me know.  I'll try to help ",
                    "if I can.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.RE1, EMsg.RE1Xre02, "Checking in", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a RE1Xre03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateRE1Xre03(final MessagingContext context,
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
                EMilestone.RE1, EMsg.RE1Xre03, "Checking in", body.toString());
    }

    /**
     * Generates a report row for a RE1Rre99 message. This applies to students who have been stuck on the Unit 1 Review
     * Exam through all 4 messages.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateRE1Rre99(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("It looks like you're stuck on the ", crsName,
                " Unit 1 Review Exam.  I'd like to help - ");

        if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
            body.addln("can you come into the Precalculus Center to talk about things?");
        } else {
            body.addln("do you want to set up a Teams meeting to talk about things?");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.RE1, EMsg.RE1Rre99, "Stuck on Unit 1 Review Exam?", body.toString());
    }
}

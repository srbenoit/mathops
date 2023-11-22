package dev.mathops.app.ops.snapin.messaging.factory;

import dev.mathops.core.TemporalUtils;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.app.ops.snapin.messaging.EMilestone;
import dev.mathops.app.ops.snapin.messaging.EMsg;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;
import dev.mathops.app.ops.snapin.messaging.MsgUtils;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;

import java.time.LocalDate;
import java.time.Period;

/**
 * A factory class that can generate messages to students who are "late" (urgency >= 1) and who have not yet passed the
 * User's exam.
 */
public enum LateMessageUsersExamFactory {
    ;

    /**
     * Generates an appropriate message when the course has been started but the user's exam has not been passed.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    public static MessageToSend generate(final MessagingContext context,
                                         final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (status.daysSinceLastMessage > 3) {
            if (status.failedTriesOnUsers == 0) {
                // User's exam not yet tried
                if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.USRRus00)) {
                    result = generateUSRRus00(context, status);
                } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.USRRus01)) {
                    result = generateUSRRus01(context, status);
                } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.USRRus02)) {
                    result = generateUSRRus02(context, status);
                } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.USRRus03)) {
                    result = generateUSRRus03(context, status);
                } else {
                    Log.warning("Student ", context.student.stuId, " stuck not having tried User's Exam");
                }
            } else {
                final Period sinceLastTry = Period.between(status.lastUsersTry, context.today);
                final int daysSinceLastTry = sinceLastTry.getDays();

                if (daysSinceLastTry >= 2) {
                    if (status.failedTriesOnUsers < 4) {
                        // Tried a few times
                        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.USRRus00, EMsg.USRRus04)) {
                            result = generateUSRRus04(context, status);
                        } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.USRRus01, EMsg.USRRus05)) {
                            result = generateUSRRus05(context, status);
                        } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.USRRus02, EMsg.USRRus06)) {
                            result = generateUSRRus06(context, status);
                        } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.USRRus03, EMsg.USRRus07)) {
                            result = generateUSRRus07(context, status);
                        } else {
                            Log.warning("Student ", context.student.stuId, " stuck not having passed User's Exam");
                        }
                    } else {
                        // Tried many times
                        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.USRRus00, EMsg.USRRus04,
                                EMsg.USRXus00)) {
                            result = generateUSRXus00(context, status);
                        } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.USRRus01, EMsg.USRRus05,
                                EMsg.USRXus01)) {
                            result = generateUSRXus01(context, status);
                        } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.USRRus02, EMsg.USRRus06,
                                EMsg.USRXus02)) {
                            result = generateUSRXus02(context, status);
                        } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.USRRus03, EMsg.USRRus07,
                                EMsg.USRXus03)) {
                            result = generateUSRXus03(context, status);
                        } else {
                            Log.warning("Student ", context.student.stuId, " stuck not having passed User's Exam");
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Generates a report row for a USRRus00 message. This applies to students who have started their first course but
     * not attempted the User's exam and have not yet received a USRRus00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUSRRus00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(0).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("It looks like you've found the course site and started ", crsName,
                ".  The next step is to review the Student Guide and take the User's exam, which ",
                "covers course policies.  It's basically a 'syllabus quiz'.  Once you've passed that, ",
                "you'll be able to get into the course content.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.USERS,
                EMsg.USRRus00, "User's Exam", body.toString());
    }

    /**
     * Generates a report row for a USRRus01 message. This applies to students who have started their first course but
     * not attempted the User's exam and have not yet received a USRRus01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUSRRus01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(0).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Just a reminder to try to get the User's Exam out of the way so you can get ",
                "started on the ", crsName, " materials...");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.USERS,
                EMsg.USRRus01, "User's Exam", body.toString());
    }

    /**
     * Generates a report row for a USRRus02 message. This applies to students who have started their first course but
     * not attempted the User's exam and have not yet received a USRRus02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUSRRus02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(0).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("Just another reminder to knock out the User's Exam so you can start working on ",
                crsName, ".  ");

        if (context.today.isAfter(status.milestones.r2)) {
            body.addln("The course due dates are happening, and the first two Review Exam due ",
                    "dates have already come and gone - missing those dates costs 3 points each ",
                    "toward your course grade.");
        } else if (context.today.isAfter(status.milestones.r1)) {
            body.addln("The course due dates are happening, and the first Review Exam due date ",
                    "has already come and gone - missing those dates costs 3 points toward your ",
                    "course grade.");
        } else if (context.today.equals(status.milestones.r1)) {
            body.addln("The course due dates are happening, and the first Review Exam due date ",
                    "is today - missing those dates costs 3 points toward your course grade.");
        } else {
            final LocalDate cutoff = status.milestones.r1.minusDays(3L);

            if (context.today.isBefore(cutoff)) {
                body.addln(
                        "The course due dates are starting to come along, and I want to make sure ",
                        "you don't end up behind schedule and having to rush to get caught up.");
            } else {
                final String dateStr = TemporalUtils.FMT_WMD.format(status.milestones.r1);
                body.addln(
                        "The course due dates are happening, and the first Review Exam due date is ",
                        "already coming up on ", dateStr, ".");
            }
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.USERS,
                EMsg.USRRus02, "User's Exam", body.toString());
    }

    /**
     * Generates a report row for a USRRus03 message. This applies to students who have started their first course but
     * not attempted the User's exam and have not yet received a USRRus03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUSRRus03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(0).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("I'm just wondering if you're planning to drop ", crsName,
                " this semester - if so, I'll stop pestering you about the User's Exam...");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.USERS,
                EMsg.USRRus03, "User's Exam", body.toString());
    }

    /**
     * Generates a report row for a USRRus04 message. This applies to students who have started their first course,
     * tried the User's exam less than 4 times, and have not yet received a USRRus00 or USRRus04 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUSRRus04(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(0).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("It looks like you've found the course site and started ", crsName,
                ", and you've already tried out the User's exam.  Once you've passed that, you'll be ",
                "able to get into the course content.");
        body.addln();

        body.addln("If you need to, you can have the Student Guide open when you take the User's ",
                "Exam - we just want to be sure you've seen the most important things in there.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.USERS,
                EMsg.USRRus04, "User's Exam", body.toString());
    }

    /**
     * Generates a report row for a USRRus05 message. This applies to students who have started their first course,
     * tried the User's exam less than 4 times, and have not yet received a USRRus01 or USRRus05 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUSRRus05(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(0).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Just a reminder to try to get the User's Exam out of the way so you can get ",
                "started on the ", crsName, " materials...");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.USERS,
                EMsg.USRRus05, "User's Exam", body.toString());
    }

    /**
     * Generates a report row for a USRRus06 message. This applies to students who have started their first course,
     * tried the User's exam less than 4 times, and have not yet received a USRRus02 or USRRus06 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUSRRus06(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(0).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("Just another reminder to knock out the User's Exam so you can start working on ",
                crsName, ".  ");
        if (context.today.isAfter(status.milestones.r2)) {
            body.addln("The course due dates are happening, and the first two Review Exam due ",
                    "dates have already come and gone - missing those dates costs 3 points each ",
                    "toward your course grade.");
        } else if (context.today.isAfter(status.milestones.r1)) {
            body.addln("The course due dates are happening, and the first Review Exam due date ",
                    "has already come and gone - missing those dates costs 3 points toward your ",
                    "course grade.");
        } else if (context.today.equals(status.milestones.r1)) {
            body.addln("The course due dates are happening, and the first Review Exam due date ",
                    "is today - missing those dates costs 3 points toward your course grade.");
        } else {
            final LocalDate cutoff = status.milestones.r1.minusDays(3L);

            if (context.today.isBefore(cutoff)) {
                body.addln(
                        "The course due dates are starting to come along, and I want to make sure ",
                        "you don't end up behind schedule and having to rush to get caught up.");
            } else {
                final String dateStr = TemporalUtils.FMT_WMD.format(status.milestones.r1);
                body.addln(
                        "The course due dates are happening, and the first Review Exam due date is ",
                        "already coming up on ", dateStr, ".");
            }
        }
        body.addln();

        body.addln("Let me know if you have any questions about the course policies.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.USERS,
                EMsg.USRRus06, "User's Exam", body.toString());
    }

    /**
     * Generates a report row for a USRRus07 message. This applies to students who have started their first course,
     * tried the User's exam less than 4 times, and have not yet received a USRRus03 or USRRus07 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUSRRus07(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(0).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("I'm just wondering if you're planning to drop ", crsName,
                " this semester - if so, I'll stop pestering you about the User's Exam...");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.USERS,
                EMsg.USRRus07, "User's Exam", body.toString());
    }

    /**
     * Generates a report row for a USRXus00 message. This applies to students who have started their first course,
     * tried the User's exam 4 or more times, and have not yet received a USRRus00, USRRus04 or USRXus00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUSRXus00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(0).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("It looks like you've found the course site and started ", crsName,
                ", and you've already tried out the User's exam.  Once you've passed that, you'll be ",
                "able to get into the course content.");
        body.addln();

        body.addln("If you need to, you can have the Student Guide open when you take the User's ",
                "Exam - we just want to be sure you've seen the most important things in there.");
        body.addln();

        body.addln("Let me know if you have any questions about the course policies.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.USERS,
                EMsg.USRXus00, "User's Exam", body.toString());
    }

    /**
     * Generates a report row for a USRXus01 message. This applies to students who have started their first course,
     * tried the User's exam 4 or more times, not tried in the last 2 days, and have not yet received a USRRus01,
     * USRRus05, USRXus01, or USRXus02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUSRXus01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(0).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Just a reminder to try to get the User's Exam out of the way so you can get ",
                "started on the ", crsName, " materials...");
        body.addln();

        body.addln("If you're having trouble finding any of that information in the Student Guide,",
                "let me know, I'll try to help out.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.USERS,
                EMsg.USRXus01, "User's Exam", body.toString());
    }

    /**
     * Generates a report row for a USRXus02 message. This applies to students who have started their first course,
     * tried the User's exam 4 or more times, tried in the last 2 days, and have not yet received a USRRus01, USRRus05,
     * USRXus01, or USRXus02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUSRXus02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(0).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("Just another reminder to knock out the User's Exam so you can start working on ",
                crsName, ".  ");

        if (context.today.isAfter(status.milestones.r2)) {
            body.addln("The course due dates are happening, and the first two Review Exam due ",
                    "dates have already come and gone - missing those dates costs 3 points each ",
                    "toward your course grade.");
        } else if (context.today.isAfter(status.milestones.r1)) {
            body.addln("The course due dates are happening, and the first Review Exam due date ",
                    "has already come and gone - missing those dates costs 3 points toward your ",
                    "course grade.");
        } else if (context.today.equals(status.milestones.r1)) {
            body.addln("The course due dates are happening, and the first Review Exam due date ",
                    "is today - missing those dates costs 3 points toward your course grade.");
        } else {
            final LocalDate cutoff = status.milestones.r1.minusDays(3L);

            if (context.today.isBefore(cutoff)) {
                body.addln(
                        "The course due dates are starting to come along, and I want to make sure ",
                        "you don't end up behind schedule and having to rush to get caught up.");
            } else {
                final String dateStr = TemporalUtils.FMT_WMD.format(status.milestones.r1);
                body.addln(
                        "The course due dates are happening, and the first Review Exam due date is ",
                        "already coming up on ", dateStr, ".");
            }
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.USERS,
                EMsg.USRXus02, "User's Exam", body.toString());
    }

    /**
     * Generates a report row for a USRXus03 message. This applies to students who have started their first course,
     * tried the User's exam 4 or more times, and have not yet received a USRRus03, USRRus07, or USRXus03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateUSRXus03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(0).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("I'm just wondering if you're planning to drop ", crsName,
                " this semester - if so, I'll stop pestering you about the User's Exam...");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.USERS,
                EMsg.USRXus03, "User's Exam", body.toString());
    }
}

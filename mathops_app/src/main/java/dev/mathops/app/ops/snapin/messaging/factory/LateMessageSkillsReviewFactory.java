package dev.mathops.app.ops.snapin.messaging.factory;

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
 * A factory class that can generate messages to students who are "late" (urgency >= 1) who have passed the User's exam
 * but not yet passed the Skills Review exam in their current course.
 */
public enum LateMessageSkillsReviewFactory {
    ;

    /**
     * Generates an appropriate message for a student who has not yet passed the Skills Review exam.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    public static MessageToSend generate(final MessagingContext context, final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.failedTriesOnSR >= 4) {
            result = generateSkillsReviewFailedMany(context, status);
        } else if (status.failedTriesOnSR > 0) {
            result = generateSkillsReviewFailedFew(context, status);
        } else {
            result = generateSkillsReviewNotTried(context, status);
        }

        return result;
    }

    /**
     * Generates an appropriate message when the user's exam has been passed but the Skills Review exam has not been
     * attempted.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSkillsReviewNotTried(final MessagingContext context,
                                                              final MessagingCourseStatus status) {

        MessageToSend result = null;

        final boolean noSR0 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.SKLRsr00);
        final boolean noSR1 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.SKLRsr01);
        final boolean noSR2 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.SKLRsr02);
        final boolean noSR3 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.SKLRsr03);

        if (noSR0) {
            result = generateSKLRsr00(context, status);
        } else if (noSR1) {
            result = generateSKLRsr01(context, status);
        } else if (noSR2) {
            result = generateSKLRsr02(context, status);
        } else if (noSR3) {
            result = generateSKLRsr03(context, status);
        } else if (status.daysSinceLastActivity > 3) {
            final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.SKLRsr99);

            if (no99) {
                result = generateSKLRsr99(context, status);
            } else {
                Log.info("Student ", context.student.stuId, " stuck on Skills Review exam");
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when the Skills Review exam has been attempted less than 4 times but not yet
     * passed.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSkillsReviewFailedFew(final MessagingContext context,
                                                               final MessagingCourseStatus status) {

        MessageToSend result = null;

        final Period sinceLastTry = Period.between(status.lastSRTry, context.today);
        final int daysSinceLastTry = sinceLastTry.getDays();

        if (daysSinceLastTry >= 2) {
            final boolean noSR04 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.SKLRsr00, EMsg.SKLRsr04);
            final boolean noSR15 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.SKLRsr01, EMsg.SKLRsr05);
            final boolean noSR26 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.SKLRsr02, EMsg.SKLRsr06);
            final boolean noSR37 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.SKLRsr03, EMsg.SKLRsr07);

            if (noSR04) {
                result = generateSKLRsr04(context, status);
            } else if (noSR15) {
                result = generateSKLRsr05(context, status);
            } else if (noSR26) {
                result = generateSKLRsr06(context, status);
            } else if (noSR37) {
                result = generateSKLRsr07(context, status);
            } else if (status.daysSinceLastActivity > 3) {
                final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.SKLRsr99);

                if (no99) {
                    result = generateSKLRsr99(context, status);
                } else {
                    Log.info("Student ", context.student.stuId, " stuck on Skills Review exam");
                }
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when the Skills Review exam has been attempted 4 or more times but not yet
     * passed.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSkillsReviewFailedMany(final MessagingContext context,
                                                                final MessagingCourseStatus status) {

        MessageToSend result = null;

        final boolean noSR040 =
                MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.SKLRsr00, EMsg.SKLRsr04, EMsg.SKLXsr00);
        final boolean noSR1512 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.SKLRsr01, EMsg.SKLRsr05,
                EMsg.SKLXsr01, EMsg.SKLXsr02);
        final boolean noSR37 =
                MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.SKLRsr03, EMsg.SKLRsr07, EMsg.SKLXsr03);

        if (noSR040) {
            result = generateSKLXsr00(context, status);
        } else if (noSR1512) {
            final Period sinceLastTry = Period.between(status.lastSRTry, context.today);
            final int daysSinceLastTry = sinceLastTry.getDays();

            if (daysSinceLastTry > 2) {
                result = generateSKLXsr01(context, status);
            } else {
                result = generateSKLXsr02(context, status);
            }
        } else if (noSR37) {
            result = generateSKLXsr03(context, status);
        } else if (status.daysSinceLastActivity > 3) {
            final boolean no99 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.SKLRsr99);

            if (no99) {
                result = generateSKLRsr99(context, status);
            } else {
                Log.info("Student ", context.student.stuId, " stuck on Skills Review exam");
            }
        }

        return result;
    }

    /**
     * Generates a report row for a SKLRsr00 message. This applies to students who have passed the User's exam but not
     * attempted the Skills Review exam and have not yet received a SKLRsr00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSKLRsr00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        if (context.currentRegIndex == 0) {
            // First experience with a Skills Review
            body.addln("You're ready to dive into the ", crsName, " content - the course starts ",
                    "out with a 'Skills Review' exam that covers some background material.  It ",
                    "shouldn't be too bad, so give it a try.");
            body.addln();

            body.addln("If you pass it on the first atempt, great!  You can move into Unit 1.  If ",
                    "not, then some review videos and practice problems will open up for you to brush ",
                    "up with.  You have unlimited tries on the Skills Review exam.");
        } else if (context.currentRegIndex == 1) {
            body.addln("You're all set to start in with the ", crsName, " course.  This course ",
                    "also starts out with a 'Skills Review' exam.");
            body.addln();

            body.addln("Like before, you should take it once, and if you pass, you can move ",
                    "forward.  If not, the review videos and practice problems will become available ",
                    "to help you get past it.");
        } else {
            body.addln("It looks like you're all set to move on to the Skills Review in ", crsName,
                    ".");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.SR, EMsg.SKLRsr00, "Skills Review", body.toString());
    }

    /**
     * Generates a report row for a SKLRsr01 message. This applies to students who have passed the User's exam but not
     * attempted the Skills Review exam and have not yet received a SKLRsr01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSKLRsr01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Just a reminder to try to get past the Skills Review Exam so you can move on ",
                "to the Unit 1 content.  Feel free to use our Learning Assistants to help if ",
                "you have questions on that material.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.SR, EMsg.SKLRsr01, "Skills Review", body.toString());
    }

    /**
     * Generates a report row for a SKLRsr02 message. This applies to students who have passed the User's exam but not
     * attempted the Skills Review exam and have not yet received a SKLRsr02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSKLRsr02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        if (context.currentRegIndex > 0) {
            body.addln("Sorry to keep bothering you - I'm sure the semester is busy. But ",
                    "when you get time, please try to get started in ", crsName, ".");
        } else {
            body.addln("Sorry to keep bothering you - I'm sure the semester is busy. But when ",
                    "you have time, please try the ", crsName, " Skills Review exam once, just to ",
                    "get a sense of how challenging it's going to be.");
        }
        body.addln();

        if (context.today.isAfter(status.milestones.r1)) {
            body.addln("Course due dates are ticking along - the first Review Exam due date has ",
                    "already come and gone, and each missed Review Exam due date costs a few points.");
        } else if (context.today.equals(status.milestones.r1)) {
            body.addln("Course due dates are ticking along - the first Review Exam due date is ",
                    "today, and each missed Review Exam due date costs a few points.");
        } else {
            final LocalDate cutoff = status.milestones.r1.minusDays(3L);

            if (context.today.isBefore(cutoff)) {
                body.addln("Course due dates are starting to tick along, and I want to make sure ",
                        "you don't end up behind schedule and having to rush to get caught up.");
            } else {
                body.addln(
                        "Course due dates are ticking along - the first Review Exam due date is ",
                        "already coming up.");
            }
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.SR, EMsg.SKLRsr02, "Skills Review", body.toString());
    }

    /**
     * Generates a report row for a SKLRsr03 message. This applies to students who have passed the User's exam but not
     * attempted the Skills Review exam and have not yet received a SKLRsr03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSKLRsr03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("It's getting kind of late to get started on the ", crsName, " content.  Is ",
                "there anything I can do to help you get started?");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.SR, EMsg.SKLRsr03, "Skills Review", body.toString());
    }

    /**
     * Generates a report row for a SKLRsr04 message. This applies to students who have passed the User's exam, tried
     * the Skills Review exam less than 4 times, and have not yet received a SKLRsr00 or SKLRsr04 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSKLRsr04(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        if (context.currentRegIndex == 0) {
            // First experience with a Skills Review
            body.addln("You're ready to dive into the ", crsName, " content.  It looks like ",
                    "you've already tried out the 'Skills Review' exam - very good!");
            body.addln();

            body.addln("After you've tried that exam once, some review materials will become ",
                    "available in case you need to brush up on some background skills.  You have ",
                    "unlimited tries on the Skills Review exam.");
        } else if (context.currentRegIndex == 1) {
            body.addln("You're all set to start in with the ", crsName, " course.  This course ",
                    "also starts out with a 'Skills Review' exam.");
            body.addln();

            body.addln("Like before, once you've tried that exam once, review videos and practice ",
                    "problems will become available to help you get past it.");
        } else {
            body.addln("It looks like you've already moved on to the Skills Review in ", crsName,
                    " - now that you've taken it, there should be review materials to use if you ",
                    "need them.");
            body.addln();
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.SR, EMsg.SKLRsr04, "Skills Review", body.toString());
    }

    /**
     * Generates a report row for a SKLRsr05 message. This applies to students who have passed the User's exam, tried
     * the Skills Review exam less than 4 times, and have not yet received a SKLRsr01 or SKLRsr05 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSKLRsr05(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Just a reminder to keep trying to get past the Skills Review Exam so you can ",
                "move on to the Unit 1 content.  Feel free to use our Learning Assistants to help if ",
                "you have questions on that material.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.SR, EMsg.SKLRsr05, "Skills Review", body.toString());
    }

    /**
     * Generates a report row for a SKLRsr06 message. This applies to students who have passed the User's exam, tried
     * the Skills Review exam less than 4 times, and have not yet received a SKLRsr02 or SKLRsr06 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSKLRsr06(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        body.addln("Sorry to keep bothering you - I'm sure the semester is busy. But when you ",
                "have time, please see if you can knock out the ", crsName, " Skills Review exam.  ",
                "If you have questions or need some help, please let me know.");
        body.addln();

        if (context.today.isAfter(status.milestones.r1)) {
            body.addln("Course due dates are ticking along - the first Review Exam due date has ",
                    "already come and gone, and each missed Review Exam due date costs a few points.");
        } else if (context.today.equals(status.milestones.r1)) {
            body.addln("Course due dates are ticking along - the first Review Exam due date is ",
                    "today, and each missed Review Exam due date costs a few points.");
        } else {
            final LocalDate cutoff = status.milestones.r1.minusDays(3L);

            if (context.today.isBefore(cutoff)) {
                body.addln("Course due dates are starting to tick along, and I want to make sure ",
                        "you don't end up behind schedule and having to rush to get caught up.");
            } else {
                body.addln(
                        "Course due dates are ticking along - the first Review Exam due date is ",
                        "already coming up.");
            }
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.SR, EMsg.SKLRsr06, "Skills Review", body.toString());
    }

    /**
     * Generates a report row for a SKLRsr07 message. This applies to students who have passed the User's exam, tried
     * the Skills Review exam less than 4 times, and have not yet received a SKLRsr03 or SKLRsr07 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSKLRsr07(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("It's getting kind of late to get started on the ", crsName, " content.  Is ",
                "there anything I can do to help you get through the Skills Review?");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.SR, EMsg.SKLRsr07, "Skills Review", body.toString());
    }

    /**
     * Generates a report row for a SKLXsr00 message. This applies to students who have passed the User's exam, tried
     * the Skills Review exam 4 or more times, and have not yet received a SKLRsr00, SKLRsr04 or SKLXsr00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSKLXsr00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        if (context.currentRegIndex == 0) {
            // First experience with a Skills Review
            body.addln("You're ready to dive into the ", crsName, " content.  It looks like ",
                    "you've already tried out the 'Skills Review' exam, but it might be giving you ",
                    "some trouble.");
        } else if (context.currentRegIndex == 1) {
            body.addln("You're all set to start in with the ", crsName, " course.  This course ",
                    "also starts out with a 'Skills Review' exam.  It looks like that exam is causing ",
                    "some headaches.");
        } else {
            body.addln("It looks like you've already moved on to the Skills Review in ", crsName,
                    ", but it's causing some trouble.");
        }
        body.addln();

        body.addln("Not to worry - we can help.  In addition to the review materials on the ",
                "course site, you can work with our Learning Assistants - they can answer ",
                "questions and might be able to help with something that's unclear.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.SR, EMsg.SKLXsr00, "Skills Review", body.toString());
    }

    /**
     * Generates a report row for a SKLXsr01 message. This applies to students who have passed the User's exam, tried
     * the Skills Review exam 4 or more times, not tried in the last 2 days, and have not yet received a SKLRsr01,
     * SKLRsr05, SKLXsr01, or SKLXsr02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSKLXsr01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        body.addln("Sorry to keep bothering you - I'm sure the semester is busy. But I don't ",
                "want to see you get stuck for too long on the ", crsName, " Skills Review.");
        body.addln();

        if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
            body.add("Are you able to come in to sit down with our Learning assistants?  ");
        } else {
            body.add("Are you able to connect with our online learning assistants?  ");
        }
        body.addln("They can probably help you get past it.  If not, let mw know and I'll try to ",
                "find a time to help or can try to figure out how to connect you with resources to ",
                "help out.");
        body.addln();

        body.addln("In any case, don't give up.  We'll try to help you get past this...");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.SR, EMsg.SKLXsr01, "Skills Review", body.toString());
    }

    /**
     * Generates a report row for a SKLXsr02 message. This applies to students who have passed the User's exam, tried
     * the Skills Review exam 4 or more times, tried in the last 2 days, and have not yet received a SKLRsr01, SKLRsr05,
     * SKLXsr01, or SKLXsr02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSKLXsr02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        body.addln("Sorry to keep bothering you - I'm sure the semester is busy. But I don't ",
                "want to see you get stuck for too long on the ", crsName, "Skills Review.");
        body.addln();

        if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
            body.add("Are you able to come in to sit down with our Learning assistants?  ");
        } else {
            body.add("Are you able to connect with our online learning assistants?  ");
        }
        body.addln("They can probably help you get past it.  If not, let mw know and I'll try to ",
                "find a time to help or can try to figure out how to connect you with resources to ",
                "help out.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.SR, EMsg.SKLXsr02, "Skills Review", body.toString());
    }

    /**
     * Generates a report row for a SKLXsr03 message. This applies to students who have passed the User's exam, tried
     * the Skills Review exam 4 or more times, and have not yet received a SKLRsr03, SKLRsr07, or SKLXsr03 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSKLXsr03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Just checking in again - please let me know if there's anything I can do, or ",
                "if you want to talk about options if you don't think you'll be able to complete ",
                crsName, " this semester.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.SR, EMsg.SKLXsr03, "Skills Review", body.toString());
    }

    /**
     * Generates a report row for a SKLRsr99 message. This applies to students who have been stuck on the Skills Review
     * through all 4 messages.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateSKLRsr99(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.get(context.currentRegIndex).course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("It looks like you're stuck on the ", crsName,
                " Skills Review.  I'd like to help - ");

        if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
            body.addln("can you come into the Precalculus Center to talk about the course?");
        } else {
            body.addln("do you want to set up a Teams meeting to talk about the course?");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.SR, EMsg.SKLRsr99, "Stuck on Skills Review?", body.toString());
    }
}

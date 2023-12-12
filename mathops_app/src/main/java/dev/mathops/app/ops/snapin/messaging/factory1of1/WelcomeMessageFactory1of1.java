package dev.mathops.app.ops.snapin.messaging.factory1of1;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.app.ops.snapin.messaging.EMilestone;
import dev.mathops.app.ops.snapin.messaging.EMsg;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;
import dev.mathops.app.ops.snapin.messaging.MsgUtils;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;

import java.time.LocalTime;

/**
 * A factory class that can generate a "Welcome" message for students in one course.
 */
enum WelcomeMessageFactory1of1 {
    ;

    /**
     * Generates an appropriate "Welcome" message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    static MessageToSend generate(final MessagingContext context, final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.metPrereq) {
            final boolean afterR1 = context.today.isAfter(status.milestones.r1);

            if (status.started) {
                if (status.passedUsers) {
                    if (status.passedSR) {
                        result = generateWELOK0(context, status);
                    } else if (status.failedTriesOnSR == 0) {
                        // Student has not attempted SR exam
                        if (afterR1) {
                            // SR not attempted, after RE1 due date
                            result = generateWELCSR01(context, status);
                        } else {
                            // SR not attempted, on or before RE1 due date
                            result = generateWELCSR00(context, status);
                        }
                    } else if (status.failedTriesOnSR < 4) {
                        // Student has attempted SR exam fewer than 4 times
                        if (afterR1) {
                            // SR attempted <4, after RE1 due date
                            result = generateWELCSR03(context, status);
                        } else {
                            // SR attempted <4, on or before RE1 due date
                            result = generateWELCSR02(context, status);
                        }
                    } else {
                        // Student has attempted SR exam 4 or more times
                        if (afterR1) {
                            // SR attempted >=4, after RE1 due date
                            result = generateWELCSR05(context, status);
                        } else {
                            // SR attempted >=4, on or before RE1 due date
                            result = generateWELCSR04(context, status);
                        }
                    }
                } else if (status.failedTriesOnUsers == 0) {
                    // Student has not attempted User's exam
                    if (afterR1) {
                        // User's not attempted, after RE1 due date
                        result = generateWELCus01(context, status);
                    } else {
                        // User's not attempted, on or before H1.1 due date
                        result = generateWELCus00(context, status);
                    }
                } else if (status.failedTriesOnUsers < 4) {
                    // Student has attempted User's exam fewer than 4 times
                    if (afterR1) {
                        // User's attempted <4, after RE1 due date
                        result = generateWELCus03(context, status);
                    } else {
                        // User's attempted <4, on or before H1.1 due date
                        result = generateWELCus02(context, status);
                    }
                } else {
                    // Student has attempted User's exam 4 or more times
                    if (afterR1) {
                        // User's attempted >=4, after RE1 due date
                        result = generateWELCus05(context, status);
                    } else {
                        // User's attempted >=4, on or before H1.1 due date
                        result = generateWELCus04(context, status);
                    }
                }
            } else if (afterR1) {
                // Course not yet started, after RE1 due date
                result = generateWELCst01(context, status);
            } else {
                // Course not yet started, on or before RE1 due date
                result = generateWELCst00(context, status);
            }
        } else {
            // Student has not met prerequisite
            result = generateWELCpr00(context, status);
        }

        return result;
    }

    /**
     * Generates a report row for a "**WELCPR0" message. This applies to students who have not yet satisfied the
     * prerequisites for their first course.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateWELCpr00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final RawStudent stu = context.student;
        final String firstReg = context.sortedRegs.get(0).course;

        final HtmlBuilder body = new HtmlBuilder(1000);
        emitOpening(stu, context, body);

        body.add("There is one immediate issue to resolve: it looks like you don't have the ",
                "prerequisites you need to start ", MsgUtils.courseName(firstReg), ".  ");

        if (RawRecordConstants.M126.equals(firstReg)) {
            if (context.prereqLogic.hasSatisfiedPrereqsFor(RawRecordConstants.M125)) {
                body.add("You have the prereqs for MATH 125 - you may need to add MATH 125 too so ",
                        "you can meet the prerequisite for MATH 126.  ");
            } else if (context.prereqLogic.hasSatisfiedPrereqsFor(RawRecordConstants.M118)) {
                body.add("You have the prereqs for MATH 118 - you may need to add MATH 118 and ",
                        "MATH 125 too so you can meet the prerequisite for MATH 126.  ");
            } else {
                body.add("Have you completed the Math Placement Tool, or do you have transfer ",
                        "credit coming in?");
            }
        } else if (RawRecordConstants.M125.equals(firstReg)) {
            if (context.prereqLogic.hasSatisfiedPrereqsFor(RawRecordConstants.M118)) {
                body.add("You have the prereqs for MATH 118 - you may need to add MATH 118 too so ",
                        "you can meet the prerequisite for MATH 125.");
            } else if (context.prereqLogic.hasSatisfiedPrereqsFor(RawRecordConstants.M117)) {
                body.add("You have the prereqs for MATH 117 - you may need to add MATH 117 and ",
                        "MATH 118 too so you can meet the prerequisite for MATH 125.");
            } else {
                body.add("Have you completed the Math Placement Tool, or do you have transfer ",
                        "credit coming in?");
            }
        } else if (RawRecordConstants.M124.equals(firstReg)) {
            if (context.prereqLogic.hasSatisfiedPrereqsFor(RawRecordConstants.M118)) {
                body.add("You have the prereqs for MATH 118 - you may need to add MATH 118 too so ",
                        "you can meet the prerequisite for MATH 124.");
            } else if (context.prereqLogic.hasSatisfiedPrereqsFor(RawRecordConstants.M117)) {
                body.add("You have the prereqs for MATH 117 - you may need to add MATH 117 and ",
                        "MATH 118 too so you can meet the prerequisite for MATH 124.");
            } else {
                body.add("Have you completed the Math Placement Tool, or do you have transfer ",
                        "credit coming in?");
            }
        } else if (RawRecordConstants.M118.equals(firstReg)
                && context.prereqLogic.hasSatisfiedPrereqsFor(RawRecordConstants.M117)) {
            body.add("You have the prereqs for MATH 117 - you may need to add MATH 117 too so ",
                    "you can meet the prerequisite for MATH 118.");
        } else {
            body.add("Have you completed the Math Placement Tool, or do you have transfer ",
                    "credit coming in?");
        }

        body.addln(
                "  Let me know how I can help get this resolved quickly so you can get started.");
        body.addln();

        body.addln("If you have questions about registration or prerequisites, please let me ",
                "know, or send an email to precalc_math@colostate.edu.");
        body.addln();

        emitClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.WELCOME,
                EMsg.WELCpr00, MsgUtils.buildWelcomeSubject(context), body.toString());
    }

    /**
     * Generates a report row for a "**WELCST0" message. This applies to students who have not yet started the first
     * course, when the current date is on or earlier than the first homework due date. This should be the vast majority
     * of students, if we are timely on sending welcome messages. We should make sure Canvas courses are published
     * before sending these.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateWELCst00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final RawStudent stu = context.student;

        final HtmlBuilder body = new HtmlBuilder(1000);
        emitOpening(stu, context, body);

        emitStartParagraph(body);
        emitStartHelp(body);

        emitClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.WELCOME,
                EMsg.WELCst00, MsgUtils.buildWelcomeSubject(context), body.toString());
    }

    /**
     * Generates a report row for a "**WELCST1" message. This applies to students who have not yet started the first
     * course, when the current date is after the first review exam due date.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateWELCst01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final RawStudent stu = context.student;

        final HtmlBuilder body = new HtmlBuilder(1000);
        emitOpening(stu, context, body);

        emitStartParagraph(body);

        body.addln("The semester is well underway, and we're already past the due date for the ",
                "first Unit Review Exam, so it's important that you get started as soon as you can to ",
                "avoid losing more points to late penalties.");
        body.addln();

        emitStartHelp(body);

        emitClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.WELCOME,
                EMsg.WELCst01, MsgUtils.buildWelcomeSubject(context), body.toString());
    }

    /**
     * Emits a paragraph within a "start" message.
     *
     * @param body the {@code HtmlBuilder} to which to append
     */
    private static void emitStartParagraph(final HtmlBuilder body) {

        body.addln("You should find your course in Canvas (https://colostate.instructure.com), ",
                "with some information and a link to the main course web site.  Once you get to the ",
                "course page, read over the Student Guide to see how the program works.  Once you get ",
                "started, remember to check the \"Exam Deadlines\" link to stay on track with due ",
                "dates.");
        body.addln();
    }

    /**
     * Emits a paragraph within a "start" message.
     *
     * @param body the {@code HtmlBuilder} to which to append
     */
    private static void emitStartHelp(final HtmlBuilder body) {

        body.addln("If you can't find the course web site, or if you have any questions about ",
                "what's in the Student Guide, you can send an email to precalc_math@colostate.edu.");
        body.addln();
    }

    /**
     * Generates a report row for a "**WELCUS0" message. This applies to students who have started the first course but
     * not yet attempted the User's exam, when the current date is on or earlier than the first review exam due date.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateWELCus00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final RawStudent stu = context.student;

        final HtmlBuilder body = new HtmlBuilder(1000);
        emitOpening(stu, context, body);

        body.add("It looks like you've found the course in Canvas, and gotten in to the course ",
                "web site.  Please read over the Student Guide to see how the program works, and take ",
                "the User's Exam (a syllabus quiz to make sure you know course policies).  Once you ",
                "get started with course content, remember to check the \"Exam Deadlines\" link to ",
                "stay on track with due dates.");
        body.addln();

        body.addln("If you can't find the course web site, or if you have any questions about ",
                "what's in the Student Guide, you can send an email to precalc_math@colostate.edu.");
        body.addln();

        emitClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.WELCOME,
                EMsg.WELCus00, MsgUtils.buildWelcomeSubject(context), body.toString());
    }

    /**
     * Generates a report row for a "**WELCUS1" message. This applies to students who have started the first course but
     * not yet attempted the User's exam, when the current date is after the first review exam due date.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateWELCus01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final RawStudent stu = context.student;

        final HtmlBuilder body = new HtmlBuilder(1000);
        emitOpening(stu, context, body);

        body.add("It looks like you've found the course in Canvas, and gotten in to the course ",
                "web site.  Please read over the Student Guide to see how the program works, and take ",
                "the User's Exam (a syllabus quiz to make sure you know course policies).  Once you ",
                "get started with course content, remember to check the \"Exam Deadlines\" link to ",
                "stay on track with due dates.");
        body.addln();

        body.addln("The semester is flying by, and we're already past the due date for the first ",
                "Unit Review Exam, so it's important that you get started as soon as you can to avoid ",
                "losing more points to late penalties.");
        body.addln();

        body.addln("If you can't find the course web site, or if you have any questions about ",
                "what's in the Student Guide, you can send an email to precalc_math@colostate.edu.");
        body.addln();

        emitClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.WELCOME,
                EMsg.WELCus01, MsgUtils.buildWelcomeSubject(context), body.toString());
    }

    /**
     * Generates a report row for a "**WELCUS2" message. This applies to students who have attempted the User's exam
     * three or fewer times without passing, when the current date is on or earlier than the first review exam due
     * date.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateWELCus02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final RawStudent stu = context.student;

        final HtmlBuilder body = new HtmlBuilder(1000);
        emitOpening(stu, context, body);

        body.add("It looks like you've found the course in Canvas, and gotten in to the course ",
                "web site, and tried out the User's Exam.  Answers to the User's Exam questions are ",
                "in the Student Guide - you are welcome to have the Student Guide open while you take ",
                "the User's exam - we just want to make sure you know course policies before you ",
                "start.  Once you get started with course content, remember to check the ",
                "\"Exam Deadlines\" link to stay on track with due dates.");
        body.addln();

        body.addln("If you can't find the course web site, or if you have any questions about ",
                "what's in the Student Guide, you can send an email to precalc_math@colostate.edu.");
        body.addln();

        emitClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.WELCOME,
                EMsg.WELCus02, MsgUtils.buildWelcomeSubject(context), body.toString());
    }

    /**
     * Generates a report row for a "**WELCUS3" message. This applies to students who have attempted the User's exam
     * three or fewer times without passing, when the current date is after the first review exam due date.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateWELCus03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final RawStudent stu = context.student;

        final HtmlBuilder body = new HtmlBuilder(1000);
        emitOpening(stu, context, body);

        body.add("It looks like you've found the course in Canvas, and gotten in to the course ",
                "web site, and tried out the User's Exam.  Answers to the User's Exam questions are ",
                "in the Student Guide - you are welcome to have the Student Guide open while you take ",
                "the User's exam - we just want to make sure you know course policies before you ",
                "start.  Once you get started with course content, remember to check the ",
                "\"Exam Deadlines\" link to stay on track with due dates.");
        body.addln();

        body.addln("The semester is flying by, and we're already past the due date for the first ",
                "Unit Review Exam, so it's important that you get started as soon as you can to avoid ",
                "losing more points to late penalties.");
        body.addln();

        body.addln("If you can't find the course web site, or if you have any questions about ",
                "what's in the Student Guide, you can send an email to precalc_math@colostate.edu.");
        body.addln();

        emitClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.WELCOME,
                EMsg.WELCus03, MsgUtils.buildWelcomeSubject(context), body.toString());
    }

    /**
     * Generates a report row for a "**WELCUS4" message. This applies to students who have attempted the User's exam
     * four or more times without passing, when the current date is on or earlier than the first review exam due date.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateWELCus04(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final RawStudent stu = context.student;

        final HtmlBuilder body = new HtmlBuilder(1000);
        emitOpening(stu, context, body);

        body.add("It looks like you've found the course in Canvas, and gotten in to the course ",
                "web site, and tried out the User's Exam a few times.  Answers to the User's Exam ",
                "questions are in the Student Guide - you are welcome to have the Student Guide open ",
                "while you take the User's exam - we just want to make sure you know course policies ",
                "before you start.  Once you get started with course content, remember to check the ",
                "\"Exam Deadlines\" link to stay on track with due dates.");
        body.addln();

        body.addln("If you can't find the course web site, or if you have any questions about ",
                "what's in the Student Guide, you can send an email to precalc_math@colostate.edu.");
        body.addln();

        emitClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.WELCOME,
                EMsg.WELCus04, MsgUtils.buildWelcomeSubject(context), body.toString());
    }

    /**
     * Generates a report row for a "**WELCUS5" message. This applies to students who have attempted the User's exam
     * four or more times without passing, when the current date is after the first review exam due date.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateWELCus05(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final RawStudent stu = context.student;

        final HtmlBuilder body = new HtmlBuilder(1000);
        emitOpening(stu, context, body);

        body.add("It looks like you've found the course in Canvas, and gotten in to the course ",
                "web site, and tried out the User's Exam a few times.  Answers to the User's Exam ",
                "questions are in the Student Guide - you are welcome to have the Student Guide open ",
                "while you take the User's exam - we just want to make sure you know course policies ",
                "before you start.  Once you get started with course content, remember to check the ",
                "\"Exam Deadlines\" link to stay on track with due dates.");
        body.addln();

        body.addln("The semester is flying by, and we're already past the due date for the first ",
                "Unit Review Exam, so it's important that you get started as soon as you can to avoid ",
                "losing more points to late penalties.");
        body.addln();

        body.addln("If you can't find the course web site, or if you have any questions about ",
                "what's in the Student Guide, you can send an email to precalc_math@colostate.edu.");
        body.addln();

        emitClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.WELCOME,
                EMsg.WELCus05, MsgUtils.buildWelcomeSubject(context), body.toString());
    }

    /**
     * Generates a report row for a "**WELCSR0" message. This applies to students who have passed the User's exam but
     * not yet attempted the Skills Review exam, when the current date is on or earlier than the first review exam due
     * date.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateWELCSR00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final RawStudent stu = context.student;

        final HtmlBuilder body = new HtmlBuilder(1000);
        emitOpening(stu, context, body);

        final String firstReg = context.sortedRegs.get(0).course;

        body.add(" It looks like you've already passed the User's Exam - excellent!  Your next ",
                "step is to take the ", MsgUtils.courseName(firstReg), " Skills Review exam.  If you pass that ",
                "on the first try, you can dive right in to the main course content.  If not, some ",
                "review materials will open up and you can review and take the Skills Review exam ",
                "again.  Remember to check the \"Exam Deadlines\" link to stay on track with due ",
                "dates.");

        body.addln();

        body.addln("If you have questions about the course, please send an email to ",
                "precalc_math@colostate.edu.");
        body.addln();

        emitClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.WELCOME,
                EMsg.WELCsr00, MsgUtils.buildWelcomeSubject(context), body.toString());
    }

    /**
     * Generates a report row for a "**WELCSR1" message. This applies to students who have passed the User's exam but
     * not yet attempted the Skills Review, when the current date is after the first review exam due date.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateWELCSR01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final RawStudent stu = context.student;

        final HtmlBuilder body = new HtmlBuilder(1000);
        emitOpening(stu, context, body);

        final String firstReg = context.sortedRegs.get(0).course;

        body.add(" It looks like you've already passed the User's Exam - excellent!  Your next ",
                "step is to take the ", MsgUtils.courseName(firstReg), " Skills Review exam.  If you pass that ",
                "on the first try, you can dive right in to the main course content.  If not, some ",
                "review materials will open up and you can review and take the Skills Review exam ",
                "again.  Remember to check the \"Exam Deadlines\" link to stay on track with due ",
                "dates.");
        body.addln();

        body.addln("The semester is flying by, and we're already past the due date for the first ",
                "Unit Review Exam, so it's important that you get past the Skills Review as soon as ",
                "you can to avoid losing more points to late penalties.");
        body.addln();

        body.addln("If you have questions about the course, please send an email to ",
                "precalc_math@colostate.edu.");
        body.addln();

        emitClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.WELCOME,
                EMsg.WELCsr01, MsgUtils.buildWelcomeSubject(context), body.toString());
    }

    /**
     * Generates a report row for a "**WELCSR2" message. This applies to students who have attempted the Skills Review
     * exam three or fewer times without passing, when the current date is on or earlier than the first review exam due
     * date.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateWELCSR02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final RawStudent stu = context.student;

        final HtmlBuilder body = new HtmlBuilder(1000);
        emitOpening(stu, context, body);

        final String firstReg = context.sortedRegs.get(0).course;

        body.add(" It looks like you've already passed the User's Exam - excellent!  Your next ",
                "step is to get past the ", MsgUtils.courseName(firstReg), " Skills Review exam.  In addition ",
                "to the review materials on the course web page, we have help available many hours ",
                "each week - check the \"Getting Help\" link for details.  Also, remember to check ",
                "the \"Exam Deadlines\" link to stay on track with due dates.");
        body.addln();

        body.addln("If you have questions about the course, please send an email to ",
                "precalc_math@colostate.edu.");
        body.addln();

        emitClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.WELCOME,
                EMsg.WELCsr02, MsgUtils.buildWelcomeSubject(context), body.toString());
    }

    /**
     * Generates a report row for a "**WELCSR3" message. This applies to students who have attempted the Skills Review
     * exam three or fewer times without passing, when the current date is after the first review exam due date.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateWELCSR03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final RawStudent stu = context.student;

        final HtmlBuilder body = new HtmlBuilder(1000);
        emitOpening(stu, context, body);

        final String firstReg = context.sortedRegs.get(0).course;

        body.add(" It looks like you've already passed the User's Exam - excellent!  Your next ",
                "step is to get past the ", MsgUtils.courseName(firstReg), " Skills Review exam.  In addition ",
                "to the review materials on the course web page, we have help available many hours ",
                "each week - check the \"Getting Help\" link for details.  Also, remember to check ",
                "the \"Exam Deadlines\" link to stay on track with due dates.");
        body.addln();

        body.addln("The semester is flying by, and we're already past the due date for the first ",
                "Unit Review Exam, so it's important that you get past the Skills Review and into ",
                "Unit 1 as soon as you can to avoid losing more points to late penalties.");
        body.addln();

        body.addln("If you have questions about the course, please send an email to ",
                "precalc_math@colostate.edu.");
        body.addln();

        emitClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.WELCOME,
                EMsg.WELCsr03, MsgUtils.buildWelcomeSubject(context), body.toString());
    }

    /**
     * Generates a report row for a "**WELCSR4" message. This applies to students who have attempted the Skills Review
     * exam four or more times without passing, when the current date is on or earlier than the first review exam due
     * date.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateWELCSR04(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final RawStudent stu = context.student;

        final HtmlBuilder body = new HtmlBuilder(1000);
        emitOpening(stu, context, body);

        final String firstReg = context.sortedRegs.get(0).course;

        body.add(" It looks like you've already passed the User's Exam (excellent!), but the ",
                MsgUtils.courseName(firstReg), " Skills Review exam is causing some trouble.  In addition ",
                "to the review materials on the course web page, we have help available many hours ",
                "each week - check the \"Getting Help\" link for details.  Also, remember to check ",
                "the \"Exam Deadlines\" link to stay on track with due dates.");
        body.addln();

        body.addln("If you have questions about the course, please send an email to ",
                "precalc_math@colostate.edu.");
        body.addln();

        emitClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.WELCOME,
                EMsg.WELCsr04, MsgUtils.buildWelcomeSubject(context), body.toString());
    }

    /**
     * Generates a report row for a "**WELCSR8" message. This applies to students who have attempted the Skills Review
     * exam four or more times without passing, when the current date is after the first review exam due date.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateWELCSR05(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final RawStudent stu = context.student;

        final HtmlBuilder body = new HtmlBuilder(1000);
        emitOpening(stu, context, body);

        final String firstReg = context.sortedRegs.get(0).course;

        body.add(" It looks like you've already passed the User's Exam (excellent!), but the ",
                MsgUtils.courseName(firstReg), " Skills Review exam is causing some trouble.  In addition ",
                "to the review materials on the course web page, we have help available many hours ",
                "each week - check the \"Getting Help\" link for details.  Also, remember to check ",
                "the \"Exam Deadlines\" link to stay on track with due dates.");
        body.addln();

        body.addln("The semester is flying by, and we're already past the due date for the first ",
                "Unit Review Exam, so it's important that you get past the Skills Review exam as ",
                "soon as you can - let me know if you need some help connecting with our learning ",
                "assistants or finding resources.");
        body.addln();

        body.addln("If you have questions about the course, please send an email to ",
                "precalc_math@colostate.edu.");
        body.addln();

        emitClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.WELCOME,
                EMsg.WELCsr05, MsgUtils.buildWelcomeSubject(context), body.toString());
    }

    /**
     * Generates a report row for a "**WELOK0" message. This applies to students who have opened their first course,
     * passed the User's exam, and passed the Skills Review exam by the time we first notice their registration.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateWELOK0(final MessagingContext context,
                                                final MessagingCourseStatus status) {

        final RawStudent stu = context.student;
        final RawStcourse firstReg = context.sortedRegs.get(0);

        final HtmlBuilder body = new HtmlBuilder(1000);
        emitOpening(stu, context, body);

        if (status.passedFIN) {
            body.add("I see you've already powered through the course and passed the Final Exam - ",
                    "that's amazing!  You can re-test on any of the Unit or Final exams to try to ",
                    "increase your point total.");
        } else if (status.passedUE4) {
            body.add("I see you've already powered through all four units - that's amazing!  ",
                    "All you have left to do is to pass the Final exam.  Then you can re-test on any ",
                    "of the Unit or Final exams to try to increase your point total.");
        } else {
            if (status.passedUE3) {
                body.add("I see you've already powered through three units - that's amazing!  If ",
                        "you finish up Unit 4 and pass the Final exam, you should be done with the ",
                        "course.  After that, you can re-test on any of the Unit or Final exams to ",
                        "try to increase your point total.");
            } else if (status.passedUE2) {
                body.add("I see you've already powered through two units - excellent!  It looks ",
                        "like you have things under control.  Once you've passed the last units and ",
                        "the Final Exam, you can re-test on any of the Unit or Final exams to try to ",
                        "increase your point total.");
            } else if (status.passedUE1) {
                body.add("I see you've already powered through Unit 1 - excellent!  It looks like ",
                        "you have things under control.  Once you've passed the other three units and ",
                        "the Final Exam, you can re-test on any of the Unit or Final exams to try to ",
                        "increase your point total.");
            } else if (status.passedRE1) {
                body.add("I see you've already powered through Unit 1 - excellent!  It looks like ",
                        "you have things under control.  Once you've passed the proctored Unit 1 ",
                        "Exam, you can move on to the next unit.");
            } else {
                body.add("I see you've already passed the User's Exam and the Skills Review ",
                        "exam - excellent!  Your next steps are to go through the objectives in the ",
                        "first unit, and then take the unit review exam.");
            }

            body.addln();

            body.add("If you need help with any of the material, we have ");
            if (!firstReg.sect.isEmpty() && firstReg.sect.charAt(0) == '0') {
                body.add("both in-person and online");
            } else {
                body.add("online");
            }
            body.addln(" help available.  Check the \"Getting Help\" link in the course page for ",
                    "details and hours.");
            body.addln();

            body.addln("Also, remember to check the \"Exam Deadlines\" link on the course page ",
                    "to stay on track.");
        }
        body.addln();

        body.addln("If you have any questions, please send an email to ",
                "precalc_math@colostate.edu.");
        body.addln();

        emitClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(0), EMilestone.WELCOME,
                EMsg.WELCok00, MsgUtils.buildWelcomeSubject(context), body.toString());
    }

    /**
     * Emits the salutation and the opening boilerplate text for the welcome message.
     *
     * @param stu     the student object
     * @param context the messaging context
     * @param body    the {@code HtmlBuilder} to which to emit content
     */
    private static void emitOpening(final RawStudent stu, final MessagingContext context,
                                    final HtmlBuilder body) {

        if (stu.prefName == null) {
            if (stu.firstName == null) {
                if (LocalTime.now().getHour() < 12) {
                    body.addln("Good morning,");
                } else {
                    body.addln("Good afternoon,");
                }
            } else {
                body.add("Hi ");
                body.add(MsgUtils.sanitizeName(stu.firstName));
                body.addln(",");
            }
        } else {
            body.add("Hi ");
            body.add(MsgUtils.sanitizeName(stu.prefName));
            body.addln(",");
        }
        body.addln();

        final RawStcourse firstReg = context.sortedRegs.get(0);
        body.addln("Welcome to the Precalculus Program!  My name is Anita Pattison - one of the ",
                "co-directors for the program.  I'll try to help you get settled in, and then I'll be ",
                "keeping track of your progress, and checking in from time to time to see how you're ",
                "doing.  I want to do all I can to help you to succeed in ",
                MsgUtils.courseName(firstReg.course),
                ".  If you're on track, I'll try not to bother you too much, except maybe with ",
                "reminders of important due dates. But if it seems like you might be struggling with ",
                "something, I will reach out to see how I can help.");
        body.addln();

        body.add("This course has online lessons and online assignments, followed by ");
        if (!firstReg.sect.isEmpty() && firstReg.sect.charAt(0) == '0') {
            // On-campus section - in-person testing
            body.addln("an in-person exam for each unit, and an in-person final exam.  The ",
                    "in-person exams are taken in the Precalculus Center, Weber 138.");
        } else {
            // Remote section - online testing
            body.addln("a proctored exam for each unit and a proctored final exam.  You can take ",
                    "proctored exams online (using a compatible device with a webcam) or in-person ",
                    "in the Precalculus Center, Weber 138, if you happen to live near campus.");
        }
        body.addln();

        body.addln("An important note: this is not a self-paced course!  Unit Review exams and ",
                "the Final exam all have deadline dates.  If a Unit Review exam is not passed by ",
                "its deadline, 3 points are lost.  If the Final Exam is not completed by its ",
                "deadline, the course cannot be completed!");
        body.addln();

        body.addln("This course participates in the CSU Bookstore's \"Day One Access\" program, ",
                "which means the e-text will automatically be activated and charged to your student ",
                "account (I believe the cost is $19).");
        body.addln();
    }

    /**
     * Emits the closing boilerplate text for the welcome message.
     *
     * @param body the {@code HtmlBuilder} to which to emit content
     */
    private static void emitClosing(final HtmlBuilder body) {

        body.addln("I hope you have a great semester!");
        body.addln();

        body.addln("Kind regards,");
        body.addln("Anita Pattison");
        body.addln("(she/her/hers)");
        body.addln();
        body.addln("Precalculus Center Co-Director");
        body.addln("Department of Mathematics");
    }
}

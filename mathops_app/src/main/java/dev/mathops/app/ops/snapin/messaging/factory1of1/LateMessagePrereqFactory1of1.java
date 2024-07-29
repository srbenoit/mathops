package dev.mathops.app.ops.snapin.messaging.factory1of1;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.app.ops.snapin.messaging.EMilestone;
import dev.mathops.app.ops.snapin.messaging.EMsg;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;
import dev.mathops.app.ops.snapin.messaging.MsgUtils;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;

import java.sql.SQLException;
import java.util.List;

/**
 * A factory class that can generate messages to 1-course students who are "late" (urgency >= 1) and who have not yet
 * satisfied prerequisites for their first class.
 */
enum LateMessagePrereqFactory1of1 {
    ;

    /**
     * Generates an appropriate message when the student has not satisfied the prerequisite.
     *
     * @param cache   the data cache
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    static MessageToSend generate(final Cache cache, final MessagingContext context,
                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (status.daysSinceLastMessage > 3) {
            int placementAvailable = 0;
            try {
                final List<RawStmpe> attempts =
                        RawStmpeLogic.queryLegalByStudent(cache, context.student.stuId);
                placementAvailable = Math.max(0, 2 - attempts.size());
            } catch (final SQLException ex) {
                Log.warning("Failed to query number of placement attempts.", ex);
            }

            final boolean no036 =
                    MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.PREQpr00, EMsg.PREQpr03, EMsg.PREQpr06);
            final boolean no147 =
                    MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.PREQpr01, EMsg.PREQpr04, EMsg.PREQpr07);
            final boolean no258 =
                    MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.PREQpr02, EMsg.PREQpr05, EMsg.PREQpr08);

            if (placementAvailable == 2) {
                if (no036) {
                    result = generatePREQpr00(context, status);
                } else if (no147) {
                    result = generatePREQpr01(context, status);
                } else if (no258) {
                    result = generatePREQpr02(context, status);
                } else if (status.daysSinceLastMessage > 10) {
                    if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.PREQpr09)) {
                        result = generatePREQpr09(context, status);
                    } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.PREQpr10)) {
                        result = generatePREQpr10(context, status);
                    } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.PREQpr11)) {
                        result = generatePREQpr11(context, status);
                    }
                }
            } else if (placementAvailable == 1) {
                if (no036) {
                    result = generatePREQpr03(context, status);
                } else if (no147) {
                    result = generatePREQpr04(context, status);
                } else if (no258) {
                    result = generatePREQpr05(context, status);
                } else if (status.daysSinceLastMessage > 10) {
                    if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.PREQpr09)) {
                        result = generatePREQpr09(context, status);
                    } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.PREQpr10)) {
                        result = generatePREQpr10(context, status);
                    } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.PREQpr11)) {
                        result = generatePREQpr11(context, status);
                    }
                }
            } else if (no036) {
                result = generatePREQpr06(context, status);
            } else if (no147) {
                result = generatePREQpr07(context, status);
            } else if (no258) {
                result = generatePREQpr08(context, status);
            } else if (status.daysSinceLastMessage > 10) {
                if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.PREQpr09)) {
                    result = generatePREQpr09(context, status);
                } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.PREQpr10)) {
                    result = generatePREQpr10(context, status);
                } else if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.PREQpr11)) {
                    result = generatePREQpr11(context, status);
                }
            }
        }

        return result;
    }

    /**
     * Generates a report row for a PREQpr00 message. This applies to students who have not satisfied the prerequisite,
     * no previous PREQpr00, PREQpr03, or PREQpr06 message has been sent, and the student has not tried placement.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generatePREQpr00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.getFirst().course;
        final String crsName = MsgUtils.courseName(courseId);
        final String subject = "Prerequisites for " + crsName;

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Any progress on getting the prerequisite situation resolved for ", crsName,
                "?  It looks like you still have two Math Placement Tool attempts that could work.");
        body.addln();

        if (status.milestones.r1.minusDays(3L).isBefore(context.today)) {
            body.addln("Course due dates are moving along - I want to make sure you get things ",
                    "straightened out in time to succeed in the course.");
            body.addln();
        }

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, MsgUtils.ZERO, EMilestone.PREREQ, EMsg.PREQpr00, subject,
                body.toString());
    }

    /**
     * Generates a report row for a PREQpr01 message. This applies to students who have not satisfied the prerequisite,
     * no previous PREQpr01, PREQpr04, or PREQpr07 message has been sent, and the student has not tried placement.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generatePREQpr01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.getFirst().course;
        final String crsName = MsgUtils.courseName(courseId);
        final String subject = "Prerequisites for " + crsName;

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Just checking again to see if there's anything I can do to help you get the ",
                "prerequisites cleared for ", crsName, ".  Have you checked out the Math Placement ",
                "web site to see if that's a good option?");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, MsgUtils.ZERO, EMilestone.PREREQ, EMsg.PREQpr01, subject,
                body.toString());
    }

    /**
     * Generates a report row for a PREQpr02 message. This applies to students who have not satisfied the prerequisite,
     * no previous PREQpr02, PREQpr05, or PREQpr08 message has been sent, and the student has not tried placement.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generatePREQpr02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.getFirst().course;
        final String crsName = MsgUtils.courseName(courseId);
        final String subject = "Prerequisites for " + crsName;

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Sorry for being a pest - would it make sense at this point to consider taking ",
                crsName, " in a future semester, after you have time to go through placement or maybe ",
                "transfer in some coursework that lets you get in to the course?");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, MsgUtils.ZERO, EMilestone.PREREQ, EMsg.PREQpr02, subject,
                body.toString());
    }

    /**
     * Generates a report row for a PREQpr03 message. This applies to students who have not satisfied the prerequisite,
     * no previous PREQpr00, PREQpr03, or PREQpr06 message has been sent, and the student has tried placement once.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generatePREQpr03(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.getFirst().course;
        final String crsName = MsgUtils.courseName(courseId);
        final String subject = "Prerequisites for " + crsName;

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("Any progress on getting the prerequisite situation resolved for ", crsName,
                "?  It looks like you still have a Math Placement Tool attempt that could work.");

        if (RawRecordConstants.M117.equals(courseId)) {
            body.add("  Completing the ELM Tutorial would also clear that prerequisite.");
        }
        body.addln();
        body.addln();

        if (status.milestones.r1.minusDays(3L).isBefore(context.today)) {
            body.addln("Course due dates are moving along - I want to make sure you get things ",
                    "straightened out in time to succeed in the course.");
            body.addln();
        }

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, MsgUtils.ZERO, EMilestone.PREREQ, EMsg.PREQpr03, subject,
                body.toString());
    }

    /**
     * Generates a report row for a PREQpr04 message. This applies to students who have not satisfied the prerequisite,
     * no previous PREQpr01, PREQpr04, or PREQpr07 message has been sent, and the student has tried placement once.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generatePREQpr04(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.getFirst().course;
        final String crsName = MsgUtils.courseName(courseId);
        final String subject = "Prerequisites for " + crsName;

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("Just checking again to see if there's anything I can do to help you get ",
                "the prerequisites cleared for ", crsName, ".  Let me know if I can help with ");

        if (RawRecordConstants.M117.equals(courseId)) {
            body.add("Math Placement or the ELM Tutorial.");
        } else {
            body.add("Math Placement.");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, MsgUtils.ZERO, EMilestone.PREREQ, EMsg.PREQpr04, subject,
                body.toString());
    }

    /**
     * Generates a report row for a PREQpr05 message. This applies to students who have not satisfied the prerequisite,
     * no previous PREQpr02, PREQpr05, or PREQpr08 message has been sent, and the student has tried placement once.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generatePREQpr05(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.getFirst().course;
        final String crsName = MsgUtils.courseName(courseId);
        final String subject = "Prerequisites for " + crsName;

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("Sorry for being a pest - would it make sense at this point to consider taking ",
                crsName, " in a future semester, after you have time to go through placement ");
        if (RawRecordConstants.M117.equals(courseId)) {
            body.add("or the ELM Tutorial ");
        }
        body.addln("or maybe transfer in some coursework that lets you get in to the course?");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, MsgUtils.ZERO, EMilestone.PREREQ, EMsg.PREQpr05, subject,
                body.toString());
    }

    /**
     * Generates a report row for a PREQpr06 message. This applies to students who have not satisfied the prerequisite,
     * no previous PREQpr00, PREQpr03, or PREQpr06 message has been sent, and the student has no placement attempts
     * remaining.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generatePREQpr06(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.getFirst().course;
        final String crsName = MsgUtils.courseName(courseId);
        final String subject = "Prerequisites for " + crsName;

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("Any progress on getting the prerequisite situation resolved for ", crsName, "?");

        if (RawRecordConstants.M117.equals(courseId)) {
            body.add("  Completing the ELM Tutorial is the best way to get that done - otherwise, ",
                    "you would need transfer credit of some sort.");
        } else {
            body.add("  Do you have any transfer credit coming in?  If not, you might need to add ",
                    "prerequisite courses to your schedule to allow you to access ", crsName, ".");
        }
        body.addln();
        body.addln();

        if (status.milestones.r1.minusDays(3L).isBefore(context.today)) {
            body.addln("Course due dates are moving along - I want to make sure you get things ",
                    "straightened out in time to succeed in the course.");
            body.addln();
        }

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, MsgUtils.ZERO, EMilestone.PREREQ, EMsg.PREQpr06, subject,
                body.toString());
    }

    /**
     * Generates a report row for a PREQpr07 message. This applies to students who have not satisfied the prerequisite,
     * no previous PREQpr01, PREQpr04, or PREQpr07 message has been sent, and the student has no placement attempts
     * remaining.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generatePREQpr07(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.getFirst().course;
        final String crsName = MsgUtils.courseName(courseId);
        final String subject = "Prerequisites for " + crsName;

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("Just checking again to see if there's anything I can do to help you get the ",
                "prerequisite cleared for ", crsName, ".");

        if (RawRecordConstants.M118.equals(courseId)) {
            if (context.prereqLogic.hasSatisfiedPrerequisitesFor(RawRecordConstants.M117)) {
                body.add("  Do you have room in your schedule to add MATH 117?");
            } else {
                body.add("  Could you quickly run through the ELM Tutorial to get placed into ",
                        "MATH 117, and then add MATH 117?");
            }
        } else if (RawRecordConstants.M124.equals(courseId)
                || RawRecordConstants.M125.equals(courseId)) {
            if (context.prereqLogic.hasSatisfiedPrerequisitesFor(RawRecordConstants.M118)) {
                body.add("  Do you have room in your schedule to add MATH 118?");
            } else {
                body.add("  Do you have room in your schedule to add MATH 117 and MATH 118?");
            }
        } else if (RawRecordConstants.M126.equals(courseId)) {
            if (context.prereqLogic.hasSatisfiedPrerequisitesFor(RawRecordConstants.M125)) {
                body.add("  Do you have room in your schedule to add MATH 125?");
            } else if (context.prereqLogic.hasSatisfiedPrerequisitesFor(RawRecordConstants.M118)) {
                body.add("  Do you have room in your schedule to add MATH 118 and MATH 125?");
            } else {
                body.add("  Do you have room in your schedule to add MATH 117, MATH 118, and ",
                        "MATH 125?");
            }
        }
        body.addln();
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, MsgUtils.ZERO, EMilestone.PREREQ, EMsg.PREQpr07, subject,
                body.toString());
    }

    /**
     * Generates a report row for a PREQpr08 message. This applies to students who have not satisfied the prerequisite,
     * no previous PREQpr02, PREQpr05, or PREQpr08 message has been sent, and the student has no placement attempts
     * remaining.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generatePREQpr08(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.getFirst().course;
        final String crsName = MsgUtils.courseName(courseId);
        final String subject = "Prerequisites for " + crsName;

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.add("Sorry for being a pest - would it make sense at this point to consider taking ",
                crsName, " in a future semester, after you have time to go through placement ");
        if (RawRecordConstants.M117.equals(courseId)) {
            body.add("or the ELM Tutorial ");
        }
        body.addln("or maybe transfer in some coursework that lets you get in to the course?");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, MsgUtils.ZERO, EMilestone.PREREQ, EMsg.PREQpr08, subject,
                body.toString());
    }

    /**
     * Generates a report row for a PREQpr09 message. This applies to students who have received the full suite of
     * "prereq" messages prompting them to do something, but have not resolved the situation, and it has been 10 days
     * since the last communication.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generatePREQpr09(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.getFirst().course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("Just wanted to remind you that you're still registered for ", crsName,
                " without having the prerequisites to start.  Can I help out with a course drop?  ",
                "If you can't drop that course and you'd like me to stop bugging you about it, let ",
                "me know.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, MsgUtils.ZERO, EMilestone.PREREQ, EMsg.PREQpr09, crsName,
                body.toString());
    }

    /**
     * Generates a report row for a PREQpr10 message. This applies to students who have received the full suite of
     * "prereq" messages prompting them to do something, but have not resolved the situation, and it has been 10 days
     * since the last communication.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generatePREQpr10(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.getFirst().course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("One more reminder that ", crsName, " is still on your schedule this ",
                "semester.  We will submit a U grade at the end of the term (that does not affect ",
                "GPA, but does stay on your transcript).  If you withdraw, a W would get recorded ",
                "instead, and you might be able to appeal for a late drop to have it removed from ",
                "your transcript completely.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, MsgUtils.ZERO, EMilestone.PREREQ, EMsg.PREQpr10, crsName,
                body.toString());
    }

    /**
     * Generates a report row for a PREQpr11 message. This applies to students who have received the full suite of
     * "prereq" messages prompting them to do something, but have not resolved the situation, and it has been 10 days
     * since the last communication.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row with the message to be sent to the student
     */
    private static MessageToSend generatePREQpr11(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final String courseId = context.sortedRegs.getFirst().course;
        final String crsName = MsgUtils.courseName(courseId);

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        body.addln("One last reminder about ", crsName,
                ", and then I'll quit bugging you about it...");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, MsgUtils.ZERO, EMilestone.PREREQ, EMsg.PREQpr11, crsName,
                body.toString());
    }
}

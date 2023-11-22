package dev.mathops.app.ops.snapin.messaging.factory;

import dev.mathops.core.TemporalUtils;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.app.ops.snapin.messaging.EMilestone;
import dev.mathops.app.ops.snapin.messaging.EMsg;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;
import dev.mathops.app.ops.snapin.messaging.MsgUtils;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;

import java.time.LocalDate;

/**
 * A factory class that can generate messages to students who are "late" (urgency >= 1) who have not passed the final
 * exam. This will get called 0-3 days before the final exam due date if the final exam has not been passed, or if the
 * student has their "last try" attempt available.
 */
public enum LateMessageFINFactory {
    ;

    /**
     * Generates an appropriate message for a student who has not yet passed the final exam.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    public static MessageToSend generate(final MessagingContext context, final MessagingCourseStatus status) {

        final MessageToSend result;

        final int tries = status.failedTriesOnFIN;

        final LocalDate today = LocalDate.now();
        final LocalDate finalDeadline = status.milestones.fin;

        if (today.isAfter(finalDeadline)) {

            // Within the "last try" period
            if (status.lastTryAvailable) {
                final LocalDate lastDeadline = status.milestones.last;

                if (today.isAfter(lastDeadline)) {
                    result = generateBlocked(context, status);
                } else {
                    if (tries == 0) {
                        result = generateLastTryNotTried(context, status);
                    } else {
                        result = generateLastTryTried(context, status);
                    }
                }
            } else {
                result = generateBlocked(context, status);
            }
        } else {
            // On or before the final exam deadline
            if (tries == 0) {
                // FIN never attempted
                result = generateNotTried(context, status);
            } else if (tries < 4) {
                // FIN attempted <4 times, not passed
                result = generateFailedFew(context, status);
            } else {
                // FIN attempted >=4 times, not passed
                result = generateFailedMany(context, status);
            }
        }

        return result;
    }

    /**
     * Generates an appropriate message when the Final has not been attempted but the due date has passed, and we're
     * within the "last try" period with a last try available.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateLastTryNotTried(final MessagingContext context,
                                                         final MessagingCourseStatus status) {

        // Presumably, the student has already received one or more of the "Final is due soon"
        // messages...

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.LASTfe00)) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            final LocalDate last = status.milestones.last;

            body.add("You haven't passed the Final exam yet, but since you were eligible for that ",
                    "exam on its due date, the system has automatically given you another try by the ",
                    "end of the day ");
            if (last.equals(context.today)) {
                body.addln("today.");
            } else {
                body.addln("on ", TemporalUtils.FMT_WMD.format(last), ".");
            }
            body.addln();

            body.addln("If you pass the exam on that try, great - you're all finished.  If not, ",
                    "please let us know and we will talk about what options are available and next ",
                    "steps.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.FIN, EMsg.LASTfe00, "Final exam additional try", body.toString());
        }

        return result;
    }

    /**
     * Generates an appropriate message when the Final has been attempted but the due date has passed, and we're within
     * the "last try" period with a last try available.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateLastTryTried(final MessagingContext context,
                                                      final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.LASTfe00, EMsg.LASTfe01)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            final LocalDate last = status.milestones.last;

            body.add("You haven't passed the Final exam yet, but since you were eligible for that ",
                    "exam on its due date, the system has automatically given you another try by the ",
                    "end of the day ");

            if (last.equals(context.today)) {
                body.addln("today.");
            } else {
                body.addln("on ", TemporalUtils.FMT_WMD.format(last), ".");
            }

            body.addln();

            body.addln("If you pass the exam on that try, great - you're all finished.  If not, ",
                    "please let us know and we will talk about what options are available and next ",
                    "steps.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.FIN, EMsg.LASTfe01, "Final exam additional try", body.toString());
        }

        return result;
    }

    /**
     * Generates an appropriate message when the final due date and last try dates have passed and the student has not
     * completed the course. IF they are close, we recommend they come talk to us - if not, we recommend next steps
     * after not completing a course.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateBlocked(final MessagingContext context,
                                                 final MessagingCourseStatus status) {

        final MessageToSend result;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.BLOKwd00)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln(
                    "The Final exam was not passed by its due date, so the system has locked you ",
                    "out of your course.  At this point, we will submit a U grade for that course ",
                    "(which does not affect GPA, but does appear on a transcript).");
            body.addln();

            if (context.currentRegIndex == context.pace - 1) {
                // This is the last course
                if (status.passedRE4) {
                    // Close to finishing - we may want to allow some leeway
                    if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
                        body.addln("Please stop in at the Precalculus Center office to talk about what ",
                                "options are available and next steps.");
                    } else {
                        body.addln("Do you want to talk about options and possible next steps?");
                    }
                    body.addln();
                }
            } else {
                // Not the last course - student can withdraw from a later course to get more time
                if (status.passedRE4) {
                    body.add("You can withdraw from ");
                    if (context.pace > 2) {
                        body.add("your last course");
                    } else {
                        body.add("your second course ");
                    }
                    body.add(" to get more time to finish this course, but you were very ",
                            "close to finishing - ");

                    if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
                        body.addln("please stop in at the Precalculus Center office to talk about ",
                                "what options are available and next steps.");
                    } else {
                        body.addln("do you want to talk about options and possible next steps?");
                    }
                    body.addln();
                } else {
                    // Not close to passing - should withdraw from a later course
                    body.add("In order to get more time to finish this course, you will need to ",
                            "withdraw from a later course (via RamWeb).  That will make the deadline ",
                            "schedule stretch out for ");
                    if (context.pace > 2) {
                        body.addln("the courses that remain.");
                    } else {
                        body.addln("this course.");
                    }
                    body.addln();
                }
            }

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.BLOK, EMsg.BLOKwd00, "Locked out of course", body.toString());
        } else {
            result =
                    new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.BLOK, EMsg.BLOKwd00, "Blocked", null);
        }

        return result;
    }

    /**
     * Generates an appropriate message when the Final Exam has not been attempted.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateNotTried(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        final boolean noFE0 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.FINRfe00);
        final boolean noFE1 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.FINRfe01);
        final boolean noFE2 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.FINRfe02);

        if (noFE0) {
            result = generateFINRfe00(context, status);
        } else if (noFE1) {
            result = generateFINRfe01(context, status);
        } else if (noFE2) {
            result = generateFINRfe02(context, status);
        } else {
            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.FIN, EMsg.FINRfe02, "Final not passed", null);
        }

        return result;
    }

    /**
     * Generates an appropriate message when the Final Exam has been attempted less than 4 times.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFailedFew(final MessagingContext context,
                                                   final MessagingCourseStatus status) {

        final MessageToSend result;

        final boolean noFE04 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.FINRfe00, EMsg.FINRfe04);
        final boolean noFE15 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.FINRfe01, EMsg.FINRfe05);
        final boolean noFE26 = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.FINRfe02, EMsg.FINRfe06);

        if (noFE04) {
            result = generateFINRfe04(context, status);
        } else if (noFE15) {
            result = generateFINRfe05(context, status);
        } else if (noFE26) {
            result = generateFINRfe06(context, status);
        } else {
            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.FIN, EMsg.FINRfe06, "Final not passed", null);
        }

        return result;
    }

    /**
     * Generates an appropriate message when the Final Exam has been attempted 4 or more times but not yet passed.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFailedMany(final MessagingContext context,
                                                    final MessagingCourseStatus status) {

        final MessageToSend result;

        final boolean noFE040 =
                MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.FINRfe00, EMsg.FINRfe04, EMsg.FINXfe00);
        final boolean noFE151 =
                MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.FINRfe01, EMsg.FINRfe05, EMsg.FINXfe01);
        final boolean noFE262 =
                MsgUtils. hasNoMsgOfCodes(context.messages, EMsg.FINRfe02, EMsg.FINRfe06, EMsg.FINXfe02);

        if (noFE040) {
            result = generateFINXfe00(context, status);
        } else if (noFE151) {
            result = generateFINXfe01(context, status);
        } else if (noFE262) {
            result = generateFINXfe02(context, status);
        } else {
            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.FIN, EMsg.FINXfe02, "Final not passed", null);
        }

        return result;
    }

    /**
     * Generates a report row for a FINRfe00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFINRfe00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        final LocalDate due = status.milestones.fin;

        body.add("It looks like you're all set for the Final Exam - you're almost done! ");

        if (due.equals(context.today)) {
            body.add("The due date is this evening.  ");
        } else {
            body.add("The due date is ", TemporalUtils.FMT_WMD.format(due), ".  ");
        }

        body.addln("You need to pass the Final Exam with a 16 or higher to complete the course.  ",
                "Just like with the Unit exams, you have unlimited tries, but there are no ",
                "restrictions like having to do something after two non-passing tries.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.FIN, EMsg.FINRfe00, "Course deadline", body.toString());
    }

    /**
     * Generates a report row for a FINRfe01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFINRfe01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 2) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to finish the Final exam before the course deadline...");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.FIN, EMsg.FINRfe01, "Course deadline", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a FINRfe02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFINRfe02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);
        final LocalDate due = status.milestones.fin;

        if (due.equals(context.today)) {
            body.addln("Another reminder about the Final exam that's due today.");
        } else {
            body.addln("Another reminder about the Final exam that's due ",
                    TemporalUtils.FMT_WMD.format(due), ".");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.FIN, EMsg.FINRfe02, "Course deadline", body.toString());
    }

    /**
     * Generates a report row for a FINRfe04 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFINRfe04(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        final LocalDate due = status.milestones.fin;

        body.add("It looks like you've already had a run at the Final Exam - you're almost ",
                "done!  The due date is ");

        if (due.equals(context.today)) {
            body.add("today");
        } else {
            body.add(TemporalUtils.FMT_WMD.format(due));
        }

        body.addln(".  You need to pass the Final Exam with a 16 or higher to complete the ",
                "course.  Just like with the Unit exams, you have unlimited tries, but there are no ",
                "restrictions like having to do something after two non-passing tries.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.FIN, EMsg.FINRfe04, "Course deadline", body.toString());
    }

    /**
     * Generates a report row for a FINRfe05 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFINRfe05(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 2) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to finish the Final exam before the course deadline...");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.FIN, EMsg.FINRfe05, "Course deadline", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a FINRfe06 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFINRfe06(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);
        final LocalDate due = status.milestones.fin;

        if (due.equals(context.today)) {
            body.addln("Another reminder about the Final exam that's due today.");
        } else {
            body.addln("Another reminder about the Final exam that's due ",
                    TemporalUtils.FMT_WMD.format(due), ".");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.FIN, EMsg.FINRfe06, "Course deadline", body.toString());
    }

    /**
     * Generates a report row for a FINXfe00 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFINXfe00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

        final LocalDate due = status.milestones.fin;

        body.add("It looks like you've already had a few tries at the Final Exam - you're almost done! ");

        if (due.equals(context.today)) {
            body.add("The due date is this evening.  ");
        } else {
            body.add("The due date is ", TemporalUtils.FMT_WMD.format(due), ".  ");
        }

        body.addln("You need to pass the Final Exam with a 16 or higher to complete the course.  ",
                "Just like with the Unit exams, you have unlimited tries, but there are no ",
                "restrictions like having to do something after two non-passing tries.");
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.FIN, EMsg.FINXfe00, "Course deadline", body.toString());
    }

    /**
     * Generates a report row for a FINXfe01 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFINXfe01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final MessageToSend result;

        if (status.daysSinceLastActivity > 2) {
            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Just a reminder to finish the Final exam before the course deadline...");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                            EMilestone.FIN, EMsg.FINXfe01, "Course deadline", body.toString());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a FINXfe02 message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    private static MessageToSend generateFINXfe02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        final HtmlBuilder body = MsgUtils.simpleOpening(context.student);
        final LocalDate due = status.milestones.fin;

        if (due.equals(context.today)) {
            body.addln("Another reminder about the Final exam that's due today.");
        } else {
            body.addln("Another reminder about the Final exam that's due ",
                    TemporalUtils.FMT_WMD.format(due), ".");
        }
        body.addln();

        MsgUtils.emitSimpleClosing(body);

        return new MessageToSend(context, status, Integer.valueOf(context.currentRegIndex + 1),
                EMilestone.FIN, EMsg.FINXfe02, "Course deadline", body.toString());
    }

}

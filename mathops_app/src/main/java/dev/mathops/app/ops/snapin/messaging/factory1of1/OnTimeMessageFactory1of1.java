package dev.mathops.app.ops.snapin.messaging.factory1of1;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.schema.legacy.RawStcourse;
import dev.mathops.app.ops.snapin.messaging.EMilestone;
import dev.mathops.app.ops.snapin.messaging.EMsg;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;
import dev.mathops.app.ops.snapin.messaging.MsgUtils;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;
import dev.mathops.text.builder.HtmlBuilder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * A factory class that can generate messages to students who are "on time" (urgency <= 0)
 */
enum OnTimeMessageFactory1of1 {
    ;

    /**
     * Generates an appropriate message.
     *
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    static MessageToSend generate(final MessagingContext context, final MessagingCourseStatus status) {

        final LocalDate today = context.today;
        LocalDate plusThree = today;
        for (int i = 0; i < 3; ++i) {
            if (plusThree.getDayOfWeek() == DayOfWeek.FRIDAY) {
                plusThree = plusThree.plusDays(3L);
            } else if (plusThree.getDayOfWeek() == DayOfWeek.SATURDAY) {
                plusThree = plusThree.plusDays(2L);
            } else {
                plusThree = plusThree.plusDays(1L);
            }
        }

        final MessageToSend result;

        if (status.passedFIN) {
            // NOTE: If student does not yet have 54, their "urgency" will not be zero, so the
            // "LateMessatgeFactory" class will generate the message, but just in case we get here,
            // don't do anything
            if (status.totalScore < 54) {
                Log.warning("On-time message factory called for student with less than 54");
                result = null;
            } else {
                if (status.totalScore < 62) {
                    // Grade is currently "C"
                    if (status.maxPossibleScore >= 65) {
                        // Can earn A or B
                        result = generateGRDCok00(context, status);
                    } else if (status.maxPossibleScore >= 62) {
                        // Can earn B
                        result = generateGRDCok01(context, status);
                    } else {
                        // Grade is C, and C is highest that can be earned
                        result = generateGRDCok02(context, status);
                    }
                } else if (status.totalScore < 65) {
                    if (status.maxPossibleScore >= 65) {
                        // Grade is B, can earn A
                        result = generateGRDBok00(context, status);
                    } else {
                        // Grade is B, and B is highest that can be earned
                        result = generateGRDBok01(context, status);
                    }
                } else {
                    // Grade is A
                    result = generateGRDAok00(context, status);
                }
            }
        } else if (status.passedUE4) {
            if (today.isAfter(status.milestones.fin)) {
                // The fact that the student is considered "on time" after the final deadline should
                // mean they have a "last try" available today - check to be sure, warn if not...
                if (status.lastTryAvailable) {
                    if (status.failedTriesOnFIN == 0) {
                        result = generateLASTfe00(context, status);
                    } else {
                        result = generateLASTfe01(context, status);
                    }
                } else {
                    // We'll assume a future run will generate a BLOCKED message
                    Log.warning(
                            "On-time message after final exam due date without last try available");
                    result = null;
                }
            } else if (plusThree.isAfter(status.milestones.fin)) {
                // Final is due soon!
                result = generateFINRok01(context, status);
            } else {
                // UE4 passed, but final is still a bit in the future
                result = generateFINRok00(context, status);
            }
        } else if (status.passedRE4) {
            // Passed RE4 but not UE4
            if (plusThree.isAfter(status.milestones.fin)) {
                result = generateUE4Rok00(context, status);
            } else {
                result = null;
            }
        } else if (status.passedUE3) {
            // Passed UE3 but not RE4
            if (plusThree.isAfter(status.milestones.r4)) {
                // RE4 is due soon!
                result = generateRE4Rok01(context, status);
            } else {
                // UE3 passed, but RE4 is still a bit in the future
                result = generateRE4Rok00(context, status);
            }
        } else if (status.passedRE3) {
            // Passed RE3 but not UE3
            if (plusThree.isAfter(status.milestones.r4)) {
                result = generateUE3Rok00(context, status);
            } else {
                result = null;
            }
        } else if (status.passedUE2) {
            // Passed UE2 but not RE3
            if (plusThree.isAfter(status.milestones.r3)) {
                // RE3 is due soon!
                result = generateRE3Rok01(context, status);
            } else {
                // UE2 passed, but RE3 is still a bit in the future
                result = generateRE3Rok00(context, status);
            }
        } else if (status.passedRE2) {
            // Passed RE2 but not UE2
            if (plusThree.isAfter(status.milestones.r3)) {
                result = generateUE2Rok00(context, status);
            } else {
                result = null;
            }
        } else if (status.passedUE1) {
            // Passed UE1 but not RE2
            if (plusThree.isAfter(status.milestones.r2)) {
                // RE2 is due soon!
                result = generateRE2Rok01(context, status);
            } else {
                // UE1 passed, but RE2 is still a bit in the future
                result = generateRE2Rok00(context, status);
            }
        } else if (status.passedRE1) {
            // Passed RE1 but not UE1
            if (plusThree.isAfter(status.milestones.r2)) {
                result = generateUE1Rok01(context, status);
            } else {
                result = generateUE1Rok00(context, status);
            }
        } else if (status.passedSR) {
            // Passed SR but not RE1
            if (plusThree.isAfter(status.milestones.r1)) {
                // RE1 is due soon!
                result = generateRE1Rok01(context, status);
            } else {
                // SR passed, but RE1 is still a bit in the future
                result = generateRE1Rok00(context, status);
            }
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Generates a report row for a "GRDCok00" message. This is called when the final exam has been passed and the
     * student has a C grade but could earn a B or A.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateGRDCok00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.GRDCok00, EMsg.GRDCok01, EMsg.GRDCok02)) {

            final String courseId = context.sortedRegs.getFirst().course;
            final String crsName = MsgUtils.courseName(courseId);

            final String subject = "Re-testing to increase score";

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            final String lastDateStr = TemporalUtils.FMT_MD.format(context.lastDayToTest);

            body.addln("Great job passing ", crsName, "!  Your point total will currently give ",
                    "you a C grade for the course.  You can still re-take Unit and Final exams if you ",
                    "want to try to increase your score.  You can re-test until the last class day of ",
                    "the semester (", lastDateStr, "), and we only count your highest score on each ",
                    "exam.");
            body.addln();

            body.addln(
                    "If you can get your point total to 62 points, your grade will become a B, and ",
                    "if you can get to 65, it will become an A.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.MAX,
                    EMsg.GRDCok00, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "**GRCok01" message. This is called when the final exam has been passed and the
     * student has a C grade but could earn a B but not an A.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateGRDCok01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.GRDCok00, EMsg.GRDCok01, EMsg.GRDCok02)) {

            final String courseId = context.sortedRegs.getFirst().course;
            final String crsName = MsgUtils.courseName(courseId);
            final String subject = "Re-testing to increase score";

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);
            final String lastDateStr = TemporalUtils.FMT_MD.format(context.lastDayToTest);

            body.addln("Great job passing ", crsName, "!  Your point total will currently give ",
                    "you a C grade for the course.  You can still re-take Unit and Final exams if you ",
                    "want to try to increase your score.  You can re-test until the last class day of ",
                    "the semester (", lastDateStr, "), and we only count your highest score on each ",
                    "exam.");
            body.addln();

            body.addln("If you can get your point total to 62 points, your grade will become a B.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.MAX,
                    EMsg.GRDCok01, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "**GRCok02" message. This is sent when the final exam has been passed and the
     * student has a C grade and cannot earn any higher grade.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateGRDCok02(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.GRDCok00, EMsg.GRDCok01, EMsg.GRDCok02)) {

            final String courseId = context.sortedRegs.getFirst().course;
            final String crsName = MsgUtils.courseName(courseId);
            final String subject = "Re-testing to increase score";

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Great job passing ", crsName, "!  Your point total gives you a C grade ",
                    "for the course.");
            body.addln();

            body.addln("You're all finished in the course - have a great rest of your semester!");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.MAX,
                    EMsg.GRDCok02, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "GRDBok00" message. This is sent when the final exam has been passed and the student
     * has a B grade but could earn an A.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateGRDBok00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.GRDBok00, EMsg.GRDBok01)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            final String courseId = context.sortedRegs.getFirst().course;
            final String crsName = MsgUtils.courseName(courseId);
            final String subject;

            final String lastDateStr = TemporalUtils.FMT_MD.format(context.lastDayToTest);

            if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.GRDCok00, EMsg.GRDCok01, EMsg.GRDCok02)) {
                subject = "Re-testing to increase score";

                body.addln("Great job passing ", crsName, "!  Your point total will currently ",
                        "give you a B grade for the course.  You can still re-take Unit and Final ",
                        "exams if you want to try to increase your score.  You can re-test until the ",
                        "last class day of the semester (", lastDateStr, "), and we only count your ",
                        "highest score on each exam.");
                body.addln();

                body.addln("If you can get your point total to 65, your grade will become an A.");
            } else {
                // Student got an earlier message when they had a "C" but they have improved that
                // score to "B" now - they can still get to the "A"...
                subject = "Grade improved to B";

                body.addln("You've raised your grade in ", crsName, " to a B - well done!  ",
                        "You can keep re-testing until ", lastDateStr, " to shoot for 65 points to ",
                        "get the A.");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.MAX,
                    EMsg.GRDBok00, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "GRDBok01" message. This is sent when the final exam has been passed and the student
     * has a B grade and cannot earn any higher grade.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateGRDBok01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.GRDBok00, EMsg.GRDBok01)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            final String courseId = context.sortedRegs.getFirst().course;
            final String crsName = MsgUtils.courseName(courseId);
            final String subject;

            if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.GRDCok00, EMsg.GRDCok01, EMsg.GRDCok02)) {

                subject = "Re-testing to increase score";

                body.addln("Great job passing ", crsName,
                        "!  Your point total gives you a B grade for the course.");
            } else {
                // Student got an earlier message when they had a "C" but they have improved that
                // score to "B" now - they can still get to the "A"...
                subject = "Grade improved to B";

                body.addln("You've raised your grade in ", crsName, " to a B - well done!");
            }
            body.addln();

            body.addln("You're all finished in the course - have a great rest of your semester!");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.MAX,
                    EMsg.GRDBok01, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "GRDAok00" message. This is sent when the final exam has been passed and the student
     * has an A grade.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateGRDAok00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.GRDAok00)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            final String courseId = context.sortedRegs.getFirst().course;
            final String crsName = MsgUtils.courseName(courseId);
            final String subject;

            if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.GRDCok00, EMsg.GRDCok01, EMsg.GRDCok02,
                    EMsg.GRDBok00, EMsg.GRDBok01)) {

                subject = crsName + " passed with an A";

                body.addln("Great job passing ", crsName, " with an A grade!");
            } else {
                // Student got an earlier message when they had a "B" or "C", but they have improved
                // that score to "A" now...
                subject = "Grade improved to A";

                body.addln("You've raised your grade in ", crsName, " to an A - excellent!");
            }
            body.addln();

            body.addln("You're all finished in the course - have a great rest of your semester!");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.MAX,
                    EMsg.GRDAok00, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "LASTfe00" message. This is sent when the final has not been attempted by its due
     * date, but a "last try" attempt is available today.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateLASTfe00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.LASTfe00, EMsg.LASTfe01)) {

            final RawStcourse reg = context.sortedRegs.getFirst();
            final String courseId = reg.course;
            final String crsName = MsgUtils.courseName(courseId);

            final String subject = "Last day to pass " + crsName + " Final Exam";

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("The deadline for the " + crsName + " Final Exam has passed, but because ",
                    "you were eligible to take that exam when it was due, you have a last chance on ",
                    "that exam today!");
            body.addln();

            if (!reg.sect.isEmpty() && reg.sect.charAt(0) == '0') {
                body.addln("If you are not able to pass the Final exam today, please stop in to ",
                        "the Precalculus Center office to talk about your options.");
            } else {
                body.addln("If you are not able to pass the Final exam today, please get back in ",
                        "touch to talk about your options.");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.F1,
                    EMsg.LASTfe00, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "LASTfe01" message. This is sent when the final has been attempted but not passed by
     * its due date, but a "last try" attempt is available today.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateLASTfe01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.LASTfe00, EMsg.LASTfe01)) {

            final RawStcourse reg = context.sortedRegs.getFirst();
            final String courseId = reg.course;
            final String crsName = MsgUtils.courseName(courseId);

            final String subject = "Last day to pass " + crsName + " Final Exam";

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("The deadline for the " + crsName + " Final Exam has passed, but because ",
                    "you were eligible to take that exam when it was due, you have a last chance on ",
                    "that exam today!");
            body.addln();

            if (!reg.sect.isEmpty() && reg.sect.charAt(0) == '0') {
                body.addln("If you are not able to pass the Final exam today, please stop in to ",
                        "the Precalculus Center office to talk about your options.");
            } else {
                body.addln("If you are not able to pass the Final exam today, please get back in ",
                        "touch to talk about your options.");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.F1,
                    EMsg.LASTfe01, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "FINRok00" message. This is sent when the Unit 4 exam has been passed, but the Final
     * exam due date is still some time in the future.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateFINRok00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.FINRok00)) {

            final String subject = "Congrats on Unit 4 exam";

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            if (status.ue4Score == 10) {
                body.addln("Congratulations on passing the Unit 4 exam with a perfect ",
                        "score - great job!  Just the Final Exam to go...");
            } else {
                body.addln("Congratulations on passing the Unit 4 exam!  Just the Final Exam ",
                        "to go...");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.FIN,
                    EMsg.FINRok00, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "FINRok01" message. This is sent when the Unit 4 exam has been passed, the Final
     * exam due date is 1-3 days in the future, but the Final exam has not yet been passed.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateFINRok01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.FINRok01, EMsg.UE4Rok00)) {

            final boolean noCongrat = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.FINRok00);

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            if (noCongrat) {
                if (status.ue4Score == 10) {
                    body.addln("Congratulations on passing the Unit 4 exam with a perfect ",
                            "score - great job!");
                } else {
                    body.addln("Congratulations on passing the Unit 4 exam!");
                }
                body.addln();
            }

            final String courseId = context.sortedRegs.getFirst().course;
            final String crsName = MsgUtils.courseName(courseId);
            final String subject;

            if (context.today.equals(status.milestones.fin)) {
                // The final exam deadline is TODAY
                subject = crsName + " Final exam deadline today";

                body.addln("The ", crsName, " course completion deadline is today - I just ",
                        "wanted to be sure you were aware.  You need to pass the Final exam (with ",
                        "a 16) today to complete the course.");
                body.addln();

                if (status.totalScore + 16 < 54) {
                    final String lastDateStr = TemporalUtils.FMT_MD.format(context.lastDayToTest);
                    body.addln("If you pass the Final exam today but don't have 54 points, you ",
                            "can keep re-taking exams to try to raise your score until the last day ",
                            "of classes (", lastDateStr, ").");
                    body.addln();
                }
            } else {
                // The final exam deadline is in the next 1-3 days
                subject = crsName + " Final exam deadline reminder";

                final LocalDate finalMs = status.milestones.fin;
                final String dateStr = TemporalUtils.FMT_WMD.format(finalMs);

                body.addln("The ", crsName, " course completion deadline (", dateStr,
                        ") is coming up - I wanted to make sure that date was on your radar.  You ",
                        "need to pass the Final exam (with a 16) by that date to complete the course.");
                body.addln();

                if (status.totalScore + 16 < 54) {
                    final String lastDateStr = TemporalUtils.FMT_MD.format(context.lastDayToTest);
                    body.addln("If you pass the Final exam by then, but don't have 54 points, you ",
                            "can keep re-taking exams to try to raise your score until the last day ",
                            "of classes (", lastDateStr, ").");
                    body.addln();
                }
            }

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.FIN,
                    EMsg.FINRok01, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "UE4Rok00" message. This is sent when the Unit 4 Review exam has been passed, the
     * Unit 4 Exam has not yet been passed, and the final exam deadline is soon.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateUE4Rok00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE4Rok00)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            final String courseId = context.sortedRegs.getFirst().course;
            final String crsName = MsgUtils.courseName(courseId);
            final String subject;

            if (context.today.equals(status.milestones.fin)) {
                subject = crsName + " Final exam deadline today";

                body.addln("The ", crsName, " course completion deadline is today - I just ",
                        "wanted to be sure you were aware.  You need to pass the Unit 4 exam and ",
                        "then pass the Final exam (with a 16) today to complete the course.");
                body.addln();

                if (status.totalScore + 24 < 54) {
                    final String lastDateStr = TemporalUtils.FMT_MD.format(context.lastDayToTest);
                    body.addln("If you pass both exams today, but don't have 54 points, you can ",
                            "keep re-taking exams to try to raise your score until the last day of ",
                            "classes (", lastDateStr, ").");
                    body.addln();
                }
            } else {
                subject = crsName + " Final exam deadline reminder";

                final LocalDate finalMs = status.milestones.fin;
                final String dateStr = TemporalUtils.FMT_WMD.format(finalMs);

                body.addln("The ", crsName, " course completion deadline (", dateStr,
                        ") is coming up - I wanted to make sure that date was on your radar.  You ",
                        "need to pass the Unit 4 Exam and the Final exam (with a 16) by the end of ",
                        "that day to complete the course.");
                body.addln();

                if (status.totalScore + 24 < 54) {
                    final String lastDateStr = TemporalUtils.FMT_MD.format(context.lastDayToTest);
                    body.addln("If you pass both exams by then, but don't have 54 points, you ",
                            "can keep re-taking exams to try to raise your score until the last day ",
                            "of classes (", lastDateStr, ").");
                    body.addln();
                }
            }

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.UE4,
                    EMsg.UE4Rok00, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "RE4Rok00" message. This is sent when RE4 has not yet been passed, and its due date
     * is some time in the future.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateRE4Rok00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        // We only message for a perfect score...
        if (status.ue3Score == 10 && MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE4Rok00, EMsg.RE4Rok01)) {

            final String subject = "Great job on Unit 3 exam";

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            body.addln("Great job earning a perfect score on the Unit 3 exam!");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.RE4,
                    EMsg.RE4Rok00, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "RE4Rok01" message. This is sent when the RE4 due date is 1-3 days in the future,
     * RE4 has not yet been passed.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateRE4Rok01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE4Rok01)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);
            final String subject = "Unit 4 Review due date";

            final boolean noCongrat = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE4Rok00);
            if (noCongrat && status.ue3Score == 10) {
                body.addln("Great job earning a perfect score on the Unit 3 exam!");
                body.addln();
            }

            if (context.today.equals(status.milestones.r4)) {
                body.addln("I just wanted to make sure you remembered that today is the due date ",
                        "for the Unit 4 Review Exam.");
            } else {
                final String dayName =
                        status.milestones.r4.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);

                body.addln("I just wanted to to make sure you remembered that the due date for ",
                        "the Unit 4 Review Exam is ", dayName, ".");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.RE4,
                    EMsg.RE4Rok01, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "UE3Rok00" message. This is sent when the Unit 3 Review exam has been passed, the
     * Unit 3 Exam has not yet been passed, and the deadline for the Unit 4 review is soon.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateUE3Rok00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE3Rok00)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            final String courseId = context.sortedRegs.getFirst().course;
            final String crsName = MsgUtils.courseName(courseId);
            final String subject;

            if (context.today.equals(status.milestones.r4)) {
                subject = crsName + " Unit 4 Review exam deadline today";

                body.addln("I just wanted to remind you that the Unit 4 Review exam is due ",
                        "today.");
            } else {
                subject = crsName + " Unit 4 Review exam deadline reminder";

                final String dateStr = TemporalUtils.FMT_WMD.format(status.milestones.r4);

                body.addln("I just wanted to remind you that the Unit 4 Review exam is due ",
                        dateStr, ".");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.UE3,
                    EMsg.UE3Rok00, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "RE3Rok00" message. This is sent when RE3 has not yet been passed, and its due date
     * is some time in the future.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateRE3Rok00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        // We only message for a perfect score...
        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE3Rok00, EMsg.RE3Rok01)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);
            final String subject;

            if (status.ue2Score == 10) {
                subject = "Great job on Unit 2 exam";

                body.addln("Great job earning a perfect score on the Unit 2 exam!");
                body.addln();

                body.addln("It seems like you're doing great - if there's anything you need, ",
                        "just let me know.");
            } else {
                subject = "Good job on Unit 2 exam";

                body.addln("I just wanted to touch base - it seems like you're doing great, but ",
                        "if there's anything you need, just let me know.");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.RE3,
                    EMsg.RE3Rok00, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "RE3Rok01" message. This is sent when the RE3 due date is 1-3 days in the future,
     * RE3 has not yet been passed.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateRE3Rok01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE3Rok01)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);
            final String subject = "Unit 3 Review due date";

            final boolean noCongrat = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE3Rok00);
            if (noCongrat && status.ue2Score == 10) {
                body.addln("Great job earning a perfect score on the Unit 2 exam!");
                body.addln();
            }

            if (context.today.equals(status.milestones.r3)) {
                body.addln("I just wanted to make sure you remembered that today is the due date ",
                        "for the Unit 3 Review Exam.  It seems like you're doing great in the course, ",
                        "but if there's anything you need, just let me know.");
            } else {
                final String dayName =
                        status.milestones.r3.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);

                body.addln("I just wanted to to make sure you remembered that the due date for ",
                        "the Unit 3 Review Exam is ", dayName, ".  It seems like you're doing great ",
                        "in the course, but if there's anything you need, just let me know.");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.RE3,
                    EMsg.RE3Rok01, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "UE2Rok00" message. This is sent when the Unit 2 Review exam has been passed, the
     * Unit 2 Exam has not yet been passed, and the deadline for the Unit 3 review is soon.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateUE2Rok00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE2Rok00)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            final String courseId = context.sortedRegs.getFirst().course;
            final String crsName = MsgUtils.courseName(courseId);
            final String subject;

            if (context.today.equals(status.milestones.r3)) {
                subject = crsName + " Unit 3 Review exam due today";

                body.addln("Just a quick check-in, and a reminder that the Unit 3 Review exam is ",
                        "due today.");
            } else {
                subject = crsName + " Unit 3 Review exam due date reminder";

                final String dateStr = TemporalUtils.FMT_WMD.format(status.milestones.r3);

                body.addln("Just a quick check-in, and a reminder that the Unit 3 Review exam is ",
                        "due ", dateStr, ".");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.UE2,
                    EMsg.UE2Rok00, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "RE2Rok00" message. This is sent when RE3 has not yet been passed, and its due date
     * is some time in the future.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateRE2Rok00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        // We only message for a perfect score...
        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE2Rok00, EMsg.RE2Rok01)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);
            final String subject;

            if (status.ue1Score == 10) {
                subject = "Great job on Unit 1 exam";

                body.addln("Great job scoring a 10 on the Unit 1 exam!");
                body.addln();
            } else {
                subject = "Good job on Unit 1 exam";
            }

            body.add("You can move on to Unit 2, and take the Unit 2 Review once all the ",
                    "objectives are complete.");

            if (status.ue1Score < 10) {
                body.add("  You will still be able to go back and re-take the Unit 1 exam if you ",
                        "want to increase your score - we only count the highest passing score you've ",
                        "earned for each exam, so there's no risk in re-taking.");
            }
            body.addln();
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.RE2,
                    EMsg.RE2Rok00, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "RE2Rok01" message. This is sent when the RE2 due date is 1-3 days in the future,
     * RE2 has not yet been passed.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateRE2Rok01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE2Rok01)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);
            final String subject = "Unit 2 Review due date";

            final boolean noCongrat = MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE2Rok00);
            if (noCongrat && status.ue1Score == 10) {
                body.addln("Great job scoring a 10 on the Unit 1 exam!");
                body.addln();
            }

            if (context.today.equals(status.milestones.r2)) {
                body.addln("The Unit 2 review exam is due today - as long as it's passed by ",
                        "midnight tonight (Mountain Time zone), you will get those 3 points.");
            } else {
                final String dayName =
                        status.milestones.r2.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);

                body.addln("The Unit 2 review exam is due ", dayName, " - as long as it's passed ",
                        "by midnight that evening (Mountain Time zone), you will get those 3 points.");
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.RE2,
                    EMsg.RE2Rok01, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "UE1Rok00" message. This is sent when the Unit 1 Review exam has been passed, the
     * Unit 1 Exam has not yet been passed, but the deadline for the Unit 2 review is not for a while.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateUE1Rok00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE1Rok00, EMsg.UE1Rok01)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            final String courseId = context.sortedRegs.getFirst().course;
            final String crsName = MsgUtils.courseName(courseId);
            final String subject;

            subject = crsName + " Unit 1 Exam available";

            body.addln("Good job getting the Unit 1 Review passed - that has unlocked the Unit 1 ",
                    "proctored exam.");
            body.addln();

            final boolean isLocal = !status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0'
                    && !status.isInSpecial("RAMWORK");

            if (isLocal) {
                body.addln("You can take that exam in the Precalculus Center (Weber 137) any ",
                        "time we're open - no need to schedule an appointmen.  All you need is your ",
                        "RamCard and something to write with, we have free lockers you can use to ",
                        "secure any other items during the exam.  We will provide an on-screen TI-84 ",
                        "calculator and scratch paper.");
            } else {
                body.addln("You can take that exam online over a webcam session any time - no ",
                        "need to schedule an appointment.  When you take the exam, please have your ",
                        "RamCard available to show to the camera.  You can have a personal ",
                        "calculator, something to write with, and blank scratch paper, but no other ",
                        "references.");
            }
            body.addln();

            body.addln("Once you pass the Unit exam with an 8 or higher, the Unit 2 Review exam ",
                    "can be unlocked by completing all the Unit 2 objectives.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.UE1,
                    EMsg.UE1Rok00, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "UE1Rok00" message. This is sent when the Unit 1 Review exam has been passed, the
     * Unit 1 Exam has not yet been passed, and the deadline for the Unit 2 review is soon.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateUE1Rok01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE1Rok01)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            final String courseId = context.sortedRegs.getFirst().course;
            final String crsName = MsgUtils.courseName(courseId);
            final String subject;

            if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.UE1Rok00)) {
                // We have not yet explained unit exams - do so.
                subject = crsName + " Unit 1 Exam available";

                body.addln("Good job getting the Unit 1 Review passed - that has unlocked the ",
                        "Unit 1 proctored exam.");
                body.addln();

                final boolean isLocal = !status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0'
                        && !status.isInSpecial("RAMWORK");

                if (isLocal) {
                    body.addln("You can take that exam in the Precalculus Center (Weber 137) any ",
                            "time we're open - no need to schedule an appointmen.  All you need is ",
                            "your RamCard and something to write with, we have free lockers you can ",
                            "use to secure any other items during the exam.  We will provide an ",
                            "on-screen TI-84 calculator and scratch paper.");
                } else {
                    body.addln("You can take that exam online over a webcam session any time - no ",
                            "need to schedule an appointment.  When you take the exam, please have ",
                            "your RamCard available to show to the camera.  You can have a personal ",
                            "calculator, something to write with, and blank scratch paper, but no ",
                            "other references.");
                }
                body.addln();

                body.addln(
                        "Once you pass the Unit exam with an 8 or higher, the Unit 2 Review exam ",
                        "can be unlocked by completing all the Unit 2 objectives.");
                body.addln();

                if (context.today.equals(status.milestones.r2)) {
                    body.addln("Also, just as a reminder, the Unit 2 Review exam is due this ",
                            "evening.");
                } else {
                    final String dayName = status.milestones.r2.getDayOfWeek()
                            .getDisplayName(TextStyle.FULL, Locale.US);

                    body.addln("Also, just as a reminder, the Unit 2 Review exam is due by ",
                            dayName, " evening.");
                }
            } else {
                // Student already had info on unit exams - just remind about due date
                if (context.today.equals(status.milestones.r2)) {
                    subject = crsName + " Unit 2 Review due today";

                    body.addln("I wanted to see how things were going, and to remind you that the ",
                            "Unit 2 Review exam is due by this evening.");
                } else {
                    subject = crsName + " Unit 2 Review due date";

                    final String dayName = status.milestones.r2.getDayOfWeek()
                            .getDisplayName(TextStyle.FULL, Locale.US);

                    body.addln("I wanted to see how things were going, and to remind you that the ",
                            "Unit 2 Review exam is due by ", dayName, " evening.");
                }
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.UE1,
                    EMsg.UE1Rok01, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "RE1Rok00" message. This is sent when RE1 has not yet been passed, and its due date
     * is some time in the future.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateRE1Rok00(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE1Rok00, EMsg.RE1Rok01)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);

            final String courseId = context.sortedRegs.getFirst().course;
            final String crsName = MsgUtils.courseName(courseId);
            final String subject = crsName + " Unit 1 is open";

            final String dateStr = TemporalUtils.FMT_WMD.format(status.milestones.r1);

            body.addln("It looks like you're moving along well in ", crsName, " - well done.");
            body.addln();

            // First experience with a Review Exam
            body.add("The Unit 1 content is open, and its Review exam is due ", dateStr, ".  This ",
                    "is like a preview of the proctored Unit Exam - it's ten questions, the same ",
                    "kinds of questions in the objective assignments.  Once you pass the Review Exam ",
                    "(with an 8 or higher), you can move on to the proctored Unit Exam ");

            final boolean isLocal = !status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0'
                    && !status.isInSpecial("RAMWORK");

            if (isLocal) {
                body.addln("(which you will take in the Precalculus Center).");
            } else {
                body.addln("(which you can take online over a webcam session).");
            }
            body.addln();

            body.addln("It's worth 3 points to get the Unit 1 Review exam passed before midnight ",
                    "on its due date.  If you don't pass by then, its not a big deal - just pass it ",
                    "as soon as you can and you can keep moving forward - you will just miss out on ",
                    "those 3 points.  Please let me know if you have any questions or need anything.");
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.RE1,
                    EMsg.RE1Rok00, subject, body.toString());
        }

        return result;
    }

    /**
     * Generates a report row for a "RE1Rok01" message. This is sent when the RE1 due date is 1-3 days in the future,
     * RE1 has not yet been passed.
     *
     * @param context the messaging context
     * @param status  the status
     * @return the report row
     */
    private static MessageToSend generateRE1Rok01(final MessagingContext context,
                                                  final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE1Rok01)) {

            final HtmlBuilder body = MsgUtils.simpleOpening(context.student);
            final String courseId = context.sortedRegs.getFirst().course;
            final String crsName = MsgUtils.courseName(courseId);
            final String subject;

            if (MsgUtils.hasNoMsgOfCodes(context.messages, EMsg.RE1Rok00)) {
                // We have not explained review/unit exams yet - do so...
                body.addln("It looks like you're moving along in ", crsName, " - well done.");
                body.addln();

                body.add("The Unit 1 content is open, and its Review exam is due ");

                if (context.today.equals(status.milestones.r1)) {
                    subject = crsName + " Unit 1 Review due today";

                    body.add("today!");
                } else {
                    subject = crsName + " Unit 1 Review due soon";

                    final String dayName = status.milestones.r1.getDayOfWeek()
                            .getDisplayName(TextStyle.FULL, Locale.US);

                    body.add("on ", dayName, ".");
                }

                // First experience with a Review Exam
                body.add("  The Review Exam is like a preview of the proctored Unit Exam - it's ",
                        "ten questions, the same kinds of questions in the objective assignments.  ",
                        "Once you pass the Review Exam (with an 8 or higher), you can move on to the ",
                        "proctored Unit Exam ");

                if (status.isInSpecial("RAMWORK")) {
                    body.addln(" (which you can take online over a wemcam).");
                } else if (!status.reg.sect.isEmpty() && status.reg.sect.charAt(0) == '0') {
                    body.addln(" (which you will take in the Precalculus Center).");
                } else {
                    body.addln(" (which you can take online over a webcam).");
                }
                body.addln();

                body.addln("It's worth 3 points to get the Unit 1 Review exam passed before ",
                        "midnight on its due date.  If you don't pass by then, its not a big deal - ",
                        "just pass it as soon as you can and you can keep moving forward - you will ",
                        "just miss out on those 3 points.  Please let me know if you have any ",
                        "questions or need anything.");
            } else {
                // Student got the earlier explanation of unit/reviews - just remind about RE1 date
                if (context.today.equals(status.milestones.r1)) {
                    subject = crsName + " Unit 1 Review due today";

                    body.addln("I just wanted to send a reminder that the Unit 1 Review exam is ",
                            "due this evening.  Please let me know if you have any questions or ",
                            "need anything.");
                } else {
                    subject = crsName + " Unit 1 Review due soon";

                    final String dayName = status.milestones.r1.getDayOfWeek()
                            .getDisplayName(TextStyle.FULL, Locale.US);

                    body.addln("I just wanted to send a reminder about the Unit 1 review exam ",
                            "due date on ", dayName, ".  Please let me know if you have any ",
                            "questions or need anything.");
                }
            }
            body.addln();

            MsgUtils.emitSimpleClosing(body);

            result = new MessageToSend(context, status, Integer.valueOf(1), EMilestone.RE1,
                    EMsg.RE1Rok01, subject, body.toString());
        }

        return result;
    }
}

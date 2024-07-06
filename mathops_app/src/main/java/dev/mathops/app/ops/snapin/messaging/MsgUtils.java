package dev.mathops.app.ops.snapin.messaging;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStmsg;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;

/**
 * The base class for message factories.
 */
public enum MsgUtils {
    ;

    /** A commonly used string. */
    private static final String COMMA_SPACE = ", ";

    /** A commonly used string. */
    private static final String AND = " and ";

    /** A commonly used integer. */
    public static final Integer ZERO = Integer.valueOf(0);

    /** A commonly used integer. */
    public static final Integer ONE = Integer.valueOf(1);

    /** A commonly used integer. */
    public static final Integer TWO = Integer.valueOf(2);

    /** A commonly used integer. */
    public static final Integer THREE = Integer.valueOf(3);

    /** A commonly used integer. */
    public static final Integer FOUR = Integer.valueOf(4);

    /** A commonly used integer. */
    public static final Integer FIVE = Integer.valueOf(5);


    /**
     * Creates the subject line for the welcome message.
     *
     * @param context the messaging context
     * @return the subject line
     */
    public static String buildWelcomeSubject(final MessagingContext context) {

        final RawStcourse firstReg = context.sortedRegs.getFirst();
        final HtmlBuilder subject = new HtmlBuilder(100);
        subject.add("Welcome to ");

        if (context.pace == 5) {
            subject.add(courseName(firstReg.course));
            subject.add(COMMA_SPACE);
            subject.add(courseName(context.sortedRegs.get(1).course));
            subject.add(COMMA_SPACE);
            subject.add(courseName(context.sortedRegs.get(2).course));
            subject.add(COMMA_SPACE);
            subject.add(courseName(context.sortedRegs.get(3).course));
            subject.add(AND);
            subject.add(courseName(context.sortedRegs.get(4).course));
        } else if (context.pace == 4) {
            subject.add(courseName(firstReg.course));
            subject.add(COMMA_SPACE);
            subject.add(courseName(context.sortedRegs.get(1).course));
            subject.add(COMMA_SPACE);
            subject.add(courseName(context.sortedRegs.get(2).course));
            subject.add(AND);
            subject.add(courseName(context.sortedRegs.get(3).course));
        } else if (context.pace == 3) {
            subject.add(courseName(firstReg.course));
            subject.add(COMMA_SPACE);
            subject.add(courseName(context.sortedRegs.get(1).course));
            subject.add(AND);
            subject.add(courseName(context.sortedRegs.get(2).course));
        } else if (context.pace == 2) {
            subject.add(courseName(firstReg.course));
            subject.add(AND);
            subject.add(courseName(context.sortedRegs.get(1).course));
        } else {
            subject.add(courseName(firstReg.course));
        }
        subject.add('.');

        return subject.toString();
    }

    /**
     * Generates the course name associated with a course.
     *
     * @param courseId the course ID
     * @return the course name, like "MATH 117"
     */
    public static String courseName(final String courseId) {

        return switch (courseId) {
            case RawRecordConstants.M117 -> "MATH 117";
            case RawRecordConstants.M118 -> "MATH 118";
            case RawRecordConstants.M124 -> "MATH 124";
            case RawRecordConstants.M125 -> "MATH 125";
            case RawRecordConstants.M126 -> "MATH 126";
            case null, default -> courseId;
        };
    }

    /**
     * Attempts to "sanitize" a name by removing trailing initials. For example, "Fadek A A H H B" becomes "Fadek".
     *
     * @param name the name to sanitize
     * @return the sanitized name
     */
    public static String sanitizeName(final String name) {

        String result = name;

        int len = result.length();
        while (len > 3 && result.charAt(len - 2) == ' '
                && Character.isLetter(result.charAt(len - 1))) {
            result = result.substring(0, len - 2);
            len = result.length();
        }

        return result;
    }

    /**
     * Tests if the student has not been sent any messages with any of a specified list of message codes.
     *
     * @param messages the list of messages that have been sent to the student
     * @param codes    the message codes
     * @return true if the student has not yet received a message with any of the given codes
     */
    public static boolean hasNoMsgOfCodes(final Iterable<RawStmsg> messages, final EMsg... codes) {

        boolean shouldSend = true;

        outer:
        for (final RawStmsg msg : messages) {
            if (codes != null) {
                for (final EMsg code : codes) {
                    if (code != null && code.name().equals(msg.msgCode)) {
                        shouldSend = false;
                        break outer;
                    }
                }
            }
        }

        return shouldSend;
    }

    /**
     * Returns the number of times a student has received a message with a code in a given list.
     *
     * @param messages the list of messages that have been sent to the student
     * @param codes    the message codes
     * @return the number of matching messages the student has been sent
     */
    public static int countMsgOfCodes(final Iterable<RawStmsg> messages, final EMsg... codes) {

        int count = 0;

        for (final RawStmsg msg : messages) {
            if (codes != null) {
                for (final EMsg code : codes) {
                    if (code != null && code.name().equals(msg.msgCode)) {
                        ++count;
                    }
                }
            }
        }

        return count;
    }

    /**
     * Finds the number of days since the last time a message with a given code has been sent.
     *
     * @param messages the list of messages that have been sent to the student
     * @param today    the current date
     * @param code     the message code
     * @return the number of days, {@code Integer.MAX_VALUE} if no message has been sent with the specified code
     */
    public static int daysSinceCode(final Iterable<RawStmsg> messages, final ChronoLocalDate today,
                                       final EMsg code) {

        LocalDate lastSent = null;

        for (final RawStmsg msg : messages) {
            if (code.name().equals(msg.msgCode)
                    && (lastSent == null || lastSent.isBefore(msg.msgDt))) {
                lastSent = msg.msgDt;
            }
        }

        int result;

        if (lastSent == null) {
            result = Integer.MAX_VALUE;
        } else {
            result = 0;
            while (lastSent.isBefore(today)) {
                lastSent = lastSent.plusDays(1L);
                ++result;
            }
        }

        return result;
    }

    /**
     * Emits the salutation and the opening boilerplate text for the welcome message.
     *
     * @param stu the student object
     * @return a new {@code HtmlBuilder} with the opening appended
     */
    public static HtmlBuilder simpleOpening(final RawStudent stu) {

        final HtmlBuilder body = new HtmlBuilder(1000);

        if (stu.prefName == null) {
            if (stu.firstName == null) {
                if (LocalTime.now().getHour() < 12) {
                    body.addln("Good morning,");
                } else {
                    body.addln("Good afternoon,");
                }
            } else {
                body.add("Hi ");
                body.add(sanitizeName(stu.firstName));
                body.addln(",");
            }
        } else {
            body.add("Hi ");
            body.add(sanitizeName(stu.prefName));
            body.addln(",");
        }
        body.addln();

        return body;
    }

    /**
     * Emits the closing boilerplate text for the welcome message.
     *
     * @param body the {@code HtmlBuilder} to which to emit content
     */
    public static void emitSimpleClosing(final HtmlBuilder body) {

        body.addln("Anita Pattison");
    }

    /**
     * Tests if the student has not been sent any messages for a specified milestone.
     *
     * @param messages the list of messages that have been sent to the student
     * @param ms       the milestone
     * @return true if the student has not yet received a message under the specified milestone
     */
    public static boolean hasNoMsgForMilestone(final Iterable<RawStmsg> messages, final EMilestone ms) {

        boolean shouldSend = true;

        for (final RawStmsg msg : messages) {
            if (msg.touchPoint.equals(ms.code)) {
                shouldSend = false;
                break;
            }
        }

        return shouldSend;
    }

    /**
     * Finds the date of the most recent message sent to a student, and calculates the number of weekdays that have
     * elapsed since then.
     *
     * @param context the messaging context
     * @return the date of the most recent message
     */
    static int latestMessageWeekdaysAgo(final MessagingContext context) {

        LocalDate latest = null;

        for (final RawStmsg msg : context.messages) {
            if (latest == null || latest.isBefore(msg.msgDt)) {
                latest = msg.msgDt;
            }
        }

        int daysAgo;

        if (latest == null) {
            daysAgo = 100;
        } else {
            final LocalDate today = context.today;

            daysAgo = 0;
            while (latest.isBefore(today)) {

                if (latest.getDayOfWeek() != DayOfWeek.SATURDAY
                        && latest.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    ++daysAgo;
                }

                latest = latest.plusDays(1L);
            }

        }

        return daysAgo;
    }
}

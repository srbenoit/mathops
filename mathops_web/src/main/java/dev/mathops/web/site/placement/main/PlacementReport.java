package dev.mathops.web.site.placement.main;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.PlacementStatus;
import dev.mathops.db.old.logic.mathplan.data.MathPlanConstants;
import dev.mathops.db.old.rawlogic.RawStmathplanLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Generates the content of the web page that displays the student's placement status.
 */
final class PlacementReport {

    /**
     * Private constructor to prevent direct instantiation.
     */
    private PlacementReport() {

        super();
    }

    /**
     * Creates the HTML of the placement report.
     *
     * @param cache       the data cache
     * @param status      the placement tool status for the student
     * @param session     the user's login session information
     * @param title       an optional title (heading) for the top of the page
     * @param includeLink {@code true} to include links to information on results and the page to take the exam
     *                    remotely
     * @param htm         the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    static void doPlacementReport(final Cache cache, final PlacementStatus status,
                                  final ImmutableSessionInfo session, final String title, final boolean includeLink,
                                  final HtmlBuilder htm) throws SQLException {

        if (title != null) {
            htm.sH(2).add(title).eH(2);
        }

        htm.sDiv("indent11");

        if (status.attemptsUsed == 0) {

            htm.sP("indent11", "style='padding-left:32px;'");
            htm.add("<img src='/images/orange2.png' style='margin:0 0 0 -32px; padding-right:10px;'/>");
            htm.addln(" You have not yet completed the Math Placement Tool.");
            htm.eP();

            htm.sP("indent11");
            htm.addln(" Visit our  <a href='welcome.html'>Math Placement Directory</a>");
            htm.addln(" for information on using the Math Placement Tool.");
            htm.eP();
        } else {
            appendYouHaveTaken(htm, status);

            htm.div("vgap");

            htm.addln("<fieldset>");
            htm.addln("<legend>Your best results so far:</legend>");

            // Display list of courses the student is qualified to register for
            boolean heading = true;
            boolean comma = false;
            boolean noneButAucc = true;

            for (final String course : status.clearedFor) {

                if (heading) {
                    heading = false;
                    htm.sP("indent11");
                    htm.addln(" <img src='/images/check.png'/> &nbsp; You are cleared to register for:");
                }

                if (comma) {
                    htm.add(", ");
                }

                comma = true;
                htm.add("<strong>", course, "</strong>");

                if (!("MATH 101".equals(course) || "MATH 105".equals(course) || "STAT 100".equals(course)
                      || "STAT 201".equals(course) || "STAT 204".equals(course))) {
                    noneButAucc = false;
                }
            }

            if (!heading) {
                htm.eP();
            }

            // Display list of courses the student has placed out of
            heading = true;
            comma = false;

            for (final String course : status.placedOutOf) {

                if (heading) {
                    heading = false;
                    htm.sP("indent11");
                    htm.addln(" <img src='/images/check.png'/> &nbsp; You have placed out of:");
                }

                if (comma) {
                    htm.add(", ");
                }

                comma = true;
                htm.add("<strong>", course, "</strong>");
                noneButAucc = false;
            }

            if (!heading) {
                htm.eP();
            }

            // Display list of courses the student has earned credit for
            heading = true;
            comma = false;

            for (final String course : status.earnedCreditFor) {

                if (heading) {
                    heading = false;
                    htm.sP("indent11");
                    htm.addln(" <img src='/images/check.png'/> &nbsp; You have earned placement credit for:");
                }

                if (comma) {
                    htm.add(", ");
                }

                comma = true;
                htm.add("<strong>", course, "</strong>");
                noneButAucc = false;
            }

            if (!heading) {
                htm.eP();
            }

            if (noneButAucc) {
                htm.addln("<p class='indent11' style='margin-bottom:0;margin-top:10pt;'>");
                htm.addln("<img style='position:relative; top:-1px' src='/images/error.png'/> &nbsp; ");

                htm.addln("MATH 101, MATH 105, STAT 100, STAT 201, and STAT 204 do not satisfy the degree requirements",
                        " for some majors.  Consult the <a href='https://www.catalog.colostate.edu/'>University  ",
                        "Catalog</a> to see if these courses are appropriate for your desired major.");
                htm.eP();

                htm.addln("<p class='indent11' style='margin-top:10pt;'>");
                htm.addln("<img style='position:relative; top:-1px' src='/images/info.png'/> &nbsp; ");
                htm.addln("<span class='blue'>What should I do now?</span>");
                htm.eP();

                if (status.attemptsRemaining > 0) {
                    htm.sP("indent33");
                    htm.add("To become eligible to register for additional mathematics courses, including MATH 117, ",
                            "MATH 120, or MATH 127, you should use the <a href='review.html' class='ulink'>",
                            "Math Placement Review materials</a> to study, and try the Math Placement Tool again.");
                    htm.eP();

                    htm.sP("indent33");
                    htm.add("Alternately, to become eligible to register for just MATH 117, MATH 120, or MATH 127, ",
                            "you may complete the ELM Tutorial and take the ELM Exam.");
                } else {
                    htm.sP("indent33");
                    htm.addln(" To become eligible to register for MATH 117, MATH 120, or MATH 127, you must ",
                            "complete the ELM Tutorial and take the ELM Exam.");
                }
                htm.eP();
            }

            // Only show adviser comment if the student is known to have an adviser
            final RawStudent student = RawStudentLogic.query(cache, session.getEffectiveUserId(), false);

            if (student != null && student.adviserEmail != null) {
                htm.div("vgap");
                htm.sP("indent11");
                htm.addln(" You should check with your adviser for complete information concerning your math ",
                        "requirements.");
                htm.eP();
            }

            if (includeLink) {
                htm.hr();
                htm.addln("<p style='text-align:center;'>");
                htm.addln(" Click <strong><a href='https://www.math.colostate.edu/placement/understand_MPE.shtml'>",
                        "here</a></strong> for information to help you understand your placement results.");
                htm.eP();
            }

            htm.addln("</fieldset>");
        }

        logStudentAccess(cache, session);

        htm.eDiv(); // indent11
    }

    /**
     * Appends a message describing the number of times the student has completed the Math Placement Tool in both
     * proctored and unproctored settings, and how many tries are available in each.
     *
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param status the student's placement status
     */
    private static void appendYouHaveTaken(final HtmlBuilder htm, final PlacementStatus status) {

        htm.sP("indent11", "style='padding-left:32px;'");
        htm.add("<img src='/images/orange2.png' style='margin:-2px 0 0 -32px; padding-right:10px;'/>");

        if (status.attemptsUsed == 0) {
            htm.addln(" You have not yet completed the Math Placement Tool.");
        } else {
            htm.add(" You have completed the Math Placement Tool ");
            appendNumTimes(htm, status.attemptsUsed);
            htm.add(". ");

            if (status.attemptsRemaining == 0) {
                htm.addln(" You have no attempts remaining.");
            } else if (status.attemptsRemaining == 1) {
                htm.addln(" You have 1 attempt remaining.");
            } else {
                htm.addln(" You have " + status.attemptsRemaining + " attempts remaining.");
            }
        }

        htm.eP();
    }

    /**
     * Appends a string telling the user how many times they have attempted the exam or how many attempts they may use.
     * Based on the number of times passed in, one of the following is appended to the {@code HtmlBuilder}:
     *
     * <pre>
     *   0 times
     *   1 time
     *   N times (where N is a number larger than 1)
     * </pre>
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param numTimes the number of times
     */
    private static void appendNumTimes(final HtmlBuilder htm, final int numTimes) {

        if (numTimes == 1) {
            htm.add("one time");
        } else {
            htm.add(Integer.toString(numTimes), " times");
        }
    }

    /**
     * Logs student access to the site - used to drive a checkmark in the welcome site as part of the placement
     * process.
     *
     * @param cache   the data cache
     * @param session the user session
     * @throws SQLException if there is an error accessing the database
     */
    private static void logStudentAccess(final Cache cache, final ImmutableSessionInfo session) throws SQLException {

        final String studentId = session.getEffectiveUserId();

        if (studentId != null && session.actAsUserId == null) {
            // If we don't have a record of this user checking their results, add one

            final List<RawStmathplan> responses = RawStmathplanLogic.queryLatestByStudentPage(cache, studentId,
                    MathPlanConstants.CHECKED_RESULTS_PROFILE);

            if (responses.isEmpty()) {
                final RawStudent stu = RawStudentLogic.query(cache, studentId, false);

                if (stu != null) {
                    final LocalDateTime when = session.getNow().toLocalDateTime();

                    final RawStmathplan log = new RawStmathplan(studentId, stu.pidm, null,
                            MathPlanConstants.CHECKED_RESULTS_PROFILE, when.toLocalDate(), Integer.valueOf(1), "Y",
                            Integer.valueOf(TemporalUtils.minuteOfDay(when)), Long.valueOf(session.loginSessionTag));

                    RawStmathplanLogic.INSTANCE.insert(cache, log);
                }
            }
        }
    }
}

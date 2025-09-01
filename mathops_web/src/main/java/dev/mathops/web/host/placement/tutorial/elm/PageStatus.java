
package dev.mathops.web.host.placement.tutorial.elm;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.tutorial.ELMTutorialStatus;
import dev.mathops.db.schema.RawRecordConstants;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.schema.legacy.RawCunit;
import dev.mathops.db.schema.legacy.RawCusection;
import dev.mathops.db.schema.legacy.RawExam;
import dev.mathops.db.schema.legacy.RawStexam;
import dev.mathops.db.rec.TermRec;
import dev.mathops.session.ExamWriter;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Generates the content of the web page that displays the student's status in the tutorial.
 */
enum PageStatus {
    ;

    /**
     * Displays the page that shows the student's current status is a course.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param status  the student status with respect to the ELM Tutorial
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final ElmTutorialSite site,
                             final ServletRequest req, final HttpServletResponse resp,
                             final ImmutableSessionInfo session, final ELMTutorialStatus status)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Entry Level Mathematics Tutorial",
                "/elm-tutorial/home.html", Page.ADMIN_BAR, null, false, true);

        htm.sDiv("menupanel");
        TutorialMenu.buildMenu(cache, session, status, htm);
        htm.sDiv("panel");

        doStatusContent(cache, status, session, htm);

        htm.eDiv(); // (end "panel" div)
        htm.eDiv(); // (end "menupanel" div)
        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }

    /**
     * Starts the page that shows the student's current status is a course.
     *
     * @param cache   the data cache
     * @param status  the student status with respect to the ELM Tutorial
     * @param session the user's login session information
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void doStatusContent(final Cache cache, final ELMTutorialStatus status,
                                        final ImmutableSessionInfo session, final HtmlBuilder htm) throws SQLException {

        htm.add("<h2 class='title' style='margin-bottom:3px;'>");
        final String name = status.student.getScreenName();
        htm.add("ELM Tutorial Status for ", name);
        htm.eH(2).div("vgap");

        final SystemData systemData = cache.getSystemData();
        final TermRec active = systemData.getActiveTerm();

        if (status.elmExamPassed) {
            htm.sDiv("indent22");
            htm.addln("Congratulations!  You have completed the ELM Tutorial and passed the <b>ELM Exam</b>.  This ",
                    "makes you eligible to register for <strong>MATH 117</strong> or <strong>MATH 120</strong>.");
        } else {
//            if (status.elm3Passed) {
//                htm.sDiv("indent22");
//                htm.addln("You have completed three units of the ELM Tutorial.  This makes you eligible to register ",
//                        "for the combination of <strong>MATH 116</strong> and <strong>MATH 117</strong>.");
//                htm.eDiv();
//
//                htm.eH(2).div("vgap2");
//            }

            htm.sDiv("indent22");
            htm.addln("To qualify for <strong>MATH 117</strong> or <strong>MATH 120</strong>, you must complete all ",
                    "four units of the ELM Tutorial and pass the ELM Exam.");
            htm.eDiv();

            htm.eH(2).div("vgap2");
            htm.sH(4).add("Tutorial Deadline:").eH(4);
            htm.sDiv("indent11");

            LocalDate deleteDate = systemData.getExamDeleteDate(RawRecordConstants.M100T, "1", active.term);

            if (deleteDate != null) {
                final LocalDate today = session.getNow().toLocalDate();

                if (deleteDate.isBefore(today)) {
                    // Current-term delete date is already in the past - need the subsequent term's delete date
                    final TermRec nextTerm = cache.getSystemData().getNextTerm();
                    final LocalDate deleteDate2 = systemData.getExamDeleteDate(RawRecordConstants.M100T, "1",
                            nextTerm.term);
                    if (deleteDate2 != null) {
                        deleteDate = deleteDate2;
                    }
                }

                htm.addln("If you do not earn a passing score on the proctored ELM Exam by the registration deadline ",
                        "for the next regular (Fall or Spring) semester, you will be required to start over with ",
                        "Unit 1 of the ELM Tutorial. The next deadline for completing the ELM Tutorial and passing ",
                        "the ELM Exam is <strong>", TemporalUtils.FMT_MDY.format(deleteDate), "</strong>.");
            }
        }
        htm.eDiv();

        htm.div("vgap").hr().div("vgap0");
        htm.sH(3).add("Tutorial Progress:").eH(3);

        for (int unit = 1; unit < 5; ++unit) {
            unitProgress(cache, status, Integer.valueOf(unit), active.term, htm);
        }
    }

    /**
     * Shows the progress of the user in one unit.
     *
     * @param cache     the data cache
     * @param status    the student status with respect to the ELM Tutorial
     * @param unit      the unit number
     * @param activeKey the active term key
     * @param htm       the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void unitProgress(final Cache cache, final ELMTutorialStatus status, final Integer unit,
                                     final TermKey activeKey, final HtmlBuilder htm) throws SQLException {

        htm.sDiv("indent11");
        htm.add("<strong> &bull; Unit ", unit, ":</strong>");
        htm.eDiv();

        final SystemData systemData = cache.getSystemData();
        final RawCunit cunit = systemData.getCourseUnit(RawRecordConstants.M100T, unit, activeKey);

        if (cunit == null) {
            htm.sP("red").add("Unable to look up course/unit data.").eP();
        } else {
            final RawCusection cusect = systemData.getCourseUnitSection(RawRecordConstants.M100T, "1", unit, activeKey);

            if (cusect == null) {
                htm.sP("red").add("Unable to look up course/unit/section data.").eP();
            } else {
                final String studentId = status.student.stuId;

                final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId, RawRecordConstants.M100T,
                        unit, false, "R", "U");

                showUnitProgress(cache, cunit, cusect, exams, htm);

                for (final RawStexam exam : exams) {
                    final String path = ExamWriter.makeWebExamPath(activeKey.shortString, studentId,
                            exam.serialNbr.longValue());

                    htm.sDiv("indent22");
                    htm.addln("<a class='ulink' href='see_past_exam.html?course=M%20100T&exam=", exam.version,
                            "&xml=", path, CoreConstants.SLASH, ExamWriter.EXAM_FILE, "&upd=", path,
                            CoreConstants.SLASH, ExamWriter.ANSWERS_FILE, "'>View the ", exam.getExamLabel(), "</a>");
                    htm.eDiv();
                }

                htm.div("vgap2");
            }
        }
    }

    /**
     * Shows the progress for an instructional unit.
     *
     * @param cache  the data cache
     * @param cunit  the course unit data
     * @param cusect the unit section data
     * @param exams  the student's exams (includes non-passed exams)
     * @param htm    the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void showUnitProgress(final Cache cache, final RawCunit cunit, final RawCusection cusect,
                                         final Iterable<RawStexam> exams, final HtmlBuilder htm)
            throws SQLException {

        final int unit = cunit.unit.intValue();

        final SystemData systemData = cache.getSystemData();

        // Gather the exam model and label for the needed exams
        final RawExam unitExam = systemData.getActiveExamByCourseUnitType(cunit.course, cunit.unit, "U");
        final String unitExamLabel = unitExam == null ? null : unitExam.buttonLabel;

        final RawExam unitRevExam = systemData.getActiveExamByCourseUnitType(cunit.course, cunit.unit, "R");
        final String unitRevExamLabel = unitRevExam == null ? null : unitRevExam.buttonLabel;

        // See if the student has a passing review exam on record and display the student's review
        // exam status
        boolean passedReview = false;
        if (unitRevExamLabel != null) {

            int reviewAttempts = 0;
            for (final RawStexam ste : exams) {
                if ("R".equals(ste.examType) && ste.unit.equals(cunit.unit)) {
                    ++reviewAttempts;
                    if ("Y".equals(ste.passed)) {
                        passedReview = true;
                    }
                }
            }

            htm.sDiv("indent22");
            if (reviewAttempts == 0) {
                htm.addln("You have <strong>not yet taken</strong> the <span class='green'>", unitRevExamLabel,
                        "</span>.");
            } else if (passedReview) {
                htm.addln("You have <strong>passed</strong> the <span class='green'>", unitRevExamLabel, "</span>.");
            } else {
                htm.addln("You have <strong>not yet passed</strong> the <span class='green'>", unitRevExamLabel,
                        "</span>.");
            }
            htm.eDiv();
        } else {
            // There appears to be no review exam for this unit, so consider it "passed"
            passedReview = true;
        }

        // If there is a passed review, or the review is not needed, show Unit exam status
        if (passedReview && unitExamLabel != null) {

            int unitAttempts = 0;
            int bestScore = 0;
            boolean passedUnit = false;
            for (final RawStexam ste : exams) {
                if ("U".equals(ste.examType) && ste.unit.equals(cunit.unit)) {
                    ++unitAttempts;
                    if ("Y".equals(ste.passed)) {
                        passedUnit = true;
                    }
                    if (ste.examScore != null && ste.examScore.intValue() > bestScore) {
                        bestScore = ste.examScore.intValue();
                    }
                }
            }

            if (unitAttempts > 0) {
                htm.sDiv("indent22");
                if (passedUnit) {
                    htm.addln("You have <strong>passed</strong> the ", unitExamLabel);
                } else {
                    htm.add("You have attempted the ", unitExamLabel, CoreConstants.SPC);
                    if (unitAttempts > 1) {
                        htm.addln(Integer.toString(unitAttempts), " times.");
                    } else {
                        htm.addln("1 time.");
                    }
                }
                htm.eDiv();

                htm.sDiv("indent22");
                htm.addln("Your best score on the ", unitExamLabel, ": <strong style='color:blue'>",
                        Integer.toString(bestScore), "</strong>");

                if (cunit.possibleScore != null && cunit.possibleScore.intValue() > 0) {
                    if (cusect.ueMasteryScore != null && cusect.ueMasteryScore.intValue() > 0) {
                        htm.addln("(out of <strong>", cunit.possibleScore,
                                "</strong> possible, minimum required score is <strong>", cusect.ueMasteryScore,
                                "</strong>).");
                    } else {
                        htm.addln("(out of <strong>", cunit.possibleScore, "</strong> possible).");
                    }
                }
                htm.eDiv();
            } else if (unit == 4) {
                htm.sDiv("indent22");

                // Note: we know at this point the review was passed...
                htm.addln(passedUnit ? "You have passed" : "You may take",
                        " the ELM Exam to become eligible for <strong>MATH 117</strong> or <strong>MATH 120</strong>.");
                htm.eDiv();
            }
        }
    }
}

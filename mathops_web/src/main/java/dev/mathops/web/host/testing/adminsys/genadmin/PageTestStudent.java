package dev.mathops.web.host.testing.adminsys.genadmin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.course.PaceTrackLogic;
import dev.mathops.db.logic.course.RegistrationsLogic;
import dev.mathops.db.rec.MasteryAttemptQaRec;
import dev.mathops.db.reclogic.MasteryAttemptQaLogic;
import dev.mathops.db.schema.RawRecordConstants;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStetextLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.schema.legacy.RawCsection;
import dev.mathops.db.schema.legacy.RawEtext;
import dev.mathops.db.schema.legacy.RawEtextCourse;
import dev.mathops.db.schema.legacy.RawExam;
import dev.mathops.db.schema.legacy.RawMpeCredit;
import dev.mathops.db.schema.legacy.RawSpecialStus;
import dev.mathops.db.schema.legacy.RawStcourse;
import dev.mathops.db.schema.legacy.RawStetext;
import dev.mathops.db.schema.legacy.RawStexam;
import dev.mathops.db.schema.legacy.RawSthomework;
import dev.mathops.db.schema.legacy.RawStmpe;
import dev.mathops.db.schema.legacy.RawStterm;
import dev.mathops.db.schema.legacy.RawStudent;
import dev.mathops.db.rec.AssignmentRec;
import dev.mathops.db.rec.MasteryAttemptRec;
import dev.mathops.db.rec.MasteryExamRec;
import dev.mathops.db.reclogic.MasteryAttemptLogic;
import dev.mathops.db.rec.TermRec;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.LiveSessionInfo;
import dev.mathops.session.sitelogic.bogus.ETextLogic;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.testing.adminsys.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Pages to the configuration of the test student.
 */
enum PageTestStudent {
    ;

    /** The list of placement attempts. */
    private static final String[] PTS = {"Placement Attempt 1", "Placement Attempt 2", "Placement Attempt 3"};

    /** The input keys for the placement attempts. */
    private static final String[] PT_KEYS = {"p1", "p2", "p3"};

    /** The placement outcomes. */
    private static final String[] PT_OUTCOMES = {RawRecordConstants.M100C, RawRecordConstants.M117,
            RawRecordConstants.M118, RawRecordConstants.M124, RawRecordConstants.M125, RawRecordConstants.M126};

    /** The input keys for the placement outcomes. */
    private static final String[] PT_OUTCOME_KEYS = {"C", "7", "8", "4", "5", "6"};

    /** The special student types. */
    private static final String[] SPECIAL = {RawSpecialStus.ATHLETE, RawSpecialStus.DCE, RawSpecialStus.DCEN,
            RawSpecialStus.ELM, RawSpecialStus.EMPLOY, RawSpecialStus.ENGRPLC, RawSpecialStus.ENGRSTU,
            RawSpecialStus.MANAGER, RawSpecialStus.MPT3, RawSpecialStus.M116, RawSpecialStus.M384,
            RawSpecialStus.ORIENTN, RawSpecialStus.PCT117, RawSpecialStus.PCT118, RawSpecialStus.PCT124,
            RawSpecialStus.PCT125, RawSpecialStus.PCT126, RawSpecialStus.PROCTOR, RawSpecialStus.RAMWORK,
            RawSpecialStus.RIUSEPU, RawSpecialStus.SKIP_UE, RawSpecialStus.STAFF, RawSpecialStus.TUTOR};

    /** The tutorial course IDs. */
    private static final String[] TUTORIALS = {RawRecordConstants.M100T, RawRecordConstants.M1170,
            RawRecordConstants.M1180, RawRecordConstants.M1240, RawRecordConstants.M1250, RawRecordConstants.M1260};

    /** A zero-length array used when creating other arrays. */
    private static final String[] ZERO_LEN_STR_ARRAY = new String[0];

    /**
     * Generates a page that shows the student's current configuration with controls to change that configuration.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doTestStudentsPage(final Cache cache, final AdminSite site,
                                   final ServletRequest req, final HttpServletResponse resp,
                                   final ImmutableSessionInfo session) throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

        GenAdminPage.emitNavBlock(EAdminTopic.TEST_STUDENTS, htm);

        htm.addln("<h1>Test Student</h1>");

        final RawStudent student = RawStudentLogic.query(cache, RawStudent.TEST_STUDENT_ID, false);

        // Script functions used within
        htm.addln("<script>");
        htm.addln(" function enable(id) {");
        htm.addln("  var e = document.getElementById(id);");
        htm.addln("  if (e) e.disabled=false;");
        htm.addln(" }");
        htm.addln(" function disable(id) {");
        htm.addln("  var e = document.getElementById(id);");
        htm.addln("  if (e) e.disabled=true;");
        htm.addln(" }");
        htm.addln(" function check(id) {");
        htm.addln("  var e = document.getElementById(id);");
        htm.addln("  if (e) e.checked=true;");
        htm.addln(" }");
        htm.addln(" function uncheck(id) {");
        htm.addln("  var e = document.getElementById(id);");
        htm.addln("  if (e) e.checked=false;");
        htm.addln(" }");
        htm.addln("</script>");

        htm.sDiv("indent11");

        if (student == null) {
            htm.addln("<span class='redred'>Test student record not found in database...</span>");
        } else {
            emitStudent(cache, htm, student);
            emitSpecials(cache, htm);
            emitPlacement(cache, htm);
            emitTutorials(cache, htm);
            emitRegistrations(cache, htm);
            emitEtexts(cache, htm);
        }

        htm.eDiv();

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits the portion of the test student configuration with student configuration.
     *
     * @param cache   the data cache
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param student the student model
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitStudent(final Cache cache, final HtmlBuilder htm,
                                    final RawStudent student) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();
        final String shortAct = active.term.shortString;

        htm.addln("<h3>Student Attributes <span class='dim'>(Student ID: ",
                RawStudent.TEST_STUDENT_ID, ")</span></h3>");
        htm.addln("<form action='teststu_update_student.html' method='post'>");

        htm.addln(" Appl.&nbsp;Term&nbsp;<select name='apln_term'>");
        htm.add("  <option id='term", shortAct, "'");
        if (student.aplnTerm != null && shortAct.equals(student.aplnTerm.shortString)) {
            htm.add(" selected='selected'");
        }
        htm.addln(" value='", shortAct, "'>",
                active.term.longString, "</option>");

        final List<TermRec> future = cache.getSystemData().getFutureTerms();
        for (final TermRec fut : future) {
            final String shortFut = fut.term.shortString;
            htm.add("  <option id='term", shortFut, "'");
            if (student.aplnTerm != null && shortFut.equals(student.aplnTerm.shortString)) {
                htm.add(" selected='selected'");
            }
            htm.addln(" value='", shortFut, "'>", fut.term.longString, "</option>");
        }
        htm.addln(" </select>&nbsp;&nbsp;");

        final Float limit = student.timelimitFactor;
        final double lim = limit == null ? 1.0 : limit.doubleValue();

        htm.addln(" Time&nbsp;limit&nbsp;<select name='timelimit'>");
        htm.addln("  <option id='tl10' value='10'", lim < 1.25 ? " selected='selected'" : CoreConstants.EMPTY,
                ">N/A</option>");
        htm.addln("  <option id='tl15' value='15'", lim >= 1.25 && lim < 1.75 ? " selected='selected'" :
                        CoreConstants.EMPTY,
                ">x 1.5</option>");
        htm.addln("  <option id='tl20' value='20'", lim >= 1.75 && lim < 2.25 ? " selected='selected'" :
                        CoreConstants.EMPTY,
                ">x 2.0</option>");
        htm.addln("  <option id='tl25' value='25'", lim >= 2.25 && lim < 2.75 ? " selected='selected'" :
                        CoreConstants.EMPTY,
                ">x 2.5</option>");
        htm.addln("  <option id='tl30' value='30'", lim >= 2.75 ? " selected='selected'" : CoreConstants.EMPTY,
                ">x 3.0</option>");
        htm.addln(" </select>&nbsp;&nbsp;");

        htm.add(" &nbsp;<input type='checkbox' id='lic' name='licensed'");
        if ("Y".equals(student.licensed)) {
            htm.add(" checked='checked'");
        }
        htm.addln(">&nbsp;<label for='lic'>Licensed</label>&nbsp;&nbsp;");

        htm.addln("ACT&nbsp;<input type='text' id='act' name='act_score' size='2'>&nbsp;&nbsp;");
        htm.addln("SAT&nbsp;<input type='text' id='sat' name='sat_score' size='3'></br>");
        htm.sDiv("gap").eDiv();

        htm.div("clear");
        htm.sDiv("right");
        htm.addln("<input type='submit' value='Update Student Information'>");
        htm.eDiv();
        htm.div("clear");
        htm.addln("</form>");

        // Emit a script to set field values (overriding form auto-fill)
        final String actualApln = student.aplnTerm == null ? null : student.aplnTerm.shortString;

        htm.addln("<script>");

        final Float factor = student.timelimitFactor;
        htm.addln("  document.getElementById(\"term", actualApln, "\").selected=true;");
        final double fact = factor == null ? 1.0 : factor.doubleValue();
        if (fact <= 1.0) {
            htm.addln("  document.getElementById(\"tl10\").selected=true;");
        } else if (fact <= 1.5) {
            htm.addln("  document.getElementById(\"tl15\").selected=true;");
        } else if (fact <= 2.0) {
            htm.addln("  document.getElementById(\"tl20\").selected=true;");
        } else if (fact <= 2.5) {
            htm.addln("  document.getElementById(\"tl25\").selected=true;");
        } else {
            htm.addln("  document.getElementById(\"tl30\").selected=true;");
        }

        htm.addln("  document.getElementById(\"lic\").checked=", "Y".equals(student.licensed) ? "true" : "false", ";");

        final Integer actScore = student.actScore;
        htm.addln("  document.getElementById(\"act\").value=\"",
                actScore == null ? CoreConstants.EMPTY : actScore.toString(), "\";");

        final Integer satScore = student.satScore;
        htm.addln("  document.getElementById(\"sat\").value=\"",
                satScore == null ? CoreConstants.EMPTY : satScore.toString(), "\";");

        htm.addln("</script>");

        htm.hr();
    }

    /**
     * Emits the portion of the test student configuration with special student configuration.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitSpecials(final Cache cache, final HtmlBuilder htm) throws SQLException {

        htm.addln("<h3>Special Student Categories</h3>");
        htm.addln("<form action='teststu_update_special.html' method='post'>");

        htm.addln("<table>");
        htm.add(" <tr>");
        int count = 0;
        for (final String spec : SPECIAL) {
            final String lower = spec.toLowerCase(Locale.ROOT);
            htm.add("<td><input type='checkbox' id='", lower,
                    "' name='", lower, "'>&nbsp;<label for='", lower,
                    "'>", spec, "</label></td>");
            count++;
            if (count == 6 || count == 12) {
                htm.addln(" </tr>");
                htm.add(" <tr>");
            }
        }
        htm.addln(" </tr>");
        htm.addln("</table>");

        htm.div("clear");
        htm.sDiv("right");
        htm.addln("<input type='submit' value='Update Special Student Categories'>");
        htm.eDiv();
        htm.div("clear");
        htm.addln("</form>");

        final List<RawSpecialStus> allSpec =
                RawSpecialStusLogic.queryByStudent(cache, RawStudent.TEST_STUDENT_ID);
        final Collection<String> types = new ArrayList<>(allSpec.size());
        for (final RawSpecialStus spec : allSpec) {
            types.add(spec.stuType);
        }

        htm.addln("<script>");
        for (final String spec : SPECIAL) {
            final String lower = spec.toLowerCase(Locale.ROOT);
            htm.addln("  document.getElementById(\"", lower, "\").checked=", types.contains(spec) ? "true" : "false",
                    ";");
        }
        htm.addln("</script>");

        htm.hr();
    }

    /**
     * Emits the portion of the test student configuration with placement exam configuration.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitPlacement(final Cache cache, final HtmlBuilder htm)
            throws SQLException {

        // Get existing placement attempts
        final List<RawStmpe> placements = RawStmpeLogic.queryLegalByStudent(cache, RawStudent.TEST_STUDENT_ID);
        placements.sort(new RawStmpe.FinishDateTimeComparator());

        final List<RawMpeCredit> results =
                RawMpeCreditLogic.queryByStudent(cache, RawStudent.TEST_STUDENT_ID);

        final int numPts = PTS.length;
        final RawStmpe[] attempts = new RawStmpe[numPts];

        int index = 0;
        for (final RawStmpe test : placements) {
            attempts[index] = test;
            ++index;
        }

        htm.addln("<h3>Placement Exam Status <span class='dim'>(N=None, P=Placed, C=Credit Earned)</span></h3>");
        htm.addln("<form action='teststu_update_placement.html' method='post'>");

        htm.addln(" <table>");

        // Header row showing the possible outcomes
        htm.addln("  <tr><th colspan='2'></th>");
        for (final String outcome : PT_OUTCOMES) {
            htm.addln("      <th class='blr' colspan='3'>", outcome, "</th> <th></th>");
        }
        htm.addln("  </tr>");

        // Sub-head with status in each outcome (None, Placed, Credit)
        htm.addln("  <tr><td colspan='2'></td>");
        final int numOutcomes = PT_OUTCOMES.length;
        for (int i = 0; i < numOutcomes; ++i) {
            htm.addln("      <td class='tightbl'>N</td>");
            htm.addln("      <td class='tight'>P</td>");
            if (i > 0) {
                htm.addln("      <td class='tightbr'>C</td>");
            } else {
                htm.addln("      <td class='tightbr'></td>");
            }
            htm.addln("      <td></td>");
        }
        htm.addln("  </tr>");

        // Present a row for each possible exam
        for (int i = 0; i < numPts; ++i) {
            htm.addln("  <tr>");

            htm.addln("   <td><input type='checkbox' id='", PT_KEYS[i], "' name='", PT_KEYS[i],
                    "' onClick='examClick(\"", PT_KEYS[i], "\");'></td>");
            htm.addln("   <td>", PTS[i], "</td>");

            // For each outcome, create three radio buttons
            for (int j = 0; j < numOutcomes; ++j) {
                final String name = PT_KEYS[i] + PT_OUTCOME_KEYS[j];
                htm.addln("   <td class='tightbl'><input type='radio' id='", name, "n' name='", name,
                        "' value='N'></td>");
                htm.addln("   <td class='tight'><input type='radio' id='", name, "p' name='", name,
                        "' value='P'></td>");
                if (i == 0 || j == 0) {
                    htm.addln("   <td class='tightbr'></td>");
                } else {
                    htm.addln("   <td class='tightbr'><input type='radio' id='", name, "c' name='", name,
                            "' value='C'></td>");
                }
                htm.addln("   <td></td>");
            }

            htm.addln("  </tr>");
        }

        htm.addln(" </table>");

        // Emit JavaScript to set controls to appropriate values (overriding form auto-fill)
        htm.addln("<script>");

        for (int i = 0; i < numPts; ++i) {
            htm.addln("  document.getElementById(\"", PT_KEYS[i], "\").checked=",
                    attempts[i] == null ? "false" : "true", ";");

            for (int j = 0; j < numOutcomes; ++j) {
                final String name = PT_KEYS[i] + PT_OUTCOME_KEYS[j];

                if (attempts[i] == null) {
                    htm.addln("  disable(\"", name, "n\");");
                    htm.addln("  disable(\"", name, "p\");");
                    htm.addln("  disable(\"", name, "c\");");
                    htm.addln("  uncheck(\"", name, "n\");");
                    htm.addln("  uncheck(\"", name, "p\");");
                    htm.addln("  uncheck(\"", name, "c\");");
                } else {
                    htm.addln("  enable(\"", name, "n\");");
                    htm.addln("  enable(\"", name, "p\");");
                    htm.addln("  enable(\"", name, "c\");");

                    int best = 0;
                    for (final RawMpeCredit result : results) {
                        if (result.course.equals(PT_OUTCOMES[j])) {
                            if (result.serialNbr == null) {
                                continue;
                            }

                            if (result.serialNbr.equals(attempts[i].serialNbr)) {

                                final String res = result.examPlaced;
                                if ("C".equals(res)) {
                                    best = 2;
                                    break;
                                } else if ("P".equals(res)) {
                                    best = 1;
                                }
                            }
                        }
                    }

                    htm.addln(best != 0 ? "  uncheck(\"" : "check(\"", name, "n\");");
                    htm.addln(best == 1 ? "check(\"" : "  uncheck(\"", name, "p\");");
                    htm.addln(best == 2 ? "check(\"" : "  uncheck(\"", name, "c\");");
                }
            }
        }
        htm.addln("</script>");

        // Emit JavaScript to handle checkbox clicks
        htm.addln("<script>");
        htm.addln(" function examClick(examKey) {");
        htm.addln("   if (document.getElementById(examKey).checked) {");
        for (int j = 0; j < numOutcomes; ++j) {
            htm.addln("     enable(examKey + \"", PT_OUTCOME_KEYS[j], "n\");");
            htm.addln("     enable(examKey + \"", PT_OUTCOME_KEYS[j], "p\");");
            htm.addln("     enable(examKey + \"", PT_OUTCOME_KEYS[j], "c\");");
            htm.addln("     check(examKey + \"", PT_OUTCOME_KEYS[j], "n\");");
        }
        htm.addln("   } else {");
        for (int j = 0; j < numOutcomes; ++j) {
            htm.addln("     disable(examKey + \"", PT_OUTCOME_KEYS[j], "n\");");
            htm.addln("     disable(examKey + \"", PT_OUTCOME_KEYS[j], "p\");");
            htm.addln("     disable(examKey + \"", PT_OUTCOME_KEYS[j], "c\");");
            htm.addln("     uncheck(examKey + \"", PT_OUTCOME_KEYS[j], "n\");");
            htm.addln("     uncheck(examKey + \"", PT_OUTCOME_KEYS[j], "p\");");
            htm.addln("     uncheck(examKey + \"", PT_OUTCOME_KEYS[j], "c\");");
        }
        htm.addln("   }");
        htm.addln(" }");
        htm.addln("</script>");

        htm.div("clear");
        htm.sDiv("right");
        htm.addln("<input type='submit' value='Update Placement Status'>");
        htm.eDiv();
        htm.div("clear");
        htm.addln("</form>");

        htm.hr();
    }

    /**
     * Emits the portion of the test student configuration with Tutorial configurations.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitTutorials(final Cache cache, final HtmlBuilder htm) throws SQLException {

        final Map<String, String[]> results = new HashMap<>(6);

        for (final String tutorial : TUTORIALS) {
            final List<RawStexam> exams = RawStexamLogic.getExams(cache, RawStudent.TEST_STUDENT_ID, tutorial, false,
                    RawStexamLogic.ALL_EXAM_TYPES);

            // Extract student's current status in each exam for the current tutorial
            final String[] tutResults = {"N", "N", "N", "N", "N", "N"};
            results.put(tutorial, tutResults);

            for (final RawStexam test : exams) {

                final int unit = test.unit.intValue();
                if (unit > 4) {
                    continue;
                }

                final String type = test.examType;

                if ("R".equals(type)) {
                    if ("Y".equals(test.passed)) {
                        tutResults[unit] = "P";
                    } else if ("N".equals(test.passed) && !"P".equals(tutResults[unit])) {
                        tutResults[unit] = "F";
                    }
                } else if ("U".equals(type) && unit == 4) {
                    if ("Y".equals(test.passed)) {
                        tutResults[5] = "P";
                    } else if ("N".equals(test.passed) && !"P".equals(tutResults[5])) {
                        tutResults[5] = "F";
                    }
                }
            }
        }

        htm.addln("<h3>Tutorial Status <span class='dim'>(N=None, F=Failed, P=Passed)</span></h3>");
        htm.addln("<form action='teststu_update_tutorial.html' method='post'>");

        htm.addln(" <table>");
        htm.addln("  <tr><td colspan='2'></td>");
        htm.addln("      <th colspan='3' class='blr'>Skills Rev.</th> <td></td>");
        htm.addln("      <th colspan='3' class='blr'>Review 1</th> <td></td>");
        htm.addln("      <th colspan='3' class='blr'>Review 2</th> <td></td>");
        htm.addln("      <th colspan='3' class='blr'>Review 3</th> <td></td>");
        htm.addln("      <th colspan='3' class='blr'>Review 4</th> <td></td>");
        htm.addln("      <th colspan='3' class='blr'>Tutorial Exam</th>");
        htm.addln("  </tr>");
        htm.addln("  <tr><td colspan='2'></td>");
        htm.addln("      <td class='tightbl'>N</td>");
        htm.addln("      <td class='tight'  >F</td>");
        htm.addln("      <td class='tightbr'>P</td><td> &nbsp; </td>");
        htm.addln("      <td class='tightbl'>N</td>");
        htm.addln("      <td class='tight'  >F</td>");
        htm.addln("      <td class='tightbr'>P</td><td> &nbsp; </td>");
        htm.addln("      <td class='tightbl'>N</td>");
        htm.addln("      <td class='tight'  >F</td>");
        htm.addln("      <td class='tightbr'>P</td><td> &nbsp; </td>");
        htm.addln("      <td class='tightbl'>N</td>");
        htm.addln("      <td class='tight'  >F</td>");
        htm.addln("      <td class='tightbr'>P</td><td> &nbsp; </td>");
        htm.addln("      <td class='tightbl'>N</td>");
        htm.addln("      <td class='tight'  >F</td>");
        htm.addln("      <td class='tightbr'>P</td><td> &nbsp; </td>");
        htm.addln("      <td class='tightbl'>N</td>");
        htm.addln("      <td class='tight'  >F</td>");
        htm.addln("      <td class='tightbr'>P</td>");
        htm.addln("  </tr>");

        for (final String tutorial : TUTORIALS) {
            final String key = tutorial.replace(CoreConstants.SPC, CoreConstants.EMPTY);

            htm.addln("  <tr><td>", tutorial,
                    "</td><td> &nbsp; </td>");

            if (RawRecordConstants.M100T.equals(tutorial)) {
                htm.addln("      <td class='tightbl'></td>");
                htm.addln("      <td class='tight'  ></td>");
                htm.addln("      <td class='tightbr'></td>");
            } else {
                htm.addln("      <td class='tightbl'><input type='radio' id='", key, "0N' name='", key,
                        "0' value='N'></td>");
                htm.addln("      <td class='tight'  ><input type='radio' id='", key, "0F' name='", key,
                        "0' value='F'></td>");
                htm.addln("      <td class='tightbr'><input type='radio' id='", key, "0P' name='", key,
                        "0' value='P'></td>");
            }
            htm.addln("      <td></td>");

            htm.addln("      <td class='tightbl'><input type='radio' id='", key,
                    "1N' name='", key, "1' value='N'></td>");
            htm.addln("      <td class='tight'  ><input type='radio' id='", key,
                    "1F' name='", key, "1' value='F'></td>");
            htm.addln("      <td class='tightbr'><input type='radio' id='", key,
                    "1P' name='", key, "1' value='P'></td>");
            htm.addln("      <td></td>");
            htm.addln("      <td class='tightbl'><input type='radio' id='", key,
                    "2N' name='", key, "2' value='N'></td>");
            htm.addln("      <td class='tight'  ><input type='radio' id='", key,
                    "2F' name='", key, "2' value='F'></td>");
            htm.addln("      <td class='tightbr'><input type='radio' id='", key,
                    "2P' name='", key, "2' value='P'></td>");
            htm.addln("      <td></td>");
            htm.addln("      <td class='tightbl'><input type='radio' id='", key,
                    "3N' name='", key, "3' value='N'></td>");
            htm.addln("      <td class='tight'  ><input type='radio' id='", key,
                    "3F' name='", key, "3' value='F'></td>");
            htm.addln("      <td class='tightbr'><input type='radio' id='", key,
                    "3P' name='", key, "3' value='P'></td>");
            htm.addln("      <td></td>");
            htm.addln("      <td class='tightbl'><input type='radio' id='", key,
                    "4N' name='", key, "4' value='N'></td>");
            htm.addln("      <td class='tight'  ><input type='radio' id='", key,
                    "4F' name='", key, "4' value='F'></td>");
            htm.addln("      <td class='tightbr'><input type='radio' id='", key,
                    "4P' name='", key, "4' value='P'></td>");
            htm.addln("      <td></td>");
            htm.addln("      <td class='tightbl'><input type='radio' id='", key,
                    "EN' name='", key, "E' value='N'></td>");
            htm.addln("      <td class='tight'  ><input type='radio' id='", key,
                    "EF' name='", key, "E' value='F'></td>");
            htm.addln("      <td class='tightbr'><input type='radio' id='", key,
                    "EP' name='", key, "E' value='P'></td>");
            htm.addln("      <td></td>");
            htm.addln("  </tr>");
        }

        htm.addln(" </table>");

        htm.div("clear");
        htm.sDiv("right");
        htm.addln("<input type='submit' value='Update Tutorial Status'>");
        htm.eDiv();
        htm.div("clear");
        htm.addln("</form>");

        // Emit JavaScript to set controls to appropriate values (overriding form auto-fill)
        htm.addln("<script>");

        // Set review exam states
        for (final String tutorial : TUTORIALS) {
            final String[] tutResults = results.get(tutorial);
            final String key = tutorial.replace(CoreConstants.SPC, CoreConstants.EMPTY);

            for (int i = 0; i < 5; ++i) {
                htm.addln("  check(\"", key, Integer.toString(i), tutResults[i], "\");");
            }
            htm.addln("  check(\"", key, "E", tutResults[5], "\");");
        }
        htm.addln("</script>");

        htm.hr();
    }

    /**
     * Emits the portion of the test student configuration with course registration configuration.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitRegistrations(final Cache cache, final HtmlBuilder htm) throws SQLException {

        final SystemData systemData = cache.getSystemData();
        final TermRec active = systemData.getActiveTerm();

        // Get existing registrations
        final List<RawStcourse> currentTermRegs = RawStcourseLogic.queryByStudent(cache, RawStudent.TEST_STUDENT_ID,
                active.term, true, false);

        // Get all non-bogus courses
        final List<RawCsection> sections = systemData.getCourseSections(active.term);

        // Extract a sorted set of course IDs that have non-bogus sections
        final Set<String> courseIds = new TreeSet<>();
        for (final RawCsection sect : sections) {
            if ("550".equals(sect.sect) || "Y".equals(sect.bogus)) {
                continue;
            }
            final String courseId = sect.course;
            if (!"M 384".equals(courseId)) {
                courseIds.add(courseId);
            }
        }
        final String[] courseIdArray = courseIds.toArray(ZERO_LEN_STR_ARRAY);
        final int numCourseIds = courseIdArray.length;

        final String[][] sectArray = new String[numCourseIds][];
        final Set<String> sorted = new TreeSet<>();

        // Sort sections and find the maximum number of applicable sections in any course
        int max = 0;
        for (int i = 0; i < numCourseIds; ++i) {

            for (final RawCsection sect : sections) {
                if ("550".equals(sect.sect) || "Y".equals(sect.bogus)) {
                    continue;
                }

                if (courseIdArray[i].equals(sect.course)) {
                    sorted.add(sect.sect);
                }
            }
            sectArray[i] = sorted.toArray(ZERO_LEN_STR_ARRAY);
            max = Math.max(max, sectArray[i].length);
            sorted.clear();
        }

        final RawStterm existingStterm = RawSttermLogic.query(cache, active.term, RawStudent.TEST_STUDENT_ID);

        if (existingStterm == null) {
            htm.addln("<h3>Course Registrations and Progress (no STTERM record)</h3>");
        } else {
            final RawStudent student = RawStudentLogic.query(cache, RawStudent.TEST_STUDENT_ID, false);

            htm.sH(3).add("Course Registrations and Progress (pace ", existingStterm.pace, " track ",
                    existingStterm.paceTrack);
            if (student == null || student.pacingStructure == null) {
                htm.add(", no pacing structure");
            } else {
                htm.add(" pacing structure ", student.pacingStructure);
            }
            htm.add(")").eH(3);
        }
        htm.addln("<form action='teststu_update_reg.html' method='post'>");

        htm.addln(" <table>");

        // One row per course: course ID, all sections, incomplete toggles
        final int span = max + 7;
        for (int i = 0; i < numCourseIds; ++i) {
            final String crs = courseIdArray[i].replace(' ', '_');

            htm.addln("<tr><td></td>");
            htm.addln("<td colspan='" + span + "' style='background:#007400;height:1px;'></td>");
            htm.addln("</tr>");
            htm.addln("<tr>");

            htm.addln("<td><input type='checkbox' id='", crs, "reg' name='", crs, "reg' onClick='regClick(\"", crs,
                    "\");'></td>");
            htm.addln("<td class='br'><strong><label for='", crs, "reg'>", courseIdArray[i], "</label></strong></td>");

            for (int j = 0; j < max; ++j) {
                htm.add("<td>");
                if (sectArray[i].length > j) {
                    final String id = crs + sectArray[i][j];
                    htm.add("<input type='radio' id='", id, "' name='", crs, "' value='", sectArray[i][j],
                            "'/><label for='", id, "'>", sectArray[i][j], "</label>");
                }
                htm.addln("</td>");
            }

            htm.add("   <td class='br'>");
            htm.addln("<input type='checkbox' id='", crs, "prereq' name='", crs, "prereq'/><label for='",
                    crs, "prereq'>", "Prereq</label>");
            htm.addln(" &nbsp;</td>");

            htm.add("   <td class='bl'>");
            htm.add("<input type='checkbox' id='", crs, "start' name='", crs, "start'/><label for='",
                    crs, "start'>", "Started</label>");
            htm.addln(" &nbsp;</td>");

            htm.add("   <td class='bl'>");
            htm.add("<input type='checkbox' id='", crs, "inc' name='", crs, "inc'/><label for='",
                    crs, "inc'>", "Inc.</label>");
            htm.addln(" &nbsp;</td>");

            htm.add("   <td>");
            htm.add("<input type='checkbox' id='", crs, "incip' name='", crs, "incip'/><label for='",
                    crs, "incip'>", "In&nbspProgress</label>");
            htm.addln(" &nbsp;</td>");

            htm.add("   <td class='br'>");
            htm.add("<input type='checkbox' id='", crs, "incc' name='", crs, "incc'/><label for='",
                    crs, "incc'>", "Counted</label>");
            htm.addln(" &nbsp;</td>");

            htm.add("   <td><span class='dim'> &nbsp;");
            boolean searching = true;
            for (final RawStcourse reg : currentTermRegs) {
                if (courseIdArray[i].equals(reg.course)) {
                    if (reg.paceOrder == null) {
                        htm.add("No Pace Order");
                    } else {
                        htm.add("Pace Order: ", reg.paceOrder);
                    }
                    searching = false;
                }
            }
            if (searching) {
                htm.add("Not registered");
            }
            htm.addln("</span></td>");
            htm.addln("  </tr>");

            // See if test student is registered in the course, and only emit coursework if so
            final String courseId = courseIdArray[i];
            final int len = sectArray[i].length;
            for (int j = 0; j < len; ++j) {
                final String sect = sectArray[i][j];
                for (final RawStcourse reg : currentTermRegs) {
                    if (courseId.equals(reg.course) && sect.equals(reg.sect)) {
                        emitCourseWork(cache, htm, reg, span);
                        break;
                    }
                }
            }
        }
        htm.addln(" </table>");

        htm.div("clear");
        htm.sDiv("right");
        htm.addln("<input type='submit' value='Update Registrations'>");
        htm.eDiv();
        htm.div("clear");
        htm.addln("</form>");

        // Emit JavaScript to set controls to appropriate values (overriding form auto-fill)
        htm.addln("<script>");

        for (int i = 0; i < numCourseIds; ++i) {
            final String courseId = courseIdArray[i];
            final String key = courseId.replace(' ', '_');

            boolean isReg = false;

            final int numSects = sectArray[i].length;
            for (int j = 0; j < numSects; ++j) {
                final String sect = sectArray[i][j];

                // See if test student is registered
                for (final RawStcourse reg : currentTermRegs) {
                    if (courseId.equals(reg.course) && sect.equals(reg.sect)) {

                        htm.addln("  check(\"", key, sect, "\");");

                        if ("Y".equals(reg.prereqSatis)) {
                            htm.addln("  check(\"", key, "prereq\");");
                        } else {
                            htm.addln("  uncheck(\"", key, "prereq\");");
                        }

                        if ("Y".equals(reg.openStatus)) {
                            htm.addln("  check(\"", key, "start\");");
                        } else {
                            htm.addln("  uncheck(\"", key, "start\");");
                        }

                        if ("Y".equals(reg.iInProgress)) {
                            htm.addln("  check(\"", key, "inc\");");
                            htm.addln("  check(\"", key, "incip\");");
                            if ("Y".equals(reg.iCounted)) {
                                htm.addln("  check(\"", key, "incc\");");
                            } else {
                                htm.addln("  uncheck(\"", key, "incc\");");
                            }
                        } else if ("Y".equals(reg.iCounted)) {
                            htm.addln("  check(\"", key, "inc\");");
                            htm.addln("  uncheck(\"", key, "incip\");");
                            htm.addln("  check(\"", key, "incc\");");
                        } else {
                            htm.addln("  uncheck(\"", key, "inc\");");
                            htm.addln("  uncheck(\"", key, "incip\");");
                            htm.addln("  uncheck(\"", key, "incc\");");
                        }

                        isReg = true;
                        break;
                    }
                }
            }

            if (isReg) {
                htm.addln("  check(\"", key, "reg\");");
                htm.addln("  enable(\"", key, "prereq\");");
                htm.addln("  enable(\"", key, "start\");");
                htm.addln("  enable(\"", key, "inc\");");
                htm.addln("  enable(\"", key, "incip\");");
                htm.addln("  enable(\"", key, "incc\");");
                for (final String sect : sorted) {
                    htm.addln("     enable(\"", key, sect, "\");");
                }
            } else {
                htm.addln("  uncheck(\"", key, "reg\");");
                htm.addln("  disable(\"", key, "prereq\");");
                htm.addln("  disable(\"", key, "start\");");
                htm.addln("  disable(\"", key, "inc\");");
                htm.addln("  disable(\"", key, "incip\");");
                htm.addln("  disable(\"", key, "incc\");");
                for (final String sect : sorted) {
                    htm.addln("     disable(\"", key, sect, "\");");
                }
            }

            for (int j = 0; j < max; ++j) {
                if (sectArray[i].length > j) {
                    htm.addln(isReg ? "  enable(\"" : "  disable(\"",
                            key, sectArray[i][j], "reg\");");
                }
            }

            // See if student has an e-text
            final LiveSessionInfo live = new LiveSessionInfo(CoreConstants.newId(ISessionManager.SESSION_ID_LEN),
                    "None", ERole.STUDENT);
            live.setUserInfo(RawStudent.TEST_STUDENT_ID, "Test", "Student", "Test Student");
            final ImmutableSessionInfo session = new ImmutableSessionInfo(live);

            final boolean etext = ETextLogic.canStudentAccessCourse(cache, session, courseId);

            if (etext) {
                htm.addln("  check(\"", key, "etext\");");
            } else {
                htm.addln("  uncheck(\"", key, "etext\");");
            }
        }
        htm.addln("</script>");

        // Emit JavaScript to handle checkbox clicks
        htm.addln("<script>");
        htm.addln(" function regClick(courseKey) {");
        htm.addln("   if (document.getElementById(courseKey +\"reg\").checked) {");
        htm.addln("     enable(courseKey + \"prereq\");");
        htm.addln("     enable(courseKey + \"start\");");
        htm.addln("     enable(courseKey + \"inc\");");
        htm.addln("     enable(courseKey + \"incc\");");
        htm.addln("     enable(courseKey + \"incip\");");
        for (final String sect : sorted) {
            htm.addln("     enable(courseKey + \"", sect, "\");");
        }
        htm.addln("   } else {");
        htm.addln("     disable(courseKey + \"prereq\");");
        htm.addln("     disable(courseKey + \"start\");");
        htm.addln("     disable(courseKey + \"inc\");");
        htm.addln("     disable(courseKey + \"incc\");");
        htm.addln("     disable(courseKey + \"incip\");");
        for (final String sect : sorted) {
            htm.addln("     disable(courseKey + \"", sect, "\");");
        }
        htm.addln("   }");
        htm.addln(" }");
        htm.addln("</script>");

        htm.hr();
    }

    /**
     * Emits checkboxes and radio buttons to configure what course work the student has done in the course.
     *
     * @param cache      the data cache
     * @param htm        the {@code HtmlBuilder} to which to append
     * @param reg        the registration
     * @param numColumns the number of columns in the table
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitCourseWork(final Cache cache, final HtmlBuilder htm,
                                       final RawStcourse reg, final int numColumns) throws SQLException {

        final SystemData systemData = cache.getSystemData();
        final TermRec active = systemData.getActiveTerm();
        final RawCsection csection = systemData.getCourseSection(reg.course, reg.sect, active.term);

        if ("MAS".equals(csection.gradingStd)) {
            emitCourseWorkNew(cache, htm, reg, numColumns);
        } else {
            emitCourseWorkOld(cache, htm, reg);
        }
    }

    /**
     * Emits checkboxes and radio buttons to configure what course work the student has done in the course.
     *
     * @param cache      the data cache
     * @param htm        the {@code HtmlBuilder} to which to append
     * @param reg        the registration
     * @param numColumns the number of columns in the table
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitCourseWorkNew(final Cache cache, final HtmlBuilder htm, final RawStcourse reg,
                                          final int numColumns) throws SQLException {

        final SystemData systemData = cache.getSystemData();

        final List<AssignmentRec> allAssignments = systemData.getActiveAssignmentsByCourseType(reg.course, "ST");
        final List<MasteryExamRec> allExams = systemData.getActiveMasteryExamsByCourse(reg.course);
        allExams.sort(null);

        final List<RawSthomework> allHw = RawSthomeworkLogic.getHomeworks(cache, RawStudent.TEST_STUDENT_ID,
                reg.course, false);

        final List<MasteryAttemptRec> allAttempts = MasteryAttemptLogic.INSTANCE.queryByStudent(cache, reg.stuId);

        htm.addln("  <tr><td></td>");
        htm.addln("    <td colspan='", Integer.valueOf(numColumns), "' style='background:#007400;height:1px;'></td>");
        htm.addln("  </tr>");

        for (int unit = 1; unit <= 8; ++unit) {
            emitUnitNew(htm, reg.course, unit, allAssignments, allExams, allHw, allAttempts, numColumns);
        }
    }

    /**
     * Emit the controls to set the homework and exam state for a unit.
     *
     * @param htm            the {@code HtmlBuilder} to which to append
     * @param courseId       the course ID
     * @param unit           the unit number
     * @param allAssignments all assignments configured in the course
     * @param allExams       all mastery exams configured in the course
     * @param allHw          all homeworks submitted by the student
     * @param allAttempts    all mastery attempts by the student
     * @param numColumns     the number of columns in the table
     */
    private static void emitUnitNew(final HtmlBuilder htm, final String courseId, final int unit,
                                    final Iterable<AssignmentRec> allAssignments,
                                    final Iterable<MasteryExamRec> allExams, final Collection<RawSthomework> allHw,
                                    final Iterable<MasteryAttemptRec> allAttempts, final int numColumns) {

        // Find the assignment IDs for the Skills Review and the 3 Learning Target assignments

        String srAssignId = null;
        String lt1AssignId = null;
        String lt2AssignId = null;
        String lt3AssignId = null;
        for (final AssignmentRec hw : allAssignments) {
            if (hw.unit.intValue() == unit && "ST".equals(hw.assignmentType)) {
                final int obj = hw.objective.intValue();

                if (obj == 0) {
                    srAssignId = hw.assignmentId;
                } else if (obj == 1) {
                    lt1AssignId = hw.assignmentId;
                } else if (obj == 2) {
                    lt2AssignId = hw.assignmentId;
                } else if (obj == 3) {
                    lt3AssignId = hw.assignmentId;
                }
            }
        }

        // Find the exam IDs for the three standard exams

        String lt1ExamId = null;
        String lt2ExamId = null;
        String lt3ExamId = null;
        for (final MasteryExamRec exam : allExams) {
            if (exam.unit.intValue() == unit && "ST".equals(exam.examType)) {
                final int obj = exam.objective.intValue();

                if (obj == 1) {
                    lt1ExamId = exam.examId;
                } else if (obj == 2) {
                    lt2ExamId = exam.examId;
                } else if (obj == 3) {
                    lt3ExamId = exam.examId;
                }
            }
        }

        // Determine whether the assignments have been attempted/mastered

        boolean triedSR = false;
        boolean passedSR = false;
        boolean triedHW1 = false;
        boolean passedHW1 = false;
        boolean triedHW2 = false;
        boolean passedHW2 = false;
        boolean triedHW3 = false;
        boolean passedHW3 = false;
        for (final RawSthomework attempt : allHw) {
            if (attempt.version.equals(srAssignId)) {
                if ("Y".equals(attempt.passed)) {
                    passedSR = true;
                }
                triedSR = true;
            } else if (attempt.version.equals(lt1AssignId)) {
                if ("Y".equals(attempt.passed)) {
                    passedHW1 = true;
                }
                triedHW1 = true;
            } else if (attempt.version.equals(lt2AssignId)) {
                if ("Y".equals(attempt.passed)) {
                    passedHW2 = true;
                }
                triedHW2 = true;
            } else if (attempt.version.equals(lt3AssignId)) {
                if ("Y".equals(attempt.passed)) {
                    passedHW3 = true;
                }
                triedHW3 = true;
            }
        }

        // Determine whether the exams have been attempted/mastered

        boolean triedU1 = false;
        boolean passedU1 = false;
        boolean triedU2 = false;
        boolean passedU2 = false;
        boolean triedU3 = false;
        boolean passedU3 = false;
        for (final MasteryAttemptRec attempt : allAttempts) {
            if (attempt.examId.equals(lt1ExamId)) {
                if ("Y".equals(attempt.passed)) {
                    passedU1 = true;
                }
                triedU1 = true;
            } else if (attempt.examId.equals(lt2ExamId)) {
                if ("Y".equals(attempt.passed)) {
                    passedU2 = true;
                }
                triedU2 = true;
            } else if (attempt.examId.equals(lt3ExamId)) {
                if ("Y".equals(attempt.passed)) {
                    passedU3 = true;
                }
                triedU3 = true;
            }
        }

        // Emit the row

        final String color1 = unit % 2 == 0 ? "#dfd" : "#ddf";
        final String color2 = unit % 2 == 0 ? "#efe" : "#eef";

        // Generate a key, like "M_125", and an ID, like "M_125_st6" for this units inputs
        final String key = courseId.replace(' ', '_');
        final String unitStr = Integer.toString(unit);
        final String id = key + "st" + unitStr;

        htm.addln("  <tr><td></td>");

        htm.addln("    <td style='background:", color1, ";'>Unit&nbsp;", unitStr, ":&nbsp;</td>");
        htm.add("    <td colspan='", Integer.toString(numColumns - 1), "' style='background:", color2, ";'>");

        // Assignment inputs have IDs like 'M_125st6_a1"

        htm.add("<label for='", id, "_sr'>SR</label> <select id='", id, "_sr' name='", id, "_sr'>");
        htm.add("  <option value='Y'", (passedSR ? " selected" : CoreConstants.EMPTY), ">Y</option>");
        htm.add("  <option value='N'", (triedSR && !passedSR ? " selected" : CoreConstants.EMPTY), ">N</option>");
        htm.add("  <option value='X'", (!triedSR && !passedSR ? " selected" : CoreConstants.EMPTY), ">-</option>");
        htm.addln("</select> ");

        htm.add("<label for='", id, "_a1'>HW 1</label> <select id='", id, "_a1' name='", id, "_a1'>");
        htm.add("  <option value='Y'", (passedHW1 ? " selected" : CoreConstants.EMPTY), ">Y</option>");
        htm.add("  <option value='N'", (triedHW1 && !passedHW1 ? " selected" : CoreConstants.EMPTY), ">N</option>");
        htm.add("  <option value='X'", (!triedHW1 && !passedHW1 ? " selected" : CoreConstants.EMPTY), ">-</option>");
        htm.addln("</select> ");

        htm.add("<label for='", id, "_a2'>HW 2</label> <select id='", id, "_a2' name='", id, "_a2'>");
        htm.add("  <option value='Y'", (passedHW2 ? " selected" : CoreConstants.EMPTY), ">Y</option>");
        htm.add("  <option value='N'", (triedHW2 && !passedHW2 ? " selected" : CoreConstants.EMPTY), ">N</option>");
        htm.add("  <option value='X'", (!triedHW2 && !passedHW2 ? " selected" : CoreConstants.EMPTY), ">-</option>");
        htm.addln("</select> ");

        htm.add("<label for='", id, "_a3'>HW 3</label> <select id='", id, "_a3' name='", id, "_a3'>");
        htm.add("  <option value='Y'", (passedHW3 ? " selected" : CoreConstants.EMPTY), ">Y</option>");
        htm.add("  <option value='N'", (triedHW3 && !passedHW3 ? " selected" : CoreConstants.EMPTY), ">N</option>");
        htm.add("  <option value='X'", (!triedHW3 && !passedHW3 ? " selected" : CoreConstants.EMPTY), ">-</option>");
        htm.addln("</select> &nbsp; ");

        // Mastery exam inputs have IDs like 'M_125st6_m1"

        htm.add("<label for='", id, "_m1'>Master 1</label> <select id='", id, "_m1' name='", id, "_m1'>");
        htm.add("  <option value='Y'", (passedU1 ? " selected" : CoreConstants.EMPTY), ">Y</option>");
        htm.add("  <option value='N'", (triedU1 && !passedU1 ? " selected" : CoreConstants.EMPTY), ">N</option>");
        htm.add("  <option value='X'", (!triedU1 && !passedU1 ? " selected" : CoreConstants.EMPTY), ">-</option>");
        htm.addln("</select> ");

        htm.add("<label for='", id, "_m2'>Master 2</label> <select id='", id, "_m2' name='", id, "_m2'>");
        htm.add("  <option value='Y'", (passedU2 ? " selected" : CoreConstants.EMPTY), ">Y</option>");
        htm.add("  <option value='N'", (triedU2 && !passedU2 ? " selected" : CoreConstants.EMPTY), ">N</option>");
        htm.add("  <option value='X'", (!triedU2 && !passedU2 ? " selected" : CoreConstants.EMPTY), ">-</option>");
        htm.addln("</select> ");

        htm.add("<label for='", id, "_m3'>Master 3</label> <select id='", id, "_m3' name='", id, "_m3'>");
        htm.add("  <option value='Y'", (passedU3 ? " selected" : CoreConstants.EMPTY), ">Y</option>");
        htm.add("  <option value='N'", (triedU3 && !passedU3 ? " selected" : CoreConstants.EMPTY), ">N</option>");
        htm.add("  <option value='X'", (!triedU3 && !passedU3 ? " selected" : CoreConstants.EMPTY), ">-</option>");
        htm.addln("</select> ");

        htm.addln("  </td></tr>");
    }

    /**
     * Emits checkboxes and radio buttons to configure what course work the student has done in the course.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param reg   the registration
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitCourseWorkOld(final Cache cache, final HtmlBuilder htm, final RawStcourse reg)
            throws SQLException {

        final String key = reg.course.replace(' ', '_');
        final String prefix = key.substring(key.length() - 2);

        final List<RawStexam> allExams = RawStexamLogic.getExams(cache, RawStudent.TEST_STUDENT_ID, reg.course, true);

        htm.addln("  <tr><td></td>");
        htm.addln("    <td colspan='9' style='background:#007400;height:1px;'></td>");
        htm.addln("  </tr>");

        final String srExamId = prefix + "GAT";
        boolean tried = false;
        boolean passed = false;
        for (final RawStexam test : allExams) {
            if (srExamId.equals(test.version)) {
                tried = true;
                if ("Y".equals(test.passed)) {
                    passed = true;
                }
            }
        }

        htm.addln("  <tr><td></td>");
        htm.addln("    <td style='background:#dfd;'>Skills: &nbsp;</td>");
        htm.addln("    <td colspan='8' style='background:#efe;'>");
        htm.addln("<select id='", key, "exams0' name='", key, "exams0'>");
        htm.add("  <option value='N'");
        if (!tried) {
            htm.add(" selected");
        }
        htm.addln(">Skills review exam not attempted</option>");
        htm.add("  <option value='F'");
        if (tried && !passed) {
            htm.add(" selected");
        }
        htm.addln(">Skills review exam not passed</option>");
        htm.add("  <option value='P'");
        if (tried && passed) {
            htm.add(" selected");
        }
        htm.addln(">Skills review exam passed</option>");
        htm.addln("</select>");
        htm.addln("  </td></tr>");

        emitUnitOld(cache, htm, reg.course, Long.valueOf(1L));
        emitUnitOld(cache, htm, reg.course, Long.valueOf(2L));
        emitUnitOld(cache, htm, reg.course, Long.valueOf(3L));
        emitUnitOld(cache, htm, reg.course, Long.valueOf(4L));

        final String feExamId = prefix + "FIN";
        boolean triedFe = false;
        boolean passedFe = false;
        for (final RawStexam test : allExams) {
            if (feExamId.equals(test.version)) {
                triedFe = true;
                if ("Y".equals(test.passed)) {
                    passedFe = true;
                }
            }
        }

        htm.addln("  <tr><td></td>");
        htm.addln("    <td style='background:#ddf;'>Final: &nbsp;</td>");
        htm.addln("    <td colspan='8' style='background:#eef;'>");
        htm.addln("<select id='", key, "exams5' name='", key, "exams5'>");
        htm.add("  <option value='N'");
        if (!triedFe) {
            htm.add(" selected='selected'");
        }
        htm.addln(">Final exam not attempted</option>");
        htm.add("  <option value='F'");
        if (triedFe && !passedFe) {
            htm.add(" selected");
        }
        htm.addln(">Final exam not passed</option>");
        htm.add("  <option value='P'");
        if (tried && passedFe) {
            htm.add(" selected");
        }
        htm.addln(">Final exam passed</option>");
        htm.addln("</select>");
        htm.addln("  </td></tr>");

        htm.addln("  <tr><td></td>");
        htm.addln("    <td colspan='9' style='background:#007400;height:1px;'></td>");
        htm.addln("  </tr>");
        htm.addln("  <tr>");
        htm.addln("    <td colspan='10' style='background:#fff;height:0.7em;'></td>");
        htm.addln("  </tr>");

        // Build the script that will set the checkbox and select states
        final List<RawSthomework> allHw =
                RawSthomeworkLogic.queryByStudent(cache, RawStudent.TEST_STUDENT_ID, false);

        htm.addln("<script>");
        if (hasHw(allHw, prefix + "11H")) {
            htm.addln("  check(\"", key, "hw11\");");
        }
        if (hasHw(allHw, prefix + "12H")) {
            htm.addln("  check(\"", key, "hw12\");");
        }
        if (hasHw(allHw, prefix + "13H")) {
            htm.addln("  check(\"", key, "hw13\");");
        }
        if (hasHw(allHw, prefix + "14H")) {
            htm.addln("  check(\"", key, "hw14\");");
        }
        if (hasHw(allHw, prefix + "15H")) {
            htm.addln("  check(\"", key, "hw15\");");
        }

        if (hasHw(allHw, prefix + "21H")) {
            htm.addln("  check(\"", key, "hw21\");");
        }
        if (hasHw(allHw, prefix + "22H")) {
            htm.addln("  check(\"", key, "hw22\");");
        }
        if (hasHw(allHw, prefix + "23H")) {
            htm.addln("  check(\"", key, "hw23\");");
        }
        if (hasHw(allHw, prefix + "24H")) {
            htm.addln("  check(\"", key, "hw24\");");
        }
        if (hasHw(allHw, prefix + "25H")) {
            htm.addln("  check(\"", key, "hw25\");");
        }

        if (hasHw(allHw, prefix + "31H")) {
            htm.addln("  check(\"", key, "hw31\");");
        }
        if (hasHw(allHw, prefix + "32H")) {
            htm.addln("  check(\"", key, "hw32\");");
        }
        if (hasHw(allHw, prefix + "33H")) {
            htm.addln("  check(\"", key, "hw33\");");
        }
        if (hasHw(allHw, prefix + "34H")) {
            htm.addln("  check(\"", key, "hw34\");");
        }
        if (hasHw(allHw, prefix + "35H")) {
            htm.addln("  check(\"", key, "hw35\");");
        }

        if (hasHw(allHw, prefix + "41H")) {
            htm.addln("  check(\"", key, "hw41\");");
        }
        if (hasHw(allHw, prefix + "42H")) {
            htm.addln("  check(\"", key, "hw42\");");
        }
        if (hasHw(allHw, prefix + "43H")) {
            htm.addln("  check(\"", key, "hw43\");");
        }
        if (hasHw(allHw, prefix + "44H")) {
            htm.addln("  check(\"", key, "hw44\");");
        }
        if (hasHw(allHw, prefix + "45H")) {
            htm.addln("  check(\"", key, "hw45\");");
        }
        htm.addln("</script>");
    }

    /**
     * Emit the controls to set the homework and exam state for a unit.
     *
     * @param cache    the data cache
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param courseId the course ID
     * @param unit     the unit number
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitUnitOld(final Cache cache, final HtmlBuilder htm, final String courseId,
                                    final Long unit) throws SQLException {

        final List<RawStexam> allExams = RawStexamLogic.getExams(cache, RawStudent.TEST_STUDENT_ID, courseId, true);

        final String key = courseId.replace(' ', '_');
        final String prefix = key.substring(key.length() - 2);

        final String reExamId = prefix + unit + "RE";
        final String ueExamId = prefix + unit + "UE";

        boolean triedR1 = false;
        boolean passedR1 = false;
        boolean triedR2 = false;
        boolean passedR2 = false;
        boolean triedU1 = false;
        boolean passedU1 = false;
        boolean triedU2 = false;
        boolean passedU2 = false;
        boolean passedU3 = false;

        for (final RawStexam exam : allExams) {
            if (reExamId.equals(exam.version)) {
                if (triedR1 && triedU1 && triedU2) {
                    // This is R2
                    triedR2 = true;
                    if ("Y".equals(exam.passed)) {
                        passedR2 = true;
                    }
                } else {
                    triedR1 = true;
                    if ("Y".equals(exam.passed)) {
                        passedR1 = true;
                    }
                }
            } else if (passedR1 && ueExamId.equals(exam.version)) {
                if (triedU1) {
                    // This is U2
                    if (triedU2) {
                        // This is U3
                        if ("Y".equals(exam.passed)) {
                            passedU3 = true;
                        }
                    } else {
                        triedU2 = true;
                        if ("Y".equals(exam.passed)) {
                            passedU2 = true;
                        }
                    }
                } else {
                    triedU1 = true;
                    if ("Y".equals(exam.passed)) {
                        passedU1 = true;
                    }
                }
            }
        }

        final String color1 = unit.intValue() % 2 == 0 ? "#dfd" : "#ddf";
        final String color2 = unit.intValue() % 2 == 0 ? "#efe" : "#eef";

        htm.addln("  <tr><td></td>");

        final String id = key + "hw" + unit;

        htm.addln("    <td style='background:", color1, ";'>Unit ", unit, ": &nbsp;</td>");
        htm.addln("    <td colspan='8' style='background:", color2, ";'>");
        htm.addln("<input type='checkbox' id='", id, "1' name='", id, "1'><label for='", id, "1'>", unit,
                ".1</label> &nbsp;");
        htm.addln("<input type='checkbox' id='", id, "2' name='", id, "2'><label for='", id, "2'>", unit,
                ".2</label> &nbsp;");
        htm.addln("<input type='checkbox' id='", id, "3' name='", id, "3'><label for='", id, "3'>", unit,
                ".3</label> &nbsp;");
        htm.addln("<input type='checkbox' id='", id, "4' name='", id, "4'><label for='", id, "4'>", unit,
                ".4</label> &nbsp;");
        htm.addln("<input type='checkbox' id='", id, "5' name='", id, "5'><label for='", id, "5'>", unit,
                ".5</label> &nbsp;");

        htm.addln("<select id='", key, "exams", unit, "' name='", key, "exams", unit, "'>");

        htm.add("  <option value='N'");
        if (!triedR1) {
            htm.add(" selected='selected'");
        }
        htm.addln(">No exams taken</option>");

        htm.add("  <option value='F'");
        if (triedR1 && !passedR1) {
            htm.add(" selected='selected'");
        }
        htm.addln(">Failed RE</option>");

        htm.add("  <option value='P'");
        if (passedR1 && !triedU1) {
            htm.add(" selected='selected'");
        }
        htm.addln(">Passed RE</option>");

        htm.add("  <option value='PF'");
        if (triedU1 && !passedU1 && !triedU2) {
            htm.add(" selected='selected'");
        }
        htm.addln(">Passed RE, 1 Failed UE</option>");

        htm.add("  <option value='PFF'");
        if (triedU2 && !passedU2) {
            htm.add(" selected='selected'");
        }
        htm.addln(">Passed RE, 2 Failed UE</option>");

        htm.add("  <option value='PP'");
        if (passedU1) {
            htm.add(" selected='selected'");
        }
        htm.addln(">Passed RE, Passed UE</option>");

        htm.add("  <option value='PFFF'");
        if (triedR2 && !passedR2) {
            htm.add(" selected='selected'");
        }
        htm.addln(">Passed RE, 2 Failed UE, Failed RE</option>");

        htm.add("  <option value='PFFP'");
        if (triedR2 && passedR2) {
            htm.add(" selected='selected'");
        }
        htm.addln(">Passed RE, 2 Failed UE, Passed RE</option>");

        htm.add("  <option value='PFFPP'");
        if (passedU3) {
            htm.add(" selected='selected'");
        }
        htm.addln(">Passed RE, 2 Failed UE, Passed RE, Passed UE</option>");

        htm.addln("</select>");
        htm.addln("  </td></tr>");
    }

    /**
     * Tests whether a homework exists on the student record.
     *
     * @param allHw the list of all student homeworks
     * @param hwId  the ID for which to search
     * @return {@code true} if the homework has been done
     */
    private static boolean hasHw(final Iterable<RawSthomework> allHw, final String hwId) {

        boolean hit = false;

        for (final RawSthomework hw : allHw) {
            if (hwId.equals(hw.version)) {
                hit = true;
                break;
            }
        }

        return hit;
    }

    /**
     * Emits the portion of the test student configuration with e-texts.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitEtexts(final Cache cache, final HtmlBuilder htm) throws SQLException {

        htm.addln("<h3>E-Texts</h3>");
        htm.addln("<form action='teststu_update_etext.html' method='post'>");

        htm.addln(" <table>");
        htm.addln("  <tr>");
        htm.addln("  <tr><th colspan='2'>E-Text</th>");
        htm.addln("      <th class='blr'>Course(s)</th> <td></td>");
        htm.addln("      <th class='blr'>Status</th>");
        htm.addln("  </tr>");

        final SystemData systemData = cache.getSystemData();

        final List<RawEtext> allEtexts = systemData.getETexts();
        final List<RawStetext> stEtexts = RawStetextLogic.queryByStudent(cache, RawStudent.TEST_STUDENT_ID);

        // Emit active rows first, then inactive rows
        for (final RawEtext etext : allEtexts) {
            if ("Y".equals(etext.active)) {
                emitEtextRow(cache, htm, etext, stEtexts);
            }
        }
        for (final RawEtext etext : allEtexts) {
            if ("N".equals(etext.active)) {
                emitEtextRow(cache, htm, etext, stEtexts);
            }
        }

        htm.addln(" </table>");
        htm.div("clear");
        htm.sDiv("right");
        htm.addln("<input type='submit' value='Update E-Text Records'>");
        htm.eDiv();
        htm.div("clear");
        htm.addln("</form>");

        htm.hr();
    }

    /**
     * Emits a single row in the e-text record.
     *
     * @param cache    the data cache
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param etext    the e-text record
     * @param stEtexts the list of student e-texts
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitEtextRow(final Cache cache, final HtmlBuilder htm, final RawEtext etext,
                                     final Iterable<RawStetext> stEtexts) throws SQLException {

        final String etextId = etext.etextId;

        // emit the e-text ID
        if ("Y".equals(etext.active)) {
            htm.addln("  <tr><td>", etextId, "</td><td> &nbsp; </td>");
        } else {
            htm.addln("  <tr><td>", etextId, " (inactive)</td><td> &nbsp; </td>");
        }

        // emit the courses that e-text grants access to

        final SystemData systemData = cache.getSystemData();

        final List<RawEtextCourse> etc = systemData.getETextCoursesByETextId(etextId);
        final int numEtc = etc.size();
        final HtmlBuilder builder = new HtmlBuilder(10 * numEtc);
        for (int j = 0; j < numEtc; ++j) {
            if (j > 0) {
                builder.add(", ");
            }
            builder.add(etc.get(j).course);
        }

        htm.addln("  <td class='blr'>", builder.toString(), "</td><td> &nbsp; </td>");

        // emit the student's status with respect to the e-text
        int numActive = 0;
        int numRefunded = 0;
        int numExpired = 0;
        for (final RawStetext stetext : stEtexts) {
            if (stetext.etextId.equals(etextId)) {
                if (stetext.refundDt == null) {
                    if ((stetext.expirationDt == null) || !LocalDate.now().isAfter(stetext.expirationDt)) {
                        ++numActive;
                    } else {
                        ++numExpired;
                    }
                } else {
                    ++numRefunded;
                }
            }
        }

        htm.addln("  <td class='blr'>");
        htm.addln("   <select name='etext", etextId.replace(' ', '_'), "'>");

        htm.add("    <option value='none'");
        if (numActive + numRefunded + numExpired == 0) {
            htm.add(" selected='SELECTED'");
        }
        htm.addln("     >-");
        htm.addln("    </option>");

        htm.add("    <option value='active'");
        if (numActive > 0) {
            htm.add(" selected='SELECTED'");
        }
        htm.addln("     >ACTIVE");
        htm.addln("    </option>");

        htm.add("    <option value='refunded'");
        if (numActive == 0 && numRefunded > 0) {
            htm.add(" selected='SELECTED'");
        }
        htm.addln("     >REFUNDED");
        htm.addln("    </option>");

        htm.add("    <option value='expired'");
        if (numActive == 0 && numRefunded == 0 && numExpired > 0) {
            htm.add(" selected='SELECTED'");
        }
        htm.addln("     >EXPIRED");
        htm.addln("    </option>");

        htm.addln("   </select>");
        htm.addln("  </td></tr>");
    }

    /**
     * Updates the test student data.
     *
     * @param cache the data cache
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void updateStudent(final Cache cache, final ServletRequest req,
                              final HttpServletResponse resp) throws IOException, SQLException {

        final String aplnTerm = req.getParameter("apln_term");
        final String timeLimit = req.getParameter("timelimit");
        final String licensed = req.getParameter("licensed");
        String act = req.getParameter("act_score");
        String sat = req.getParameter("sat_score");

        if (AbstractSite.isParamInvalid(aplnTerm) || AbstractSite.isParamInvalid(timeLimit)
            || AbstractSite.isParamInvalid(licensed) || AbstractSite.isParamInvalid(act)
            || AbstractSite.isParamInvalid(sat)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  apln_term='", aplnTerm, "'");
            Log.warning("  timelimit='", timeLimit, "'");
            Log.warning("  licensed='", licensed, "'");
            Log.warning("  act_score='", act, "'");
            Log.warning("  sat_score='", sat, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            if (act.isEmpty()) {
                act = null;
            }
            if (sat.isEmpty()) {
                sat = null;
            }

            Integer newAct;
            try {
                newAct = act == null ? null : Integer.valueOf(act);
            } catch (final NumberFormatException ex) {
                Log.warning(ex);
                newAct = null;
            }

            Integer newSat;
            try {
                newSat = sat == null ? null : Integer.valueOf(sat);
            } catch (final NumberFormatException ex) {
                Log.warning(ex);
                newSat = null;
            }

            final RawStudent stuModel =
                    RawStudentLogic.query(cache, RawStudent.TEST_STUDENT_ID, false);

            if (stuModel != null) {
                if (aplnTerm != null && stuModel.aplnTerm != null
                    && !aplnTerm.equals(stuModel.aplnTerm.shortString)) {

                    final ETermName termName = ETermName.forName(aplnTerm.substring(0, 2));
                    try {
                        RawStudentLogic.updateApplicationTerm(cache, stuModel.stuId,
                                new TermKey(termName, 2000 + Integer.parseInt(aplnTerm.substring(2))));
                    } catch (final NumberFormatException ex) {
                        Log.warning("Unable to parse term string ", aplnTerm);
                    }
                }

                if (timeLimit != null) {
                    final Float existing = stuModel.timelimitFactor;
                    if ("10".equals(timeLimit)) {
                        if (existing != null) {
                            RawStudentLogic.updateTimeLimitFactor(cache, stuModel.stuId, null);
                        }
                    } else if ("15".equals(timeLimit)) {
                        if (existing == null || Math.abs(existing.doubleValue() - 1.5) > 0.01) {
                            RawStudentLogic.updateTimeLimitFactor(cache, stuModel.stuId, Float.valueOf(1.5f));
                        }
                    } else if ("20".equals(timeLimit)) {
                        if (existing == null || Math.abs(existing.doubleValue() - 2.0) > 0.01) {
                            RawStudentLogic.updateTimeLimitFactor(cache, stuModel.stuId, Float.valueOf(2.0f));
                        }
                    } else if ("25".equals(timeLimit)) {
                        if (existing == null || Math.abs(existing.doubleValue() - 2.5) > 0.01) {
                            RawStudentLogic.updateTimeLimitFactor(cache, stuModel.stuId, Float.valueOf(2.5f));
                        }
                    } else if ("30".equals(timeLimit)
                               && (existing == null || Math.abs(existing.doubleValue() - 3.0) > 0.01)) {
                        RawStudentLogic.updateTimeLimitFactor(cache, stuModel.stuId, Float.valueOf(3.0f));
                    }
                }

                final String oldLic = stuModel.licensed;
                if ("on".equals(licensed)) {
                    if (oldLic == null || "N".equals(oldLic)) {
                        RawStudentLogic.updateLicensed(cache, stuModel.stuId, "Y");
                    }
                } else if (oldLic == null || "Y".equals(oldLic)) {
                    RawStudentLogic.updateLicensed(cache, stuModel.stuId, "N");
                }

                final Integer oldAct = stuModel.actScore;
                final Integer oldSat = stuModel.satScore;

                if ((!Objects.equals(oldAct, newAct)
                     || !Objects.equals(oldSat, newSat))) {

                    RawStudentLogic.updateTestScores(cache, stuModel.stuId, newAct, newSat, stuModel.apScore);
                }

                resp.sendRedirect("test_student.html");
            }
        }
    }

    /**
     * Updates the test student special student categories.
     *
     * @param cache the data cache
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void updateSpecial(final Cache cache, final ServletRequest req,
                              final HttpServletResponse resp) throws IOException, SQLException {

        final int numSpec = SPECIAL.length;
        final boolean[] want = new boolean[numSpec];
        for (int i = 0; i < numSpec; ++i) {
            final String lower = SPECIAL[i].toLowerCase(Locale.ROOT);
            want[i] = "on".equals(req.getParameter(lower));
        }

        final List<RawSpecialStus> existing = RawSpecialStusLogic.queryByStudent(cache, RawStudent.TEST_STUDENT_ID);
        final Collection<String> curTypes = new ArrayList<>(existing.size());
        for (final RawSpecialStus test : existing) {
            curTypes.add(test.stuType);
        }

        for (int i = 0; i < numSpec; ++i) {
            if (want[i]) {
                if (!curTypes.contains(SPECIAL[i])) {
                    RawSpecialStusLogic.insert(cache, new RawSpecialStus(RawStudent.TEST_STUDENT_ID,
                            SPECIAL[i], null, null));
                }
            } else if (curTypes.contains(SPECIAL[i])) {
                for (final RawSpecialStus test : existing) {
                    if (SPECIAL[i].equals(test.stuType)) {
                        RawSpecialStusLogic.delete(cache, test);
                        break;
                    }
                }
            }
        }

        resp.sendRedirect("test_student.html");
    }

    /**
     * Updates the test student placement data.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void updatePlacement(final Cache cache, final AdminSite site, final ServletRequest req,
                                final HttpServletResponse resp) throws IOException, SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();
        final RawStudent testStu = RawStudentLogic.query(cache, RawStudent.TEST_STUDENT_ID, false);

        final int numPts = PTS.length;
        final int numOutcomes = PT_OUTCOMES.length;

        // Fetch the desired configuration from the request
        final boolean[] wantExam = new boolean[numPts];
        final String[][] wantOutcomes = new String[numPts][numOutcomes];
        for (int i = 0; i < numPts; ++i) {
            final String param = req.getParameter(PT_KEYS[i]);
            wantExam[i] = "on".equals(param);
            for (int j = 0; j < numOutcomes; ++j) {
                wantOutcomes[i][j] = req.getParameter(PT_KEYS[i] + PT_OUTCOME_KEYS[j]);

                if (AbstractSite.isParamInvalid(wantOutcomes[i][j])) {
                    Log.warning("Invalid request parameters - possible attack:");
                    Log.warning("  ", PT_KEYS[i], PT_OUTCOME_KEYS[j], "='", wantOutcomes[i][j], "'");
                    wantOutcomes[i][j] = null;
                }
            }
        }

        // Fetch the current configuration
        final List<RawStmpe> placements = RawStmpeLogic.queryLegalByStudent(cache, RawStudent.TEST_STUDENT_ID);
        placements.sort(new RawStmpe.FinishDateTimeComparator());

        final List<RawMpeCredit> results =
                RawMpeCreditLogic.queryByStudent(cache, RawStudent.TEST_STUDENT_ID);

        final RawStmpe[] attempts = new RawStmpe[numPts];

        int index = 0;
        for (final RawStmpe test : placements) {
            attempts[index] = test;
            ++index;
        }

        boolean error = false;

        // See what changes need to be made
        for (int i = 0; i < numPts; ++i) {

            if (wantExam[i]) {
                if (attempts[i] == null) {
                    // Build the attempt
                    insertPlacement(cache, active, testStu, i, wantOutcomes[i]);
                } else {
                    final LocalDateTime examDt = attempts[i].getFinishDateTime();
                    final Long serial = attempts[i].serialNbr;

                    // See if the attempt results need to be adjusted - we do not attempt to adjust
                    // the exam record - just the outcomes
                    for (int j = 0; j < numOutcomes; j++) {
                        if ("N".equals(wantOutcomes[i][j])) {

                            // Delete any results we don't want
                            for (final RawMpeCredit res : results) {
                                if ((PT_OUTCOMES[j].equals(res.course) && res.serialNbr.equals(serial))
                                    && !RawMpeCreditLogic.delete(cache, res)) {
                                    Log.warning("Failed to delete test student MPE_CREDIT");
                                    error = true;
                                }
                            }

                        } else if ("P".equals(wantOutcomes[i][j]) || "C".equals(wantOutcomes[i][j])) {

                            // Insert any results we want but don't have
                            boolean searching = true;
                            for (final RawMpeCredit res : results) {
                                if (PT_OUTCOMES[j].equals(res.course) && res.serialNbr.equals(serial)) {
                                    searching = false;
                                    break;
                                }
                            }

                            if (searching && examDt != null) {
                                insertResult(cache, serial.longValue(), PT_OUTCOMES[j], examDt.toLocalDate(),
                                        wantOutcomes[i][j]);
                            }
                        }
                    }
                }
            } else if (attempts[i] != null) {
                // Delete the attempt and any related outcomes
                if (RawStmpeLogic.deleteExamAndAnswers(cache, attempts[i])) {
                    if (attempts[i] != null) {
                        final Long serial = attempts[i].serialNbr;

                        for (final RawMpeCredit res : results) {
                            if ((res.serialNbr != null && res.serialNbr.equals(serial))
                                && !RawMpeCreditLogic.delete(cache, res)) {
                                Log.warning("Failed to delete test student MPE_CREDIT");
                                error = true;
                            }
                        }
                    }
                } else {
                    Log.warning("Failed to delete test student STMPE with answers");
                    error = true;
                }
            }
        }

        if (error) {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), null, false, site.getTitle(), "home.html", Page.NO_BARS, null,
                    false, true);

            htm.sDiv("indent22");
            htm.sDiv("errorbox");
            htm.addln("There was an error updating the student record.");
            htm.eDiv();
            htm.eDiv();

            Page.endOrdinaryPage(cache, site, htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);

        } else {
            resp.sendRedirect("test_student.html");
        }
    }

    /**
     * Constructs a placement attempt and inserts it along with associated placement credit records.
     *
     * @param cache      the data cache
     * @param activeTerm the active term
     * @param testStu    the test student
     * @param index      the index of the attempt, to ensure different timestamps
     * @param outcomes   the outcomes
     * @throws SQLException if there was an error accessing the database
     */
    private static void insertPlacement(final Cache cache, final TermRec activeTerm, final RawStudent testStu,
                                        final int index, final String[] outcomes) throws SQLException {

        final long serial = AbstractHandlerBase.generateSerialNumber(false);

        final LocalDate examDt = LocalDate.now();

        final String academic;
        final int yr = activeTerm.term.shortYear.intValue();
        if (activeTerm.term.name == ETermName.SPRING) {
            academic = Integer.toString(yr - 1) + yr;
        } else {
            academic = Integer.toString(yr) + (yr + 1);
        }

        final int numOutcomes = PT_OUTCOMES.length;

        boolean placed = false;
        for (int i = 0; i < numOutcomes; ++i) {
            final String outcome = outcomes[i];
            if ("P".equals(outcome) || "C".equals(outcome)) {
                insertResult(cache, serial, PT_OUTCOMES[i], examDt, outcome);
                placed = true;
            }
        }

        final int finish = (index + 8) * 60;

        final RawStmpe record = new RawStmpe(RawStudent.TEST_STUDENT_ID, "MPTTC",
                academic, examDt, Integer.valueOf(finish - 1), Integer.valueOf(finish),
                testStu.lastName, testStu.firstName, testStu.middleInitial, null, Long.valueOf(serial),
                Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0),
                Integer.valueOf(0), Integer.valueOf(0), placed ? "Y" : "N",
                "P");

        RawStmpeLogic.insert(cache, record);
    }

    /**
     * Inserts a placement credit record.
     *
     * @param cache   the data cache
     * @param serial  the serial number
     * @param course  the course in which credit was earned
     * @param examDt  the exam date/time
     * @param outcome the outcome
     * @throws SQLException if there was an error accessing the database
     */
    private static void insertResult(final Cache cache, final long serial, final String course,
                                     final LocalDate examDt, final String outcome) throws SQLException {

        final RawMpeCredit credit = new RawMpeCredit(RawStudent.TEST_STUDENT_ID, course, outcome,
                examDt, null, Long.valueOf(serial), "MPTTC", "TC");

        RawMpeCreditLogic.apply(cache, credit);
    }

    /**
     * Updates the test student Tutorial data.
     *
     * @param cache the data cache
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void updateTutorials(final Cache cache, final ServletRequest req,
                                final HttpServletResponse resp) throws IOException, SQLException {

        for (final String tutorial : TUTORIALS) {
            final String key = tutorial.replace(CoreConstants.SPC, CoreConstants.EMPTY);

            final String[] review = {req.getParameter(key + "0"),
                    req.getParameter(key + "1"),
                    req.getParameter(key + "2"),
                    req.getParameter(key + "3"),
                    req.getParameter(key + "4")};
            final String unit = req.getParameter(key + "E");

            // Delete all existing tutorial exams for the test user first
            final List<RawStexam> exams = RawStexamLogic.getExams(cache, RawStudent.TEST_STUDENT_ID,
                    tutorial, false, RawStexamLogic.ALL_EXAM_TYPES);
            for (final RawStexam exam : exams) {
                RawStexamLogic.delete(cache, exam);
            }

            for (int i = 0; i < 5; ++i) {
                if ("F".equals(review[i])) {
                    insertTutorialReview(cache, tutorial, Integer.valueOf(i), false);
                } else if ("P".equals(review[i])) {
                    insertTutorialReview(cache, tutorial, Integer.valueOf(i), true);
                }
            }

            if ("F".equals(unit)) {
                insertTutorialExam(cache, tutorial, false);
            } else if ("P".equals(unit)) {
                insertTutorialExam(cache, tutorial, true);
            }
        }

        resp.sendRedirect("test_student.html");
    }

    /**
     * Constructs a tutorial review exam and inserts it.
     *
     * @param cache    the data cache
     * @param tutorial the tutorial course ID
     * @param unit     the unit
     * @param passing  {@code true} to insert a passing exam; {@code false} for a non-passing exam
     * @throws SQLException if there is an error accessing the database
     */
    private static void insertTutorialReview(final Cache cache, final String tutorial, final Integer unit,
                                             final boolean passing) throws SQLException {

        final long serial = AbstractHandlerBase.generateSerialNumber(false);
        final LocalDateTime examDt = LocalDateTime.now();
        // Use passing scores of 8, 12, 16, 20, to make sane values easy to calculate
        final int passingScore = 4 + (unit.intValue() << 2);

        final SystemData systemData = cache.getSystemData();

        final RawExam exam = systemData.getActiveExamByCourseUnitType(tutorial, unit, "R");

        if (exam != null) {
            final int time = examDt.getHour() * 60 + examDt.getMinute();

            final RawStexam record = new RawStexam(Long.valueOf(serial), exam.version, RawStudent.TEST_STUDENT_ID,
                    examDt.toLocalDate(), Integer.valueOf(passing ? passingScore : 1), Integer.valueOf(passingScore),
                    Integer.valueOf(time), Integer.valueOf(time), "Y", passing ? "Y" : "N", null, tutorial, unit,
                    "R", passing ? "Y" : "N", null, null);

            RawStexamLogic.insert(cache, record);
            // this is committed by the caller...
        } else {
            Log.warning("No unit review exam found for ", tutorial, " unit ", unit);
        }
    }

    /**
     * Constructs a tutorial exam and inserts it.
     *
     * @param cache    the data cache
     * @param tutorial the tutorial course ID
     * @param passing  {@code true} to insert a passing exam; {@code false} for a non-passing exam
     * @throws SQLException if there is an error accessing the database
     */
    private static void insertTutorialExam(final Cache cache, final String tutorial,
                                           final boolean passing) throws SQLException {

        final long serial = AbstractHandlerBase.generateSerialNumber(false);
        final LocalDateTime examDt = LocalDateTime.now();

        final SystemData systemData = cache.getSystemData();

        final RawExam exam = systemData.getActiveExamByCourseUnitType(tutorial, Integer.valueOf(4), "U");

        if (exam != null) {
            final int time = examDt.getHour() * 60 + examDt.getMinute();
            final RawStexam record = new RawStexam(Long.valueOf(serial), exam.version, RawStudent.TEST_STUDENT_ID,
                    examDt.toLocalDate(), Integer.valueOf(passing ? 20 : 1), Integer.valueOf(16),
                    Integer.valueOf(time), Integer.valueOf(time), "Y", passing ? "Y" : "N", null, tutorial,
                    Integer.valueOf(4), "R", passing ? "Y" : "N", null, null);

            RawStexamLogic.insert(cache, record);
            // this is committed by the caller...
        } else {
            Log.warning("No unit exam found for ", tutorial, " unit 4");
        }
    }

    /**
     * Updates the test student e-text records.
     *
     * @param cache the data cache
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void updateETexts(final Cache cache, final ServletRequest req,
                             final HttpServletResponse resp) throws IOException, SQLException {

        final SystemData systemData = cache.getSystemData();

        final List<RawEtext> allEtexts = systemData.getETexts();
        final List<RawStetext> stEtexts = RawStetextLogic.queryByStudent(cache, RawStudent.TEST_STUDENT_ID);

        for (final RawEtext etext : allEtexts) {
            final String etextId = etext.etextId;
            final String want = req.getParameter("etext" + etextId.replace(' ', '_'));

            // get the student's status , then compare with desired status
            int numActive = 0;
            int numRefunded = 0;
            int numExpired = 0;
            for (final RawStetext stetext : stEtexts) {
                if (stetext.etextId.equals(etextId)) {
                    if (stetext.refundDt == null) {
                        if ((stetext.expirationDt == null) || !LocalDate.now().isAfter(stetext.expirationDt)) {
                            ++numActive;
                        } else {
                            ++numExpired;
                        }
                    } else {
                        ++numRefunded;
                    }
                }
            }

            if ("none".equals(want)) {
                if (numActive + numRefunded + numExpired > 0) {
                    deleteStudentEtexts(cache, stEtexts, etextId);
                }
            } else if ("active".equals(want)) {
                if (numActive == 0 || numExpired > 0 || numRefunded > 0) {
                    deleteStudentEtexts(cache, stEtexts, etextId);
                    insertStudentEText(cache, etextId, false, false);
                }
            } else if ("refunded".equals(want)) {
                if (numActive > 0 || numExpired > 0 || numRefunded == 0) {
                    deleteStudentEtexts(cache, stEtexts, etextId);
                    insertStudentEText(cache, etextId, true, false);
                }
            } else if ("expired".equals(want) && (numActive > 0 || numRefunded > 0 || numExpired == 0)) {
                deleteStudentEtexts(cache, stEtexts, etextId);
                insertStudentEText(cache, etextId, false, true);
            }
        }

        resp.sendRedirect("test_student.html");
    }

    /**
     * Deletes all student e-texts for a particular e-text ID.
     *
     * @param cache    the data cache
     * @param stEtexts the student e-texts
     * @param etextId  the e-text ID
     * @throws SQLException if there is an error accessing the database
     */
    private static void deleteStudentEtexts(final Cache cache, final Iterable<RawStetext> stEtexts,
                                            final String etextId) throws SQLException {

        final Iterator<RawStetext> iter = stEtexts.iterator();
        while (iter.hasNext()) {
            final RawStetext stetext = iter.next();
            if (etextId.equals(stetext.etextId)) {
                RawStetextLogic.delete(cache, stetext);
                iter.remove();
            }
        }
    }

    /**
     * Inserts a student e-text record.
     *
     * @param cache    the data cache
     * @param etextId  the e-text ID
     * @param refunded true if record was refunded
     * @param expired  true if record has expired
     * @throws SQLException if there is an error accessing the database
     */
    private static void insertStudentEText(final Cache cache, final String etextId, final boolean refunded,
                                           final boolean expired) throws SQLException {

        final LocalDate today = LocalDate.now();
        final LocalDate yester = today.minusDays(1L);
        final LocalDate dayBef = yester.minusDays(1L);

        final RawStetext model = new RawStetext(RawStudent.TEST_STUDENT_ID, etextId, dayBef, "88888888", expired ?
                yester : null, yester, refunded ? yester : null, refunded ? "Admin Web Site config" : null);

        RawStetextLogic.insert(cache, model);
    }

    /**
     * Updates the test student registrations.
     *
     * @param cache the data cache
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there was an error writing the response
     * @throws SQLException if there was an error accessing the database
     */
    static void updateRegistrations(final Cache cache, final ServletRequest req,
                                    final HttpServletResponse resp) throws IOException, SQLException {

        final SystemData systemData = cache.getSystemData();
        final TermRec active = systemData.getActiveTerm();

        final List<RawCsection> sections = systemData.getCourseSections(active.term);

        final Collection<String> courseIds = new TreeSet<>();
        for (final RawCsection sect : sections) {
            if ("550".equals(sect.sect) || "Y".equals(sect.bogus)) {
                continue;
            }
            final String courseId = sect.course;
            if (!"M 384".equals(courseId)) {
                courseIds.add(courseId);
            }
        }

        final List<RawStcourse> curReg = RawStcourseLogic.queryByStudent(cache, RawStudent.TEST_STUDENT_ID, true,
                false);
        int order = 1;

        for (final String courseId : courseIds) {
            final String crs = courseId.replace(' ', '_');

            final List<AssignmentRec> assignments = systemData.getActiveAssignmentsByCourseType(courseId, null);
            final List<MasteryExamRec> masteryExams = systemData.getActiveMasteryExamsByCourse(courseId);

            final boolean isReg = "on".equals(req.getParameter(crs + "reg"));
            final boolean hasPrereq = "on".equals(req.getParameter(crs + "prereq"));
            final boolean isStarted = "on".equals(req.getParameter(crs + "start"));
            final boolean isInc = "on".equals(req.getParameter(crs + "inc"));
            final boolean isInProg = isInc && "on".equals(req.getParameter(crs + "incip"));
            final boolean isCounted = isInc && "on".equals(req.getParameter(crs + "incc"));

            deleteHwExam(cache, courseId);

            if (isReg) {
                final String sect = req.getParameter(crs);
                if (sect != null) {
                    // Delete any existing row in case parameters have changed
                    for (final RawStcourse test : curReg) {
                        if (courseId.equals(test.course)) {
                            RawStcourseLogic.delete(cache, test);
                        }
                    }

                    insertReg(cache, courseId, sect, order, isStarted, hasPrereq, isInc, isCounted, isInProg);
                    ++order;
                    insertHWExam(cache, courseId, sect, req, assignments, masteryExams);
                }
            } else {
                for (final RawStcourse test : curReg) {
                    if (courseId.equals(test.course)) {
                        RawStcourseLogic.delete(cache, test);
                    }
                }
            }
        }

        // Fix up the STTERM record and the pacing structure on the STUDENT record

        final RegistrationsLogic.ActiveTermRegistrations activeRegs =
                RegistrationsLogic.gatherActiveTermRegistrations(cache, RawStudent.TEST_STUDENT_ID);
        final List<RawStcourse> inPace = activeRegs.inPace();

        final RawStterm existingStterm = RawSttermLogic.query(cache, active.term, RawStudent.TEST_STUDENT_ID);
        if (existingStterm != null) {
            RawSttermLogic.delete(cache, existingStterm);
        }

        final RawStudent student = RawStudentLogic.query(cache, RawStudent.TEST_STUDENT_ID, false);

        if (inPace.isEmpty()) {
            if (student != null && student.pacingStructure != null) {
                RawStudentLogic.updatePacingStructure(cache, RawStudent.TEST_STUDENT_ID, null);
            }
        } else {
            final int pace = PaceTrackLogic.determinePace(inPace);
            final String track = PaceTrackLogic.determinePaceTrack(inPace, pace);
            final String first = PaceTrackLogic.determineFirstCourse(inPace);

            final RawStterm newStterm = new RawStterm(active.term, RawStudent.TEST_STUDENT_ID, Integer.valueOf(pace),
                    track, first, null, null, null);
            RawSttermLogic.insert(cache, newStterm);

            final String pacing = PaceTrackLogic.determinePacingStructure(cache, RawStudent.TEST_STUDENT_ID, inPace,
                    null);

            if (student != null && !Objects.equals(student.pacingStructure, pacing)) {
                RawStudentLogic.updatePacingStructure(cache, RawStudent.TEST_STUDENT_ID, pacing);
            }
        }

        resp.sendRedirect("test_student.html");
    }

    /**
     * Constructs a student registration record.
     *
     * @param cache        the database cache
     * @param courseId     the course ID
     * @param sect         the section number
     * @param order        the pace order
     * @param isStarted    {@code true} if the course should be marked as started
     * @param hasPrereq    {@code true} if the prerequisite is satisfied
     * @param isInc        {@code true} to create an incomplete
     * @param isCounted    {@code true} if incomplete should count toward pace
     * @param isInProgress {@code true} if incomplete is in progress
     * @throws SQLException of there was an error accessing the database
     */
    private static void insertReg(final Cache cache, final String courseId, final String sect, final int order,
                                  final boolean isStarted, final boolean hasPrereq, final boolean isInc,
                                  final boolean isCounted, final boolean isInProgress) throws SQLException {

        final SystemData systemData = cache.getSystemData();

        final TermRec activeTerm = systemData.getActiveTerm();
        final TermRec priorTerm = systemData.getPriorTerm();
        final String instrnType = systemData.getInstructionType(courseId, sect, activeTerm.term);

        final TermKey term;
        if (!isInc || isInProgress) {
            term = activeTerm.term;
        } else {
            term = priorTerm.term;
        }

        final RawStcourse stcourse;

        if (isInc) {
            if (isInProgress) {
                stcourse = new RawStcourse(term, // termKey
                        RawStudent.TEST_STUDENT_ID, // stuId
                        courseId, // course
                        sect, // section
                        Integer.valueOf(order), // paceOrder
                        isStarted ? "Y" : null, // openStatus
                        "I", // gradingOption
                        "N", // completed
                        null, // score
                        null, // courseGrade
                        "Y", // prereqSaatis
                        "Y", // initClassRoll
                        "N", // stuProvided
                        "Y", // finalClassRoll
                        null, // examPlaced
                        null, // zeroUnit
                        null, // timeoutFactor
                        null, // forfeitI
                        "Y", // iInProgress
                        isCounted ? "Y" : "N", // iCounted
                        "N", // ctrlTest
                        null, // deferredFDt
                        Integer.valueOf(0), // bypassTimeout
                        instrnType == null ? "RI" : instrnType, // instrnType
                        null, // registrationStatus
                        LocalDate.of(2013, Month.DECEMBER, 30), // lastClassRollDt
                        priorTerm.term, // iTermKey
                        activeTerm.endDate); // iDeadlineDt
            } else {
                stcourse = new RawStcourse(term, // termKey
                        RawStudent.TEST_STUDENT_ID, // stuId
                        courseId, // course
                        sect, // section
                        Integer.valueOf(order), // paceOrder
                        isStarted ? "Y" : null, // openStatus
                        "I", // gradingOption
                        "Y", // completed
                        Integer.valueOf(72), // score
                        "A", // courseGrade
                        "Y", // prereqSaatis
                        "Y", // initClassRoll
                        "N", // stuProvided
                        "Y", // finalClassRoll
                        null, // examPlaced
                        null, // zeroUnit
                        null, // timeoutFactor
                        null, // forfeitI
                        "N", // iInProgress
                        isCounted ? "Y" : "N", // iCounted
                        "N", // ctrlTest
                        null, // deferredFDt
                        Integer.valueOf(0), // bypassTimeout
                        instrnType == null ? "RI" : instrnType, // instrnType
                        null, // registrationStatus
                        LocalDate.of(2013, Month.DECEMBER, 30), // lastClassRollDt
                        priorTerm.term, // iTermKey
                        activeTerm.endDate); // iDeadlineDt
            }
        } else {
            stcourse = new RawStcourse(term, // termKey
                    RawStudent.TEST_STUDENT_ID, // stuId
                    courseId, // course
                    sect, // section
                    Integer.valueOf(order), // paceOrder
                    isStarted ? "Y" : null, // openStatus
                    "I", // gradingOption
                    "N", // completed
                    null, // score
                    null, // courseGrade
                    hasPrereq ? "Y" : "N", // prereqSaatis
                    "Y", // initClassRoll
                    "N", // stuProvided
                    "Y", // finalClassRoll
                    null, // examPlaced
                    null, // zeroUnit
                    null, // timeoutFactor
                    null, // forfeitI
                    "N", // iInProgress
                    null, // iCounted
                    "N", // ctrlTest
                    null, // deferredFDt
                    Integer.valueOf(0), // bypassTimeout
                    instrnType == null ? "RI" : instrnType, // instrnType
                    null, // registrationStatus
                    LocalDate.of(2013, Month.DECEMBER, 30), // lastClassRollDt
                    null, // iTermKey
                    null); // iDeadlineDt
        }

        final boolean success = RawStcourseLogic.insert(cache, stcourse);

        if (success && isInc) {
            final RawStterm stTerm = new RawStterm(priorTerm.term, RawStudent.TEST_STUDENT_ID, Integer.valueOf(1), "A",
                    courseId, null, null, null);

            RawSttermLogic.insert(cache, stTerm);
        }
    }

    /**
     * Deletes all homework and exam records on the test student's record for a particular course.
     *
     * @param cache    the data cache
     * @param courseId the course ID
     * @throws SQLException if there is an error accessing the database
     */
    private static void deleteHwExam(final Cache cache, final String courseId) throws SQLException {

        // Delete homeworks on record
        final List<RawSthomework> hws = RawSthomeworkLogic.queryByStudentCourse(cache, RawStudent.TEST_STUDENT_ID,
                courseId, true);
        for (final RawSthomework hw : hws) {
            RawSthomeworkLogic.delete(cache, hw);
        }

        // Delete exams on record
        final List<RawStexam> exams = RawStexamLogic.getExams(cache, RawStudent.TEST_STUDENT_ID, courseId, true);
        for (final RawStexam exam : exams) {
            RawStexamLogic.delete(cache, exam);
        }

        final SystemData systemData = cache.getSystemData();

        // Delete mastery attempts on record
        final List<MasteryExamRec> masteryExams = systemData.getActiveMasteryExamsByCourse(courseId);

        final MasteryAttemptLogic attemptLogic = MasteryAttemptLogic.INSTANCE;
        final MasteryAttemptQaLogic attemptQaLogic = MasteryAttemptQaLogic.INSTANCE;
        for (final MasteryExamRec exam : masteryExams) {
            final List<MasteryAttemptRec> attempts = attemptLogic.queryByStudentExam(cache, RawStudent.TEST_STUDENT_ID,
                    exam.examId, false);
            for (final MasteryAttemptRec attempt : attempts) {

                final List<MasteryAttemptQaRec> qas = attemptQaLogic.queryByAttempt(cache, attempt.serialNbr,
                        attempt.examId);
                for (final MasteryAttemptQaRec qa : qas) {
                    attemptQaLogic.delete(cache, qa);
                }

                attemptLogic.delete(cache, attempt);
            }
        }
    }

    /**
     * Inserts the homeworks and exams on the test student record to match the checkboxes selected when the form is
     * submitted.
     *
     * @param cache        the data cache
     * @param courseId     the course ID
     * @param sect         the section number
     * @param req          the request
     * @param assignments  all assignments for the course
     * @param masteryExams all mastery exams for the course
     * @throws SQLException if there is an error accessing the database
     */
    private static void insertHWExam(final Cache cache, final String courseId, final String sect,
                                     final ServletRequest req, final Iterable<AssignmentRec> assignments,
                                     final Iterable<MasteryExamRec> masteryExams) throws SQLException {

        final String key = courseId.replace(' ', '_');

        if (key.startsWith("MATH_")) {

            // Standards-based course assignments and exams
            // Assignment inputs have IDs like 'M_125st6_a1", with values "Y", "N", or "X"
            // Mastery exam inputs have IDs like 'M_125st6_m1", with values "Y", "N", or "X"

            for (int u = 1; u <= 10; ++u) {

//                for (final Map.Entry<String, String[]> param : req.getParameterMap().entrySet()) {
//                    final String pk = param.getKey();
//                    final String[] pv = param.getValue();
//                    final String pvStr = Arrays.toString(pv);
//                    Log.info("  PARAM {", pk, "}=", pvStr);
//                }

                final String stprefix = key + "st" + u;

                final String sr = req.getParameter(stprefix + "_sr");
                final String a1 = req.getParameter(stprefix + "_a1");
                final String a2 = req.getParameter(stprefix + "_a2");
                final String a3 = req.getParameter(stprefix + "_a3");

                final String m1 = req.getParameter(stprefix + "_m1");
                final String m2 = req.getParameter(stprefix + "_m2");
                final String m3 = req.getParameter(stprefix + "_m3");

                if ("Y".equals(sr) || "N".equals(sr)) {
                    String assignId = null;
                    for (final AssignmentRec rec : assignments) {
                        if (rec.unit.intValue() == u && rec.objective.intValue() == 0
                            && "ST".equals(rec.assignmentType)) {
                            assignId = rec.assignmentId;
                        }
                    }

                    if (assignId == null) {
                        Log.warning("Unable to determine unit " + u + " Skills Review assignment ID");
                    } else {
                        insertHW(cache, u, 0, courseId, sect, assignId, "ST", sr);
                    }
                }

                if ("Y".equals(a1) || "N".equals(a1)) {
                    String assignId = null;
                    for (final AssignmentRec rec : assignments) {
                        if (rec.unit.intValue() == u && rec.objective.intValue() == 1 && "ST".equals(
                                rec.assignmentType)) {
                            assignId = rec.assignmentId;
                        }
                    }

                    if (assignId == null) {
                        Log.warning("Unable to determine unit " + u + " Learning Target 1 assignment ID");
                    } else {
                        insertHW(cache, u, 1, courseId, sect, assignId, "ST", a1);
                    }
                }

                if ("Y".equals(a2) || "N".equals(a2)) {
                    String assignId = null;
                    for (final AssignmentRec rec : assignments) {
                        if (rec.unit.intValue() == u && rec.objective.intValue() == 2 && "ST".equals(
                                rec.assignmentType)) {
                            assignId = rec.assignmentId;
                        }
                    }

                    if (assignId == null) {
                        Log.warning("Unable to determine unit " + u + " Learning Target 2 assignment ID");
                    } else {
                        insertHW(cache, u, 2, courseId, sect, assignId, "ST", a2);
                    }
                }

                if ("Y".equals(a3) || "N".equals(a3)) {
                    String assignId = null;
                    for (final AssignmentRec rec : assignments) {
                        if (rec.unit.intValue() == u && rec.objective.intValue() == 3 && "ST".equals(
                                rec.assignmentType)) {
                            assignId = rec.assignmentId;
                        }
                    }

                    if (assignId == null) {
                        Log.warning("Unable to determine unit " + u + " Learning Target 3 assignment ID");
                    } else {
                        insertHW(cache, u, 3, courseId, sect, assignId, "ST", a3);
                    }
                }

                if ("Y".equals(m1) || "N".equals(m1)) {
                    String examId = null;
                    for (final MasteryExamRec rec : masteryExams) {
                        if (rec.unit.intValue() == u && rec.objective.intValue() == 1 && "ST".equals(rec.examType)) {
                            examId = rec.examId;
                        }
                    }

                    if (examId == null) {
                        Log.warning("Unable to determine unit " + u + " Learning Target 1 mastery ID");
                    } else {
                        insertMastery(cache, u, 1, examId, m1);
                    }
                }

                if ("Y".equals(m2) || "N".equals(m2)) {
                    String examId = null;
                    for (final MasteryExamRec rec : masteryExams) {
                        if (rec.unit.intValue() == u && rec.objective.intValue() == 2 && "ST".equals(rec.examType)) {
                            examId = rec.examId;
                        }
                    }

                    if (examId == null) {
                        Log.warning("Unable to determine unit " + u + " Learning Target 2 mastery ID");
                    } else {
                        insertMastery(cache, u, 2, examId, m2);
                    }
                }

                if ("Y".equals(m3) || "N".equals(m3)) {
                    String examId = null;
                    for (final MasteryExamRec rec : masteryExams) {
                        if (rec.unit.intValue() == u && rec.objective.intValue() == 3 && "ST".equals(rec.examType)) {
                            examId = rec.examId;
                        }
                    }

                    if (examId == null) {
                        Log.warning("Unable to determine unit " + u + " Learning Target 3 mastery ID");
                    } else {
                        insertMastery(cache, u, 3, examId, m3);
                    }
                }
            }
        } else {

            // Old course homeworks and exams

            final String prefix = key.substring(key.length() - 2);
            final String srExamId = prefix + "GAT";

            final String srExam = req.getParameter(key + "exams0");
            if ("F".equals(srExam)) {
                insertExam(cache, Integer.valueOf(0), 1, courseId, srExamId, "R", false, false);
            } else if ("P".equals(srExam)) {
                insertExam(cache, Integer.valueOf(0), 1, courseId, srExamId, "R", true, true);
            }

            for (int u = 1; u < 5; ++u) {
                final Integer unit = Integer.valueOf(u);

                if ("on".equals(req.getParameter(key + "hw" + u + "1"))) {
                    insertHW(cache, u, 1, courseId, sect, prefix + u + "1H", "HW", "Y");
                }
                if ("on".equals(req.getParameter(key + "hw" + u + "2"))) {
                    insertHW(cache, u, 2, courseId, sect, prefix + u + "2H", "HW", "Y");
                }
                if ("on".equals(req.getParameter(key + "hw" + u + "3"))) {
                    insertHW(cache, u, 3, courseId, sect, prefix + u + "3H", "HW", "Y");
                }
                if ("on".equals(req.getParameter(key + "hw" + u + "4"))) {
                    insertHW(cache, u, 4, courseId, sect, prefix + u + "4H", "HW", "Y");
                }
                if ("on".equals(req.getParameter(key + "hw" + u + "5"))) {
                    insertHW(cache, u, 5, courseId, sect, prefix + u + "5H", "HW", "Y");
                }

                final String reExamId = prefix + u + "RE";
                final String ueExamId = prefix + u + "UE";

                final String proctoredType = "U";
                final String reviewType1 = "R";
                final String reviewType2 = "R";

                final String unitExam = req.getParameter(key + "exams" + u);
                if ("F".equals(unitExam)) {
                    insertExam(cache, unit, 1, courseId, reExamId, reviewType1, false, false);
                } else if ("P".equals(unitExam)) {
                    insertExam(cache, unit, 1, courseId, reExamId, reviewType1, true, true);
                } else if ("PF".equals(unitExam)) {
                    insertExam(cache, unit, 1, courseId, reExamId, reviewType1, true, true);
                    insertExam(cache, unit, 2, courseId, ueExamId, proctoredType, false, false);
                } else if ("PFF".equals(unitExam)) {
                    insertExam(cache, unit, 1, courseId, reExamId, reviewType1, true, true);
                    insertExam(cache, unit, 2, courseId, ueExamId, proctoredType, false, false);
                    insertExam(cache, unit, 3, courseId, ueExamId, proctoredType, false, false);
                } else if ("PP".equals(unitExam)) {
                    insertExam(cache, unit, 1, courseId, reExamId, reviewType1, true, true);
                    insertExam(cache, unit, 2, courseId, ueExamId, proctoredType, true, true);
                } else if ("PFFF".equals(unitExam)) {
                    insertExam(cache, unit, 1, courseId, reExamId, reviewType1, true, true);
                    insertExam(cache, unit, 2, courseId, ueExamId, proctoredType, false, false);
                    insertExam(cache, unit, 3, courseId, ueExamId, proctoredType, false, false);
                    insertExam(cache, unit, 4, courseId, reExamId, reviewType2, false, false);
                } else if ("PFFP".equals(unitExam)) {
                    insertExam(cache, unit, 1, courseId, reExamId, reviewType1, true, true);
                    insertExam(cache, unit, 2, courseId, ueExamId, proctoredType, false, false);
                    insertExam(cache, unit, 3, courseId, ueExamId, proctoredType, false, false);
                    insertExam(cache, unit, 4, courseId, reExamId, reviewType2, true, true);
                } else if ("PFFPP".equals(unitExam)) {
                    insertExam(cache, unit, 1, courseId, reExamId, reviewType1, true, true);
                    insertExam(cache, unit, 2, courseId, ueExamId, proctoredType, false, false);
                    insertExam(cache, unit, 3, courseId, ueExamId, proctoredType, false, false);
                    insertExam(cache, unit, 4, courseId, reExamId, reviewType2, true, false);
                    insertExam(cache, unit, 5, courseId, ueExamId, proctoredType, true, true);
                }
            }

            final Integer finUnit = Integer.valueOf(5);
            final String finExam = req.getParameter(key + "exams5");
            final String feExamId = prefix + "FIN";

            if ("F".equals(finExam)) {
                insertExam(cache, finUnit, 1, courseId, feExamId, "F", false, false);
            } else if ("P".equals(finExam)) {
                insertExam(cache, finUnit, 1, courseId, feExamId, "F", true, true);
            }
        }
    }

    /**
     * Inserts a record of an exam.
     *
     * @param cache       the data cache
     * @param unit        the unit (0 for the Skills Review, 5 for the Final)
     * @param index       the index within the unit, used to ensure exam order
     * @param courseId    the course ID
     * @param examId      the exam ID
     * @param examType    the exam type
     * @param passed      {@code true} to mark the exam as passed
     * @param firstPassed {@code true} to mark the exam as the first passed
     * @throws SQLException if there is an error accessing the database
     */
    private static void insertExam(final Cache cache, final Integer unit, final int index,
                                   final String courseId, final String examId, final String examType,
                                   final boolean passed,
                                   final boolean firstPassed) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();

        final int days = unit.intValue() * 5 + index;
        LocalDate day = active.startDate;
        for (int i = 0; i < days; ++i) {
            day = day.plusDays(1L);
        }
        final LocalTime start = LocalTime.now().minusSeconds(1L);
        final LocalTime end = LocalTime.now();

        final int startInt = start.getHour() * 60 + start.getMinute();
        final int endInt = end.getHour() * 60 + end.getMinute();

        final int score;
        final int mastery;
        if (passed) {
            if ("F".equals(examType)) {
                score = 20;
                mastery = 16;
            } else {
                score = 10;
                mastery = 8;
            }
        } else {
            score = 1;
            if ("F".equals(examType)) {
                mastery = 16;
            } else {
                mastery = 8;
            }
        }

        final RawStexam exam = new RawStexam(Long.valueOf(AbstractHandlerBase.generateSerialNumber(false)), examId,
                RawStudent.TEST_STUDENT_ID, day, Integer.valueOf(score), Integer.valueOf(mastery),
                Integer.valueOf(startInt), Integer.valueOf(endInt), "Y", passed ? "Y" : "N", null, courseId, unit,
                examType, firstPassed ? "Y" : "N", null, null);

        RawStexamLogic.insert(cache, exam);
    }

    /**
     * Inserts a record of a homework assignment.
     *
     * @param cache     the data cache
     * @param unit      the unit (0 for the Skills Review, 5 for the Final)
     * @param objective the objective within the unit (used to order assignments)
     * @param courseId  the course ID
     * @param sect      the section number
     * @param hwId      the homework ID
     * @param passed    "Y" if passed, "N" if not
     * @throws SQLException if there is an error accessing the database
     */
    private static void insertHW(final Cache cache, final int unit, final int objective, final String courseId,
                                 final String sect, final String hwId, final String type, final String passed)
            throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();

        final int days = unit * 5 + objective;
        LocalDate day = active.startDate;
        for (int i = 0; i < days; ++i) {
            day = day.plusDays(1L);
        }

        final Integer time = Integer.valueOf(TemporalUtils.minuteOfDay(LocalDateTime.now()));
        final long sn = AbstractHandlerBase.generateSerialNumber(false);

        final Long serialNbrStr = Long.valueOf(sn);
        final RawSthomework sthw = new RawSthomework(serialNbrStr, hwId, RawStudent.TEST_STUDENT_ID, day,
                Integer.valueOf(3), time, time, "Y", passed, type, courseId, sect, Integer.valueOf(unit),
                Integer.valueOf(objective), "N", null, null);

        RawSthomeworkLogic.insert(cache, sthw);
    }

    /**
     * Inserts a record of a mastery attempt.
     *
     * @param cache     the data cache
     * @param unit      the unit
     * @param objective the objective
     * @param examId    the exam ID
     * @param passed    {@code true} to mark the exam as passed
     * @throws SQLException if there is an error accessing the database
     */
    private static void insertMastery(final Cache cache, final int unit, final int objective,
                                      final String examId, final String passed) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();

        final int days = unit * 3 + objective;
        LocalDate day = active.startDate;
        for (int i = 0; i < days; ++i) {
            day = day.plusDays(1L);
        }
        final LocalTime now = LocalTime.now();
        final LocalDateTime start = LocalDateTime.of(day, now.minusSeconds(10L));
        final LocalDateTime end = LocalDateTime.of(day, now);

        final int score = "Y".equals(passed) ? 2 : 1;
        final int sn = (int) AbstractHandlerBase.generateSerialNumber(false);

        final MasteryAttemptRec attempt = new MasteryAttemptRec(Integer.valueOf(sn), examId, RawStudent.TEST_STUDENT_ID,
                start, end, Integer.valueOf(score), Integer.valueOf(2), passed, passed, "TC");

        MasteryAttemptLogic.INSTANCE.insert(cache, attempt);
    }
}

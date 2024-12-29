package dev.mathops.web.site.admin.office;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawCalcsLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCalcs;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A page to handle issuance of testing center calculators.
 */
enum PageTestingIssueCalc {
    ;

    /**
     * Generates the page that gathers information to issue a testing center calculator.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = OfficePage.startOfficePage(cache, site, session, true);

        htm.sDiv("center");
        htm.sH(2).add("Testing Center").eH(2);

        htm.sDiv("buttonstack");

        htm.hr().div("vgap");
        htm.sH(3).add("Testing Center Calculators").eH(3);

        htm.addln("<form method='get' action='testing_issue_calc.html' ",
                "style='display:inline-block; width:150px;'>",
                "<button class='navlit'>Issue</button>", //
                "</form>",
                "<form method='get' action='testing_collect_calc.html' ",
                "style='display:inline-block; width:150px;'>",
                "<button class='nav'>Collect</button>", //
                "</form>");

        htm.hr().div("vgap");
        htm.sH(3).add("Testing Center & Quiet Testing").eH(3);

        htm.addln("<form method='get' action='testing_issue_exam.html'>");
        htm.add("<button class='nav'>Issue Exam</button>");
        htm.addln("</form>");

        htm.hr().div("vgap");

        htm.eDiv(); // buttonstack

        final String stuId = req.getParameter("stu");
        final String calc = req.getParameter("calc");

        // Enter the calculator ID.
        htm.addln("<form class='stuform' method='post' action='testing_issue_calc.html'>");

        if (stuId == null || stuId.isEmpty()) {
            // Request the student ID
            emitStudentIdField(htm, null, false, true);
            emitSubmit(htm, "Submit");
        } else {
            final String cleanStu = stuId.trim().replace(CoreConstants.SPC, CoreConstants.EMPTY)
                    .replace(CoreConstants.DASH, CoreConstants.EMPTY);
            final RawStudent stu = RawStudentLogic.query(cache, cleanStu, false);

            if (stu == null) {
                // Invalid student ID - request the student ID again
                emitStudentIdField(htm, null, false, true);
                emitError(htm, "Student not found");
                emitSubmit(htm, "Reset Form");
            } else {
                final String name = (stu.prefName == null ? stu.firstName : stu.prefName)
                        + CoreConstants.SPC + stu.lastName;

                if (calc == null) {
                    // Request the calculator ID
                    emitStudentIdField(htm, stuId, true, true);
                    emitInfo(htm, name);
                    emitCalcIdField(htm, null, true);
                    emitSubmit(htm, "Submit");
                } else {
                    final String cleanCalc =
                            calc.trim().replace(CoreConstants.SPC, CoreConstants.EMPTY);

                    Integer calcNum = null;
                    try {
                        calcNum = Integer.valueOf(cleanCalc);
                    } catch (final NumberFormatException ex) {
                        Log.warning("Invalid calculator number entered: ", calc, ex);
                    }

                    if (calcNum == null) {
                        // Bad calculator number - request again
                        emitStudentIdField(htm, stuId, true, true);
                        emitInfo(htm, name);
                        emitCalcIdField(htm, null, false);
                        emitError(htm, "Invalid calculator number");
                        emitSubmit(htm, "Submit");
                    } else {
                        // Valid...

                        final LocalTime time = LocalTime.now();
                        final int serial =
                                time.getHour() * 3600 + time.getMinute() * 60 + time.getSecond();

                        final RawCalcs issued = new RawCalcs(stuId, cleanCalc, "0",
                                Long.valueOf(serial), LocalDate.now());

                        if (RawCalcsLogic.INSTANCE.insert(cache, issued)) {
                            emitStudentIdField(htm, stuId, true, false);
                            emitInfo(htm, name);
                            emitCalcIdField(htm, calc, false);
                            emitInfo(htm, "This calculator has been issued.");
                            emitClose(htm);
                        } else {
                            emitStudentIdField(htm, stuId, true, false);
                            emitInfo(htm, name);
                            emitCalcIdField(htm, calc, false);
                            emitError(htm, "There was an error issuing the calculator!");
                            emitSubmit(htm, "Clear");
                        }
                    }
                }
            }
        }

        htm.addln("</form>");

        htm.eDiv(); // buttonstack

        htm.eDiv(); // Center

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Processes the POST from the form to issue a make-up exam. This method validates the request parameters, and
     * inserts a new record of an in-progress make-up exam, then prints status.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doPost(final Cache cache, final AdminSite site, final ServletRequest req,
                       final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        doGet(cache, site, req, resp, session);
    }

    /**
     * Emits the labeled field for the student ID.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param value    the initial value of the field
     * @param readonly true if the field should be read-only
     * @param include  true to include field in form submit
     */
    private static void emitStudentIdField(final HtmlBuilder htm, final String value,
                                           final boolean readonly, final boolean include) {

        htm.sTable().sTr();
        htm.sTd("r").add("Student&nbsp;ID:").eTd().sTd();
        htm.add("<input type='text' size='16'");
        if (include) {
            htm.add(" name='stu'");
        }
        if (readonly) {
            htm.add(" readonly");
        } else {
            htm.add(" autocomplete='off' data-lpignore='true' autofocus");
        }
        if (value != null) {
            htm.add(" value='", value, "'");
        }
        htm.add("/>");
        htm.eTd().eTr().eTable();
    }

    /**
     * Emits the labeled field for the calculator ID.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param value   the initial value of the field
     * @param include true to include field in form submit
     */
    private static void emitCalcIdField(final HtmlBuilder htm, final String value, final boolean include) {

        htm.sTable().sTr();
        htm.sTd("r").add("Calculator&nbsp;Number:").eTd().sTd();
        htm.add("<input type='text' size='8'");
        if (include) {
            htm.add(" name='calc'");
        }
        htm.add(" autocomplete='off' data-lpignore='true' autofocus");
        if (value != null) {
            htm.add(" value='", value, "'");
        }
        htm.add("/>");
        htm.eTd().eTr().eTable();
    }

    /**
     * Emits an informational message.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param message the message
     */
    private static void emitInfo(final HtmlBuilder htm, final String message) {

        htm.sP("info").add(message).eP();
    }

    /**
     * Emits an error message.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param message the message
     */
    private static void emitError(final HtmlBuilder htm, final String message) {

        htm.sP("error").add(message).eP();
    }

    /**
     * Emits the form submit button with a specified label.
     *
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param label the button label
     */
    private static void emitSubmit(final HtmlBuilder htm, final String label) {

        htm.div("vgap");
        htm.addln("<button class='btn' type='submit'>", label, "</button>");
    }

    /**
     * Emits the form submit button with a specified label.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitClose(final HtmlBuilder htm) {

        htm.div("vgap");
        htm.addln("<a class='btn' href='testing.html'>Ok</a>");
    }
}

package dev.mathops.web.site.admin.office;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawCalcsLogic;
import dev.mathops.db.old.rawrecord.RawCalcs;
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

/**
 * A page to handle issuance of testing center calculators.
 */
enum PageTestingCollectCalc {
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
                "<button class='nav'>Issue</button>", //
                "</form>",
                "<form method='get' action='testing_collect_calc.html' ",
                "style='display:inline-block; width:150px;'>",
                "<button class='navlit'>Collect</button>", //
                "</form>");

        htm.hr().div("vgap");
        htm.sH(3).add("Testing Center & Quiet Testing").eH(3);

        htm.addln("<form method='get' action='testing_issue_exam.html'>");
        htm.add("<button class='nav'>Issue Exam</button>");
        htm.addln("</form>");

        htm.hr().div("vgap");

        htm.eDiv(); // buttonstack

        final String calc = req.getParameter("calc");

        // Enter the calculator ID.
        htm.addln("<form class='stuform' method='post' action='testing_collect_calc.html'>");

        if (calc == null) {
            // Request the calculator ID
            emitCalcIdField(htm, null, true);
            emitSubmit(htm, "Submit");
        } else {
            final String cleanCalc = calc.trim().replace(CoreConstants.SPC, CoreConstants.EMPTY);

            Integer calcNum = null;
            try {
                calcNum = Integer.valueOf(cleanCalc);
            } catch (final NumberFormatException ex) {
                Log.warning("Invalid calculator number entered: ", calc, ex);
            }

            if (calcNum == null) {
                // Bad calculator number - request again
                emitCalcIdField(htm, null, false);
                emitError(htm, "Invalid calculator number");
                emitSubmit(htm, "Reset Form");
            } else {
                final RawCalcs found = RawCalcsLogic.queryByCalculatorId(cache, cleanCalc);

                if (found == null) {
                    // Bad calculator number - request again
                    emitCalcIdField(htm, null, false);
                    emitError(htm, "That calculator is not currently checked out.");
                    emitSubmit(htm, "Reset Form");
                } else if (RawCalcsLogic.delete(cache, found)) {
                    emitCalcIdField(htm, calc, false);
                    emitInfo(htm, "This calculator has been returned.");
                    emitClose(htm);
                } else {
                    emitCalcIdField(htm, calc, false);
                    emitError(htm, "There was an error returning the calculator!");
                    emitSubmit(htm, "Clear");
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

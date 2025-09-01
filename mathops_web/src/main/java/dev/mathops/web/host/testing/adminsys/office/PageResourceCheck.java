package dev.mathops.web.host.testing.adminsys.office;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawResourceLogic;
import dev.mathops.db.old.rawlogic.RawStresourceLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.schema.legacy.RawResource;
import dev.mathops.db.schema.legacy.RawStresource;
import dev.mathops.db.schema.legacy.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.testing.adminsys.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * A page to manage resources.
 */
enum PageResourceCheck {
    ;

    /**
     * Generates the page that prompts the user to log in.
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
        htm.sH(2).add("Resource Loan and Return").eH(2);
        htm.div("vgap2");

        htm.sDiv("buttonstack");
        htm.addln("<form method='get' action='resource_loan.html'>");
        htm.add("<button class='nav'>Loan Item</button>");
        htm.addln("</form>");

        htm.addln("<form method='get' action='resource_return.html'>");
        htm.add("<button class='nav'>Return Item</button>");
        htm.addln("</form>");

        htm.addln("<form method='get' action='resource_check.html'>");
        htm.add("<button class='navlit'>Check Student Loans</button>");
        htm.addln("</form>");

        htm.eDiv(); // buttonstack

        final String stuId = req.getParameter("stu");

        // Scan the student ID, then scan the resource ID
        htm.addln("<form class='stuform' method='post' action='resource_check.html'>");

        if (stuId == null || stuId.isEmpty()) {
            // Request the student ID
            emitStudentIdField(htm, null, false);
            emitSubmit(htm, "Submit");
        } else {
            final String cleanStu = stuId.trim().replace(CoreConstants.SPC, CoreConstants.EMPTY)
                    .replace(CoreConstants.DASH, CoreConstants.EMPTY);
            final RawStudent stu = RawStudentLogic.query(cache, cleanStu, false);

            if (stu == null) {
                // Invalid student ID - request the student ID again
                emitStudentIdField(htm, null, false);
                emitError(htm, "Student not found");
                emitSubmit(htm, "Reset Form");
            } else {
                final String name = (stu.prefName == null ? stu.firstName : stu.prefName)
                        + CoreConstants.SPC + stu.lastName;

                // Display student loans
                emitStudentIdField(htm, stuId, true);
                emitInfo(htm, name);

                final List<RawStresource> all = RawStresourceLogic.queryByStudent(cache, stuId);
                if (all.isEmpty()) {
                    emitInfo(htm, "No resources checked out");
                } else {
                    int numOpen = 0;
                    for (final RawStresource row : all) {
                        if (row.returnDt == null) {
                            ++numOpen;
                        }
                    }

                    if (numOpen == 0) {
                        emitInfo(htm, "No resources currently checked out");
                    } else {
                        htm.hr();
                        htm.sDiv(null, "style='text-align:left; font-size:smaller;'");
                        htm.addln("Resources currently checked out:");

                        for (final RawStresource row : all) {
                            if (row.returnDt == null) {
                                final RawResource res = RawResourceLogic.query(cache, row.resourceId);

                                htm.sP().add("&bull; ", res == null ? "Unknown resource" : res.resourceDesc,
                                        "<br/>checked out ", TemporalUtils.FMT_MDY.format(row.loanDt),
                                        " and due ", TemporalUtils.FMT_MDY.format(row.dueDt)).eP();
                            }
                        }
                        htm.eDiv();
                    }

                    if (numOpen < all.size()) {
                        htm.hr();
                        htm.sDiv(null, "style='text-align:left; font-size:smaller;'");
                        htm.addln("Resource checkout history:");

                        for (final RawStresource row : all) {
                            if (row.returnDt != null) {
                                final RawResource res = RawResourceLogic.query(cache, row.resourceId);

                                htm.sP().add("&bull; ", res == null ? "Unknown resource" : res.resourceDesc,
                                                "<br/>checked out ", TemporalUtils.FMT_MDY.format(row.loanDt)).eP();
                            }
                        }
                        htm.eDiv();
                    }

                    emitClose(htm);
                }
            }
        }

        htm.addln("</form>");

        htm.eDiv(); // Center

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits the labeled field for the student ID.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param value    the initial value of the field
     * @param readonly true if the field should be read-only
     */
    private static void emitStudentIdField(final HtmlBuilder htm, final String value, final boolean readonly) {

        htm.sTable().sTr();
        htm.sTd("r").add("Student&nbsp;ID:").eTd().sTd();
        htm.add("<input type='text' size='18' name='stu'");
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
     * Emits an error message.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param message the message
     */
    private static void emitError(final HtmlBuilder htm, final String message) {

        htm.sP("error").add(message).eP();
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
        htm.addln("<a class='btn' href='resource.html'>Ok</a>");
    }

    /**
     * Generates the page that prompts the user to log in.
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
}

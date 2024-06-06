package dev.mathops.web.site.admin.office;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.old.rawlogic.RawResourceLogic;
import dev.mathops.db.old.rawlogic.RawStresourceLogic;
import dev.mathops.db.old.rawrecord.RawResource;
import dev.mathops.db.old.rawrecord.RawStresource;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * A page to manage resources.
 */
enum PageResourceReturn {
    ;

    /**
     * Generates the page that prompts the user to log in.
     *
     * @param studentData the student data object
     * @param site        the owning site
     * @param req         the request
     * @param resp        the response
     * @param session     the user session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final StudentData studentData, final AdminSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = OfficePage.startOfficePage(studentData, site, session, true);

        htm.sDiv("center");
        htm.sH(2).add("Resource Loan and Return").eH(2);
        htm.div("vgap2");

        htm.sDiv("buttonstack");
        htm.addln("<form method='get' action='resource_loan.html'>");
        htm.add("<button class='nav'>Loan Item</button>");
        htm.addln("</form>");

        htm.addln("<form method='get' action='resource_return.html'>");
        htm.add("<button class='navlit'>Return Item</button>");
        htm.addln("</form>");

        htm.addln("<form method='get' action='resource_check.html'>");
        htm.add("<button class='nav'>Check Student Loans</button>");
        htm.addln("</form>");

        htm.eDiv(); // buttonstack

        final String resId = req.getParameter("res");

        // Scan the student ID, then scan the resource ID
        htm.addln("<form class='stuform' method='post' action='resource_return.html'>");

        if (resId == null || resId.isEmpty()) {
            // Request the student ID
            emitResourceIdField(htm, null, false, true);
            emitSubmit(htm, "Submit");
        } else {
            final String cleanRes = resId.trim().replace(CoreConstants.SPC, CoreConstants.EMPTY)
                    .replace(CoreConstants.DASH, CoreConstants.EMPTY);

            final RawStresource outstanding = RawStresourceLogic.queryOutstanding(cache, cleanRes);

            if (outstanding == null) {
                final RawResource res = RawResourceLogic.query(cache, cleanRes);

                emitResourceIdField(htm, resId, true, false);
                if (res == null) {
                    emitError(htm, "Invalid resource ID");
                } else {
                    emitError(htm, "Resource " + resId + " is not currently checked out");
                }
                emitSubmit(htm, "Clear");
            } else {
                final LocalDateTime now = session.getNow().toLocalDateTime();
                final int minute = now.getHour() * 60 + now.getMinute();

                if (RawStresourceLogic.updateReturnDateTime(cache, outstanding, now.toLocalDate(),
                        Integer.valueOf(minute))) {
                    emitResourceIdField(htm, resId, true, true);
                    emitInfo(htm, "Resource has been returned.");
                    emitClose(htm);
                } else {
                    emitResourceIdField(htm, resId, true, false);
                    emitError(htm, "Failed to check in resource!");
                    emitSubmit(htm, "Clear");
                }
            }
        }

        htm.addln("</form>");

        htm.eDiv(); // Center

        Page.endOrdinaryPage(studentData, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Emits the labeled field for the resource ID.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param value    the initial value of the field
     * @param readonly true if the field should be read-only
     * @param include  true to include field in form submit
     */
    private static void emitResourceIdField(final HtmlBuilder htm, final String value,
                                            final boolean readonly, final boolean include) {

        htm.sTable().sTr();
        htm.sTd("r").add("Resource&nbsp;ID:").eTd().sTd();
        htm.add("<input type='text' size='16' ");
        if (include) {
            htm.add(" name='res'");
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

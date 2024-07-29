package dev.mathops.web.site.admin.office;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawResourceLogic;
import dev.mathops.db.old.rawlogic.RawStresourceLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawResource;
import dev.mathops.db.old.rawrecord.RawStresource;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * A page to manage resources.
 */
enum PageResourceLoan {
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
        htm.add("<button class='navlit'>Loan Item</button>");
        htm.addln("</form>");

        htm.addln("<form method='get' action='resource_return.html'>");
        htm.add("<button class='nav'>Return Item</button>");
        htm.addln("</form>");

        htm.addln("<form method='get' action='resource_check.html'>");
        htm.add("<button class='nav'>Check Student Loans</button>");
        htm.addln("</form>");

        htm.eDiv(); // buttonstack

        final String stuId = req.getParameter("stu");
        final String resId = req.getParameter("res");

        // Scan the student ID, then scan the resource ID
        htm.addln("<form class='stuform' method='post' action='resource_loan.html'>");

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
            } else if ("F".equals(stu.sevAdminHold)) {
                // Student has hold (cannot check out resources) - display error
                emitStudentIdField(htm, null, false, true);
                emitError(htm, "Student has HOLD:");

                final List<RawAdminHold> holds = RawAdminHoldLogic.queryByStudent(cache, stu.stuId);

                for (final RawAdminHold hold : holds) {
                    final String msg = RawAdminHoldLogic.getStaffMessage(hold.holdId);
                    emitError(htm, Objects.requireNonNullElseGet(msg, () -> "Hold " + hold.holdId));
                }

                emitSubmit(htm, "Reset Form");
            } else {
                final String name = (stu.prefName == null ? stu.firstName : stu.prefName)
                        + CoreConstants.SPC + stu.lastName;

                if (resId == null) {
                    // Request the resource ID
                    emitStudentIdField(htm, stuId, true, true);
                    emitInfo(htm, name);
                    emitResourceIdField(htm, null, false, true);
                    emitSubmit(htm, "Submit");
                } else {
                    final String cleanRes = resId.trim().replace(CoreConstants.SPC, CoreConstants.EMPTY)
                            .replace(CoreConstants.DASH, CoreConstants.EMPTY);
                    final RawResource res = RawResourceLogic.query(cache, cleanRes);

                    if (res == null) {
                        // Bad resource ID - request again
                        emitStudentIdField(htm, stuId, true, true);
                        emitInfo(htm, name);
                        emitResourceIdField(htm, resId, true, false);
                        emitError(htm, "Resource not found");
                        emitSubmit(htm, "Rescan resource");
                    } else {
                        final Integer daysAllowed = res.daysAllowed;

                        final RawStresource outstanding =
                                RawStresourceLogic.queryOutstanding(cache, resId);

                        if (outstanding == null) {
                            final LocalDateTime now = session.getNow().toLocalDateTime();
                            final LocalDate today = now.toLocalDate();
                            final int start = now.getHour() * 60 + now.getMinute();

                            final LocalDate due = today.plusDays(
                                    (long) (daysAllowed == null ? 1 : daysAllowed.intValue()));

                            final RawStresource loan = new RawStresource(stu.stuId, res.resourceId,
                                    today, Integer.valueOf(start), due, null, null, Integer.valueOf(0),
                                    today);

                            if (RawStresourceLogic.INSTANCE.insert(cache, loan)) {
                                emitStudentIdField(htm, stuId, true, false);
                                emitInfo(htm, name);
                                emitResourceIdField(htm, resId, true, false);
                                emitInfo(htm, "Loan has been recorded.");
                                emitClose(htm);
                            } else {
                                emitStudentIdField(htm, stuId, true, false);
                                emitInfo(htm, name);
                                emitResourceIdField(htm, resId, true, false);
                                emitError(htm, "There was an error recording the loan!");
                                emitSubmit(htm, "Clear");
                            }

                        } else {
                            // Resource ID already checked out - request again (unless checked out
                            // to the requesting student - then we're done)
                            emitStudentIdField(htm, stuId, true, true);
                            emitInfo(htm, name);
                            emitResourceIdField(htm, resId, true, true);
                            emitError(htm, "Resource already checked out");
                            emitSubmit(htm, "Submit");
                        }
                    }
                }
            }
        }

        htm.addln("</form>");

        htm.eDiv(); // Center

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
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
     * Emits the labeled field for the resource ID.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param value    the initial value of the field
     * @param readonly true if the field should be read-only
     * @param include  true to include field in form submit
     */
    private static void emitResourceIdField(final HtmlBuilder htm, final String value,
                                            final boolean readonly, final boolean include) {

        htm.div("vgap");

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

package dev.mathops.web.site.admin.sysadmin.turn;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.sysadmin.ESysadminTopic;
import dev.mathops.web.site.admin.sysadmin.SysAdminPage;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Top-level page for database server management.
 */
public enum PageTurnServers {
    ;

    /**
     * Generates the page.
     *
     * @param studentData the student data object
     * @param site        the owning site
     * @param req         the request
     * @param resp        the response
     * @param session     the user's session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final StudentData studentData, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        if (session.role == ERole.SYSADMIN) {
            final HtmlBuilder htm = SysAdminPage.startSysAdminPage(studentData, site, session);

            SysAdminPage.emitNavBlock(ESysadminTopic.TURN_SERVERS, htm);
            emitPageContent(htm);

            SysAdminPage.endSysAdminPage(studentData, htm, site, req, resp);
        } else {
            SysAdminPage.sendNotAuthorizedPage(studentData, site, req, resp);
        }
    }

    /**
     * Emits the content of the page.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitPageContent(final HtmlBuilder htm) {

        htm.sH(2).add("TURN Servers").eH(2);
    }
}

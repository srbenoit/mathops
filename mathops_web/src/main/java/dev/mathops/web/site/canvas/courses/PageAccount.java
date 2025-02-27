package dev.mathops.web.site.canvas.courses;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.reclogic.TermLogic;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.canvas.CanvasPageUtils;
import dev.mathops.web.site.canvas.CanvasSite;
import dev.mathops.web.site.canvas.ECanvasPanel;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * This page shows the "Account" content.
 */
public enum PageAccount {
    ;

    /**
     * Starts the page that shows the status of all assignments and grades.
     *
     * @param cache    the data cache
     * @param site     the owning site
     * @param courseId the course ID
     * @param req      the request
     * @param resp     the response
     * @param session  the user's login session information
     * @param metadata the metadata object with course structure data
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final CanvasSite site, final String courseId, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session,
                             final Metadata metadata) throws IOException,
            SQLException {

        final String stuId = session.getEffectiveUserId();
        final RawStcourse registration = CanvasPageUtils.confirmRegistration(cache, stuId, courseId);

        if (registration == null) {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
            final MetadataCourse metaCourse = metadata.getCourse(registration.course);
            if (metaCourse == null) {
                // TODO: Error display, course not part of this system rather than a redirect to Home
                final String homePath = site.makeRootPath("home.htm");
                resp.sendRedirect(homePath);
            } else {
                presentAccount(cache, site, req, resp, session, registration, metaCourse);
            }

        }
    }

    /**
     * Presents the "Account" information.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     * @param metaCourse   the metadata object with course structure data
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentAccount(final Cache cache, final CanvasSite site, final ServletRequest req,
                               final HttpServletResponse resp, final ImmutableSessionInfo session,
                               final RawStcourse registration, final MetadataCourse metaCourse)
            throws IOException, SQLException {

        final TermRec active = TermLogic.get(cache).queryActive(cache);
        final List<RawCsection> csections = RawCsectionLogic.queryByTerm(cache, active.term);

        RawCsection csection = null;
        for (final RawCsection test : csections) {
            if (registration.course.equals(test.course) && registration.sect.equals(test.sect)) {
                csection = test;
                break;
            }
        }

        if (csection == null) {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            final String siteTitle = site.getTitle();

            CanvasPageUtils.startPage(htm, siteTitle);

            // Emit the course number and section at the top
            CanvasPageUtils.emitCourseTitleAndSection(htm, metaCourse, csection);

            htm.sDiv("pagecontainer");

            CanvasPageUtils.emitLeftSideMenu(htm, metaCourse.id, ECanvasPanel.ACCOUNT);

            htm.sDiv("flexmain");

            htm.sH(2).add("Account Preferences").eH(2);
            htm.hr();

            CanvasPageUtils.endPage(htm);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
        }
    }
}

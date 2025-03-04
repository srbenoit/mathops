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
 * This page shows the "Getting Help" content.
 */
public enum PageHelp {
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
                             final Metadata metadata) throws IOException, SQLException {

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
                presentSyllabus(cache, site, req, resp, session, registration, metaCourse);
            }
        }
    }

    /**
     * Presents the "Syllabus" information.
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
    static void presentSyllabus(final Cache cache, final CanvasSite site, final ServletRequest req,
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

            CanvasPageUtils.emitLeftSideMenu(htm, metaCourse, ECanvasPanel.GETTING_HELP);

            htm.sDiv("flexmain");

            htm.sH(2).add("Getting Help").eH(2);
            htm.hr();

            htm.sH(3).add("In-Person Help").eH(3);

            htm.sP().add("The Precalculus Center has a staff of trained learning assistants who can provide help with ",
                    "the math content of MATH 117 through MATH 126 in our Learning Center, Weber 136.").eP();

            htm.sH(4).add("In-Person Help Hours in the Precalculus Center").eH(4);

            htm.addln("<ul>");
            htm.addln("<li> January 21 through May 9, 2025").br()
                    .add("&nbsp; 10:00 am - 4:00 pm, Monday").br()
                    .add("&nbsp; 10:00 am - 8:00 pm, Tuesday - Thursday").br()
                    .add("10:00 am - 4:00 pm, Friday").br()
                    .add("Noon - 4:00 pm, Sunday</li>");
            htm.addln("</ul>");

            htm.sP().add("The Precalculus Center will be closed March 15 through 23.").eP();
            htm.hr();

            htm.sH(3).add("Online Help").eH(3);

            htm.sH(4).add("Online Help Hours via an open Microsoft Teams meeting").eH(4);

            htm.addln("<ul>");
            htm.addln("<li> January 28 through May 9, 2025 (except closure dates noted above)").br()
                    .add("&nbsp; Monday  10:00 am - noon   and   1:00 pm - 4:00 pm").br()
                    .add("&nbsp; Tuesday  10:00 am - 7:00 pm").br()
                    .add("&nbsp; Wednesday  1:00 pm - 3:00 pm   and   5:00 pm - 8:00 pm").br()
                    .add("&nbsp; Thursday  10:00 am - noon   and   1:00 pm - 5:00 pm").br()
                    .add("&nbsp; Friday  10:00 am - 1:00 pm</li>");
            htm.addln("</ul>");

            htm.sP().add(
                    "<a href='https://teams.microsoft" +
                    ".com/l/meetup-join/19%3ameeting_NWZmZGQ4OTctZmJhMC00NmQ1LWFjMjUtOWZiNTk1ZjdmODhi%40thread" +
                    ".v2/0?context=%7b%22Tid%22%3a%22afb58802-ff7a-4bb1-ab21-367ff2ecfc8b%22%2c%22Oid%22%3a" +
                    "%22ebcdd035-78e6-465a-9538-ad4f39aefb65%22%7d'>Join Microsoft Teams Meeting</a>").eP();
            htm.hr();

            htm.sH(3).add("In - Person Help in the Adult Learners and Veteran Services Office").eH(3);

            htm.sH(4).add("In - Person Help Hours in the ALVS Office (LSC 282)").eH(4);

            htm.addln("<ul>");
            htm.addln("<li> January 28 through May 9, 2025 (except closure dates noted above)").br()
                    .add("&nbsp; 2:00 pm - 4:00 pm, Monday").br()
                    .add("&nbsp; Noon - 2:00 pm, Tuesday").br()
                    .add("&nbsp; 11:00 am - 1:00 pm, Thursday/li>");
            htm.addln("</ul>");

            CanvasPageUtils.endPage(htm);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
        }
    }
}

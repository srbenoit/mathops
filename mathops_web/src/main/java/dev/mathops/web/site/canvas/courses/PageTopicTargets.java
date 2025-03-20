package dev.mathops.web.site.canvas.courses;

import dev.mathops.commons.log.Log;
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
 * This page shows the learning targets and exam status associated with a topic module.
 */
public enum PageTopicTargets {
    ;

    /**
     * Starts the page that shows the course outline with student progress.
     *
     * @param cache    the data cache
     * @param site     the owning site
     * @param courseId the course ID
     * @param topicId  the topic ID
     * @param req      the request
     * @param resp     the response
     * @param session  the user's login session information
     * @param metadata the metadata object with course structure data
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final CanvasSite site, final String courseId,
                             final String topicId, final ServletRequest req, final HttpServletResponse resp,
                             final ImmutableSessionInfo session, final Metadata metadata)
            throws IOException, SQLException {

        final String stuId = session.getEffectiveUserId();
        final RawStcourse registration = CanvasPageUtils.confirmRegistration(cache, stuId, courseId);

        if (registration == null) {
            Log.warning("No registration found for student ", stuId, " in course ", courseId);
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
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
                Log.warning("No course section record for ", courseId, " section ", registration.sect);
                final String homePath = site.makeRootPath("home.html");
                resp.sendRedirect(homePath);
            } else {
                final MetadataCourse metaCourse = metadata.getCourse(registration.course);
                if (metaCourse == null) {
                    Log.warning("No course metadata for ", courseId);
                    // TODO: Error display, course not part of this system rather than a redirect to Home
                    final String homePath = site.makeRootPath("home.htm");
                    resp.sendRedirect(homePath);
                } else {
                    boolean seeking = true;
                    for (final MetadataCourseModule metaCourseModule : metaCourse.modules) {
                        if (topicId.equals(metaCourseModule.id)) {
                            presentTargetsPage(cache, site, req, resp, registration, csection, metaCourse,
                                    metaCourseModule);
                            seeking = false;
                            break;
                        }
                    }
                    if (seeking) {
                        Log.warning("No course topic metadata for topic ", topicId, " in ", courseId);
                        final String homePath = site.makeRootPath("home.html");
                        resp.sendRedirect(homePath);
                    }
                }
            }
        }
    }

    /**
     * Presents  a course and module.
     *
     * @param cache            the data cache
     * @param site             the owning site
     * @param req              the request
     * @param resp             the response
     * @param registration     the student registration record
     * @param csection         the course section configuration record
     * @param metaCourse       metadata related to the course
     * @param metaCourseModule metadata related to the topic module within the course
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentTargetsPage(final Cache cache, final CanvasSite site, final ServletRequest req,
                                   final HttpServletResponse resp, final RawStcourse registration,
                                   final RawCsection csection, final MetadataCourse metaCourse,
                                   final MetadataCourseModule metaCourseModule) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();

        CanvasPageUtils.startPage(htm, siteTitle);

        // Emit the course number and section at the top
        CanvasPageUtils.emitCourseTitleAndSection(htm, metaCourse, csection);

        htm.sDiv("pagecontainer");

        CanvasPageUtils.emitLeftSideMenu(htm, metaCourse, "../", ECanvasPanel.MODULES);

        htm.sDiv("flexmain");

        final MetadataTopic meta = metaCourseModule.topicMetadata;

        htm.sH(2);
        if (meta.thumbnailFile != null) {
            final String imageUrl = "/media/" + metaCourseModule.directory + "/" + meta.thumbnailFile;
            if (meta.thumbnailAltText == null) {
                htm.addln("<img class='module-thumb' src='", imageUrl, "'/>");
            } else {
                htm.addln("<img class='module-thumb' src='", imageUrl, "' alt='", meta.thumbnailAltText, "'/>");
            }
        }
        htm.sDiv("module-title");
        htm.add(metaCourseModule.heading, " Learning Targets").br();
        htm.addln("<div style='color:#D9782D; margin-top:6px;'>", meta.title, "</div>");
        htm.addln("<a class='smallbtn' href='module.html'>Open Textbook Chapter</a>");
        htm.eDiv();
        htm.eH(2);

        htm.hr();
        htm.div("vgap0");

        htm.sDiv("left");
        htm.addln("<img class='module-thumb' src='/www/images/etext/target_thumb.png' ",
                "alt='A dartboard with several magnetic darts.'/>");
        htm.eDiv();

        htm.sH(3).add("Module Learning Targets").eH(3);
        htm.sDiv("clear").eDiv();

        htm.eDiv(); // flexmain
        htm.eDiv(); // pagecontainer

        CanvasPageUtils.endPage(htm);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }
}

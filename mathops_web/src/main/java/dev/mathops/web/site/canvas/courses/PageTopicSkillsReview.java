package dev.mathops.web.site.canvas.courses;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.MainData;
import dev.mathops.db.logic.TermData;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.rec.main.StandardsCourseModuleRec;
import dev.mathops.db.rec.main.StandardsCourseRec;
import dev.mathops.db.rec.term.StandardsCourseSectionRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.canvas.CanvasPageUtils;
import dev.mathops.web.site.canvas.CanvasSite;
import dev.mathops.web.site.canvas.ECanvasPanel;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * This page shows the Skills Review for a topic module.
 */
public enum PageTopicSkillsReview {
    ;

    /** The URL prefix for the video server. */
    private static final String VIDEO_URL = "https://nibbler.math.colostate.edu/media/";

    /**
     * Starts the page that shows the course outline with student progress.
     *
     * @param cache     the data cache
     * @param site      the owning site
     * @param courseId  the course ID
     * @param moduleNbr the module number
     * @param req       the request
     * @param resp      the response
     * @param session   the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final CanvasSite site, final String courseId,
                             final Integer moduleNbr, final ServletRequest req, final HttpServletResponse resp,
                             final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String stuId = session.getEffectiveUserId();
        final RawStcourse registration = CanvasPageUtils.confirmRegistration(cache, stuId, courseId);

        if (registration == null) {
            Log.warning("No registration found for student ", stuId, " in course ", courseId);
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
            final MainData mainData = cache.getMainData();
            final StandardsCourseRec course = mainData.getStandardsCourse(registration.course);
            if (course == null) {
                // TODO: Error display, course not part of this system rather than a redirect to Home
                Log.warning("No course record for ", courseId);
                final String homePath = site.makeRootPath("home.htm");
                resp.sendRedirect(homePath);
            } else {
                final TermData termData = cache.getTermData();
                final StandardsCourseSectionRec section = termData.getStandardsCourseSection(registration.course,
                        registration.sect);

                if (section == null) {
                    Log.warning("No course section record for ", courseId, " section ", registration.sect);
                    final String homePath = site.makeRootPath("home.html");
                    resp.sendRedirect(homePath);
                } else {
                    final StandardsCourseModuleRec module = mainData.getStandardsCourseModule(courseId, moduleNbr);
                    if (module == null) {
                        Log.warning("No course module record for module ", moduleNbr, " in ", courseId);
                        final String homePath = site.makeRootPath("home.html");
                        resp.sendRedirect(homePath);
                    } else {
                        presentSkillsReviewPage(cache, site, req, resp, registration, section, course,
                                module);
                    }
                }
            }
        }
    }

    /**
     * Presents  a course and module.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param registration the student registration record
     * @param section      the course section object
     * @param course       the course object
     * @param module       the course module object
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentSkillsReviewPage(final Cache cache, final CanvasSite site, final ServletRequest req,
                                        final HttpServletResponse resp, final RawStcourse registration,
                                        final StandardsCourseSectionRec section, final StandardsCourseRec course,
                                        final StandardsCourseModuleRec module) throws IOException, SQLException {

        final MetadataTopic meta = metaCourseModule.topicMetadata;

        if (meta.isValid()) {
            emitTopicSkillsReview(cache, site, req, resp, registration, section, course, module,
                    metaCourseModule.topicModuleDir);
        }
    }

    /**
     * Presents  a course and module.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param registration the student registration record
     * @param section      the course section object
     * @param course       the course object
     * @param module       the course module object
     * @param topicDir     the directory in which to locate topic media
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitTopicSkillsReview(final Cache cache, final CanvasSite site, final ServletRequest req,
                                              final HttpServletResponse resp, final RawStcourse registration,
                                              final StandardsCourseSectionRec section, final StandardsCourseRec course,
                                              final StandardsCourseModuleRec module, final File topicDir)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();

        CanvasPageUtils.startPage(htm, siteTitle);

        // Emit the course number and section at the top
        CanvasPageUtils.emitCourseTitleAndSection(htm, course, section);

        htm.sDiv("pagecontainer");

        CanvasPageUtils.emitLeftSideMenu(htm, course, "../", ECanvasPanel.MODULES);

        htm.sDiv("flexmain");

        htm.sH(2);
        if (meta.thumbnailFile != null) {
            final String imageUrl = "/media/" + metaCourseTopic.directory + "/" + meta.thumbnailFile;
            if (meta.thumbnailAltText == null) {
                htm.addln("<img class='module-thumb' src='", imageUrl, "'/>");
            } else {
                htm.addln("<img class='module-thumb' src='", imageUrl, "' alt='", meta.thumbnailAltText, "'/>");
            }
        }
        htm.sDiv("module-title");
        htm.add(metaCourseTopic.heading, " Textbook Chapter").br();
        htm.addln("<div style='color:#D9782D; margin-top:6px;'>", meta.title, "</div>");
        htm.eDiv();
        htm.eH(2);

        htm.hr();
        htm.div("vgap0");

        htm.sDiv("left");
        htm.addln("<img class='module-thumb' src='/www/images/etext/skills_review.png' ",
                "alt='A set of connected links in the shape of a brain.'/>");
        htm.eDiv();

        htm.sH(3).add("Skills Review").eH(3);
        htm.addln("<a class='ulink' href='module.html'>Return to Textbook Chapter</a>");
        htm.sDiv("clear").eDiv();

        final File dir = new File(topicDir, "10_skills_review");

        if (dir.exists() && dir.isDirectory()) {

            // TODO: Scan for topics and present...
        }

        htm.eDiv(); // flexmain
        htm.eDiv(); // pagecontainer

        CanvasPageUtils.endPage(htm);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }
}

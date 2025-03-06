package dev.mathops.web.site.canvas.courses;

import dev.mathops.commons.installation.EPath;
import dev.mathops.commons.installation.PathList;
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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * This page shows a module Topic.
 */
public enum PageTopicModule {
    ;

    /** The URL prefix for the video server. */
    private static final String VIDEO_URL = "https://nibbler.math.colostate.edu/media/";

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
                    for (final MetadataCourseTopic metaCourseTopic : metaCourse.topics) {
                        if (topicId.equals(metaCourseTopic.id)) {
                            presentTopicPage(cache, site, req, resp, registration, csection, metaCourse,
                                    metaCourseTopic);
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
     * @param cache           the data cache
     * @param site            the owning site
     * @param req             the request
     * @param resp            the response
     * @param registration    the student registration record
     * @param csection        the course section configuration record
     * @param metaCourse      metadata related to the course
     * @param metaCourseTopic metadata related to the topic within the course
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentTopicPage(final Cache cache, final CanvasSite site, final ServletRequest req,
                                 final HttpServletResponse resp, final RawStcourse registration,
                                 final RawCsection csection, final MetadataCourse metaCourse,
                                 final MetadataCourseTopic metaCourseTopic) throws IOException, SQLException {

        final File wwwPath = PathList.getInstance().get(EPath.WWW_PATH);
        final File publicPath = wwwPath.getParentFile();
        final File mediaPath = new File(publicPath, "media");

        final File topicDir = new File(mediaPath, metaCourseTopic.directory);
        final File topicMetaFile = new File(topicDir, "metadata.json");

        if (topicDir.exists()) {
            if (topicMetaFile.exists()) {
                final MetadataTopic meta = PageModules.loadTopicMetadata(topicMetaFile);

                if (Objects.nonNull(meta.title)) {
                    emitTopicModule(cache, site, req, resp, registration, csection, metaCourse, metaCourseTopic,
                            meta, topicDir);
                }
            } else {
                final String topicMetaFilePath = topicMetaFile.getAbsolutePath();
                Log.warning("Topic metadata file (", topicMetaFilePath, ") does not exist");
            }
        } else {
            final String topicDirPath = topicDir.getAbsolutePath();
            Log.warning("Topic directory (", topicDirPath, ") does not exist");
        }
    }

    /**
     * Presents  a course and module.
     *
     * @param cache           the data cache
     * @param site            the owning site
     * @param req             the request
     * @param resp            the response
     * @param registration    the student registration record
     * @param csection        the course section configuration record
     * @param metaCourse      metadata related to the course
     * @param metaCourseTopic metadata related to the course topic
     * @param meta            metadata related to the topic
     * @param topicDir        the directory in which to locate topic media
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitTopicModule(final Cache cache, final CanvasSite site, final ServletRequest req,
                                        final HttpServletResponse resp, final RawStcourse registration,
                                        final RawCsection csection, final MetadataCourse metaCourse,
                                        final MetadataCourseTopic metaCourseTopic, final MetadataTopic meta,
                                        final File topicDir) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();

        CanvasPageUtils.startPage(htm, siteTitle);

        // Emit the course number and section at the top
        CanvasPageUtils.emitCourseTitleAndSection(htm, metaCourse, csection);

        htm.sDiv("pagecontainer");

        CanvasPageUtils.emitLeftSideMenu(htm, metaCourse, "../", ECanvasPanel.MODULES);

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

        presentIntroductoryLessons(htm, topicDir);
        presentSkillsReview(htm, topicDir);
        presentLearningTargets(htm, topicDir);
        presentExplorations(htm, topicDir);
        presentApplications(htm, topicDir);
        presentConcludingLessons(htm, topicDir);

        htm.eDiv(); // flexmain
        htm.eDiv(); // pagecontainer

        CanvasPageUtils.endPage(htm);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }

    /**
     * Presents any introductory lessons found in the topic directory.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param topicDir the directory in which to locate topic media
     */
    private static void presentIntroductoryLessons(final HtmlBuilder htm, final File topicDir) {

        for (int i = 1; i < 9; ++i) {
            final String dirName = "0" + i + "_intro_" + i;
            final File dir = new File(topicDir, dirName);

            if (dir.exists() && dir.isDirectory()) {

                htm.addln("<details class='module'>");
                htm.add("  <summary class='module-summary'>");
                htm.add("Introductory Lesson " + i);
                htm.addln("</summary>");

                // TODO: Emit lesson media

                htm.addln("</details>");
            } else {
                break;
            }
        }
    }

    /**
     * Presents the module Skills Review content, if found.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param topicDir the directory in which to locate topic media
     */
    private static void presentSkillsReview(final HtmlBuilder htm, final File topicDir) {

        final File dir = new File(topicDir, "10_skills_review");

        if (dir.exists() && dir.isDirectory()) {

            htm.addln("<details class='module'>");
            htm.add("  <summary class='module-summary'>");
            htm.add("Skills Review");
            htm.addln("</summary>");

            // TODO: Emit Skills Review content

            htm.addln("</details>");
        }
    }

    /**
     * Presents the module learning targets.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param topicDir the directory in which to locate topic media
     */
    private static void presentLearningTargets(final HtmlBuilder htm, final File topicDir) {

        int count = 0;
        for (int i = 1; i < 9; ++i) {
            final String dirName = "1" + i + "_standard_" + i;
            final File dir = new File(topicDir, dirName);

            if (dir.exists() && dir.isDirectory()) {

                htm.addln("<details class='module'>");
                htm.add("  <summary class='module-summary'>");
                htm.add("Learning Target " + i);
                htm.addln("</summary>");

                // TODO: Emit standard content

                htm.addln("</details>");
                ++count;
            } else {
                break;
            }
        }

        if (count == 9) {
            for (int i = 10; i < 19; ++i) {
                final String dirName = (10 + i) + "_standard_" + i;
                final File dir = new File(topicDir, dirName);

                if (dir.exists() && dir.isDirectory()) {

                    htm.addln("<details class='module'>");
                    htm.add("  <summary class='module-summary'>");
                    htm.add("Learning Target " + i);
                    htm.addln("</summary>");

                    // TODO: Emit standard content

                    htm.addln("</details>");
                    ++count;
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Presents all module-level explorations found.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param topicDir the directory in which to locate topic media
     */
    private static void presentExplorations(final HtmlBuilder htm, final File topicDir) {

        final File dir = new File(topicDir, "40_explorations");

        if (dir.exists() && dir.isDirectory()) {

            htm.addln("<details class='module'>");
            htm.add("  <summary class='module-summary'>");
            htm.add("Explorations (Optional)");
            htm.addln("</summary>");

            // TODO: Emit Explorations

            htm.addln("</details>");
        }
    }

    /**
     * Presents all module-level applications found.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param topicDir the directory in which to locate topic media
     */
    private static void presentApplications(final HtmlBuilder htm, final File topicDir) {

        final File dir = new File(topicDir, "41_applications");

        if (dir.exists() && dir.isDirectory()) {

            htm.addln("<details class='module'>");
            htm.add("  <summary class='module-summary'>");
            htm.add("Applications (Optional)");
            htm.addln("</summary>");

            // TODO: Emit Applications

            htm.addln("</details>");
        }
    }

    /**
     * Presents any concluding lessons found in the topic directory.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param topicDir the directory in which to locate topic media
     */
    private static void presentConcludingLessons(final HtmlBuilder htm, final File topicDir) {

        for (int i = 1; i < 9; ++i) {
            final String dirName = "9" + i + "_conclusion_" + i;
            final File dir = new File(topicDir, dirName);

            if (dir.exists() && dir.isDirectory()) {

                htm.addln("<details class='module'>");
                htm.add("  <summary class='module-summary'>");
                htm.add("Concluding Lesson " + i);
                htm.addln("</summary>");

                // TODO: Emit lesson media

                htm.addln("</details>");
            } else {
                break;
            }
        }
    }
}

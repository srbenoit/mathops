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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * This page shows a module Topic.
 */
public enum PageTopicModule {
    ;

    /** The URL prefix for the video server. */
    private static final String VIDEO_URL = "https://nibbler.math.colostate.edu/media/";

    /** The URL prefix for the web server. */
    private static final String WEB_URL = "/media/";

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
                            presentTopicPage(cache, site, req, resp, registration, csection, metaCourse,
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
    static void presentTopicPage(final Cache cache, final CanvasSite site, final ServletRequest req,
                                 final HttpServletResponse resp, final RawStcourse registration,
                                 final RawCsection csection, final MetadataCourse metaCourse,
                                 final MetadataCourseModule metaCourseModule) throws IOException, SQLException {

        final MetadataTopic meta = metaCourseModule.topicMetadata;

        if (meta.isValid()) {
            emitTopicModule(cache, site, req, resp, registration, csection, metaCourse, metaCourseModule,
                    meta, metaCourseModule.topicModuleDir);
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
                                        final MetadataCourseModule metaCourseTopic, final MetadataTopic meta,
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

        doModuleIntroLessons(htm, topicDir);
        doModuleSkillsReview(htm, topicDir);
        doModuleStandards(htm, metaCourseTopic.directory, topicDir);
        doModuleExplorations(htm, topicDir);
        doModuleApplications(htm, topicDir);
        doModuleConcludingLessons(htm, topicDir);

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
    private static void doModuleIntroLessons(final HtmlBuilder htm, final File topicDir) {

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
    private static void doModuleSkillsReview(final HtmlBuilder htm, final File topicDir) {

        final File dir = new File(topicDir, "10_skills_review");

        if (dir.exists() && dir.isDirectory()) {
            final MetadataSkillsReview meta = new MetadataSkillsReview(dir);

            htm.addln("<details class='module'>");
            htm.add("  <summary class='module-summary'>Skills Review</summary>");
            htm.sDiv("module-item");

            htm.sDiv("left");
            htm.addln("<img class='module-thumb' src='/www/images/etext/skills_review.png' ",
                    "alt='A set of connected links in the shape of a brain.'/>");
            htm.eDiv();

            if (meta.description == null) {
                htm.addln("The <span style='color:#D9782D'>Skills Review</span> provides a refresher of skills from ",
                        "prior courses that we use in this chapter.  Using this review is optional, but if you need ",
                        "it, it's here.");
            } else {
                final int index = meta.description.toLowerCase(Locale.ROOT).indexOf(" skills review ");
                if (index == -1) {
                    htm.addln(meta.description);
                } else {
                    htm.add(meta.description.substring(0, index + 1));
                    htm.add("<span style='color:#D9782D'>");
                    htm.add(meta.description.substring(index + 1, index + 14));
                    htm.add("</span>");
                    htm.addln(meta.description.substring(index + 14));
                }
            }

            htm.sDiv("clear").eDiv();
            htm.sDiv(null, "style='margin-left:92px; margin-top:8px;'");

            if (!meta.objectives.isEmpty()) {
                htm.addln("Review Topics:");

                htm.addln("<ul style='font-weight:300; margin-top:6px;'>");
                for (final MetadataObjective metaObjective : meta.objectives) {
                    htm.addln("  <li>", metaObjective.title, "</li>");
                }
                htm.addln("</ul>");
            }
            htm.eDiv(); // Indented 92px

            htm.sDiv("center");
            htm.addln("<a class='btn' href='review.html'>Open the Skills Review...</a>");
            htm.eDiv();

            htm.eDiv(); // module-item
            htm.addln("</details>");
        }
    }

    /**
     * Presents the module learning targets.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param path     the relative path to lesson files
     * @param topicDir the directory in which to locate topic media
     */
    private static void doModuleStandards(final HtmlBuilder htm, final String path, final File topicDir) {

        int count = 0;
        for (int i = 1; i < 9; ++i) {
            final String dirName = "1" + i + "_standard_" + i;
            final File dir = new File(topicDir, dirName);

            if (dir.exists() && dir.isDirectory()) {
                emitStandard(htm, path + "/" + dirName, i, dir);
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
                    emitStandard(htm, path + "/" + dirName, i, dir);
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Presents the content of a single standard.
     *
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param path        the relative path to lesson files
     * @param number      the standard number
     * @param standardDir the directory in which to locate standard media
     */
    private static void emitStandard(final HtmlBuilder htm, final String path, final int number,
                                     final File standardDir) {

        htm.addln("<details class='module'>");
        final String numberStr = Integer.toString(number);
        htm.add("  <summary class='module-summary'>Learning Target ", numberStr, "</summary>");
        htm.sDiv("module-item");

        // Emit any introductory lessons found
        for (int i = 1; i < 10; ++i) {
            final String introName = "0" + i + "_intro_" + i;
            final File introDir = new File(standardDir, introName);
            if (introDir.exists() && introDir.isDirectory()) {
                if (i == 1) {
                    htm.sH(4).add("Introductory Lessons").eH(4);
                }
                emitLesson(htm, path + "/" + introName, i, introDir);
            } else {
                break;
            }
        }

        // Emit objectives
        for (int i = 11; i < 30; ++i) {
            final String objName = i + "_objective_" + MetadataSkillsReview.SUFFIXES.substring(i, i + 1);
            final File objectiveDir = new File(standardDir, objName);
            if (objectiveDir.exists() && objectiveDir.isDirectory()) {
                emitStandardObjective(htm, path + "/" + objName, i - 10, objectiveDir);
            } else {
                break;
            }
        }

        // Emit explorations
        final File explorationsDir = new File(standardDir, "40_explorations");
        if (explorationsDir.exists() && explorationsDir.isDirectory()) {
            emitStandardExplorations(htm, explorationsDir);
        }

        // Emit applications
        final File applicationsDir = new File(standardDir, "41_applications");
        if (applicationsDir.exists() && applicationsDir.isDirectory()) {
            emitStandardApplications(htm, applicationsDir);
        }

        // Emit any concluding lessons found
        for (int i = 1; i < 10; ++i) {
            final String conclusionName = "9" + i + "_conclusion_" + i;
            final File conclusionDir = new File(standardDir, conclusionName);
            if (conclusionDir.exists() && conclusionDir.isDirectory()) {
                if (i == 1) {
                    htm.sH(4).add("Concluding Lessons").eH(4);
                }
                emitLesson(htm, path + "/" + conclusionName, i, conclusionDir);
            } else {
                break;
            }
        }

        htm.eDiv(); // module-item
        htm.addln("</details>");
    }

    /**
     * Emits a lesson.
     *
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param path      the relative path to lesson files
     * @param number    a lesson number
     * @param lessonDir the directory in which to locate lesson media
     */
    private static void emitLesson(final HtmlBuilder htm, final String path, final int number, final File lessonDir) {

        final File finalVideo = new File(lessonDir, "final.mp4");
        final File finalVtt = new File(lessonDir, "final.vtt");
        final File finalTxt = new File(lessonDir, "final.txt");

        if (finalVideo.exists()) {
            final String numberStr = Integer.toString(number);
            htm.sP().add("<strong>Lesson ", numberStr, "</strong>");

            htm.sDiv("indent2");
            htm.addln("<video class='lesson' controls>");
            htm.addln("  <source type='video/mp4' src='", VIDEO_URL, "/", path, "/final.mp4'/>");
            if (finalVtt.exists()) {
                htm.addln("  <track kind='subtitles' srclang='en' label='English' default src='", WEB_URL, "/", path,
                        "/final.vtt'/>");
            }
            htm.addln("  Your browser does not support inline video.");
            htm.addln("</video>");
            htm.eDiv(); // indent2

            if (finalTxt.exists()) {
                htm.sDiv("indent2");
                htm.addln("<a href='", WEB_URL, "/", path,
                        "/final.txt'>Access a plain-text transcript for screen-readers.</a>");
                htm.eDiv(); // indent2
            }
        }
    }

    /**
     * Emits an example.
     *
     * @param htm        the {@code HtmlBuilder} to which to append
     * @param path       the relative path to example files
     * @param exampleDir the directory in which to locate example media
     */
    private static void emitExample(final HtmlBuilder htm, final String path, final File exampleDir) {

        final File finalVideo = new File(exampleDir, "final.mp4");
        final File finalVtt = new File(exampleDir, "final.vtt");
        final File finalTxt = new File(exampleDir, "final.txt");

        if (finalVideo.exists()) {
            htm.sP().add("<strong>Example</strong>");

            htm.addln("<video class='lesson' controls>");
            htm.addln("  <source type='video/mp4' src='", VIDEO_URL, "/", path, "/final.mp4'/>");
            if (finalVtt.exists()) {
                htm.addln("  <track kind='subtitles' srclang='en' label='English' default src='", WEB_URL, "/", path,
                        "/final.vtt'/>");
            }
            htm.addln("  Your browser does not support inline video.");
            htm.addln("</video>");

            if (finalTxt.exists()) {
                htm.addln("<a href='", WEB_URL, "/", path,
                        "/final.txt'>Access a plain-text transcript for screen-readers.</a>");
            }
        }
    }

    /**
     * Emits an objective.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param path         the relative path to lesson files
     * @param number       the objective number
     * @param objectiveDir the directory in which to locate objective media
     */
    private static void emitStandardObjective(final HtmlBuilder htm, final String path, final int number,
                                              final File objectiveDir) {

        final String numberStr = Integer.toString(number);
        htm.sH(4).add("Objective ", numberStr).eH(4);

        // Emit any objective lessons found
        for (int i = 1; i < 10; ++i) {
            final String introName = "1" + i + "_lesson_" + i;
            final File introDir = new File(objectiveDir, introName);
            if (introDir.exists() && introDir.isDirectory()) {
                emitLesson(htm, path + "/" + introName, i, introDir);
            } else {
                break;
            }
        }

        // Emit any examples found
        final File examplesDir = new File(objectiveDir, "30_examples");
        if (examplesDir.exists() && examplesDir.isDirectory()) {
            final File[] dirs = examplesDir.listFiles();
            if (dirs != null) {
                final List<File> dirList = Arrays.asList(dirs);
                dirList.sort(null);

                for (final File dir : dirList) {
                    if (dir.isDirectory()) {
                        emitExample(htm, path + "/" + objectiveDir.getName() + "/30_examples/" + dir.getName(), dir);
                    }
                }
            }
        }

        final File explorationsDir = new File(objectiveDir, "40_explorations");
        if (explorationsDir.exists() && explorationsDir.isDirectory()) {

            // TODO: emit explorations
        }

        final File applicationsDir = new File(objectiveDir, "41_applications");
        if (applicationsDir.exists() && applicationsDir.isDirectory()) {

            // TODO: emit applications
        }
    }

    /**
     * Emits explorations for a standard.
     *
     * @param htm             the {@code HtmlBuilder} to which to append
     * @param explorationsDir the directory in which to locate explorations
     */
    private static void emitStandardExplorations(final HtmlBuilder htm, final File explorationsDir) {

        // TODO:
    }

    /**
     * Emits applications for a standard.
     *
     * @param htm             the {@code HtmlBuilder} to which to append
     * @param applicationsDir the directory in which to locate explorations
     */
    private static void emitStandardApplications(final HtmlBuilder htm, final File applicationsDir) {

        // TODO:
    }

    /**
     * Presents all module-level explorations found.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param topicDir the directory in which to locate topic media
     */
    private static void doModuleExplorations(final HtmlBuilder htm, final File topicDir) {

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
    private static void doModuleApplications(final HtmlBuilder htm, final File topicDir) {

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
    private static void doModuleConcludingLessons(final HtmlBuilder htm, final File topicDir) {

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

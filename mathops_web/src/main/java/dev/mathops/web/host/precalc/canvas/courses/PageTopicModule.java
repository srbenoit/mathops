package dev.mathops.web.host.precalc.canvas.courses;

import dev.mathops.commons.installation.EPath;
import dev.mathops.commons.installation.PathList;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.course.MetadataCourseModule;
import dev.mathops.db.course.MetadataObjective;
import dev.mathops.db.course.MetadataSkillsReview;
import dev.mathops.db.course.MetadataStandard;
import dev.mathops.db.logic.MainData;
import dev.mathops.db.logic.TermData;
import dev.mathops.db.schema.legacy.RawStcourse;
import dev.mathops.db.rec.main.StandardsCourseModuleRec;
import dev.mathops.db.rec.main.StandardsCourseRec;
import dev.mathops.db.rec.term.StandardsCourseSectionRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.host.precalc.canvas.CanvasPageUtils;
import dev.mathops.web.host.precalc.canvas.CanvasSite;
import dev.mathops.web.host.precalc.canvas.ECanvasPanel;
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
                Log.warning("No course record for ", courseId);
                // TODO: Error display, course not part of this system rather than a redirect to Home
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
                        Log.warning("No module record for module ", moduleNbr, " in ", courseId);
                        final String homePath = site.makeRootPath("home.html");
                        resp.sendRedirect(homePath);
                    } else {
                        // Locate "media root" which is typically /opt/public/media
                        final File wwwPath = PathList.getInstance().get(EPath.WWW_PATH);
                        final File publicPath = wwwPath.getParentFile();
                        final File mediaRoot = new File(publicPath, "media");

                        presentModulePage(cache, site, req, resp, registration, course, section, module, mediaRoot);
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
     * @param course       the course object
     * @param section      the course section object
     * @param module       the course module object
     * @param mediaRoot    the root media directory relative to which the module path is specified
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentModulePage(final Cache cache, final CanvasSite site, final ServletRequest req,
                                  final HttpServletResponse resp, final RawStcourse registration,
                                  final StandardsCourseRec course, StandardsCourseSectionRec section,
                                  StandardsCourseModuleRec module, final File mediaRoot) throws IOException,
            SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();

        CanvasPageUtils.startPage(htm, siteTitle);

        // Emit the course number and section at the top
        CanvasPageUtils.emitCourseTitleAndSection(htm, course, section);

        htm.sDiv("pagecontainer");

        CanvasPageUtils.emitLeftSideMenu(htm, course, "../", ECanvasPanel.MODULES);

        htm.sDiv("flexmain");

        final MetadataCourseModule meta = new MetadataCourseModule(mediaRoot, module.modulePath, module.moduleNbr);

        if (meta.isValid()) {

            htm.sH(2);
            if (meta.thumbnailFile != null) {
                final String imageUrl = "/media/" + meta.moduleRelPath + "/" + meta.thumbnailFile;
                if (meta.thumbnailAltText == null) {
                    htm.addln("<img class='module-thumb' src='", imageUrl, "'/>");
                } else {
                    htm.addln("<img class='module-thumb' src='", imageUrl, "' alt='", meta.thumbnailAltText, "'/>");
                }
            }
            htm.sDiv("module-title");
            htm.add("Module ", module.moduleNbr, " Textbook Chapter").br();
            htm.addln("<div style='color:#D9782D; margin-top:6px;'>", meta.title, "</div>");
            htm.addln("<a class='smallbtn' href='assignments.html'>Go to Homework Assignments</a> &nbsp; ");
            htm.addln("<a class='smallbtn' href='targets.html'>Go to Learning Target Exams</a>");
            htm.eDiv();
            htm.eH(2);

            doModuleIntroLessons(htm, meta);
            doModuleSkillsReview(htm, meta);
            doModuleStandards(htm, meta);
            doModuleExplorations(htm, meta);
            doModuleApplications(htm, meta);
            doModuleConcludingLessons(htm, meta);

        } else {
            htm.sP().add("Error: Unable to load configuration of module ", module.moduleNbr).eP();
        }

        htm.eDiv(); // flexmain
        htm.eDiv(); // pagecontainer

        CanvasPageUtils.endPage(htm);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }

    /**
     * Presents any introductory lessons found in the topic directory.
     *
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param meta the metadata describing the module
     */
    private static void doModuleIntroLessons(final HtmlBuilder htm, final MetadataCourseModule meta) {

        for (int i = 1; i < 9; ++i) {
            final String dirName = "0" + i + "_intro_" + i;
            final File dir = new File(meta.moduleDir, dirName);

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
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param meta the metadata describing the module
     */
    private static void doModuleSkillsReview(final HtmlBuilder htm, final MetadataCourseModule meta) {

        final File dir = new File(meta.moduleDir, "10_skills_review");

        if (dir.exists() && dir.isDirectory()) {
            final MetadataSkillsReview skillsMeta = new MetadataSkillsReview(dir);

            htm.addln("<details class='module'>");
            htm.add("  <summary class='module-summary'>Skills Review</summary>");
            htm.sDiv("module-item");

            htm.sDiv("left");
            htm.addln("<img class='module-thumb' src='/www/images/etext/skills_review.png' ",
                    "alt='A set of connected links in the shape of a brain.'/>");
            htm.eDiv();

            if (skillsMeta.description == null) {
                htm.addln("The <span style='color:#D9782D'>Skills Review</span> provides a refresher of skills from ",
                        "prior courses that we use in this chapter.  Using this review is optional, but if you need ",
                        "it, it's here.");
            } else {
                final int index = skillsMeta.description.toLowerCase(Locale.ROOT).indexOf(" skills review ");
                if (index == -1) {
                    htm.addln(skillsMeta.description);
                } else {
                    htm.add(skillsMeta.description.substring(0, index + 1));
                    htm.add("<span style='color:#D9782D'>");
                    htm.add(skillsMeta.description.substring(index + 1, index + 14));
                    htm.add("</span>");
                    htm.addln(skillsMeta.description.substring(index + 14));
                }
            }

            htm.sDiv("clear").eDiv();
            htm.sDiv(null, "style='margin-left:92px; margin-top:8px;'");

            if (!skillsMeta.objectives.isEmpty()) {
                htm.addln("Review Topics:");

                htm.addln("<ul style='font-weight:300; margin-top:6px;'>");
                for (final MetadataObjective metaObjective : skillsMeta.objectives) {
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
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param meta the metadata describing the module
     */
    private static void doModuleStandards(final HtmlBuilder htm, final MetadataCourseModule meta) {

        int count = 0;
        for (int i = 1; i < 9; ++i) {
            final String dirName = "1" + i + "_standard_" + i;
            final File dir = new File(meta.moduleDir, dirName);
            final String path = meta.moduleRelPath + "/" + dirName;

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
                final File dir = new File(meta.moduleDir, dirName);
                final String path = meta.moduleRelPath + "/" + dirName;

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

        final MetadataStandard meta = new MetadataStandard(standardDir);

        htm.addln("<details class='standard'>");
        final String numberStr = Integer.toString(number);
        htm.add("  <summary class='standard-summary'>");
        htm.add("Learning Target ", numberStr);
        if (!(meta.title == null || meta.title.isBlank())) {
            htm.addln(":");
            htm.sDiv("summary-subtitle").add(meta.title).eDiv();
        }
        htm.addln("</summary>");

        if (!(meta.description == null || meta.description.isBlank())) {
            htm.sDiv("module-item");

            htm.sDiv("indent0");
            htm.add("After completing this learning target, I will be able to ");
            htm.eDiv();

            htm.sDiv("indent", "style='color:#D9782D;margin-top:6px;'");
            htm.addln(meta.description);
            htm.eDiv();

            htm.eDiv(); // module-item
        }

        // Emit any introductory lessons found
        boolean foundIntro = false;
        for (int i = 1; i < 10; ++i) {
            final String introName = "0" + i + "_intro_" + i;
            final File introDir = new File(standardDir, introName);
            if (introDir.exists() && introDir.isDirectory()) {
                if (i == 1) {
                    htm.sDiv("module-item");
                    htm.addln("<details class='module'>");
                    htm.add("  <summary class='module-summary'>");
                    htm.add("Introductory Lessons");
                    htm.addln("</summary>");

                    // TODO: Emit lesson media

                    foundIntro = true;
                }
                emitLesson(htm, path + "/" + introName, i, introDir);
            } else {
                break;
            }
        }
        if (foundIntro) {
            htm.addln("</details>");
            htm.eDiv(); // module-item
        }

        // Emit objectives
        for (int i = 11; i < 30; ++i) {
            final String objName = i + "_objective_" + MetadataSkillsReview.SUFFIXES.charAt(i);
            final File objectiveDir = new File(standardDir, objName);
            if (objectiveDir.exists() && objectiveDir.isDirectory()) {

                final int index = i - 11;
                MetadataObjective objectiveMeta = null;
                if (meta.objectives.size() > index) {
                    objectiveMeta = meta.objectives.get(index);
                }

                emitStandardObjective(htm, path + "/" + objName, i - 10, objectiveDir, objectiveMeta);
            } else {
                break;
            }
        }

        // Emit explorations
        final File explorationsDir = new File(standardDir, "40_explorations");
        if (explorationsDir.exists() && explorationsDir.isDirectory()) {
            htm.sDiv("module-item");
            emitStandardExplorations(htm, explorationsDir);
            htm.eDiv(); // module-item
        }

        // Emit applications
        final File applicationsDir = new File(standardDir, "41_applications");
        if (applicationsDir.exists() && applicationsDir.isDirectory()) {
            htm.sDiv("module-item");
            emitStandardApplications(htm, applicationsDir);
            htm.eDiv(); // module-item
        }

        // Emit any concluding lessons found
        boolean foundConclusion = false;
        for (int i = 1; i < 10; ++i) {
            final String conclusionName = "9" + i + "_conclusion_" + i;
            final File conclusionDir = new File(standardDir, conclusionName);
            if (conclusionDir.exists() && conclusionDir.isDirectory()) {
                if (i == 1) {
                    htm.sDiv("module-item");
                    htm.addln("<details class='module'>");
                    htm.add("  <summary class='module-summary'>");
                    htm.add("Concluding Lessons");
                    htm.addln("</summary>");
                    foundConclusion = true;
                }
                emitLesson(htm, path + "/" + conclusionName, i, conclusionDir);
            } else {
                break;
            }
        }
        if (foundConclusion) {
            htm.addln("</details>");
            htm.eDiv(); // module-item
        }

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

            htm.sDiv("indent");
            htm.addln("<video class='lesson' controls>");
            htm.addln("  <source type='video/mp4' src='", VIDEO_URL, path, "/final.mp4'/>");
            if (finalVtt.exists()) {
                htm.addln("  <track kind='subtitles' srclang='en' label='English' default src='", WEB_URL, "/", path,
                        "/final.vtt'/>");
            }
            htm.addln("  Your browser does not support inline video.");
            htm.addln("</video>");
            htm.eDiv(); // indent

            if (finalTxt.exists()) {
                htm.sDiv("indent");
                htm.addln("<a href='", WEB_URL, path,
                        "/final.txt'>Access a plain-text transcript for screen-readers.</a>");
                htm.eDiv(); // indent
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

        final File examplePdf = new File(exampleDir, "example.pdf");
        final File finalVideo = new File(exampleDir, "final.mp4");
        final File finalVtt = new File(exampleDir, "final.vtt");
        final File finalTxt = new File(exampleDir, "final.txt");

        if (finalVideo.exists()) {

            htm.addln("<details class='example'>");
            htm.add("  <summary class='example-summary'>");
            htm.add("Worked Example");
            htm.addln("</summary>");

            htm.sDiv("module-item");

            if (examplePdf.exists()) {
                htm.addln(" <a target='_blank' href='", VIDEO_URL, "/", path,
                        "/example.pdf'> (Open PDF Example Document).</a>");
            }

            htm.sDiv("indent");
            htm.addln("<video class='lesson' controls>");
            htm.addln("  <source type='video/mp4' src='", VIDEO_URL, "/", path, "/final.mp4'/>");
            if (finalVtt.exists()) {
                htm.addln("  <track kind='subtitles' srclang='en' label='English' default src='", WEB_URL, "/", path,
                        "/final.vtt'/>");
            }
            htm.addln("  Your browser does not support inline video.");
            htm.addln("</video>");
            htm.eDiv(); // indent

            if (finalTxt.exists()) {
                htm.sDiv("indent");
                htm.addln("<a target='_blank' href='", VIDEO_URL, "/", path,
                        "/final.txt'>Access a plain-text transcript for screen-readers.</a>");
                htm.eDiv(); // indent
            }

            htm.eDiv(); // module-item
            htm.addln("</details>");
        }
    }

    /**
     * Emits an objective.
     *
     * @param htm           the {@code HtmlBuilder} to which to append
     * @param path          the relative path to lesson files
     * @param number        the objective number
     * @param objectiveDir  the directory in which to locate objective media
     * @param objectiveMeta objective metadata
     */
    private static void emitStandardObjective(final HtmlBuilder htm, final String path, final int number,
                                              final File objectiveDir, final MetadataObjective objectiveMeta) {

        final String numberStr = Integer.toString(number);

        htm.sDiv("module-item");
        htm.addln("<details class='objective'>");
        htm.add("  <summary class='objective-summary'>");
        htm.add("Objective ", numberStr);
        if (!(objectiveMeta.title == null || objectiveMeta.title.isBlank())) {
            htm.addln(":");
            htm.sDiv("summary-subtitle").add(objectiveMeta.title).eDiv();
        }
        htm.addln("</summary>");

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
                        emitExample(htm, path + "/30_examples/" + dir.getName(), dir);
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

        htm.addln("</details>");
        htm.eDiv(); // module-item
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
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param meta the metadata describing the module
     */
    private static void doModuleExplorations(final HtmlBuilder htm, final MetadataCourseModule meta) {

        final File dir = new File(meta.moduleDir, "40_explorations");

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
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param meta the metadata describing the module
     */
    private static void doModuleApplications(final HtmlBuilder htm, final MetadataCourseModule meta) {

        final File dir = new File(meta.moduleDir, "41_applications");

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
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param meta the metadata describing the module
     */
    private static void doModuleConcludingLessons(final HtmlBuilder htm, final MetadataCourseModule meta) {

        for (int i = 1; i < 9; ++i) {
            final String dirName = "9" + i + "_conclusion_" + i;
            final File dir = new File(meta.moduleDir, dirName);

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

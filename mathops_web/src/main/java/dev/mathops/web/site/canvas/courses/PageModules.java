package dev.mathops.web.site.canvas.courses;

import dev.mathops.commons.file.FileLoader;
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
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;
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
 * This page shows the outline and E-text content for a single standards-based course. This is an outline page with a
 * list of modules, each with its status.
 *
 * <p>
 * It is assumed that this page can only be accessed by someone who has passed the user's exam and has legitimate access
 * to the e-text. This page does not check those conditions.
 */
public enum PageModules {
    ;

    /**
     * Starts the page that shows the course outline with student progress.
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
                presentModulesPage(cache, site, req, resp, session, registration, metaCourse);
            }
        }
    }

    /**
     * Presents the list of course modules.
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
    static void presentModulesPage(final Cache cache, final CanvasSite site, final ServletRequest req,
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

            CanvasPageUtils.emitLeftSideMenu(htm, metaCourse, null, ECanvasPanel.MODULES);

            htm.sDiv("flexmain");

            htm.sH(2).add("Modules").eH(2);
            htm.hr();

            emitCourseModules(htm, registration, metaCourse);

            htm.eDiv(); // flexmain
            htm.eDiv(); // pagecontainer

            CanvasPageUtils.endPage(htm);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
        }
    }

    /**
     * Emits all course modules, including an introductory module.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param registration the student's registration record
     * @param metaCourse   the metadata object with course structure data
     */
    private static void emitCourseModules(final HtmlBuilder htm, final RawStcourse registration,
                                          final MetadataCourse metaCourse) {

        emitIntroModule(htm);

        final File wwwPath = PathList.getInstance().get(EPath.WWW_PATH);
        final File publicPath = wwwPath.getParentFile();
        final File mediaPath = new File(publicPath, "media");

        for (final MetadataCourseTopic metaCourseTopic : metaCourse.topics) {
            final File topicDir = new File(mediaPath, metaCourseTopic.directory);
            final File topicMetaFile = new File(topicDir, "metadata.json");

            if (topicDir.exists()) {
                if (topicMetaFile.exists()) {
                    final MetadataTopic meta = loadTopicMetadata(topicMetaFile);

                    if (Objects.nonNull(meta.title)) {
                        emitTopicModule(htm, metaCourseTopic, meta, mediaPath);
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
    }

    /**
     * Emits the "Introduction" module with the "Start Here" and "How to Successfully Navigate this Course" items.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitIntroModule(final HtmlBuilder htm) {

        startModule(htm, null, "Introduction");

        emitChecklistModuleItem(htm, "/www/images/etext/start-thumb.png", "Starting line of race track",
                "start_here.html",
                "Start Here", new ModuleItemChecklistEntry("Set Account Preferences", true));

        emitChecklistModuleItem(htm, "/www/images/etext/navigation-thumb.png", "Man at wheel of ship at sea",
                "navigating.html",
                "How to Successfully Navigate this Course",
                new ModuleItemChecklistEntry("Syllabus Quiz", false));

        endModule(htm);
    }

    /**
     * Emits a single topic module.
     *
     * @param htm             the {@code HtmlBuilder} to which to append
     * @param metaCourseTopic the metadata describing the topic within the course
     * @param meta            the topic module metadata object
     * @param topicDir        the root directory in which to find topic media files
     */
    private static void emitTopicModule(final HtmlBuilder htm, final MetadataCourseTopic metaCourseTopic,
                                        final MetadataTopic meta, final File topicDir) {

        startModule(htm, metaCourseTopic.heading, meta.title);

        // The top-level Topic Module object in the web page has three items:
        // - E-Text Chapter: [title]
        // - Module Assignments (with completion status)
        // - Learning Target Exams (with completion status)

        // The topic module with ID "M01" lives at "M01/module.html"
        final String modulePath = metaCourseTopic.id + "/module.html";

        if (meta.thumbnailFile == null) {
            emitChapterItem(htm, null, null, modulePath, meta.title);
        } else {
            final String imageUrl = "/media/" + metaCourseTopic.directory + "/" + meta.thumbnailFile;
            emitChapterItem(htm, imageUrl, meta.thumbnailAltText, modulePath, meta.title);
        }

        // Required assignments, with status
        final String assignmentsPath = metaCourseTopic.id + "/assignments.html";
        emitChecklistModuleItem(htm, "/www/images/etext/required_assignment_thumb.png", "A student doing homework.",
                assignmentsPath, "Module Assignments",
                new ModuleItemChecklistEntry("Assignment 1", false),
                new ModuleItemChecklistEntry("Assignment 2", false),
                new ModuleItemChecklistEntry("Assignment 3", false));

        // Learning Target Exams, with status
        final String examsPath = metaCourseTopic.id + "/targets.html";
        emitChecklistModuleItem(htm, "/www/images/etext/target_thumb.png", "A dartboard with several magnetic darts",
                examsPath, "Learning Targets",
                new ModuleItemChecklistEntry("Target 1", false),
                new ModuleItemChecklistEntry("Target 2", false),
                new ModuleItemChecklistEntry("Target 3", false));

        endModule(htm);
    }

    /**
     * Emits the HTML to start a module.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param heading the heading
     * @param title   the title
     */
    private static void startModule(final HtmlBuilder htm, final String heading, final String title) {

        htm.addln("<details class='module'>");
        htm.add("  <summary class='module-summary'>");

        if (heading == null) {
            htm.add(title);
        } else {
            htm.add(heading, ": <span style='color:#D9782D'>", title, "</span>");
        }

        htm.addln("</summary>");
    }

    /**
     * Emits the HTML to end a module.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void endModule(final HtmlBuilder htm) {

        htm.addln("</details>");
    }

    /**
     * Emits a module item with a heading and title.
     *
     * @param htm           the {@code HtmlBuilder} to which to append
     * @param thumbImage    the thumbnail image
     * @param thumbImageAlt the ALT text for the thumbnail image
     * @param href          the link reference
     * @param title         the item title
     */
    private static void emitChapterItem(final HtmlBuilder htm, final String thumbImage, final String thumbImageAlt,
                                        final String href, final String title) {

        htm.sDiv("module-item");

        if (thumbImage != null) {
            htm.addln("<a href='", href, "'><img class='module-thumb' src='", thumbImage, "' alt='", thumbImageAlt,
                    "'/></a>");
        }

        htm.sDiv("module-title");
        if (title == null) {
            htm.addln("<span style='color:#D9782D'><a class='ulink2' href='", href, "'>Textbook Chapter</a></span>");
        } else {
            htm.addln("Textbook Chapter:").br();
            htm.addln("<div style='color:#D9782D; margin-top:4px; margin-left:20px'>",
                    "<a class='ulink2' href='", href, "'>", title, "</a></div>");
        }
        htm.eDiv();

        htm.eDiv();
    }

    /**
     * Emits a module item with a checklist.
     *
     * @param htm           the {@code HtmlBuilder} to which to append
     * @param thumbImage    the thumbnail image
     * @param thumbImageAlt the ALT text for the thumbnail image
     * @param href          the link reference
     * @param title         the item title
     * @param checklist     an option list of checklist items
     */
    private static void emitChecklistModuleItem(final HtmlBuilder htm, final String thumbImage,
                                                final String thumbImageAlt, final String href, final String title,
                                                final ModuleItemChecklistEntry... checklist) {

        htm.sDiv("module-item");

        if (thumbImage != null) {
            htm.addln("<a href='", href, "'><img class='module-thumb' src='", thumbImage, "' alt='", thumbImageAlt,
                    "'/></a>");
        }

        htm.sDiv("module-title");
        htm.addln("<a class='ulink2' href='", href, "'>", title, "</a>");
        if (checklist != null && checklist.length > 0) {
            htm.br();
            for (final ModuleItemChecklistEntry entry : checklist) {
                htm.add("<div style='display:inline-block; width:20px; height:2px;'></div>");
                htm.add("<img class='module-item-checkbox' src='/www/images/etext/",
                        (entry.checked() ? "box_checked_18.png" : "box_unchecked_18.png"),
                        "'/>", entry.label());
                htm.br();
            }
        }
        htm.eDiv();

        htm.eDiv();
    }

    /**
     * A record with data for a checklist item within a module item.
     *
     * @param label   the label
     * @param checked true if the checkbox is checked (the item has been completed)
     */
    private record ModuleItemChecklistEntry(String label, boolean checked) {
    }

    /**
     * Loads the topic metadata.
     *
     * @param file the file to load
     * @return the metadata object if successful; {@code null} if not
     */
    static MetadataTopic loadTopicMetadata(final File file) {

        MetadataTopic result = null;

        final String fileData = FileLoader.loadFileAsString(file, true);
        try {
            final Object parsedObj = JSONParser.parseJSON(fileData);

            if (parsedObj instanceof final JSONObject parsedJson) {
                result = new MetadataTopic(parsedJson);
            } else {
                Log.warning("Top-level object in parsed 'metadata.json' is not JSON Object.");
            }
        } catch (final ParsingException ex) {
            Log.warning("Failed to parse 'metadata.json' file data.", ex);
        }

        return result;
    }
}

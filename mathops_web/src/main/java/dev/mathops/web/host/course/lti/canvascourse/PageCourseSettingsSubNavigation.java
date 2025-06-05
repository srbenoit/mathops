package dev.mathops.web.host.course.lti.canvascourse;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.rec.main.LtiRegistrationRec;
import dev.mathops.db.rec.term.LtiContextCourseSectionRec;
import dev.mathops.db.rec.term.LtiContextRec;
import dev.mathops.db.reclogic.term.LtiContextCourseSectionLogic;
import dev.mathops.db.reclogic.term.LtiContextLogic;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.xml.XmlEscaper;
import dev.mathops.web.host.course.lti.LtiSite;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A page with the "course_settings_sub_navigation" placement.
 *
 * <p>
 * This page allows the teacher to associate a CSU course/section with this Canvas course and configure this tool with
 * respect to that section.  This must be done before other configurations can be done.
 * </p>
 */
enum PageCourseSettingsSubNavigation {
    ;
    /** The number of minutes a post permission can wait before expiring. */
    private static final long PERMISSION_EXPIRY_MINUTES = 5L;

    /** A map from token string to the post permission object. */
    private static final Map<String, PostPermission> PERMISSIONS = new HashMap<>(10);

    /** An empty list of context course sections. */
    private static final List<LtiContextCourseSectionRec> EMPTY_LIST = new ArrayList<>(0);

    /**
     * Shows the page.
     *
     * @param cache    the data cache
     * @param req      the request
     * @param resp     the response
     * @param redirect the redirect
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final ServletRequest req, final HttpServletResponse resp,
                      final LtiSite.PendingTargetRedirect redirect) throws IOException, SQLException {

        final JSONObject payload = redirect.idTokenPayload();

        final String deploymentId = payload.getStringProperty(
                "https://purl.imsglobal.org/spec/lti/claim/deployment_id");
        String returnUrl = null;
        String contextId = null;
        String contextTitle = null;
        String canvasCourseId = null;
        boolean isAdmin = false;
        boolean isInstructor = false;

        final Object pres = payload.getProperty("https://purl.imsglobal.org/spec/lti/claim/launch_presentation");
        if (pres instanceof final JSONObject presObject) {
            returnUrl = presObject.getStringProperty("return_url");
        }

        final Object context = payload.getProperty("https://purl.imsglobal.org/spec/lti/claim/context");
        if (context instanceof final JSONObject contextObj) {
            contextId = contextObj.getStringProperty("id");
            contextTitle = contextObj.getStringProperty("title");
            if (contextTitle == null || contextTitle.isBlank()) {
                contextTitle = contextObj.getStringProperty("label");
            }
        }

        final Object roles = payload.getProperty("https://purl.imsglobal.org/spec/lti/claim/roles");
        if (roles instanceof final Object[] rolesArray) {
            for (final Object role : rolesArray) {
                if ("http://purl.imsglobal.org/vocab/lis/v2/institution/person#Administrator".equals(role)
                    || "http://purl.imsglobal.org/vocab/lis/v2/system/person#SysAdmin".equals(role)) {
                    isAdmin = true;
                } else if ("http://purl.imsglobal.org/vocab/lis/v2/institution/person#Instructor".equals(role)
                           || "http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor".equals(role)) {
                    isInstructor = true;
                }
            }
        }

        final Object custom = payload.getProperty("https://purl.imsglobal.org/spec/lti/claim/custom");
        if (custom instanceof final JSONObject customObj) {
            String test = customObj.getStringProperty("canvas_course_id");
            if (!(test == null || test.isBlank() || "$Canvas.course.id".equals(test))) {
                canvasCourseId = test;
            }
        }

        final HtmlBuilder htm = new HtmlBuilder(1000);

        htm.addln("<!DOCTYPE html>").addln("<html>").addln("<head>");
        htm.addln(" <meta http-equiv='Content-Type' content='text/html;charset=utf-8'/>")
                .addln(" <link rel='stylesheet' href='ltistyle.css' type='text/css'>")
                .addln(" <title>", LtiSite.TOOL_NAME, "</title>");
        htm.addln("</head>");
        htm.addln("<body style='background:white; padding:20px;'>");

        htm.sH(1).add(LtiSite.TOOL_NAME).eH(1);
        htm.sH(2).add("Link Course").eH(2);
        htm.hr().sDiv("indent");

        if (isInstructor || isAdmin) {
            final LtiRegistrationRec registration = redirect.registration();

            final PostData data = new PostData(registration, deploymentId, contextId, canvasCourseId, contextTitle,
                    returnUrl);
            final String token = createPostPermission(data);

            final LtiContextRec existingContext = LtiContextLogic.INSTANCE.query(cache, registration.clientId,
                    registration.issuer, deploymentId, contextId);

            if (existingContext != null) {
                // Show the existing configuration, with the option to update
                final List<LtiContextCourseSectionRec> existingSections =
                        LtiContextCourseSectionLogic.INSTANCE.queryForContext(cache, existingContext.clientId,
                                existingContext.issuer, existingContext.deploymentId, existingContext.contextId);
                htm.sP().addln("This course is currently linked to the following institutional course sections:")
                        .eP();

                htm.sP("indent");
                for (final LtiContextCourseSectionRec rec : existingSections) {
                    htm.addln(rec.courseId, " section ", rec.sectionNbr).br();
                }
                htm.eP();

                htm.hr();
                htm.sP().add("You can change the set of linked course sections with the following form.").eP();
                htm.sP().add("If this LMS course includes multiple cross-listed sections, select all of the ",
                        "corresponding institutional sections.").eP();
                htm.div("vgap");

                emitLinkCourseForm(cache, htm, token, returnUrl, existingSections);
            } else {
                htm.sP().add("Before this LTI tool can provide course content and assignments within the LMS, it ",
                        "must be linked to one or more institutional course sections.").eP();

                htm.sP().add("Please select the institutional course section(s) to link to this LMS course.").eP();
                htm.sP().add("If this LMS course includes multiple cross-listed sections, select all of the ",
                        "corresponding institutional sections.").eP();
                htm.div("vgap");

                emitLinkCourseForm(cache, htm, token, returnUrl, EMPTY_LIST);
            }
        } else {
            // A user with only student permission accessed this page.  This should not be possible, but...
            htm.sP().add("Configuration settings for ", LtiSite.TOOL_NAME,
                    " are available only to instructors and administrators.").eP();
            htm.div("vgap");

            if (returnUrl != null) {
                final String escapedReturn = XmlEscaper.escape(returnUrl);
                htm.addln("<a class='btn' href='", escapedReturn, "'>Close</a>");
            }
        }

        htm.eDiv(); // indent
        htm.addln("</body></html>");

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Processes a POST from this page.
     *
     * @param cache the data cache
     * @param req   the request
     * @param resp  the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doPost(final Cache cache, final ServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        final String token = req.getParameter("token");
        final PostPermission permission = getPermission(token);

        if (permission == null) {
            PageError.showErrorPage(req, resp, "Link Course", "Permission to perform this update was not found.");
        } else {
            final HtmlBuilder htm = new HtmlBuilder(1000);

            htm.addln("<!DOCTYPE html>").addln("<html>").addln("<head>");
            htm.addln(" <meta http-equiv='Content-Type' content='text/html;charset=utf-8'/>")
                    .addln(" <link rel='stylesheet' href='ltistyle.css' type='text/css'>")
                    .addln(" <title>", LtiSite.TOOL_NAME, "</title>");
            htm.addln("</head>");
            htm.addln("<body style='background:white; padding:20px;'>");

            htm.sH(1).add(LtiSite.TOOL_NAME).eH(1);
            htm.sH(2).add("Link Course").eH(2);
            htm.hr().sDiv("indent");

            final List<RawCsection> sections = getCourseSections(cache, htm);
            final List<RawCsection> checkedSections = new ArrayList<>(10);
            for (final RawCsection test : sections) {
                final String key = test.course + "_" + test.sect;
                if (req.getParameter(key) instanceof String) {
                    checkedSections.add(test);
                }
            }

            final PostData data = permission.data();
            final LtiRegistrationRec registration = data.registration();
            final String deploymentId = data.deploymentId();
            final String contextId = data.contextId();

            // See if there is already a Context record for this context
            final LtiContextRec existingContext = LtiContextLogic.INSTANCE.query(cache, registration.clientId,
                    registration.issuer, deploymentId, contextId);

            if (existingContext == null) {
                if (checkedSections.isEmpty()) {
                    htm.sP().addln("No course sections were selected; no action was taken.").eP();
                } else {
                    // Create the new context record and associated context course section records
                    final LtiContextRec newContext = new LtiContextRec(registration.clientId, registration.issuer,
                            deploymentId, contextId, data.lmsCourseId(), data.lmsCourseTitle());

                    if (LtiContextLogic.INSTANCE.insert(cache, newContext)) {
                        boolean ok = true;
                        for (final RawCsection sect : checkedSections) {
                            final LtiContextCourseSectionRec ccs = new LtiContextCourseSectionRec(registration.clientId,
                                    registration.issuer, deploymentId, contextId, sect.course, sect.sect);
                            if (!LtiContextCourseSectionLogic.INSTANCE.insert(cache, ccs)) {
                                htm.sP("error").addln("An error occurred storing context course/section data.").eP();
                                ok = false;
                            }
                        }

                        if (ok) {
                            htm.sP().addln("This course is now linked to the following institutional course sections:")
                                    .eP();
                            htm.sP("indent");
                            for (final RawCsection test : checkedSections) {
                                htm.addln(test.course, " section ", test.sect).br();
                            }
                            htm.eP();
                        }

                        htm.div("vgap");
                        htm.addln("<a class='btn' href='", XmlEscaper.escape(data.returnUrl()), "'>Close</a>");
                    } else {
                        htm.sP("error").addln("An error occurred storing LTI context data.").eP();
                    }
                }
            } else {
                // There is an existing context record - we are editing that configuration...

                // Update the context record if the non-key data has changed.
                if (!(Objects.equals(existingContext.lmsCourseId, data.lmsCourseId())
                      && Objects.equals(existingContext.lmsCourseTitle, data.lmsCourseTitle()))) {

                    final LtiContextRec newContext = new LtiContextRec(existingContext.clientId, existingContext.issuer,
                            existingContext.deploymentId, existingContext.contextId, data.lmsCourseId(),
                            data.lmsCourseTitle());
                    LtiContextLogic.INSTANCE.update(cache, newContext);
                }

                // Check for updates needed to the course/section registrations
                final List<LtiContextCourseSectionRec> existingSections =
                        LtiContextCourseSectionLogic.INSTANCE.queryForContext(cache, existingContext.clientId,
                                existingContext.issuer, existingContext.deploymentId, existingContext.contextId);

                // Scan for new context course/section records that need to be inserted
                for (final RawCsection checked : checkedSections) {
                    boolean found = false;
                    for (final LtiContextCourseSectionRec test : existingSections) {
                        if (test.courseId.equals(checked.course) && test.sectionNbr.equals(checked.sect)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        // Need to insert a new record
                        final LtiContextCourseSectionRec newSect = new LtiContextCourseSectionRec(registration.clientId,
                                registration.issuer, deploymentId, contextId, checked.course, checked.sect);
                        LtiContextCourseSectionLogic.INSTANCE.insert(cache, newSect);
                    }
                }

                // Scan for existing context course/section records that need to be deleted
                for (final LtiContextCourseSectionRec test : existingSections) {
                    boolean shouldDelete = true;
                    for (final RawCsection checked : checkedSections) {
                        if (test.courseId.equals(checked.course) && test.sectionNbr.equals(checked.sect)) {
                            shouldDelete = false;
                            break;
                        }
                    }

                    if (shouldDelete) {
                        LtiContextCourseSectionLogic.INSTANCE.delete(cache, test);
                    }
                }

                // Re-query and present an updated list:
                final List<LtiContextCourseSectionRec> finalSections =
                        LtiContextCourseSectionLogic.INSTANCE.queryForContext(cache, existingContext.clientId,
                                existingContext.issuer, existingContext.deploymentId, existingContext.contextId);
                htm.sP().addln("This course is now linked to the following institutional course sections:")
                        .eP();
                htm.sP("indent");
                for (final LtiContextCourseSectionRec rec : finalSections) {
                    htm.addln(rec.courseId, " section ", rec.sectionNbr).br();
                }
                htm.eP();

                htm.div("vgap");
                htm.addln("<a class='btn' href='", XmlEscaper.escape(data.returnUrl()), "'>Close</a>");
            }

            htm.eDiv(); // indent

            htm.addln("</body></html>");

            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
        }
    }

    /**
     * Emits the form to link the course to an institutional course and section.
     *
     * @param cache     the data cache
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param token     the token that grants permission to execute a POST
     * @param returnUrl the return URL
     * @param existing  the list of existing sections to pre-check
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitLinkCourseForm(final Cache cache, final HtmlBuilder htm, final String token,
                                           final String returnUrl, final List<LtiContextCourseSectionRec> existing)
            throws SQLException {

        final List<RawCsection> sections = getCourseSections(cache, htm);

        if (!sections.isEmpty()) {
            htm.addln("<form class='indent' method='POST'>");
            htm.add("<input type='hidden' id='plc' name='plc' value='course_settings_sub_navigation'/>");
            htm.add("<input type='hidden' id='token' name='token' value='", token, "'/>");
            String priorCourse = "foo";
            final int size = sections.size();
            for (int i = 0; i < size; ++i) {
                final RawCsection sect = sections.get(i);
                final String key = sect.course + "_" + sect.sect;
                final String dispCourse = sect.course.replace("M ", "MATH ");
                if (i == size - 1) {
                    if (priorCourse.equals(sect.course)) {
                        htm.sP("tight", "style='border-bottom: solid 1px gray;'");
                    } else {
                        htm.sP("tight", "style='border-top: solid 1px gray; border-bottom: solid 1px gray;'");
                    }
                } else {
                    if (priorCourse.equals(sect.course)) {
                        htm.sP("tight");
                    } else {
                        htm.sP("tight", "style='border-top: solid 1px gray;'");
                    }
                }
                priorCourse = sect.course;

                boolean found = false;
                for (final LtiContextCourseSectionRec test : existing) {
                    if (test.courseId.equals(sect.course) && test.sectionNbr.equals(sect.sect)) {
                        found = true;
                        break;
                    }
                }

                if (found) {
                    htm.add("<input type='checkbox' id='", key, "' name='", key, "' checked='checked'/>");
                } else {
                    htm.add("<input type='checkbox' id='", key, "' name='", key, "'/>");
                }
                htm.addln(" <label for='", key, "'>", dispCourse, " section ", sect.sect, "</label>");
                htm.eP();
            }

            htm.div("vgap");
            htm.addln("<input class='btn' type='submit' value='Submit'/> &nbsp; ");
            htm.addln("<a class='btn' href='", XmlEscaper.escape(returnUrl), "'>Cancel</a>");
            htm.addln("</form>");
        }
    }

    /**
     * Gathers the list of course/section records that are applicable to this tool.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append error messages
     * @return the list of applicable sections
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawCsection> getCourseSections(final Cache cache, final HtmlBuilder htm) throws SQLException {

        final SystemData systemData = cache.getSystemData();
        final TermRec active = systemData.getActiveTerm();

        List<RawCsection> result = null;

        if (active == null) {
            htm.sP("error").add("Error: Unable to determine the active term.").eP();
        } else {
            final List<RawCsection> sections = RawCsectionLogic.queryByTerm(cache, active.term);

            if (sections.isEmpty()) {
                htm.sP("error").add("Error: No institutional sections found.").eP();
            } else {
                result = new ArrayList<>(sections.size());
                for (final RawCsection sect : sections) {
                    final String crs = sect.course;
                    if ("M 115".equals(crs) || "MATH 115".equals(crs)
                        || "M 116".equals(crs) || "MATH 116".equals(crs)
                        || "M 117".equals(crs) || "MATH 117".equals(crs)
                        || "M 118".equals(crs) || "MATH 118".equals(crs)
                        || "M 124".equals(crs) || "MATH 124".equals(crs)
                        || "M 125".equals(crs) || "MATH 125".equals(crs)
                        || "M 126".equals(crs) || "MATH 126".equals(crs)) {

                        // Omit placement credit sections
                        if (!"OT".equals(sect.instrnType)) {
                            result.add(sect);
                        }
                    }
                }
                result.sort(null);
            }
        }

        return result;
    }

    /**
     * Creates a one-time, short-term permission to execute a POST.
     *
     * @param data the data needed to process the POST
     * @return the token to use for the redirect
     */
    private static String createPostPermission(final PostData data) {

        synchronized (PERMISSIONS) {
            String token = CoreConstants.newId(24);
            while (PERMISSIONS.containsKey(token)) {
                token = CoreConstants.newId(24);
            }
            final LocalDateTime now = LocalDateTime.now();
            final LocalDateTime expiry = now.plusMinutes(PERMISSION_EXPIRY_MINUTES);

            final PostPermission permission = new PostPermission(token, data, expiry);
            PERMISSIONS.put(token, permission);

            return token;
        }
    }

    /**
     * Retrieves (and deletes) the one-time post permission.
     *
     * @param token the token value
     * @return the associated redirect, null if none found
     */
    private static PostPermission getPermission(final String token) {

        synchronized (PERMISSIONS) {
            final PostPermission result = PERMISSIONS.remove(token);

            if (!PERMISSIONS.isEmpty()) {
                // Check for expired and remove them
                final LocalDateTime now = LocalDateTime.now();

                final Set<Map.Entry<String, PostPermission>> entrySet = PERMISSIONS.entrySet();
                final Iterator<Map.Entry<String, PostPermission>> iter = entrySet.iterator();
                while (iter.hasNext()) {
                    final Map.Entry<String, PostPermission> entry = iter.next();
                    final PostPermission value = entry.getValue();
                    final LocalDateTime expiry = value.expiry();
                    if (expiry.isBefore(now)) {
                        iter.remove();
                    }
                }
            }

            return result;
        }
    }

    /**
     * Data needed to process a POST request.
     *
     * @param registration   the LTI registration
     * @param deploymentId   the deployment ID
     * @param contextId      the context ID
     * @param lmsCourseId    the LMS course ID
     * @param lmsCourseTitle the LMS course title
     * @param returnUrl      the return URL
     */
    record PostData(LtiRegistrationRec registration, String deploymentId, String contextId, String lmsCourseId,
                    String lmsCourseTitle, String returnUrl) {
    }

    /**
     * A one-time, short-lived permission to execute a POST to this page.
     *
     * @param nonce  the nonce
     * @param data   the data needed to process the POST request
     * @param expiry the date/time the redirect will expire
     */
    record PostPermission(String nonce, PostData data, LocalDateTime expiry) {
    }
}

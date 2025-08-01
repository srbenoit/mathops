package dev.mathops.web.host.placement.placement;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.mathplan.MathPlanLogic;
import dev.mathops.db.logic.mathplan.types.EMathPlanStatus;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Supports integration with RamReady, which tests whether a user has created a Math plan, and presents a status for the
 * student to be included in the RamReady checklist.
 */
enum RamReadyService {
    ;

    /** The subpath for checking student completion of the Math Plan. */
    private static final String CHECK_MATH_PLAN = "ramready.svc/CheckMathPlan/";

    /** The subpath for checking student completion of the Math Placement Tool. */
    private static final String CHECK_MATH_PLACEMENT = "ramready.svc/CheckMathPlacement/";

    /**
     * Processes a request of this form.
     *
     * <pre>
     * https://[host]/ramready.svc/CheckMathPlan/[PIDM]
     * </pre>
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param subpath The subpath, known to begin with "ramready.svc"
     * @param req     the request
     * @param resp    the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void process(final Cache cache, final MathPlacementSite site, final String subpath,
                        final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        if ("ramready.svc".equals(subpath)) {
            sendServiceDefinition(cache, site, req, resp);
        } else {
            final String sharedSecret = req.getHeader("X-CSUMATH-RamWeb");

            if ("93u54ki3bngtowIE".equals(sharedSecret)) {
                if (subpath.startsWith(CHECK_MATH_PLAN)) {
                    doCheckMathPlan(cache, subpath, req, resp);
                } else if (subpath.startsWith(CHECK_MATH_PLACEMENT)) {
                    doCheckMathPlacement(cache, subpath, req, resp);
                } else {
                    Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
                    sendErrorMessage(req, resp, "Unknown web service called.");
                }
            } else {
                Log.warning("Invalid or missing shared secret on RAMWeb request.");
                sendErrorMessage(req, resp, "Unable to verify credentials.");
            }
        }
    }

    /**
     * Sends page content with a basic description of the web service.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void sendServiceDefinition(final Cache cache, final MathPlacementSite site,
                                              final ServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.RAMREADY_SVC), null, false, Page.ADMIN_BAR, null, true, true);

        htm.sDiv("inset2");
        htm.sDiv("shaded2left");

        htm.sH(3).add(Res.get(Res.RAMREADY_SVC)).eH(3);

        htm.sP();
        htm.addln("The Mathematics Plan and Placement Status Service is available.  This is not ",
                "a full WSDL service.  It simply responds to GET requests over https to provide ",
                "student status with respect to the creation of a personalized mathematics plan and ",
                "completion of the Math Placement process.");
        htm.eP();

        htm.sP();
        htm.addln("This service is maintained by the Department of Mathematics. Please direct ",
                "questions to <a href='mailto:Steve.Benoit@colostate.edu'>",
                "Steve.Benoit@colostate.edu</a>.");
        htm.eP();

        htm.sP();
        htm.addln("All queries to this service must take place over a TLS connection and must ",
                "include a service access key provided by the Mathematics department in the ",
                "<code>X-CSUMATH-RamWeb</code> HTTP request parameter.");
        htm.eP();

        htm.sP();
        htm.addln("This service currently supports the following queries:");
        htm.eP();
        htm.hr();

        htm.sH(4).add("ramready.svc/CheckMathPlan/[PIDM]").eH(4);

        htm.sP("indent");
        htm.addln("Tests whether or not the student identified by [PIDM] has completed their");
        htm.addln("Mathematics Plan (where 'completion' means the student has clicked the");
        htm.addln("checkbox indicating they acknowledge that the plan is only a recommendation,");
        htm.addln("which allows the plan to be viewed).");
        htm.eP();

        htm.sP("indent");
        htm.addln("The response will be one of six JSON formatted objects.");
        htm.eP();

        htm.sP("indent");
        htm.addln("<span style='font-size:20pt;position:relative;top:4px;'>&bull;</span> ",
                "If the request succeeds and the student has completed the Math Plan activity:");
        htm.eP();
        htm.addln("<pre class='indent2 headercolor'>");
        htm.addln("{");
        htm.addln("  \"Message\":\"&lt;a target='_blank' ",
                "href='https://placement.math.colostate.edu/welcome/secure/shibboleth.html'&gt;",
                "Review my Mathematics Plan&lt;/a&gt;\",");
        htm.addln("  \"Status\":\"COMPLETED\"");
        htm.addln("}");
        htm.addln("</pre>");

        htm.sP("indent");
        htm.addln("<span style='font-size:20pt;position:relative;top:4px;'>&bull;</span> ",
                "If the request succeeds and the student has not yet completed the Math Plan ",
                "activity:");
        htm.eP();
        htm.addln("<pre class='indent2 headercolor'>");
        htm.addln("{");
        htm.addln("  \"Message\":\"&lt;a target='_blank' ",
                "href='https://placement.math.colostate.edu/welcome/secure/shibboleth.html'&gt;<br>",
                "    Create my Personalized Mathematics Plan&lt;/a&gt;&lt;br&gt;<br>",
                "    All majors at CSU include at least one quantitative reasoning course (for<br>",
                "    a total of three credits) to graduate. Create your Personalized Mathematics<br>",
                "    Plan to view the math or statistics course(s) for your major (or majors of<br>",
                "    interest) and to determine whether you should complete the Math Placement<br>",
                "    process before Ram Orientation.\",");
        htm.addln("  \"Status\":\"NOT STARTED\"");
        htm.addln("}");
        htm.addln("</pre>");

        htm.sP("indent");
        htm.addln("<span style='font-size:20pt;position:relative;top:4px;'>&bull;</span> ",
                "If the request fails because a PIDM was not included in the request:");
        htm.eP();
        htm.addln("<pre class='indent2 headercolor'>");
        htm.addln("{");
        htm.addln("  \"Message\":\"Mathematics Plan status is not available.\",");
        htm.addln("  \"Status\":\"NOT STARTED\"");
        htm.addln("}");
        htm.addln("</pre>");

        htm.sP("indent");
        htm.addln("<span style='font-size:20pt;position:relative;top:4px;'>&bull;</span> ",
                "If the request fails because the PIDM in the request was not valid (note the ",
                "slight difference from the message above, to distinguish these two cases):");
        htm.eP();
        htm.addln("<pre class='indent2 headercolor'>");
        htm.addln("{");
        htm.addln("  \"Message\":\"Mathematics Plan status not available.\",");
        htm.addln("  \"Status\":\"NOT STARTED\"");
        htm.addln("}");
        htm.addln("</pre>");

        htm.sP("indent");
        htm.addln("<span style='font-size:20pt;position:relative;top:4px;'>&bull;</span> ",
                "If the request fails because an unrecognized service was called (the only ",
                "recognized services are 'CheckMathPlan' and 'CheckMathPlacement'):");
        htm.eP();
        htm.addln("<pre class='indent2 headercolor'>");
        htm.addln("{");
        htm.addln("  \"Message\":\"Unknown web service called.\",");
        htm.addln("  \"Status\":\"NOT STARTED\"");
        htm.addln("}");
        htm.addln("</pre>");

        htm.sP("indent");
        htm.addln("<span style='font-size:20pt;position:relative;top:4px;'>&bull;</span> ",
                "If the request fails because the service access key was not provided in the ",
                "request or was not valid:");
        htm.eP();
        htm.addln("<pre class='indent2 headercolor'>");
        htm.addln("{");
        htm.addln("  \"Message\":\"Unable to verify credentials.\",");
        htm.addln("  \"Status\":\"NOT STARTED\"");
        htm.addln("}");
        htm.addln("</pre>");

        htm.hr();

        htm.sH(4).add("ramready.svc/CheckMathPlacement/[PIDM]").eH(4);

        htm.sP("indent");
        htm.addln("Tests whether or not the student identified by [PIDM] has completed the ",
                "Math Placement process (where 'completion' means the student has completed the Math ",
                "Placement Tool at least one time).");
        htm.eP();

        htm.sP("indent");
        htm.addln("The response will be one of seven JSON formatted objects.");
        htm.eP();

        htm.sP("indent");
        htm.addln("<span style='font-size:20pt;position:relative;top:4px;'>&bull;</span> ",
                "If the request succeeds but the student has either (1) not yet completed the ",
                "Math Plan activity, or (2) they have completed the activity, and the result was that ",
                "Math Placement is not needed - in either case, the student should not have a ",
                "checklist item requiring completion of the Placement Process:");
        htm.eP();
        htm.addln("<pre class='indent2 headercolor'>");
        htm.addln("{");
        htm.addln("  \"Message\":\"\",");
        htm.addln("  \"Status\":\"\"");
        htm.addln("}");
        htm.addln("</pre>");

        htm.sP("indent");
        htm.addln("<span style='font-size:20pt;position:relative;top:4px;'>&bull;</span> ",
                "If the request succeeds, the student has completed the Math Plan activity, the ",
                "result indicated they are required to complete Math Placement, and they have ",
                "completed the Math Placement process:");
        htm.eP();
        htm.addln("<pre class='indent2 headercolor'>");
        htm.addln("{");
        htm.addln("  \"Message\":\"&lt;a target='_blank' ",
                "href='https://placement.math.colostate.edu/welcome/secure/shibboleth.html'>",
                "Review my Mathematics Plan</a>\",");
        htm.addln("  \"Status\":\"COMPLETED\"");
        htm.addln("}");
        htm.addln("</pre>");

        htm.sP("indent");
        htm.addln("<span style='font-size:20pt;position:relative;top:4px;'>&bull;</span> ",
                "If the request succeeds, the student has completed the Math Plan activity, the ",
                "result indicated they are required to complete Math Placement, but the student has ",
                "not yet completed Math Placement:");
        htm.eP();
        htm.addln("<pre class='indent2 headercolor'>");
        htm.addln("{");
        htm.addln("  \"Message\":\"<a target='_blank' ",
                "href='https://placement.math.colostate.edu/welcome/secure/shibboleth.html'>",
                "Complete the Math Placement Process</a><br>",
                "Based on your personalized Mathematics Plan, you should complete the ",
                "Math Placement process before Ram Orientation.\",");
        htm.addln("  \"Status\":\"NOT STARTED\"");
        htm.addln("}");
        htm.addln("</pre>");

        htm.sP("indent");
        htm.addln("<span style='font-size:20pt;position:relative;top:4px;'>&bull;</span> ",
                "If the request fails because a PIDM was not included in the request:");
        htm.eP();
        htm.addln("<pre class='indent2 headercolor'>");
        htm.addln("{");
        htm.addln("  \"Message\":\"Math Placement status is not available.\",");
        htm.addln("  \"Status\":\"NOT STARTED\"");
        htm.addln("}");
        htm.addln("</pre>");

        htm.sP("indent");
        htm.addln("<span style='font-size:20pt;position:relative;top:4px;'>&bull;</span> ",
                "If the request fails because the PIDM in the request was not valid (note the ",
                "slight difference from the message above, to distinguish these two cases):");
        htm.eP();
        htm.addln("<pre class='indent2 headercolor'>");
        htm.addln("{");
        htm.addln("  \"Message\":\"Math Placement status not available.\",");
        htm.addln("  \"Status\":\"NOT STARTED\"");
        htm.addln("}");
        htm.addln("</pre>");

        htm.sP("indent");
        htm.addln("<span style='font-size:20pt;position:relative;top:4px;'>&bull;</span> ",
                "If the request fails because an unrecognized service was called (the only ",
                "recognized services are 'CheckMathPlan' and 'CheckMathPlacement'):");
        htm.eP();
        htm.addln("<pre class='indent2 headercolor'>");
        htm.addln("{");
        htm.addln("  \"Message\":\"Unknown web service called.\",");
        htm.addln("  \"Status\":\"NOT STARTED\"");
        htm.addln("}");
        htm.addln("</pre>");

        htm.sP("indent");
        htm.addln("<span style='font-size:20pt;position:relative;top:4px;'>&bull;</span> ",
                "If the request fails because the service access key was not provided in the ",
                "request or was not valid:");
        htm.eP();
        htm.addln("<pre class='indent2 headercolor'>");
        htm.addln("{");
        htm.addln("  \"Message\":\"Unable to verify credentials.\",");
        htm.addln("  \"Status\":\"NOT STARTED\"");
        htm.addln("}");
        htm.addln("</pre>");

        htm.eDiv();
        htm.eDiv();

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Looks up a student based on PIDM, tests that student's completion of the math plan, and sends a JSON-formatted
     * response.
     *
     * @param cache   the data cache
     * @param subpath The subpath, known to begin with "ramready.svc"
     * @param req     the request
     * @param resp    the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void doCheckMathPlan(final Cache cache, final String subpath, final ServletRequest req,
                                        final HttpServletResponse resp) throws IOException, SQLException {

        final int len = CHECK_MATH_PLAN.length();
        final String pidmStr = subpath.substring(len);

        try {
            final Integer pidm = Integer.valueOf(pidmStr);
            final RawStudent student = RawStudentLogic.queryByInternalId(cache, pidm);
            if (student == null) {
                Log.warning("No student found with PIDM: ", pidmStr);
                sendErrorMessage(req, resp, "Mathematics Plan status not available.");
            } else {
                final EMathPlanStatus status = MathPlanLogic.getStatus(cache, student.stuId);

                if (status == EMathPlanStatus.PLAN_COMPLETED_PLACEMENT_NEEDED
                    || status == EMathPlanStatus.PLAN_COMPLETED_PLACEMENT_NOT_NEEDED) {
                    // Student has completed math plan
                    sendCompletedPlanMessage(req, resp);
                } else {
                    // Student has not completed math plan
                    sendMathPlanNotCompletedMessage(req, resp);
                }
            }
        } catch (final NumberFormatException ex) {
            Log.warning("Invalid PIDM string: ", pidmStr, ex);
            sendErrorMessage(req, resp, "Mathematics Plan status not available.");
        }
    }

    /**
     * Looks up a student based on PIDM, tests that student's completion of math placement, and sends a JSON-formatted
     * response.
     *
     * @param cache   the data cache
     * @param subpath The subpath, known to begin with "ramready.svc"
     * @param req     the request
     * @param resp    the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void doCheckMathPlacement(final Cache cache, final String subpath, final ServletRequest req,
                                             final HttpServletResponse resp) throws IOException, SQLException {

        final int len = CHECK_MATH_PLACEMENT.length();
        final String pidmStr = subpath.substring(len);

        try {
            final Integer pidm = Integer.valueOf(pidmStr);
            final RawStudent student = RawStudentLogic.queryByInternalId(cache, pidm);

            if (student == null) {
                Log.warning("No student found with PIDM ", pidmStr);
                sendErrorMessage(req, resp, "Math Placement status not available.");
            } else {
                try {
                    final StudentData studentData = cache.getStudent(student.stuId);
                    final List<RawStmpe> placementAttempts = studentData.getLegalPlacementAttempts();

                    if (placementAttempts.isEmpty()) {
                        final EMathPlanStatus planStatus = MathPlanLogic.getStatus(cache, student.stuId);

                        if (planStatus == EMathPlanStatus.PLAN_COMPLETED_PLACEMENT_NOT_NEEDED) {
                            sendEmptyMessage(req, resp);
                        } else {
                            sendMathPlacementNotCompletedMessage(req, resp);
                        }
                    } else {
                        sendCompletedPlacementMessage(req, resp);
                    }
                } catch (final SQLException ex) {
                    Log.warning("Unable to determine placement status for: ", student.stuId, ex);
                    sendErrorMessage(req, resp, "Math Placement status not available.");
                }
            }
        } catch (final NumberFormatException ex) {
            Log.warning("Invalid PIDM string: ", pidmStr, ex);
            sendErrorMessage(req, resp, "Math Placement status not available.");
        }
    }

    /**
     * Sends the "fail gracefully" response for an error condition.
     *
     * @param req  the request
     * @param resp the response
     * @param msg  the message
     * @throws IOException if there is an error writing the response
     */
    private static void sendErrorMessage(final ServletRequest req, final HttpServletResponse resp,
                                         final String msg) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.addln("{");
        htm.addln("  \"Message\":\"", msg, "\",");
        htm.addln("  \"Status\":\"NOT STARTED\"");
        htm.addln("}");

        AbstractSite.sendReply(req, resp, "application/json", htm);
    }

    /**
     * Sends the message appropriate to a student who has completed their plan.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void sendCompletedPlanMessage(final ServletRequest req, final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.addln("{");
        htm.addln("  \"Message\":\"<a target='_blank' ",
                "href='https://placement.math.colostate.edu/welcome/secure/shibboleth.html'>",
                "Review my Mathematics Plan</a>\",");
        htm.addln("  \"Status\":\"COMPLETED\"");
        htm.addln("}");

        AbstractSite.sendReply(req, resp, "application/json", htm);
    }

    /**
     * Sends the message appropriate to a student who has completed their plan.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void sendCompletedPlacementMessage(final ServletRequest req, final HttpServletResponse resp)
            throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.addln("{");
        htm.addln("  \"Message\":\"<a target='_blank' ",
                "href='https://placement.math.colostate.edu/secure/shibboleth.html'>",
                "Review my Math Placement results</a>\",");
        htm.addln("  \"Status\":\"COMPLETED\"");
        htm.addln("}");

        AbstractSite.sendReply(req, resp, "application/json", htm);
    }

    /**
     * Sends a message with empty message and status. Used to indicate that a question is not relevant to the student
     * (and should not appear in their RamReady checklist).
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void sendEmptyMessage(final ServletRequest req, final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.addln("{");
        htm.addln("  \"Message\":\"\",");
        htm.addln("  \"Status\":\"\"");
        htm.addln("}");

        AbstractSite.sendReply(req, resp, "application/json", htm);
    }

    /**
     * Sends the message appropriate to a student who has not completed their plan.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void sendMathPlanNotCompletedMessage(final ServletRequest req, final HttpServletResponse resp)
            throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.addln("{");
        htm.addln("  \"Message\":\"<a target='_blank' ",
                "href='https://placement.math.colostate.edu/welcome/secure/shibboleth.html'>",
                "Create my Personalized Mathematics Plan</a><br>",
                "All majors at CSU include at least one quantitative reasoning course (for a total ",
                "of three credits) to graduate. Create your Personalized Mathematics Plan to view the ",
                "math or statistics course(s) for your major (or majors of interest) and to determine ",
                "whether you should complete the Math Placement process before Ram Orientation.\",");
        htm.addln("  \"Status\":\"NOT STARTED\"");
        htm.addln("}");

        AbstractSite.sendReply(req, resp, "application/json", htm);
    }

    /**
     * Sends the message appropriate to a student who has not completed Math Placement.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void sendMathPlacementNotCompletedMessage(final ServletRequest req, final HttpServletResponse resp)
            throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.addln("{");
        htm.addln("  \"Message\":\"<a target='_blank' ",
                "href='https://placement.math.colostate.edu/welcome/secure/shibboleth.html'>",
                "Complete the Math Placement Process</a><br>",
                "Based on your personalized Mathematics Plan, you should complete the ",
                "Math Placement process before Ram Orientation.\",");
        htm.addln("  \"Status\":\"NOT STARTED\"");
        htm.addln("}");

        AbstractSite.sendReply(req, resp, "application/json", htm);
    }
}

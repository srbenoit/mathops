package dev.mathops.web.site.placement.main;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.sitelogic.mathplan.MathPlanLogic;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Supports integration with RamStart, which tests whether a user has created a Math plan, and presents a status for the
 * student to be included in the RamStart checklist.
 */
enum RamStartService {
    ;

    /**
     * Sends a descriptive HTML page with details on the service and how it is used.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void sendDescription(final Cache cache, final MathPlacementSite site, final HttpServletRequest req,
                                final HttpServletResponse resp) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.RAMSTART_SVC), null, false, Page.ADMIN_BAR, null, true, true);

        htm.sDiv("inset2");
        htm.sDiv("shaded2left");

        htm.sH(3).add(Res.get(Res.RAMSTART_SVC)).eH(3);

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

        htm.sH(4).add("ramstart.svc/CheckMathPlan/[PIDM]").eH(4);

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

        htm.sH(4).add("ramstart.svc/CheckMathPlacement/[PIDM]").eH(4);

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
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Processes a request of this form.
     *
     * <pre>
     * https://[host]/[path]/ramstart
     * </pre>
     *
     * @param cache the data cache
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void process(final Cache cache, final MathPlacementSite site, final HttpServletRequest req,
                        final HttpServletResponse resp) throws IOException, SQLException {

        final String pidmString = req.getHeader("pidm");
        final String sharedSecret = req.getHeader("Authorization");

        if (pidmString == null) {
            Log.warning("Invalid or missing PIDM on RamStart request.");
            sendEmptyMessage(req, resp);
        } else if ("Bearer 93u54ki3bngtowIE".equals(sharedSecret)) {
            try {
                final int pidm = Integer.parseInt(pidmString);

                final RawStudent student = RawStudentLogic.queryByInternalId(cache, Integer.valueOf(pidm));
                if (student == null) {
                    sendEmptyMessage(req, resp);
                } else {
                    generateResponse(cache, site, req, resp, pidm, student);
                }
            } catch (final NumberFormatException ex) {
                Log.warning("Invalid PIDM string: ", pidmString, ex);
                sendEmptyMessage(req, resp);
            }
        } else {
            Log.warning("Invalid or missing authorization code on RamStart request.");
            resp.sendError(401);
        }
    }

    /**
     * Generates the response message.
     *
     * @param cache   the data cache
     * @param req     the request
     * @param resp    the response
     * @param pidm    the student PIDM
     * @param student the student record
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void generateResponse(final Cache cache, final MathPlacementSite site, final HttpServletRequest req,
                                         final HttpServletResponse resp, final int pidm,  final RawStudent student)
            throws IOException, SQLException {

        final HtmlBuilder json = new HtmlBuilder(500);

        json.addln("{");
        json.addln("  \"description\": \"For questions about Math Placement, please contact the Precalculus ",
                "Center at <a href='mailto:precalc_math@colostate.edu'>precalc_math@colostate.edu</a> or ",
                "(970) 491-5761\",");

        final LocalDateTime whenMathPlanCompleted = MathPlanLogic.getMathPlanWhenCompleted(cache, pidm);
        final LocalDateTime whenPlacementCompleted = MathPlanLogic.hasTakenPlacement(cache, student.stuId);

        // 0 = Placement not needed; 1 = placement needed but not yet completed; 2 = placement completed or
        // requirements satisfied
        final int status = MathPlanLogic.getMathPlacementStatus(cache, pidm);


        if (whenMathPlanCompleted == null || whenPlacementCompleted == null) {

        } else {
            // Both have been completed
        }

        final MathPlanLogic logic = new MathPlanLogic(site.getDbProfile());


        json.addln("}");
        AbstractSite.sendReply(req, resp, Page.MIME_APP_JSON, json.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Sends the message appropriate to a student who has completed their plan.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void sendCompletedPlanMessage(final ServletRequest req,
                                                 final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.addln("{");
        htm.addln("  \"Message\":\"<a target='_blank' ",
                "href='https://placement.math.colostate.edu/welcome/secure/shibboleth.html'>",
                "Review my Mathematics Plan</a>\",");
        htm.addln("  \"Status\":\"COMPLETED\"");
        htm.addln("}");

        AbstractSite.sendReply(req, resp, "application/json", htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Sends the message appropriate to a student who has completed their plan.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void sendCompletedPlacementMessage(final ServletRequest req,
                                                      final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.addln("{");
        htm.addln("  \"Message\":\"<a target='_blank' ",
                "href='https://placement.math.colostate.edu/secure/shibboleth.html'>",
                "Review my Math Placement results</a>\",");
        htm.addln("  \"Status\":\"COMPLETED\"");
        htm.addln("}");

        AbstractSite.sendReply(req, resp, "application/json", htm.toString().getBytes(StandardCharsets.UTF_8));
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

        final String reply = "{}";

        AbstractSite.sendReply(req, resp, "application/json", reply.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Sends the message appropriate to a student who has not completed their plan.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void sendMathPlanNotCompletedMessage(final ServletRequest req,
                                                        final HttpServletResponse resp) throws IOException {

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

        AbstractSite.sendReply(req, resp, "application/json", htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Sends the message appropriate to a student who has not completed Math Placement.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    private static void sendMathPlacementNotCompletedMessage(final ServletRequest req,
                                                             final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.addln("{");
        htm.addln("  \"Message\":\"<a target='_blank' ",
                "href='https://placement.math.colostate.edu/welcome/secure/shibboleth.html'>",
                "Complete the Math Placement Process</a><br>",
                "Based on your personalized Mathematics Plan, you should complete the ",
                "Math Placement process before Ram Orientation.\",");
        htm.addln("  \"Status\":\"NOT STARTED\"");
        htm.addln("}");

        AbstractSite.sendReply(req, resp, "application/json", htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}

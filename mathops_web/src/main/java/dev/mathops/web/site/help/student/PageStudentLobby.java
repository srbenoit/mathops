package dev.mathops.web.site.help.student;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.help.HelpLogic;
import dev.mathops.web.site.help.HelpSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * The lobby, where students who have selected a context (course, unit, etc.) can wait for a tutor to accept their help
 * request and create a session, at which time they are redirected to the help page.
 *
 * <p>
 * This page gives students the option of posting a question to the forums, or to cancel theor request.
 */
public enum PageStudentLobby {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final HelpSite site, final HttpServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HelpContext context = new HelpContext(req);
        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.NO_BARS, null, false, true);

        emitPageContent(site, req, htm, context, session);

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Emits page content.
     *
     * @param site    the owning site
     * @param req     the request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param context the help context
     * @param session the login session
     */
    private static void emitPageContent(final HelpSite site, final ServletRequest req, final HtmlBuilder htm,
                                        final HelpContext context, final ImmutableSessionInfo session) {

        htm.sH(1).add(Res.get(Res.LIVE_HELP_HEADING)).eH(1);

        htm.div("vgap");

        if (HelpLogic.isHelpOpenNow()) {
            emitWebsocketScript(site, req, htm, context, session);
            emitPleaseWaitAndThrobber(htm);
            htm.div("clear").hr();

            emitOpenEmailOptions(context, htm);
        } else {
            emitNotOpenMessage(htm);
            htm.div("clear").hr();

            emitClosedEmailOptions(htm);
        }
        htm.div("vgap").hr();
    }

    /**
     * Emits scripts needed to manage the web socket.
     *
     * @param site    the owning site
     * @param req     the request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param context the help context
     * @param session the login session
     */
    private static void emitWebsocketScript(final HelpSite site, final ServletRequest req,
                                            final HtmlBuilder htm, final HelpContext context,
                                            final ImmutableSessionInfo session) {

        if (context.course != null) {
            final String path = site.siteProfile.path;
            final String redirect = path + (path.endsWith(CoreConstants.SLASH) ? "help.html" : "/help.html");

            htm.addln("<script>");

            htm.addln(" let socket = new WebSocket(\"wss://", req.getServerName(), "/ws/helpqueue\");");

            htm.addln(" socket.onopen = function(e) {");
            htm.addln("   socket.send('Session:", session.loginSessionId, "');");
            htm.add("   socket.send('Help:c=", context.course);
            if (context.unit >= 0) {
                htm.add("&u=", Integer.toString(context.unit));
                if (context.obj >= 0) {
                    htm.add("&o=", Integer.toString(context.obj));
                }
            }
            htm.addln("');");
            htm.addln(" };");

            htm.addln(" socket.onmessage = function(event) {");
            htm.addln("   if (event.data.startsWith('StartHelp:')) {");
            htm.addln("     token = event.data.substring(10);");
            htm.addln("     url = '", redirect, "?token=' + token;");
            htm.addln("     window.location=url;");
            htm.addln("   }");
            htm.addln(" };");

            htm.addln(" socket.onclose = function(event) {");
            htm.addln("   getElementById('throbber').setInnerHtml(",
                    "'Unable to connect to a learning assistant at this Time. Please try again later.');");
            htm.addln(" };");

            htm.addln(" socket.onerror = function(error) {");
            htm.addln("   getElementById('throbber').setInnerHtml(",
                    "'Unable to connect to a learning assistant at this Time. Please try again later.');");
            htm.addln(" };");

            htm.addln(" setInterval(\"touch()\", 5000);");
            htm.addln(" function touch () {");
            htm.addln("   socket.send('Touch');");
            htm.addln(" }");

            htm.addln("</script>");
        }
    }

    /**
     * Emits a "please wait" message and throbber.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitPleaseWaitAndThrobber(final HtmlBuilder htm) {

        htm.sDiv("left")
                .add("<img class='hidebelow600' src='/images/help/coffee_with_laptop.png' ",
                        "style='border:1px solid #aaa; margin:3px 15px 10px 0;'/>")
                .eDiv();

        htm.sDiv(null, "style='display:inline-block;line-height:1.4em;'");
        htm.add("Please wait...");
        htm.sDiv("indent0").add("We are connecting you with a learning assistant.").eDiv();

        htm.sDiv("center", "id='throbber'");
        htm.addln("<svg xmlns='http://www.w3.org/2000/svg' ",
                "xmlns:xlink='http://www.w3.org/1999/xlink' ",
                "style='margin:auto;background:#fff;display:block;' ",
                "width='70px' height='70px' viewBox='0 0 100 100' preserveAspectRatio='xMidYMid'>");
        htm.addln("<circle cx='50' cy='50' r='24' stroke-width='6' stroke='#1e4d2b' ",
                "stroke-dasharray='37.7 37.7' fill='none' stroke-linecap='round' ",
                "transform='rotate(0 50 50)'>");
        htm.addln("<animateTransform attributeName='transform' type='rotate' dur='2s' ",
                "repeatCount='indefinite' keyTimes='0;1' values='0 50 50;360 50 50'>",
                "</animateTransform>");
        htm.addln("</circle>");
        htm.addln("<circle cx='50' cy='50' r='17' stroke-width='6' stroke='#c8c372' ",
                "stroke-dasharray='26.7 26.73' stroke-dashoffset='26.7' fill='none' ",
                "stroke-linecap='round' transform='rotate(0 50 50)'>");
        htm.addln("<animateTransform attributeName='transform' type='rotate' dur='2s' ",
                "repeatCount='indefinite' keyTimes='0;1' values='0 50 50;-360 50 50'>",
                "</animateTransform>");
        htm.addln("</circle>");
        htm.addln("</svg>");
        htm.eDiv();

        htm.eDiv();
    }

    /**
     * Emits a message indicating online help is not currently open.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitNotOpenMessage(final HtmlBuilder htm) {

        final LocalDateTime whenAvailable = HelpLogic.getWhenNextAvailable();

        htm.sDiv("left")
                .add("<img class='hidebelow500' src='/images/help/sleeping.png' ",
                        "style='border:1px solid #aaa; margin:3px 15px 10px 0;'/>")
                .eDiv();

        htm.add("Our online learning assistants are not currently available.").br().br();
        htm.add("Course assistants will be back online at ",
                TemporalUtils.FMT_HM_A.format(whenAvailable), " on ",
                TemporalUtils.FMT_WMD.format(whenAvailable), CoreConstants.DOT);
    }

    /**
     * Emits a message inviting student to ask a question in a forum or via email.
     *
     * @param context the help context
     * @param htm     the {@code HtmlBuilder} to which to append
     */
    private static void emitOpenEmailOptions(final HelpContext context, final HtmlBuilder htm) {

        htm.add("Don't want to wait?").br().br();

        htm.add("You can also post your question to the course forum, or email our course ",
                "assistants.");

        htm.div("clear");
        htm.div("vgap2");

        htm.addln("<script>");
        htm.addln(" function pick(target) {");
        htm.addln("  window.location.assign(target);");
        htm.addln(" }");
        htm.addln("</script>");

        htm.add("<nav class='action'>")
                .add("<button class='action' onclick='pick(\"forum.html?c=",
                        context.course);
        if (context.unit >= 0) {
            htm.add("&u=", Integer.toString(context.unit));
            if (context.obj >= 0) {
                htm.add("&o=", Integer.toString(context.obj));
            }
        }
        htm.add("\");'Post to the<br>Course Forum</button>").add("</nav>");

        htm.sP().add("The course forum is shared by all students in the course.  Course assistants ",
                "monitor these forums, but other students may be able to help if they see your ",
                "post before our learning assistants.").eP();

        htm.div("clear");
        htm.div("vgap");

        htm.add("<nav class='action'>").add("<button class='action' onclick='pick(\"email.html?c=", context.course);
        if (context.unit >= 0) {
            htm.add("&u=", Integer.toString(context.unit));
            if (context.obj >= 0) {
                htm.add("&o=", Integer.toString(context.obj));
            }
        }
        htm.add("\");'>", "E-mail a <br>Course Assistant</button>").add("</nav>");

        htm.sP().add("Our learning assistants will respond to e-mail questions when they are not ",
                "working with students.").eP();

        htm.div("clear");
    }

    /**
     * Emits a message inviting student to ask a question in a forum or via email.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitClosedEmailOptions(final HtmlBuilder htm) {

        htm.add("We would still like to help!").br().br();

        htm.add("You can post your question to the course forum, or email our course assistants.");

        htm.div("clear");
        htm.div("vgap2");

        htm.addln("<script>");
        htm.addln(" function pick(target) {");
        htm.addln("  window.location.assign(target);");
        htm.addln(" }");
        htm.addln("</script>");

        htm.add("<nav class='action'>")
                .add("<button class='action' onclick='pick(\"forum.html?id=117\");'>",
                        "Post to the<br>Course Forum</button>")
                .add("</nav>");

        htm.sP().add("The course forum is shared by all students in the course.  Other students ",
                "may be able to help if they see your post before our learning assistants.").eP();

        htm.div("clear");
        htm.div("vgap");

        htm.add("<nav class='action'>")
                .add("<button class='action' onclick='pick(\"email.html?id=117\");'>",
                        "E-mail a <br>Course Assistant</button>")
                .add("</nav>");

        htm.sP().add("When our learning assistants return, they will respond to e-mail questions, ",
                "and check the course forums for unanswered questions.").eP();

        htm.div("clear");
    }
}

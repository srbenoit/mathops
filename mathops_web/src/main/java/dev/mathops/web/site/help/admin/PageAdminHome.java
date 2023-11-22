package dev.mathops.web.site.help.admin;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.help.HelpAdminBar;
import dev.mathops.web.site.help.HelpSite;
import dev.mathops.web.skin.IServletSkin;
import dev.mathops.web.skin.SkinnedPage;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Generates the home page for an ADMINISTRATOR user. This page shows the state of the online help system, and gives the
 * user the option to act as a TUTOR or STUDENT.
 */
public enum PageAdminHome {
    ;

    /**
     * Processes a GET in un-maximized form.
     *
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException if there is an error writing the response
     */
    public static void doGet(final HelpSite site, final ServletRequest req, final ServletResponse resp,
                             final ImmutableSessionInfo session) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        SkinnedPage.startOrdinaryPage(site, htm, false, Res.get(Res.SITE_TITLE), "homemax.html",
                HelpAdminBar.INSTANCE, session);

        final IServletSkin skin = site.skin;
        final int delta = skin.getTopBarHeight(false, false) + skin.getBottomBarHeight(false)
                + HelpAdminBar.INSTANCE.getAdminBarHeight(session) + 32; // 32 from .layer padding

        htm.sDiv("admingrid",
                "style='height:calc(100vh - " + delta + "px);'");

        emitStatsPane(htm);
        emitHoursPane(htm);
        emitTutorsPane(htm);
        emitQueuePane(htm);
        emitLogPane(htm);

        htm.eDiv();

        emitScripts(req, htm, session);

        SkinnedPage.endOrdinaryPage(site, htm);
        SkinnedPage.sendReply(req, resp, SkinnedPage.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Processes a GET in maximized form.
     *
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException if there is an error writing the response
     */
    public static void doMaxGet(final HelpSite site, final ServletRequest req, final ServletResponse resp,
                                final ImmutableSessionInfo session) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        SkinnedPage.startOrdinaryMaxPage(site, htm, false, Res.get(Res.SITE_TITLE), "home.html",
                HelpAdminBar.INSTANCE, session);

        final IServletSkin skin = site.skin;
        final int delta = skin.getTopBarHeight(false, true) + skin.getBottomBarHeight(true)
                + HelpAdminBar.INSTANCE.getAdminBarHeight(session);

        htm.sDiv("admingrid",
                "style='height:calc(100vh - " + delta + "px);'");

        emitStatsPane(htm);
        emitHoursPane(htm);
        emitTutorsPane(htm);
        emitQueuePane(htm);
        emitLogPane(htm);

        htm.eDiv();

        emitScripts(req, htm, session);

        SkinnedPage.endOrdinaryMaxPage(site, htm);
        SkinnedPage.sendReply(req, resp, SkinnedPage.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Emits the "stats" pane.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitStatsPane(final HtmlBuilder htm) {

        htm.sDiv(null, "style='background:GhostWhite; border:1px solid black; grid-area:stats;'");

        // Title bar
        htm.sDiv(null,
                "style='background: #59595B; width:100%; height:26px; font-family:factoria-medium; ",
                "font-size:14px; padding:2px 0 0 6px; color:white; border-bottom:1px solid black; ",
                "text-shadow: 0 0 3px black;'");
        htm.add("<img style='margin-bottom:-6px; padding-right:10px;' ",
                "src='/images/help/stats.png'/>");
        htm.add("Reports and Statistics");
        htm.eDiv();

        htm.sDiv(null, "style='background:#f2f2f2; padding:2px 6px; height:calc(100% - 26px);' ",
                "id='hadm_stats'");
        htm.eDiv();

        htm.eDiv();
    }

    /**
     * Emits the "hours" pane.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitHoursPane(final HtmlBuilder htm) {

        htm.sDiv(null, "style='background:GhostWhite; border:1px solid black; grid-area:hours;'");

        // Title bar
        htm.sDiv(null,
                "style='background: #59595B; width:100%; height:26px; font-family:factoria-medium; ",
                "font-size:14px; padding:2px 0 0 6px; color:white; border-bottom:1px solid black; ",
                "text-shadow: 0 0 3px black;'");
        htm.add("<img style='margin-bottom:-6px; padding-right:10px;' ",
                "src='/images/help/clock.png'/>");
        htm.add("Online Help Hours");
        htm.eDiv();

        htm.sDiv(null, "style='background:#f2f2f2; height:calc(100% - 26px); font-size:12px; ",
                "line-height:17px; padding:2px 6px; overflow-y:scroll;' id='hadm_hours'");
        htm.eDiv();

        htm.eDiv();
    }

    /**
     * Emits the "tutors" pane.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitTutorsPane(final HtmlBuilder htm) {

        htm.sDiv(null, "style='background:GhostWhite; border:1px solid black; grid-area:tutors;'");

        // Title bar
        htm.sDiv(null,
                "style='background: #D9782D; width:100%; height:26px; font-family:factoria-medium; ",
                "font-size:14px; padding:2px 0 0 6px; color:white; border-bottom:1px solid black; ",
                "text-shadow: 0 0 3px black;'");
        htm.add("<img style='margin-bottom:-6px; padding-right:10px;' ",
                "src='/images/help/tutors.png'/>");
        htm.add("Active Tutors");
        htm.eDiv();

        htm.sDiv(null, "style='width:100%; background:#ffede2; color:black; font-size:12px; "
                        + "line-height:17px; padding:4px 6px; height:calc(100% - 26px); overflow-y:scroll;'",
                "id='hadm_tutors'");
        htm.eDiv();

        htm.eDiv();
    }

    /**
     * Emits the "queue" pane.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitQueuePane(final HtmlBuilder htm) {

        htm.sDiv(null, "style='background:GhostWhite; border:1px solid black; grid-area:queue;'");

        // Title bar
        htm.sDiv(null,
                "style='background: #1e4d2b; width:100%; height:26px; font-family:factoria-medium; ",
                "font-size:14px; padding:2px 0 0 6px; color:white; border-bottom:1px solid black; ",
                "text-shadow: 0 0 3px black;'");
        htm.add("<img style='margin-bottom:-6px; padding-right:10px;' ",
                "src='/images/help/queue.png'/>");
        htm.add("Live Help Queue");
        htm.eDiv();

        // Average wait time
        htm.sDiv(null,
                "style='background: #C8C372; width:100%; height:19px; font-family:factoria-medium; "
                        + "font-size:12px; line-height:17px; padding:2px 0 0 6px; color:black; "
                        + "border-bottom:1px solid black;'");
        htm.add("Avg. Wait Time: ");
        htm.add("<span id='hadm_waittime'> </span>.");
        htm.eDiv();

        // Student help requests

        htm.sDiv(null, "style='width:100%; background:#e5f5e8; color:black; font-size:12px; "
                        + "line-height:17px; padding:4px 6px; height:calc(100% - 45px); overflow-y:scroll;'",
                "id='hadm_queue'");
        htm.eDiv();

        htm.eDiv();
    }

    /**
     * Emits the "log" pane.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitLogPane(final HtmlBuilder htm) {

        htm.sDiv(null, "style='background:GhostWhite; border:1px solid black; grid-area:log;'");

        // Title bar
        htm.sDiv(null,
                "style='background: #59595B; width:100%; height:26px; font-family:factoria-medium; ",
                "font-size:14px; padding:2px 0 0 6px; color:white; border-bottom:1px solid black; ",
                "text-shadow: 0 0 3px black;'");
        htm.add("<img style='margin-bottom:-6px; padding-right:10px;' ",
                "src='/images/help/log.png'/>");
        htm.add("Activity Monitor");
        htm.eDiv();

        htm.sDiv(null, "style='width:100%; background:#e5f5e8; color:black; font-size:12px; "
                        + "line-height:17px; padding:2px 6px; height:calc(100% - 26px); overflow-y:scroll;'",
                "id='hadm_log'");
        htm.eDiv();

        htm.eDiv();
    }

    /**
     * Emits scripts to manage web socket connection to server.
     *
     * @param req     the request, from which to obtain server name
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param session the login session
     */
    private static void emitScripts(final ServletRequest req, final HtmlBuilder htm,
                                    final ImmutableSessionInfo session) {

        htm.addln("<script>");
        htm.addln(" function processHours(obj) {");
        htm.addln("  var h = \"\";");
        htm.addln("  if (obj.hours.hasOwnProperty('dateRanges')) {");
        htm.addln("   obj.hours.dateRanges.forEach(hoursAppedRange);");
        htm.addln("   function hoursAppedRange(r) {");
        htm.addln("    if (r.hasOwnProperty('start') && r.hasOwnProperty('end')) {");
        htm.addln("     h = h + \"<b>\" + r.start + \"</b> to <b>\" + r.end + \"</b><br/>\";");
        htm.addln("     r.blocks.forEach(hoursAppendBlock);");
        htm.addln("    }");
        htm.addln("   }");
        htm.addln("   function hoursAppendBlock(b) {");
        htm.addln("    for (i = 0; i < b.timeRanges.length; ++i) {");
        htm.addln("     h = h + \" &nbsp;<tt> \" + b.weekdays + \"&nbsp; \" ",
                "+ b.timeRanges[i].start + \" - \" + b.timeRanges[i].end + \"</tt><br/>\";");
        htm.addln("    }");
        htm.addln("   }");
        htm.addln("  }");
        htm.addln("  document.getElementById(\"hadm_hours\").innerHTML = h;");
        htm.addln(" }");

        htm.addln(" function processQueue(obj) {");
        htm.addln("  document.getElementById(\"hadm_waittime\").innerHTML = obj.avgWaitTime;");

        htm.addln("  if (obj.hasOwnProperty('tutors')) {");
        htm.addln("   var tutors = obj.tutors;");
        htm.addln("   var tutstr = \"\";");
        htm.addln("   var numtut = tutors.length;");
        htm.addln("   for (var i = 0; i < numtut; i++) {");
        htm.addln("     tutstr = tutstr + tutors[i].name + \" (\" + tutors[i].stu + \")<br/>\";");
        htm.addln("   }");
        htm.addln("   document.getElementById(\"hadm_tutors\").innerHTML = tutstr;");
        htm.addln("  }");

        htm.addln("  var s = \"\";");

        htm.addln("  if (obj.hasOwnProperty('active')) {");
        htm.addln("   var req  = obj.active;");

        htm.addln("   var numactive = req.length;");
        htm.addln("   for (var i = 0; i < numactive; i++) {");
        htm.addln("     s = s + req[i].screen + \" (\" + req[i].stu + \")\";");

        htm.addln("     if (req[i].course != null) {");
        htm.addln("       s = s + \" [\" + req[i].course;");
        htm.addln("       if (req[i].unit != null) {");
        htm.addln("         s = s + \".\" + req[i].unit;");
        htm.addln("         if (req[i].objective != null) {");
        htm.addln("           s = s + \".\" + req[i].objective;");
        htm.addln("         }");
        htm.addln("       }");
        htm.addln("       s = s + \"]\";");
        htm.addln("     } else if (req[i].hw != null) {");
        htm.addln("       s = s.concat(\" [HW]\");");
        htm.addln("     } else if (req[i].past != null) {");
        htm.addln("       s = s.concat(\" [Exam]\");");
        htm.addln("     } else if (req[i].media != null) {");
        htm.addln("       s = s.concat(\" [Video]\");");
        htm.addln("     }");
        htm.addln("     s = s + \" - \" + req[i].wait + \"<br/>\";");
        htm.addln("   }");
        htm.addln("  }");

        htm.addln("  s = s + \"<hr/><span style='color:gray'>\";");

        htm.addln("  if (obj.hasOwnProperty('canceled')) {");
        htm.addln("   var req  = obj.canceled;");

        htm.addln("   var numactive = req.length;");
        htm.addln("   for (var i = 0; i < numactive; i++) {");
        htm.addln("     s = s + req[i].screen + \" (\" + req[i].stu + \")\";");

        htm.addln("     if (req[i].course != null) {");
        htm.addln("       s = s + \" [\" + req[i].course;");
        htm.addln("       if (req[i].unit != null) {");
        htm.addln("         s = s + \".\" + req[i].unit;");
        htm.addln("         if (req[i].objective != null) {");
        htm.addln("           s = s + \".\" + req[i].objective;");
        htm.addln("         }");
        htm.addln("       }");
        htm.addln("       s = s + \"]\";");
        htm.addln("     } else if (req[i].hw != null) {");
        htm.addln("       s = s + \" (HW)\";");
        htm.addln("     } else if (req[i].past != null) {");
        htm.addln("       s = s + \" (Exam)\";");
        htm.addln("     } else if (req[i].media != null) {");
        htm.addln("       s = s + \" (Video)\";");
        htm.addln("     }");
        htm.addln("     s = s + \" - \" + req[i].wait + \"<br/>\";");
        htm.addln("   }");
        htm.addln("  }");
        htm.addln("  s = s + \"</span>\";");

        // htm.addln(" alert(s);");
        htm.addln("  document.getElementById(\"hadm_queue\").innerHTML = s;");
        htm.addln(" }");

        htm.addln(" function processLog(obj) {");
        htm.addln("   var log = obj.log;");
        htm.addln("   var logstr = \"\";");
        htm.addln("   var numlog = log.length;");
        htm.addln("   for (var i = 0; i < numlog; i++) {");
        htm.addln("     logstr = logstr + log[i] + \"<br/>\";");
        htm.addln("   }");
        htm.addln("   document.getElementById(\"hadm_log\").innerHTML = logstr;");
        htm.addln(" }");

        htm.addln(" var hqSock = new WebSocket(\"wss://", req.getServerName(),
                "/ws/helpqueue\");");

        htm.addln(" hqSock.onmessage = function(event) {");
        htm.addln("  var obj = JSON.parse(event.data);");
        htm.addln("  if (obj.hasOwnProperty(\"hours\")) {");
        htm.addln("   processHours(obj);");
        htm.addln("  } else if (obj.hasOwnProperty(\"avgWaitTime\")) {");
        htm.addln("   processQueue(obj);");
        htm.addln("  } else if (obj.hasOwnProperty(\"log\")) {");
        htm.addln("   processLog(obj);");
        htm.addln("  }");
        htm.addln(" }");

        htm.addln(" hqSock.onopen = function(event) {");
        htm.addln("  hqSock.send(\"Session:" + session.loginSessionId
                + "\");");
        htm.addln("  hqSock.send(\"GetHours\");");
        htm.addln("  hqSock.send(\"GetQueue\");");
        htm.addln("  hqSock.send(\"GetStats\");");
        htm.addln("  hqSock.send(\"GetLog\");");
        htm.addln(" };");
        htm.addln("</script>");

        htm.eDiv();
    }
}

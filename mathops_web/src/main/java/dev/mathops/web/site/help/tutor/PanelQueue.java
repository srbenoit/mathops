package dev.mathops.web.site.help.tutor;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.session.ImmutableSessionInfo;

import jakarta.servlet.ServletRequest;

/**
 * Generates the content of the "Request Queue" panel. This panel lists the learning assistants who are "on duty" and
 * the queue of student requests. It has a button for a learning assistant to claim a request and review its context.
 * The queue should update automatically every few seconds.
 */
enum PanelQueue {
    ;

    /**
     * Generates the panel content.
     *
     * @param req       the servlet request, used to get the host information
     * @param session   the login session
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param maximized true if page is maximized
     */
    static void emitPanel(final ServletRequest req, final ImmutableSessionInfo session,
                          final HtmlBuilder htm, final boolean maximized) {

        htm.sDiv("threecolumnpane", "style='background:white; border:1px solid black;'");

        // Title bar
        htm.sDiv(null, "style='background: #1e4d2b; width:100%; height:26px; font-family:factoria-medium; ",
                "font-size:14px; padding:2px 0 0 6px; color:white; border-bottom:1px solid black;'");
        htm.add("<img style='margin-bottom:-6px; padding-right:10px;' ",
                "src='/images/help/queue.png'/>");
        htm.add("Live Help Queue");
        htm.eDiv();

        // Average wait time
        htm.sDiv(null, "style='background: #C8C372; width:100%; height:19px; font-family:factoria-medium; ",
                "font-size:12px; padding:4px 0 0 6px; color:black;'");
        htm.add("Avg. Wait Time: ");
        htm.add("<span id='hqrq_eproijt345'> </span>.");
        htm.eDiv();

        // Tutors online
        htm.sDiv(null, "style='background: #FFF; width:100%; height:21px; font-family:factoria-medium; ",
                "font-size:12px; padding:4px 0 0 6px; color:black; border-top: 1px #1e4d2b solid; ",
                "border-bottom:1px #1e4d2b solid;'");
        htm.add("Tutors On-Line:");
        htm.eDiv();
        htm.sDiv(null, "style='width:100%; background:#d5f0d9; color:black; font-size:12px; ",
                "line-height:17px; font-weight:500; padding:4px 6px; height:84px; overflow-y:scroll;' ",
                "id='hqrq_x249u3405j32'");
        htm.eDiv();

        // Students help requests
        htm.sDiv(null, "style='background: #FFF; width:100%; height:21px; font-family:factoria-medium; ",
                "font-size:12px; padding:4px 0 0 6px; color:black; border-top: 1px #1e4d2b solid; ",
                "border-bottom:1px #1e4d2b solid;'");
        htm.add("Student Help Requests:");
        htm.eDiv();

        htm.sDiv(null, "style='height:21px; background:#e5f5e8; text-align:center; padding:2px;'");
        htm.add("<form action='", maximized ? "livehelpmax.html" : "livehelp.html", "' method='get'>");
        htm.add("  <input style='font-size:12px;' id='hqrq_p9wum4vo3' type='submit' value='Accept Request'/>");
        htm.add("</form>");
        htm.eDiv();

        htm.sDiv(null, "style='width:100%; background:#e5f5e8; color:black; font-size:12px; ",
                "line-height:17px; font-weight:500; padding:4px 6px; min-height:calc(100% - 192px); ",
                "overflow-y:scroll;' id='hqrq_asdp4i33ea'");
        htm.eDiv();

        final String host = req.getServerName();
        htm.addln("<script>");
        htm.addln(" var hqSock = new WebSocket(\"wss://", host, "/ws/helpqueue\");");

        htm.addln(" hqSock.onmessage = function (event) {");
        htm.addln("   var obj  = JSON.parse(event.data);");

        htm.addln("   var wait  = obj.avgWaitTime;");
        htm.addln("   document.getElementById(\"hqrq_eproijt345\").innerHTML = wait;");

        htm.addln("   var tutors = obj.tutors;");
        htm.addln("   var tutstr = \"\";");
        htm.addln("   var numtut = tutors.length;");
        htm.addln("   for (var i = 0; i < numtut; i++) {");
        htm.addln("     tutstr = tutstr + tutors[i].name + \"<br/>\";");
        htm.addln("   }");
        htm.addln("   document.getElementById(\"hqrq_x249u3405j32\").innerHTML = tutstr;");

        htm.addln("   var req  = obj.active;");
        htm.addln("   var numreq = req.length;");
        htm.addln("   if (numreq > 0) {");
        htm.addln("     document.getElementById(\"hqrq_p9wum4vo3\").disabled = false;");
        htm.addln("   } else {");
        htm.addln("     document.getElementById(\"hqrq_p9wum4vo3\").disabled = true;");
        htm.addln("   }");

        htm.addln("   var s = \"\";");
        htm.addln("   for (var i = 0; i < numreq; i++) {");
        htm.addln("     if (i == 0) { s = s.concat(\"<b>\")}");
        htm.addln("     s = s + req[i].screen;");
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
        htm.addln("       s = s + \" [HW]\";");
        htm.addln("     } else if (req[i].past != null) {");
        htm.addln("       s = s + \" [Exam]\";");
        htm.addln("     } else if (req[i].media != null) {");
        htm.addln("       s = s + \" [Video]\";");
        htm.addln("     }");
        htm.addln("     s = s + \" - \" + req[i].wait;");
        htm.addln("     if (i == 0) { s = s.concat(\"</b>\")}");
        htm.addln("     s = s + \"<br/>\";");
        htm.addln("   }");
        htm.addln("   document.getElementById(\"hqrq_asdp4i33ea\").innerHTML = s;");
        htm.addln(" }");

        htm.addln(" hqSock.onopen = function (event) {");
        htm.addln("   hqSock.send(\"Session:" + session.loginSessionId + "\");");
        htm.addln("   hqSock.send(\"GetQueue\");");
        htm.addln(" };");
        htm.addln("</script>");

        htm.eDiv();
    }
}

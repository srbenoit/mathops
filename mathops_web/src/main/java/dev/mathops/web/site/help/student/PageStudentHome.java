package dev.mathops.web.site.help.student;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.help.HelpAdminBar;
import dev.mathops.web.site.help.HelpSite;
import dev.mathops.web.skin.SkinnedPage;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Generates the live help page.
 */
public final class PageStudentHome {

    /**
     * Private constructor to prevent instantiation.
     */
    private PageStudentHome() {

        super();
    }

    /**
     * Generates the page.
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
        SkinnedPage.startOrdinaryPage(site, htm, false, Res.get(Res.SITE_TITLE), null, HelpAdminBar.INSTANCE,
                session);

        htm.sH(1).add(Res.get(Res.LIVE_HELP_HEADING)).eH(1);

        htm.div("vgap");

        htm.sDiv("left")
                .add("<img class='hidebelow600' src='/images/help/laptop.png' ",
                        "style='border:1px solid #aaa; margin:3px 15px 10px 0;'/>")
                .eDiv();

        htm.sDiv(null, "style='display:inline-block;line-height:1.4em;'");
        htm.add("Providing live, online help with:<br/>");
        htm.add("&nbsp;&bull; &nbsp;Precalculus courses (MATH 117 ",
                "<span class='hidebelow400'>through</span><span class='hideabove400'>-</span> 126)<br/>");
        htm.add("&nbsp;&bull; &nbsp;The Entry-Level Math (ELM) Tutorial<br/>");
        htm.add("&nbsp;&bull; &nbsp;The Precalculus Tutorials<br/>");
        htm.add("&nbsp;&bull; &nbsp;Math Placement review<br/>");
        htm.eDiv();

        htm.div("clear");
        htm.sP();
        htm.addln("<strong>Hours of operation</strong>:");
        htm.eP();
        htm.sP("indent", "id='hstu_hours'").eP();

        htm.hr();

        htm.div("vgap");
        htm.sP();
        htm.addln("<strong>Please select from the following</strong>:");
        htm.eP();

        htm.addln("<script>");
        htm.addln(" function pick(target) {");
        htm.addln("  window.location.assign(target);");
        htm.addln(" }");
        htm.addln("</script>");

        htm.add("<nav>");
        htm.add("<button class='nav4' id='first' ",
                "onclick='pick(\"topic.html?id=117\");'>MATH 117</button>");
        htm.add("<button class='nav4' ",
                "onclick='pick(\"topic.html?id=118\");'>MATH 118</button>");
        htm.add("<button class='nav4' ",
                "onclick='pick(\"topic.html?id=124\");'>MATH 124</button>");
        htm.add("<button class='nav4' id='last' ",
                "onclick='pick(\"topic.html?id=125\");'>MATH 125</button>");
        htm.add("<button class='nav4' id='first' ",
                "onclick='pick(\"topic.html?id=126\");'>MATH 126</button>");
        htm.add("<button class='nav4' ",
                "onclick='pick(\"topic.html?id=ELM\");'>ELM Tutorial</button>");
        htm.add("<button class='nav4' ",
                "onclick='pick(\"topic.html?id=PCT\");'>Precalc Tutorials</button>");
        htm.add("<button class='nav4' id='last' ",
                "onclick='pick(\"topic.html?id=MPR\");'>Math Placement</button>");
        htm.addln("</nav>");

        htm.addln("<script>");

        htm.addln(" let hqSock = new WebSocket(\"wss://",
                req.getServerName(), "/ws/helpqueue\");");

        htm.addln(" hqSock.onopen = function(e) {");
        htm.addln("   hqSock.send('Session:", session.loginSessionId,
                "');");
        htm.addln("   hqSock.send('GetHours');");
        htm.addln(" };");

        htm.addln(" function processHours(obj) {");
        htm.addln("  var h = \"\";");

        htm.addln("  if (obj.hours.hasOwnProperty('dateRanges')) {");
        htm.addln("    obj.hours.dateRanges.forEach(hoursAppedRange);");
        htm.addln("    function hoursAppedRange(r) {");
        htm.addln("      if (r.hasOwnProperty('start') && r.hasOwnProperty('end')) {");
        htm.addln("        h = h + r.start + \" to \" + r.end + \"<br/>\";");
        htm.addln("        r.blocks.forEach(hoursAppendBlock);");
        htm.addln("      }");
        htm.addln("    }");
        htm.addln("   function hoursAppendBlock(b) {");
        htm.addln("     h = h + \"<table class='indent'>\";");
        htm.addln("     for (i = 0; i < b.timeRanges.length; ++i) {");
        htm.addln("       h = h + \"<tr><td>\";");
        htm.addln("       if (\"-MTWRF-\" == b.weekdays) {");
        htm.addln("         h = h + \"Monday - Friday: \";");
        htm.addln("       } else if (\"-MTWR--\" == b.weekdays) {");
        htm.addln("         h = h + \"Monday - Thursday: \";");
        htm.addln("       } else if (\"-----F-\" == b.weekdays) {");
        htm.addln("         h = h + \"Friday: \";");
        htm.addln("       } else {");
        htm.addln("         h = h + b.weekdays;");
        htm.addln("       }");
        htm.addln("       h = h + \"</td><td> &nbsp; \" + b.timeRanges[i].start + \" - \" ",
                "+ b.timeRanges[i].end + \"</td></tr>\";");
        htm.addln("     }");
        htm.addln("     h = h + \"</table>\";");
        htm.addln("    }");
        htm.addln("  }");

        htm.addln("  document.getElementById(\"hstu_hours\").innerHTML = h;");
        htm.addln(" }");

        htm.addln(" hqSock.onmessage = function(event) {");
        htm.addln("  var obj = JSON.parse(event.data);");
        htm.addln("  if (obj.hasOwnProperty(\"hours\")) {");
        htm.addln("   processHours(obj);");
        htm.addln("  }");
        htm.addln(" }");

        htm.addln("</script>");

        SkinnedPage.endOrdinaryPage(site, htm);
        SkinnedPage.sendReply(req, resp, SkinnedPage.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}

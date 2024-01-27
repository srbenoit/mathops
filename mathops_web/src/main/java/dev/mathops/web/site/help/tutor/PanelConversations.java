package dev.mathops.web.site.help.tutor;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.session.ImmutableSessionInfo;

import jakarta.servlet.ServletRequest;

/**
 * Generates the content of the "Conversations" panel. This panel lists the set of all conversation contexts, along with
 * the number of messages (read and unread) in each.
 */
enum PanelConversations {
    ;

    /** The background color for the panel. */
    // public static final String BACKGROUND = "#efdfd6";
    private static final String BACKGROUND = "#ffede2";

    /**
     * Generates the panel content.
     *
     * @param req     the servlet request, used to get the host information
     * @param session the login session
     * @param htm     the {@code HtmlBuilder} to which to append
     */
    static void emitPanel(final ServletRequest req, final ImmutableSessionInfo session, final HtmlBuilder htm) {

        htm.sDiv("threecolumnpane", "style='background:white; border:1px solid black; background:white;'");

        // Title bar
        htm.sDiv(null,
                "style='background: #D9782D; width:100%; height:26px; font-family:factoria-medium; ",
                "font-size:14px; padding:2px 0 0 6px; color:white; border-bottom:1px solid black; ",
                "text-shadow: 0 0 2px black;'");
        htm.add("<img style='margin-bottom:-6px; padding-right:10px;' ",
                "src='/images/help/conversation.png'/>");
        htm.add("Conversations");
        htm.eDiv();

        htm.sDiv(null, "style='width:100%; background: ", BACKGROUND,
                "; padding:6px; font-size:14px; min-height:calc(100% - 26px); "
                , "overflow-y:scroll;' id='hc_0w459utgj'");

        // BEGIN: content that will get replaced by data from Web Socket

        htm.div("vgap2");

        htm.sDiv("center", "id='throbber'");
        htm.addln("<svg xmlns='http://www.w3.org/2000/svg' ",
                "xmlns:xlink='http://www.w3.org/1999/xlink' ",
                "style='margin:auto;background:", BACKGROUND,
                ";display:block;' width='70px' height='70px' viewBox='0 0 100 100' ",
                "preserveAspectRatio='xMidYMid'>");
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

        // END: content that will get replaced by data from Web Socket

        htm.eDiv();

        emitScripts(req, session, htm);

        htm.eDiv();
    }

    /**
     * Emits the JavaScript code to manage the web socket endpoint.
     *
     * @param req     the servlet request, used to get the host information
     * @param session the login session
     * @param htm     the {@code HtmlBuilder} to which to append
     */
    private static void emitScripts(final ServletRequest req, final ImmutableSessionInfo session,
                                    final HtmlBuilder htm) {

        final String host = req.getServerName();

        htm.addln("<script>");
        htm.addln(" var hcSock = new WebSocket(\"wss://", host, "/ws/helpconversations\");");

        htm.addln(" hcSock.onmessage = function(evt) {");
        htm.addln("   var elem = document.getElementById('hc_0w459utgj');");

        htm.addln("   if (evt.data.startsWith(\"SessionError:\")) {");
        htm.addln("     elem.innerHTML = evt.data.substring(13);");
        htm.addln("   } else {");
        htm.addln("     elem.innerHTML = \"Foo\";");
        htm.addln("   }");
        htm.addln(" };");

        htm.addln(" hcSock.onopen = function(evt) {");
        htm.addln("   hcSock.send('Session:", session.loginSessionId, "');");
        htm.addln(" };");

        htm.addln(" hcSock.onclose = function(evt) {");
        htm.addln(" };");

        htm.addln(" hcSock.onerror = function(err) {");
        htm.addln(" };");

        htm.addln("</script>");
    }
}

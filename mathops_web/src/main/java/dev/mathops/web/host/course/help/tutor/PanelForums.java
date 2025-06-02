package dev.mathops.web.host.course.help.tutor;

import dev.mathops.session.ImmutableSessionInfo;

import dev.mathops.text.builder.HtmlBuilder;
import jakarta.servlet.ServletRequest;

/**
 * Generates the content of the "Forums" panel. This panel lists the configured course forums, along with the number of
 * messages (read and unread) in each.
 */
enum PanelForums {
    ;

    /**
     * Generates the panel content.
     *
     * @param req     the servlet request, used to get the host information
     * @param session the login session
     * @param htm     the {@code HtmlBuilder} to which to append
     */
    static void emitPanel(final ServletRequest req, final ImmutableSessionInfo session,
                          final HtmlBuilder htm) {

        emitWebsocketScript(req, htm, session);

        htm.sDiv("threecolumnpane", "style='background:white; border:1px solid black; background:white;'");

        // Title bar
        htm.sDiv(null,
                "style='background: #59595B; width:100%; height:26px; font-family:factoria-medium; ",
                "font-size:14px; padding:2px 0 0 6px; color:white; border-bottom:1px solid black; ",
                "text-shadow: 0 0 3px black;'");
        htm.add("<img style='margin-bottom:-6px; padding-right:10px;' src='/images/help/forum.png'/>");
        htm.add("Forums");
        htm.eDiv();

        htm.sDiv(null, "style='background:#f2f2f2; padding:2px 6px; height:calc(100% - 26px);' id='hf_oium46yhekc'");
        htm.eDiv();

        htm.eDiv();
    }

    /**
     * Emits scripts needed to manage the web socket.
     *
     * @param req     the request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param session the login session
     */
    private static void emitWebsocketScript(final ServletRequest req, final HtmlBuilder htm,
                                            final ImmutableSessionInfo session) {

        htm.addln("<script>");

        htm.addln(" let socket = new WebSocket(\"wss://", req.getServerName(), "/ws/helpforums\");");

        htm.addln(" socket.onopen = function(e) {");
        htm.addln("   socket.send('Session:", session.loginSessionId, "');");
        htm.addln(" };");

        htm.addln(" socket.onmessage = function(event) {");
        htm.addln("   if (event.data.startsWith('{\"fora\": [')) {");
        htm.addln("     var obj = JSON.parse(event.data);");
        htm.addln("     var h = '';");
        htm.addln("     var len = obj.fora.length;");
        htm.addln("     for (var i = 0; i < len; i++) {");
        htm.addln("       var forum = obj.fora[i];");
        htm.addln("       h += '<details class=\"forum\">';");
        htm.addln("       h += '<summary>' + forum.title + ' (' + forum.totalPosts ");
        htm.addln("          + ' posts, ' + forum.totalUnread + ' unread)</summary>';");
        htm.addln("       h += 'This will be the list of posts...';");
        htm.addln("       h += '</details>';");
        htm.addln("     }");
        htm.addln("     document.getElementById('hf_oium46yhekc').innerHTML = h;");
        htm.addln("   }");
        htm.addln(" };");

        htm.addln(" socket.onclose = function(event) {");
        htm.addln(" };");

        htm.addln(" socket.onerror = function(error) {");
        htm.addln(" };");

        htm.addln("</script>");
    }
}

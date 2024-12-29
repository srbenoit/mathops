package dev.mathops.web.site.help.student;

import dev.mathops.text.builder.HtmlBuilder;

/**
 * Generates the content of the "Chat" panel.
 */
enum PanelChat {
    ;

    /**
     * Generates the panel content.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    static void emitPanel(final HtmlBuilder htm) {

        htm.sDiv(null, "style='background:GhostWhite; border:1px solid black; grid-row-start: span 2;'");

        htm.sDiv(null, "style='background:#C8C372; width:100%; height:20px; border-bottom:1px solid black;'");
        htm.sDiv(null, "style='font-family:factoria-medium; font-size:14px; padding:2px 0 0 6px; color:black;'");
        htm.add("Chat");
        htm.eDiv();
        htm.eDiv();

        // TODO: Conversation content and new message entry box.

        htm.eDiv();
    }
}

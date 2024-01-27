package dev.mathops.web.site.help.tutor;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * Generates the content of the "Chat" panel.
 */
enum PanelMediaChat {
    ;

    /**
     * Generates the panel content.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param enabled true to enable panel; false while in "preview" mode
     */
    static void emitPanel(final HtmlBuilder htm, final boolean enabled) {

        htm.sDiv(null, "style='background:GhostWhite; border:1px solid black; grid-area: media;'");

        htm.sDiv(null, "style='background: #1e4d2b; width:100%; height:21px;",
                "font-family:factoria-medium; font-size:15px; padding:2px 0 0 6px; color:white;'");
        htm.add("Video, Audio, and Chat");
        htm.eDiv();

        if (enabled) {
            // Video feeds: [ Instructor live webcam ] [ Student feed if sent ]

            htm.sDiv("videobox");
            htm.addln("<video autoplay='true' id='instructorcam' ",
                    "style='background-color: #333;width:100%;height:100%;'></video>");
            htm.eDiv();

            htm.sDiv("videobox");
            htm.addln("<video autoplay='true' id='studentcam' ",
                    "style='background-color: #333;width:100%;height:100%;'></video>");
            htm.eDiv();

            // Video controls
            htm.sDiv("controlsbox");
            htm.addln("<button>Stop</button>");
            htm.addln("<button>Mute</button>");
            htm.eDiv();
            htm.sDiv("controlsbox");
            htm.addln("<button>Stop</button>");
            htm.addln("<button>Mute</button>");
            htm.eDiv();

            htm.addln("<script>");
            htm.addln("var video = document.querySelector('#instructorcam');");
            htm.addln("if (navigator.mediaDevices.getUserMedia) {");
            htm.addln("  navigator.mediaDevices.getUserMedia({ video: true })");
            htm.addln("    .then(function (stream) {");
            htm.addln("      video.srcObject = stream;");
            htm.addln("    })");
            htm.addln("    .catch(function (err0r) {");
            htm.addln("      console.log(\"Something went wrong!\");");
            htm.addln("    });");
            htm.addln("}");
            htm.addln("</script>");

            // Chat portion of the window
            htm.sDiv(null,
                    "style='background: #CEC; width:100%; height:18px; font-family:factoria-medium; ",
                    "font-size:12px; padding:3px 0 0 6px; color:black;'");
            htm.add("Chat");
            htm.eDiv();
        }

        // TODO: Content:

        htm.eDiv();
    }
}

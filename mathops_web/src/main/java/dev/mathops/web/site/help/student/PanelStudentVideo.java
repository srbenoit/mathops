package dev.mathops.web.site.help.student;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * Generates the content of the "Student Video" panel.
 */
enum PanelStudentVideo {
    ;

    /**
     * Generates the panel content.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    static void emitPanel(final HtmlBuilder htm) {

        htm.sDiv(null, "style='background:GhostWhite; border:1px solid black;'");

        htm.sDiv(null, "style='background:#C8C372; width:100%; height:20px; border-bottom:1px solid black;'");
        htm.sDiv(null, "style='font-family:factoria-medium; font-size:14px; padding:2px 0 0 6px; color:black;'");
        htm.add("My Video");
        htm.eDiv();
        htm.eDiv();

        htm.sDiv("videobox");
        htm.addln("<video autoplay='true' id='studentcam' ",
                "style='background-color: #333; width:100%; height:100%;'></video>");
        htm.eDiv();

        // Video controls
        htm.sDiv("controlsbox");
        htm.addln("<button>Stop</button>");
        htm.addln("<button>Mute</button>");
        htm.eDiv();

        htm.addln("<script>");
        htm.addln("var video = document.querySelector('#studentcam');");
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

        htm.eDiv();
    }
}

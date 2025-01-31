package dev.mathops.web.site.placement.main;

import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * A base class for Math Placement pages with shared methods and constants.
 */
enum MPPage {
    ;

    /** Site email address. */
    static final String EMAIL = "precalc_math@colostate.edu";

    /**
     * Emits the page header.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    static void emitMathPlanHeader(final HtmlBuilder htm) {

        htm.sDiv("center");
        htm.sH(1, "shaded")
                .add("Create <span class='hideabove600'>Your</span><span class='hideabove400'><br></span>",
                        "<span class='hidebelow600'>a Personalized</span> Math Plan")
                .eH(1);
        htm.eDiv(); // center

        htm.div("vgap");
    }

    /**
     * Emits an error message indicating student record was not found.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    static void emitNoStudentDataError(final HtmlBuilder htm) {

        htm.sDiv("inset2");
        htm.sDiv("shaded2left");

        htm.sP().add(Res.get(Res.ERR_NO_STUDENT)).eP();

        htm.sP().add(Res.get(Res.ERR_SEND_EMAIL_PRE), " <a class='underline' href='mailto:", EMAIL,
                "'>", EMAIL, "</a> ", Res.get(Res.ERR_SEND_EMAIL_POST)).eP();

        htm.eDiv(); // shaded2left
        htm.eDiv(); // inset2
    }

    /**
     * Emits JavaScript scripts.
     *
     * @param htm        the {@code HtmlBuilder} to which to append
     * @param extraLines extra script lines to add to the startup code
     */
    public static void emitScripts(final HtmlBuilder htm, final String... extraLines) {

        htm.addln("<script>");
        htm.addln("fixBackgroundSizeCover = function(event) {");
        htm.addln("  var bgImageWidth = 1920,");
        htm.addln("    bgImageHeight = 2103,");
        htm.addln("    bgImageRatio = bgImageWidth / bgImageHeight,");
        htm.addln("    windowSizeRatio = window.innerWidth / window.innerHeight;");
        htm.addln("  if (bgImageRatio > windowSizeRatio) {");
        htm.addln("    document.body.style.backgroundSize = 'auto 100vh';");
        htm.addln("  } else {");
        htm.addln("    document.body.style.backgroundSize = '100vw auto';");
        htm.addln("  }");
        htm.addln("};");

        htm.addln("fixBackgroundSizeCover();");
        htm.addln("window.addEventListener('resize', fixBackgroundSizeCover);");

        if (extraLines != null) {
            for (final String line : extraLines) {
                htm.addln(line);
            }
        }

        htm.addln("</script>");
    }

    /**
     * Creates an {@code HtmlBuilder} and emits the start of a "type 3" page, which shows a "Welcome to Math Placement"
     * header, and the "Logged in as ___" bar with a button to return to the home (pre-login) page.
     *
     * @param site    the owning site
     * @param session the user's login session information
     * @return the constructed {@code HtmlBuilder}
     */
    static HtmlBuilder startPage1(final MathPlacementSite site, final ImmutableSessionInfo session) {

        final HtmlBuilder htm = new HtmlBuilder(8192);
        Page.startNofooterPage(htm, site.getTitle(), session, true, Page.ADMIN_BAR, null, false, false);

        htm.sDiv("center");
        htm.sH(1, "shaded").add("Welcome to Math Placement").eH(1);
        htm.eDiv(); // center
        htm.div("vgap");

        MathPlacementSite.emitLoggedInAs1(htm, session);

        return htm;
    }

    /**
     * Creates an {@code HtmlBuilder} and emits the start of a "type 3" page, which shows a "Welcome to Math Placement"
     * header, and the "Logged in as ___" bar with a button to return to the secure landing page.
     *
     * @param site    the owning site
     * @param session the user's login session information
     * @return the constructed {@code HtmlBuilder}
     */
    static HtmlBuilder startReviewPage2(final MathPlacementSite site, final ImmutableSessionInfo session) {

        final HtmlBuilder htm = new HtmlBuilder(8192);
        Page.startNofooterPage(htm, site.getTitle(), session, true, Page.NO_BARS, null, false, false);

        htm.sDiv("center");
        htm.sH(1, "shaded").add("Welcome to Math Placement Review").eH(1);
        htm.eDiv(); // center
        htm.div("vgap");

        MathPlacementSite.emitLoggedInAs2(htm, session);

        return htm;
    }

    /**
     * Creates an {@code HtmlBuilder} and emits the start of a "type 3" page, which shows a "Welcome to Math Placement"
     * header, and the "Logged in as ___" bar with a button to return to the secure landing page.
     *
     * @param site    the owning site
     * @param session the user's login session information
     * @return the constructed {@code HtmlBuilder}
     */
    static HtmlBuilder startPage2(final MathPlacementSite site, final ImmutableSessionInfo session) {

        final HtmlBuilder htm = new HtmlBuilder(8192);
        Page.startNofooterPage(htm, site.getTitle(), session, true, Page.NO_BARS, null, false, false);

        htm.sDiv("center");
        htm.sH(1, "shaded").add("Welcome to Math Placement").eH(1);
        htm.eDiv(); // center
        htm.div("vgap");

        MathPlacementSite.emitLoggedInAs2(htm, session);

        return htm;
    }

    /**
     * Creates an {@code HtmlBuilder} and emits the start of a "type 3" page, which shows a "Welcome to Math Placement"
     * header, and the "Logged in as ___" bar with a button to return to the Math Placement Tool.
     *
     * @param site    the owning site
     * @param session the user's login session information
     * @return the constructed {@code HtmlBuilder}
     */
    public static HtmlBuilder startPage3(final MathPlacementSite site, final ImmutableSessionInfo session) {

        final HtmlBuilder htm = new HtmlBuilder(8192);
        Page.startNofooterPage(htm, site.getTitle(), session, true, Page.NO_BARS, null, false, false);

        htm.sDiv("center");
        htm.sH(1, "shaded").add("Welcome to Math Placement").eH(1);
        htm.eDiv(); // center
        htm.div("vgap");

        MathPlacementSite.emitLoggedInAs3(htm, session);

        return htm;
    }

    /**
     * Ends the page and sends it to the client.
     *
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    public static void endPage(final HtmlBuilder htm, final ServletRequest req,
                               final HttpServletResponse resp) throws IOException {

        Page.endNoFooterPage(htm, false);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}

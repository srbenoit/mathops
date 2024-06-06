package dev.mathops.web.site.tutorial.elm;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.logic.ELMTutorialStatus;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.session.ImmutableSessionInfo;

import java.sql.SQLException;

/**
 * A menu that lists the courses a user is registered for, along with links to update the user's profile, manage
 * e-texts, and view a recommended progress schedule.
 */
enum TutorialMenu {
    ;

    /**
     * Builds the menu based on the current logged-in user session and appends its HTML representation to an
     * {@code HtmlBuilder}.
     *
     * @param cache   the data cache
     * @param session the session
     * @param status  the student status with respect to the ELM Tutorial
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    public static void buildMenu(final Cache cache, final ImmutableSessionInfo session,
                                 final ELMTutorialStatus status, final HtmlBuilder htm) throws SQLException {

        htm.addln("<nav class='menu'>");
        htm.sDiv("menubox");

        buildMenuContent(cache, session, status, htm);

        htm.eDiv(); // menubox
        htm.addln("</nav>");
    }

    /**
     * Builds the menu once available courses have been determined.
     *
     * @param cache   the data cache
     * @param session the session
     * @param status  the student status with respect to the ELM Tutorial
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void buildMenuContent(final Cache cache, final ImmutableSessionInfo session,
                                         final ELMTutorialStatus status, final HtmlBuilder htm) throws SQLException {

        boolean isTutor = "AACTUTOR".equals(session.getEffectiveUserId()) || session.role.canActAs(ERole.TUTOR);

        if (!isTutor) {
            isTutor = RawSpecialStusLogic.isSpecialType(cache, session.getEffectiveUserId(),
                    session.getNow().toLocalDate(), "TUTOR");
        }

        htm.sDiv().add("<a class='ulink' href='home.html'>Home</a>").eDiv();
        htm.div("vgap0");

        htm.addln("<a class='ulink' target='_blank' ",
                "href='/www/media/ELM_information.pdf'>",
                "ELM Tutorial Instructions", //
                "</a>");
        htm.div("vgap0");

        htm.addln("<a class='ulink' href='onlinehelp.html'>",
                "<b>Getting Help</b>", //
                "</a>");

        if (!isTutor) {
            htm.div("vgap");
            htm.sH(1, "menu").add("Placement Information").eH(1);

            htm.addln("<a class='ulink' href='placement_report.html'>",
                    "My Placement Results</a>").br();
        }

        htm.div("vgap");
        htm.sH(1, "menu").add("ELM Tutorial Content").eH(1);

        final boolean lock =
                status.webSiteAvailability.current == null || !status.eligibleForElmTutorial;

        htm.sDiv("center");
        if (lock && !isTutor) {
            htm.addln("<strong class='menu2 gray'>Tutorial not available</strong>");
        } else {
            htm.addln("<a class='menubtn' href='tutorial.html'>",
                    "Go to Tutorial Content</a>");
            htm.eDiv();

            htm.sDiv("center");
            htm.addln("<a class='menubtn' href='tutorial_status.html'>",
                    "View my Current Status</a>");
        }
        htm.eDiv();

        htm.div("vgap");
    }
}

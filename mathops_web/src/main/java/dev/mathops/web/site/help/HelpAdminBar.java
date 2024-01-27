package dev.mathops.web.site.help;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.web.site.ISkinnedAdminBar;

import jakarta.servlet.ServletRequest;

/**
 * An admin bar that shows the logged-in user's name, and additional controls if the user is an administrator or tutor.
 */
public final class HelpAdminBar implements ISkinnedAdminBar {

    /** The bar height, in CSS pixels. */
    private static final int HEIGHT = 26;

    /** The single instance. */
    public static final ISkinnedAdminBar INSTANCE = new HelpAdminBar();

    /**
     * Private constructor to prevent instantiation.
     */
    private HelpAdminBar() {

        // No action
    }

    /**
     * Gets the height of the admin bar, in CSS pixels.
     *
     * @param session the session
     * @return the height
     */
    @Override
    public int getAdminBarHeight(final ImmutableSessionInfo session) {

        final ERole role = session.role;

        return role.canActAs(ERole.ADMINISTRATOR) || role.canActAs(ERole.TUTOR) ? HEIGHT : 0;
    }

    /**
     * Emits the HTML for the admin bar.
     *
     * @param site    the owning site
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param session the session
     */
    @Override
    public void emitAdminBar(final HelpSite site, final HtmlBuilder htm, final ImmutableSessionInfo session) {

        final ERole role = session.role;

        if (role.canActAs(ERole.ADMINISTRATOR) || role.canActAs(ERole.TUTOR)) {

            htm.sDiv(null, "style='height:" + HEIGHT
                    + "px; background:#C8C372; color:black;border-bottom:1px solid black;"
                    + "font-family:factoria-medium; font-size:14px; "
                    + "padding:6px 6px 0 6px;'");

            final ERole effective = session.getEffectiveRole();

            htm.sDiv("right", "style='position:relative;top:-3px;'");
            htm.add("<form style='display:inline;' action='helpbar.html' ",
                    "method='get'>");
            htm.add("<label for='selrole'>Role: </label>");
            htm.add("<select style='position:relative;top:-1px;' ",
                    "name='selrole' id='selrole'>");

            if (role.canActAs(ERole.ADMINISTRATOR)) {

                // Administrator can choose TUTOR or STUDENT role as well

                if (effective.canActAs(ERole.ADMINISTRATOR)) {
                    htm.add("<option selected value='", ERole.ADMINISTRATOR.abbrev,
                            "'>Administrator</option>");
                    htm.add("<option value='", ERole.TUTOR.abbrev,
                            "'>Tutor</option>");
                    htm.add("<option value='", ERole.STUDENT.abbrev,
                            "'>Student</option>");
                } else {
                    htm.add("<option value='", ERole.ADMINISTRATOR.abbrev,
                            "'>Administrator</option>");

                    if (effective.canActAs(ERole.TUTOR)) {
                        htm.add("<option selected value='", ERole.TUTOR.abbrev,
                                "'>Tutor</option>");
                        htm.add("<option value='", ERole.STUDENT.abbrev,
                                "'>Student</option>");
                    } else {
                        htm.add("<option value='", ERole.TUTOR.abbrev,
                                "'>Tutor</option>");
                        htm.add("<option selected  value='", ERole.STUDENT.abbrev,
                                "'>Student</option>");
                    }
                }

            } else if (role.canActAs(ERole.TUTOR)) {

                // Tutor can choose STUDENT role as well

                if (effective.canActAs(ERole.TUTOR)) {
                    htm.add("<option selected value='", ERole.TUTOR.abbrev,
                            "'>Tutor</option>");
                    htm.add("<option value='", ERole.STUDENT.abbrev,
                            "'>Student</option>");
                } else {
                    htm.add("<option value='", ERole.TUTOR.abbrev,
                            "'>Tutor</option>");
                    htm.add("<option selected  value='", ERole.STUDENT.abbrev,
                            "'>Student</option>");
                }
            }

            htm.add("</select> ");
            htm.add("<input style='position:relative;top:-1px;' ",
                    "type='submit' value='Go'/>");
            htm.add("</form>");
            htm.eDiv();

            htm.add(Res.fmt(Res.LOGGED_IN_AS, session.getEffectiveScreenName()));

            htm.eDiv();
        }
    }

    /**
     * Processes a POST from the help admin bar, which can update the effective role of a login session.
     *
     * @param req     the request
     * @param session the session (immutable, so, the session is changed in the session manager, a new immutable session
     *                info is generated and returned)
     * @return the new session, with role updated as needed
     */
    static ImmutableSessionInfo processPost(final ServletRequest req,
                                            final ImmutableSessionInfo session) {

        ImmutableSessionInfo newSession = session;

        final String prm = req.getParameter("selrole");
        final ERole role = session.role;

        if (ERole.STUDENT.abbrev.equals(prm)) {
            if (role.canActAs(ERole.STUDENT)) {
                if (role != ERole.STUDENT) {
                    newSession = SessionManager.getInstance()
                            .setEffectiveRole(session.loginSessionId, ERole.STUDENT).session;
                }
            } else {
                Log.warning("Attempt to set role to STUDENT by user ",
                        session.userId, " who is not authorized to act as STUDENT");
            }
        } else if (ERole.TUTOR.abbrev.equals(prm)) {
            if (role.canActAs(ERole.TUTOR)) {
                if (role != ERole.TUTOR) {
                    newSession = SessionManager.getInstance()
                            .setEffectiveRole(session.loginSessionId, ERole.TUTOR).session;
                }
            } else {
                Log.warning("Attempt to set role to TUTOR by user ",
                        session.userId, " who is not authorized to act as TUTOR");
            }
        } else if (ERole.ADMINISTRATOR.abbrev.equals(prm)) {
            if (role.canActAs(ERole.ADMINISTRATOR)) {
                if (role != ERole.ADMINISTRATOR) {
                    newSession = SessionManager.getInstance()
                            .setEffectiveRole(session.loginSessionId, ERole.ADMINISTRATOR).session;
                }
            } else {
                Log.warning("Attempt to set role to ADMINISTRATOR by user ",
                        session.userId, " who is not authorized to act as ADMINISTRATOR");
            }
        }

        return newSession;
    }
}

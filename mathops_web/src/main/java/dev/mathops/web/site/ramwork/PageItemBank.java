package dev.mathops.web.site.ramwork;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.proctoring.student.ProctoringSite;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates the item bank page.
 */
enum PageItemBank {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void showPage(final Cache cache, final RamWorkSite site, final HttpServletRequest req,
                         final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final ERole role = session.getEffectiveRole();

        if (role.canActAs(ERole.ADMINISTRATOR)) {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.ADMIN_BAR, null, false, true);

            htm.div("vgap");
            htm.sH(1).add("Item Bank").eH(1);

            appendItemBank(htm, site, req, resp, session);

            Page.endOrdinaryPage(cache, site, htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Appends the item bank directory.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     */
    private static void appendItemBank(final HtmlBuilder htm, final RamWorkSite site, final HttpServletRequest req,
                                       final HttpServletResponse resp, final ImmutableSessionInfo session) {

        // Show the item bank and allow the user to click on an item to load it and test

        // =======================================================================================

        htm.hr();
        htm.sH(1, "items").add("Math Placement Review Items").eH(1);
        htm.div("vgap");

        htm.addln("<details><summary class='items'>Algebra Section (132 items)</summary>");

        String pre = "math.m100r.problems.algebra";

        htm.sP("items").add("<strong>Question 1:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a1.A1a'>[A1a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a1.A1b'>[A1b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a1.A1c'>[A1c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a1.A1d'>[A1d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a1.A1e'>[A1e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a1.A1f'>[A1f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a1.A1g'>[A1g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a1.A1h'>[A1h]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 2:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a2.A2a'>[A2a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a2.A2b'>[A2b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a2.A2e'>[A2e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a2.A2f'>[A2f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a2.A2g'>[A2g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a2.A2h'>[A2h]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a2.A2i'>[A2i]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a2.A2j'>[A2j]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 3:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a3.A3a'>[A3a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a3.A3b'>[A3b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a3.A3d'>[A3d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a3.A3e'>[A3e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a3.A3f'>[A3f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a3.A3g'>[A3g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a3.A3h'>[A3h]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a3.A3i'>[A3i]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a3.A3j'>[A3j]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a3.A3k'>[A3k]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a3.A3l'>[A3l]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a3.A3m'>[A3m]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a3.A3n'>[A3n]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a3.A3o'>[A3o]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 4:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a4.A4a'>[A4a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a4.A4b'>[A4b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a4.A4c'>[A4c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a4.A4d'>[A4d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a4.A4e'>[A4e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a4.A4f'>[A4f]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 5:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a5.A5b'>[A5b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a5.A5c'>[A5c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a5.A5d'>[A5d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a5.A5e'>[A5e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a5.A5f'>[A5f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a5.A5g'>[A5g]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 6:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a6.A6a'>[A6a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a6.A6b'>[A6b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a6.A6c'>[A6c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a6.A6d'>[A6d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a6.A6e'>[A6e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a6.A6f'>[A6f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a6.A6g'>[A6g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a6.A6h'>[A6h]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 7:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a7.A7a'>[A7a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a7.A7b'>[A7b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a7.A7c'>[A7c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a7.A7d'>[A7d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a7.A7e'>[A7e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a7.A7f'>[A7f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a7.A7g'>[A7g]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 8:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a8.A8a'>[A8a]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 9:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a9.A9a'>[A9a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a9.A9b'>[A9b]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 10:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a10.A10a'>[A10a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a10.A10b'>[A10b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a10.A10c'>[A10c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a10.A10d'>[A10d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a10.A10e'>[A10e]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 11:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a11.A11d'>[A11d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a11.A11e'>[A11e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a11.A11f'>[A11f]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 12:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a12.A12a'>[A12a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a12.A12b'>[A12b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a12.A12c'>[A12c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a12.A12d'>[A12d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a12.A12e'>[A12e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a12.A12f'>[A12f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a12.A12g'>[A12g]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 13:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a13.A13b'>[A13b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a13.A13c'>[A13c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a13.A13d'>[A13d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a13.A13e'>[A13e]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 14:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a14.A14a'>[A14a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a14.A14b'>[A14b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a14.A14c'>[A14c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a14.A14d'>[A14d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a14.A14e'>[A14e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a14.A14f'>[A14f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a14.A14g'>[A14g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a14.A14h'>[A14h]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a14.A14i'>[A14i]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 15:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a15.A15a'>[A15a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a15.A15b'>[A15b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a15.A15d'>[A15d]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 16:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a16.A16a'>[A16a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a16.A16b'>[A16b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a16.A16c'>[A16c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a16.A16d'>[A16d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a16.A16e'>[A16e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a16.A16f'>[A16f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a16.A16g'>[A16g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a16.A16h'>[A16h]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a16.A16i'>[A16i]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 17:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a17.A17a'>[A17a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a17.A17b'>[A17b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a17.A17c'>[A17c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a17.A17d'>[A17d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a17.A17e'>[A17e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a17.A17f'>[A17f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a17.A17g'>[A17g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a17.A17h'>[A17h]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a17.A17i'>[A17i]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a17.A17j'>[A17j]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 18:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a18.A18b'>[A18b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a18.A18c'>[A18c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a18.A18d'>[A18d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a18.A18e'>[A18e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a18.A18g'>[A18g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a18.A18h'>[A18h]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 19:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a19.A19a'>[A19a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a19.A19b'>[A19b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a19.A19c'>[A19c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a19.A19d'>[A19d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a19.A19e'>[A19e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a19.A19f'>[A19f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a19.A19g'>[A19g]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 20:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a20.A20a'>[A20a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a20.A20b'>[A20b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a20.A20c'>[A20c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a20.A20d'>[A20d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a20.A20e'>[A20e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a20.A20e2'>[A20e2]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a20.A20f'>[A20f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a20.A20g'>[A20g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".a20.A20h'>[A20h]</a> &nbsp;");
        htm.eP();

        htm.addln("</details>");

        // ---------------------------------------------------------------------------------------

        htm.div("vgap");

        htm.addln("<details><summary class='items'>",
                "Functions/Graphs Section (91 items)",
                "</summary>");

        pre = "math.m100r.problems.functions_graphs";

        htm.sP("items").add("<strong>Question 1:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f1.F1a'>[F1a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f1.F1b'>[F1b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f1.F1c'>[F1c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f1.F1d'>[F1d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f1.F1e'>[F1e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f1.F1f'>[F1f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f1.F1g'>[F1g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f1.F1h'>[F1h]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f1.F1i'>[F1i]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f1.F1j'>[F1j]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f1.F1k'>[F1k]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f1.F1l'>[F1l]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f1.F1m'>[F1m]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f1.F1n'>[F1n]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f1.F1o'>[F1o]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f1.F1p'>[F1p]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 2:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f2.F2a'>[F2a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f2.F2b'>[F2b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f2.F2c'>[F2c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f2.F2d'>[F2d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f2.F2e'>[F2e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f2.F2f'>[F2f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f2.F2g'>[F2g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f2.F2h'>[F2h]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 3:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f3.F3a'>[F3a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f3.F3b'>[F3b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f3.F3c'>[F3c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f3.F3d'>[F3d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f3.F3e'>[F3e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f3.F3f'>[F3f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f3.F3g'>[F3g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f3.F3h'>[F3h]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 4:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f4.F4a'>[F4a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f4.F4b'>[F4b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f4.F4c'>[F4c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f4.F4d'>[F4d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f4.F4e'>[F4e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f4.F4f'>[F4f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f4.F4g'>[F4g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f4.F4h'>[F4h]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f4.F4i'>[F4i]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f4.F4j'>[F4j]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f4.F4k'>[F4k]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f4.F4l'>[F4l]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f4.F4m'>[F4m]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f4.F4n'>[F4n]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f4.F4o'>[F4o]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f4.F4p'>[F4p]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f4.F4q'>[F4q]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 5:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5a'>[F5a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5b'>[F5b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5c'>[F5c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5d'>[F5d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5e'>[F5e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5f'>[F5f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5g'>[F5g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5h'>[F5h]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5i'>[F5i]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5j'>[F5j]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5k'>[F5k]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5l'>[F5l]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5m'>[F5m]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5n'>[F5n]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5o'>[F5o]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5p'>[F5p]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5q'>[F5q]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5r'>[F5r]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5s'>[F5s]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5t'>[F5t]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5u'>[F5u]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5v'>[F5v]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5w'>[F5w]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f5.F5x'>[F5x]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 6:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f6.F6a'>[F6a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f6.F6b'>[F6b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f6.F6c'>[F6c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f6.F6d'>[F6d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f6.F6e'>[F6e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f6.F6f'>[F6f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f6.F6g'>[F6g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f6.F6h'>[F6h]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f6.F6i'>[F6i]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f6.F6j'>[F6j]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f6.F6k'>[F6k]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 7:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f7.F7a'>[F7a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f7.F7b'>[F7b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f7.F7c'>[F7c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f7.F7d'>[F7d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f7.F7e'>[F7e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f7.F7f'>[F7f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f7.F7g'>[F7g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".f7.F7h'>[F7h]</a> &nbsp;");

        htm.addln("</details>");

        // TODO:
        htm.sDiv("landmark");
        htm.sP("landmark").add("Items above this line have been reviewed").eP();
        htm.eDiv();
        // TODO:

        // ---------------------------------------------------------------------------------------

        htm.div("vgap");

        htm.addln("<details open><summary class='items'>",
                "Trigonometry Section (197 items)",
                "</summary>");

        pre = "math.m100r.problems.trigonometry";

        htm.sP("items").add("<strong>Question 1:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t1.T1a'>[T1a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t1.T1b'>[T1b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t1.T1c'>[T1c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t1.T1d'>[T1d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t1.T1e'>[T1e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t1.T1f'>[T1f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t1.T1g'>[T1g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t1.T1h'>[T1h]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t1.T1i'>[T1i]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t1.T1j'>[T1j]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 2:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t2.T2a'>[T2a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t2.T2b'>[T2b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t2.T2c'>[T2c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t2.T2d'>[T2d]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 3:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t3.T3a'>[T3a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t3.T3b'>[T3b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t3.T3c'>[T3c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t3.T3d'>[T3d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t3.T3e'>[T3e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t3.T3f'>[T3f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t3.T3g'>[T3g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t3.T3h'>[T3h]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t3.T3i'>[T3i]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t3.T3j'>[T3j]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t3.T3k'>[T3k]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 4:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t4.T4e'>[T4e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t4.T4f'>[T4f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t4.T4g'>[T4g]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 5:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t5.T5a'>[T5a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t5.T5b'>[T5b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t5.T5c'>[T5c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t5.T5d'>[T5d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t5.T5e'>[T5e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t5.T5f'>[T5f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t5.T5g'>[T5g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t5.T5h'>[T5h]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t5.T5i'>[T5i]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t5.T5j'>[T5j]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t5.T5k'>[T5k]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t5.T5l'>[T5l]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 6:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t6.T6a'>[T6a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t6.T6b'>[T6b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t6.T6c'>[T6c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t6.T6d'>[T6d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t6.T6e'>[T6e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t6.T6f'>[T6f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t6.T6g'>[T6g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t6.T6h'>[T6h]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 7:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t7.T7b'>[T7b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t7.T7c'>[T7c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t7.T7d'>[T7d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t7.T7e'>[T7e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t7.T7f'>[T7f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t7.T7g'>[T7g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t7.T7h'>[T7h]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t7.T7i'>[T7i]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t7.T7j'>[T7j]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t7.T7k'>[T7k]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t7.T7l'>[T7l]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t7.T7m'>[T7m]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t7.T7n'>[T7n]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 8:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t8.T8a'>[T8a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t8.T8b'>[T8b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t8.T8c'>[T8c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t8.T8d'>[T8d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t8.T8e'>[T8e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t8.T8f'>[T8f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t8.T8g'>[T8g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t8.T8h'>[T8h]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t8.T8i'>[T8i]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t8.T8j'>[T8j]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t8.T8k'>[T8k]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t8.T8l'>[T8l]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t8.T8m'>[T8m]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t8.T8n'>[T8n]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 9:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9ab'>[T9ab]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9ac'>[T9ac]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9a'>[T9a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9c'>[T9c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9d'>[T9d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9e'>[T9e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9f'>[T9f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9g'>[T9g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9h'>[T9h]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9i'>[T9i]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9j'>[T9j]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9k'>[T9k]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9l'>[T9l]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9m'>[T9m]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9n'>[T9n]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9o'>[T9o]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9p'>[T9p]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9q'>[T9q]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9r'>[T9r]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9s'>[T9s]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9t'>[T9t]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9u'>[T9u]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9v'>[T9v]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9w'>[T9w]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9x'>[T9x]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t9.T9y'>[T9y]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 10:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t10.T10a'>[T10a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t10.T10b'>[T10b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t10.T10c'>[T10c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t10.T10d'>[T10d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t10.T10g'>[T10g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t10.T10h'>[T10h]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t10.T10p'>[T10p]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t10.T10s'>[T10s]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t10.T10u'>[T10u]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t10.T10v'>[T10v]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t10.T10w'>[T10w]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 11:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t11.T11a'>[T11a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t11.T11b'>[T11b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t11.T11c'>[T11c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t11.T11d'>[T11d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t11.T11e'>[T11e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t11.T11f'>[T11f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t11.T11g'>[T11g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t11.T11h'>[T11h]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t11.T11i'>[T11i]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t11.T11j'>[T11j]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t11.T11k'>[T11k]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t11.T11l'>[T11l]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t11.T11m'>[T11m]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t11.T11n'>[T11n]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t11.T11o'>[T11o]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 12:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12a'>[T12a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12b'>[T12b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12c'>[T12c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12d'>[T12d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12e'>[T12e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12f'>[T12f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12i'>[T12i]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12j'>[T12j]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12k'>[T12k]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12l'>[T12l]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12m'>[T12m]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12n'>[T12n]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12o'>[T12o]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12p'>[T12p]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12q'>[T12q]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12r'>[T12r]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12s'>[T12s]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12t'>[T12t]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12u'>[T12u]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12v'>[T12v]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12w'>[T12w]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12x'>[T12x]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12y'>[T12y]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T12z'>[T12z]</a> &nbsp;");
        htm.eP();

        htm.sP("items").add("<strong>Question 13:<strong> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13aa'>[T13aa]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13ab'>[T13ab]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13ac'>[T13ac]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13ad'>[T13ad]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13ae'>[T13ae]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13af'>[T13af]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13ag'>[T13ag]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13ah'>[T13ah]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13ai'>[T13ai]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13aj'>[T13aj]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13ak'>[T13ak]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13al'>[T13al]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13am'>[T13am]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13an'>[T13an]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13ao'>[T13ao]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13ap'>[T13ap]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13aq'>[T13aq]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13ar'>[T13ar]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13as'>[T13as]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13at'>[T13at]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t13.T13a'>[T13a]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13b'>[T13b]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13c'>[T13c]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13d'>[T13d]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13e'>[T13e]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13f'>[T13f]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13g'>[T13g]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13h'>[T13h]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13i'>[T13i]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13j'>[T13j]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13k'>[T13k]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13l'>[T13l]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13m'>[T13m]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13n'>[T13n]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13o'>[T13o]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13p'>[T13p]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13q'>[T13q]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13r'>[T13r]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13s'>[T13s]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13t'>[T13t]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13u'>[T13u]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13v'>[T13v]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13w'>[T13w]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13x'>[T13x]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13y'>[T13y]</a> &nbsp;");
        htm.add("<a href='item.html?id=", pre, ".t12.T13z'>[T13z]</a> &nbsp;");
        htm.eP();

        htm.addln("</details>");

        // =======================================================================================
        htm.hr();
        htm.sH(1, "items").add("Math Placement Items").eH(1);

        // =======================================================================================
        htm.hr();
        htm.sH(1, "items").add("ELM Tutorial Items").eH(1);

        // =======================================================================================
        htm.hr();
        htm.sH(1, "items").add("MATH 117 Items").eH(1);

        // =======================================================================================
        htm.hr();
        htm.sH(1, "items").add("MATH 118 Items").eH(1);

        // =======================================================================================
        htm.hr();
        htm.sH(1, "items").add("MATH 124 Items").eH(1);

        // =======================================================================================
        htm.hr();
        htm.sH(1, "items").add("MATH 125 Items").eH(1);

        // =======================================================================================
        htm.hr();
        htm.sH(1, "items").add("MATH 126 Items").eH(1);

        // =======================================================================================
        htm.hr();
        htm.sH(1, "items").add("User's Exam Items").eH(1);

    }

    /**
     * Appends page content appropriate to staff members to the page. A staff member is typically departmental staff,
     * answering questions from students about how to access the site, or what they are seeing, when something is due.
     * Staff members should not make decisions about extensions or update grades - that should be delegated to
     * instructors. Staff members can also help students update their profile with accessibility profile information.
     *
     * <p>
     * A reader or data entry person who helps a student with a severe disability access course content would also be
     * considered staff, so they should be able to access course materials as a student.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     */
    private static void appendStaffContent(final HtmlBuilder htm, final ProctoringSite site,
                                           final HttpServletRequest req, final HttpServletResponse resp,
                                           final ImmutableSessionInfo session) {

        // Staff view:
        // 1. Create and edit items in the item bank (and links to course content for each)
        // 2. Create and manage assignments and exams (selecting from item bank)
        // 3. Access a course as staff member
        // 4. Access a course as student, manage profile and access assignments
        // 5. View statistics and reports
    }

    /**
     * Appends page content appropriate to course instructors to the page.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     */
    private static void appendInstructorContent(final HtmlBuilder htm, final ProctoringSite site,
                                                final HttpServletRequest req, final HttpServletResponse resp,
                                                final ImmutableSessionInfo session) {

        // Instructor view:
        // 1. Manage roster, TAs, graders
        // 2. Manage assignments, individual student settings and due-dates
        // 3. Grade-book for course
        // 4. Hand-grading needed
        // 5. Statistics summary
        // 6. Act as student
        // 7. Access and moderate forums and help hours
        // 8. Manage availability of calculator or graphing tools per assignment or student
    }

    /**
     * Appends page content appropriate to student advisers to the page. An adviser can look at student progress to see
     * if they are turning in assignments or getting behind.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     */
    private static void appendAdviserContent(final HtmlBuilder htm, final ProctoringSite site,
                                             final HttpServletRequest req, final HttpServletResponse resp,
                                             final ImmutableSessionInfo session) {

        // Adviser view:
        // Student reports for all students assigned to the adviser
    }

    /**
     * Appends page content appropriate to an authorized tutor. A tutor can access all assignments and exams in
     * "solutions mode" and all course instructional materials, but cannot submit assignments for credit or score. They
     * can generate assignments in practice mode.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     */
    private static void appendTutorContent(final HtmlBuilder htm, final ProctoringSite site,
                                           final HttpServletRequest req, final HttpServletResponse resp,
                                           final ImmutableSessionInfo session) {

        // Tutor view:
        // 1. My profile and preferences
        // 2. List of courses for which I'm authorized to act as a tutor
        // 3. Access forums and help hours
        // 4. Assignment in practice mode: navigate or print, get help, link to course content
        // 5. Online calculator or graphing tools (available while proctored)
    }

    /**
     * Appends page content appropriate to a student.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     */
    private static void appendStudentContent(final HtmlBuilder htm, final ProctoringSite site,
                                             final HttpServletRequest req, final HttpServletResponse resp,
                                             final ImmutableSessionInfo session) {

        // Student view:
        // 1. My profile and preferences
        // 2. List of courses for which I'm enrolled (if just one, simplify the view)
        // 3. A calendar of upcoming due dates
        // 4. Grades and statistics summary
        // 5. Access forums and help hours
        // 6. Within assignment: navigate or print, get help, link to course videos/content
        // 7. Online calculator or graphing tools (available while proctored)
        // 8. Schedule a proctored exam
    }

    /**
     * Appends page content appropriate to a guest user. A guest can "tour" the site under any of the available roles to
     * look at sample/demo data and try out assessments in practice mode. No data is recorded (profile settings are not
     * retained beyond a session, for example).
     *
     * <p>
     * A guest account can be configured to allow access to certain courses or assessments to provide anonymous practice
     * modules that can be attached to other courses or made available as review or tutorial.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     */
    private static void appendGuestContent(final HtmlBuilder htm, final ProctoringSite site,
                                           final HttpServletRequest req, final HttpServletResponse resp,
                                           final ImmutableSessionInfo session) {

        // Guest view:
        // 1. Menu of available views, each accessed in "demo" mode
    }
}

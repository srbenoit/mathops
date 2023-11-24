package dev.mathops.web.site.ramwork;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.rawlogic.RawTreePathLogic;
import dev.mathops.db.rawrecord.RawTreePath;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/**
 * Generates the QTI item bank page.
 */
enum PageQTIItemBank {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @param error   an error message to display
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void showPage(final Cache cache, final RamWorkSite site, final HttpServletRequest req,
                         final HttpServletResponse resp, final ImmutableSessionInfo session, final String error)
            throws IOException, SQLException {

        final ERole role = session.getEffectiveRole();

        if (role.canActAs(ERole.ADMINISTRATOR)) {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.ADMIN_BAR, null,
                    false, true);

            htm.div("vgap");
            htm.sH(1).add("QTI Item Bank").eH(1);
            htm.hr();

            htm.sDiv(null, "style='display:inline-block;"
                    + "width:200px;border-right:2px solid #ccc;'");

            htm.sH(2).add("Directory").eH(2);

            htm.sDiv("small");
            appendItemBank(cache, htm, site, req, resp, session);
            if (error != null) {
                Log.warning(error);
                htm.div("vgap");
                htm.sDiv("error").add(error).eDiv();
            }
            htm.eDiv(); // small

            htm.eDiv(); // inline-block

            Page.endOrdinaryPage(cache, site, htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Appends the item bank directory.
     *
     * @param cache   the data cache
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws SQLException if there is an error accessing the database
     */
    private static void appendItemBank(final Cache cache, final HtmlBuilder htm, final RamWorkSite site,
                                       final HttpServletRequest req, final HttpServletResponse resp,
                                       final ImmutableSessionInfo session) throws SQLException {

        final List<RawTreePath> allPaths = RawTreePathLogic.INSTANCE.queryAll(cache);
        Log.info("There were " + allPaths.size() + " paths");

        final List<RawTreePath.TreeNode> tree = RawTreePathLogic.organizeIntoTree(allPaths);
        Log.info("The tree has " + tree.size() + " root nodes");

        emitTreeLevel(tree, null, htm);
    }

    /**
     * Emits the HTML representation of a level in a tree.
     *
     * @param level      the list of nodes at this tree level.
     * @param parentNode the parent node (null for top-level nodes)
     * @param htm        the {@code HtmlBuilder} to which to append
     */
    private static void emitTreeLevel(final Iterable<? extends RawTreePath.TreeNode> level,
                                      final RawTreePath.TreeNode parentNode, final HtmlBuilder htm) {

        final int depth = parentNode == null ? 0 : parentNode.treePath.depth.intValue() + 1;

        int maxSort = -1;
        for (final RawTreePath.TreeNode node : level) {
            htm.addln("<details open ",
                    "style='padding-left:2px;border-left:1px solid #ccc;'>");
            htm.addln("<summary>", node.treePath.ident, "</summary>");
            htm.sDiv("indent0");
            emitTreeLevel(node.nodes, node, htm);
            htm.eDiv();
            htm.addln("</details>");
            maxSort = Math.max(maxSort, node.treePath.sortOrder.intValue());
        }

        final String id = parentNode == null ? "_"
                : parentNode.getPath().replace('/', '_');

        final String checkFunctionName = "check" + id;

        htm.addln("<script>");
        htm.addln("function ", checkFunctionName, "(txt) {");
        htm.addln("  var b=document.getElementById('X-", id, "');");
        htm.addln("  if (b) {");
        htm.addln("     const regex = new RegExp('[A-Za-z][A-Za-z0-9]*');");
        htm.addln("     if (regex.test(txt)) {");
        htm.addln("        b.disabled = false;");
        htm.addln("     } else {");
        htm.addln("        b.disabled = true;");
        htm.addln("     }");
        htm.addln("  } else {");
        htm.addln("    b.disabled = true;");
        htm.addln("  }");
        htm.addln("}");
        htm.addln("</script>");

        htm.addln("<form action='qtiitembank.html' method='post'>");

        htm.addln("<input type='text' data-lpignore='true' id='", id,
                "' name='ident' size='6' maxlength='12' onInput='",
                checkFunctionName, "(this.value)'/>");

        htm.addln("<input type='hidden' name='depth' value='",
                Integer.toString(depth), "'/>");

        if (parentNode != null) {
            htm.addln("<input type='hidden' name='parent_ident' value='",
                    parentNode.treePath.ident, "'/>");
        }
        htm.addln("<input type='hidden' name='sort_order' value='",
                Integer.toString(maxSort + 1), "'/>");

        htm.addln(" <button id='X-", id, "' type='submit' disabled>",
                "Add", //
                "</button>");
        htm.addln("</form>");
    }

    /**
     * Processes a POST request.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void processPost(final Cache cache, final RamWorkSite site,
                            final HttpServletRequest req, final HttpServletResponse resp,
                            final ImmutableSessionInfo session) throws IOException, SQLException {

        String error = null;

        final String ident = req.getParameter("ident");
        final String depth = req.getParameter("depth");
        final String parentIdent = req.getParameter("parent_ident");
        final String sortOrder = req.getParameter("sort_order");

        if (ident != null && depth != null && sortOrder != null) {
            try {
                final int theDepth = Integer.parseInt(depth);
                final int theSortOrder = Integer.parseInt(sortOrder);
                boolean valid = isIdentifier(ident);

                if (valid && (theDepth > 0)) {
                    if (parentIdent == null) {
                        error = "No parent identifier for depth " + depth;
                    } else {
                        valid = isIdentifier(parentIdent);
                    }
                }

                if (valid) {
                    if (theDepth < 0 || theDepth > 1000) {
                        error = "Invalid depth: " + depth;
                    } else if (theSortOrder < 0 || theSortOrder > 32767) {
                        error = "Invalid sort order: " + sortOrder;
                    } else {
                        error = createTreePath(cache, ident, theDepth, parentIdent, theSortOrder);
                    }
                }
            } catch (final NumberFormatException ex) {
                Log.warning(ex);
            }
        }

        showPage(cache, site, req, resp, session, error);
    }

    /**
     * Tests whether a string is a valid identifier.
     *
     * @param toTest the string to test
     * @return true if a valid identifier; false if not
     */
    private static boolean isIdentifier(final String toTest) {

        boolean valid = true;

        if (!toTest.isEmpty() && toTest.length() <= 12) {
            final char ch0 = toTest.charAt(0);
            if ((ch0 >= 'A' && ch0 <= 'Z') || (ch0 >= 'a' && ch0 <= 'z')) {
                for (int i = 1; i < toTest.length(); ++i) {
                    final char ch = toTest.charAt(i);
                    if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')) {
                        continue;
                    }
                    valid = false;
                    break;
                }
            } else {
                valid = false;
            }
        } else {
            valid = false;
        }

        if (!valid) {
            Log.warning("Invalid identifier: ", toTest);
        }

        return valid;
    }

    /**
     * Creates and inserts a tree path record, but first tests whether there is an existing record with the same values
     * in primary key fields.
     *
     * @param cache       the data cache
     * @param ident       the identifier
     * @param depth       the depth
     * @param parentIdent the parent identifier
     * @param sortOrder   the sort order
     * @return null if successful, an error message if not
     * @throws SQLException if there is an error accessing the database
     */
    private static String createTreePath(final Cache cache, final String ident, final int depth,
                                         final String parentIdent, final int sortOrder) throws SQLException {

        String error = null;

        final RawTreePath existing = RawTreePathLogic.query(cache, ident, depth, parentIdent);

        if (existing == null) {
            final RawTreePath toInsert = new RawTreePath(ident, parentIdent, Integer.valueOf(depth),
                    Integer.valueOf(sortOrder), null);
            if (!RawTreePathLogic.INSTANCE.insert(cache, toInsert)) {
                error = "Error: Failed to create directory entry.";
            }
        } else {
            error = "Error: Directory entry already exists.";
        }

        return error;
    }
}

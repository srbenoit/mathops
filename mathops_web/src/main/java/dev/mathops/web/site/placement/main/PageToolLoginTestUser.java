package dev.mathops.web.site.placement.main;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.TermKey;
import dev.mathops.db.svc.term.TermLogic;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.session.SessionResult;
import dev.mathops.session.login.TestStudentLoginProcessor;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates a page that allows the user to select a test user under which to log in.
 */
enum PageToolLoginTestUser {
    ;

    /** Zero-length array used in construction of other arrays. */
    private static final byte[] ZERO_LEN_BYTE_ARR = new byte[0];

    /**
     * Generates a page that supports logging in as one of the test users whose ID begins with '99'.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final MathPlacementSite site,
                             final ServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(8192);
        Page.startNofooterPage(htm, site.getTitle(), null, true, Page.NO_BARS, null, false, false);
        htm.sDiv("inset2");
        htm.sDiv("shaded2left");

        htm.sP().add("This page supports logging in as a test student to see what the web pages display for ",
                "students in a variety of situations.  Test students are \"read-only\", and cannot start or ",
                "submit exams.").eP();

        htm.div("vgap").hr();

        htm.addln("<script>");
        htm.addln(" function rebuildId(cmd) {");
        // Following two statements prevent recursive calls as checkboxes are made consistent
        htm.addln("   if (rebuildId.isupdating != true) {");
        htm.addln("     rebuildId.isupdating = true;");

        htm.addln("     var termIdx = document.getElementById('apln_term_select').selectedIndex;");
        htm.addln("     var dce = document.getElementById('spcl_dce').checked;");
        htm.addln("     var pre = document.getElementById('spcl_preview').checked;");

        htm.addln("     if (cmd == 'p_m101') {");
        htm.addln("       if (document.getElementById('p_m101').checked) {");
        htm.addln("         document.getElementById('p_m100c').checked = false;");
        htm.addln("         document.getElementById('p_m117').checked = false;");
        htm.addln("         document.getElementById('p_m118').checked = false;");
        htm.addln("         document.getElementById('p_m124').checked = false;");
        htm.addln("         document.getElementById('p_m125').checked = false;");
        htm.addln("         document.getElementById('p_m126').checked = false;");
        htm.addln("         document.getElementById('c_m117').checked = false;");
        htm.addln("         document.getElementById('c_m118').checked = false;");
        htm.addln("         document.getElementById('c_m124').checked = false;");
        htm.addln("         document.getElementById('c_m125').checked = false;");
        htm.addln("         document.getElementById('c_m126').checked = false;");
        htm.addln("         document.getElementById('e_pt117').checked = false;");
        htm.addln("         document.getElementById('e_pt118').checked = false;");
        htm.addln("         document.getElementById('e_pt124').checked = false;");
        htm.addln("         document.getElementById('e_pt125').checked = false;");
        htm.addln("         document.getElementById('e_pt126').checked = false;");
        htm.addln("       }");
        htm.addln("     } else if (cmd == 'p_m100c') {");
        htm.addln("       if (document.getElementById('p_m100c').checked) {");
        htm.addln("         document.getElementById('p_m101').checked = false;");
        htm.addln("       } else {");
        htm.addln("         document.getElementById('p_m101').checked = true;");
        htm.addln("         document.getElementById('p_m117').checked = false;");
        htm.addln("         document.getElementById('p_m118').checked = false;");
        htm.addln("         document.getElementById('p_m124').checked = false;");
        htm.addln("         document.getElementById('p_m125').checked = false;");
        htm.addln("         document.getElementById('p_m126').checked = false;");
        htm.addln("         document.getElementById('c_m117').checked = false;");
        htm.addln("         document.getElementById('c_m118').checked = false;");
        htm.addln("         document.getElementById('c_m124').checked = false;");
        htm.addln("         document.getElementById('c_m125').checked = false;");
        htm.addln("         document.getElementById('c_m126').checked = false;");
        htm.addln("         document.getElementById('e_pt117').checked = false;");
        htm.addln("         document.getElementById('e_pt118').checked = false;");
        htm.addln("         document.getElementById('e_pt124').checked = false;");
        htm.addln("         document.getElementById('e_pt125').checked = false;");
        htm.addln("         document.getElementById('e_pt126').checked = false;");
        htm.addln("       }");
        htm.addln("     } else if (cmd == 'p_m117') {");
        htm.addln("       if (document.getElementById('p_m117').checked) {");
        htm.addln("         document.getElementById('p_m101').checked = false;");
        htm.addln("         document.getElementById('p_m100c').checked = true;");
        htm.addln("       } else {");
        htm.addln("         document.getElementById('p_m118').checked = false;");
        htm.addln("         document.getElementById('p_m124').checked = false;");
        htm.addln("         document.getElementById('p_m125').checked = false;");
        htm.addln("         document.getElementById('p_m126').checked = false;");
        htm.addln("         document.getElementById('c_m117').checked = false;");
        htm.addln("         document.getElementById('c_m118').checked = false;");
        htm.addln("         document.getElementById('c_m124').checked = false;");
        htm.addln("         document.getElementById('c_m125').checked = false;");
        htm.addln("         document.getElementById('c_m126').checked = false;");
        htm.addln("         document.getElementById('e_pt118').checked = false;");
        htm.addln("         document.getElementById('e_pt124').checked = false;");
        htm.addln("         document.getElementById('e_pt125').checked = false;");
        htm.addln("         document.getElementById('e_pt126').checked = false;");
        htm.addln("       }");
        htm.addln("     } else if (cmd == 'p_m118') {");
        htm.addln("       if (document.getElementById('p_m118').checked) {");
        htm.addln("         document.getElementById('p_m101').checked = false;");
        htm.addln("         document.getElementById('p_m100c').checked = true;");
        htm.addln("         document.getElementById('p_m117').checked = true;");
        htm.addln("       } else {");
        htm.addln("         document.getElementById('p_m124').checked = false;");
        htm.addln("         document.getElementById('p_m125').checked = false;");
        htm.addln("         document.getElementById('p_m126').checked = false;");
        htm.addln("         document.getElementById('c_m118').checked = false;");
        htm.addln("         document.getElementById('c_m124').checked = false;");
        htm.addln("         document.getElementById('c_m125').checked = false;");
        htm.addln("         document.getElementById('c_m126').checked = false;");
        htm.addln("         document.getElementById('e_pt124').checked = false;");
        htm.addln("         document.getElementById('e_pt125').checked = false;");
        htm.addln("         document.getElementById('e_pt126').checked = false;");
        htm.addln("       }");
        htm.addln("     } else if (cmd == 'p_m124') {");
        htm.addln("       if (document.getElementById('p_m124').checked) {");
        htm.addln("         document.getElementById('p_m101').checked = false;");
        htm.addln("         document.getElementById('p_m100c').checked = true;");
        htm.addln("         document.getElementById('p_m117').checked = true;");
        htm.addln("         document.getElementById('p_m118').checked = true;");
        htm.addln("       } else {");
        htm.addln("         document.getElementById('c_m124').checked = false;");
        htm.addln("         document.getElementById('e_pt125').checked = false;");
        htm.addln("         document.getElementById('e_pt126').checked = false;");
        htm.addln("       }");
        htm.addln("     } else if (cmd == 'p_m125') {");
        htm.addln("       if (document.getElementById('p_m125').checked) {");
        htm.addln("         document.getElementById('p_m101').checked = false;");
        htm.addln("         document.getElementById('p_m100c').checked = true;");
        htm.addln("         document.getElementById('p_m117').checked = true;");
        htm.addln("         document.getElementById('p_m118').checked = true;");
        htm.addln("       } else {");
        htm.addln("         document.getElementById('p_m126').checked = false;");
        htm.addln("         document.getElementById('c_m125').checked = false;");
        htm.addln("         document.getElementById('c_m126').checked = false;");
        htm.addln("         document.getElementById('e_pt126').checked = false;");
        htm.addln("       }");
        htm.addln("     } else if (cmd == 'p_m126') {");
        htm.addln("       if (document.getElementById('p_m126').checked) {");
        htm.addln("         document.getElementById('p_m101').checked = false;");
        htm.addln("         document.getElementById('p_m100c').checked = true;");
        htm.addln("         document.getElementById('p_m117').checked = true;");
        htm.addln("         document.getElementById('p_m118').checked = true;");
        htm.addln("         document.getElementById('p_m125').checked = true;");
        htm.addln("       } else {");
        htm.addln("         document.getElementById('c_m126').checked = false;");
        htm.addln("       }");
        htm.addln("     } else if (cmd == 'c_m117') {");
        htm.addln("       if (document.getElementById('c_m117').checked) {");
        htm.addln("         document.getElementById('p_m100c').checked = true;");
        htm.addln("         document.getElementById('p_m117').checked = true;");
        htm.addln("       }");
        htm.addln("     } else if (cmd == 'c_m118') {");
        htm.addln("       if (document.getElementById('c_m118').checked) {");
        htm.addln("         document.getElementById('p_m101').checked = false;");
        htm.addln("         document.getElementById('p_m100c').checked = true;");
        htm.addln("         document.getElementById('p_m117').checked = true;");
        htm.addln("         document.getElementById('p_m118').checked = true;");
        htm.addln("       }");
        htm.addln("     } else if (cmd == 'c_m124') {");
        htm.addln("       if (document.getElementById('c_m124').checked) {");
        htm.addln("         document.getElementById('p_m101').checked = false;");
        htm.addln("         document.getElementById('p_m100c').checked = true;");
        htm.addln("         document.getElementById('p_m117').checked = true;");
        htm.addln("         document.getElementById('p_m118').checked = true;");
        htm.addln("         document.getElementById('p_m124').checked = true;");
        htm.addln("       }");
        htm.addln("     } else if (cmd == 'c_m125') {");
        htm.addln("       if (document.getElementById('c_m125').checked) {");
        htm.addln("         document.getElementById('p_m101').checked = false;");
        htm.addln("         document.getElementById('p_m100c').checked = true;");
        htm.addln("         document.getElementById('p_m117').checked = true;");
        htm.addln("         document.getElementById('p_m118').checked = true;");
        htm.addln("         document.getElementById('p_m125').checked = true;");
        htm.addln("       }");
        htm.addln("     } else if (cmd == 'c_m126') {");
        htm.addln("       if (document.getElementById('c_m126').checked) {");
        htm.addln("         document.getElementById('p_m101').checked = false;");
        htm.addln("         document.getElementById('p_m100c').checked = true;");
        htm.addln("         document.getElementById('p_m117').checked = true;");
        htm.addln("         document.getElementById('p_m118').checked = true;");
        htm.addln("         document.getElementById('p_m125').checked = true;");
        htm.addln("         document.getElementById('p_m126').checked = true;");
        htm.addln("       }");
        htm.addln("     } else if (cmd == 'e_elm4') {");
        htm.addln("       if (document.getElementById('e_elm4').checked) {");
        htm.addln("         document.getElementById('e_pt117').checked = false;");
        htm.addln("         document.getElementById('e_pt118').checked = false;");
        htm.addln("         document.getElementById('e_pt124').checked = false;");
        htm.addln("         document.getElementById('e_pt125').checked = false;");
        htm.addln("         document.getElementById('e_pt126').checked = false;");
        htm.addln("       }");
        htm.addln("     } else if (cmd == 'e_pt117') {");
        htm.addln("       if (document.getElementById('e_pt117').checked) {");
        htm.addln("         document.getElementById('e_elm4').checked = false;");
        htm.addln("         document.getElementById('e_pt118').checked = false;");
        htm.addln("         document.getElementById('e_pt124').checked = false;");
        htm.addln("         document.getElementById('e_pt125').checked = false;");
        htm.addln("         document.getElementById('e_pt126').checked = false;");
        htm.addln("       }");
        htm.addln("     } else if (cmd == 'e_pt118') {");
        htm.addln("       if (document.getElementById('e_pt118').checked) {");
        htm.addln("         document.getElementById('e_elm4').checked = false;");
        htm.addln("         document.getElementById('e_pt117').checked = false;");
        htm.addln("         document.getElementById('e_pt124').checked = false;");
        htm.addln("         document.getElementById('e_pt125').checked = false;");
        htm.addln("         document.getElementById('e_pt126').checked = false;");
        htm.addln("       }");
        htm.addln("     } else if (cmd == 'e_pt124') {");
        htm.addln("       if (document.getElementById('e_pt124').checked) {");
        htm.addln("         document.getElementById('e_elm4').checked = false;");
        htm.addln("         document.getElementById('e_pt117').checked = false;");
        htm.addln("         document.getElementById('e_pt118').checked = false;");
        htm.addln("         document.getElementById('e_pt125').checked = false;");
        htm.addln("         document.getElementById('e_pt126').checked = false;");
        htm.addln("       }");
        htm.addln("     } else if (cmd == 'e_pt125') {");
        htm.addln("       if (document.getElementById('e_pt125').checked) {");
        htm.addln("         document.getElementById('e_elm4').checked = false;");
        htm.addln("         document.getElementById('e_pt117').checked = false;");
        htm.addln("         document.getElementById('e_pt118').checked = false;");
        htm.addln("         document.getElementById('e_pt124').checked = false;");
        htm.addln("         document.getElementById('e_pt126').checked = false;");
        htm.addln("       }");
        htm.addln("     } else if (cmd == 'e_pt126') {");
        htm.addln("       if (document.getElementById('e_pt126').checked) {");
        htm.addln("         document.getElementById('e_elm4').checked = false;");
        htm.addln("         document.getElementById('e_pt117').checked = false;");
        htm.addln("         document.getElementById('e_pt118').checked = false;");
        htm.addln("         document.getElementById('e_pt124').checked = false;");
        htm.addln("         document.getElementById('e_pt125').checked = false;");
        htm.addln("       }");
        htm.addln("     }");

        htm.addln("     var ch5 = document.getElementById('apln_exams_taken').selectedIndex;");
        htm.addln("     var p100c = document.getElementById('p_m100c').checked;");
        htm.addln("     var p117 = document.getElementById('p_m117').checked;");
        htm.addln("     var p118 = document.getElementById('p_m118').checked;");
        htm.addln("     var p124 = document.getElementById('p_m124').checked;");
        htm.addln("     var p125 = document.getElementById('p_m125').checked;");
        htm.addln("     var p126 = document.getElementById('p_m126').checked;");

        htm.addln("     if (ch5 == '0') {");
        htm.addln("       document.getElementById('p_m101').checked = false;");
        htm.addln("       document.getElementById('p_m100c').checked = false;");
        htm.addln("       document.getElementById('p_m117').checked = false;");
        htm.addln("       document.getElementById('p_m118').checked = false;");
        htm.addln("       document.getElementById('p_m124').checked = false;");
        htm.addln("       document.getElementById('p_m125').checked = false;");
        htm.addln("       document.getElementById('p_m126').checked = false;");
        htm.addln("       document.getElementById('c_m117').checked = false;");
        htm.addln("       document.getElementById('c_m118').checked = false;");
        htm.addln("       document.getElementById('c_m124').checked = false;");
        htm.addln("       document.getElementById('c_m125').checked = false;");
        htm.addln("       document.getElementById('c_m126').checked = false;");
        htm.addln("       document.getElementById('e_elm4').checked = false;");
        htm.addln("       document.getElementById('e_pt117').checked = false;");
        htm.addln("       document.getElementById('e_pt118').checked = false;");
        htm.addln("       document.getElementById('e_pt124').checked = false;");
        htm.addln("       document.getElementById('e_pt125').checked = false;");
        htm.addln("       document.getElementById('e_pt126').checked = false;");
        htm.addln("       document.getElementById('p_m101').disabled = true;");
        htm.addln("       document.getElementById('p_m100c').disabled = true;");
        htm.addln("       document.getElementById('p_m117').disabled = true;");
        htm.addln("       document.getElementById('p_m118').disabled = true;");
        htm.addln("       document.getElementById('p_m124').disabled = true;");
        htm.addln("       document.getElementById('p_m125').disabled = true;");
        htm.addln("       document.getElementById('p_m126').disabled = true;");
        htm.addln("       document.getElementById('c_m117').disabled = true;");
        htm.addln("       document.getElementById('c_m118').disabled = true;");
        htm.addln("       document.getElementById('c_m124').disabled = true;");
        htm.addln("       document.getElementById('c_m125').disabled = true;");
        htm.addln("       document.getElementById('c_m126').disabled = true;");
        htm.addln("       document.getElementById('e_elm4').disabled = true;");
        htm.addln("       document.getElementById('e_pt117').disabled = true;");
        htm.addln("       document.getElementById('e_pt118').disabled = true;");
        htm.addln("       document.getElementById('e_pt124').disabled = true;");
        htm.addln("       document.getElementById('e_pt125').disabled = true;");
        htm.addln("       document.getElementById('e_pt126').disabled = true;");
        htm.addln("     } else {");
        htm.addln("       document.getElementById('p_m101').disabled = false;");
        htm.addln("       document.getElementById('p_m100c').disabled = false;");
        htm.addln("       document.getElementById('p_m117').disabled = false;");
        htm.addln("       document.getElementById('p_m118').disabled = false;");
        htm.addln("       document.getElementById('p_m124').disabled = false;");
        htm.addln("       document.getElementById('p_m125').disabled = false;");
        htm.addln("       document.getElementById('p_m126').disabled = false;");
        htm.addln("       if (ch5 == '3') {");
        htm.addln("         document.getElementById('c_m117').disabled = true;");
        htm.addln("         document.getElementById('c_m118').disabled = true;");
        htm.addln("         document.getElementById('c_m124').disabled = true;");
        htm.addln("         document.getElementById('c_m125').disabled = true;");
        htm.addln("         document.getElementById('c_m126').disabled = true;");
        htm.addln("       } else {");
        htm.addln("         document.getElementById('c_m117').disabled = false;");
        htm.addln("         document.getElementById('c_m118').disabled = false;");
        htm.addln("         document.getElementById('c_m124').disabled = false;");
        htm.addln("         document.getElementById('c_m125').disabled = false;");
        htm.addln("         document.getElementById('c_m126').disabled = false;");
        htm.addln("       }");
        htm.addln("       document.getElementById('e_elm4').disabled = p100c;");
        htm.addln("       document.getElementById('e_pt117').disabled = p117 || !p100c;");
        htm.addln("       document.getElementById('e_pt118').disabled = p118 || !p117;");
        htm.addln("       document.getElementById('e_pt124').disabled = p124 || !p118;");
        htm.addln("       document.getElementById('e_pt125').disabled = p125 || !p124;");
        htm.addln("       document.getElementById('e_pt126').disabled = p126 || !p125 || !p124;");
        htm.addln("     }");

        htm.addln("     var ch6;");
        htm.addln("     if (ch5 == '0') {");
        htm.addln("       ch6 = '0'");
        htm.addln("     } else if (document.getElementById('p_m100c').checked) {");
        htm.addln("       if (document.getElementById('p_m117').checked) {");
        htm.addln("         if (document.getElementById('p_m118').checked) {");
        htm.addln("           if (document.getElementById('p_m124').checked) {");
        htm.addln("             if (document.getElementById('p_m125').checked) {");
        htm.addln("               if (document.getElementById('p_m126').checked) {");
        htm.addln("                 ch6 = '8'");
        htm.addln("               } else {");
        htm.addln("                 ch6 = '6'");
        htm.addln("               }");
        htm.addln("             } else {");
        htm.addln("               ch6 = '4'");
        htm.addln("             }");
        htm.addln("           } else if (document.getElementById('p_m125').checked) {");
        htm.addln("             if (document.getElementById('p_m126').checked) {");
        htm.addln("               ch6 = '7'");
        htm.addln("             } else {");
        htm.addln("               ch6 = '5'");
        htm.addln("             }");
        htm.addln("           } else {");
        htm.addln("             ch6 = '3'");
        htm.addln("           }");
        htm.addln("         } else {");
        htm.addln("           ch6 = '2'");
        htm.addln("         }");
        htm.addln("       } else {");
        htm.addln("         ch6 = '1'");
        htm.addln("       }");
        htm.addln("     } else {");
        htm.addln("       ch6 = '0'");
        htm.addln("     }");

        htm.addln("     var ch7;");
        htm.addln("     if (ch5 == '0') {");
        htm.addln("       ch7 = '0'");
        htm.addln("     } else if (document.getElementById('c_m117').checked) {");
        htm.addln("       if (document.getElementById('c_m118').checked) {");
        htm.addln("         if (document.getElementById('c_m124').checked) {");
        htm.addln("           if (document.getElementById('c_m125').checked) {");
        htm.addln("             if (document.getElementById('c_m126').checked) {");
        htm.addln("               ch7 = '8'");
        htm.addln("             } else {");
        htm.addln("               ch7 = '6'");
        htm.addln("             }");
        htm.addln("           } else {");
        htm.addln("             ch7 = '4'");
        htm.addln("           }");
        htm.addln("         } else if (document.getElementById('c_m125').checked) {");
        htm.addln("           if (document.getElementById('c_m126').checked) {");
        htm.addln("             ch7 = '7'");
        htm.addln("           } else {");
        htm.addln("             ch7 = '5'");
        htm.addln("           }");
        htm.addln("         } else {");
        htm.addln("           ch7 = '3'");
        htm.addln("         }");
        htm.addln("       } else {");
        htm.addln("         ch7 = '2'");
        htm.addln("       }");
        htm.addln("     } else {");
        htm.addln("       ch7 = '0'");
        htm.addln("     }");

        htm.addln("     var ch8;");
        htm.addln("     if (termIdx == 0) {");
        htm.addln("       ch8 = dce?(pre?'3':'1'):(pre?'2':'0');");
        htm.addln("     } else if (termIdx == 1) {");
        htm.addln("       ch8 = dce?(pre?'7':'5'):(pre?'6':'4');");
        htm.addln("     } else if (termIdx == 2) {");
        htm.addln("       ch8 = dce?(pre?'B':'9'):(pre?'A':'8');");
        htm.addln("     } else if (termIdx == 3) {");
        htm.addln("       ch8 = dce?(pre?'F':'D'):(pre?'E':'C');");
        htm.addln("     } else if (termIdx == 4) {");
        htm.addln("       ch8 = dce?(pre?'J':'H'):(pre?'I':'G');");
        htm.addln("     } else if (termIdx == 5) {");
        htm.addln("       ch8 = dce?(pre?'N':'L'):(pre?'M':'K');");
        htm.addln("     } else {");
        htm.addln("       ch8 = dce?(pre?'R':'P'):(pre?'Q':'O');");
        htm.addln("     }");

        htm.addln("     var ch9;");
        htm.addln("     if (document.getElementById('e_pt126').checked) {");
        htm.addln("       ch9 = '6';");
        htm.addln("     } else if (document.getElementById('e_pt125').checked) {");
        htm.addln("       ch9 = '5';");
        htm.addln("     } else if (document.getElementById('e_pt124').checked) {");
        htm.addln("       ch9 = '4';");
        htm.addln("     } else if (document.getElementById('e_pt118').checked) {");
        htm.addln("       ch9 = '3';");
        htm.addln("     } else if (document.getElementById('e_pt117').checked) {");
        htm.addln("       ch9 = '2';");
        htm.addln("     } else if (document.getElementById('e_elm4').checked) {");
        htm.addln("       ch9 = '1';");
        htm.addln("     } else {");
        htm.addln("       ch9 = '0';");
        htm.addln("     }");

        htm.addln("     var constructed = '99PL' + ch5 + ch6 + ch7 + ch8 + ch9");
        htm.addln("     document.getElementById('tsid').value=constructed;");
        htm.addln("     var span = document.getElementById('tsidspan');");
        htm.addln("     while (span.firstChild) {");
        htm.addln("       span.removeChild(span.firstChild);");
        htm.addln("     }");
        htm.addln("     span.appendChild(document.createTextNode(constructed));");
        htm.addln("   }");
        htm.addln("   rebuildId.isupdating = false;");
        htm.addln(" }");
        htm.addln("</script>");

        htm.sDiv("indent11");
        htm.addln("<form action='login_test_user_by_id.html' method='post'>");

        // Application term input
        htm.sP().addln("<label for='apln_term_select'>",
                "Select test student's application term: &nbsp; ", //
                "</label> <select id='apln_term_select' onchange='rebuildId(\"\")'>");
        TermKey term = TermLogic.get(cache).queryActive(cache).term;
        for (int i = 0; i < 6; ++i) {
            htm.addln(" <option value='", term.shortString, "'>", term.longString, "</option>");
            term = term.add(1);
        }
        htm.addln("</select>").eP();

        // Special student categories inputs
        htm.sP().addln("Test user's special student categories: &nbsp; ");
        htm.addln("<input type='checkbox' id='spcl_dce' onchange='rebuildId(\"\")'>",
                "<label for='spcl_dce'>DCE</label> &nbsp; ");
        htm.addln("<input type='checkbox' id='spcl_preview' onchange='rebuildId(\"\")'>",
                "<label for='spcl_preview'>PREVIEW</label> ").eP();

        // Exams taken
        htm.sP().addln("<label for='apln_exams_taken'>",
                "What exams has this student taken? &nbsp; ", //
                "</label> <select id='apln_exams_taken' onchange='rebuildId(\"taken\")'>");
        htm.addln(" <option value='0'>None</option>");
        htm.addln(" <option value='1'>One Challenge Exam</option>");
        htm.addln(" <option value='2'>Two Challenge Exams</option>");
        htm.addln(" <option value='3'>One Placement Exam</option>");
        htm.addln(" <option value='4'>One Placement Exam, One Challenge Exam</option>");
        htm.addln(" <option value='5'>One Placement Exam, Two Challenge Exams</option>");
        htm.addln("</select>").eP();

        // Placement and credit outcomes
        htm.sDiv("center");
        htm.sP().addln("What results and eligibility should this student have:").eP(); //
        htm.sTable("plan-table", "style='display:inline-table;'");
        htm.sTr().sTd().add("<b>Placed:</b>").eTd() //
                .sTd().add("<b>Earned credit for:</b>").eTd() //
                .sTd().add("<b>Eligible for:</b>").eTd().eTr();
        htm.sTr().sTd().add(
                        "<input type='checkbox' id='p_m101' checked disabled onchange='rebuildId(\"p_m101\")'>",
                        "<label for='p_m101'>None (MATH 101)</label>").eTd().sTd().eTd().sTd()
                .add("<input type='checkbox' disabled id='e_elm4' onchange='rebuildId(\"e_elm4\")'>",
                        "<label for='e_elm4'>ELM Exam</label>")
                .eTd().eTr();
        htm.sTr().sTd()
                .add("<input type='checkbox' disabled id='p_m100c' onchange='rebuildId(\"p_m100c\")'>",
                        "<label for='p_m100c'>Into MATH 117</label>")
                .eTd().sTd().eTd().sTd()
                .add("<input type='checkbox' disabled id='e_pt117' onchange='rebuildId(\"e_pt117\")'>",
                        "<label for='e_pt117'>Precalc Tutorial Exam (117)</label>")
                .eTd().eTr();
        htm.sTr().sTd()
                .add("<input type='checkbox' disabled id='p_m117' onchange='rebuildId(\"p_m117\")'>",
                        "<label for='p_m117'>out of MATH 117</label>")
                .eTd().sTd()
                .add("<input type='checkbox' disabled id='c_m117' onchange='rebuildId(\"c_m117\")'>",
                        "<label for='c_m117'>MATH 117</label>")
                .eTd().sTd()
                .add("<input type='checkbox' disabled id='e_pt118' onchange='rebuildId(\"e_pt118\")'>",
                        "<label for='e_pt118'>Precalc Tutorial Exam (118)</label>")
                .eTd().eTr();
        htm.sTr().sTd()
                .add("<input type='checkbox' disabled id='p_m118' onchange='rebuildId(\"p_m118\")'>",
                        "<label for='p_m118'>out of MATH 118</label>")
                .eTd().sTd()
                .add("<input type='checkbox' disabled id='c_m118' onchange='rebuildId(\"c_m118\")'>",
                        "<label for='c_m118'>MATH 118</label>")
                .eTd().sTd()
                .add("<input type='checkbox' disabled id='e_pt124' onchange='rebuildId(\"e_pt124\")'>",
                        "<label for='e_pt124'>Precalc Tutorial Exam (124)</label>")
                .eTd().eTr();
        htm.sTr().sTd()
                .add("<input type='checkbox' disabled id='p_m124' onchange='rebuildId(\"p_m124\")'>",
                        "<label for='p_m124'>out of MATH 124</label>")
                .eTd().sTd()
                .add("<input type='checkbox' disabled id='c_m124' onchange='rebuildId(\"c_m124\")'>",
                        "<label for='c_m124'>MATH 124</label>")
                .eTd().sTd()
                .add("<input type='checkbox' disabled id='e_pt125' onchange='rebuildId(\"e_pt125\")'>",
                        "<label for='e_pt125'>Precalc Tutorial Exam (125)</label>")
                .eTd().eTr();
        htm.sTr().sTd()
                .add("<input type='checkbox' disabled id='p_m125' onchange='rebuildId(\"p_m125\")'>",
                        "<label for='p_m125'>out of MATH 125</label>")
                .eTd().sTd()
                .add("<input type='checkbox' disabled id='c_m125' onchange='rebuildId(\"c_m125\")'>",
                        "<label for='c_m125'>MATH 125</label>")
                .eTd().sTd()
                .add("<input type='checkbox' disabled id='e_pt126' onchange='rebuildId(\"e_pt126\")'>",
                        "<label for='e_pt126'>Precalc Tutorial Exam (126)</label>")
                .eTd().eTr();
        htm.sTr().sTd()
                .add("<input type='checkbox' disabled id='p_m126' onchange='rebuildId(\"p_m126\")'>",
                        "<label for='p_m126'>out of MATH 126</label>")
                .eTd().sTd()
                .add("<input type='checkbox' disabled id='c_m126' onchange='rebuildId(\"c_m126\")'>",
                        "<label for='c_m126'>MATH 126</label>")
                .eTd().sTd().eTd().eTr();
        htm.eTable();
        htm.eDiv();

        htm.div("vgap2");
        htm.addln("Generated test student ID: &nbsp; <code id='tsidspan'></code>");
        htm.div("vgap2");
        htm.sDiv("center");

        htm.addln("<input type='hidden' id='tsid' name='tsid'/>");
        htm.addln("<input class='btn' type='submit' value='Log in'/>");
        htm.eDiv();
        htm.addln("</form>");
        htm.eDiv();

        htm.eDiv(); // shaded2left
        htm.eDiv(); // inset2

        MPPage.emitScripts(htm, "rebuildId(this);");
        MPPage.endPage(htm, req, resp);
    }

    /**
     * Generates a page that supports logging in as one of the test users whose ID begins with '99'.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doPost(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                       final HttpServletResponse resp) throws IOException, SQLException {

        final String studentId = req.getParameter("tsid");

        Log.info("Logging in test student " + studentId);

        final SessionManager mgr = SessionManager.getInstance();

        final Map<String, String> fields = new HashMap<>(1);
        fields.put(TestStudentLoginProcessor.STU_ID, studentId);
        final SessionResult result = mgr.login(cache,
                mgr.identifyProcessor(TestStudentLoginProcessor.TYPE), fields, site.doLiveRegQueries());

        final ImmutableSessionInfo sess = result.session;

        if (sess == null) {
            doGet(cache, site, req, resp);
        } else {
            final String redirect = "tool.html";

            Log.info("Session is " + sess.loginSessionId, ", redirecting to ", redirect);

            // Install the session ID cookie in the response
            Log.info("Adding session ID cookie ", req.getServerName());
            final Cookie cook = new Cookie(SessionManager.SESSION_ID_COOKIE, sess.loginSessionId);
            cook.setDomain(req.getServerName());
            cook.setPath(CoreConstants.SLASH);
            cook.setMaxAge(-1);
            cook.setSecure(true);
            resp.addCookie(cook);

            resp.setHeader("Location", redirect);
            resp.setStatus(HttpServletResponse.SC_FOUND);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, ZERO_LEN_BYTE_ARR);
        }
    }
}

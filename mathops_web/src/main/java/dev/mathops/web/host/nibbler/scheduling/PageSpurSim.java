package dev.mathops.web.host.nibbler.scheduling;

import dev.mathops.db.Cache;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates a page with a simulation for the Spur campus academic program.
 */
enum PageSpurSim {
    ;

    /**
     * Generates the page.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param type  the site type
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void showPage(final Cache cache, final SchedulingSite site, final ESiteType type, final ServletRequest req,
                         final HttpServletResponse resp) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startEmptyPage(htm, "Scheduling", true);

        htm.sH(1).add("Spur Campus Academic Program Schedule Simulation").eH(1);

        htm.addln("<form action='spursim.html' method='POST'>");
        htm.sP().add("Step 1: Define the set of courses to be offered each semester:").eP();
        htm.sDiv("left", "style='padding-left:20px;'");

        htm.add("<b>Fall Semester:</b>:").br();
        htm.add("<input type='checkbox' name='FA_SEMINAR'>Seminar</input>");
        htm.eDiv();

        htm.sDiv("left", "style='padding-left:20px;'");
        htm.add("<b>Spring Semester:</b>:").br();
        htm.add("<input type='checkbox' name='FA_SEMINAR'>Seminar</input>");
        htm.eDiv();





//        public static final Course SEMINAR = new Course("SEMINAR", CRED1, 40, true);
//
//        // Fall Courses
//
//        public static final Course LIFE102 = new Course("LIFE 102", CRED4, 40, false);
//
//        public static final Course MATH112 = new Course("MATH 112", CRED3, 40, false);
//
//        public static final Course CS150B = new Course("CS 150B", CRED3, 40, false);
//
//        public static final Course IDEA110 = new Course("IDEA 110", CRED3, 40, false);
//
//        public static final Course HDFS101 = new Course("HDFS 101", CRED3, 40, false);
//
//        public static final Course AGRI116 = new Course("AGRI 116", CRED3, 40, false); // Did not run in 2024
//
//        public static final Course AB111 = new Course("AB 111", CRED3, 40, false); // Did not run in 2024
//
//        public static final Course ERHS220 = new Course("ERHS 220", CRED3, 40, false);
//
//        public static final Course POLS131 = new Course("POLS 131", CRED3, 40, false);
//
//        public static final Course AREC222 = new Course("AREC 222", CRED3, 40, false);
//
//        public static final Course SPCM100 = new Course("SPCM 100", CRED3, 40, false);
//
//        public static final Course BZ101 = new Course("BZ 101", CRED3, 40, false);
//
//        // Spring CoursesAGRI
//
//        public static final Course CO150 = new Course("CO 150", CRED3, 24, false);
//
//        public static final Course SOC220 = new Course("SOC 220", CRED3, 40, false);
//
//        public static final Course LIFE103 = new Course("LIFE 103", CRED3, 40, false);
//
//        public static final Course CHEM111 = new Course("CHEM 111/112", CRED3, 40, false);
//
//        public static final Course IDEA210 = new Course("IDEA 210", CRED3, 40, false);
//
//        public static final Course MIP101 = new Course("MIP 101", CRED3, 40, false);
//
//        public static final Course CS201 = new Course("CS 201", CRED3, 40, false);
//
//        public static final Course HISTORY = new Course("HISTORY", CRED3, 40, false);
//
//        public static final Course IU173 = new Course("IU 173", CRED3, 24, false);
//
//        public static final Course IU174 = new Course("IU 174", CRED3, 24, false);






        htm.addln("</form>");

        Page.endEmptyPage(htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}

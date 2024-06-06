package dev.mathops.web.site.proctoring.media;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.json.JSONObject;
import dev.mathops.commons.parser.json.JSONParser;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.websocket.proctor.MPSEndpoint;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Generates the home page.
 */
enum PageHome {
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
    static void showPage(final Cache cache, final ProctoringMediaSite site,
                         final ServletRequest req, final HttpServletResponse resp,
                         final ImmutableSessionInfo session) throws IOException, SQLException {

        final ERole role = session.getEffectiveRole();

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.ADMIN_BAR, null, false, true);

        htm.sDiv(null, "style='padding-left:16px; padding-right:16px;'");

        if (role.canActAs(ERole.ADMINISTRATOR) || role.canActAs(ERole.OFFICE_STAFF)
                || role.canActAs(ERole.DIRECTOR) || role.canActAs(ERole.PROCTOR)) {
            htm.sH(2).add(Res.get(Res.HOME_HEADING)).eH(2);
            emitFiles(gatherFiles(site.dataDir), htm);
        } else {
            htm.sP().addln("Not authorized to access proctoring media management").eP();
        }

        htm.eDiv();

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gathers a list of sessions, organized by student, by scanning the proctoring data directory.
     *
     * @param dataDir the proctoring data directory
     * @return a list from student record to the list of proctoring session records
     */
    private static Map<StudentRec, List<SessionRec>> gatherFiles(final File dataDir) {

        // Assemble a list of students and sessions and then sort by student name
        final Map<StudentRec, List<SessionRec>> map = new TreeMap<>();

        final File[] stuDirs = dataDir.listFiles();
        if (stuDirs != null) {
            for (final File stuDir : stuDirs) {
                if (stuDir.getName().endsWith(".png")) {
                    // Stock images served from proctoring data directory so site can find them
                    continue;
                }

                final File meta = new File(stuDir, "meta.json");
                final String str = FileLoader.loadFileAsString(meta, false);
                StudentRec stuRec;

                if (str == null) {
                    stuRec = new StudentRec(stuDir.getName(), "* ERROR", "No metadata");
                } else {
                    try {
                        // Log.info(str);

                        final Object parsed = JSONParser.parseJSON(str);
                        if (parsed instanceof final JSONObject obj) {
                            stuRec = new StudentRec(stuDir.getName(), obj.getStringProperty("last"),
                                    obj.getStringProperty("first"));
                        } else {
                            stuRec = new StudentRec(stuDir.getName(), "* ERROR",
                                    "Bad metadata - " + parsed.getClass().getName());
                        }

                    } catch (final ParsingException ex) {
                        Log.warning(ex);
                        stuRec = new StudentRec(stuDir.getName(), "* ERROR", "Bad metadata - " + ex.getMessage());
                    }

                }

                map.put(stuRec, scanSessions(stuRec, stuDir));
            }
        }

        return map;
    }

    /**
     * Scans for all sessions in a student directory, and creates a list of their IDs
     *
     * @param stuRec the student record
     * @param stuDir the student directory
     * @return the list if IDs
     */
    private static List<SessionRec> scanSessions(final StudentRec stuRec, final File stuDir) {

        final List<SessionRec> result;

        final File[] sessionDirs = stuDir.listFiles();

        if (sessionDirs == null) {
            result = new ArrayList<>(0);
        } else {
            result = new ArrayList<>(sessionDirs.length);

            for (final File sessionDir : sessionDirs) {
                final String id = sessionDir.getName();

                if ("meta.json".equals(id)) {
                    continue;
                }

                LocalDateTime dttime = null;
                if (id.length() >= 6) {
                    dttime = parseLocalDateTime(id);
                }

                if (dttime == null) {
                    Log.warning("Invalid session dir: ", sessionDir.getAbsolutePath());
                } else {
                    boolean isElevated = false;
                    boolean isReviewed = false;

                    if (new File(sessionDir, "elevated1.json").exists()) {
                        isElevated = true;
                        isReviewed = true;
                    } else if (new File(sessionDir, "review1.json").exists()) {
                        isReviewed = true;
                    }

                    result.add(new SessionRec(stuRec, id, dttime, isReviewed, isElevated));
                }
            }

            Collections.sort(result);
        }

        return result;
    }

    /**
     * Emits the list of students, with all the sessions under each student.
     *
     * @param map the map from student to the list of sessions for that student
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitFiles(final Map<StudentRec, List<SessionRec>> map, final HtmlBuilder htm) {

        // First view is organized by date, to make it easier to manage reviews

        htm.hr();

        htm.sDiv(null, "id='vw-by-date'");

        htm.sH(3).add("Proctoring Sessions, by date &nbsp; ")
                .add("<button class='btnsmall' onclick='show_by_student()'>Organize by student</button>").eH(3);

        final Map<LocalDate, List<SessionRec>> byDate = new TreeMap<>();

        for (final Map.Entry<StudentRec, List<SessionRec>> entry : map.entrySet()) {
            for (final SessionRec rec : entry.getValue()) {
                final LocalDate dt = rec.whenStarted.toLocalDate();
                final List<SessionRec> list = byDate.computeIfAbsent(dt, ld -> new ArrayList<>(10));
                list.add(rec);
            }
        }

        if (byDate.isEmpty()) {
            htm.sP().add("No proctoring sessions found").eP();
        } else {
            for (final Map.Entry<LocalDate, List<SessionRec>> entry : byDate.entrySet()) {
                Collections.sort(entry.getValue());

                // Count those in need of reviews
                int numWithoutReviews = 0;
                int numElevated = 0;
                int numReviewed = 0;
                for (final SessionRec rec : entry.getValue()) {
                    if (rec.elevated) {
                        ++numElevated;
                    } else if (rec.reviewed) {
                        ++numReviewed;
                    } else {
                        ++numWithoutReviews;
                    }
                }

                htm.addln("<details>");
                htm.add("<summary>", TemporalUtils.FMT_MDY.format(entry.getKey()));
                if (numElevated + numWithoutReviews > 0) {
                    htm.add(" (");
                    if (numElevated > 0) {
                        htm.add("<strong class='elevated'>").add(numElevated).add(" elevated</strong>");
                        if (numWithoutReviews > 0 && numReviewed > 0) {
                            htm.add(", ");
                        }
                    }
                    if (numWithoutReviews > 0) {
                        htm.add("<strong class='needsreview'>")
                                .add(numWithoutReviews).add(numWithoutReviews == 1 ? " needs" : " need")
                                .add(" review</strong>");
                        if (numReviewed > 0) {
                            htm.add(", ");
                        }
                    }
                    if (numReviewed > 0) {
                        htm.add("<strong class='reviewed'>").add(numReviewed).add(" reviewed</strong>");
                    }

                    htm.add(")");
                }
                htm.addln("</summary>");
                htm.sDiv("indent");

                for (final SessionRec rec : entry.getValue()) {

                    htm.addln("<details>");
                    htm.add("<summary>");
                    htm.add(TemporalUtils.FMT_HMS_A.format(rec.whenStarted.toLocalTime()));

                    if (rec.elevated) {
                        htm.add(" (<strong class='elevated'>elevated</strong>)");
                    } else if (rec.reviewed) {
                        htm.add(" (<strong class='reviewed'>reviewed</strong>)");
                    } else {
                        htm.add(" (<strong class='needsreview'>needs review</strong>)");
                    }

                    htm.add("</summary>");
                    htm.sDiv("indent");

                    htm.addln("<a class='ulink' href='details.html?stu=" + rec.stuRec.studentId + "&psid="
                            + rec.sessionId + "'>", rec.stuRec.toString(), "</a>").br();

                    htm.eDiv(); // indent
                    htm.addln("</details>");
                }

                htm.eDiv(); // indent
                htm.addln("</details>");
            }
        }

        htm.eDiv(); // view-by-date

        // Second view is organized by student, to make it easier to search

        htm.sDiv(null, "id='vw-by-stu'", "style='display:none;'");
        htm.sH(3).add("Proctoring Sessions, by student &nbsp; ")
                .add("<button class='btnsmall' onclick='show_by_date()'>Organize by date</button>").eH(3);

        if (map.isEmpty()) {
            htm.sP().add("No proctoring sessions found").eP();
        } else {
            for (final Map.Entry<StudentRec, List<SessionRec>> entry : map.entrySet()) {

                // Count those in need of reviews
                int numWithoutReviews = 0;
                int numElevated = 0;
                int numReviewed = 0;
                for (final SessionRec rec : entry.getValue()) {
                    if (rec.elevated) {
                        ++numElevated;
                    } else if (rec.reviewed) {
                        ++numReviewed;
                    } else {
                        ++numWithoutReviews;
                    }
                }

                htm.addln("<details>");
                htm.addln("<summary>", entry.getKey().toString());
                if (numElevated + numWithoutReviews > 0) {
                    htm.add(" (");
                    if (numElevated > 0) {
                        htm.add("<strong class='elevated'>").add(numElevated).add(" elevated</strong>");
                        if (numWithoutReviews > 0 || numReviewed > 0) {
                            htm.add(", ");
                        }
                    }
                    if (numWithoutReviews > 0) {
                        htm.add("<strong class='needsreview'>")
                                .add(numWithoutReviews).add(numWithoutReviews == 1 ? " needs" : " need")
                                .add(" review</strong>");
                        if (numReviewed > 0) {
                            htm.add(", ");
                        }
                    }
                    if (numReviewed > 0) {
                        htm.add("<strong class='revierwed'>").add(numReviewed).add(" reviewed</strong>");
                    }
                    htm.add(")");
                }
                htm.add("</summary>");

                htm.sDiv("indent");

                for (final SessionRec rec : entry.getValue()) {
                    htm.addln("<details>");

                    htm.add("<summary>");
                    htm.add(TemporalUtils.FMT_MDY_AT_HMS_A.format(rec.whenStarted));

                    if (rec.elevated) {
                        htm.add(" (<strong class='elevated'>elevated</strong>)");
                    } else if (rec.reviewed) {
                        htm.add(" (<strong class='reviewed'>reviewed</strong>)");
                    } else {
                        htm.add(" (<strong class='needsreview'>needs review</strong>)");
                    }

                    htm.add("</summary>");
                    htm.sDiv("indent");

                    htm.addln("<a class='ulink' href='details.html?stu=" + rec.stuRec.studentId + "&psid="
                            + rec.sessionId + "'>", rec.stuRec.toString(), "</a>").br();

                    htm.eDiv(); // indent
                    htm.addln("</details>");
                }

                htm.eDiv(); // indent
                htm.addln("</details>");
            }
        }

        htm.eDiv(); // view-by-date

        htm.addln("<script>");
        htm.addln("function show_by_date() {");
        htm.addln("  document.getElementById('vw-by-date').style.display='block';");
        htm.addln("  document.getElementById('vw-by-stu').style.display='none';");
        htm.addln("}");
        htm.addln("function show_by_student() {");
        htm.addln("  document.getElementById('vw-by-date').style.display='none';");
        htm.addln("  document.getElementById('vw-by-stu').style.display='block';");
        htm.addln("}");
        htm.addln("</script>");
    }

    /**
     * Converts a 6-character string into a LocalDateTime.
     *
     * @param str the string to parse
     * @return the result
     */
    private static LocalDateTime parseLocalDateTime(final String str) {

        LocalDateTime dttime = null;

        final String lex = MPSEndpoint.LEXICAL_CHARS;

        final int year = 1990 + lex.indexOf(str.charAt(0));
        final int month = lex.indexOf(str.charAt(1));
        final int day = lex.indexOf(str.charAt(2));
        final int hour = lex.indexOf(str.charAt(3));
        final int min = lex.indexOf(str.charAt(4));
        final int sec = lex.indexOf(str.charAt(5));

        if (year > 2000 && month > 0 && day > 0 && hour >= 0 && min >= 0 && sec >= 0) {
            try {
                dttime = LocalDateTime.of(year, month, day, hour, min, sec);
            } catch (final DateTimeException ex) {
                Log.warning(ex);
            }
        } else {
            Log.warning("Invalid date from PSID: ", str);
        }

        return dttime;
    }

    /**
     * A student record.
     */
    static final class StudentRec implements Comparable<StudentRec> {

        /** The student ID. */
        final String studentId;

        /** The last name. */
        final String lastName;

        /** The first name. */
        final String firstName;

        /**
         * Constructs a new {@code StudentRec}.
         *
         * @param theStudentId the student ID
         * @param theLastName  the first name
         * @param theFirstName the last name
         */
        StudentRec(final String theStudentId, final String theLastName, final String theFirstName) {

            this.studentId = theStudentId;
            this.lastName = theLastName;
            this.firstName = theFirstName;
        }

        /**
         * Generates the string representation of the student.
         *
         * @return a string of the form "Doe, John (898765432)".
         */
        @Override
        public String toString() {

            return this.lastName + ", " + this.firstName + " (" + this.studentId + ")";
        }

        /**
         * Compares two student records for order, where null values sort after non-nulls.
         *
         * @param o the object to which to compare
         * @return -1, 0, or 1 if this object is less than, equal to, or greater than "o"
         */
        @Override
        public int compareTo(final StudentRec o) {

            int result;

            if (this.lastName == null) {
                result = o.lastName == null ? 0 : 1;
            } else if (o.lastName == null) {
                result = -1;
            } else {
                result = this.lastName.compareTo(o.lastName);
            }

            if (result == 0) {
                if (this.firstName == null) {
                    result = o.firstName == null ? 0 : 1;
                } else if (o.firstName == null) {
                    result = -1;
                } else {
                    result = this.firstName.compareTo(o.firstName);
                }

                if (result == 0) {
                    if (this.studentId == null) {
                        result = o.studentId == null ? 0 : 1;
                    } else if (o.studentId == null) {
                        result = -1;
                    } else {
                        result = this.studentId.compareTo(o.studentId);
                    }
                }
            }

            return result;
        }
    }

    /**
     * A proctoring session record.
     */
    static final class SessionRec implements Comparable<SessionRec> {

        /** The student record. */
        final StudentRec stuRec;

        /** The session ID. */
        final String sessionId;

        /** The date/time the session started. */
        final LocalDateTime whenStarted;

        /** True if reviews exist on the record. */
        final boolean reviewed;

        /** True if review has been elevated. */
        final boolean elevated;

        /**
         * Constructs a new {@code SessionRec}.
         *
         * @param theStuRec      the student record
         * @param theSessionId   the proctoring session ID
         * @param theWhenStarted the date/time the session started
         * @param isReviewed     true if there is at least one review
         * @param isElevated     true if there is an elevated review
         */
        SessionRec(final StudentRec theStuRec, final String theSessionId, final LocalDateTime theWhenStarted,
                   final boolean isReviewed, final boolean isElevated) {

            this.stuRec = theStuRec;
            this.sessionId = theSessionId;
            this.whenStarted = theWhenStarted;
            this.reviewed = isReviewed;
            this.elevated = isElevated;
        }

        /**
         * Compares two session records for order, where null values sort after non-nulls.
         *
         * @param o the object to which to compare
         * @return -1, 0, or 1 if this object is less than, equal to, or greater than "o"
         */
        @Override
        public int compareTo(final SessionRec o) {

            final int result;

            if (this.whenStarted == null) {
                result = o.whenStarted == null ? 0 : 1;
            } else if (o.whenStarted == null) {
                result = -1;
            } else {
                result = this.whenStarted.compareTo(o.whenStarted);
            }

            return result;
        }
    }
}

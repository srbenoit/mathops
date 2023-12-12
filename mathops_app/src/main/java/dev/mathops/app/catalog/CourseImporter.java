package dev.mathops.app.catalog;

import dev.mathops.core.ESuccessFailure;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.xml.XmlEscaper;
import dev.mathops.db.old.rec.CatalogCourseRec;
import dev.mathops.db.type.CatalogCourseNumber;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * A crawler that imports course information from the catalog.
 */
enum CourseImporter {
    ;

    /** The URL of the top-level course page. */
    private static final String HOST = "https://catalog.colostate.edu";

    /** The user agent string to use in the request. */
    private static final String USER_AGENT = "Mozilla/5.0";

    /** The URL of the top-level course page. */
    private static final String TOP_PAGE_URL = HOST + "/general-catalog/courses-az/";

    /** A tag that marks the start of data in the top-level page. */
    private static final String TOP_CONTENT_START = "<div id=\"atozindex\">";

    /** A tag that marks the end of data in the top-level page. */
    private static final String TOP_CONTENT_END = "</div>";

    /** The prefix of a course category link in the top-level page. */
    private static final String TOP_ITEM_PREFIX = "<li><a href=\"/general-catalog/courses-az/";

    /** A separator within a course category link in the top-level page. */
    private static final String TOP_ITEM_SEP = "/\">";

    /** The suffix of a course category link in the top-level page. */
    private static final String TOP_ITEM_SUFFIX = "</a></li>";

    /** A tag that marks the start of data in a category page. */
    private static final String CAT_PAGE_START = "<div class=\"courseblock\">";

    /** A tag that marks the end of data in a category page. */
    private static final String CAT_PAGE_END = "<div id=\"footer\">";

    /** The prefix of a course block in a category page. */
    private static final String CAT_ITEM_PREFIX = "<div class=\"courseblock\">";

    /** The suffix of a course block in a category page. */
    private static final String CAT_ITEM_SUFFIX = "</div>";

    /** The prefix of the main information line for a course. */
    private static final String INFO_PREFIX = "<p class=\"courseblocktitle\"><strong>";

    /** The suffix of the main information line for a course. */
    private static final String INFO_SUFFIX = "</strong></p>";

    /** The prefix of the data field area for a course. */
    private static final String FIELDS_PREFIX = "<p class=\"courseblockdesc\">";

    /** The suffix of the data field area for a course. */
    private static final String FIELDS_SUFFIX = "</p>";

    /**
     * Imports courses.
     */
    static void importCourses() {

        final String courseListContent = loadPageHtml(TOP_PAGE_URL);

        final int pageStartLen = TOP_CONTENT_START.length();
        final int contentStart = courseListContent.indexOf(TOP_CONTENT_START);

        if (contentStart == -1) {
            Log.warning("Failed to find start of course category link region.");
        } else {
            final int contentEnd = courseListContent.indexOf(TOP_CONTENT_END, contentStart + pageStartLen);
            if (contentEnd == -1) {
                Log.warning("Failed to find end of course category link region.");
            } else {
                final String substring = courseListContent.substring(contentStart + pageStartLen, contentEnd);
                scanTopPageContent(substring);
            }
        }
    }

    /**
     * Scans the content portion of the top-level page for links to category sub-pages.
     *
     * @param substring the content portion of the top-level page to scan for links
     */
    private static void scanTopPageContent(final String substring) {

        final int prefixLen = TOP_ITEM_PREFIX.length();
        final int sepLen = TOP_ITEM_SEP.length();

        int index = substring.indexOf(TOP_ITEM_PREFIX);
        while (index != -1) {
            final int sep = substring.indexOf(TOP_ITEM_SEP, index + prefixLen);
            if (sep != -1) {
                final int end = substring.indexOf(TOP_ITEM_SUFFIX, sep + sepLen);
                if (end != -1) {
                    final String path = substring.substring(index + 13, sep);
                    final String title = substring.substring(sep + sepLen, end);

                    importCategoryPage(title, path);
                }
            }
            index = substring.indexOf(TOP_ITEM_PREFIX, index + prefixLen);
        }
    }

    /**
     * Imports a course category page.
     *
     * @param title the category title (which should end with the course prefix, in parentheses)
     * @param path  the path
     */
    private static void importCategoryPage(final String title, final String path) {

        Log.info("Course category: ", title);

        final String catPageUrl = HOST + path;
        final String catPageContent = loadPageHtml(catPageUrl);

        final int pageStartLen = CAT_PAGE_START.length();
        final int contentStart = catPageContent.indexOf(CAT_PAGE_START);

        if (contentStart == -1) {
            Log.warning("Failed to find start of course blocks region.");
        } else {
            final int contentEnd = catPageContent.indexOf(CAT_PAGE_END, contentStart + pageStartLen);
            if (contentEnd == -1) {
                Log.warning("Failed to find end of course blocks region.");
            } else {
                final String substring = catPageContent.substring(contentStart, contentEnd);
                scanCategoryPageContent(substring);
            }
        }
    }

    /**
     * Scans the content portion of a category page for blocks with course information
     *
     * @param substring the content portion of the top-level page to scan for links
     */
    private static void scanCategoryPageContent(final String substring) {

        final int prefixLen = CAT_ITEM_PREFIX.length();

        int index = substring.indexOf(CAT_ITEM_PREFIX);
        while (index != -1) {
            final int end = substring.indexOf(CAT_ITEM_SUFFIX, index + prefixLen);
            if (end == -1) {
                Log.warning("Failed to find end of course block.");
            } else {
                final String blockString = substring.substring(index + prefixLen, end);
                processCourseBlock(blockString);
            }

            index = substring.indexOf(CAT_ITEM_PREFIX, index + prefixLen);
        }
    }

    /**
     * Processes an information block for a single course.
     *
     * @param content the content
     */
    private static void processCourseBlock(final String content) {

        final int infoPrefixLen = INFO_PREFIX.length();
        final int infoStart = content.indexOf(INFO_PREFIX);
        if (infoStart == -1) {
            Log.warning("Failed to find start of information line.");
        } else {
            final int infoEnd = content.indexOf(INFO_SUFFIX, infoStart + infoPrefixLen);

            if (infoEnd == -1) {
                Log.warning("Failed to find end of information line.");
            } else {
                final int infoSuffixLen = INFO_SUFFIX.length();
                final int fieldsPrefixLen = INFO_PREFIX.length();
                final int fieldsStart = content.indexOf(FIELDS_PREFIX, infoEnd + infoSuffixLen);
                if (fieldsStart == -1) {
                    Log.warning("Failed to find start of data fields block.");
                } else {
                    final int fieldsEnd = content.indexOf(FIELDS_SUFFIX, fieldsStart + fieldsPrefixLen);

                    if (fieldsEnd == -1) {
                        Log.warning("Failed to find end of data fields block.");
                    } else {
                        final CatalogCourseRec record = new CatalogCourseRec();

                        final String infoLine = content.substring(infoStart + infoPrefixLen, infoEnd);
                        final String fields = content.substring(fieldsStart + fieldsPrefixLen, fieldsEnd);

                        if (processCourseInfoLine(infoLine, record) == ESuccessFailure.SUCCESS
                                && processCourseFields(fields, record) == ESuccessFailure.SUCCESS) {

                            // TODO:
                            // Log.fine(record.serializedString());
                        }
                    }
                }
            }
        }
    }

    /**
     * Processes the information line at the top of a course information block.
     *
     * @param infoLine the information line
     * @param record   the course record being populated
     * @return the result
     */
    private static ESuccessFailure processCourseInfoLine(final String infoLine, final CatalogCourseRec record) {

        // Info line format:
        // SOWK&#160;706&#160;&#160;Advanced Research Methods for Social Work&#160;&#160;Credits: 3&#160;(1-0-2)

        ESuccessFailure ok = ESuccessFailure.SUCCESS;

        int sep1Start = infoLine.indexOf("&#160;&#160;");
        final int sep1End;
        if (sep1Start == -1) {
            sep1Start = infoLine.indexOf("\u00A0\u00A0");
            if (sep1Start == -1) {
                Log.info("Processing ", infoLine);
                Log.warning("Failed to find end of course number.");
                ok = ESuccessFailure.FAILURE;
            }
            sep1End = sep1Start + 2;
        } else {
            sep1End = sep1Start + 12;
        }

        if (sep1Start != -1) {
            final String number = infoLine.substring(0, sep1Start);

            int sep2Start = number.indexOf("&#160;");
            if (sep2Start == -1) {
                sep2Start = number.indexOf('\u00A0');
            }

            if (sep2Start == -1) {
                Log.info("Processing ", infoLine);
                Log.warning("Failed to parse course prefix and number.");
                ok = ESuccessFailure.FAILURE;
            } else {
                record.courseNumber = CatalogCourseNumber.parse(number);

                int sep3Start = infoLine.indexOf("&#160;&#160;", sep1End);
                if (sep3Start == -1) {
                    sep3Start = infoLine.indexOf("\u00A0\u00A0", sep1End);
                    if (sep3Start == -1) {
                        Log.info("Processing ", infoLine);
                        Log.warning("Failed to find end of course title.");
                        ok = ESuccessFailure.FAILURE;
                    }
                }

                if (sep3Start != -1) {
                    record.title = XmlEscaper.unescape(infoLine.substring(sep1End, sep3Start));
                }
            }
        }

        return ok;
    }

    /**
     * Processes data on a single course
     *
     * @param fields the block with data fields
     * @param record the course record being populated
     */
    private static ESuccessFailure processCourseFields(final String fields, final CatalogCourseRec record) {

        ESuccessFailure ok = ESuccessFailure.SUCCESS;

        int startStrong = fields.indexOf("<strong>");
        while (ok == ESuccessFailure.SUCCESS && startStrong != -1) {
            final int endStrong = fields.indexOf("</strong>", startStrong + 8);

            if (endStrong == -1) {
                Log.info("Processing ", fields);
                Log.info("Record:    ", record.serializedString());
                Log.warning("Missing closure of 'strong' tag in course information block.");
                ok = ESuccessFailure.FAILURE;
            } else {
                final String fieldName = fields.substring(startStrong + 8, endStrong).trim();
                if (!fieldName.isBlank()) {
                    final int valueEnd = fields.indexOf("<br", endStrong + 9);
                    if (valueEnd == -1) {
                        Log.info("Processing ", fields);
                        Log.info("Record:    ", record.serializedString());
                        Log.warning("Missing line break at end of '", fieldName, " field in course information block.");
                        ok = ESuccessFailure.FAILURE;
                    } else {
                        final String value = fields.substring(endStrong + 9, valueEnd).trim();

                        switch (fieldName) {
                            case "Course Description:" -> record.description = value;
                            case "Prerequisite:" -> {
                                if (!"None.".equals(value)) {
//                                record.prerequisite = PrerequisiteFactory.parsePrerequisite(value);
                                }
                            }
                            case "Restriction:", "Restrictions:" -> record.restriction = value;
                            case "Registration Information:" -> record.registrationInfo = value;
                            case "Term Offered:", "Terms Offered:", "Grade Mode:", "Grade Modes:" -> {
                            }
                            // TODO: Parse into set of terms
                            // TODO: Parse into grade mode
                            case "Special Course Fee:", "Special Course Fees:" -> record.specialCourseFee = value;
                            case "Additional Information:" -> record.additionalInfo = value;
                            default -> Log.warning("Unexpected field name: ", fieldName);
                        }
                    }
                }
            }

            startStrong = fields.indexOf("<strong>", startStrong + 8);
        }

        return ok;
    }

    /**
     * Loads and returns the contents of a web page.
     *
     * @param url the page URL
     * @return the page content
     */
    private static String loadPageHtml(final String url) {

        final StringBuilder response = new StringBuilder(1000);

        try {
            final URL courseUrl = new URL(url);
            final HttpsURLConnection connection = (HttpsURLConnection) courseUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            final int responseCode = connection.getResponseCode();
//            Log.info("GET Response Code = " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (final BufferedReader in = new BufferedReader(new InputStreamReader(
                        connection.getInputStream(), StandardCharsets.UTF_8))) {

                    String inputLine = in.readLine();
                    while (inputLine != null) {
                        response.append(inputLine);
                        inputLine = in.readLine();
                    }
                }
            } else {
                Log.warning("GET request did not work.");
            }
        } catch (final MalformedURLException | ProtocolException ex) {
            Log.warning("Unable to connect to catalog course page.", ex);
        } catch (final IOException ex) {
            Log.warning("Unable to read from catalog course page.", ex);
        }

        return response.toString();
    }
}

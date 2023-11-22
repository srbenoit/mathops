package dev.mathops.app.catalog;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.type.CatalogCourseNumber;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A factory that can create prerequisite trees or parse them from the text representation stored in the university
 * catalog.
 */
enum PrerequisiteFactory {
    ;

    /** A pre-compiled regular expression for string splitting. */
    private static final Pattern OR_PATTERN = Pattern.compile(" or ");

    /** A pre-compiled regular expression for string splitting. */
    private static final Pattern AND_PATTERN = Pattern.compile(" and ");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber AB230 = new CatalogCourseNumber("AB", "230");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber AB270 = new CatalogCourseNumber("AB", "270");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber AEBA8155 = new CatalogCourseNumber("AEBA", "8155");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber AEIN8255 = new CatalogCourseNumber("AEIN", "8255");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber AEIN8210 = new CatalogCourseNumber("AEIN", "8210");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber AEIN8212 = new CatalogCourseNumber("AEIN", "8212");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber AEIN8213 = new CatalogCourseNumber("AEIN", "8213");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber ACT210 = new CatalogCourseNumber("ACT", "210");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber ACT211 = new CatalogCourseNumber("ACT", "211");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber ACT220 = new CatalogCourseNumber("ACT", "220");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber ACT311 = new CatalogCourseNumber("ACT", "311");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber ACT312 = new CatalogCourseNumber("ACT", "312");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber ACT321 = new CatalogCourseNumber("ACT", "321");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber ACT330 = new CatalogCourseNumber("ACT", "330");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber ACT350 = new CatalogCourseNumber("ACT", "350");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber AGED220 = new CatalogCourseNumber("AGED", "220");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber AGED330 = new CatalogCourseNumber("AGED", "330");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber AGED430 = new CatalogCourseNumber("AGED", "430");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber AREC202 = new CatalogCourseNumber("AREC", "202");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber AREC230 = new CatalogCourseNumber("AREC", "230");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber BSPM302 = new CatalogCourseNumber("BSPM", "302");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber BSPM361 = new CatalogCourseNumber("BSPM", "361");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber BUS150 = new CatalogCourseNumber("BUS", "150");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber BZ101 = new CatalogCourseNumber("BZ", "101");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber BZ104 = new CatalogCourseNumber("BZ", "104");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber BZ105 = new CatalogCourseNumber("BZ", "105");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber BZ110 = new CatalogCourseNumber("BZ", "110");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber BZ111 = new CatalogCourseNumber("BZ", "111");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber BZ120 = new CatalogCourseNumber("BZ", "120");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber BZ192 = new CatalogCourseNumber("BZ", "192");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber BZ346 = new CatalogCourseNumber("BZ", "346");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber BZ350 = new CatalogCourseNumber("BZ", "350");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber CHEM103 = new CatalogCourseNumber("CHEM", "103");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber CHEM104 = new CatalogCourseNumber("CHEM", "104");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber CHEM105 = new CatalogCourseNumber("CHEM", "105");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber CHEM107 = new CatalogCourseNumber("CHEM", "107");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber CHEM108 = new CatalogCourseNumber("CHEM", "108");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber CHEM111 = new CatalogCourseNumber("CHEM", "111");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber CHEM113 = new CatalogCourseNumber("CHEM", "113");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber CHEM117 = new CatalogCourseNumber("CHEM", "117");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber CHEM120 = new CatalogCourseNumber("CHEM", "120");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber CHEM192 = new CatalogCourseNumber("CHEM", "192");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber CIS120 = new CatalogCourseNumber("CIS", "120");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber CS110 = new CatalogCourseNumber("CS", "110");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber ECON202 = new CatalogCourseNumber("ECON", "202");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber ECON204 = new CatalogCourseNumber("ECON", "204");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber LIFE102 = new CatalogCourseNumber("LIFE", "102");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber LIFE103 = new CatalogCourseNumber("LIFE", "103");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber LIFE162 = new CatalogCourseNumber("LIFE", "162");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber MATH141 = new CatalogCourseNumber("MATH", "141");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber MATH155 = new CatalogCourseNumber("MATH", "155");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber MATH160 = new CatalogCourseNumber("MATH", "160");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber MIP250 = new CatalogCourseNumber("MIP", "250");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber MIP300 = new CatalogCourseNumber("MIP", "300");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber MIP303 = new CatalogCourseNumber("MIP", "303");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber SOCR330 = new CatalogCourseNumber("SOCR", "330");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber STAT201 = new CatalogCourseNumber("STAT", "201");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber STAT204 = new CatalogCourseNumber("STAT", "204");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber STAT301 = new CatalogCourseNumber("STAT", "301");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber STAT307 = new CatalogCourseNumber("STAT", "307");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber STAT311 = new CatalogCourseNumber("STAT", "311");

    /** A course used in prerequisites. */
    private static final CatalogCourseNumber STAT315 = new CatalogCourseNumber("STAT", "315");

    /**
     * Processes the text of a "Prerequisite" field.
     *
     * @param value the field value string
     * @return the parsed prerequisite if successful; {@code null} if not
     */
    private static AbstractPrerequisiteNode parsePrerequisite(final String value) {

        final String normalized = normalizePrerequisite(value);

        AbstractPrerequisiteNode result = parseSimple(normalized);

        if (result == null) {
            result = parseSimpleOr(normalized);

            if (result == null) {
                result = parseSimpleAnd(normalized);

                if (result == null) {
                    result = parseCompound(normalized);

                    if (result == null) {
                        result = parseSpecialized(normalized);

                        if (result == null) {
                            Log.fine("CANT PARSE :", normalized);
//                        } else {
//                            Log.fine("SPECIALIZED:", normalized);
                        }
//                    } else {
//                        Log.fine("COMPOUND:", normalized);
                    }
//                } else {
//                    Log.fine("SIMPLE AND :", normalized);
                }
//            } else {
//                Log.fine("SIMPLE OR :", normalized);
            }
//        } else {
//            Log.fine("SIMPLE :", normalized);
        }

        return result;
    }

    /**
     * Normalizes a prerequisite string.
     *
     * @param value the string to normalize
     * @return the normalized string
     */
    private static String normalizePrerequisite(final String value) {

        final String replaced1 = value.replace('\u00A0', ' ');
        final String replaced2 = replaced1.replace("&#160;", CoreConstants.SPC);

        final int len = replaced2.length();
        final HtmlBuilder normalized = new HtmlBuilder(len);

        int start = 0;
        int hrefOpen = replaced2.indexOf("<a href=", start);
        while (hrefOpen != -1) {
            if (hrefOpen > start) {
                normalized.add(replaced2.substring(start, hrefOpen));
            }

            final int openTagEnd = replaced2.indexOf("\">", hrefOpen + 8);
            if (openTagEnd == -1) {
                Log.warning("Could not find end of link open tag in prerequisite string");
                start = len;
                normalized.reset();
                break;
            }

            final int closeTag = replaced2.indexOf("</a>", openTagEnd + 2);
            if (closeTag == -1) {
                Log.warning("Could not find link close tag in prerequisite string");
                start = len;
                normalized.reset();
                break;
            }

            normalized.add(replaced2.substring(openTagEnd + 2, closeTag));

            start = closeTag + 4;
            hrefOpen = replaced2.indexOf("<a href=", start);
        }

        if (start < len) {
            normalized.add(replaced2.substring(start));
        }

        return normalized.toString();
    }

    /**
     * Attempts to parse a normalized prerequisite string as a "simple" description with a single course.  For example:
     *
     * <ul>
     *     <li>ABC 123.</li>
     *     <li>ABC 123 with a minimum grade of B-.</li>
     *     <li>ABC 123, may be taken concurrently.</li>
     *     <li>ABC 123 with a minimum grade of B-, may be taken concurrently.</li>
     * </ul>
     * <p>
     * The period at the end is optional.
     *
     * @param normalized the string to attempt to parse
     * @return the parsed {@code SimplePrerequisiteNode} if successful; null if not
     */
    private static SimplePrerequisiteNode parseSimple(final String normalized) {

        SimplePrerequisiteNode result = null;

        // Make sure any '.' character found is at the end of the string
        int dot = normalized.indexOf('.');
        if (dot == -1) {
            dot = normalized.length();
        }

        if (dot >= (normalized.length() - 1)) {
            final int spc = normalized.indexOf(' ');
            if (spc > 0 && spc < 5) {
                final String prefix = normalized.substring(0, spc);
                final int nextSpc = normalized.indexOf(' ', spc + 1);
                final int nextComma = normalized.indexOf(", may be taken concurrently", spc + 1);

                if (nextComma == -1) {
                    if (nextSpc == -1) {
                        if (spc + 8 > dot) {
                            // "ABC 123."
                            final String number = normalized.substring(spc + 1, dot);
                            final CatalogCourseNumber courseNumber = new CatalogCourseNumber(prefix, number);
                            result = new SimplePrerequisiteNode(courseNumber);
//                        } else {
//                            Log.info("NOT SIMPLE H [", normalized, "]");
                        }
                    } else {
                        final int phrase = normalized.indexOf(" with a minimum grade of ", spc + 1);
                        if (phrase == nextSpc) {
                            // Course number is followed by minimum grade
                            if (dot < phrase + 29) {
                                // "ABC 123 with a minimum grade of B-, may be taken concurrently."
                                final String number = normalized.substring(spc + 1, phrase);
                                final String minGrade = normalized.substring(phrase + 25, dot);
                                final CatalogCourseNumber courseNumber = new CatalogCourseNumber(prefix, number);
                                result = new SimplePrerequisiteNode(courseNumber, minGrade);
//                            } else {
//                                Log.info("NOT SIMPLE G [", normalized, "]");
                            }
//                        } else {
//                            Log.info("NOT SIMPLE F [", normalized, "]");
                        }
                    }
                } else if (nextComma + 27 == dot) {
                    // Within this block, the course may be taken concurrently, and "nextComma" can be treated as
                    // the end of the remaining content
                    if (nextSpc < nextComma) {
                        final int phrase = normalized.indexOf(" with a minimum grade of ", spc + 1);
                        if (phrase == nextSpc) {
                            // Course number is followed by minimum grade (and there is a comma after)

                            if (nextComma < phrase + 29) {
                                // "ABC 123 with a minimum grade of B-, may be taken concurrently."
                                final String number = normalized.substring(spc + 1, phrase);
                                final String minGrade = normalized.substring(phrase + 25, nextComma);
                                final CatalogCourseNumber courseNumber = new CatalogCourseNumber(prefix, number);
                                result = new SimplePrerequisiteNode(courseNumber, minGrade, true);
//                            } else {
//                                Log.info("NOT SIMPLE E [", normalized, "]");
                            }
//                        } else {
//                            Log.info("NOT SIMPLE D [", normalized, "]");
                        }
                    } else {
                        // "ABC 123, may be taken concurrently."
                        final String number = normalized.substring(spc + 1, nextComma);
                        final CatalogCourseNumber courseNumber = new CatalogCourseNumber(prefix, number);
                        result = new SimplePrerequisiteNode(courseNumber, true);
                    }
//                } else {
//                    Log.info("NOT SIMPLE C [", normalized, "]");
                }
//            } else {
//                Log.info("NOT SIMPLE B [", normalized, "]");
            }
//        } else {
//            Log.info("NOT SIMPLE A [", normalized, "]");
        }

        return result;
    }

    /**
     * Attempts to parse a normalized prerequisite string as a combination of two or more simple strings separated by "
     * or ".  For example,
     *
     * <ul>
     *     <li>ABC 123 or ABC 124.</li>
     *     <li>ABC 123 with a minimum grade of B- or ABC 124</li>
     *     <li>ABC 123, may be taken concurrently or ABC 123, may be taken concurrently.</li>
     * </ul>
     *
     * @param normalized the string to attempt to parse
     * @return the parsed prerequisite if successful; {@code null} if not
     */
    private static OrPrerequisiteNode parseSimpleOr(final CharSequence normalized) {

        OrPrerequisiteNode result = null;

        final String[] segments = OR_PATTERN.split(normalized);
        final int count = segments.length;

        if (count > 1) {
            final SimplePrerequisiteNode[] parsed = new SimplePrerequisiteNode[count];

            boolean ok = true;

            for (int i = 0; i < count - 1; ++i) {
                parsed[i] = parseSimple(segments[i]);
                if (parsed[i] == null) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                parsed[count - 1] = parseSimple(segments[count - 1]);
                if (parsed[count - 1] == null) {
                    ok = false;
                }
            }

            if (ok) {
                result = new OrPrerequisiteNode(parsed);
//                Log.info("SIMPLE OR [", normalized, "] (", Integer.toString(record.prerequisites.size()), ")");
            }
        }

        return result;
    }

    /**
     * Attempts to parse a normalized prerequisite string as a combination of two or more simple strings separated by "
     * and ".  For example,
     *
     * <ul>
     *     <li>ABC 123 and ABC 124.</li>
     *     <li>ABC 123 with a minimum grade of B- and ABC 124</li>
     *     <li>ABC 123, may be taken concurrently and ABC 123, may be taken concurrently.</li>
     * </ul>
     *
     * @param normalized the string to attempt to parse
     * @return the parsed prerequisite if successful; {@code null} if not
     */
    private static AndPrerequisiteNode parseSimpleAnd(final CharSequence normalized) {

        AndPrerequisiteNode result = null;

        final String[] segments = AND_PATTERN.split(normalized);
        final int count = segments.length;

        if (count > 1) {
            final SimplePrerequisiteNode[] parsed = new SimplePrerequisiteNode[count];

            boolean ok = true;

            for (int i = 0; i < count - 1; ++i) {
                parsed[i] = parseSimple(segments[i]);
                if (parsed[i] == null) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                parsed[count - 1] = parseSimple(segments[count - 1]);
                if (parsed[count - 1] == null) {
                    ok = false;
                }
            }

            if (ok) {
                result = new AndPrerequisiteNode(parsed);
//                Log.info("SIMPLE AND [", normalized, "] (", Integer.toString(record.prerequisites.size()), ")");
            }
        }

        return result;
    }

    /**
     * Attempts to parse a normalized prerequisite string as a "compound" prerequisite statement involving parenthesized
     * sub-expressions separated by " and" or " or "./
     *
     * <ul>
     *     <li>(ABC 123 and ABC 124) or (ABC 456).</li>
     * </ul>
     *
     * @param normalized the string to attempt to parse
     * @return the parsed prerequisite if successful; {@code null} if not
     */
    private static AbstractPrerequisiteNode parseCompound(final String normalized) {

        final int len = normalized.length();

        // The following will store the normalized string with all parenthesized expressions replaced by "~".
        // So "(ABC 123 and ABC 124) or (ABC 456)." would be converted to "~ or ~." here.  As we go, parsing
        // parenthesized sub-expressions, we accumulate them in an "inner" list.
        final HtmlBuilder extracted = new HtmlBuilder(20);
        final List<AbstractPrerequisiteNode> innerNodes = new ArrayList<>(5);

        int pos = 0;
        int contentStart = 0;
        int openParen = 0;
        int parenLevel = 0;
        while (pos < len) {
            final int charAt = normalized.charAt(pos);

            if (charAt == '(') {
                if (parenLevel == 0) {
                    openParen = pos;
                    if (pos - 1 > contentStart) {
                        extracted.add(normalized.substring(contentStart, pos));
                    }
                }
                ++parenLevel;
            } else if (charAt == ')' && parenLevel > 0) {
                --parenLevel;
                if (parenLevel == 0) {
                    // Reached the end of a sub-expression
                    contentStart = pos + 1;
                    final String innerString = normalized.substring(openParen + 1, pos);

                    final AbstractPrerequisiteNode innerNode = parsePrerequisite(innerString);
                    if (innerNode == null) {
                        break;
                    }
                    innerNodes.add(innerNode);
                    extracted.add('~');
                }
            }

            ++pos;
        }

        AbstractPrerequisiteNode result = null;

        if (pos == len && !innerNodes.isEmpty()) {
            // We were able to parse all sub-expressions

            final String extractedStr = extracted.toString().trim();
            final int numInner = innerNodes.size();

            if (numInner == 1) {
                if ("~".equals(extractedStr)) {
                    result = innerNodes.get(0);
                }
            } else if (numInner == 2) {
                if ("~ or ~".equals(extractedStr)) {
                    result = new OrPrerequisiteNode(innerNodes.get(0), innerNodes.get(1));
                } else if ("~ and ~".equals(extractedStr)) {
                    result = new AndPrerequisiteNode(innerNodes.get(0), innerNodes.get(1));
                }
            } else if (numInner == 3) {
                if ("~ or ~ or ~".equals(extractedStr)) {
                    result = new OrPrerequisiteNode(innerNodes.get(0), innerNodes.get(1), innerNodes.get(2));
                } else if ("~ and ~ and ~".equals(extractedStr)) {
                    result = new AndPrerequisiteNode(innerNodes.get(0), innerNodes.get(1), innerNodes.get(2));
                }
            } else if (numInner == 4) {
                if ("~ or ~ or ~ or ~".equals(extractedStr)) {
                    result = new OrPrerequisiteNode(innerNodes.get(0), innerNodes.get(1), innerNodes.get(2),
                            innerNodes.get(3));
                } else if ("~ and ~ and ~ and ~".equals(extractedStr)) {
                    result = new AndPrerequisiteNode(innerNodes.get(0), innerNodes.get(1), innerNodes.get(2),
                            innerNodes.get(3));
                }
            } else if (numInner == 5) {
                if ("~ or ~ or ~ or ~ or ~".equals(extractedStr)) {
                    result = new OrPrerequisiteNode(innerNodes.get(0), innerNodes.get(1), innerNodes.get(2),
                            innerNodes.get(3), innerNodes.get(4));
                } else if ("~ and ~ and ~ and ~ and ~".equals(extractedStr)) {
                    result = new AndPrerequisiteNode(innerNodes.get(0), innerNodes.get(1), innerNodes.get(2),
                            innerNodes.get(3), innerNodes.get(4));
                }
            }
        }

        return result;
    }

    /**
     * Parses a "specialized" prerequisite string that cannot be recognized by simpler algorithms.
     *
     * @param normalized the string to attempt to parse
     * @return the parsed prerequisite if successful; {@code null} if not
     */
    private static AbstractPrerequisiteNode parseSpecialized(final String normalized) {

        AbstractPrerequisiteNode result = null;

        if (("AEIN 8255 with a minimum grade of S++ - at least 1 course, may be taken concurrently.")
                .equals(normalized)) {
            result = new SimplePrerequisiteNode(AEIN8255, "S++", true);

        } else if (("AEBA 8155 with a minimum grade of S++ - at least 1 course, may be taken concurrently.")
                .equals(normalized)) {
            result = new SimplePrerequisiteNode(AEBA8155, "S", true);

        } else if (("ACT 311 and ACT 312 or ACT 311 and ACT 321 or ACT 311 and ACT 330 or ACT 311 and "
                + "ACT 350 or ACT 312 and ACT 321 or ACT 312 and ACT 330 or ACT 312 and ACT 350 or ACT 321 and "
                + "ACT 330 or ACT 321 and ACT 350 or ACT 330 and ACT 350.").equals(normalized)) {
            final SimplePrerequisiteNode act311 = new SimplePrerequisiteNode(ACT311);
            final SimplePrerequisiteNode act312 = new SimplePrerequisiteNode(ACT312);
            final SimplePrerequisiteNode act321 = new SimplePrerequisiteNode(ACT321);
            final SimplePrerequisiteNode act330 = new SimplePrerequisiteNode(ACT330);
            final SimplePrerequisiteNode act350 = new SimplePrerequisiteNode(ACT350);
            final AndPrerequisiteNode and1 = new AndPrerequisiteNode(act311, act312);
            final AndPrerequisiteNode and2 = new AndPrerequisiteNode(act311, act321);
            final AndPrerequisiteNode and3 = new AndPrerequisiteNode(act311, act330);
            final AndPrerequisiteNode and4 = new AndPrerequisiteNode(act311, act350);
            final AndPrerequisiteNode and5 = new AndPrerequisiteNode(act312, act321);
            final AndPrerequisiteNode and6 = new AndPrerequisiteNode(act312, act330);
            final AndPrerequisiteNode and7 = new AndPrerequisiteNode(act312, act350);
            final AndPrerequisiteNode and8 = new AndPrerequisiteNode(act321, act330);
            final AndPrerequisiteNode and9 = new AndPrerequisiteNode(act321, act350);
            final AndPrerequisiteNode and10 = new AndPrerequisiteNode(act330, act350);
            result = new OrPrerequisiteNode(and1, and2, and3, and4, and5, and6, and7, and8, and9, and10);

        } else if (("BZ 100 to 199 - at least 3 credits or CHEM 100 to 199 - at least 3 credits.").equals(normalized)) {
            final SimplePrerequisiteNode bz101 = new SimplePrerequisiteNode(BZ101);
            final SimplePrerequisiteNode bz104 = new SimplePrerequisiteNode(BZ104);
            final SimplePrerequisiteNode bz105 = new SimplePrerequisiteNode(BZ105);
            final SimplePrerequisiteNode bz110 = new SimplePrerequisiteNode(BZ110);
            final SimplePrerequisiteNode bz111 = new SimplePrerequisiteNode(BZ111);
            final SimplePrerequisiteNode bz120 = new SimplePrerequisiteNode(BZ120);
            final SimplePrerequisiteNode bz192 = new SimplePrerequisiteNode(BZ192);
            final SimplePrerequisiteNode chem103 = new SimplePrerequisiteNode(CHEM103);
            final SimplePrerequisiteNode chem104 = new SimplePrerequisiteNode(CHEM104);
            final SimplePrerequisiteNode chem105 = new SimplePrerequisiteNode(CHEM105);
            final SimplePrerequisiteNode chem107 = new SimplePrerequisiteNode(CHEM107);
            final SimplePrerequisiteNode chem108 = new SimplePrerequisiteNode(CHEM108);
            final SimplePrerequisiteNode chem111 = new SimplePrerequisiteNode(CHEM111);
            final SimplePrerequisiteNode chem113 = new SimplePrerequisiteNode(CHEM113);
            final SimplePrerequisiteNode chem117 = new SimplePrerequisiteNode(CHEM117);
            final SimplePrerequisiteNode chem120 = new SimplePrerequisiteNode(CHEM120);
            final SimplePrerequisiteNode chem192 = new SimplePrerequisiteNode(CHEM192);
            final AndPrerequisiteNode and1 = new AndPrerequisiteNode(bz101);
            final AndPrerequisiteNode and2 = new AndPrerequisiteNode(bz104);
            final AndPrerequisiteNode and3 = new AndPrerequisiteNode(bz105, bz111, bz192);
            final AndPrerequisiteNode and4 = new AndPrerequisiteNode(bz110);
            final AndPrerequisiteNode and5 = new AndPrerequisiteNode(bz120);
            final AndPrerequisiteNode and6 = new AndPrerequisiteNode(chem103);
            final AndPrerequisiteNode and7 = new AndPrerequisiteNode(chem104, chem105);
            final AndPrerequisiteNode and8 = new AndPrerequisiteNode(chem107);
            final AndPrerequisiteNode and9 = new AndPrerequisiteNode(chem105, chem108);
            final AndPrerequisiteNode and10 = new AndPrerequisiteNode(chem111);
            final AndPrerequisiteNode and11 = new AndPrerequisiteNode(chem113);
            final AndPrerequisiteNode and12 = new AndPrerequisiteNode(chem117);
            final AndPrerequisiteNode and13 = new AndPrerequisiteNode(chem120);
            final AndPrerequisiteNode and14 = new AndPrerequisiteNode(chem104, chem192);
            final AndPrerequisiteNode and15 = new AndPrerequisiteNode(chem192, chem108);
            result = new OrPrerequisiteNode(and1, and2, and3, and4, and5, and6, and7, and8, and9, and10, and11, and12,
                    and13, and14, and15);

        } else if (("LIFE 100 to 199 - at least 3 credits.").equals(normalized)) {

            result = new CreditCountPrerequisiteNode(3, null, LIFE102, LIFE103, LIFE162);

        }

        return result;
    }
}

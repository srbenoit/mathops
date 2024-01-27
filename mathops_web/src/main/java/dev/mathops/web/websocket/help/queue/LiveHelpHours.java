package dev.mathops.web.websocket.help.queue;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Hours during which live tutoring is available.
 *
 * <p>
 * Data is presented as a list of date ranges, and within each date range, a list of weekdays and time ranges. For
 * example, from May 15 to August 9, Monday-Friday 9AM to 5PM, and Saturday from 2PM to 6PM.
 */
final class LiveHelpHours {

    /** The list of date ranges. */
    private final List<DateRange> dateRanges;

    /**
     * Constructs a new {@code LiveHelpHours}.
     */
    LiveHelpHours() {

        this.dateRanges = new ArrayList<>(2);

        // BEGIN TEST DATA: Add some test data to check sending of the hours to the tutor site

        final DateRange range1 = new DateRange(LocalDate.of(2020, 5, 15), LocalDate.of(2020, 8, 9));
        this.dateRanges.add(range1);

        final Block weekdays1 = new Block();
        weekdays1.addWeekday(DayOfWeek.MONDAY);
        weekdays1.addWeekday(DayOfWeek.TUESDAY);
        weekdays1.addWeekday(DayOfWeek.WEDNESDAY);
        weekdays1.addWeekday(DayOfWeek.THURSDAY);
        weekdays1.addWeekday(DayOfWeek.FRIDAY);

        weekdays1.addTimeRange(new TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0)));
        range1.addBlock(weekdays1);

        // END TEST DATA
    }

//    /**
//     * Tests whether live help is available at a specific date/time.
//     *
//     * @param now the date/time to test
//     * @return true if hours are available {@code now}
//     */
//    public boolean isOpen(final LocalDateTime now) {
//
//        boolean open = false;
//
//        for (final DateRange range : this.dateRanges) {
//            if (range.isOpen(now)) {
//                open = true;
//                break;
//            }
//        }
//
//        return open;
//    }

    /**
     * Generates the serialized JSON representation of the hours.
     *
     * <pre>
     * { "hours": {
     *   "dateRanges": [
     *     { "start": "May 15, 2020",
     *       "end": "July 8, 2020",
     *       "blocks": [
     *         { "weekdays": "-MTWRF-",
     *           "timeRanges": [
     *             {"start": "8:00 AM", "end": "5:00 PM" }
     *             ... additional time ranges ...
     *           ]
     *         },
     *         ... additional blocks ...
     *       ]
     *     },
     *     ... additional date ranges ...
     *   ]
     * }}
     * </pre>
     *
     * @return the JSON serialized representation
     */
    String toJSON() {

        final HtmlBuilder htm = new HtmlBuilder(200);

        htm.add("{ \"hours\": {");
        htm.add("  \"dateRanges\": [");

        final int numDates = this.dateRanges.size();
        for (int i = 0; i < numDates; ++i) {
            final DateRange dr = this.dateRanges.get(i);
            dr.appendJSON(htm);
            if (i + 1 < numDates) {
                htm.addln(CoreConstants.COMMA_CHAR);
            } else {
                htm.addln();
            }
        }

        htm.addln("  ]");
        htm.addln("}}");

        return htm.toString();
    }

    /**
     * A date range.
     */
    private static final class DateRange {

        /** The start date. */
        private final LocalDate start;

        /** The end date. */
        private final LocalDate end;

        /** The blocks. */
        private final List<Block> blocks;

        /**
         * The date range.
         *
         * @param theStart the start date
         * @param theEnd   the end date
         */
        private DateRange(final LocalDate theStart, final LocalDate theEnd) {

            this.start = theStart;
            this.end = theEnd;
            this.blocks = new ArrayList<>(2);
        }

        /**
         * Adds a block with time ranges for a set of weekdays.
         *
         * @param theBlock the block
         */
        void addBlock(final Block theBlock) {

            this.blocks.add(theBlock);
        }

//        /**
//         * Tests whether a date range includes a specific date/time.
//         *
//         * @param now the date/time to test
//         * @return true if {@code now} is included
//         */
//        public boolean isOpen(final LocalDateTime now) {
//
//            boolean open;
//
//            final LocalDate nowDate = now.toLocalDate();
//
//            if (nowDate.isBefore(this.start) || nowDate.isAfter(this.end)) {
//                open = false;
//            } else {
//                open = false;
//
//                for (Block block : this.blocks) {
//                    if (block.isOpen(now)) {
//                        open = true;
//                        break;
//                    }
//                }
//            }
//
//            return open;
//        }

        /**
         * Generates the serialized JSON representation of the date range.
         *
         * <pre>
         *     { "start": "May 15, 2020",
         *       "end": "July 8, 2020",
         *       "blocks": [
         *         { "weekdays": "-MTWRF-",
         *           "timeRanges": [
         *            {"start": "8:00 AM", "end": "5:00 PM" }
         *             ... additional time ranges ...
         *           ]
         *         },
         *         ... additional blocks ...
         *       ]
         *     }
         * </pre>
         *
         * @param htm the {@code HtmlBuilder} to which to append
         */
        void appendJSON(final HtmlBuilder htm) {

            htm.add("    { \"start\": \"");
            appendDate(this.start, htm);
            htm.addln("\",");

            htm.add("      \"end\": \"");
            appendDate(this.end, htm);
            htm.addln("\",");

            htm.addln("      \"blocks\": [");

            final int numBlocks = this.blocks.size();
            for (int i = 0; i < numBlocks; ++i) {
                final Block block = this.blocks.get(i);

                block.appendJSON(htm);

                if (i + 1 < numBlocks) {
                    htm.addln(CoreConstants.COMMA_CHAR);
                } else {
                    htm.addln();
                }
            }

            htm.addln("      ]");
            htm.addln("    }");
        }

        /**
         * Appends a formatted date to a {@code HtmlBuilder}.
         *
         * @param dt  the date
         * @param htm the {@code HtmlBuilder} to which to append
         */
        private static void appendDate(final TemporalAccessor dt, final HtmlBuilder htm) {

            htm.add(TemporalUtils.FMT_MDY.format(dt));

            //
            // htm.add(dt.getYear()).add('-');
            // if (dt.getMonthValue() < 10) {
            // htm.add('0');
            // }
            // htm.add(dt.getMonthValue()).add('-');
            // if (dt.getDayOfMonth() < 10) {
            // htm.add('0');
            // }
            // htm.add(dt.getDayOfMonth());
        }
    }

    /**
     * A block of weekdays with time ranges, like "Monday - Friday, 9am to 5pm and 6pm to 8pm"
     */
    private static final class Block {

        /** The weekdays (bitwise combination of Weekday). */
        private final EnumSet<DayOfWeek> weekdays;

        /** The time ranges. */
        private final List<TimeRange> timeRanges;

        /**
         * Constructs a new {@code Block}.
         */
        private Block() {

            this.weekdays = EnumSet.noneOf(DayOfWeek.class);
            this.timeRanges = new ArrayList<>(2);
        }

        /**
         * Adds a weekday.
         *
         * @param day the weekday
         */
        void addWeekday(final DayOfWeek day) {

            this.weekdays.add(day);
        }

        /**
         * Adds a time range.
         *
         * @param range the time range
         */
        void addTimeRange(final TimeRange range) {

            this.timeRanges.add(range);
        }

//        /**
//         * Tests whether a block includes a specific date/time.
//         *
//         * @param now the date/time to test
//         * @return true if {@code now} is included
//         */
//        public boolean isOpen(final LocalDateTime now) {
//
//            boolean open;
//
//            if (this.weekdays.contains(now.getDayOfWeek())) {
//                open = false;
//                for (TimeRange range : this.timeRanges) {
//                    if (range.isOpen(now)) {
//                        open = true;
//                        break;
//                    }
//                }
//            } else {
//                open = false;
//            }
//
//            return open;
//        }

        /**
         * Generates the serialized JSON representation of the block.
         *
         * <pre>
         *         { "weekdays": "-MTWRF-",
         *           "timeRanges": [
         *             {"start": "8:00 AM", "end": "5:00 PM" }
         *             ... additional time ranges ...
         *           ]
         *         }
         * </pre>
         *
         * @param htm the {@code HtmlBuilder} to which to append
         */
        void appendJSON(final HtmlBuilder htm) {

            htm.add("        { \"weekdays\": \"");
            htm.add(this.weekdays.contains(DayOfWeek.SUNDAY) ? 'S' : '-');
            htm.add(this.weekdays.contains(DayOfWeek.MONDAY) ? 'M' : '-');
            htm.add(this.weekdays.contains(DayOfWeek.TUESDAY) ? 'T' : '-');
            htm.add(this.weekdays.contains(DayOfWeek.WEDNESDAY) ? 'W' : '-');
            htm.add(this.weekdays.contains(DayOfWeek.THURSDAY) ? 'R' : '-');
            htm.add(this.weekdays.contains(DayOfWeek.FRIDAY) ? 'F' : '-');
            htm.add(this.weekdays.contains(DayOfWeek.SATURDAY) ? 'S' : '-');
            htm.addln("\",");

            htm.addln("          \"timeRanges\": [");

            final int numTimes = this.timeRanges.size();
            for (int i = 0; i < numTimes; ++i) {
                final TimeRange range = this.timeRanges.get(i);

                range.appendJSON(htm);

                if (i + 1 < numTimes) {
                    htm.addln(CoreConstants.COMMA_CHAR);
                } else {
                    htm.addln();
                }
            }

            htm.addln("          ]");
            htm.addln("        }");
        }
    }

    /**
     * A time range.
     */
    private static final class TimeRange {

        /** The start time. */
        private final LocalTime start;

        /** The end time. */
        private final LocalTime end;

        /**
         * The time range.
         *
         * @param theStart the start time
         * @param theEnd   the end time
         */
        private TimeRange(final LocalTime theStart, final LocalTime theEnd) {

            this.start = theStart;
            this.end = theEnd;
        }

        /**
         * Generates the serialized JSON representation of the block.
         *
         * <pre>
         *             {"start": "8:00 AM", "end": "5:00 PM" }
         * </pre>
         *
         * @param htm the {@code HtmlBuilder} to which to append
         */
        void appendJSON(final HtmlBuilder htm) {

            htm.add("            { \"start\": \"");
            appendTime(this.start, htm);
            htm.add("\", \"end\": \"");
            appendTime(this.end, htm);
            htm.addln("\" }");
        }

//        /**
//         * Tests whether a block includes a specific date/time.
//         *
//         * @param now the date/time to test
//         * @return true if {@code now} is included
//         */
//        public boolean isOpen(final LocalDateTime now) {
//
//            boolean open;
//
//            final LocalTime nowTime = now.toLocalTime();
//
//            if (nowTime.isBefore(this.start) || nowTime.isAfter(this.end)) {
//                open = false;
//            } else {
//                open = true;
//            }
//
//            return open;
//        }

        /**
         * Appends a formatted time to a {@code HtmlBuilder}.
         *
         * @param tm  the time
         * @param htm the {@code HtmlBuilder} to which to append
         */
        private static void appendTime(final LocalTime tm, final HtmlBuilder htm) {

            if (tm.getHour() < 12) {
                htm.add(tm.getHour());
            } else {
                htm.add(tm.getHour() - 12);
            }

            htm.add(':');
            if (tm.getMinute() < 10) {
                htm.add('0');
            }
            htm.add(tm.getMinute());

            if (tm.getHour() < 12) {
                htm.add(" AM");
            } else {
                htm.add(" PM");
            }
        }
    }
}

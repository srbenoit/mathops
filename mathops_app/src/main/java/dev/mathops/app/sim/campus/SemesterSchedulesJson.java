package dev.mathops.app.sim.campus;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.json.JSONObject;
import dev.mathops.commons.parser.json.JSONParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

/**
 * A utility to load and store semester schedule data in JSON format.
 *
 * <pre>
 * [
 *     {"id":"15_week_standard",
 *      "weeksOfClass":15,
 *      "startTimes":[ {"day":"MONDAY", "time":"8:00" },
 *                     {"day":"TUESDAY", "time":"8:00" }, ... ],
 *      "endTimes":[ {"day":"MONDAY", "time":"17:00" },
 *                   {"day":"TUESDAY", "time":"17:00" }, ... ],
 *      "scheduleTypes":[ {"day":"MONDAY", "type":"CLASS_50_PASSING_10" },
 *                        {"day":"TUESDAY", "type":"CLASS_75_PASSING_15" }, ... ]
 *     },
 *     ...
 * ]
 * </pre>
 */
public enum SemesterSchedulesJson {
    ;

    /** The filename of the file that stores semester schedules data. */
    private static final String FILENAME = "semester-schedules.json";

    /**
     * Attempts to load campus room data from a data directory.
     *
     * @param dataDir the data directory
     * @param target  a list to which to add all records found
     */
    public static void load(final File dataDir, final Collection<? super SemesterSchedule> target) {

        final File dataFile = new File(dataDir, FILENAME);
        if (dataFile.exists()) {
            final String path = dataFile.getAbsolutePath();

            final String json = FileLoader.loadFileAsString(dataFile, false);
            if (json == null) {
                Log.warning("Unable to read ", path);
            } else {
                try {
                    final Object parsed = JSONParser.parseJSON(json);
                    if (parsed instanceof final Object[] array) {
                        for (final Object obj : array) {
                            if (obj instanceof final JSONObject jsonObj) {
                                final String id = jsonObj.getStringProperty("id");
                                final Number cap = jsonObj.getNumberProperty("capacity");

                                if (id == null) {
                                    Log.warning("Object in ", path, " did not have 'id' attribute.");
                                } else if (cap == null) {
                                    Log.warning("Object in ", path, " did not have 'capacity' attribute.");
                                } else {
                                    final SemesterSchedule schedule = new SemesterSchedule(id, cap.intValue());
                                    target.add(schedule);
                                }
                            } else {
                                Log.warning("Array element within ", path, " was not a JSON object.");
                            }
                        }
                    } else {
                        Log.warning("JSON content of ", path, " was not an array.");
                    }
                } catch (final ParsingException ex) {
                    Log.warning("Failed to read ", path, ex);
                }
            }
        }
    }

    /**
     * Attempts to store semester schedule data to a data directory.
     *
     * @param dataDir the data directory
     * @param source  a list of semester schedules to write
     */
    public static void store(final File dataDir, final List<SemesterSchedule> source) {

        final HtmlBuilder builder = buildJson(source);
        final String json = builder.toString();

        final File dataFile = new File(dataDir, FILENAME);

        try (final FileWriter writer = new FileWriter(dataFile, StandardCharsets.UTF_8)) {
            writer.write(json);
        } catch (final IOException ex) {
            final String path = dataFile.getAbsolutePath();
            Log.warning("Failed to write ", path, ex);
        }
    }

    /**
     * Builds the JSON representation of a list of campus rooms.
     *
     * @param source the list of campus rooms
     * @return the JSON
     */
    private static HtmlBuilder buildJson(final List<SemesterSchedule> source) {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.addln("[");
        final int count = source.size();
        for (int i = 0; i < count; ++i) {
            final SemesterSchedule sched = source.get(i);

            final String id = sched.getId();
            final int weeks = sched.getWeeksOfClass();

            builder.addln("    {\"id\":\"", id, "\",");

            builder.addln("     \"weeksOfClass\":\"");
            builder.add(weeks);
            builder.addln("\",");

            builder.addln("     \"startTimes\":[ ");
            boolean comma = false;
            for (final DayOfWeek day : DayOfWeek.values()) {
                final LocalTime start = sched.getStartTime(day);
                if (start != null) {
                    if (comma) {
                        builder.addln(",");
                        builder.add("                    ");
                    }
                    final String dayName = day.name();
                    builder.add("{\"day\":\"", dayName, "\", \"time\":", start, "\" }");
                    comma = true;
                }
            }
            builder.addln("],");

            builder.addln("     \"endTimes\":[ ");
            comma = false;
            for (final DayOfWeek day : DayOfWeek.values()) {
                final LocalTime end = sched.getEndTime(day);
                if (end != null) {
                    if (comma) {
                        builder.addln(",");
                        builder.add("                  ");
                    }
                    final String dayName = day.name();
                    builder.add("{\"day\":\"", dayName, "\", \"time\":", end, "\" }");
                    comma = true;
                }
            }
            builder.addln("],");

            builder.addln("     \"scheduleTypes\":[ ");
            comma = false;
            for (final DayOfWeek day : DayOfWeek.values()) {
                final EDailyScheduleType type = sched.getScheduleType(day);
                if (type != null) {
                    if (comma) {
                        builder.addln(",");
                        builder.add("                       ");
                    }
                    final String dayName = day.name();
                    final String typeName = type.name();
                    builder.add("{\"day\":\"", dayName, "\", \"type\":", typeName, "\" }");
                    comma = true;
                }
            }
            builder.addln("]");

            if (i < count - 1) {
                builder.addln("    },");
            } else {
                builder.addln("    }");
            }
        }
        builder.addln("]");

        return builder;
    }
}

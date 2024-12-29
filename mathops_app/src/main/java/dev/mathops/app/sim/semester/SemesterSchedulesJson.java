package dev.mathops.app.sim.semester;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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
                                final Number weeks = jsonObj.getNumberProperty("weeksOfClass");

                                if (id == null) {
                                    Log.warning("Object in ", path, " did not have 'id' attribute.");
                                } else if (weeks == null) {
                                    Log.warning("Object in ", path, " did not have 'weeksOfClass' attribute.");
                                } else {
                                    final SemesterSchedule schedule = new SemesterSchedule(id, weeks.intValue());

                                    if (isDailyDataValid(jsonObj, schedule)) {
                                        target.add(schedule);
                                    }
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
     * Examines the "daily" configuration data in a JSON object to ensure it is valid.  If the data is valid, it is
     * stored in a supploed schedule object.
     *
     * @param jsonObj  the JSON object to examine
     * @param schedule the schedule to which to add daily configuration data if valid
     * @return true if data was valid
     */
    private static boolean isDailyDataValid(final JSONObject jsonObj, final SemesterSchedule schedule) {

        final Object startTimes = jsonObj.getProperty("startTimes");
        final Object endTimes = jsonObj.getProperty("endTimes");
        final Object scheduleTypes = jsonObj.getProperty("scheduleTypes");

        boolean valid = false;

        if (startTimes instanceof final Object[] startTimesArray
            && endTimes instanceof final Object[] endTimesArray
            && scheduleTypes instanceof final Object[] scheduleTypesArray) {

            final int count = startTimesArray.length;

            if (count == endTimesArray.length && count == scheduleTypesArray.length) {

                final Map<DayOfWeek, LocalTime> parsedStartTimes = new EnumMap<>(DayOfWeek.class);
                final Map<DayOfWeek, LocalTime> parsedEndTimes = new EnumMap<>(DayOfWeek.class);
                final Map<DayOfWeek, EDailyScheduleType> parsedScheduleTypes = new EnumMap<>(DayOfWeek.class);

                valid = true;
                for (int i = 0; i < count; ++i) {

                    if (startTimesArray[i] instanceof final JSONObject startTimeObj
                        && endTimesArray[i] instanceof final JSONObject endTimeObj
                        && scheduleTypesArray[i] instanceof final JSONObject scheduleTypeObj) {

                        final String startDayStr = startTimeObj.getStringProperty("day");
                        final String endDayStr = endTimeObj.getStringProperty("day");
                        final String scheduleDayStr = scheduleTypeObj.getStringProperty("day");

                        final String startTimeStr = startTimeObj.getStringProperty("time");
                        final String endTimeStr = endTimeObj.getStringProperty("time");
                        final String scheduleTypeStr = scheduleTypeObj.getStringProperty("type");

                        try {
                            final DayOfWeek startDay = DayOfWeek.valueOf(startDayStr);
                            final LocalTime startTime = LocalTime.parse(startTimeStr);
                            parsedStartTimes.put(startDay, startTime);

                            final DayOfWeek endDay = DayOfWeek.valueOf(endDayStr);
                            final LocalTime endTime = LocalTime.parse(endTimeStr);
                            parsedEndTimes.put(endDay, endTime);

                            final DayOfWeek scheduleDay = DayOfWeek.valueOf(scheduleDayStr);
                            final EDailyScheduleType scheduleType = EDailyScheduleType.valueOf(scheduleTypeStr);
                            parsedScheduleTypes.put(scheduleDay, scheduleType);

                        } catch (final IllegalArgumentException ex) {
                            Log.warning("Invalid entry in startTimes, endTimes, or scheduleType array.");
                            valid = false;
                            break;
                        }
                    } else {
                        Log.warning("Entry in startTimes, endTimes, or scheduleType array is not JSON Object.");
                        valid = false;
                        break;
                    }
                }

                if (valid) {
                    for (final DayOfWeek day : DayOfWeek.values()) {
                        final LocalTime start = parsedStartTimes.get(day);
                        final LocalTime end = parsedEndTimes.get(day);
                        final EDailyScheduleType type = parsedScheduleTypes.get(day);

                        if (start != null && end != null && type != null) {
                            schedule.addDayOfWeek(day, start, end, type);
                        }
                    }
                }
            } else {
                Log.warning("StartTimes, endTimes, and scheduleTypes arrays in schedule JSON object not same length.");
            }
        } else {
            Log.warning("Did not find startTimes, endTimes, and scheduleTypes array in schedule JSON object.");
        }

        return valid;
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

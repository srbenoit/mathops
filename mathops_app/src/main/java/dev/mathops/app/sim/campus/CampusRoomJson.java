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
import java.util.Collection;
import java.util.List;

/**
 * A utility to load and store campus room data in JSON format.
 *
 * <p>
 * Room data is stored in a JSON data file with the following format:
 *
 * <pre>
 * [
 *     {"id":"classroom1", "capacity":40},
 *     {"id":"lab1", "capacity":26}
 * ]
 * </pre>
 */
public enum CampusRoomJson {
    ;

    /** The filename of the file that stores campus room data. */
    private static final String FILENAME = "campus-rooms.json";

    /**
     * Attempts to load campus room data from a data directory.
     *
     * @param dataDir the data directory
     * @param target  a list to which to add all records found
     */
    public static void load(final File dataDir, final Collection<? super CampusRoom> target) {

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
                                    final CampusRoom room = new CampusRoom(id, cap.intValue());
                                    target.add(room);
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
     * Attempts to store campus room data to a data directory.
     *
     * @param dataDir the data directory
     * @param source  a list of campus rooms to write
     */
    public static void store(final File dataDir, final List<CampusRoom> source) {

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
    private static HtmlBuilder buildJson(List<CampusRoom> source) {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.addln("[");
        final int count = source.size();
        for (int i = 0; i < count; ++i) {
            final CampusRoom room = source.get(i);

            final String id = room.getId();
            final int cap = room.getCapacity();

            builder.add("    {\"id\":\"", id, "\", \"capacity\":");
            builder.add(cap);
            if (i < count - 1) {
                builder.addln("},");
            } else {
                builder.addln("}");
            }
        }
        builder.addln("]");
        return builder;
    }
}

package dev.mathops.app.sim.rooms;

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

/**
 * A utility to load and store campus room data in JSON format.  Data is loaded from (and stored to) a file named
 * 'campus-rooms.json' in a specified data directory.
 *
 * <pre>
 * [
 *     {
 *         "config":"config name 1",
 *         "rooms":[
 *             {"id":"classroom1", "capacity":40},
 *             {"id":"lab1", "capacity":26}
 *         ]
 *     }
 * ]
 * </pre>
 */
enum RoomSetJson {
    ;

    /** The filename of the file that stores campus room data. */
    private static final String FILENAME = "campus-rooms.json";

    /**
     * Attempts to load campus room data from a data directory.
     *
     * @param dataDir the data directory
     * @param target  a list to which to add all records found
     */
    static void load(final File dataDir, final RoomSetsListModel target) {

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
                                final RoomSet config = parseConfig(jsonObj, path, target);
                                if (config != null) {
                                    if (!target.canAddElement(config)) {
                                        Log.warning("Duplicate room set name in ", path, ".");
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
     * Attempts to parse a {@code CampusRoomConfig} from a JSON object.
     *
     * @param jsonObj    the JSON object
     * @param path       the path (for error logging)
     * @param owningList the room sets list that will own room sets
     * @return the parsed {@code CampusRoomConfig}; null on any error
     */
    private static RoomSet parseConfig(final JSONObject jsonObj, final String path,
                                       final RoomSetsListModel owningList) {

        RoomSet result = null;

        final String name = jsonObj.getStringProperty("config");
        final Object parsed = jsonObj.getProperty("rooms");

        if (name == null) {
            Log.warning("Object in ", path, " did not have 'config' attribute.");
        } else if (parsed == null) {
            Log.warning("Object in ", path, " did not have 'rooms' attribute.");
        } else {
            if (parsed instanceof final Object[] array) {
                result = new RoomSet(name, owningList);
                final RoomSetTableModel rooms = result.getTableModel();

                for (final Object obj : array) {
                    if (obj instanceof final JSONObject inner) {
                        final Room room = parseRoom(inner, path);
                        if (room != null) {
                            rooms.add(room);
                        }
                    } else {
                        Log.warning("Array element within ", path, " was not a JSON object.");
                    }
                }
            } else {
                Log.warning("JSON content of ", path, " was not an array.");
            }
        }

        return result;
    }

    /**
     * Attempts to parse a {@code CampusRoom} from a JSON object.
     *
     * @param jsonObj the JSON object
     * @param path    the path (for error logging)
     * @return the parsed {@code CampusRoom}; null on any error
     */
    private static Room parseRoom(final JSONObject jsonObj, final String path) {

        Room result = null;

        final String id = jsonObj.getStringProperty("id");
        final Number cap = jsonObj.getNumberProperty("capacity");

        if (id == null) {
            Log.warning("Object in ", path, " did not have 'id' attribute.");
        } else if (cap == null) {
            Log.warning("Object in ", path, " did not have 'capacity' attribute.");
        } else {
            final int capValue = cap.intValue();
            result = new Room(id, capValue);
        }

        return result;
    }

    /**
     * Attempts to store campus room data to a data directory.
     *
     * @param dataDir the data directory
     * @param source  a list of campus rooms to write
     */
    static void store(final File dataDir, final RoomSetsListModel source) {

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
    private static HtmlBuilder buildJson(final RoomSetsListModel source) {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.addln("[");
        final int numConfigs = source.getSize();
        for (int i = 0; i < numConfigs; ++i) {
            final RoomSet config = source.getElementAt(i);

            final String name = config.getName();
            final RoomSetTableModel rooms = config.getTableModel();

            builder.addln("    {");
            builder.addln("        \"config\":\"", name, "\",");
            builder.addln("        \"rooms\":[");

            final int numRooms = rooms.getRowCount();
            for (int j = 0; j < numRooms; ++j) {
                final Room room = rooms.getRow(j);
                final String id = room.getId();
                final int cap = room.getCapacity();

                builder.add("            {\"id\":\"", id, "\", \"capacity\":");
                builder.add(cap);
                builder.addln(j < numRooms - 1 ? "}," : "}");
            }
            builder.addln("        ]");

            builder.addln(i < numConfigs - 1 ? "    }," : "    }");
        }
        builder.addln("]");

        return builder;
    }
}

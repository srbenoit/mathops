package dev.mathops.db.course;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;

import java.io.File;

/**
 * Utility methods used to load JSON objects from files.
 */
public enum JSONUtils {
    ;

    /**
     * Attempts to load a file with a specified name and parse it into a JSON object.
     *
     * @param dir      the directory in which to find the file
     * @param filename the filename, like "metadata.json"
     * @return the parsed JSON object; {@code null} if the file could not be read or parsed
     */
    public static JSONObject loadJsonFile(final File dir, final String filename) {

        final File metadataFile = new File(dir, filename);

        JSONObject result = null;

        final String fileData = FileLoader.loadFileAsString(metadataFile, true);

        if (fileData == null) {
            final String metaPath = metadataFile.getAbsolutePath();
            Log.warning("Unable to load ", metaPath);
        } else {
            try {
                final Object parsedObj = JSONParser.parseJSON(fileData);

                if (parsedObj instanceof final JSONObject parsedJson) {
                    result = parsedJson;
                } else {
                    final String metaPath = metadataFile.getAbsolutePath();
                    Log.warning("Top-level object in ", metaPath, " is not JSON Object.");
                }
            } catch (final ParsingException ex) {
                final String metaPath = metadataFile.getAbsolutePath();
                Log.warning("Failed to parse " + metaPath, ex);
            }
        }

        return result;
    }
}

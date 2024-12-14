package dev.mathops.app.database.dbimport;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;

import java.io.File;

/**
 * A container for data loaded from an import directory.  This includes the table definitions from {db-name}.sql as well
 * as all table data.  This class validates the data on construction, and fails to construct if the data is not valid.
 */
class DataToImport {

    /**
     * Constructs a new {@code DataToImport}.  This attempts to load and interpret the files in the export directory and
     * stores the interpreted data in a format that can be used to construct the PostgreSQL database.
     *
     * @param exportDir the directory containing the export
     * @throws IllegalArgumentException if the SQL file could not be loaded or interpreted or the associated data files
     *                                  could not be found, loaded, or interpreted
     */
    DataToImport(final File exportDir) throws IllegalArgumentException {

        final File[] allFiles = exportDir.listFiles();

        if (allFiles == null) {
            throw new IllegalArgumentException("The import directory contains no files!");
        }

        File sqlFile = null;

        for (final File file : allFiles) {
            final String name = file.getName();
            if (name.endsWith(".sql")) {
                if (sqlFile == null) {
                    sqlFile = file;
                } else {
                    throw new IllegalArgumentException("Multiple {db-name}.sql files found in import directory.");
                }
            } else if (!name.endsWith(".unl")) {
                throw new IllegalArgumentException("Unexpected file in import directory: " + name);
            }
        }

        if (sqlFile == null) {
            throw new IllegalArgumentException("Missing {db-name}.sql file in import directory.");
        }

        loadSqlFile(sqlFile);
        loadDataFiles(exportDir);
    }

    /**
     * Attempts to load the SQL file.
     *
     * @param sqlFile the SQL file
     * @throws IllegalArgumentException if the file could not be loaded or interpreted
     */
    private void loadSqlFile(final File sqlFile) throws IllegalArgumentException {

        final String sqlFileName = sqlFile.getName();

        final String[] lines = FileLoader.loadFileAsLines(sqlFile, false);
        if (lines == null) {
            throw new IllegalArgumentException("Unable to load " + sqlFileName);
        }

        String dbName = null;

        final int numLines = lines.length;
        int index = 0;
        while (index < numLines) {
            final String line = lines[index];

            if (line.startsWith("{ DATABASE ")) {
                final String sub = line.substring(11).trim();
                final int nameEnd = sub.indexOf((int) CoreConstants.SPC_CHAR);
                if (nameEnd == -1) {
                    final String lineStr = makeLineNumberText(index, sqlFileName);
                    throw new IllegalArgumentException("Unable to interpret database name" + lineStr);
                }
                dbName = sub.substring(0, nameEnd);
                ++index;
            } else if (line.startsWith("{ TABLE ")) {
                if (dbName == null) {
                    final String lineStr = makeLineNumberText(index, sqlFileName);
                    throw new IllegalArgumentException("TABLE definition before DATABASE definition" + lineStr);
                }

                index = processTableDefinition(sqlFileName, dbName, lines, index);
            } else {
                // Ignore this line...
                ++index;
            }
        }
    }

    /**
     * Attempts to process a table definition in the SQL file.
     *
     * @param dbName    the database name
     * @param lines     the set of lines from the SQL file
     * @param lineIndex the index of the line where the table definition starts
     * @return the index of the first line after the table definition
     * @throws IllegalArgumentException if the table definition could not be interpreted
     */
    private int processTableDefinition(final String sqlFileName, final String dbName, final String[] lines,
                                       final int lineIndex) throws IllegalArgumentException {

        final String tableLine = lines[lineIndex];
        final String sub = tableLine.substring(8);
        final int nameEnd = sub.indexOf((int) CoreConstants.SPC_CHAR);
        if (nameEnd == -1) {
            final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
            throw new IllegalArgumentException("Unable to interpret table nam " + lineStr);
        }

        final String qualifiedName = sub.substring(0, nameEnd);
        final String prefix = "\"" + dbName + "\".";
        final int prefixLen = prefix.length();
        final String tableName = qualifiedName.startsWith(prefix) ? qualifiedName.substring(prefixLen) : qualifiedName;

        Log.info("Found table name: ", tableName);

        final int numLines = lines.length;
        int result = numLines;

        TableDefinition tableDef = null;

        int index = lineIndex + 1;
        int phase = 0;
        while (index < numLines) {
            final String line = lines[index];

            if (line.isBlank()) {
                ++index;
                continue;
            }

            final String trimmed = line.trim();

            if (trimmed.startsWith("{ unload file name = ")) {
                tableDef = makeTableDefinition(tableName, trimmed);
            } else if (trimmed.startsWith("create table ")) {
                if (tableDef == null) {
                    final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
                    throw new IllegalArgumentException("'table create' line found before unload file data" + lineStr);
                }
                if (phase > 0) {
                    final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
                    throw new IllegalArgumentException("Unexpected 'table create'" + lineStr);
                }

                final String expect = "create table " + qualifiedName;
                if (trimmed.equals(expect)) {
                    phase = 1;
                } else {
                    final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
                    throw new IllegalArgumentException("Unexpected 'table create'" + lineStr + " (found '" + trimmed
                                                       + "', expected '" + expect + "')");
                }
            } else if ("(".equals(trimmed)) {
                if (phase == 1) {
                    // Phase 2 is "gathering field definitions"
                    phase = 2;
                } else {
                    final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
                    throw new IllegalArgumentException("Unexpected '('" + lineStr);
                }
            } else if (trimmed.startsWith(")")) {
                if (phase == 2) {
                    phase = 3;
                    result = index + 1;
                    break;
                }

                final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
                throw new IllegalArgumentException("Unexpected ')'" + lineStr);
            } else if (phase == 2) {
                final FieldDefinition field = new FieldDefinition(trimmed, lineIndex, sqlFileName);
                tableDef.fields.add(field);
            } else {
                final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
                throw new IllegalArgumentException("Unexpected" + lineStr);
            }

            ++index;
        }

        if (phase != 3) {
            final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
            throw new IllegalArgumentException("Unable to interpret fields in table definition starting" + lineStr);
        }

        if (tableDef.fields.isEmpty()) {
            final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
            throw new IllegalArgumentException("No fields defined in table definition starting" + lineStr);
        }

        return result;
    }

    /**
     * Attempts to interpret an "unload file" line from the database SQL file and construct a table definition object
     * with the table name, unload file name, and expected number of rows.
     *
     * @param tableName the table name
     * @param line      the line to interpret
     * @return the table definition object
     * @throws IllegalArgumentException if the table definition could not be interpreted
     */
    private TableDefinition makeTableDefinition(final String tableName, final String line) {

        TableDefinition tableDef = null;

        final String unloadSub = line.substring(21);
        final int unloadNameEnd = unloadSub.indexOf((int) CoreConstants.SPC_CHAR);
        if (unloadNameEnd == -1) {
            throw new IllegalArgumentException("Unable to interpret unload file name from [" + line + "].");
        }
        final String unloadFile = unloadSub.substring(0, unloadNameEnd);
        final int next = unloadSub.indexOf(" number of rows = ", unloadNameEnd);
        if (next == -1) {
            throw new IllegalArgumentException("Unable to find number of rows from [" + line + "].");
        }
        final String numRowsSub = unloadSub.substring(next + 18);
        final int numRowsEnd = numRowsSub.indexOf((int) CoreConstants.SPC_CHAR);
        if (numRowsEnd == -1) {
            throw new IllegalArgumentException("Unable to find end of number of rows from [" + line + "].");
        }

        final String numRowsStr = numRowsSub.substring(0, numRowsEnd);
        try {
            final int numRows = Integer.parseInt(numRowsStr);
            if (numRows < 0) {
                throw new IllegalArgumentException("Invalid number of rows in [" + line + "].");
            }

            tableDef = new TableDefinition(tableName, unloadFile, numRows);
            Log.info("    Unload file '", unloadFile, "' should have ", Integer.toString(numRows), " rows");
        } catch (final NumberFormatException ex) {
            throw new IllegalArgumentException("Unable to interpret number of rows from [" + line + "].", ex);
        }

        return tableDef;
    }

    /**
     * Attempts to load the data files associated with the tables from the SQL file.
     *
     * @param exportDir the directory containing the export
     * @throws IllegalArgumentException if the file could not be loaded or interpreted
     */
    private void loadDataFiles(final File exportDir) throws IllegalArgumentException {

    }

    /**
     * A utility method that makes a string of the form "in line 10 of filename.sql" for use in error messages.
     *
     * @param lineIndex the 0-based line index
     * @param filename  the filename
     * @return the message (which has a 1-based line number rather than 0-based index)
     */
     static String makeLineNumberText(final int lineIndex, final String filename) {

        final String numberString = Integer.toString(lineIndex + 1);

        return " in line [" + numberString + "] of " + filename;
    }
}

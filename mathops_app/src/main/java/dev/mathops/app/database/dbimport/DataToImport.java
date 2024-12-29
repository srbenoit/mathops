package dev.mathops.app.database.dbimport;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A container for data loaded from an import directory.  This includes the table definitions from {db-name}.sql as well
 * as all table data.  This class validates the data on construction, and fails to construct if the data is not valid.
 */
class DataToImport {

    /** The default delimiter. */
    private static final int DEFAULT_DELIMITER = '|';

    /** Character used to escape delimiters (and itself) in strings. */
    private static final int ESCAPE_CHAR = '\\';

    /** The delimiter for unload files. */
    private final int delimiter;

    /** The list of table definitions. */
    final List<TableDefinition> tables;

    /** Synonyms from export file (map from synonym name to table name). */
    final Map<String, String> synonyms;

    /** The list of index definitions. */
    final List<IndexDefinition> indexes;

    /** The list of unique index definitions. */
    final List<UniqueIndexDefinition> uniqueIndexes;

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

        this.tables = new ArrayList<>(100);
        this.synonyms = new HashMap<>(5);
        this.indexes = new ArrayList<>(20);
        this.uniqueIndexes = new ArrayList<>(20);

        this.delimiter = loadSqlFile(sqlFile);
        loadDataFiles(exportDir);
    }

    /**
     * Attempts to load the SQL file.
     *
     * @param sqlFile the SQL file
     * @return the delimiter that will be used in unload files
     * @throws IllegalArgumentException if the file could not be loaded or interpreted
     */
    private int loadSqlFile(final File sqlFile) throws IllegalArgumentException {

        final String sqlFileName = sqlFile.getName();

        final String[] lines = FileLoader.loadFileAsLines(sqlFile, false);
        if (lines == null) {
            throw new IllegalArgumentException("Unable to load " + sqlFileName);
        }

        String dbName = null;
        int theDelimiter = DEFAULT_DELIMITER;

        final int numLines = lines.length;
        int index = 0;
        while (index < numLines) {
            final String line = lines[index];

            if (line.startsWith("{ DATABASE ")) {
                final String sub = line.substring(11).trim();
                final int nameEnd = sub.indexOf(CoreConstants.SPC_CHAR);
                if (nameEnd == -1) {
                    final String lineStr = makeLineNumberText(index, sqlFileName);
                    throw new IllegalArgumentException("Unable to interpret database name" + lineStr);
                }
                dbName = sub.substring(0, nameEnd);

                final int delimiterIndex = line.indexOf("delimiter ", nameEnd);
                if (delimiterIndex > 0) {
                    final String postDelim = line.substring(delimiterIndex + 10).trim();
                    if (!postDelim.isEmpty()) {
                        theDelimiter = postDelim.charAt(0);
                    }
                }

                ++index;
            } else if (line.startsWith("{ TABLE ")) {
                index = processTableDefinition(sqlFileName, dbName, lines, index);
            } else if (line.startsWith("create synonym ")) {
                processSynonymDefinition(sqlFileName, dbName, line, index);
                ++index;
            } else if (line.startsWith("create index ")) {
                index = processIndexDefinition(sqlFileName, dbName, lines, index);
            } else if (line.startsWith("create cluster index ")) {
                index = processClusterIndexDefinition(sqlFileName, dbName, lines, index);
            } else if (line.startsWith("create unique index ")) {
                index = processUniqueIndexDefinition(sqlFileName, dbName, lines, index);
            } else if (line.startsWith("create unique cluster index ")) {
                index = processUniqueClusterIndexDefinition(sqlFileName, dbName, lines, index);
            } else if (line.startsWith("create ")) {
                final String lineStr = makeLineNumberText(index, sqlFileName);
                throw new IllegalArgumentException("Unsupported create" + lineStr);
            } else {
                // Ignore this line...
                ++index;
            }
        }

        return theDelimiter;
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

        if (dbName == null) {
            final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
            throw new IllegalArgumentException("TABLE definition before DATABASE definition" + lineStr);
        }

        final String tableLine = lines[lineIndex];
        final String sub = tableLine.substring(8);
        final int nameEnd = sub.indexOf(CoreConstants.SPC_CHAR);
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
                    final String lineStr = makeLineNumberText(index, sqlFileName);
                    throw new IllegalArgumentException("'table create' line found before unload file data" + lineStr);
                }
                if (phase > 0) {
                    final String lineStr = makeLineNumberText(index, sqlFileName);
                    throw new IllegalArgumentException("Unexpected 'table create'" + lineStr);
                }

                final String expect = "create table " + qualifiedName;
                if (trimmed.equals(expect)) {
                    phase = 1;
                } else {
                    final String lineStr = makeLineNumberText(index, sqlFileName);
                    throw new IllegalArgumentException("Unexpected 'table create'" + lineStr + " (found '" + trimmed
                                                       + "', expected '" + expect + "')");
                }
            } else if ("(".equals(trimmed)) {
                if (phase == 1) {
                    // Phase 2 is "gathering field definitions"
                    phase = 2;
                } else {
                    final String lineStr = makeLineNumberText(index, sqlFileName);
                    throw new IllegalArgumentException("Unexpected '('" + lineStr);
                }
            } else if (trimmed.startsWith(")")) {
                if (phase == 2) {
                    phase = 3;
                    result = index + 1;
                    break;
                }

                final String lineStr = makeLineNumberText(index, sqlFileName);
                throw new IllegalArgumentException("Unexpected ')'" + lineStr);
            } else if (phase == 2) {
                final FieldDefinition field = new FieldDefinition(trimmed, index, sqlFileName);
                tableDef.fields.add(field);
            } else {
                final String lineStr = makeLineNumberText(index, sqlFileName);
                throw new IllegalArgumentException("Unexpected" + lineStr);
            }

            ++index;
        }

        if (phase != 3) {
            final String lineStr = makeLineNumberText(index, sqlFileName);
            throw new IllegalArgumentException("Unable to interpret fields in table definition starting" + lineStr);
        }

        if (tableDef.fields.isEmpty()) {
            final String lineStr = makeLineNumberText(index, sqlFileName);
            throw new IllegalArgumentException("No fields defined in table definition starting" + lineStr);
        }

        tableDef.computeLongestFieldName();
        this.tables.add(tableDef);

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
        final int unloadNameEnd = unloadSub.indexOf(CoreConstants.SPC_CHAR);
        if (unloadNameEnd == -1) {
            throw new IllegalArgumentException("Unable to interpret unload file name from [" + line + "].");
        }
        final String unloadFile = unloadSub.substring(0, unloadNameEnd);
        final int next = unloadSub.indexOf(" number of rows = ", unloadNameEnd);
        if (next == -1) {
            throw new IllegalArgumentException("Unable to find number of rows from [" + line + "].");
        }
        final String numRowsSub = unloadSub.substring(next + 18);
        final int numRowsEnd = numRowsSub.indexOf(CoreConstants.SPC_CHAR);
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
        } catch (final NumberFormatException ex) {
            throw new IllegalArgumentException("Unable to interpret number of rows from [" + line + "].", ex);
        }

        return tableDef;
    }

    /**
     * Attempts to process a synonym definition in the SQL file.
     *
     * @param dbName    the database name
     * @param line      the line from the SQL file with the definition
     * @param lineIndex the index of the line with the synonym definition
     * @throws IllegalArgumentException if the synonym definition could not be interpreted
     */
    private void processSynonymDefinition(final String sqlFileName, final String dbName, final String line,
                                          final int lineIndex) throws IllegalArgumentException {

        if (dbName == null) {
            final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
            throw new IllegalArgumentException("Synonym definition before database defined" + lineStr);
        }

        final int lineLen = line.length();

        if ((int) line.charAt(lineLen - 1) != ';') {
            final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
            throw new IllegalArgumentException("Synonym definition without trailing semicolon" + lineStr);
        }

        final int forIndex = line.indexOf(" for ");
        if (forIndex == -1) {
            final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
            throw new IllegalArgumentException("Unable to interpret synonym definition" + lineStr);
        }

        final String synonymQualified = line.substring(15, forIndex);
        final String tableQualified = line.substring(forIndex + 5, lineLen - 1);

        final String prefix = "\"" + dbName + "\".";
        if (synonymQualified.startsWith(prefix) && tableQualified.startsWith(prefix)) {
            final int prefixLen = prefix.length();
            final String synonym = synonymQualified.substring(prefixLen);
            final String table = tableQualified.substring(prefixLen);
            this.synonyms.put(synonym, table);
        } else {
            final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
            throw new IllegalArgumentException("Unable to interpret synonym definition" + lineStr);
        }
    }

    /**
     * Attempts to process an index definition in the SQL file.
     *
     * @param dbName    the database name
     * @param lines     the set of lines from the SQL file
     * @param lineIndex the index of the line where the table definition starts
     * @return the index of the first line after the table definition
     * @throws IllegalArgumentException if the table definition could not be interpreted
     */
    private int processIndexDefinition(final String sqlFileName, final String dbName, final String[] lines,
                                       final int lineIndex) throws IllegalArgumentException {

        if (dbName == null) {
            final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
            throw new IllegalArgumentException("Index definition before DATABASE definition" + lineStr);
        }

        int nextIndex = lineIndex + 1;

        final String line1 = lines[lineIndex].substring(13);
        final int semicolon1 = line1.indexOf(';');

        final String toProcess;

        if (semicolon1 == -1 && lineIndex + 1 < lines.length) {
            final String line2 = lines[lineIndex + 1].trim();
            final int semicolon2 = line2.indexOf(';');
            if (semicolon2 == -1) {
                final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
                throw new IllegalArgumentException("Unable to interpret index definition" + lineStr);
            }
            toProcess = line1 + " " + line2;
            ++nextIndex;
        } else {
            toProcess = line1;
        }

        this.indexes.add(new IndexDefinition(toProcess, lineIndex, sqlFileName, dbName));

        return nextIndex;
    }

    /**
     * Attempts to process a cluster index definition in the SQL file.
     *
     * @param dbName    the database name
     * @param lines     the set of lines from the SQL file
     * @param lineIndex the index of the line where the table definition starts
     * @return the index of the first line after the table definition
     * @throws IllegalArgumentException if the table definition could not be interpreted
     */
    private int processClusterIndexDefinition(final String sqlFileName, final String dbName, final String[] lines,
                                              final int lineIndex) throws IllegalArgumentException {

        if (dbName == null) {
            final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
            throw new IllegalArgumentException("Cluster index definition before DATABASE definition" + lineStr);
        }

        int nextIndex = lineIndex + 1;

        final String line1 = lines[lineIndex].substring(21);
        final int semicolon1 = line1.indexOf(';');

        final String toProcess;

        if (semicolon1 == -1 && lineIndex + 1 < lines.length) {
            final String line2 = lines[lineIndex + 1].trim();
            final int semicolon2 = line2.indexOf(';');
            if (semicolon2 == -1) {
                final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
                throw new IllegalArgumentException("Unable to interpret cluster index definition" + lineStr);
            }
            toProcess = line1 + " " + line2;
            ++nextIndex;
        } else {
            toProcess = line1;
        }

        this.indexes.add(new IndexDefinition(toProcess, lineIndex, sqlFileName, dbName));

        return nextIndex;
    }

    /**
     * Attempts to process a unique index definition in the SQL file.
     *
     * @param dbName    the database name
     * @param lines     the set of lines from the SQL file
     * @param lineIndex the index of the line where the table definition starts
     * @return the index of the first line after the table definition
     * @throws IllegalArgumentException if the table definition could not be interpreted
     */
    private int processUniqueIndexDefinition(final String sqlFileName, final String dbName, final String[] lines,
                                             final int lineIndex) throws IllegalArgumentException {

        if (dbName == null) {
            final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
            throw new IllegalArgumentException("Unique index definition before DATABASE definition" + lineStr);
        }

        int nextIndex = lineIndex + 1;

        final String line1 = lines[lineIndex].substring(20);
        final int semicolon1 = line1.indexOf(';');

        final String toProcess;

        if (semicolon1 == -1 && lineIndex + 1 < lines.length) {
            final String line2 = lines[lineIndex + 1].trim();
            final int semicolon2 = line2.indexOf(';');
            if (semicolon2 == -1) {
                final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
                throw new IllegalArgumentException("Unable to interpret unique index definition" + lineStr);
            }
            toProcess = line1 + " " + line2;
            ++nextIndex;
        } else {
            toProcess = line1;
        }

        this.uniqueIndexes.add(new UniqueIndexDefinition(toProcess, lineIndex, sqlFileName, dbName));

        return nextIndex;
    }

    /**
     * Attempts to process a unique cluster index definition in the SQL file.
     *
     * @param dbName    the database name
     * @param lines     the set of lines from the SQL file
     * @param lineIndex the index of the line where the table definition starts
     * @return the index of the first line after the table definition
     * @throws IllegalArgumentException if the table definition could not be interpreted
     */
    private int processUniqueClusterIndexDefinition(final String sqlFileName, final String dbName, final String[] lines,
                                                    final int lineIndex) throws IllegalArgumentException {

        if (dbName == null) {
            final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
            throw new IllegalArgumentException("Unique cluster index definition before DATABASE definition" + lineStr);
        }

        int nextIndex = lineIndex + 1;

        final String line1 = lines[lineIndex].substring(28);
        final int semicolon1 = line1.indexOf(';');

        final String toProcess;

        if (semicolon1 == -1 && lineIndex + 1 < lines.length) {
            final String line2 = lines[lineIndex + 1].trim();
            final int semicolon2 = line2.indexOf(';');
            if (semicolon2 == -1) {
                final String lineStr = makeLineNumberText(lineIndex, sqlFileName);
                throw new IllegalArgumentException("Unable to interpret unique cluster index definition" + lineStr);
            }
            toProcess = line1 + " " + line2;
            ++nextIndex;
        } else {
            toProcess = line1;
        }

        this.uniqueIndexes.add(new UniqueIndexDefinition(toProcess, lineIndex, sqlFileName, dbName));

        return nextIndex;
    }

    /**
     * Attempts to load the data files associated with the tables from the SQL file.
     *
     * @param exportDir the directory containing the export
     * @throws IllegalArgumentException if the file could not be loaded or interpreted
     */
    private void loadDataFiles(final File exportDir) throws IllegalArgumentException {

        for (final TableDefinition table : this.tables) {
            final File file = new File(exportDir, table.unloadFile);

            final String[] lines = FileLoader.loadFileAsLines(file, false);
            if (lines == null) {
                throw new IllegalArgumentException("Unable to load '" + table.unloadFile + "'");
            }

            final int numFields = table.fields.size();
            final List<String> merged = mergeLines(lines, this.delimiter, numFields);

            final int numLines = merged.size();

            if (numLines < table.numRows) {
                throw new IllegalArgumentException("'" + table.unloadFile + "' has " + numLines + " lines, but "
                                                   + table.tableName + " table indicates it should have "
                                                   + table.numRows + " rows");
            }

            for (int i = 0; i < numLines; ++i) {
                final String line = merged.get(i);
                table.addRow(line, this.delimiter, i);
            }
        }
    }

    /**
     * Merges lines that have been split (lines that end with '\' should be merged with the following line, with the '\'
     * replaced by a newline)
     *
     * @param lines     the list of lines to be merged
     * @param numFields the expected number of fields per line
     * @return the list of merged lines
     */
    private static List<String> mergeLines(final String[] lines, final int delim, final int numFields) {

        final int count = lines.length;
        final List<String> result = new ArrayList<>(lines.length);

        for (int i = 0; i < count; ++i) {
            final String line = lines[i];

            final int numDelimiters = countUnescapedDelimiters(line, delim);
            if (numDelimiters >= numFields) {
                result.add(line);
            } else {
                final int lineLen = line.length();
                final StringBuilder builder = new StringBuilder(lineLen * 2);
                builder.append(line);
                int totalDelimeters = numDelimiters;
                for (int j = i + 1; j < count; ++j) {
                    final String line2 = lines[j];
                    builder.append('\n');
                    builder.append(line2);
                    totalDelimeters += countUnescapedDelimiters(line2, delim);
                    if (totalDelimeters >= numFields) {
                        result.add(builder.toString());
                        i = j;
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Counts the number of delimiters that are not preceded by '\'.
     *
     * @param line      the line of text
     * @param delimiter the delimiter
     * @return the number of unescaped delimiters found
     */
    private static int countUnescapedDelimiters(final String line, final int delimiter) {

        int count = 0;
        final int len = line.length();

        for (int i = 0; i < len; ++i) {
            if ((int) line.charAt(i) == delimiter) {
                if (i > 0) {
                    if ((int) line.charAt(i - 1) == ESCAPE_CHAR) {
                        // Count the number of '\' characters that precede the delimiter
                        int numEscapes = 1;
                        for (int j = i - 2; j > 0 && (int) line.charAt(j) == ESCAPE_CHAR; --j) {
                            ++numEscapes;
                        }
                        // If there are an even number of escape characters, this delimiter was not escaped
                        if ((numEscapes & 0x01) == 0x00) {
                            ++count;
                        }
                    } else {
                        ++count;
                    }
                } else {
                    ++count;
                }
            }
        }

        return count;
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

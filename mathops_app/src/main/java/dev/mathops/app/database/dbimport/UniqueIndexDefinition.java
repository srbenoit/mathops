package dev.mathops.app.database.dbimport;

import dev.mathops.commons.builder.HtmlBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * A unique index definition, as interpreted from the SQL file of an export.
 */
final class UniqueIndexDefinition {

    /** The index name. */
    final String indexName;

    /** The table name. */
    final String tableName;

    /** The field names. */
    final List<String> fieldNames;

    /**
     * Constructs a new {@code UniqueIndexDefinition}.
     *
     * @param sql         the SQL field definition string
     * @param lineIndex   the index of the line being processed, for error reporting
     * @param sqlFileName the name of the SQL file being processed, for error reporting
     * @param dbName      the database name
     * @throws IllegalArgumentException if the string cannot be parsed
     */
    UniqueIndexDefinition(final String sql, final int lineIndex, final String sqlFileName, final String dbName)
            throws IllegalArgumentException {

        final int onIndex = sql.indexOf(" on ");
        if (onIndex == -1) {
            final String lineStr = DataToImport.makeLineNumberText(lineIndex, sqlFileName);
            throw new IllegalArgumentException("Unable to find index name in unique index definition" + lineStr);
        }

        final String prefix = "\"" + dbName + "\".";
        final int prefixLen = prefix.length();

        final String indexNameQualified = sql.substring(0, onIndex).trim();
        this.indexName = indexNameQualified.startsWith(prefix) ? indexNameQualified.substring(prefixLen)
                : indexNameQualified;

        final String afterOn = sql.substring(onIndex + 4).trim();

        final int tableEnd = afterOn.indexOf('(');
        if (tableEnd == -1) {
            final String lineStr = DataToImport.makeLineNumberText(lineIndex, sqlFileName);
            throw new IllegalArgumentException("Unable to find table name in unique index definition" + lineStr);
        }

        final String tableNameQualified = afterOn.substring(0, tableEnd).trim();
        this.tableName = tableNameQualified.startsWith(prefix) ? tableNameQualified.substring(prefixLen)
                : tableNameQualified;

        final String afterTable = afterOn.substring(tableEnd + 1);
        final int fieldEnd = afterTable.indexOf(')');

        if (fieldEnd == -1) {
            final String lineStr = DataToImport.makeLineNumberText(lineIndex, sqlFileName);
            throw new IllegalArgumentException("Unable to find field list in unique index definition" + lineStr);
        }

        final String fieldList = afterTable.substring(0, fieldEnd);
        final String[] split = fieldList.split(",");

        this.fieldNames = new ArrayList<>(split.length);
        for (final String entry : split) {
            final String trimmed = entry.trim();
            this.fieldNames.add(trimmed);
        }
    }

    /**
     * Generates an SQL statement to create the index.
     *
     * @param schemaName the schema name
     * @return the SQL statement
     */
    String makeCreateSql(final String schemaName) {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add("CREATE UNIQUE INDEX ", this.indexName, " ON ", schemaName, ".", this.tableName, " (");
        final int numFields = this.fieldNames.size();
        if (numFields > 0) {
            final String first = this.fieldNames.getFirst();
            builder.add(first);

            for (int i = 1; i < numFields; ++i) {
                final String name = this.fieldNames.get(i);
                builder.add(",", name);
            }
        }
        builder.addln(") TABLESPACE primary_ts;");

        return builder.toString();
    }
}

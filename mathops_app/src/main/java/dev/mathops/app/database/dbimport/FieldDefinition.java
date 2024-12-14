package dev.mathops.app.database.dbimport;

import dev.mathops.commons.CoreConstants;

/**
 * A field definition, as interpreted from the SQL file of an export.
 */
class FieldDefinition {

    /** The field name. */
    final String fieldName;

    /**
     * Constructs a new {@code FieldDefinition}.
     *
     * @param sql       the SQL field definition string
     * @param lineIndex the index of the line being processed, for error reporting
     * @param filename  the name of the SQL file being processed, for error reporting
     * @throws IllegalArgumentException if the string cannot be parsed
     */
    FieldDefinition(final String sql, final int lineIndex, final String filename) throws IllegalArgumentException {

        final int nameEnd = sql.indexOf((int) CoreConstants.SPC_CHAR);

        if (nameEnd < 1) {
            final String lineStr = DataToImport.makeLineNumberText(lineIndex, filename);
            throw new IllegalArgumentException("Unable to find end of field name" + lineStr);
        }

        this.fieldName = sql.substring(0, nameEnd);
    }
}

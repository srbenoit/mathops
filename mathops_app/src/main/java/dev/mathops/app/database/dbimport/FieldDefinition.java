package dev.mathops.app.database.dbimport;

import dev.mathops.commons.CoreConstants;
import dev.mathops.text.builder.HtmlBuilder;

import java.math.BigInteger;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Locale;

/**
 * A field definition, as interpreted from the SQL file of an export.
 */
final class FieldDefinition {

    /** The field name. */
    final String fieldName;

    /** The field type (a value from {@code java.sql.Types}). */
    final int type;

    /** The field length for character fields, and overall length for decimal fields. */
    final int length;

    /** The precision for decimal fields. */
    final int precision;

    /** True if value is required; false if nulls are allowed. */
    final boolean required;

    /**
     * Constructs a new {@code FieldDefinition}.
     *
     * @param sql       the SQL field definition string
     * @param lineIndex the index of the line being processed, for error reporting
     * @param filename  the name of the SQL file being processed, for error reporting
     * @throws IllegalArgumentException if the string cannot be parsed
     */
    FieldDefinition(final String sql, final int lineIndex, final String filename) throws IllegalArgumentException {

        final int nameEnd = sql.indexOf(CoreConstants.SPC_CHAR);

        if (nameEnd < 1) {
            final String lineStr = DataToImport.makeLineNumberText(lineIndex, filename);
            throw new IllegalArgumentException("Unable to find end of field name" + lineStr);
        }

        this.fieldName = sql.substring(0, nameEnd);

        final String afterName = sql.substring(nameEnd + 1).trim();
        final int typeEnd = afterName.indexOf(CoreConstants.SPC_CHAR);

        String typeStr;
        if (typeEnd == -1) {
            typeStr = afterName;
        } else {
            typeStr = afterName.substring(0, typeEnd);
        }
        if (typeStr.endsWith(CoreConstants.COMMA)) {
            final int typeLen = typeStr.length();
            typeStr = typeStr.substring(0, typeLen - 1);
        }

        int theLength = 0;
        int thePrecision = 0;

        if ("smallint".equals(typeStr)) {
            this.type = Types.SMALLINT;
        } else if ("integer".equals(typeStr)) {
            this.type = Types.INTEGER;
        } else if ("bigint".equals(typeStr)) {
            this.type = Types.BIGINT;
        } else if ("date".equals(typeStr)) {
            this.type = Types.DATE;
        } else if ("text".equals(typeStr)) {
            this.type = Types.CLOB;
        } else if (typeStr.startsWith("char(") && typeStr.endsWith(")")) {
            this.type = Types.CHAR;
            final int typeLen = typeStr.length();
            final String lengthStr = typeStr.substring(5, typeLen - 1);
            try {
                theLength = Integer.parseInt(lengthStr);
            } catch (final NumberFormatException ex) {
                final String lineStr = DataToImport.makeLineNumberText(lineIndex, filename);
                throw new IllegalArgumentException("Unable to interpret field length" + lineStr, ex);
            }
        } else if (typeStr.startsWith("varchar(") && typeStr.endsWith(")")) {
            this.type = Types.VARCHAR;
            final int typeLen = typeStr.length();
            final String lengthStr = typeStr.substring(8, typeLen - 1);
            try {
                theLength = Integer.parseInt(lengthStr);
            } catch (final NumberFormatException ex) {
                final String lineStr = DataToImport.makeLineNumberText(lineIndex, filename);
                throw new IllegalArgumentException("Unable to interpret field length" + lineStr, ex);
            }
        } else if (typeStr.startsWith("decimal(") && typeStr.endsWith(")")) {
            this.type = Types.DECIMAL;
            final int typeLen = typeStr.length();
            final int separator = typeStr.indexOf(CoreConstants.COMMA_CHAR);

            if (separator > 8 && separator < typeLen - 2) {
                final String lengthStr = typeStr.substring(8, separator);
                final String precisionStr = typeStr.substring(separator + 1, typeLen - 1);
                try {
                    theLength = Integer.parseInt(lengthStr);
                    thePrecision = Integer.parseInt(precisionStr);
                } catch (final NumberFormatException ex) {
                    final String lineStr = DataToImport.makeLineNumberText(lineIndex, filename);
                    throw new IllegalArgumentException("Unable to interpret decimal type '" + typeStr + "'" + lineStr,
                            ex);
                }
            } else {
                final String lineStr = DataToImport.makeLineNumberText(lineIndex, filename);
                throw new IllegalArgumentException("Unable to interpret decimal type '" + typeStr + "'" + lineStr);
            }
        } else if ("datetime".equals(typeStr)) {
            if (afterName.contains(" year to second")) {
                this.type = Types.TIMESTAMP;
            } else {
                final String lineStr = DataToImport.makeLineNumberText(lineIndex, filename);
                throw new IllegalArgumentException("Unsupported date/time precision '" + afterName + "'" + lineStr);
            }
        } else {
            final String lineStr = DataToImport.makeLineNumberText(lineIndex, filename);
            throw new IllegalArgumentException("Unsupported data type '" + afterName + "'" + lineStr);
        }

        this.length = theLength;
        this.precision = thePrecision;

        this.required = afterName.contains("not null");
    }

    /**
     * Attempts to interpret a value string.
     *
     * @param valueString the value string
     * @return the interpreted object
     * @throws IllegalArgumentException if the value string could not be interpreted
     */
    Object interpret(final String valueString) throws IllegalArgumentException {

        final Object result;

        if (valueString.isEmpty()) {
            result = null;
        } else if (this.type == Types.SMALLINT || this.type == Types.INTEGER) {
            try {
                result = Integer.valueOf(valueString);
            } catch (final NumberFormatException ex) {
                throw new IllegalArgumentException("Unable to interpret date/time", ex);
            }
        } else if (this.type == Types.BIGINT) {
            try {
                result = Long.valueOf(valueString);
            } catch (final NumberFormatException ex) {
                throw new IllegalArgumentException("Unable to interpret date/time", ex);
            }
        } else if (this.type == Types.DECIMAL) {
            try {
                result = Double.valueOf(valueString);
            } catch (final NumberFormatException ex) {
                throw new IllegalArgumentException("Unable to interpret date/time", ex);
            }
        } else if (this.type == Types.DATE) {
            try {
                final SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                final ZoneId zoneId = ZoneId.systemDefault();
                result = fmt.parse(valueString).toInstant().atZone(zoneId).toLocalDate();
            } catch (final ParseException ex) {
                throw new IllegalArgumentException("Unable to interpret date", ex);
            }
        } else if (this.type == Types.TIMESTAMP) {
            try {
                final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                final ZoneId zoneId = ZoneId.systemDefault();
                result = fmt.parse(valueString).toInstant().atZone(zoneId).toLocalDateTime();
            } catch (final ParseException ex) {
                throw new IllegalArgumentException("Unable to interpret date/time", ex);
            }
        } else if (this.type == Types.CHAR || this.type == Types.VARCHAR || this.type == Types.CLOB) {
            result = valueString;
        } else {
            throw new IllegalArgumentException("Unsupported field type.");
        }

        return result;
    }

    /**
     * Appends the column specification for this field as part of the SQL "CREATE TABLE" statement.
     *
     * @param builder         the {@code HtmlBuilder} to which to append
     * @param maxFieldNameLen the length of the longest field name in the table
     */
    void appendCreateSql(final HtmlBuilder builder, final int maxFieldNameLen) {

        builder.add(this.fieldName);
        final int toAdd = maxFieldNameLen - this.fieldName.length();
        for (int i = 0; i < toAdd; ++i) {
            builder.add(CoreConstants.SPC);
        }

        if (this.type == Types.SMALLINT) {
            builder.add(" smallint");
        } else if (this.type == Types.INTEGER) {
            builder.add(" integer");
        } else if (this.type == Types.BIGINT) {
            builder.add(" bigint");
        } else if (this.type == Types.DECIMAL) {
            builder.add(" decimal(");
            builder.add(Integer.toString(this.length));
            builder.add(",");
            builder.add(Integer.toString(this.precision));
            builder.add(")");
        } else if (this.type == Types.DATE) {
            builder.add(" date");
        } else if (this.type == Types.TIMESTAMP) {
            builder.add(" timestamp(0)");
        } else if (this.type == Types.CHAR || this.type == Types.VARCHAR) {
            builder.add(" varchar(");
            builder.add(Integer.toString(this.length));
            builder.add(")");
        } else if (this.type == Types.CLOB) {
            builder.add(" text");
        }

        if (this.required) {
            builder.add(" NOT NULL");
        }
    }
}

package dev.mathops.app.database.dba;

/**
 * A column in a table.
 *
 * @param name       the column name
 * @param type       the SQL type (java.sql.Types)
 * @param valueClass the value class
 * @param size       the size, for character types
 * @param digits     the number of digits after the decimal, for decimal types
 * @param nullable   1 if nullable, 0 if not
 */
public record Column(String name, int type, Class<?> valueClass, int size, int digits, int nullable) {
}

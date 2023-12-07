package dev.mathops.db.generalized;

/**
 * Supported types of database field.
 */
public enum EFieldType {

    /** Java String. */
    STRING,

    /** Java Boolean. */
    BOOLEAN,

    /** Java Byte. */
    BYTE,

    /** Java Integer. */
    INTEGER,

    /** Java Long. */
    LONG,

    /** Java Float. */
    FLOAT,

    /** Java Double. */
    DOUBLE,

    /** Java BigInteger. */
    BIG_INTEGER,

    /** Java BigDecimal. */
    BIG_DECIMAL,

    /** Java byte array. */
    BLOB,

    /** Java LocalDate. */
    LOCAL_DATE,

    /** Java LocalTime. */
    LOCAL_TIME,

    /** Java LocalDateTime. */
    LOCAL_DATE_TIME,
}

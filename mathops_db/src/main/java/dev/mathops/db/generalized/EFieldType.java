package dev.mathops.db.generalized;

import dev.mathops.commons.builder.SimpleBuilder;

import java.sql.Blob;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Supported types of database field.
 */
public enum EFieldType {

    /** Java String. */
    STRING(String.class),

    /** Java Boolean. */
    BOOLEAN(Boolean.class),

    /** Java Byte. */
    BYTE(Byte.class),

    /** Java Integer. */
    INTEGER(Integer.class),

    /** Java Long. */
    LONG(Long.class),

    /** Java Float. */
    FLOAT(Float.class),

    /** Java Double. */
    DOUBLE(Double.class),

    /** Java byte array. */
    BLOB(Blob.class),

    /** Java LocalDate. */
    LOCAL_DATE(LocalDate.class),

    /** Java LocalTime. */
    LOCAL_TIME(LocalTime.class),

    /** Java LocalDateTime. */
    LOCAL_DATE_TIME(LocalDateTime.class);

    /** The type class. */
    public final Class<?> cls;

    /**
     * Constructs a new {@code EFieldType}.
     *
     * @param theCls the value class
     */
    EFieldType(final Class<?> theCls) {

        this.cls = theCls;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String clsName = this.cls.getName();

        return SimpleBuilder.concat("EFieldType{cls=", clsName, "}");
    }
}

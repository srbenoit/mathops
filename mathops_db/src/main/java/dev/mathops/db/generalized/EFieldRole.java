package dev.mathops.db.generalized;

import dev.mathops.commons.builder.SimpleBuilder;

/**
 * Roles within a table that a field can take on.
 */
public enum EFieldRole {

    /**
     * The field participates in the primary key, and can be used to partition data between nodes.  Such a field may
     * not contain a null value.
     */
    PARTITION_KEY(false),

    /**
     * The field participates in the primary key, and can be used to cluster data within a node.  Such a field may
     * not contain a null value.
     */
    CLUSTERING_KEY(false),

    /** A field that doe not participate in the primary key, but cannot be null. */
    NOT_NULL(false),

    /** A field that does not participate in the primary key and may be null. */
    NULLABLE(true);

    /** True if the field is nullable. */
    final boolean nullable;

    /**
     * Constructs a new {@code EFieldRole}.
     * @param isNullable true if the field is nullable
     */
    EFieldRole(final boolean isNullable) {

        this.nullable = isNullable;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String nullableStr = Boolean.toString(this.nullable);

        return SimpleBuilder.concat("EFieldRole{nullable=", nullableStr, "}");
    }
}

package dev.mathops.db.generalized;

/**
 * Roles within a table that a field can take on.
 */
public enum EFieldRole {

    /**
     * The field participates in the primary key, and can be used to partition data between nodes.  Such a field may
     * not contain a null value.
     */
    PARTITION_KEY,

    /**
     * The field participates in the primary key, and can be used to cluster data within a node.  Such a field may
     * not contain a null value.
     */
    CLUSTERING_KEY,

    /** A field that does not participate in the primary key, but cannot be null. */
    NOT_NULL,

    /** A field that does not participate in the primary key and may be null. */
    NULLABLE,
}

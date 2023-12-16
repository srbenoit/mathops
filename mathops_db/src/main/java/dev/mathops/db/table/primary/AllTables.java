package dev.mathops.db.table.primary;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.generalized.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * A container object that holds all table definitions in the "primary" schema.
 */
public final class AllTables {

    /** The singleton collection of all tables. */
    public static final AllTables INSTANCE = new AllTables();

    /** The tables in the schema. */
    public final List<Table> tables;

    /**
     * Constructs a new {@code AllTables}.
     */
    private AllTables() {

        this.tables = new ArrayList<>(10);

        this.tables.add(TermTable.INSTANCE);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("PrimarySchema{tables=", this.tables, "}");
    }
}

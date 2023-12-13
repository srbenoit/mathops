package dev.mathops.db.old.schema.csuods;

import dev.mathops.core.ClassList;
import dev.mathops.db.old.SchemaBuilder;

/**
 * A factory for the CSU ODS schema.
 */
public final class Builder extends SchemaBuilder { // Public so reflection can find it...

    /**
     * Constructs a new {@code Builder}.
     */
    public Builder() {

        super(ClassList.scanClasses("dev.mathops.db.old.ifaces"));
    }
}

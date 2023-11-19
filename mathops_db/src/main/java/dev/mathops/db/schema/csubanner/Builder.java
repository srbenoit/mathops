package dev.mathops.db.schema.csubanner;

import dev.mathops.core.ClassList;
import dev.mathops.db.SchemaBuilder;

/**
 * A factory for the CSU Banner schema.
 */
public final class Builder extends SchemaBuilder { // Public so reflection can find it...

    /**
     * Constructs a new {@code Builder}.
     */
    public Builder() {

        super(ClassList.scanClasses("dev.mathops.db.ifaces"));
    }
}

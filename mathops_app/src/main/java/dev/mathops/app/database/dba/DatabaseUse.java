package dev.mathops.app.database.dba;

import dev.mathops.db.EDbUse;
import dev.mathops.db.cfg.Database;

/** A record of a database and use. */
record DatabaseUse(Database database, EDbUse use) {

}

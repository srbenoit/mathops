package dev.mathops.app.database.dba;

import dev.mathops.db.cfg.EDbUse;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.Login;

/**
 * A record of a database and use.
 *
 * @param database the database
 * @param login    the login
 * @param use      the use
 */
record DatabaseUse(Database database, Login login, EDbUse use) {

}

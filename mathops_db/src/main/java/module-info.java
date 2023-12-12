module mathops_db {
    exports dev.mathops.db.old.rawrecord;
    exports dev.mathops.db.old.rec;
    exports dev.mathops.db;
    exports dev.mathops.db.old.cfg;
    exports dev.mathops.db.old.ifaces;
    exports dev.mathops.db.old.rawlogic;
    exports dev.mathops.db.old.svc.term;
    exports dev.mathops.db.old.logic;
    exports dev.mathops.db.enums;
    exports dev.mathops.db.old.schema;
    exports dev.mathops.db.old.reclogic;
    exports dev.mathops.db.old.reclogic.query;
    exports dev.mathops.db.type;
    exports dev.mathops.db.old;
    exports dev.mathops.db.generalized.connection;
    exports dev.mathops.db.config;
    requires transitive java.desktop;
    requires transitive java.logging;
    requires java.sql;
    requires mathops_core;
}
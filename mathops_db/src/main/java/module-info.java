module mathops_db {
    exports dev.mathops.db.rawrecord;
    exports dev.mathops.db.rec;
    exports dev.mathops.db;
    exports dev.mathops.db.cfg;
    exports dev.mathops.db.ifaces;
    exports dev.mathops.db.rawlogic;
    exports dev.mathops.db.svc.term;
    exports dev.mathops.db.logic;
    exports dev.mathops.db.enums;
    exports dev.mathops.db.schema;
    exports dev.mathops.db.reclogic;
    exports dev.mathops.db.reclogic.query;
    exports dev.mathops.db.type;
    requires transitive java.desktop;
    requires transitive java.logging;
    requires java.sql;
    requires mathops_core;
}
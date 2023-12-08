module mathops_dbjobs {
    exports dev.mathops.dbjobs.batch;
    exports dev.mathops.dbjobs.batch.daily;
    exports dev.mathops.dbjobs.report.cron;
    exports dev.mathops.dbjobs.report;
    requires transitive java.desktop;
    requires transitive java.logging;
    requires java.sql;
    requires mathops_core;
    requires mathops_db;
}
module mathops_app {
    requires java.desktop;
    requires java.sql;
    requires java.prefs;

    requires mathops_core;
    requires mathops_db;
    requires mathops_dbjobs;
    requires mathops_font;
    requires mathops_assessment;
    requires mathops_session;
    requires jwabbit;

    requires com.formdev.flatlaf;
    requires com.oracle.database.jdbc;
}
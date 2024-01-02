/**
 * Applications module.
 */
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

    requires javafx.controls;
    requires com.formdev.flatlaf;
    requires com.oracle.database.jdbc;
    requires javafx.fxml;

    exports dev.mathops.app.db;
    exports dev.mathops.app.db.config;
    exports dev.mathops.app.db.config.model;
}
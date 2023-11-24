module mathops_web {
    requires java.desktop;
    requires java.sql;

    requires mathops_core;
    requires mathops_db;
    requires mathops_dbjobs;
    requires mathops_assessment;
    requires mathops_session;

    requires com.oracle.database.jdbc;
    requires jdk.httpserver;
    requires jakarta.servlet;
    requires jakarta.websocket;
}
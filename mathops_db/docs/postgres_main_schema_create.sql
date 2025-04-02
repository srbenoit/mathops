-- postgres_main_schema_create.sql
-- (this script is designed to be run under the 'math' database owner)
-- /opt/postgresql/bin/psql -d math -U math

-- ------------------------------------------------------------------------------------------------
-- Before executing this script, ensure that all actions in 'postgres_database_create.sql' have
-- been completed.
-- ------------------------------------------------------------------------------------------------

-- Create the schemas in the "math" database.

CREATE SCHEMA IF NOT EXISTS mathops AUTHORIZATION math;         -- Production mathops
CREATE SCHEMA IF NOT EXISTS mathops_dev AUTHORIZATION math;     -- Development mathops
CREATE SCHEMA IF NOT EXISTS mathops_test AUTHORIZATION math;    -- Test mathops

CREATE SCHEMA IF NOT EXISTS main AUTHORIZATION math;            -- Production main
CREATE SCHEMA IF NOT EXISTS main_dev AUTHORIZATION math;        -- Development main
CREATE SCHEMA IF NOT EXISTS main_test AUTHORIZATION math;       -- Test main

CREATE SCHEMA IF NOT EXISTS legacy AUTHORIZATION math;          -- Production legacy
CREATE SCHEMA IF NOT EXISTS legacy_dev AUTHORIZATION math;      -- Development legacy
CREATE SCHEMA IF NOT EXISTS legacy_test AUTHORIZATION math;     -- Test legacy

CREATE SCHEMA IF NOT EXISTS extern AUTHORIZATION math;          -- Production extern
CREATE SCHEMA IF NOT EXISTS extern_dev AUTHORIZATION math;      -- Development extern
CREATE SCHEMA IF NOT EXISTS extern_test AUTHORIZATION math;     -- Test extern

CREATE SCHEMA IF NOT EXISTS analytics AUTHORIZATION math;       -- Production analytics
CREATE SCHEMA IF NOT EXISTS analytics_dev AUTHORIZATION math;   -- Development analytics
CREATE SCHEMA IF NOT EXISTS analytics_test AUTHORIZATION math;  -- Test analytics

-- ================================================================================================
-- Create schema objects within the 'main', 'main_dev', and 'main_test' schemas.
-- ================================================================================================

-- ------------------------------------------------------------------------------------------------
-- TABLE: which_db
--
--   Allows client code to ensure the prefix it is using to access tables is providing the expected
--   data context (production, development, or test).
--
--   USAGE: Single record.
--   EST. RECORDS: 1
--   RETENTION: Stored in MAIN schema, retained.
--   EST. RECORD SIZE: 4 bytes
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS main.which_db;
CREATE TABLE IF NOT EXISTS main.which_db (
    descr               char(4)        NOT NULL,  -- 'PROD' or 'DEV' or 'TEST'
    PRIMARY KEY (descr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main.which_db OWNER to math;
INSERT INTO main.which_db (descr) values ('PROD');

-- DROP TABLE IF EXISTS main_dev.which_db;
CREATE TABLE IF NOT EXISTS main_dev.which_db (
    descr               char(4)        NOT NULL,
    PRIMARY KEY (descr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_dev.which_db OWNER to math;
INSERT INTO main_dev.which_db (descr) values ('DEV');

-- DROP TABLE IF EXISTS main_test.which_db;
CREATE TABLE IF NOT EXISTS main_test.which_db (
    descr               char(4)        NOT NULL,
    PRIMARY KEY (descr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_test.which_db OWNER to math;
INSERT INTO main_test.which_db (descr) values ('TEST');

-- ------------------------------------------------------------------------------------------------
-- TABLE: facility
--
--   Each record defines a facility that provides services to students.  Facilities have operating
--   hours that can be displayed and may have scheduled closures.  Facilities can be physical, like
--   a classroom or office, or virtual, like online help hours or a proctoring service.
--
--   USAGE: One record per facility.
--   EST. RECORDS: 10
--   RETENTION: Stored in MAIN schema, retained.
--   EST. RECORD SIZE: 100 bytes
--   EST. TOTAL SPACE: 1 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS main.facility;
CREATE TABLE IF NOT EXISTS main.facility (
    facility_id          char(10)        NOT NULL,  -- A unique ID for each facility (not visible)
    facility_name        varchar(100)    NOT NULL,  -- The facility name (visible)
    building_name        varchar(40),               -- Building name, null if virtual
    room_nbr             varchar(20),               -- Room number, null if virtual
    PRIMARY KEY (facility_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main.facility OWNER to math;

-- DROP TABLE IF EXISTS main_dev.facility;
CREATE TABLE IF NOT EXISTS main_dev.facility (
    facility_id          char(10)        NOT NULL,
    facility_name        varchar(100)    NOT NULL,
    building_name        varchar(40),
    room_nbr             varchar(20),
    PRIMARY KEY (facility_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_dev.facility OWNER to math;

-- DROP TABLE IF EXISTS main_test.facility;
CREATE TABLE IF NOT EXISTS main_test.facility (
    facility_id          char(10)        NOT NULL,
    facility_name        varchar(100)    NOT NULL,
    building_name        varchar(40),
    room_nbr             varchar(20),
    PRIMARY KEY (facility_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_test.facility OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: facility_hours
--
--   Each record defines block of hours/weekdays that the facility operates or is available to
--   students.  Rows have a well-defined display order to drive website presentations of hours,
--   and should be non-overlapping.
--
--   USAGE: One record per block, where a block specifies some range of hours over some set of.
--          weekdays between a starting and an ending date.
--   EST. RECORDS: 4 per facility x 10 facilities = 40
--   RETENTION: Stored in MAIN schema, retained.
--   EST. RECORD SIZE: 200 bytes
--   EST. TOTAL SPACE: 2 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS main.facility_hours;
CREATE TABLE IF NOT EXISTS main.facility_hours (
    facility_id          char(10)        NOT NULL,  -- The facility ID (references facility table)
    display_index        smallint        NOT NULL,  -- The display index (rows display in order)
    weekdays             smallint        NOT NULL,  -- Weekday (logical OR of 1=Sun, 2=Mon, 4=Tue,
                                                    --  8=Wed, 16=Thu, 32=Fri, 64=Sat)
    start_date           date            NOT NULL,  -- The first date the facility is open
    end_date             date            NOT NULL,  -- The last date the facility is open
    open_time_1          time            NOT NULL,  -- The time the facility opens
    close_time_1         time            NOT NULL,  -- The time the facility closes
    open_time_2          time,                      -- The time the facility re-opens
    close_time_2         time,                      -- The time the facility closes after re-opening
    PRIMARY KEY (facility_id, display_index)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main.facility_hours OWNER to math;

-- DROP TABLE IF EXISTS main_dev.facility_hours;
CREATE TABLE IF NOT EXISTS main_dev.facility_hours (
    facility_id          char(10)        NOT NULL,
    display_index        smallint        NOT NULL,
    weekdays             smallint        NOT NULL,
    start_date           date            NOT NULL,
    end_date             date            NOT NULL,
    open_time_1          time            NOT NULL,
    close_time_1         time            NOT NULL,
    open_time_2          time,
    close_time_2         time,
    PRIMARY KEY (facility_id, display_index)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_dev.facility_hours OWNER to math;

-- DROP TABLE IF EXISTS main_test.facility_hours;
CREATE TABLE IF NOT EXISTS main_test.facility_hours (
    facility_id          char(10)        NOT NULL,
    display_index        smallint        NOT NULL,
    weekdays             smallint        NOT NULL,
    start_date           date            NOT NULL,
    end_date             date            NOT NULL,
    open_time_1          time            NOT NULL,
    close_time_1         time            NOT NULL,
    open_time_2          time,
    close_time_2         time,
    PRIMARY KEY (facility_id, display_index)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_test.facility_hours OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: facility_closure
--
--   Each record represents a closure of a facility on a single day.  Examples are holidays,
--   semester breaks, and ad-hoc closures due to weather, natural disaster, etc.  Storing closures
--   separately from operating hours avoids having to define (and display) a large number of ranges
--   of operating hours.  Hours can be displayed by showing all the operating hours, then a note
--   that the facility is closed on the closure dates/times.
--
--   USAGE: One record per closure.
--   EST. RECORDS: 8 per facility x 10 facilities = 80
--   RETENTION: Stored in MAIN schema, retained, but updated over time.
--   EST. RECORD SIZE: 200 bytes
--   EST. TOTAL SPACE: 2 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS main.facility_closure;
CREATE TABLE IF NOT EXISTS main.facility_closure (
    facility_id          char(10)        NOT NULL,  -- The facility ID (references facility table)
    closure_date         date            NOT NULL,  -- The date of the closure
    closure_type         char(10)        NOT NULL,  -- The type of closure ('HOLIDAY, 'SP_BREAK',
                                                    --  'FA_BREAK', 'WEATHER', 'EMERGENCY', 'MAINT',
                                                    --  'EVENT')
    start_time           time,                      -- Start time, or null if all day
    end_time             time,                      -- End time, or null if all day
    PRIMARY KEY (facility_id, closure_date)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main.facility_closure OWNER to math;

-- DROP TABLE IF EXISTS main_dev.facility_closure;
CREATE TABLE IF NOT EXISTS main_dev.facility_closure (
    facility_id          char(10)        NOT NULL,
    closure_date         date            NOT NULL,
    closure_type         char(10)        NOT NULL,
    start_time           time,
    end_time             time,
    PRIMARY KEY (facility_id, closure_date)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_dev.facility_closure OWNER to math;

-- DROP TABLE IF EXISTS main_test.facility_closure;
CREATE TABLE IF NOT EXISTS main_test.facility_closure (
    facility_id          char(10)        NOT NULL,
    closure_date         date            NOT NULL,
    closure_type         char(10)        NOT NULL,
    start_time           time,
    end_time             time,
    PRIMARY KEY (facility_id, closure_date)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_test.facility_closure OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: standards_course
--
--   Each record defines a standards-based course.
--
--   USAGE: One record per course.
--   EST. RECORDS: 5
--   RETENTION: Stored in MAIN schema, retained.
--   EST. RECORD SIZE: 80 bytes
--   EST. TOTAL SPACE: 1 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS main.standards_course;
CREATE TABLE IF NOT EXISTS main.standards_course (
    course_id            char(10)        NOT NULL,  -- The unique course ID
    course_title         varchar(50)     NOT NULL,  -- The course title
    nbr_modules          smallint        NOT NULL,  -- The number of modules in the course
    nbr_credits          smallint        NOT NULL,  -- The number of credits the course carries
    allow_lend           integer         NOT NULL,  -- Bitwise OR of resource type identifiers (1=Textbook,
                                                    -- 2 = Calculator/manual, 4=Laptop, 8=Headphones)
    metadata_path        varchar(50),               -- For metadata-based courses, the relative path of metadata,
                                                    -- like "05_trig/MATH_125.json"
    PRIMARY KEY (course_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main.standards_course OWNER to math;

-- DROP TABLE IF EXISTS main_dev.standards_course;
CREATE TABLE IF NOT EXISTS main_dev.standards_course (
    course_id            char(10)        NOT NULL,
    course_title         varchar(50)     NOT NULL,
    nbr_modules          smallint        NOT NULL,
    nbr_credits          smallint        NOT NULL,
    allow_lend           integer         NOT NULL,
    metadata_path        varchar(50),
    PRIMARY KEY (course_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_dev.standards_course OWNER to math;

-- DROP TABLE IF EXISTS main_test.standards_course;
CREATE TABLE IF NOT EXISTS main_test.standards_course (
    course_id            char(10)        NOT NULL,
    course_title         varchar(50)     NOT NULL,
    nbr_modules          smallint        NOT NULL,
    nbr_credits          smallint        NOT NULL,
    allow_lend           integer         NOT NULL,
    metadata_path        varchar(50),
    PRIMARY KEY (course_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_test.standards_course OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: standards_course_module
--
--   Each record defines a single module in a standards-based course.
--
--   USAGE: One record per module, 8 per course.
--   EST. RECORDS: 40
--   RETENTION: Stored in MAIN schema, retained.
--   EST. RECORD SIZE: 40 bytes
--   EST. TOTAL SPACE: 2 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS main.standards_course_module;
CREATE TABLE IF NOT EXISTS main.standards_course_module (
    course_id            char(10)        NOT NULL,  -- The course ID (references standards_course)
    module_nbr           smallint        NOT NULL,  -- The module number (1 for the first module)
    nbr_standards        smallint        NOT NULL,  -- The number of standards in the module
    module_path          varchar(50),               -- For metadata-based courses, the relative path of the module,
                                                    -- like "05_trig/01_angles"
    PRIMARY KEY (course_id, module_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main.standards_course_module OWNER to math;

-- DROP TABLE IF EXISTS main_dev.standards_course_module;
CREATE TABLE IF NOT EXISTS main_dev.standards_course_module (
    course_id            char(10)        NOT NULL,
    module_nbr           smallint        NOT NULL,
    nbr_standards        smallint        NOT NULL,
    module_path          varchar(50),
    PRIMARY KEY (course_id, module_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_dev.standards_course_module OWNER to math;

-- DROP TABLE IF EXISTS main_test.standards_course_module;
CREATE TABLE IF NOT EXISTS main_test.standards_course_module (
    course_id            char(10)        NOT NULL,
    module_nbr           smallint        NOT NULL,
    nbr_standards        smallint        NOT NULL,
    module_path          varchar(50),
    PRIMARY KEY (course_id, module_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_test.standards_course_module OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: standard_assignment
--
--   Each record defines an assignment associated with a standard in a course module.
--
--   USAGE: One record per standard, 24 per course.
--   EST. RECORDS: 120
--   RETENTION: Stored in MAIN schema, retained.
--   EST. RECORD SIZE: 60 bytes
--   EST. TOTAL SPACE: 8 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS main.standard_assignment;
CREATE TABLE IF NOT EXISTS main.standard_assignment (
    assignment_id        varchar(20)     NOT NULL,  -- The unique assignment ID
    assignment_type      char(2)         NOT NULL,  -- The assignment type
    course_id            char(10)        NOT NULL,  -- The course ID (references standards_course)
    module_nbr           smallint        NOT NULL,  -- The module number (1 for the first module)
    standard_nbr         smallint        NOT NULL,  -- The standard number (1 for the first standard in a module)
    pts_possible         smallint,                  -- The number of points possible
    min_passing_score    smallint,                  -- The minimum score that is considered "passing"
    tree_ref             varchar(250)    NOT NULL,  -- For tree reference of the assessment
    PRIMARY KEY (assignment_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main.standard_assignment OWNER to math;

-- DROP TABLE IF EXISTS main_dev.standard_assignment;
CREATE TABLE IF NOT EXISTS main_dev.standard_assignment (
    assignment_id        varchar(20)     NOT NULL,
    assignment_type      char(2)         NOT NULL,
    course_id            char(10)        NOT NULL,
    module_nbr           smallint        NOT NULL,
    standard_nbr         smallint        NOT NULL,
    pts_possible         smallint,
    min_passing_score    smallint,
    tree_ref             varchar(250)    NOT NULL,
    PRIMARY KEY (assignment_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_dev.standard_assignment OWNER to math;

-- DROP TABLE IF EXISTS main_test.standard_assignment;
CREATE TABLE IF NOT EXISTS main_test.standard_assignment (
    assignment_id        varchar(20)     NOT NULL,
    assignment_type      char(2)         NOT NULL,
    course_id            char(10)        NOT NULL,
    module_nbr           smallint        NOT NULL,
    standard_nbr         smallint        NOT NULL,
    pts_possible         smallint,
    min_passing_score    smallint,
    tree_ref             varchar(250)    NOT NULL,
    PRIMARY KEY (assignment_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_test.standard_assignment OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: standard_exam
--
--   Each record defines an exam associated with a standard in a course module.
--
--   USAGE: One record per standard, 24 per course.
--   EST. RECORDS: 120
--   RETENTION: Stored in MAIN schema, retained.
--   EST. RECORD SIZE: 60 bytes
--   EST. TOTAL SPACE: 8 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS main.standard_exam;
CREATE TABLE IF NOT EXISTS main.standard_exam (
    exam_id              varchar(20)     NOT NULL,  -- The unique exam ID
    exam_type            char(2)         NOT NULL,  -- The exam type
    course_id            char(10)        NOT NULL,  -- The course ID (references standards_course)
    module_nbr           smallint        NOT NULL,  -- The module number (1 for the first module)
    standard_nbr         smallint        NOT NULL,  -- The standard number (1 for the first standard in a module)
    pts_possible         smallint,                  -- The number of points possible
    min_passing_score    smallint,                  -- The minimum score that is considered "passing"
    tree_ref             varchar(250)    NOT NULL,  -- For tree reference of the assessment
    PRIMARY KEY (exam_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main.standard_exam OWNER to math;

-- DROP TABLE IF EXISTS main_dev.standard_exam;
CREATE TABLE IF NOT EXISTS main_dev.standard_exam (
    exam_id              varchar(20)     NOT NULL,
    exam_type            char(2)         NOT NULL,
    course_id            char(10)        NOT NULL,
    module_nbr           smallint        NOT NULL,
    standard_nbr         smallint        NOT NULL,
    pts_possible         smallint,
    min_passing_score    smallint,
    tree_ref             varchar(250)    NOT NULL,
    PRIMARY KEY (exam_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_dev.standard_exam OWNER to math;

-- DROP TABLE IF EXISTS main_test.standard_exam;
CREATE TABLE IF NOT EXISTS main_test.standard_exam (
    exam_id              varchar(20)     NOT NULL,
    exam_type            char(2)         NOT NULL,
    course_id            char(10)        NOT NULL,
    module_nbr           smallint        NOT NULL,
    standard_nbr         smallint        NOT NULL,
    pts_possible         smallint,
    min_passing_score    smallint,
    tree_ref             varchar(250)    NOT NULL,
    PRIMARY KEY (exam_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_test.standard_exam OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: course_survey
--
-- A survey that can be attached to a course section.
--
--   USAGE: Created once, updated as needed.
--   EST. RECORDS: 3 (version for F2F, version for hybrid version for CE, version for
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 15 bytes
--   EST. TOTAL SPACE: 352 MB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS main.course_survey;
CREATE TABLE IF NOT EXISTS main.course_survey (
    survey_id           char(10)       NOT NULL,  -- The survey ID
    open_pct            smallint       NOT NULL,  -- The percentage of course completion when survey opens
    pref_value          smallint       NOT NULL,  -- The percentage of course completion when survey closes
    PRIMARY KEY (survey_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main.course_survey OWNER to math;

-- DROP TABLE IF EXISTS main_dev.course_survey;
CREATE TABLE IF NOT EXISTS main_dev.course_survey (
    survey_id           char(10)       NOT NULL,
    open_pct            smallint       NOT NULL,
    pref_value          smallint       NOT NULL,
    PRIMARY KEY (survey_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_dev.course_survey OWNER to math;

-- DROP TABLE IF EXISTS main_test.course_survey;
CREATE TABLE IF NOT EXISTS main_test.course_survey (
    survey_id           char(10)       NOT NULL,
    open_pct            smallint       NOT NULL,
    pref_value          smallint       NOT NULL,
    PRIMARY KEY (survey_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_test.course_survey OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: course_survey_item
--
-- A single item on a course survey.
--
--   USAGE: Created once, updated as needed.
--   EST. RECORDS: 3 * 5 = 15
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 140 bytes
--   EST. TOTAL SPACE: 2 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS main.course_survey_item;
CREATE TABLE IF NOT EXISTS main.course_survey_item (
    survey_id           char(10)       NOT NULL,  -- The survey ID
    item                smallint       NOT NULL,  -- The item number
    item_type           smallint       NOT NULL,  -- The item type (1 = M/C, 2 = M/S, 3 = Likert, 4 = Text)
    prompt_html         varchar(250)   NOT NULL,
    PRIMARY KEY (survey_id, item)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main.course_survey_item OWNER to math;

-- DROP TABLE IF EXISTS main_dev.course_survey_item;
CREATE TABLE IF NOT EXISTS main_dev.course_survey_item (
    survey_id           char(10)       NOT NULL,
    item                smallint       NOT NULL,
    item_type           smallint       NOT NULL,
    prompt_html         varchar(250)   NOT NULL,
    PRIMARY KEY (survey_id, item)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_dev.course_survey_item OWNER to math;

-- DROP TABLE IF EXISTS main_test.course_survey_item;
CREATE TABLE IF NOT EXISTS main_test.course_survey_item (
    survey_id           char(10)       NOT NULL,
    item                smallint       NOT NULL,
    item_type           smallint       NOT NULL,
    prompt_html         varchar(250)   NOT NULL,
    PRIMARY KEY (survey_id, item)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_test.course_survey_item OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: course_survey_item_choice
--
-- A single item on a course survey.
--
--   USAGE: Created once, updated as needed.
--   EST. RECORDS: 3 * 3 * 5  = 45
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 120 bytes
--   EST. TOTAL SPACE: 6 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS main.course_survey_item_choice;
CREATE TABLE IF NOT EXISTS main.course_survey_item_choice (
    survey_id           char(10)       NOT NULL,  -- The survey ID
    item                smallint       NOT NULL,  -- The item number
    choice              smallint       NOT NULL,  -- The choice number
    choice_html         varchar(250)   NOT NULL,
    PRIMARY KEY (survey_id, item, choice)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main.course_survey_item_choice OWNER to math;

-- DROP TABLE IF EXISTS main_dev.course_survey_item_choice;
CREATE TABLE IF NOT EXISTS main_dev.course_survey_item_choice (
    survey_id           char(10)       NOT NULL,  -- The survey ID
    item                smallint       NOT NULL,  -- The item number
    choice              smallint       NOT NULL,  -- The choice number
    choice_html         varchar(250)   NOT NULL,
    PRIMARY KEY (survey_id, item, choice)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_dev.course_survey_item_choice OWNER to math;

-- DROP TABLE IF EXISTS main_test.course_survey_item_choice;
CREATE TABLE IF NOT EXISTS main_test.course_survey_item_choice (
    survey_id           char(10)       NOT NULL,  -- The survey ID
    item                smallint       NOT NULL,  -- The item number
    choice              smallint       NOT NULL,  -- The choice number
    choice_html         varchar(250)   NOT NULL,
    PRIMARY KEY (survey_id, item, choice)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS main_test.course_survey_item_choice OWNER to math;



-- postgres_term_schema_create.sql
-- (this script is designed to be run under the 'math' database owner)
-- /opt/postgresql/bin/psql -d math -U math

-- ------------------------------------------------------------------------------------------------
-- Before executing this script, ensure that all actions in 'postgres_database_create.sql' have
-- been completed.
-- ------------------------------------------------------------------------------------------------

-- Create the schemas in the "math" database.

CREATE SCHEMA IF NOT EXISTS term_dev AUTHORIZATION math;        -- Development term
CREATE SCHEMA IF NOT EXISTS term_test AUTHORIZATION math;       -- Test term

CREATE SCHEMA IF NOT EXISTS term_202510 AUTHORIZATION math;     -- Spring 2025 term
CREATE SCHEMA IF NOT EXISTS term_202560 AUTHORIZATION math;     -- Summer 2025 term
CREATE SCHEMA IF NOT EXISTS term_202590 AUTHORIZATION math;     -- Fall 2025 term

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
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 4 bytes
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS term_202510.which_db;
CREATE TABLE IF NOT EXISTS term_202510.which_db (
    descr                    char(4)        NOT NULL,  -- 'PROD' or 'DEV' or 'TEST'
    PRIMARY KEY (descr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.which_db OWNER to math;
INSERT INTO term_202510.which_db (descr) values ('PROD');

-- DROP TABLE IF EXISTS term_202560.which_db;
CREATE TABLE IF NOT EXISTS term_202560.which_db (
    descr                    char(4)        NOT NULL,
    PRIMARY KEY (descr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202560.which_db OWNER to math;
INSERT INTO term_202560.which_db (descr) values ('PROD');

-- DROP TABLE IF EXISTS term_202590.which_db;
CREATE TABLE IF NOT EXISTS term_202590.which_db (
    descr                    char(4)        NOT NULL,
    PRIMARY KEY (descr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202590.which_db OWNER to math;
INSERT INTO term_202590.which_db (descr) values ('PROD');

-- DROP TABLE IF EXISTS term_dev.which_db;
CREATE TABLE IF NOT EXISTS term_dev.which_db (
    descr                    char(4)        NOT NULL,
    PRIMARY KEY (descr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_dev.which_db OWNER to math;
INSERT INTO term_dev.which_db (descr) values ('DEV');

-- DROP TABLE IF EXISTS term_test.which_db;
CREATE TABLE IF NOT EXISTS term_test.which_db (
    descr                    char(4)        NOT NULL,
    PRIMARY KEY (descr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_test.which_db OWNER to math;
INSERT INTO term_test.which_db (descr) values ('TEST');

-- ------------------------------------------------------------------------------------------------
-- TABLE: standards_course_grading_system
--
--   Each record defines a grading system for a standards-based course.
--
--   USAGE: A few standard grading systems that are referenced by multiple sections.
--   EST. RECORDS: 3
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 110 bytes
--   EST. TOTAL SPACE: 124 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS term_202510.standards_course_grading_system;
CREATE TABLE IF NOT EXISTS term_202510.standards_course_grading_system (
    grading_system_id         CHAR(6)         NOT NULL,  -- A unique ID for the grading system
    nbr_standards             smallint        NOT NULL,  -- The number of standards in the course
    min_standards             smallint        NOT NULL,  -- The minimum number of standards mastered to complete course
    max_unmastered_essential  smallint        NOT NULL,  -- The maximum number of "essential" standards that can be
                                                         --     unmastered to complete the course
    homework_pts              smallint        NOT NULL,  -- The number of points for completing each homework assignment
    on_time_mastery_pts       smallint        NOT NULL,  -- The number of points for mastering a standard on time
    late_mastery_pts          smallint        NOT NULL,  -- The number of points for mastering a standard late
    a_min_score               smallint        NOT NULL,  -- The minimum score needed to earn an A grade
    b_min_score               smallint        NOT NULL,  -- The minimum score needed to earn a B grade
    c_min_score               smallint        NOT NULL,  -- The minimum score needed to earn a C grade
    d_min_score               smallint,                  -- The minimum score needed to earn a D grade (null if no D's)
    u_min_score               smallint,                  -- The minimum score needed to earn a U grade (rather than F,
                                                         --     null if no F's given for too few standards)
    min_standards_for_inc     smallint,                  -- If the course allows Incompletes for students who do not
                                                         --     pass (for second and later courses in a term), the
                                                         --     minimum number of standards mastered to be eligible
                                                         --     for Incomplete, null if Incompletes not automatic
    PRIMARY KEY (grading_system_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.standards_course_grading_system OWNER to math;

-- DROP TABLE IF EXISTS term_202560.standards_course_grading_system;
CREATE TABLE IF NOT EXISTS term_202560.standards_course_grading_system (
    grading_system_id         CHAR(6)         NOT NULL,
    nbr_standards             smallint        NOT NULL,
    min_standards             smallint        NOT NULL,
    max_unmastered_essential  smallint        NOT NULL,
    homework_pts              smallint        NOT NULL,
    on_time_mastery_pts       smallint        NOT NULL,
    late_mastery_pts          smallint        NOT NULL,
    a_min_score               smallint        NOT NULL,
    b_min_score               smallint        NOT NULL,
    c_min_score               smallint        NOT NULL,
    d_min_score               smallint,
    u_min_score               smallint,
    min_standards_for_inc     smallint,
    PRIMARY KEY (grading_system_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202560.standards_course_grading_system OWNER to math;

-- DROP TABLE IF EXISTS term_202590.standards_course_grading_system;
CREATE TABLE IF NOT EXISTS term_202590.standards_course_grading_system (
    grading_system_id         CHAR(6)         NOT NULL,
    nbr_standards             smallint        NOT NULL,
    min_standards             smallint        NOT NULL,
    max_unmastered_essential  smallint        NOT NULL,
    homework_pts              smallint        NOT NULL,
    on_time_mastery_pts       smallint        NOT NULL,
    late_mastery_pts          smallint        NOT NULL,
    a_min_score               smallint        NOT NULL,
    b_min_score               smallint        NOT NULL,
    c_min_score               smallint        NOT NULL,
    d_min_score               smallint,
    u_min_score               smallint,
    min_standards_for_inc     smallint,
    PRIMARY KEY (grading_system_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202590.standards_course_grading_system OWNER to math;

-- DROP TABLE IF EXISTS term_dev.standards_course_grading_system;
CREATE TABLE IF NOT EXISTS term_dev.standards_course_grading_system (
    grading_system_id         CHAR(6)         NOT NULL,
    nbr_standards             smallint        NOT NULL,
    min_standards             smallint        NOT NULL,
    max_unmastered_essential  smallint        NOT NULL,
    homework_pts              smallint        NOT NULL,
    on_time_mastery_pts       smallint        NOT NULL,
    late_mastery_pts          smallint        NOT NULL,
    a_min_score               smallint        NOT NULL,
    b_min_score               smallint        NOT NULL,
    c_min_score               smallint        NOT NULL,
    d_min_score               smallint,
    u_min_score               smallint,
    min_standards_for_inc     smallint,
    PRIMARY KEY (grading_system_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_dev.standards_course_grading_system OWNER to math;

-- DROP TABLE IF EXISTS term_test.standards_course_grading_system;
CREATE TABLE IF NOT EXISTS term_test.standards_course_grading_system (
    grading_system_id         CHAR(6)         NOT NULL,
    nbr_standards             smallint        NOT NULL,
    min_standards             smallint        NOT NULL,
    max_unmastered_essential  smallint        NOT NULL,
    homework_pts              smallint        NOT NULL,
    on_time_mastery_pts       smallint        NOT NULL,
    late_mastery_pts          smallint        NOT NULL,
    a_min_score               smallint        NOT NULL,
    b_min_score               smallint        NOT NULL,
    c_min_score               smallint        NOT NULL,
    d_min_score               smallint,
    u_min_score               smallint,
    min_standards_for_inc     smallint,
    PRIMARY KEY (grading_system_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_test.standards_course_grading_system OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: standards_course_section
--
--   Each record defines a section of a standards-based course offered in a term
--
--   USAGE: One record per module, 8 per course.
--   EST. RECORDS: 25 * 3 * 15 = 1,125
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 110 bytes
--   EST. TOTAL SPACE: 124 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS term_202510.standards_course_section;
CREATE TABLE IF NOT EXISTS term_202510.standards_course_section (
    course_id                 char(10)        NOT NULL,  -- The unique course ID (references standards_course)
    section_nbr               char(4)         NOT NULL,  -- The section number (from the registration system)
    crn                       char(6)         NOT NULL,  -- The CRN (from the registration system)
    aries_start_date          date            NOT NULL,  -- The "official" start date of the course
    aries_end_date            date            NOT NULL,  -- The "official" end date of the course
    first_class_date          date            NOT NULL,  -- The first date the course is available to students
    last_class_date           date            NOT NULL,  -- The last date the course is available to students
    subterm                   char(5)         NOT NULL,  -- The subterm ('FULL', 'HALF1', 'HALF2', 'NN:MM' for weeks
                                                         --     NN through MM)
    grading_system_id         char(6)         NOT NULL,  -- The grading system to use for the section
    campus                    char(2)         NOT NULL,  -- The campus code ('FC'=Fort Collins, 'SP'=Spur,
                                                         --     'CE'=Continuing Ed.)
    delivery_mode             char(2)         NOT NULL,  -- The delivery mode ('RF'=Resident Face-to-Face,
                                                         --     'RH'=Resident Hybrid, 'RO'=Resident Online,
                                                         --     'DO'=Distance Online)
    canvas_id                 varchar(40),               -- The ID of the associated Canvas course
    instructor                varchar(30),               -- The name of the instructor assigned to the section
    building_name             varchar(40),               -- The name of the building where class sessions meet
    room_nbr                  varchar(20),               -- The room number where classes meet
    weekdays                  smallint,                  -- The weekdays the class meets (logical OR of 1=Sun, 2=Mon,
                                                         --     4=Tue, 8=Wed, 16=Thu, 32=Fri, 64=Sat)
    PRIMARY KEY (course_id, section_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.standards_course_section OWNER to math;

-- DROP TABLE IF EXISTS term_202560.standards_course_section;
CREATE TABLE IF NOT EXISTS term_202560.standards_course_section (
    course_id                 char(10)        NOT NULL,
    section_nbr               char(4)         NOT NULL,
    crn                       char(6)         NOT NULL,
    aries_start_date          date            NOT NULL,
    aries_end_date            date            NOT NULL,
    first_class_date          date            NOT NULL,
    last_class_date           date            NOT NULL,
    subterm                   char(5)         NOT NULL,
    grading_system_id         char(6)         NOT NULL,
    campus                    char(2)         NOT NULL,
    delivery_mode             char(2)         NOT NULL,
    canvas_id                 varchar(40),
    instructor                varchar(30),
    building_name             varchar(40),
    room_nbr                  varchar(20),
    weekdays                  smallint,
    PRIMARY KEY (course_id, section_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202560.standards_course_section OWNER to math;

-- DROP TABLE IF EXISTS term_202590.standards_course_section;
CREATE TABLE IF NOT EXISTS term_202590.standards_course_section (
    course_id                 char(10)        NOT NULL,
    section_nbr               char(4)         NOT NULL,
    crn                       char(6)         NOT NULL,
    aries_start_date          date            NOT NULL,
    aries_end_date            date            NOT NULL,
    first_class_date          date            NOT NULL,
    last_class_date           date            NOT NULL,
    subterm                   char(5)         NOT NULL,
    grading_system_id         char(6)         NOT NULL,
    campus                    char(2)         NOT NULL,
    delivery_mode             char(2)         NOT NULL,
    canvas_id                 varchar(40),
    instructor                varchar(30),
    building_name             varchar(40),
    room_nbr                  varchar(20),
    weekdays                  smallint,
    PRIMARY KEY (course_id, section_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202590.standards_course_section OWNER to math;

-- DROP TABLE IF EXISTS term_dev.standards_course_section;
CREATE TABLE IF NOT EXISTS term_dev.standards_course_section (
    course_id                 char(10)        NOT NULL,
    section_nbr               char(4)         NOT NULL,
    crn                       char(6)         NOT NULL,
    aries_start_date          date            NOT NULL,
    aries_end_date            date            NOT NULL,
    first_class_date          date            NOT NULL,
    last_class_date           date            NOT NULL,
    subterm                   char(5)         NOT NULL,
    grading_system_id         char(6)         NOT NULL,
    campus                    char(2)         NOT NULL,
    delivery_mode             char(2)         NOT NULL,
    canvas_id                 varchar(40),
    instructor                varchar(30),
    building_name             varchar(40),
    room_nbr                  varchar(20),
    weekdays                  smallint,
    PRIMARY KEY (course_id, section_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_dev.standards_course_section OWNER to math;

-- DROP TABLE IF EXISTS term_test.standards_course_section;
CREATE TABLE IF NOT EXISTS term_test.standards_course_section (
    course_id                 char(10)        NOT NULL,
    section_nbr               char(4)         NOT NULL,
    crn                       char(6)         NOT NULL,
    aries_start_date          date            NOT NULL,
    aries_end_date            date            NOT NULL,
    first_class_date          date            NOT NULL,
    last_class_date           date            NOT NULL,
    subterm                   char(5)         NOT NULL,
    grading_system_id         char(6)         NOT NULL,
    campus                    char(2)         NOT NULL,
    delivery_mode             char(2)         NOT NULL,
    canvas_id                 varchar(40),
    instructor                varchar(30),
    building_name             varchar(40),
    room_nbr                  varchar(20),
    weekdays                  smallint,
    PRIMARY KEY (course_id, section_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_test.standards_course_section OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: standard_assignment_attempt
--
--   Each record records a student attempt on a homework assignment in a course.
--
--   USAGE: One record per attempt.
--   EST. RECORDS: 6000 students * 2 courses * 48 assignments * 2 attempts each = 1,152,000
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 110 bytes
--   EST. TOTAL SPACE: 120 MB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS term_202510.standard_assignment_attempt;
CREATE TABLE IF NOT EXISTS term_202510.standard_assignment_attempt (
    serial_nbr                integer         NOT NULL,  -- A unique serial number for the attempt
    student_id                char(9)         NOT NULL,  -- The student ID who submitted the attempt
    assignment_id             varchar(20)     NOT NULL,  -- The ID of the assignment (references standard_assignment)
    attempt_date              date            NOT NULL,  -- The date when the attempt is to be counted (the submission
                                                         --     date or the day before submission for some assignments
                                                         --     accepted early the following day)
    attempt_time_sec          integer         NOT NULL,  -- The time of day when the assignment was submitted, in
                                                         --     seconds (typically 0 to 86,399, but 86,400 or greater
                                                         --     if assignment accepted early the following day and
                                                         --     counted as submitted in prior day)
    course_id                 char(10)        NOT NULL,  -- The course ID (from standard_assignment)
    module_nbr                smallint        NOT NULL,  -- The module number (from standard_assignment)
    standard_nbr              smallint        NOT NULL,  -- The standard number (from standard_assignment)
    pts_possible              smallint,                  -- The number of points possible (from standard_assignment)
    min_passing_score         smallint,                  -- The minimum passing score (from standard_assignment)
    score                     smallint        NOT NULL,  -- The earned score
    passed                    char(1)         NOT NULL,  -- "Y" = passed, "N" = not passed, "G" = ignore, "P" = passed
                                                         --     but invalidated (say, for academic misconduct)
    PRIMARY KEY (serial_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.standard_assignment_attempt OWNER to math;

-- DROP TABLE IF EXISTS term_202560.standard_assignment_attempt;
CREATE TABLE IF NOT EXISTS term_202560.standard_assignment_attempt (
    serial_nbr                integer         NOT NULL,
    student_id                char(9)         NOT NULL,
    assignment_id             varchar(20)     NOT NULL,
    attempt_date              date            NOT NULL,
    attempt_time_sec          integer         NOT NULL,
    course_id                 char(10)        NOT NULL,
    module_nbr                smallint        NOT NULL,
    standard_nbr              smallint        NOT NULL,
    pts_possible              smallint,
    min_passing_score         smallint,
    score                     smallint        NOT NULL,
    passed                    char(1)         NOT NULL,
    PRIMARY KEY (serial_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202560.standard_assignment_attempt OWNER to math;

-- DROP TABLE IF EXISTS term_202590.standard_assignment_attempt;
CREATE TABLE IF NOT EXISTS term_202590.standard_assignment_attempt (
    serial_nbr                integer         NOT NULL,
    student_id                char(9)         NOT NULL,
    assignment_id             varchar(20)     NOT NULL,
    attempt_date              date            NOT NULL,
    attempt_time_sec          integer         NOT NULL,
    course_id                 char(10)        NOT NULL,
    module_nbr                smallint        NOT NULL,
    standard_nbr              smallint        NOT NULL,
    pts_possible              smallint,
    min_passing_score         smallint,
    score                     smallint        NOT NULL,
    passed                    char(1)         NOT NULL,
    PRIMARY KEY (serial_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202590.standard_assignment_attempt OWNER to math;

-- DROP TABLE IF EXISTS term_dev.standard_assignment_attempt;
CREATE TABLE IF NOT EXISTS term_dev.standard_assignment_attempt (
    serial_nbr                integer         NOT NULL,
    student_id                char(9)         NOT NULL,
    assignment_id             varchar(20)     NOT NULL,
    attempt_date              date            NOT NULL,
    attempt_time_sec          integer         NOT NULL,
    course_id                 char(10)        NOT NULL,
    module_nbr                smallint        NOT NULL,
    standard_nbr              smallint        NOT NULL,
    pts_possible              smallint,
    min_passing_score         smallint,
    score                     smallint        NOT NULL,
    passed                    char(1)         NOT NULL,
    PRIMARY KEY (serial_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_dev.standard_assignment_attempt OWNER to math;

-- DROP TABLE IF EXISTS term_test.standard_assignment_attempt;
CREATE TABLE IF NOT EXISTS term_test.standard_assignment_attempt (
    serial_nbr                integer         NOT NULL,
    student_id                char(9)         NOT NULL,
    assignment_id             varchar(20)     NOT NULL,
    attempt_date              date            NOT NULL,
    attempt_time_sec          integer         NOT NULL,
    course_id                 char(10)        NOT NULL,
    module_nbr                char(10)        NOT NULL,
    standard_nbr              smallint        NOT NULL,
    pts_possible              smallint,
    min_passing_score         smallint,
    score                     smallint        NOT NULL,
    passed                    char(1)         NOT NULL,
    PRIMARY KEY (serial_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_test.standard_assignment_attempt OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: standard_assignment_attempt_qa
--
--   Each record records one answer on a student attempt on a homework assignment in a course.
--
--   USAGE: On average, 8 records per attempt.
--   EST. RECORDS: 6000 students * 2 courses * 48 assignments * 2 attempts each * 4 records = 4,608,000
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 20 bytes
--   EST. TOTAL SPACE: 87 MB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS term_202510.standard_assignment_attempt_qa;
CREATE TABLE IF NOT EXISTS term_202510.standard_assignment_attempt_qa (
    serial_nbr                integer         NOT NULL,  -- The serial number (references standard_assignment_attempt)
    question_nbr              smallint        NOT NULL,  -- The question number
    points                    smallint        NOT NULL,  -- Points earned
    item_id                   varchar(20),               -- The item ID
    PRIMARY KEY (serial_nbr, question_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.standard_assignment_attempt_qa OWNER to math;

-- DROP TABLE IF EXISTS term_202560.standard_assignment_attempt_qa;
CREATE TABLE IF NOT EXISTS term_202560.standard_assignment_attempt_qa (
    serial_nbr                integer         NOT NULL,
    question_nbr              smallint        NOT NULL,
    points                    smallint        NOT NULL,
    item_id                   varchar(20),
    PRIMARY KEY (serial_nbr, question_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202560.standard_assignment_attempt_qa OWNER to math;

-- DROP TABLE IF EXISTS term_202590.standard_assignment_attempt_qa;
CREATE TABLE IF NOT EXISTS term_202590.standard_assignment_attempt_qa (
    serial_nbr                integer         NOT NULL,
    question_nbr              smallint        NOT NULL,
    points                    smallint        NOT NULL,
    item_id                   varchar(20),
    PRIMARY KEY (serial_nbr, question_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202590.standard_assignment_attempt_qa OWNER to math;

-- DROP TABLE IF EXISTS term_dev.standard_assignment_attempt_qa;
CREATE TABLE IF NOT EXISTS term_dev.standard_assignment_attempt_qa (
    serial_nbr                integer         NOT NULL,
    question_nbr              smallint        NOT NULL,
    points                    smallint        NOT NULL,
    item_id                   varchar(20),
    PRIMARY KEY (serial_nbr, question_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_dev.standard_assignment_attempt_qa OWNER to math;

-- DROP TABLE IF EXISTS term_test.standard_assignment_attempt_qa;
CREATE TABLE IF NOT EXISTS term_test.standard_assignment_attempt_qa (
    serial_nbr                integer         NOT NULL,
    question_nbr              smallint        NOT NULL,
    points                    smallint        NOT NULL,
    item_id                   varchar(20),
    PRIMARY KEY (serial_nbr, question_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_test.standard_assignment_attempt_qa OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: standards_milestone
--
-- The configured milestones for standards-based courses, where each pace and pace track defines
-- a set of milestones for all courses in that track.
--
--   USAGE: Created with each new term.  Updated only for events like weather closures.
--   EST. RECORDS: 240 (8 per course in a pace/track * 15 pace/index * 2 tracks)
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 16 bytes
--   EST. TOTAL SPACE: 4 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS term_202510.standards_milestone;
CREATE TABLE IF NOT EXISTS term_202510.standards_milestone (
    pace_track               char(1)        NOT NULL,  -- The pace track
    pace                     smallint       NOT NULL,  -- The pace
    pace_index               smallint       NOT NULL,  -- The pace index
    module_nbr               smallint       NOT NULL,  -- The module number
    ms_type                  char(4)        NOT NULL,  -- The milestone type
    ms_date                  date           NOT NULL,  -- The milestone date
    PRIMARY KEY (pace_track, pace, pace_index, module_nbr, ms_type)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.standards_milestone OWNER to math;

-- DROP TABLE IF EXISTS term_202560.standards_milestone;
CREATE TABLE IF NOT EXISTS term_202560.standards_milestone (
    pace_track               char(1)        NOT NULL,
    pace                     smallint       NOT NULL,
    pace_index               smallint       NOT NULL,
    module_nbr               smallint       NOT NULL,
    ms_type                  char(4)        NOT NULL,
    ms_date                  date           NOT NULL,
    PRIMARY KEY (pace_track, pace, pace_index, module_nbr, ms_type)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202560.standards_milestone OWNER to math;

-- DROP TABLE IF EXISTS term_202590.standards_milestone;
CREATE TABLE IF NOT EXISTS term_202590.standards_milestone (
    pace_track               char(1)        NOT NULL,
    pace                     smallint       NOT NULL,
    pace_index               smallint       NOT NULL,
    module_nbr               smallint       NOT NULL,
    ms_type                  char(4)        NOT NULL,
    ms_date                  date           NOT NULL,
    PRIMARY KEY (pace_track, pace, pace_index, module_nbr, ms_type)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202590.standards_milestone OWNER to math;

-- DROP TABLE IF EXISTS term_dev.standards_milestone;
CREATE TABLE IF NOT EXISTS term_dev.standards_milestone (
    pace_track               char(1)        NOT NULL,
    pace                     smallint       NOT NULL,
    pace_index               smallint       NOT NULL,
    module_nbr               smallint       NOT NULL,
    ms_type                  char(4)        NOT NULL,
    ms_date                  date           NOT NULL,
    PRIMARY KEY (pace_track, pace, pace_index, module_nbr, ms_type)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_dev.standards_milestone OWNER to math;

-- DROP TABLE IF EXISTS term_test.standards_milestone;
CREATE TABLE IF NOT EXISTS term_test.standards_milestone (
    pace_track               char(1)        NOT NULL,
    pace                     smallint       NOT NULL,
    pace_index               smallint       NOT NULL,
    module_nbr               smallint       NOT NULL,
    ms_type                  char(4)        NOT NULL,
    ms_date                  date           NOT NULL,
    PRIMARY KEY (pace_track, pace, pace_index, module_nbr, ms_type)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_test.standards_milestone OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: student_standards_milestone
--
-- An override for a particular standard milestone for a student.
--
--   USAGE: Created (or updated) as extensions are given to students for SDC accommodations or
--          extenuating circumstances.
--   EST. RECORDS: 1000
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 32 bytes
--   EST. TOTAL SPACE: 32 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS term_202510.student_standards_milestone;
CREATE TABLE IF NOT EXISTS term_202510.student_standards_milestone (
    student_id               char(9)        NOT NULL,  -- The student ID
    pace_track               char(1)        NOT NULL,  -- The pace track
    pace                     smallint       NOT NULL,  -- The pace
    pace_index               smallint       NOT NULL,  -- The pace index
    module_nbr               smallint       NOT NULL,  -- The module number
    ms_type                  char(4)        NOT NULL,  -- The milestone type
    ms_date                  date           NOT NULL,  -- The new milestone date
    PRIMARY KEY (student_id, pace_track, pace, pace_index, module_nbr, ms_type)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.student_standards_milestone OWNER to math;

-- DROP TABLE IF EXISTS term_202560.student_standards_milestone;
CREATE TABLE IF NOT EXISTS term_202560.student_standards_milestone (
    student_id               char(9)        NOT NULL,
    pace_track               char(1)        NOT NULL,
    pace                     smallint       NOT NULL,
    pace_index               smallint       NOT NULL,
    module_nbr               smallint       NOT NULL,
    ms_type                  char(4)        NOT NULL,
    ms_date                  date           NOT NULL,
    PRIMARY KEY (student_id, pace_track, pace, pace_index, module_nbr, ms_type)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202560.student_standards_milestone OWNER to math;

-- DROP TABLE IF EXISTS term_202590.student_standards_milestone;
CREATE TABLE IF NOT EXISTS term_202590.student_standards_milestone (
    student_id               char(9)        NOT NULL,
    pace_track               char(1)        NOT NULL,
    pace                     smallint       NOT NULL,
    pace_index               smallint       NOT NULL,
    module_nbr               smallint       NOT NULL,
    ms_type                  char(4)        NOT NULL,
    ms_date                  date           NOT NULL,
    PRIMARY KEY (student_id, pace_track, pace, pace_index, module_nbr, ms_type)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202590.student_standards_milestone OWNER to math;

-- DROP TABLE IF EXISTS term_dev.student_standards_milestone;
CREATE TABLE IF NOT EXISTS term_dev.student_standards_milestone (
    student_id               char(9)        NOT NULL,
    pace_track               char(1)        NOT NULL,
    pace                     smallint       NOT NULL,
    pace_index               smallint       NOT NULL,
    module_nbr               smallint       NOT NULL,
    ms_type                  char(4)        NOT NULL,
    ms_date                  date           NOT NULL,
    PRIMARY KEY (student_id, pace_track, pace, pace_index, module_nbr, ms_type)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_dev.student_standards_milestone OWNER to math;

-- DROP TABLE IF EXISTS term_test.student_standards_milestone;
CREATE TABLE IF NOT EXISTS term_test.student_standards_milestone (
    student_id               char(9)        NOT NULL,
    pace_track               char(1)        NOT NULL,
    pace                     smallint       NOT NULL,
    pace_index               smallint       NOT NULL,
    module_nbr               smallint       NOT NULL,
    ms_type                  char(4)        NOT NULL,
    ms_date                  date           NOT NULL,
    PRIMARY KEY (student_id, pace_track, pace, pace_index, module_nbr, ms_type)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_test.student_standards_milestone OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: student_course_mastery
-- 
-- A student's mastery status in a standards-based course (it should be possible to calculate the student's score
-- and course completion status from this record only).
--
--   USAGE: Created when course is started, updated throughout term as status changes.
--   EST. RECORDS: 6000 student * 2 courses = 12,000
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 104 bytes
--   EST. TOTAL SPACE: 1300 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS term_202510.student_course_mastery;
CREATE TABLE IF NOT EXISTS term_202510.student_course_mastery (
    student_id               char(9)        NOT NULL,  -- The student ID
    course_id                char(10)       NOT NULL,  -- The course ID
    course_structure         varchar(200)   NOT NULL,  -- The course structure in a format like:
                                                       --     "aAabbbCcc...zzZ", where each letter (a-z) represents a
                                                       --     module and each repetition of that letter represents a
                                                       --     standard, lowercase = non-essential, uppercase = essential
    homework_status          varchar(200)   NOT NULL,  -- Student status on standard homeworks for each standard in a
                                                       --     format like "YN---", the same length as the course
                                                       --     structure, Y=passed, N=attempted, -=not attempted
    mastery_status           varchar(200)   NOT NULL,  -- Student mastery status for each standard, in a format like
                                                       --     "Yyn---", the same length as the course structure,
                                                       --     Y=mastered on time, y=mastered late, N=attempted,
                                                       --     -=not attempted
    completed                char(1)        NOT NULL,  -- "Y" if course is completed, "N" if not.
    score                    smallint       NOT NULL,  -- The current score
    PRIMARY KEY (student_id, course_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.student_course_mastery OWNER to math;

-- DROP TABLE IF EXISTS term_202560.student_course_mastery;
CREATE TABLE IF NOT EXISTS term_202560.student_course_mastery (
    student_id               char(9)        NOT NULL,
    course_id                char(10)       NOT NULL,
    course_structure         varchar(200)   NOT NULL,
    homework_status          varchar(200)   NOT NULL,
    mastery_status           varchar(200)   NOT NULL,
    completed                char(1)        NOT NULL,
    score                    smallint       NOT NULL,
    PRIMARY KEY (student_id, course_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202560.student_course_mastery OWNER to math;

-- DROP TABLE IF EXISTS term_202590.student_course_mastery;
CREATE TABLE IF NOT EXISTS term_202590.student_course_mastery (
    student_id               char(9)        NOT NULL,
    course_id                char(10)       NOT NULL,
    course_structure         varchar(200)   NOT NULL,
    homework_status          varchar(200)   NOT NULL,
    mastery_status           varchar(200)   NOT NULL,
    completed                char(1)        NOT NULL,
    score                    smallint       NOT NULL,
    PRIMARY KEY (student_id, course_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202590.student_course_mastery OWNER to math;

-- DROP TABLE IF EXISTS term_dev.student_course_mastery;
CREATE TABLE IF NOT EXISTS term_dev.student_course_mastery (
    student_id               char(9)        NOT NULL,
    course_id                char(10)       NOT NULL,
    course_structure         varchar(200)   NOT NULL,
    homework_status          varchar(200)   NOT NULL,
    mastery_status           varchar(200)   NOT NULL,
    completed                char(1)        NOT NULL,
    score                    smallint       NOT NULL,
    PRIMARY KEY (student_id, course_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_dev.student_course_mastery OWNER to math;

-- DROP TABLE IF EXISTS term_test.student_course_mastery;
CREATE TABLE IF NOT EXISTS term_test.student_course_mastery (
    student_id               char(9)        NOT NULL,
    course_id                char(10)       NOT NULL,
    course_structure         varchar(200)   NOT NULL,
    homework_status          varchar(200)   NOT NULL,
    mastery_status           varchar(200)   NOT NULL,
    completed                char(1)        NOT NULL,
    score                    smallint       NOT NULL,
    PRIMARY KEY (student_id, course_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_test.student_course_mastery OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: student_course_preferences
--
-- A student's account preferences for a course.
--
--   USAGE: Created when course is started, updated throughout term as status changes.
--   EST. RECORDS: 6000 student * 4 prefs = 24,000
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 15 bytes
--   EST. TOTAL SPACE: 352 MB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS term_202510.student_preference;
CREATE TABLE IF NOT EXISTS term_202510.student_preference (
    student_id               char(9)        NOT NULL,  -- The student ID
    pref_key                 char(4)        NOT NULL,  -- A key that identifies a preference ('ANXI', 'SELF', 'LEVL',
                                                       --     'MESG')
    pref_value               smallint       NOT NULL,  -- The student's preference setting
    PRIMARY KEY (student_id, pref_key)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.student_preference OWNER to math;
CREATE INDEX ON term_202510.student_preference (student_id) TABLESPACE primary_ts;

-- DROP TABLE IF EXISTS term_202560.student_preference;
CREATE TABLE IF NOT EXISTS term_202560.student_preference (
    student_id               char(9)        NOT NULL,
    pref_key                 char(4)        NOT NULL,
    pref_value               smallint       NOT NULL,
    PRIMARY KEY (student_id, pref_key)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202560.student_preference OWNER to math;
CREATE INDEX ON term_202560.student_preference (student_id) TABLESPACE primary_ts;

-- DROP TABLE IF EXISTS term_202590.student_preference;
CREATE TABLE IF NOT EXISTS term_202590.student_preference (
    student_id               char(9)        NOT NULL,
    pref_key                 char(4)        NOT NULL,
    pref_value               smallint       NOT NULL,
    PRIMARY KEY (student_id, pref_key)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202590.student_preference OWNER to math;
CREATE INDEX ON term_202590.student_preference (student_id) TABLESPACE primary_ts;

-- DROP TABLE IF EXISTS term_dev.student_preference;
CREATE TABLE IF NOT EXISTS term_dev.student_preference (
    student_id               char(9)        NOT NULL,
    pref_key                 char(4)        NOT NULL,
    pref_value               smallint       NOT NULL,
    PRIMARY KEY (student_id, pref_key)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_dev.student_preference OWNER to math;
CREATE INDEX ON term_dev.student_preference (student_id) TABLESPACE primary_ts;

-- DROP TABLE IF EXISTS term_test.student_preference;
CREATE TABLE IF NOT EXISTS term_test.student_preference (
    student_id               char(9)        NOT NULL,
    pref_key                 char(4)       NOT NULL,
    pref_value               smallint       NOT NULL,
    PRIMARY KEY (student_id, pref_key)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_test.student_preference OWNER to math;
CREATE INDEX ON term_test.student_preference (student_id) TABLESPACE primary_ts;


-- ------------------------------------------------------------------------------------------------
-- TABLE: course_section_survey
--
-- An attachment of a course survey to a section.
--
--   USAGE: Created once, updated as needed.
--   EST. RECORDS: 3 (version for F2F, version for hybrid version for CE, version for
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 15 bytes
--   EST. TOTAL SPACE: 352 MB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS term_202510.course_section_survey;
CREATE TABLE IF NOT EXISTS term_202510.course_section_survey (
    course_id                 char(10)        NOT NULL,  -- The course ID
    section_nbr               char(4)         NOT NULL,  -- The section number
    survey_id                 char(10)        NOT NULL,  -- The survey ID
    PRIMARY KEY (course_id, section_nbr, survey_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.course_section_survey OWNER to math;

-- DROP TABLE IF EXISTS term_202560.course_section_survey;
CREATE TABLE IF NOT EXISTS term_202560.course_section_survey (
    course_id                 char(10)        NOT NULL,
    section_nbr               char(4)         NOT NULL,
    survey_id                 char(10)        NOT NULL,
    PRIMARY KEY (course_id, section_nbr, survey_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202560.course_section_survey OWNER to math;

-- DROP TABLE IF EXISTS term_202590.course_section_survey;
CREATE TABLE IF NOT EXISTS term_202590.course_section_survey (
    course_id                 char(10)        NOT NULL,
    section_nbr               char(4)         NOT NULL,
    survey_id                 char(10)        NOT NULL,
    PRIMARY KEY (course_id, section_nbr, survey_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202590.course_section_survey OWNER to math;

-- DROP TABLE IF EXISTS term_dev.course_section_survey;
CREATE TABLE IF NOT EXISTS term_dev.course_section_survey (
    course_id                 char(10)        NOT NULL,
    section_nbr               char(4)         NOT NULL,
    survey_id                 char(10)        NOT NULL,
    PRIMARY KEY (course_id, section_nbr, survey_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_dev.course_section_survey OWNER to math;

-- DROP TABLE IF EXISTS term_test.course_section_survey;
CREATE TABLE IF NOT EXISTS term_test.course_section_survey (
    course_id                 char(10)        NOT NULL,
    section_nbr               char(4)         NOT NULL,
    survey_id                 char(10)        NOT NULL,
    PRIMARY KEY (course_id, section_nbr, survey_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_test.course_section_survey OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: course_survey_response
--
-- A student's response to a course survey.
--
--   USAGE: One record per student response.
--   EST. RECORDS: 6000 student * 2 courses * 20% rate = 2,400
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 15 bytes
--   EST. TOTAL SPACE: 352 MB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS term_202510.course_survey_response;
CREATE TABLE IF NOT EXISTS term_202510.course_survey_response (
    serial_nbr               integer        NOT NULL,  -- A unique serial number for the response
    survey_id                char(10)       NOT NULL,  -- The survey ID
    student_id               char(9)        NOT NULL,  -- The student ID
    response_date            date           NOT NULL,  -- The response date
    response_time            time           NOT NULL,  -- The response time
    PRIMARY KEY (serial_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.course_survey_response OWNER to math;

-- DROP TABLE IF EXISTS term_202560.course_survey_response;
CREATE TABLE IF NOT EXISTS term_202560.course_survey_response (
    serial_nbr               integer        NOT NULL,
    survey_id                char(10)       NOT NULL,
    student_id               char(9)        NOT NULL,
    response_date            date           NOT NULL,
    response_time            time           NOT NULL,
    PRIMARY KEY (serial_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202560.course_survey_response OWNER to math;

-- DROP TABLE IF EXISTS term_202590.course_survey_response;
CREATE TABLE IF NOT EXISTS term_202590.course_survey_response (
    serial_nbr               integer        NOT NULL,
    survey_id                char(10)       NOT NULL,
    student_id               char(9)        NOT NULL,
    response_date            date           NOT NULL,
    response_time            time           NOT NULL,
    PRIMARY KEY (serial_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202590.course_survey_response OWNER to math;

-- DROP TABLE IF EXISTS term_dev.course_survey_response;
CREATE TABLE IF NOT EXISTS term_dev.course_survey_response (
    serial_nbr               integer        NOT NULL,
    survey_id                char(10)       NOT NULL,
    student_id               char(9)        NOT NULL,
    response_date            date           NOT NULL,
    response_time            time           NOT NULL,
    PRIMARY KEY (serial_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_dev.course_survey_response OWNER to math;

-- DROP TABLE IF EXISTS term_test.course_survey_response;
CREATE TABLE IF NOT EXISTS term_test.course_survey_response (
    serial_nbr               integer        NOT NULL,
    survey_id                char(10)       NOT NULL,
    student_id               char(9)        NOT NULL,
    response_date            date           NOT NULL,
    response_time            time           NOT NULL,
    PRIMARY KEY (serial_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_test.course_survey_response OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: course_survey_response_item_choice
--
-- A student's response to a single choice-based item on a course survey.
--
--   USAGE: One record per choice item per student response.
--   EST. RECORDS: 6000 student * 2 courses * 20% rate * 3 items = 7,200
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 8 bytes
--   EST. TOTAL SPACE: 58 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS term_202510.course_survey_response_item_choice;
CREATE TABLE IF NOT EXISTS term_202510.course_survey_response_item_choice (
    serial_nbr               integer        NOT NULL,  -- A unique serial number for the response
    item_nbr                 smallint       NOT NULL,  -- The item number
    response_choice          smallint       NOT NULL,  -- The selected choice value(s)
    PRIMARY KEY (serial_nbr, item_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.course_survey_response_item_choice OWNER to math;

-- DROP TABLE IF EXISTS term_202560.course_survey_response_item_choice;
CREATE TABLE IF NOT EXISTS term_202560.course_survey_response_item_choice (
    serial_nbr               integer        NOT NULL,
    item_nbr                 smallint       NOT NULL,
    response_choice          smallint       NOT NULL,
    PRIMARY KEY (serial_nbr, item_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202560.course_survey_response_item_choice OWNER to math;

-- DROP TABLE IF EXISTS term_202590.course_survey_response_item_choice;
CREATE TABLE IF NOT EXISTS term_202590.course_survey_response_item_choice (
    serial_nbr               integer        NOT NULL,
    item_nbr                 smallint       NOT NULL,
    response_choice          smallint       NOT NULL,
    PRIMARY KEY (serial_nbr, item_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202590.course_survey_response_item_choice OWNER to math;

-- DROP TABLE IF EXISTS term_dev.course_survey_response_item_choice;
CREATE TABLE IF NOT EXISTS term_dev.course_survey_response_item_choice (
    serial_nbr               integer        NOT NULL,
    item_nbr                 smallint       NOT NULL,
    response_choice          smallint       NOT NULL,
    PRIMARY KEY (serial_nbr, item_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_dev.course_survey_response_item_choice OWNER to math;

-- DROP TABLE IF EXISTS term_test.course_survey_response_item_choice;
CREATE TABLE IF NOT EXISTS term_test.course_survey_response_item_choice (
    serial_nbr               integer        NOT NULL,
    item_nbr                 smallint       NOT NULL,
    response_choice          smallint       NOT NULL,
    PRIMARY KEY (serial_nbr, item_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_test.course_survey_response_item_choice OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: course_survey_response_item_text
--
-- A student's response to a single choice-based item on a course survey.
--
--   USAGE: One record per choice item per student response.
--   EST. RECORDS: 6000 student * 2 courses * 20% rate * 3 items = 7,200
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 250 bytes
--   EST. TOTAL SPACE: 1800 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS term_202510.course_survey_response_item_text;
CREATE TABLE IF NOT EXISTS term_202510.course_survey_response_item_text (
    serial_nbr               integer        NOT NULL,  -- A unique serial number for the response
    item_nbr                 smallint       NOT NULL,  -- The item number
    response_text            text           NOT NULL,  -- The entered text
    PRIMARY KEY (serial_nbr, item_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.course_survey_response_item_text OWNER to math;

-- DROP TABLE IF EXISTS term_202560.course_survey_response_item_text;
CREATE TABLE IF NOT EXISTS term_202560.course_survey_response_item_text (
    serial_nbr               integer        NOT NULL,
    item_nbr                 smallint       NOT NULL,
    response_text            text           NOT NULL,
    PRIMARY KEY (serial_nbr, item_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202560.course_survey_response_item_text OWNER to math;

-- DROP TABLE IF EXISTS term_202590.course_survey_response_item_text;
CREATE TABLE IF NOT EXISTS term_202590.course_survey_response_item_text (
    serial_nbr               integer        NOT NULL,
    item_nbr                 smallint       NOT NULL,
    response_text            text           NOT NULL,
    PRIMARY KEY (serial_nbr, item_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202590.course_survey_response_item_text OWNER to math;

-- DROP TABLE IF EXISTS term_dev.course_survey_response_item_text;
CREATE TABLE IF NOT EXISTS term_dev.course_survey_response_item_text (
    serial_nbr               integer        NOT NULL,
    item_nbr                 smallint       NOT NULL,
    response_text            text           NOT NULL,
    PRIMARY KEY (serial_nbr, item_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_dev.course_survey_response_item_text OWNER to math;

-- DROP TABLE IF EXISTS term_test.course_survey_response_item_text;
CREATE TABLE IF NOT EXISTS term_test.course_survey_response_item_text (
    serial_nbr               integer        NOT NULL,
    item_nbr                 smallint       NOT NULL,
    response_text            text           NOT NULL,
    PRIMARY KEY (serial_nbr, item_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_test.course_survey_response_item_text OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: lti_context
--
-- A LMS context (course) in which the LTI tool is configured.
--
--   USAGE: Created once, static.
--   EST. RECORDS: 20
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 120 bytes
--   EST. TOTAL SPACE: 120 bytes
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS term_202510.lti_course;
CREATE TABLE IF NOT EXISTS term_202510.lti_context (
    client_id                varchar(40)    NOT NULL,  -- The client ID provided by the LMS
    issuer                   varchar(250)   NOT NULL,  -- The issuer host name
    deployment_id            varchar(250)   NOT NULL,  -- The deployment ID
    context_id               varchar(250)   NOT NULL,  -- The LMS course context ID
    lms_course_id            varchar(40),              -- The LMS course ID (typically a small integer)
    lms_course_title         varchar(250),             -- The LMS course title
    PRIMARY KEY (client_id, issuer, deployment_id, context_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.lti_context OWNER to math;

-- DROP TABLE IF EXISTS term_202560.lti_context;
CREATE TABLE IF NOT EXISTS term_202560.lti_context (
    client_id                varchar(40)    NOT NULL,
    issuer                   varchar(250)   NOT NULL,
    deployment_id            varchar(250)   NOT NULL,
    context_id               varchar(250)   NOT NULL,
    lms_course_id            varchar(40),
    lms_course_title         varchar(250),
    PRIMARY KEY (client_id, issuer, deployment_id, context_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202560.lti_context OWNER to math;

-- DROP TABLE IF EXISTS term_202590.lti_context;
CREATE TABLE IF NOT EXISTS term_202590.lti_context (
    client_id                varchar(40)    NOT NULL,
    issuer                   varchar(250)   NOT NULL,
    deployment_id            varchar(250)   NOT NULL,
    context_id               varchar(250)   NOT NULL,
    lms_course_id            varchar(40),
    lms_course_title         varchar(250),
    PRIMARY KEY (client_id, issuer, deployment_id, context_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202590.lti_context OWNER to math;

-- DROP TABLE IF EXISTS term_dev.lti_context;
CREATE TABLE IF NOT EXISTS term_dev.lti_context (
    client_id                varchar(40)    NOT NULL,
    issuer                   varchar(250)   NOT NULL,
    deployment_id            varchar(250)   NOT NULL,
    context_id               varchar(250)   NOT NULL,
    lms_course_id            varchar(40),
    lms_course_title         varchar(250),
    PRIMARY KEY (client_id, issuer, deployment_id, context_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_dev.lti_context OWNER to math;

-- DROP TABLE IF EXISTS term_test.lti_context;
CREATE TABLE IF NOT EXISTS term_test.lti_context (
    client_id                varchar(40)    NOT NULL,
    issuer                   varchar(250)   NOT NULL,
    deployment_id            varchar(250)   NOT NULL,
    context_id               varchar(250)   NOT NULL,
    lms_course_id            varchar(40),
    lms_course_title         varchar(250),
    PRIMARY KEY (client_id, issuer, deployment_id, context_id)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_test.lti_context OWNER to math;

-- ------------------------------------------------------------------------------------------------
-- TABLE: lti_context_course_section
--
-- A connection between a deployment of a registered LTI tool within a context (LMS course) and a
-- CSU course section.
--
--   USAGE: Created once, static.
--   EST. RECORDS: 20
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 120 bytes
--   EST. TOTAL SPACE: 120 bytes
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS term_202510.lti_context_course_section;
CREATE TABLE IF NOT EXISTS term_202510.lti_context_course_section (
    client_id                varchar(40)    NOT NULL,  -- The client ID provided by the LMS
    issuer                   varchar(250)   NOT NULL,  -- The issuer host name
    deployment_id            varchar(250)   NOT NULL,  -- The deployment ID
    context_id               varchar(250)   NOT NULL,  -- The LMS course context ID
    course_id                char(10)       NOT NULL,  -- The institution course ID
    section_nbr              char(4)        NOT NULL,  -- The institution section number
    PRIMARY KEY (client_id, issuer, deployment_id, context_id, course_id, section_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.lti_context_course_section OWNER to math;

-- DROP TABLE IF EXISTS term_202560.lti_context_course_section;
CREATE TABLE IF NOT EXISTS term_202560.lti_context_course_section (
    client_id                varchar(40)    NOT NULL,
    issuer                   varchar(250)   NOT NULL,
    deployment_id            varchar(250)   NOT NULL,
    context_id               varchar(250)   NOT NULL,
    course_id                char(10)       NOT NULL,
    section_nbr              char(4)        NOT NULL,
    PRIMARY KEY (client_id, issuer, deployment_id, context_id, course_id, section_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202560.lti_context_course_section OWNER to math;

-- DROP TABLE IF EXISTS term_202590.lti_context_course_section;
CREATE TABLE IF NOT EXISTS term_202590.lti_context_course_section (
    client_id                varchar(40)    NOT NULL,
    issuer                   varchar(250)   NOT NULL,
    deployment_id            varchar(250)   NOT NULL,
    context_id               varchar(250)   NOT NULL,
    course_id                char(10)       NOT NULL,
    section_nbr              char(4)        NOT NULL,
    PRIMARY KEY (client_id, issuer, deployment_id, context_id, course_id, section_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202590.lti_context_course_section OWNER to math;

-- DROP TABLE IF EXISTS term_dev.lti_context_course_section;
CREATE TABLE IF NOT EXISTS term_dev.lti_context_course_section (
    client_id                varchar(40)    NOT NULL,
    issuer                   varchar(250)   NOT NULL,
    deployment_id            varchar(250)   NOT NULL,
    context_id               varchar(250)   NOT NULL,
    course_id                char(10)       NOT NULL,
    section_nbr              char(4)        NOT NULL,
    PRIMARY KEY (client_id, issuer, deployment_id, context_id, course_id, section_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_dev.lti_context_course_section OWNER to math;

-- DROP TABLE IF EXISTS term_test.lti_context_course_section;
CREATE TABLE IF NOT EXISTS term_test.lti_context_course_section (
    client_id                varchar(40)    NOT NULL,
    issuer                   varchar(250)   NOT NULL,
    deployment_id            varchar(250)   NOT NULL,
    context_id               varchar(250)   NOT NULL,
    course_id                char(10)       NOT NULL,
    section_nbr              char(4)        NOT NULL,
    PRIMARY KEY (client_id, issuer, deployment_id, context_id, course_id, section_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_test.lti_context_course_section OWNER to math;



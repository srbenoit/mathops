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
--   RETENTION: Stored in MAIN schema, retained.
--   EST. RECORD SIZE: 4 bytes
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS term_202510.which_db;
CREATE TABLE IF NOT EXISTS term_202510.which_db (
    descr               char(4)        NOT NULL,  -- 'PROD' or 'DEV' or 'TEST'
    PRIMARY KEY (descr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.which_db OWNER to math;
INSERT INTO term_202510.which_db (descr) values ('PROD');

-- DROP TABLE IF EXISTS term_202560.which_db;
CREATE TABLE IF NOT EXISTS term_202560.which_db (
    descr               char(4)        NOT NULL,
    PRIMARY KEY (descr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202560.which_db OWNER to math;
INSERT INTO term_202560.which_db (descr) values ('PROD');

-- DROP TABLE IF EXISTS term_202590.which_db;
CREATE TABLE IF NOT EXISTS term_202590.which_db (
    descr               char(4)        NOT NULL,
    PRIMARY KEY (descr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202590.which_db OWNER to math;
INSERT INTO term_202590.which_db (descr) values ('PROD');

-- DROP TABLE IF EXISTS term_dev.which_db;
CREATE TABLE IF NOT EXISTS term_dev.which_db (
    descr               char(4)        NOT NULL,
    PRIMARY KEY (descr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_dev.which_db OWNER to math;
INSERT INTO term_dev.which_db (descr) values ('DEV');

-- DROP TABLE IF EXISTS term_test.which_db;
CREATE TABLE IF NOT EXISTS term_test.which_db (
    descr               char(4)        NOT NULL,
    PRIMARY KEY (descr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_test.which_db OWNER to math;
INSERT INTO term_test.which_db (descr) values ('TEST');

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
    course_id            char(10)        NOT NULL,  -- The unique course ID (references standards_course)
    section_nbr          char(4)         NOT NULL,  -- The section number (from the registration system)
    crn                  char(6)         NOT NULL,  -- The CRN (from the registration system)
    aries_start_date     date            NOT NULL,  -- The "official" start date of the course
    aries_end_date       date            NOT NULL,  -- The "official" end date of the course
    first_class_date     date            NOT NULL,  -- The first date the course is available to students
    last_class_date      date            NOT NULL,  -- The last date the course is available to students
    instructor           varchar(30),               -- The name of the instructor assigned to the section
    campus               char(2),                   -- The campus code
    building_name        varchar(40),               -- The name of the building where class sessions meet
    room_nbr             varchar(20),               -- The room number where classes meet
    weekdays             smallint,                  -- The weekdays the class meets (logical OR of 1=Sun, 2=Mon, 4=Tue,
                                                    --     8=Wed, 16=Thu, 32=Fri, 64=Sat)
    a_min_score          smallint,                  -- The minimum score needed to earn an A grade
    b_min_score          smallint,                  -- The minimum score needed to earn a B grade
    c_min_score          smallint,                  -- The minimum score needed to earn a C grade
    d_min_score          smallint,                  -- The minimum score needed to earn a D grade
    survey_id            char(5),                   -- The ID of the student survey attached to the course
    canvas_id            varchar(40),               -- The ID of the associated Canvas course
    subterm              char(5),                   -- The subterm ('FULL', 'HALF1', 'HALF2', or 'NN:MM" for weeks)
    on_time_mastery_pts  smallint,                  -- The points awarded for mastering a standard on time
    late_mastery_pts     smallint,                  -- The points awarded for mastering a standard late
    homework_pts         smallint,                  -- The points awarded for passing a homework assignment
    PRIMARY KEY (course_id, section_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.standards_course_section OWNER to math;

-- DROP TABLE IF EXISTS term_202560.standards_course_section;
CREATE TABLE IF NOT EXISTS term_202560.standards_course_section (
    course_id            char(10)        NOT NULL,
    section_nbr          char(4)         NOT NULL,
    crn                  char(6)         NOT NULL,
    aries_start_date     date            NOT NULL,
    aries_end_date       date            NOT NULL,
    first_class_date     date            NOT NULL,
    last_class_date      date            NOT NULL,
    instructor           varchar(30),
    campus               char(2),
    building_name        varchar(40),
    room_nbr             varchar(20),
    weekdays             smallint,
    a_min_score          smallint,
    b_min_score          smallint,
    c_min_score          smallint,
    d_min_score          smallint,
    survey_id            char(5),
    canvas_id            varchar(40),
    subterm              char(5),
    on_time_mastery_pts  smallint,
    late_mastery_pts     smallint,
    homework_pts         smallint,
    PRIMARY KEY (course_id, section_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202560.standards_course_section OWNER to math;

-- DROP TABLE IF EXISTS term_202590.standards_course_section;
CREATE TABLE IF NOT EXISTS term_202590.standards_course_section (
    course_id            char(10)        NOT NULL,
    section_nbr          char(4)         NOT NULL,
    crn                  char(6)         NOT NULL,
    aries_start_date     date            NOT NULL,
    aries_end_date       date            NOT NULL,
    first_class_date     date            NOT NULL,
    last_class_date      date            NOT NULL,
    instructor           varchar(30),
    campus               char(2),
    building_name        varchar(40),
    room_nbr             varchar(20),
    weekdays             smallint,
    a_min_score          smallint,
    b_min_score          smallint,
    c_min_score          smallint,
    d_min_score          smallint,
    survey_id            char(5),
    canvas_id            varchar(40),
    subterm              char(5),
    on_time_mastery_pts  smallint,
    late_mastery_pts     smallint,
    homework_pts         smallint,
    PRIMARY KEY (course_id, section_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202590.standards_course_section OWNER to math;

-- DROP TABLE IF EXISTS term_dev.standards_course_section;
CREATE TABLE IF NOT EXISTS term_dev.standards_course_section (
    course_id            char(10)        NOT NULL,
    section_nbr          char(4)         NOT NULL,
    crn                  char(6)         NOT NULL,
    aries_start_date     date            NOT NULL,
    aries_end_date       date            NOT NULL,
    first_class_date     date            NOT NULL,
    last_class_date      date            NOT NULL,
    instructor           varchar(30),
    campus               char(2),
    building_name        varchar(40),
    room_nbr             varchar(20),
    weekdays             smallint,
    a_min_score          smallint,
    b_min_score          smallint,
    c_min_score          smallint,
    d_min_score          smallint,
    survey_id            char(5),
    canvas_id            varchar(40),
    subterm              char(5),
    on_time_mastery_pts  smallint,
    late_mastery_pts     smallint,
    homework_pts         smallint,
    PRIMARY KEY (course_id, section_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_dev.standards_course_section OWNER to math;

-- DROP TABLE IF EXISTS term_test.standards_course_section;
CREATE TABLE IF NOT EXISTS term_test.standards_course_section (
    course_id            char(10)        NOT NULL,
    section_nbr          char(4)         NOT NULL,
    crn                  char(6)         NOT NULL,
    aries_start_date     date            NOT NULL,
    aries_end_date       date            NOT NULL,
    first_class_date     date            NOT NULL,
    last_class_date      date            NOT NULL,
    instructor           varchar(30),
    campus               char(2),
    building_name        varchar(40),
    room_nbr             varchar(20),
    weekdays             smallint,
    a_min_score          smallint,
    b_min_score          smallint,
    c_min_score          smallint,
    d_min_score          smallint,
    survey_id            char(5),
    canvas_id            varchar(40),
    subterm              char(5),
    on_time_mastery_pts  smallint,
    late_mastery_pts     smallint,
    homework_pts         smallint,
    PRIMARY KEY (course_id, section_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_test.standards_course_section OWNER to math;


-- ------------------------------------------------------------------------------------------------
-- TABLE: standard_assignment_attempt
--
--   Each record defines a section of a standards-based course offered in a term
--
--   USAGE: One record per module, 8 per course.
--   EST. RECORDS: 25 * 3 * 15 = 1,125
--   RETENTION: Stored in TERM schema, retained for 15 years
--   EST. RECORD SIZE: 110 bytes
--   EST. TOTAL SPACE: 124 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS term_202510.standard_assignment_attempt;
CREATE TABLE IF NOT EXISTS term_202510.standard_assignment_attempt (
    student_id           char(9)         NOT NULL,  -- The student ID who submitted the attempt
    serial_nbr           integer         NOT NULL,  -- A unique serial number for the attempt
    assignment_id        varchar(20)     NOT NULL,  -- The ID of the assignment (references standard_assignment)
    course_id            char(10)        NOT NULL,  -- The course ID (copied from standard_assignment)
    module_nbr           char(10)        NOT NULL,  -- The course ID (copied from standard_assignment)
    standard_nbr         smallint        NOT NULL,  -- The standard number (copied from standard_assignment)
    pts_possible         smallint,                  -- The number of points possible (copied from standard_assignment)
    min_passing_score    smallint,                  -- The minimum passing score (copied from standard_assignment)
    score                smallint        NOT NULL,  -- The earned score
    passed               char(1)         NOT NULL,  -- "Y"  if passed, "N" if not passed, "G" to ignore, "P" if passed
                                                    --     but invalidated (say, for academic misconduct)
    PRIMARY KEY (student_id, serial_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.standard_assignment_attempt OWNER to math;

















-- ------------------------------------------------------------------------------------------------
-- TABLE: mastery_attempt
-- 
-- Each object represents a student's attempt on a mastery exam.  Note that a single proctored exam
-- delivered in a testing center may include multiple mastery exams merged into a single testing
-- session.  The serial number will be the same for all of these, but will be unique for each
-- proctored testing session, so the primary key for this record is the combination of serial number
-- and exam ID.
--
-- USAGE: Historical data - created once, updated rarely, queried rarely
-- EST. RECORDS: 600,000
-- RETENTION: Stored in TERM schema, retained as archive data
-- EST. RECORD SIZE: 50 bytes
-- EST. TOTAL SPACE: 30,000 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS sm23.mastery_attempt;
CREATE TABLE IF NOT EXISTS term_sm23.mastery_attempt (
    serial_nbr          integer        NOT NULL,  -- The serial number of the exam session
    exam_id             varchar(20)    NOT NULL,  -- The exam ID (references mastery_exam record)
    stu_id              char(9)        NOT NULL,  -- The ID of the student
    when_started        timestamp(0)   NOT NULL,  -- The date/time the exam was started
    when_finished       timestamp(0)   NOT NULL,  -- The date/time the exam was submitted
    exam_score          smallint       NOT NULL,  -- The exam score
    mastery_score       smallint,                 -- The mastery score
    passed              char(1)        NOT NULL,  -- "Y" if passed, "N" if not, "G" to ignore,
                                                  --   "P" if taken away
    is_first_passed     char(1),                  -- "Y" if this is the first passing attempt for
                                                  --   the student on the mastery exam, "N" if not
    exam_source         char(2),                  -- The exam source, if known
                                                  --   "RM" if remote
                                                  --   "TC" if from a testing center
                                                  --   "HG" if hand-graded
    PRIMARY KEY (serial_nbr, exam_id)
) TABLESPACE sm23_tbl;

-- DROP INDEX IF EXISTS sm23.mastery_attempt_ix1;
CREATE INDEX IF NOT EXISTS mastery_attempt_ix1 ON sm23.mastery_attempt (
    stu_id
) TABLESPACE sm23_idx;

-- DROP TABLE IF EXISTS term_d.mastery_attempt;
CREATE TABLE IF NOT EXISTS term_d.mastery_attempt (
    serial_nbr          integer        NOT NULL,
    exam_id             varchar(20)    NOT NULL,
    stu_id              char(9)        NOT NULL,
    when_started        timestamp(0)   NOT NULL,
    when_finished       timestamp(0)   NOT NULL,
    exam_score          smallint       NOT NULL,
    mastery_score       smallint,
    passed              char(1)        NOT NULL,
    is_first_passed     char(1),
    exam_source         char(2),
    PRIMARY KEY (serial_nbr, exam_id)
) TABLESPACE dev_tbl;

-- DROP INDEX IF EXISTS term_d.mastery_attempt_ix1;
CREATE INDEX IF NOT EXISTS mastery_attempt_ix1 ON term_d.mastery_attempt (
    stu_id
) TABLESPACE dev_idx;

-- DROP TABLE IF EXISTS term_t.mastery_attempt;
CREATE TABLE IF NOT EXISTS term_t.mastery_attempt (
    serial_nbr          integer        NOT NULL,
    exam_id             varchar(20)    NOT NULL,
    stu_id              char(9)        NOT NULL,
    when_started        timestamp(0)   NOT NULL,
    when_finished       timestamp(0)   NOT NULL,
    exam_score          smallint       NOT NULL,
    mastery_score       smallint,
    passed              char(1)        NOT NULL,
    is_first_passed     char(1),
    exam_source         char(2),
    PRIMARY KEY (serial_nbr, exam_id)
) TABLESPACE test;



-- ------------------------------------------------------------------------------------------------
-- TABLE: mastery_attempt_qa
-- 
-- Answers to individual questions on a mastery attempt.
--
-- USAGE: Historical data - created once, updated rarely, queried rarely
-- EST. RECORDS: 1,200,000
-- RETENTION: Stored in TERM schema, retained as archive data
-- EST. RECORD SIZE: 16 bytes
-- EST. TOTAL SPACE: 19,200 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS sm23.mastery_attempt_qa;
CREATE TABLE IF NOT EXISTS term_sm23.mastery_attempt_qa (
    serial_nbr          integer        NOT NULL,  -- The serial number of the exam session
    exam_id             varchar(20)    NOT NULL,  -- The exam ID (references mastery_exam record)
    question_nbr        smallint       NOT NULL,  -- The question number
    correct             char(1)        NOT NULL,  -- "Y" if correct, "N" if not
    PRIMARY KEY (serial_nbr, exam_id, question_nbr)
) TABLESPACE sm23_tbl;

-- DROP TABLE IF EXISTS term_d.mastery_attempt_qa;
CREATE TABLE IF NOT EXISTS term_d.mastery_attempt_qa (
    serial_nbr          integer        NOT NULL,
    exam_id             varchar(20)    NOT NULL,
    question_nbr        smallint       NOT NULL,
    correct             char(1)        NOT NULL,
    PRIMARY KEY (serial_nbr, exam_id, question_nbr)
) TABLESPACE dev_tbl;

-- DROP TABLE IF EXISTS term_t.mastery_attempt_qa;
CREATE TABLE IF NOT EXISTS term_t.mastery_attempt_qa (
    serial_nbr          integer        NOT NULL,
    exam_id             varchar(20)    NOT NULL,
    question_nbr        smallint       NOT NULL,
    correct             char(1)        NOT NULL,
    PRIMARY KEY (serial_nbr, exam_id, question_nbr)
) TABLESPACE test;



-- ------------------------------------------------------------------------------------------------
-- TABLE: stu_course_mastery
-- 
-- A student's mastery status in a standards-based course.
--
-- USAGE: Created when course is started, updated throughout term as status changes
-- EST. RECORDS: 12,000
-- RETENTION: Stored in TERM schema, retained as archive data
-- EST. RECORD SIZE: 32 bytes
-- EST. TOTAL SPACE: 384 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS sm23.stu_course_mastery;
CREATE TABLE IF NOT EXISTS term_sm23.stu_course_mastery (
    stu_id              char(9)        NOT NULL,  -- The student ID
    course_id           char(10)       NOT NULL,  -- The course ID
    score               smallint       NOT NULL,  -- The current score
    nbr_mastered_h1     smallint       NOT NULL,  -- The number mastered in first half
    nbr_mastered_h2     smallint       NOT NULL,  -- The number mastered in second half
    nbr_eligible        smallint       NOT NULL,  -- The number eligible but not yet mastered
    explor_1_status     char(2),                  -- Status in exploration 1
    explor_2_status     char(2),                  -- Status in exploration 2
    PRIMARY KEY (stu_id, course_id)
) TABLESPACE sm23_tbl;

-- DROP TABLE IF EXISTS term_d.stu_course_mastery;
CREATE TABLE IF NOT EXISTS term_d.stu_course_mastery (
    stu_id              char(9)        NOT NULL,
    course_id           char(10)       NOT NULL,
    score               smallint       NOT NULL,
    nbr_mastered_h1     smallint       NOT NULL,
    nbr_mastered_h2     smallint       NOT NULL,
    nbr_eligible        smallint       NOT NULL,
    explor_1_status     char(2),
    explor_2_status     char(2),
    PRIMARY KEY (stu_id, course_id)
) TABLESPACE dev_tbl;

-- DROP TABLE IF EXISTS term_t.stu_course_mastery;
CREATE TABLE IF NOT EXISTS term_t.stu_course_mastery (
    stu_id              char(9)        NOT NULL,
    course_id           char(10)       NOT NULL,
    score               smallint       NOT NULL,
    nbr_mastered_h1     smallint       NOT NULL,
    nbr_mastered_h2     smallint       NOT NULL,
    nbr_eligible        smallint       NOT NULL,
    explor_1_status     char(2),
    explor_2_status     char(2),
    PRIMARY KEY (stu_id, course_id)
) TABLESPACE test;



-- ------------------------------------------------------------------------------------------------
-- TABLE: stu_unit_mastery
-- 
-- A student's mastery status in a single unit of a standards-based course.
--
-- USAGE: Created when course is started, updated throughout term as status changes
-- EST. RECORDS: 120,000
-- RETENTION: Stored in TERM schema, retained as archive data
-- EST. RECORD SIZE: 34 bytes
-- EST. TOTAL SPACE: 4,080 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS sm23.stu_unit_mastery;
CREATE TABLE IF NOT EXISTS term_sm23.stu_unit_mastery (
    stu_id              char(9)        NOT NULL,  -- The student ID
    course_id           char(10)       NOT NULL,  -- The course ID
    unit                smallint       NOT NULL,  -- The unit
    score               smallint       NOT NULL,  -- The current score
    sr_status           char(2),                  -- Status in Skills Review
    s1_status           char(3),                  -- Status in Standard 1
    s2_status           char(3),                  -- Status in Standard 2
    s3_status           char(3),                  -- Status in Standard 3
    PRIMARY KEY (stu_id, course_id, unit)
) TABLESPACE sm23_tbl;

-- DROP TABLE IF EXISTS term_d.stu_unit_mastery;
CREATE TABLE IF NOT EXISTS term_d.stu_unit_mastery (
    stu_id              char(9)        NOT NULL,
    course_id           char(10)       NOT NULL,
    unit                smallint       NOT NULL,
    score               smallint       NOT NULL,
    sr_status           char(2),
    s1_status           char(3),
    s2_status           char(3),
    s3_status           char(3),
    PRIMARY KEY (stu_id, course_id, unit)
) TABLESPACE dev_tbl;

-- DROP TABLE IF EXISTS term_t.stu_unit_mastery;
CREATE TABLE IF NOT EXISTS term_t.stu_unit_mastery (
    stu_id              char(9)        NOT NULL,
    course_id           char(10)       NOT NULL,
    unit                smallint       NOT NULL,
    score               smallint       NOT NULL,
    sr_status           char(2),
    s1_status           char(3),
    s2_status           char(3),
    s3_status           char(3),
    PRIMARY KEY (stu_id, course_id, unit)
) TABLESPACE test;



-- ------------------------------------------------------------------------------------------------
-- TABLE: std_milestone
-- 
-- The configured milestones for standards-based courses, where each pace and pace track defines
-- a new set of milestones for all courses in the track.
--
-- USAGE: Created with each new term.  Updated only for events like weather closures.
-- EST. RECORDS: 390 (13 per course in a pace/track * 15 pace/index * 2 tracks) 
-- RETENTION: Stored in TERM schema, retained as archive data
-- EST. RECORD SIZE: 20 bytes
-- EST. TOTAL SPACE: 8 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS sm23.std_milestone;
CREATE TABLE IF NOT EXISTS term_sm23.std_milestone (
    pace_track          char(1)        NOT NULL,  -- The pace track
    pace                smallint       NOT NULL,  -- The pace
    pace_index          smallint       NOT NULL,  -- The pace index
    unit                smallint       NOT NULL,  -- The unit
    objective           smallint       NOT NULL,  -- The objective
    ms_type             char(2)        NOT NULL,  -- The milestone type
    ms_date             date,                     -- The milestone date
    PRIMARY KEY (pace_track, pace, pace_index, unit, objective, ms_type)
) TABLESPACE sm23_tbl;

-- DROP TABLE IF EXISTS term_d.std_milestone;
CREATE TABLE IF NOT EXISTS term_d.std_milestone (
    pace_track          char(1)        NOT NULL,
    pace                smallint       NOT NULL,
    pace_index          smallint       NOT NULL,
    unit                smallint       NOT NULL,
    objective           smallint       NOT NULL,
    ms_type             char(2)        NOT NULL,
    ms_date             date,
    PRIMARY KEY (pace_track, pace, pace_index, unit, objective, ms_type)
) TABLESPACE dev_tbl;

-- DROP TABLE IF EXISTS term_t.std_milestone;
CREATE TABLE IF NOT EXISTS term_t.std_milestone (
    pace_track          char(1)        NOT NULL,
    pace                smallint       NOT NULL,
    pace_index          smallint       NOT NULL,
    unit                smallint       NOT NULL,
    objective           smallint       NOT NULL,
    ms_type             char(2)        NOT NULL,
    ms_date             date,
    PRIMARY KEY (pace_track, pace, pace_index, unit, objective, ms_type)
) TABLESPACE test;



-- ------------------------------------------------------------------------------------------------
-- TABLE: stu_std_milestone
-- 
-- An override for a particular standard milestone for a student.
--
-- USAGE: Created (or updated) as extensions are given to students for SDC accommodations or
--        extenuating circumstances.
-- EST. RECORDS: 1000 
-- RETENTION: Stored in TERM schema, retained as archive data
-- EST. RECORD SIZE: 30 bytes
-- EST. TOTAL SPACE: 30 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS sm23.stu_std_milestone;
CREATE TABLE IF NOT EXISTS term_sm23.stu_std_milestone (
    stu_id              char(9)        NOT NULL,  -- The student ID
    pace_track          char(1)        NOT NULL,  -- The pace track
    pace                smallint       NOT NULL,  -- The pace
    pace_index          smallint       NOT NULL,  -- The pace index
    unit                smallint       NOT NULL,  -- The unit
    objective           smallint       NOT NULL,  -- The objective
    ms_type             char(2)        NOT NULL,  -- The milestone type
    ms_date             date           NOT NULL,  -- The milestone date
    PRIMARY KEY (stu_id, pace_track, pace, pace_index, unit, objective, ms_type)
) TABLESPACE sm23_tbl;

-- DROP TABLE IF EXISTS term_d.stu_std_milestone;
CREATE TABLE IF NOT EXISTS term_d.stu_std_milestone (
    stu_id              char(9)        NOT NULL,
    pace_track          char(1)        NOT NULL,
    pace                smallint       NOT NULL,
    pace_index          smallint       NOT NULL,
    unit                smallint       NOT NULL,
    objective           smallint       NOT NULL,
    ms_type             char(2)        NOT NULL,
    ms_date             date           NOT NULL,
    PRIMARY KEY (stu_id, pace_track, pace, pace_index, unit, objective, ms_type)
) TABLESPACE dev_tbl;

-- DROP TABLE IF EXISTS term_t.stu_std_milestone;
CREATE TABLE IF NOT EXISTS term_t.stu_std_milestone (
    stu_id              char(9)        NOT NULL,
    pace_track          char(1)        NOT NULL,
    pace                smallint       NOT NULL,
    pace_index          smallint       NOT NULL,
    unit                smallint       NOT NULL,
    objective           smallint       NOT NULL,
    ms_type             char(2)        NOT NULL,
    ms_date             date           NOT NULL,
    PRIMARY KEY (stu_id, pace_track, pace, pace_index, unit, objective, ms_type)
) TABLESPACE test;













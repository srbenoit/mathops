-- postgres_schema_create.sql
-- (this script is designed to be run under the 'math' database owner)
-- /opt/postgresql/bin/psql -d math -U math

-- ------------------------------------------------------------------------------------------------
-- Before executing this script, ensure that all actions in 'postgres_database_create.sql' have
-- been completed.
-- ------------------------------------------------------------------------------------------------

-- Create the schemas in the "math" database.

CREATE SCHEMA IF NOT EXISTS main;    -- Production main
CREATE SCHEMA IF NOT EXISTS main_d;  -- Development main
CREATE SCHEMA IF NOT EXISTS main_t;  -- Test main
CREATE SCHEMA IF NOT EXISTS term_d;  -- Development term
CREATE SCHEMA IF NOT EXISTS term_t;  -- Test term
CREATE SCHEMA IF NOT EXISTS anlyt;   -- Production analytics
CREATE SCHEMA IF NOT EXISTS anlyt_t; -- Test analytics
CREATE SCHEMA IF NOT EXISTS sp23;    -- SP23 term
CREATE SCHEMA IF NOT EXISTS sm23;    -- SM23 term
CREATE SCHEMA IF NOT EXISTS fa23;    -- FA23 term



-- ================================================================================================
-- Create schema objects within the 'main', 'main_d', and 'main_t' schemas. 
-- ================================================================================================

-- ------------------------------------------------------------------------------------------------
-- SERVICE: "Term"
--   TABLE: term
--
--   The configuration of a term.
--
--   USAGE: One record per term, a new record created each term.
--   EST. RECORDS: 60
--   RETENTION: Stored in MAIN schema, retained as archive data
--   EST. RECORD SIZE: 40 bytes
--   EST. TOTAL SPACE: 2 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS main.term;
CREATE TABLE IF NOT EXISTS main.term (
    term                integer        NOT NULL,  -- The term number, like "202390"
    start_date          date           NOT NULL,  -- The start date
    end_date            date           NOT NULL,  -- The end date
    academic_year       char(4)        NOT NULL,  -- The academic year, like "2324"
    active_index        smallint       NOT NULL,  -- The index within the sequence of terms, the
                                                  --   active term is 0, prior is -1, next is 1
    drop_deadline       date,                     -- The last date to drop
    withdraw_deadline   date,                     -- The last date to withdraw
    PRIMARY KEY (term)
) TABLESPACE main_tbl;

-- DROP TABLE IF EXISTS main_d.term;
CREATE TABLE IF NOT EXISTS main_d.term (
    term                integer        NOT NULL,
    start_date          date           NOT NULL,
    end_date            date           NOT NULL,
    academic_year       char(4)        NOT NULL,
    active_index        smallint       NOT NULL,
    drop_deadline       date,
    withdraw_deadline   date,
    PRIMARY KEY (term)
) TABLESPACE dev_tbl;

-- DROP TABLE IF EXISTS main_t.term;
CREATE TABLE IF NOT EXISTS main_t.term (
    term                integer        NOT NULL,
    start_date          date           NOT NULL,
    end_date            date           NOT NULL,
    academic_year       char(4)        NOT NULL,
    active_index        smallint       NOT NULL,
    drop_deadline       date,
    withdraw_deadline   date,
    PRIMARY KEY (term)
) TABLESPACE test;






-- ------------------------------------------------------------------------------------------------
-- TABLE: assignment
-- 
-- Each object represents an assignment that can be used in a course or tutorial.
--
-- USAGE: Configuration data - created once, updated rarely, queried frequently
-- EST. RECORDS: 530
-- RETENTION: Indefinite
-- EST. RECORD SIZE: 100 bytes
-- EST. TOTAL SPACE: 53 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS main.assignment;
CREATE TABLE IF NOT EXISTS main.assignment (
    assignment_id       varchar(20)    NOT NULL,  -- The assignment ID
    assignment_type     char(2)        NOT NULL,  -- The assignment type (EAssignmentType)
    course_id           char(10)       NOT NULL,  -- The course ID for which the assignment is
                                                  --   intended
    unit                smallint       NOT NULL,  -- The course unit for which the assignment is
                                                  --   intended
    objective           smallint       NOT NULL,  -- The course unit objective for which the
                                                  --   assignment is intended
    tree_ref            varchar(250),             -- The unique tree reference for the assignment,
                                                  --   to allow assignments to be organized as a tree
    title               varchar(60),              -- The assignment title
    when_active         timestamp(0),             -- The date/time the assignment was activated
    when_pulled         timestamp(0),             -- The date/time the assignment was deactivated
    PRIMARY KEY (assignment_id)
) TABLESPACE main_tbl;

-- DROP INDEX IF EXISTS main.assignment_ix1;
CREATE INDEX IF NOT EXISTS assignment_ix1 ON main.assignment (
    course_id
) TABLESPACE main_idx;

-- DROP TABLE IF EXISTS main_d.assignment;
CREATE TABLE IF NOT EXISTS main_d.assignment (
    assignment_id       varchar(20)    NOT NULL,
    assignment_type     char(2)        NOT NULL,
    course_id           char(10)       NOT NULL,
    unit                smallint       NOT NULL,
    objective           smallint       NOT NULL,
    tree_ref            varchar(250),
    title               varchar(60),
    when_active         timestamp(0),
    when_pulled         timestamp(0),
    PRIMARY KEY (assignment_id)
) TABLESPACE dev_tbl;

-- DROP INDEX IF EXISTS main_d.assignment_ix1;
CREATE INDEX IF NOT EXISTS assignment_ix1 ON main_d.assignment (
    course_id
) TABLESPACE dev_idx;

-- DROP TABLE IF EXISTS main_t.assignment;
CREATE TABLE IF NOT EXISTS main_t.assignment (
    assignment_id       varchar(20)    NOT NULL,
    assignment_type     char(2)        NOT NULL,
    course_id           char(10)       NOT NULL,
    unit                smallint       NOT NULL,
    objective           smallint       NOT NULL,
    tree_ref            varchar(250),
    title               varchar(60),
    when_active         timestamp(0),
    when_pulled         timestamp(0),
    PRIMARY KEY (assignment_id)
) TABLESPACE test;



-- ------------------------------------------------------------------------------------------------
-- TABLE: mastery_exam
-- 
-- Each object represents a mastery exam that can be used in a course.
--
-- USAGE: Configuration data - created once, updated rarely, queried frequently
-- EST. RECORDS: 150
-- RETENTION: Indefinite
-- EST. RECORD SIZE: 130 bytes
-- EST. TOTAL SPACE: 20 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS main.mastery_exam;
CREATE TABLE IF NOT EXISTS main.mastery_exam (
    exam_id             varchar(20)    NOT NULL,  -- The exam ID
    exam_type           char(2)        NOT NULL,  -- The exam type (EExamType)
    course_id           char(10)       NOT NULL,  -- The course ID for which the exam is intended
    unit                smallint       NOT NULL,  -- The course unit for which the exam is intended
    objective           smallint       NOT NULL,  -- The course unit objective for which the exam
                                                  --   is intended
    tree_ref            varchar(250),             -- The unique tree reference for the exam, to
                                                  --   allow exams to be organized as a tree
    title               varchar(60),              -- The exam title
    button_label        varchar(50),              -- The label for the button to launch the exam
    when_active         timestamp(0),             -- The date/time the exam was activated
    when_pulled         timestamp(0),             -- The date/time the exam was deactivated
    PRIMARY KEY (exam_id)
) TABLESPACE main_tbl;

-- DROP INDEX IF EXISTS main.mastery_exam_ix1;
CREATE INDEX IF NOT EXISTS mastery_exam_ix1 ON main.mastery_exam (
    course_id
) TABLESPACE main_idx;

-- DROP TABLE IF EXISTS main_d.mastery_exam;
CREATE TABLE IF NOT EXISTS main_d.mastery_exam (
    exam_id             varchar(20)    NOT NULL,
    exam_type           char(2)        NOT NULL,
    course_id           char(10)       NOT NULL,
    unit                smallint       NOT NULL,
    objective           smallint       NOT NULL,
    tree_ref            varchar(250),
    title               varchar(60),
    button_label        varchar(50),
    when_active         timestamp(0),
    when_pulled         timestamp(0),
    PRIMARY KEY (exam_id)
) TABLESPACE dev_tbl;

-- DROP INDEX IF EXISTS main_d.mastery_exam_ix1;
CREATE INDEX IF NOT EXISTS mastery_exam_ix1 ON main_d.mastery_exam (
    course_id
) TABLESPACE dev_idx;

-- DROP TABLE IF EXISTS main_t.mastery_exam;
CREATE TABLE IF NOT EXISTS main_t.mastery_exam (
    exam_id             varchar(20)    NOT NULL,
    exam_type           char(2)        NOT NULL,
    course_id           char(10)       NOT NULL,
    unit                smallint       NOT NULL,
    objective           smallint       NOT NULL,
    tree_ref            varchar(250),
    title               varchar(60),
    button_label        varchar(50),
    when_active         timestamp(0),
    when_pulled         timestamp(0),
    PRIMARY KEY (exam_id)
) TABLESPACE test;



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
CREATE TABLE IF NOT EXISTS sm23.mastery_attempt (
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
CREATE TABLE IF NOT EXISTS sm23.mastery_attempt_qa (
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
CREATE TABLE IF NOT EXISTS sm23.stu_course_mastery (
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
CREATE TABLE IF NOT EXISTS sm23.stu_unit_mastery (
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
CREATE TABLE IF NOT EXISTS sm23.std_milestone (
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
CREATE TABLE IF NOT EXISTS sm23.stu_std_milestone (
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





-- ------------------------------------------------------------------------------------------------
-- TABLE: catalog_course
--
-- Each object represents a course found in the University catalog.
--
-- USAGE: Configuration data - created once, updated rarely, queried frequently
-- EST. RECORDS: 1000
-- RETENTION: Indefinite
-- EST. RECORD SIZE: 1024 bytes
-- EST. TOTAL SPACE: 1000 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS main.catalog_course;
CREATE TABLE IF NOT EXISTS main.catalog_course (
    course_id           char(10)       NOT NULL,  -- The course ID, like "MATH 126"
    prefix              char(4)        NOT NULL,  -- The course prefix, like "MATH"
    number              char(6)        NOT NULL,  -- The course number, like "126" (may contain letters)
    title               text,                     -- The course title
    description         text,                     -- The catalog description
    registration_info   text,                     -- The catalog registration information
    terms_offered       smallint,                 -- Bitwise OR of FALL(0x01), SPRING(0x02), SUMMER(0x04)
    grade_mode          smallint,                 -- A grade mode enumeration value
    special_course_fee  text,                     -- The catalog course fee information
    additional_info     text,                     -- The catalog additional information
    gt_code             char(4),                  -- GT code, like "MA1"
    min_credits         smallint       NOT NULL,  -- The minimum number of credits
    max_credits         smallint       NOT NULL,  -- The maximum number of credits
    PRIMARY KEY (course_id)
) TABLESPACE main_tbl;

-- DROP INDEX IF EXISTS main.catalog_course_ix1;
CREATE INDEX IF NOT EXISTS catalog_course_ix1 ON main.catalog_course (
    course_id
) TABLESPACE main_idx;

-- DROP TABLE IF EXISTS main_d.catalog_course;
CREATE TABLE IF NOT EXISTS main_d.catalog_course (
    course_id           char(10)       NOT NULL,  -- The course ID, like "MATH 126"
    prefix              char(4)        NOT NULL,  -- The course prefix, like "MATH"
    number              char(6)        NOT NULL,  -- The course number, like "126" (may contain letters)
    title               text,                     -- The course title
    description         text,                     -- The catalog description
    registration_info   text,                     -- The catalog registration information
    terms_offered       smallint,                 -- Bitwise OR of FALL(0x01), SPRING(0x02), SUMMER(0x04)
    grade_mode          smallint,                 -- A grade mode enumeration value
    special_course_fee  text,                     -- The catalog course fee information
    additional_info     text,                     -- The catalog additional information
    gt_code             char(4),                  -- GT code, like "MA1"
    min_credits         smallint       NOT NULL,  -- The minimum number of credits
    max_credits         smallint       NOT NULL,  -- The maximum number of credits
    PRIMARY KEY (course_id)
) TABLESPACE dev_tbl;

-- DROP TABLE IF EXISTS main_t.catalog_course;
CREATE TABLE IF NOT EXISTS main_t.catalog_course (
    course_id           char(10)       NOT NULL,  -- The course ID, like "MATH 126"
    prefix              char(4)        NOT NULL,  -- The course prefix, like "MATH"
    number              char(6)        NOT NULL,  -- The course number, like "126" (may contain letters)
    title               text,                     -- The course title
    description         text,                     -- The catalog description
    registration_info   text,                     -- The catalog registration information
    terms_offered       smallint,                 -- Bitwise OR of FALL(0x01), SPRING(0x02), SUMMER(0x04)
    grade_mode          smallint,                 -- A grade mode enumeration value
    special_course_fee  text,                     -- The catalog course fee information
    additional_info     text,                     -- The catalog additional information
    gt_code             char(4),                  -- GT code, like "MA1"
    min_credits         smallint       NOT NULL,  -- The minimum number of credits
    max_credits         smallint       NOT NULL,  -- The maximum number of credits
    PRIMARY KEY (course_id)
) TABLESPACE test;

-- ------------------------------------------------------------------------------------------------
-- TABLE: catalog_course_prereq
--
-- Each object represents a node in a prerequisite tree for a catalog course.  Leaf nodes define
-- individual courses, branch nodes specify an AND, OR, or "select N credits from" requirement,
-- with a child list of courses or subordinate prerequisites.
--
-- USAGE: Configuration data - created once, updated rarely, queried frequently
-- EST. RECORDS: 1000
-- RETENTION: Indefinite
-- EST. RECORD SIZE: 1024 bytes
-- EST. TOTAL SPACE: 1000 KB
-- ------------------------------------------------------------------------------------------------

-- DROP TABLE IF EXISTS main_d.catalog_course_prereq;
CREATE TABLE IF NOT EXISTS main.catalog_course_prereq (
    prereq_id           char(10)       NOT NULL,
    parent_prereq_id    char(10)       NOT NULL,
    prereq_type         char(1)        NOT NULL,
    prereq_course_id    char(10),
    prereq_min_grade    char(4),
    concurrent_allowed  char(1),
    PRIMARY KEY (prereq_id)
) TABLESPACE main_tbl;

-- DROP INDEX IF EXISTS main_d.catalog_course_prereq_ix1;
CREATE INDEX IF NOT EXISTS catalog_course_prereq_ix1 ON main.catalog_course_prereq (
    prereq_id
) TABLESPACE main_idx;

-- DROP TABLE IF EXISTS main_d.catalog_course_prereq;
CREATE TABLE IF NOT EXISTS main_d.catalog_course_prereq (
    prereq_id           char(10)       NOT NULL,
    parent_prereq_id    char(10)       NOT NULL,
    prereq_type         char(1)        NOT NULL,
    prereq_course_id    char(10),
    prereq_min_grade    char(4),
    concurrent_allowed  char(1),
    PRIMARY KEY (prereq_id)
) TABLESPACE dev_tbl;

-- DROP TABLE IF EXISTS main_t.catalog_course_prereq;
CREATE TABLE IF NOT EXISTS main_t.catalog_course_prereq (
    prereq_id           char(10)       NOT NULL,
    parent_prereq_id    char(10)       NOT NULL,
    prereq_type         char(1)        NOT NULL,
    prereq_course_id    char(10),
    prereq_min_grade    char(4),
    concurrent_allowed  char(1),
    PRIMARY KEY (prereq_id)
) TABLESPACE test;










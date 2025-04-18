-- postgres_term_data_load.sql
-- (this script is designed to be run under the 'math' database owner)
-- /opt/postgresql/bin/psql -d math -U math

-- ------------------------------------------------------------------------------------------------
-- Before executing this script, ensure that all actions in 'postgres_database_create.sql' and
-- 'postgres_term_schema_create.sql' have been completed.
-- ------------------------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------------------------
-- TABLE: standards_course_grading_system
-- ------------------------------------------------------------------------------------------------

INSERT INTO term_202510.standards_course_grading_system (grading_system_id,nbr_standards,min_standards,
  nbr_essential_standards, min_essential_standards, homework_pts, on_time_mastery_pts, late_mastery_pts,
  a_min_score, b_min_score, c_min_score, d_min_score, u_min_score, min_standards_for_inc) VALUES (
  'S_24_6', 24, 18, 6, 5, 0, 5, 4, 108, 96, 85, 72, 12, 15);

INSERT INTO term_202560.standards_course_grading_system (grading_system_id,nbr_standards,min_standards,
  nbr_essential_standards, min_essential_standards, homework_pts, on_time_mastery_pts, late_mastery_pts,
  a_min_score, b_min_score, c_min_score, d_min_score, u_min_score, min_standards_for_inc) VALUES (
  'S_24_6', 24, 18, 6, 5, 0, 5, 4, 108, 96, 85, 72, 12, 15);

INSERT INTO term_202590.standards_course_grading_system (grading_system_id,nbr_standards,min_standards,
  nbr_essential_standards, min_essential_standards, homework_pts, on_time_mastery_pts, late_mastery_pts,
  a_min_score, b_min_score, c_min_score, d_min_score, u_min_score, min_standards_for_inc) VALUES (
  'S_24_6', 24, 18, 6, 5, 0, 5, 4, 108, 96, 85, 72, 12, 15);

INSERT INTO term_dev.standards_course_grading_system (grading_system_id,nbr_standards,min_standards,
  nbr_essential_standards, min_essential_standards, homework_pts, on_time_mastery_pts, late_mastery_pts,
  a_min_score, b_min_score, c_min_score, d_min_score, u_min_score, min_standards_for_inc) VALUES (
  'S_24_6', 24, 18, 6, 5, 0, 5, 4, 108, 96, 85, 72, 12, 15);

-- ------------------------------------------------------------------------------------------------
-- TABLE: standards_course_section
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
    subterm                   char(5)         NOT NULL,  -- The subterm ('FULL', 'HALF1', 'HALF2', 'NN:MM" for weeks)
    grading_system_id         char(6)         NOT NULL,  -- The grading system to use for the section
    campus                    char(2),                   -- The campus code
    canvas_id                 varchar(40),               -- The ID of the associated Canvas course
    instructor                varchar(30),               -- The name of the instructor assigned to the section
    building_name             varchar(40),               -- The name of the building where class sessions meet
    room_nbr                  varchar(20),               -- The room number where classes meet
    weekdays                  smallint,                  -- The weekdays the class meets (logical OR of 1=Sun, 2=Mon,
                                                         --     4=Tue, 8=Wed, 16=Thu, 32=Fri, 64=Sat)
    PRIMARY KEY (course_id, section_nbr)
) TABLESPACE primary_ts;
ALTER TABLE IF EXISTS term_202510.standards_course_section OWNER to math;










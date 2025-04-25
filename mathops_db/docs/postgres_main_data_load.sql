-- postgres_main_data_load.sql
-- (this script is designed to be run under the 'math' database owner)
-- /opt/postgresql/bin/psql -d math -U math

-- ------------------------------------------------------------------------------------------------
-- Before executing this script, ensure that all actions in 'postgres_database_create.sql' and
-- 'postgres_main_schema_create.sql' have been completed.
-- ------------------------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------------------------
-- TABLE: facility
-- ------------------------------------------------------------------------------------------------

INSERT INTO main.facility (facility_id,facility_name,building_name,room_nbr) VALUES (
  'PC_CENTER', 'Precalculus Center', 'Weber', '137');
INSERT INTO main.facility (facility_id,facility_name,building_name,room_nbr) VALUES (
  'PC_ONLINE', 'Precalculus Center Online Help', null, null);
INSERT INTO main.facility (facility_id,facility_name,building_name,room_nbr) VALUES (
  'PC_ALVS', 'In-Person Help in the Adult Learners and Veteran Services Office', 'LSC', '282');

INSERT INTO main_dev.facility (facility_id,facility_name,building_name,room_nbr) VALUES (
  'PC_CENTER', 'Precalculus Center', 'Weber', '137');
INSERT INTO main_dev.facility (facility_id,facility_name,building_name,room_nbr) VALUES (
  'PC_ONLINE', 'Precalculus Center Online Help', null, null);
INSERT INTO main_dev.facility (facility_id,facility_name,building_name,room_nbr) VALUES (
  'PC_ALVS', 'In-Person Help in the Adult Learners and Veteran Services Office', 'LSC', '282');

-- ------------------------------------------------------------------------------------------------
-- TABLE: facility_hours
-- ------------------------------------------------------------------------------------------------

INSERT INTO main.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_CENTER', 1, 2, '2025-01-21', '2025-05-09', '10:00:00', '16:00:00', null, null);
INSERT INTO main.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_CENTER', 2, 28, '2025-01-21', '2025-05-09', '10:00:00', '20:00:00', null, null);
INSERT INTO main.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_CENTER', 3, 32, '2025-01-21', '2025-05-09', '10:00:00', '16:00:00', null, null);
INSERT INTO main.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_CENTER', 4, 1, '2025-01-21', '2025-05-09', '12:00:00', '16:00:00', null, null);

INSERT INTO main.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_ONLINE', 1, 2, '2025-01-21', '2025-05-09', '13:00:00', '14:00:00', '15:00:00', '16:00:00');
INSERT INTO main.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_ONLINE', 2, 4, '2025-01-21', '2025-05-09', '11:00:00', '13:00:00', '15:00:00', '19:00:00');
INSERT INTO main.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_ONLINE', 3, 8, '2025-01-21', '2025-05-09', '13:00:00', '15:00:00', '17:00:00', '19:00:00');
INSERT INTO main.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_ONLINE', 4, 16, '2025-01-21', '2025-05-09', '10:00:00', '11:00:00', '15:00:00', '17:00:00');
INSERT INTO main.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_ONLINE', 5, 32, '2025-01-21', '2025-05-09', '11:00:00', '12:00:00', null, null);

INSERT INTO main.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_ALVS', 1, 2, '2025-01-28', '2025-05-09', '14:00:00', '16:00:00', null, null);
INSERT INTO main.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_ALVS', 2, 4, '2025-01-28', '2025-05-09', '12:00:00', '14:00:00', null, null);
INSERT INTO main.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_ALVS', 3, 16, '2025-01-28', '2025-05-09', '11:00:00', '13:00:00', null, null);

INSERT INTO main_dev.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_CENTER', 1, 2, '2025-01-21', '2025-05-09', '10:00:00', '16:00:00', null, null);
INSERT INTO main_dev.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_CENTER', 2, 28, '2025-01-21', '2025-05-09', '10:00:00', '20:00:00', null, null);
INSERT INTO main_dev.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_CENTER', 3, 32, '2025-01-21', '2025-05-09', '10:00:00', '16:00:00', null, null);
INSERT INTO main_dev.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_CENTER', 4, 1, '2025-01-21', '2025-05-09', '12:00:00', '16:00:00', null, null);

INSERT INTO main_dev.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_ONLINE', 1, 2, '2025-01-21', '2025-05-09', '13:00:00', '14:00:00', '15:00:00', '16:00:00');
INSERT INTO main_dev.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_ONLINE', 2, 4, '2025-01-21', '2025-05-09', '11:00:00', '13:00:00', '15:00:00', '19:00:00');
INSERT INTO main_dev.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_ONLINE', 3, 8, '2025-01-21', '2025-05-09', '13:00:00', '15:00:00', '17:00:00', '19:00:00');
INSERT INTO main_dev.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_ONLINE', 4, 16, '2025-01-21', '2025-05-09', '10:00:00', '11:00:00', '15:00:00', '17:00:00');
INSERT INTO main_dev.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_ONLINE', 5, 32, '2025-01-21', '2025-05-09', '11:00:00', '12:00:00', null, null);

INSERT INTO main_dev.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_ALVS', 1, 2, '2025-01-28', '2025-05-09', '14:00:00', '16:00:00', null, null);
INSERT INTO main_dev.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_ALVS', 2, 4, '2025-01-28', '2025-05-09', '12:00:00', '14:00:00', null, null);
INSERT INTO main_dev.facility_hours (facility_id, display_index, weekdays, start_date, end_date,
  open_time_1, close_time_1, open_time_2, close_time_2) VALUES (
  'PC_ALVS', 3, 16, '2025-01-28', '2025-05-09', '11:00:00', '13:00:00', null, null);

-- ------------------------------------------------------------------------------------------------
-- TABLE: facility_closure
-- ------------------------------------------------------------------------------------------------

INSERT INTO main.facility_closure (facility_id, start_date, end_date, closure_type, start_time, end_time) VALUES (
  'PC_CENTER', '2025-03-15', '2025-03-23', 'SP_BREAK', null, null);
INSERT INTO main.facility_closure (facility_id, start_date, end_date, closure_type, start_time, end_time) VALUES (
  'PC_ONLINE', '2025-03-15', '2025-03-23', 'SP_BREAK', null, null);
INSERT INTO main.facility_closure (facility_id, start_date, end_date, closure_type, start_time, end_time) VALUES (
  'PC_ALVS', '2025-03-15', '2025-03-23', 'SP_BREAK', null, null);

INSERT INTO main_dev.facility_closure (facility_id, start_date, end_date, closure_type, start_time, end_time) VALUES (
  'PC_CENTER', '2025-03-15', '2025-03-23', 'SP_BREAK', null, null);
INSERT INTO main_dev.facility_closure (facility_id, start_date, end_date, closure_type, start_time, end_time) VALUES (
  'PC_ONLINE', '2025-03-15', '2025-03-23', 'SP_BREAK', null, null);
INSERT INTO main_dev.facility_closure (facility_id, start_date, end_date, closure_type, start_time, end_time) VALUES (
  'PC_ALVS', '2025-03-15', '2025-03-23', 'SP_BREAK', null, null);

-- ------------------------------------------------------------------------------------------------
-- TABLE: standards_course
-- NOTE: These records SHOULD be created through the process of "installing" a course from a file
--       structure.
-- ------------------------------------------------------------------------------------------------

INSERT INTO main.standards_course (course_id, course_title, nbr_modules, nbr_credits, allow_lend, metadata_path)
  VALUES ('MATH 117', 'College Algebra in Context I', 8, 1, 15, '03_alg/MATH_117.json');
INSERT INTO main.standards_course (course_id, course_title, nbr_modules, nbr_credits, allow_lend, metadata_path)
  VALUES ('MATH 118', 'College Algebra in Context II', 8, 1, 15, '03_alg/MATH_118.json');
INSERT INTO main.standards_course (course_id, course_title, nbr_modules, nbr_credits, allow_lend, metadata_path)
  VALUES ('MATH 124', 'Logarithmic and Exponential Functions', 8, 1, 15, '04_logexp/MATH_124.json');
INSERT INTO main.standards_course (course_id, course_title, nbr_modules, nbr_credits, allow_lend, metadata_path)
  VALUES ('MATH 125', 'Numerical Trigonometry', 8, 1, 15, '05_trig/MATH_125.json');
INSERT INTO main.standards_course (course_id, course_title, nbr_modules, nbr_credits, allow_lend, metadata_path)
  VALUES ('MATH 126', 'Analytic Trigonometry', 8, 1, 15, '05_trig/MATH_126.json');

INSERT INTO main_dev.standards_course (course_id, course_title, nbr_modules, nbr_credits, allow_lend, metadata_path)
  VALUES ('MATH 117', 'College Algebra in Context I', 8, 1, 15, '03_alg/MATH_117.json');
INSERT INTO main_dev.standards_course (course_id, course_title, nbr_modules, nbr_credits, allow_lend, metadata_path)
  VALUES ('MATH 118', 'College Algebra in Context II', 8, 1, 15, '03_alg/MATH_118.json');
INSERT INTO main_dev.standards_course (course_id, course_title, nbr_modules, nbr_credits, allow_lend, metadata_path)
  VALUES ('MATH 124', 'Logarithmic and Exponential Functions', 8, 1, 15, '04_logexp/MATH_124.json');
INSERT INTO main_dev.standards_course (course_id, course_title, nbr_modules, nbr_credits, allow_lend, metadata_path)
  VALUES ('MATH 125', 'Numerical Trigonometry', 8, 1, 15, '05_trig/MATH_125.json');
INSERT INTO main_dev.standards_course (course_id, course_title, nbr_modules, nbr_credits, allow_lend, metadata_path)
  VALUES ('MATH 126', 'Analytic Trigonometry', 8, 1, 15, '05_trig/MATH_126.json');

-- ------------------------------------------------------------------------------------------------
-- TABLE: standards_course_module
-- NOTE: These records SHOULD be created through the process of "installing" a course from a file
--       structure.
-- ------------------------------------------------------------------------------------------------

INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 117', 1, 3, 0, '03_alg/01_quantities');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 117', 2, 3, 0, '03_alg/02_relations_graphs');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 117', 3, 3, 0, '03_alg/03_functions');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 117', 4, 3, 0, '03_alg/04_linear_fxns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 117', 5, 3, 0, '03_alg/05_quadratic_fxns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 117', 6, 3, 0, '03_alg/06_inverse_fxns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 117', 7, 3, 0, '03_alg/07_rate_of_change');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 117', 8, 3, 0, '03_alg/08_apps_linear_quadratic');

INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 118', 1, 3, 0, '03_alg/09_piecewise');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 118', 2, 3, 0, '03_alg/10_polynomial');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 118', 3, 3, 0, '03_alg/11_rational_expr');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 118', 4, 3, 0, '03_alg/12_rational_fxns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 118', 5, 3, 0, '03_alg/13_variation');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 118', 6, 3, 0, '03_alg/14_apps_functions');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 118', 7, 3, 0, '03_alg/15_systems');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 118', 8, 3, 0, '03_alg/16_solving_systems');

INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 124', 1, 3, 0, '04_logexp/01_discrete');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 124', 2, 3, 0, '04_logexp/02_exp_expr');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 124', 3, 3, 0, '04_logexp/03_exp_fxns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 124', 4, 3, 0, '04_logexp/04_exp_apps');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 124', 5, 3, 0, '04_logexp/05_logs');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 124', 6, 3, 0, '04_logexp/06_log_fxns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 124', 7, 3, 0, '04_logexp/07_log_apps');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 124', 8, 3, 0, '04_logexp/08_series');

INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 125', 1, 3, 1, '05_trig/01_angles');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 125', 2, 3, 1, '05_trig/02_triangles');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 125', 3, 3, 0, '05_trig/03_unit_circle');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 125', 4, 3, 1, '05_trig/04_trig_fxns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 125', 5, 3, 0, '05_trig/05_transform');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 125', 6, 3, 2, '05_trig/06_right_triangle');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 125', 7, 3, 1, '05_trig/07_inv_trig_fxns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 125', 8, 3, 1, '05_trig/08_law_of_sines_cosines');

INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 126', 1, 3, 1, '05_trig/09_basic_ident');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 126', 2, 3, 0, '05_trig/10_sum_diff_ident');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 126', 3, 3, 1, '05_trig/11_mult_half_angle_ident');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 126', 4, 3, 1, '05_trig/12_trig_eqns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 126', 5, 3, 1, '05_trig/13_polar_coords');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 126', 6, 3, 0, '05_trig/14_polar_fxns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 126', 7, 3, 1, '05_trig/15_complex');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 126', 8, 3, 0, '05_trig/16_applications');

INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 117', 1, 3, 0, '03_alg/01_quantities');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 117', 2, 3, 0, '03_alg/02_relations_graphs');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 117', 3, 3, 0, '03_alg/03_functions');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 117', 4, 3, 0, '03_alg/04_linear_fxns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 117', 5, 3, 0, '03_alg/05_quadratic_fxns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 117', 6, 3, 0, '03_alg/06_inverse_fxns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 117', 7, 3, 0, '03_alg/07_rate_of_change');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 117', 8, 3, 0, '03_alg/08_apps_linear_quadratic');

INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 118', 1, 3, 0, '03_alg/09_piecewise');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 118', 2, 3, 0, '03_alg/10_polynomial');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 118', 3, 3, 0, '03_alg/11_rational_expr');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 118', 4, 3, 0, '03_alg/12_rational_fxns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 118', 5, 3, 0, '03_alg/13_variation');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 118', 6, 3, 0, '03_alg/14_apps_functions');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 118', 7, 3, 0, '03_alg/15_systems');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 118', 8, 3, 0, '03_alg/16_solving_systems');

INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 124', 1, 3, 0, '04_logexp/01_discrete');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 124', 2, 3, 0, '04_logexp/02_exp_expr');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 124', 3, 3, 0, '04_logexp/03_exp_fxns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 124', 4, 3, 0, '04_logexp/04_exp_apps');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 124', 5, 3, 0, '04_logexp/05_logs');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 124', 6, 3, 0, '04_logexp/06_log_fxns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 124', 7, 3, 0, '04_logexp/07_log_apps');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 124', 8, 3, 0, '04_logexp/08_series');

INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 125', 1, 3, 1, '05_trig/01_angles');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 125', 2, 3, 1, '05_trig/02_triangles');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 125', 3, 3, 0, '05_trig/03_unit_circle');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 125', 4, 3, 1, '05_trig/04_trig_fxns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 125', 5, 3, 0, '05_trig/05_transform');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 125', 6, 3, 2, '05_trig/06_right_triangle');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 125', 7, 3, 1, '05_trig/07_inv_trig_fxns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 125', 8, 3, 1, '05_trig/08_law_of_sines_cosines');

INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 126', 1, 3, 1, '05_trig/09_basic_ident');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 126', 2, 3, 0, '05_trig/10_sum_diff_ident');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 126', 3, 3, 1, '05_trig/11_mult_half_angle_ident');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 126', 4, 3, 1, '05_trig/12_trig_eqns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 126', 5, 3, 1, '05_trig/13_polar_coords');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 126', 6, 3, 0, '05_trig/14_polar_fxns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 126', 7, 3, 1, '05_trig/15_complex');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, nbr_essential, module_path) VALUES (
  'MATH 126', 8, 3, 0, '05_trig/16_applications');

-- ------------------------------------------------------------------------------------------------
-- TABLE: standards_course_standard
-- NOTE: These records SHOULD be created through the process of "installing" a course from a file
--       structure.
-- ------------------------------------------------------------------------------------------------
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 1, 1, 'I can classify and work with angles.', 'Y');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 1, 2, 'I can use the geometry of angles to find relationships between angles in a diagram.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 1, 3, 'I can work with angles in the standard position in the Cartesian plane.', 'N');

INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 2, 1, 'I can classify and work with triangles.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 2, 2, 'I can use relationships between similar triangles to calculate triangle side lengths.',
          'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 2, 3, 'I can apply the Pythagorean theorem to right triangles.', 'Y');

INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 3, 1, 'I can locate points on the unit circle.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 3, 2, 'I can interpret angle in terms of arc length.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 3, 3, 'I can work with sector area.', 'N');

INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 4, 1, 'I can define and interpret the six trigonometric functions.', 'Y');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 4, 2, 'I can graph and interpret graphs of the six trigonometric functions.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 4, 3, 'I can evaluate trigonometric functions in several contexts.', 'N');

INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 5, 1,
  'I can find and interpret shifts and scalings of trigonometric functions, both graphically and algebraically.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 5, 2,
  'I can determine the algebraic form of a shifted and scaled trigonometric function from its graph.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 5, 3, 'I can model data or real-world phenomena using sine and cosine functions.', 'N');

INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 6, 1,
  'I can express the relationships between side lengths of a right triangle using trigonometric functions.', 'Y');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 6, 2, 'I can use trigonometric functions to calculate side lengths in right triangles.', 'Y');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 6, 3,
  'I can use the right triangle relationships for triangles in quadrants 2, 3, and 4 by finding reference angles.',
  'N');

INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 7, 1, 'I can work with inverse functions and identify when a function has an inverse.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 7, 2, 'I can work with inverse trigonometric functions.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 7, 3, 'I can apply inverse trigonometric functions to solve problems.', 'Y');

INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 8, 1, 'I can recall and apply the law of sines.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 8, 2, 'I can recall and apply the law of cosines.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 8, 3, 'I can solve general triangle problems.', 'Y');

INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 1, 1, 'I can identify and work with identities.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 1, 2, 'I can recall and apply various forms of the fundamental trigonometric identities.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 1, 3, 'I can use fundamental trigonometric identities to rewrite and simplify expressions.', 'Y');

INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 2, 1, 'I can interpret and apply the sum and difference identities.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 2, 2, 'I can use sum and difference identities to evaluate expressions.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 2, 3, 'I can apply product-to-sum and sum-to-product identities.', 'N');

INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 3, 1, 'I can interpret and apply double- and multiple-angle identities.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 3, 2, 'I can use half-angle identities.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 3, 3,
  'I can apply double-, multiple-, and half-angle identities to evaluate expressions and solve application problems.',
  'Y');

INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 4, 1, 'I can apply identities to find all solutions to trigonometric equations.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 4, 2, 'I can solve trigonometric equations.', 'Y');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 4, 3, 'I can evaluate compositions of trigonometric and inverse trigonometric functions.', 'N');

INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 5, 1, 'I can plot and interpret points represented in polar coordinates.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 5, 2, 'I can convert points in the plane between Cartesian and Polar coordinates.', 'Y');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 5, 3, 'I can use polar coordinates in application contexts.', 'N');

INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 6, 1, 'I can evaluate and graph polar functions and interpret polar data', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 6, 2,
   'I can plot and interpret polar equations and inequalities where radius is given as a function of angle.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 6, 3,
  'I can construct polar functions whose graphs have desired properties or model real-world forms.', 'N');

INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 7, 1, 'I can work with complex numbers in standard form.', 'Y');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 7, 2, 'I can work with complex numbers in trigonometric form.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 7, 3, 'I can work with complex numbers in exponential form.', 'N');

INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 8, 1, 'I can solve application problems that involve fixed or varying angles.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 8, 2, 'I can solve application problems that involve rotation.', 'N');
INSERT INTO main.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 8, 3, 'I can solve application problems that involve distances, arc length, or sector area.',
  'N');

INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 1, 1, 'I can classify and work with angles.', 'Y');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 1, 2, 'I can use the geometry of angles to find relationships between angles in a diagram.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 1, 3, 'I can work with angles in the standard position in the Cartesian plane.', 'N');

INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 2, 1, 'I can classify and work with triangles.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 2, 2, 'I can use relationships between similar triangles to calculate triangle side lengths.',
          'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 2, 3, 'I can apply the Pythagorean theorem to right triangles.', 'Y');

INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 3, 1, 'I can locate points on the unit circle.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 3, 2, 'I can interpret angle in terms of arc length.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 3, 3, 'I can work with sector area.', 'N');

INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 4, 1, 'I can define and interpret the six trigonometric functions.', 'Y');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 4, 2, 'I can graph and interpret graphs of the six trigonometric functions.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 4, 3, 'I can evaluate trigonometric functions in several contexts.', 'N');

INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 5, 1,
  'I can find and interpret shifts and scalings of trigonometric functions, both graphically and algebraically.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 5, 2,
  'I can determine the algebraic form of a shifted and scaled trigonometric function from its graph.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 5, 3, 'I can model data or real-world phenomena using sine and cosine functions.', 'N');

INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 6, 1,
  'I can express the relationships between side lengths of a right triangle using trigonometric functions.', 'Y');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 6, 2, 'I can use trigonometric functions to calculate side lengths in right triangles.', 'Y');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 6, 3,
  'I can use the right triangle relationships for triangles in quadrants 2, 3, and 4 by finding reference angles.',
  'N');

INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 7, 1, 'I can work with inverse functions and identify when a function has an inverse.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 7, 2, 'I can work with inverse trigonometric functions.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 7, 3, 'I can apply inverse trigonometric functions to solve problems.', 'Y');

INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 8, 1, 'I can recall and apply the law of sines.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 8, 2, 'I can recall and apply the law of cosines.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 125', 8, 3, 'I can solve general triangle problems.', 'Y');

INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 1, 1, 'I can identify and work with identities.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 1, 2, 'I can recall and apply various forms of the fundamental trigonometric identities.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 1, 3, 'I can use fundamental trigonometric identities to rewrite and simplify expressions.', 'Y');

INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 2, 1, 'I can interpret and apply the sum and difference identities.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 2, 2, 'I can use sum and difference identities to evaluate expressions.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 2, 3, 'I can apply product-to-sum and sum-to-product identities.', 'N');

INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 3, 1, 'I can interpret and apply double- and multiple-angle identities.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 3, 2, 'I can use half-angle identities.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 3, 3,
  'I can apply double-, multiple-, and half-angle identities to evaluate expressions and solve application problems.',
  'Y');

INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 4, 1, 'I can apply identities to find all solutions to trigonometric equations.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 4, 2, 'I can solve trigonometric equations.', 'Y');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 4, 3, 'I can evaluate compositions of trigonometric and inverse trigonometric functions.', 'N');

INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 5, 1, 'I can plot and interpret points represented in polar coordinates.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 5, 2, 'I can convert points in the plane between Cartesian and Polar coordinates.', 'Y');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 5, 3, 'I can use polar coordinates in application contexts.', 'N');

INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 6, 1, 'I can evaluate and graph polar functions and interpret polar data', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 6, 2,
   'I can plot and interpret polar equations and inequalities where radius is given as a function of angle.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 6, 3,
  'I can construct polar functions whose graphs have desired properties or model real-world forms.', 'N');

INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 7, 1, 'I can work with complex numbers in standard form.', 'Y');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 7, 2, 'I can work with complex numbers in trigonometric form.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 7, 3, 'I can work with complex numbers in exponential form.', 'N');

INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 8, 1, 'I can solve application problems that involve fixed or varying angles.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 8, 2, 'I can solve application problems that involve rotation.', 'N');
INSERT INTO main_dev.standards_course_standard (course_id, module_nbr, standard_nbr, learning_objective, is_essential)
  VALUES ('MATH 126', 8, 3, 'I can solve application problems that involve distances, arc length, or sector area.',
  'N');







-- ------------------------------------------------------------------------------------------------
-- TABLE: standard_assignment
-- ------------------------------------------------------------------------------------------------

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M1_S1_HW', 'HW', 'MATH 117', 1, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M1_S2_HW', 'HW', 'MATH 117', 1, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M1_S3_HW', 'HW', 'MATH 117', 1, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M1_S1_MA', 'MA', 'MATH 117', 1, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M1_S2_MA', 'MA', 'MATH 117', 1, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M1_S3_MA', 'MA', 'MATH 117', 1, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M2_S1_HW', 'HW', 'MATH 117', 2, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M2_S2_HW', 'HW', 'MATH 117', 2, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M2_S3_HW', 'HW', 'MATH 117', 2, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M2_S1_MA', 'MA', 'MATH 117', 2, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M2_S2_MA', 'MA', 'MATH 117', 2, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M2_S3_MA', 'MA', 'MATH 117', 2, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M3_S1_HW', 'HW', 'MATH 117', 3, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M3_S2_HW', 'HW', 'MATH 117', 3, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M3_S3_HW', 'HW', 'MATH 117', 3, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M3_S1_MA', 'MA', 'MATH 117', 3, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M3_S2_MA', 'MA', 'MATH 117', 3, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M3_S3_MA', 'MA', 'MATH 117', 3, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M4_S1_HW', 'HW', 'MATH 117', 4, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M4_S2_HW', 'HW', 'MATH 117', 4, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M4_S3_HW', 'HW', 'MATH 117', 4, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M4_S1_MA', 'MA', 'MATH 117', 4, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M4_S2_MA', 'MA', 'MATH 117', 4, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M4_S3_MA', 'MA', 'MATH 117', 4, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M5_S1_HW', 'HW', 'MATH 117', 5, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M5_S2_HW', 'HW', 'MATH 117', 5, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M5_S3_HW', 'HW', 'MATH 117', 5, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M5_S1_MA', 'MA', 'MATH 117', 5, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M5_S2_MA', 'MA', 'MATH 117', 5, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M5_S3_MA', 'MA', 'MATH 117', 5, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M6_S1_HW', 'HW', 'MATH 117', 6, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M6_S2_HW', 'HW', 'MATH 117', 6, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M6_S3_HW', 'HW', 'MATH 117', 6, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M6_S1_MA', 'MA', 'MATH 117', 6, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M6_S2_MA', 'MA', 'MATH 117', 6, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M6_S3_MA', 'MA', 'MATH 117', 6, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M7_S1_HW', 'HW', 'MATH 117', 7, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M7_S2_HW', 'HW', 'MATH 117', 7, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M7_S3_HW', 'HW', 'MATH 117', 7, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M7_S1_MA', 'MA', 'MATH 117', 7, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M7_S2_MA', 'MA', 'MATH 117', 7, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M7_S3_MA', 'MA', 'MATH 117', 7, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M8_S1_HW', 'HW', 'MATH 117', 8, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M8_S2_HW', 'HW', 'MATH 117', 8, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M8_S3_HW', 'HW', 'MATH 117', 8, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M8_S1_MA', 'MA', 'MATH 117', 8, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M8_S2_MA', 'MA', 'MATH 117', 8, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M8_S3_MA', 'MA', 'MATH 117', 8, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M1_S1_HW', 'HW', 'MATH 118', 1, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M1_S2_HW', 'HW', 'MATH 118', 1, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M1_S3_HW', 'HW', 'MATH 118', 1, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M1_S1_MA', 'MA', 'MATH 118', 1, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M1_S2_MA', 'MA', 'MATH 118', 1, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M1_S3_MA', 'MA', 'MATH 118', 1, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M2_S1_HW', 'HW', 'MATH 118', 2, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M2_S2_HW', 'HW', 'MATH 118', 2, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M2_S3_HW', 'HW', 'MATH 118', 2, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M2_S1_MA', 'MA', 'MATH 118', 2, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M2_S2_MA', 'MA', 'MATH 118', 2, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M2_S3_MA', 'MA', 'MATH 118', 2, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M3_S1_HW', 'HW', 'MATH 118', 3, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M3_S2_HW', 'HW', 'MATH 118', 3, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M3_S3_HW', 'HW', 'MATH 118', 3, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M3_S1_MA', 'MA', 'MATH 118', 3, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M3_S2_MA', 'MA', 'MATH 118', 3, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M3_S3_MA', 'MA', 'MATH 118', 3, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M4_S1_HW', 'HW', 'MATH 118', 4, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M4_S2_HW', 'HW', 'MATH 118', 4, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M4_S3_HW', 'HW', 'MATH 118', 4, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M4_S1_MA', 'MA', 'MATH 118', 4, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M4_S2_MA', 'MA', 'MATH 118', 4, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M4_S3_MA', 'MA', 'MATH 118', 4, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M5_S1_HW', 'HW', 'MATH 118', 5, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M5_S2_HW', 'HW', 'MATH 118', 5, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M5_S3_HW', 'HW', 'MATH 118', 5, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M5_S1_MA', 'MA', 'MATH 118', 5, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M5_S2_MA', 'MA', 'MATH 118', 5, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M5_S3_MA', 'MA', 'MATH 118', 5, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M6_S1_HW', 'HW', 'MATH 118', 6, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M6_S2_HW', 'HW', 'MATH 118', 6, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M6_S3_HW', 'HW', 'MATH 118', 6, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M6_S1_MA', 'MA', 'MATH 118', 6, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M6_S2_MA', 'MA', 'MATH 118', 6, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M6_S3_MA', 'MA', 'MATH 118', 6, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M7_S1_HW', 'HW', 'MATH 118', 7, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M7_S2_HW', 'HW', 'MATH 118', 7, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M7_S3_HW', 'HW', 'MATH 118', 7, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M7_S1_MA', 'MA', 'MATH 118', 7, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M7_S2_MA', 'MA', 'MATH 118', 7, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M7_S3_MA', 'MA', 'MATH 118', 7, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M8_S1_HW', 'HW', 'MATH 118', 8, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M8_S2_HW', 'HW', 'MATH 118', 8, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M8_S3_HW', 'HW', 'MATH 118', 8, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M8_S1_MA', 'MA', 'MATH 118', 8, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M8_S2_MA', 'MA', 'MATH 118', 8, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M8_S3_MA', 'MA', 'MATH 118', 8, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M1_S1_HW', 'HW', 'MATH 124', 1, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M1_S2_HW', 'HW', 'MATH 124', 1, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M1_S3_HW', 'HW', 'MATH 124', 1, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M1_S1_MA', 'MA', 'MATH 124', 1, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M1_S2_MA', 'MA', 'MATH 124', 1, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M1_S3_MA', 'MA', 'MATH 124', 1, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M2_S1_HW', 'HW', 'MATH 124', 2, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M2_S2_HW', 'HW', 'MATH 124', 2, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M2_S3_HW', 'HW', 'MATH 124', 2, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M2_S1_MA', 'MA', 'MATH 124', 2, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M2_S2_MA', 'MA', 'MATH 124', 2, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M2_S3_MA', 'MA', 'MATH 124', 2, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M3_S1_HW', 'HW', 'MATH 124', 3, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M3_S2_HW', 'HW', 'MATH 124', 3, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M3_S3_HW', 'HW', 'MATH 124', 3, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M3_S1_MA', 'MA', 'MATH 124', 3, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M3_S2_MA', 'MA', 'MATH 124', 3, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M3_S3_MA', 'MA', 'MATH 124', 3, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M4_S1_HW', 'HW', 'MATH 124', 4, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M4_S2_HW', 'HW', 'MATH 124', 4, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M4_S3_HW', 'HW', 'MATH 124', 4, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M4_S1_MA', 'MA', 'MATH 124', 4, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M4_S2_MA', 'MA', 'MATH 124', 4, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M4_S3_MA', 'MA', 'MATH 124', 4, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M5_S1_HW', 'HW', 'MATH 124', 5, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M5_S2_HW', 'HW', 'MATH 124', 5, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M5_S3_HW', 'HW', 'MATH 124', 5, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M5_S1_MA', 'MA', 'MATH 124', 5, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M5_S2_MA', 'MA', 'MATH 124', 5, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M5_S3_MA', 'MA', 'MATH 124', 5, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M6_S1_HW', 'HW', 'MATH 124', 6, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M6_S2_HW', 'HW', 'MATH 124', 6, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M6_S3_HW', 'HW', 'MATH 124', 6, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M6_S1_MA', 'MA', 'MATH 124', 6, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M6_S2_MA', 'MA', 'MATH 124', 6, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M6_S3_MA', 'MA', 'MATH 124', 6, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M7_S1_HW', 'HW', 'MATH 124', 7, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M7_S2_HW', 'HW', 'MATH 124', 7, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M7_S3_HW', 'HW', 'MATH 124', 7, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M7_S1_MA', 'MA', 'MATH 124', 7, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M7_S2_MA', 'MA', 'MATH 124', 7, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M7_S3_MA', 'MA', 'MATH 124', 7, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M8_S1_HW', 'HW', 'MATH 124', 8, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M8_S2_HW', 'HW', 'MATH 124', 8, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M8_S3_HW', 'HW', 'MATH 124', 8, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M8_S1_MA', 'MA', 'MATH 124', 8, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M8_S2_MA', 'MA', 'MATH 124', 8, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M8_S3_MA', 'MA', 'MATH 124', 8, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M1_S1_HW', 'HW', 'MATH 125', 1, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M1_S2_HW', 'HW', 'MATH 125', 1, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M1_S3_HW', 'HW', 'MATH 125', 1, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M1_S1_MA', 'MA', 'MATH 125', 1, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M1_S2_MA', 'MA', 'MATH 125', 1, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M1_S3_MA', 'MA', 'MATH 125', 1, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M2_S1_HW', 'HW', 'MATH 125', 2, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M2_S2_HW', 'HW', 'MATH 125', 2, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M2_S3_HW', 'HW', 'MATH 125', 2, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M2_S1_MA', 'MA', 'MATH 125', 2, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M2_S2_MA', 'MA', 'MATH 125', 2, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M2_S3_MA', 'MA', 'MATH 125', 2, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M3_S1_HW', 'HW', 'MATH 125', 3, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M3_S2_HW', 'HW', 'MATH 125', 3, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M3_S3_HW', 'HW', 'MATH 125', 3, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M3_S1_MA', 'MA', 'MATH 125', 3, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M3_S2_MA', 'MA', 'MATH 125', 3, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M3_S3_MA', 'MA', 'MATH 125', 3, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M4_S1_HW', 'HW', 'MATH 125', 4, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M4_S2_HW', 'HW', 'MATH 125', 4, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M4_S3_HW', 'HW', 'MATH 125', 4, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M4_S1_MA', 'MA', 'MATH 125', 4, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M4_S2_MA', 'MA', 'MATH 125', 4, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M4_S3_MA', 'MA', 'MATH 125', 4, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M5_S1_HW', 'HW', 'MATH 125', 5, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M5_S2_HW', 'HW', 'MATH 125', 5, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M5_S3_HW', 'HW', 'MATH 125', 5, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M5_S1_MA', 'MA', 'MATH 125', 5, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M5_S2_MA', 'MA', 'MATH 125', 5, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M5_S3_MA', 'MA', 'MATH 125', 5, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M6_S1_HW', 'HW', 'MATH 125', 6, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M6_S2_HW', 'HW', 'MATH 125', 6, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M6_S3_HW', 'HW', 'MATH 125', 6, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M6_S1_MA', 'MA', 'MATH 125', 6, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M6_S2_MA', 'MA', 'MATH 125', 6, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M6_S3_MA', 'MA', 'MATH 125', 6, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M7_S1_HW', 'HW', 'MATH 125', 7, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M7_S2_HW', 'HW', 'MATH 125', 7, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M7_S3_HW', 'HW', 'MATH 125', 7, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M7_S1_MA', 'MA', 'MATH 125', 7, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M7_S2_MA', 'MA', 'MATH 125', 7, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M7_S3_MA', 'MA', 'MATH 125', 7, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M8_S1_HW', 'HW', 'MATH 125', 8, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M8_S2_HW', 'HW', 'MATH 125', 8, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M8_S3_HW', 'HW', 'MATH 125', 8, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M8_S1_MA', 'MA', 'MATH 125', 8, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M8_S2_MA', 'MA', 'MATH 125', 8, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M8_S3_MA', 'MA', 'MATH 125', 8, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M1_S1_HW', 'HW', 'MATH 126', 1, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M1_S2_HW', 'HW', 'MATH 126', 1, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M1_S3_HW', 'HW', 'MATH 126', 1, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M1_S1_MA', 'MA', 'MATH 126', 1, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M1_S2_MA', 'MA', 'MATH 126', 1, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M1_S3_MA', 'MA', 'MATH 126', 1, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M2_S1_HW', 'HW', 'MATH 126', 2, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M2_S2_HW', 'HW', 'MATH 126', 2, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M2_S3_HW', 'HW', 'MATH 126', 2, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M2_S1_MA', 'MA', 'MATH 126', 2, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M2_S2_MA', 'MA', 'MATH 126', 2, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M2_S3_MA', 'MA', 'MATH 126', 2, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M3_S1_HW', 'HW', 'MATH 126', 3, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M3_S2_HW', 'HW', 'MATH 126', 3, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M3_S3_HW', 'HW', 'MATH 126', 3, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M3_S1_MA', 'MA', 'MATH 126', 3, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M3_S2_MA', 'MA', 'MATH 126', 3, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M3_S3_MA', 'MA', 'MATH 126', 3, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M4_S1_HW', 'HW', 'MATH 126', 4, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M4_S2_HW', 'HW', 'MATH 126', 4, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M4_S3_HW', 'HW', 'MATH 126', 4, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M4_S1_MA', 'MA', 'MATH 126', 4, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M4_S2_MA', 'MA', 'MATH 126', 4, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M4_S3_MA', 'MA', 'MATH 126', 4, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M5_S1_HW', 'HW', 'MATH 126', 5, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M5_S2_HW', 'HW', 'MATH 126', 5, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M5_S3_HW', 'HW', 'MATH 126', 5, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M5_S1_MA', 'MA', 'MATH 126', 5, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M5_S2_MA', 'MA', 'MATH 126', 5, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M5_S3_MA', 'MA', 'MATH 126', 5, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M6_S1_HW', 'HW', 'MATH 126', 6, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M6_S2_HW', 'HW', 'MATH 126', 6, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M6_S3_HW', 'HW', 'MATH 126', 6, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M6_S1_MA', 'MA', 'MATH 126', 6, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M6_S2_MA', 'MA', 'MATH 126', 6, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M6_S3_MA', 'MA', 'MATH 126', 6, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M7_S1_HW', 'HW', 'MATH 126', 7, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M7_S2_HW', 'HW', 'MATH 126', 7, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M7_S3_HW', 'HW', 'MATH 126', 7, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M7_S1_MA', 'MA', 'MATH 126', 7, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M7_S2_MA', 'MA', 'MATH 126', 7, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M7_S3_MA', 'MA', 'MATH 126', 7, 3, 2, 2);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M8_S1_HW', 'HW', 'MATH 126', 8, 1, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M8_S2_HW', 'HW', 'MATH 126', 8, 2, 3, 3);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M8_S3_HW', 'HW', 'MATH 126', 8, 3, 3, 3);

INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M8_S1_MA', 'MA', 'MATH 126', 8, 1, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M8_S2_MA', 'MA', 'MATH 126', 8, 2, 2, 2);
INSERT INTO main.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M8_S3_MA', 'MA', 'MATH 126', 8, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M1_S1_HW', 'HW', 'MATH 117', 1, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M1_S2_HW', 'HW', 'MATH 117', 1, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M1_S3_HW', 'HW', 'MATH 117', 1, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M1_S1_MA', 'MA', 'MATH 117', 1, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M1_S2_MA', 'MA', 'MATH 117', 1, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M1_S3_MA', 'MA', 'MATH 117', 1, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M2_S1_HW', 'HW', 'MATH 117', 2, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M2_S2_HW', 'HW', 'MATH 117', 2, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M2_S3_HW', 'HW', 'MATH 117', 2, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M2_S1_MA', 'MA', 'MATH 117', 2, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M2_S2_MA', 'MA', 'MATH 117', 2, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M2_S3_MA', 'MA', 'MATH 117', 2, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M3_S1_HW', 'HW', 'MATH 117', 3, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M3_S2_HW', 'HW', 'MATH 117', 3, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M3_S3_HW', 'HW', 'MATH 117', 3, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M3_S1_MA', 'MA', 'MATH 117', 3, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M3_S2_MA', 'MA', 'MATH 117', 3, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M3_S3_MA', 'MA', 'MATH 117', 3, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M4_S1_HW', 'HW', 'MATH 117', 4, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M4_S2_HW', 'HW', 'MATH 117', 4, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M4_S3_HW', 'HW', 'MATH 117', 4, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M4_S1_MA', 'MA', 'MATH 117', 4, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M4_S2_MA', 'MA', 'MATH 117', 4, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M4_S3_MA', 'MA', 'MATH 117', 4, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M5_S1_HW', 'HW', 'MATH 117', 5, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M5_S2_HW', 'HW', 'MATH 117', 5, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M5_S3_HW', 'HW', 'MATH 117', 5, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M5_S1_MA', 'MA', 'MATH 117', 5, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M5_S2_MA', 'MA', 'MATH 117', 5, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M5_S3_MA', 'MA', 'MATH 117', 5, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M6_S1_HW', 'HW', 'MATH 117', 6, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M6_S2_HW', 'HW', 'MATH 117', 6, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M6_S3_HW', 'HW', 'MATH 117', 6, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M6_S1_MA', 'MA', 'MATH 117', 6, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M6_S2_MA', 'MA', 'MATH 117', 6, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M6_S3_MA', 'MA', 'MATH 117', 6, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M7_S1_HW', 'HW', 'MATH 117', 7, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M7_S2_HW', 'HW', 'MATH 117', 7, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M7_S3_HW', 'HW', 'MATH 117', 7, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M7_S1_MA', 'MA', 'MATH 117', 7, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M7_S2_MA', 'MA', 'MATH 117', 7, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M7_S3_MA', 'MA', 'MATH 117', 7, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M8_S1_HW', 'HW', 'MATH 117', 8, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M8_S2_HW', 'HW', 'MATH 117', 8, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M8_S3_HW', 'HW', 'MATH 117', 8, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M8_S1_MA', 'MA', 'MATH 117', 8, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M8_S2_MA', 'MA', 'MATH 117', 8, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('117_M8_S3_MA', 'MA', 'MATH 117', 8, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M1_S1_HW', 'HW', 'MATH 118', 1, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M1_S2_HW', 'HW', 'MATH 118', 1, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M1_S3_HW', 'HW', 'MATH 118', 1, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M1_S1_MA', 'MA', 'MATH 118', 1, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M1_S2_MA', 'MA', 'MATH 118', 1, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M1_S3_MA', 'MA', 'MATH 118', 1, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M2_S1_HW', 'HW', 'MATH 118', 2, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M2_S2_HW', 'HW', 'MATH 118', 2, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M2_S3_HW', 'HW', 'MATH 118', 2, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M2_S1_MA', 'MA', 'MATH 118', 2, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M2_S2_MA', 'MA', 'MATH 118', 2, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M2_S3_MA', 'MA', 'MATH 118', 2, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M3_S1_HW', 'HW', 'MATH 118', 3, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M3_S2_HW', 'HW', 'MATH 118', 3, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M3_S3_HW', 'HW', 'MATH 118', 3, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M3_S1_MA', 'MA', 'MATH 118', 3, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M3_S2_MA', 'MA', 'MATH 118', 3, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M3_S3_MA', 'MA', 'MATH 118', 3, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M4_S1_HW', 'HW', 'MATH 118', 4, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M4_S2_HW', 'HW', 'MATH 118', 4, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M4_S3_HW', 'HW', 'MATH 118', 4, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M4_S1_MA', 'MA', 'MATH 118', 4, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M4_S2_MA', 'MA', 'MATH 118', 4, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M4_S3_MA', 'MA', 'MATH 118', 4, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M5_S1_HW', 'HW', 'MATH 118', 5, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M5_S2_HW', 'HW', 'MATH 118', 5, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M5_S3_HW', 'HW', 'MATH 118', 5, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M5_S1_MA', 'MA', 'MATH 118', 5, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M5_S2_MA', 'MA', 'MATH 118', 5, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M5_S3_MA', 'MA', 'MATH 118', 5, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M6_S1_HW', 'HW', 'MATH 118', 6, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M6_S2_HW', 'HW', 'MATH 118', 6, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M6_S3_HW', 'HW', 'MATH 118', 6, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M6_S1_MA', 'MA', 'MATH 118', 6, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M6_S2_MA', 'MA', 'MATH 118', 6, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M6_S3_MA', 'MA', 'MATH 118', 6, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M7_S1_HW', 'HW', 'MATH 118', 7, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M7_S2_HW', 'HW', 'MATH 118', 7, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M7_S3_HW', 'HW', 'MATH 118', 7, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M7_S1_MA', 'MA', 'MATH 118', 7, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M7_S2_MA', 'MA', 'MATH 118', 7, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M7_S3_MA', 'MA', 'MATH 118', 7, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M8_S1_HW', 'HW', 'MATH 118', 8, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M8_S2_HW', 'HW', 'MATH 118', 8, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M8_S3_HW', 'HW', 'MATH 118', 8, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M8_S1_MA', 'MA', 'MATH 118', 8, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M8_S2_MA', 'MA', 'MATH 118', 8, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('118_M8_S3_MA', 'MA', 'MATH 118', 8, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M1_S1_HW', 'HW', 'MATH 124', 1, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M1_S2_HW', 'HW', 'MATH 124', 1, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M1_S3_HW', 'HW', 'MATH 124', 1, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M1_S1_MA', 'MA', 'MATH 124', 1, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M1_S2_MA', 'MA', 'MATH 124', 1, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M1_S3_MA', 'MA', 'MATH 124', 1, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M2_S1_HW', 'HW', 'MATH 124', 2, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M2_S2_HW', 'HW', 'MATH 124', 2, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M2_S3_HW', 'HW', 'MATH 124', 2, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M2_S1_MA', 'MA', 'MATH 124', 2, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M2_S2_MA', 'MA', 'MATH 124', 2, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M2_S3_MA', 'MA', 'MATH 124', 2, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M3_S1_HW', 'HW', 'MATH 124', 3, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M3_S2_HW', 'HW', 'MATH 124', 3, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M3_S3_HW', 'HW', 'MATH 124', 3, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M3_S1_MA', 'MA', 'MATH 124', 3, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M3_S2_MA', 'MA', 'MATH 124', 3, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M3_S3_MA', 'MA', 'MATH 124', 3, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M4_S1_HW', 'HW', 'MATH 124', 4, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M4_S2_HW', 'HW', 'MATH 124', 4, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M4_S3_HW', 'HW', 'MATH 124', 4, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M4_S1_MA', 'MA', 'MATH 124', 4, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M4_S2_MA', 'MA', 'MATH 124', 4, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M4_S3_MA', 'MA', 'MATH 124', 4, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M5_S1_HW', 'HW', 'MATH 124', 5, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M5_S2_HW', 'HW', 'MATH 124', 5, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M5_S3_HW', 'HW', 'MATH 124', 5, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M5_S1_MA', 'MA', 'MATH 124', 5, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M5_S2_MA', 'MA', 'MATH 124', 5, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M5_S3_MA', 'MA', 'MATH 124', 5, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M6_S1_HW', 'HW', 'MATH 124', 6, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M6_S2_HW', 'HW', 'MATH 124', 6, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M6_S3_HW', 'HW', 'MATH 124', 6, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M6_S1_MA', 'MA', 'MATH 124', 6, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M6_S2_MA', 'MA', 'MATH 124', 6, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M6_S3_MA', 'MA', 'MATH 124', 6, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M7_S1_HW', 'HW', 'MATH 124', 7, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M7_S2_HW', 'HW', 'MATH 124', 7, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M7_S3_HW', 'HW', 'MATH 124', 7, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M7_S1_MA', 'MA', 'MATH 124', 7, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M7_S2_MA', 'MA', 'MATH 124', 7, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M7_S3_MA', 'MA', 'MATH 124', 7, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M8_S1_HW', 'HW', 'MATH 124', 8, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M8_S2_HW', 'HW', 'MATH 124', 8, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M8_S3_HW', 'HW', 'MATH 124', 8, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M8_S1_MA', 'MA', 'MATH 124', 8, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M8_S2_MA', 'MA', 'MATH 124', 8, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('124_M8_S3_MA', 'MA', 'MATH 124', 8, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M1_S1_HW', 'HW', 'MATH 125', 1, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M1_S2_HW', 'HW', 'MATH 125', 1, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M1_S3_HW', 'HW', 'MATH 125', 1, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M1_S1_MA', 'MA', 'MATH 125', 1, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M1_S2_MA', 'MA', 'MATH 125', 1, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M1_S3_MA', 'MA', 'MATH 125', 1, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M2_S1_HW', 'HW', 'MATH 125', 2, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M2_S2_HW', 'HW', 'MATH 125', 2, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M2_S3_HW', 'HW', 'MATH 125', 2, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M2_S1_MA', 'MA', 'MATH 125', 2, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M2_S2_MA', 'MA', 'MATH 125', 2, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M2_S3_MA', 'MA', 'MATH 125', 2, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M3_S1_HW', 'HW', 'MATH 125', 3, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M3_S2_HW', 'HW', 'MATH 125', 3, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M3_S3_HW', 'HW', 'MATH 125', 3, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M3_S1_MA', 'MA', 'MATH 125', 3, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M3_S2_MA', 'MA', 'MATH 125', 3, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M3_S3_MA', 'MA', 'MATH 125', 3, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M4_S1_HW', 'HW', 'MATH 125', 4, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M4_S2_HW', 'HW', 'MATH 125', 4, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M4_S3_HW', 'HW', 'MATH 125', 4, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M4_S1_MA', 'MA', 'MATH 125', 4, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M4_S2_MA', 'MA', 'MATH 125', 4, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M4_S3_MA', 'MA', 'MATH 125', 4, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M5_S1_HW', 'HW', 'MATH 125', 5, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M5_S2_HW', 'HW', 'MATH 125', 5, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M5_S3_HW', 'HW', 'MATH 125', 5, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M5_S1_MA', 'MA', 'MATH 125', 5, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M5_S2_MA', 'MA', 'MATH 125', 5, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M5_S3_MA', 'MA', 'MATH 125', 5, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M6_S1_HW', 'HW', 'MATH 125', 6, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M6_S2_HW', 'HW', 'MATH 125', 6, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M6_S3_HW', 'HW', 'MATH 125', 6, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M6_S1_MA', 'MA', 'MATH 125', 6, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M6_S2_MA', 'MA', 'MATH 125', 6, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M6_S3_MA', 'MA', 'MATH 125', 6, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M7_S1_HW', 'HW', 'MATH 125', 7, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M7_S2_HW', 'HW', 'MATH 125', 7, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M7_S3_HW', 'HW', 'MATH 125', 7, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M7_S1_MA', 'MA', 'MATH 125', 7, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M7_S2_MA', 'MA', 'MATH 125', 7, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M7_S3_MA', 'MA', 'MATH 125', 7, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M8_S1_HW', 'HW', 'MATH 125', 8, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M8_S2_HW', 'HW', 'MATH 125', 8, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M8_S3_HW', 'HW', 'MATH 125', 8, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M8_S1_MA', 'MA', 'MATH 125', 8, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M8_S2_MA', 'MA', 'MATH 125', 8, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('125_M8_S3_MA', 'MA', 'MATH 125', 8, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M1_S1_HW', 'HW', 'MATH 126', 1, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M1_S2_HW', 'HW', 'MATH 126', 1, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M1_S3_HW', 'HW', 'MATH 126', 1, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M1_S1_MA', 'MA', 'MATH 126', 1, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M1_S2_MA', 'MA', 'MATH 126', 1, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M1_S3_MA', 'MA', 'MATH 126', 1, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M2_S1_HW', 'HW', 'MATH 126', 2, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M2_S2_HW', 'HW', 'MATH 126', 2, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M2_S3_HW', 'HW', 'MATH 126', 2, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M2_S1_MA', 'MA', 'MATH 126', 2, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M2_S2_MA', 'MA', 'MATH 126', 2, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M2_S3_MA', 'MA', 'MATH 126', 2, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M3_S1_HW', 'HW', 'MATH 126', 3, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M3_S2_HW', 'HW', 'MATH 126', 3, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M3_S3_HW', 'HW', 'MATH 126', 3, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M3_S1_MA', 'MA', 'MATH 126', 3, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M3_S2_MA', 'MA', 'MATH 126', 3, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M3_S3_MA', 'MA', 'MATH 126', 3, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M4_S1_HW', 'HW', 'MATH 126', 4, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M4_S2_HW', 'HW', 'MATH 126', 4, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M4_S3_HW', 'HW', 'MATH 126', 4, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M4_S1_MA', 'MA', 'MATH 126', 4, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M4_S2_MA', 'MA', 'MATH 126', 4, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M4_S3_MA', 'MA', 'MATH 126', 4, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M5_S1_HW', 'HW', 'MATH 126', 5, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M5_S2_HW', 'HW', 'MATH 126', 5, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M5_S3_HW', 'HW', 'MATH 126', 5, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M5_S1_MA', 'MA', 'MATH 126', 5, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M5_S2_MA', 'MA', 'MATH 126', 5, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M5_S3_MA', 'MA', 'MATH 126', 5, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M6_S1_HW', 'HW', 'MATH 126', 6, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M6_S2_HW', 'HW', 'MATH 126', 6, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M6_S3_HW', 'HW', 'MATH 126', 6, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M6_S1_MA', 'MA', 'MATH 126', 6, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M6_S2_MA', 'MA', 'MATH 126', 6, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M6_S3_MA', 'MA', 'MATH 126', 6, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M7_S1_HW', 'HW', 'MATH 126', 7, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M7_S2_HW', 'HW', 'MATH 126', 7, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M7_S3_HW', 'HW', 'MATH 126', 7, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M7_S1_MA', 'MA', 'MATH 126', 7, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M7_S2_MA', 'MA', 'MATH 126', 7, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M7_S3_MA', 'MA', 'MATH 126', 7, 3, 2, 2);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M8_S1_HW', 'HW', 'MATH 126', 8, 1, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M8_S2_HW', 'HW', 'MATH 126', 8, 2, 3, 3);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M8_S3_HW', 'HW', 'MATH 126', 8, 3, 3, 3);

INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M8_S1_MA', 'MA', 'MATH 126', 8, 1, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M8_S2_MA', 'MA', 'MATH 126', 8, 2, 2, 2);
INSERT INTO main_dev.standard_assignment (assignment_id, assignment_type, course_id, module_nbr, standard_nbr,
  pts_possible, min_passing_score) VALUES ('126_M8_S3_MA', 'MA', 'MATH 126', 8, 3, 2, 2);

-- ------------------------------------------------------------------------------------------------
-- TABLE: course_survey
-- ------------------------------------------------------------------------------------------------

INSERT INTO main.course_survey (survey_id, survey_title, prompt_html) VALUES (
  'PC_PRE', 'Course Personalization Survey',
  'This survey lets you tell us how you feel about math, what sorts of things you are interested in, and what math '
  'you need to take for your program of study.  Our course delivery system can use this information to personalize '
  'the course content to try to best suit your needs and goals.');

INSERT INTO main_dev.course_survey (survey_id, survey_title, prompt_html) VALUES (
  'PC_PRE', 'Course Personalization Survey',
  'This survey lets you tell us how you feel about math, what sorts of things you are interested in, and what math '
  'you need to take for your program of study.  Our course delivery system can use this information to personalize '
  'the course content to try to best suit your needs and goals.');

-- ------------------------------------------------------------------------------------------------
-- TABLE: course_survey_item
-- ------------------------------------------------------------------------------------------------

INSERT INTO main.course_survey_item (survey_id, item_nbr, item_type, prompt_html) VALUES ('PC_PRE', 1, 2,
  'Which areas of study are most interesting to you (you may select multiple options):');
INSERT INTO main.course_survey_item (survey_id, item_nbr, item_type, prompt_html) VALUES ('PC_PRE', 2, 1,
  'What math courses do you believe you will need to take to complete your program of study?');
INSERT INTO main.course_survey_item (survey_id, item_nbr, item_type, prompt_html) VALUES ('PC_PRE', 3, 3,
  'I find mathematics interesting.');
INSERT INTO main.course_survey_item (survey_id, item_nbr, item_type, prompt_html) VALUES ('PC_PRE', 4, 3,
  'I enjoy doing mathematics.');
INSERT INTO main.course_survey_item (survey_id, item_nbr, item_type, prompt_html) VALUES ('PC_PRE', 5, 3,
  'I am confident in my mathematical abilities.');
INSERT INTO main.course_survey_item (survey_id, item_nbr, item_type, prompt_html) VALUES ('PC_PRE', 6, 3,
  'I am able to learn mathematics.');
INSERT INTO main.course_survey_item (survey_id, item_nbr, item_type, prompt_html) VALUES ('PC_PRE', 7, 4,
  'When working on mathematics, my anxiety level is usually:');

INSERT INTO main_dev.course_survey_item (survey_id, item_nbr, item_type, prompt_html) VALUES ('PC_PRE', 1, 2,
  'Which areas of study are most interesting to you (you may select multiple options):');
INSERT INTO main_dev.course_survey_item (survey_id, item_nbr, item_type, prompt_html) VALUES ('PC_PRE', 2, 1,
  'What math courses do you believe you will need to take to complete your program of study?');
INSERT INTO main_dev.course_survey_item (survey_id, item_nbr, item_type, prompt_html) VALUES ('PC_PRE', 3, 3,
  'I find mathematics interesting.');
INSERT INTO main_dev.course_survey_item (survey_id, item_nbr, item_type, prompt_html) VALUES ('PC_PRE', 4, 3,
  'I enjoy doing mathematics.');
INSERT INTO main_dev.course_survey_item (survey_id, item_nbr, item_type, prompt_html) VALUES ('PC_PRE', 5, 3,
  'I am confident in my mathematical abilities.');
INSERT INTO main_dev.course_survey_item (survey_id, item_nbr, item_type, prompt_html) VALUES ('PC_PRE', 6, 3,
  'I am able to learn mathematics.');
INSERT INTO main_dev.course_survey_item (survey_id, item_nbr, item_type, prompt_html) VALUES ('PC_PRE', 7, 4,
  'When working on mathematics, my anxiety level is usually:');

-- ------------------------------------------------------------------------------------------------
-- TABLE: course_survey_item_choice
-- ------------------------------------------------------------------------------------------------

INSERT INTO main.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 1, 'Arts, Humanities, and Design');
INSERT INTO main.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 2, 'Education and Teaching');
INSERT INTO main.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 3, 'Environmental and Natural Resources');
INSERT INTO main.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 4, 'Global and Social Sciences');
INSERT INTO main.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 5, 'Health, Life, and Food Sciences');
INSERT INTO main.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 6, 'Land, Plant, and Animal Sciences');
INSERT INTO main.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 7, 'Math, Physical Sciences, and Engineering');
INSERT INTO main.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 8, 'Organization, Management, and Enterprise');
INSERT INTO main.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 9, 'Medicine or Veterinary Medicine');
INSERT INTO main.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 10, 'Law');

INSERT INTO main.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 2, 1, 'I just need to satisfy the core curriculum requirement, but don''t need specific math courses');
INSERT INTO main.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 2, 2, 'I need to complete College Algebra');
INSERT INTO main.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 2, 3, 'I need College Algebra and Trigonometry');
INSERT INTO main.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 2, 4, 'I need a Calculus course');
INSERT INTO main.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 2, 5, 'I need a sequence of multiple Calculus courses');
INSERT INTO main.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 2, 6, 'I need courses beyond Calculus (like Differential Equations, Linear Algebra, etc.)');

INSERT INTO main_dev.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 1, 'Arts, Humanities, and Design');
INSERT INTO main_dev.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 2, 'Education and Teaching');
INSERT INTO main_dev.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 3, 'Environmental and Natural Resources');
INSERT INTO main_dev.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 4, 'Global and Social Sciences');
INSERT INTO main_dev.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 5, 'Health, Life, and Food Sciences');
INSERT INTO main_dev.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 6, 'Land, Plant, and Animal Sciences');
INSERT INTO main_dev.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 7, 'Math, Physical Sciences, and Engineering');
INSERT INTO main_dev.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 8, 'Organization, Management, and Enterprise');
INSERT INTO main_dev.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 9, 'Medicine or Veterinary Medicine');
INSERT INTO main_dev.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 1, 10, 'Law');

INSERT INTO main_dev.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 2, 1, 'I just need to satisfy the core curriculum requirement, but don''t need specific math courses');
INSERT INTO main_dev.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 2, 2, 'I need to complete College Algebra');
INSERT INTO main_dev.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 2, 3, 'I need College Algebra and Trigonometry');
INSERT INTO main_dev.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 2, 4, 'I need a Calculus course');
INSERT INTO main_dev.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 2, 5, 'I need a sequence of multiple Calculus courses');
INSERT INTO main_dev.course_survey_item_choice (survey_id, item_nbr, choice_nbr, choice_html) VALUES (
  'PC_PRE', 2, 6, 'I need courses beyond Calculus (like Differential Equations, Linear Algebra, etc.)');




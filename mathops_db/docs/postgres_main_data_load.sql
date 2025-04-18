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
-- ------------------------------------------------------------------------------------------------

INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 117', 1, 3, '03_alg/01_quantities');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 117', 2, 3, '03_alg/02_relations_graphs');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 117', 3, 3, '03_alg/03_functions');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 117', 4, 3, '03_alg/04_linear_fxns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 117', 5, 3, '03_alg/05_quadratic_fxns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 117', 6, 3, '03_alg/06_inverse_fxns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 117', 7, 3, '03_alg/07_rate_of_change');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 117', 8, 3, '03_alg/08_apps_linear_quadratic');

INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 118', 1, 3, '03_alg/09_quantities');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 118', 2, 3, '03_alg/10_polynomial');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 118', 3, 3, '03_alg/11_rational_expr');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 118', 4, 3, '03_alg/12_rational_fxns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 118', 5, 3, '03_alg/13_variation');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 118', 6, 3, '03_alg/14_apps_functions');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 118', 7, 3, '03_alg/15_systems');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 118', 8, 3, '03_alg/16_solving_systems');

INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 124', 1, 3, '04_logexp/01_discrete');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 124', 2, 3, '04_logexp/02_exp_expr');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 124', 3, 3, '04_logexp/03_exp_fxns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 124', 4, 3, '04_logexp/04_exp_apps');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 124', 5, 3, '04_logexp/05_logs');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 124', 6, 3, '04_logexp/06_log_fxns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 124', 7, 3, '04_logexp/07_log_apps');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 124', 8, 3, '04_logexp/08_series');

INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 125', 1, 3, '05_trig/01_angles');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 125', 2, 3, '05_trig/02_triangles');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 125', 3, 3, '05_trig/03_unit_circle');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 125', 4, 3, '05_trig/04_trig_fxns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 125', 5, 3, '05_trig/05_transform');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 125', 6, 3, '05_trig/06_right_triangle');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 125', 7, 3, '05_trig/07_inv_trig_fxns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 125', 8, 3, '05_trig/08_law_of_sines_cosines');

INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 126', 1, 3, '05_trig/09_basic_ident');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 126', 2, 3, '05_trig/10_sum_diff_ident');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 126', 3, 3, '05_trig/11_mult_half_angle_ident');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 126', 4, 3, '05_trig/12_trig_eqns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 126', 5, 3, '05_trig/13_polar_coords');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 126', 6, 3, '05_trig/14_polar_fxns');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 126', 7, 3, '05_trig/15_complex');
INSERT INTO main.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 126', 8, 3, '05_trig/16_applications');

INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 117', 1, 3, '03_alg/01_quantities');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 117', 2, 3, '03_alg/02_relations_graphs');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 117', 3, 3, '03_alg/03_functions');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 117', 4, 3, '03_alg/04_linear_fxns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 117', 5, 3, '03_alg/05_quadratic_fxns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 117', 6, 3, '03_alg/06_inverse_fxns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 117', 7, 3, '03_alg/07_rate_of_change');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 117', 8, 3, '03_alg/08_apps_linear_quadratic');

INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 118', 1, 3, '03_alg/09_quantities');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 118', 2, 3, '03_alg/10_polynomial');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 118', 3, 3, '03_alg/11_rational_expr');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 118', 4, 3, '03_alg/12_rational_fxns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 118', 5, 3, '03_alg/13_variation');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 118', 6, 3, '03_alg/14_apps_functions');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 118', 7, 3, '03_alg/15_systems');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 118', 8, 3, '03_alg/16_solving_systems');

INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 124', 1, 3, '04_logexp/01_discrete');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 124', 2, 3, '04_logexp/02_exp_expr');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 124', 3, 3, '04_logexp/03_exp_fxns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 124', 4, 3, '04_logexp/04_exp_apps');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 124', 5, 3, '04_logexp/05_logs');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 124', 6, 3, '04_logexp/06_log_fxns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 124', 7, 3, '04_logexp/07_log_apps');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 124', 8, 3, '04_logexp/08_series');

INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 125', 1, 3, '05_trig/01_angles');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 125', 2, 3, '05_trig/02_triangles');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 125', 3, 3, '05_trig/03_unit_circle');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 125', 4, 3, '05_trig/04_trig_fxns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 125', 5, 3, '05_trig/05_transform');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 125', 6, 3, '05_trig/06_right_triangle');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 125', 7, 3, '05_trig/07_inv_trig_fxns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 125', 8, 3, '05_trig/08_law_of_sines_cosines');

INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 126', 1, 3, '05_trig/09_basic_ident');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 126', 2, 3, '05_trig/10_sum_diff_ident');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 126', 3, 3, '05_trig/11_mult_half_angle_ident');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 126', 4, 3, '05_trig/12_trig_eqns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 126', 5, 3, '05_trig/13_polar_coords');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 126', 6, 3, '05_trig/14_polar_fxns');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 126', 7, 3, '05_trig/15_complex');
INSERT INTO main_dev.standards_course_module (course_id, module_nbr, nbr_standards, module_path) VALUES (
  'MATH 126', 8, 3, '05_trig/16_applications');

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
  'PC_PRE_F2F', 'Course Personalization Survey',
  'This survey lets you tell us how you feel about math, what sorts of things you are interested in, and what math '
  'you need to take for your program of study.  Our course delivery system can use this information to personalize '
  'the course content to try to best suit your needs and goals.');
INSERT INTO main.course_survey (survey_id, survey_title, prompt_html) VALUES (
  'PC_PRE_DST', 'Course Personalization Survey',
  'This survey lets you tell us how you feel about math, what sorts of things you are interested in, and what math '
  'you need to take for your program of study.  Our course delivery system can use this information to personalize '
  'the course content to try to best suit your needs and goals.');
INSERT INTO main.course_survey (survey_id, survey_title, prompt_html) VALUES (
  'PC_PRE_HYB', 'Course Personalization Survey',
  'This survey lets you tell us how you feel about math, what sorts of things you are interested in, and what math '
  'you need to take for your program of study.  Our course delivery system can use this information to personalize '
  'the course content to try to best suit your needs and goals.');
INSERT INTO main.course_survey (survey_id, survey_title, prompt_html) VALUES (
  'PC_SAT_F2F', 'Precalculus Course Satisfaction Survey',
  'Please give us feedback about this course to help us improve the course in the future.');
INSERT INTO main.course_survey (survey_id, survey_title, prompt_html) VALUES (
  'PC_SAT_DST', 'Precalculus Course Satisfaction Survey',
  'Please give us feedback about this course to help us improve the course in the future.');
INSERT INTO main.course_survey (survey_id, survey_title, prompt_html) VALUES (
  'PC_SAT_HYB', 'Precalculus Course Satisfaction Survey',
  'Please give us feedback about this course to help us improve the course in the future.');

INSERT INTO main_dev.course_survey (survey_id, survey_title, prompt_html) VALUES (
  'PC_PRE_F2F', 'Course Personalization Survey',
  'This survey lets you tell us how you feel about math, what sorts of things you are interested in, and what math '
  'you need to take for your program of study.  Our course delivery system can use this information to personalize '
  'the course content to try to best suit your needs and goals.');
INSERT INTO main_dev.course_survey (survey_id, survey_title, prompt_html) VALUES (
  'PC_PRE_DST', 'Course Personalization Survey',
  'This survey lets you tell us how you feel about math, what sorts of things you are interested in, and what math '
  'you need to take for your program of study.  Our course delivery system can use this information to personalize '
  'the course content to try to best suit your needs and goals.');
INSERT INTO main_dev.course_survey (survey_id, survey_title, prompt_html) VALUES (
  'PC_PRE_HYB', 'Course Personalization Survey',
  'This survey lets you tell us how you feel about math, what sorts of things you are interested in, and what math '
  'you need to take for your program of study.  Our course delivery system can use this information to personalize '
  'the course content to try to best suit your needs and goals.');
INSERT INTO main_dev.course_survey (survey_id, survey_title, prompt_html) VALUES (
  'PC_SAT_F2F', 'Precalculus Course Satisfaction Survey',
  'Please give us feedback about this course to help us improve the course in the future.');
INSERT INTO main_dev.course_survey (survey_id, survey_title, prompt_html) VALUES (
  'PC_SAT_DST', 'Precalculus Course Satisfaction Survey',
  'Please give us feedback about this course to help us improve the course in the future.');
INSERT INTO main_dev.course_survey (survey_id, survey_title, prompt_html) VALUES (
  'PC_SAT_HYB', 'Precalculus Course Satisfaction Survey',
  'Please give us feedback about this course to help us improve the course in the future.');

-- ------------------------------------------------------------------------------------------------
-- TABLE: course_survey_item TODO: looking for standard/validated questions
-- ------------------------------------------------------------------------------------------------



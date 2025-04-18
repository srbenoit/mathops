-- postgres_main_data_load.sql
-- (this script is designed to be run under the 'math' database owner)
-- /opt/postgresql/bin/psql -d math -U math

-- ------------------------------------------------------------------------------------------------
-- Before executing this script, ensure that all actions in 'postgres_database_create.sql' and
-- 'posgres_main_schema_create.sql' have been completed.
-- ------------------------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------------------------
-- TABLE: facility
-- ------------------------------------------------------------------------------------------------

INSERT INTO main.facility (facility_id,facility_name,building_name,room_nbr) VALUES (
  'PC_CENTER', 'Precalculus Center', 'Weber', '137');
INSERT INTO main.facility (facility_id,facility_name,building_name,room_nbr) VALUES (
  'PC_ONLINE', 'Precalculus Center Online Help', null, null);

INSERT INTO main_dev.facility (facility_id,facility_name,building_name,room_nbr) VALUES (
  'PC_CENTER', 'Precalculus Center', 'Weber', '137');
INSERT INTO main_dev.facility (facility_id,facility_name,building_name,room_nbr) VALUES (
  'PC_ONLINE', 'Precalculus Center Online Help', null, null);

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

-- ------------------------------------------------------------------------------------------------
-- TABLE: facility_closure
-- ------------------------------------------------------------------------------------------------

INSERT INTO main.facility_closure (facility_id, start_date, end_date, closure_type, start_time, end_time) VALUES (
  'PC_CENTER', '2025-03-15', '2025-03-23', 'SP_BREAK', null, null);
INSERT INTO main.facility_closure (facility_id, start_date, end_date, closure_type, start_time, end_time) VALUES (
  'PC_ONLINE', '2025-03-15', '2025-03-23', 'SP_BREAK', null, null);

INSERT INTO main_dev.facility_closure (facility_id, start_date, end_date, closure_type, start_time, end_time) VALUES (
  'PC_CENTER', '2025-03-15', '2025-03-23', 'SP_BREAK', null, null);
INSERT INTO main_dev.facility_closure (facility_id, start_date, end_date, closure_type, start_time, end_time) VALUES (
  'PC_ONLINE', '2025-03-15', '2025-03-23', 'SP_BREAK', null, null);



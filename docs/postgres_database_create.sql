-- postgres_database_create.sql
-- (this script is designed to be run under the 'postgres' database superuser)

-- ------------------------------------------------------------------------------------------------
-- Before executing this script, do the following:
-- 
-- cd /opt
-- mkdir pgarchive pgbackup pgcluster pglog pgschemas
-- chmod 700 pgarchive pgbackup pgcluster pglog pgschemas
--
-- cd /opt/pgschemas
-- mkdir pgdata-main-tblspc pgdata-main-index 
-- mkdir pgdata-dev-tblspc pgdata-dev-index
-- mkdir pgdata-anlyt-tblspc pgdata-anlyt-index
-- mkdir pgdata-test-tblspc
-- mkdir pgdata-fa23-tblspc pgdata-fa23-index
-- mkdir pgdata-sm23-tblspc pgdata-sm23-index
-- mkdir pgdata-sp23-tblspc pgdata-sp23-index
-- chmod 700 *
--
-- /opt/postgresql/bin/initdb -D /opt/pgcluster
--
-- /opt/postgresql/bin/pg_ctl -D /opt/pgcluster start
-- /opt/postgresql/bin/psql postgresql://localhost:5432/postgres
-- postgres=# alter role postgres with password '**42**!'; 
--   (CTRL-D to exit)
-- /opt/postgresql/binpg_ctl -D /opt/pgcluster stop
--
-- cd /opt/pgcluster
-- vi pg_hba.conf
--   Change "method" on all lines from "trust" to "scram-sha-256"
--   Add these lines to allow connections from fresnel, havoc, numan, nibbler):
--   host    all             all             129.82.154.93/32        scram-sha-256
--   host    all             all             129.82.154.12/32        scram-sha-256
--   host    all             all             129.82.154.28/32        scram-sha-256
--   host    all             all             129.82.154.170/32       scram-sha-256
--
-- cd /opt/pgcluster
-- vi postgresql.conf
--   log_directory = '/opt/pglog'
-- ------------------------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------------------------
-- Creates the 'math' role and database and the tablespaces that will hold database objects

CREATE ROLE math LOGIN CREATEDB CREATEROLE PASSWORD '**41**!';

CREATE DATABASE math OWNER math;

CREATE TABLESPACE main_tbl LOCATION '/opt/pgschemas/pgdata-main-tblspc';
CREATE TABLESPACE main_idx LOCATION '/opt/pgschemas/pgdata-main-index';
GRANT CREATE ON TABLESPACE main_tbl TO math WITH GRANT OPTION;
GRANT CREATE ON TABLESPACE main_idx TO math WITH GRANT OPTION;

CREATE TABLESPACE dev_tbl LOCATION '/opt/pgschemas/pgdata-dev-tblspc';
CREATE TABLESPACE dev_idx LOCATION '/opt/pgschemas/pgdata-dev-index';
GRANT CREATE ON TABLESPACE dev_tbl TO math WITH GRANT OPTION;
GRANT CREATE ON TABLESPACE dev_idx TO math WITH GRANT OPTION;

CREATE TABLESPACE anlyt_tbl LOCATION '/opt/pgschemas/pgdata-anlyt-tblspc';
CREATE TABLESPACE anlyt_idx LOCATION '/opt/pgschemas/pgdata-anlyt-index';
GRANT CREATE ON TABLESPACE anlyt_tbl TO math WITH GRANT OPTION;
GRANT CREATE ON TABLESPACE anlyt_idx TO math WITH GRANT OPTION;

CREATE TABLESPACE test LOCATION '/opt/pgschemas/pgdata-test-tblspc';
GRANT CREATE ON TABLESPACE test TO math WITH GRANT OPTION;

CREATE TABLESPACE sp23_tbl LOCATION '/opt/pgschemas/pgdata-sp23-tblspc';
CREATE TABLESPACE sp23_idx LOCATION '/opt/pgschemas/pgdata-sp23-index';
GRANT CREATE ON TABLESPACE sp23_tbl  TO math WITH GRANT OPTION;
GRANT CREATE ON TABLESPACE sp23_idx TO math WITH GRANT OPTION;

CREATE TABLESPACE sm23_tbl LOCATION '/opt/pgschemas/pgdata-sm23-tblspc';
CREATE TABLESPACE sm23_idx LOCATION '/opt/pgschemas/pgdata-sm23-index';
GRANT CREATE ON TABLESPACE sm23_tbl TO math WITH GRANT OPTION;
GRANT CREATE ON TABLESPACE sm23_idx TO math WITH GRANT OPTION;

CREATE TABLESPACE fa23_tbl LOCATION '/opt/pgschemas/pgdata-fa23-tblspc';
CREATE TABLESPACE fa23_idx LOCATION '/opt/pgschemas/pgdata-fa23-index';
GRANT CREATE ON TABLESPACE fa23_tbl TO math WITH GRANT OPTION;
GRANT CREATE ON TABLESPACE fa23_idx TO math WITH GRANT OPTION;


-- To connect as 'math':
-- /opt/postgresql/bin/psql -d math -U math

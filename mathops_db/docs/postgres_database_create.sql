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

CREATE ROLE math WITH LOGIN CREATEDB CREATEROLE INHERIT PASSWORD '**41**!';

CREATE DATABASE math WITH OWNER = math ENCODING = 'UTF8';

CREATE TABLESPACE main_tbl OWNER math LOCATION '/opt/pg_tblspc/pgdata-main-tblspc';
CREATE TABLESPACE main_idx OWNER math LOCATION '/opt/pg_tblspc/pgdata-main-index';

CREATE TABLESPACE dev_tbl OWNER math LOCATION '/opt/pg_tblspc/pgdata-dev-tblspc';
CREATE TABLESPACE dev_idx OWNER math LOCATION '/opt/pg_tblspc/pgdata-dev-index';

CREATE TABLESPACE anlyt_tbl OWNER math LOCATION '/opt/pg_tblspc/pgdata-anlyt-tblspc';
CREATE TABLESPACE anlyt_idx OWNER math LOCATION '/opt/pg_tblspc/pgdata-anlyt-index';

CREATE TABLESPACE test OWNER math LOCATION '/opt/pg_tblspc/pgdata-test-tblspc';

CREATE TABLESPACE sp24_tbl OWNER math LOCATION '/opt/pg_tblspc/pgdata-sp24-tblspc';
CREATE TABLESPACE sp24_idx OWNER math LOCATION '/opt/pg_tblspc/pgdata-sp24-index';

CREATE TABLESPACE fa23_tbl OWNER math LOCATION '/opt/pg_tblspc/pgdata-fa23-tblspc';
CREATE TABLESPACE fa23_idx OWNER math LOCATION '/opt/pg_tblspc/pgdata-fa23-index';

CREATE TABLESPACE sp23_tbl OWNER math LOCATION '/opt/pg_tblspc/pgdata-sp23-tblspc';
CREATE TABLESPACE sp23_idx OWNER math LOCATION '/opt/pg_tblspc/pgdata-sp23-index';


-- To connect as 'math':
-- /opt/postgresql/bin/psql -d math -U math

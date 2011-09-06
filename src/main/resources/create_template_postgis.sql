-- Create the template database
CREATE DATABASE template_postgis WITH TEMPLATE = template0 ENCODING = 'UTF8';
\c template_postgis

-- Add PLPGSQL language support
CREATE LANGUAGE plpgsql;

---PostGIS
\cd `pg_config --sharedir`
\i ./contrib/postgis-1.5/postgis.sql
\i ./contrib/postgis-1.5/spatial_ref_sys.sql

---PgRoutingCore
--\i /opt/pgrouting1.03/core/sql/routing_core.sql
--\i /opt/pgrouting1.03/core/sql/routing_core_wrappers.sql

-- Enabling users to alter spatial tables
GRANT ALL ON geometry_columns TO PUBLIC;
GRANT ALL ON geography_columns TO PUBLIC;
GRANT ALL ON spatial_ref_sys TO PUBLIC;

-- Garbage-collect and freeze
VACUUM FULL;
VACUUM FREEZE;

-- Allows non-superusers the ability to create from this template
UPDATE pg_database SET datistemplate = TRUE WHERE datname = 'template_postgis';
UPDATE pg_database SET datallowconn = FALSE WHERE datname = 'template_postgis';

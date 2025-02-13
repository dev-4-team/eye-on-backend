CREATE EXTENSION IF NOT EXISTS pg_bigm;
DROP INDEX IF EXISTS location_name_bigm_idx;
CREATE INDEX location_name_bigm_idx ON locations USING gin (name gin_bigm_ops);
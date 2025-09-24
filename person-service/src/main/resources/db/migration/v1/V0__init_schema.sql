CREATE SCHEMA IF NOT EXISTS person;
CREATE SCHEMA IF NOT EXISTS person_history;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
SET search_path TO person,person_history,public;
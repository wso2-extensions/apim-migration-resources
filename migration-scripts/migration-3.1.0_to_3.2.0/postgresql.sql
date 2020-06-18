DROP TABLE IF EXISTS AM_KEY_MANAGER;
CREATE TABLE  IF NOT EXISTS AM_KEY_MANAGER (
  UUID VARCHAR(50) NOT NULL,
  NAME VARCHAR(100) NULL,
  DISPLAY_NAME VARCHAR(100) NULL,
  DESCRIPTION VARCHAR(256) NULL,
  TYPE VARCHAR(45) NULL,
  CONFIGURATION BYTEA NULL,
  ENABLED INT NULL,
  TENANT_DOMAIN VARCHAR(100) NULL,
  PRIMARY KEY (UUID),
  UNIQUE (NAME,TENANT_DOMAIN)
);

DO $$ DECLARE con_name varchar(200);
BEGIN
SELECT 'ALTER TABLE AM_APPLICATION_REGISTRATION DROP CONSTRAINT ' || tc .constraint_name || ';' INTO con_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name
WHERE constraint_type = 'UNIQUE' AND tc.table_name = 'am_application_registration' AND kcu.column_name = 'token_type';

EXECUTE con_name;
END $$;

ALTER TABLE AM_APPLICATION_REGISTRATION
    ADD KEY_MANAGER VARCHAR(255) DEFAULT 'Default',
    ADD CONSTRAINT UNIQUE (SUBSCRIBER_ID,APP_ID,TOKEN_TYPE,KEY_MANAGER);

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
ALTER TABLE AM_APPLICATION_KEY_MAPPING
    ADD UUID VARCHAR(50) NOT NULL DEFAULT uuid_generate_v1(),
    ADD KEY_MANAGER VARCHAR(50) NOT NULL DEFAULT 'Default',
    ADD APP_INFO BYTEA NULL,
    ADD CONSTRAINT application_key_unique UNIQUE(APPLICATION_ID,KEY_TYPE,KEY_MANAGER);

DO $$ DECLARE con_name varchar(200);
BEGIN
SELECT 'ALTER TABLE AM_APPLICATION_KEY_MAPPING DROP CONSTRAINT ' || tc .constraint_name || ';' INTO con_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name
WHERE constraint_type = 'PRIMARY KEY' AND tc.table_name = 'am_application_key_mapping';
EXECUTE con_name;
END $$;



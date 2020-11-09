ALTER TABLE AM_SUBSCRIBER ALTER COLUMN USER_ID VARCHAR(255);
ALTER TABLE AM_APPLICATION ADD GROUP_ID VARCHAR(100);
ALTER TABLE AM_API ADD CONTEXT_TEMPLATE VARCHAR(256);
ALTER TABLE AM_APPLICATION_KEY_MAPPING ADD CREATE_MODE VARCHAR(30) DEFAULT 'CREATED';
ALTER TABLE AM_APPLICATION_REGISTRATION ADD TOKEN_SCOPE VARCHAR(256) DEFAULT 'default';
ALTER TABLE AM_APPLICATION_REGISTRATION ADD INPUTS VARCHAR(256);
ALTER TABLE AM_API_LC_EVENT ALTER COLUMN USER_ID VARCHAR(255) NOT NULL;
UPDATE AM_API SET CONTEXT_TEMPLATE = CONTEXT WHERE CONTEXT_TEMPLATE IS NULL;
ALTER TABLE AM_API ADD CREATED_BY VARCHAR(100), CREATED_TIME DATETIME, UPDATED_BY VARCHAR(100), UPDATED_TIME DATETIME;
ALTER TABLE AM_SUBSCRIBER ADD CREATED_BY VARCHAR(100), CREATED_TIME DATETIME, UPDATED_BY VARCHAR(100), UPDATED_TIME DATETIME;
ALTER TABLE AM_SUBSCRIPTION ADD CREATED_BY VARCHAR(100), CREATED_TIME DATETIME, UPDATED_BY VARCHAR(100), UPDATED_TIME DATETIME;
ALTER TABLE AM_APPLICATION ADD CREATED_BY VARCHAR(100), CREATED_TIME DATETIME, UPDATED_BY VARCHAR(100), UPDATED_TIME DATETIME;
ALTER TABLE AM_API ADD API_UUID VARCHAR(255);
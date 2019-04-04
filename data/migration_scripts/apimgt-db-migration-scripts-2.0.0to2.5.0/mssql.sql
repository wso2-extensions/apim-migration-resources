ALTER TABLE AM_SUBSCRIPTION_KEY_MAPPING ALTER COLUMN ACCESS_TOKEN VARCHAR(512);
ALTER TABLE AM_APPLICATION_REGISTRATION ALTER COLUMN TOKEN_SCOPE VARCHAR(1500);
ALTER TABLE AM_APPLICATION ADD TOKEN_TYPE VARCHAR(10);
ALTER TABLE AM_API_SCOPES ADD PRIMARY KEY (API_ID, SCOPE_ID);
DELETE FROM AM_ALERT_TYPES_VALUES WHERE ALERT_TYPE_ID = (SELECT ALERT_TYPE_ID FROM AM_ALERT_TYPES WHERE ALERT_TYPE_NAME = 'AbnormalRefreshAlert' AND STAKE_HOLDER = 'subscriber');
DELETE FROM AM_ALERT_TYPES WHERE ALERT_TYPE_NAME = 'AbnormalRefreshAlert' AND STAKE_HOLDER = 'subscriber';

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[AM_CERTIFICATE_METADATA]') AND TYPE IN (N'U'))
CREATE TABLE AM_CERTIFICATE_METADATA (
  TENANT_ID INTEGER NOT NULL,
  ALIAS VARCHAR(45) NOT NULL,
  END_POINT VARCHAR(100) NOT NULL,
  CONSTRAINT PK_ALIAS PRIMARY KEY (ALIAS),
  CONSTRAINT END_POINT_CONSTRAINT UNIQUE (END_POINT)
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[AM_APPLICATION_GROUP_MAPPING]') AND TYPE IN (N'U'))
CREATE TABLE AM_APPLICATION_GROUP_MAPPING (
    APPLICATION_ID INTEGER NOT NULL,
    GROUP_ID VARCHAR(512),
    TENANT VARCHAR(255),
    PRIMARY KEY (APPLICATION_ID,GROUP_ID,TENANT),
    FOREIGN KEY (APPLICATION_ID) REFERENCES AM_APPLICATION(APPLICATION_ID) ON DELETE CASCADE ON UPDATE CASCADE
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[AM_USAGE_UPLOADED_FILES]') AND TYPE IN (N'U'))
CREATE TABLE AM_USAGE_UPLOADED_FILES (
  TENANT_DOMAIN VARCHAR(255) NOT NULL,
  FILE_NAME VARCHAR(255) NOT NULL,
  FILE_TIMESTAMP DATETIME DEFAULT GETDATE(),
  FILE_PROCESSED INTEGER DEFAULT 0,
  FILE_CONTENT VARBINARY(MAX) DEFAULT NULL,
  PRIMARY KEY (TENANT_DOMAIN, FILE_NAME, FILE_TIMESTAMP)
);


IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[AM_API_LC_PUBLISH_EVENTS]') AND TYPE IN (N'U'))
CREATE TABLE AM_API_LC_PUBLISH_EVENTS (
    ID INTEGER NOT NULL IDENTITY,
    TENANT_DOMAIN VARCHAR(255) NOT NULL,
    API_ID VARCHAR(500) NOT NULL,
    EVENT_TIME DATETIME DEFAULT GETDATE(),
    PRIMARY KEY (ID)
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[AM_APPLICATION_ATTRIBUTES]') AND TYPE IN (N'U'))
CREATE TABLE AM_APPLICATION_ATTRIBUTES (
  APPLICATION_ID INTEGER NOT NULL,
  NAME VARCHAR(255) NOT NULL,
  VALUE VARCHAR(1024) NOT NULL,
  TENANT_ID INTEGER NOT NULL,
  PRIMARY KEY (APPLICATION_ID,NAME),
  FOREIGN KEY (APPLICATION_ID) REFERENCES AM_APPLICATION (APPLICATION_ID) ON DELETE CASCADE ON UPDATE CASCADE
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[AM_LABELS]') AND TYPE IN (N'U'))
CREATE TABLE AM_LABELS (
  LABEL_ID VARCHAR(50),
  NAME VARCHAR(255),
  DESCRIPTION VARCHAR(1024),
  TENANT_DOMAIN VARCHAR(255),
  UNIQUE (NAME,TENANT_DOMAIN),
  PRIMARY KEY (LABEL_ID)
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[AM_LABEL_URLS]') AND TYPE IN (N'U'))
CREATE TABLE AM_LABEL_URLS (
  LABEL_ID VARCHAR(50),
  ACCESS_URL VARCHAR(255),
  PRIMARY KEY (LABEL_ID,ACCESS_URL),
  FOREIGN KEY (LABEL_ID) REFERENCES AM_LABELS(LABEL_ID) ON UPDATE CASCADE ON DELETE CASCADE
);

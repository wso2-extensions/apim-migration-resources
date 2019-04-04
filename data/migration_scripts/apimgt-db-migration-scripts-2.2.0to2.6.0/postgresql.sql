ALTER TABLE AM_APPLICATION ADD TOKEN_TYPE VARCHAR(10);
ALTER TABLE AM_API_SCOPES ADD PRIMARY KEY (API_ID, SCOPE_ID);
DELETE FROM AM_ALERT_TYPES_VALUES WHERE ALERT_TYPE_ID = (SELECT ALERT_TYPE_ID FROM AM_ALERT_TYPES WHERE ALERT_TYPE_NAME = 'AbnormalRefreshAlert' AND STAKE_HOLDER = 'subscriber');

DROP TABLE IF EXISTS AM_ALERT_TYPES;
DROP SEQUENCE IF EXISTS  AM_ALERT_TYPES_SEQ;
CREATE SEQUENCE AM_ALERT_TYPES_SEQ START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS AM_ALERT_TYPES (
            ALERT_TYPE_ID INTEGER DEFAULT NEXTVAL('am_alert_types_seq'),
            ALERT_TYPE_NAME VARCHAR(255) NOT NULL ,
	    STAKE_HOLDER VARCHAR(100) NOT NULL,           
            PRIMARY KEY (ALERT_TYPE_ID)
);
INSERT INTO AM_ALERT_TYPES (ALERT_TYPE_NAME, STAKE_HOLDER) VALUES ('AbnormalResponseTime', 'publisher');
INSERT INTO AM_ALERT_TYPES (ALERT_TYPE_NAME, STAKE_HOLDER) VALUES ('AbnormalBackendTime', 'publisher');
INSERT INTO AM_ALERT_TYPES (ALERT_TYPE_NAME, STAKE_HOLDER) VALUES ('AbnormalRequestsPerMin', 'subscriber');
INSERT INTO AM_ALERT_TYPES (ALERT_TYPE_NAME, STAKE_HOLDER) VALUES ('AbnormalRequestPattern', 'subscriber');
INSERT INTO AM_ALERT_TYPES (ALERT_TYPE_NAME, STAKE_HOLDER) VALUES ('UnusualIPAccess', 'subscriber');
INSERT INTO AM_ALERT_TYPES (ALERT_TYPE_NAME, STAKE_HOLDER) VALUES ('FrequentTierLimitHitting', 'subscriber');
INSERT INTO AM_ALERT_TYPES (ALERT_TYPE_NAME, STAKE_HOLDER) VALUES ('ApiHealthMonitor', 'publisher');

DROP TABLE IF EXISTS AM_APPLICATION_ATTRIBUTES;
CREATE TABLE IF NOT EXISTS AM_APPLICATION_ATTRIBUTES (
  APPLICATION_ID INTEGER NOT NULL,
  NAME VARCHAR(255) NOT NULL,
  VALUE VARCHAR(1024) NOT NULL,
  TENANT_ID INTEGER NOT NULL,
  PRIMARY KEY (APPLICATION_ID,NAME),
  FOREIGN KEY (APPLICATION_ID) REFERENCES AM_APPLICATION (APPLICATION_ID) ON DELETE CASCADE ON UPDATE CASCADE
);

DROP TABLE IF EXISTS AM_LABELS;
CREATE TABLE IF NOT EXISTS AM_LABELS (
  LABEL_ID VARCHAR(50),
  NAME VARCHAR(255),
  DESCRIPTION VARCHAR(1024),
  TENANT_DOMAIN VARCHAR(255),
  UNIQUE (NAME,TENANT_DOMAIN),
  PRIMARY KEY (LABEL_ID)
);

DROP TABLE IF EXISTS AM_LABEL_URLS;
CREATE TABLE IF NOT EXISTS AM_LABEL_URLS (
  LABEL_ID VARCHAR(50),
  ACCESS_URL VARCHAR(255),
  PRIMARY KEY (LABEL_ID,ACCESS_URL),
  FOREIGN KEY (LABEL_ID) REFERENCES AM_LABELS(LABEL_ID) ON UPDATE CASCADE ON DELETE CASCADE
);

ALTER TABLE AM_SUBSCRIBER
    ALTER COLUMN DATE_SUBSCRIBED TYPE TIMESTAMP,
    ALTER COLUMN DATE_SUBSCRIBED SET NOT NULL,
    ALTER COLUMN CREATED_TIME TYPE TIMESTAMP,
    ALTER COLUMN UPDATED_TIME TYPE TIMESTAMP;

ALTER TABLE AM_APPLICATION
    ALTER COLUMN CREATED_TIME TYPE TIMESTAMP,
    ALTER COLUMN UPDATED_TIME TYPE TIMESTAMP;

ALTER TABLE AM_API
    ALTER COLUMN CREATED_TIME TYPE TIMESTAMP,
    ALTER COLUMN UPDATED_TIME TYPE TIMESTAMP;

ALTER TABLE AM_SUBSCRIPTION
    ALTER COLUMN LAST_ACCESSED TYPE TIMESTAMP,
    ALTER COLUMN CREATED_TIME TYPE TIMESTAMP,
    ALTER COLUMN UPDATED_TIME TYPE TIMESTAMP;

ALTER TABLE AM_API_LC_EVENT
    ALTER COLUMN EVENT_DATE TYPE TIMESTAMP,
    ALTER COLUMN EVENT_DATE SET NOT NULL;

ALTER TABLE AM_API_COMMENTS
    ALTER COLUMN DATE_COMMENTED TYPE TIMESTAMP,
    ALTER COLUMN DATE_COMMENTED SET NOT NULL;

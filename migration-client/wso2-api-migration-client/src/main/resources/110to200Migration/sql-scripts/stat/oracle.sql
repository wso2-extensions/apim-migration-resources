ALTER TABLE API_LAST_ACCESS_TIME_SUMMARY
ADD CONSTRAINT last_access_time_pri_key PRIMARY KEY (tenantDomain,apiPublisher,api);
/
ALTER TABLE API_REQUEST_SUMMARY
ADD CONSTRAINT api_request_summary_pri_key PRIMARY KEY (api,api_version,version,apiPublisher,consumerKey,userId,context,hostName,year,month,day);
/
ALTER TABLE API_VERSION_USAGE_SUMMARY
ADD CONSTRAINT api_version_usage_pri_key PRIMARY KEY (api,version,apiPublisher,context,hostName,year,month,day);
/
ALTER TABLE API_Resource_USAGE_SUMMARY
ADD CONSTRAINT api_resource_usage_pri_key PRIMARY KEY (api,version,apiPublisher,consumerKey,context,resourcePath,method,hostName,year,month,day);
/
ALTER TABLE API_RESPONSE_SUMMARY
ADD CONSTRAINT api_response_pri_key PRIMARY KEY (api_version,apiPublisher,context,hostName,year,month,day);
/
ALTER TABLE API_FAULT_SUMMARY
ADD CONSTRAINT api_fault_pri_key PRIMARY KEY (api,version,apiPublisher,consumerKey,context,hostName,year,month,day);
/
ALTER TABLE API_DESTINATION_SUMMARY
ADD CONSTRAINT api_destination_pri_key PRIMARY KEY (api,version,apiPublisher,context,destination,hostName,year,month,day);
/
ALTER TABLE API_THROTTLED_OUT_SUMMARY ADD throttledOutReason VARCHAR(256) DEFAULT '-';
/
ALTER TABLE API_THROTTLED_OUT_SUMMARY
ADD CONSTRAINT api_throttled_pri_key PRIMARY KEY (api,api_version,context,apiPublisher,applicationName,tenantDomain,year,month,day,throttledOutReason);
/
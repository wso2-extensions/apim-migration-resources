ALTER TABLE API_LAST_ACCESS_TIME_SUMMARY ADD PRIMARY KEY(tenantDomain,apiPublisher,api);
ALTER TABLE API_REQUEST_SUMMARY ADD PRIMARY KEY(api,api_version,version,apiPublisher,consumerKey,userId,context,hostName,year,month,day);
ALTER TABLE API_VERSION_USAGE_SUMMARY ADD PRIMARY KEY(api,version,apiPublisher,context,hostName,year,month,day);
ALTER TABLE API_RESOURCE_USAGE_SUMMARY add primary key(api,version,apiPublisher,consumerKey,context,resourcePath,method,hostName,year,month,day);
ALTER TABLE API_RESPONSE_SUMMARY ADD PRIMARY KEY(api_version,apiPublisher,context,hostName,year,month,day);
ALTER TABLE API_FAULT_SUMMARY ADD PRIMARY KEY(api,version,apiPublisher,consumerKey,context,hostName,year,month,day);
ALTER TABLE API_DESTINATION_SUMMARY ADD PRIMARY KEY(api,version,apiPublisher,context,destination,hostName,year,month,day);
ALTER TABLE API_THROTTLED_OUT_SUMMARY ADD COLUMN throttledOutReason VARCHAR(256) NOT NULL DEFAULT '-';
ALTER TABLE API_THROTTLED_OUT_SUMMARY ADD PRIMARY KEY (api,api_version,context,apiPublisher,applicationName,tenantDomain,year,month,day,throttledOutReason);
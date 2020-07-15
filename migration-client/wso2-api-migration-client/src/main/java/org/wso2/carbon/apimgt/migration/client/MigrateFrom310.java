/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.migration.client;

import io.swagger.models.apideclaration.Api;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.migration.APIMigrationException;
import org.wso2.carbon.apimgt.migration.client.sp_migration.APIMStatMigrationException;
import org.wso2.carbon.apimgt.migration.dao.APIMgtDAO;
import org.wso2.carbon.apimgt.migration.dto.ResourceScopeInfoDTO;
import org.wso2.carbon.apimgt.migration.dto.ResourceScopeMappingDTO;
import org.wso2.carbon.apimgt.migration.dto.ScopeInfoDTO;
import org.wso2.carbon.apimgt.migration.dto.APIURLMappingInfoDTO;
import org.wso2.carbon.apimgt.migration.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.migration.dto.APIInfoScopeMappingDTO;
import org.wso2.carbon.apimgt.migration.dto.APIScopeMappingDTO;
import org.wso2.carbon.apimgt.migration.dto.AMAPIResourceScopeMappingDTO;
import org.wso2.carbon.apimgt.migration.util.RegistryService;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MigrateFrom310 extends MigrationClientBase implements MigrationClient {

    private static final Log log = LogFactory.getLog(ScopeRoleMappingPopulationClient.class);
    private static final String SEPERATOR = "/";
    private static final String SPLITTER = ":";
    private static final String TENANT_IDENTIFIER = "t";
    private static final String APPLICATION_ROLE_PREFIX = "Application/";
    private RegistryService registryService;

    public MigrateFrom310(String tenantArguments, String blackListTenantArguments, String tenantRange,
                          RegistryService registryService, TenantManager tenantManager) throws UserStoreException {
        super(tenantArguments, blackListTenantArguments, tenantRange, tenantManager);
        this.registryService = registryService;
    }

    @Override
    public void databaseMigration() throws APIMigrationException, SQLException {
    }

    @Override
    public void registryResourceMigration() throws APIMigrationException {
    }

    @Override
    public void fileSystemMigration() throws APIMigrationException {
    }

    @Override
    public void cleanOldResources() throws APIMigrationException {
    }

    @Override
    public void statsMigration() throws APIMigrationException, APIMStatMigrationException {
    }

    @Override
    public void tierMigration(List<String> options) throws APIMigrationException {
    }

    @Override
    public void updateArtifacts() throws APIMigrationException {
    }

    @Override
    public void populateSPAPPs() throws APIMigrationException {
    }

    @Override
    public void populateScopeRoleMapping() throws APIMigrationException {
    }

    @Override
    public void scopeMigration() throws APIMigrationException {
        APIMgtDAO apiMgtDAO = APIMgtDAO.getInstance();
        // Step 1: remove duplicate entries
        ArrayList<APIScopeMappingDTO> duplicateList = new ArrayList<>();
        ArrayList<APIScopeMappingDTO> scopeAMData = apiMgtDAO.getAMScopeData();
        ArrayList<ResourceScopeInfoDTO> scopeResourceData = apiMgtDAO.getResourceScopeData();
        for (APIScopeMappingDTO scopeAMDataDTO : scopeAMData) {
            int flag = 0;
            for (ResourceScopeInfoDTO resourceScopeInfoDTO : scopeResourceData) {
                if (scopeAMDataDTO.getScopeId() == Integer.parseInt(resourceScopeInfoDTO.getScopeId())) {
                    flag += 1;
                }
            }
            if (flag == 0) {
                duplicateList.add(scopeAMDataDTO);
            }
        }
        apiMgtDAO.removeDuplicateScopeEntries(duplicateList);

        // Step 2: Remove duplicate versioned scopes registered for versioned APIs
        ArrayList<APIInfoScopeMappingDTO> apiInfoScopeMappingDTOS = apiMgtDAO.getAPIInfoScopeData();
        Map<String, Integer> apiScopeToScopeIdMapping = new HashMap<>();
        for (APIInfoScopeMappingDTO scopeInfoDTO : apiInfoScopeMappingDTOS) {
            String apiScopeKey = scopeInfoDTO.getApiName() + ":" + scopeInfoDTO.getApiProvider() +
                    ":" + scopeInfoDTO.getScopeName();
            if (apiScopeToScopeIdMapping.containsKey(apiScopeKey)) {
                int scopeId = apiScopeToScopeIdMapping.get(apiScopeKey);
                if (scopeId != scopeInfoDTO.getScopeId()) {
                    apiMgtDAO.updateScopeResource(scopeId, scopeInfoDTO.getResourcePath(), scopeInfoDTO.getScopeId());
                    APIScopeMappingDTO apiScopeMappingDTO = new APIScopeMappingDTO();
                    apiScopeMappingDTO.setApiId(scopeInfoDTO.getApiId());
                    apiScopeMappingDTO.setScopeId(scopeInfoDTO.getScopeId());
                    ArrayList<APIScopeMappingDTO> scopeRemovalList = new ArrayList<>();
                    scopeRemovalList.add(apiScopeMappingDTO);
                    apiMgtDAO.removeDuplicateScopeEntries(scopeRemovalList);
                }
            } else {
                apiScopeToScopeIdMapping.put(apiScopeKey, scopeInfoDTO.getScopeId());
            }
        }

        // Step 3: Move entries in IDN_RESORCE_SCOPE_MAPPING table to AM_API_RESOURCE_SCOPE_MAPPING table
        ArrayList<APIInfoDTO> apiData = apiMgtDAO.getAPIData();
        ArrayList<APIURLMappingInfoDTO> urlMappingData = apiMgtDAO.getAPIURLMappingData();
        List<AMAPIResourceScopeMappingDTO> amapiResourceScopeMappingDTOList = new ArrayList<>();
        for (APIInfoDTO apiInfoDTO : apiData) {
            String context = apiInfoDTO.getApiContext();
            String version = apiInfoDTO.getApiVersion();
            for (APIURLMappingInfoDTO apiurlMappingInfoDTO : urlMappingData) {
                if (apiurlMappingInfoDTO.getApiId() == apiInfoDTO.getApiId()) {
                    String resourcePath = context + "/" + version + apiurlMappingInfoDTO.getUrlPattern() + ":" +
                            apiurlMappingInfoDTO.getHttpMethod();
                    int urlMappingId = apiurlMappingInfoDTO.getUrlMappingId();
                    int scopeId = apiMgtDAO.getScopeId(resourcePath);
                    if (scopeId != -1) {
                        ScopeInfoDTO scopeInfoDTO = apiMgtDAO.getScopeInfoByScopeId(scopeId);
                        String scopeName = scopeInfoDTO.getScopeName();
                        int tenantId = scopeInfoDTO.getTenantID();
                        AMAPIResourceScopeMappingDTO amapiResourceScopeMappingDTO = new AMAPIResourceScopeMappingDTO();
                        amapiResourceScopeMappingDTO.setScopeName(scopeName);
                        amapiResourceScopeMappingDTO.setUrlMappingId(urlMappingId);
                        amapiResourceScopeMappingDTO.setTenantId(tenantId);
                        amapiResourceScopeMappingDTOList.add(amapiResourceScopeMappingDTO);
                    }
                }
            }
        }
        apiMgtDAO.addDataToResourceScopeMapping(amapiResourceScopeMappingDTOList);
    }

    @Override
    public void spMigration() throws APIMigrationException {

        List<Tenant> tenantList = getTenantsArray();
        // Iterate for each tenant. The reason we do this migration step wise for each tenant is so that, we do not
        // overwhelm the amount of rows returned for each database call in systems with a large tenant count.
        for (Tenant tenant : tenantList) {
            ArrayList<String> appNames =  APIMgtDAO.getAppsByTenantId(tenant.getId());
            ArrayList<String> appRoleNames =  new ArrayList<>();
            if (appNames != null) {
                for (String applicationName : appNames) {
                    appRoleNames.add(APPLICATION_ROLE_PREFIX.concat(applicationName.trim()));
                }
                try {
                    RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
                    UserRealm realm = realmService.getTenantUserRealm(tenant.getId());
                    UserStoreManager manager = realm.getUserStoreManager();
                    if (manager.isExistingUser(tenant.getAdminName())) {
                        // Passing null for deleted scopes. The rest api properly handles this null value.
                        manager.updateRoleListOfUser(tenant.getAdminName(), null,
                                appRoleNames.toArray(new String[0]));
                    }
                } catch (UserStoreException e) {
                    log.error("Error in updating tenant admin user roles for application retrieval!", e);
                }
                // We extract the tenant aware username and separate the domain.
                String userDomain = UserCoreUtil.extractDomainFromName(tenant.getAdminName());
                String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(tenant.getAdminName());
                String userName = UserCoreUtil.removeDomainFromName(tenantAwareUsername);
                APIMgtDAO.updateSPAppOwner(tenant.getId(), userName, userDomain);
            }

            ArrayList<String> consumerKeys =  APIMgtDAO.getAppsOfTypeJWT(tenant.getId());
            if (consumerKeys != null) {
                for (String consumerKey : consumerKeys) {
                    APIMgtDAO.updateTokenTypeToJWT(consumerKey);
                }
            }
        }
    }

    @Override
    public void appGrantMigration() throws APIMigrationException {
        APIMgtDAO.updateGrantType();
    }
}

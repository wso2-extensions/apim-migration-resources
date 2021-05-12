/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.migration.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.migration.APIMigrationException;
import org.wso2.carbon.apimgt.migration.client.internal.ServiceHolder;
import org.wso2.carbon.apimgt.migration.dao.APIMgtDAO;
import org.wso2.carbon.apimgt.migration.dto.OauthAppInfoDto;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This Class used to handle Missing OAuth App Assignment for roles in Service Provider.
 */
public class OauthAppAssignmentClient extends MigrationClientBase {

    private static final Log log = LogFactory.getLog(OauthAppAssignmentClient.class);
    APIMgtDAO apiMgtDAO = APIMgtDAO.getInstance();

    public OauthAppAssignmentClient(String tenantArguments, String blackListTenantArguments, String tenantRange,
                                    TenantManager tenantManager) throws UserStoreException {

        super(tenantArguments, blackListTenantArguments, tenantRange, tenantManager);
    }

    public void assignOauthAppToOwners() throws APIMigrationException {

        for (Tenant tenant : getTenantsArray()) {
            Map<String, List<OauthAppInfoDto>> oauthAppInfoDtos = apiMgtDAO.retrieveOAuthAppInfoDtoForTenants(tenant);
            for (Map.Entry<String, List<OauthAppInfoDto>> oauthAppInfoDto : oauthAppInfoDtos.entrySet()) {
                String qualifiedUser = oauthAppInfoDto.getKey();
                try {
                    UserStoreManager userStoreManager =
                            ServiceHolder.getRealmService().getTenantUserRealm(tenant.getId()).getUserStoreManager();
                    boolean existingUser = userStoreManager.isExistingUser(qualifiedUser);
                    if (existingUser) {
                        List<OauthAppInfoDto> oauthApps = oauthAppInfoDto.getValue();
                        if (oauthApps != null) {
                            List<String> roleList = new ArrayList<>();
                            for (OauthAppInfoDto oauthApp : oauthApps) {
                                String qualifiedRole = "Application/".concat(oauthApp.getOauthAppName());
                                boolean existingRole = userStoreManager.isExistingRole(qualifiedRole);
                                if (!existingRole) {
                                    log.debug("User Role " + qualifiedRole + " does not Exist, Creating Role.");
                                    userStoreManager.addRole(qualifiedRole, new String[0], new Permission[0]);
                                    log.debug("User Role " + qualifiedRole + "Created.");
                                }
                                roleList.add(qualifiedRole);
                            }
                            if (roleList.size() > 0) {
                                userStoreManager.updateRoleListOfUser(qualifiedUser, new String[]{},
                                        roleList.toArray(new String[0]));
                            }
                        }
                    } else {
                        log.error("Unable to assign Oauth Applications to User " + qualifiedUser + " due to the user " +
                                "does not exist.");
                    }
                } catch (UserStoreException e) {
                    log.error("Error while Assigning Oauth Apps to user ", e);
                }
            }
        }

    }
}

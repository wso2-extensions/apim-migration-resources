/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.migration.APIMigrationException;
import org.wso2.carbon.apimgt.migration.util.ApimDBUtil;
import org.wso2.carbon.apimgt.migration.util.RegistryService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MigrateFrom310to320 extends MigrationClientBase implements MigrationClient {

    private static final Log log = LogFactory.getLog(MigrateFrom310to320.class);
    private RegistryService registryService;

    public MigrateFrom310to320(String tenantArguments, String blackListTenantArguments, String tenantRange, RegistryService
            registryService, TenantManager tenantManager)
            throws UserStoreException {
        super(tenantArguments, blackListTenantArguments, tenantRange, tenantManager);
        this.registryService = registryService;
    }

    @Override
    public void databaseMigration() throws APIMigrationException, SQLException {
    }

    @Override
    public void registryResourceMigration() throws APIMigrationException {
//        migrateFaultSequencesInRegistry();
    }

    @Override
    public void fileSystemMigration() throws APIMigrationException {
    }

    @Override
    public void cleanOldResources() throws APIMigrationException {
    }

    @Override
    public void statsMigration() throws APIMigrationException {
    }

    @Override
    public void tierMigration(List<String> options) throws APIMigrationException {
    }

    @Override
    public void updateArtifacts() throws APIMigrationException {
    }

    @Override
    public void populateSPAPPs() throws APIMigrationException {
        List<Tenant> tenantList = getTenantsArray();
        for (Tenant tenant : tenantList) {
            ArrayList<String> appNames =  ApimDBUtil.getAppsByTenantId(tenant.getId());
            if (appNames != null) {
                for (String applicationName : appNames) {
                    String applicationRole = "Application/".concat(applicationName.trim());
                    try {
                        RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
                        UserRealm realm = realmService.getTenantUserRealm(tenant.getId());
                        UserStoreManager manager = realm.getUserStoreManager();
                        if (manager.isExistingUser(tenant.getAdminName())) {
                            manager.updateRoleListOfUser(tenant.getAdminName(), null,
                                    new String[] {applicationRole} );
                        }
                    } catch (UserStoreException e) {
                        e.printStackTrace();
                    }
                }
                String userDomain = UserCoreUtil.extractDomainFromName(tenant.getAdminName());
                String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(tenant.getAdminName());
                String userName = UserCoreUtil.removeDomainFromName(tenantAwareUsername);
                ApimDBUtil.updateSPAppOwner(tenant.getId(), userName, userDomain);
            }
        }
    }

    private void migrateFaultSequencesInRegistry() {

    }

    private static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}

/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.migration.APIMigrationException;
import org.wso2.carbon.apimgt.migration.client.sp_migration.APIMStatMigrationException;
import org.wso2.carbon.apimgt.migration.dao.APIMgtDAO;
import org.wso2.carbon.apimgt.migration.util.RegistryService;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MigrateFrom400 extends MigrationClientBase implements MigrationClient {
    APIMgtDAO apiMgtDAO = APIMgtDAO.getInstance();

    public MigrateFrom400(String tenantArguments, String blackListTenantArguments, String tenantRange,
                          RegistryService registryService, TenantManager tenantManager) throws UserStoreException {
        super(tenantArguments, blackListTenantArguments, tenantRange, tenantManager);
    }

    @Override
    public void databaseMigration() throws APIMigrationException, SQLException {
        populateApiCategoryOrganizations();
        populateApiOrganizations();
        populateApplicationOrganizations();
        populateSubscriberOrganizations();
    }

    private void populateApiCategoryOrganizations() throws APIMigrationException {
        try {
            Map<Integer, String> tenantIdsAndOrganizations = APIUtil.getAllTenantsWithSuperTenant().stream()
                    .collect(Collectors.toMap(Tenant::getId, Tenant::getDomain));
            apiMgtDAO.updateApiCategoryOrganizations(tenantIdsAndOrganizations);
        } catch (UserStoreException e) {
            throw new APIMigrationException("Failed to retrieve tenants");
        }
    }

    private void populateApiOrganizations() throws APIMigrationException {
        apiMgtDAO.updateApiOrganizations();
    }

    private void populateApplicationOrganizations() throws APIMigrationException {
        Map<Integer, String> subscriberOrganizations = new HashMap<>();
        Map<Integer, Integer> subscriberIdsAndTenantIds = apiMgtDAO.getSubscriberIdsAndTenantIds();
        for (Map.Entry<Integer, Integer> subscriberIdAndTenantId : subscriberIdsAndTenantIds.entrySet()) {
            String organization = APIUtil.getTenantDomainFromTenantId(subscriberIdAndTenantId.getValue());
            subscriberOrganizations.put(subscriberIdAndTenantId.getKey(), organization);
        }
        apiMgtDAO.updateApplicationOrganizations(subscriberOrganizations);
    }

    private void populateSubscriberOrganizations() throws APIMigrationException {
        Map<String, String> subscriberIdsAndOrganizations = new HashMap<>();
        Map<String, Integer> userIdsAndTenantIds = apiMgtDAO.getSUserIdsAndTenantIds();
        for (Map.Entry<String, Integer> userIdsAndTenantId : userIdsAndTenantIds.entrySet()) {
            String organization = APIUtil.getTenantDomainFromTenantId(userIdsAndTenantId.getValue());
            subscriberIdsAndOrganizations.put(userIdsAndTenantId.getKey(), organization);
        }
        apiMgtDAO.updateSubscribersOrganizations(subscriberIdsAndOrganizations);

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

    }

    @Override
    public void spMigration() throws APIMigrationException {

    }

    @Override
    public void updateScopeRoleMappings() throws APIMigrationException {

    }
}

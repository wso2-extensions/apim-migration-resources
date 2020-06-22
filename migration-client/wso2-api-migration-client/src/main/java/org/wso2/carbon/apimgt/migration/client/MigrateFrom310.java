package org.wso2.carbon.apimgt.migration.client;

import io.swagger.models.apideclaration.Api;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.migration.APIMigrationException;
import org.wso2.carbon.apimgt.migration.client.sp_migration.APIMStatMigrationException;
import org.wso2.carbon.apimgt.migration.dao.APIMgtDAO;
import org.wso2.carbon.apimgt.migration.dto.ResourceScopeMappingDTO;
import org.wso2.carbon.apimgt.migration.util.RegistryService;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MigrateFrom310 extends MigrationClientBase implements MigrationClient {

    private static final Log log = LogFactory.getLog(ScopeRoleMappingPopulationClient.class);
    private static final String RESOURCE_PATH = "RESOURCE_PATH";
    private static final String SCOPE_ID = "SCOPE_ID";
    private static final String TENANT_ID = "TENANT_ID";
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
        try {
            for (Tenant tenant : getTenantsArray()) {
                List<ResourceScopeMappingDTO> resourceScopeMappingDTOs = new ArrayList<>();
                ResultSet rsResourceScopeData = apiMgtDAO.getResourceScopeData(Integer.toString(tenant.getId()));
                while (rsResourceScopeData.next()) {
                    String resourcePath = rsResourceScopeData.getString(RESOURCE_PATH);
                    String scopeId = rsResourceScopeData.getString(SCOPE_ID);
                    String tenantID = rsResourceScopeData.getString(TENANT_ID);
                    String[] resourcePathArray = resourcePath.split(SEPERATOR);
                    String[] splittedResPathArray = resourcePath.split(SPLITTER);
                    String context = null;
                    String urlPattern = null;
                    String urlPatternWithMethod = resourcePathArray[resourcePathArray.length - 1];
                    String[] urlPatternArray = urlPatternWithMethod.split(SPLITTER);
                    urlPattern = SEPERATOR + urlPatternArray[0];
                    String httpMethod = splittedResPathArray[1];
                    if (resourcePathArray[1].equals(TENANT_IDENTIFIER)) {
                        context = SEPERATOR + resourcePathArray[1] + SEPERATOR + resourcePathArray[2] + SEPERATOR +
                                resourcePathArray[3] + SEPERATOR + resourcePathArray[4];
                    } else {
                        context = SEPERATOR + resourcePathArray[1] + SEPERATOR + resourcePathArray[2];
                    }
                    String apiId = APIMgtDAO.getInstance().getAPIID(context);
                    if (apiId != null && httpMethod != null && scopeId != null && urlPattern != null &&
                            tenantID != null) {
                        ResourceScopeMappingDTO resourceScopeMappingDTO = new ResourceScopeMappingDTO();
                        resourceScopeMappingDTO.setApiId(apiId);
                        resourceScopeMappingDTO.setHttpMethod(httpMethod);
                        resourceScopeMappingDTO.setScopeId(scopeId);
                        resourceScopeMappingDTO.setUrlPattern(urlPattern);
                        resourceScopeMappingDTO.setTenantID(tenantID);
                        resourceScopeMappingDTOs.add(resourceScopeMappingDTO);
                    }
                }
                apiMgtDAO.addDataToResourceScopeMapping(resourceScopeMappingDTOs);
            }
        } catch (SQLException ex) {
            log.error("Failed to Migrate Scopes", ex);
        }
    }

    @Override
    public void spMigration() throws APIMigrationException {

        List<Tenant> tenantList = getTenantsArray();
        // Iterate for each tenant. The reason we do this migration step wise for each tenant is so that, we do not
        // overwhelm the amount of rows returned for each database call in systems with a large tenant count.
        for (Tenant tenant : tenantList) {
            ArrayList<String> appNames =  APIMgtDAO.getAppsByTenantId(tenant.getId());
            if (appNames != null) {
                for (String applicationName : appNames) {
                    String applicationRole = APPLICATION_ROLE_PREFIX.concat(applicationName.trim());
                    try {
                        RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
                        UserRealm realm = realmService.getTenantUserRealm(tenant.getId());
                        UserStoreManager manager = realm.getUserStoreManager();
                        if (manager.isExistingUser(tenant.getAdminName())) {
                            // Passing null for deleted scopes. The rest api properly handles this null value.
                            manager.updateRoleListOfUser(tenant.getAdminName(), null,
                                    new String[] {applicationRole} );
                        }
                    } catch (UserStoreException e) {
                        log.error("Error in updating tenant admin user roles for application retrieval!", e);
                    }
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
}

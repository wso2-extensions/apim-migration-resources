package org.wso2.carbon.apimgt.migration.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.migration.APIMigrationException;
import org.wso2.carbon.apimgt.migration.dao.APIMgtDAO;
import org.wso2.carbon.apimgt.migration.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.migration.util.RegistryService;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.List;

public class MigrateUUIDToDB extends MigrationClientBase{

    private RegistryService registryService;
    protected Registry registry;
    protected TenantManager tenantManager;
    private static final Log log = LogFactory.getLog(ScopeRoleMappingPopulationClient.class);
    APIMgtDAO apiMgtDAO = APIMgtDAO.getInstance();
    public MigrateUUIDToDB(String tenantArguments, String blackListTenantArguments, String tenantRange,
                           RegistryService registryService, TenantManager tenantManager) throws UserStoreException {
        super(tenantArguments, blackListTenantArguments, tenantRange, tenantManager);
        this.registryService = registryService;
        this.tenantManager = tenantManager;
    }

    /**
     * Get the List of APIs and pass it to DAO method to update the uuid
     * @throws APIMigrationException
     */
    public void moveUUIDToDBFromRegistry() throws APIMigrationException {
        List<APIInfoDTO> apiInfoDTOList = new ArrayList<>();
        try {
            this.registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceUserRegistry();
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(this.registry,
                    APIConstants.API_KEY);
            GenericArtifact[] artifacts = artifactManager.getAllGenericArtifacts();
            for(GenericArtifact artifact: artifacts){
                API api = APIUtil.getAPI(artifact);
                if(api != null){
                    APIInfoDTO apiInfoDTO = new APIInfoDTO();
                    apiInfoDTO.setUuid(api.getUUID());
                    apiInfoDTO.setApiProvider(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
                    apiInfoDTO.setApiName(api.getId().getApiName());
                    apiInfoDTO.setApiVersion(api.getId().getVersion());
                    apiInfoDTOList.add(apiInfoDTO);
                }
            }

            Tenant[] tenants = tenantManager.getAllTenants();
            for(Tenant tenant : tenants)
            {
                int apiTenantId = tenantManager.getTenantId(tenant.getDomain());
                APIUtil.loadTenantRegistry(apiTenantId);
                startTenantFlow(tenant.getDomain());
                this.registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceUserRegistry(
                        MultitenantUtils.getTenantAwareUsername(APIUtil.getTenantAdminUserName(tenant.getDomain())),
                        apiTenantId);
                GenericArtifactManager tenantArtifactManager = APIUtil.getArtifactManager(this.registry,
                        APIConstants.API_KEY);
                GenericArtifact[] tenantArtifacts = tenantArtifactManager.getAllGenericArtifacts();
                for(GenericArtifact artifact: tenantArtifacts){
                    API api = APIUtil.getAPI(artifact);
                    if(api != null){
                        APIInfoDTO apiInfoDTO = new APIInfoDTO();
                        apiInfoDTO.setUuid(api.getUUID());
                        apiInfoDTO.setApiProvider(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
                        apiInfoDTO.setApiName(api.getId().getApiName());
                        apiInfoDTO.setApiVersion(api.getId().getVersion());
                        apiInfoDTOList.add(apiInfoDTO);
                    }
                }
            }
            apiMgtDAO.updateUUID(apiInfoDTOList);

        } catch (RegistryException e) {
            log.error("Error while intitiation the registry", e);
        } catch (UserStoreException e){
            log.error("Error while retrieving the tenants", e);
        } catch (APIManagementException e) {
            log.error("Error while Retrieving API artifact from the registry", e);
        }

    }
    protected void startTenantFlow(String tenantDomain) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
    }
}

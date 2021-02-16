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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.migration.APIMigrationException;
import org.wso2.carbon.apimgt.migration.client.sp_migration.APIMStatMigrationException;
import org.wso2.carbon.apimgt.migration.dao.APIMgtDAO;
import org.wso2.carbon.apimgt.migration.dto.*;
import org.wso2.carbon.apimgt.migration.util.RegistryService;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MigrateFrom320 extends MigrationClientBase implements MigrationClient {

    private static final Log log = LogFactory.getLog(MigrateFrom320.class);
    private static final String SEPERATOR = "/";
    private static final String SPLITTER = ":";
    private static final String TENANT_IDENTIFIER = "t";
    private static char[] TRUST_STORE_PASSWORD = System.getProperty("javax.net.ssl.trustStorePassword").toCharArray();
    private static String TRUST_STORE = System.getProperty("javax.net.ssl.trustStore");
    private static String CERTIFICATE_TYPE = "X.509";
    public static final String BEGIN_CERTIFICATE_STRING = "-----BEGIN CERTIFICATE-----\n";
    public static final String END_CERTIFICATE_STRING = "-----END CERTIFICATE-----";
    private static final String KEY_STORE_TYPE = "JKS";
    private static final String APPLICATION_ROLE_PREFIX = "Application/";
    private RegistryService registryService;
    protected Registry registry;
    private TenantManager tenantManager;
    APIMgtDAO apiMgtDAO = APIMgtDAO.getInstance();

    public MigrateFrom320(String tenantArguments, String blackListTenantArguments, String tenantRange,
                          RegistryService registryService, TenantManager tenantManager) throws UserStoreException {
        super(tenantArguments, blackListTenantArguments, tenantRange, tenantManager);
        this.registryService = registryService;
        this.tenantManager = tenantManager;
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
    public void updateScopeRoleMappings() throws APIMigrationException {
    }

    @Override
    public void scopeMigration() throws APIMigrationException {
    }

    @Override
    public void spMigration() throws APIMigrationException {
    }

    @Override
    public void checkCrossTenantAPISubscriptions(TenantManager tenantManager, boolean ignoreCrossTenantSubscriptions)
            throws APIMigrationException {
    }

    public void updateRegistryPathsOfIconAndWSDL() throws APIMigrationException {
        try {
            List<Tenant> tenants = APIUtil.getAllTenantsWithSuperTenant();
            for (Tenant tenant : tenants) {
                List<APIInfoDTO> apiInfoDTOList = new ArrayList<>();
                int apiTenantId = tenantManager.getTenantId(tenant.getDomain());
                APIUtil.loadTenantRegistry(apiTenantId);
                startTenantFlow(tenant.getDomain(), apiTenantId,
                        MultitenantUtils.getTenantAwareUsername(APIUtil.getTenantAdminUserName(tenant.getDomain())));
                this.registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceUserRegistry(
                        MultitenantUtils.getTenantAwareUsername(APIUtil.getTenantAdminUserName(tenant.getDomain())),
                        apiTenantId);
                GenericArtifactManager tenantArtifactManager = APIUtil.getArtifactManager(this.registry,
                        APIConstants.API_KEY);
                GenericArtifact[] tenantArtifacts = tenantArtifactManager.getAllGenericArtifacts();
                for (GenericArtifact artifact : tenantArtifacts) {
                    API api = APIUtil.getAPI(artifact);
                    if (api != null) {
                        APIInfoDTO apiInfoDTO = new APIInfoDTO();
                        apiInfoDTO.setUuid(api.getUUID());
                        apiInfoDTO.setApiProvider(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
                        apiInfoDTO.setApiName(api.getId().getApiName());
                        apiInfoDTO.setApiVersion(api.getId().getVersion());
                        apiInfoDTOList.add(apiInfoDTO);
                    }
                }
                for (APIInfoDTO apiInfoDTO : apiInfoDTOList) {
                    String apiPath = GovernanceUtils.getArtifactPath(registry, apiInfoDTO.getUuid());
                    int prependIndex = apiPath.lastIndexOf("/api");
                    String artifactPath = apiPath.substring(0, prependIndex);
                    String artifactOldPathIcon = APIConstants.API_IMAGE_LOCATION + RegistryConstants.PATH_SEPARATOR
                            + apiInfoDTO.getApiProvider() + RegistryConstants.PATH_SEPARATOR + apiInfoDTO.getApiName()
                            + RegistryConstants.PATH_SEPARATOR + apiInfoDTO.getApiVersion()
                            + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;
                    if (registry.resourceExists(artifactOldPathIcon)) {
                        Resource resource = registry.get(artifactOldPathIcon);
                        String thumbPath = artifactPath + RegistryConstants.PATH_SEPARATOR
                                + APIConstants.API_ICON_IMAGE;
                        registry.put(thumbPath, resource);
                        GenericArtifact apiArtifact = tenantArtifactManager.getGenericArtifact(apiInfoDTO.getUuid());
                        apiArtifact.setAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL, thumbPath);
                        tenantArtifactManager.updateGenericArtifact(apiArtifact);
                    }
                    startTenantFlow(tenant.getDomain(), apiTenantId,
                            MultitenantUtils.getTenantAwareUsername(APIUtil.getTenantAdminUserName(tenant.getDomain())));
                    String wsdlResourcePathOld = APIConstants.API_WSDL_RESOURCE_LOCATION
                            + RegistryPersistenceUtil.createWsdlFileName(apiInfoDTO.getApiProvider(),
                            apiInfoDTO.getApiName(), apiInfoDTO.getApiVersion());
                    if (registry.resourceExists(wsdlResourcePathOld)) {
                        Resource resource = registry.get(wsdlResourcePathOld);
                        String wsdlResourcePath;
                        String wsdlResourcePathArchive = artifactPath + RegistryConstants.PATH_SEPARATOR
                                + APIConstants.API_WSDL_ARCHIVE_LOCATION + apiInfoDTO.getApiProvider()
                                + APIConstants.WSDL_PROVIDER_SEPERATOR + apiInfoDTO.getApiName()
                                + apiInfoDTO.getApiVersion() + APIConstants.ZIP_FILE_EXTENSION;
                        String wsdlResourcePathFile = artifactPath + RegistryConstants.PATH_SEPARATOR
                                + RegistryPersistenceUtil.createWsdlFileName(apiInfoDTO.getApiProvider(),
                                apiInfoDTO.getApiName(), apiInfoDTO.getApiVersion());
                        if (APIConstants.APPLICATION_ZIP.equals(resource.getMediaType())) {
                            wsdlResourcePath = wsdlResourcePathArchive;
                        } else {
                            wsdlResourcePath = wsdlResourcePathFile;
                        }
                        registry.copy(wsdlResourcePathOld, wsdlResourcePath);
                        GenericArtifact apiArtifact = tenantArtifactManager.getGenericArtifact(apiInfoDTO.getUuid());
                        String absoluteWSDLResourcePath = RegistryUtils
                                .getAbsolutePath(RegistryContext.getBaseInstance(),
                                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + wsdlResourcePath;
                        String wsdlRegistryPath = RegistryConstants.PATH_SEPARATOR + "registry" +
                                RegistryConstants.PATH_SEPARATOR + "resource" + absoluteWSDLResourcePath;
                        apiArtifact.setAttribute(APIConstants.API_OVERVIEW_WSDL, wsdlRegistryPath);
                        tenantArtifactManager.updateGenericArtifact(apiArtifact);
                    }
                }
                PrivilegedCarbonContext.endTenantFlow();
            }
        } catch (RegistryException e) {
            log.error("Error while intitiation the registry", e);
        } catch (UserStoreException e) {
            log.error("Error while retrieving the tenants", e);
        } catch (APIManagementException e) {
            log.error("Error while Retrieving API artifact from the registry", e);
        }
    }

    public void apiRevisionRelatedMigration() throws APIMigrationException {
        try {
            List<Tenant> tenants = APIUtil.getAllTenantsWithSuperTenant();
            for (Tenant tenant : tenants) {
                List<APIInfoDTO> apiInfoDTOList = new ArrayList<>();
                int apiTenantId = tenantManager.getTenantId(tenant.getDomain());
                APIUtil.loadTenantRegistry(apiTenantId);
                startTenantFlow(tenant.getDomain(), apiTenantId,
                        MultitenantUtils.getTenantAwareUsername(APIUtil.getTenantAdminUserName(tenant.getDomain())));
                this.registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceUserRegistry(
                        MultitenantUtils.getTenantAwareUsername(APIUtil.getTenantAdminUserName(tenant.getDomain())),
                        apiTenantId);
                GenericArtifactManager tenantArtifactManager = APIUtil.getArtifactManager(this.registry,
                        APIConstants.API_KEY);
                GenericArtifact[] tenantArtifacts = tenantArtifactManager.getAllGenericArtifacts();
                APIProvider apiProviderTenant = APIManagerFactory.getInstance().getAPIProvider(
                        APIUtil.getTenantAdminUserName(tenant.getDomain()));
                for (GenericArtifact artifact : tenantArtifacts) {
                    API api = APIUtil.getAPI(artifact);
                    if (api != null) {
                        APIInfoDTO apiInfoDTO = new APIInfoDTO();
                        apiInfoDTO.setUuid(api.getUUID());
                        apiInfoDTO.setApiProvider(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
                        apiInfoDTO.setApiName(api.getId().getApiName());
                        apiInfoDTO.setApiVersion(api.getId().getVersion());
                        apiInfoDTOList.add(apiInfoDTO);
                    }
                }
                for (APIInfoDTO apiInfoDTO : apiInfoDTOList) {
                    //adding the api revision
                    APIRevision apiRevision = new APIRevision();
                    apiRevision.setApiUUID(apiInfoDTO.getUuid());
                    apiRevision.setDescription("Initial revision created in migration process");
                    String revisionId = apiProviderTenant.addAPIRevision(apiRevision, tenant.getDomain());
                    // retrieve api artifacts
                    GenericArtifact apiArtifact = tenantArtifactManager.getGenericArtifact(apiInfoDTO.getUuid());
                    List<APIRevisionDeployment> apiRevisionDeployments = new ArrayList<APIRevisionDeployment>();
                    String environemnts = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS);
                    String[] arrOfEnvironments = environemnts.split(",");
                    for (String environment : arrOfEnvironments) {
                        APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
                        apiRevisionDeployment.setRevisionUUID(revisionId);
                        apiRevisionDeployment.setDeployment(environment);
                        apiRevisionDeployment.setDisplayOnDevportal(true);
                        apiRevisionDeployments.add(apiRevisionDeployment);
                    }
                    if (!apiRevisionDeployments.isEmpty()) {
                        apiProviderTenant.addAPIRevisionDeployment(apiInfoDTO.getUuid(), revisionId,
                                apiRevisionDeployments);
                    }
                }
            }
        } catch (RegistryException e) {
            log.error("Error while intitiation the registry", e);
        } catch (UserStoreException e){
            log.error("Error while retrieving the tenants", e);
        } catch (APIManagementException e) {
            log.error("Error while Retrieving API artifact from the registry", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    protected void startTenantFlow(String tenantDomain, int tenantId, String username) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
    }

    public void migrateEndpointCertificates() {

        File trustStoreFile = new File(TRUST_STORE);

        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (InputStream localTrustStoreStream = new FileInputStream(trustStoreFile)) {
                trustStore.load(localTrustStoreStream, TRUST_STORE_PASSWORD);
            }
            Set<String> aliases = APIMgtDAO.getInstance().retrieveListOfEndpointCertificateAliases();
            Map<String, String> certificateMap = new HashMap<>();
            if (aliases != null) {
                for (String alias : aliases) {
                    Certificate certificate = trustStore.getCertificate(alias);
                    if (certificate != null) {
                        byte[] encoded = Base64.encodeBase64(certificate.getEncoded());
                        String base64EncodedString = BEGIN_CERTIFICATE_STRING.concat(new String(encoded)).concat("\n")
                                .concat(END_CERTIFICATE_STRING);
                        base64EncodedString = Base64.encodeBase64URLSafeString(base64EncodedString.getBytes());
                        certificateMap.put(alias, base64EncodedString);
                    }
                }
            }
            APIMgtDAO.getInstance().updateEndpointCertificates(certificateMap);
        } catch (NoSuchAlgorithmException | IOException | CertificateException
                | KeyStoreException | APIMigrationException e) {
            log.error("Error while Migrating Endpoint Certificates", e);
        }
    }
}

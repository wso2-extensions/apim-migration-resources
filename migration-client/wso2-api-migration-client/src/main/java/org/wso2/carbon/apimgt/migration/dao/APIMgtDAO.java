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

package org.wso2.carbon.apimgt.migration.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.migration.APIMigrationException;
import org.wso2.carbon.apimgt.migration.dto.ResourceScopeInfoDTO;
import org.wso2.carbon.apimgt.migration.dto.ResourceScopeMappingDTO;
import org.wso2.carbon.apimgt.migration.dto.UserRoleFromPermissionDTO;
import org.wso2.carbon.apimgt.migration.util.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class APIMgtDAO {
    private static final Log log = LogFactory.getLog(APIMgtDAO.class);
    private static APIMgtDAO INSTANCE = null;
    private static final String RESOURCE_PATH = "RESOURCE_PATH";
    private static final String SCOPE_ID = "SCOPE_ID";
    private static final String TENANT_ID = "TENANT_ID";
    private static String GET_RESOURCE_SCOPE_SQL = "SELECT * FROM IDN_OAUTH2_RESOURCE_SCOPE WHERE TENANT_ID = ?";
    private static String INSERT_INTO_AM_API_RESOURCE_SCOPE_MAPPING =
            "INSERT INTO AM_API_RESOURCE_SCOPE_MAPPING VALUES " +
                    "((SELECT NAME FROM IDN_OAUTH2_SCOPE WHERE SCOPE_ID = ?), " +
                    "(SELECT URL_MAPPING_ID FROM AM_API_URL_MAPPING WHERE API_ID = ? AND HTTP_METHOD = ? " +
                    "AND URL_PATTERN = ?), ? );";
    private static String GET_APPS_BY_TENANT_ID = "SELECT APP_NAME\n" +
                    "FROM IDN_OAUTH_CONSUMER_APPS OCA INNER JOIN AM_APPLICATION_KEY_MAPPING AKM ON" +
                    " OCA.CONSUMER_KEY=AKM.CONSUMER_KEY\n" +
                    "WHERE TENANT_ID = ?";
    private static String GET_APPS_OF_TYPE_JWT = "SELECT IOP.CONSUMER_KEY FROM AM_APPLICATION AMA \n" +
            "INNER JOIN AM_APPLICATION_KEY_MAPPING AKM ON AMA.APPLICATION_ID=AKM.APPLICATION_ID\n" +
            "INNER JOIN IDN_OIDC_PROPERTY IOP ON AKM.CONSUMER_KEY=IOP.CONSUMER_KEY\n" +
            "WHERE AMA.TOKEN_TYPE = 'JWT' AND PROPERTY_KEY  = 'tokenType' AND TENANT_ID = ?";
    private static String UPDATE_SERVICE_PROVIDER_OWNER = "UPDATE" +
            " IDN_OAUTH_CONSUMER_APPS SET USERNAME = ?, USER_DOMAIN = ? WHERE TENANT_ID = ? " +
            "AND CONSUMER_KEY IN (SELECT CONSUMER_KEY FROM AM_APPLICATION_KEY_MAPPING);";
    private static String UPDATE_TOKEN_TYPE_TO_JWT = "UPDATE IDN_OIDC_PROPERTY SET" +
            " PROPERTY_VALUE = ? WHERE PROPERTY_KEY  = 'tokenType' AND CONSUMER_KEY = ?;";

    private static String GET_API_ID = "SELECT API_ID FROM AM_API WHERE CONTEXT = ?";

    private APIMgtDAO() {
    }

    public static APIMgtDAO getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new APIMgtDAO();
        }
        return INSTANCE;
    }

    /**
     * This mehthod is used to get data from IDN_OAUTH2_RESOURCE_SCOPE by tenant id
     * @param tenantId
     * @return
     * @throws APIMigrationException
     */
    public ArrayList<ResourceScopeInfoDTO> getResourceScopeData(String tenantId) throws APIMigrationException {
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(GET_RESOURCE_SCOPE_SQL)) {
                ps.setString(1, tenantId);
                try (ResultSet resultSet = ps.executeQuery()) {
                    ArrayList<ResourceScopeInfoDTO> scopeInfoList = new ArrayList<>();
                    while (resultSet.next()) {
                        ResourceScopeInfoDTO scopeInfo = new ResourceScopeInfoDTO();
                        scopeInfo.setResourcePath(resultSet.getString(RESOURCE_PATH));
                        scopeInfo.setScopeId(resultSet.getString(SCOPE_ID));
                        scopeInfo.setTenantID(resultSet.getString(TENANT_ID));
                        scopeInfoList.add(scopeInfo);
                    }
                    return scopeInfoList;
                }
            }
        } catch (SQLException ex) {
            throw new APIMigrationException("Failed to get data from IDN_OAUTH2_RESOURCE_SCOPE", ex);
        }
    }

    /**
     * This method is used to get API Id using API context
     * @param context
     * @return
     * @throws APIMigrationException
     */
    public String getAPIID(String context) throws APIMigrationException {
        String apiId = null;
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(GET_API_ID)) {
                ps.setString(1, context);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        apiId = rs.getString("API_ID");
                    }
                }
            }
        } catch (SQLException ex) {
            throw new APIMigrationException("Failed to get API ID using context : " + context, ex);
        }
        return apiId;
    }

    /**
     * This method is used to insert data to AM_API_RESOURCE_SCOPE_MAPPING table
     * @param resourceScopeMappingDTOS
     * @throws APIMigrationException
     */
    public void addDataToResourceScopeMapping(List<ResourceScopeMappingDTO> resourceScopeMappingDTOS)
            throws APIMigrationException {
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement psAddResourceScope =
                         conn.prepareStatement(INSERT_INTO_AM_API_RESOURCE_SCOPE_MAPPING)) {
                for (ResourceScopeMappingDTO resourceScopeMappingDTO : resourceScopeMappingDTOS) {
                    psAddResourceScope.setString(1, resourceScopeMappingDTO.getScopeId());
                    psAddResourceScope.setString(2, resourceScopeMappingDTO.getApiId());
                    psAddResourceScope.setString(3, resourceScopeMappingDTO.getHttpMethod());
                    psAddResourceScope.setString(4, resourceScopeMappingDTO.getUrlPattern());
                    psAddResourceScope.setString(5, resourceScopeMappingDTO.getTenantID());
                    psAddResourceScope.addBatch();
                }
                psAddResourceScope.executeBatch();
            }
        } catch (SQLException ex) {
            throw new APIMigrationException("Failed to add dato AM_API_RESOURCE_SCOPE_MAPPING table : ", ex);
        }
    }

    /**
     * Get the list of names of applications created via the dev portal for a given tenant
     *
     * @param tenantId The relevant tenant of which applications needs to be fetched
     * @return List of application names of applications created via the dev portal for a given tenant
     */
    public static ArrayList<String> getAppsByTenantId(int tenantId) throws APIMigrationException {

        ArrayList<String> appNames = new ArrayList<String>();
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(GET_APPS_BY_TENANT_ID)){
                preparedStatement.setInt(1, tenantId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    connection.commit();
                    while (resultSet.next()) {
                        appNames.add(resultSet.getString("APP_NAME"));
                    }
                }
            }

        } catch (SQLException e) {
            throw new APIMigrationException("SQLException when executing: ".concat(GET_APPS_BY_TENANT_ID), e);
        }
        return appNames;
    }

    /**
     * Get the list of consumer keys corresponding to the apps created of the type JWT
     *
     * @param tenantId Relevant tenant ID of the service provider owner
     * @return List of consumer keys corresponding to the apps created of the type JWT
     */
    public static ArrayList<String> getAppsOfTypeJWT(int tenantId) throws APIMigrationException {

        ArrayList<String> consumerKeys = new ArrayList<String>();
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(GET_APPS_OF_TYPE_JWT)){
                preparedStatement.setInt(1, tenantId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    connection.commit();
                    while (resultSet.next()) {
                        consumerKeys.add(resultSet.getString("CONSUMER_KEY"));
                    }
                }
            }

        } catch (SQLException e) {
            throw new APIMigrationException("SQLException when executing: ".concat(GET_APPS_OF_TYPE_JWT), e);
        }
        return consumerKeys;
    }

    /**
     * Alter the owner of the service provider created
     *
     * @param tenantId Tenant ID of the service provider owner
     * @param username Username of the service provider owner
     * @param userDomain User domain of the service provider
     */
    public static void updateSPAppOwner(int tenantId, String username, String userDomain) throws APIMigrationException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SERVICE_PROVIDER_OWNER)){
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, userDomain);
                preparedStatement.setInt(3, tenantId);
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new APIMigrationException("SQLException when executing: ".concat(UPDATE_SERVICE_PROVIDER_OWNER), e);
        }
    }

    /**
     * Updates the token type of the service provider corresponding to the consumer key provided to the JWT token type
     *
     * @param consumerKey The consumer key of the application that needs to be altered
     */
    public static void updateTokenTypeToJWT(String consumerKey) throws APIMigrationException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_TOKEN_TYPE_TO_JWT)){
                preparedStatement.setString(1, "JWT");
                preparedStatement.setString(2, consumerKey);
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new APIMigrationException("SQLException when executing: ".concat(UPDATE_TOKEN_TYPE_TO_JWT), e);
        }
    }
}

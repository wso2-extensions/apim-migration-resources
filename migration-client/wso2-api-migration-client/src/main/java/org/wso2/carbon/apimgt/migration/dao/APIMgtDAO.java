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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.migration.APIMigrationException;
import org.wso2.carbon.apimgt.migration.dto.ResourceScopeInfoDTO;
import org.wso2.carbon.apimgt.migration.dto.ResourceScopeMappingDTO;
import org.wso2.carbon.apimgt.migration.dto.ScopeInfoDTO;
import org.wso2.carbon.apimgt.migration.dto.APIURLMappingInfoDTO;
import org.wso2.carbon.apimgt.migration.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.migration.dto.APIInfoScopeMappingDTO;
import org.wso2.carbon.apimgt.migration.dto.APIScopeMappingDTO;
import org.wso2.carbon.apimgt.migration.dto.AMAPIResourceScopeMappingDTO;
import org.wso2.carbon.apimgt.migration.util.Constants;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
    private static final String SCOPE_NAME = "NAME";
    private static final String URL_MAPPING_ID = "URL_MAPPING_ID";
    private static final String URL_PATTERN = "URL_PATTERN";
    private static final String HTTP_METHOD = "HTTP_METHOD";
    private static final String API_ID = "API_ID";
    private static final String API_NAME = "API_NAME";
    private static final String API_VERSION = "API_VERSION";
    private static final String API_PROVIDER = "API_PROVIDER";
    private static final String CONTEXT = "CONTEXT";
    private static final String CONTEXT_TEMPLATE = "CONTEXT_TEMPLATE";
    private static final String SCOPE_ID = "SCOPE_ID";
    private static final String SCOPE_DISPLAY_NAME = "DISPLAY_NAME";
    private static final String SCOPE_DESCRIPTION = "DESCRIPTION";
    private static final String SCOPE_TYPE = "SCOPE_TYPE";
    private static final String TENANT_ID = "TENANT_ID";
    private static String GET_RESOURCE_SCOPE_SQL = "SELECT * FROM IDN_OAUTH2_RESOURCE_SCOPE";
    private static String GET_AM_API_SQL = "SELECT * FROM AM_API";
    private static String GET_AM_API_URL_MAPPING_SQL = "SELECT * FROM AM_API_URL_MAPPING";
    private static String GET_API_INFO_SCOPE_SQL = "SELECT APIS.API_ID, APIS.SCOPE_ID, API.API_NAME, " +
            " API.API_VERSION, API.API_PROVIDER, IOS.NAME, IORS.RESOURCE_PATH FROM " +
            " ((AM_API_SCOPES APIS LEFT JOIN IDN_OAUTH2_SCOPE IOS  ON APIS.SCOPE_ID =  IOS.SCOPE_ID ) " +
            " LEFT JOIN IDN_OAUTH2_RESOURCE_SCOPE IORS ON APIS.SCOPE_ID =  IORS.SCOPE_ID) LEFT JOIN AM_API API " +
            " ON API.API_ID = APIS.API_ID";
    private static String GET_AM_API_SCOPE_SQL = "SELECT * FROM AM_API_SCOPES";
    private static String GET_SCOPE_BY_ID_SQL = "SELECT * FROM IDN_OAUTH2_SCOPE WHERE SCOPE_ID = ?";
    private static String INSERT_INTO_AM_API_RESOURCE_SCOPE_MAPPING =
            "INSERT INTO AM_API_RESOURCE_SCOPE_MAPPING (SCOPE_NAME, URL_MAPPING_ID, TENANT_ID) VALUES " +
                    "(?,?,?)";
    private static String GET_APPS_BY_TENANT_ID = "SELECT APP_NAME " +
                    "FROM IDN_OAUTH_CONSUMER_APPS OCA INNER JOIN AM_APPLICATION_KEY_MAPPING AKM ON" +
                    " OCA.CONSUMER_KEY=AKM.CONSUMER_KEY " +
                    "WHERE TENANT_ID = ?";
    private static String GET_APPS_OF_TYPE_JWT = "SELECT IOP.CONSUMER_KEY FROM AM_APPLICATION AMA " +
            "INNER JOIN AM_APPLICATION_KEY_MAPPING AKM ON AMA.APPLICATION_ID=AKM.APPLICATION_ID " +
            "INNER JOIN IDN_OIDC_PROPERTY IOP ON AKM.CONSUMER_KEY=IOP.CONSUMER_KEY " +
            "WHERE AMA.TOKEN_TYPE = 'JWT' AND PROPERTY_KEY = 'tokenType' AND TENANT_ID = ?";
    private static String UPDATE_SERVICE_PROVIDER_OWNER = "UPDATE" +
            " IDN_OAUTH_CONSUMER_APPS SET USERNAME = ?, USER_DOMAIN = ? WHERE TENANT_ID = ? " +
            "AND CONSUMER_KEY IN (SELECT CONSUMER_KEY FROM AM_APPLICATION_KEY_MAPPING);";
    private static String UPDATE_TOKEN_TYPE_TO_JWT = "UPDATE IDN_OIDC_PROPERTY SET" +
            " PROPERTY_VALUE = ? WHERE PROPERTY_KEY  = 'tokenType' AND CONSUMER_KEY = ?;";

    private static String GET_API_ID = "SELECT API_ID FROM AM_API WHERE CONTEXT = ?";
    private static String GET_SCOPE_ID = "SELECT SCOPE_ID FROM IDN_OAUTH2_RESOURCE_SCOPE WHERE RESOURCE_PATH = ?";

    private static String DELETE_SCOPE_FROM_AM_API_SCOPES = "DELETE FROM AM_API_SCOPES WHERE" +
            " API_ID = ? AND SCOPE_ID = ?";
    private static String DELETE_SCOPE_FROM_IDN_OAUTH2_SCOPES = "DELETE FROM IDN_OAUTH2_SCOPE WHERE" +
            " SCOPE_ID = ?";
    private static String UPDATE_SCOPE_ID_IN_RESOURCE = "UPDATE" +
            " IDN_OAUTH2_RESOURCE_SCOPE SET SCOPE_ID = ? WHERE SCOPE_ID = ?";

    private static String GET_CONSUMER_KEYS = "SELECT CONSUMER_KEY FROM AM_APPLICATION_KEY_MAPPING";
    private static String GET_GRANT_TYPE = "SELECT GRANT_TYPES from IDN_OAUTH_CONSUMER_APPS where CONSUMER_KEY = ?";
    private static String UPDATE_APP_INFO = "Update AM_APPLICATION_KEY_MAPPING set APP_INFO = ? where CONSUMER_KEY = ?";

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
     * @return
     * @throws APIMigrationException
     */
    public ArrayList<ResourceScopeInfoDTO> getResourceScopeData() throws APIMigrationException {
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(GET_RESOURCE_SCOPE_SQL)) {
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
     * This mehthod is used to get data from AM_API table
     * @return
     * @throws APIMigrationException
     */
    public ArrayList<APIInfoDTO> getAPIData() throws APIMigrationException {
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(GET_AM_API_SQL)) {
                try (ResultSet resultSet = ps.executeQuery()) {
                    ArrayList<APIInfoDTO> apiInfoList = new ArrayList<>();
                    while (resultSet.next()) {
                        APIInfoDTO apiInfoDTO = new APIInfoDTO();
                        apiInfoDTO.setApiId(resultSet.getInt(API_ID));
                        apiInfoDTO.setApiName(resultSet.getString(API_NAME));
                        apiInfoDTO.setApiProvider(resultSet.getString(API_PROVIDER));
                        apiInfoDTO.setApiVersion(resultSet.getString(API_VERSION));
                        apiInfoDTO.setApiContext(resultSet.getString(CONTEXT));
                        apiInfoDTO.setGetApiContextTemplate(resultSet.getString(CONTEXT_TEMPLATE));
                        apiInfoList.add(apiInfoDTO);
                    }
                    return apiInfoList;
                }
            }
        } catch (SQLException ex) {
            throw new APIMigrationException("Failed to get data from AM_API table", ex);
        }
    }

    /**
     * This method is used to get data from AM_API_URL_MAPPING table
     * @return
     * @throws APIMigrationException
     */
    public ArrayList<APIURLMappingInfoDTO> getAPIURLMappingData() throws APIMigrationException {
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(GET_AM_API_URL_MAPPING_SQL)) {
                try (ResultSet resultSet = ps.executeQuery()) {
                    ArrayList<APIURLMappingInfoDTO> apiurlMappingInfoDTOArrayList = new ArrayList<>();
                    while (resultSet.next()) {
                        APIURLMappingInfoDTO apiurlMappingInfoDTO = new APIURLMappingInfoDTO();
                        apiurlMappingInfoDTO.setUrlMappingId(resultSet.getInt(URL_MAPPING_ID));
                        apiurlMappingInfoDTO.setApiId(resultSet.getInt(API_ID));
                        apiurlMappingInfoDTO.setHttpMethod(resultSet.getString(HTTP_METHOD));
                        apiurlMappingInfoDTO.setUrlPattern(resultSet.getString(URL_PATTERN));
                        apiurlMappingInfoDTOArrayList.add(apiurlMappingInfoDTO);
                    }
                    return apiurlMappingInfoDTOArrayList;
                }
            }
        } catch (SQLException ex) {
            throw new APIMigrationException("Failed to get data from AM_API_URL_MAPPING table", ex);
        }
    }

    /**
     * This method is used to get data from AM_API_SCOPE, IDN_OAUTH2_SCOPE by tenant id
     * @return
     * @throws APIMigrationException
     */
    public ArrayList<APIInfoScopeMappingDTO> getAPIInfoScopeData() throws APIMigrationException {
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(GET_API_INFO_SCOPE_SQL)) {
                try (ResultSet resultSet = ps.executeQuery()) {
                    ArrayList<APIInfoScopeMappingDTO> scopeAPIInfoList = new ArrayList<>();
                    while (resultSet.next()) {
                        APIInfoScopeMappingDTO apiInfoScopeMappingDTO = new APIInfoScopeMappingDTO();
                        apiInfoScopeMappingDTO.setScopeId(resultSet.getInt(SCOPE_ID));
                        apiInfoScopeMappingDTO.setApiId(resultSet.getInt(API_ID));
                        apiInfoScopeMappingDTO.setScopeName(resultSet.getString(SCOPE_NAME));
                        apiInfoScopeMappingDTO.setApiName(resultSet.getString(API_NAME));
                        apiInfoScopeMappingDTO.setApiProvider(resultSet.getString(API_PROVIDER));
                        apiInfoScopeMappingDTO.setApiVersion(resultSet.getString(API_VERSION));
                        apiInfoScopeMappingDTO.setResourcePath(resultSet.getString(RESOURCE_PATH));
                        scopeAPIInfoList.add(apiInfoScopeMappingDTO);
                    }
                    return scopeAPIInfoList;
                }
            }
        } catch (SQLException ex) {
            throw new APIMigrationException("Failed to get data from AM_API_SCOPE, IDN_OAUTH2_SCOPE tables", ex);
        }
    }

    /**
     * Alter the scope id in the resource scope
     *
     * @param scopeId Scope ID
     * @param resourcePath Resource Path
     * @param newScopeId Scope ID
     */
    public static void updateScopeResource(int newScopeId, String resourcePath, int scopeId) throws APIMigrationException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SCOPE_ID_IN_RESOURCE)){
                preparedStatement.setInt(1, newScopeId);
                preparedStatement.setInt(2, scopeId);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new APIMigrationException("SQLException when executing: ".concat(UPDATE_SCOPE_ID_IN_RESOURCE), e);
        }
    }


    /**
     * This mehthod is used to get data from AM_API_SCOPE
     * @return
     * @throws APIMigrationException
     */
    public ArrayList<APIScopeMappingDTO> getAMScopeData() throws APIMigrationException {
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(GET_AM_API_SCOPE_SQL)) {
                try (ResultSet resultSet = ps.executeQuery()) {
                    ArrayList<APIScopeMappingDTO> scopeInfoList = new ArrayList<>();
                    while (resultSet.next()) {
                        APIScopeMappingDTO apiScopeMappingDTO = new APIScopeMappingDTO();
                        apiScopeMappingDTO.setApiId(resultSet.getInt(API_ID));
                        apiScopeMappingDTO.setScopeId(resultSet.getInt(SCOPE_ID));
                        scopeInfoList.add(apiScopeMappingDTO);
                    }
                    return scopeInfoList;
                }
            }
        } catch (SQLException ex) {
            throw new APIMigrationException("Failed to get data from AM_API_SCOPE", ex);
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
     * This method is used to retrieve the scope id given the resource path
     * @param resourcePath
     * @throws APIMigrationException
     */
    public int getScopeId(String resourcePath)
            throws APIMigrationException {
        int scopeId = -1;
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(GET_SCOPE_ID)) {
                ps.setString(1, resourcePath);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        scopeId = rs.getInt(SCOPE_ID);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new APIMigrationException("Failed to get SCOPE ID using resource path : " + resourcePath, ex);
        }
        return scopeId;
    }

    /**
     * This method is used to retrieve the scope details given the scopeId
     * @param scopeId
     * @throws APIMigrationException
     */
    public ScopeInfoDTO getScopeInfoByScopeId(int scopeId)
            throws APIMigrationException {
        ScopeInfoDTO scopeInfoDTO = new ScopeInfoDTO();
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(GET_SCOPE_BY_ID_SQL)) {
                ps.setInt(1, scopeId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        scopeInfoDTO.setScopeId(rs.getInt(SCOPE_ID));
                        scopeInfoDTO.setScopeName(rs.getString(SCOPE_NAME));
                        scopeInfoDTO.setScopeDisplayName(rs.getString(SCOPE_DISPLAY_NAME));
                        scopeInfoDTO.setScopeDescription(rs.getString(SCOPE_DESCRIPTION));
                        scopeInfoDTO.setTenantID(rs.getInt(TENANT_ID));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new APIMigrationException("Failed to retrieve the scope details given the scopeId : " + scopeId, ex);
        }
        return scopeInfoDTO;
    }

    /**
     * This method is used to insert data to AM_API_RESOURCE_SCOPE_MAPPING table
     * @param resourceScopeMappingDTOS
     * @throws APIMigrationException
     */
    public void addDataToResourceScopeMapping(List<AMAPIResourceScopeMappingDTO> resourceScopeMappingDTOS)
            throws APIMigrationException {
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement psAddResourceScope =
                         conn.prepareStatement(INSERT_INTO_AM_API_RESOURCE_SCOPE_MAPPING)) {
                for (AMAPIResourceScopeMappingDTO resourceScopeMappingDTO : resourceScopeMappingDTOS) {
                    psAddResourceScope.setString(1, resourceScopeMappingDTO.getScopeName());
                    psAddResourceScope.setInt(2, resourceScopeMappingDTO.getUrlMappingId());
                    psAddResourceScope.setInt(3, resourceScopeMappingDTO.getTenantId());
                    psAddResourceScope.addBatch();
                }
                psAddResourceScope.executeBatch();
            }
        } catch (SQLException ex) {
            throw new APIMigrationException("Failed to add data to AM_API_RESOURCE_SCOPE_MAPPING table : ", ex);
        }
    }

    /**
     * This method is used to remove the duplicate data from IDN_OAUTH2_SCOPE, AM_API_SCOPE
     * and IDN_OAUTH2_SCOPE_BINDING tables
     * @param duplicateList
     * @throws APIMigrationException
     */
    public void removeDuplicateScopeEntries(ArrayList<APIScopeMappingDTO> duplicateList)
            throws APIMigrationException {
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            for (APIScopeMappingDTO apiScopeMappingDTOS : duplicateList) {
                try (PreparedStatement preparedStatement = conn.prepareStatement(DELETE_SCOPE_FROM_AM_API_SCOPES)) {
                    preparedStatement.setInt(1, apiScopeMappingDTOS.getApiId());
                    preparedStatement.setInt(2, apiScopeMappingDTOS.getScopeId());
                    preparedStatement.executeUpdate();
                }
                try (PreparedStatement preparedStatement = conn.prepareStatement(DELETE_SCOPE_FROM_IDN_OAUTH2_SCOPES)) {
                    preparedStatement.setInt(1, apiScopeMappingDTOS.getScopeId());
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new APIMigrationException("Failed to delete duplicate scope data : ", ex);
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

    /**
     * This method is used to update data to AM_APPLICATION_KEY_MAPPING table
     * @throws APIMigrationException
     */
    public static void updateGrantType() throws APIMigrationException {
        ArrayList<String> consumerKeys = new ArrayList<String>();
        String consumerKey = null;
        String grantType = null;
        String grantJson = null;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(GET_CONSUMER_KEYS)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    connection.commit();
                    while (resultSet.next()) {
                        consumerKey = resultSet.getString("CONSUMER_KEY");
                        try (PreparedStatement preparedStatement1 = connection.prepareStatement(GET_GRANT_TYPE)) {
                            preparedStatement1.setString(1, consumerKey);
                            try (ResultSet resultSet1 = preparedStatement1.executeQuery()) {
                                connection.commit();
                                while (resultSet1.next()) {
                                    grantType = resultSet1.getString("GRANT_TYPES");
                                    grantJson = "{\"parameters\":{\"grant_types\": \""+ grantType + "\"}}";
                                    try (PreparedStatement preparedStatement2 =
                                                 connection.prepareStatement(UPDATE_APP_INFO)) {
                                        InputStream in = new ByteArrayInputStream(grantJson.getBytes());
                                        preparedStatement2.
                                                setBinaryStream(1, in, grantJson.getBytes().length);
                                        preparedStatement2.setString(2, consumerKey);
                                        preparedStatement2.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIMigrationException("SQLException when executing: ".concat(GET_CONSUMER_KEYS), e);
        }
    }
}

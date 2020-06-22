package org.wso2.carbon.apimgt.migration.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.migration.APIMigrationException;
import org.wso2.carbon.apimgt.migration.dto.ResourceScopeMappingDTO;
import org.wso2.carbon.apimgt.migration.dto.UserRoleFromPermissionDTO;
import org.wso2.carbon.apimgt.migration.util.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class APIMgtDAO {
    private static final Log log = LogFactory.getLog(SharedDAO.class);
    private static APIMgtDAO INSTANCE = null;
    private static String GET_RESOURCE_SCOPE_SQL = "SELECT * FROM IDN_OAUTH2_RESOURCE_SCOPE WHERE TENANT_ID = ?";
    private static String INSERT_INTO_AM_API_RESOURCE_SCOPE_MAPPING =
            "INSERT INTO AM_API_RESOURCE_SCOPE_MAPPING VALUES " +
                    "((SELECT NAME FROM IDN_OAUTH2_SCOPE WHERE SCOPE_ID = ?), " +
                    "(SELECT URL_MAPPING_ID FROM AM_API_URL_MAPPING WHERE API_ID = ? AND HTTP_METHOD = ? " +
                    "AND URL_PATTERN = ?), ? );";

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
    public ResultSet getResourceScopeData(String tenantId) throws APIMigrationException {
        ResultSet resultSet = null;
        try {
            Connection conn = APIMgtDBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(GET_RESOURCE_SCOPE_SQL);
            ps.setString(1, tenantId);
            resultSet = ps.executeQuery();
        } catch (SQLException ex) {
            throw new APIMigrationException("Failed to get data from IDN_OAUTH2_RESOURCE_SCOPE", ex);
        }
        return resultSet;
    }

    /**
     * This method is used to get API Id using API context
     * @param context
     * @return
     * @throws APIMigrationException
     */
    public String getAPIID(String context) throws APIMigrationException {
        String apiId = null;
        try {
            Connection conn = APIMgtDBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(GET_API_ID);
            ps.setString(1, context);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                apiId = rs.getString("API_ID");
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
        try {
            Connection conn = APIMgtDBUtil.getConnection();
            PreparedStatement psAddResourceScope = conn.prepareStatement(INSERT_INTO_AM_API_RESOURCE_SCOPE_MAPPING);
            for (ResourceScopeMappingDTO resourceScopeMappingDTO : resourceScopeMappingDTOS) {
                psAddResourceScope.setString(1, resourceScopeMappingDTO.getScopeId());
                psAddResourceScope.setString(2, resourceScopeMappingDTO.getApiId());
                psAddResourceScope.setString(3, resourceScopeMappingDTO.getHttpMethod());
                psAddResourceScope.setString(4, resourceScopeMappingDTO.getUrlPattern());
                psAddResourceScope.setString(5, resourceScopeMappingDTO.getTenantID());
                psAddResourceScope.addBatch();
            }
            psAddResourceScope.executeBatch();
        } catch (SQLException ex) {
            throw new APIMigrationException("Failed to add dato AM_API_RESOURCE_SCOPE_MAPPING table : ", ex);
        }
    }
}

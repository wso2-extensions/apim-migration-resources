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

package org.wso2.carbon.apimgt.migration.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.migration.APIMigrationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ApimDBUtil {
    private static volatile DataSource dataSource = null;
    private static final String DATA_SOURCE_NAME = "jdbc/WSO2AM_DB";

    private static final Log log = LogFactory.getLog(ApimDBUtil.class);

    public static void initialize() throws APIMigrationException {
        try {
            Context ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(DATA_SOURCE_NAME);
        } catch (NamingException e) {
            throw new APIMigrationException("Error while looking up the data " +
                    "source: " + DATA_SOURCE_NAME, e);
        }
    }

    public static ArrayList<String> getAppsByTenantId(int tenantId) {

        ArrayList<String> appNames = new ArrayList<String>();
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT APP_NAME\n" +
                    "FROM IDN_OAUTH_CONSUMER_APPS OCA INNER JOIN AM_APPLICATION_KEY_MAPPING AKM ON" +
                    " OCA.CONSUMER_KEY=AKM.CONSUMER_KEY\n" +
                    "WHERE TENANT_ID = ?")){
                preparedStatement.setInt(1, tenantId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    connection.commit();
                    while (resultSet.next()) {
                        appNames.add(resultSet.getString("APP_NAME"));
                    }
                }
            }

        } catch (SQLException e) {
            log.error("SQLException when executing: " + "SELECT APP_NAME\\n\" +\n" +
                    "                    \"FROM IDN_OAUTH_CONSUMER_APPS OCA INNER JOIN AM_APPLICATION_KEY_MAPPING AKM ON\" +\n" +
                    "                    \" OCA.CONSUMER_KEY=AKM.CONSUMER_KEY\\n\" +\n" +
                    "                    \"WHERE TENANT_ID = ?", e);
        }
        return appNames;
    }

    public static ArrayList<String> getAppsOfTypeJWT(int tenantId) {

        ArrayList<String> consumerKeys = new ArrayList<String>();
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT IOP.CONSUMER_KEY\n" +
                    "FROM AM_APPLICATION AMA \n" +
                    "INNER JOIN AM_APPLICATION_KEY_MAPPING AKM ON AMA.APPLICATION_ID=AKM.APPLICATION_ID\n" +
                    "INNER JOIN IDN_OIDC_PROPERTY IOP ON AKM.CONSUMER_KEY=IOP.CONSUMER_KEY\n" +
                    "WHERE AMA.TOKEN_TYPE = 'JWT' AND PROPERTY_KEY  = 'tokenType' AND TENANT_ID = ?")){
                preparedStatement.setInt(1, tenantId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    connection.commit();
                    while (resultSet.next()) {
                        consumerKeys.add(resultSet.getString("CONSUMER_KEY"));
                    }
                }
            }

        } catch (SQLException e) {
            log.error("SQLException when executing: " + "SELECT APP_NAME\\n\" +\n" +
                    "                    \"FROM IDN_OAUTH_CONSUMER_APPS OCA INNER JOIN AM_APPLICATION_KEY_MAPPING AKM ON\" +\n" +
                    "                    \" OCA.CONSUMER_KEY=AKM.CONSUMER_KEY\\n\" +\n" +
                    "                    \"WHERE TENANT_ID = ?", e);
        }
        return consumerKeys;
    }

    public static void updateSPAppOwner(int tenantId, String username, String userDomain) {

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE" +
                    " IDN_OAUTH_CONSUMER_APPS SET USERNAME = ?, USER_DOMAIN = ? WHERE TENANT_ID = ? " +
                    "AND CONSUMER_KEY IN (SELECT CONSUMER_KEY FROM AM_APPLICATION_KEY_MAPPING);")){
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, userDomain);
                preparedStatement.setInt(3, tenantId);
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            log.error("SQLException when executing: " + "", e);
        }
    }

    public static void updateTokenTypeToJWT(String consumerKey) {

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE IDN_OIDC_PROPERTY SET" +
                    " PROPERTY_VALUE = ? WHERE PROPERTY_KEY  = 'tokenType' AND CONSUMER_KEY = ?;")){
                preparedStatement.setString(1, "JWT");
                preparedStatement.setString(2, consumerKey);
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            log.error("SQLException when executing: " + "", e);
        }
    }


}

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

package org.wso2.carbon.apimgt.migration.dto;

import java.util.Objects;

/**
 * This class is used to store the scope info returned by the database
 */
public class ScopeInfoDTO {
    private int scopeId;
    private String scopeName;
    private String scopeDisplayName;
    private String scopeType;
    private String scopeDescription;
    private int tenantID;

    public String getScopeName() {
        return scopeName;
    }

    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }

    public String getScopeDisplayName() {
        return scopeDisplayName;
    }

    public void setScopeDisplayName(String scopeDisplayName) {
        this.scopeDisplayName = scopeDisplayName;
    }

    public String getScopeType() {
        return scopeType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    public String getScopeDescription() {
        return scopeDescription;
    }

    public void setScopeDescription(String scopeDescription) {
        this.scopeDescription = scopeDescription;
    }

    public int getScopeId() {
        return scopeId;
    }

    public void setScopeId(int scopeId) {
        this.scopeId = scopeId;
    }

    public int getTenantID() {
        return tenantID;
    }

    public void setTenantID(int tenantID) {
        this.tenantID = tenantID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScopeInfoDTO that = (ScopeInfoDTO) o;
        return tenantID == that.tenantID &&
                Objects.equals(scopeName, that.scopeName) &&
                Objects.equals(scopeDisplayName, that.scopeDisplayName) &&
                Objects.equals(scopeType, that.scopeType) &&
                Objects.equals(scopeDescription, that.scopeDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scopeId, scopeName, scopeDisplayName, scopeType, scopeDescription, tenantID);
    }
}

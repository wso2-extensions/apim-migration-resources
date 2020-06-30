package org.wso2.carbon.apimgt.migration.dto;

public class APIScopeMappingDTO {
    private int apiId;
    private int scopeId;

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public int getScopeId() {
        return scopeId;
    }

    public void setScopeId(int scopeId) {
        this.scopeId = scopeId;
    }
}

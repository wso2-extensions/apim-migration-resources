package org.wso2.carbon.apimgt.migration.dto;

import org.wso2.carbon.apimgt.api.model.VHost;

import java.util.ArrayList;
import java.util.List;

public class GatewayEnvironmentDTO {
    private String uuid;
    private String name;
    private String tenantDomain;
    private String displayName;
    private String description;
    private List<VHost> vhosts = new ArrayList<>();

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<VHost> getVhosts() {
        return vhosts;
    }

    public void setVhosts(List<VHost> vhosts) {
        this.vhosts = vhosts;
    }
}

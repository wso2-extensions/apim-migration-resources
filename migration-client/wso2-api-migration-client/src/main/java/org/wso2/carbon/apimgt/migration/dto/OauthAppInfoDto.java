package org.wso2.carbon.apimgt.migration.dto;

public class OauthAppInfoDto {

    private String oauthAppName;
    private String consumerKey;
    private String owner;
    private int tenantId;
    private String userDomain;

    public String getOauthAppName() {

        return oauthAppName;
    }

    public void setOauthAppName(String oauthAppName) {

        this.oauthAppName = oauthAppName;
    }

    public String getConsumerKey() {

        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {

        this.consumerKey = consumerKey;
    }

    public String getOwner() {

        return owner;
    }

    public void setOwner(String owner) {

        this.owner = owner;
    }

    public int getTenantId() {

        return tenantId;
    }

    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
    }

    public String getUserDomain() {

        return userDomain;
    }

    public void setUserDomain(String userDomain) {

        this.userDomain = userDomain;
    }
}

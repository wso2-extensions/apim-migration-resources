package org.wso2.carbon.apimgt.migration.dto;

public class APIURLMappingInfoDTO {
    private int urlMappingId;
    private int apiId;
    private String httpMethod;
    private String urlPattern;

    public int getUrlMappingId() {
        return urlMappingId;
    }

    public void setUrlMappingId(int urlMappingId) {
        this.urlMappingId = urlMappingId;
    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }
}

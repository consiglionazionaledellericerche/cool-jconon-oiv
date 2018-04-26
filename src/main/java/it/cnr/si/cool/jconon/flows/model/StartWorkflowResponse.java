package it.cnr.si.cool.jconon.flows.model;

import java.util.List;

public class StartWorkflowResponse {
    private String id;
    private String url;
    private String businessKey;
    private Boolean suspended;
    private Boolean ended;
    private String processDefinitionId;
    private String processDefinitionUrl;
    private String processDefinitionKey;
    private String activityId;
    private List variables;
    private String tenantId;
    private String name;
    private Boolean completed;

    public StartWorkflowResponse() {
    }

    @Override
    public String toString() {
        return "StartWorkflowResponse{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", businessKey='" + businessKey + '\'' +
                ", suspended=" + suspended +
                ", ended=" + ended +
                ", processDefinitionId='" + processDefinitionId + '\'' +
                ", processDefinitionUrl='" + processDefinitionUrl + '\'' +
                ", processDefinitionKey='" + processDefinitionKey + '\'' +
                ", activityId='" + activityId + '\'' +
                ", variables=" + variables +
                ", tenantId='" + tenantId + '\'' +
                ", name='" + name + '\'' +
                ", completed=" + completed +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public Boolean getSuspended() {
        return suspended;
    }

    public void setSuspended(Boolean suspended) {
        this.suspended = suspended;
    }

    public Boolean getEnded() {
        return ended;
    }

    public void setEnded(Boolean ended) {
        this.ended = ended;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessDefinitionUrl() {
        return processDefinitionUrl;
    }

    public void setProcessDefinitionUrl(String processDefinitionUrl) {
        this.processDefinitionUrl = processDefinitionUrl;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public List getVariables() {
        return variables;
    }

    public void setVariables(List variables) {
        this.variables = variables;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
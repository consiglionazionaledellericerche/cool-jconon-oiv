package it.cnr.si.cool.jconon.flows.model;

import java.util.Date;

public class ProcessInstanceResponse {
    private String id;
    private Date startTime;
    private Date endTime;

    public ProcessInstanceResponse() {
    }

    public String getId() {
        return id;
    }

    public ProcessInstanceResponse setId(String id) {
        this.id = id;
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    public ProcessInstanceResponse setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public Date getEndTime() {
        return endTime;
    }

    public ProcessInstanceResponse setEndTime(Date endTime) {
        this.endTime = endTime;
        return this;
    }

    @Override
    public String toString() {
        return "ProcessInstanceResponse{" +
                "id='" + id + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}

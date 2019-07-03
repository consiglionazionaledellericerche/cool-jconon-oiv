/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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

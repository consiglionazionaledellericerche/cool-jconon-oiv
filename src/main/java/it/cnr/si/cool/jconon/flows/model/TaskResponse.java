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

public class TaskResponse {
    public static String PREAVVISO_RIGETTO = "PREAVVISO RIGETTO", SOCCORSO_ISTRUTTORIO = "SOCCORSO ISTRUTTORIO";

    public String id;
    public String name;

    public TaskResponse() {
    }

    public String getId() {
        return id;
    }

    public TaskResponse setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public TaskResponse setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        return "TaskResponse{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

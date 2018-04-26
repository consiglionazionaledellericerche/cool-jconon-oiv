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

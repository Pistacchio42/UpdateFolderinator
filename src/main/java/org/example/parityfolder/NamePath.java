package org.example.parityfolder;

public class NamePath implements java.io.Serializable{
    private String name;
    private String parth;

    public NamePath(String name, String parth) {
        this.name = name;
        this.parth = parth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParth() {
        return parth;
    }

    public void setParth(String parth) {
        this.parth = parth;
    }
}

package com.example.canonico.myapplication;

/**
 * Created by Canonico on 09/01/2016.
 */
public class Contact {
    private long id;
    private String num;
    private String name;



    public String toString() {
        StringBuilder sb=new StringBuilder();
        sb.append(name).append(" (").append(num).append(")");

        return name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

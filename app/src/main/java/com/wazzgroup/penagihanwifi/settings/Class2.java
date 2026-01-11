package com.wazzgroup.penagihanwifi.settings;

public class Class2 {
    private int id;
    private String perangkat;
    private String status;
    public Class2(int id, String perangkat,String status) {
        this.id = id;
        this.perangkat = perangkat;
        this.status = status;
    }
    public int getId() { return id; }
    public String getperangkat() { return perangkat; }
    public String getstatus() { return status; }
}

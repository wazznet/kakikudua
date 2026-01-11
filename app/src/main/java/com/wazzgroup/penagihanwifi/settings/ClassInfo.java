package com.wazzgroup.penagihanwifi.settings;

public class ClassInfo {
    private int id;
    private String versi;
    private String list_update;
    private String tanggal;
    public ClassInfo(int id, String versi, String list_update, String tanggal) {
        this.id = id;
        this.versi = versi;
        this.list_update = list_update;
        this.tanggal = tanggal;
    }
    public int getId() { return id; }
    public String getversi() { return versi; }
    public String getlist_update() { return list_update; }
    public String gettanggal() { return tanggal; }
}

package com.wazzgroup.penagihanwifi.settings;

public class Class {
    private int id;
    private String nama;
    private int jumlah;
    private int harga;

    public Class(int id, String nama, int jumlah,int harga) {
        this.id = id;
        this.nama = nama;
        this.jumlah = jumlah;
        this.harga = harga;
    }
    public int getId() { return id; }
    public String getNama() { return nama; }
    public int getJumlah() { return jumlah; }
    public int getharga() { return harga; }
}

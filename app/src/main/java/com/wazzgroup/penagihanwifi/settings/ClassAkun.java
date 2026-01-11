package com.wazzgroup.penagihanwifi.settings;

public class ClassAkun {
    private int id;
    private String nama;
    private String akses;
    private int area;
    private String nama_area;

    private String nohp;
    private String username;
    public ClassAkun(int id, String nama, String akses, String username, String nohp, int area, String nama_area) {
        this.id = id;
        this.nama = nama;
        this.akses = akses;

        this.username = username;
        this.nohp = nohp;
        this.area = area;
        this.nama_area = nama_area;
    }
    public int getId() { return id; }
    public String getnama() { return nama; }
    public String getakses() { return akses; }
    public int getarea() { return area; }
    public String getnama_area() { return nama_area; }
    public String getnohp() { return nohp; }
    public String getusername() { return username; }
}

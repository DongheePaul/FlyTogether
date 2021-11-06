package com.dong.Vod_List;

public class v_list_item {

    private String vod_id;
    private String bj_id;
    private String st_Thumbnail;
    private String vod_name;

    public v_list_item(String vod_id, String bj_id, String st_Thumbnail, String vod_name) {
        this.vod_id = vod_id;
        this.bj_id = bj_id;
        this.st_Thumbnail = st_Thumbnail;
        this.vod_name = vod_name;
    }

    public String getVod_id() {
        return vod_id;
    }

    public void setVod_id(String vod_id) {
        this.vod_id = vod_id;
    }

    public String getBj_id() {
        return bj_id;
    }

    public void setBj_id(String bj_id) {
        this.bj_id = bj_id;
    }

    public String getSt_Thumbnail() {
        return st_Thumbnail;
    }

    public void setSt_Thumbnail(String st_Thumbnail) {
        this.st_Thumbnail = st_Thumbnail;
    }

    public String getVod_name() {
        return vod_name;
    }

    public void setVod_name(String vod_name) {
        this.vod_name = vod_name;
    }
}

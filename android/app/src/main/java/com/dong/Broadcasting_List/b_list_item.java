package com.dong.Broadcasting_List;

public class b_list_item {
    private String room_id;
    private String room_name;
    private String bj_name;

    public b_list_item(String room_id, String room_name, String bj_name) {
        this.room_id = room_id;
        this.room_name = room_name;
        this.bj_name = bj_name;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public String getBj_name() {
        return bj_name;
    }

    public void setBj_name(String bj_name) {
        this.bj_name = bj_name;
    }

}

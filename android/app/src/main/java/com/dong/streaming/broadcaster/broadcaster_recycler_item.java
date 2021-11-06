package com.dong.streaming.broadcaster;

public class broadcaster_recycler_item {
private String id;
    private String message;


    public broadcaster_recycler_item(String id, String message) {
        this.id = id;
        this.message = message;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

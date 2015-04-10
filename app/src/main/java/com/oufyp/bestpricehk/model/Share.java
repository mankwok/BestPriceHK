package com.oufyp.bestpricehk.model;

import java.io.Serializable;

public class Share implements Serializable {
    private String pid;
    private String productName;
    private String username;
    private String details;
    private String msg;
    private String timestamp;

    public Share(String pid, String productName, String username, String details, String msg, String timestamp) {
        this.pid = pid;
        this.productName = productName;
        this.username = username;
        this.details = details;
        this.msg = msg;
        this.timestamp = timestamp;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUsername() {
        return username;
    }

    public void setUid(String uid) {
        this.username = uid;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

package com.oufyp.bestpricehk.model;

import com.google.android.gms.maps.model.LatLng;

public class Store {
    private String id;
    private String name;
    private LatLng latLong;
    private String address;
    private int type;
    private String phone;

    public Store(String id, String name, LatLng latLong, String address, int type) {
        this.id = id;
        this.name = name;
        this.latLong = latLong;
        this.address = address;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getLatLong() {
        return latLong;
    }

    public void setLatLong(LatLng latLong) {
        this.latLong = latLong;
    }
}

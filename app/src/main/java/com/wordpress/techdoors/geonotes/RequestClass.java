package com.wordpress.techdoors.geonotes;

/**
 * Created by amit on 22/03/16.
 */
public class RequestClass {

    String name;
    String lat;
    String lon;

    public String getName() {
        return name;
    }

    public void setName(String firstName) {
        this.name = firstName;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLat(String lastName) {
        this.lat = lastName;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public RequestClass(String name, String lat, String lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    public RequestClass() {
    }
}

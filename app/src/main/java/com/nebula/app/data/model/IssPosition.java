package com.nebula.app.data.model;

import com.google.gson.annotations.SerializedName;

public class IssPosition {
    @SerializedName("latitude") private double latitude;
    @SerializedName("longitude") private double longitude;
    @SerializedName("altitude") private double altitude;
    @SerializedName("velocity") private double velocity;
    @SerializedName("timestamp") private long timestamp;

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getAltitude() { return altitude; }
    public double getVelocity() { return velocity; }
    public long getTimestamp() { return timestamp; }
}

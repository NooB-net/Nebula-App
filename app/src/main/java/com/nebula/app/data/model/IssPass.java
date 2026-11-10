package com.nebula.app.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

@Entity(tableName = "iss_passes")
public class IssPass implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @SerializedName("risetime") private long riseTime;
    @SerializedName("duration") private int duration;
    private double latitude;
    private double longitude;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public long getRiseTime() { return riseTime; }
    public void setRiseTime(long riseTime) { this.riseTime = riseTime; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}

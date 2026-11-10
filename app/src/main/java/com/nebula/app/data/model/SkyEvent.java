package com.nebula.app.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "sky_events")
public class SkyEvent implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private String type; // METEOR, ECLIPSE, MOON, PLANET, CONJUNCTION, ISS
    private String date;
    private String description;
    private long timestamp;
    private double azimuth;
    private double altitude;
    private boolean saved;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public double getAzimuth() { return azimuth; }
    public void setAzimuth(double azimuth) { this.azimuth = azimuth; }
    public double getAltitude() { return altitude; }
    public void setAltitude(double altitude) { this.altitude = altitude; }
    public boolean isSaved() { return saved; }
    public void setSaved(boolean saved) { this.saved = saved; }
}

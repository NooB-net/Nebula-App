package com.nebula.app.data.model;

import java.io.Serializable;

/** A space photograph plus its description (from NASA's Image & Video Library, or a bundled fallback). */
public class SpacePhoto implements Serializable {
    private String nasaId;
    private String title;
    private String description;
    private String imageUrl;   // ~medium rendition
    private String thumbUrl;   // ~thumb rendition (fallback)
    private String date;       // display date
    private String credit;     // e.g. "NASA/JPL"
    private int localRes;      // bundled drawable id for offline fallback, 0 if remote
    private String hook;       // optional short call-to-action used by notifications

    public String getNasaId() { return nasaId; }
    public void setNasaId(String nasaId) { this.nasaId = nasaId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getThumbUrl() { return thumbUrl; }
    public void setThumbUrl(String thumbUrl) { this.thumbUrl = thumbUrl; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getCredit() { return credit; }
    public void setCredit(String credit) { this.credit = credit; }
    public int getLocalRes() { return localRes; }
    public void setLocalRes(int localRes) { this.localRes = localRes; }
    public String getHook() { return hook; }
    public void setHook(String hook) { this.hook = hook; }
}

package com.nebula.app.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** One reminder for one sky event. An event can have several reminders. */
@Entity(tableName = "reminders")
public class Reminder {
    public static final int REPEAT_NONE = 0;
    public static final int REPEAT_DAILY = 1;
    public static final int REPEAT_WEEKLY = 2;

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String ownerId;      // which account (or guest) this reminder belongs to
    private long eventId;
    private String eventName;
    private long eventTime;     // when the sky event itself happens
    private long triggerTime;   // when this reminder fires next
    private String note;        // optional user note
    private int repeat;         // REPEAT_* (repeats until the event starts)
    private boolean active;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public long getEventId() { return eventId; }
    public void setEventId(long eventId) { this.eventId = eventId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public long getEventTime() { return eventTime; }
    public void setEventTime(long eventTime) { this.eventTime = eventTime; }
    public long getTriggerTime() { return triggerTime; }
    public void setTriggerTime(long triggerTime) { this.triggerTime = triggerTime; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public int getRepeat() { return repeat; }
    public void setRepeat(int repeat) { this.repeat = repeat; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

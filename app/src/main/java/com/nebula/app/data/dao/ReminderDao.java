package com.nebula.app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.nebula.app.data.model.Reminder;
import java.util.List;

@Dao
public interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Reminder reminder);

    @Query("SELECT * FROM reminders WHERE active = 1 AND ownerId = :owner ORDER BY triggerTime ASC")
    List<Reminder> getActiveForOwner(String owner);

    @Query("SELECT * FROM reminders WHERE eventId = :eventId AND active = 1 AND ownerId = :owner ORDER BY triggerTime ASC")
    List<Reminder> getActiveForEvent(long eventId, String owner);

    @Query("DELETE FROM reminders WHERE ownerId = :owner")
    void deleteForOwner(String owner);

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    Reminder get(int id);

    @Query("SELECT * FROM reminders ORDER BY triggerTime ASC")
    List<Reminder> getAll();

    @Query("UPDATE reminders SET active = 0 WHERE id = :id")
    void deactivate(int id);

    @Query("DELETE FROM reminders WHERE id = :id")
    void delete(int id);

    @Query("DELETE FROM reminders")
    void deleteAll();
}

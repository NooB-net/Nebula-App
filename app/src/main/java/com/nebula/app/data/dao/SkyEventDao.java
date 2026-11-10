package com.nebula.app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.nebula.app.data.model.SkyEvent;
import java.util.List;

@Dao
public interface SkyEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SkyEvent> events);

    @Query("SELECT * FROM sky_events ORDER BY timestamp ASC")
    List<SkyEvent> getAll();

    @Query("SELECT * FROM sky_events WHERE type = :type ORDER BY timestamp ASC")
    List<SkyEvent> getByType(String type);

    @Query("SELECT * FROM sky_events WHERE saved = 1")
    List<SkyEvent> getSaved();

    @Query("DELETE FROM sky_events")
    void deleteAll();
}

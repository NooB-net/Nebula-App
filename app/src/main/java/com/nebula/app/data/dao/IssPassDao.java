package com.nebula.app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.nebula.app.data.model.IssPass;
import java.util.List;

@Dao
public interface IssPassDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<IssPass> passes);

    @Query("SELECT * FROM iss_passes ORDER BY riseTime ASC")
    List<IssPass> getAll();

    @Query("DELETE FROM iss_passes")
    void deleteAll();
}

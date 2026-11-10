package com.nebula.app.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.nebula.app.data.dao.SkyEventDao;
import com.nebula.app.data.dao.ReminderDao;
import com.nebula.app.data.model.SkyEvent;
import com.nebula.app.data.model.Reminder;
import com.nebula.app.data.model.IssPass;
import com.nebula.app.data.dao.IssPassDao;

// v3: reminders are per account (ownerId). v2: reminders gained eventId/eventTime/note/repeat; APOD + local crew tables removed
// (photos now come from the NASA image library, socials from the cloud).
@Database(
    entities = {SkyEvent.class, Reminder.class, IssPass.class},
    version = 3,
    exportSchema = false
)
public abstract class NebulaDatabase extends RoomDatabase {

    private static volatile NebulaDatabase INSTANCE;

    public abstract SkyEventDao skyEventDao();
    public abstract ReminderDao reminderDao();
    public abstract IssPassDao issPassDao();

    public static NebulaDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (NebulaDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            NebulaDatabase.class,
                            "nebula_database"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}

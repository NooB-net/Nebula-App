package com.nebula.app.data.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenHelper;
import androidx.room.RoomOpenHelper.Delegate;
import androidx.room.RoomOpenHelper.ValidationResult;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.room.util.TableInfo.Column;
import androidx.room.util.TableInfo.ForeignKey;
import androidx.room.util.TableInfo.Index;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration;
import com.nebula.app.data.dao.IssPassDao;
import com.nebula.app.data.dao.IssPassDao_Impl;
import com.nebula.app.data.dao.ReminderDao;
import com.nebula.app.data.dao.ReminderDao_Impl;
import com.nebula.app.data.dao.SkyEventDao;
import com.nebula.app.data.dao.SkyEventDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation"})
public final class NebulaDatabase_Impl extends NebulaDatabase {
  private volatile SkyEventDao _skyEventDao;

  private volatile ReminderDao _reminderDao;

  private volatile IssPassDao _issPassDao;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `sky_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `type` TEXT, `date` TEXT, `description` TEXT, `timestamp` INTEGER NOT NULL, `azimuth` REAL NOT NULL, `altitude` REAL NOT NULL, `saved` INTEGER NOT NULL)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `reminders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ownerId` TEXT, `eventId` INTEGER NOT NULL, `eventName` TEXT, `eventTime` INTEGER NOT NULL, `triggerTime` INTEGER NOT NULL, `note` TEXT, `repeat` INTEGER NOT NULL, `active` INTEGER NOT NULL)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `iss_passes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `riseTime` INTEGER NOT NULL, `duration` INTEGER NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '73c11a38363bf3dcb8d601a65425de59')");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `sky_events`");
        _db.execSQL("DROP TABLE IF EXISTS `reminders`");
        _db.execSQL("DROP TABLE IF EXISTS `iss_passes`");
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onDestructiveMigration(_db);
          }
        }
      }

      @Override
      public void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      @Override
      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      @Override
      public void onPreMigrate(SupportSQLiteDatabase _db) {
        DBUtil.dropFtsSyncTriggers(_db);
      }

      @Override
      public void onPostMigrate(SupportSQLiteDatabase _db) {
      }

      @Override
      public RoomOpenHelper.ValidationResult onValidateSchema(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsSkyEvents = new HashMap<String, TableInfo.Column>(9);
        _columnsSkyEvents.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSkyEvents.put("name", new TableInfo.Column("name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSkyEvents.put("type", new TableInfo.Column("type", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSkyEvents.put("date", new TableInfo.Column("date", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSkyEvents.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSkyEvents.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSkyEvents.put("azimuth", new TableInfo.Column("azimuth", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSkyEvents.put("altitude", new TableInfo.Column("altitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSkyEvents.put("saved", new TableInfo.Column("saved", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSkyEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSkyEvents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSkyEvents = new TableInfo("sky_events", _columnsSkyEvents, _foreignKeysSkyEvents, _indicesSkyEvents);
        final TableInfo _existingSkyEvents = TableInfo.read(_db, "sky_events");
        if (! _infoSkyEvents.equals(_existingSkyEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "sky_events(com.nebula.app.data.model.SkyEvent).\n"
                  + " Expected:\n" + _infoSkyEvents + "\n"
                  + " Found:\n" + _existingSkyEvents);
        }
        final HashMap<String, TableInfo.Column> _columnsReminders = new HashMap<String, TableInfo.Column>(9);
        _columnsReminders.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("ownerId", new TableInfo.Column("ownerId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("eventId", new TableInfo.Column("eventId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("eventName", new TableInfo.Column("eventName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("eventTime", new TableInfo.Column("eventTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("triggerTime", new TableInfo.Column("triggerTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("note", new TableInfo.Column("note", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("repeat", new TableInfo.Column("repeat", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("active", new TableInfo.Column("active", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReminders = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesReminders = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoReminders = new TableInfo("reminders", _columnsReminders, _foreignKeysReminders, _indicesReminders);
        final TableInfo _existingReminders = TableInfo.read(_db, "reminders");
        if (! _infoReminders.equals(_existingReminders)) {
          return new RoomOpenHelper.ValidationResult(false, "reminders(com.nebula.app.data.model.Reminder).\n"
                  + " Expected:\n" + _infoReminders + "\n"
                  + " Found:\n" + _existingReminders);
        }
        final HashMap<String, TableInfo.Column> _columnsIssPasses = new HashMap<String, TableInfo.Column>(5);
        _columnsIssPasses.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIssPasses.put("riseTime", new TableInfo.Column("riseTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIssPasses.put("duration", new TableInfo.Column("duration", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIssPasses.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIssPasses.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysIssPasses = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesIssPasses = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoIssPasses = new TableInfo("iss_passes", _columnsIssPasses, _foreignKeysIssPasses, _indicesIssPasses);
        final TableInfo _existingIssPasses = TableInfo.read(_db, "iss_passes");
        if (! _infoIssPasses.equals(_existingIssPasses)) {
          return new RoomOpenHelper.ValidationResult(false, "iss_passes(com.nebula.app.data.model.IssPass).\n"
                  + " Expected:\n" + _infoIssPasses + "\n"
                  + " Found:\n" + _existingIssPasses);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "73c11a38363bf3dcb8d601a65425de59", "3d371a28256aa90f5dcdef5a51209a67");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "sky_events","reminders","iss_passes");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `sky_events`");
      _db.execSQL("DELETE FROM `reminders`");
      _db.execSQL("DELETE FROM `iss_passes`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(SkyEventDao.class, SkyEventDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReminderDao.class, ReminderDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(IssPassDao.class, IssPassDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  public List<Migration> getAutoMigrations(
      @NonNull Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecsMap) {
    return Arrays.asList();
  }

  @Override
  public SkyEventDao skyEventDao() {
    if (_skyEventDao != null) {
      return _skyEventDao;
    } else {
      synchronized(this) {
        if(_skyEventDao == null) {
          _skyEventDao = new SkyEventDao_Impl(this);
        }
        return _skyEventDao;
      }
    }
  }

  @Override
  public ReminderDao reminderDao() {
    if (_reminderDao != null) {
      return _reminderDao;
    } else {
      synchronized(this) {
        if(_reminderDao == null) {
          _reminderDao = new ReminderDao_Impl(this);
        }
        return _reminderDao;
      }
    }
  }

  @Override
  public IssPassDao issPassDao() {
    if (_issPassDao != null) {
      return _issPassDao;
    } else {
      synchronized(this) {
        if(_issPassDao == null) {
          _issPassDao = new IssPassDao_Impl(this);
        }
        return _issPassDao;
      }
    }
  }
}

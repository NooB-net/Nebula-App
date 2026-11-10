package com.nebula.app.data.dao;

import android.database.Cursor;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.nebula.app.data.model.Reminder;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class ReminderDao_Impl implements ReminderDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Reminder> __insertionAdapterOfReminder;

  private final SharedSQLiteStatement __preparedStmtOfDeleteForOwner;

  private final SharedSQLiteStatement __preparedStmtOfDeactivate;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public ReminderDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReminder = new EntityInsertionAdapter<Reminder>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `reminders` (`id`,`ownerId`,`eventId`,`eventName`,`eventTime`,`triggerTime`,`note`,`repeat`,`active`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Reminder value) {
        stmt.bindLong(1, value.getId());
        if (value.getOwnerId() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getOwnerId());
        }
        stmt.bindLong(3, value.getEventId());
        if (value.getEventName() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getEventName());
        }
        stmt.bindLong(5, value.getEventTime());
        stmt.bindLong(6, value.getTriggerTime());
        if (value.getNote() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.getNote());
        }
        stmt.bindLong(8, value.getRepeat());
        final int _tmp = value.isActive() ? 1 : 0;
        stmt.bindLong(9, _tmp);
      }
    };
    this.__preparedStmtOfDeleteForOwner = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM reminders WHERE ownerId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeactivate = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "UPDATE reminders SET active = 0 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM reminders WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM reminders";
        return _query;
      }
    };
  }

  @Override
  public long insert(final Reminder reminder) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      long _result = __insertionAdapterOfReminder.insertAndReturnId(reminder);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteForOwner(final String owner) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteForOwner.acquire();
    int _argIndex = 1;
    if (owner == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, owner);
    }
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfDeleteForOwner.release(_stmt);
    }
  }

  @Override
  public void deactivate(final int id) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeactivate.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, id);
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfDeactivate.release(_stmt);
    }
  }

  @Override
  public void delete(final int id) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, id);
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfDelete.release(_stmt);
    }
  }

  @Override
  public void deleteAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public List<Reminder> getActiveForOwner(final String owner) {
    final String _sql = "SELECT * FROM reminders WHERE active = 1 AND ownerId = ? ORDER BY triggerTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (owner == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, owner);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
      final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
      final int _cursorIndexOfEventName = CursorUtil.getColumnIndexOrThrow(_cursor, "eventName");
      final int _cursorIndexOfEventTime = CursorUtil.getColumnIndexOrThrow(_cursor, "eventTime");
      final int _cursorIndexOfTriggerTime = CursorUtil.getColumnIndexOrThrow(_cursor, "triggerTime");
      final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
      final int _cursorIndexOfRepeat = CursorUtil.getColumnIndexOrThrow(_cursor, "repeat");
      final int _cursorIndexOfActive = CursorUtil.getColumnIndexOrThrow(_cursor, "active");
      final List<Reminder> _result = new ArrayList<Reminder>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final Reminder _item;
        _item = new Reminder();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final String _tmpOwnerId;
        if (_cursor.isNull(_cursorIndexOfOwnerId)) {
          _tmpOwnerId = null;
        } else {
          _tmpOwnerId = _cursor.getString(_cursorIndexOfOwnerId);
        }
        _item.setOwnerId(_tmpOwnerId);
        final long _tmpEventId;
        _tmpEventId = _cursor.getLong(_cursorIndexOfEventId);
        _item.setEventId(_tmpEventId);
        final String _tmpEventName;
        if (_cursor.isNull(_cursorIndexOfEventName)) {
          _tmpEventName = null;
        } else {
          _tmpEventName = _cursor.getString(_cursorIndexOfEventName);
        }
        _item.setEventName(_tmpEventName);
        final long _tmpEventTime;
        _tmpEventTime = _cursor.getLong(_cursorIndexOfEventTime);
        _item.setEventTime(_tmpEventTime);
        final long _tmpTriggerTime;
        _tmpTriggerTime = _cursor.getLong(_cursorIndexOfTriggerTime);
        _item.setTriggerTime(_tmpTriggerTime);
        final String _tmpNote;
        if (_cursor.isNull(_cursorIndexOfNote)) {
          _tmpNote = null;
        } else {
          _tmpNote = _cursor.getString(_cursorIndexOfNote);
        }
        _item.setNote(_tmpNote);
        final int _tmpRepeat;
        _tmpRepeat = _cursor.getInt(_cursorIndexOfRepeat);
        _item.setRepeat(_tmpRepeat);
        final boolean _tmpActive;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfActive);
        _tmpActive = _tmp != 0;
        _item.setActive(_tmpActive);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<Reminder> getActiveForEvent(final long eventId, final String owner) {
    final String _sql = "SELECT * FROM reminders WHERE eventId = ? AND active = 1 AND ownerId = ? ORDER BY triggerTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, eventId);
    _argIndex = 2;
    if (owner == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, owner);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
      final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
      final int _cursorIndexOfEventName = CursorUtil.getColumnIndexOrThrow(_cursor, "eventName");
      final int _cursorIndexOfEventTime = CursorUtil.getColumnIndexOrThrow(_cursor, "eventTime");
      final int _cursorIndexOfTriggerTime = CursorUtil.getColumnIndexOrThrow(_cursor, "triggerTime");
      final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
      final int _cursorIndexOfRepeat = CursorUtil.getColumnIndexOrThrow(_cursor, "repeat");
      final int _cursorIndexOfActive = CursorUtil.getColumnIndexOrThrow(_cursor, "active");
      final List<Reminder> _result = new ArrayList<Reminder>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final Reminder _item;
        _item = new Reminder();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final String _tmpOwnerId;
        if (_cursor.isNull(_cursorIndexOfOwnerId)) {
          _tmpOwnerId = null;
        } else {
          _tmpOwnerId = _cursor.getString(_cursorIndexOfOwnerId);
        }
        _item.setOwnerId(_tmpOwnerId);
        final long _tmpEventId;
        _tmpEventId = _cursor.getLong(_cursorIndexOfEventId);
        _item.setEventId(_tmpEventId);
        final String _tmpEventName;
        if (_cursor.isNull(_cursorIndexOfEventName)) {
          _tmpEventName = null;
        } else {
          _tmpEventName = _cursor.getString(_cursorIndexOfEventName);
        }
        _item.setEventName(_tmpEventName);
        final long _tmpEventTime;
        _tmpEventTime = _cursor.getLong(_cursorIndexOfEventTime);
        _item.setEventTime(_tmpEventTime);
        final long _tmpTriggerTime;
        _tmpTriggerTime = _cursor.getLong(_cursorIndexOfTriggerTime);
        _item.setTriggerTime(_tmpTriggerTime);
        final String _tmpNote;
        if (_cursor.isNull(_cursorIndexOfNote)) {
          _tmpNote = null;
        } else {
          _tmpNote = _cursor.getString(_cursorIndexOfNote);
        }
        _item.setNote(_tmpNote);
        final int _tmpRepeat;
        _tmpRepeat = _cursor.getInt(_cursorIndexOfRepeat);
        _item.setRepeat(_tmpRepeat);
        final boolean _tmpActive;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfActive);
        _tmpActive = _tmp != 0;
        _item.setActive(_tmpActive);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Reminder get(final int id) {
    final String _sql = "SELECT * FROM reminders WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
      final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
      final int _cursorIndexOfEventName = CursorUtil.getColumnIndexOrThrow(_cursor, "eventName");
      final int _cursorIndexOfEventTime = CursorUtil.getColumnIndexOrThrow(_cursor, "eventTime");
      final int _cursorIndexOfTriggerTime = CursorUtil.getColumnIndexOrThrow(_cursor, "triggerTime");
      final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
      final int _cursorIndexOfRepeat = CursorUtil.getColumnIndexOrThrow(_cursor, "repeat");
      final int _cursorIndexOfActive = CursorUtil.getColumnIndexOrThrow(_cursor, "active");
      final Reminder _result;
      if(_cursor.moveToFirst()) {
        _result = new Reminder();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _result.setId(_tmpId);
        final String _tmpOwnerId;
        if (_cursor.isNull(_cursorIndexOfOwnerId)) {
          _tmpOwnerId = null;
        } else {
          _tmpOwnerId = _cursor.getString(_cursorIndexOfOwnerId);
        }
        _result.setOwnerId(_tmpOwnerId);
        final long _tmpEventId;
        _tmpEventId = _cursor.getLong(_cursorIndexOfEventId);
        _result.setEventId(_tmpEventId);
        final String _tmpEventName;
        if (_cursor.isNull(_cursorIndexOfEventName)) {
          _tmpEventName = null;
        } else {
          _tmpEventName = _cursor.getString(_cursorIndexOfEventName);
        }
        _result.setEventName(_tmpEventName);
        final long _tmpEventTime;
        _tmpEventTime = _cursor.getLong(_cursorIndexOfEventTime);
        _result.setEventTime(_tmpEventTime);
        final long _tmpTriggerTime;
        _tmpTriggerTime = _cursor.getLong(_cursorIndexOfTriggerTime);
        _result.setTriggerTime(_tmpTriggerTime);
        final String _tmpNote;
        if (_cursor.isNull(_cursorIndexOfNote)) {
          _tmpNote = null;
        } else {
          _tmpNote = _cursor.getString(_cursorIndexOfNote);
        }
        _result.setNote(_tmpNote);
        final int _tmpRepeat;
        _tmpRepeat = _cursor.getInt(_cursorIndexOfRepeat);
        _result.setRepeat(_tmpRepeat);
        final boolean _tmpActive;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfActive);
        _tmpActive = _tmp != 0;
        _result.setActive(_tmpActive);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<Reminder> getAll() {
    final String _sql = "SELECT * FROM reminders ORDER BY triggerTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
      final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
      final int _cursorIndexOfEventName = CursorUtil.getColumnIndexOrThrow(_cursor, "eventName");
      final int _cursorIndexOfEventTime = CursorUtil.getColumnIndexOrThrow(_cursor, "eventTime");
      final int _cursorIndexOfTriggerTime = CursorUtil.getColumnIndexOrThrow(_cursor, "triggerTime");
      final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
      final int _cursorIndexOfRepeat = CursorUtil.getColumnIndexOrThrow(_cursor, "repeat");
      final int _cursorIndexOfActive = CursorUtil.getColumnIndexOrThrow(_cursor, "active");
      final List<Reminder> _result = new ArrayList<Reminder>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final Reminder _item;
        _item = new Reminder();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final String _tmpOwnerId;
        if (_cursor.isNull(_cursorIndexOfOwnerId)) {
          _tmpOwnerId = null;
        } else {
          _tmpOwnerId = _cursor.getString(_cursorIndexOfOwnerId);
        }
        _item.setOwnerId(_tmpOwnerId);
        final long _tmpEventId;
        _tmpEventId = _cursor.getLong(_cursorIndexOfEventId);
        _item.setEventId(_tmpEventId);
        final String _tmpEventName;
        if (_cursor.isNull(_cursorIndexOfEventName)) {
          _tmpEventName = null;
        } else {
          _tmpEventName = _cursor.getString(_cursorIndexOfEventName);
        }
        _item.setEventName(_tmpEventName);
        final long _tmpEventTime;
        _tmpEventTime = _cursor.getLong(_cursorIndexOfEventTime);
        _item.setEventTime(_tmpEventTime);
        final long _tmpTriggerTime;
        _tmpTriggerTime = _cursor.getLong(_cursorIndexOfTriggerTime);
        _item.setTriggerTime(_tmpTriggerTime);
        final String _tmpNote;
        if (_cursor.isNull(_cursorIndexOfNote)) {
          _tmpNote = null;
        } else {
          _tmpNote = _cursor.getString(_cursorIndexOfNote);
        }
        _item.setNote(_tmpNote);
        final int _tmpRepeat;
        _tmpRepeat = _cursor.getInt(_cursorIndexOfRepeat);
        _item.setRepeat(_tmpRepeat);
        final boolean _tmpActive;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfActive);
        _tmpActive = _tmp != 0;
        _item.setActive(_tmpActive);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

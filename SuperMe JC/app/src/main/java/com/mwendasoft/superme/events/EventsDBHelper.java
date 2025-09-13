package com.mwendasoft.superme.events;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mwendasoft.superme.core.SuperMeDBHelper;
import java.util.HashSet;
import java.util.*;

public class EventsDBHelper {
    private SQLiteDatabase db;
    private final SuperMeDBHelper dbHelper;
    private final HashSet<String> existingTables = new HashSet<>();

    public EventsDBHelper(Context context) {
        dbHelper = new SuperMeDBHelper(context);
    }

    public void open() {
        try {
            if (db == null || !db.isOpen()) {
                db = dbHelper.getWritableDatabase();
                Log.d("EventsDBHelper", "Database opened at: " + db.getPath());
            }
        } catch (SQLException e) {
            Log.e("Database Error", "Failed to open database: " + e.getMessage());
        }
    }

    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
            db = null;
        }
    }
	
	public SQLiteDatabase getDatabase() {
        return dbHelper.getReadableDatabase();
    }

    private boolean tableExists(String tableName) {
        if (existingTables.contains(tableName)) return true;

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
            if (cursor != null && cursor.moveToFirst()) {
                existingTables.add(tableName);
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e("Table Check Error", "Error checking table existence: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public long insertEvent(ContentValues values) {
        open();
        if (db == null) return -1;

        if (!tableExists("events")) return -1;

        try {
            return db.insertOrThrow("events", null, values);
        } catch (Exception e) {
            Log.e("Insert Error", "Error inserting event: " + e.getMessage());
            return -1;
        }
    }

    // Media methods aligned with DiariesDBHelper style
    public int insertEventMediaPaths(int eventId, List<String> paths, String type) {
        open();
        if (!tableExists("events_media")) return -1;

        ContentValues values = new ContentValues();
        values.put("event_id", eventId);
        values.put("media_type", type);

        for (String path : paths) {
            values.put("file_path", path);
            db.insert("events_media", null, values);
        }

        return paths.size(); // Return number of inserted paths
    }

    public List<String> getMediaPaths(int eventId, String mediaType) {
        open();
        if (!tableExists("events_media")) return new ArrayList<>();

        List<String> mediaPaths = new ArrayList<>();
        Cursor cursor = db.query(
            "events_media", 
            new String[]{"file_path"}, 
            "event_id = ? AND media_type = ?", 
            new String[]{String.valueOf(eventId), mediaType}, 
            null, null, null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                mediaPaths.add(cursor.getString(0));
            }
            cursor.close();
        }

        return mediaPaths;
    }

    public List<String> getAllMediaPaths(int eventId) {
        open();
        if (!tableExists("events_media")) return new ArrayList<>();

        List<String> paths = new ArrayList<>();
        Cursor cursor = db.rawQuery(
            "SELECT file_path FROM events_media WHERE event_id = ?", 
            new String[]{String.valueOf(eventId)}
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                paths.add(cursor.getString(0));
            }
            cursor.close();
        }
        return paths;
    }

    // Rest of the methods remain unchanged...
    public Cursor getAllEvents() {
        open();
        if (db == null) return null;

        if (!tableExists("events")) return null;

        try {
            return db.rawQuery("SELECT * FROM events ORDER BY date DESC, time DESC", null);
        } catch (Exception e) {
            Log.e("Fetch Error", "Error fetching events: " + e.getMessage());
            return null;
        }
    }

    public Cursor getAllEventTitles() {
        open();
        if (db == null) return null;

        if (!tableExists("events")) return null;

        try {
            return db.rawQuery("SELECT title FROM events ORDER BY date ASC", null);
        } catch (Exception e) {
            Log.e("Fetch Error", "Error fetching event titles: " + e.getMessage());
            return null;
        }
    }

    public Cursor getEventByTitle(String title) {
        open();
        if (db == null) return null;

        if (!tableExists("events")) return null;

        try {
            return db.rawQuery("SELECT * FROM events WHERE title = ?", new String[]{title});
        } catch (Exception e) {
            Log.e("Fetch Error", "Error fetching event: " + e.getMessage());
            return null;
        }
    }

    public Cursor getEventById(int eventId) {
        open();
        if (db == null) return null;

        if (!tableExists("events")) return null;

        try {
            return db.rawQuery("SELECT * FROM events WHERE id = ?", new String[]{String.valueOf(eventId)});
        } catch (Exception e) {
            Log.e("Fetch Error", "Error fetching event: " + e.getMessage());
            return null;
        }
    }
	
	public int getEventsCount() {
		open();
		if (db == null) return 0;

		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM events", null);
		int count = 0;

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
			cursor.close();
		}

		return count;
	}
	
    // In EventsDBHelper.java
	public Cursor getAllEventsWithReminders() {
		open();
		return db.query(
			"events",
			null,
			"reminder_set = 1",  // Only items with reminders
			null, null, null, null
		);
	}

    public int markEventAsAttended(int eventId) {
        open();
        if (db == null) return -1;

        ContentValues values = new ContentValues();
        values.put("attended", 1);

        try {
            return db.update("events", values, "id = ?", new String[]{String.valueOf(eventId)});
        } catch (Exception e) {
            Log.e("Update Error", "Error updating event: " + e.getMessage());
            return -1;
        }
    }

    public int updateEvent(String title, ContentValues values) {
        open();
        if (db == null) return -1;

        if (!tableExists("events")) return -1;

        try {
            return db.update("events", values, "title = ?", new String[]{title});
        } catch (Exception e) {
            Log.e("Update Error", "Error updating event: " + e.getMessage());
            return -1;
        }
    }

    public int deleteEvent(int eventId) {
        open();
        if (db == null) return -1;

        if (!tableExists("events")) return -1;

        try {
            return db.delete("events", "id = ?", new String[]{String.valueOf(eventId)});
        } catch (Exception e) {
            Log.e("Delete Error", "Error deleting event: " + e.getMessage());
            return -1;
        }
    }

    public int getEventIdByTitle(String title) {
        open();
        int eventId = -1;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT id FROM events WHERE title = ?", new String[]{title});
            if (cursor != null && cursor.moveToFirst()) {
                eventId = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("EventsDbBHelper", "Error fetching event ID by name: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return eventId;
    }
}

package com.mwendasoft.superme.diaries;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.mwendasoft.superme.core.SuperMeDBHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.*;

public class DiariesDBHelper {
    private SQLiteDatabase db;
    private final SuperMeDBHelper dbHelper;

    public DiariesDBHelper(Context context) {
        dbHelper = new SuperMeDBHelper(context);
    }

    public void open() throws SQLException {
        if (db == null || !db.isOpen()) {
            db = dbHelper.getWritableDatabase();
        }
    }

    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    public SQLiteDatabase getDatabase() {
        if (db == null || !db.isOpen()) {
            open();
        }
        return db;
    }

    public long insertDiaryEntry(ContentValues values) {
        open();
        if (!dbHelper.doesTableExist("diaries")) return -1;

        return db.insert("diaries", null, values);
    }
	
	public int insertDiaryMediaPaths(int diaryId, List<String> paths, String type) {
		open();
		if (db == null) return -1;

		ContentValues values = new ContentValues();
		values.put("diary_id", diaryId);
		values.put("media_type", type);

		for (String path : paths) {
			values.put("file_path", path);
			db.insert("diary_media", null, values);
		}

		return 1;
	}
	
	public List<String> getMediaPaths(int diaryId, String mediaType) {
		open();

		List<String> mediaPaths = new ArrayList<>();
		Cursor cursor = db.query(
			"diary_media",                          // Table name
			new String[]{"file_path"},            // Columns to return
			"diary_id = ? AND media_type = ?",                        // WHERE clause
			new String[]{String.valueOf(diaryId), mediaType}, // WHERE args
			null, null, null                      // groupBy, having, orderBy
		);

		if (cursor != null) {
			while (cursor.moveToNext()) {
				String path = cursor.getString(cursor.getColumnIndexOrThrow("file_path"));
				mediaPaths.add(path);
			}
			cursor.close();
		}

		return mediaPaths;
	}

	// Get all media file paths for a note
	public List<String> getAllMediaPaths(int diaryId) {
		open();
		List<String> paths = new ArrayList<>();
		Cursor cursor = db.rawQuery("SELECT file_path FROM diary_media WHERE diary_id = ?", new String[]{String.valueOf(diaryId)});

		if (cursor.moveToFirst()) {
			do {
				paths.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return paths;
	}

    public Cursor getAllDiaryEntries() {
        open();
        if (!dbHelper.doesTableExist("diaries")) return null;

        return db.query("diaries", null, null, null, null, null, "date DESC");
    }

    public Cursor getDiaryTitles() {
        open();
        if (!dbHelper.doesTableExist("diaries")) return null;

        return db.query("diaries", new String[]{"title"}, null, null, null, null, "date DESC");
    }
	
	public Cursor getAllEntries() {
        open();
        if (!dbHelper.doesTableExist("diaries")) return null;
		
        return db.rawQuery("SELECT * FROM diaries ORDER BY date DESC, time DESC", null);
	}
	
	public Cursor getDiaryTitlesAndMood() {
		open();
        if (!dbHelper.doesTableExist("diaries")) return null;

		return db.rawQuery("SELECT id, title, mood, date, time FROM diaries;", null);
      }

    public Cursor getDiaryEntryByTitle(String title) {
        open();
        if (!dbHelper.doesTableExist("diaries")) return null;
		
		return db.rawQuery("SELECT * FROM diaries WHERE title = ?", new String[]{title});
    }
	
	public Cursor getDiaryEntryById(int entryId) {
        open();
        if (!dbHelper.doesTableExist("diaries")) return null;

		return db.rawQuery("SELECT * FROM diaries WHERE id = ?", new String[]{String.valueOf(entryId)});
    }
	
	public int getEntryCount() {
		open();
		if (db == null) return 0;

		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM diaries", null);
		int count = 0;

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
			cursor.close();
		}

		return count;
	}
	
	public int updateEntry(ContentValues values, int entryId) {
        open();
        if (!dbHelper.doesTableExist("diaries")) {
            return 0;
        }
        return db.update("diaries", values, "id = ?", new String[]{String.valueOf(entryId)});
    }
	
	public int deleteDiaryEntry(int diaryId) {
		open();
        if (!dbHelper.doesTableExist("diaries")) {
            return 0;
        }
        return db.delete("diaries", "id = ?", new String[]{String.valueOf(diaryId)});
	}

    public String formatDiaryDate(String dbDate) {
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat formattedDate = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());

        try {
            Date date = dbFormat.parse(dbDate);
            return (date != null) ? formattedDate.format(date) : dbDate;
        } catch (ParseException e) {
            e.printStackTrace();
            return dbDate;
        }
    }
}
	   
	   
 
	

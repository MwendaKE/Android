package com.mwendasoft.superme.clips;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mwendasoft.superme.core.SuperMeDBHelper;
import java.util.*;

public class ClipsDBHelper {
    private SQLiteDatabase db;
    private SuperMeDBHelper dbHelper;

    public ClipsDBHelper(Context context) {
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

    public long insertClip(ContentValues values) {
        open();
        if (!dbHelper.doesTableExist("clips")) {
            return -1;
        }
        return db.insert("clips", null, values);
    }

    public Cursor getAllClips() {
        open();
        if (!dbHelper.doesTableExist("clips")) {
            return null;
        }
        return db.rawQuery("SELECT * FROM clips ORDER BY writer ASC", null);
    }
	
	public int getClipsCount() {
		open();
		if (db == null) return 0;

		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM clips", null);
		int count = 0;

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
			cursor.close();
		}

		return count;
	}

    public Cursor getClipByWriter(String writer) {
        open();
        if (!dbHelper.doesTableExist("clips")) {
            return null;
        }
        return db.rawQuery("SELECT * FROM clips WHERE writer = ?", new String[]{writer});
    }
	
	public List<String> getClipsWriters() {
		open();
		List<String> authors = new ArrayList<>();
		if (db == null) return authors;

		Cursor cursor = db.rawQuery("SELECT DISTINCT writer FROM clips ORDER BY writer ASC", null);

		if (cursor != null) {
			try {
				while (cursor.moveToNext()) {
					String author = cursor.getString(0);
					if (author != null && !author.trim().isEmpty()) {
						authors.add(author);
					}
				}
			} finally {
				cursor.close();
			}
		}

		return authors;
	}

	public int getClipsWriterCount() {
		open();
		if (db == null) return 0;

		Cursor cursor = db.rawQuery("SELECT COUNT(DISTINCT writer) FROM clips", null);
		int count = 0;

		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					count = cursor.getInt(0);
				}
			} finally {
				cursor.close();
			}
		}

		return count;
	}

    public int updateClip(int clipId, ContentValues values) {
        open();
        if (!dbHelper.doesTableExist("clips")) {
            return 0;
        }
        try {
            return db.update("clips", values, "id = ?", new String[]{String.valueOf(clipId)});
        } catch (Exception e) {
            Log.e("Update Error", "Error updating clip: " + e.getMessage());
            return -1;
        }
    }

    public int getClipId(String clipText, String clipWriter, String clipSource) {
        open();
        if (!dbHelper.doesTableExist("clips")) {
            return -1;
        }
        Cursor cursor = db.rawQuery(
            "SELECT id FROM clips WHERE clip = ? AND writer = ? AND source = ?",
            new String[]{clipText, clipWriter, clipSource}
        );

        int clipId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            clipId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            cursor.close();
        }

        return clipId;
    }

    public int deleteClip(String clipText, String clipWriter, String clipSource) {
        open();
        if (!dbHelper.doesTableExist("clips")) {
            return 0;
        }
        return db.delete("clips", "clip = ? AND writer = ? AND source = ?", new String[]{clipText, clipWriter, clipSource});
    }
}

package com.mwendasoft.superme.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.mwendasoft.superme.core.SuperMeDBHelper;
import android.util.*;
import java.util.*;

public class NotesDBHelper {
    private SQLiteDatabase db;
    private final SuperMeDBHelper dbHelper;

    public NotesDBHelper(Context context) {
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
            db = null;
        }
    }
	
	public SQLiteDatabase getDatabase() {
        return dbHelper.getReadableDatabase();
    }

    public long insertNote(ContentValues values) {
        open();
        if (db == null) return -1;
        return db.insert("notes", null, values);
    }
	
	public int insertNoteMediaPaths(int noteId, List<String> paths, String type) {
		open();
		if (db == null) return -1;

		ContentValues values = new ContentValues();
		values.put("note_id", noteId);
		values.put("media_type", type);

		for (String path : paths) {
			values.put("file_path", path);
			db.insert("note_media", null, values);
		}

		return 1;
	}
	
	public List<String> getMediaPaths(int noteId, String mediaType) {
		open();
		
		List<String> mediaPaths = new ArrayList<>();
		Cursor cursor = db.query(
			"note_media",                          // Table name
			new String[]{"file_path"},            // Columns to return
			"note_id = ? AND media_type = ?",     // WHERE clause
			new String[]{String.valueOf(noteId), mediaType}, // WHERE args
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
	public List<String> getAllMediaPaths(int noteId) {
		open();
		List<String> paths = new ArrayList<>();
		Cursor cursor = db.rawQuery("SELECT file_path FROM note_media WHERE note_id = ?", new String[]{String.valueOf(noteId)});

		if (cursor.moveToFirst()) {
			do {
				paths.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return paths;
	}

    public Cursor getAllNotes() {
        open();
        if (db == null) return null;
        return db.rawQuery("SELECT * FROM notes ORDER BY datetime DESC", null);
    }

    public Cursor getNoteById(int id) {
		open();
		if (db == null) return null;
		return db.rawQuery("SELECT * FROM notes WHERE id = ?", new String[]{String.valueOf(id)});
	}
	
	public int getNotesCount() {
		open();
		if (db == null) return 0;

		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM notes", null);
		int count = 0;

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
			cursor.close();
		}

		return count;
	}

    public int updateNote(ContentValues values, int noteId) {
        open();
        if (db == null) return 0;
        return db.update("notes", values, "id = ?", new String[]{String.valueOf(noteId)});
    }

    public int deleteNote(int noteId) {
        open();
        if (db == null) return 0;

        return db.delete("notes", "id = ?", new String[]{String.valueOf(noteId)});
    }
}

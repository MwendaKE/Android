package com.mwendasoft.superme.music;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mwendasoft.superme.core.SuperMeDBHelper;

public class SongsDBHelper {
    private SQLiteDatabase db;
    private final SuperMeDBHelper dbHelper;

    public SongsDBHelper(Context context) {
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
        dbHelper.close();
    }

    public SQLiteDatabase getDatabase() {
        if (db == null || !db.isOpen()) {
            db = dbHelper.getReadableDatabase();
        }
        return db;
    }

    public long insertSong(ContentValues values) {
        open();
        return db.insert("songs", null, values);
    }
	
	public int insertSongMediaPath(int songId, String path) {
		open();
		if (db == null) return -1;

		ContentValues values = new ContentValues();
		values.put("music_id", songId);
		values.put("file_path", path);
		
		db.insert("music_media", null, values);

		return 1;
	}
	
	public int updateSongMediaPath(int songId, String path) {
		open();
		if (db == null) return -1;

		ContentValues values = new ContentValues();
		values.put("file_path", path);

		db.update("music_media", values, "id = ?", new String[]{String.valueOf(songId)});
		
		return 1;
	}

    public Cursor getAllSongs() {
        open();
        return db.rawQuery("SELECT * FROM songs", null);
    }
	
	public int getSongsCount() {
		open();
		if (db == null) return 0;

		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM songs", null);
		int count = 0;

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
			cursor.close();
		}

		return count;
	}
	
	public String getMusicPath(int songId) {
		open();

		String musicPath = null;

		Cursor cursor = db.query(
			"music_media",                          // Table name
			new String[]{"file_path"},            // Columns to return
			"music_id = ?",                        // WHERE clause
			new String[]{String.valueOf(songId)}, // WHERE args
			null, null, null                      // groupBy, having, orderBy
		);

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				musicPath = cursor.getString(cursor.getColumnIndexOrThrow("file_path"));
			}
			cursor.close();
		}

		return musicPath;
	}
	
	public Cursor getSongLyriesById(int songId) {
        open();
        return db.rawQuery("SELECT lyrics FROM songs WHERE id = ?", new String[]{String.valueOf(songId)});
    }

    public Cursor getSongByTitleAndArtist(String title, int artistId) {
        open();
        return db.rawQuery("SELECT * FROM songs WHERE title = ? and artist = ?", new String[]{title, String.valueOf(artistId)});
    }
	
	public Cursor getSongById(int songId) {
        open();
        return db.rawQuery("SELECT * FROM songs WHERE id =?", new String[]{String.valueOf(songId)});
    }

    public Cursor getSongTitlesWithArtist() {
        open();
        return db.rawQuery("SELECT title, artist FROM songs", null);
    }

    public int updateSong(int songId, ContentValues values) {
        return db.update("songs", values, "id = ?", new String[]{String.valueOf(songId)});
    }

    public int deleteSong(int songId) {
        open();
        return db.delete("songs", "id = ?", new String[]{String.valueOf(songId)});
    }
}

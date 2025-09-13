package com.mwendasoft.newsip.newshelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.database.*;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "newsip.db";
    private static final int DATABASE_VERSION = 2; // upgraded to recreate table

    private static final String TABLE_BOOKMARKS = "bookmarks";

    // NOTE: Changed id to _id for SimpleCursorAdapter compatibility
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_IS_BOOKMARKED = "isBookmarked";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_BOOKMARKS + " (" +
			COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			COLUMN_TITLE + " TEXT NOT NULL, " +
			COLUMN_URL + " TEXT UNIQUE, " +
			COLUMN_IS_BOOKMARKED + " INTEGER DEFAULT 0)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARKS);
        onCreate(db);
    }

    public void addBookmark(String title, String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_URL, url);
        values.put(COLUMN_IS_BOOKMARKED, 1);

        db.insertWithOnConflict(TABLE_BOOKMARKS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void removeBookmark(String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_BOOKMARKED, 0);
        db.update(TABLE_BOOKMARKS, values, COLUMN_URL + " = ?", new String[]{url});
    }

    public boolean isBookmarked(String url) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean bookmarked = false;

        try {
            cursor = db.query(TABLE_BOOKMARKS,
							  new String[]{COLUMN_IS_BOOKMARKED},
							  COLUMN_URL + " = ?",
							  new String[]{url},
							  null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int state = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_BOOKMARKED));
                bookmarked = (state == 1);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return bookmarked;
    }

    public Cursor getAllBookmarks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
			"SELECT * FROM " + TABLE_BOOKMARKS +
			" WHERE " + COLUMN_IS_BOOKMARKED + " = 1" +
			" ORDER BY " + COLUMN_ID + " DESC", null);
    }
	
	public int deleteBookmark(String url) {
		SQLiteDatabase db = this.getWritableDatabase();
		try {
			return db.delete(TABLE_BOOKMARKS, "url = ?", new String[]{url});
		} catch (SQLException e) {
			return 0;
		}
	}
}

package com.mwendasoft.superme.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.util.Log;

public class SuperMeDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "SuperMeDBHelper";
    private static final String DATABASE_NAME = "SuperMeDB.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_AUTHORS = "authors";
    private static final String TABLE_CATEGORIES = "categories";
    private static final String TABLE_BOOKS = "books";
    private static final String TABLE_SONGS = "songs";
    private static final String TABLE_MUSIC_MEDIA = "music_media";
    private static final String TABLE_SPARK_VIDEOS = "spark_videos";
    private static final String TABLE_POEMS = "poems";
    private static final String TABLE_POEMS_MEDIA = "poems_media";
    private static final String TABLE_QUOTES = "quotes";
    private static final String TABLE_CLIPS = "clips";
    private static final String TABLE_ARTICLES = "articles";
    private static final String TABLE_EVENTS = "events";
    private static final String TABLE_EVENTS_MEDIA = "events_media";
    private static final String TABLE_DIARIES = "diaries";
    private static final String TABLE_DIARY_MEDIA = "diary_media";
    private static final String TABLE_TASKS = "tasks";
    private static final String TABLE_TASKS_MEDIA = "tasks_media";
    private static final String TABLE_SUMMARIES = "summaries";
    private static final String TABLE_NOTES = "notes";
    private static final String TABLE_NOTE_MEDIA = "note_media";
    private static final String TABLE_PEOPLE = "people";

    public SuperMeDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        executeInTransaction(db, new DatabaseOperation() {
				@Override
				public void execute(SQLiteDatabase db) {
					createAllTables(db);
				}
			});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, final int oldVersion, final int newVersion) {
        executeInTransaction(db, new DatabaseOperation() {
				@Override
				public void execute(SQLiteDatabase db) {
					Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
					dropAllTables(db);
					createAllTables(db);
				}
			});
    }

    private void createAllTables(SQLiteDatabase db) {
        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys = ON;");

        // Authors table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_AUTHORS + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "author_name TEXT NOT NULL UNIQUE COLLATE NOCASE, " +
				   "author_categ TEXT NOT NULL);");

        // Categories table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORIES + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "category TEXT NOT NULL UNIQUE COLLATE NOCASE);");

        // Books table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_BOOKS + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "title TEXT NOT NULL, " +
				   "author INTEGER NOT NULL, " +
				   "review TEXT NOT NULL, " +
				   "category INTEGER NOT NULL, " +
				   "read INTEGER NOT NULL DEFAULT 0 CHECK (read IN (0,1,2)), " +
				   "FOREIGN KEY (author) REFERENCES " + TABLE_AUTHORS + "(id) ON UPDATE RESTRICT ON DELETE RESTRICT, " +
				   "FOREIGN KEY (category) REFERENCES " + TABLE_CATEGORIES + "(id) ON UPDATE CASCADE ON DELETE RESTRICT);");

        // Songs table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SONGS + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "title TEXT NOT NULL, " +
				   "artist INTEGER NOT NULL, " +
				   "lyrics TEXT NOT NULL, " +
				   "genre INTEGER NOT NULL, " +
				   "FOREIGN KEY (artist) REFERENCES " + TABLE_AUTHORS + "(id) ON UPDATE RESTRICT ON DELETE RESTRICT, " +
				   "FOREIGN KEY (genre) REFERENCES " + TABLE_CATEGORIES + "(id) ON UPDATE CASCADE ON DELETE RESTRICT);");

        // Music Media
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MUSIC_MEDIA + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "music_id INTEGER NOT NULL, " +
				   "file_path TEXT NOT NULL UNIQUE, " +
				   "FOREIGN KEY(music_id) REFERENCES " + TABLE_SONGS + "(id) ON DELETE CASCADE);");

        // Spark Media
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SPARK_VIDEOS + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "file_path TEXT NOT NULL UNIQUE, " +
				   "category INTEGER NOT NULL);");

        // Poems table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_POEMS + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "title TEXT NOT NULL, " +
				   "poem TEXT NOT NULL, " +
				   "poet INTEGER NOT NULL, " +
				   "FOREIGN KEY (poet) REFERENCES " + TABLE_AUTHORS + "(id) ON UPDATE RESTRICT ON DELETE RESTRICT);");

        // Poems Media (images only)
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_POEMS_MEDIA + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "poem_id INTEGER NOT NULL, " +
				   "file_path TEXT NOT NULL UNIQUE, " +
				   "FOREIGN KEY(poem_id) REFERENCES " + TABLE_POEMS + "(id) ON DELETE CASCADE);");

        // Quotes table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_QUOTES + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "author INTEGER NOT NULL, " +
				   "quote TEXT NOT NULL UNIQUE COLLATE NOCASE, " +
				   "FOREIGN KEY (author) REFERENCES " + TABLE_AUTHORS + "(id) ON UPDATE RESTRICT ON DELETE RESTRICT);");

        // Clips table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CLIPS + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "clip TEXT NOT NULL, " +
				   "writer TEXT NOT NULL, " +
				   "source TEXT NOT NULL);");

        // Articles table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ARTICLES + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "title TEXT NOT NULL, " +
				   "writer INTEGER NOT NULL, " +
				   "category INTEGER NOT NULL, " +
				   "body TEXT NOT NULL, " +
				   "date TEXT NOT NULL, " +
				   "link TEXT, " +
				   "FOREIGN KEY (category) REFERENCES " + TABLE_CATEGORIES + "(id) ON UPDATE CASCADE ON DELETE RESTRICT, " +
				   "FOREIGN KEY (writer) REFERENCES " + TABLE_AUTHORS + "(id) ON UPDATE RESTRICT ON DELETE RESTRICT);");

        // Events table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_EVENTS + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "title TEXT NOT NULL, " +
				   "date TEXT NOT NULL, " +
				   "time TEXT NOT NULL, " +
				   "notes TEXT NOT NULL, " +
				   "address TEXT NOT NULL, " +
				   "budget REAL NOT NULL DEFAULT 0, " +
				   "attended INTEGER NOT NULL DEFAULT 0);");

        // Events Media
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_EVENTS_MEDIA + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "event_id INTEGER NOT NULL, " +
				   "media_type TEXT NOT NULL CHECK(media_type IN ('image','audio','video')), " +
				   "file_path TEXT NOT NULL UNIQUE, " +
				   "FOREIGN KEY(event_id) REFERENCES " + TABLE_EVENTS + "(id) ON DELETE CASCADE);");

        // Diaries table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DIARIES + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "title TEXT NOT NULL, " +
				   "date TEXT NOT NULL, " +
				   "time TEXT NOT NULL, " +
				   "mood TEXT NOT NULL, " +
				   "description TEXT NOT NULL);");

        // Diary Media
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DIARY_MEDIA + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "diary_id INTEGER NOT NULL, " +
				   "media_type TEXT NOT NULL CHECK(media_type IN ('image','audio','video')), " +
				   "file_path TEXT NOT NULL UNIQUE, " +
				   "FOREIGN KEY(diary_id) REFERENCES " + TABLE_DIARIES + "(id) ON DELETE CASCADE);");

        // Tasks table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TASKS + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "title TEXT NOT NULL, " +
				   "sdate TEXT NOT NULL, " +
				   "stime TEXT NOT NULL, " +
				   "duration INTEGER NOT NULL DEFAULT 1, " +
				   "edate TEXT NOT NULL, " +
				   "category INTEGER NOT NULL, " +
				   "success INTEGER NOT NULL DEFAULT 0 CHECK (success IN (0,1)), " +
				   "description TEXT NOT NULL, " +
				   "FOREIGN KEY (category) REFERENCES " + TABLE_CATEGORIES + "(id) ON UPDATE CASCADE ON DELETE RESTRICT);");

        // Tasks Media (images only)
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TASKS_MEDIA + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "task_id INTEGER NOT NULL, " +
				   "file_path TEXT NOT NULL UNIQUE, " +
				   "FOREIGN KEY(task_id) REFERENCES " + TABLE_TASKS + "(id) ON DELETE CASCADE);");

        // Summaries table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SUMMARIES + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "title TEXT NOT NULL, " +
				   "author TEXT NOT NULL, " +
				   "favorite INTEGER NOT NULL DEFAULT 0 CHECK (favorite IN (0,1)), " +
				   "summary TEXT NOT NULL);");

        // Notes
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NOTES + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "title TEXT NOT NULL, " +
				   "datetime TEXT NOT NULL, " +
				   "importance INTEGER NOT NULL DEFAULT 0 CHECK (importance IN (0,1)), " +
				   "notes TEXT NOT NULL);");

        // Notes Media
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NOTE_MEDIA + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "note_id INTEGER NOT NULL, " +
				   "media_type TEXT NOT NULL CHECK(media_type IN ('image','audio','video')), " +
				   "file_path TEXT NOT NULL UNIQUE, " +
				   "FOREIGN KEY(note_id) REFERENCES " + TABLE_NOTES + "(id) ON DELETE CASCADE);");

        // People
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PEOPLE + " (" +
				   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "name TEXT NOT NULL, " +
				   "occupation TEXT NOT NULL, " +
				   "description TEXT NOT NULL);");
    }

    private void dropAllTables(SQLiteDatabase db) {
        // Drop all tables in reverse order of dependency
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE_MEDIA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS_MEDIA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS_MEDIA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DIARY_MEDIA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POEMS_MEDIA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MUSIC_MEDIA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPARK_VIDEOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUMMARIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DIARIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARTICLES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEOPLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUTHORS);
    }

    public boolean doesTableExist(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return false;
        }

        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery(
				"SELECT name FROM sqlite_master WHERE type='table' AND name=?",
				new String[]{tableName});
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking table: " + tableName, e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private interface DatabaseOperation {
        void execute(SQLiteDatabase db);
    }

    private void executeInTransaction(SQLiteDatabase db, DatabaseOperation operation) {
        db.beginTransaction();
        try {
            operation.execute(db);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Database operation failed", e);
        } finally {
            db.endTransaction();
        }
    }
}

package com.mwendasoft.superme.sparks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mwendasoft.superme.core.SuperMeDBHelper;

import java.util.ArrayList;
import java.util.List;

public class SparksDBHelper {
    private static final String TAG = "SparksDBHelper";
    private static final String TABLE_NAME = "spark_videos";
    private static final String COL_FILE_PATH = "file_path";
    private static final String COL_CATEGORY = "category";

    private SQLiteDatabase db;
    private final SuperMeDBHelper dbHelper;

    public SparksDBHelper(Context context) {
        dbHelper = new SuperMeDBHelper(context);
    }

    public void open() throws SQLException {
        try {
            if (db == null || !db.isOpen()) {
                db = dbHelper.getWritableDatabase();
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error opening database", e);
            throw e;
        }
    }

    public void close() {
        try {
            if (db != null && db.isOpen()) {
                db.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error closing database", e);
        } finally {
            db = null;
        }
    }

    // Insert one video with transaction
    public long insertVideo(String filePath, int category) {
        try {
            open();
            if (db == null) return -1;

            ContentValues values = new ContentValues();
            values.put(COL_FILE_PATH, filePath);
            values.put(COL_CATEGORY, category);

            return db.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting video", e);
            return -1;
        }
    }

    // Insert multiple videos in a single transaction
    public int insertMultipleVideos(List<String> paths, int category) {
        try {
            open();
            if (db == null || paths == null) return -1;

            db.beginTransaction();
            try {
                for (String path : paths) {
                    ContentValues values = new ContentValues();
                    values.put(COL_FILE_PATH, path);
                    values.put(COL_CATEGORY, category);
                    db.insert(TABLE_NAME, null, values);
                }
                db.setTransactionSuccessful();
                return paths.size();
            } finally {
                db.endTransaction();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inserting multiple videos", e);
            return -1;
        }
    }

    // Get videos by category with null checks
    public List<String> getVideosByCategory(int category) {
        List<String> videos = new ArrayList<>();
        Cursor cursor = null;
        try {
            open();
            if (db == null) return videos;

            cursor = db.query(
                TABLE_NAME,
                new String[]{COL_FILE_PATH},
                COL_CATEGORY + " = ?",
                new String[]{String.valueOf(category)},
                null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    videos.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
            return videos;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching videos", e);
            return videos;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }
	
	/**
	 * Returns the total number of videos in the spark_videos table
	 * @return count of videos, or -1 if error occurs
	 */
	public int getVideoCount() {
		Cursor cursor = null;
		try {
			open();
			if (db == null) return -1;

			cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
			if (cursor != null && cursor.moveToFirst()) {
				return cursor.getInt(0);
			}
			return -1;
		} catch (Exception e) {
			Log.e(TAG, "Error getting video count", e);
			return -1;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

    // Delete video with error handling
    public int deleteSpark(String filePath) {
        try {
            open();
            if (db == null) return 0;

            return db.delete(
                TABLE_NAME,
                COL_FILE_PATH + " = ?",
                new String[]{filePath}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error deleting video", e);
            return 0;
        }
    }
}

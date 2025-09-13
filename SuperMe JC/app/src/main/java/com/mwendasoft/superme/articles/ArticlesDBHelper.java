package com.mwendasoft.superme.articles;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mwendasoft.superme.core.SuperMeDBHelper;

public class ArticlesDBHelper {
    private SQLiteDatabase db;
    private SuperMeDBHelper dbHelper;
    private Context context;
    private static final String TAG = "ArticlesDBHelper";

    public ArticlesDBHelper(Context context) {
        this.context = context;
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
            if (db != null) {
                if (db.isOpen()) {
                    db.close();
                }
                db = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error closing database", e);
        }
    }
	
	public SQLiteDatabase getDatabase() {
        return dbHelper.getReadableDatabase();
    }

    public boolean isDatabaseOpen() {
        return db != null && db.isOpen();
    }

    public long insertArticle(ContentValues values) {
        try {
            open();
            if (!dbHelper.doesTableExist("articles")) {
                Log.w(TAG, "Articles table doesn't exist");
                return -1;
            }
            return db.insert("articles", null, values);
        } catch (SQLException e) {
            Log.e(TAG, "Error inserting article", e);
            return -1;
        }
    }

    public Cursor getAllArticles() {
		open();
        if (!dbHelper.doesTableExist("articles")) {
            return null;
        }
        return db.rawQuery("SELECT * FROM articles ORDER BY title ASC", null);
    }

    public Cursor getArticleById(int articleId) {
        open();
        if (!dbHelper.doesTableExist("articles")) {
            return null;
        }
        return db.rawQuery("SELECT * FROM articles WHERE id = ?", new String[]{String.valueOf(articleId)});
	}
	
	public int getArticlesCount() {
		open();
		if (db == null) return 0;

		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM articles", null);
		int count = 0;

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
			cursor.close();
		}

		return count;
	}

    public int updateArticle(ContentValues values, int articleId) {
        try {
            open();
            if (!dbHelper.doesTableExist("articles")) {
                Log.w(TAG, "Articles table doesn't exist");
                return 0;
            }
            return db.update("articles", values, "id = ?", 
							 new String[]{String.valueOf(articleId)});
        } catch (SQLException e) {
            Log.e(TAG, "Error updating article", e);
            return 0;
        }
    }

    public int deleteArticle(int articleId) {
        try {
            open();
            if (!dbHelper.doesTableExist("articles")) {
                Log.w(TAG, "Articles table doesn't exist");
                return 0;
            }
            return db.delete("articles", "id = ?", 
							 new String[]{String.valueOf(articleId)});
        } catch (SQLException e) {
            Log.e(TAG, "Error deleting article", e);
            return 0;
        }
    }
}

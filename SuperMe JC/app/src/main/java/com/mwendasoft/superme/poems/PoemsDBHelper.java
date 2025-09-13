package com.mwendasoft.superme.poems;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.mwendasoft.superme.core.SuperMeDBHelper;
import android.util.*;
import java.util.*;

public class PoemsDBHelper {
    private SQLiteDatabase db;
    private SuperMeDBHelper dbHelper;

    public PoemsDBHelper(Context context) {
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

    public long insertPoem(ContentValues values) {
        open();
        if (!dbHelper.doesTableExist("poems")) {
            return -1;
        }
        return db.insert("poems", null, values);
    }
	
	public long insertPoemMediaPath(int poemId, String path) {
		open();
		if (db == null) {
			return -1;
		}

		ContentValues values = new ContentValues();
		values.put("poem_id", poemId);
		values.put("file_path", path);

		try {
			return db.insert("poems_media", null, values);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			close();
		}
	}
	
	public int updatePoemMediaPath(int poemId, String path) {
		open();
		if (db == null) return -1;

		ContentValues values = new ContentValues();
		values.put("file_path", path);

		db.update("poems_media", values, "id = ?", new String[]{String.valueOf(poemId)});

		return 1;
	}
	
	public List<String> getMediaPaths(int poemId) {
		open();
		List<String> mediaPaths = new ArrayList<>();

		Cursor cursor = db.query(
			"poems_media",                          // Table name
			new String[]{"file_path"},            // Columns to return
			"poem_id = ?",                        // WHERE clause
			new String[]{String.valueOf(poemId)}, // WHERE args
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
	
	public String getPoemPath(int poemId) {
		open();

		String poemPath = null;

		Cursor cursor = db.query(
			"poems_media",                          // Table name
			new String[]{"file_path"},            // Columns to return
			"poem_id = ?",                        // WHERE clause
			new String[]{String.valueOf(poemId)}, // WHERE args
			null, null, null                      // groupBy, having, orderBy
		);

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				poemPath = cursor.getString(cursor.getColumnIndexOrThrow("file_path"));
			}
			cursor.close();
		}

		return poemPath;
	}

    public Cursor getAllPoems() {
        open();
        if (!dbHelper.doesTableExist("poems")) {
            return null;
        }
        return db.rawQuery("SELECT * FROM poems ORDER BY title ASC", null);
    }
	
	public int getPoemsCount() {
		open();
		if (db == null) return 0;

		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM poems", null);
		int count = 0;

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
			cursor.close();
		}

		return count;
	}

    public Cursor getPoemWithPoet() {
        open();
        if (!dbHelper.doesTableExist("poems")) {
            return null;
        }
        return db.rawQuery("SELECT title, poet FROM poems", null);
    }

    public Cursor getPoemDetailByTitleAndAuthor(String title, int poetId) {
        open();
        if (!dbHelper.doesTableExist("poems")) {
            return null;
        }
        return db.rawQuery("SELECT poem FROM poems WHERE title = ? AND poet = ?", new String[]{title, String.valueOf(poetId)});
    }
	
	public Cursor getPoemById(int poemId) {
        open();
        if (!dbHelper.doesTableExist("poems")) {
            return null;
        }
        return db.rawQuery("SELECT * FROM poems WHERE id = ?", new String[]{String.valueOf(poemId)});
    }
	
	public Cursor getPoemEditDetail(String title, int poetId) {
        open();
        if (!dbHelper.doesTableExist("poems")) {
            return null;
        }
        return db.rawQuery("SELECT * FROM poems WHERE title = ? AND poet = ?", new String[]{title, String.valueOf(poetId)});
    }
	
	public Cursor getPoemByTitleAndAuthor(String title, int poetId) {
        open();
        if (!dbHelper.doesTableExist("poems")) {
            return null;
        }
        return db.rawQuery("SELECT * FROM poems WHERE title = ? AND poet = ?", new String[]{title, String.valueOf(poetId)});
    }

    public int updatePoem(int poemId, ContentValues values) {
        open();
        if (!dbHelper.doesTableExist("poems")) {
            return 0;
        }
        return db.update("poems", values, "id = ?", new String[]{String.valueOf(poemId)});
    }

    public int deletePoem(int poemId) {
        open();
        if (!dbHelper.doesTableExist("poems")) {
            return 0;
        }
        return db.delete("poems", "id = ?", new String[]{String.valueOf(poemId)});
    }
}

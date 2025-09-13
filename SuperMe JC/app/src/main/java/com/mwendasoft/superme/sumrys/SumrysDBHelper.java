package com.mwendasoft.superme.sumrys;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.mwendasoft.superme.core.SuperMeDBHelper;
import android.util.*;
import java.util.*;

public class SumrysDBHelper {
    private SQLiteDatabase db;
    private final SuperMeDBHelper dbHelper;

    public SumrysDBHelper(Context context) {
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

    public long insertSummary(ContentValues values) {
        open();
        if (db == null) return -1;
        return db.insert("summaries", null, values);
    }

    public Cursor getAllSummaries() {
        open();
        if (db == null) return null;
        return db.rawQuery("SELECT * FROM summaries ORDER BY title ASC", null);
    }
	
	public int getSummariesCount() {
		open();
		if (db == null) return 0;

		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM summaries", null);
		int count = 0;

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
			cursor.close();
		}

		return count;
	}
	
	public List<String> getSummaryAuthors() {
		open();
		List<String> authors = new ArrayList<>();
		if (db == null) return authors;

		Cursor cursor = db.rawQuery("SELECT DISTINCT author FROM summaries ORDER BY author ASC", null);

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
	
	public int getSummariesAuthorCount() {
		open();
		if (db == null) return 0;

		Cursor cursor = db.rawQuery("SELECT COUNT(DISTINCT author) FROM summaries", null);
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
	
	public List<String> getSummariesByAuthor(String authorName) {
		open();
		List<String> summaryList = new ArrayList<>();

		if (db == null) {
			return summaryList;
		}

		Cursor cursor = null;
		try {
			cursor = db.rawQuery("SELECT title FROM summaries WHERE author = ? ORDER BY title ASC", 
								 new String[]{authorName});

			while (cursor.moveToNext()) {
				String title = cursor.getString(0);
				summaryList.add(title);
			}
		} catch (Exception e) {
			Log.e("Fetch Error", "Error fetching summaries by author: " + e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return summaryList;
	}
	
	public int getSumrysCountByAuthor(String authorName) {
		open();
		int count = 0;

		if (db == null) {
			return count;
		}

		Cursor cursor = null;
		try {
			cursor = db.rawQuery("SELECT COUNT(*) FROM summaries WHERE author = ?", 
								 new String[]{authorName});

			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
		} catch (Exception e) {
			Log.e("Fetch Error", "Error counting books by author: " + e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return count;
	}
	
	public List<String> getBooksByAuthor(String authorName) {
		open();
		List<String> bookList = new ArrayList<>();

		if (db == null) {
			return bookList;
		}

		Cursor cursor = null;
		try {
			cursor = db.rawQuery("SELECT title FROM summaries WHERE author = ? ORDER BY title ASC", 
								 new String[]{authorName});

			while (cursor.moveToNext()) {
				String title = cursor.getString(0);
				bookList.add(title);
			}
		} catch (Exception e) {
			Log.e("Fetch Error", "Error fetching books by author: " + e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return bookList;
	}
	
	public Cursor getUniqueAuthorsFromSumrys() {
		open();

		if (db == null) {
			return null;
		}

		try {
			return db.rawQuery("SELECT DISTINCT author FROM summaries", null);
		} catch (Exception e) {
			Log.e("DB_ERROR", "Error getting unique authors: " + e.getMessage());
			return null;
		}
	}
	
    public Cursor getSummaryById(int id) {
		open();
		if (db == null) return null;
		return db.rawQuery("SELECT * FROM summaries WHERE id = ?", new String[]{String.valueOf(id)});
	}

    public int updateSummary(ContentValues values, int summaryId) {
        open();
        if (db == null) return 0;
        return db.update("summaries", values, "id = ?", new String[]{String.valueOf(summaryId)});
    }

    public int deleteSummary(int summaryId) {
        open();
        if (db == null) return 0;

        return db.delete("summaries", "id = ?", new String[]{String.valueOf(summaryId)});
    }

	public int setSummaryAsFavorite(int summaryId) {
		open();
		if (db == null) return -1;

		ContentValues values = new ContentValues();
		values.put("favorite", 1);

		try {
			return db.update("summaries", values, "id = ?", new String[]{String.valueOf(summaryId)});
		} catch (Exception e) {
			Log.e("Update Error", "Error updating summary: " + e.getMessage());
			return -1;
		}
	}
	
	public int unSetSummaryAsFavorite(int summaryId) {
		open();
		if (db == null) return -1;

		ContentValues values = new ContentValues();
		values.put("favorite", 0);

		try {
			return db.update("summaries", values, "id = ?", new String[]{String.valueOf(summaryId)});
		} catch (Exception e) {
			Log.e("Update Error", "Error updating summary: " + e.getMessage());
			return -1;
		}
	}
}

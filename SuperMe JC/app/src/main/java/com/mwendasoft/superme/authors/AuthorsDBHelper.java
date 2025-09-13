package com.mwendasoft.superme.authors;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.mwendasoft.superme.core.SuperMeDBHelper;
import java.util.*;
import android.util.*;

public class AuthorsDBHelper {
    private SQLiteDatabase db;
    private SuperMeDBHelper dbHelper;

    public AuthorsDBHelper(Context context) {
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
	
	public long insertAuthor(String name, String category) {
		open();
		if (!dbHelper.doesTableExist("authors")) {
			return -1;
		}
		ContentValues values = new ContentValues();
		values.put("author_name", name);
		values.put("author_categ", category);
		return db.insert("authors", null, values);
	}

    public Cursor getAllAuthors() {
        open();
        if (!dbHelper.doesTableExist("authors")) {
            return null;
        }
        return db.rawQuery("SELECT * FROM authors ORDER BY author_name ASC", null);
    }
	
	public List<String> getAuthorsList() {
		if (db == null || !db.isOpen()) {
			open();
		}

		List<String> authors = new ArrayList<>();
		Cursor cursor = db.rawQuery("SELECT author_name FROM authors ORDER BY author_name ASC", null);

		if (cursor != null && cursor.moveToFirst()) {
			do {
				authors.add(cursor.getString(cursor.getColumnIndexOrThrow("author_name")));
			} while (cursor.moveToNext());
			cursor.close();
		}

		return authors;
	}

    public Cursor getAllAuthorsWithIds() {
        open();
        if (!dbHelper.doesTableExist("authors")) {
            return null;
        }
        return db.rawQuery("SELECT id, author_name FROM authors ORDER BY author_name ASC", null);
    }

    public Cursor getAuthorByName(String name) {
        open();
        if (!dbHelper.doesTableExist("authors")) {
            return null;
        }
        return db.rawQuery("SELECT * FROM authors WHERE author_name = ?", new String[]{name});
    }
	
	public int getAuthorsCount() {
		open();
		if (db == null) return 0;

		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM authors", null);
		int count = 0;

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
			cursor.close();
		}

		return count;
	}
	
    public int updateAuthor(int authorId, ContentValues values) {
        open();
        if (!dbHelper.doesTableExist("authors")) {
            return 0; // Indicate failure
        }
        
        return db.update("authors", values, "id = ?", new String[]{String.valueOf(authorId)});
    }

    public int deleteAuthor(String name) {
        open();
        if (!dbHelper.doesTableExist("authors")) {
            return 0;
        }
        return db.delete("authors", "author_name = ?", new String[]{name});
    }
	
	public int getAuthorIdByName(String authorName) {
        open();
        int authorId = -1;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT id FROM authors WHERE author_name = ?", new String[]{authorName});
            if (cursor != null && cursor.moveToFirst()) {
                authorId = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("QuotesDBHelper", "Error fetching author ID by name: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return authorId;
    }
	
	public String getAuthorNameById(int authorId) {
		open();
		String authorName = "Unknown Int Author";
		Cursor cursor = null;

		try {
			cursor = db.rawQuery("SELECT author_name FROM authors WHERE id = ?", new String[]{String.valueOf(authorId)});
			if (cursor.moveToFirst()) {
				authorName = cursor.getString(0);
			}
		} catch (Exception e) {
			Log.e("Fetch Error", "Error fetching author by int id: " + e.getMessage());
		} finally {
			if (cursor != null) cursor.close();
		}

		return authorName;
	}
}

package com.mwendasoft.superme.quotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mwendasoft.superme.core.SuperMeDBHelper;
import java.util.*;

public class QuotesDBHelper {
    private SQLiteDatabase db;
    private final SuperMeDBHelper dbHelper;

    public QuotesDBHelper(Context context) {
        dbHelper = new SuperMeDBHelper(context);
    }

    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
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

    public long insertQuote(ContentValues values) {
        open();
        return db.insert("quotes", null, values);
    }

    public Cursor getAllQuotes() {
        open();
        return db.rawQuery(
			"SELECT quotes.id, quotes.quote, quotes.author, authors.author_name " +
			"FROM quotes " +
			"JOIN authors ON quotes.author = authors.id " +
			"ORDER BY authors.author_name ASC", null
		);
    }

    public Cursor getQuoteByAuthor(String authorName) {
        open();
        int authorId = getAuthorIdByName(authorName);
        return db.rawQuery("SELECT * FROM quotes WHERE author = ?", new String[]{String.valueOf(authorId)});
    }
	
    public int updateQuote(int quoteId, ContentValues values) {
        open();
        return db.update("quotes", values, "id = ?", new String[]{String.valueOf(quoteId)});
    }

    public int deleteQuote(int quoteId) {
        open();
		return db.delete("quotes", "id = ?", new String[]{String.valueOf(quoteId)});
    }

    public String getAuthorById(int authorId) {
        open();
        String authorName = "Unknown";
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT author_name FROM authors WHERE id = ?", new String[]{String.valueOf(authorId)});
            if (cursor != null && cursor.moveToFirst()) {
                authorName = cursor.getString(0);
            }
        } catch (Exception e) {
            Log.e("QuotesDBHelper", "Error fetching author by ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return authorName;
    }

    private int getAuthorIdByName(String authorName) {
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
	
	public int getQuotesCount() {
		open();
		if (db == null) return 0;

		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM quotes", null);
		int count = 0;

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
			cursor.close();
		}

		return count;
	}
	
	public int getQuotesCountByAuthor(String authorName) {
		open();
		int count = 0;

		if (db == null) {
			return count;
		}

		Cursor cursor = null;
		try {
			// First get the author ID
			int authorId = getAuthorIdByName(authorName);
			if (authorId == -1) {
				return count; // Author not found
			}

			// Then count books by this author
			cursor = db.rawQuery("SELECT COUNT(*) FROM quotes WHERE author = ?", 
								 new String[]{String.valueOf(authorId)});

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
}

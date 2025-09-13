package com.mwendasoft.superme.books;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mwendasoft.superme.core.SuperMeDBHelper;
import java.util.*;

public class BooksDBHelper {
    private SQLiteDatabase db;
    private final SuperMeDBHelper dbHelper;
    private final HashSet<String> existingTables = new HashSet<>();

    public BooksDBHelper(Context context) {
        dbHelper = new SuperMeDBHelper(context);
    }

    public void open() {
        try {
            if (db == null || !db.isOpen()) {
                db = dbHelper.getWritableDatabase();
                Log.d("BooksDBHelper", "Database opened at: " + db.getPath());
            }
        } catch (SQLException e) {
            Log.e("Database Error", "Failed to open database: " + e.getMessage());
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

    public long insertBook(String table, String nullColumnHack, ContentValues values) {
		open();
		if (db == null) return -1;

		try {
			return db.insertOrThrow(table, nullColumnHack, values);
		} catch (Exception e) {
			Log.e("Insert Error", "Error inserting book: " + e.getMessage());
			return -1;
		}
	}

    public Cursor getAllBooks() {
        open();
        if (db == null) return null;

        if (!tableExists("books")) return null;

        try {
            return db.rawQuery("SELECT * FROM books ORDER BY title ASC", null);
        } catch (Exception e) {
            Log.e("Fetch Error", "Error fetching books: " + e.getMessage());
            return null;
        }
    }
	
	public int getBooksCount() {
		open();
		if (db == null) return 0;

		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM books", null);
		int count = 0;

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
			cursor.close();
		}

		return count;
	}
	
	public List<String> getBooksByAuthor(String authorName) {
		open();
		List<String> bookList = new ArrayList<>();

		if (db == null || !tableExists("books") || !tableExists("authors")) {
			return bookList;
		}

		Cursor cursor = null;
		try {
			// First get the author ID
			int authorId = getAuthorIdByName(authorName);
			if (authorId == -1) {
				return bookList; // Author not found
			}

			// Then get all books by this author
			cursor = db.rawQuery("SELECT title FROM books WHERE author = ? ORDER BY title ASC", 
								 new String[]{String.valueOf(authorId)});

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
	
	public int getBooksCountByAuthor(String authorName) {
		open();
		int count = 0;

		if (db == null || !tableExists("books") || !tableExists("authors")) {
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
			cursor = db.rawQuery("SELECT COUNT(*) FROM books WHERE author = ?", 
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
	
	public Cursor getUniqueAuthorsFromBooks() {
		open();

		if (db == null) {
			return null;
		}

		try {
			return db.rawQuery("SELECT DISTINCT author FROM books", null);
		} catch (Exception e) {
			Log.e("DB_ERROR", "Error getting unique authors: " + e.getMessage());
			return null;
		}
	}
	
    public int updateBook(String tableName, int id, ContentValues values) {
		open();
		if (db == null) return -1;

		try {
			return db.update(tableName, values, "id = ?", new String[]{String.valueOf(id)});
		} catch (Exception e) {
			Log.e("Update Error", "Error updating book: " + e.getMessage());
			return -1;
		}
	}

    public int deleteBook(String title, String authorName) {
		open();
		if (db == null) return -1;

		int authorId = getAuthorIdByName(authorName);  // Convert name to ID
		if (authorId == -1) return -1; // Not found

		try {
			return db.delete("books", "title = ? AND author = ?", new String[]{title, String.valueOf(authorId)});
		} catch (Exception e) {
			Log.e("Delete Error", "Error deleting book: " + e.getMessage());
			return -1;
		}
	}
	
	public int markBookAsRead(String title, String authorName) {
		open();
		if (db == null) return -1;
		
		int authorId = getAuthorIdByName(authorName);  // Convert name to ID
		if (authorId == -1) return -1; // Not found
		
		ContentValues values = new ContentValues();
		values.put("read", 1);

		try {
			return db.update("books", values, "title = ? AND author = ?", new String[]{title, String.valueOf(authorId)});
		} catch (Exception e) {
			Log.e("Update Error", "Error updating book: " + e.getMessage());
			return -1;
		}
	}
	
	public List<String> getFormattedBookList() {
		open();
		List<String> bookList = new ArrayList<>();

		if (db == null || !tableExists("books")) return bookList;

		Cursor cursor = null;
		try {
			String title, authorName;
			cursor = db.rawQuery("SELECT title, author FROM books ORDER BY title ASC", null);
			
			while (cursor.moveToNext()) {
				title  = cursor.getString(0);
				int authorId = cursor.getInt(1);
				authorName = getBookAuthorById(authorId);
				bookList.add(title + " by " + authorName);
			}
		} catch (Exception e) {
			Log.e("Fetch Error", "Error fetching formatted book list: " + e.getMessage());
		} finally {
			if (cursor != null) cursor.close();
		}

		return bookList;
	}
	
    public Cursor getBookByTitleAndAuthor(String title, String authorName) {
		open();
		if (db == null || !tableExists("books")) return null;

		try {
			int authorId = getAuthorIdByName(authorName);
			if (authorId == -1) return null; // Author not found

			return db.rawQuery("SELECT * FROM books WHERE title = ? AND author = ?", 
							   new String[]{title, String.valueOf(authorId)});
		} catch (Exception e) {
			Log.e("Fetch Error", "Error fetching book: " + e.getMessage());
			return null;
		}
	}
	
	public Cursor getBookId(String title, String authorName) {
		open();
		if (db == null || !tableExists("books")) return null;

		try {
			int authorId = getAuthorIdByName(authorName);
			if (authorId == -1) return null; // Author not found

			return db.rawQuery("SELECT id FROM books WHERE title = ? AND author = ?", 
							   new String[]{title, String.valueOf(authorId)});
		} catch (Exception e) {
			Log.e("Fetch Error", "Error fetching book: " + e.getMessage());
			return null;
		}
	}
	
	public Cursor getBookById(int bookId) {
		open();
		if (db == null || !tableExists("books")) return null;

		try {
			return db.rawQuery("SELECT * FROM books WHERE id = ?", new String[]{String.valueOf(bookId)});
		} catch (Exception e) {
			Log.e("Fetch Error", "Error fetching book: " + e.getMessage());
			return null;
		}
	}

    private boolean tableExists(String tableName) {
        if (existingTables.contains(tableName)) return true;

        open();
        if (db == null) return false;

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
            boolean exists = cursor.getCount() > 0;
            if (exists) existingTables.add(tableName);
            return exists;
        } catch (Exception e) {
            Log.e("Table Check Error", "Error checking table existence: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }
	
	public String getBookAuthorById(int authorId) {
		open();
		if (db == null || !tableExists("authors")) return null;

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
	
	private int getAuthorIdByName(String author) {
		open();
		if (db == null || !tableExists("authors")) return -1;

		int authorId = -1;
		Cursor cursor = null;

		try {
			cursor = db.rawQuery("SELECT id FROM authors WHERE author_name = ?", new String[]{author});
			if (cursor.moveToFirst()) {
				authorId = cursor.getInt(0);
			}
		} catch (Exception e) {
			Log.e("Fetch Error", "Error fetching author by name: " + e.getMessage());
		} finally {
			if (cursor != null) cursor.close();
		}

		return authorId;
	}
}

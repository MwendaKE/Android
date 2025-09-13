package com.mwendasoft.superme.categories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.mwendasoft.superme.core.SuperMeDBHelper;
import android.util.*;

public class CategoriesDBHelper {
    private SQLiteDatabase db;
    private SuperMeDBHelper dbHelper;

    public CategoriesDBHelper(Context context) {
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

    public long insertCategory(String name) {
        open();
        if (!dbHelper.doesTableExist("categories")) {
            return -1;
        }
        ContentValues values = new ContentValues();
        values.put("category", name);
        return db.insert("categories", null, values);
    }

    public Cursor getAllCategories() {
        open();
        if (!dbHelper.doesTableExist("categories")) {
            return null;
        }
        return db.rawQuery("SELECT * FROM categories ORDER BY category ASC", null);
    }
	
	public int getCategoriesCount() {
		open();
		if (db == null) return 0;

		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM categories", null);
		int count = 0;

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
			cursor.close();
		}

		return count;
	}
	
    public Cursor getAllCategoriesWithIds() {
        open();
        if (!dbHelper.doesTableExist("categories")) {
            return null;
        }
        return db.rawQuery("SELECT id, category FROM categories ORDER BY category ASC", null);
    }

    public Cursor getCategoryById(int id) {
		open();
		if (!dbHelper.doesTableExist("categories")) {
			return null;
		}
		return db.rawQuery("SELECT category FROM categories WHERE id = ?", new String[]{String.valueOf(id)});
	}
	
	public String getCategoryByIdAdvanced(int id) {
		open();
		if (!dbHelper.doesTableExist("categories")) {
			return null;
		}

		Cursor cursor = db.rawQuery("SELECT category FROM categories WHERE id = ?", new String[]{String.valueOf(id)});
		String category = null;

		if (cursor != null && cursor.moveToFirst()) {
			category = cursor.getString(0); // or use cursor.getColumnIndex("category")
			cursor.close();
		}

		return category;
	}
	
	public Cursor getCategoryByName(String name) {
        open();
        if (!dbHelper.doesTableExist("categories")) {
            return null;
        }
        return db.rawQuery("SELECT * FROM categories WHERE category = ?", new String[]{name});
    }

    public int updateCategory(int categId, ContentValues values) {
        open();
        if (!dbHelper.doesTableExist("categories")) {
            return 0;
        }
        
        return db.update("categories", values, "id = ?", new String[]{String.valueOf(categId)});
    }

    public int deleteCategory(String name) {
        open();
        if (!dbHelper.doesTableExist("categories")) {
            return 0;
        }
        return db.delete("categories", "category = ?", new String[]{name});
    }
	
	public int getCategoryIdByName(String categName) {
        open();
        int categId = -1;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT id FROM categories WHERE category = ?", new String[]{categName});
            if (cursor != null && cursor.moveToFirst()) {
                categId = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("CategoryDBHelper", "Error fetching category ID by name: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return categId;
    }
	
	public String getCategoryNameById(int categId) {
		open();
		String categName = "Unknown Category";
		Cursor cursor = null;

		try {
			cursor = db.rawQuery("SELECT category FROM categories WHERE id = ?", new String[]{String.valueOf(categId)});
			if (cursor.moveToFirst()) {
				categName = cursor.getString(0);
			}
		} catch (Exception e) {
			Log.e("Fetch Error", "Error fetching author by int id: " + e.getMessage());
		} finally {
			if (cursor != null) cursor.close();
		}

		return categName;
	}
}

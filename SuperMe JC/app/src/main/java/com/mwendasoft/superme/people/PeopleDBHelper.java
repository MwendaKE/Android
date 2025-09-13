package com.mwendasoft.superme.people;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.mwendasoft.superme.core.SuperMeDBHelper;
import android.util.*;

public class PeopleDBHelper {
    private SQLiteDatabase db;
    private final SuperMeDBHelper dbHelper;

    public PeopleDBHelper(Context context) {
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
	
	public SQLiteDatabase getDatabase() {
        return dbHelper.getReadableDatabase();
    }

    public long insertPerson(ContentValues values) {
        open();
        if (db == null) return -1;
        return db.insert("people", null, values);
    }

    public Cursor getAllPersons() {
        open();
        if (db == null) return null;
        return db.rawQuery("SELECT * FROM people ORDER BY name ASC", null);
    }

	public int getPeopleCount() {
		open();
		if (db == null) return 0;

		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM people", null);
		int count = 0;

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
			cursor.close();
		}

		return count;
	}

    public Cursor getPersonById(int id) {
		open();
		if (db == null) return null;
		return db.rawQuery("SELECT * FROM people WHERE id = ?", new String[]{String.valueOf(id)});
	}

    public int updatePerson(ContentValues values, int personId) {
        open();
        if (db == null) return 0;
        return db.update("people", values, "id = ?", new String[]{String.valueOf(personId)});
    }

    public int deletePeople(int personId) {
        open();
        if (db == null) return 0;

        return db.delete("people", "id = ?", new String[]{String.valueOf(personId)});
    }
}

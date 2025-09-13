package com.mwendasoft.superme.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SuperMeDBTransfer {
    private static final String TAG = "SuperMeDBTransfer";
    private static final String DB_NAME = "SuperMeDB.db";
    private static final String OLD_DB_PATH = "/sdcard/Documents/Data/SuperMe.db";
    private final String newDbPath;
    private final Context context;

    public SuperMeDBTransfer(Context context) {
        this.context = context;
        File extDir = context.getExternalFilesDir(null);
        if (extDir != null) {
            newDbPath = extDir.getAbsolutePath() + File.separator + DB_NAME;
        } else {
            newDbPath = context.getFilesDir().getAbsolutePath() + File.separator + DB_NAME;
        }
    }

    private void showErrorDialog(final Activity activity, final String message) {
        if (activity == null || activity.isFinishing()) {
            Log.e(TAG, "Activity is null or finishing, cannot show dialog");
            return;
        }
        activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						new AlertDialog.Builder(activity)
							.setTitle("Error")
							.setMessage(message)
							.setPositiveButton("OK", null)
							.show();
					} catch (Exception e) {
						Log.e(TAG, "Failed to show error dialog", e);
					}
				}
			});
    }

    public void checkAndMoveDatabase(Activity activity) {
		if (activity == null) {
			Log.e(TAG, "Activity is null");
			return;
		}

		File oldDb = new File(OLD_DB_PATH);
		File newDb = new File(newDbPath);

		if (!oldDb.exists()) {
			Log.e(TAG, "Source database not found: " + OLD_DB_PATH);
			showErrorDialog(activity, "Source database not found.");
			return;
		}

		// Clean up existing database files
		if (newDb.exists()) {
			SQLiteDatabase db = null;
			try {
				db = SQLiteDatabase.openDatabase(newDbPath, null, SQLiteDatabase.OPEN_READWRITE);
				if (db != null) {
					db.close();
				}
				if (!newDb.delete()) {
					Log.e(TAG, "Failed to delete existing database");
					showErrorDialog(activity, "Failed to replace existing database.");
					return;
				}
			} catch (Exception e) {
				Log.e(TAG, "Error handling existing database", e);
			} finally {
				if (db != null && db.isOpen()) {
					db.close();
				}
			}
		}

		// Delete temporary files
		new File(newDbPath + "-shm").delete();
		new File(newDbPath + "-wal").delete();
		new File(newDbPath + "-journal").delete();

		// Perform file copy (simplified version)
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			inputStream = new FileInputStream(oldDb);
			outputStream = new FileOutputStream(newDb);

			byte[] buffer = new byte[1024];
			int length;

			while ((length = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}

			Log.i(TAG, "Database moved successfully");

			if (!oldDb.delete()) {
				Log.w(TAG, "Couldn't delete old database");
			}
		} catch (IOException e) {
			Log.e(TAG, "Database transfer failed", e);
			showErrorDialog(activity, "Database transfer failed:\n" + e.getMessage());
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				Log.e(TAG, "Error closing streams", e);
			}
		}
	}

    public void transferDataIfEmpty(Activity activity) {
        if (activity == null) {
            Log.e(TAG, "Activity is null");
            return;
        }

        File newDbFile = new File(newDbPath);
        if (!newDbFile.exists()) {
            Log.e(TAG, "Target database missing");
            showErrorDialog(activity, "Database not found. Please restart.");
            return;
        }

        SQLiteDatabase oldDatabase = null;
        SQLiteDatabase newDatabase = null;
        try {
            oldDatabase = SQLiteDatabase.openDatabase(newDbPath, null, SQLiteDatabase.OPEN_READWRITE);
            newDatabase = SQLiteDatabase.openDatabase(
                context.getDatabasePath(DB_NAME).getPath(),
                null,
                SQLiteDatabase.OPEN_READWRITE);

            String[][] tables = {
                {"books_new", "books", "title,author,review,category,read", "title,author,review,category,read"},
                {"clips", "clips", "clip,writer,source_title", "clip,writer,source"},
                {"tasks_new", "tasks", "title,sdate,stime,edate,duration,category,success,description", "title,sdate,stime,edate,duration,category,success,description"},
                //{"events", "events", "title,date,time,notes,address,budget,attended", "title,date,time,notes,address,budget,attended"},
                //{"diaries", "diaries", "title,date,time,mood,description", "title,date,time,mood,description"},
				{"events", "events", "event_name,event_date,event_time,event_notes,address,budget,attended", "title,date,time,notes,address,budget,attended"},
                {"diaries", "diaries", "title, entry_date, entry_time, mood, description", "title, date, time, mood, description"},
				{"quotes_new", "quotes", "author, quote", "author, quote"},
				{"songs_new", "songs", "title,artist,lyrics,genre", "title,artist,lyrics,genre"},
                {"poems_new", "poems", "title,poem,poet", "title,poem,poet"},
                {"authors_new", "authors", "author_name,author_categ", "author_name,author_categ"},
                {"categories_new", "categories", "category", "category"},
                {"summaries", "summaries", "title,author,favorite,summary", "title,author,favorite,summary"}
            };

            for (String[] table : tables) {
                transferTableIfEmpty(oldDatabase, newDatabase, 
									 table[0], table[1], 
									 table[2].split(","), table[3].split(","));
            }

        } catch (Exception e) {
            Log.e(TAG, "Data transfer failed", e);
            showErrorDialog(activity, "Data transfer failed: " + e.getMessage());
        } finally {
            if (oldDatabase != null && oldDatabase.isOpen()) {
                oldDatabase.close();
            }
            if (newDatabase != null && newDatabase.isOpen()) {
                newDatabase.close();
            }
        }
    }

    private void transferTableIfEmpty(SQLiteDatabase oldDb, SQLiteDatabase newDb, 
									  String oldTable, String newTable,
									  String[] oldCols, String[] newCols) {
        if (oldDb == null || newDb == null) {
            Log.e(TAG, "Databases are null");
            return;
        }

        try {
            // Verify target table exists
            if (!tableExists(newDb, newTable)) {
                Log.w(TAG, "Skipping " + newTable + " - table missing");
                return;
            }

            // Skip if target table has data
            if (hasData(newDb, newTable)) return;

            // Verify source table exists
            if (!tableExists(oldDb, oldTable)) {
                Log.w(TAG, "Source table missing: " + oldTable);
                return;
            }

            // Build column list
            String columns = String.join(",", oldCols);

            // Perform data transfer
            try (Cursor cursor = oldDb.rawQuery("SELECT " + columns + " FROM " + oldTable, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        ContentValues values = new ContentValues();
                        for (int i = 0; i < oldCols.length && i < newCols.length; i++) {
                            addValueToContentValues(cursor, values, i, newCols[i]);
                        }
                        newDb.insert(newTable, null, values);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Transfer failed for " + newTable, e);
        }
    }

    private boolean tableExists(SQLiteDatabase db, String table) {
        if (db == null) return false;
        try (Cursor c = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?", 
            new String[]{table})) {
            return c != null && c.getCount() > 0;
        }
    }

    private boolean hasData(SQLiteDatabase db, String table) {
        if (db == null) return false;
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + table, null)) {
            return c != null && c.moveToFirst() && c.getInt(0) > 0;
        }
    }

    private void addValueToContentValues(Cursor cursor, ContentValues values, int index, String key) {
        if (cursor == null || values == null || key == null) return;
        try {
            switch (cursor.getType(index)) {
                case Cursor.FIELD_TYPE_STRING:
                    values.put(key, cursor.getString(index));
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    values.put(key, cursor.getInt(index));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    values.put(key, cursor.getFloat(index));
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    values.put(key, cursor.getBlob(index));
                    break;
                case Cursor.FIELD_TYPE_NULL:
                    values.putNull(key);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding value to ContentValues", e);
        }
    }
}

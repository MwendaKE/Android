package com.mwendasoft.superme.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.mwendasoft.superme.core.SuperMeDBHelper;
import android.util.*;
import java.util.*;

public class TasksDBHelper {
    private SQLiteDatabase db;
    private final SuperMeDBHelper dbHelper;

    public TasksDBHelper(Context context) {
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

    public long insertTask(ContentValues values) {
        open();
        if (db == null) return -1;
        return db.insert("tasks", null, values);
    }
	
	public int insertTaskMediaPaths(int taskId, List<String> paths) {
		open();
		if (db == null) return -1;

		ContentValues values = new ContentValues();
		values.put("task_id", taskId);
		
		for (String path : paths) {
			values.put("file_path", path);
			db.insert("tasks_media", null, values);
		}

		return 1;
	}
	
	public List<String> getMediaPaths(int taskId) {
		open();
		List<String> mediaPaths = new ArrayList<>();

		Cursor cursor = db.query(
			"tasks_media",                          // Table name
			new String[]{"file_path"},            // Columns to return
			"task_id = ?",                        // WHERE clause
			new String[]{String.valueOf(taskId)}, // WHERE args
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

    public Cursor getTaskTitles() {
        open();
        if (db == null) return null;
        return db.rawQuery("SELECT title FROM tasks", null);
    }

    public Cursor getAllTasks() {
        open();
        if (db == null) return null;
        return db.rawQuery("SELECT * FROM tasks ORDER BY edate DESC", null);
    }

    public Cursor getTaskByTitle(String title) {
        open();
        if (db == null) return null;
        return db.rawQuery("SELECT * FROM tasks WHERE title = ?", new String[]{title});
    }
	
	public Cursor getTaskById(int id) {
		open();
		if (db == null) return null;
		return db.rawQuery("SELECT * FROM tasks WHERE id = ?", new String[]{String.valueOf(id)});
	}
	
	public Cursor getAllTasksWithReminders() {
		open();
		return db.query(
			"tasks", 
			null, 
			"reminder_set = 1",  // Only items with reminders
			null, null, null, null
		);
	}
	
    public int updateTask(ContentValues values, int taskId) {
        open();
        if (db == null) return 0;
        return db.update("tasks", values, "id = ?", new String[]{String.valueOf(taskId)});
    }
	
    public int deleteTask(int taskId) {
        open();
        if (db == null) return 0;

        return db.delete("tasks", "id = ?", new String[]{String.valueOf(taskId)});
    }
	
	public int markTaskAsCompleted(int taskId) {
		open();
		if (db == null) return -1;

		ContentValues values = new ContentValues();
		values.put("success", 1);

		try {
			return db.update("tasks", values, "id = ?", new String[]{String.valueOf(taskId)});
		} catch (Exception e) {
			Log.e("Update Error", "Error updating task: " + e.getMessage());
			return -1;
		}
	}
	
	public int getTasksCount() {
		open();
		if (db == null) return 0;

		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM tasks", null);
		int count = 0;

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
			cursor.close();
		}

		return count;
	}
	
	public int getTaskIdByTitle(String title) {
        open();
        int taskId = -1;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT id FROM tasks WHERE title = ?", new String[]{title});
            if (cursor != null && cursor.moveToFirst()) {
                taskId = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("TasksDbBHelper", "Error fetching task ID by name: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return taskId;
    }
}

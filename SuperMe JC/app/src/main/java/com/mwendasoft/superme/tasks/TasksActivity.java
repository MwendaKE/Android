package com.mwendasoft.superme.tasks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.BaseActivity;
import android.app.*;
import android.database.*;
import android.content.*;
import android.widget.*;
import android.view.*;
import com.mwendasoft.superme.events.*;
import java.io.*;
import android.os.*;
import com.mwendasoft.superme.reminders.*;
import com.mwendasoft.superme.helpers.*;

public class TasksActivity extends BaseActivity {
    private TasksDBHelper dbHelper;
    private ListView tasksListView;
	private TextView tasksListTitle, tasksCountBadge;
	private ImageButton addTaskFab;
	
	private TasksViewAdapter tasksAdapter;
    private ArrayList<Task> tasks;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tasks_activity);
		
		tasksListTitle = findViewById(R.id.tasksListTitle);
		tasksCountBadge = findViewById(R.id.tasksCountBadge);
	    tasksListView = findViewById(R.id.tasksListView);
		addTaskFab = findViewById(R.id.addTaskFab);
		
		registerForContextMenu(tasksListView);
		
		tasks = new ArrayList<>(); // Initialize list
		dbHelper = new TasksDBHelper(this);
		
		tasksAdapter = new TasksViewAdapter(this, tasks);
		tasksListView.setAdapter(tasksAdapter);
	
		loadTasks();

		tasksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Task selectedTask = tasks.get(position);
				openTaskDetail(selectedTask);
			}
		});
		
		addTaskFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TasksActivity.this, TaskAddActivity.class);
				startActivity(intent);
			}
		});
	}
	
	// ==== CONTEXT MENU METHODS (CMMs) === //

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.tasksListView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		    Task selectedTask = tasks.get(info.position);
			menu.setHeaderTitle(selectedTask.getTitle());

			menu.add(Menu.NONE, 1, 1, "Open");
			menu.add(Menu.NONE, 2, 2, "Edit");
			menu.add(Menu.NONE, 3, 3, "Mark Completed");
			menu.add(Menu.NONE, 4, 4, "Delete");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Task selectedTask = tasks.get(info.position);

		switch (item.getItemId()) {
			case 1: // Open
				openTaskDetail(selectedTask);
				return true;
			case 4: // Delete
				confirmAndDeleteTask(selectedTask);
				return true;
			case 3: // Mark Read
				markTaskAsCompleted(selectedTask);
				return true;
			case 2: // Edit
				editTask(selectedTask);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	private void confirmAndDeleteTask(final Task selectedTask) {
		new AlertDialog.Builder(this)
			.setTitle("Delete Task")
			.setMessage("Are you sure you want to delete \"" + selectedTask.getTitle() + "\"?")
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//Step 1: Cancel scheduled notification first
					ReminderScheduler.cancelReminders(TasksActivity.this, 
					      ReminderScheduler.TYPE_TASK,
						  selectedTask.getId(),
						  ConvertTimeToMillis.convertToMillis(selectedTask.getEdate())
				    );
					
					// Step 2: Delete the media folder e.g., ".superme/notes/Note 15"
					File diaryFolder = new File(Environment.getExternalStorageDirectory(), ".superme/tasks/Task " + selectedTask.getId());
					deleteFolderRecursive(diaryFolder);

					// Step 3: Delete any individual media files as safety
					List<String> allMediaPaths = dbHelper.getMediaPaths(selectedTask.getId());
					for (String path : allMediaPaths) {
						File file = new File(path);
						if (file.exists()) {
							file.delete();
						}
					}
					
					dbHelper.deleteTask(selectedTask.getId());
					tasks.remove(selectedTask);
					loadTasks();
					tasksAdapter.notifyDataSetChanged();
				}
			})
			.setNegativeButton("No", null)
			.show();
	}
	
	private void deleteFolderRecursive(File folder) {
		if (folder.exists()) {
			File[] files = folder.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						deleteFolderRecursive(file);
					} else {
						file.delete();
					}
				}
			}
			folder.delete(); // delete the folder itself
		}
	}

	private void markTaskAsCompleted(Task task) {
		dbHelper.markTaskAsCompleted(task.getId());
		loadTasks();
		ToastMessagesManager.show(this, "\"" + task.getTitle() + "\" marked as completed.");
	}

	private void editTask(Task task) {
		Intent intent = new Intent(TasksActivity.this, TaskEditActivity.class);
		intent.putExtra("selectedTask", task);
		startActivity(intent);
	}

	// == CMMs == //

    private void loadTasks() {
		tasks.clear(); // Avoid duplicates

		Cursor cursor = dbHelper.getAllTasks();

		if (cursor != null) {
			int idIndex = cursor.getColumnIndexOrThrow("id");
            int titleIndex = cursor.getColumnIndexOrThrow("title");
			int sdateIndex = cursor.getColumnIndexOrThrow("sdate");
			int stimeIndex = cursor.getColumnIndexOrThrow("stime");
			int durationIndex = cursor.getColumnIndexOrThrow("duration");
			int edateIndex = cursor.getColumnIndexOrThrow("edate");
			int successIndex = cursor.getColumnIndexOrThrow("success");
			int categoryIndex = cursor.getColumnIndexOrThrow("category");
			int descriptionIndex = cursor.getColumnIndexOrThrow("description");
			
            while (cursor.moveToNext()) {
				int id = cursor.getInt(idIndex);
                String title = cursor.getString(titleIndex);
				String sdate = cursor.getString(sdateIndex);
				String stime = cursor.getString(stimeIndex);
				int duration = cursor.getInt(durationIndex);
				String edate = cursor.getString(edateIndex);
				int success = cursor.getInt(successIndex);
				int category = cursor.getInt(categoryIndex);
				String description = cursor.getString(descriptionIndex);
				
                if (title != null && !title.isEmpty()) {
                    tasks.add(new Task(id, title, sdate, stime, duration, edate, category, success, description));
                }
            }
            cursor.close();
        }

		// SET TITLE AND COUNT //

		tasksListTitle.setText(R.string.tasks);
		tasksCountBadge.setText(String.valueOf(tasks.size()));

        if (tasks.isEmpty()) {
			new AlertDialog.Builder(this)
				.setTitle(R.string.diary)
				.setMessage("No tasks found. Would you like to add a new one?")
				.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						 Intent intent = new Intent(TasksActivity.this, TaskAddActivity.class);
						 startActivity(intent);
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})
				.show();
		} else {
			tasksAdapter.notifyDataSetChanged();
		}
	}

    private void openTaskDetail(Task task) {
		Intent intent = new Intent(this, TaskDetailActivity.class);
		intent.putExtra("selectedTask", task);
		startActivity(intent);
	}

	@Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
	
	@Override
	protected void onResume() { // Updates activity when it comes to view again.
		super.onResume();
		loadTasks();
	}
}

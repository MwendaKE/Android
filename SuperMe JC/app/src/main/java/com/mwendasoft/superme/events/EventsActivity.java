package com.mwendasoft.superme.events;

import android.app.AlertDialog; 
import android.content.DialogInterface; 
import android.content.Intent; 
import android.database.Cursor; 
import android.os.Bundle; 
import android.view.View; 
import android.widget.AdapterView; 
import android.widget.ListView;
import android.widget.TextView; 
import java.util.ArrayList;
import com.mwendasoft.superme.R; 
import com.mwendasoft.superme.BaseActivity;
import android.widget.*;
import android.view.*;
import android.app.*;
import android.content.*;
import java.io.*;
import android.os.*;
import java.util.*;
import com.mwendasoft.superme.reminders.*;

public class EventsActivity extends BaseActivity {
    // Constants for context menu items
    private static final int MENU_OPEN = 1;
    private static final int MENU_EDIT = 2;
    private static final int MENU_MARK_ATTENDED = 3;
    private static final int MENU_DELETE = 4;

    private EventsDBHelper dbHelper; 
    private ListView eventsListView; 
    private ImageButton addEventFab;
    private TextView eventsListTitle, eventsCountBadge; 

    private EventsViewAdapter eventsAdapter;
    private final ArrayList<Event> events = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_activity);

        initializeViews();
        setupDatabaseHelper();
        setupAdapter();
        setupListeners();

        loadEvents();
    }

    private void initializeViews() {
        eventsListView = findViewById(R.id.eventsListView);
        eventsListTitle = findViewById(R.id.eventsListTitle);
        eventsCountBadge = findViewById(R.id.eventsCountBadge);
        addEventFab = findViewById(R.id.addEventFab);

        registerForContextMenu(eventsListView);
    }

    private void setupDatabaseHelper() {
        dbHelper = new EventsDBHelper(this);
    }

    private void setupAdapter() {
        eventsAdapter = new EventsViewAdapter(this, events);
        eventsListView.setAdapter(eventsAdapter);
    }

    private void setupListeners() {
        eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Event selectedEvent = events.get(position);
					openEventView(selectedEvent);
				}
			});

        addEventFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(EventsActivity.this, EventAddActivity.class));
				}
			});
    }

    // ==== CONTEXT MENU METHODS === //

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.eventsListView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Event selectedEvent = events.get(info.position);
            menu.setHeaderTitle(selectedEvent.getTitle());

            menu.add(Menu.NONE, MENU_OPEN, 1, "Open");
            menu.add(Menu.NONE, MENU_EDIT, 2, "Edit");
            menu.add(Menu.NONE, MENU_MARK_ATTENDED, 3, "Mark Attended");
            menu.add(Menu.NONE, MENU_DELETE, 4, "Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) return super.onContextItemSelected(item);

        Event selectedEvent = events.get(info.position);

        switch (item.getItemId()) {
            case MENU_OPEN:
                openEventView(selectedEvent);
                return true;
            case MENU_DELETE:
                confirmAndDeleteEvent(selectedEvent);
                return true;
            case MENU_MARK_ATTENDED:
                markEventAsAttended(selectedEvent);
                return true;
            case MENU_EDIT:
                editEvent(selectedEvent);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void confirmAndDeleteEvent(final Event selectedEvent) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete \"" + selectedEvent.getTitle() + "\"?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cancelEventNotification(selectedEvent);
					deleteEventMedia(selectedEvent);
                    deleteEventFromDatabase(selectedEvent);
                }
            })
            .setNegativeButton("No", null)
            .show();
    }

    private void cancelEventNotification(Event event) {
		String eventDateTime = event.getDate() + " " + event.getTime();
		
        ReminderScheduler.cancelReminders(EventsActivity.this, 
										  ReminderScheduler.TYPE_EVENT,
										  event.getId(),
										  ConvertTimeToMillis.convertToMillis(eventDateTime)
										  );
	}

    private void deleteEventFromDatabase(Event event) {
        dbHelper.deleteEvent(event.getId());
        events.remove(event);
        if (eventsAdapter != null) {
            eventsAdapter.notifyDataSetChanged();
        }
        updateEventCount();
    }
	
	private void deleteEventMedia(Event event) {
		File eventFolder = new File(Environment.getExternalStorageDirectory(), ".superme/events/Event " + event.getId());
		deleteFolderRecursive(eventFolder);
		List<String> allMediaPaths = dbHelper.getAllMediaPaths(event.getId());
		for (String path : allMediaPaths) {
			File file = new File(path);
			if (file.exists()) {
				file.delete();
			}
		}
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

    private void markEventAsAttended(Event event) {
        dbHelper.markEventAsAttended(event.getId());
        Toast.makeText(this, "\"" + event.getTitle() + "\" marked as attended.", Toast.LENGTH_SHORT).show();
        loadEvents(); // Refresh the list to show updated status
    }

    private void editEvent(Event event) {
        Intent intent = new Intent(EventsActivity.this, EventEditActivity.class);
        intent.putExtra("selectedEvent", event);
        startActivity(intent);
    }

    private void loadEvents() {
        events.clear();

        Cursor cursor = null;
        try {
            cursor = dbHelper.getAllEvents();
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow("id");
                int titleIndex = cursor.getColumnIndexOrThrow("title");
                int dateIndex = cursor.getColumnIndexOrThrow("date");
                int timeIndex = cursor.getColumnIndexOrThrow("time");
                int notesIndex = cursor.getColumnIndexOrThrow("notes");
                int addressIndex = cursor.getColumnIndexOrThrow("address");
                int budgetIndex = cursor.getColumnIndexOrThrow("budget");
                int attendedIndex = cursor.getColumnIndexOrThrow("attended");

                do {
                    int id = cursor.getInt(idIndex);
                    String title = cursor.getString(titleIndex);
                    String date = cursor.getString(dateIndex);
                    String time = cursor.getString(timeIndex);
                    String notes = cursor.getString(notesIndex);
                    String address = cursor.getString(addressIndex);
                    float budget = cursor.getFloat(budgetIndex);
                    int attended = cursor.getInt(attendedIndex);

                    if (title != null && !title.isEmpty()) {
                        events.add(new Event(id, title, date, time, notes, address, budget, attended));
                    }
                } while (cursor.moveToNext());
            }

            updateUIAfterLoading();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void updateUIAfterLoading() {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					eventsListTitle.setText(R.string.events);
					updateEventCount();

					if (events.isEmpty()) {
						showNoEventsDialog();
					} else if (eventsAdapter != null) {
						eventsAdapter.notifyDataSetChanged();
					}
				}
			});
    }

    private void updateEventCount() {
        eventsCountBadge.setText(String.valueOf(events.size()));
    }

    private void showNoEventsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.events)
            .setMessage("No events found. Would you like to add a new one?")
            .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(EventsActivity.this, EventAddActivity.class));
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            })
            .show();
    }

    private void openEventView(Event event) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("selectedEvent", event);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }
}

package com.mwendasoft.superme.events;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.BaseActivity;
import com.mwendasoft.superme.authors.AuthorsDBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.*;
import android.content.*;
import android.widget.*;
import android.util.*;
import android.content.res.*;
import java.util.*;
import java.util.Calendar;
import com.mwendasoft.superme.core.*;
import android.net.*;
import java.io.*;
import android.os.*;
import android.nfc.*;
import com.mwendasoft.superme.reminders.*;
import android.icu.text.*;
import android.icu.util.TimeZone;
import com.mwendasoft.superme.helpers.*;

public class EventAddActivity extends BaseActivity {
    private EditText eventTitleAdd, eventDateAdd, eventTimeAdd, eventAddressAdd, eventBudgetAdd, eventNotesAdd;
    private EditText eventImageAdd, eventVideoAdd, eventAudioAdd;
	private Spinner eventAttendanceAddSpinner;
	private Button eventSaveAddBtn;
    private EventsDBHelper eventsDBHelper;

	private HashMap<String, Integer> attendanceMap = new HashMap<>();
	private ArrayList<String> attendanceList= new ArrayList<>();
    private ArrayAdapter<String> attendanceAdapter;
	
	private List<Uri> imageUris = new ArrayList<>();
	private List<Uri> audioUris = new ArrayList<>();
	private List<Uri> videoUris = new ArrayList<>();

	private static final int PICK_IMAGE = 1001;
	private static final int PICK_AUDIO = 1002;
	private static final int PICK_VIDEO = 1003;
	
	private int selectedAttendanceId;

	private String TAG = "EventAddActivity";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_add_activity);

		eventsDBHelper = new EventsDBHelper(this);
		attendanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, attendanceList);
		
		selectedAttendanceId = 2;
		
		initializeViews();
		populateAttendedSpinner();
		
		imageUris.clear();
        audioUris.clear();
        videoUris.clear();
		
        eventAttendanceAddSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedAttendance = parent.getItemAtPosition(position).toString();
					selectedAttendanceId = attendanceMap.getOrDefault(selectedAttendance, 2);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});

        eventDateAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDatePicker();
				}
			});

		eventTimeAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showTimePicker();
				}
			});

        eventSaveAddBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					saveEvent();
				}
			});

		eventImageAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFiles("image/*", PICK_IMAGE);
				}
			});

		eventVideoAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFiles("video/*", PICK_VIDEO);
				}
			});

		eventAudioAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFiles("audio/*", PICK_AUDIO);
				}
			});
    }
	
	private void pickFiles(String type, int requestCode) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType(type);
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		startActivityForResult(Intent.createChooser(intent, "Select Files"), requestCode);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK && data != null) {
			int count = 0;

			if (data.getClipData() != null) {
				count = data.getClipData().getItemCount();
				for (int i = 0; i < count; i++) {
					Uri uri = data.getClipData().getItemAt(i).getUri();
					addUriToList(uri, requestCode);
				}
			} else if (data.getData() != null) {
				count = 1;
				addUriToList(data.getData(), requestCode);
			}

			// Update button text based on requestCode
			switch (requestCode) {
				case PICK_IMAGE: // Example: IMAGES
					eventImageAdd.setText(count + " images selected");
					break;
				case PICK_VIDEO: // Example: VIDEOS
					eventVideoAdd.setText(count + " videos selected");
					break;
				case PICK_AUDIO: // Example: AUDIO
					eventAudioAdd.setText(count + " audio files selected");
					break;
			}

			ToastMessagesManager.show(this, "Files added.");
		}
	}

	private void addUriToList(Uri uri, int requestCode) {
		switch (requestCode) {
			case PICK_IMAGE: imageUris.add(uri); break;
			case PICK_AUDIO: audioUris.add(uri); break;
			case PICK_VIDEO: videoUris.add(uri); break;
		}
	}

	// === END CODE FOR ADDING MEDIA === //
	
	private void initializeViews() {
		eventTitleAdd = findViewById(R.id.eventTitleAdd);
        eventDateAdd = findViewById(R.id.eventDateAdd);
		eventTimeAdd = findViewById(R.id.eventTimeAdd);
		eventAddressAdd = findViewById(R.id.eventAddressAdd);
        eventBudgetAdd = findViewById(R.id.eventBudgetAdd);
		eventNotesAdd = findViewById(R.id.eventNotesAdd);
		eventAttendanceAddSpinner = findViewById(R.id.eventAttendanceAddSpinner);
		eventSaveAddBtn = findViewById(R.id.eventSaveAddBtn);
		
		eventImageAdd = findViewById(R.id.eventImageAdd);
        eventVideoAdd = findViewById(R.id.eventVideoAdd);
		eventAudioAdd = findViewById(R.id.eventAudioAdd);
	}

	private void populateAttendedSpinner() {
		attendanceList.add("Not Yet Attended");
		attendanceList.add("Attended");
		attendanceList.add("Never Attended");

		attendanceMap.put("Never Attended", 0);
		attendanceMap.put("Attended", 1);
		attendanceMap.put("Not Yet Attended", 2);
		
		attendanceAdapter.notifyDataSetChanged();
		attendanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		eventAttendanceAddSpinner.setAdapter(attendanceAdapter);
	}

	private void showDatePicker() {
		final Calendar calendar = Calendar.getInstance();

		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH); // 0-based
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		DatePickerDialog datePickerDialog = new DatePickerDialog(
			this,
			new DatePickerDialog.OnDateSetListener() {
				@Override
				public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
					String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
					eventDateAdd.setText(selectedDate);
				}
			},
			year, month, day
		);

		datePickerDialog.show();
	}

	private void showTimePicker() {
		final Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);

		TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
				String formattedTime = String.format(Locale.getDefault(), "%02d%02d", selectedHour, selectedMinute);
				eventTimeAdd.setText(formattedTime);
			}
		};

		// true = 24-hour format
		TimePickerDialog timePickerDialog = new TimePickerDialog(this, timeSetListener, hour, minute, true);
		timePickerDialog.show();
	}

	private void saveEvent() {
		String title = eventTitleAdd.getText().toString().trim();
		String notes = eventNotesAdd.getText().toString().trim();
		String date = eventDateAdd.getText().toString().trim();
		String time = eventTimeAdd.getText().toString().trim();
		String address = eventAddressAdd.getText().toString().trim();
		String budget = eventBudgetAdd.getText().toString().trim();

		// Input validation
		if (title.isEmpty() || notes.isEmpty() || budget.isEmpty() || address.isEmpty()) {
			ToastMessagesManager.show(this, "Please fill all fields!");
			return;
		}

		if (date.isEmpty() || time.isEmpty()) {
			ToastMessagesManager.show(this, "Please select valid date and time!");
			return;
		}

		if (selectedAttendanceId == -1) {
			ToastMessagesManager.show(this, "Please select a valid attendance!");
			return;
		}

		// Prepare values for database
		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("date", date);
		values.put("time", time);
		values.put("notes", notes);
		values.put("address", address);
		values.put("budget", budget);
		values.put("attended", selectedAttendanceId);

		// Insert event into database
		long eventId = eventsDBHelper.insertEvent(values);

		if (eventId != -1) {
			String folderName = "Event " + eventId;

			// Save images
			if (imageUris != null && !imageUris.isEmpty()) {
				List<String> savedImages = saveMediaFiles(imageUris, folderName, ".jpg");
				if (savedImages != null && !savedImages.isEmpty()) {
					eventsDBHelper.insertEventMediaPaths((int) eventId, savedImages, "image");
				}
			}

			// Save audio
			if (audioUris != null && !audioUris.isEmpty()) {
				List<String> savedAudio = saveMediaFiles(audioUris, folderName, ".mp3");
				if (savedAudio != null && !savedAudio.isEmpty()) {
					eventsDBHelper.insertEventMediaPaths((int) eventId, savedAudio, "audio");
				}
			}

			// Save videos
			if (videoUris != null && !videoUris.isEmpty()) {
				List<String> savedVideos = saveMediaFiles(videoUris, folderName, ".mp4");
				if (savedVideos != null && !savedVideos.isEmpty()) {
					eventsDBHelper.insertEventMediaPaths((int) eventId, savedVideos, "video");
				}
			}

			// Clear media lists
			imageUris.clear();
			audioUris.clear();
			videoUris.clear();

			// Reset buttons
			eventImageAdd.setText("Add Images");
			eventVideoAdd.setText("Add Videos");
			eventAudioAdd.setText("Add Audio");

			//#####
			String eventDateTime = date + " " + time;
			long dueTime = ConvertTimeToMillis.convertToMillis(eventDateTime);
			
			ReminderScheduler.scheduleReminders(this,
			        ReminderScheduler.TYPE_EVENT,
					eventId,
					dueTime
			);
			//#####

			ToastMessagesManager.show(this, "Added successfully. Reminders set for:" + dueTime);
			
		} else {
			ToastMessagesManager.show(this, ",Error saving event!");
		}

		finish();
	}
	
	private List<String> saveMediaFiles(List<Uri> uris, String eventFolderName, String extension) {
		List<String> savedPaths = new ArrayList<String>();
		File mediaFolder = getMediaFolder(eventFolderName);

		if (mediaFolder == null) {
			return savedPaths;
		}

		for (Uri uri : uris) {
			String fileName = "event_media_" + System.currentTimeMillis() + extension;
			File file = saveFileFromUri(uri, mediaFolder, fileName);
			if (file != null) {
				savedPaths.add(file.getAbsolutePath());
			}
		}

		return savedPaths;
	}

	private File getMediaFolder(String eventFolderName) {
		File baseFolder = new File(Environment.getExternalStorageDirectory(), 
								   ".superme/events/" + eventFolderName);

		if (!baseFolder.exists()) {
			if (!baseFolder.mkdirs()) {
				Log.e(TAG, "Failed to create directory: " + baseFolder.getAbsolutePath());
				return null;
			}
		}

		return baseFolder;
	}

	private File saveFileFromUri(Uri uri, File destDir, String fileName) {
		InputStream in = null;
		OutputStream out = null;
		File destFile = new File(destDir, fileName);

		try {
			in = getContentResolver().openInputStream(uri);
			if (in == null) {
				Log.e(TAG, "Cannot open input stream for URI: " + uri);
				return null;
			}

			out = new FileOutputStream(destFile);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			// Verify file was saved
			if (destFile.length() == 0) {
				destFile.delete();
				return null;
			}

			return destFile;
		} catch (IOException e) {
			Log.e(TAG, "Error saving file", e);
			if (destFile.exists()) {
				destFile.delete();
			}
			return null;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				Log.e(TAG, "Error closing streams", e);
			}
		}
	}
}

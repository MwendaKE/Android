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
import android.net.*;
import android.media.*;
import java.io.*;
import android.os.*;
import android.view.*;
import android.graphics.*;
import android.provider.*;
import com.mwendasoft.superme.helpers.*;

public class EventEditActivity extends BaseActivity {
    private EditText eventTitleEdit, eventDateEdit, eventTimeEdit, eventAddressEdit, eventBudgetEdit, eventNotesEdit;
    private EditText eventImageEditText, eventVideoEditText, eventAudioEditText;
	private Spinner eventAttendanceEditSpinner;
	private Button eventSaveEditBtn;
    private EventsDBHelper eventsDBHelper;

	private HashMap<String, Integer> attendanceMap = new HashMap<>();
	private ArrayList<String> attendanceList= new ArrayList<>();
    private ArrayAdapter<String> attendanceAdapter;
	private Event selectedEvent;
	
	private List<Uri> imageUris = new ArrayList<>();
	private List<Uri> audioUris = new ArrayList<>();
	private List<Uri> videoUris = new ArrayList<>();

	private static final int PICK_IMAGE = 1001;
	private static final int PICK_AUDIO = 1002;
	private static final int PICK_VIDEO = 1003;

	private MediaPlayer mediaPlayer;
	private boolean isPaused = false;
	private ImageView currentAudioIcon;
	private File currentAudioFile;
	
	private int selectedAttendanceId, eventId;
	private String eventTitle;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_edit_activity);

		initializeViews();
		
        imageUris.clear();
        audioUris.clear();
        videoUris.clear();
		
		eventsDBHelper = new EventsDBHelper(this);
		attendanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, attendanceList);
		
		selectedEvent = (Event) getIntent().getSerializableExtra("selectedEvent");
		eventTitle = selectedEvent.getTitle();
		eventId = selectedEvent.getId();
		
		selectedAttendanceId = -1;
		
		populateAttendedSpinner();

		loadEvent();
		loadAllMediaFiles();
		
        eventAttendanceEditSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedAttendance = parent.getItemAtPosition(position).toString();
					selectedAttendanceId = attendanceMap.getOrDefault(selectedAttendance, -1);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});

        eventDateEdit.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDatePicker();
				}
			});

		eventTimeEdit.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showTimePicker();
				}
			});

        eventSaveEditBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					saveEvent();
				}
			});
			
		// ==== CODE FOR ADDING MEDIA == //

		eventImageEditText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFiles("image/*", PICK_IMAGE);
				}
			});

		eventVideoEditText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFiles("video/*", PICK_VIDEO);
				}
			});

		eventAudioEditText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFiles("audio/*", PICK_AUDIO);
				}
			});
    }

	// MEDIA PICKUP //

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
					addUriToList(data.getClipData().getItemAt(i).getUri(), requestCode);
				}
			} else if (data.getData() != null) {
				count = 1;
				addUriToList(data.getData(), requestCode);
			}
			switch (requestCode) {
				case PICK_IMAGE: eventImageEditText.setText(count + " images selected"); break;
				case PICK_VIDEO: eventVideoEditText.setText(count + " videos selected"); break;
				case PICK_AUDIO: eventAudioEditText.setText(count + " audio files selected"); break;
			}
		}
	}

	private void addUriToList(Uri uri, int requestCode) {
		switch (requestCode) {
			case PICK_IMAGE: imageUris.add(uri); break;
			case PICK_AUDIO: audioUris.add(uri); break;
			case PICK_VIDEO: videoUris.add(uri); break;
		}
	}

	//-------------
	
	private void initializeViews() {
		eventTitleEdit = findViewById(R.id.eventTitleEdit);
        eventDateEdit = findViewById(R.id.eventDateEdit);
		eventTimeEdit = findViewById(R.id.eventTimeEdit);
		eventAddressEdit = findViewById(R.id.eventAddressEdit);
        eventBudgetEdit = findViewById(R.id.eventBudgetEdit);
		eventNotesEdit = findViewById(R.id.eventNotesEdit);
		eventAttendanceEditSpinner = findViewById(R.id.eventAttendanceEditSpinner);
		eventSaveEditBtn = findViewById(R.id.eventSaveEditBtn);
		eventImageEditText = findViewById(R.id.eventImageEdit);
        eventVideoEditText = findViewById(R.id.eventVideoEdit);
		eventAudioEditText = findViewById(R.id.eventAudioEdit);
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
		eventAttendanceEditSpinner.setAdapter(attendanceAdapter);
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
					eventDateEdit.setText(selectedDate);
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
				eventTimeEdit.setText(formattedTime);
			}
		};

		// true = 24-hour format
		TimePickerDialog timePickerDialog = new TimePickerDialog(this, timeSetListener, hour, minute, true);
		timePickerDialog.show();
	}

	private void loadEvent() {
		eventTitleEdit.setText(eventTitle);
		eventNotesEdit.setText(selectedEvent.getNotes());
		eventDateEdit.setText(selectedEvent.getDate());
		eventTimeEdit.setText(selectedEvent.getTime());
		eventAddressEdit.setText(selectedEvent.getAddress());
		eventBudgetEdit.setText(String.format("%.2f", selectedEvent.getBudget()));
		
		// Attended Spinner Update
		String attendanceName = getKeyByValue(attendanceMap, selectedEvent.getAttendance());
		int attendancePos = attendanceAdapter.getPosition(attendanceName);
		eventAttendanceEditSpinner.setSelection(attendancePos);
	}
	
	private void loadAllMediaFiles() {
		File folder = new File(Environment.getExternalStorageDirectory(), ".superme/events/Event " + eventId);

		LinearLayout imageContainer = findViewById(R.id.eventImagesEditContainer);
		LinearLayout audioContainer = findViewById(R.id.eventAudioEditContainer);
		LinearLayout videoContainer = findViewById(R.id.eventVideosEditContainer);

	    imageContainer.removeAllViews();
		audioContainer.removeAllViews();
		videoContainer.removeAllViews();

		if (folder.exists() && folder.isDirectory()) {
			File[] files = folder.listFiles();
			if (files != null && files.length > 0) {
				for (File file : files) {
					String name = file.getName().toLowerCase();

					if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif")) {
						addImageItem(file, imageContainer);
					} else if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".m4a")) {
						addAudioItem(file, audioContainer);
					} else if (name.endsWith(".mp4") || name.endsWith(".3gp") || name.endsWith(".avi")) {
						addVideoItem(file, videoContainer);
					}
				}
			}
		}
	}
    
    private void saveEvent() {
		String title = eventTitleEdit.getText().toString().trim();
		String notes = eventNotesEdit.getText().toString().trim();
		String date = eventDateEdit.getText().toString().trim();
		String time = eventTimeEdit.getText().toString().trim();
		String address = eventAddressEdit.getText().toString().trim();
		String budget = eventBudgetEdit.getText().toString().trim();

		if (title.isEmpty() || notes.isEmpty() || budget.isEmpty() || address.isEmpty()) {
			ToastMessagesManager.show(this, "Please fill all fields!");
			return;
		}

		if (date.isEmpty() || time.isEmpty()) {
			ToastMessagesManager.show(this, "Select valid date and time!");
			return;
		}

		if (selectedAttendanceId == -1) {
			ToastMessagesManager.show(this, "Select a valid attendance!");
			return;
		}
		
		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("date", date);
		values.put("time", time);
		values.put("notes", notes);
		values.put("address", address);
		values.put("budget", budget);
		values.put("attended", selectedAttendanceId);

		long resultId = eventsDBHelper.updateEvent(eventTitle, values);
		
		//#####
		if (resultId > 0) {
			String folderName = "Event " + eventId;
			List<String> savedImages = saveMediaFiles(imageUris, folderName, ".jpg");
			List<String> savedAudio = saveMediaFiles(audioUris, folderName, ".mp3");
			List<String> savedVideos = saveMediaFiles(videoUris, folderName, ".mp4");

			eventsDBHelper.insertEventMediaPaths(eventId, savedImages, "image");
			eventsDBHelper.insertEventMediaPaths(eventId, savedAudio, "audio");
			eventsDBHelper.insertEventMediaPaths(eventId, savedVideos, "video");
			
			ToastMessagesManager.show(this, "Updated successfully.");
		} else {
			ToastMessagesManager.show(this, "Updated successfully!");
		}
		
		finish();
	}
	
	private void addImageItem(final File file, LinearLayout container) {
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(8, 8, 8, 8);

		ImageView imageView = new ImageView(this);
		imageView.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

		// Load thumbnail instead of full image
		Bitmap thumbnail = ThumbnailUtils.extractThumbnail(
			BitmapFactory.decodeFile(file.getAbsolutePath()),
			200, 200
		);
		imageView.setImageBitmap(thumbnail);

		Button deleteBtn = new Button(this);
		deleteBtn.setText("Remove");
		deleteBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (file.delete()) {
						loadAllMediaFiles();
						ToastMessagesManager.show(EventEditActivity.this, "Image removed successfully!");
					}
				}
			});

		layout.addView(imageView);
		layout.addView(deleteBtn);
		container.addView(layout);
	}

	private void addVideoItem(final File file, LinearLayout container) {
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(8, 8, 8, 8);

		// Create a thumbnail of the video
		Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(
			file.getAbsolutePath(),
			MediaStore.Video.Thumbnails.MINI_KIND
		);

		if (thumbnail != null) {
			ImageView thumbnailView = new ImageView(this);

			// Maintain original thumbnail dimensions
			int width = thumbnail.getWidth();
			int height = thumbnail.getHeight();
			float aspectRatio = (float) width / height;

			// Resize based on a fixed width while maintaining aspect ratio
			int desiredWidth = 300;
			int desiredHeight = (int) (desiredWidth / aspectRatio);

			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(desiredWidth, desiredHeight);
			thumbnailView.setLayoutParams(params);
			thumbnailView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			thumbnailView.setImageBitmap(thumbnail);

			layout.addView(thumbnailView);
		}

		Button deleteBtn = new Button(this);
		deleteBtn.setText("Remove");
		deleteBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (file.delete()) {
						loadAllMediaFiles();
						ToastMessagesManager.show(EventEditActivity.this, "Video removed successfully!");
					}
				}
			});

		layout.addView(deleteBtn);
		container.addView(layout);
    }

	// -- AUDIO LOGIC SETUP -- //

	private void addAudioItem(final File file, LinearLayout container) {
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(8, 8, 8, 8);
		layout.setGravity(Gravity.CENTER_HORIZONTAL);

		// Use an ImageView to represent the audio file visually
		final ImageView audioIcon = new ImageView(this);
		audioIcon.setLayoutParams(new ViewGroup.LayoutParams(150, 150));
		audioIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		audioIcon.setImageResource(android.R.drawable.ic_media_play); // You can replace with a custom icon

		// Play audio on icon click
		audioIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mediaPlayer == null || currentAudioFile == null || !currentAudioFile.equals(file)) {
						playAudio(file, audioIcon);
					} else {
						if (mediaPlayer.isPlaying()) {
							pauseAudio();
						} else if (isPaused) {
							resumeAudio();
						}
					}
				}
			});

		Button deleteBtn = new Button(this);
		deleteBtn.setText("Remove");
		deleteBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					confirmDelete(file, new Runnable() {
							@Override
							public void run() {
								// Stop if this is the currently playing audio
								if (currentAudioFile != null && currentAudioFile.equals(file)) {
									stopAudio();
								}
								if (file.delete()) {
									loadAllMediaFiles(); // Refresh UI
									ToastMessagesManager.show(EventEditActivity.this, "Audio removed successfully!");
								} else {
									ToastMessagesManager.show(EventEditActivity.this, "Failed to delete!");
								}
							}
						});
				}
			});

		layout.addView(audioIcon);
		layout.addView(deleteBtn);
		container.addView(layout);
	}

	private void confirmDelete(final File file, final Runnable onDeleteConfirmed) {
		new AlertDialog.Builder(this)
			.setTitle("Delete Voice")
			.setMessage("Are you sure you want to delete this voice?")
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					onDeleteConfirmed.run();
				}
			})
			.setNegativeButton("No", null)
			.show();
	}

	private void playAudio(File file, ImageView icon) {
		try {
			if (mediaPlayer != null) {
				stopAudio(); // stop previous audio
			}
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setDataSource(file.getAbsolutePath());
			mediaPlayer.prepare();
			mediaPlayer.start();
			currentAudioIcon = icon;
			currentAudioFile = file;
			isPaused = false;
			icon.setImageResource(android.R.drawable.ic_media_pause);

			mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						stopAudio();
					}
				});
		} catch (IOException e) {
			e.printStackTrace();
			ToastMessagesManager.show(this, "Error playing audio!");
		}
	}

	private void pauseAudio() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			isPaused = true;
			if (currentAudioIcon != null) {
				currentAudioIcon.setImageResource(android.R.drawable.ic_media_play);
			}
		}
	}

	private void resumeAudio() {
		if (mediaPlayer != null && isPaused) {
			mediaPlayer.start();
			isPaused = false;
			if (currentAudioIcon != null) {
				currentAudioIcon.setImageResource(android.R.drawable.ic_media_pause);
			}
		}
	}

	private void stopAudio() {
		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
			isPaused = false;
			if (currentAudioIcon != null) {
				currentAudioIcon.setImageResource(android.R.drawable.ic_media_play);
			}
			currentAudioIcon = null;
			currentAudioFile = null;
		}
	}

	private List<String> saveMediaFiles(List<Uri> uris, String eventFolderName, String extension) {
		List<String> savedPaths = new ArrayList<>();
		File mediaFolder = getEventMediaFolder(eventFolderName);
		for (Uri uri : uris) {
			String fileName = "event_media_" + System.currentTimeMillis() + extension;
			File file = copyUriToFile(uri, mediaFolder, fileName);
			if (file != null) {
				savedPaths.add(file.getAbsolutePath());
			}
		}
		return savedPaths;
	}

	private File getEventMediaFolder(String eventFolderName) {
		File baseFolder = new File(Environment.getExternalStorageDirectory(), ".superme/events/" + eventFolderName);
		if (!baseFolder.exists()) baseFolder.mkdirs();
		return baseFolder;
	}

	private File copyUriToFile(Uri uri, File destDir, String fileName) {
		try {
			if (uri == null || destDir == null) return null;
			if (!destDir.exists()) destDir.mkdirs();
			File destFile = new File(destDir, fileName);
			InputStream in = getContentResolver().openInputStream(uri);
			OutputStream out = new FileOutputStream(destFile);
			byte[] buffer = new byte[4096];
			int length;
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			in.close();
			out.close();
			return destFile;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String getKeyByValue(HashMap<String, Integer> map, int value) {
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			if (entry.getValue() == value) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	
}

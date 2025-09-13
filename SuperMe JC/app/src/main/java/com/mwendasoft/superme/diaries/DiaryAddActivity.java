package com.mwendasoft.superme.diaries;

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
import com.mwendasoft.superme.categories.CategoriesDBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.*;
import android.content.*;
import android.widget.*;
import android.util.*;
import android.content.res.*;
import java.util.*;
import android.net.*;
import java.io.*;
import android.os.*;
import com.mwendasoft.superme.helpers.*;

public class DiaryAddActivity extends BaseActivity {
    private EditText diaryTitleAdd, diaryDateAdd, diaryTimeAdd, diaryDescriptionAdd;
	private EditText diaryImageAdd, diaryVideoAdd, diaryAudioAdd;
    private Spinner diaryMoodAddSpinner;
	private Button diarySaveAddBtn;
    private DiariesDBHelper diariesDBHelper;
	
	private List<Uri> imageUris = new ArrayList<>();
	private List<Uri> audioUris = new ArrayList<>();
	private List<Uri> videoUris = new ArrayList<>();

	private static final int PICK_IMAGE = 1001;
	private static final int PICK_AUDIO = 1002;
	private static final int PICK_VIDEO = 1003;
	
    private ArrayAdapter<String> moodAdapter;
	private String selectedMood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary_add_activity);

        diaryTitleAdd = findViewById(R.id.diaryTitleAdd);
        diaryDescriptionAdd = findViewById(R.id.diaryDescriptionAdd);
		diaryDateAdd = findViewById(R.id.diaryDateAdd);
		diaryTimeAdd = findViewById(R.id.diaryTimeAdd);
        diaryMoodAddSpinner = findViewById(R.id.diaryMoodAddSpinner);
		diarySaveAddBtn = findViewById(R.id.diarySaveAddBtn);
		
		diaryImageAdd = findViewById(R.id.diaryImageAdd);
        diaryVideoAdd = findViewById(R.id.diaryVideoAdd);
		diaryAudioAdd = findViewById(R.id.diaryAudioAdd);
		
		imageUris.clear();
        audioUris.clear();
        videoUris.clear();
		
		diariesDBHelper = new DiariesDBHelper(this);
		
		populateMoodSpinner();
		
        diaryMoodAddSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				selectedMood = parent.getItemAtPosition(position).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// Do nothing
			}
		});
		
        diaryDateAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDatePicker();
				}
			});
			
		diaryDateAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDatePicker();
				}
			});

        diaryTimeAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showTimePicker();
				}
			});
			
		diarySaveAddBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					saveDiary();
				}
			});
			
		// ==== CODE FOR ADDING MEDIA == //

		diaryImageAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFile("image/*", PICK_IMAGE);
				}
			});

		diaryVideoAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFile("video/*", PICK_VIDEO);
				}
			});

		diaryAudioAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFile("audio/*", PICK_AUDIO);
				}
			});
	}

	private void pickFile(String type, int requestCode) {
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
					diaryImageAdd.setText(count + " images selected");
					break;
				case PICK_VIDEO: // Example: VIDEOS
					diaryVideoAdd.setText(count + " videos selected");
					break;
				case PICK_AUDIO: // Example: AUDIO
					diaryAudioAdd.setText(count + " audio files selected");
					break;
			}

			Toast.makeText(this, "Files added", Toast.LENGTH_SHORT).show();
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
	
	private void populateMoodSpinner() {
		String[] items = {"Happy", "Sad", "Confused", "Other"};
		moodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
		moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		diaryMoodAddSpinner.setAdapter(moodAdapter);
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
					diaryDateAdd.setText(selectedDate);
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
				diaryTimeAdd.setText(formattedTime);
			}
		};

		// true = 24-hour format
		TimePickerDialog timePickerDialog = new TimePickerDialog(this, timeSetListener, hour, minute, true);
		timePickerDialog.show();
	}

    
    private void saveDiary() {
		String title = diaryTitleAdd.getText().toString().trim();
		String description = diaryDescriptionAdd.getText().toString().trim();
		String date = diaryDateAdd.getText().toString().trim();
		String time = diaryTimeAdd.getText().toString().trim();
		
		if (title.isEmpty() || description.isEmpty()) {
			ToastMessagesManager.show(this, "Please fill all fields!");
			return;
		}

		if (date.isEmpty() || time.isEmpty()) {
			ToastMessagesManager.show(this, "Select valid datetime!");
			return;
		}

		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("description", description);
		values.put("date", date);
		values.put("time", time);
		values.put("mood", selectedMood);
		
		long entryId = diariesDBHelper.insertDiaryEntry(values);
		
		if (entryId != -1) {
            String folderName = "Entry " + entryId;

            // Save media files to folder and get their final paths
            List<String> savedImages = saveMediaFiles(imageUris, folderName, ".jpg");
            List<String> savedAudio = saveMediaFiles(audioUris, folderName, ".mp3");
            List<String> savedVideos = saveMediaFiles(videoUris, folderName, ".mp4");

            // Store paths in DB
            if (!savedImages.isEmpty()) {
                diariesDBHelper.insertDiaryMediaPaths((int) entryId, savedImages, "image");
            }
            if (!savedAudio.isEmpty()) {
                diariesDBHelper.insertDiaryMediaPaths((int) entryId, savedAudio, "audio");
            }
            if (!savedVideos.isEmpty()) {
                diariesDBHelper.insertDiaryMediaPaths((int) entryId, savedVideos, "video");
            }

            // Clear the media lists after saving
            imageUris.clear();
            audioUris.clear();
            videoUris.clear();

            // Reset the button texts
            diaryImageAdd.setText(getString(R.string.add_images));
            diaryVideoAdd.setText(getString(R.string.add_videos));
            diaryAudioAdd.setText(getString(R.string.add_audio));

			ToastMessagesManager.show(this, "Added successfully.");
            finish();
        } else {
			ToastMessagesManager.show(this, "Adding failed!");
        }
	}
	
	//#######
	
	private List<String> saveMediaFiles(List<Uri> uris, String diaryFolderName, String extension) {
		List<String> savedPaths = new ArrayList<>();

		File mediaFolder = getDiaryMediaFolder(diaryFolderName);

		for (Uri uri : uris) {
			String fileName = "diary_media_" + System.currentTimeMillis() + extension;
			File file = copyUriToFile(uri, mediaFolder, fileName);
			if (file != null) {
				savedPaths.add(file.getAbsolutePath());
			}
		}

		return savedPaths;
	}

	private File getDiaryMediaFolder(String diaryFolderName) {
		File baseFolder = new File(
			Environment.getExternalStorageDirectory(),
			".superme/diaries/" + diaryFolderName
		);

		if (!baseFolder.exists()) {
			baseFolder.mkdirs();
		}

		return baseFolder;
	}

	private File copyUriToFile(Uri uri, File destDir, String fileName) {
		if (uri == null || destDir == null || fileName == null) {
			Log.e("copyUriToFile", "One or more inputs are null");
			return null;
		}

		// Make sure the destination directory exists
		if (!destDir.exists()) {
			if (!destDir.mkdirs()) {
				Log.e("copyUriToFile", "Failed to create directory: " + destDir.getAbsolutePath());
				return null;
			}
		}

		File destFile = new File(destDir, fileName);

		try {
			InputStream in = getContentResolver().openInputStream(uri);
			if (in == null) {
				Log.e("copyUriToFile", "Unable to open input stream for URI: " + uri);
				return null;
			}

			OutputStream out = new FileOutputStream(destFile);

			byte[] buffer = new byte[4096]; // use a bigger buffer for performance
			int length;
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();

			return destFile;

		} catch (IOException e) {
			e.printStackTrace();
			Log.e("copyUriToFile", "IOException: " + e.getMessage());
			return null;
		}
	}
}

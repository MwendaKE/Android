package com.mwendasoft.superme.notes;

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
import com.mwendasoft.superme.core.*;
import com.mwendasoft.superme.categories.*;
import java.time.format.*;
import java.time.*;
import android.text.*;
import java.text.*;
import android.net.*;
import java.io.*;
import android.os.*;
import com.mwendasoft.superme.helpers.*;

public class NoteAddActivity extends BaseActivity {
    private EditText notesTitleAdd, notesImageAdd, notesVideoAdd, notesAudioAdd;
    private CheckBox notesImportanceAddCheckbox;
	private Button notesSaveAddBtn;
	private NotesDBHelper notesDBHelper;
	private SuperMeMarkupTextEditor notesMarkupEditorAdd;

	private List<Uri> imageUris = new ArrayList<>();
	private List<Uri> audioUris = new ArrayList<>();
	private List<Uri> videoUris = new ArrayList<>();

	private static final int PICK_IMAGE = 1001;
	private static final int PICK_AUDIO = 1002;
	private static final int PICK_VIDEO = 1003;

	private int important;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_add_activity);

		imageUris.clear();
        audioUris.clear();
        videoUris.clear();
		
		notesTitleAdd = findViewById(R.id.notesTitleAdd);
        notesMarkupEditorAdd = findViewById(R.id.notesMarkupEditorAdd);
		notesImportanceAddCheckbox = findViewById(R.id.notesImportanceAddCheckbox);
		notesSaveAddBtn = findViewById(R.id.notesSaveAddBtn);
		
		notesImageAdd = findViewById(R.id.notesImageAdd);
        notesVideoAdd = findViewById(R.id.notesVideoAdd);
		notesAudioAdd = findViewById(R.id.notesAudioAdd);
		
		notesDBHelper = new NotesDBHelper(this);
		
		notesImportanceAddCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					important = 1;
				} else {
					important = 0;
				}
			}
		});

		// ==== CODE FOR ADDING MEDIA == //
		
		notesImageAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFile("image/*", PICK_IMAGE);
				}
			});
			
		notesVideoAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFile("video/*", PICK_VIDEO);
				}
			});
			
		notesAudioAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFile("audio/*", PICK_AUDIO);
				}
			});
			
		notesSaveAddBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					saveNote();
				}
			});
    }
	
	// === END CODE FOR ADDING MEDIA === //
	
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
					notesImageAdd.setText(count + " images selected");
					break;
				case PICK_VIDEO: // Example: VIDEOS
					notesVideoAdd.setText(count + " videos selected");
					break;
				case PICK_AUDIO: // Example: AUDIO
					notesAudioAdd.setText(count + " audio files selected");
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
	
	private void saveNote() {
		String title = notesTitleAdd.getText().toString().trim();
        String noteText = notesMarkupEditorAdd.getRawText();
		
        if (title.isEmpty() || noteText.isEmpty()) {
			ToastMessagesManager.show(this, "Please fill all fields!");
            return;
        }

        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("notes", noteText);
        values.put("datetime", getCurrentDateTime());
        values.put("importance", important);

        long noteId = notesDBHelper.insertNote(values);

        if (noteId != -1) {
            String folderName = "Note " + noteId;

            // Save media files to folder and get their final paths
            List<String> savedImages = saveMediaFiles(imageUris, folderName, ".jpg");
            List<String> savedAudio = saveMediaFiles(audioUris, folderName, ".mp3");
            List<String> savedVideos = saveMediaFiles(videoUris, folderName, ".mp4");

            // Store paths in DB
            if (!savedImages.isEmpty()) {
                notesDBHelper.insertNoteMediaPaths((int) noteId, savedImages, "image");
            }
            if (!savedAudio.isEmpty()) {
                notesDBHelper.insertNoteMediaPaths((int) noteId, savedAudio, "audio");
            }
            if (!savedVideos.isEmpty()) {
                notesDBHelper.insertNoteMediaPaths((int) noteId, savedVideos, "video");
            }

            // Clear the media lists after saving
            imageUris.clear();
            audioUris.clear();
            videoUris.clear();

            // Reset the button texts
            notesImageAdd.setText(getString(R.string.add_images));
            notesVideoAdd.setText(getString(R.string.add_videos));
            notesAudioAdd.setText(getString(R.string.add_audio));

			ToastMessagesManager.show(this, "Added successfully!");
            finish();
        } else {
			ToastMessagesManager.show(this, "Adding failed!");
        }
    }
	
	private List<String> saveMediaFiles(List<Uri> uris, String noteFolderName, String extension) {
		List<String> savedPaths = new ArrayList<>();

		File mediaFolder = getNoteMediaFolder(noteFolderName);

		for (Uri uri : uris) {
			String fileName = "note_media_" + System.currentTimeMillis() + extension;
			File file = copyUriToFile(uri, mediaFolder, fileName);
			if (file != null) {
				savedPaths.add(file.getAbsolutePath());
			}
		}

		return savedPaths;
	}
	
	private File getNoteMediaFolder(String noteFolderName) {
		File baseFolder = new File(
			Environment.getExternalStorageDirectory(),
			".superme/notes/" + noteFolderName
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
	
	
	public String getCurrentDateTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HHmm", Locale.getDefault());
		return sdf.format(new Date());
	}
}

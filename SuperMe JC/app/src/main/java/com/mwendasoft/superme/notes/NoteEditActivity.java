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
import android.media.*;
import android.view.*;
import android.graphics.*;
import android.provider.*;
import com.mwendasoft.superme.helpers.*;

public class NoteEditActivity extends BaseActivity {
    private EditText notesTitleEdit, notesImageEditText, notesVideoEditText, notesAudioEditText;
    private CheckBox notesImportanceEditCheckbox;
	private Button notesSaveEditBtn;
	private SuperMeMarkupTextEditor notesMarkupEditorEdit;
	
	private NotesDBHelper notesDBHelper;

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
	
	private int important;
	private Note selectedNote;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit_activity);

		notesTitleEdit = findViewById(R.id.notesTitleEdit);
        notesMarkupEditorEdit = findViewById(R.id.notesMarkupEditorEdit);
		notesImportanceEditCheckbox = findViewById(R.id.notesImportanceEditCheckbox);
		notesSaveEditBtn = findViewById(R.id.notesSaveEditBtn);

		notesImageEditText = findViewById(R.id.notesImageEdit);
        notesVideoEditText = findViewById(R.id.notesVideoEdit);
		notesAudioEditText = findViewById(R.id.notesAudioEdit);
	
		notesDBHelper = new NotesDBHelper(this);
		selectedNote = (Note) getIntent().getSerializableExtra("selectedNote");
		
		loadNote();
		loadAllMediaFiles();
		
		notesImportanceEditCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						important = 1;
					} else {
						important = 0;
					}
				}
			});
			
		notesSaveEditBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					updateNote();
				}
			});
			
		notesImageEditText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFile("image/*", PICK_IMAGE);
				}
			});

		notesVideoEditText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFile("video/*", PICK_VIDEO);
				}
			});

		notesAudioEditText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFile("audio/*", PICK_AUDIO);
				}
			});
    }
	
	private void loadNote() {
		Cursor cursor = notesDBHelper.getNoteById(selectedNote.getId());

		if (cursor != null && cursor.moveToFirst()) {
			int titleIndex = cursor.getColumnIndex("title");
			int textIndex = cursor.getColumnIndex("notes");
			int favIndex = cursor.getColumnIndex("importance");
			
			String title = cursor.getString(titleIndex);
			String text = cursor.getString(textIndex);
			int fav = cursor.getInt(favIndex);
			
			notesTitleEdit.setText(title);
		    notesMarkupEditorEdit.setTextWithMarkup(text);
		    notesImportanceEditCheckbox.setChecked(fav == 1);
		}
	}
	
	// == MEDIA DISPLAY IN THE EDIT == //
	
	private void loadAllMediaFiles() {
		File folder = new File(Environment.getExternalStorageDirectory(), ".superme/notes/Note " + selectedNote.getId());

		LinearLayout imageContainer = findViewById(R.id.notesImagesEditContainer);
		LinearLayout audioContainer = findViewById(R.id.notesAudioEditContainer);
		LinearLayout videoContainer = findViewById(R.id.notesVideosEditContainer);

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
						Toast.makeText(NoteEditActivity.this, "Image removed", Toast.LENGTH_SHORT).show();
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
				ToastMessagesManager.show(NoteEditActivity.this, "Video removed successfully.");
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
									ToastMessagesManager.show(NoteEditActivity.this, "Audio removed successfully!");
								} else {
									ToastMessagesManager.show(NoteEditActivity.this, "Delete failed!");
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
			.setTitle("Delete Audio")
			.setMessage("Are you sure you want to delete this audio file?")
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
	
	//-- END AUDIO SETUP LOGIC --//
	
	// == END MEDIA == //
	
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
					addUriToList(data.getClipData().getItemAt(i).getUri(), requestCode);
				}
			} else if (data.getData() != null) {
				count = 1;
				addUriToList(data.getData(), requestCode);
			}
			switch (requestCode) {
				case PICK_IMAGE: notesImageEditText.setText(count + " images selected"); break;
				case PICK_VIDEO: notesVideoEditText.setText(count + " videos selected"); break;
				case PICK_AUDIO: notesAudioEditText.setText(count + " audio files selected"); break;
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
		File baseFolder = new File(Environment.getExternalStorageDirectory(), ".superme/notes/" + noteFolderName);
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

	private void updateNote() {
		String title = notesTitleEdit.getText().toString().trim();
		String noteText = notesMarkupEditorEdit.getRawText();

		if (title.isEmpty() || noteText.isEmpty()) {
			ToastMessagesManager.show(this, "Please fill all the fields!");
			return;
		}

		ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("notes", noteText);
		values.put("importance", important);

		int noteId = selectedNote.getId();
		
		int rowsUpdated = notesDBHelper.updateNote(values, noteId);
		
		if (rowsUpdated > 0) {
			String folderName = "Note " + noteId;
			List<String> savedImages = saveMediaFiles(imageUris, folderName, ".jpg");
			List<String> savedAudio = saveMediaFiles(audioUris, folderName, ".mp3");
			List<String> savedVideos = saveMediaFiles(videoUris, folderName, ".mp4");

			notesDBHelper.insertNoteMediaPaths(noteId, savedImages, "image");
			notesDBHelper.insertNoteMediaPaths(noteId, savedAudio, "audio");
			notesDBHelper.insertNoteMediaPaths(noteId, savedVideos, "video");

			ToastMessagesManager.show(this, "Note updated");
			finish();
		} else {
			ToastMessagesManager.show(this, "Update failed!");
		}
	}
}

package com.mwendasoft.superme.music;

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
import java.io.*;
import android.os.*;
import android.media.*;
import android.view.*;
import android.net.*;
import com.mwendasoft.superme.helpers.*;

public class SongEditActivity extends BaseActivity {
    private EditText songEditTitle, songEditLyrics, songAudioEdit;
    private Spinner songEditAuthorSpinner, songEditCategorySpinner;
	private ImageButton btnEditSongAuthor, btnEditSongCategory;
    private Button songEditBtn;
    private SongsDBHelper songsDBHelper;
    private AuthorsDBHelper authorsDBHelper;
    private CategoriesDBHelper categoriesDBHelper;

    private HashMap<String, Integer> authorMap = new HashMap<>();
	private ArrayList<String> authorList = new ArrayList<>();
	private ArrayAdapter<String> authorAdapter;

    private HashMap<String, Integer> categoryMap = new HashMap<>();
	private ArrayList<String> categoryList = new ArrayList<>();
	private ArrayAdapter<String> categoryAdapter;

	private MediaPlayer mediaPlayer;
	private boolean isPaused = false;
	private ImageView currentAudioIcon;
	private File currentAudioFile;
	private Song selectedSong;
	
	private Uri audioUri; // just one audio file
	private final int PICK_AUDIO = 1002;
	
	private String songArtist;
	private int songId, selectedAuthorId, selectedCategoryId, songArtistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_edit_activity);

        songEditTitle = findViewById(R.id.songEditTitle);
        songEditLyrics = findViewById(R.id.songEditLyrics);
        songEditAuthorSpinner = findViewById(R.id.songEditAuthorSpinner);
        songEditCategorySpinner = findViewById(R.id.songEditCategorySpinner);
        songAudioEdit = findViewById(R.id.songAudioEdit);
		songEditBtn = findViewById(R.id.songEditBtn);

		btnEditSongAuthor = findViewById(R.id.btnEditSongAuthor);
		btnEditSongCategory = findViewById(R.id.btnEditSongCategory);

        songsDBHelper = new SongsDBHelper(this);
        authorsDBHelper = new AuthorsDBHelper(this);
        categoriesDBHelper = new CategoriesDBHelper(this);

		authorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, authorList);
		categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryList);

	    songEditAuthorSpinner.setAdapter(authorAdapter);
	    songEditCategorySpinner.setAdapter(categoryAdapter);

		selectedSong = (Song) getIntent().getSerializableExtra("selectedSong");
		songArtist = selectedSong.getSongArtist();
		songId = selectedSong.getId();
		songArtistId = authorsDBHelper.getAuthorIdByName(songArtist);
		
        loadAuthors();
        loadCategories();
		loadSong();
		loadAudioFile();
		
        songEditAuthorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedAuthor = parent.getItemAtPosition(position).toString();
					selectedAuthorId = authorMap.getOrDefault(selectedAuthor, -1);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});

		songEditCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedCategory = parent.getItemAtPosition(position).toString();
					selectedCategoryId = categoryMap.getOrDefault(selectedCategory, -1);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});

		btnEditSongAuthor.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAddAuthorDialog();
				}
			});

		btnEditSongCategory.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAddCategoryDialog();
				}
			});

        songEditBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					saveSong();
				}
			});
			
		songAudioEdit.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFile("audio/*", PICK_AUDIO);
				}
			});
    }
	
	private void pickFile(String type, int requestCode) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("audio/*");
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); // Ensure only one file can be picked
		startActivityForResult(Intent.createChooser(intent, "Select Audio"), PICK_AUDIO);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK && data != null && requestCode == PICK_AUDIO) {
			audioUri = data.getData(); // store directly
			if (audioUri != null) {
				songAudioEdit.setText("1 audio file selected");
				ToastMessagesManager.show(SongEditActivity.this, "Audio file added.");
			}
		}
	}
	
	private void loadSong() {
		Cursor cursor = songsDBHelper.getSongById(songId);

        if (cursor != null && cursor.moveToNext()) {
			int titleIndex = cursor.getColumnIndexOrThrow("title");
            int artistIndex = cursor.getColumnIndexOrThrow("artist");
			int lyricsIndex = cursor.getColumnIndexOrThrow("lyrics");
			int genreIndex = cursor.getColumnIndexOrThrow("genre");
			
			String title = cursor.getString(titleIndex);
			int artistId = cursor.getInt(artistIndex);
			String lyrics = cursor.getString(lyricsIndex);
			int genreId = cursor.getInt(genreIndex);
			
			songEditTitle.setText(title);
			songEditLyrics.setText(lyrics);
			
			
			// Artist Spinner Update
			String artistName = getKeyByValue(authorMap, artistId);
			int artistPos = authorAdapter.getPosition(artistName);
			songEditAuthorSpinner.setSelection(artistPos);
			
			// Genre Spinner Update
			String categName = getKeyByValue(categoryMap, genreId);
			int genrePos = categoryAdapter.getPosition(categName);
			songEditCategorySpinner.setSelection(genrePos);
			
            cursor.close();
        }
	}
	
	//======== START AUDIO LOGIC =========//
	
	private void loadAudioFile() {
		File folder = new File(Environment.getExternalStorageDirectory(), ".superme/songs/Song " + songId);

		LinearLayout audioContainer = findViewById(R.id.songsAudioEditContainer);
		TextView audioLabel = findViewById(R.id.songsAudioTextLabel);
		View songAudioEdit = findViewById(R.id.songAudioEdit); // your add-audio field (e.g. Button or EditText)

		audioContainer.removeAllViews();

		if (folder.exists() && folder.isDirectory()) {
			File[] files = folder.listFiles();
			if (files != null) {
				for (File file : files) {
					String name = file.getName().toLowerCase();
					if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".m4a")) {
						addAudioItem(file, audioContainer);
						audioLabel.setVisibility(View.VISIBLE);
						songAudioEdit.setVisibility(View.GONE); // HIDE the add-audio field
						return; // Stop after first audio
					}
				}
			}
		}

		// No audio file found
		audioLabel.setVisibility(View.GONE);
		songAudioEdit.setVisibility(View.VISIBLE); // SHOW the add-audio field
	}
	
	private void addAudioItem(final File file, LinearLayout container) {
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(8, 8, 8, 8);
		layout.setGravity(Gravity.CENTER_HORIZONTAL);

		// Use an ImageView to represent the audio file visually
		final ImageView audioIcon = new ImageView(this);
		audioIcon.setLayoutParams(new ViewGroup.LayoutParams(150, 150));
		audioIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		audioIcon.setImageResource(android.R.drawable.ic_media_play); // Replace with custom icon if desired

		// Play audio on icon click
		audioIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mediaPlayer == null || currentAudioFile == null || !currentAudioFile.equals(file)) {
						playAudio(file, audioIcon);
						ToastMessagesManager.show(SongEditActivity.this, "Playing audio...");
					} else {
						if (mediaPlayer.isPlaying()) {
							pauseAudio();
							ToastMessagesManager.show(SongEditActivity.this, "Audio paused!");
						} else if (isPaused) {
							resumeAudio();
							ToastMessagesManager.show(SongEditActivity.this, "Audio resumed!");
						}
					}
				}
			});

		// Create delete button
		Button deleteBtn = new Button(this);
		deleteBtn.setText("Remove");
		deleteBtn.setLayoutParams(new ViewGroup.LayoutParams(
									  ViewGroup.LayoutParams.WRAP_CONTENT,
									  ViewGroup.LayoutParams.WRAP_CONTENT
								  ));
		deleteBtn.setPadding(0, 10, 0, 0); // Spacing between icon and button

		deleteBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					confirmDelete(file, new Runnable() {
							@Override
							public void run() {
								if (currentAudioFile != null && currentAudioFile.equals(file)) {
									stopAudio();
								}
								if (file.delete()) {
									loadAudioFile(); // Refresh UI
									ToastMessagesManager.show(SongEditActivity.this, "Audio removed sucessfully.");
								} else {
									ToastMessagesManager.show(SongEditActivity.this, "Failed to delete!");
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
			Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show();
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
	
    private void loadAuthors() {
		authorList.add(getString(R.string.select_artist));
		authorMap.put(getString(R.string.select_artist), -1);

		Cursor cursor = authorsDBHelper.getAllAuthorsWithIds();

		if (cursor == null) {
			authorList.add(getString(R.string.no_artists_available));
			authorMap.put(getString(R.string.no_artists_available), -1);
		} else {
			// Ensure the cursor is fully processed
			if (cursor.getCount() == 0) {
				authorList.add(getString(R.string.no_artists_available));
				authorMap.put(getString(R.string.no_artists_available), -1);
			} else {
				// Process all rows
				while (cursor.moveToNext()) {
					int id = cursor.getInt(cursor.getColumnIndex("id")); // Ensure the correct column index is used
					String name = cursor.getString(cursor.getColumnIndex("author_name")); // Correct column name for the author's name
					authorMap.put(name, id);
					authorList.add(name);
				}
			}
			cursor.close(); // Close cursor here to avoid premature closing
		}

		// Check if authorNames is not empty and add to adapter
		if (authorList.isEmpty()) {
			authorList.add(getString(R.string.no_artists_available)); // Fallback if the list is still empty
		}

		// Setup the adapter and attach it to the spinner
		authorAdapter.notifyDataSetChanged();
		songEditAuthorSpinner.setAdapter(authorAdapter);
	}

    private void loadCategories() {
		categoryList.add(getString(R.string.select_genre));
		categoryMap.put(getString(R.string.select_genre),-1);

		Cursor cursor = categoriesDBHelper.getAllCategoriesWithIds();

		if (cursor == null) {
			categoryList.add(getString(R.string.no_genres_available));
			categoryMap.put(getString(R.string.no_genres_available), -1);
		} else {
			if (cursor.getCount() == 0) {
				categoryList.add(getString(R.string.no_genres_available));
				categoryMap.put(getString(R.string.no_genres_available), -1);
			} else {
				while (cursor.moveToNext()) {
					int id = cursor.getInt(0);
					String name = cursor.getString(1);
					categoryMap.put(name, id);
					categoryList.add(name);
				}
			}
			cursor.close(); // Moved inside the null check
		}

		categoryAdapter.notifyDataSetChanged();
		songEditCategorySpinner.setAdapter(categoryAdapter);
	}

    private void saveSong() {
		String title = songEditTitle.getText().toString().trim();
		String lyrics = songEditLyrics.getText().toString().trim();

		if (title.isEmpty() || lyrics.isEmpty()) {
			ToastMessagesManager.show(this, "Please fill all fields!");
			return;
		}

		if (selectedAuthorId == -1) {
			ToastMessagesManager.show(this, "Select a valid artist!");
			return;
		}

		if (selectedCategoryId == -1) {
			ToastMessagesManager.show(this, "Select a valid category!");
			return;
		}

		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("artist", selectedAuthorId);
		values.put("lyrics", lyrics);
		values.put("genre", selectedCategoryId);

		long rowsUpdated = songsDBHelper.updateSong(songId, values);

		if (rowsUpdated > 0) {
			if (audioUri != null) {
				String folderName = "Song " + songId;
				String savedPath = saveMediaFile(audioUri, folderName, ".mp3");

				if (savedPath != null) {
					int mediaUpdate = songsDBHelper.updateSongMediaPath((int) songId, savedPath);
					if (mediaUpdate > 0) {
						ToastMessagesManager.show(this, "Updated successfully!");
					} else {
						ToastMessagesManager.show(this, "Failed to update audio path!");
					}
				} else {
					ToastMessagesManager.show(this, "Failed to save audio file");
				}
			} else {
				ToastMessagesManager.show(this, "Updated successfully!");
			}

			finish();
		} else {
			ToastMessagesManager.show(this, "Update failed!");
		}
	}
	
	private String saveMediaFile(Uri uri, String songFolderName, String extension) {
		File mediaFolder = getSongMediaFolder(songFolderName);
		String fileName = "song_media_" + System.currentTimeMillis() + extension;
		File file = copyUriToFile(uri, mediaFolder, fileName);

		if (file != null) {
			return file.getAbsolutePath();
		} else {
			return null;
		}
	}

	private File getSongMediaFolder(String songFolderName) {
		File baseFolder = new File(
			Environment.getExternalStorageDirectory(),
			".superme/songs/" + songFolderName
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

	private void showAddAuthorDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.add_artist));

		// Convert 55dp to pixels dynamically
		int heightInDp = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, 55, 
			Resources.getSystem().getDisplayMetrics()
		);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, // width
			heightInDp  // height
		);

		int marginInPx = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics() // Convert 20dp to px
		);

		params.setMargins(0, 0, 0, marginInPx); // values in pixels

		// Create a layout for the dialog
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(20, 20, 20, 20);

		// Author Name Input
		final EditText artistNameInput = new EditText(this);
		artistNameInput.setBackgroundResource(R.drawable.edit_text_border);
		artistNameInput.setHint(getString(R.string.enter_artist_hint));
		artistNameInput.setLayoutParams(params);

		layout.addView(artistNameInput);

		// Author Category Spinner
		final Spinner categorySpinner = new Spinner(this);
		categorySpinner.setBackgroundResource(R.drawable.edit_text_border);
		categorySpinner.setLayoutParams(params);
		String[] categories = {"Poet", "Novelist", "Songwriter", "Playwright", "Other"};
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
		categorySpinner.setAdapter(adapter);
		layout.addView(categorySpinner);

		builder.setView(layout);

		builder.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String artistName = artistNameInput.getText().toString().trim();
					String authorCategory = categorySpinner.getSelectedItem().toString();

					if (!artistName.isEmpty()) {
						Cursor cursor = authorsDBHelper.getAuthorByName(artistName);
						if (cursor != null) {
						    if (cursor.getCount() > 0) {
								ToastMessagesManager.show(SongEditActivity.this, "Artist already exists!");
							} else {
								authorsDBHelper.insertAuthor(artistName, authorCategory);
								int authorId = authorsDBHelper.getAuthorIdByName(artistName);
							    authorList.add(artistName);
								authorMap.put(artistName, authorId);
								authorAdapter.notifyDataSetChanged();

								// Use indexOf to set correct selection
								int newIndex = authorList.indexOf(artistName);
								if (newIndex != -1) {
								    songEditAuthorSpinner.setSelection(newIndex);
								}
							}
							cursor.close(); // Only close once
						}
					}
				}
			});

		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

		builder.setCancelable(false);
		builder.show();
	}

	private void showAddCategoryDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.add_genre));

		// Convert 55dp to pixels dynamically
		int heightInDp = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, 55, 
			Resources.getSystem().getDisplayMetrics()
		);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, // width
			heightInDp  // height
		);

		int marginInPx = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics() // Convert 20dp to px
		);

		params.setMargins(0, 0, 0, marginInPx); // values in pixels

		// Create a layout for the dialog
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(20, 20, 20, 20);

		// Category Name Input
		final EditText categoryInput = new EditText(this);
		categoryInput.setBackgroundResource(R.drawable.edit_text_border);
		categoryInput.setLayoutParams(params);
		categoryInput.setHint(getString(R.string.enter_genre_hint));

		layout.addView(categoryInput);

		builder.setView(layout);

		builder.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String newCategory = categoryInput.getText().toString().trim();

					if (!newCategory.isEmpty()) {
						// Check if category exists
						Cursor cursor = categoriesDBHelper.getCategoryByName(newCategory);
						if (cursor != null) {
							if (cursor.getCount() > 0) {
								ToastMessagesManager.show(SongEditActivity.this, "Genre already exists!");
							} else {
								categoriesDBHelper.insertCategory(newCategory);
								int categoryId = categoriesDBHelper.getCategoryIdByName(newCategory);
								categoryList.add(newCategory);
								categoryMap.put(newCategory, categoryId);

								// Recreate the adapter with the updated list
								categoryAdapter = new ArrayAdapter<>(SongEditActivity.this, android.R.layout.simple_spinner_dropdown_item, categoryList);
								categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
								songEditCategorySpinner.setAdapter(categoryAdapter);

								// Set the selection to the newly added category
								int newIndex = categoryList.indexOf(newCategory);
								if (newIndex != -1) {
									songEditCategorySpinner.setSelection(newIndex);
								}
							}
							cursor.close();
						}
					}
				}
			});

		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

		builder.setCancelable(false);
		builder.show();
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

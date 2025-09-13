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
import android.net.*;
import java.util.*;
import java.io.*;
import android.os.*;
import com.mwendasoft.superme.helpers.*;

public class SongAddActivity extends BaseActivity {
    private EditText songAddTitle, songAddLyrics, songAudioAdd;
    private Spinner songAddAuthorSpinner, songAddCategorySpinner;
	private ImageButton btnAddSongAuthor, btnAddSongCategory;
    private Button songAddBtn;
    private SongsDBHelper songsDBHelper;
    private AuthorsDBHelper authorsDBHelper;
    private CategoriesDBHelper categoriesDBHelper;

    private HashMap<String, Integer> authorMap = new HashMap<>();
	private ArrayList<String> authorList = new ArrayList<>();
	private ArrayAdapter<String> authorAdapter;
	
    private HashMap<String, Integer> categoryMap = new HashMap<>();
	private ArrayList<String> categoryList = new ArrayList<>();
	private ArrayAdapter<String> categoryAdapter;
	
	private Uri audioUri; // just one audio file
	private final int PICK_AUDIO = 1002;
	
	private int selectedAuthorId, selectedCategoryId;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_add_activity);

        songAddTitle = findViewById(R.id.songAddTitle);
        songAddLyrics = findViewById(R.id.songAddLyrics);
        songAddAuthorSpinner = findViewById(R.id.songAddAuthorSpinner);
        songAddCategorySpinner = findViewById(R.id.songAddCategorySpinner);
        songAddBtn = findViewById(R.id.songAddBtn);
		
		songAudioAdd = findViewById(R.id.songAudioAdd);
        
		btnAddSongAuthor = findViewById(R.id.btnAddSongAuthor);
		btnAddSongCategory = findViewById(R.id.btnAddSongCategory);

        songsDBHelper = new SongsDBHelper(this);
        authorsDBHelper = new AuthorsDBHelper(this);
        categoriesDBHelper = new CategoriesDBHelper(this);

		authorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, authorList);
		categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryList);
		
	    songAddAuthorSpinner.setAdapter(authorAdapter);
	    songAddCategorySpinner.setAdapter(categoryAdapter);
		
        loadAuthors();
        loadCategories();
        
        songAddAuthorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedAuthor = parent.getItemAtPosition(position).toString();
					selectedAuthorId = authorMap.getOrDefault(selectedAuthor, -1);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});

		songAddCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedCategory = parent.getItemAtPosition(position).toString();
					selectedCategoryId = categoryMap.getOrDefault(selectedCategory, -1);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});
			
		btnAddSongAuthor.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAddAuthorDialog();
				}
			});

		btnAddSongCategory.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAddCategoryDialog();
				}
			});
			
		songAudioAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFile("audio/*", PICK_AUDIO);
				}
			});

        songAddBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					saveSong();
				}
			});
    }
	
	// === END CODE FOR ADDING MEDIA === //

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
				songAudioAdd.setText("1 audio file selected");
				ToastMessagesManager.show(SongAddActivity.this, "Audio file added!");
			}
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
		songAddAuthorSpinner.setAdapter(authorAdapter);
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
		songAddCategorySpinner.setAdapter(categoryAdapter);
	}

    private void saveSong() {
		String title = songAddTitle.getText().toString().trim();
		String lyrics = songAddLyrics.getText().toString().trim();

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

		long songId = songsDBHelper.insertSong(values);
		
		if (songId != -1 && audioUri != null) {
			String folderName = "Song " + songId;

			String savedPath = saveMediaFile(audioUri, folderName, ".mp3");

			if (savedPath != null) {
				songsDBHelper.insertSongMediaPath((int) songId, savedPath);
				ToastMessagesManager.show(this, "Added successfully.");
			} else {
				ToastMessagesManager.show(this, "Failed to add!");
			}

			finish();
		} else {
			ToastMessagesManager.show(this, "Audio file not added. You can choose and add later.");
			finish();
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
								ToastMessagesManager.show(SongAddActivity.this, "Artist already exists!");
							} else {
								authorsDBHelper.insertAuthor(artistName, authorCategory);
								int authorId = authorsDBHelper.getAuthorIdByName(artistName);
							    authorList.add(artistName);
								authorMap.put(artistName, authorId);
								authorAdapter.notifyDataSetChanged();

								// Use indexOf to set correct selection
								int newIndex = authorList.indexOf(artistName);
								if (newIndex != -1) {
								    songAddAuthorSpinner.setSelection(newIndex);
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
								ToastMessagesManager.show(SongAddActivity.this, "Genre already esists!");
							} else {
								categoriesDBHelper.insertCategory(newCategory);
								int categoryId = categoriesDBHelper.getCategoryIdByName(newCategory);
								categoryList.add(newCategory);
								categoryMap.put(newCategory, categoryId);

								// Recreate the adapter with the updated list
								categoryAdapter = new ArrayAdapter<>(SongAddActivity.this, android.R.layout.simple_spinner_dropdown_item, categoryList);
								categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
								songAddCategorySpinner.setAdapter(categoryAdapter);

								// Set the selection to the newly added category
								int newIndex = categoryList.indexOf(newCategory);
								if (newIndex != -1) {
									songAddCategorySpinner.setSelection(newIndex);
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
}

package com.mwendasoft.superme.poems;

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
import java.io.*;
import android.os.*;
import com.mwendasoft.superme.helpers.*;

public class PoemAddActivity extends BaseActivity {
    private EditText poemAddTitle, poemAddPoemText, poemImageAdd;
    private Spinner poemAddPoetSpinner;
	private ImageButton btnAddPoet;
    private Button poemAddBtn;
    private PoemsDBHelper poemsDBHelper;
    private AuthorsDBHelper authorsDBHelper;
    
    private HashMap<String, Integer> authorMap = new HashMap<>();
	private ArrayList<String> authorList = new ArrayList<>();
	private ArrayAdapter<String> authorAdapter;

	private Uri imageUri; // Store single image URI instead of a list
	private static final int PICK_IMAGE = 1001;
	
    private int selectedAuthorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poem_add_activity);

        poemAddTitle = findViewById(R.id.poemAddTitle);
        poemAddPoemText = findViewById(R.id.poemAddPoemText);
        poemAddPoetSpinner = findViewById(R.id.poemAddPoetSpinner);
        poemAddBtn = findViewById(R.id.poemAddBtn);
	    poemImageAdd = findViewById(R.id.poemImageAdd);
		
		btnAddPoet = findViewById(R.id.btnAddPoet);
		
        poemsDBHelper = new PoemsDBHelper(this);
        authorsDBHelper = new AuthorsDBHelper(this);
        
		authorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, authorList);
	    poemAddPoetSpinner.setAdapter(authorAdapter);
	    
        loadAuthors();
        
		poemAddPoetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedAuthor = parent.getItemAtPosition(position).toString();
					selectedAuthorId = authorMap.getOrDefault(selectedAuthor, -1);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});

		btnAddPoet.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAddAuthorDialog();
				}
			});

		poemAddBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					savePoem();
				}
			});
			
		poemImageAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickImage();
				}
			});
    }
	
	//#######
    private void pickImage() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		// Remove the multiple selection option
		startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
			// Clear any previous selection
			imageUri = null;

			// Get the single image URI
			if (data.getData() != null) {
				imageUri = data.getData();

				// Update UI
				poemImageAdd.setText("1 image selected");
			}
		}
	}
	//#######

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
		poemAddPoetSpinner.setAdapter(authorAdapter);
	}

    private void savePoem() {
		String title = poemAddTitle.getText().toString().trim();
		String poemText = poemAddPoemText.getText().toString().trim();

		if (title.isEmpty()) {
			ToastMessagesManager.show(this, "Please fill all the fields!");
			return;
		}

		if (selectedAuthorId == -1) {
			ToastMessagesManager.show(this, "Select a valid author!");
			return;
		}

		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("poet", selectedAuthorId);
		values.put("poem", poemText);

		long poemId = poemsDBHelper.insertPoem(values);

		if (poemId != -1) {
			if (imageUri != null) {
				try {
					String folderName = "Poem " + poemId;
					String savedImagePath = saveImageFile(imageUri, folderName);

					if (savedImagePath != null) {
						poemsDBHelper.insertPoemMediaPath((int) poemId, savedImagePath);
					}

					// Clear and reset UI
					imageUri = null;
					poemImageAdd.setText(getString(R.string.add_images));
					ToastMessagesManager.show(this, "Added successfully!");
				} catch (IOException e) {
					ToastMessagesManager.show(this, "Adding failed!");
					e.printStackTrace();
				}
			}
			finish();
		}
	}

	private String saveImageFile(Uri uri, String poemFolderName) throws IOException {
		File mediaFolder = getPoemMediaFolder(poemFolderName);
		String fileName = "poem_media_" + System.currentTimeMillis() + ".jpg";
		File destinationFile = new File(mediaFolder, fileName);

		InputStream inputStream = getContentResolver().openInputStream(uri);
		OutputStream outputStream = new FileOutputStream(destinationFile);

		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) > 0) {
			outputStream.write(buffer, 0, length);
		}

		inputStream.close();
		outputStream.close();

		return destinationFile.getAbsolutePath();
	}

	private File getPoemMediaFolder(String poemFolderName) throws IOException {
		File baseFolder = new File(
			Environment.getExternalStorageDirectory(),
			".superme/poems/" + poemFolderName
		);

		if (!baseFolder.exists() && !baseFolder.mkdirs()) {
			throw new IOException("Failed to create directory");
		}
		return baseFolder;
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
		final EditText authorNameInput = new EditText(this);
		authorNameInput.setBackgroundResource(R.drawable.edit_text_border);
		authorNameInput.setHint(getString(R.string.enter_artist_hint));
		authorNameInput.setLayoutParams(params);
		layout.addView(authorNameInput);

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
					String artistName = authorNameInput.getText().toString().trim();
					String authorCategory = categorySpinner.getSelectedItem().toString();

					if (!artistName.isEmpty()) {
						Cursor cursor = authorsDBHelper.getAuthorByName(artistName);
						if (cursor != null) {
						    if (cursor.getCount() > 0) {
								ToastMessagesManager.show(PoemAddActivity.this, "Author already exists!");
							} else {
								authorsDBHelper.insertAuthor(artistName, authorCategory);
								int authorId = authorsDBHelper.getAuthorIdByName(artistName);
							    authorList.add(artistName);
								authorMap.put(artistName, authorId);
								authorAdapter.notifyDataSetChanged();

								// Use indexOf to set correct selection
								int newIndex = authorList.indexOf(artistName);
								if (newIndex != -1) {
								    poemAddPoetSpinner.setSelection(newIndex);
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
}

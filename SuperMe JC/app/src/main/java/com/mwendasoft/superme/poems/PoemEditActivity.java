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
import android.view.*;
import android.graphics.*;
import com.mwendasoft.superme.helpers.*;

public class PoemEditActivity extends BaseActivity {
    private EditText poemTitleEdit, poemTextEdit, poemImageEdit;
    private Spinner poemEditPoetSpinner;
	private ImageButton poetEditBtn;
    private Button poemEditSaveBtn;
    private PoemsDBHelper poemsDBHelper;
    private AuthorsDBHelper authorsDBHelper;

    private HashMap<String, Integer> authorMap = new HashMap<>();
	private ArrayList<String> authorList = new ArrayList<>();
	private ArrayAdapter<String> authorAdapter;

	private Uri imageUri; // Store single image URI instead of a list
	private static final int PICK_IMAGE = 1001;
	
    private int selectedAuthorId;
	private Poem selectedPoem;
	private String poemTitle, poemAuthor;
    private int poetId, poemId;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poem_edit_activity);

        poemTitleEdit = findViewById(R.id.poemTitleEdit);
        poemTextEdit = findViewById(R.id.poemTextEdit);
        poemEditPoetSpinner = findViewById(R.id.poemEditPoetSpinner);
        poemEditSaveBtn = findViewById(R.id.poemEditSaveBtn);
		poemImageEdit = findViewById(R.id.poemImageEdit);
		
		poetEditBtn = findViewById(R.id.poetEditBtn);

        poemsDBHelper = new PoemsDBHelper(this);
        authorsDBHelper = new AuthorsDBHelper(this);

		authorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, authorList);
	    poemEditPoetSpinner.setAdapter(authorAdapter);

		selectedPoem = (Poem) getIntent().getSerializableExtra("selectedPoem");
		poemId = selectedPoem.getId();
		poemTitle = getIntent().getStringExtra("poemTitle");
		poemAuthor = getIntent().getStringExtra("poemAuthor");
		poetId = authorsDBHelper.getAuthorIdByName(poemAuthor);
		
        loadAuthors();
		loadPoem();
		loadImageFile();
		
		poemEditPoetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedAuthor = parent.getItemAtPosition(position).toString();
					selectedAuthorId = authorMap.getOrDefault(selectedAuthor, -1);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});

		poetEditBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAddAuthorDialog();
				}
			});

		poemEditSaveBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					savePoem();
				}
			});
			
		poemImageEdit.setOnClickListener(new View.OnClickListener() {
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
				poemImageEdit.setText("1 image selected");
		    }
		}
	}
	
	//#######
	
	private void loadImageFile() {
		File folder = new File(Environment.getExternalStorageDirectory(), ".superme/poems/Poem " + poemId);
		LinearLayout imageContainer = findViewById(R.id.poemImageEditContainer);
		imageContainer.removeAllViews();

		boolean imageFound = false;

		if (folder.exists() && folder.isDirectory()) {
			File[] files = folder.listFiles();
			if (files != null) {
				for (File file : files) {
					String name = file.getName().toLowerCase();
					if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")) {
						addImageItem(file, imageContainer);
						imageFound = true;
						break;
					}
				}
			}
		}

		poemImageEdit.setVisibility(imageFound ? View.GONE : View.VISIBLE);
	}

	private void addImageItem(final File file, LinearLayout container) {
		// Create container layout
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(8, 8, 8, 8);
		layout.setGravity(Gravity.CENTER_HORIZONTAL);

		// Create and configure ImageView
		ImageView imageView = new ImageView(this);
		LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
			200,  // Fixed width in pixels
			200   // Fixed height in pixels
		);
		imageView.setLayoutParams(imageParams);
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

		// Load image
		try {
			Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
			}
		} catch (Exception e) {
			// If image fails to load, we'll just show an empty ImageView
		}

		// Create delete button
		Button deleteBtn = new Button(this);
		deleteBtn.setText("Remove");
		deleteBtn.setLayoutParams(new ViewGroup.LayoutParams(
									  ViewGroup.LayoutParams.WRAP_CONTENT,
									  ViewGroup.LayoutParams.WRAP_CONTENT
								  ));
		deleteBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDeleteConfirmation(file);
				}
			});

		// Add views to layout
		layout.addView(imageView);
		layout.addView(deleteBtn);
		container.addView(layout);
	}

	private void showDeleteConfirmation(final File file) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Delete Image");
		builder.setMessage("Are you sure you want to delete this image?");
		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (file.delete()) {
						loadImageFile(); // Refresh the view
						ToastMessagesManager.show(PoemEditActivity.this, "Image deleted successfully.");
					} else {
						ToastMessagesManager.show(PoemEditActivity.this, "Delete failed!");
					}
				}
			});
		builder.setNegativeButton("Cancel", null);
		builder.show();
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
		poemEditPoetSpinner.setAdapter(authorAdapter);
	}
	
	private void loadPoem() {
		Cursor cursor = poemsDBHelper.getPoemById(poemId);

        if (cursor != null && cursor.moveToNext()) {
			int titleIndex = cursor.getColumnIndexOrThrow("title");
            int poemIndex = cursor.getColumnIndexOrThrow("poem");
			int poetIndex = cursor.getColumnIndexOrThrow("poet");
			
			String title = cursor.getString(titleIndex);
			int poetId = cursor.getInt(poetIndex);
			String poem = cursor.getString(poemIndex);
			
			poemTitleEdit.setText(title);
			poemTextEdit.setText(poem);

			// Artist Spinner Update
			String poetName = getKeyByValue(authorMap, poetId);
			int poetPos = authorAdapter.getPosition(poetName);
			poemEditPoetSpinner.setSelection(poetPos);

            cursor.close();
        }
	}

    private void savePoem() {
		String title = poemTitleEdit.getText().toString().trim();
		String poemText = poemTextEdit.getText().toString().trim();

		if (title.isEmpty()) {
			ToastMessagesManager.show(this, "Please fill all the field!");
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

		long rowsUpdated = poemsDBHelper.updatePoem(poemId, values);
		
		if (rowsUpdated != -1) {
			if (imageUri != null) {
				try {
					String folderName = "Poem " + poemId;
					String savedImagePath = saveImageFile(imageUri, folderName);

					if (savedImagePath != null) {
						poemsDBHelper.updatePoemMediaPath((int) poemId, savedImagePath);
					}

					// Clear and reset UI
					imageUri = null;
					poemImageEdit.setText(getString(R.string.add_images));
					ToastMessagesManager.show(this, "Updated successfully!");
				} catch (IOException e) {
					ToastMessagesManager.show(this, "Failed to save image!");
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
								ToastMessagesManager.show(PoemEditActivity.this, "Poet already exists!");
							} else {
								authorsDBHelper.insertAuthor(artistName, authorCategory);
								int authorId = authorsDBHelper.getAuthorIdByName(artistName);
							    authorList.add(artistName);
								authorMap.put(artistName, authorId);
								authorAdapter.notifyDataSetChanged();

								// Use indexOf to set correct selection
								int newIndex = authorList.indexOf(artistName);
								if (newIndex != -1) {
								    poemEditPoetSpinner.setSelection(newIndex);
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
	
	private String getKeyByValue(HashMap<String, Integer> map, int value) {
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			if (entry.getValue() == value) {
				return entry.getKey();
			}
		}
		return null;
	}
}

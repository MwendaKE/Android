package com.mwendasoft.superme.authors;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.BaseActivity;
import android.app.*;
import android.database.*;
import android.content.*;
import android.widget.*;
import android.view.*;
import com.mwendasoft.superme.events.*;
import com.mwendasoft.superme.books.*;
import android.graphics.*;
import com.mwendasoft.superme.sumrys.*;
import java.util.*;

public class AuthorsActivity extends BaseActivity {
    private AuthorsDBHelper dbHelper;
	private BooksDBHelper booksDbHelper;
	private SumrysDBHelper sumrysDbHelper;
    private ListView authorsListView;
    private TextView authorsListTitle, authorsCountBadge;
    private ImageButton addAuthorFab;

    private AuthorsAdapter authorsAdapter;
    private final ArrayList<Author> authors = new ArrayList<>(); // Initialize as final

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authors_activity);

        initializeViews();
        setupDatabaseHelpers();
        setupAdapters();
        setupListeners();

        loadAuthors();
    }

    private void initializeViews() {
        authorsListTitle = findViewById(R.id.authorsListTitle);
        authorsCountBadge = findViewById(R.id.authorsCountBadge);
        authorsListView = findViewById(R.id.authorsListView);
        addAuthorFab = findViewById(R.id.addAuthorFab);

    }

    private void setupDatabaseHelpers() {
        dbHelper = new AuthorsDBHelper(this);
		booksDbHelper = new BooksDBHelper(this);
		sumrysDbHelper = new SumrysDBHelper(this);
     }

    private void setupAdapters() {
        authorsAdapter = new AuthorsAdapter(this, authors);
        authorsListView.setAdapter(authorsAdapter);
    }

    private void setupListeners() {
        authorsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Author author = authors.get(position);
					showBooksByAuthorDialog(author.getName());
				}
			});
			
	    // Long click to edit the author
		authorsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					// Get the clicked author
					Author author = authors.get(position);

					// Show edit dialog
					showEditAuthorDialog(author);

					// Return true to indicate the long click was handled
					return true;
				}
			});

        addAuthorFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAddAuthorDialog();
				}
			});
    }
	
    private void loadAuthors() {
        authors.clear();

        Cursor cursor = null;
        try {
            cursor = dbHelper.getAllAuthors();
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow("id");
                int nameIndex = cursor.getColumnIndexOrThrow("author_name");
                int occupationIndex = cursor.getColumnIndexOrThrow("author_categ");
                
                do {
                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    String occupation = cursor.getString(occupationIndex);
                    
                    if (name != null && !name.isEmpty()) {
                        authors.add(new Author(id, name, occupation));
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
					authorsListTitle.setText("AUTHORS");
					authorsCountBadge.setText(String.valueOf(authors.size()));

					if (authors.isEmpty()) {
						showNoAuthorsDialog();
					} else if (authorsAdapter != null) {
						authorsAdapter.notifyDataSetChanged();
					}
				}
			});
    }
	
	private void showBooksByAuthorDialog(String authorName) {
		List<String> books = booksDbHelper.getBooksByAuthor(authorName);
		List<String> sumrys = sumrysDbHelper.getBooksByAuthor(authorName);
		
		books.addAll(sumrys);
		
		// int bookCount = booksDbHelper.getBooksCountByAuthor(authorName);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(authorName + "  (" + books.size() + ")");

		if (books.isEmpty()) {
			builder.setMessage("No books found for this author");
		} else {
			// Create a custom view with a ListView
			LayoutInflater inflater = getLayoutInflater();
			View dialogView = inflater.inflate(R.layout.dialog_authors_list_work, null);

			ListView listView = dialogView.findViewById(R.id.authorWorkListView);
			ArrayAdapter<String> adapter = new ArrayAdapter<>(
				this,
				android.R.layout.simple_list_item_1,
				books
			);
			listView.setAdapter(adapter);
			builder.setView(dialogView);
		}

		builder.setPositiveButton("Close", null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private void showAddAuthorDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Add Author");

		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_add_author, null);

		final EditText authorNameInput = dialogView.findViewById(R.id.diaogAddAuthorName);
		final Spinner authorCategorySpinner = dialogView.findViewById(R.id.dialogAddAuthorCategorySpinner);

		final String[] categories = {
			"Novelist",
			"Playwright",
			"Songwriter",
			"Poet",
			"Essayist",
			"Biographer",
			"Screenwriter",
			"Journalist",
			"Historian",
			"Memoirist",
			"Columnist",
			"Critic",
			"Blogger",
			"Children's Author",
			"Fantasy Author",
			"Science Fiction Author",
			"Horror Writer",
			"Mystery Writer",
			"Romance Author",
			"Thriller Author",
			"Non-fiction Author",
			"Technical Writer",
			"Academic Writer",
			"Spiritual Writer",
			"Travel Writer",
			"Speechwriter",
			"Comic Book Writer",
			"Satirist",
			"Lyricist",
			"Ghostwriter",
			"Other"
		};
		
		ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
		categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		authorCategorySpinner.setAdapter(categoryAdapter);

		builder.setView(dialogView);

		// Create the dialog first
		final AlertDialog dialog = builder.create();

		// Set Add button
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add", (DialogInterface.OnClickListener) null);

		// Set Cancel button
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface d, int which) {
					dialog.dismiss();
				}
			});

		dialog.setCancelable(false);
		dialog.show();

		// After showing, override Add button behavior
		Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		addButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String authorName = authorNameInput.getText().toString().trim();
					String authorCategory = authorCategorySpinner.getSelectedItem().toString();

					if (!authorName.isEmpty()) {
						Cursor cursor = dbHelper.getAuthorByName(authorName);
						if (cursor != null) {
							if (cursor.getCount() > 0) {
								Toast.makeText(AuthorsActivity.this, "Author already exists", Toast.LENGTH_SHORT).show();
							} else {
								dbHelper.insertAuthor(authorName, authorCategory);
								Toast.makeText(AuthorsActivity.this, "Added author '" + authorName + "'", Toast.LENGTH_SHORT).show();
								loadAuthors();
								dialog.dismiss(); // ✅ Close dialog only after success
							}
							cursor.close();
						}
					} else {
						Toast.makeText(AuthorsActivity.this, "Please enter author name", Toast.LENGTH_SHORT).show();
					}
				}
			});
	}
	
	private void showEditAuthorDialog(final Author author) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Edit Author");

		// Inflate your layout
		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_edit_author, null);

		// Get views
		final EditText authorNameInput = dialogView.findViewById(R.id.diaogEditAuthorName);
		final Spinner categorySpinner = dialogView.findViewById(R.id.dialogEditAuthorCategorySpinner);

		// Set current values
		authorNameInput.setText(author.getName());

		final String[] categories = {
			"Novelist",
			"Playwright",
			"Songwriter",
			"Poet",
			"Essayist",
			"Biographer",
			"Screenwriter",
			"Journalist",
			"Historian",
			"Memoirist",
			"Columnist",
			"Critic",
			"Blogger",
			"Children's Author",
			"Fantasy Author",
			"Science Fiction Author",
			"Horror Writer",
			"Mystery Writer",
			"Romance Author",
			"Thriller Author",
			"Non-fiction Author",
			"Technical Writer",
			"Academic Writer",
			"Spiritual Writer",
			"Travel Writer",
			"Speechwriter",
			"Comic Book Writer",
			"Satirist",
			"Lyricist",
			"Ghostwriter",
			"Other"
		};
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categorySpinner.setAdapter(adapter);

		// Set current category in spinner
		int position = Arrays.asList(categories).indexOf(author.getOccupation());
		if (position >= 0) {
			categorySpinner.setSelection(position);
		}

		builder.setView(dialogView);

		// Create dialog first
		final AlertDialog dialog = builder.create();

		// Set buttons
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Update", (DialogInterface.OnClickListener) null);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialog.dismiss();
				}
			});

		dialog.setCancelable(false);
		dialog.show();

		// Override Save button click
		Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		saveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String newName = authorNameInput.getText().toString().trim();
					String newCategory = categorySpinner.getSelectedItem().toString();

					if (newName.isEmpty()) {
						Toast.makeText(AuthorsActivity.this, "Please enter author name", Toast.LENGTH_SHORT).show();
						return;
					}

					// Update author
					ContentValues values = new ContentValues();
					values.put("author_name", newName);
					values.put("author_categ", newCategory);
					
					int updated = dbHelper.updateAuthor(author.getId(), values);

					if (updated > 0) {
						Toast.makeText(AuthorsActivity.this, "Author updated", Toast.LENGTH_SHORT).show();
						loadAuthors();
						dialog.dismiss();
					} else {
						Toast.makeText(AuthorsActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
					}
				}
			});
	}
	
    private void showNoAuthorsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Authors");
        builder.setMessage("No authors found. Would you like to add a new one?");
        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(AuthorsActivity.this, SumryAddActivity.class));
				}
			});
        builder.setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
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
        loadAuthors();
    }
}

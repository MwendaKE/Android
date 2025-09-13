package com.mwendasoft.superme.books;

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
import com.mwendasoft.superme.helpers.*;

public class AddBookActivity extends BaseActivity {
    private EditText bookAddEditTitle, bookAddEditReview;
    private Spinner bookAddAuthorSpinner, bookAddCategorySpinner, bookAddReadSpinner;
	private ImageButton btnAddBookAuthor, btnAddBookCategory;
    private Button bookAddSaveBtn;
    private BooksDBHelper booksDBHelper;
    private AuthorsDBHelper authorsDBHelper;
    private CategoriesDBHelper categoriesDBHelper;

    private HashMap<String, Integer> authorMap = new HashMap<>();
    private HashMap<String, Integer> categoryMap = new HashMap<>();
    private HashMap<String, Integer> bookReadMap = new HashMap<>();

	private ArrayList<String> authorList = new ArrayList<>();
	private ArrayList<String> categoryList = new ArrayList<>();
	private ArrayList<String> readOptionsList = new ArrayList<>();
	
	private ArrayAdapter<String> authorAdapter;
	private ArrayAdapter<String> categoryAdapter;
	private ArrayAdapter<String> readOptionsAdapter;
	
	private int selectedAuthorId, selectedCategoryId, selectedBookRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_book_activity);

        bookAddEditTitle = findViewById(R.id.bookAddEditTitle);
        bookAddEditReview = findViewById(R.id.bookAddEditReview);
        bookAddReadSpinner = findViewById(R.id.bookAddReadSpinner);
        bookAddAuthorSpinner = findViewById(R.id.bookAddAuthorSpinner);
        bookAddCategorySpinner = findViewById(R.id.bookAddCategorySpinner);
        bookAddSaveBtn = findViewById(R.id.bookAddSaveBtn);
		
		btnAddBookAuthor = findViewById(R.id.btnAddBookAuthor);
		btnAddBookCategory = findViewById(R.id.btnAddBookCategory);

        booksDBHelper = new BooksDBHelper(this);
        authorsDBHelper = new AuthorsDBHelper(this);
        categoriesDBHelper = new CategoriesDBHelper(this);

		authorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, authorList);
		categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryList);
		readOptionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, readOptionsList);
		
	    bookAddAuthorSpinner.setAdapter(authorAdapter);
	    bookAddCategorySpinner.setAdapter(categoryAdapter);
		bookAddReadSpinner.setAdapter(readOptionsAdapter);
		
        loadAuthors();
        loadCategories();
        loadBookReadOptions();
	
        bookAddAuthorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedAuthor = parent.getItemAtPosition(position).toString();
					selectedAuthorId = authorMap.getOrDefault(selectedAuthor, -1);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});
		
		bookAddCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedCategory = parent.getItemAtPosition(position).toString();
					selectedCategoryId = categoryMap.getOrDefault(selectedCategory, -1);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});
			
		
        bookAddReadSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedStatus = parent.getItemAtPosition(position).toString();
					selectedBookRead = bookReadMap.getOrDefault(selectedStatus, 0);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});
			
		btnAddBookAuthor.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAddAuthorDialog();
				}
			});

		btnAddBookCategory.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAddCategoryDialog();
				}
			});
			
        bookAddSaveBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					saveBook();
				}
			});
    }

    private void loadAuthors() {
		authorList.add("--Select an author--");
		authorMap.put("--Select an author--", -1);
		
		Cursor cursor = authorsDBHelper.getAllAuthorsWithIds();
		
		if (cursor == null) {
			showErrorDialog("Failed to load authors. Database error.");
			authorList.add("No Authors Available");
			authorMap.put("No Authors Available", -1);
		} else {
			// Ensure the cursor is fully processed
			if (cursor.getCount() == 0) {
				authorList.add("No Authors Available");
				authorMap.put("No Authors Available", -1);
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
			authorList.add("No Authors Available"); // Fallback if the list is still empty
		}
		
		// Setup the adapter and attach it to the spinner
		authorAdapter.notifyDataSetChanged();
		bookAddAuthorSpinner.setAdapter(authorAdapter);
	}

    private void loadCategories() {
		categoryList.add("--Select a category--");
		
		Cursor cursor = categoriesDBHelper.getAllCategoriesWithIds();

		if (cursor == null) {
			showErrorDialog("Failed to load categories. Database error.");
			categoryList.add("No Categories Available");
			categoryMap.put("No Categories Available", -1);
		} else {
			if (cursor.getCount() == 0) {
				categoryList.add("No Categories Available");
				categoryMap.put("No Categories Available", -1);
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
		bookAddCategorySpinner.setAdapter(categoryAdapter);
	}

    private void loadBookReadOptions() {
        readOptionsList.add("Read");
        readOptionsList.add("Reading");
        readOptionsList.add("Not read");

        bookReadMap.put("Read", 1);
        bookReadMap.put("Reading", 2);
        bookReadMap.put("Not read", 0);
		
		// Notify the adapter in case the data has changed
		readOptionsAdapter.notifyDataSetChanged();
		bookAddReadSpinner.setAdapter(readOptionsAdapter);
    }

    private void saveBook() {
		String title = bookAddEditTitle.getText().toString().trim();
		String review = bookAddEditReview.getText().toString().trim();

		if (title.isEmpty() || review.isEmpty()) {
			ToastMessagesManager.show(this, "Please fill all fields!");
			return;
		}

		if (selectedAuthorId == -1) {
			ToastMessagesManager.show(this, "Please select a valid author!");
			return;
		}

		if (selectedCategoryId == -1) {
			ToastMessagesManager.show(this, "Please select a valid category!");
			return;
		}

		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("author", selectedAuthorId);
		values.put("review", review);
		values.put("read", selectedBookRead);
		values.put("category", selectedCategoryId);

		long result = booksDBHelper.insertBook("books", null, values);
		ToastMessagesManager.show(this, result != -1 ? "Added successfully." : "Adding failed!");
		if (result != -1) finish();
	}
	
	private void showAddAuthorDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Add Author");

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
		final EditText inputName = new EditText(this);
		inputName.setBackgroundResource(R.drawable.edit_text_border);
		inputName.setHint("Enter Author Name");
		inputName.setLayoutParams(params);

		layout.addView(inputName);

		// Author Category Spinner
		final Spinner categorySpinner = new Spinner(this);
		categorySpinner.setBackgroundResource(R.drawable.edit_text_border);
		categorySpinner.setLayoutParams(params);
		String[] categories = {"Poet", "Novelist", "Songwriter", "Playwright", "Other"};
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
		categorySpinner.setAdapter(adapter);
		layout.addView(categorySpinner);

		builder.setView(layout);

		builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String authorName = inputName.getText().toString().trim();
					String authorCategory = categorySpinner.getSelectedItem().toString();

					if (!authorName.isEmpty()) {
						Cursor cursor = authorsDBHelper.getAuthorByName(authorName);
						if (cursor != null) {
						    if (cursor.getCount() > 0) {
								ToastMessagesManager.show(AddBookActivity.this, "Author already exists!");
							} else {
								authorsDBHelper.insertAuthor(authorName, authorCategory);
								int authorId = authorsDBHelper.getAuthorIdByName(authorName);
							    authorList.add(authorName);
								authorMap.put(authorName, authorId);
								authorAdapter.notifyDataSetChanged();

								// Use indexOf to set correct selection
								int newIndex = authorList.indexOf(authorName);
								if (newIndex != -1) {
								    bookAddAuthorSpinner.setSelection(newIndex);
							}
						}
						cursor.close(); // Only close once
						}
					}
				}
			});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
		builder.setTitle("Add Category");

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
		final EditText input = new EditText(this);
		input.setBackgroundResource(R.drawable.edit_text_border);
		input.setLayoutParams(params);
		input.setHint("Enter Category Name");

		layout.addView(input);

		builder.setView(layout);

		builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String newCategory = input.getText().toString().trim();

					if (!newCategory.isEmpty()) {
						// Check if category exists
						Cursor cursor = categoriesDBHelper.getCategoryByName(newCategory);
						if (cursor != null) {
							if (cursor.getCount() > 0) {
								ToastMessagesManager.show(AddBookActivity.this, "Category already exists!");
							} else {
								categoriesDBHelper.insertCategory(newCategory);
								int categoryId = categoriesDBHelper.getCategoryIdByName(newCategory);
								categoryList.add(newCategory);
								categoryMap.put(newCategory, categoryId);

								// Recreate the adapter with the updated list
								categoryAdapter = new ArrayAdapter<>(AddBookActivity.this, android.R.layout.simple_spinner_dropdown_item, categoryList);
								categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
								bookAddCategorySpinner.setAdapter(categoryAdapter);

								// Set the selection to the newly added category
								int newIndex = categoryList.indexOf(newCategory);
								if (newIndex != -1) {
									bookAddCategorySpinner.setSelection(newIndex);
								}
							}
							cursor.close();
						}
					}
				}
			});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

		builder.setCancelable(false);
		builder.show();
	}
	
	private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            })
            .show();
    }
}

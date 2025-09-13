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
import java.util.*;
import com.mwendasoft.superme.helpers.*;

public class EditBookActivity extends BaseActivity {
    private EditText bookEditEditTitle, bookEditEditReview;
    private Spinner bookEditAuthorSpinner, bookEditCategorySpinner, bookEditReadSpinner;
	private ImageButton btnEditBookAuthor, btnEditBookCategory;
    private Button bookEditSaveBtn;
    private BooksDBHelper booksDBHelper;
    private AuthorsDBHelper authorsDBHelper;
    private CategoriesDBHelper categoriesDBHelper;

    private HashMap<String, Integer> authorMap = new HashMap<>();
    private HashMap<String, Integer> categoryMap = new HashMap<>();
    private HashMap<String, Integer> bookReadMap = new HashMap<>();

	private ArrayList<String> authorList = new ArrayList<>();
	private ArrayList<String> categoryList = new ArrayList<>();
	private ArrayList<String> bookReadOptionsList = new ArrayList<>();
	
	private ArrayAdapter<String> authorAdapter;
	private ArrayAdapter<String> categoryAdapter;
	private ArrayAdapter<String> bookReadOptionsAdapter;

	private Book selectedBook;
	private int selectedAuthorId, selectedCategoryId, selectedBookRead, bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_book_activity);

        bookEditEditTitle = findViewById(R.id.bookEditEditTitle);
        bookEditEditReview = findViewById(R.id.bookEditEditReview);
        bookEditReadSpinner = findViewById(R.id.bookEditReadSpinner);
        bookEditAuthorSpinner = findViewById(R.id.bookEditAuthorSpinner);
        bookEditCategorySpinner = findViewById(R.id.bookEditCategorySpinner);
        bookEditSaveBtn = findViewById(R.id.bookEditSaveBtn);

		btnEditBookAuthor = findViewById(R.id.btnEditBookAuthor);
		btnEditBookCategory = findViewById(R.id.btnEditBookCategory);

        booksDBHelper = new BooksDBHelper(this);
        authorsDBHelper = new AuthorsDBHelper(this);
        categoriesDBHelper = new CategoriesDBHelper(this);

		authorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, authorList);
		categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryList);
		bookReadOptionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, bookReadOptionsList);
		
	    bookEditAuthorSpinner.setAdapter(authorAdapter);
	    bookEditCategorySpinner.setAdapter(categoryAdapter);

		selectedBook = (Book) getIntent().getSerializableExtra("selectedBook");
		
        loadAuthors();
        loadCategories();
        loadBookReadOptions();
		loadBook();

        bookEditAuthorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedAuthor = parent.getItemAtPosition(position).toString();
					selectedAuthorId = authorMap.getOrDefault(selectedAuthor, -1);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});

		bookEditCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedCategory = parent.getItemAtPosition(position).toString();
					selectedCategoryId = categoryMap.getOrDefault(selectedCategory, -1);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});


        bookEditReadSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedStatus = parent.getItemAtPosition(position).toString();
					selectedBookRead = bookReadMap.getOrDefault(selectedStatus, 0);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});

		btnEditBookAuthor.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAddAuthorDialog();
				}
			});

		btnEditBookCategory.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAddCategoryDialog();
				}
			});

        bookEditSaveBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					saveBook();
				}
			});
    }

    private void loadAuthors() {
		Cursor cursor = authorsDBHelper.getAllAuthorsWithIds();
		int authNum = 0;
		
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
					authNum += 1;
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
		bookEditAuthorSpinner.setAdapter(authorAdapter);
	}

    private void loadCategories() {
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
		bookEditCategorySpinner.setAdapter(categoryAdapter);
	}

    private void loadBookReadOptions() {
		// Clear first to avoid duplicates if called more than once
		bookReadOptionsList.clear();
		bookReadMap.clear();

		bookReadOptionsList.add("Read");
		bookReadOptionsList.add("Reading");
		bookReadOptionsList.add("Not read");

		bookReadMap.put("Read", 1);
		bookReadMap.put("Reading", 2);
		bookReadMap.put("Not read", 0);

		// Notify the adapter in case the data has changed
		bookReadOptionsAdapter.notifyDataSetChanged();
		bookEditReadSpinner.setAdapter(bookReadOptionsAdapter);
	}
	
	private void loadBook() { 
	    Cursor cursor = booksDBHelper.getBookById(selectedBook.getId());

		if (cursor != null && cursor.moveToFirst()) {
			String title = cursor.getString(cursor.getColumnIndex("title"));
			int authorId = cursor.getInt(cursor.getColumnIndex("author"));
			String review = cursor.getString(cursor.getColumnIndex("review"));
			int statusId = cursor.getInt(cursor.getColumnIndex("read"));
		    int categoryId = cursor.getInt(cursor.getColumnIndex("category"));
			
			// Title and Review 
			bookEditEditTitle.setText(title);
			bookEditEditReview.setText(review);
			
			// Read Status Spinner Update
			String statusText = getKeyByValue(bookReadMap, statusId);
			int statusPos = bookReadOptionsAdapter.getPosition(statusText);
			bookEditReadSpinner.setSelection(statusPos);

			// Author Spinner Update
			String authorName = getKeyByValue(authorMap, authorId);
			int authorPos = authorAdapter.getPosition(authorName);
			bookEditAuthorSpinner.setSelection(authorPos);

			// Category Spinner Update
			String categoryName = getKeyByValue(categoryMap, categoryId); // You'll need to implement this
			int categoryPos = categoryAdapter.getPosition(categoryName);
			bookEditCategorySpinner.setSelection(categoryPos);
			
			cursor.close();
		}

		else {
			bookEditEditReview.setText("Unable to edit review for this book!");
			bookEditEditReview.setFocusable(false);
			bookEditEditReview.setClickable(false);
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
	
	private void saveBook() {
		String title = bookEditEditTitle.getText().toString().trim();
		String review = bookEditEditReview.getText().toString().trim();

		if (title.isEmpty() || review.isEmpty()) {
			ToastMessagesManager.show(this, "Please fill all fields!");
			return;
		}

		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("author", selectedAuthorId);
		values.put("review", review);
		values.put("read", selectedBookRead);
		values.put("category", selectedCategoryId);

		long result = booksDBHelper.updateBook("books", selectedBook.getId(), values);
		ToastMessagesManager.show(this, result != -1 ? "Added successfully!" : "Adding failed!");
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
								ToastMessagesManager.show(EditBookActivity.this, "Author already exists!");
							} else {
								authorsDBHelper.insertAuthor(authorName, authorCategory);
							    authorList.add(authorName);
								authorAdapter.notifyDataSetChanged();

								// Use indexOf to set correct selection
								int newIndex = authorList.indexOf(authorName);
								if (newIndex != -1) {
								    bookEditAuthorSpinner.setSelection(newIndex);
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
								ToastMessagesManager.show(EditBookActivity.this, "Category already exists!");
						    } else {
								categoriesDBHelper.insertCategory(newCategory);
								categoryList.add(newCategory);

								// Recreate the adapter with the updated list
								categoryAdapter = new ArrayAdapter<>(EditBookActivity.this, android.R.layout.simple_spinner_dropdown_item, categoryList);
								categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
								bookEditCategorySpinner.setAdapter(categoryAdapter);

								// Set the selection to the newly added category
								int newIndex = categoryList.indexOf(newCategory);
								if (newIndex != -1) {
									bookEditCategorySpinner.setSelection(newIndex);
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

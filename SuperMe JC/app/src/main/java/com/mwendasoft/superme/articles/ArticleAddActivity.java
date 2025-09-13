package com.mwendasoft.superme.articles;

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
import android.icu.text.*;
import com.mwendasoft.superme.helpers.*;

public class ArticleAddActivity extends BaseActivity {
    private EditText articleAddTitle, articleAddText, articleAddUrl;
    private Spinner articleAddAuthorSpinner, articleAddCategorySpinner;
	private ImageButton articleAddAuthorBtn, articleAddCategoryBtn;
    private Button articleAddBtn;
    private ArticlesDBHelper articlesDBHelper;
    private AuthorsDBHelper authorsDBHelper;
    private CategoriesDBHelper categoriesDBHelper;

    private HashMap<String, Integer> authorMap = new HashMap<>();
	private ArrayList<String> authorList = new ArrayList<>();
	private ArrayAdapter<String> authorAdapter;

    private HashMap<String, Integer> categoryMap = new HashMap<>();
	private ArrayList<String> categoryList = new ArrayList<>();
	private ArrayAdapter<String> categoryAdapter;

	private int selectedAuthorId, selectedCategoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_add_activity);

        articleAddTitle = findViewById(R.id.articleAddTitle);
        articleAddText = findViewById(R.id.articleAddText);
		articleAddUrl = findViewById(R.id.articleAddUrl);
		
        articleAddAuthorSpinner = findViewById(R.id.articleAddAuthorSpinner);
        articleAddCategorySpinner = findViewById(R.id.articleAddCategorySpinner);
        articleAddBtn = findViewById(R.id.articleAddBtn);

		articleAddAuthorBtn = findViewById(R.id.articleAddAuthorBtn);
		articleAddCategoryBtn = findViewById(R.id.articleAddCategoryBtn);

        articlesDBHelper = new ArticlesDBHelper(this);
        authorsDBHelper = new AuthorsDBHelper(this);
        categoriesDBHelper = new CategoriesDBHelper(this);

		authorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, authorList);
		categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryList);

	    articleAddAuthorSpinner.setAdapter(authorAdapter);
	    articleAddCategorySpinner.setAdapter(categoryAdapter);

        loadAuthors();
        loadCategories();

        articleAddAuthorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedAuthor = parent.getItemAtPosition(position).toString();
					selectedAuthorId = authorMap.getOrDefault(selectedAuthor, -1);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});

		articleAddCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedCategory = parent.getItemAtPosition(position).toString();
					selectedCategoryId = categoryMap.getOrDefault(selectedCategory, -1);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});

		articleAddAuthorBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAddAuthorDialog();
				}
			});

		articleAddCategoryBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAddCategoryDialog();
				}
			});
			
		articleAddBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					saveArticle();
				}
			});
    }
	
	private void loadAuthors() {
		authorList.add(getString(R.string.select_author));
		authorMap.put(getString(R.string.select_author), -1);

		Cursor cursor = authorsDBHelper.getAllAuthorsWithIds();

		if (cursor == null) {
			authorList.add(getString(R.string.no_authors));
			authorMap.put(getString(R.string.no_authors), -1);
		} else {
			// Ensure the cursor is fully processed
			if (cursor.getCount() == 0) {
				authorList.add(getString(R.string.no_authors));
				authorMap.put(getString(R.string.no_authors), -1);
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
			authorList.add(getString(R.string.no_authors)); // Fallback if the list is still empty
		}

		// Setup the adapter and attach it to the spinner
		authorAdapter.notifyDataSetChanged();
		articleAddAuthorSpinner.setAdapter(authorAdapter);
	}

    private void loadCategories() {
		categoryList.add(getString(R.string.select_category));
		categoryMap.put(getString(R.string.select_category),-1);

		Cursor cursor = categoriesDBHelper.getAllCategoriesWithIds();

		if (cursor == null) {
			categoryList.add(getString(R.string.no_categories));
			categoryMap.put(getString(R.string.no_categories), -1);
		} else {
			if (cursor.getCount() == 0) {
				categoryList.add(getString(R.string.no_categories));
				categoryMap.put(getString(R.string.no_categories), -1);
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
		articleAddCategorySpinner.setAdapter(categoryAdapter);
	}

    private void saveArticle() {
		String title = articleAddTitle.getText().toString().trim();
		String text = articleAddText.getText().toString().trim();
		String link = articleAddUrl.getText().toString().trim();
		
		if (title.isEmpty() || text.isEmpty()) {
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
		values.put("writer", selectedAuthorId);
		values.put("category", selectedCategoryId);
		values.put("body", text);
		values.put("date", getCurrentDate());
		values.put("link", link);

		long result = articlesDBHelper.insertArticle(values);
		ToastMessagesManager.show(this, "Added successfully!");
		if (result != -1) finish();
	}
	
	public String getCurrentDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		return sdf.format(new Date());
	}

	private void showAddAuthorDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.add_writer));

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
		final EditText writerNameInput = new EditText(this);
		writerNameInput.setBackgroundResource(R.drawable.edit_text_border);
		writerNameInput.setHint(getString(R.string.name_hint));
		writerNameInput.setLayoutParams(params);

		layout.addView(writerNameInput);

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
					String artistName = writerNameInput.getText().toString().trim();
					String authorCategory = categorySpinner.getSelectedItem().toString();

					if (!artistName.isEmpty()) {
						Cursor cursor = authorsDBHelper.getAuthorByName(artistName);
						if (cursor != null) {
						    if (cursor.getCount() > 0) {
							    ToastMessagesManager.show(ArticleAddActivity.this, "Author already exists!");
							} else {
								authorsDBHelper.insertAuthor(artistName, authorCategory);
								int authorId = authorsDBHelper.getAuthorIdByName(artistName);
							    authorList.add(artistName);
								authorMap.put(artistName, authorId);
								authorAdapter.notifyDataSetChanged();

								// Use indexOf to set correct selection
								int newIndex = authorList.indexOf(artistName);
								if (newIndex != -1) {
								    articleAddAuthorSpinner.setSelection(newIndex);
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
		builder.setTitle(getString(R.string.add_category));

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
		categoryInput.setHint(getString(R.string.category_hint));

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
								ToastMessagesManager.show(ArticleAddActivity.this, "Category exists!");
							} else {
								categoriesDBHelper.insertCategory(newCategory);
								int categoryId = categoriesDBHelper.getCategoryIdByName(newCategory);
								categoryList.add(newCategory);
								categoryMap.put(newCategory, categoryId);
								categoryAdapter.notifyDataSetChanged();
								
								// Set the selection to the newly added category
								int newIndex = categoryList.indexOf(newCategory);
								if (newIndex != -1) {
									articleAddCategorySpinner.setSelection(newIndex);
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

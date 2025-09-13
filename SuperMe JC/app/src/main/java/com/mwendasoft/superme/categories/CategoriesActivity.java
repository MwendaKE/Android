package com.mwendasoft.superme.categories;

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
import android.util.*;
import com.mwendasoft.superme.helpers.*;

public class CategoriesActivity extends BaseActivity {
    private CategoriesDBHelper dbHelper;
    private ListView categsListView;
    private TextView categsListTitle, categsCountBadge;
    private ImageButton addCategFab;

    private CategoriesAdapter categsAdapter;
    private final ArrayList<Category> categories = new ArrayList<>(); // Initialize as final

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.categories_activity);

        initializeViews();
        setupDatabaseHelpers();
        setupAdapters();
        setupListeners();

        loadCategories();
    }

    private void initializeViews() {
        categsListTitle = findViewById(R.id.categsListTitle);
        categsCountBadge = findViewById(R.id.categsCountBadge);
        categsListView = findViewById(R.id.categsListView);
        addCategFab = findViewById(R.id.addCategFab);

    }

    private void setupDatabaseHelpers() {
        dbHelper = new CategoriesDBHelper(this);
	}

    private void setupAdapters() {
        categsAdapter = new CategoriesAdapter(this, categories);
        categsListView.setAdapter(categsAdapter);
    }

    private void setupListeners() {
		categsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					//Toast.makeText(CategoriesActivity.this, "Long click to edit this category.", Toast.LENGTH_SHORT).show();
					ToastMessagesManager.show(CategoriesActivity.this, "Long click to edit this category");
				}
			});
			
        categsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					// Get the clicked author
					Category categ = categories.get(position);
					showEditCategoryDialog(categ);
					return true;
				}
			});

        addCategFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAddCategoryDialog();
				}
			});
    }

    private void loadCategories() {
        categories.clear();

        Cursor cursor = null;
        try {
            cursor = dbHelper.getAllCategories();
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow("id");
                int nameIndex = cursor.getColumnIndexOrThrow("category");
                
                do {
                    int id = cursor.getInt(idIndex);
                    String categName = cursor.getString(nameIndex);
                   
                    if (categName != null && !categName.isEmpty()) {
                        categories.add(new Category(id, categName));
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
					categsListTitle.setText("CATEGORIES");
					categsCountBadge.setText(String.valueOf(categories.size()));

					if (categories.isEmpty()) {
						showNoCategsDialog();
					} else if (categsAdapter != null) {
						categsAdapter.notifyDataSetChanged();
					}
				}
			});
    }

	private void showNoCategsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Categories");
        builder.setMessage("No categories found. Would you like to add a new one?");
        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					showAddCategoryDialog();
				}
			});
        builder.setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }
	
	private void showAddCategoryDialog() {
		// Create dialog builder
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Add Category");

		// Convert dp to pixels
		int inputHeight = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 55, getResources().getDisplayMetrics());
		int bottomMargin = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

		// Set layout parameters
		LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, inputHeight);
		inputParams.setMargins(0, 0, 0, bottomMargin);

		// Create layout and EditText
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(20, 20, 20, 20);

		final EditText input = new EditText(this);
		input.setHint("Enter Category Name");
		input.setBackgroundResource(R.drawable.edit_text_border);
		input.setLayoutParams(inputParams);
		layout.addView(input);

		builder.setView(layout);

		// Handle Add button
		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String newCategory = input.getText().toString().trim();

					if (!newCategory.isEmpty()) {
						Cursor cursor = dbHelper.getCategoryByName(newCategory);

						if (cursor != null) {
							if (cursor.getCount() > 0) {
								ToastMessagesManager.show(CategoriesActivity.this, "Category already exists!");
							} else {
								long result = dbHelper.insertCategory(newCategory);
								
								if ((int) result != -1) {
									ToastMessagesManager.show(CategoriesActivity.this, "Category added successfully!");
									loadCategories();
								}
							}
							cursor.close();
						}
					}
				}
			});

		// Handle Cancel button
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

		builder.setCancelable(false);
		builder.show();
	}
	
	private void showEditCategoryDialog(final Category categ) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Edit Category");

		// Convert dp to pixels
		int inputHeight = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, 55, getResources().getDisplayMetrics());
		int bottomMargin = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

		LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, inputHeight);
		inputParams.setMargins(0, 0, 0, bottomMargin);

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(20, 20, 20, 20);

		final EditText input = new EditText(this);
		input.setLayoutParams(inputParams);
		input.setBackgroundResource(R.drawable.edit_text_border);
		input.setText(categ.getName());
		input.setSelection(input.getText().length());
		layout.addView(input);

		builder.setView(layout);

		// Create dialog object
		final AlertDialog dialog = builder.create();

		// Add buttons
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Update", (DialogInterface.OnClickListener) null);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int which) {
					dialogInterface.cancel();
				}
			});

		dialog.setCancelable(false);
		dialog.show();

		// Override Save button click
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String newName = input.getText().toString().trim();

					if (!newName.isEmpty()) {
						if (newName.equals(categ.getName())) {
							ToastMessagesManager.show(CategoriesActivity.this, "No changes made!");
						} else {
							Cursor cursor = dbHelper.getCategoryByName(newName);
							if (cursor != null) {
								if (cursor.getCount() > 0) {
									ToastMessagesManager.show(CategoriesActivity.this, "Category already exists!");
								} else {
									ContentValues values = new ContentValues();
									values.put("category", newName);

									int success = dbHelper.updateCategory(categ.getId(), values);
									if (success != -1) {
										ToastMessagesManager.show(CategoriesActivity.this, "Category updated successfully!");
										dialog.dismiss(); // only dismiss if update is successful
										loadCategories();
									} else {
										ToastMessagesManager.show(CategoriesActivity.this, "Failed to update!");
									}
								}
								cursor.close();
							}
						}
					}
				}
			});
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
        loadCategories();
    }
}

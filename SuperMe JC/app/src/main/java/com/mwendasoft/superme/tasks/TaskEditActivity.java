package com.mwendasoft.superme.tasks;

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
import android.net.*;
import java.io.*;
import android.os.*;
import android.view.*;
import android.media.*;
import android.graphics.*;
import com.mwendasoft.superme.helpers.*;

public class TaskEditActivity extends BaseActivity {
    private EditText taskTitleEdit, taskDescriptionEdit, taskImageEditText;
	
    private Spinner taskCategoryEditSpinner;
	private CheckBox taskSuccessEditCheckbox;
	private Button taskSaveEditBtn;
    private TasksDBHelper tasksDBHelper;
	private CategoriesDBHelper categoriesDBHelper;

	private HashMap<String, Integer> categoryMap = new HashMap<>();
	private ArrayList<String> categoryList= new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;

	private List<Uri> imageUris = new ArrayList<>();
	private static final int PICK_IMAGE = 1001;
	
	private int selectedCategoryId;
	private int success, taskId;
	private Task selectedTask;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_edit_activity);

		taskTitleEdit = findViewById(R.id.taskTitleEdit);
        taskDescriptionEdit = findViewById(R.id.taskDescriptionEdit);
		taskCategoryEditSpinner = findViewById(R.id.taskCategoryEditSpinner);
		taskSuccessEditCheckbox = findViewById(R.id.taskSuccessEditCheckbox);
		taskImageEditText = findViewById(R.id.taskImageEdit);
        
		taskSaveEditBtn = findViewById(R.id.taskSaveEditBtn);

		tasksDBHelper = new TasksDBHelper(this);
		categoriesDBHelper = new CategoriesDBHelper(this);

		categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryList);
		
		selectedTask = (Task) getIntent().getSerializableExtra("selectedTask");
		taskId = selectedTask.getId();
		
		loadCategories();
		loadTask();
		loadAllImageFiles();

        taskCategoryEditSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedCategory = parent.getItemAtPosition(position).toString();
					selectedCategoryId = categoryMap.getOrDefault(selectedCategory, -1);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});

		taskSuccessEditCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						success = 1;
					} else {
						success = 0;
					}
				}
			});
			
		taskImageEditText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickImages();
				}
			});

        taskSaveEditBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					saveTask();
				}
			});
    }
	
	//#######
    private void pickImages() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
			int count = 0;
			imageUris.clear(); // Clear previous selections if you want fresh selection each time

			if (data.getClipData() != null) {
				// Multiple images selected
				count = data.getClipData().getItemCount();
				for (int i = 0; i < count; i++) {
					Uri uri = data.getClipData().getItemAt(i).getUri();
					imageUris.add(uri);
				}
			} else if (data.getData() != null) {
				// Single image selected
				count = 1;
				imageUris.add(data.getData());
			}

			// Update UI
			taskImageEditText.setText(count + " images selected");
		}
	}
	//#######

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
		taskCategoryEditSpinner.setAdapter(categoryAdapter);
	}

	private void loadTask() {
		Cursor cursor = tasksDBHelper.getTaskById(taskId);

		if (cursor != null && cursor.moveToFirst()) {
			int titleIndex = cursor.getColumnIndex("title");
			int descriptionIndex = cursor.getColumnIndex("description");
			int categIndex = cursor.getColumnIndex("category");
			int successIndex = cursor.getColumnIndex("success");
			
			String title = cursor.getString(titleIndex);
			String description = cursor.getString(descriptionIndex);
			int categoryId = cursor.getInt(categIndex);
			int successId = cursor.getInt(successIndex);
			
			taskTitleEdit.setText(title);
			taskDescriptionEdit.setText(description);

			String categName = getKeyByValue(categoryMap, categoryId);
			int categPos = categoryAdapter.getPosition(categName);
			taskCategoryEditSpinner.setSelection(categPos);
			
			taskSuccessEditCheckbox.setChecked(successId == 1);
		}	
	}
	
	private void loadAllImageFiles() {
		File folder = new File(Environment.getExternalStorageDirectory(), ".superme/tasks/Task " + taskId);

		LinearLayout imageContainer = findViewById(R.id.taskImagesEditContainer);
		
		imageContainer.removeAllViews();
		
		if (folder.exists() && folder.isDirectory()) {
			File[] files = folder.listFiles();
			if (files != null && files.length > 0) {
				for (File file : files) {
					addImageItem(file, imageContainer);
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
						loadAllImageFiles();
						ToastMessagesManager.show(TaskEditActivity.this, "Image removed successfully.");
					}
				}
			});

		layout.addView(imageView);
		layout.addView(deleteBtn);
		container.addView(layout);
	}

	private void saveTask() {
		String title = taskTitleEdit.getText().toString().trim();
		String description = taskDescriptionEdit.getText().toString().trim();

		if (title.isEmpty() || description.isEmpty()) {
			ToastMessagesManager.show(this, "Please fill all the fields!");
			return;
		}

		if (selectedCategoryId == -1) {
			ToastMessagesManager.show(this, "Select a valid category!");
			return;
		}

		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("category", selectedCategoryId);
		values.put("success", success);
		values.put("description", description);

		long rowsUpdated = tasksDBHelper.updateTask(values, taskId);

		if (rowsUpdated > 0) {
			if (imageUris != null && !imageUris.isEmpty()) {
				try {
					String folderName = "Task " + taskId; 
					List<String> savedImages = saveImageFiles(imageUris, folderName);

					if (!savedImages.isEmpty()) {
						tasksDBHelper.insertTaskMediaPaths(taskId, savedImages); 
					}

					// Clear and reset UI
					imageUris.clear();
					taskImageEditText.setText(getString(R.string.add_images));
				} catch (IOException e) {
					Log.e("SaveTask", "Error saving images", e);
					ToastMessagesManager.show(TaskEditActivity.this, "Error saving images");
				}
			}

			ToastMessagesManager.show(this, "Updated successfully.");
			finish();
		} else {
			ToastMessagesManager.show(this, "Update failed");
		}
	}

	private List<String> saveImageFiles(List<Uri> uris, String taskFolderName) throws IOException {
		List<String> savedPaths = new ArrayList<>();
		File mediaFolder = getTaskMediaFolder(taskFolderName);

		for (Uri uri : uris) {
			if (uri == null) continue; // Skip null URIs

			String fileName = "task_media_" + System.currentTimeMillis() + ".jpg";
			File copiedFile = copyUriToFile(uri, mediaFolder, fileName);
			if (copiedFile != null) {
				savedPaths.add(copiedFile.getAbsolutePath());
			}
		}
		return savedPaths;
	}

	private File getTaskMediaFolder(String taskFolderName) throws IOException {
		// Changed to use app-specific storage
		File baseFolder = new File(
			Environment.getExternalStorageDirectory(),
			".superme/tasks/" + taskFolderName
		);

		if (!baseFolder.exists() && !baseFolder.mkdirs()) {
			throw new IOException("Failed to create directory: " + baseFolder.getAbsolutePath());
		}
		return baseFolder;
	}

	private File copyUriToFile(Uri uri, File destDir, String fileName) throws IOException {
		if (uri == null || destDir == null || fileName == null) {
			throw new IllegalArgumentException("Input parameters cannot be null");
		}

		File destFile = new File(destDir, fileName);

		try (InputStream in = getContentResolver().openInputStream(uri);
		OutputStream out = new FileOutputStream(destFile)) {

			if (in == null) {
				throw new IOException("Failed to open URI input stream");
			}

			byte[] buffer = new byte[4096];
			int length;
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			return destFile;
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
}

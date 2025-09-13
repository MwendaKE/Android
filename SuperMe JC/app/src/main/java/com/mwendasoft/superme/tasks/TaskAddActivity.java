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
import com.mwendasoft.superme.reminders.*;
import com.mwendasoft.superme.helpers.*;

public class TaskAddActivity extends BaseActivity {
    private EditText taskTitleAdd, taskSDateAdd, taskSTimeAdd, taskDurationAdd, taskEdateAdd, taskDescriptionAdd, taskImageAdd;
	private Spinner taskDurationAddSpinner, taskCategoryAddSpinner;
	private CheckBox taskSuccessAddCheckbox;
	private Button taskSaveAddBtn;
    private TasksDBHelper tasksDBHelper;
	private CategoriesDBHelper categoriesDBHelper;

	private HashMap<String, Integer> categoryMap = new HashMap<>();
	private ArrayList<String> categoryList= new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;
	
	private HashMap<String, Integer> durationMap = new HashMap<>();
	private ArrayList<String> durationList= new ArrayList<>();
    private ArrayAdapter<String> durationAdapter;
	
	private List<Uri> imageUris = new ArrayList<>();
	private static final int PICK_IMAGE = 1001;
	
	private int selectedCategoryId;
	private int success, durationInDays;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_add_activity);

		TextWatcher watcher = new TextWatcher() {
			@Override public void afterTextChanged(Editable s) { updateEndDate(); }
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
		};
		
        taskTitleAdd = findViewById(R.id.taskTitleAdd);
        taskSDateAdd = findViewById(R.id.taskSDateAdd);
		taskSTimeAdd = findViewById(R.id.taskSTimeAdd);
		taskDurationAdd = findViewById(R.id.taskDurationAdd);
        taskEdateAdd = findViewById(R.id.taskEdateAdd);
		taskDescriptionAdd = findViewById(R.id.taskDescriptionAdd);
		taskImageAdd = findViewById(R.id.taskImageAdd);
		
		taskDurationAddSpinner = findViewById(R.id.taskDurationAddSpinner);
		taskCategoryAddSpinner = findViewById(R.id.taskCategoryAddSpinner);
		taskSuccessAddCheckbox = findViewById(R.id.taskSuccessAddCheckbox);
		
		// ADD A TEXT WATCHER TO THE INPUTS
		
		taskSDateAdd.addTextChangedListener(watcher);
		taskSTimeAdd.addTextChangedListener(watcher);
		taskDurationAdd.addTextChangedListener(watcher);
		
		//
		taskSaveAddBtn = findViewById(R.id.taskSaveAddBtn);

		tasksDBHelper = new TasksDBHelper(this);
		categoriesDBHelper = new CategoriesDBHelper(this);
		
		categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryList);
		durationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, durationList);
		
		durationInDays = 0;
		
		populateDurationSpinner();
		loadCategories();

        taskDurationAddSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					updateEndDate();
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});

		taskCategoryAddSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedCategory = parent.getItemAtPosition(position).toString();
					selectedCategoryId = categoryMap.getOrDefault(selectedCategory, -1);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});
			
		taskSuccessAddCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						success = 1;
					} else {
						success = 0;
					}
				}
			});
			
        taskSDateAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showSDatePicker();
				}
			});
			
		taskSTimeAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showTimePicker();
				}
			});
			
		taskDurationAdd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus) {
						String date = taskSDateAdd.getText().toString().trim();
						String time = taskSTimeAdd.getText().toString().trim();
						int numDays = convertToDays(Integer.parseInt(taskDurationAdd.getText().toString().trim()),
													taskDurationAddSpinner.getSelectedItem().toString());

						if (!date.isEmpty() && !time.isEmpty() && numDays > 0) {
							try {
								String result = calculateDeadline(date, time, numDays);
								taskEdateAdd.setText(result);
							} catch (Exception e) {
								taskEdateAdd.setText("0000-00-00 0000");
							}
						}
					}
				}
			});
			
		taskImageAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickImages();
				}
			});

        taskSaveAddBtn.setOnClickListener(new View.OnClickListener() {
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
			taskImageAdd.setText(count + " images selected");
		}
	}
	//#######

	private void populateDurationSpinner() {
		durationList.add("Days");
		durationList.add("Weeks");
		durationList.add("Months");
		durationList.add("Years");

		durationMap.put("Days", 0);
		durationMap.put("Weeks", 1);
		durationMap.put("Months", 2);
		durationMap.put("Years", 3);

		durationAdapter.notifyDataSetChanged();
		taskDurationAddSpinner.setAdapter(durationAdapter);
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
		taskCategoryAddSpinner.setAdapter(categoryAdapter);
	}

	private void showSDatePicker() {
		final Calendar calendar = Calendar.getInstance();

		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH); // 0-based
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		DatePickerDialog datePickerDialog = new DatePickerDialog(
			this,
			new DatePickerDialog.OnDateSetListener() {
				@Override
				public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
					String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
					taskSDateAdd.setText(selectedDate);
				}
			},
			year, month, day
		);

		datePickerDialog.show();
	}
	
	private void showTimePicker() {
		final Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);

		TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
				String formattedTime = String.format(Locale.getDefault(), "%02d%02d", selectedHour, selectedMinute);
				taskSTimeAdd.setText(formattedTime);
			}
		};

		// true = 24-hour format
		TimePickerDialog timePickerDialog = new TimePickerDialog(this, timeSetListener, hour, minute, true);
		timePickerDialog.show();
	}
		
	private void updateEndDate() {
		String date = taskSDateAdd.getText().toString().trim();
		String time = taskSTimeAdd.getText().toString().trim();
		String valueStr = taskDurationAdd.getText().toString().trim();
		String unit = taskDurationAddSpinner.getSelectedItem().toString();

		if (date.isEmpty() || time.isEmpty() || valueStr.isEmpty()) return;

		try {
			int value = Integer.parseInt(valueStr);
			durationInDays = convertToDays(value, unit);
			String result = calculateDeadline(date, time, durationInDays);
			taskEdateAdd.setText(result);
		} catch (Exception e) {
			taskEdateAdd.setText("0000-00-00 0000");
		}
	}
	
	//
	public int convertToDays(int value, String unit) {
		switch (unit) {
			case "Days":
				return value;
			case "Weeks":
				return value * 7;
			case "Months":
				return value * 30; // Approximate
			case "Years":
				return value * 365; // Approximate
			default:
				return 0; // Safe fallback for unknown units
		}
	}
	
	public static String calculateDeadline(String date, String time, int numberOfDays) {
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");
		LocalDateTime startDateTime = LocalDateTime.parse(date + " " + time, inputFormatter);
		LocalDateTime deadline = startDateTime.plusDays(numberOfDays);
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");
		return deadline.format(outputFormatter);
	}

	private void saveTask() {
		// Get input values
		String title = taskTitleAdd.getText().toString().trim();
		String sdate = taskSDateAdd.getText().toString().trim();
		String stime = taskSTimeAdd.getText().toString().trim();
		String edate = taskEdateAdd.getText().toString().trim();
		String description = taskDescriptionAdd.getText().toString().trim();
		
		// Validation
		if (title.isEmpty() || description.isEmpty()) {
			ToastMessagesManager.show(this, "Please fill al the fields!");
			return;
		}

		if (sdate.isEmpty() || stime.isEmpty()) {
			ToastMessagesManager.show(this, "Select a valid date and time!");
			return;
		}

		if (selectedCategoryId == -1) {
			ToastMessagesManager.show(this, "Select a valid category!");
			return;
		}

		// Prepare task data
		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("sdate", sdate);
		values.put("stime", stime);
		values.put("edate", edate);
		values.put("duration", durationInDays);
		values.put("category", selectedCategoryId);
		values.put("success", success);
		values.put("description", description);

		// Insert task
		long taskId = tasksDBHelper.insertTask(values);

		if (taskId == -1) {
			ToastMessagesManager.show(this, "Adding failed!");
			return;
		}

		// Save images if any
		try {
			if (imageUris != null && !imageUris.isEmpty()) {
				String folderName = "Task " + taskId;
				List<String> savedImages = saveImageFiles(imageUris, folderName);

				if (!savedImages.isEmpty()) {
					tasksDBHelper.insertTaskMediaPaths((int) taskId, savedImages);
				}

				// Clear and reset UI
				imageUris.clear();
				taskImageAdd.setText(getString(R.string.add_images));
			}

			//#####
			ReminderScheduler.scheduleReminders(this,
												ReminderScheduler.TYPE_TASK,
												taskId,
												ConvertTimeToMillis.convertToMillis(edate)
												);
			//#####

			ToastMessagesManager.show(this, "Added successfully!");
			finish();

		} catch (IOException e) {
			Log.e("SaveTask", "Error saving task", e);
			ToastMessagesManager.show(this, "Error saving task media!");
		}
	}

	private List<String> saveImageFiles(List<Uri> uris, String taskFolderName) throws IOException {
		List<String> savedPaths = new ArrayList<>();
		File mediaFolder = getTaskMediaFolder(taskFolderName);

		for (Uri uri : uris) {
			String fileName = "task_media_" + System.currentTimeMillis() + ".jpg";
			File copiedFile = copyUriToFile(uri, mediaFolder, fileName);
			if (copiedFile != null) {
				savedPaths.add(copiedFile.getAbsolutePath());
			}
		}
		return savedPaths;
	}

	private File getTaskMediaFolder(String taskFolderName) throws IOException {
		File baseFolder = new File(
			Environment.getExternalStorageDirectory(),
			".superme/tasks/" + taskFolderName
		);

		if (!baseFolder.exists() && !baseFolder.mkdirs()) {
			throw new IOException("Failed to create directory");
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
}

package com.mwendasoft.superme.diaries;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import com.mwendasoft.superme.R;
import com.mwendasoft.superme.BaseActivity;
import android.widget.*;
import android.view.*;
import java.io.*;
import android.os.*;
import java.util.*;

public class DiaryActivity extends BaseActivity {
    private DiariesDBHelper dbHelper;
    private ListView diariesListView;
    private TextView diaryListTitle, diaryEntryCountBadge;
    private ArrayList<Diary> diaries;
    private DiaryViewAdapter diaryAdapter;
	private ImageButton addDiaryFab;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary_activity);

        diariesListView = findViewById(R.id.diariesListView);
        diaryListTitle = findViewById(R.id.diaryListTitle);
		diaryEntryCountBadge = findViewById(R.id.diaryEntryCountBadge);
		addDiaryFab = findViewById(R.id.addDiaryFab);
		
		dbHelper = new DiariesDBHelper(this);
		
        diaries = new ArrayList<>();
        diaryAdapter = new DiaryViewAdapter(this, diaries);
        diariesListView.setAdapter(diaryAdapter);

        registerForContextMenu(diariesListView);
		
        loadDiaries();

        diariesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Diary selectedEntry = diaries.get(position);
					openDiaryDetailView(selectedEntry);
				}
			});
			
		addDiaryFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(DiaryActivity.this, DiaryAddActivity.class);
					startActivity(intent);
				}
			});
    }
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.diariesListView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			Diary selectedDiary = diaries.get(info.position);
			menu.setHeaderTitle(selectedDiary.getTitle());
			menu.add(Menu.NONE, 1, 1, "Open");
			menu.add(Menu.NONE, 2, 2, "Edit");
			menu.add(Menu.NONE, 3, 3, "Delete");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Diary selectedEntry = diaries.get(info.position);
		
		switch (item.getItemId()) {
			case 1: // Open
				openDiaryDetailView(selectedEntry);
				return true;
			case 3: // Delete
				confirmAndDeleteDiary(selectedEntry);
				return true;
			case 2: // Edit
				editDiary(selectedEntry);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	private void confirmAndDeleteDiary(final Diary diary) {
		new AlertDialog.Builder(this)
			.setTitle(R.string.delete)
			.setMessage("Are you sure you want to delete \"" + diary.getTitle() + "\" diary entry?")
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Step 1: Delete the media folder e.g., ".superme/notes/Note 15"
					File diaryFolder = new File(Environment.getExternalStorageDirectory(), ".superme/diaries/Entry " + diary.getId());
					deleteFolderRecursive(diaryFolder);

					// Step 2: Delete any individual media files as safety
					List<String> allMediaPaths = dbHelper.getAllMediaPaths(diary.getId());
					for (String path : allMediaPaths) {
						File file = new File(path);
						if (file.exists()) {
							file.delete();
						}
					}
					
					dbHelper.deleteDiaryEntry(diary.getId());
					diaries.remove(diary);
					diaryAdapter.notifyDataSetChanged();
				}
			})
			.setNegativeButton(R.string.no, null)
			.show();
	}
	
	private void deleteFolderRecursive(File folder) {
		if (folder.exists()) {
			File[] files = folder.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						deleteFolderRecursive(file);
					} else {
						file.delete();
					}
				}
			}
			folder.delete(); // delete the folder itself
		}
	}

	private void editDiary(Diary diary) {
		Intent intent = new Intent(DiaryActivity.this, DiaryEditActivity.class);
		intent.putExtra("diaryTitle", diary.getTitle());
		startActivity(intent);
	}

    private void loadDiaries() {
        diaries.clear();

        Cursor cursor = dbHelper.getAllEntries();
        if (cursor != null) {
			int idIndex = cursor.getColumnIndex("id");
            int titleIndex = cursor.getColumnIndex("title");
			int moodIndex = cursor.getColumnIndex("mood");
			int dateIndex = cursor.getColumnIndex("date");
			int timeIndex = cursor.getColumnIndex("time");
			
            while (cursor.moveToNext()) {
				int id = cursor.getInt(idIndex);
                String title = cursor.getString(titleIndex);
				String mood = cursor.getString(moodIndex);
				String date = cursor.getString(dateIndex);
				String time = cursor.getString(timeIndex);
				
                if (title != null && !title.isEmpty()) {
                    diaries.add(new Diary(id, title, mood, date, time));
                }
            }
            cursor.close();
        }

        // SET TITLE AND COUNT //

		diaryListTitle.setText(R.string.diary_entries);
		diaryEntryCountBadge.setText(String.valueOf(diaries.size()));

        if (diaries.isEmpty()) {
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.diary);
			builder.setMessage("No entries found. Would you like to add a new one?");
			builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(DiaryActivity.this, DiaryAddActivity.class);
						startActivity(intent);
					}
				});
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
				
			AlertDialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(false);
			dialog.show();
			
		} else {
			diaryAdapter.notifyDataSetChanged();
		}

		// END //
    }

    private void openDiaryDetailView(Diary entry) {
        Intent intent = new Intent(this, DiaryDetailActivity.class);
        intent.putExtra("selectedEntry", entry);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) dbHelper.close();
        super.onDestroy();
    }
	
	@Override
	protected void onResume() { // Updates activity when it comes to view again.
		super.onResume();
		loadDiaries();
	}
}

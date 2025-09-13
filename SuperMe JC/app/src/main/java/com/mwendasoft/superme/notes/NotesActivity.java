package com.mwendasoft.superme.notes;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import java.util.ArrayList;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.BaseActivity;
import android.widget.*;
import android.view.*;
import com.mwendasoft.superme.quotes.*;
import com.mwendasoft.superme.authors.*;
import java.util.*;
import java.io.*;
import android.os.*;

public class NotesActivity extends BaseActivity {
	private NotesDBHelper dbHelper;
    private ListView notesListView;
    private TextView notesListTitle, notesCountBadge;
	private ImageButton addNoteFab;
    private ArrayList<Note> notes;
    private NotesAdapter notesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_activity);

        notesListView = findViewById(R.id.notesListView);
        notesListTitle = findViewById(R.id.notesListTitle);
		notesCountBadge = findViewById(R.id.notesCountBadge);
		addNoteFab = findViewById(R.id.addNoteFab);

		dbHelper = new NotesDBHelper(this);
		notes = new ArrayList<>();
        notesAdapter = new NotesAdapter(this, notes);
        notesListView.setAdapter(notesAdapter); // Set adapter before loading data

		// == REGISTER FOR CONTEXT MENU == //
        registerForContextMenu(notesListView);
		// == //

        loadNotes();

		notesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Note clickedNote = notes.get(position);
				if (clickedNote != null) {
					openNoteDetail(clickedNote);
				}
			}
		});

		addNoteFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(NotesActivity.this, NoteAddActivity.class);
				startActivity(intent);
			}
		});
    }

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.notesListView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			Note selectedNote = notes.get(info.position);
			menu.setHeaderTitle(selectedNote.getTitle());
			menu.add(Menu.NONE, 1, 1, "Open");
			menu.add(Menu.NONE, 2, 2, "Edit");
			menu.add(Menu.NONE, 3, 3, "Delete");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Note selectedNote = notes.get(info.position);

		switch (item.getItemId()) {
			case 1: // Open
				openNoteDetail(selectedNote);
				return true;
			case 3: // Delete
				confirmAndDeleteNote(selectedNote);
				return true;
			case 2: // Edit
				editNote(selectedNote);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	private void confirmAndDeleteNote(final Note note) {
		new AlertDialog.Builder(this)
			.setTitle("Delete Note")
			.setMessage("Are you sure you want to delete \"" + note.getTitle() + "\"?")
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Step 1: Delete the media folder e.g., ".superme/notes/Note 15"
					File noteFolder = new File(Environment.getExternalStorageDirectory(), ".superme/notes/Note " + note.getId());
					deleteFolderRecursive(noteFolder);

					// Step 2: Delete any individual media files as safety
					List<String> allMediaPaths = dbHelper.getAllMediaPaths(note.getId());
					for (String path : allMediaPaths) {
						File file = new File(path);
						if (file.exists()) {
							file.delete();
						}
					}

					// Step 3: Delete the note from the database
					dbHelper.deleteNote(note.getId());
					loadNotes();
					notes.remove(note);
					notesAdapter.notifyDataSetChanged();
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

	private void editNote(Note note) {
		Intent intent = new Intent(NotesActivity.this, NoteEditActivity.class);
		intent.putExtra("selectedNote", note);
		startActivity(intent);
	}

    private void loadNotes() {
        notes.clear(); // Avoid duplicates

        Cursor cursor = dbHelper.getAllNotes();

        if (cursor != null) {
            int idIndex = cursor.getColumnIndexOrThrow("id");
            int titleIndex = cursor.getColumnIndexOrThrow("title");
			int importanceIndex = cursor.getColumnIndexOrThrow("importance");
            int notesIndex = cursor.getColumnIndexOrThrow("notes");
			
            while (cursor.moveToNext()) {
                int noteId = cursor.getInt(idIndex);
                String title = cursor.getString(titleIndex);
				int importance = cursor.getInt(importanceIndex);
                String notesText = cursor.getString(notesIndex);
				
                notes.add(new Note(noteId, title, importance, notesText));
            }
            cursor.close();
        }

		// SET TITLE AND COUNT //

		notesListTitle.setText(R.string.notes);
		notesCountBadge.setText(String.valueOf(notes.size()));

        if (notes.isEmpty()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.notes);
				builder.setMessage("No notes found. Would you like to add a new one?");
				builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(NotesActivity.this, NoteAddActivity.class);
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
			notesAdapter.notifyDataSetChanged();
		}
		
		//
    }

	private void openNoteDetail(Note note) {
		Intent intent = new Intent(this, NoteDetailActivity.class);
		intent.putExtra("selectedNote", note);
		startActivity(intent);
	}
	
    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
	
	@Override
	protected void onResume() { // Updates activity when it comes to view again.
		super.onResume();
		loadNotes();
	}
}

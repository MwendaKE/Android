package com.mwendasoft.superme.people;

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
import android.content.*;
import android.util.*;
import android.content.res.*;
import android.text.*;
import android.content.ClipboardManager;
import android.text.InputType;
import android.support.v4.content.*;
import android.support.v4.app.*;
import java.io.*;
import com.mwendasoft.superme.helpers.*;

public class PeopleActivity extends BaseActivity {
	// AddPersonListener will show an error until you override the method "showEditPersonDialog"
    private PeopleDBHelper dbHelper;
    private ListView peopleListView;
    private TextView peopleListTitle, peopleCountBadge;
	private ImageButton addPersonFab;
    private ArrayList<Personn> people;
    private PeopleViewAdapter peopleAdapter;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.people_activity);

        peopleListView = findViewById(R.id.peopleListView);
		registerForContextMenu(peopleListView);

        peopleListTitle = findViewById(R.id.peopleListTitle);
		peopleCountBadge = findViewById(R.id.peopleCountBadge);
		addPersonFab = findViewById(R.id.addPersonFab);

        dbHelper = new PeopleDBHelper(this);
        dbHelper.open();  

        people = new ArrayList<>();
        peopleAdapter = new PeopleViewAdapter(this, people);
        peopleListView.setAdapter(peopleAdapter); // Set adapter before loading data

        loadPeople();
		
		peopleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Personn selectedPerson = people.get(position);
					openPersonDetail(selectedPerson);
				}
			});

		addPersonFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAddPersonDialog();
				}
			});
    }
	
	public void openPersonDetail(Personn person) {
		Intent intent = new Intent(this, PeopleDetailActivity.class);
        intent.putExtra("personId", person.getId());
	    startActivity(intent);
	}
	
    private void loadPeople() {
        people.clear(); // Avoid duplicates

		Cursor cursor = dbHelper.getAllPersons();

        if (cursor != null) {
			int idIndex = cursor.getColumnIndexOrThrow("id");
            int nameIndex = cursor.getColumnIndexOrThrow("name");
            int occupationIndex = cursor.getColumnIndexOrThrow("occupation");
            int detailsIndex = cursor.getColumnIndexOrThrow("description");

            while (cursor.moveToNext()) {
				int id = cursor.getInt(idIndex);
                String name = cursor.getString(nameIndex);
                String occupation = cursor.getString(occupationIndex);
                String details = cursor.getString(detailsIndex);

                people.add(new Personn(id, name, occupation, details));
            }
            cursor.close();
        }

		peopleListTitle.setText(R.string.people);
		peopleCountBadge.setText(String.valueOf(people.size()));

        if (people.isEmpty()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.people);
			builder.setMessage("No persons found. Would you like to add a new one?");
			builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						showAddPersonDialog();
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
			peopleAdapter.notifyDataSetChanged();
		}
    }

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.peopleListView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle("Choose an action");

			menu.add(Menu.NONE, 1, 1, "Open");
			menu.add(Menu.NONE, 2, 2, "Edit");
			menu.add(Menu.NONE, 3, 3, "Delete");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Personn selectedPerson = people.get(info.position);

		switch (item.getItemId()) {
			case 1: // Open
				openPersonDetail(selectedPerson);
				return true;
			case 3: // Delete
				confirmAndDeletePerson(selectedPerson);
				return true;
			case 2: // Edit
				showEditPersonDialog(selectedPerson.getId());
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	private void confirmAndDeletePerson(final Personn person) {
		new AlertDialog.Builder(this)
			.setTitle(R.string.delete)
			.setMessage("Are you sure you want to delete this person?")
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dbHelper.deletePeople(person.getId());
					people.remove(person);
					peopleAdapter.notifyDataSetChanged();
				}
			})
			.setNegativeButton("No", null)
			.show();
	}

	public void showAddPersonDialog() {
		// Inflate the layout
		View dialogView = LayoutInflater.from(this).inflate(R.layout.people_add_dialog, null);

		// Get references to views
		final EditText personNameAdd  = dialogView.findViewById(R.id.personNameAdd);
		final EditText personOccupationAdd = dialogView.findViewById(R.id.personOccupationAdd);
		final EditText personDescriptionAdd = dialogView.findViewById(R.id.personDescriptionAdd);

		// Show dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("New Person");
		builder.setView(dialogView);
		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String personName = personNameAdd.getText().toString().trim();
					String personOccupation = personOccupationAdd.getText().toString().trim();
					String personDetails = personDescriptionAdd.getText().toString().trim();

					ContentValues values = new ContentValues();
					values.put("name", personName);
					values.put("occupation", personOccupation);
					values.put("description", personDetails);

					long rowId = dbHelper.insertPerson(values);

					if (rowId != -1) {
						loadPeople();
						ToastMessagesManager.show(PeopleActivity.this, "Added successfully.");
						peopleAdapter.notifyDataSetChanged();
					} else {
						ToastMessagesManager.show(PeopleActivity.this, "Failed to add person!");
					}
				}
			});
		builder.setNegativeButton("Cancel", null);

		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		dialog.show();
	}
	
	public void showEditPersonDialog(final int personId) {
		Cursor cursor = dbHelper.getPersonById(personId);

		if (cursor == null && !cursor.moveToFirst()) return;
		
		String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
		String occupation = cursor.getString(cursor.getColumnIndexOrThrow("occupation"));
		String detail = cursor.getString(cursor.getColumnIndexOrThrow("description"));

		final String currentName = name;
		final String currentOcccupation = occupation;
		final String currentDetails = detail;
		
		// Inflate the layout
		View dialogView = LayoutInflater.from(this).inflate(R.layout.people_edit_dialog, null);

		// Get references to views
		final EditText personNameEdit = dialogView.findViewById(R.id.personNameEdit);
		final EditText personOccupationEdit = dialogView.findViewById(R.id.personOccupationEdit);
		final EditText personDescriptionEdit = dialogView.findViewById(R.id.personDescriptionEdit);

		personNameEdit.setText(currentName);
		personOccupationEdit.setText(currentOcccupation);
		personDescriptionEdit.setText(currentDetails);
		
		// Show dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Edit Person");
		builder.setView(dialogView);
		builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String personName = personNameEdit.getText().toString().trim();
					String personOccupation = personOccupationEdit.getText().toString().trim();
					String personDetails = personDescriptionEdit.getText().toString().trim();

					ContentValues values = new ContentValues();
					values.put("name", personName);
					values.put("occupation", personOccupation);
					values.put("description", personDetails);

					long rowId = dbHelper.updatePerson(values, personId);

					if (rowId != -1) {
						loadPeople();
						ToastMessagesManager.show(PeopleActivity.this, "Updated successfully.");
						peopleAdapter.notifyDataSetChanged();
					} else {
						ToastMessagesManager.show(PeopleActivity.this, "Failed to update person!");
					}
				}
			});
		builder.setNegativeButton("Cancel", null);

		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		dialog.show();
		//************
	}
	
    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}

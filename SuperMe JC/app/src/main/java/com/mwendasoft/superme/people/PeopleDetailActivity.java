package com.mwendasoft.superme.people;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import com.mwendasoft.superme.R;
import com.mwendasoft.superme.BaseActivity;
import android.text.method.*;
import java.text.*;
import java.util.*;
import android.widget.*;
import android.view.*;
import android.content.*;
import com.mwendasoft.superme.core.*;

public class PeopleDetailActivity extends BaseActivity {
    private PeopleDBHelper dbHelper;
	private TextView peopleDetailTitle, peopleOccupationView,peopleDetailView; 
    private ImageButton editPersonFab;

	private int personId;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.people_detail_activity);

        peopleDetailTitle = findViewById(R.id.peopleDetailTitle);
        peopleOccupationView = findViewById(R.id.peopleOccupationView);
		peopleDetailView = findViewById(R.id.peopleDetailView);

		editPersonFab = findViewById(R.id.editPersonFab);

        dbHelper = new PeopleDBHelper(this);

		personId = getIntent().getIntExtra("personId", -1);
		
		if (personId == -1) {
			Toast.makeText(this, "Error loading person data", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		loadPerson();

		editPersonFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
				    showEditPersonDialog(personId);
				}
			});
    }

	private void loadPerson() {
		Cursor cursor = dbHelper.getPersonById(personId);

		if (cursor != null && cursor.moveToFirst()) {
			int nameIndex = cursor.getColumnIndexOrThrow("name");
			int occupationIndex = cursor.getColumnIndexOrThrow("occupation");
			int descriptionIndex = cursor.getColumnIndexOrThrow("description");

			String name = cursor.getString(nameIndex);
			String occupation = cursor.getString(occupationIndex);
			String detail = cursor.getString(descriptionIndex);

			peopleDetailTitle.setText(name);
		    peopleOccupationView.setText(occupation);
			peopleDetailView.setText(detail);
		}

		if (cursor != null) {
			cursor.close();
		}
	}
	
	private void showEditPersonDialog(final int personId) {
		Cursor cursor = null;
		try {
			cursor = dbHelper.getPersonById(personId);
			if (cursor == null || !cursor.moveToFirst()) return;

			String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
			String occupation = cursor.getString(cursor.getColumnIndexOrThrow("occupation"));
			String detail = cursor.getString(cursor.getColumnIndexOrThrow("description"));

			View dialogView = LayoutInflater.from(this).inflate(R.layout.people_edit_dialog, null);
			final EditText personNameEdit = dialogView.findViewById(R.id.personNameEdit);
			final EditText personOccupationEdit = dialogView.findViewById(R.id.personOccupationEdit);
			final EditText personDescriptionEdit = dialogView.findViewById(R.id.personDescriptionEdit);

			personNameEdit.setText(name);
			personOccupationEdit.setText(occupation);
			personDescriptionEdit.setText(detail);

			new AlertDialog.Builder(this)
				.setTitle("Edit Person")
				.setView(dialogView)
				.setPositiveButton("Save", new DialogInterface.OnClickListener() {
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
							loadPerson();
							Toast.makeText(getApplicationContext(), "Person inserted successfully", Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(getApplicationContext(), "Failed: Person not inserted!", Toast.LENGTH_SHORT).show();
						}
					}
				})
				.setNegativeButton("Cancel", null)
				.setCancelable(false)
				.show();
		} finally {
			if (cursor != null) cursor.close();
		}
	}

	@Override
    protected void onDestroy() {
        if (dbHelper != null) dbHelper.close();
        super.onDestroy();
    }

	@Override
	protected void onResume() { // Updates activity when it comes to view again.
		super.onResume();
		loadPerson();
	}
}

package com.mwendasoft.superme.sumrys;

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
import com.mwendasoft.superme.helpers.*;

public class SumryAddActivity extends BaseActivity {
    private EditText sumryTitleAdd, sumryAuthorAdd, sumrySummaryAdd;
    private CheckBox sumryFavoriteAddCheckbox;
	private Button sumrySaveAddBtn;
    private SumrysDBHelper sumrysDBHelper;
	
	private int favorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sumry_add_activity);

		sumryTitleAdd = findViewById(R.id.sumryTitleAdd);
        sumryAuthorAdd = findViewById(R.id.sumryAuthorAdd);
		sumrySummaryAdd = findViewById(R.id.sumrySummaryAdd);
		
		sumryFavoriteAddCheckbox = findViewById(R.id.sumryFavoriteAddCheckbox);
		sumrySaveAddBtn = findViewById(R.id.sumrySaveAddBtn);

		sumrysDBHelper = new SumrysDBHelper(this);
		sumryFavoriteAddCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						favorite = 1;
					} else {
						favorite = 0;
					}
				}
			});

        sumrySaveAddBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					saveSummary();
				}
			});
    }

	private void saveSummary() {
		String title = sumryTitleAdd.getText().toString().trim();
		String author = sumryAuthorAdd.getText().toString().trim();
		String summary = sumrySummaryAdd.getText().toString().trim();
		
		if (title.isEmpty() || summary.isEmpty()) {
			ToastMessagesManager.show(this, "Please fill all the fields!");
			return;
		}

		ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("author", author);
        values.put("summary", summary);
        values.put("favorite", favorite);

		long result = sumrysDBHelper.insertSummary(values);
		ToastMessagesManager.show(this, result != -1 ? "Added successfully." : "Adding failed!");
		
		if (result != -1)  {
			finish();
		}
	}
}

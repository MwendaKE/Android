package com.mwendasoft.superme.sumrys;

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

public class SumryDetailActivity extends BaseActivity {
    private SumrysDBHelper dbHelper;
	private TextView sumryDetailTitle, sumryAuthorView,sumryDetailView; 
    private ImageButton editSumryFab;

	private Sumry selectedSumry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sumry_detail_activity);

        sumryDetailTitle = findViewById(R.id.sumryDetailTitle);
        sumryAuthorView = findViewById(R.id.sumryAuthorView);
		sumryDetailView = findViewById(R.id.sumryDetailView);
		
		editSumryFab = findViewById(R.id.editSumryFab);

        dbHelper = new SumrysDBHelper(this);

		selectedSumry = (Sumry) getIntent().getSerializableExtra("selectedSumry");

		loadSummary();

		editSumryFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(SumryDetailActivity.this, SumryEditActivity.class);
					intent.putExtra("selectedSumry", selectedSumry);
					startActivity(intent);
				}
			});
    }

	private void loadSummary() {
		Cursor cursor = dbHelper.getSummaryById(selectedSumry.getId());

		if (cursor != null && cursor.moveToFirst()) {
			int titleIndex = cursor.getColumnIndexOrThrow("title");
			int authorIndex = cursor.getColumnIndexOrThrow("author");
			int sumryIndex = cursor.getColumnIndexOrThrow("summary");
			
			String title = cursor.getString(titleIndex);
			String author = cursor.getString(authorIndex);
			String sumry = cursor.getString(sumryIndex);
			
			sumryDetailTitle.setText(title);
		    sumryAuthorView.setText(author);
			sumryDetailView.setText(sumry);
		}

		if (cursor != null) {
			cursor.close();
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
		loadSummary();
	}
}

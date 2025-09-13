package com.mwendasoft.superme.sumrys;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class SumryEditActivity extends BaseActivity {
    private EditText sumryTitleEdit, sumryAuthorEdit, sumrySummaryEdit;
    private CheckBox sumryFavoriteEditCheckbox;
	private Button sumrySaveEditBtn;
    private SumrysDBHelper sumrysDBHelper;
	private Sumry selectedSumry;
	
	private int favorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sumry_edit_activity);

		sumryTitleEdit = findViewById(R.id.sumryTitleEdit);
        sumryAuthorEdit = findViewById(R.id.sumryAuthorEdit);
		sumrySummaryEdit = findViewById(R.id.sumrySummaryEdit);

		sumryFavoriteEditCheckbox = findViewById(R.id.sumryFavoriteEditCheckbox);
		sumrySaveEditBtn = findViewById(R.id.sumrySaveEditBtn);

		sumrysDBHelper = new SumrysDBHelper(this);
		
		selectedSumry = (Sumry) getIntent().getSerializableExtra("selectedSumry");
		
		loadSummary();
		
		sumryFavoriteEditCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						favorite = 1;
					} else {
						favorite = 0;
					}
				}
			});

        sumrySaveEditBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					saveSummary();
				}
			});
    }
	
	private void loadSummary() {
		sumryTitleEdit.setText(selectedSumry.getTitle());
		sumryAuthorEdit.setText(selectedSumry.getAuthor());
		sumrySummaryEdit.setText(selectedSumry.getSumry());
		sumryFavoriteEditCheckbox.setChecked(selectedSumry.getFavorite() == 1);
	}

	private void saveSummary() {
		String title = sumryTitleEdit.getText().toString().trim();
		String author = sumryAuthorEdit.getText().toString().trim();
		String summary = sumrySummaryEdit.getText().toString().trim();

		if (title.isEmpty() || summary.isEmpty()) {
			ToastMessagesManager.show(this, "Please fill all the fields!");
			return;
		}

		ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("author", author);
        values.put("summary", summary);
        values.put("favorite", favorite);

		long result = sumrysDBHelper.updateSummary(values, selectedSumry.getId());
		ToastMessagesManager.show(this, result != -1 ? "Updated successfully." : "Failed to update!");
		
		if (result != -1)  {
			finish();
		}
	}
}

package com.mwendasoft.superme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.BaseActivity;
import android.widget.*;
import android.view.*;
import android.content.*;
import android.support.v4.content.*;

public class SearchViewActivity extends BaseActivity {
    private ListView searchResultsListView;
	private TextView searchResultsCountBadge;
	private ArrayList<SearchItem> searchResultsList;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_view_activity);

        searchResultsListView = findViewById(R.id.searchResultsListView);
		registerForContextMenu(searchResultsListView);
		searchResultsCountBadge = findViewById(R.id.searchResultsCountBadge);
		
        //###
		ArrayList<SearchItem> searchResults = (ArrayList<SearchItem>) getIntent().getSerializableExtra("results");
		//##
		
        if (searchResults != null && !searchResults.isEmpty()) {
			searchResultsCountBadge.setText(String.valueOf(searchResults.size()));
			searchResultsList = new ArrayList<>(searchResults);
            searchResultsListView.setAdapter(new SearchViewAdapter(this, searchResultsList));
        } else {
            Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
        }
    }
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.searchResultsListView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle("Choose an action");

			menu.add(Menu.NONE, 1, 1, "Share");
			menu.add(Menu.NONE, 2, 2, "Copy");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		SearchItem selectedItem = searchResultsList.get(info.position);

		switch (item.getItemId()) {
			case 1: // Share
				shareClip(selectedItem);
				return true;
			case 2: // Copy
				copyClipText(selectedItem);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	private boolean shareClip(SearchItem selectedItem) {
		String shareText = selectedItem.getText1() + " ~ " + selectedItem.getText2();

		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
		startActivity(Intent.createChooser(shareIntent, "Share via"));
		return true;
	}

	private void copyClipText(SearchItem selectedItem) {
		String copyText = selectedItem.getText1() + " ~ " + selectedItem.getText2();

		ClipboardManager clipboard = (ClipboardManager) ContextCompat.getSystemService(this, ClipboardManager.class);
		if (clipboard != null) {
			ClipData clipData = ClipData.newPlainText("Search Results",copyText);
			clipboard.setPrimaryClip(clipData);
			Toast.makeText(this, "Selected text copied to clipboard", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Clipboard unavailable", Toast.LENGTH_SHORT).show();
		}
	}
}

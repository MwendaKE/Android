package com.mwendasoft.newsip.bookmarks;

import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.mwendasoft.newsip.newshelpers.*;
import com.mwendasoft.newsip.bookmarks.*; 
import java.util.*;
import android.app.*;
import com.mwendasoft.newsip.*;
import android.view.*;

public class BookmarksActivity extends BaseActivity {

	private ListView listView;
	private DatabaseHelper dbHelper;
	private BookmarksAdapter adapter;
	private List<BookmarkItem> bookmarks = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bookmarks_activity);
		updateTabHighlighting(R.id.navBookmarks);
		
		listView = findViewById(R.id.bookmarksListView);
		registerForContextMenu(listView);
		dbHelper = new DatabaseHelper(this);

		loadBookmarks();

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					BookmarkItem item = (BookmarkItem) adapter.getItem(position);
					if (item != null) {
						openBookmark(item);
					}
				}
			});
	}
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.bookmarksListView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle("Options");
			menu.add(Menu.NONE, 1, 1, "Open");
			menu.add(Menu.NONE, 2, 2, "Delete");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		BookmarkItem selectedBookmark = bookmarks.get(info.position);
		
		switch (item.getItemId()) {
			case 1: // Open
				openBookmark(selectedBookmark);
				return true;
			case 2: // Delete
				confirmAndDeleteBookmark(selectedBookmark);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	private void openBookmark(BookmarkItem item) {
		Intent intent = new Intent(BookmarksActivity.this, NewsDetailActivity.class);
		intent.putExtra("title", item.getTitle());
		intent.putExtra("url", item.getUrl());
		startActivity(intent);
	}

	private void confirmAndDeleteBookmark(final BookmarkItem item) {
		new AlertDialog.Builder(this)
			.setTitle("Delete Bookmark")
			.setMessage("Are you sure you want to delete \"" + item.getTitle() + "\"?")
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dbHelper.deleteBookmark(item.getUrl());
					loadBookmarks();
					bookmarks.remove(item);
					adapter.notifyDataSetChanged();
				}
			})
			.setNegativeButton("No", null)
			.show();
	}
	
	private void loadBookmarks() {
		bookmarks.clear();
		
		Cursor cursor = dbHelper.getAllBookmarks();

		if (cursor == null || cursor.getCount() == 0) {
			new AlertDialog.Builder(this)
				.setTitle("No Bookmarks")
				.setMessage("You have no bookmarks to view.")
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish(); // Go back to previous screen
					}
				})
				.show();
			return;
		}

		while (cursor.moveToNext()) {
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String url = cursor.getString(cursor.getColumnIndex("url"));
			bookmarks.add(new BookmarkItem(title, url));
		}
		cursor.close(); // Don't forget to close the cursor

		adapter = new BookmarksAdapter(this, bookmarks);
		listView.setAdapter(adapter);
	}
}

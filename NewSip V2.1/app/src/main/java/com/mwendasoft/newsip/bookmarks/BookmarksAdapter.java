package com.mwendasoft.newsip.bookmarks;

import android.content.Context;
import android.view.*;
import android.widget.*;
import com.mwendasoft.newsip.bookmarks.*;
import com.mwendasoft.newsip.R;
import java.util.List;

public class BookmarksAdapter extends BaseAdapter {

	private Context context;
	private List<BookmarkItem> bookmarks;
	private LayoutInflater inflater;

	public BookmarksAdapter(Context context, List<BookmarkItem> bookmarks) {
		this.context = context;
		this.bookmarks = bookmarks;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return bookmarks != null ? bookmarks.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		if (bookmarks != null && position >= 0 && position < bookmarks.size()) {
			return bookmarks.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position; // You can use position since there's no unique ID
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.bookmarks_list_item, parent, false);
		}

		TextView titleView = view.findViewById(R.id.bookmarkTitle);
		BookmarkItem item = bookmarks.get(position);
		titleView.setText(item.getTitle());

		return view;
	}
}

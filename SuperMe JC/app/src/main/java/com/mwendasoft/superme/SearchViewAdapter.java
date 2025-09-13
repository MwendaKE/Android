package com.mwendasoft.superme;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

import com.mwendasoft.superme.R;
import android.text.*;
import android.text.style.*;
import android.support.v4.content.*;
import android.graphics.*;


public class SearchViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<SearchItem> searchResultsList;

    public SearchViewAdapter(Context context, ArrayList<SearchItem> searchResultsList) {
        this.context = context;
        this.searchResultsList = searchResultsList;
    }

    @Override
    public int getCount() {
        return searchResultsList.size();
    }

    @Override
    public Object getItem(int position) {
        return searchResultsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.search_results_list_item, parent, false);
        }
		
		TextView searchResultsTextPri = convertView.findViewById(R.id.searchResultsTextView1);
        TextView searchResultsTextSec = convertView.findViewById(R.id.searchResultsTextView2);
		SearchItem item = searchResultsList.get(position);
		
		String searchText1 = item.getText1();
		String searchText2 = item.getText2();
		String searchText3 = item.getText3();
		
		String secondaryText = searchText2 + ", " + searchText3;

		SpannableString spannable = new SpannableString(secondaryText);

		// Style for the title (green, bold)
		spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.customForestGreen)), 0, searchText2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.customOchre)), searchText2.length() + 1, secondaryText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		searchResultsTextPri.setText(searchText1);
		searchResultsTextSec.setText(spannable);
		
        return convertView;
    }
}

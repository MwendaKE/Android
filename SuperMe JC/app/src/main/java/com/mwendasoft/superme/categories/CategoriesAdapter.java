package com.mwendasoft.superme.categories;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.core.*;
import android.text.*;
import android.text.style.*;
import android.graphics.*;
import com.mwendasoft.superme.categories.*;
import android.support.v4.content.*;
import android.support.v4.util.*;
import android.widget.*;
import com.mwendasoft.superme.books.*;
import com.mwendasoft.superme.sumrys.*;
import com.mwendasoft.superme.quotes.*;

public class CategoriesAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Category> categs;
	
	private CategoriesDBHelper dbHelper;

	public CategoriesAdapter(Context context, ArrayList<Category> authors) {
        this.context = context;
        this.categs = authors;
    }

    @Override
    public int getCount() {
        return categs.size();
    }

    @Override
    public Object getItem(int position) {
        return categs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the view if not reused
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.categories_list_item, parent, false);
        }

        final Category categ = categs.get(position);
		dbHelper = new CategoriesDBHelper(context);
		
        // Get views from layout
        TextView categNameView = convertView.findViewById(R.id.categNameView);
        
		// Set title
		SpannableStringBuilder titleText = new SpannableStringBuilder(categ.getName());
        titleText.setSpan(new ForegroundColorSpan(Color.parseColor("#0B6623")), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleText.setSpan(new StyleSpan(Typeface.BOLD), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		categNameView.setText(titleText);
		
        return convertView;
    }
}

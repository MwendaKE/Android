package com.mwendasoft.superme.authors;

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

public class AuthorsAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Author> authors;
	private AuthorsDBHelper dbHelper;
	private BooksDBHelper bkHelper;
	private SumrysDBHelper smHelper;
	private QuotesDBHelper qtHelper;
	
	public AuthorsAdapter(Context context, ArrayList<Author> authors) {
        this.context = context;
        this.authors = authors;
    }

    @Override
    public int getCount() {
        return authors.size();
    }

    @Override
    public Object getItem(int position) {
        return authors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the view if not reused
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.author_list_item, parent, false);
        }

        final Author author = authors.get(position);
		dbHelper = new AuthorsDBHelper(context);
		bkHelper = new BooksDBHelper(context);
		smHelper = new SumrysDBHelper(context);
		qtHelper = new QuotesDBHelper(context);
		
        // Get views from layout
        TextView authorNameView = convertView.findViewById(R.id.authorNameView);
        TextView authorOccupationView = convertView.findViewById(R.id.authorOccupationView);
		TextView authorWorksCountBadge = convertView.findViewById(R.id.authorWorksCountBadge);
		
		// Set title
		SpannableStringBuilder titleText = new SpannableStringBuilder(author.getName());
        titleText.setSpan(new ForegroundColorSpan(Color.parseColor("#0B6623")), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleText.setSpan(new StyleSpan(Typeface.BOLD), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		authorNameView.setText(titleText);

		// Set author
        SpannableStringBuilder authorText = new SpannableStringBuilder(author.getOccupation());
        authorText.setSpan(new ForegroundColorSpan(Color.parseColor("#8D4004")), 0, authorText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        authorOccupationView.setText(authorText);
		
		int booksCount = bkHelper.getBooksCountByAuthor(author.getName());
		int summaryCount = smHelper.getSumrysCountByAuthor(author.getName());
	
		int totalCount = booksCount + summaryCount;
		
		authorWorksCountBadge.setText(String.valueOf(totalCount));
		
        return convertView;
    }
}

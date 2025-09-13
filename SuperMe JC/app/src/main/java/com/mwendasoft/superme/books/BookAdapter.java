package com.mwendasoft.superme.books;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

import com.mwendasoft.superme.R;
import android.text.*;
import android.support.v4.content.*;
import android.text.style.*;
import android.graphics.*;


public class BookAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Book> books;

    public BookAdapter(Context context, ArrayList<Book> books) {
        this.context = context;
        this.books = books;
    }

    @Override
    public int getCount() {
        return books.size();
    }

    @Override
    public Object getItem(int position) {
        return books.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.book_list_item, parent, false);
		}

		TextView bookTitleView = convertView.findViewById(R.id.bookTitleView);
		TextView bookAuthorView = convertView.findViewById(R.id.bookAuthorView);
		
		Book book = books.get(position);
		
		// Set title
		String bookEmoji = getBookEmoji(book.getStatus());
		SpannableStringBuilder titleText = new SpannableStringBuilder(bookEmoji + "  " + book.getTitle());
        titleText.setSpan(new ForegroundColorSpan(getBookColor(book.getStatus())), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleText.setSpan(new StyleSpan(Typeface.BOLD), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		bookTitleView.setText(titleText);

		// Set author
        SpannableStringBuilder authorText = new SpannableStringBuilder(book.getAuthor());
        authorText.setSpan(new ForegroundColorSpan(getBookColor(book.getStatus())), 0, authorText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		//authorText.setSpan(new StyleSpan(Typeface.ITALIC), 0, authorText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bookAuthorView.setText(authorText);

		return convertView;
	}
	
	private int getBookColor(int readStatus) {
		int readColor = ContextCompat.getColor(context, R.color.customForestGreen);
		int readingColor = ContextCompat.getColor(context, R.color.customOchre);
		int notReadColor = ContextCompat.getColor(context, R.color.customMaroon);
		int otherColor = ContextCompat.getColor(context, R.color.customMidnightBlack);

		if (readStatus == 0) {return notReadColor;} 
		else if (readStatus == 1) {return readColor;} 
		else if (readStatus == 2) {return readingColor;}
		else return otherColor;
	}

	private String getBookEmoji(int readStatus) {
		if (readStatus == 0) {return "📕";} 
		else if (readStatus == 1) {return "📗";} 
		else if (readStatus == 2) {return "📙";}
		else return "📚";
	}
}

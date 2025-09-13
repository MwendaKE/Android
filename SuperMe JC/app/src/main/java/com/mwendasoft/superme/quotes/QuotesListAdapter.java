package com.mwendasoft.superme.quotes;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

import com.mwendasoft.superme.R;
import android.text.*;


public class QuotesListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Quote> quotesList;

    public QuotesListAdapter(Context context, ArrayList<Quote> quotesList) {
        this.context = context;
        this.quotesList = quotesList;
    }

    @Override
    public int getCount() {
        return quotesList.size();
    }

    @Override
    public Object getItem(int position) {
        return quotesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.quotes_list_item, parent, false);
        }

        TextView quotesListItem = convertView.findViewById(R.id.quotesListItem);
        TextView quoteAuthorView = convertView.findViewById(R.id.quoteAuthorView);
		
		Quote quote = quotesList.get(position);
		
		quotesListItem.setText(quote.getQuoteText());
		quoteAuthorView.setText(quote.getAuthorName());
		
        return convertView;
    }
}

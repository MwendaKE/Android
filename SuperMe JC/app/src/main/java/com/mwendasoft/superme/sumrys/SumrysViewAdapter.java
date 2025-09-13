package com.mwendasoft.superme.sumrys;

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

public class SumrysViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Sumry> summaries;
	private SumrysDBHelper dbHelper;
    
	public SumrysViewAdapter(Context context, ArrayList<Sumry> summaries) {
        this.context = context;
        this.summaries = summaries;
    }

    @Override
    public int getCount() {
        return summaries.size();
    }

    @Override
    public Object getItem(int position) {
        return summaries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the view if not reused
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.sumry_list_item, parent, false);
        }

        final Sumry sumry = summaries.get(position);
		dbHelper = new SumrysDBHelper(context);
		
        // Get views from layout
        TextView sumryTitleView = convertView.findViewById(R.id.sumryTitleView);
        TextView sumryAuthorView = convertView.findViewById(R.id.sumryAuthorView);
		final ImageButton sumryFavoriteButton = convertView.findViewById(R.id.sumryFavoriteButton);
        
		final int isFavorite = sumry.getFavorite();
		
		if (isFavorite == 1) {
			sumryFavoriteButton.setColorFilter(Color.parseColor("#800000")); // Maroon
		} else {
			sumryFavoriteButton.setColorFilter(null); // Default
		}
		
		sumryFavoriteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isFavorite == 0) {
					sumryFavoriteButton.setColorFilter(Color.parseColor("#800000")); // Maroon
					dbHelper.setSummaryAsFavorite(sumry.getId());
					sumry.setFavorite();
				} else {
					sumryFavoriteButton.setColorFilter(null); // Default
					dbHelper.unSetSummaryAsFavorite(sumry.getId());
					sumry.unSetFavorite();
				}
			}
		});
		
		// Set title
		SpannableStringBuilder titleText = new SpannableStringBuilder(sumry.getTitle());
        titleText.setSpan(new ForegroundColorSpan(Color.parseColor("#0B6623")), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleText.setSpan(new StyleSpan(Typeface.BOLD), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		sumryTitleView.setText(titleText);
		
		// Set author
        SpannableStringBuilder authorText = new SpannableStringBuilder(sumry.getAuthor());
        authorText.setSpan(new ForegroundColorSpan(Color.parseColor("#8D4004")), 0, authorText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sumryAuthorView.setText(authorText);

        return convertView;
    }
}

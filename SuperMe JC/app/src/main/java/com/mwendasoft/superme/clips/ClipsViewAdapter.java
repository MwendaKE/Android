package com.mwendasoft.superme.clips;

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
import android.graphics.*;
import android.support.v4.content.*;


public class ClipsViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Clip> clipsList;

    public ClipsViewAdapter(Context context, ArrayList<Clip> clipsList) {
        this.context = context;
        this.clipsList = clipsList;
    }

    @Override
    public int getCount() {
        return clipsList.size();
    }

    @Override
    public Object getItem(int position) {
        return clipsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.clips_list_item, parent, false);
        }
		
		TextView clipsListItem = convertView.findViewById(R.id.clipsListItem);
		
		Clip clip = clipsList.get(position);
		String clipText = clip.getClip();
		String clipSource = clip.getSource();
		String clipWriter = clip.getWriter();
		String fullClipText = clipText + " ~ " + clipWriter + ", " + clipSource;
        
		SpannableString spannable = new SpannableString(fullClipText);
		
		int clipStart = 0;
		int clipEnd = clipText.length();
		int writerStart = clipEnd + 3;
		int writerEnd = writerStart + clipWriter.length();
		int sourceStart = writerEnd + 2;
		int sourceEnd = sourceStart + clipSource.length();
		
		int clipColor = ContextCompat.getColor(context, R.color.customBlack);
		int sourceColor = ContextCompat.getColor(context, R.color.customJadeGreen);
		int writerColor = ContextCompat.getColor(context, R.color.customOchre);
		
		// COLORS
		
		spannable.setSpan(new ForegroundColorSpan(clipColor), clipStart, clipEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); 
		spannable.setSpan(new ForegroundColorSpan(writerColor), writerStart, writerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); 
		spannable.setSpan(new ForegroundColorSpan(sourceColor), sourceStart, sourceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); 
		
		// ITALICS
		
		spannable.setSpan(new StyleSpan(Typeface.ITALIC), writerStart, writerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannable.setSpan(new StyleSpan(Typeface.ITALIC), sourceStart, sourceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		clipsListItem.setText(spannable);

        return convertView;
    }
}

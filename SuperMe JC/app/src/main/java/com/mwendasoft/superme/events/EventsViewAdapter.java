package com.mwendasoft.superme.events;

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
import android.support.v4.content.*;


public class EventsViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Event> eventsList;
	private SuperMeTimeHelper appTimeHelper;

    public EventsViewAdapter(Context context, ArrayList<Event> eventsList) {
        this.context = context;
        this.eventsList = eventsList;
    }

    @Override
    public int getCount() {
        return eventsList.size();
    }

    @Override
    public Object getItem(int position) {
        return eventsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false);
        }
		
		Event event = eventsList.get(position);
        
		appTimeHelper = new SuperMeTimeHelper(event.getDate(), event.getTime());
		
		TextView eventListTitle = convertView.findViewById(R.id.eventListTitle);
		TextView eventListDateTime = convertView.findViewById(R.id.eventListDateTime);
        
		SpannableString formattedTitle = getFormattedTitle(event.getAttendance(), event.getTitle());
		SpannableString formattedVenueTitle = getFormattedSubtitle(event.getAttendance(), event.getAddress());
		eventListTitle.setText(formattedTitle);
		eventListDateTime.setText(formattedVenueTitle);
		
        return convertView;
    }

	private SpannableString getFormattedSubtitle(int attendance, String eventVenue) {
		String formattedTime = appTimeHelper.getDueTime();

		// Combine formattedTime and subTitle into one string
		String fullTitle = formattedTime + "  |  " + eventVenue;
		int venueColor = (attendance == 2) ? ContextCompat.getColor(context, R.color.customForestGreen) : ContextCompat.getColor(context, R.color.customGray);
		int timeColor = (attendance == 2) ? ContextCompat.getColor(context, R.color.customWine) : ContextCompat.getColor(context, R.color.customGray);
		
		int venueStart = formattedTime.length() + 5; // +5 for the space and pipe
		int venueEnd = venueStart + eventVenue.length();
		
		SpannableString spannable = new SpannableString(fullTitle);
		spannable.setSpan(new ForegroundColorSpan(timeColor), 0, formattedTime.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (attendance == 2) spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, formattedTime.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannable.setSpan(new ForegroundColorSpan(venueColor), venueStart, venueEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (attendance == 2) spannable.setSpan(new StyleSpan(Typeface.BOLD), venueStart, venueEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		return spannable;
	}

	private SpannableString getFormattedTitle(int attendance, String title) {
		String eventEmoji = getEventEmoji(attendance);
		String fullTitle = eventEmoji + "  " + title;

		SpannableString spannable = new SpannableString(fullTitle);

		int color = getTitleColor(attendance);

		spannable.setSpan(new ForegroundColorSpan(color), 0, fullTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, fullTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		return spannable;
	}

	private String getEventEmoji(int attendance) {
		switch (attendance) {
			case 0: return "⌛";
			case 1: return "🗓️";
			case 2: return "⏳";
			default: return "⏰️️";
		}
	}

	private int getTitleColor(int attendance) {
		int attendedColor = ContextCompat.getColor(context, R.color.customForestGreen);
		int notYetAttendedColor = ContextCompat.getColor(context, R.color.customOchre);
		int notAttended = ContextCompat.getColor(context, R.color.customRust);
		int otherColor = ContextCompat.getColor(context, R.color.colorPrimary);

		switch (attendance) {
			case 0: return notAttended; // Green
			case 1: return attendedColor;   // Maroon
			case 2: return notYetAttendedColor;
			default: return otherColor;      // Yellow
		}
	}

	// END //
}

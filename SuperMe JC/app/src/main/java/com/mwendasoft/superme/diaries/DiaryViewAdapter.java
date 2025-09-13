package com.mwendasoft.superme.diaries;

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
import com.mwendasoft.superme.core.*;

public class DiaryViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Diary> diaryList;
	private SuperMeAppHelper appHelper;
	
    public DiaryViewAdapter(Context context, ArrayList<Diary> diaryList) {
        this.context = context;
        this.diaryList = diaryList;
    }

    @Override
    public int getCount() {
        return diaryList.size();
    }

    @Override
    public Object getItem(int position) {
        return diaryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.diary_list_item, parent, false);
        }
		
		Diary selectedDiary = diaryList.get(position);
		
		String diaryTitle = selectedDiary.getTitle();
		String diaryMood = selectedDiary.getMood();
		String diaryDate = selectedDiary.getDate();
		String diaryTime = selectedDiary.getTime();
		
		appHelper = new SuperMeAppHelper(diaryDate, diaryTime);
		
		String formattedDateTime = appHelper.getFormattedDate() + ",  " + appHelper.getFormattedTime();
		String relativeTime = appHelper.getRelativeTime();
		
        TextView diaryTitleView = convertView.findViewById(R.id.diaryTitle);
		TextView diaryEntryTime = convertView.findViewById(R.id.diaryEntryTime);
		
        diaryTitleView.setText(getStyledDiaryTitle(diaryMood, diaryTitle));
		diaryEntryTime.setText(formattedDateTime + "  |  " + relativeTime);
		
        return convertView;
    }
	
	// STYLE THE DIARY TITLE //
	
	private SpannableString getStyledDiaryTitle(String mood, String diaryTitle) {
		String moodEmoji = getMoodEmoji(mood);
		String fullTitle = moodEmoji + "  " + diaryTitle;

		SpannableString spannable = new SpannableString(fullTitle);

		int color = getMoodColor(mood);

		spannable.setSpan(new ForegroundColorSpan(color), 0, fullTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, fullTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		return spannable;
	}
	
	private String getMoodEmoji(String mood) {
		switch (mood.toLowerCase()) {
			case "happy": return "😊";
			case "sad": return "😢";
			case "confused": return "😕";
			default: return "✍️";
		}
	}

	private int getMoodColor(String mood) {
		int sadColor = ContextCompat.getColor(context, R.color.customGray);
		int happyColor = ContextCompat.getColor(context, R.color.customForestGreen);
		int confusedColor = ContextCompat.getColor(context, R.color.customRose);
		int otherColor = ContextCompat.getColor(context, R.color.customRust);
		
		switch (mood.toLowerCase()) {
			case "happy": return happyColor; // Green
			case "sad": return sadColor;   // Maroon
			case "confused": return confusedColor;
			default: return otherColor;      // Yellow
		}
	}
	
	// END //
}

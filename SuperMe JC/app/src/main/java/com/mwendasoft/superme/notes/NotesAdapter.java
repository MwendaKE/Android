package com.mwendasoft.superme.notes;

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


public class NotesAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Note> notes;

    public NotesAdapter(Context context, ArrayList<Note> notes) {
        this.context = context;
        this.notes = notes;
    }

    @Override
    public int getCount() {
        return notes.size();
    }

    @Override
    public Object getItem(int position) {
        return notes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.note_list_item, parent, false);
        }

        TextView songListItem = convertView.findViewById(R.id.noteTitle);

		Note note = notes.get(position);
		
		// Set title
		SpannableStringBuilder titleText = new SpannableStringBuilder("📝" + "  " + note.getTitle());
        titleText.setSpan(new ForegroundColorSpan(getTitleColor(note.getImportance())), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleText.setSpan(new StyleSpan(Typeface.BOLD), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		songListItem.setText(titleText);
		
        return convertView;
    }
	
	private int getTitleColor(int importance) {
		if (importance == 1) {
			return ContextCompat.getColor(context, R.color.customOchre);
	    } else {
			return ContextCompat.getColor(context, R.color.customForestGreen);
		}
	}
}

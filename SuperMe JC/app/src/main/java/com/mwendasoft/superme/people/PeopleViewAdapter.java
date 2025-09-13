package com.mwendasoft.superme.people;

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

public class PeopleViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Personn> people;
	private PeopleDBHelper dbHelper;

	public PeopleViewAdapter(Context context, ArrayList<Personn> people) {
        this.context = context;
        this.people = people;
    }

    @Override
    public int getCount() {
        return people.size();
    }

    @Override
    public Object getItem(int position) {
        return people.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the view if not reused
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.people_list_item, parent, false);
        }

        final Personn person = people.get(position);
		dbHelper = new PeopleDBHelper(context);

        // Get views from layout
        TextView personTitleView = convertView.findViewById(R.id.personTitleText);
        TextView personOccupationView = convertView.findViewById(R.id.personOccupationText);
		
		// Set title
		SpannableStringBuilder nameText = new SpannableStringBuilder(person.getName());
        nameText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, nameText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        nameText.setSpan(new StyleSpan(Typeface.BOLD), 0, nameText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		personTitleView.setText(nameText);

		// Set author
        SpannableStringBuilder occupationText = new SpannableStringBuilder(person.getOccupation());
        occupationText.setSpan(new ForegroundColorSpan(Color.parseColor("#8D4004")), 0, occupationText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        personOccupationView.setText(occupationText);

        return convertView;
    }
}

package com.mwendasoft.superme.poems;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

import com.mwendasoft.superme.R;
import android.text.*;


public class PoemViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Poem> poemList;

    public PoemViewAdapter(Context context, ArrayList<Poem> poemList) {
        this.context = context;
        this.poemList = poemList;
    }

    @Override
    public int getCount() {
        return poemList.size();
    }

    @Override
    public Object getItem(int position) {
        return poemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.poem_list_item, parent, false);
        }
		
		TextView poemListTitle = convertView.findViewById(R.id.poemTitle);

		Poem poem = poemList.get(position);
		String formatted = "💝  <b>" + poem.getPoemTitle() + "</b> | <i>" + poem.getPoemAuthor() + "</i>";

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			poemListTitle.setText(Html.fromHtml(formatted, Html.FROM_HTML_MODE_LEGACY));
		} else {
			poemListTitle.setText(Html.fromHtml(formatted));
		}
		
        return convertView;
    }
}

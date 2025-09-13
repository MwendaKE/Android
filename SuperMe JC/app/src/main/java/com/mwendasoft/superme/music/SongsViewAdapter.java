package com.mwendasoft.superme.music;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

import com.mwendasoft.superme.R;
import android.text.*;


public class SongsViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Song> songsList;

    public SongsViewAdapter(Context context, ArrayList<Song> songsList) {
        this.context = context;
        this.songsList = songsList;
    }

    @Override
    public int getCount() {
        return songsList.size();
    }

    @Override
    public Object getItem(int position) {
        return songsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.music_list_item, parent, false);
        }
		
        TextView songListItem = convertView.findViewById(R.id.songTitle);
        
		Song song = songsList.get(position);
		String formatted = "🪉  <b>" + song.getSongTitle() + "</b> | <i>" + song.getSongArtist() + "</i>";

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			songListItem.setText(Html.fromHtml(formatted, Html.FROM_HTML_MODE_LEGACY));
		} else {
			songListItem.setText(Html.fromHtml(formatted));
		}
		
        return convertView;
    }
}

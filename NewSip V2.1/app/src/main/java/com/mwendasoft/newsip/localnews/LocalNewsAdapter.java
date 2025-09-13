package com.mwendasoft.newsip.localnews;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import android.text.*;
import org.xml.sax.*;
import com.mwendasoft.newsip.*;

public class LocalNewsAdapter extends RecyclerView.Adapter<LocalNewsAdapter.NewsViewHolder> {

    private Context context;
    private ArrayList<LocalNewsItem> newsList;

    public LocalNewsAdapter(Context context, ArrayList<LocalNewsItem> newsList) {
        this.context = context;
        this.newsList = newsList;
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_item, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NewsViewHolder holder, int position) {
        final LocalNewsItem current = newsList.get(position);

        holder.titleText.setText(current.getTitle());
        holder.descriptionText.setText(current.getDescription());
		holder.dateText.setText(current.getPubDate());
		holder.sourceText.setText(current.getSource());
	
        // Open link when item is clicked
        holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, NewsDetailActivity.class);
					intent.putExtra("title", current.getTitle());
					intent.putExtra("url", current.getLink());
					context.startActivity(intent);
				}
			});
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    class NewsViewHolder extends RecyclerView.ViewHolder {

        TextView titleText, descriptionText, dateText, sourceText;

        public NewsViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.newsTitle);
            descriptionText = itemView.findViewById(R.id.newsDescription);
			dateText = itemView.findViewById(R.id.newsDate);
			sourceText = itemView.findViewById(R.id.newsSource);
        }
    }
}

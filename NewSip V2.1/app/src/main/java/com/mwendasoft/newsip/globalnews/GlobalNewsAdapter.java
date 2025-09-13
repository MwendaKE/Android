package com.mwendasoft.newsip.globalnews;
import android.content.*;
import android.net.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

import com.mwendasoft.newsip.R;

import android.net.ParseException;
import com.mwendasoft.newsip.*;

public class GlobalNewsAdapter extends RecyclerView.Adapter <GlobalNewsAdapter.ViewHolder> {
	private List<GlobalNewsItem> newsList;
	private Context context;

	public GlobalNewsAdapter(List<GlobalNewsItem> newsList, Context context) {
		this.newsList = newsList;
		this.context = context;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.global_news_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		final GlobalNewsItem news = newsList.get(position);
		holder.title.setText(news.getTitle());
		holder.description.setText(news.getDescription());
		holder.sourceAuthor.setText(news.getAuthor() + " | " + news.getSource());
		holder.date.setText(getRelativeTime(news.getPublishedAt()));

		holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, NewsDetailActivity.class);
					intent.putExtra("title", news.getTitle());
					intent.putExtra("url", news.getUrl());
					context.startActivity(intent);
				}
			});
	}

	@Override
	public int getItemCount() {
		return newsList.size();
	}

	private String getRelativeTime(String publishedAt) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		try {
			Date date = sdf.parse(publishedAt);
			long time = date.getTime();
			long now = System.currentTimeMillis();
			long diff = now - time;

			if (diff < 60000) return "Just now"; // Less than 1 minute
			else if (diff < 3600000) return (diff / 60000) + " minutes ago"; // Less than 1 hour
			else if (diff < 86400000) return (diff / 3600000) + " hours ago"; // Less than 1 day
			else return (diff / 86400000) + " days ago"; // More than a day

		} catch (java.text.ParseException e) {  // Caught exception properly
			e.printStackTrace();
			return "Unknown date";  // Fallback in case of error
		}
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		TextView title, description, sourceAuthor, date;
		public ViewHolder(View itemView) {
			super(itemView);
			title = itemView.findViewById(R.id.title);
			description = itemView.findViewById(R.id.description);
			sourceAuthor = itemView.findViewById(R.id.sourceAuthor);
			date = itemView.findViewById(R.id.date);
		}
	}
}


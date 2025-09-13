package com.mwendasoft.superme.articles;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.BaseActivity;
import android.widget.*;
import android.graphics.*;
import android.content.*;
import android.view.*;
import com.mwendasoft.superme.authors.*;
import android.text.*;
import android.text.style.*;
import android.support.v4.content.*;
import android.text.method.*;
import java.text.*;
import java.util.*;
import android.text.format.*;
import com.mwendasoft.superme.core.*;

public class ArticleDetailActivity extends BaseActivity {
    private SuperMeTimeHelper timeHelper;
	private ArticlesDBHelper articlesDbHelper;
	private AuthorsDBHelper authorsDbHelper;
    private TextView articleDetailView, articleDetailTitle;
	private ImageButton editArticleFab;

	private Article selectedArticle;
	private String articleTitle, articleWriter;
    private int articleId, writerId;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_detail_activity);

		articleDetailView = findViewById(R.id.articleDetailView);
		articleDetailView.setTextIsSelectable(true);
		
		articleDetailTitle = findViewById(R.id.articleDetailTitle);
		
		editArticleFab = findViewById(R.id.editArticleFab);

		articlesDbHelper = new ArticlesDBHelper(this);
        authorsDbHelper = new AuthorsDBHelper(this);
		
		selectedArticle = (Article) getIntent().getSerializableExtra("selectedArticle");
        articleId = selectedArticle.getId();
		articleTitle = selectedArticle.getTitle();
		articleWriter = selectedArticle.getWriter();
		writerId = authorsDbHelper.getAuthorIdByName(articleWriter);
		
		loadArticle();

		editArticleFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(ArticleDetailActivity.this, ArticleEditActivity.class);
					intent.putExtra("selectedArticle", selectedArticle);
					startActivity(intent);
				}
			});
    }

    private void loadArticle() {
		Cursor cursor = articlesDbHelper.getArticleById(articleId);

		if (cursor != null && cursor.moveToFirst()) {
			int titleIndex = cursor.getColumnIndex("title");
			int bodyIndex = cursor.getColumnIndex("body");
			int dateIndex = cursor.getColumnIndex("date");
			int linkIndex = cursor.getColumnIndex("link");
			
			String title = cursor.getString(titleIndex);
			String bodyText = cursor.getString(bodyIndex);
			String date = cursor.getString(dateIndex);
			String link = cursor.getString(linkIndex);
			
			timeHelper = new SuperMeTimeHelper(date, "0000");
			
			String writerPrefix = "By ";
			
			date = timeHelper.getFormattedDate() + "  |  " + timeHelper.getRelativeTime();
			
			SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
			
			int dateColor = ContextCompat.getColor(this, R.color.customGray);
			int bodyColor = ContextCompat.getColor(this, R.color.customSpace);
			int linkColor = ContextCompat.getColor(this, R.color.customOchre);
			int writerColor = ContextCompat.getColor(this, R.color.customTeal);
			
			SpannableString prefixSpan = new SpannableString(writerPrefix + "  ");
			prefixSpan.setSpan(new ForegroundColorSpan(writerColor), 0, prefixSpan.length(), 0);
			
			// Writer (Green)
			SpannableString writerSpan = new SpannableString(articleWriter + "\n\n");
			writerSpan.setSpan(new ForegroundColorSpan(writerColor), 0, writerSpan.length(), 0);
			writerSpan.setSpan(new StyleSpan(Typeface.ITALIC), 0, writerSpan.length(), 0);
			stringBuilder.append(prefixSpan);
			stringBuilder.append(writerSpan);

			// Date (Dark Gray)
			SpannableString dateSpan = new SpannableString(date + "\n\n");
			dateSpan.setSpan(new ForegroundColorSpan(dateColor), 0, dateSpan.length(), 0);
			stringBuilder.append(dateSpan);

			// Body (Black)
			SpannableString bodySpan = new SpannableString(bodyText + "\n\n");
			bodySpan.setSpan(new ForegroundColorSpan(bodyColor), 0, bodySpan.length(), 0);
			stringBuilder.append(bodySpan);

			// Link (Magenta)
			SpannableString linkSpan = new SpannableString(link);
			linkSpan.setSpan(new URLSpan(link), 0, link.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			linkSpan.setSpan(new ForegroundColorSpan(linkColor), 0, link.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			linkSpan.setSpan(new StyleSpan(Typeface.ITALIC), 0, link.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			
			stringBuilder.append(linkSpan);

			articleDetailTitle.setText(title);
			articleDetailView.setText(stringBuilder);
			articleDetailView.setMovementMethod(LinkMovementMethod.getInstance());

		} else {
			showDialog("No article found for '" + articleTitle + "'");
		}

		if (cursor != null) cursor.close();
	}
	
	public String getRelativeTime1(String dateString) {
		// Given '2023-12-23' this function will return '23 December 2023' or '2 years ago'
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		try {
			Date date = sdf.parse(dateString);
			if (date != null) {
				long now = System.currentTimeMillis();
				return DateUtils.getRelativeTimeSpanString(
                    date.getTime(),
                    now,
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
				).toString();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateString; // fallback to original if parsing fails
	}
	
	public String getRelativeTime2(String dateString) {
		// Given a date, this function will return like '2 Weeks ago' or '1 year ago' etc.
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		try {
			Date pastDate = sdf.parse(dateString);
			Date now = new Date();

			long diffMillis = now.getTime() - pastDate.getTime();

			long seconds = diffMillis / 1000;
			long minutes = seconds / 60;
			long hours = minutes / 60;
			long days = hours / 24;
			long months = days / 30;
			long years = days / 365;

			if (seconds < 60) return "just now";
			else if (minutes < 60) return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
			else if (hours < 24) return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
			else if (days == 1) return "yesterday";
			else if (days < 30) return days + " day" + (days == 1 ? "" : "s") + " ago";
			else if (months < 12) return months + " month" + (months == 1 ? "" : "s") + " ago";
			else return years + " year" + (years == 1 ? "" : "s") + " ago";

		} catch (ParseException e) {
			e.printStackTrace();
			return dateString;
		}
	}

    private void showDialog(String message) {
        new AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    @Override
    protected void onDestroy() {
        articlesDbHelper.close();
        super.onDestroy();
    }
	
	@Override
    protected void onResume() {
        super.onResume();
        loadArticle();
    }
}

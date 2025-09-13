package com.mwendasoft.superme.articles;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;


import com.mwendasoft.superme.R;
import android.text.*;
import com.mwendasoft.superme.categories.*;


public class ArticleViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Article> articles;
	private CategoriesDBHelper categDBHelper;
	
    public ArticleViewAdapter(Context context, ArrayList<Article> articles) {
        this.context = context;
        this.articles = articles;
    }

    @Override
    public int getCount() {
        return articles.size();
    }

    @Override
    public Object getItem(int position) {
        return articles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.article_list_item, parent, false);
        }

		TextView articleListTitle = convertView.findViewById(R.id.articleTitle);
		TextView articleCategName = convertView.findViewById(R.id.articleCategName);
		
		Article article = articles.get(position);
		categDBHelper = new CategoriesDBHelper(context);
		
		String categoryName = categDBHelper.getCategoryNameById(article.getCategId());
		articleListTitle.setText(toTitleCase(article.getTitle()));
		articleCategName.setText(categoryName);
		
        return convertView;
    }
	
	public String toTitleCase(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}

		StringBuilder titleCase = new StringBuilder();
		boolean nextTitleCase = true;

		for (char c : input.toCharArray()) {
			if (Character.isSpaceChar(c)) {
				nextTitleCase = true;
				titleCase.append(c);
			} else {
				if (nextTitleCase) {
					titleCase.append(Character.toUpperCase(c));
					nextTitleCase = false;
				} else {
					titleCase.append(Character.toLowerCase(c));
				}
			}
		}

		return titleCase.toString();
	}
}

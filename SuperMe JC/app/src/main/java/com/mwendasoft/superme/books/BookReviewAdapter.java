package com.mwendasoft.superme.books;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

import com.mwendasoft.superme.R;

public class BookReviewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> fullReviewList;     // This stays unchanged
    private ArrayList<String> filteredReviewList; // This one is used for display

    public BookReviewAdapter(Context context, ArrayList<String> reviewList) {
        this.context = context;
        this.fullReviewList = new ArrayList<>(reviewList); // Copy original list
        this.filteredReviewList = new ArrayList<>(reviewList); // Copy for display
    }

    @Override
    public int getCount() {
        return filteredReviewList.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredReviewList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.book_review_list_item, parent, false);
        }

        TextView reviewText = convertView.findViewById(R.id.bookReviewText);
        reviewText.setText(filteredReviewList.get(position));

        return convertView;
    }

    public void filter(String text) {
        text = text.toLowerCase();
        filteredReviewList.clear();

        if (text.isEmpty()) {
            filteredReviewList.addAll(fullReviewList); // Restore full list
        } else {
            for (String review : fullReviewList) {
                if (review.toLowerCase().contains(text)) {
                    filteredReviewList.add(review);
                }
            }
        }

        notifyDataSetChanged();
    }

    public void updateData(ArrayList<String> newReviews) {
        fullReviewList.clear();
        fullReviewList.addAll(newReviews);

        filteredReviewList.clear();
        filteredReviewList.addAll(newReviews);

        notifyDataSetChanged();
    }
}

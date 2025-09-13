package com.mwendasoft.superme.tasks;

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

public class TasksViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Task> tasksList;
    private SuperMeDateHelper dateHelper;
    private CategoriesDBHelper categDBHelper;

    public TasksViewAdapter(Context context, ArrayList<Task> tasksList) {
        this.context = context;
        this.tasksList = tasksList;
    }

    @Override
    public int getCount() {
        return tasksList.size();
    }

    @Override
    public Object getItem(int position) {
        return tasksList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the view if not reused
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.task_list_item, parent, false);
        }

        Task task = tasksList.get(position);

		dateHelper = new SuperMeDateHelper();
        categDBHelper = new CategoriesDBHelper(context);
		
        // Get views from layout
        TextView taskTitle = convertView.findViewById(R.id.taskTitle);
        TextView taskDuration = convertView.findViewById(R.id.taskDurationView);
        TextView taskDeadline = convertView.findViewById(R.id.taskDeadlineView);
        TextView taskCategory = convertView.findViewById(R.id.taskCategoryView);

        // Set title
		boolean timeIsPast = dateHelper.isTimeInPast(task.getEdate());
		int success = task.getSuccess();
		int titleColor = getTitleColor(success, timeIsPast);
		String taskEmoji = getTaskEmoji(success, timeIsPast);
		
		SpannableStringBuilder titleText = new SpannableStringBuilder(taskEmoji + "  " + task.getTitle());
        titleText.setSpan(new ForegroundColorSpan(titleColor), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleText.setSpan(new StyleSpan(Typeface.BOLD), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		taskTitle.setText(titleText);

        // Format and set duration text
        String duration = dateHelper.convertDays(task.getDuration());
        String label = "Duration: ";
        SpannableStringBuilder durationText = new SpannableStringBuilder();
        durationText.append(label);
        durationText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        durationText.setSpan(new StyleSpan(Typeface.BOLD), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int start = durationText.length();
        durationText.append(duration);
        durationText.setSpan(new ForegroundColorSpan(Color.parseColor("#8D4004")), start, durationText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        taskDuration.setText(durationText);

        // Get and set category name
        String categoryName = categDBHelper.getCategoryNameById(task.getCategoryId());
        SpannableStringBuilder categoryText = new SpannableStringBuilder(categoryName);
        categoryText.setSpan(new ForegroundColorSpan(Color.parseColor("#8D4004")), 0, categoryText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        taskCategory.setText(categoryText);

        // Start deadline countdown
        dateHelper.startCountdownMinute(task.getSdate(), task.getEdate(), task.getSuccess(), taskDeadline);

        return convertView;
    }
	
	private int getTitleColor(int success, boolean timeIsPast) {
		int successColor = ContextCompat.getColor(context, R.color.customForestGreen);
		int activeColor = ContextCompat.getColor(context, R.color.customOchre);
		int failColor = ContextCompat.getColor(context, R.color.colorPrimary);
		int otherColor = ContextCompat.getColor(context, R.color.customMidnightBlack);
		
		if (success == 0) {
			if (timeIsPast == true) {
				return failColor;
			} else {
				return activeColor;
			}
		} else if (success == 1) {
			return successColor;
		} else return otherColor;
	}
	
	private String getTaskEmoji(int success, boolean timeIsPast) {
		if (success == 0) {
			if (timeIsPast == true) {
				return "❌";
			} else {
				return "⏳";
			}
		} else if (success == 1) {
			return "✅";
		} else return "⏲️";
	}
}

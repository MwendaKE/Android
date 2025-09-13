package com.mwendasoft.superme.tasks;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.core.SuperMeAppHelper;
import com.mwendasoft.superme.helpers.DialogMultipleImageViewHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.graphics.*;
import com.mwendasoft.superme.*;
import com.mwendasoft.superme.helpers.*;

public class TaskDetailActivity extends BaseActivity {
    private static final String TAG = "TaskDetailActivity";

    private TasksDBHelper dbHelper;
    private SuperMeAppHelper appHelper;
    private TextView taskDetailTitle, taskDateAndTimeView, taskDueTimeView, taskDetailView;
    private LinearLayout tasksImageViewLayout;
    private LinearLayout taskImageContainer;
    private ImageButton editTaskFab;
    private Task selectedTask;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private List<String> imagePaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_detail_activity);

        dbHelper = new TasksDBHelper(this);
        selectedTask = (Task) getIntent().getSerializableExtra("selectedTask");
        if (selectedTask == null) {
            ToastMessagesManager.show(this, "Task data not available");
            finish();
            return;
        }

        initializeViews();
        setupEditButton();
        loadTaskData();
    }

    private void initializeViews() {
        taskDetailTitle = findViewById(R.id.taskDetailTitle);
        taskDateAndTimeView = findViewById(R.id.taskDateAndTimeView);
        taskDueTimeView = findViewById(R.id.taskDueTimeView);
        taskDetailView = findViewById(R.id.taskDetailView);
        tasksImageViewLayout = findViewById(R.id.tasksImageViewLayout);
        taskImageContainer = findViewById(R.id.taskImageContainer);
        editTaskFab = findViewById(R.id.editTaskFab);
    }

    private void setupEditButton() {
        editTaskFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(TaskDetailActivity.this, TaskEditActivity.class);
					intent.putExtra("selectedTask", selectedTask);
					startActivity(intent);
				}
			});
    }

    private void loadTaskData() {
        loadTask();
        loadMediaThumbnails();
    }

    private void loadTask() {
        Cursor cursor = null;
        try {
            cursor = dbHelper.getTaskById(selectedTask.getId());
            if (cursor != null && cursor.moveToFirst()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String sdate = cursor.getString(cursor.getColumnIndexOrThrow("sdate"));
                String stime = cursor.getString(cursor.getColumnIndexOrThrow("stime"));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow("duration"));
                String detail = cursor.getString(cursor.getColumnIndexOrThrow("description"));

                appHelper = new SuperMeAppHelper(sdate, stime);
                updateTaskUI(title, sdate, stime, duration, detail);
            } else {
                showTaskNotFoundError();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading task", e);
            ToastMessagesManager.show(this, "Error loading task");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void updateTaskUI(String title, String sdate, String stime, int duration, String detail) {
        String formattedDate = appHelper.getFormattedDate() != null ? 
            appHelper.getFormattedDate() : sdate;
        String formattedTime = appHelper.getFormattedTime() != null ? 
            appHelper.getFormattedTime() : stime;
        String relativeTime = appHelper.getRelativeTime() != null ? 
            appHelper.getRelativeTime() : "";

        taskDetailTitle.setText(title);
        taskDateAndTimeView.setText(formattedDate + ", " + formattedTime + " | " + relativeTime);
        taskDueTimeView.setText(duration + " days.");
        taskDetailView.setText(detail);
    }

    private void showTaskNotFoundError() {
        ToastMessagesManager.show(this, "Task not found");
        finish();
    }

    private void loadMediaThumbnails() {
        new Thread(new Runnable() {
				@Override
				public void run() {
					loadImages();
				}
			}).start();
    }

    private void loadImages() {
        imagePaths = dbHelper.getMediaPaths(selectedTask.getId());

        if (imagePaths == null || imagePaths.isEmpty()) {
            hideMediaLayout(tasksImageViewLayout);
            return;
        }

        final ArrayList<Bitmap> thumbnails = new ArrayList<>();
        for (String path : imagePaths) {
            Bitmap thumbnail = loadImageThumbnail(path, 600, 600);
            if (thumbnail != null) {
                thumbnails.add(thumbnail);
            }
        }

        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showMediaLayout(tasksImageViewLayout);
					taskImageContainer.removeAllViews();

					for (int i = 0; i < thumbnails.size(); i++) {
						addImageToLayout(imagePaths.get(i), thumbnails.get(i), i, thumbnails.size());
					}
				}
			});
    }

    private Bitmap loadImageThumbnail(String path, int reqWidth, int reqHeight) {
        File file = new File(path);
        if (!file.exists()) {
            return getDefaultImageThumbnail();
        }

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            if (options.outWidth == -1 || options.outHeight == -1) {
                return getDefaultImageThumbnail();
            }

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inDither = true;
            return BitmapFactory.decodeFile(path, options);
        } catch (Exception e) {
            Log.e(TAG, "Error loading thumbnail: " + path, e);
            return getDefaultImageThumbnail();
        }
    }

    private void addImageToLayout(final String path, Bitmap thumbnail, int position, int total) {
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(thumbnail != null ? thumbnail : getDefaultImageThumbnail());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(400, 400));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(5, 5, 5, 5);
        imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DialogMultipleImageViewHelper dialog = 
						new DialogMultipleImageViewHelper(TaskDetailActivity.this, imagePaths);
					dialog.showImageDialog(path);
				}
			});

        taskImageContainer.addView(imageView);

        if (position < total - 1) {
            taskImageContainer.addView(createSeparator());
        }
    }

    private View createSeparator() {
        View separator = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            dpToPx(1),
            LinearLayout.LayoutParams.MATCH_PARENT
        );
        params.setMargins(dpToPx(8), 0, dpToPx(8), 0);
        separator.setLayoutParams(params);
        separator.setBackgroundColor(Color.parseColor("#800000")); // Maroon color
        return separator;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && 
                   (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private Bitmap getDefaultImageThumbnail() {
        try {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_image_placeholder);
        } catch (Exception e) {
            return createSolidColorBitmap(200, 200, Color.LTGRAY);
        }
    }

    private Bitmap createSolidColorBitmap(int width, int height, int color) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0, 0, width, height, paint);
        return bitmap;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void hideMediaLayout(final LinearLayout layout) {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					layout.setVisibility(View.GONE);
				}
			});
    }

    private void showMediaLayout(final LinearLayout layout) {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					layout.setVisibility(View.VISIBLE);
				}
			});
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTaskData();
    }
}

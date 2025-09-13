package com.mwendasoft.superme.poems;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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
import com.mwendasoft.superme.authors.AuthorsDBHelper;
import com.mwendasoft.superme.helpers.DialogOneImageViewHelper;

import java.io.File;
import android.graphics.*;
import com.mwendasoft.superme.*;
import com.mwendasoft.superme.helpers.*;

public class PoemDetailActivity extends BaseActivity {
    private static final String TAG = "PoemDetailActivity";

    private PoemsDBHelper dbHelper;
    private AuthorsDBHelper authorsDbHelper;
    private TextView poemDetailTitle, poemDetailView;
    private ImageButton editPoemFab;
    private LinearLayout poemImageContainer;

    private Poem selectedPoem;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private int poemId, poetId;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poem_detail_activity);

        initializeViews();
        setupDatabase();
        handleIntentData();
        setupEditButton();
        loadPoemData();
    }

    private void initializeViews() {
        poemDetailTitle = findViewById(R.id.poemDetailTitle);
        poemDetailView = findViewById(R.id.poemDetailView);
        editPoemFab = findViewById(R.id.editPoemFab);
        poemImageContainer = findViewById(R.id.poemImageContainer);
    }

    private void setupDatabase() {
        dbHelper = new PoemsDBHelper(this);
        authorsDbHelper = new AuthorsDBHelper(this);
    }

    private void handleIntentData() {
        selectedPoem = (Poem) getIntent().getSerializableExtra("selectedPoem");
        if (selectedPoem == null) {
			ToastMessagesManager.show(this, "Poem data not available!");
            finish();
            return;
        }
        poemId = selectedPoem.getId();
        poetId = authorsDbHelper.getAuthorIdByName(selectedPoem.getPoemAuthor());
    }

    private void setupEditButton() {
        editPoemFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (selectedPoem != null) {
						Intent intent = new Intent(PoemDetailActivity.this, PoemEditActivity.class);
						intent.putExtra("selectedPoem", selectedPoem);
						startActivity(intent);
					} else {
						showError("Invalid poem format - cannot edit");
					}
				}
			});
    }

    private void loadPoemData() {
        loadPoem();
        loadImage();
    }

    private void loadPoem() {
        Cursor cursor = dbHelper.getPoemById(poemId);
        if (cursor != null && cursor.moveToFirst()) {
            try {
                String detailText = cursor.getString(cursor.getColumnIndexOrThrow("poem"));
                poemDetailTitle.setText(selectedPoem.getPoemTitle() + " by " + selectedPoem.getPoemAuthor());
                poemDetailView.setText(detailText);
            } catch (Exception e) {
                Log.e(TAG, "Error loading poem", e);
                showError("Error loading poem content");
            } finally {
                cursor.close();
            }
        } else {
            showError("Poem not found");
        }
    }

    private void loadImage() {
        new Thread(new Runnable() {
				@Override
				public void run() {
					File folder = new File(Environment.getExternalStorageDirectory(), ".superme/poems/Poem " + poemId);
					String foundPath = null;

					if (folder.exists() && folder.isDirectory()) {
						File[] files = folder.listFiles();
						if (files != null) {
							for (File file : files) {
								String name = file.getName().toLowerCase();
								if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")) {
									foundPath = file.getAbsolutePath();
									break; // Only take first image found
								}
							}
						}
					}

					final String finalPath = foundPath;
					runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (finalPath != null) {
									showImage(finalPath);
								} else {
									hideImage();
								}
							}
						});
				}
			}).start();
    }

    private void showImage(String path) {
        this.imagePath = path;
        poemImageContainer.setVisibility(View.VISIBLE);
        poemImageContainer.removeAllViews();

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(
									  LinearLayout.LayoutParams.WRAP_CONTENT,
									  LinearLayout.LayoutParams.WRAP_CONTENT
								  ));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setAdjustViewBounds(true);
        imageView.setPadding(5, 5, 5, 5);

        Bitmap thumbnail = loadImageThumbnail(path, 400, 400);
        imageView.setImageBitmap(thumbnail != null ? thumbnail : getDefaultImageThumbnail());

        imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DialogOneImageViewHelper dialog = new DialogOneImageViewHelper();
					dialog.showImageDialog(PoemDetailActivity.this, imagePath);
				}
			});

        poemImageContainer.addView(imageView);
    }

    private void hideImage() {
        poemImageContainer.setVisibility(View.GONE);
    }

    private Bitmap loadImageThumbnail(String path, int reqWidth, int reqHeight) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path, options);
        } catch (Exception e) {
            Log.e(TAG, "Error loading image thumbnail", e);
            return getDefaultImageThumbnail();
        }
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

    private void showError(String message) {
		ToastMessagesManager.show(this, message);
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        if (authorsDbHelper != null) {
            authorsDbHelper.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPoemData();
    }
}

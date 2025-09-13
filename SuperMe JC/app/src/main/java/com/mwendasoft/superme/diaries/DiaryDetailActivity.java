package com.mwendasoft.superme.diaries;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.core.SuperMeAppHelper;
import com.mwendasoft.superme.helpers.DialogGeneralVideoViewHelper;
import com.mwendasoft.superme.helpers.DialogMultipleImageViewHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import android.graphics.*;
import android.view.*;
import com.mwendasoft.superme.helpers.*;
import com.mwendasoft.superme.*;

public class DiaryDetailActivity extends BaseActivity {
    private static final String TAG = "DiaryDetailActivity";

    private DiariesDBHelper dbHelper;
    private SuperMeAppHelper appHelper;
    private TextView diaryDetailTitle, diaryDetailTime, diaryDetailTextView;
    private ImageButton editDiaryFab;
    private LinearLayout diaryImageViewLayout, diaryVideoViewLayout, diaryVoicesViewLayout;
    private LinearLayout diaryImageContainer, diaryVideoContainer, diaryVoicesContainer;

    private Diary selectedEntry;
    private int entryId;
    private List<String> imagePaths;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
	
	private SuperMeAudioPlayerHelper audioPlayer;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary_detail_activity);

        dbHelper = new DiariesDBHelper(this);
        selectedEntry = (Diary) getIntent().getSerializableExtra("selectedEntry");
        entryId = selectedEntry.getId();

        initializeViews();
        setupEditButton();
        loadDiaryData();
    }

    private void initializeViews() {
        diaryDetailTitle = findViewById(R.id.diaryDetailTitle);
        diaryDetailTime = findViewById(R.id.diaryDetailTime);
        diaryDetailTextView = findViewById(R.id.diaryDetailTextView);
        editDiaryFab = findViewById(R.id.editDiaryFab);

        diaryImageViewLayout = findViewById(R.id.diaryImageViewLayout);
        diaryVideoViewLayout = findViewById(R.id.diaryVideoViewLayout);
        diaryVoicesViewLayout = findViewById(R.id.diaryVoicesViewLayout);

        diaryImageContainer = findViewById(R.id.diaryImageContainer);
        diaryVideoContainer = findViewById(R.id.diaryVideoContainer);
        diaryVoicesContainer = findViewById(R.id.diaryVoicesContainer);
    }

    private void setupEditButton() {
        editDiaryFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(DiaryDetailActivity.this, DiaryEditActivity.class);
					intent.putExtra("selectedEntry", selectedEntry);
					startActivity(intent);
				}
			});
    }

    private void loadDiaryData() {
        if (entryId == -1) {
			ToastMessagesManager.show(this, "Invalid diary entry!");
            finish();
            return;
        }

        loadDiary();
        loadMediaThumbnails();
    }

    private void loadDiary() {
        Cursor cursor = dbHelper.getDiaryEntryById(entryId);
        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            String description = cursor.getString(cursor.getColumnIndex("description"));
            String mood = cursor.getString(cursor.getColumnIndex("mood"));

            updateDiaryUI(title, date, time, description, mood);
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private void updateDiaryUI(String title, String date, String time, String description, String mood) {
        appHelper = new SuperMeAppHelper(date, time);
        String formattedDateTime = appHelper.getFormattedDate() + ", " + appHelper.getFormattedTime();
        String relativeTime = appHelper.getRelativeTime();

        diaryDetailTitle.setText(title);
        diaryDetailTime.setText(formattedDateTime + " | " + relativeTime);
        diaryDetailTextView.setText(description);
    }

    private void loadMediaThumbnails() {
        new Thread(new Runnable() {
				@Override
				public void run() {
					loadImages();
					loadVideos();
					loadAudios();
				}
			}).start();
    }

    private void loadImages() {
        imagePaths = dbHelper.getMediaPaths(entryId, "image");

        if (imagePaths == null || imagePaths.isEmpty()) {
            hideMediaLayout(diaryImageViewLayout);
            return;
        }

        final ArrayList<Bitmap> thumbnails = new ArrayList<>();
        for (String path : imagePaths) {
            Bitmap thumbnail = loadImageThumbnail(path, 200, 200);
            if (thumbnail != null) {
                thumbnails.add(thumbnail);
            }
        }

        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showMediaLayout(diaryImageViewLayout);
					diaryImageContainer.removeAllViews();

					for (int i = 0; i < thumbnails.size(); i++) {
						addImageToLayout(imagePaths.get(i), thumbnails.get(i), i, thumbnails.size());
					}
				}
			});
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

    private void loadVideos() {
        final List<String> videoPaths = dbHelper.getMediaPaths(entryId, "video");

        if (videoPaths == null || videoPaths.isEmpty()) {
            hideMediaLayout(diaryVideoViewLayout);
            return;
        }

        final ArrayList<Bitmap> thumbnails = new ArrayList<>();
        for (String path : videoPaths) {
            Bitmap thumbnail = loadVideoThumbnail(path);
            if (thumbnail != null) {
                thumbnails.add(thumbnail);
            }
        }

        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showMediaLayout(diaryVideoViewLayout);
					diaryVideoContainer.removeAllViews();

					for (int i = 0; i < thumbnails.size(); i++) {
						addVideoToLayout(videoPaths.get(i), thumbnails.get(i), i, thumbnails.size());
					}
				}
			});
    }

    private Bitmap loadVideoThumbnail(String path) {
        if (path == null || path.isEmpty()) {
            return getDefaultVideoThumbnail();
        }

        File file = new File(path);
        if (!file.exists() || file.length() == 0) {
            return getDefaultVideoThumbnail();
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            Bitmap thumbnail = retriever.getFrameAtTime();
            if (thumbnail != null) {
                return thumbnail;
            }
            return ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
        } catch (Exception e) {
            Log.e(TAG, "Error loading video thumbnail", e);
            return getDefaultVideoThumbnail();
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing retriever", e);
            }
        }
    }
	
	private void addVideoToLayout(final String path, Bitmap thumbnail, int position, int total) {
        FrameLayout videoContainer = new FrameLayout(this);
        videoContainer.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        videoContainer.setPadding(5, 5, 5, 5);

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(
									  FrameLayout.LayoutParams.MATCH_PARENT,
									  FrameLayout.LayoutParams.MATCH_PARENT
								  ));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(thumbnail != null ? thumbnail : getDefaultVideoThumbnail());

        ImageView playButton = new ImageView(this);
        FrameLayout.LayoutParams playButtonParams = new FrameLayout.LayoutParams(
            dpToPx(48),
            dpToPx(48)
        );
        playButtonParams.gravity = Gravity.CENTER;
        playButton.setLayoutParams(playButtonParams);
        playButton.setImageResource(R.drawable.ic_overlay_play_button);

        videoContainer.addView(imageView);
        videoContainer.addView(playButton);

        videoContainer.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DialogGeneralVideoViewHelper videoViewer = new DialogGeneralVideoViewHelper();
					videoViewer.showVideoDialog(DiaryDetailActivity.this, path);
				}
			});

        diaryVideoContainer.addView(videoContainer);

        if (position < total - 1) {
            diaryVideoContainer.addView(createSeparator());
        }
    }

    private void loadAudios() {
        final Set<String> audioPaths = new LinkedHashSet<>(dbHelper.getMediaPaths(entryId, "audio"));

        if (audioPaths == null || audioPaths.isEmpty()) {
            hideMediaLayout(diaryVoicesViewLayout);
            return;
        }

        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showMediaLayout(diaryVoicesViewLayout);
					diaryVoicesContainer.removeAllViews();

					int i = 0;
					for (String path : audioPaths) {
						addAudioToLayout(path, i++, audioPaths.size());
					}
				}
			});
    }

    private void addImageToLayout(final String path, Bitmap thumbnail, int position, int total) {
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(thumbnail != null ? thumbnail : getDefaultImageThumbnail());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(5, 5, 5, 5);
        imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DialogMultipleImageViewHelper dialog = 
						new DialogMultipleImageViewHelper(DiaryDetailActivity.this, imagePaths);
					dialog.showImageDialog(path);
				}
			});

        diaryImageContainer.addView(imageView);

        if (position < total - 1) {
            diaryImageContainer.addView(createSeparator());
        }
    }

    private void addAudioToLayout(final String path, int position, int total) {
		// Initialize audio player if not already done
		if (audioPlayer == null) {
			audioPlayer = new SuperMeAudioPlayerHelper();
		}

		View audioView = LayoutInflater.from(this).inflate(R.layout.audio_player_helper, diaryVoicesContainer, false);

		final ImageButton btnPlayPause = (ImageButton) audioView.findViewById(R.id.btnPlayPause);
		ImageButton btnSeekForward = (ImageButton) audioView.findViewById(R.id.btnSeekForward);
		ImageButton btnSeekBack = (ImageButton) audioView.findViewById(R.id.btnSeekBack);
		ImageButton btnStop = (ImageButton) audioView.findViewById(R.id.btnStop);

		// Set initial button state
		updateButtonState(btnPlayPause, path);

		btnPlayPause.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						if (audioPlayer.isPlaying() && audioPlayer.isCurrentAudio(path)) {
							audioPlayer.pauseAudio();
						} else {
							audioPlayer.playAudio(path);
						}
						updateButtonState(btnPlayPause, path);
					} catch (IOException e) {
						e.printStackTrace();
						ToastMessagesManager.show(DiaryDetailActivity.this, "Error playing audio!");
						// Reset to play state on error
						btnPlayPause.setImageResource(R.drawable.ic_audio_play);
					}
				}
			});

		btnStop.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (audioPlayer != null) {
						audioPlayer.stopAudio();
						updateButtonState(btnPlayPause, path);
					}
				}
			});

		btnSeekForward.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (audioPlayer != null && audioPlayer.isPlaying() && audioPlayer.isCurrentAudio(path)) {
						audioPlayer.seekForward(5000); // 5 seconds forward
					}
				}
			});

		btnSeekBack.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (audioPlayer != null && audioPlayer.isPlaying() && audioPlayer.isCurrentAudio(path)) {
						audioPlayer.seekBackward(5000); // 5 seconds backward
					}
				}
			});

		diaryVoicesContainer.addView(audioView);

		if (position < total - 1) {
			diaryVoicesContainer.addView(createSeparator());
		}
	}

	private void updateButtonState(ImageButton playPauseButton, String audioPath) {
		if (audioPlayer != null && audioPlayer.isPlaying() && audioPlayer.isCurrentAudio(audioPath)) {
			playPauseButton.setImageResource(R.drawable.ic_audio_pause);
		} else {
			playPauseButton.setImageResource(R.drawable.ic_audio_play);
		}
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

    private Bitmap getDefaultVideoThumbnail() {
        try {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_video_placeholder);
        } catch (Exception e) {
            return createSolidColorBitmap(200, 200, Color.DKGRAY);
        }
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

    @Override
	protected void onDestroy() {
		if (dbHelper != null) {
            dbHelper.close();
        }
		
		if (audioPlayer != null) {
			audioPlayer.release();
			audioPlayer = null;
		}
		
		super.onDestroy();
	}
	
    @Override
    protected void onResume() {
        super.onResume();
        loadDiaryData();
    }
}

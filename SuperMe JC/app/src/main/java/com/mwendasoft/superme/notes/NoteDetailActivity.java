package com.mwendasoft.superme.notes;

import android.app.Activity;
import android.app.Dialog;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.helpers.DialogGeneralVideoViewHelper;
import com.mwendasoft.superme.helpers.DialogMultipleImageViewHelper;
import com.mwendasoft.superme.helpers.SuperMeAudioPlayerHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import android.graphics.*;
import com.mwendasoft.superme.*;
import com.mwendasoft.superme.helpers.*;

public class NoteDetailActivity extends BaseActivity {
    private static final String TAG = "NoteDetailActivity";

    private NotesDBHelper dbHelper;
    private TextView noteDetailTitle, noteDetailView;
    private LinearLayout notesImageViewLayout, notesAudioViewLayout, notesVideoViewLayout;
    private LinearLayout noteImageContainer, noteVideoContainer, noteAudioContainer;
    private ImageButton editNoteFab;
    private Note selectedNote;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private List<String> imagePaths;
    private SuperMeAudioPlayerHelper audioPlayer;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_detail_activity);

        dbHelper = new NotesDBHelper(this);
        selectedNote = (Note) getIntent().getSerializableExtra("selectedNote");

        initializeViews();
        setupEditButton();
        loadNoteData();
    }

    private void initializeViews() {
        noteDetailTitle = findViewById(R.id.noteDetailTitle);
        noteDetailView = findViewById(R.id.noteDetailView);
        editNoteFab = findViewById(R.id.editNoteFab);

        notesImageViewLayout = findViewById(R.id.notesImageViewLayout);
        notesAudioViewLayout = findViewById(R.id.notesAudioViewLayout);
        notesVideoViewLayout = findViewById(R.id.notesVideoViewLayout);

        noteImageContainer = findViewById(R.id.noteImageLayout);
        noteVideoContainer = findViewById(R.id.noteVideoLayout);
        noteAudioContainer = findViewById(R.id.noteAudioLayout);
    }

    private void setupEditButton() {
        editNoteFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(NoteDetailActivity.this, NoteEditActivity.class);
					intent.putExtra("selectedNote", selectedNote);
					startActivity(intent);
				}
			});
    }

    private void loadNoteData() {
        if (selectedNote == null || selectedNote.getId() == -1) {
			ToastMessagesManager.show(this, "Invalid note!");
            finish();
            return;
        }

        loadNote();
        loadMediaThumbnails();
    }

    private void loadNote() {
        new Thread(new Runnable() {
				@Override
				public void run() {
					Cursor cursor = dbHelper.getNoteById(selectedNote.getId());
					if (cursor != null && cursor.moveToFirst()) {
						final String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
						final String text = cursor.getString(cursor.getColumnIndexOrThrow("notes"));

						uiHandler.post(new Runnable() {
								@Override
								public void run() {
									noteDetailTitle.setText(title);
									SuperMeMarkupTextEditor.displayWithoutMarkup(noteDetailView, text);
								}
							});
					}
					if (cursor != null) {
						cursor.close();
					}
				}
			}).start();
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
        imagePaths = dbHelper.getMediaPaths(selectedNote.getId(), "image");

        if (imagePaths == null || imagePaths.isEmpty()) {
            hideMediaLayout(notesImageViewLayout);
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
					showMediaLayout(notesImageViewLayout);
					noteImageContainer.removeAllViews();

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
        final List<String> videoPaths = dbHelper.getMediaPaths(selectedNote.getId(), "video");

        if (videoPaths == null || videoPaths.isEmpty()) {
            hideMediaLayout(notesVideoViewLayout);
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
					showMediaLayout(notesVideoViewLayout);
					noteVideoContainer.removeAllViews();

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

    private void loadAudios() {
        final Set<String> audioPaths = new LinkedHashSet<>(dbHelper.getMediaPaths(selectedNote.getId(), "audio"));

        if (audioPaths == null || audioPaths.isEmpty()) {
            hideMediaLayout(notesAudioViewLayout);
            return;
        }

        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showMediaLayout(notesAudioViewLayout);
					noteAudioContainer.removeAllViews();

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
        imageView.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(5, 5, 5, 5);
        imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DialogMultipleImageViewHelper dialog = 
						new DialogMultipleImageViewHelper(NoteDetailActivity.this, imagePaths);
					dialog.showImageDialog(path);
				}
			});

        noteImageContainer.addView(imageView);

        if (position < total - 1) {
            noteImageContainer.addView(createSeparator());
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
					videoViewer.showVideoDialog(NoteDetailActivity.this, path);
				}
			});

        noteVideoContainer.addView(videoContainer);

        if (position < total - 1) {
            noteVideoContainer.addView(createSeparator());
        }
    }

    private void addAudioToLayout(final String path, int position, int total) {
        // Initialize audio player if not already done
        if (audioPlayer == null) {
            audioPlayer = new SuperMeAudioPlayerHelper();
        }

        View audioView = LayoutInflater.from(this).inflate(R.layout.audio_player_helper, noteAudioContainer, false);

        final ImageButton btnPlayPause = audioView.findViewById(R.id.btnPlayPause);
        ImageButton btnSeekForward = audioView.findViewById(R.id.btnSeekForward);
        ImageButton btnSeekBack = audioView.findViewById(R.id.btnSeekBack);
        ImageButton btnStop = audioView.findViewById(R.id.btnStop);

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
						Toast.makeText(NoteDetailActivity.this, "Error playing audio", Toast.LENGTH_SHORT).show();
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

        noteAudioContainer.addView(audioView);

        if (position < total - 1) {
            noteAudioContainer.addView(createSeparator());
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
        loadNoteData();
    }
}

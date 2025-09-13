package com.mwendasoft.superme.helpers;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.mwendasoft.superme.R;

public class DialogGeneralVideoViewHelper {
    private Dialog videoDialog;
    private VideoView videoView;
    private ImageButton btnClose;
    private ImageButton btnPlayPause;
    private ImageButton overlayPlayBtn;
    private SeekBar seekBar;
    private TextView txtCurrentTime;
    private TextView txtTotalTime;
    private LinearLayout controlsContainer;
    private Handler handler;
    private Handler hideHandler;
    private Runnable hideControls;
    private Runnable showControls;
    private Runnable updateSeekBar;
    private Runnable togglePlayPause;

    public void showVideoDialog(final Context context, final String videoPath) {
        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_general_video_view_helper, null);

        // Initialize views
        videoView = dialogView.findViewById(R.id.videoView);
        btnClose = dialogView.findViewById(R.id.btnClose);
        btnPlayPause = dialogView.findViewById(R.id.btnPlayPause);
        overlayPlayBtn = dialogView.findViewById(R.id.overlayPlayBtn);
        seekBar = dialogView.findViewById(R.id.seekBar);
        txtCurrentTime = dialogView.findViewById(R.id.txtCurrentTime);
        txtTotalTime = dialogView.findViewById(R.id.txtTotalTime);
        controlsContainer = dialogView.findViewById(R.id.controlsContainer);

        // Setup video
        videoView.setVideoPath(videoPath);

        // Initialize handlers
        handler = new Handler();
        hideHandler = new Handler();

        // Close button
        btnClose.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});

        // Auto-hide controls handler
        hideControls = new Runnable() {
            @Override
            public void run() {
                controlsContainer.setVisibility(View.GONE);
            }
        };

        // Method to show controls temporarily
        showControls = new Runnable() {
            @Override
            public void run() {
                controlsContainer.setVisibility(View.VISIBLE);
                hideHandler.removeCallbacks(hideControls);
                hideHandler.postDelayed(hideControls, 2000); // Hide after 2 seconds
            }
        };

        // Helper method to toggle play/pause state
        togglePlayPause = new Runnable() {
            @Override
            public void run() {
                if (videoView.isPlaying()) {
                    videoView.pause();
                    btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
                    overlayPlayBtn.setVisibility(View.VISIBLE);
                    overlayPlayBtn.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    videoView.start();
                    btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                    overlayPlayBtn.setVisibility(View.GONE);
                }
                showControls.run();
            }
        };

        // Play/Pause button
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					togglePlayPause.run();
				}
			});

        // Overlay play button
        overlayPlayBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					togglePlayPause.run();
				}
			});

        // Get video duration
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoPath);
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long duration = Long.parseLong(durationStr);
            txtTotalTime.setText(formatTime(duration));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }

        // Update seekbar and time
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (videoView.isPlaying()) {
                    int currentPosition = videoView.getCurrentPosition();
                    seekBar.setProgress((int) (1000 * currentPosition / videoView.getDuration()));
                    txtCurrentTime.setText(formatTime(currentPosition));
                }
                handler.postDelayed(this, 100);
            }
        };

        // Seekbar listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					if (fromUser) {
						int newPosition = (int) (progress * videoView.getDuration() / 1000);
						videoView.seekTo(newPosition);
						txtCurrentTime.setText(formatTime(newPosition));
					}
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					hideHandler.removeCallbacks(hideControls);
					handler.removeCallbacks(updateSeekBar);
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					showControls.run();
					handler.postDelayed(updateSeekBar, 100);
				}
			});

        // Video prepared listener
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
					overlayPlayBtn.setVisibility(View.GONE);
					videoView.start();
					showControls.run(); // Show controls initially
					handler.postDelayed(updateSeekBar, 100);
				}
			});

        // Handle video completion
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
					overlayPlayBtn.setVisibility(View.VISIBLE);
					overlayPlayBtn.setImageResource(android.R.drawable.ic_media_play);
					showControls.run();
				}
			});

        // Double tap to pause/play
        final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
				@Override
				public boolean onDoubleTap(MotionEvent e) {
					togglePlayPause.run();
					return true;
				}

				@Override
				public boolean onSingleTapConfirmed(MotionEvent e) {
					if (controlsContainer.getVisibility() == View.VISIBLE) {
						controlsContainer.setVisibility(View.GONE);
					} else {
						showControls.run();
					}
					return true;
				}
			});

        // Touch listener for video view
        videoView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					gestureDetector.onTouchEvent(event);
					return true;
				}
			});

        // Create and show dialog
        videoDialog = new Dialog(context);
        videoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        videoDialog.setContentView(dialogView);
        videoDialog.setCancelable(true);

        videoDialog.show();

        // Set dialog size (not fullscreen)
        Window window = videoDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    public void dismiss() {
        if (videoDialog != null && videoDialog.isShowing()) {
            // Clean up handlers
            handler.removeCallbacks(updateSeekBar);
            hideHandler.removeCallbacks(hideControls);

            // Release video resources
            if (videoView != null) {
                videoView.stopPlayback();
            }

            videoDialog.dismiss();
        }
    }

    private String formatTime(long milliseconds) {
        int seconds = (int) (milliseconds / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}

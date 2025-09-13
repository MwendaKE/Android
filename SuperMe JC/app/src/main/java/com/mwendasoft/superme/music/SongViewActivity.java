package com.mwendasoft.superme.music;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.authors.AuthorsDBHelper;
import com.mwendasoft.superme.helpers.SuperMeAudioPlayerHelper;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import android.view.*;
import android.graphics.*;
import com.mwendasoft.superme.*;

public class SongViewActivity extends BaseActivity {
    private static final String TAG = "SongViewActivity";

    private SongsDBHelper songsDbHelper;
    private AuthorsDBHelper authorsDbHelper;
    private TextView songViewTitle, songLyricsView;
    private ImageButton editSongFab;
    private LinearLayout musicAudioContainer;

    private Song selectedSong;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private SuperMeAudioPlayerHelper audioPlayer;
    private int songArtistId, songId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_view_activity);

        initializeViews();
        setupDatabase();
        handleIntentData();
        setupEditButton();
        loadSongData();
    }

    private void initializeViews() {
        songViewTitle = findViewById(R.id.songViewTitle);
        songLyricsView = findViewById(R.id.songLyricsView);
        editSongFab = findViewById(R.id.editSongFab);
        musicAudioContainer = findViewById(R.id.musicAudioContainer);
    }

    private void setupDatabase() {
        songsDbHelper = new SongsDBHelper(this);
        authorsDbHelper = new AuthorsDBHelper(this);
    }

    private void handleIntentData() {
        selectedSong = (Song) getIntent().getSerializableExtra("selectedSong");
        if (selectedSong == null) {
            Toast.makeText(this, "Song data not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        songId = selectedSong.getId();
        songArtistId = authorsDbHelper.getAuthorIdByName(selectedSong.getSongArtist());
    }

    private void setupEditButton() {
        editSongFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(SongViewActivity.this, SongEditActivity.class);
					intent.putExtra("selectedSong", selectedSong);
					startActivity(intent);
				}
			});
    }

    private void loadSongData() {
        loadSong();
        loadAudioFiles();
    }

    private void loadSong() {
        Cursor cursor = songsDbHelper.getSongLyriesById(songId);

        if (cursor != null && cursor.moveToFirst()) {
            int lyricsIndex = cursor.getColumnIndex("lyrics");
            if (lyricsIndex != -1) {
                String lyricsText = cursor.getString(lyricsIndex);
                songViewTitle.setText(selectedSong.getSongTitle() + " by " + selectedSong.getSongArtist());
                songLyricsView.setText(lyricsText);
            } else {
                showErrorDialog("Lyrics column not found");
            }
            cursor.close();
        } else {
            showErrorDialog("No lyrics found for '" + selectedSong.getSongTitle() + "'");
        }
    }

    private void loadAudioFiles() {
        new Thread(new Runnable() {
				@Override
				public void run() {
					File folder = new File(Environment.getExternalStorageDirectory(), ".superme/songs/Song " + songId);
					final Set<String> audioPaths = new LinkedHashSet<>();

					if (folder.exists() && folder.isDirectory()) {
						File[] files = folder.listFiles();
						if (files != null) {
							for (File file : files) {
								String name = file.getName().toLowerCase();
								if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".m4a")) {
									audioPaths.add(file.getAbsolutePath());
								}
							}
						}
					}

					runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (audioPaths.isEmpty()) {
									musicAudioContainer.setVisibility(View.GONE);
									return;
								}

								musicAudioContainer.setVisibility(View.VISIBLE);
								musicAudioContainer.removeAllViews();

								int i = 0;
								for (String path : audioPaths) {
									addAudioToLayout(path, i++, audioPaths.size());
								}
							}
						});
				}
			}).start();
    }

    private void addAudioToLayout(final String path, int position, int total) {
        // Initialize audio player if not already done
        if (audioPlayer == null) {
            audioPlayer = new SuperMeAudioPlayerHelper();
        }

        View audioView = LayoutInflater.from(this).inflate(R.layout.audio_player_helper, musicAudioContainer, false);

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
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(SongViewActivity.this, "Error playing audio", Toast.LENGTH_SHORT).show();
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

        musicAudioContainer.addView(audioView);

        if (position < total - 1) {
            musicAudioContainer.addView(createSeparator());
        }
    }

    private void updateButtonState(ImageButton playPauseButton, String audioPath) {
        if (audioPlayer != null && audioPlayer.isPlaying() && audioPlayer.isCurrentAudio(audioPath)) {
            playPauseButton.setImageResource(R.drawable.ic_audio_pause);
        } else {
            playPauseButton.setImageResource(R.drawable.ic_audio_play);
        }
    }

    private View createSeparator() {
        View separator = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dpToPx(1)
        );
        params.setMargins(dpToPx(8), 0, dpToPx(8), 0);
        separator.setLayoutParams(params);
        separator.setBackgroundColor(Color.parseColor("#800000")); // Maroon color
        return separator;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void showErrorDialog(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (songsDbHelper != null) {
            songsDbHelper.close();
        }
        if (authorsDbHelper != null) {
            authorsDbHelper.close();
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
        loadSongData();
    }
}

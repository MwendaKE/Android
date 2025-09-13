package com.mwendasoft.superme.helpers;

import android.media.MediaPlayer;
import java.io.IOException;

public class SuperMeAudioPlayerHelper {
    private MediaPlayer mediaPlayer;
    private String currentAudioPath;

    public SuperMeAudioPlayerHelper() {
        mediaPlayer = new MediaPlayer();
    }

    public void playAudio(String audioPath) throws IOException {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        if (currentAudioPath != null && currentAudioPath.equals(audioPath)) {
            // Toggle play/pause for current audio
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
        } else {
            // New audio - reset and prepare
            currentAudioPath = audioPath;
            mediaPlayer.reset();
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }
    }

    public void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void stopAudio() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            currentAudioPath = null;
        }
    }

    public void seekForward(int milliseconds) {
        if (mediaPlayer != null) {
            int newPosition = mediaPlayer.getCurrentPosition() + milliseconds;
            mediaPlayer.seekTo(Math.min(newPosition, mediaPlayer.getDuration()));
        }
    }

    public void seekBackward(int milliseconds) {
        if (mediaPlayer != null) {
            int newPosition = mediaPlayer.getCurrentPosition() - milliseconds;
            mediaPlayer.seekTo(Math.max(newPosition, 0));
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public boolean isCurrentAudio(String path) {
        return currentAudioPath != null && currentAudioPath.equals(path);
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public void release() {
        if (mediaPlayer != null) {
            stopAudio(); // Stop and reset before releasing
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

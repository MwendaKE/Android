package com.mwendasoft.bittowl;

import android.app.Service;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import android.graphics.*;

public class MediaCaptureHelper {
    private static final String TAG = "MediaCaptureHelper";
    private static final int AUDIO_DURATION = 10; // seconds

    // -----------------------------
    // Capture front camera photo with robust error handling
    // -----------------------------
    public static File captureFrontCameraPhoto(Service service, String folderName) {
		Camera camera = null;
		SurfaceTexture surfaceTexture = null;

		// Use a final array to hold the file reference
		final File[] photoFileHolder = new File[1];
		final CountDownLatch latch = new CountDownLatch(1);
		final boolean[] captureCompleted = new boolean[1];

		try {
			Log.d(TAG, "Attempting to open front camera...");

			// Try to open camera
			try {
				camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
			} catch (Exception e) {
				Log.w(TAG, "Front camera failed: " + e.getMessage());
				try {
					camera = Camera.open(0); // Try default camera
				} catch (Exception e2) {
					Log.e(TAG, "All camera access failed: " + e2.getMessage());
					return null;
				}
			}

			if (camera == null) {
				Log.e(TAG, "Camera is null after opening");
				return null;
			}

			// Get parameters and set basic settings
			Camera.Parameters params = camera.getParameters();

			// Set lowest quality for small file size
			params.setJpegQuality(30);
			params.setPictureFormat(PixelFormat.JPEG);

			// Set smallest available resolution
			Camera.Size smallestSize = getSmallestPictureSize(params);
			if (smallestSize != null) {
				params.setPictureSize(smallestSize.width, smallestSize.height);
			}

			camera.setParameters(params);

			// Create photo file first and store in final holder
			photoFileHolder[0] = createMediaFile(service, "User-Image", ".jpg", folderName);
			if (photoFileHolder[0] == null) {
				camera.release();
				return null;
			}

			// CRITICAL FIX: Setup preview surface before taking picture
			try {
				surfaceTexture = new SurfaceTexture(0);
				camera.setPreviewTexture(surfaceTexture);
				camera.startPreview();

				// Wait a bit for preview to start
				Thread.sleep(300);
			} catch (Exception e) {
				Log.w(TAG, "Preview setup warning: " + e.getMessage());
				// Continue anyway - some cameras might work without preview
			}

			// Use a simpler approach without preview
			camera.takePicture(null, null, new Camera.PictureCallback() {
					@Override
					public void onPictureTaken(byte[] data, Camera cam) {
						try {
							if (data != null && data.length > 0) {
								FileOutputStream fos = new FileOutputStream(photoFileHolder[0]);
								fos.write(data);
								fos.close();
								Log.d(TAG, "Photo saved successfully: " + photoFileHolder[0].length() + " bytes");
								captureCompleted[0] = true;
							} else {
								Log.e(TAG, "Camera returned empty data");
								captureCompleted[0] = false;
							}
						} catch (Exception e) {
							Log.e(TAG, "Error saving photo: " + e.getMessage());
							captureCompleted[0] = false;
						} finally {
							try {
								cam.release();
							} catch (Exception e) {
								Log.w(TAG, "Camera release error: " + e.getMessage());
							}
							latch.countDown();
						}
					}
				});

			// Wait for capture to complete with timeout
			if (latch.await(15, TimeUnit.SECONDS)) {
				if (captureCompleted[0] && photoFileHolder[0].exists() && photoFileHolder[0].length() > 0) {
					return photoFileHolder[0];
				} else {
					Log.e(TAG, "Photo capture failed or file is empty");
					if (photoFileHolder[0] != null && photoFileHolder[0].exists()) {
						photoFileHolder[0].delete();
					}
					return null;
				}
			} else {
				Log.e(TAG, "Camera timeout - taking too long to capture");
				return null;
			}

		} catch (Exception e) {
			Log.e(TAG, "Camera capture error: " + e.getMessage());
			if (camera != null) {
				try {
					camera.release();
				} catch (Exception ignore) {}
			}
			// Delete empty file if created
			if (photoFileHolder[0] != null && photoFileHolder[0].exists() && photoFileHolder[0].length() == 0) {
				photoFileHolder[0].delete();
			}
			return null;
		} finally {
			// Clean up surface texture
			if (surfaceTexture != null) {
				surfaceTexture.release();
			}
		}
	}

    // Helper method to get smallest picture size
	private static Camera.Size getSmallestPictureSize(Camera.Parameters params) {
		Camera.Size smallest = null;
		for (Camera.Size size : params.getSupportedPictureSizes()) {
			if (smallest == null || (size.width * size.height) < (smallest.width * smallest.height)) {
				smallest = size;
			}
		}
		return smallest;
	}

    // -----------------------------
    // Record audio (low quality)
    // -----------------------------
    public static File recordAudio(Service service, String folderName) {
		MediaRecorder recorder = null;
		try {
			File audioFile = createMediaFile(service, "User-Voice", ".wav", folderName);

			recorder = new MediaRecorder();
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);

			// For WAV, you might need to use PCM encoding
			// Note: This may not work on all devices
			try {
				recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); // Try AAC first
			} catch (Exception e) {
				recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT); // Fallback
			}

			recorder.setOutputFile(audioFile.getAbsolutePath());

			recorder.prepare();
			recorder.start();

			Thread.sleep(AUDIO_DURATION * 1000);
			recorder.stop();
			return audioFile;
		} catch (Exception e) {
			Log.e(TAG, "Audio recording error: " + e.getMessage());

			// Fallback to 3GP if WAV fails
			try {
				return recordAudio3GP(service, folderName);
			} catch (Exception e2) {
				return null;
			}
		} finally {
			if (recorder != null) {
				try {
					recorder.reset();
					recorder.release();
				} catch (Exception ignore) {}
			}
		}
	}

    // Fallback method for 3GP format
	private static File recordAudio3GP(Service service, String folderName) {
		MediaRecorder recorder = null;
		try {
			File audioFile = createMediaFile(service, "User-Voice", ".3gp", folderName);

			recorder = new MediaRecorder();
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			recorder.setOutputFile(audioFile.getAbsolutePath());

			recorder.prepare();
			recorder.start();

			Thread.sleep(AUDIO_DURATION * 1000);
			recorder.stop();
			return audioFile;
		} catch (Exception e) {
			Log.e(TAG, "3GP audio recording error: " + e.getMessage());
			return null;
		} finally {
			if (recorder != null) {
				try {
					recorder.reset();
					recorder.release();
				} catch (Exception ignore) {}
			}
		}
	}

    // -----------------------------
    // Create media file
    // -----------------------------
    private static File createMediaFile(Service service, String prefix, String extension, String folderName) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = prefix + "_" + timeStamp + extension;

        File storageDir = service.getExternalFilesDir(folderName);
        if (storageDir == null) {
            storageDir = new File(service.getFilesDir(), folderName);
        }
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        return new File(storageDir, fileName);
    }
}

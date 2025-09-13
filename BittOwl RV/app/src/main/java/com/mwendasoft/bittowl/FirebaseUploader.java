package com.mwendasoft.bittowl;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class FirebaseUploader {

    private static final String TAG = "BittOwlUploader";
    private static final String STORAGE_PATH = "bittowl_exports/";
    // Verify this matches EXACTLY from Firebase Console → Storage
    private static final String BUCKET_URL = "gs://bittowl-reports.appspot.com"; 
	
    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(Exception e);
    }

    public static void uploadFile(@NonNull final File file, @NonNull final UploadCallback callback) {
        // 1. Verify authentication
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onFailure(new SecurityException("User not signed in"));
            return;
        }

        // 2. Validate file
        if (!file.exists() || file.length() == 0) {
            callback.onFailure(new IllegalArgumentException("File is empty or missing"));
            return;
        }

        try {
            // 3. Initialize storage with explicit bucket
            FirebaseStorage storage = FirebaseStorage.getInstance(BUCKET_URL);
            String filename = STORAGE_PATH + Uri.encode(file.getName());
            final StorageReference storageRef = storage.getReference().child(filename);

            // 4. Set metadata
            StorageMetadata metadata = new StorageMetadata.Builder()
				.setContentType("application/zip")
				.setCustomMetadata("uploader_id", user.getUid())
				.setCustomMetadata("device", android.os.Build.MODEL)
				.build();

            // 5. Start upload
            storageRef.putFile(Uri.fromFile(file), metadata)
				.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
					@Override
					public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
						fetchDownloadUrl(storageRef, file, callback);
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						handleError(e, callback);
					}
				});

        } catch (Exception e) {
            Log.e(TAG, "Firebase init error", e);
            callback.onFailure(new Exception("Initialization failed", e));
        }
    }

    private static void fetchDownloadUrl(StorageReference ref, final File file, final UploadCallback callback) {
        ref.getDownloadUrl()
			.addOnSuccessListener(new OnSuccessListener<Uri>() {
				@Override
				public void onSuccess(Uri uri) {
					Log.d(TAG, "Upload success: " + uri);
					callback.onSuccess(uri.toString());
					cleanupFile(file);
				}
			})
			.addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {
					Log.e(TAG, "URL fetch failed", e);
					callback.onFailure(new Exception("Upload complete but URL unavailable", e));
				}
			});
    }

    private static void handleError(Exception e, UploadCallback callback) {
        String errorMsg = "Upload failed";

        if (e.getMessage() != null) {
            if (e.getMessage().contains("Permission denied")) {
                errorMsg = "Authentication error - please sign in again";
            } else if (e.getMessage().contains("network")) {
                errorMsg = "Network unavailable - check connection";
            } else if (e.getMessage().contains("404")) {
                errorMsg = "Storage path not found - check bucket URL";
            }
        }

        Log.e(TAG, errorMsg, e);
        callback.onFailure(new Exception(errorMsg, e));
    }

    private static void cleanupFile(File file) {
        if (file != null && file.exists()) {
            boolean deleted = file.delete();
            Log.d(TAG, "Local cleanup " + (deleted ? "successful" : "failed"));
        }
    }

    public static boolean isUserAuthenticated() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }
}

	

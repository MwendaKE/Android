package com.mwendasoft.bittowl;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BackblazeUploader {
    private static final String TAG = "BittOwlUploader";
    private static final String STORAGE_PATH = "bittowl_exports/";

    // Backblaze B2 Configuration - Multiple endpoints for fallback
    private static final String B2_ACCOUNT_ID = "00583afac6e61cc0000000001";
    private static final String B2_APPLICATION_KEY = "K005vBi1JZds0UsyjzgfChWh7QHMy5A";
    private static final String B2_BUCKET_ID = "d8c3ca4f6adca65e96810c1c";
    private static final String B2_BUCKET_NAME = "bittowl-reports";

    // List of API endpoints to try (fallback order)
    private static final String[] B2_API_ENDPOINTS = {
        "https://api.backblazeb2.com/b2api/v2/",
        "https://api.us-west-001.backblazeb2.com/b2api/v2/", 
        "https://api.eu-central-001.backblazeb2.com/b2api/v2/"
    };

    // Cache for authorization data
    private static String authToken = null;
    private static String apiUrl = null;
    private static String downloadUrl = null;
    private static int currentEndpointIndex = 0;

    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(Exception e);
        void onProgress(int progress);
    }

    public static void uploadFile(@NonNull final File file, @NonNull final UploadCallback callback) {
        new Thread(new Runnable() {
				@Override
				public void run() {
					int maxRetries = 3;
					int retryCount = 0;
					Exception lastError = null;

					while (retryCount < maxRetries) {
						try {
							if (attemptUpload(file, callback)) {
								return; // Success
							}
						} catch (Exception e) {
							lastError = e;
							Log.w(TAG, "Upload attempt " + (retryCount + 1) + " failed: " + e.getMessage());
						}

						retryCount++;
						if (retryCount < maxRetries) {
							try {
								// Exponential backoff: 2s, 4s, 8s
								Thread.sleep(2000 * (1 << (retryCount - 1)));
							} catch (InterruptedException ie) {
								Thread.currentThread().interrupt();
								break;
							}
							// Rotate to next endpoint
							currentEndpointIndex = (currentEndpointIndex + 1) % B2_API_ENDPOINTS.length;
						}
					}

					callback.onFailure(lastError != null ? lastError : new Exception("All upload attempts failed"));
				}
			}).start();
    }

    private static boolean attemptUpload(File file, UploadCallback callback) throws Exception {
        HttpURLConnection conn = null;
        FileInputStream fis = null;
        OutputStream os = null;

        try {
            // 1. Validate file
            if (!file.exists() || file.length() == 0) {
                throw new IllegalArgumentException("File is empty or missing");
            }

            // 2. Sanitize filename
            String sanitizedName = sanitizeFilename(file.getName());
            String fileName = STORAGE_PATH + sanitizedName;

            Log.d(TAG, "Original filename: " + file.getName());
            Log.d(TAG, "Sanitized filename: " + fileName);
            Log.d(TAG, "Using endpoint: " + B2_API_ENDPOINTS[currentEndpointIndex]);

            // 3. Authorize with Backblaze
            if (!authorizeAccount()) {
                throw new Exception("Failed to authenticate with Backblaze");
            }

            // 4. Get upload URL
            Map<String, String> uploadData = getUploadUrl();
            if (uploadData == null) {
                throw new Exception("Failed to get upload URL");
            }

            String uploadUrl = uploadData.get("uploadUrl");
            String uploadAuthToken = uploadData.get("authorizationToken");

            // 5. Prepare upload with timeouts
            URL url = new URL(uploadUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000); // 15 seconds connection timeout
            conn.setReadTimeout(30000);    // 30 seconds read timeout
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", uploadAuthToken);
            conn.setRequestProperty("X-Bz-File-Name", fileName);
            conn.setRequestProperty("X-Bz-Content-Sha1", "do_not_verify");
            conn.setRequestProperty("Content-Type", "b2/x-auto");
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode((int)file.length());

            // 6. Upload with progress tracking
            os = conn.getOutputStream();
            fis = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int bytesRead;
            long bytesUploaded = 0;
            long fileSize = file.length();

            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
                bytesUploaded += bytesRead;
                callback.onProgress((int)((bytesUploaded * 100) / fileSize));
            }

            // 7. Check response
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                String downloadUrl = generateDownloadUrl(fileName, TimeUnit.DAYS.toSeconds(7));
                callback.onSuccess(downloadUrl);
                return true;
            } else {
                String errorMsg = "HTTP " + responseCode + ": " + readErrorResponse(conn);
                throw new Exception(errorMsg);
            }
        } finally {
            try {
                if (fis != null) fis.close();
                if (os != null) os.close();
                if (conn != null) conn.disconnect();
            } catch (Exception e) {
                Log.w(TAG, "Error closing resources", e);
            }
        }
    }

    private static String sanitizeFilename(String filename) {
        return filename.replaceAll("\\s+", "_")
            .replaceAll("[^a-zA-Z0-9-_.]", "");
    }

    private static String readErrorResponse(HttpURLConnection conn) {
        try {
            InputStream es = conn.getErrorStream();
            if (es == null) return "No error details";

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = es.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString("UTF-8");
        } catch (Exception e) {
            return "Failed to read error response: " + e.getMessage();
        }
    }

    private static boolean authorizeAccount() {
		if (authToken != null) {
			return true;
		}

		HttpURLConnection conn = null;
		try {
			String authUrl = B2_API_ENDPOINTS[currentEndpointIndex] + "b2_authorize_account";
			String credentials = B2_ACCOUNT_ID + ":" + B2_APPLICATION_KEY;
			String authHeader = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

			URL url = new URL(authUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(15000);
			conn.setReadTimeout(15000);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", authHeader);

			int responseCode = conn.getResponseCode();
			Log.d(TAG, "Auth response code: " + responseCode);

			if (responseCode == 200) {
				String response = readResponse(conn);
				authToken = extractJsonValue(response, "authorizationToken");
				apiUrl = extractJsonValue(response, "apiUrl") + "/b2api/v2/";
				downloadUrl = extractJsonValue(response, "downloadUrl");
				return true;
			} else {
				// ADD THIS DEBUG LOGGING:
				String errorResponse = readErrorResponse(conn);
				Log.e(TAG, "Auth failed. HTTP " + responseCode + ": " + errorResponse);
				Log.e(TAG, "Auth URL: " + authUrl);
				Log.e(TAG, "Using Key ID: " + B2_ACCOUNT_ID);
				// Don't log full key, but first few chars for debugging
				Log.e(TAG, "Using Key prefix: " + (B2_APPLICATION_KEY != null ? B2_APPLICATION_KEY.substring(0, Math.min(10, B2_APPLICATION_KEY.length())) : "null"));
			}
		} catch (Exception e) {
			Log.e(TAG, "Authorization exception: " + e.getMessage(), e);
		} finally {
			if (conn != null) conn.disconnect();
		}
		return false;
	}

    private static Map<String, String> getUploadUrl() {
        HttpURLConnection conn = null;
        try {
            String endpoint = apiUrl + "b2_get_upload_url";
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", authToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String requestBody = "{\"bucketId\":\"" + B2_BUCKET_ID + "\"}";
            OutputStream os = conn.getOutputStream();
            os.write(requestBody.getBytes());
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                String response = readResponse(conn);
                Map<String, String> result = new HashMap<String, String>();
                result.put("uploadUrl", extractJsonValue(response, "uploadUrl"));
                result.put("authorizationToken", extractJsonValue(response, "authorizationToken"));
                return result;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get upload URL", e);
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    private static String generateDownloadUrl(String fileName, long durationSeconds) {
        try {
            return downloadUrl + "/file/" + B2_BUCKET_NAME + "/" + fileName + 
                "?Authorization=" + authToken;
        } catch (Exception e) {
            Log.e(TAG, "Failed to generate download URL", e);
            return null;
        }
    }

    private static String readResponse(HttpURLConnection conn) throws Exception {
        InputStream is = conn.getInputStream();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

    private static String extractJsonValue(String json, String key) {
        try {
            int keyIndex = json.indexOf("\"" + key + "\":");
            if (keyIndex == -1) return null;

            int valueStart = json.indexOf("\"", keyIndex + key.length() + 3) + 1;
            int valueEnd = json.indexOf("\"", valueStart);
            return json.substring(valueStart, valueEnd);
        } catch (Exception e) {
            return null;
        }
    }

    private static void cleanupFile(File file) {
        if (file != null && file.exists()) {
            boolean deleted = file.delete();
            Log.d(TAG, "Local cleanup " + (deleted ? "successful" : "failed"));
        }
    }

    public static boolean isUserAuthenticated() {
        return true;
    }
}

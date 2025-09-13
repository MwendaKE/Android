package com.mwendasoft.bittowl;

import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PipedreamUploader {

    public interface UploadCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    // Method to upload ZIP file to Pipedream with callback
    public static void uploadZipToPipedream(final File zipFile, final UploadCallback callback) {
		if (zipFile == null || !zipFile.exists()) {
			callback.onFailure(new Exception("Zip file is missing"));
			return;
		}

		// Check file size limit (50MB)
		if (zipFile.length() > 50 * 1024 * 1024) {
			callback.onFailure(new Exception("File too large for Pipedream (max 50MB)"));
			return;
		}

		new Thread(new Runnable() {
				public void run() {
					HttpURLConnection conn = null;
					DataOutputStream dos = null;
					FileInputStream fis = null;

					try {
						String boundary = "*****" + System.currentTimeMillis() + "*****";
						URL url = new URL("https://eoa1ny0elt3ir6w.m.pipedream.net/");

						conn = (HttpURLConnection) url.openConnection();
						conn.setConnectTimeout(10000); // 10 seconds timeout
						conn.setReadTimeout(20000);    // 20 seconds timeout
						conn.setDoOutput(true);
						conn.setUseCaches(false);
						conn.setRequestMethod("POST");
						conn.setRequestProperty("Connection", "Keep-Alive");
						conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

						dos = new DataOutputStream(conn.getOutputStream());

						// Write multipart form data
						dos.writeBytes("--" + boundary + "\r\n");
						dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + zipFile.getName() + "\"\r\n");
						dos.writeBytes("Content-Type: application/zip\r\n\r\n");

						// Write file data
						fis = new FileInputStream(zipFile);
						byte[] buffer = new byte[8192];
						int bytesRead;
						while ((bytesRead = fis.read(buffer)) != -1) {
							dos.write(buffer, 0, bytesRead);
						}

						dos.writeBytes("\r\n--" + boundary + "--\r\n");
						dos.flush();

						// Get response
						int responseCode = conn.getResponseCode();

						if (responseCode >= 200 && responseCode < 300) {
							callback.onSuccess();
						} else {
							String errorMsg = "HTTP " + responseCode;
							try {
								BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
								StringBuilder errorResponse = new StringBuilder();
								String line;
								while ((line = reader.readLine()) != null) {
									errorResponse.append(line);
								}
								errorMsg = errorResponse.toString();
							} catch (Exception ignored) {}

							callback.onFailure(new Exception(errorMsg));
						}

					} catch (Exception e) {
						callback.onFailure(new Exception("Network error: " + e.getMessage()));
					} finally {
						try {
							if (fis != null) fis.close();
							if (dos != null) dos.close();
							if (conn != null) conn.disconnect();
						} catch (Exception ignored) {}
					}
				}
			}).start();
	}
}

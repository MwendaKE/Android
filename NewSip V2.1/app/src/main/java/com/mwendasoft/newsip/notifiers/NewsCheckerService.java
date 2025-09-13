package com.mwendasoft.newsip.notifiers;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import com.mwendasoft.newsip.localnews.LocalNewsItem;
import com.mwendasoft.newsip.MainActivity;
import com.mwendasoft.newsip.R;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import android.app.*;
import android.support.v4.content.*;
import com.mwendasoft.newsip.*;

public class NewsCheckerService extends IntentService {
    private static final String TAG = "NewsCheckerService";
    private static final String ACTION_CHECK_NEWS = "com.mwendasoft.newsip.action.CHECK_NEWS";
    private static final String CHANNEL_ID = "news_channel";
    private static final int CHECK_INTERVAL = 60 * 60 * 1000; // 1 hour in milliseconds
	private static final int FOREGROUND_ID = 101;

    public NewsCheckerService() {
        super("NewsCheckerService");
    }

    public static void startService(Context context) {
        Log.d(TAG, "Starting immediate check");
        Intent intent = new Intent(context, NewsCheckerService.class);
        intent.setAction(ACTION_CHECK_NEWS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    /**
     * Schedules periodic checks without using exact alarms
     */
    public static void schedulePeriodicChecks(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NewsAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Log.d(TAG, "Scheduling inexact hourly checks");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Use battery-optimized inexact alarm for Android 6.0+
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + CHECK_INTERVAL,
                pendingIntent
            );
        } else {
            // Standard repeating alarm for older versions
            alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + CHECK_INTERVAL,
                CHECK_INTERVAL,
                pendingIntent
            );
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null || !ACTION_CHECK_NEWS.equals(intent.getAction())) {
            return;
        }

        Log.d(TAG, "Handling news check intent");

        // Minimal foreground service setup for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_22)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Checking for news updates")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true);

            startForeground(FOREGROUND_ID, builder.build());
        }

        checkForNewArticles();

        // Clean up foreground service if used
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "News Updates",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for new articles");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 100, 300});

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            Log.d(TAG, "Notification channel created");
        }
    }

    private void checkForNewArticles() {
        Log.d(TAG, "Starting news check");
        try {
            ArrayList<LocalNewsItem> currentArticles = new FetchRSSBackground().fetchLatest();
            if (currentArticles == null || currentArticles.isEmpty()) {
                Log.d(TAG, "No articles fetched");
                return;
            }

            SharedPreferences prefs = getSharedPreferences("NewsPrefs", MODE_PRIVATE);
            Set<String> lastTitles = prefs.getStringSet("last_titles", new HashSet<String>());
            Set<String> currentTitles = new HashSet<String>();

            int newArticles = 0;
            for (LocalNewsItem item : currentArticles) {
                currentTitles.add(item.getTitle());
                if (!lastTitles.contains(item.getTitle())) {
                    showArticleNotification(item);
                    newArticles++;
                }
            }

            prefs.edit().putStringSet("last_titles", currentTitles).apply();
            Log.d(TAG, "Check complete. New articles: " + newArticles);

        } catch (Exception e) {
            Log.e(TAG, "Error in checkForNewArticles", e);
        }
    }

    private void showArticleNotification(LocalNewsItem article) {
        Intent intent = new Intent(this, NewsDetailActivity.class);
        intent.putExtra("url", article.getLink());
        intent.putExtra("title", article.getTitle());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            (int) System.currentTimeMillis(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_22)
            .setColor(ContextCompat.getColor(this, R.color.red_1))
            .setContentTitle("New Article: " + article.getTitle())
            .setContentText(article.getDescription())
            .setStyle(new NotificationCompat.BigTextStyle()
					  .bigText(article.getDescription() + "\n\nSource: " + article.getSource()))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);

        NotificationManagerCompat.from(this)
            .notify((int) System.currentTimeMillis(), builder.build());
    }

    public static void clearNotifications(Context context) {
        NotificationManagerCompat.from(context).cancelAll();
        Log.d(TAG, "All notifications cleared");
    }
}

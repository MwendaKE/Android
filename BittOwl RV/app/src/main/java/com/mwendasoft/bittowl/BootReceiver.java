package com.mwendasoft.bittowl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.AlarmManager;
import android.app.PendingIntent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Set alarm to trigger AlarmReceiver every 6 hours
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            // 6 hours in milliseconds = 6 * 60 * 60 * 1000 = 21600000
            long interval = 21600000;
            long startTime = System.currentTimeMillis() + 60000; // Start after 1 minute

            if (alarmManager != null) {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    startTime,
                    interval,
                    pendingIntent
                );
            }
        }
    }
}

/*

· First run: 1 minute after app starts or device boots
· Subsequent runs: Every 6 hours exactly
· Persists: After device reboot (BootReceiver)
· Reliable: Uses AlarmManager.RTC_WAKEUP to wake device if sleeping

*/

package com.mwendasoft.superme.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mwendasoft.superme.core.SuperMeNotificationsHelper;

public class EventNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        if (title != null && message != null) {
            SuperMeNotificationsHelper.showNotification(context, title, message);
        }
    }
}

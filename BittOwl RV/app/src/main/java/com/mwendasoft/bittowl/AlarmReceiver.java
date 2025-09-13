package com.mwendasoft.bittowl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Start the ReportService
        Intent serviceIntent = new Intent(context, ReportService.class);
        context.startService(serviceIntent);
    }
}

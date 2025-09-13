package com.mwendasoft.newsip.notifiers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.*;
import java.util.*;

public class NewsAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Add this log to verify receiver is triggered
        Log.d("NewsCheck", "Alarm received at " + new Date());
        NewsCheckerService.startService(context);
    }
}

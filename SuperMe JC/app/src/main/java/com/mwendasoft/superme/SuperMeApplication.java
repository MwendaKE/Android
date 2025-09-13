package com.mwendasoft.superme;

import android.app.Application;
import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.util.Log;

public class SuperMeApplication extends Application {

    private static SuperMeBackupHelper dbBackupManager;
    private int activityCount = 0;
    private static final String TAG = "SuperMeApp";

    @Override
    public void onCreate() {
        super.onCreate();

        dbBackupManager = new SuperMeBackupHelper(this);

        // Check if app is freshly installed
        SharedPreferences prefs = getSharedPreferences("superme_prefs", MODE_PRIVATE);
        boolean installedBefore = prefs.getBoolean("app_installed", false);

        if (!installedBefore) {
            boolean restored = dbBackupManager.restoreIfFreshInstall();

            if (restored) {
                Toast.makeText(this, "Database restored from backup", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Database successfully restored from backup.");
                prefs.edit().putBoolean("app_installed", true).apply();
            } else {
                Toast.makeText(this, "No backup found to restore", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "No backup found to restore.");
            }
        }

        // Track app exit by counting activities
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

				public void onActivityStarted(Activity activity) {
					activityCount++;
				}

				public void onActivityStopped(Activity activity) {
					activityCount--;

					if (activityCount == 0 && dbBackupManager != null) {
						boolean backedUp = dbBackupManager.backupDatabase();

						if (backedUp) {
							Toast.makeText(getApplicationContext(), "Database backed up", Toast.LENGTH_SHORT).show();
							Log.d(TAG, "Database successfully backed up.");
						} else {
							Toast.makeText(getApplicationContext(), "Backup failed", Toast.LENGTH_SHORT).show();
							Log.d(TAG, "Database backup failed.");
						}
					}
				}

				// Required empty overrides
				public void onActivityCreated(Activity a, Bundle b) {}
				public void onActivityResumed(Activity a) {}
				public void onActivityPaused(Activity a) {}
				public void onActivitySaveInstanceState(Activity a, Bundle b) {}
				public void onActivityDestroyed(Activity a) {}
			});
    }

    public static SuperMeBackupHelper getDbBackupManager() {
        return dbBackupManager;
    }
}
/*
Why it's needed:

By specifying android:name=".SuperMeApplication", 
Android will create your custom Application class when the app launches. 
That’s where:

SuperMeBackupManager is initialized,

Restore (on first install) happens,

Backup (on real app exit) is triggered.


You don’t need to call the class manually in code. 
Android takes care of it once it sees the entry in the manifest
*/

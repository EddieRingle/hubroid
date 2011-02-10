/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github;

import android.app.Application;
import android.os.StrictMode;

public class HubroidApplication extends Application {
    private static final boolean DEVELOPER_MODE = false;

    @Override
    public void onCreate() {
        /* Enable Strict Mode if DEVELOPER_MODE is set */
        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll()
                    .permitDiskReads().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog()
                    .penaltyDeath().build());
        }
        super.onCreate();
    }

}

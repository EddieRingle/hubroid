package net.idlesoft.android.apps.github;

import android.app.Application;
import android.os.StrictMode;

public class HubroidApplication extends Application {
    private static final boolean DEVELOPER_MODE = true;

    public void onCreate() {
        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .permitDiskReads()
                    .penaltyLog()
                    .penaltyDialog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
        super.onCreate();
    }

}

/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.idlesoft.libraries.ghapi.GitHubAPI;

import android.app.Application;
import android.content.res.Resources;
import android.os.StrictMode;

public class HubroidApplication extends Application {
    private static final boolean DEVELOPER_MODE = false;

    private static GitHubAPI mGApi;

    private static GitHubClient mGitHubClient;

    public static Resources mResources;

    public static GitHubAPI getGApiInstance() {
        if (mGApi == null) {
            mGApi = new GitHubAPI();
        }
        return mGApi;
    }

    public static GitHubClient getGitHubClientInstance() {
        if (mGitHubClient == null) {
            mGitHubClient = new GitHubClient();
        }
        return mGitHubClient;
    }

    public static Resources getAppResources() {
        return mResources;
    }

    @Override
    public void onCreate() {
        /* Enable Strict Mode if DEVELOPER_MODE is set */
        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll()
                    .permitDiskReads().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog()
                    .penaltyDeath().build());
        }
        mResources = getResources();
        super.onCreate();
    }
}

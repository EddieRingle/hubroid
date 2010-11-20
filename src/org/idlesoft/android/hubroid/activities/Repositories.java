/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid.activities;

import org.idlesoft.android.hubroid.R;
import org.idlesoft.android.hubroid.activities.tabs.MyRepos;
import org.idlesoft.android.hubroid.activities.tabs.WatchedRepos;
import org.idlesoft.libraries.ghapi.GitHubAPI;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

public class Repositories extends TabActivity {
    private SharedPreferences mPrefs;
    private String mUsername;
    private String mPassword;
    private GitHubAPI mGapi = new GitHubAPI();
    private TabHost mTabHost;

    private static final String TAG = "Repositories";
    private static final String TAG_MY_REPOS = "my_repos";
    private static final String TAG_WATCHED_REPOS = "watched_repos";

    private View buildIndicator(int textRes) {
        final TextView indicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator, getTabWidget(), false);
        indicator.setText(textRes);
        return indicator;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.repositories);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        final float scale = getResources().getDisplayMetrics().density;
        final int gravatarSize = (int) (38.0f * scale + 0.5f);
        ((ImageView) findViewById(R.id.iv_repositories_gravatar))
                .setImageBitmap(Hubroid.getGravatar(Hubroid.getGravatarID(mUsername), gravatarSize));
        ((TextView) findViewById(R.id.tv_page_title)).setText(mUsername);

        mTabHost = getTabHost();

        mTabHost.addTab(mTabHost.newTabSpec(TAG_MY_REPOS)
                .setIndicator(buildIndicator(R.string.my_repos))
                .setContent(new Intent(getApplicationContext(), MyRepos.class)));
        mTabHost.addTab(mTabHost.newTabSpec(TAG_WATCHED_REPOS)
                .setIndicator(buildIndicator(R.string.watched_repos))
                .setContent(new Intent(getApplicationContext(), WatchedRepos.class)));
    }

    @Override
    public void onStart() {
        super.onStart();

        FlurryAgent.onStartSession(this, "K8C93KDB2HH3ANRDQH1Z");
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }
}
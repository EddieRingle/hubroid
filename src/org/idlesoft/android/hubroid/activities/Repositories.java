/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid.activities;

import com.flurry.android.FlurryAgent;

import org.idlesoft.android.hubroid.R;
import org.idlesoft.android.hubroid.activities.tabs.MyRepos;
import org.idlesoft.android.hubroid.activities.tabs.WatchedRepos;
import org.idlesoft.android.hubroid.utils.GravatarCache;
import org.idlesoft.libraries.ghapi.GitHubAPI;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

public class Repositories extends TabActivity {
    private static final String TAG = "Repositories";

    private static final String TAG_MY_REPOS = "my_repos";

    private static final String TAG_WATCHED_REPOS = "watched_repos";

    private final GitHubAPI mGapi = new GitHubAPI();

    private String mPassword;

    private SharedPreferences mPrefs;

    private TabHost mTabHost;

    private String mTarget;

    private String mUsername;

    private View buildIndicator(final int textRes) {
        final TextView indicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator,
                getTabWidget(), false);
        indicator.setText(textRes);
        return indicator;
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.repositories);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTarget = extras.getString("target");
        }
        if ((mTarget == null) || mTarget.equals("")) {
            mTarget = mUsername;
        }

        ((ImageView) findViewById(R.id.iv_repositories_gravatar)).setImageBitmap(GravatarCache
                .getDipGravatar(GravatarCache.getGravatarID(mTarget), 38.0f, getResources()
                        .getDisplayMetrics().density));
        ((TextView) findViewById(R.id.tv_page_title)).setText(mTarget);

        mTabHost = getTabHost();

        final Intent intent = new Intent(getApplicationContext(), MyRepos.class);
        intent.putExtra("target", mTarget);
        mTabHost.addTab(mTabHost.newTabSpec(TAG_MY_REPOS).setIndicator(
                buildIndicator(R.string.my_repos)).setContent(intent));

        intent.setClass(getApplicationContext(), WatchedRepos.class);
        mTabHost.addTab(mTabHost.newTabSpec(TAG_WATCHED_REPOS).setIndicator(
                buildIndicator(R.string.watched_repos)).setContent(intent));
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

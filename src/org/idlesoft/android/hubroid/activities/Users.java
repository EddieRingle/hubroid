/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2010 Eddie Ringle.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.android.hubroid.activities;

import org.idlesoft.android.hubroid.R;
import org.idlesoft.android.hubroid.activities.tabs.Followers;
import org.idlesoft.android.hubroid.activities.tabs.Following;
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

import com.flurry.android.FlurryAgent;

public class Users extends TabActivity {
    private SharedPreferences mPrefs;
    private String mUsername;
    private String mPassword;
    private GitHubAPI mGapi = new GitHubAPI();
    private TabHost mTabHost;

    private static final String TAG = "Users";
    private static final String TAG_FOLLOWERS = "followers";
    private static final String TAG_FOLLOWING = "following";

    private View buildIndicator(int textRes) {
        final TextView indicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator, getTabWidget(), false);
        indicator.setText(textRes);
        return indicator;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.users);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        ((ImageView) findViewById(R.id.iv_users_gravatar))
                .setImageBitmap(GravatarCache.getDipGravatar(
                        GravatarCache.getGravatarID(mUsername), 38.0f,
                        getResources().getDisplayMetrics().density));
        ((TextView) findViewById(R.id.tv_page_title)).setText(mUsername);

        mTabHost = getTabHost();

        mTabHost.addTab(mTabHost.newTabSpec(TAG_FOLLOWERS)
                .setIndicator(buildIndicator(R.string.followers))
                .setContent(new Intent(getApplicationContext(), Followers.class)));
        mTabHost.addTab(mTabHost.newTabSpec(TAG_FOLLOWING)
                .setIndicator(buildIndicator(R.string.following))
                .setContent(new Intent(getApplicationContext(), Following.class)));
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
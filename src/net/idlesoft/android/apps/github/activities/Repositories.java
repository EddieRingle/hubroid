/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.HubroidApplication;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.tabs.MyRepos;
import net.idlesoft.android.apps.github.activities.tabs.PushableRepos;
import net.idlesoft.android.apps.github.activities.tabs.WatchedRepos;
import net.idlesoft.android.apps.github.utils.GravatarCache;

import org.idlesoft.libraries.ghapi.GitHubAPI;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

public class Repositories extends TabActivity {
    private static final String TAG_MY_REPOS = "my_repos";

    private static final String TAG_WATCHED_REPOS = "watched_repos";

    private static final String TAG_PUSHABLE_REPOS = "pushable_repos";

    private final GitHubAPI mGapi = new GitHubAPI();

    private String mPassword;

    private SharedPreferences mPrefs;

    private TabHost mTabHost;

    private String mTarget;

    private String mUsername;

    private Editor mEditor;

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
        mEditor = mPrefs.edit();

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        HubroidApplication.setupActionBar(Repositories.this);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTarget = extras.getString("target");
        }
        if ((mTarget == null) || mTarget.equals("")) {
            mTarget = mUsername;
        }

        final ImageView gravatar = (ImageView) findViewById(R.id.iv_repositories_gravatar);

        gravatar.setImageBitmap(GravatarCache.getDipGravatar(GravatarCache.getGravatarID(mTarget),
                38.0f, getResources().getDisplayMetrics().density));
        ((TextView) findViewById(R.id.tv_page_title)).setText(mTarget);

        gravatar.setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                final Intent i = new Intent(Repositories.this, Profile.class);
                i.putExtra("username", mTarget);
                startActivity(i);
            }
        });

        mTabHost = getTabHost();

        Intent intent = new Intent(getApplicationContext(), MyRepos.class);
        intent.putExtra("target", mTarget);
        mTabHost.addTab(mTabHost.newTabSpec(TAG_MY_REPOS).setIndicator(
                buildIndicator(R.string.my_repos)).setContent(intent));

        intent = new Intent(getApplicationContext(), WatchedRepos.class);
        intent.putExtra("target", mTarget);
        mTabHost.addTab(mTabHost.newTabSpec(TAG_WATCHED_REPOS).setIndicator(
                buildIndicator(R.string.watched_repos)).setContent(intent));

        /* If we're viewing lists of logged in user's own repositories,
         * show those he/she has push access to */
        if (mTarget.equals(mUsername)) {
            intent = new Intent(getApplicationContext(), PushableRepos.class);
            mTabHost.addTab(mTabHost.newTabSpec(TAG_PUSHABLE_REPOS).setIndicator(
                    buildIndicator(R.string.pushable_repos)).setContent(intent));
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                final Intent i1 = new Intent(this, Hubroid.class);
                startActivity(i1);
                return true;
            case 1:
                mEditor.clear().commit();
                final Intent intent = new Intent(this, Hubroid.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (menu.hasVisibleItems()) {
            menu.clear();
        }
        menu.add(0, 0, 0, "Back to Main").setIcon(android.R.drawable.ic_menu_revert);
        menu.add(0, 1, 0, "Logout");
        return true;
    }
}

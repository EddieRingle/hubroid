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
import net.idlesoft.android.apps.github.activities.tabs.ClosedIssues;
import net.idlesoft.android.apps.github.activities.tabs.OpenIssues;

import org.idlesoft.libraries.ghapi.GitHubAPI;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

public class Issues extends TabActivity {
    private static final String TAG_CLOSED_ISSUES = "closed_issues";

    private static final String TAG_OPEN_ISSUES = "open_issues";

    private final GitHubAPI mGapi = new GitHubAPI();

    private String mPassword;

    private SharedPreferences mPrefs;

    private String mRepositoryName;

    private String mRepositoryOwner;

    private TabHost mTabHost;

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
        setContentView(R.layout.issues);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mEditor = mPrefs.edit();

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        HubroidApplication.setupActionBar(Issues.this);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryOwner = extras.getString("repo_owner");
            mRepositoryName = extras.getString("repo_name");
        }

        ((TextView) findViewById(R.id.tv_page_title)).setText("Issues");

        mTabHost = getTabHost();

        final Intent intent = new Intent(getApplicationContext(), OpenIssues.class);
        intent.putExtra("repo_owner", mRepositoryOwner);
        intent.putExtra("repo_name", mRepositoryName);

        mTabHost.addTab(mTabHost.newTabSpec(TAG_OPEN_ISSUES).setIndicator(
                buildIndicator(R.string.open_issues)).setContent(intent));

        intent.setClass(getApplicationContext(), ClosedIssues.class);
        mTabHost.addTab(mTabHost.newTabSpec(TAG_CLOSED_ISSUES).setIndicator(
                buildIndicator(R.string.closed_issues)).setContent(intent));
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final Intent intent;
        switch (item.getItemId()) {
            case -1:
                intent = new Intent(this, CreateIssue.class);
                intent.putExtra("repo_owner", mRepositoryOwner);
                intent.putExtra("repo_name", mRepositoryName);
                startActivity(intent);
                return true;
            case 0:
                intent = new Intent(this, Hubroid.class);
                startActivity(intent);
                return true;
            case 1:
                mEditor.clear().commit();
                intent = new Intent(this, Hubroid.class);
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
        menu.add(0, -1, 0, "Create Issue").setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, 0, 0, "Back to Main").setIcon(android.R.drawable.ic_menu_revert);
        menu.add(0, 1, 0, "Logout");
        return true;
    }
}

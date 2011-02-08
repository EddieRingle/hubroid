/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import com.flurry.android.FlurryAgent;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.tabs.ClosedIssues;
import net.idlesoft.android.apps.github.activities.tabs.OpenIssues;

import org.idlesoft.libraries.ghapi.GitHubAPI;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TextView;

public class Issues extends TabActivity {
    private static final String TAG_OPEN_ISSUES = "open_issues";

    private static final String TAG_CLOSED_ISSUES = "closed_issues";

    private final GitHubAPI mGapi = new GitHubAPI();

    private String mPassword;

    private SharedPreferences mPrefs;

    private TabHost mTabHost;

    private String mRepositoryOwner;

    private String mRepositoryName;

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
        setContentView(R.layout.issues);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        ((ImageButton) findViewById(R.id.btn_search)).setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                startActivity(new Intent(Issues.this, Search.class));
            }
        });

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
    public void onStart() {
        super.onStart();

        FlurryAgent.onStartSession(this, "K8C93KDB2HH3ANRDQH1Z");
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case -1:
                intent = new Intent(this, CreateIssue.class);
                intent.putExtra("repo_owner", mRepositoryOwner);
                intent.putExtra("repo_name", mRepositoryName);
                startActivity(intent);
                return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (!menu.hasVisibleItems()) {
            menu.add(0, -1, 0, "Create Issue").setIcon(android.R.drawable.ic_menu_add);
        }
        return true;
    }
}
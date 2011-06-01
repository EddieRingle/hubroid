/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.tabs.ClosedIssues;
import net.idlesoft.android.apps.github.activities.tabs.OpenIssues;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Issues extends BaseTabActivity {
    private static final String TAG_CLOSED_ISSUES = "closed_issues";

    private static final String TAG_OPEN_ISSUES = "open_issues";

    private String mRepositoryName;

    private String mRepositoryOwner;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.issues);

        setupActionBar();

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryOwner = extras.getString("repo_owner");
            mRepositoryName = extras.getString("repo_name");
        }

        ((TextView) findViewById(R.id.tv_page_title)).setText("Issues");

        final Intent intent = new Intent(getApplicationContext(), OpenIssues.class);
        intent.putExtra("repo_owner", mRepositoryOwner);
        intent.putExtra("repo_name", mRepositoryName);

        mTabHost.addTab(mTabHost.newTabSpec(TAG_OPEN_ISSUES)
                .setIndicator(buildIndicator(R.string.open_issues)).setContent(intent));

        intent.setClass(getApplicationContext(), ClosedIssues.class);
        mTabHost.addTab(mTabHost.newTabSpec(TAG_CLOSED_ISSUES)
                .setIndicator(buildIndicator(R.string.closed_issues)).setContent(intent));
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
                mPrefsEditor.clear().commit();
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

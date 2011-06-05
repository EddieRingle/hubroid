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
import android.widget.TextView;

public class Issues extends BaseTabActivity {
    private static final String TAG_CLOSED_ISSUES = "closed_issues";

    private static final String TAG_OPEN_ISSUES = "open_issues";

    public final static int CONTEXT_MENU_DETAILS = 1;

    public final static int CONTEXT_MENU_CLOSE = 2;

    public final static int CONTEXT_MENU_REOPEN = 4;

    private String mRepositoryName;

    private String mRepositoryOwner;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.issues);

        setupActionBar("Hubroid", true, true);

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
    public boolean onCreateActionClicked() {
        final Intent intent = new Intent(this, CreateIssue.class);
        intent.putExtra("repo_owner", mRepositoryOwner);
        intent.putExtra("repo_name", mRepositoryName);
        startActivityForResult(intent, 0);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            final Intent refreshIssuesIntent = new Intent(this, Issues.class);
            refreshIssuesIntent.putExtra("repo_owner", mRepositoryOwner);
            refreshIssuesIntent.putExtra("repo_name", mRepositoryName);
            startActivity(refreshIssuesIntent);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

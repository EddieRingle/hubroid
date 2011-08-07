/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.tabs.SearchRepos;
import net.idlesoft.android.apps.github.activities.tabs.SearchUsers;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class Search extends BaseTabActivity {
    private static final String TAG_SEARCH_REPOS = "search_repos";

    private static final String TAG_SEARCH_USERS = "search_users";

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == 5005) {
            Toast.makeText(Search.this, "That user has recently been deleted.", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.search);

        setupActionBar("Search GitHub", false, false);

        final Intent intent = new Intent(Search.this, SearchRepos.class);
        mTabHost.addTab(mTabHost.newTabSpec(TAG_SEARCH_REPOS)
                .setIndicator(buildIndicator(R.string.search_repos)).setContent(intent));

        intent.setClass(getApplicationContext(), SearchUsers.class);
        mTabHost.addTab(mTabHost.newTabSpec(TAG_SEARCH_USERS)
                .setIndicator(buildIndicator(R.string.search_users)).setContent(intent));
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}

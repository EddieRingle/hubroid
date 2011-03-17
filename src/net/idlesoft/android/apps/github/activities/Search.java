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
import net.idlesoft.android.apps.github.activities.tabs.SearchRepos;
import net.idlesoft.android.apps.github.activities.tabs.SearchUsers;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class Search extends TabActivity {
    private static final String TAG_SEARCH_REPOS = "search_repos";

    private static final String TAG_SEARCH_USERS = "search_users";

    private TabHost mTabHost;

    private Editor mEditor;

    private View buildIndicator(final int textRes) {
        final TextView indicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator,
                getTabWidget(), false);
        indicator.setText(textRes);
        return indicator;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == 5005) {
            Toast.makeText(Search.this, "That user has recently been deleted.", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.search);

        mEditor = getSharedPreferences(Hubroid.PREFS_NAME, 0).edit();

        HubroidApplication.setupActionBar(Search.this, false);

        mTabHost = getTabHost();

        final Intent intent = new Intent(Search.this, SearchRepos.class);
        mTabHost.addTab(mTabHost.newTabSpec(TAG_SEARCH_REPOS).setIndicator(
                buildIndicator(R.string.search_repos)).setContent(intent));

        intent.setClass(getApplicationContext(), SearchUsers.class);
        mTabHost.addTab(mTabHost.newTabSpec(TAG_SEARCH_USERS).setIndicator(
                buildIndicator(R.string.search_users)).setContent(intent));
    }

    @Override
    public void onResume() {
        super.onResume();
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

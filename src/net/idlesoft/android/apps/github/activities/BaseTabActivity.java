
package net.idlesoft.android.apps.github.activities;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

import net.idlesoft.android.apps.github.R;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.idlesoft.libraries.ghapi.GitHubAPI;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

public class BaseTabActivity extends TabActivity {
    protected SharedPreferences mPrefs;

    protected SharedPreferences.Editor mPrefsEditor;

    protected String mUsername;

    protected String mPassword;

    protected GitHubClient mGitHubClient;

    protected GitHubAPI mGApi;

    protected TabHost mTabHost;

    protected View buildIndicator(final int textRes) {
        final TextView indicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator,
                getTabWidget(), false);
        indicator.setText(textRes);
        return indicator;
    }

    protected void onCreate(final Bundle icicle, final int layout) {
        super.onCreate(icicle);
        setContentView(layout);

        mPrefs = getSharedPreferences("HubroidPrefs", 0);
        mPrefsEditor = mPrefs.edit();

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGitHubClient = new GitHubClient().setCredentials(mUsername, mPassword);
        mGApi = new GitHubAPI();
        mGApi.authenticate(mUsername, mPassword);

        mTabHost = getTabHost();
    }

    protected ActionBar getActionBar() {
        return (ActionBar) findViewById(R.id.actionbar);
    }

    public void setupActionBar(final String pTitle, final boolean pShowSearch,
            final boolean pShowCreate) {
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        if (actionBar != null) {
            int displayFlags = ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME;
            actionBar.setDisplayOptions(displayFlags);

            getMenuInflater().inflate(R.menu.actionbar, actionBar.asMenu());

            if (pTitle == null) {
                actionBar.setTitle(R.string.app_name);
            } else {
                actionBar.setTitle(pTitle);
            }

            final Action createAction = (Action) actionBar.findAction(R.id.actionbar_item_create);
            final Action searchAction = (Action) actionBar.findAction(R.id.actionbar_item_search);

            if (pShowCreate) {
                createAction.setEnabled(true);
                createAction.setVisible(true);
            }

            if (pShowSearch) {
                searchAction.setEnabled(true);
                searchAction.setVisible(true);
            }
        }
    }

    public void setupActionBar() {
        setupActionBar("Hubroid", true, false);
    }

    public void setupActionBar(final String pTitle) {
        setupActionBar(pTitle, true, false);
    }

    public void setupActionBar(final boolean pShowSearch) {
        setupActionBar("Hubroid", pShowSearch, false);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionbar_item_home:
                startActivity(new Intent(BaseTabActivity.this, Dashboard.class));
                return true;
            case R.id.actionbar_item_create:
                return onCreateActionClicked();
            case R.id.actionbar_item_search:
                startActivity(new Intent(BaseTabActivity.this, Search.class));
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public boolean onCreateActionClicked() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                mPrefsEditor.clear().commit();
                final Intent intent = new Intent(this, Hubroid.class);
                startActivity(intent);
                return true;
            case 0:
                startActivity(new Intent(this, HubroidPreferences.class));
                return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (menu.hasVisibleItems()) {
            menu.clear();
        }
        menu.add(0, 0, 0, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, 1, 0, "Logout").setIcon(android.R.drawable.ic_lock_power_off);
        return true;
    }
}

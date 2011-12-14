
package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.HubroidApplication;
import net.idlesoft.android.apps.github.R;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.idlesoft.libraries.ghapi.GitHubAPI;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class BaseActivity extends Activity {
    protected static final int NO_LAYOUT = -1;

    protected SharedPreferences mPrefs;

    protected SharedPreferences.Editor mPrefsEditor;

    protected String mUsername;

    protected String mPassword;

    protected String mOAuthToken;

    protected GitHubAPI mGApi;

    protected void onCreate(final Bundle icicle, final int layout) {
        super.onCreate(icicle);
        if (layout != NO_LAYOUT) {
            setContentView(layout);
        }

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mPrefsEditor = mPrefs.edit();

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");
        mOAuthToken = mPrefs.getString("access_token", "");

        mGApi = HubroidApplication.getGApiInstance();
        if (!mOAuthToken.equals("")) {
            mGApi.authenticate(mUsername, mOAuthToken, true);
        } else if (!mPassword.equals("")) {
            mGApi.authenticate(mUsername, mPassword, false);
        }
    }

    protected GitHubClient getGitHubClient() {
        if (!mOAuthToken.equals("")) {
            return HubroidApplication.getGitHubClientInstance().setOAuth2Token(mOAuthToken);
        } else if (!mPassword.equals("")) {
            return HubroidApplication.getGitHubClientInstance().setCredentials(mUsername, mPassword);
        } else {
            return HubroidApplication.getGitHubClientInstance();
        }
    }

    protected ActionBar getActionBar() {
        return (ActionBar) findViewById(R.id.actionbar);
    }

    public void setupActionBar(final String pTitle, final boolean pShowSearch,
            final boolean pShowCreate) {
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        if (actionBar != null) {
            int displayFlags = ActionBar.DISPLAY_SHOW_TITLE;
            if (!(this instanceof Dashboard)) {
                displayFlags |= ActionBar.DISPLAY_SHOW_HOME;
            }
            actionBar.setDisplayOptions(displayFlags);

            getMenuInflater().inflate(R.menu.actionbar, actionBar.asMenu());

            if (pTitle == null) {
                actionBar.setTitle(R.string.app_name);
            } else {
                actionBar.setTitle(pTitle);
            }

            final Action createAction = (Action) actionBar.findAction(R.id.actionbar_item_create);
            final Action searchAction = (Action) actionBar.findAction(R.id.actionbar_item_search);

            if (pShowCreate && createAction != null) {
                createAction.setEnabled(true);
                createAction.setVisible(true);
            }

            if (pShowSearch && searchAction != null) {
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
                startActivity(new Intent(BaseActivity.this, Dashboard.class));
                return true;
            case R.id.actionbar_item_create:
                return onCreateActionClicked();
            case R.id.actionbar_item_search:
                startActivity(new Intent(BaseActivity.this, Search.class));
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

    public boolean volumeZoom(KeyEvent event, WebView view) {
        // Only enable volume zooming in files if it's set in preferences
        if (mPrefs.getBoolean(getString(R.string.preferences_key_files_volume_zoom), false)) {
            int action = event.getAction();
            int keyCode = event.getKeyCode();
            switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_UP) {
                    view.zoomIn();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_UP) {
                    view.zoomOut();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
            }
        } else {
            return super.dispatchKeyEvent(event);
        }
    }
}

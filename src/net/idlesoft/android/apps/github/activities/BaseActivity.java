
package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.HubroidApplication;
import net.idlesoft.android.apps.github.R;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.idlesoft.libraries.ghapi.GitHubAPI;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class BaseActivity extends Activity {
    protected static final int NO_LAYOUT = -1;

    protected SharedPreferences mPrefs;

    protected SharedPreferences.Editor mPrefsEditor;

    protected String mUsername;

    protected String mPassword;

    protected GitHubClient mGitHubClient;

    protected GitHubAPI mGApi;

    protected void onCreate(final Bundle icicle, final int layout) {
        super.onCreate(icicle);
        if (layout != NO_LAYOUT) {
            setContentView(layout);
        }

        mPrefs = getSharedPreferences("HubroidPrefs", 0);
        mPrefsEditor = mPrefs.edit();

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGitHubClient = HubroidApplication.getGitHubClientInstance().setCredentials(mUsername,
                mPassword);
        mGApi = HubroidApplication.getGApiInstance();
        mGApi.authenticate(mUsername, mPassword);
    }

    public void setupActionBar(final String pTitle, final boolean pShowSearch,
            final boolean pLinkIcon) {
        final TextView title = (TextView) findViewById(R.id.tv_top_bar_text);
        if (pTitle == null) {
            title.setText("Hubroid");
        } else {
            title.setText(pTitle);
        }

        if (pLinkIcon) {
            final OnClickListener onActionBarIconClick = new OnClickListener() {
                public void onClick(View v) {
                    startActivity(new Intent(BaseActivity.this, Dashboard.class));
                }
            };
            final ImageView icon = (ImageView) findViewById(R.id.iv_top_bar_icon);
            icon.setOnClickListener(onActionBarIconClick);
        }

        final ImageView search = (ImageView) findViewById(R.id.btn_search);
        if (pShowSearch) {
            final OnClickListener onActionBarSearchClick = new OnClickListener() {
                public void onClick(View v) {
                    startActivity(new Intent(BaseActivity.this, Search.class));
                }
            };
            search.setOnClickListener(onActionBarSearchClick);
        } else {
            search.setVisibility(View.GONE);
        }
    }

    public void setupActionBar() {
        setupActionBar("Hubroid", true, true);
    }

    public void setupActionBar(final String pTitle) {
        setupActionBar(pTitle, true, true);
    }

    public void setupActionBar(final boolean pShowSearch) {
        setupActionBar("Hubroid", pShowSearch, true);
    }
}

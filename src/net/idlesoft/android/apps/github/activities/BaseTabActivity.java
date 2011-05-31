package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.idlesoft.libraries.ghapi.GitHubAPI;

import android.app.TabActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
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
}

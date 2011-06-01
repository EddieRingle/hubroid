package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.HubroidApplication;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.idlesoft.libraries.ghapi.GitHubAPI;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

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

		mGitHubClient = HubroidApplication.getGitHubClientInstance().setCredentials(mUsername, mPassword);
		mGApi = HubroidApplication.getGApiInstance();
		mGApi.authenticate(mUsername, mPassword);
	}
}

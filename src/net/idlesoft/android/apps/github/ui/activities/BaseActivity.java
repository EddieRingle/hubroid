/*
 * Copyright (c) 2012 Eddie Ringle
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the
 * following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following
 * disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
  * IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.idlesoft.android.apps.github.ui.activities;

import android.accounts.Account;
import android.accounts.AccountsException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.inject.Inject;
import net.idlesoft.android.apps.github.GitHubClientProvider;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.authenticator.AccountSelect;
import net.idlesoft.android.apps.github.ui.fragments.BaseFragment;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;

import static com.actionbarsherlock.app.ActionBar.DISPLAY_HOME_AS_UP;

public
class BaseActivity extends RoboSherlockFragmentActivity
{
	protected static final int NO_LAYOUT = -1;

	/*
	 * Intent Extra keys
	 */
	protected static final String KEY_CURRENT_USER = "current_user";

	/*
	 * Preferences keys
	 */
	protected static final String PREF_CURRENT_USER_LOGIN = "currentUserLogin";

	protected static final String PREF_FIRST_RUN = "firstRun";

	protected SharedPreferences mPrefs;

	protected SharedPreferences.Editor mPrefsEditor;

	protected static Account mCurrentUser;

	protected static GitHubClient mGitHubClient;

	private Class<?> mUpActivity = MainActivity.class;

	protected
	Configuration mConfiguration;

	@Inject
	private
	GitHubClientProvider mGitHubClientProvider;

	private boolean mAnonymous;

	public
	Context getContext()
	{
		return getApplicationContext();
	}

	protected
	void onCreate(final Bundle icicle, final int layout)
	{
		super.onCreate(icicle);
		if (layout != NO_LAYOUT) setContentView(layout);

		mConfiguration = getResources().getConfiguration();

		mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		mPrefsEditor = mPrefs.edit();

		getApplicationContext().setTheme(R.style.Theme_Hubroid);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
	}

	protected
	void onCreate(final Bundle icicle)
	{
		onCreate(icicle, NO_LAYOUT);
	}

	public
	boolean isMultiPane()
	{
		return getResources().getBoolean(R.bool.multi_paned);
	}

	public
	GitHubClient getGHClient() throws IOException, AccountsException
	{
		if (mGitHubClient == null) {
			if (mCurrentUser == null) {
				mGitHubClient = mGitHubClientProvider.getAnonymousClient();
			} else {
				mGitHubClient = mGitHubClientProvider.getClient(mCurrentUser);
			}
			mCurrentUser = mGitHubClientProvider.getCurrentUser();
		}
		return mGitHubClient;
	}

	public
	Account getCurrentUserAccount()
	{
		return mCurrentUser;
	}

	/**
	 * Does network I/O. Do not run on UI thread.
	 */
	public
	User getCurrentUser() throws IOException, AccountsException
	{
		return new UserService(getGHClient()).getUser();
	}

	public
	String getCurrentUserLogin()
	{
		return mPrefs.getString(PREF_CURRENT_USER_LOGIN, "");
	}

	public
	void startActivity(Class<?> targetActivity)
	{
		startActivity(new Intent(this, targetActivity));
	}

	public
	void startFragment(Class pFragmentClass, int pContainer, String pTag, Bundle pArguments, boolean pAddToBackStack)
	{
		if (BaseFragment.class.isAssignableFrom(pFragmentClass)) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			Fragment fragment;

			/* Try to create an instance of the fragment being started */
			try {
				fragment = (Fragment) pFragmentClass.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				fragment = new Fragment();
			}

			if (pArguments != null)
				fragment.setArguments(pArguments);

			/* Add the fragment */
			ft.replace(pContainer, fragment, pTag);

			/* Add to back stack if specified and commit changes */
			if (pAddToBackStack)
				ft.addToBackStack(null);

			ft.commit();
		}
	}

	public
	void startFragment(Class pFragmentClass)
	{
		startFragment(pFragmentClass, R.id.fragment_container);
	}

	public
	void startFragment(Class pFragmentClass, int pContainer)
	{
		startFragment(pFragmentClass, pContainer, null, null, true);
	}

	public
	void startFragment(Class pFragmentClass, int pContainer, String pTag)
	{
		startFragment(pFragmentClass, pContainer, pTag, null, true);
	}

	public
	void startFragment(Class pFragmentClass, int pContainer, String pTag, Bundle pArguments)
	{
		startFragment(pFragmentClass, pContainer, pTag, pArguments, true);
	}

	@Override
	public
	boolean onCreateOptionsMenu(Menu menu)
	{
		/* Inflate menu from XML */
		MenuInflater inflater = getSherlock().getMenuInflater();
		inflater.inflate(R.menu.actionbar, menu);

		/* Show default actions */
		menu.findItem(R.id.actionbar_action_select_account).setVisible(true);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public
	boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		case android.R.id.home:
			final Intent intent = new Intent();
			if ((theActionBar().getDisplayOptions() & DISPLAY_HOME_AS_UP)
					== DISPLAY_HOME_AS_UP
					&& mUpActivity != null) {
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.setClass(getApplicationContext(), mUpActivity);
			} else {
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setClass(getApplicationContext(), MainActivity.class);
			}
			startActivity(intent);

			return true;
		case R.id.actionbar_action_select_account:
			startActivity(AccountSelect.class);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public
	void onBackPressed()
	{
		if (mUpActivity == MainActivity.class && !(this instanceof MainActivity)) {
			final Intent intent = new Intent(getContext(), mUpActivity);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			finish();
		}

		super.onBackPressed();
	}

	/* Shorter-named getter method because I'm lazy */
	public
	ActionBar theActionBar()
	{
		return getSherlock().getActionBar();
	}
}

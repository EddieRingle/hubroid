/*
 * Copyright (c) 2012 Eddie Ringle
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.idlesoft.android.apps.github.ui.activities;

import com.google.inject.Inject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import net.idlesoft.android.apps.github.GitHubClientProvider;
import net.idlesoft.android.apps.github.HubroidConstants;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.activities.app.AccountSelectActivity;
import net.idlesoft.android.apps.github.ui.activities.app.HomeActivity;
import net.idlesoft.android.apps.github.ui.fragments.BaseFragment;
import net.idlesoft.android.apps.github.ui.fragments.app.AboutDialogFragment;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GsonUtils;

import android.accounts.Account;
import android.accounts.AccountsException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;

import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_ISSUE;
import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_REPO;
import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_USER;

public abstract class BaseActivity extends RoboSherlockFragmentActivity {

    protected static final int NO_LAYOUT = -1;

    /*
      * Intent Extra keys
      */
    protected static final String KEY_CURRENT_USER = "current_user";

    protected SharedPreferences mPrefs;

    protected SharedPreferences.Editor mPrefsEditor;

    protected static Account mCurrentAccount;

    protected static GitHubClient mGitHubClient;

    protected
    Configuration mConfiguration;

    @Inject
    private
    GitHubClientProvider mGitHubClientProvider;

    private FragmentTransaction mFragmentTransaction;

    private boolean mAnonymous;

    private boolean mRefreshPrevious;

    private boolean mCreateActionBarCalled = false;

    private boolean mRefreshing = false;

    private User mCurrentContext = null;

    protected HashMap<String, Object> mArgumentMap = new HashMap<String, Object>();

    private final FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener =
            new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {                    /* Invalidate the options menu whenever the backstack changes */
                    invalidateOptionsMenu();
                }
            };

    public Context getContext() {
        return getApplicationContext();
    }

    protected void onCreate(final Bundle icicle, final int layout) {
        /* Make sure we're using the right theme */
        getApplicationContext().setTheme(R.style.Theme_Hubroid);

        super.onCreate(icicle);

        if (layout != NO_LAYOUT) {
            setContentView(layout);
        }

        mConfiguration = getResources().getConfiguration();

        /*
         * Process arguments Bundle into a HashMap of different objects
		 */
        final Bundle arguments = getIntent().getExtras();
        if (arguments != null) {
            if (arguments.getString(ARG_TARGET_USER) != null) {
                mArgumentMap.put(ARG_TARGET_USER,
                        GsonUtils.fromJson(arguments.getString(ARG_TARGET_USER),
                                User.class));
            }
            if (arguments.getString(ARG_TARGET_ISSUE) != null) {
                mArgumentMap.put(ARG_TARGET_ISSUE,
                        GsonUtils.fromJson(arguments.getString(ARG_TARGET_ISSUE),
                                Issue.class));
            }
            if (arguments.getString(ARG_TARGET_REPO) != null) {
                mArgumentMap.put(ARG_TARGET_ISSUE,
                        GsonUtils.fromJson(arguments.getString(ARG_TARGET_ISSUE),
                                Repository.class));
            }
        }

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrefsEditor = mPrefs.edit();

		/* Refresh the options menu (and the rest of the Action Bar) when the backstack changes */
        getSupportFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);
    }

    protected void onCreate(final Bundle icicle) {
        onCreate(icicle, NO_LAYOUT);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
    }

    /**
     * @return The target user sent to this DataFragment as an argument, or null if none exists.
     */
    public User getTargetUser() {
        return (User) mArgumentMap.get(ARG_TARGET_USER);
    }

    /**
     * @return The target issue sent to this DataFragment as an argument, or null if none exists.
     */
    public Issue getTargetIssue() {
        return (Issue) mArgumentMap.get(ARG_TARGET_ISSUE);
    }

    /**
     * @return The target repository sent to this DataFragment as an argument, or null if none
     *         exists.
     */
    public Repository getTargetRepo() {
        return (Repository) mArgumentMap.get(ARG_TARGET_REPO);
    }

    public boolean isMultiPane() {
        return getResources().getBoolean(R.bool.multi_paned);
    }

    public GitHubClient getGHClient() throws IOException, AccountsException {
        if (mGitHubClient == null) {
            if (mCurrentAccount == null) {
                mGitHubClient = mGitHubClientProvider.getAnonymousClient();
            } else {
                mGitHubClient = mGitHubClientProvider.getClient(this, mCurrentAccount);
            }
            mCurrentAccount = mGitHubClientProvider.getCurrentUser();
        }
        return mGitHubClient;
    }

    public Account getCurrentUserAccount() {
        return mCurrentAccount;
    }

    public User getCurrentUser() {
        final String json = mPrefs.getString(HubroidConstants.PREF_CURRENT_USER, "");
        return GsonUtils.fromJson(json, User.class);
    }

    public String getCurrentUserLogin() {
        return mPrefs.getString(HubroidConstants.PREF_CURRENT_USER_LOGIN, "");
    }

    public String getCurrentContextLogin() {
        return mPrefs.getString(HubroidConstants.PREF_CURRENT_CONTEXT_LOGIN, getCurrentUserLogin());
    }

    public void setCurrentContextLogin(final String context) {
        mPrefsEditor.putString(HubroidConstants.PREF_CURRENT_CONTEXT_LOGIN, context);
        mPrefsEditor.apply();
    }

    public void startActivity(Class<?> targetActivity) {
        startActivity(new Intent(this, targetActivity));
    }

    public void startFragmentTransaction() {
        if (mFragmentTransaction != null) {
            throw new IllegalStateException(
                    "Fragment transaction already started. End the existing one before starting a new instance.");
        }

        mFragmentTransaction = getSupportFragmentManager().beginTransaction();
    }

    public void addFragmentToTransaction(Class<? extends BaseFragment> fragmentClass, int container,
            Bundle arguments) {
        if (mFragmentTransaction == null) {
            throw new IllegalStateException(
                    "BaseActivity Fragment transaction is null, start a new one with startFragmentTransaction().");
        }
        BaseFragment fragment;
        try {
            fragment = (BaseFragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            fragment = new BaseFragment();
        }
        if (arguments != null) {
            fragment.setArguments(arguments);
        }
        if (!isMultiPane() && container != R.id.container_main) {
            container = R.id.container_main;
        }
        mFragmentTransaction.replace(container, fragment, fragmentClass.getName());
    }

    public void finishFragmentTransaction(boolean backstack) {
        if (mFragmentTransaction == null) {
            throw new IllegalStateException(
                    "There is no Fragment transaction to finish (it is null).");
        }

        if (backstack) {
            mFragmentTransaction.addToBackStack(null);
        }

        mFragmentTransaction.commitAllowingStateLoss();        /* Set the activity's transaction to null so a new one can be created */
        mFragmentTransaction = null;
    }

    public void finishFragmentTransaction() {
        finishFragmentTransaction(true);
    }

    private void popToast(final String message, int length) {
        Toast.makeText(getApplication(), message, length).show();
    }

    public void popLongToast(final String message) {
        popToast(message, Toast.LENGTH_LONG);
    }

    public void popShortToast(final String message) {
        popToast(message, Toast.LENGTH_SHORT);
    }

    public void onCreateActionBar(ActionBar bar) {
        mCreateActionBarCalled = true;

        bar.setDisplayShowHomeEnabled(true);

		/* The all-white icon plays nicer with the theme */
        bar.setIcon(R.drawable.ic_launcher_white);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

		/* Inflate menu from XML */
        MenuInflater inflater = getSherlock().getMenuInflater();
        inflater.inflate(R.menu.actionbar, menu);

		/* Show default actions */
        menu.findItem(R.id.actionbar_action_select_account).setVisible(true);

        mCreateActionBarCalled = false;
        onCreateActionBar(getSupportActionBar());
        if (!mCreateActionBarCalled) {
            throw new IllegalStateException("You must call super() in onCreateActionBar()");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final Intent intent;

        switch (item.getItemId()) {
            case android.R.id.home:
                if ((getSupportActionBar().getDisplayOptions() & ActionBar.DISPLAY_HOME_AS_UP)
                        == ActionBar.DISPLAY_HOME_AS_UP) {
                    onBackPressed();
                } else {
                    intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
                return true;
            case R.id.actionbar_action_select_account:
                startActivity(AccountSelectActivity.class);
                return true;
            case R.id.actionbar_action_report_issue:
                intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/eddieringle/hubroid/issues"));
                startActivity(intent);
                return true;
            case R.id.actionbar_action_about:
                AboutDialogFragment about = new AboutDialogFragment();
                about.show(getSupportFragmentManager(), AboutDialogFragment.class.getName());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setRefreshPrevious(final boolean refreshPrevious) {
        mRefreshPrevious = refreshPrevious;
    }

    /**
     * Check if a past life wants us to refresh our data This is a one-time-only check, so be sure
     * to store the value if you need to
     */
    public boolean getRefreshPrevious() {
        final boolean oldValue = mRefreshPrevious;
        mRefreshPrevious = false;
        return oldValue;
    }

    public void onStartRefresh() {
        mRefreshing = true;
    }

    public void onFinishRefresh() {
        mRefreshing = false;
    }

    protected void doRefresh() {
    }

    /**
     * This method is useful when creating Executables to run on a DataFragment and you need to know
     * if the data should be refreshed from its source or if it's okay to use cached information (or
     * something like that).
     *
     * @return Whether or not the UIFragment is refreshing its data & UI
     */
    public boolean isRefreshing() {
        return mRefreshing;
    }

    public SharedPreferences getPrefs() {
        return mPrefs;
    }

    public SharedPreferences.Editor getPrefsEditor() {
        return mPrefsEditor;
    }
}

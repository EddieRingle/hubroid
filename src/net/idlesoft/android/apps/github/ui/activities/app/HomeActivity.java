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

package net.idlesoft.android.apps.github.ui.activities.app;

import com.actionbarsherlock.app.ActionBar;
import com.bugsense.trace.BugSenseHandler;
import com.viewpagerindicator.TabPageIndicator;

import net.idlesoft.android.apps.github.HubroidConstants;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.authenticator.AuthConstants;
import net.idlesoft.android.apps.github.ui.activities.BaseActivity;
import net.idlesoft.android.apps.github.ui.adapters.ContextListAdapter;
import net.idlesoft.android.apps.github.ui.fragments.app.ProfileFragment;
import net.idlesoft.android.apps.github.ui.fragments.app.RepositoryListFragment;
import net.idlesoft.android.apps.github.utils.AsyncLoader;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.service.OrganizationService;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountsException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.actionbarsherlock.app.ActionBar.NAVIGATION_MODE_LIST;
import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_USER;
import static net.idlesoft.android.apps.github.HubroidConstants.LOADER_CONTEXTS;
import static net.idlesoft.android.apps.github.ui.fragments.app.RepositoryListFragment.ARG_LIST_TYPE;
import static net.idlesoft.android.apps.github.ui.fragments.app.RepositoryListFragment.LIST_USER;
import static net.idlesoft.android.apps.github.ui.fragments.app.RepositoryListFragment.LIST_WATCHED;

public class HomeActivity extends BaseActivity
        implements LoaderManager.LoaderCallbacks<List<User>> {

    private static class TabHolder {

        Class fragmentClazz;

        CharSequence title;

        Bundle args = new Bundle();
    }

    private class DashboardTabPagerAdapter extends FragmentPagerAdapter {

        public ArrayList<TabHolder> holders;

        public DashboardTabPagerAdapter(FragmentManager fm) {
            super(fm);
            holders = new ArrayList<TabHolder>();
        }

        @Override
        public Fragment getItem(int position) {
            final Bundle args = new Bundle();
            final TabHolder holder = holders.get(position);
            Fragment fragment = null;

			/*
             * Try to instantiate the correct fragment, otherwise defaulting to a regular
			 * Fragment instance.
			 */
            try {
                fragment = (Fragment) holder.fragmentClazz.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                if (fragment == null) {
                    fragment = new Fragment();
                }
            }

			/*
             * Set the target user to be the currently logged in user (since we're on the
			 * Dashboard). If the user is currently using a different context, (one of his/her
			 * organizations) then we pass that login instead.
			 */
            if (getCurrentUser() != null) {
                if (!getCurrentUser().getLogin().equalsIgnoreCase(getCurrentContextLogin())) {
                    args.putString(ARG_TARGET_USER,
                            GsonUtils.toJson(
                                    new User().setLogin(getCurrentContextLogin())));
                } else {
                    args.putString(ARG_TARGET_USER, GsonUtils.toJson(getCurrentUser()));
                }
            }

			/*
             * Add any additional arguments specified by the TabHolder
			 */
            if (holder.args != null || !holder.args.isEmpty()) {
                args.putAll(holder.args);
            }

			/* Pass all arguments on to the attached fragment */
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            TabHolder holder = holders.get(position);
            if (holder.title != null) {
                return holder.title;
            } else {
                return super.getPageTitle(position);
            }
        }

        @Override
        public int getCount() {
            return holders.size();
        }
    }

    private boolean mFirstRun;

    private boolean mReadyForContext;

    private ProgressBar mProgress;

    private LinearLayout mContent;

    private TabPageIndicator mTabPageIndicator;

    private ViewPager mViewPager;

    private DashboardTabPagerAdapter mPagerAdapter;

    private ContextListAdapter mContextAdapter;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle, R.layout.home_activity);

		/* BugSense setup */
        BugSenseHandler.setup(this, "40e35a51");

        mFirstRun = mPrefs.getBoolean(HubroidConstants.PREF_FIRST_RUN, true);

        if (mFirstRun) {
			/*
			 * If this is the first time the user is using Hubroid, take them directly to the
			 * Account Selection screen so that they can log in if they so choose.
			 */
            mPrefsEditor.putBoolean(HubroidConstants.PREF_FIRST_RUN, false);
            mPrefsEditor.commit();

			/* Show the Account Selection screen first, then start the show */
            startActivityForResult(new Intent(this, AccountSelectActivity.class), 0);
            finish();
        } else {
			/*
			 * Otherwise, make sure whatever existing account Hubroid is set to use hasn't been
			 * deleted by the user in the system settings prior to returning to Hubroid.
			 */
            final String currentUserLogin = mPrefs.getString(
                    HubroidConstants.PREF_CURRENT_USER_LOGIN, null);
            final String currentUserJson = mPrefs
                    .getString(HubroidConstants.PREF_CURRENT_USER, null);
            final Account[] accounts = AccountManager.get(this).getAccountsByType(
                    AuthConstants.GITHUB_ACCOUNT_TYPE);
            int i;

            if (currentUserLogin != null && currentUserJson == null) {
                startActivityForResult(new Intent(this, AccountSelectActivity.class), 0);
                finish();
            }

            mCurrentAccount = null;

            if (currentUserLogin != null && currentUserJson != null) {
                mCurrentAccount = null;

				/* Try to find our current account */
                for (i = 0; i < accounts.length; i++) {
                    if (accounts[i].name.equals(currentUserLogin)) {
                        mCurrentAccount = accounts[i];
                    }
                }

				/*
				 * If we can't find the account, it must have been deleted.
				 * Redirect to the Account selection activity.
				 */
                if (mCurrentAccount == null) {
                    mPrefsEditor.remove(HubroidConstants.PREF_CURRENT_USER);
                    mPrefsEditor.remove(HubroidConstants.PREF_CURRENT_USER_LOGIN);
                    mPrefsEditor.remove(HubroidConstants.PREF_CURRENT_CONTEXT_LOGIN);

                    startActivityForResult(new Intent(this, AccountSelectActivity.class), 0);
                    finish();
                }
            }
        }

        mProgress = (ProgressBar) findViewById(R.id.progress);
        mContent = (LinearLayout) findViewById(R.id.content);
        mTabPageIndicator = (TabPageIndicator) findViewById(R.id.tpi_header);
        mViewPager = (ViewPager) findViewById(R.id.vp_pages);

        mPagerAdapter = new DashboardTabPagerAdapter(getSupportFragmentManager());

		/* Check to see if we're logged in */
        if (!getCurrentUserLogin().equals("")) {
			/*
			 * Add a Profile fragment to the Dashboard tab pager
			 */
            TabHolder profileHolder = new TabHolder();
            profileHolder.fragmentClazz = ProfileFragment.class;
            profileHolder.title = getString(R.string.dash_profile);
            mPagerAdapter.holders.add(profileHolder);

            TabHolder ownedReposHolder = new TabHolder();
            ownedReposHolder.fragmentClazz = RepositoryListFragment.class;
            ownedReposHolder.title = getString(R.string.repositories);
            ownedReposHolder.args.putInt(ARG_LIST_TYPE, LIST_USER);
            mPagerAdapter.holders.add(ownedReposHolder);

            TabHolder watchedReposHolder = new TabHolder();
            watchedReposHolder.fragmentClazz = RepositoryListFragment.class;
            watchedReposHolder.title = getString(R.string.repositories_watched);
            watchedReposHolder.args.putInt(ARG_LIST_TYPE, LIST_WATCHED);
            mPagerAdapter.holders.add(watchedReposHolder);
        }

		/*
		 * Now that the pager adapter has a bunch of holders to display, attach it the the
		 * ViewPager and the ViewPager to the TabPageIndicator.
		 */
        mViewPager.setAdapter(mPagerAdapter);
        mTabPageIndicator.setViewPager(mViewPager);
        mViewPager.setOffscreenPageLimit(4);

		/*
		 * Let's start the context list Loader if we're logged in.
		 */
        if (!getCurrentContextLogin().equals("")) {
            getSupportLoaderManager().restartLoader(LOADER_CONTEXTS, null, this);
        } else {
            getSupportActionBar().setTitle(R.string.app_name);
        }
    }

    @Override
    public void onCreateActionBar(ActionBar bar) {
        super.onCreateActionBar(bar);

        bar.setTitle(null);
        bar.setSubtitle(null);
        bar.setDisplayHomeAsUpEnabled(false);
        bar.setHomeButtonEnabled(false);
    }

    @Override
    public Loader<List<User>> onCreateLoader(int id, Bundle args) {
        return new AsyncLoader<List<User>>(HomeActivity.this) {
            @Override
            public List<User> loadInBackground() {
                try {
					/*
					 * Grab a list of the currently authenticated
					 * user's organizations from GitHub. After that,
					 * build an ArrayList with the user as the first
					 * item and continue adding organizations from
					 * there.
					 */
                    final OrganizationService os = new OrganizationService(getGHClient());
                    List<User> orgs = os.getOrganizations();
                    if (orgs != null) {
                        final ArrayList<User> res = new ArrayList<User>();
                        res.add(getCurrentUser());
                        int len = orgs.size();
                        for (int i = 0; i < len; i++) {
                            res.add(orgs.get(i));
                        }
                        return res;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (AccountsException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<User>> loader, List<User> data) {
		/*
		 * Move the data into an ArrayList.
		 */
        final ArrayList<User> arrayData = new ArrayList<User>();
        arrayData.addAll(data);

		/*
		 * Configure the Action Bar for list-based navigation.
		 */
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setNavigationMode(NAVIGATION_MODE_LIST);

		/*
		 * Create and fill a ContextListAdapter and pass it along
		 * to the Action Bar.
		 */
        mContextAdapter = new ContextListAdapter(this);
        mContextAdapter.fillWithItems(arrayData);
        getSupportActionBar().setListNavigationCallbacks(mContextAdapter,
                new ActionBar.OnNavigationListener() {
                    @Override
                    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				/*
				 * We can't set the context right away because of the code
				 * that follows this interface implementation, which
				 * finds the current context and sets it as the currently
				 * selected list item. To get around this, we ignore the first
				 * time this method is called.
				 */
                        if (mReadyForContext) {
                            final User target = mContextAdapter.getItem(itemPosition);
                            setCurrentContextLogin(target.getLogin());
                            final Intent rebootIntent = new Intent(HomeActivity.this,
                                    HomeActivity.class);
                            rebootIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(rebootIntent);
                            finish();
                        } else {
                            mReadyForContext = true;
                        }
                        return false;
                    }
                });

		/*
		 * Loop through the list of users/organizations to find the
		 * current context the user is browsing as and set its
		 * corresponding list item to be the one that is selected.
		 */
        int len = arrayData.size();
        for (int i = 0; i < len; i++) {
            if (arrayData.get(i).getLogin().equals(getCurrentContextLogin())) {
                getSupportActionBar().setSelectedNavigationItem(i);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<User>> loader) {
    }
}

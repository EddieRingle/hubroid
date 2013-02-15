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

import com.google.gson.reflect.TypeToken;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.github.eddieringle.android.libs.undergarment.widgets.DrawerGarment;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.services.GitHubApiService;
import net.idlesoft.android.apps.github.ui.activities.app.EventsActivity;
import net.idlesoft.android.apps.github.ui.activities.app.HomeActivity;
import net.idlesoft.android.apps.github.ui.activities.app.ProfileActivity;
import net.idlesoft.android.apps.github.ui.activities.app.RepositoriesActivity;
import net.idlesoft.android.apps.github.ui.adapters.ContextListAdapter;
import net.idlesoft.android.apps.github.ui.adapters.DashboardListAdapter;
import net.idlesoft.android.apps.github.ui.widgets.OcticonView;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GsonUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_USER;
import static net.idlesoft.android.apps.github.HubroidConstants.PREF_CURRENT_USER_LOGIN;
import static net.idlesoft.android.apps.github.services.GitHubApiService.ACTION_ORGS_SELF_MEMBERSHIPS;
import static net.idlesoft.android.apps.github.services.GitHubApiService.ARG_ACCOUNT;
import static net.idlesoft.android.apps.github.services.GitHubApiService.EXTRA_RESULT_JSON;
import static net.idlesoft.android.apps.github.ui.fragments.app.EventListFragment.ARG_EVENT_LIST_TYPE;
import static net.idlesoft.android.apps.github.ui.fragments.app.EventListFragment.LIST_USER_PRIVATE;
import static net.idlesoft.android.apps.github.ui.fragments.app.RepositoryListFragment.ARG_LIST_TYPE;
import static net.idlesoft.android.apps.github.ui.fragments.app.RepositoryListFragment.LIST_USER;
import static net.idlesoft.android.apps.github.ui.fragments.app.RepositoryListFragment.LIST_STARRED;

public class BaseDashboardActivity extends BaseActivity {

    public static final String EXTRA_CONTEXTS = "extra_contexts";

    public static final String ARG_FROM_DASHBOARD = "extra_from_dashboard";

    public static final String EXTRA_SHOWING_DASH = "extra_showing_dash";

    private boolean mFromDashboard;

    private boolean mReadyForContext = false;

    private boolean mShowingDash;

    private DrawerGarment mDrawerGarment;

    private ListView mDashboardListView;

    private DashboardListAdapter mDashboardListAdapter;

    private Spinner mContextSpinner;

    private ContextListAdapter mContextListAdapter;

    private Intent mTargetIntent;

    private AdapterView.OnItemSelectedListener mOnContextItemSelectedListener;

    private BroadcastReceiver mContextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                return;
            }

            if (intent.getAction().equals(ACTION_ORGS_SELF_MEMBERSHIPS)) {
                final ArrayList<User> contexts = new ArrayList<User>();
                contexts.add(getCurrentUser());

                final String orgsJson = intent.getStringExtra(EXTRA_RESULT_JSON);
                if (orgsJson != null) {
                    TypeToken<List<User>> token = new TypeToken<List<User>>() {
                    };
                    List<User> orgs = GsonUtils.fromJson(orgsJson, token.getType());
                    contexts.addAll(orgs);
                }

                /*
                 * Loop through the list of users/organizations to find the
                 * current context the user is browsing as and rearrange the
                 * list so that it's at the top.
                 */
                int len = contexts.size();
                for (int i = 0; i < len; i++) {
                    if (contexts.get(i).getLogin().equals(getCurrentContextLogin())) {
                        Collections.swap(contexts, i, 0);
                        break;
                    }
                }

                mContextListAdapter = new ContextListAdapter(BaseDashboardActivity.this);
                mContextListAdapter.fillWithItems(contexts);

                mContextSpinner.setAdapter(mContextListAdapter);

                mContextSpinner.setOnItemSelectedListener(mOnContextItemSelectedListener);
                mContextSpinner.setEnabled(true);
            }
        }
    };

    public DrawerGarment getDrawerGarment() {
        return mDrawerGarment;
    }

    public boolean isDrawerOpened() {
        if (getDrawerGarment() != null) {
            return getDrawerGarment().isDrawerOpened();
        }

        return false;
    }

    public boolean isFromDashboard() {
        return mFromDashboard;
    }

    @Override
    protected void onCreate(Bundle icicle, int layout) {
        super.onCreate(icicle, layout);

        if (icicle != null) {
            mShowingDash = icicle.getBoolean(EXTRA_SHOWING_DASH, false);
        }

        mDrawerGarment = new DrawerGarment(this, R.layout.dashboard);
        mDrawerGarment.setSlideTarget(DrawerGarment.SLIDE_TARGET_CONTENT);
        mDrawerGarment.setDrawerCallbacks(new DrawerGarment.IDrawerCallbacks() {
            @Override
            public void onDrawerOpened() {
                mShowingDash = true;
                supportInvalidateOptionsMenu();
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            @Override
            public void onDrawerClosed() {
                if (mTargetIntent == null) {
                    mShowingDash = false;
                    supportInvalidateOptionsMenu();
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                } else {
                    startActivity(mTargetIntent);
                    overridePendingTransition(0, 0);
                    finish();
                }
            }
        });

        if (getIntent() != null) {
            mFromDashboard = getIntent().getBooleanExtra(ARG_FROM_DASHBOARD, false);
            if (!mFromDashboard) {
                getDrawerGarment().setDrawerEnabled(false);
                mShowingDash = false;
            } else if (getIntent().hasExtra(EXTRA_SHOWING_DASH)) {
                mShowingDash = getIntent().getBooleanExtra(EXTRA_SHOWING_DASH, mShowingDash);
            }
        }

        mTargetIntent = null;

        final ArrayList<DashboardListAdapter.DashboardEntry> dashboardEntries =
                new ArrayList<DashboardListAdapter.DashboardEntry>();

        DashboardListAdapter.DashboardEntry entry = new DashboardListAdapter.DashboardEntry();
        entry.label = getString(R.string.dash_events);
        entry.icon = (new OcticonView(this)).setOcticon(OcticonView.IC_FEED)
                .setGlyphColor(Color.parseColor("#2c2c2c"))
                .setGlyphSize(72.0f)
                .toDrawable();
        entry.selected = false;
        if (this instanceof EventsActivity) {
            entry.selected = true;
        }
        entry.onEntryClickListener = new DashboardListAdapter.DashboardEntry.OnEntryClickListener() {
            @Override
            public void onClick(DashboardListAdapter.DashboardEntry entry, int i) {
                final Intent eventsIntent = new Intent(BaseDashboardActivity.this,
                        EventsActivity.class);
                eventsIntent.setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_NEW_TASK);
                eventsIntent.putExtra(ARG_FROM_DASHBOARD, true);
                eventsIntent.putExtra(ARG_TARGET_USER,
                        GsonUtils.toJson(new User().setLogin(getCurrentContextLogin())));
                eventsIntent.putExtra(ARG_EVENT_LIST_TYPE, LIST_USER_PRIVATE);
                mTargetIntent = eventsIntent;
                getDrawerGarment().closeDrawer();
            }
        };
        dashboardEntries.add(entry);

        entry = new DashboardListAdapter.DashboardEntry();
        entry.label = getString(R.string.dash_profile);
        entry.icon = (new OcticonView(this)).setOcticon(OcticonView.IC_PERSON)
                .setGlyphColor(Color.parseColor("#2c2c2c"))
                .setGlyphSize(72.0f)
                .toDrawable();
        entry.selected = false;
        if (this instanceof ProfileActivity) {
            entry.selected = true;
        }
        entry.onEntryClickListener = new DashboardListAdapter.DashboardEntry.OnEntryClickListener() {
            @Override
            public void onClick(DashboardListAdapter.DashboardEntry entry, int i) {
                final Intent profileIntent = new Intent(BaseDashboardActivity.this,
                        ProfileActivity.class);
                profileIntent.setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_NEW_TASK);
                profileIntent.putExtra(ARG_FROM_DASHBOARD, true);
                profileIntent.putExtra(ARG_TARGET_USER,
                        GsonUtils.toJson(new User().setLogin(getCurrentContextLogin())));
                mTargetIntent = profileIntent;
                getDrawerGarment().closeDrawer();
            }
        };
        dashboardEntries.add(entry);

        mDashboardListAdapter = new DashboardListAdapter(this);
        mDashboardListAdapter.fillWithItems(dashboardEntries);

        final LinearLayout headerLayout = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.dashboard_account_header, null);
        mContextSpinner = (Spinner) headerLayout.findViewById(R.id.context_spinner);
        mContextSpinner.setEnabled(false);

        mOnContextItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i,
                    long l) {
                if (mReadyForContext) {
                    /* Remove the listener so we don't handle any more events */
                    mContextSpinner.setOnItemSelectedListener(null);

                    /*
                     * Reboot the app with the new context loaded
                     */
                    final User target = mContextListAdapter.getItem(i);
                    setCurrentContextLogin(target.getLogin());
                    final Intent rebootIntent = new Intent(BaseDashboardActivity.this,
                            HomeActivity.class);
                    rebootIntent.setFlags(FLAG_ACTIVITY_CLEAR_TOP
                            | FLAG_ACTIVITY_NEW_TASK);
                    startActivity(rebootIntent);
                    finish();
                } else {
                    mReadyForContext = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        };

        mDashboardListView = (ListView) mDrawerGarment.findViewById(R.id.list);

        if (isLoggedIn()) {
            mDashboardListView.addHeaderView(headerLayout);
        }

        mDashboardListView.setAdapter(mDashboardListAdapter);
        mDashboardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (isLoggedIn()) {
                    if (i == 0) {
                        return;
                    } else {
                        i--;
                    }
                }
                final DashboardListAdapter.DashboardEntry entry = mDashboardListAdapter.getItem(i);
                if (entry != null) {
                    if (entry.onEntryClickListener != null) {
                        entry.onEntryClickListener.onClick(entry, i);
                    }
                }
            }
        });

        if (isLoggedIn()) {
            final IntentFilter contextFilter = new IntentFilter(ACTION_ORGS_SELF_MEMBERSHIPS);
            registerReceiver(mContextReceiver, contextFilter);

            if (icicle == null) {
                final Intent getContextsIntent = new Intent(this, GitHubApiService.class);
                getContextsIntent.setAction(ACTION_ORGS_SELF_MEMBERSHIPS);
                getContextsIntent.putExtra(ARG_ACCOUNT, getCurrentUserAccount());
                startService(getContextsIntent);
            } else {
                if (icicle.containsKey(EXTRA_CONTEXTS)) {
                    final String contextsJson = icicle.getString(EXTRA_CONTEXTS);
                    if (contextsJson != null) {
                        TypeToken<List<User>> token = new TypeToken<List<User>>() {
                        };
                        List<User> contexts = GsonUtils.fromJson(contextsJson, token.getType());

                        /*
                         * Loop through the list of users/organizations to find the
                         * current context the user is browsing as and rearrange the
                         * list so that it's at the top.
                         */
                        int len = contexts.size();
                        for (int i = 0; i < len; i++) {
                            if (contexts.get(i).getLogin().equals(getCurrentContextLogin())) {
                                Collections.swap(contexts, i, 0);
                                break;
                            }
                        }

                        mContextListAdapter = new ContextListAdapter(BaseDashboardActivity.this);
                        mContextListAdapter.fillWithItems(contexts);

                        mContextSpinner.setAdapter(mContextListAdapter);

                        mContextSpinner.setOnItemSelectedListener(mOnContextItemSelectedListener);
                        mContextSpinner.setEnabled(true);
                    }
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mShowingDash) {
            mDrawerGarment.openDrawer(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mReadyForContext = false;
        mContextSpinner.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mContextReceiver != null) {
            try {
                unregisterReceiver(mContextReceiver);
            } catch (IllegalArgumentException e) {
                /* Ignore this. */
            }
        }
    }

    @Override
    public void onCreateActionBar(ActionBar bar) {
        super.onCreateActionBar(bar);

        if (getDrawerGarment().isDrawerEnabled()) {
            if (isFromDashboard()) {
                bar.setIcon(R.drawable.ic_launcher_white_dashboard);
            }

            if (getDrawerGarment().isDrawerOpened()) {
                bar.setDisplayHomeAsUpEnabled(true);
            } else {
                bar.setDisplayHomeAsUpEnabled(false);
            }
        }

        if (!isFromDashboard()) {
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mContextListAdapter != null && mContextListAdapter.getAll() != null) {
            outState.putString(EXTRA_CONTEXTS, GsonUtils.toJson(mContextListAdapter.getAll()));
        }
        outState.putBoolean(EXTRA_SHOWING_DASH, mShowingDash);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerGarment.isDrawerEnabled()) {
                mDrawerGarment.toggleDrawer();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerGarment.isDrawerOpened()) {
            mDrawerGarment.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }
}

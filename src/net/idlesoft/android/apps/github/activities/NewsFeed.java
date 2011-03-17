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
import net.idlesoft.android.apps.github.adapters.ActivityFeedAdapter;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class NewsFeed extends Activity {
    private static class LoadActivityFeedTask extends AsyncTask<Void, Void, Void> {
        public NewsFeed activity;

        @Override
        protected Void doInBackground(final Void... params) {
            if (activity.mJson == null) {
                try {
                    final Response resp;
                    if (mPrivate) {
                        resp = activity.mGapi.user.private_activity();
                    } else {
                        resp = activity.mGapi.user.activity(mTargetUser);
                    }
                    if (resp.statusCode != 200) {
                        /* Let the user know something went wrong */
                        return null;
                    }
                    activity.mJson = new JSONArray(resp.resp);
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
            }
            if (activity.mJson != null) {
                activity.mActivityAdapter.loadData(activity.mJson);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            activity.mActivityAdapter.pushData();
            activity.mActivityAdapter.setIsLoadingData(false);
        }

        @Override
        protected void onPreExecute() {
            activity.mActivityAdapter.setIsLoadingData(true);
        }
    }

    private static boolean mPrivate;

    private static String mTargetUser;

    private ActivityFeedAdapter mActivityAdapter;

    private SharedPreferences.Editor mEditor;

    public GitHubAPI mGapi = new GitHubAPI();

    public JSONArray mJson;

    private ListView mListView;

    private LoadActivityFeedTask mLoadActivityTask;

    private String mPassword;

    private SharedPreferences mPrefs;

    private String mUsername;

    private final OnItemClickListener onActivityItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2,
                final long arg3) {
            try {
                final Intent intent = new Intent(getApplicationContext(), SingleActivityItem.class);
                intent.putExtra("item_json", mJson.getJSONObject(arg2).toString());
                startActivity(intent);
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.news_feed);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mEditor = mPrefs.edit();
        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        HubroidApplication.setupActionBar(NewsFeed.this);

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mTargetUser = bundle.getString("username");
            mPrivate = false;
        } else {
            mTargetUser = mUsername;
            mPrivate = true;
        }

        mListView = (ListView) findViewById(R.id.lv_news_feed);
        mListView.setOnItemClickListener(onActivityItemClick);

        mActivityAdapter = new ActivityFeedAdapter(NewsFeed.this, mListView, mPrivate == false);
        mListView.setAdapter(mActivityAdapter);

        if (mPrivate) {
            ((TextView) findViewById(R.id.tv_page_title)).setText("News Feed");
        } else {
            ((TextView) findViewById(R.id.tv_page_title)).setText(mTargetUser + "'s Activity");
        }

        mLoadActivityTask = (LoadActivityFeedTask) getLastNonConfigurationInstance();
        if (mLoadActivityTask == null) {
            mLoadActivityTask = new LoadActivityFeedTask();
        }
        mLoadActivityTask.activity = this;
        if (mLoadActivityTask.getStatus() == AsyncTask.Status.PENDING) {
            mLoadActivityTask.execute();
        }
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

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean keepGoing = true;
        try {
            if (savedInstanceState.containsKey("json")) {
                mJson = new JSONArray(savedInstanceState.getString("json"));
            } else {
                keepGoing = false;
            }
        } catch (final JSONException e) {
            keepGoing = false;
        }

        if (keepGoing == true) {
            mActivityAdapter.loadData(mJson);
            mActivityAdapter.pushData();
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mLoadActivityTask;
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        if (mJson != null) {
            savedInstanceState.putString("json", mJson.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }
}

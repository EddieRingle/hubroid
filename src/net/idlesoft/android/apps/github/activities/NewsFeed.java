/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.ActivityFeedAdapter;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class NewsFeed extends BaseActivity {
    private static class LoadActivityFeedTask extends AsyncTask<Void, Void, Void> {
        public NewsFeed activity;

        @Override
        protected Void doInBackground(final Void... params) {
            // TODO: Convert to use egit-github
            if (activity.mJson == null) {
                try {
                    final Response resp;
                    if (mPrivate) {
                        resp = activity.mGApi.user.private_activity();
                    } else {
                        resp = activity.mGApi.user.activity(mTargetUser);
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

    public JSONArray mJson;

    private ListView mListView;

    private LoadActivityFeedTask mLoadActivityTask;

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
        super.onCreate(icicle, R.layout.news_feed);

        setupActionBar();

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
            getActionBar().setTitle("News Feed");
        } else {
            getActionBar().setTitle("Public Activity");
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

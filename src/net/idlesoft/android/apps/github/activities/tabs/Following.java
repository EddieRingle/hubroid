/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities.tabs;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.Hubroid;
import net.idlesoft.android.apps.github.activities.Profile;
import net.idlesoft.android.apps.github.adapters.FollowersFollowingListAdapter;

import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class Following extends Activity {
    private static class FollowingTask extends AsyncTask<Void, Void, Void> {
        public Following activity;

        @Override
        protected Void doInBackground(final Void... params) {
            if (activity.mJson == null) {
                try {
                    final Response resp = activity.mGapi.user.following(activity.mTarget);
                    if (resp.statusCode != 200) {
                        /* Oh noez, something went wrong */
                        return null;
                    }
                    activity.mJson = (new JSONObject(resp.resp)).getJSONArray("users");
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
                activity.mAdapter.loadData(activity.mJson);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            activity.mAdapter.pushData();
            activity.mAdapter.setIsLoadingData(false);
        }

        @Override
        protected void onPreExecute() {
            activity.mAdapter.setIsLoadingData(true);
        }
    }

    private FollowersFollowingListAdapter mAdapter;

    private final GitHubAPI mGapi = new GitHubAPI();

    private JSONArray mJson;

    private ListView mListView;

    private final OnItemClickListener mOnListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                final long id) {
            final Intent i = new Intent(getApplicationContext(), Profile.class);
            try {
                i.putExtra("username", mJson.getString(position));
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            startActivity(i);
            return;
        }
    };

    private String mTarget;

    private FollowingTask mTask;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);

        final SharedPreferences prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mGapi.authenticate(prefs.getString("username", ""), prefs.getString("password", ""));

        mListView = (ListView) getLayoutInflater().inflate(R.layout.tab_listview, null);
        mListView.setOnItemClickListener(mOnListItemClick);

        setContentView(mListView);

        mAdapter = new FollowersFollowingListAdapter(Following.this, mListView);
        mListView.setAdapter(mAdapter);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTarget = extras.getString("target");
        }
        if ((mTarget == null) || mTarget.equals("")) {
            mTarget = prefs.getString("username", "");
        }

        mTask = (FollowingTask) getLastNonConfigurationInstance();
        if ((mTask == null) || (mTask.getStatus() == AsyncTask.Status.FINISHED)) {
            mTask = new FollowingTask();
        }
        mTask.activity = this;
        if (mTask.getStatus() == AsyncTask.Status.PENDING) {
            mTask.execute();
        }
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        try {
            if (savedInstanceState.containsKey("json")) {
                mJson = new JSONArray(savedInstanceState.getString("json"));
            } else {
                return;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }
        if (mJson != null) {
            mAdapter.loadData(mJson);
            mAdapter.pushData();
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mTask;
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        if (mJson != null) {
            savedInstanceState.putString("json", mJson.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }
}

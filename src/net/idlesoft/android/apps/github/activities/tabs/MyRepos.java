/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities.tabs;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.activities.Hubroid;
import net.idlesoft.android.apps.github.activities.Repository;
import net.idlesoft.android.apps.github.adapters.RepositoriesListAdapter;

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
import android.widget.TextView;

public class MyRepos extends Activity {
    private static class MyReposTask extends AsyncTask<Void, Void, Void> {
        public MyRepos mActivity;

        public MyReposTask(final MyRepos activity) {
            mActivity = activity;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            if (mActivity.mJson == null) {
                try {
                    final Response resp = mActivity.mGapi.repo.list(mActivity.mTarget);
                    if (resp.statusCode != 200) {
                        /* Oh noez, something went wrong */
                        return null;
                    }
                    mActivity.mJson = (new JSONObject(resp.resp)).getJSONArray("repositories");
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
            }
            mActivity.mAdapter = new RepositoriesListAdapter(mActivity.getApplicationContext(),
                    mActivity.mJson);
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            mActivity.mListView.setAdapter(mActivity.mAdapter);
            mActivity.mListView.setOnItemClickListener(mActivity.onListItemClick);
            mActivity.mListView.removeHeaderView(mActivity.mLoadingItem);
        }

        @Override
        protected void onPreExecute() {
            mActivity.mListView.addHeaderView(mActivity.mLoadingItem);
            mActivity.mListView.setAdapter(null);
        }
    }

    private RepositoriesListAdapter mAdapter;

    private final GitHubAPI mGapi = new GitHubAPI();

    private JSONArray mJson;

    private ListView mListView;

    public View mLoadingItem;

    private String mTarget;

    private MyReposTask mTask;

    private final OnItemClickListener onListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                final long id) {
            Intent i = new Intent(getApplicationContext(), Repository.class);
            i.putExtra("repo_owner", mTarget);
            try {
                i.putExtra("repo_name", mJson.getJSONObject(position).getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            startActivity(i);
            return;
        }
    };

    private String mUsername;

    private String mPassword;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);

        final SharedPreferences prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mUsername = prefs.getString("username", "");
        mPassword = prefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        mListView = (ListView) getLayoutInflater().inflate(R.layout.tab_listview, null);
        setContentView(mListView);

        mLoadingItem = getLayoutInflater().inflate(R.layout.loading_listitem, null);
        ((TextView) mLoadingItem.findViewById(R.id.tv_loadingListItem_loadingText))
                .setText("Loading...");

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTarget = extras.getString("target");
        }
        if ((mTarget == null) || mTarget.equals("")) {
            mTarget = mUsername;
        }

        mTask = (MyReposTask) getLastNonConfigurationInstance();
        if ((mTask == null) || (mTask.getStatus() == AsyncTask.Status.FINISHED)) {
            mTask = new MyReposTask(MyRepos.this);
        }
        mTask.mActivity = this;
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
            mAdapter = new RepositoriesListAdapter(getApplicationContext(), mJson);
        } else {
            mAdapter = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mListView.setAdapter(mAdapter);
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

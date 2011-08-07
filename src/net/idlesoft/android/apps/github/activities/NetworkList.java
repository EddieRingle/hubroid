/**
 * Hubroid - A GitHub app for Android
 *
 * Copyright (c) 2011 Eddie Ringle.
 *
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.ForkListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class NetworkList extends BaseActivity {
    private static class GetForksTask extends AsyncTask<Void, Void, Void> {
        public NetworkList activity;

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                activity.mJson = new JSONObject(activity.mGApi.repo.network(
                        activity.mRepositoryOwner, activity.mRepositoryName).resp)
                        .getJSONArray("network");
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            if (activity.mJson != null) {
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

    public ForkListAdapter mAdapter;

    private GetForksTask mGetForksTask;

    public JSONArray mJson;

    private ListView mListView;

    private final OnItemClickListener mOnForkListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View v, final int position,
                final long id) {
            try {
                final Intent i = new Intent(NetworkList.this, Repository.class);
                i.putExtra("repo_name", mJson.getJSONObject(position).getString("name"));
                i.putExtra("repo_owner", mJson.getJSONObject(position).getString("owner"));
                startActivity(i);
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public String mRepositoryName;

    public String mRepositoryOwner;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle, R.layout.network);

        setupActionBar();

        getActionBar().setTitle("Network");

        mListView = (ListView) findViewById(R.id.lv_network_list);
        mListView.setOnItemClickListener(mOnForkListItemClick);

        mAdapter = new ForkListAdapter(NetworkList.this, mListView);
        mListView.setAdapter(mAdapter);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("username");
        }
    }

    @Override
    protected void onResume() {
        mGetForksTask = (GetForksTask) getLastNonConfigurationInstance();
        if (mGetForksTask == null) {
            mGetForksTask = new GetForksTask();
        }
        mGetForksTask.activity = NetworkList.this;
        if (mGetForksTask.getStatus() == AsyncTask.Status.PENDING) {
            mGetForksTask.execute();
        }
        super.onResume();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mGetForksTask;
    }
}

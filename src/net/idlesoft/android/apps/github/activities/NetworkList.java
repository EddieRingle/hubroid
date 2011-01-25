/**
 * Hubroid - A GitHub app for Android
 * 
 * Copyright (c) 2011 Idlesoft LLC.
 * 
 * Licensed under the New BSD License.
 */

package net.idlesoft.android.apps.github.activities;

import com.flurry.android.FlurryAgent;

import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.adapters.ForkListAdapter;

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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class NetworkList extends Activity {
    private GitHubAPI mGapi = new GitHubAPI();

    public ForkListAdapter mAdapter;

    public JSONArray mJson;

    private static class GetForksTask extends AsyncTask<Void, Void, Void> {
        public NetworkList activity;

        protected void onPreExecute() {
            activity.mAdapter.setIsLoadingData(true);
        }

        protected Void doInBackground(Void... params) {
            try {
                activity.mJson = new JSONObject(activity.mGapi.repo.network(activity.mRepositoryOwner,
                        activity.mRepositoryName).resp).getJSONArray("network");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            activity.mAdapter.loadData(activity.mJson);
            return null;
        }

        protected void onPostExecute(Void result) {
            activity.mAdapter.pushData();
            activity.mAdapter.setIsLoadingData(false);
        }
    }

    private final OnItemClickListener mOnForkListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View v, final int position,
                final long id) {
            try {
                Intent i = new Intent(NetworkList.this, Repository.class);
                i.putExtra("repo_name", mJson.getJSONObject(position).getString(
                        "name"));
                i.putExtra("repo_owner", mJson.getJSONObject(position).getString(
                        "owner"));
                startActivity(i);
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private SharedPreferences mPrefs;

    public String mRepositoryName;

    public String mRepositoryOwner;

    private String mPassword;

    private String mUsername;

    private ListView mListView;

    private GetForksTask mGetForksTask;

    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.network);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        mListView = (ListView) findViewById(R.id.lv_network_list);
        mListView.setOnItemClickListener(mOnForkListItemClick);

        mAdapter = new ForkListAdapter(NetworkList.this, mListView);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("username");

            final TextView title = (TextView) findViewById(R.id.tv_page_title);
            title.setText("Network");
        }
    }

    protected void onResume() {
        mGetForksTask = (GetForksTask) getLastNonConfigurationInstance();
        if (mGetForksTask == null) {
            mGetForksTask = new GetForksTask();
        }
        mGetForksTask.activity = NetworkList.this;
        if (mGetForksTask.getStatus() == AsyncTask.Status.PENDING) {
            mGetForksTask.execute();
        }
        mListView.setAdapter(mAdapter);
        super.onResume();
    }

    public Object onRetainNonConfigurationInstance() {
        return mGetForksTask;
    }

    public void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, "K8C93KDB2HH3ANRDQH1Z");
    }

    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }
}

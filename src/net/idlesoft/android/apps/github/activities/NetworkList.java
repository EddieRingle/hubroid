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
import net.idlesoft.android.apps.github.adapters.ForkListAdapter;

import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class NetworkList extends Activity {
    private static class GetForksTask extends AsyncTask<Void, Void, Void> {
        public NetworkList activity;

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                activity.mJson = new JSONObject(activity.mGapi.repo.network(
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

    private final GitHubAPI mGapi = new GitHubAPI();

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

    private String mPassword;

    private SharedPreferences mPrefs;

    public String mRepositoryName;

    public String mRepositoryOwner;

    private String mUsername;

    private Editor mEditor;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.network);

        mPrefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mEditor = mPrefs.edit();

        mUsername = mPrefs.getString("username", "");
        mPassword = mPrefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        HubroidApplication.setupActionBar(NetworkList.this);

        mListView = (ListView) findViewById(R.id.lv_network_list);
        mListView.setOnItemClickListener(mOnForkListItemClick);

        mAdapter = new ForkListAdapter(NetworkList.this, mListView);
        mListView.setAdapter(mAdapter);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryName = extras.getString("repo_name");
            mRepositoryOwner = extras.getString("username");

            final TextView title = (TextView) findViewById(R.id.tv_page_title);
            title.setText("Network");
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
}

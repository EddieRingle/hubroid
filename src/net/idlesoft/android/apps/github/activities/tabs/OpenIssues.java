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
import net.idlesoft.android.apps.github.activities.SingleIssue;
import net.idlesoft.android.apps.github.adapters.IssuesListAdapter;

import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.idlesoft.libraries.ghapi.APIAbstract.Response;
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
import android.widget.AdapterView.OnItemClickListener;

public class OpenIssues extends Activity {
    private static class OpenIssuesTask extends AsyncTask<Void, Void, Void> {
        public OpenIssues activity;

        @Override
        protected Void doInBackground(final Void... params) {
            if (activity.mJson == null) {
                try {
                    final Response resp = activity.mGapi.issues.list(activity.mRepositoryOwner,
                            activity.mRepositoryName, "open");
                    if (resp.statusCode != 200) {
                        /* Oh noez, something went wrong */
                        return null;
                    }
                    activity.mJson = (new JSONObject(resp.resp)).getJSONArray("issues");
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
            }
            activity.mAdapter.loadData(activity.mJson);
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

    private IssuesListAdapter mAdapter;

    private final GitHubAPI mGapi = new GitHubAPI();

    private JSONArray mJson;

    private ListView mListView;

    private final OnItemClickListener mOnListItemClick = new OnItemClickListener() {
        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                final long id) {
            final Intent i = new Intent(getApplicationContext(), SingleIssue.class);
            i.putExtra("repo_owner", mRepositoryOwner);
            i.putExtra("repo_name", mRepositoryName);
            try {
                i.putExtra("json", mJson.getJSONObject(position).toString());
            } catch (final JSONException e) {
                e.printStackTrace();
            }
            startActivityForResult(i, 1);
            return;
        }
    };

    private String mPassword;

    private String mRepositoryName;

    private String mRepositoryOwner;

    private OpenIssuesTask mTask;

    private String mUsername;

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == 1) {
            mAdapter.clear();
            mJson = null;
            mTask = new OpenIssuesTask();
            mTask.activity = OpenIssues.this;
            mTask.execute();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);

        final SharedPreferences prefs = getSharedPreferences(Hubroid.PREFS_NAME, 0);
        mUsername = prefs.getString("username", "");
        mPassword = prefs.getString("password", "");

        mGapi.authenticate(mUsername, mPassword);

        mListView = (ListView) getLayoutInflater().inflate(R.layout.tab_listview, null);
        mListView.setOnItemClickListener(mOnListItemClick);

        setContentView(mListView);

        mAdapter = new IssuesListAdapter(OpenIssues.this, mListView);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRepositoryOwner = extras.getString("repo_owner");
            mRepositoryName = extras.getString("repo_name");
        }

        mTask = (OpenIssuesTask) getLastNonConfigurationInstance();
        if ((mTask == null) || (mTask.getStatus() == AsyncTask.Status.FINISHED)) {
            mTask = new OpenIssuesTask();
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
